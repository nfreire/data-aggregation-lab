package inescid.util;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.client.fluent.Content;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;

import inescid.dataaggregation.crawl.http.HttpRequest;
import inescid.dataaggregation.crawl.http.UrlRequest;
import inescid.dataaggregation.dataset.Global;

public class HttpUtil {
	public static final boolean ALWAYS_FOLLOW_REDIRECTS=true;
	public static final int RETRIES_ON_IOEXCEPTION=3;
	public static final int RETRIES_ON_HTTP_STATUS=1;
	public static final int RETRY_SLEEP_MILISECONDS=1;

	public static List<Header> getAndStoreWithHeaders(String resourceUri, File storeFile) throws IOException, InterruptedException, HttpRequestException {
		HttpRequest resourceRequest = makeRequest(resourceUri);
		FileUtils.writeByteArrayToFile(storeFile, resourceRequest.getContent().asBytes(), false);		
		List<Header> meta=new ArrayList<>(5);
		for(String headerName: new String[] { "Content-Type", "Content-Encoding", "Content-Disposition"/*, "Content-MD5"*/}) {
			for(Header h : resourceRequest.getResponse().getHeaders(headerName))
				meta.add(h);
		}
		return meta;
	}
	
	public static byte[] getAndStore(String resourceUri, File storeFile) throws IOException, InterruptedException, HttpRequestException {
		HttpRequest resourceRequest = makeRequest(resourceUri);
		byte[] asBytes = resourceRequest.getContent().asBytes();
		FileUtils.writeByteArrayToFile(storeFile, asBytes, false);		
		return asBytes;
	}
	
//	public static HttpRequest getResponse(String resourceUri) throws HttpRequestException, InterruptedException, IOException {
//		UrlRequest ldReq=new UrlRequest(resourceUri);
//		HttpRequest rdfResourceRequest = new HttpRequest(ldReq);
//		Global.getHttpRequestService().fetch(rdfResourceRequest);
//		return rdfResourceRequest;
//	}
	
	public static HttpRequest makeRequest(String resourceUri) throws HttpRequestException, InterruptedException {
		UrlRequest ldReq=new UrlRequest(resourceUri);
		return makeRequest(ldReq);
	}
	private static HttpRequest makeRequest(UrlRequest ldReq) throws HttpRequestException, InterruptedException {
		HttpRequest resourceRequest = new HttpRequest(ldReq);
		int tries = 0;
		while (true) {
			try {
				tries++;
				Global.getHttpRequestService().fetch(resourceRequest);
				int resStatusCode = resourceRequest.getResponseStatusCode();
				if (resStatusCode != 200 && resStatusCode!=304/* not modified*/) {
					if (resStatusCode >= 300 && resStatusCode<400) {
						String location = resourceRequest.getResponseHeader("Location");
						if(location!=null) {
							ldReq.setUrl(location);
							return makeRequest(ldReq);
						}
					}
					if (tries > RETRIES_ON_HTTP_STATUS)
						throw new HttpRequestException(ldReq.getUrl(), resStatusCode);
				}
				return resourceRequest;
			} catch (IOException ex) {
				if (tries > RETRIES_ON_IOEXCEPTION)
					throw new HttpRequestException(ldReq.getUrl(), ex);
			}
		}
	}
	
	public static HttpRequest makeRequest(String resourceUri, String acceptHeader) throws HttpRequestException, InterruptedException {
		return makeRequest(resourceUri, "Accept", acceptHeader);
	}
	
	public static HttpRequest makeRequest(String resourceUri, String oneHheader, String headerValue) throws HttpRequestException, InterruptedException {
		UrlRequest r=new UrlRequest(resourceUri, oneHheader, headerValue);
		return makeRequest(r);
	}
	
	public static String makeRequestForContent(String url) throws IOException, HttpRequestException, InterruptedException {
		HttpRequest reqRes = makeRequest(url);
		int statusCode = reqRes.getResponse().getStatusLine().getStatusCode();
		if(statusCode==200) 
			return reqRes.getContent().asString();				
		return null;
	}
	
	public static List<Entry<String, String>> convertHeaderStruct(List<Header> headers) {
		List<Entry<String, String>> meta=new ArrayList<>(headers.size());
		for(Header h : headers)
			meta.add(new AbstractMap.SimpleEntry<String, String>(h.getName(), h.getValue()));
		return meta;
	}
}
