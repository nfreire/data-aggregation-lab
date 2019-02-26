package inescid.dataaggregation.crawl.http;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.Header;

import inescid.dataaggregation.dataset.GlobalCore;
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
		this.httpService = GlobalCore.getHttpRequestService();
		this.cache = GlobalCore.getDataRepository();		
	}
	
	
	public CachedHttpRequestService(HttpRequestService httpService, Repository cache) {
		super();
		this.httpService = httpService;
		this.cache = cache;
	}
	
	public SimpleEntry<byte[], List<Entry<String, String>>> fetch(String resourceUrl, String oneHheader, String headerValue) throws AccessException, InterruptedException, IOException {
		UrlRequest r=new UrlRequest(resourceUrl, oneHheader, headerValue);
		return fetch(new HttpRequest(r));
	}
	public SimpleEntry<byte[], List<Entry<String, String>>> fetch(String resourceUrl) throws AccessException, InterruptedException, IOException {
		return fetch(new HttpRequest(resourceUrl));
	}
		
	public SimpleEntry<byte[], List<Entry<String, String>>> fetchRdf(String resourceUri) throws AccessException, InterruptedException, IOException {
		return fetch(resourceUri, "Accept", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
	}
	
	@SuppressWarnings("unchecked")
	public SimpleEntry<byte[], List<Entry<String, String>>> fetch(HttpRequest req) throws InterruptedException, IOException {
		try {
			SimpleEntry<byte[], List<Entry<String, String>>> resourceFetched = new RetryExec<SimpleEntry<byte[], List<Entry<String, String>>>>(retryAttempts) {
				@Override
				protected SimpleEntry<byte[], List<Entry<String, String>>> doRun() throws Exception{
					SimpleEntry<byte[], List<Entry<String, String>>> ret=new SimpleEntry<>(null, null);  
					String url = req.getUrl();
					if (cache.contains(DATASET_ID, url)) {
						ret=new SimpleEntry<>(cache.getContent(DATASET_ID, url), cache.getMeta(DATASET_ID, url));
					} else {
						httpService.fetch(req);
						if(req.getResponseStatusCode()==200) {
							List<Entry<String, String>> meta=new ArrayList<>(5);
							for(String headerName: new String[] { "Content-Type", "Content-Encoding", "Content-Disposition"/*, "Content-MD5"*/}) {
								for(Header h : req.getResponseHeaders(headerName))
									meta.add(new SimpleEntry<String, String>(h.getName(), h.getValue()));
							}
							ret=new SimpleEntry<byte[], List<Entry<String, String>>>(req.getContent().asBytes() ,meta);
							cache.save(DATASET_ID, url, ret.getKey(), ret.getValue());
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
