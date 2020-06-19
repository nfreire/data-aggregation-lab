package inescid.util;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.http.Header;

import inescid.dataaggregation.crawl.http.HttpRequest;
import inescid.dataaggregation.crawl.http.UrlRequest;
import inescid.dataaggregation.crawl.http.UrlRequest.HttpMethod;
import inescid.dataaggregation.dataset.Global;

public class HttpUtil {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(HttpUtil.class);
	
	public static final boolean ALWAYS_FOLLOW_REDIRECTS=true;
	public static final int RETRIES_ON_IOEXCEPTION=3;
	public static final int RETRIES_ON_HTTP_STATUS=1;
	public static final int RETRY_SLEEP_MILISECONDS=1;

	public static List<Entry<String, String>> getStoreAndReturnHeaders(String resourceUri, File storeFile) throws IOException, InterruptedException, AccessException {
		return getAndStoreWithHeaders(new UrlRequest(resourceUri), storeFile);
	}
	public static List<Entry<String, String>> getAndStoreWithHeaders(UrlRequest resourceUri, File storeFile) throws IOException, InterruptedException, AccessException {
		HttpRequest resourceRequest = makeRequest(resourceUri);
		FileUtils.writeByteArrayToFile(storeFile, resourceRequest.getResponseContent(), false);		
		return resourceRequest.getResponse().getHeaders();
	}
	
	public static byte[] getAndStore(String resourceUri, File storeFile) throws IOException, InterruptedException, AccessException {
		HttpRequest resourceRequest = makeRequest(resourceUri);
		byte[] asBytes = resourceRequest.getResponseContent();
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
	public static HttpRequest makeHeadRequest(String resourceUri) throws AccessException, InterruptedException {
		UrlRequest ldReq=new UrlRequest(resourceUri);
		ldReq.setMethod(HttpMethod.HEAD);
		return makeRequest(ldReq);
	}
	protected static HttpRequest makeRequest(UrlRequest ldReq) throws AccessException, InterruptedException {
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
					}else if( resStatusCode==429) {
						String retryAfter = resourceRequest.getResponseHeader("Retry-After");
						if(retryAfter!=null) {
							try {
								Thread.sleep(Long.parseLong(retryAfter));
								return makeRequest(ldReq);						
							} catch (NumberFormatException e) {
								//ignore and proceed to sleep
							}
						}
						Thread.sleep(1000);
						return makeRequest(ldReq);
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
		int statusCode = reqRes.getResponse().getStatus();
		if(statusCode==200) 
			return reqRes.getResponseContentAsString();	
		else {
			log.info("HTTP error, content: "+url+"\n"+ reqRes.getResponseContentAsString());			
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
