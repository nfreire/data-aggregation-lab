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
import org.mortbay.log.Log;

import inescid.dataaggregation.crawl.http.HttpRequest;
import inescid.dataaggregation.crawl.http.UrlRequest;
import inescid.dataaggregation.dataset.Global;

public class HttpUtil {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(HttpUtil.class);
	
	public static final boolean ALWAYS_FOLLOW_REDIRECTS=true;
	public static final int RETRIES_ON_IOEXCEPTION=3;
	public static final int RETRIES_ON_HTTP_STATUS=1;
	public static final int RETRY_SLEEP_MILISECONDS=1;

	public static List<Header> getAndStoreWithHeaders(String resourceUri, File storeFile) throws IOException, InterruptedException, AccessException {
		return getAndStoreWithHeaders(new UrlRequest(resourceUri), storeFile);
	}
	public static List<Header> getAndStoreWithHeaders(UrlRequest resourceUri, File storeFile) throws IOException, InterruptedException, AccessException {
		HttpRequest resourceRequest = makeRequest(resourceUri);
		FileUtils.writeByteArrayToFile(storeFile, resourceRequest.getContent().asBytes(), false);		
		List<Header> meta=new ArrayList<>(5);
		for(String headerName: new String[] { "Content-Type", "Content-Encoding", "Content-Disposition"/*, "Content-MD5"*/}) {
			for(Header h : resourceRequest.getResponse().getHeaders(headerName))
				meta.add(h);
		}
		return meta;
	}
	
	public static byte[] getAndStore(String resourceUri, File storeFile) throws IOException, InterruptedException, AccessException {
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
	
	public static HttpRequest makeRequest(String resourceUri) throws AccessException, InterruptedException {
		UrlRequest ldReq=new UrlRequest(resourceUri);
		return makeRequest(ldReq);
	}
	private static HttpRequest makeRequest(UrlRequest ldReq) throws AccessException, InterruptedException {
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
						throw new AccessException(ldReq.getUrl(), resStatusCode);
				}
				return resourceRequest;
			} catch (IOException ex) {
				if (tries > RETRIES_ON_IOEXCEPTION)
					throw new AccessException(ldReq.getUrl(), ex);
			}
		}
	}
	
	public static HttpRequest makeRequest(String resourceUri, String acceptHeader) throws AccessException, InterruptedException {
		return makeRequest(resourceUri, "Accept", acceptHeader);
	}
	
	public static HttpRequest makeRequest(String resourceUri, String oneHheader, String headerValue) throws AccessException, InterruptedException {
		UrlRequest r=new UrlRequest(resourceUri, oneHheader, headerValue);
		return makeRequest(r);
	}
	
	public static String makeRequestForContent(String resourceUri, String oneHheader, String headerValue) throws AccessException, InterruptedException, IOException {
		UrlRequest r=new UrlRequest(resourceUri, oneHheader, headerValue);
		return makeRequestForContent(r);
	}
	
	public static String makeRequestForContent(String url) throws IOException, AccessException, InterruptedException {
		UrlRequest r=new UrlRequest(url);
		return makeRequestForContent(r);
	}
	
	private static String makeRequestForContent(UrlRequest url) throws IOException, AccessException, InterruptedException {
		HttpRequest reqRes = makeRequest(url);
		int statusCode = reqRes.getResponse().getStatusLine().getStatusCode();
		if(statusCode==200) 
			return reqRes.getContent().asString();	
		else {
			log.info("HTTP error, content: "+url+"\n"+ reqRes.getContent().asString());			
			throw new AccessException(url.getUrl(), "HTTP response status code "+statusCode);
		}
	}
	
	public static List<Entry<String, String>> convertHeaderStruct(List<Header> headers) {
		List<Entry<String, String>> meta=new ArrayList<>(headers.size());
		for(Header h : headers)
			meta.add(new AbstractMap.SimpleEntry<String, String>(h.getName(), h.getValue()));
		return meta;
	}
}
