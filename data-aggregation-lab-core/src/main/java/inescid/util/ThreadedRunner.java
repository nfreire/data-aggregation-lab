package inescid.util;

import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadedRunner {
	ExecutorService threadPool;
	Semaphore submitionQueue;
	
	public ThreadedRunner(int numberOfThreads) {
		threadPool = Executors.newFixedThreadPool(numberOfThreads);
		submitionQueue=new Semaphore(numberOfThreads*10);
	}
	
	public void run(Runnable task) throws InterruptedException, ExecutionException {
		submitionQueue.acquire();
		threadPool.submit(task).get();
		submitionQueue.release();
	}
	
	public void awaitTermination(int minutesToWaitBeforeForcingTermination) throws InterruptedException{
		threadPool.shutdown();
		if(minutesToWaitBeforeForcingTermination>0) {
			if(!threadPool.awaitTermination(minutesToWaitBeforeForcingTermination, TimeUnit.MINUTES))
				threadPool.shutdownNow();
		} else {
			while (! threadPool.awaitTermination(30, TimeUnit.MINUTES)) {
			}
		}
	}
	public void shutdownNow(){
		threadPool.shutdownNow();
	}

}
