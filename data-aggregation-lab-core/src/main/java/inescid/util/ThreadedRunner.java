package inescid.util;

import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadedRunner {
	Semaphore semaphore;
	ExecutorService threadPool;
//	Vector<Thread> threads;
	
	
	public ThreadedRunner(int numberOfThreads) {
		semaphore=new Semaphore(numberOfThreads);
		threadPool = Executors.newFixedThreadPool(numberOfThreads);
//		threads=new Vector<Thread>();
//		for(int i=0 ; i<numberOfThreads ; i++)
//			threads.add(new Thread());
	}
	
	public void run(Runnable task) throws InterruptedException, ExecutionException {
//		Future<?> f = 
		threadPool.submit(task).get();
	}
	
	public void shutdown() throws InterruptedException{
		threadPool.shutdown();
		if(!threadPool.awaitTermination(5, TimeUnit.MINUTES)){
			threadPool.shutdownNow();
		}
	}
	public void shutdownNow(){
		threadPool.shutdownNow();
	}
}
