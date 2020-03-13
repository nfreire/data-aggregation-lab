package inescid.dataaggregation.crawl.http;

import java.util.HashSet;
import java.util.concurrent.Semaphore;

public class TaskSyncManager {
	Semaphore httpFetchSemaphore=new Semaphore(5);
	HashSet<Thread> holdingThreads=new HashSet<Thread>(5);
	
	public void acquireHttpFetch() throws InterruptedException {
		if(holdingThreads.contains(Thread.currentThread()))
			return;
		httpFetchSemaphore.acquire();
		holdingThreads.add(Thread.currentThread());
	}
	public void releaseHttpFetch() throws InterruptedException {
		holdingThreads.remove(Thread.currentThread());
		httpFetchSemaphore.release();
	}
}
