package inescid.dataaggregation.crawl.ld;

import java.io.IOException;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.Semaphore;

import javax.sound.midi.Soundbank;

import org.apache.http.HttpHeaders;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import inescid.util.DevelopementSingleton;


public class HttpRequestService {
	CloseableHttpClient httpClient;
	CookieStore httpCookieStore;
	
//	ArrayList<FetchRequest> requestsQueue=new ArrayList<>(50);
	
	Vector<Long> requestTimeStats=null;
//	Vector<Long> requestTimeStats=new Vector<>();
	
	
	public void init() {
		if(DevelopementSingleton.DEVEL_TEST && DevelopementSingleton.HTTP_REQUEST_TIME_STATS)
			requestTimeStats=new Vector<>();
		
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(200);
		cm.setDefaultMaxPerRoute(41);

		httpCookieStore = new BasicCookieStore();

		httpClient = 
				HttpClients.custom()
				.setSSLHostnameVerifier(new NoopHostnameVerifier())
				.setConnectionManager(cm) 
				.setDefaultCookieStore(httpCookieStore)
//                .setRedirectStrategy(new LaxRedirectStrategy())
				.build();
	}	
	
	public void close() throws Exception {
		httpClient.close();
	}
	
	public void start() throws Exception {
		
	}
	public void stop() throws Exception {
		
	}

	public void fetch(HttpRequest url) throws InterruptedException, IOException {
		LdGlobals.taskSyncManager.acquireHttpFetch();
		try {
			Long startTime = requestTimeStats!=null ? System.nanoTime() : null;
			HttpGet request = new HttpGet(url.getUrl());
			url.addHeaders(request);
			CloseableHttpResponse response = httpClient.execute(request);
			
			
//			if(httpCookieStore.getCookies().size()>50) {
//				httpCookieStore.clearExpired(new Date());
//			}
			if(httpCookieStore.getCookies().size()>100) 
				httpCookieStore.clear();
//			System.out.println("cookies: "+httpCookieStore.getCookies().size());
//			System.out.println("cookies: "+httpCookieStore.getCookies());
			
			
			if(startTime!=null) {
				long duration=System.nanoTime() - startTime;
				synchronized (requestTimeStats) {
					requestTimeStats.add(duration);
					if(requestTimeStats.size() % 10 == 0) {
						long sum=0;
						for(int i=requestTimeStats.size()-1; i>=requestTimeStats.size()-10 ; i--)
							sum+=requestTimeStats.get(i);
						long recTime = sum/10;
						float recRate = 60000000000f / recTime;
						float recRateHour = recRate * 60 *60;
						System.out.println("10 Requests done at "+ recTime + "ns/rec - "+recRate+" recs/sec "+ recRateHour+" recs/hour");
					}
				}
			}
			url.setResponse(response);
		}finally {
			LdGlobals.taskSyncManager.releaseHttpFetch();
		}
	}
	

//	public String printStatus() {
//		return String.format("Http Fetcher Status: %d avail., %d queued [Priority: %d avail., %d queued]",fetchSemaphore.availablePermits(), fetchSemaphore.getQueueLength(),fetchWithPrioritySemaphore.availablePermits(), fetchWithPrioritySemaphore.getQueueLength()); 
//	}
	
}
