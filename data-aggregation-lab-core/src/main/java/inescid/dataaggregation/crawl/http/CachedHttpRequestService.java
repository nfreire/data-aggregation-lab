package inescid.dataaggregation.crawl.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;

import inescid.dataaggregation.dataset.GlobalCore;
import inescid.dataaggregation.store.Repository;
import inescid.util.AccessException;
import inescid.util.HttpUtil;
import inescid.util.RdfUtil;
import inescid.util.RetryExec;

public class CachedHttpRequestService {
	public static class HttpResponse {
		public HttpResponse(byte[] body, List<Entry<String, String>> headers, int status) {
			super();
			this.body = body;
			this.headers = headers;
			this.status = status;
		}
		public byte[] body; 
		public List<Entry<String, String>> headers;
		public int status=-1;
		public String getHeader(String headerToGet) {
			for (Entry<String, String> h : headers) {
				if(h.getKey().equalsIgnoreCase(headerToGet))
					return h.getValue();
			}
			return null;
		}
		public boolean isSuccess() {
			return status==200 && hasBody();
		}
		public boolean hasBody() {
			return body!=null && body.length>0;
		}
		public String getContentType() {
			return getHeader("Content-Type");
		}
		public AccessException throwException(String resourceUrl) {
			String msg=null;
			if (hasBody()) {
				try {
					msg=new String(body, "UTF8");
					if(msg.length()>300)
						msg=msg.substring(0, 300);
				} catch (UnsupportedEncodingException e) { msg=null; }
			}
			return new AccessException(resourceUrl, status, msg);
		}
	}
	
	private static final String DATASET_ID="http-cache";
	private HttpRequestService httpService;
	private Repository cache;
	private int retryAttempts=0;
	
	public CachedHttpRequestService() {
		super();
		this.httpService = GlobalCore.getHttpRequestService();
		this.cache = GlobalCore.getDataRepository();		
	}
	
	
	public CachedHttpRequestService(HttpRequestService httpService, Repository cache) {
		super();
		this.httpService = httpService;
		this.cache = cache;
	}
	
	public HttpResponse fetch(String resourceUrl, String oneHheader, String headerValue) throws AccessException, InterruptedException, IOException {
		UrlRequest r=new UrlRequest(resourceUrl, oneHheader, headerValue);
		return fetch(new HttpRequest(r));
	}
	public HttpResponse fetch(String resourceUrl) throws AccessException, InterruptedException, IOException {
		return fetch(new HttpRequest(resourceUrl));
	}
		
	public HttpResponse fetchRdf(String resourceUri) throws AccessException, InterruptedException, IOException {
		if(StringUtils.isEmpty(resourceUri))
			throw new AccessException("parameter resource URI is null");
		return fetch(resourceUri, "Accept", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
	}
	
	@SuppressWarnings("unchecked")
	public HttpResponse fetch(HttpRequest req) throws InterruptedException, IOException {
		try {
			HttpResponse resourceFetched = new RetryExec<HttpResponse>(retryAttempts) {
				@Override
				protected HttpResponse doRun() throws Exception{
					HttpResponse ret=null;  
					String url = req.getUrl();
					if (cache.contains(DATASET_ID, url)) {
						ret=new HttpResponse(cache.getContent(DATASET_ID, url), cache.getMeta(DATASET_ID, url), 200);
					} else {
						httpService.fetch(req);
						int resStatusCode = req.getResponseStatusCode();
						if (resStatusCode != 200 && resStatusCode!=304/* not modified*/) {
							if (resStatusCode >= 300 && resStatusCode<400) {
								String location = req.getResponseHeader("Location");
								if(location!=null) {
									req.getUrlRequest().setUrl(location);
									return doRun();
								}
							}else if( resStatusCode==429) {
								String retryAfter = req.getResponseHeader("Retry-After");
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
						List<Entry<String, String>> meta=new ArrayList<>(5);
						for(String headerName: new String[] { "Content-Type", "Content-Encoding", "Content-Disposition"/*, "Content-MD5"*/}) {
							for(Header h : req.getResponseHeaders(headerName))
								meta.add(new SimpleEntry<String, String>(h.getName(), h.getValue()));
						}
						ret=new HttpResponse(req.getContent().asBytes() ,meta, req.getResponseStatusCode());
						if(req.getResponseStatusCode()==200) 
							cache.save(DATASET_ID, url, ret.body, ret.headers);
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
