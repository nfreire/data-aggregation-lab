package inescid.dataaggregation.crawl.ld;

import java.io.IOException;
import java.util.AbstractMap.SimpleImmutableEntry;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpMessage;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;

public class HttpRequest {
	UrlRequest url;
//	String contentTypeToRequest;
//	CrawlingSession session;
//	Semaphore fetchingSemaphore=new Semaphore(1);
	CloseableHttpResponse response;
	Content content;
	Throwable error;

	public HttpRequest(String url) {
		super();
		this.url = new UrlRequest(url);
	}
	
	public HttpRequest(UrlRequest url) {
		super();
		this.url = url;
	}

//	public HttpRequest(UrlRequest url, CrawlingSession session) {
//		super();
//		this.url = url;
//		this.session = session;
//	}



//	public HttpRequest(UrlRequest url, CrawlingSession crawlingSession, Throwable error) {
//		this(url, crawlingSession);
//		this.error=error;
//	}

	public String getUrl() {
		return url.getUrl();
	}

//	public void setUrl(UrlRequest url) {
//		this.url = url;
//	}
//
//	public CrawlingSession getSession() {
//		return session;
//	}
//
//	public void setSession(CrawlingSession session) {
//		this.session = session;
//	}
	
	
	/**
	 * May be used by another thread to wait for the request to be processed
	 * @throws IOException 
	 * @throws UnsupportedOperationException 
	 * 
	 * @throws InterruptedException
	 */
//	public void waitForFetchReady() throws InterruptedException {
//		fetchingSemaphore.acquire();
//		fetchingSemaphore.release();
//	}
	
//	public void fetchReady() {
//		fetchingSemaphore.release();
//	}

	public CloseableHttpResponse getResponse() {
		return response;
	}

	public void setResponse(CloseableHttpResponse response) throws UnsupportedOperationException, IOException {
		this.response = response;
		if(response.getEntity()!=null) {
			byte[] byteArray = IOUtils.toByteArray(response.getEntity().getContent());
			ContentType contentType = ContentType.get(response.getEntity());
			this.content=new Content(byteArray, contentType);
		}
		response.close();
	}
		
//		fetchingSemaphore.release();

	public int getResponseStatusCode() {
		return response.getStatusLine().getStatusCode();
	}
	public Content getContent() throws IOException {
//		byte[] byteArray = IOUtils.toByteArray(response.getEntity().getContent());
//		ContentType contentType = ContentType.get(response.getEntity());
//		response.close();
//		return new Content(byteArray, contentType);
		return content;
	}

//	public String getContentTypeToRequest() {
//		return contentTypeToRequest;
//	}
//
//	public void setContentTypeToRequest(String contentTypeToRequest) {
//		this.contentTypeToRequest = contentTypeToRequest;
//	}

	public void addHeaders(HttpMessage request) {
		for(SimpleImmutableEntry<String, String> header : url.getHeaders()) {
			request.addHeader(header.getKey(), header.getValue());
		}
	}


	
}
