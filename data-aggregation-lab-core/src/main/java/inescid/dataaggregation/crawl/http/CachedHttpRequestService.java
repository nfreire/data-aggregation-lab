package inescid.dataaggregation.crawl.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;

import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.store.Repository;
import inescid.util.AccessException;
import inescid.util.RdfUtil;
import inescid.util.RetryExec;

public class CachedHttpRequestService {
	private static final String DATASET_ID="http-cache";
	private HttpRequestService httpService;
	private Repository cache;
	private int retryAttempts=0;
	
	public CachedHttpRequestService() {
		super();
		this.httpService = Global.getHttpRequestService();
		this.cache = Global.getDataRepository();		
	}
	
	
	public CachedHttpRequestService(HttpRequestService httpService, Repository cache) {
		super();
		this.httpService = httpService;
		this.cache = cache;
	}
	
	public boolean isFollowRedirects() {
		return httpService.isFollowRedirects();
	}
	public HttpResponse fetch(String resourceUrl, String oneHheader, String headerValue) throws AccessException, InterruptedException, IOException {
		UrlRequest r=new UrlRequest(resourceUrl, oneHheader, headerValue);
		return fetch(new HttpRequest(r), false);
	}
	public HttpResponse fetch(String resourceUrl) throws AccessException, InterruptedException, IOException {
		return fetch(new HttpRequest(resourceUrl), false);
	}
		
	public HttpResponse fetchRdf(String resourceUri) throws AccessException, InterruptedException, IOException {
		if(StringUtils.isEmpty(resourceUri))
			throw new AccessException("parameter resource URI is null");
		return fetch(resourceUri, "Accept", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
	}
	
	@SuppressWarnings("unchecked")
	public HttpResponse fetch(HttpRequest req, boolean streamed) throws InterruptedException, IOException {
		try {
			HttpResponse resourceFetched = new RetryExec<HttpResponse, Exception>(retryAttempts) {
				@Override
				protected HttpResponse doRun() throws Exception{
					HttpResponse ret=null;  
					String url = req.getUrl();
					if (cache.contains(DATASET_ID, url)) {
						if (streamed)
							ret=new HttpResponse(cache.getContentStream(DATASET_ID, url), cache.getMeta(DATASET_ID, url), 200);
						else
							ret=new HttpResponse(cache.getContent(DATASET_ID, url), cache.getMeta(DATASET_ID, url), 200);
					} else {
						CloseableHttpResponse fetched = httpService.fetchWithoutCache(req);
						int resStatusCode = fetched.getStatusLine().getStatusCode();
						if (resStatusCode != 200 && resStatusCode!=304/* not modified*/) {
							if (resStatusCode >= 300 && resStatusCode<400) {
								String location = null;
								List<Header> meta=new ArrayList<>(5);
								for(Header h : fetched.getHeaders("Location")) {
									location=h.getValue();
									break;
								}
								if(location!=null) {
									req.getUrlRequest().setUrl(location);
									return doRun();
								}
							}else if( resStatusCode==429) {
								String retryAfter = null;
								List<Header> meta=new ArrayList<>(5);
								for(Header h : fetched.getHeaders("Retry-After")) {
									retryAfter=h.getValue();
									break;
								}
								if(retryAfter!=null) {
									try {
										Thread.sleep(Long.parseLong(retryAfter));
										return doRun();						
									} catch (NumberFormatException e) {
										//ignore and proceed to sleep
									}
								}
								Thread.sleep(1000);
								return doRun();						
							}
						}
						ret=new HttpResponse(fetched, streamed);
						if(ret.getStatus()==200) {
							cache.save(DATASET_ID, url, ret.getBodyStream(), ret.getHeaders());
							cache.saveMeta(DATASET_ID, url, ret.getHeaders());
						}
					}
					return ret;
				}
			}.run();
			return resourceFetched;
		} catch (IOException | InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}	
	}


	public boolean contains(String uri) {
		return cache.contains(DATASET_ID, uri);
	}

	public void setRequestRetryAttempts(int attempts) {
		this.retryAttempts = attempts;
	}


	public void remove(String uri) {
		cache.remove(DATASET_ID, uri);
	}
	
}
