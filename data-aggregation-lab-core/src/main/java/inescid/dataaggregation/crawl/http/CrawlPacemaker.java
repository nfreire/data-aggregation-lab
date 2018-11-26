package inescid.dataaggregation.crawl.http;

import java.util.Date;

public class CrawlPacemaker {
	long delayInMs;
	long lastRequest=0;
	
	public CrawlPacemaker() {
		delayInMs=200;
	}
	
	public CrawlPacemaker(int delayInSec) {
		super();
		delayInMs=delayInSec*1000;
	}
	
	public synchronized void waitDelay() {
		long now=new Date().getTime();
		long interval = now-lastRequest;
		if(interval>delayInMs) {
			lastRequest=now;
		} else {
			try {
				Thread.sleep(delayInMs-interval);
			} catch (InterruptedException e) {
				// just exit
			}
			lastRequest=new Date().getTime();;
		}
	}
}
