package inescid.dataaggregation.dataset.observer;

import java.util.ArrayList;
import java.util.List;

import inescid.dataaggregation.dataset.job.JobObserver;

public class JobObserverList implements JobObserver {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
			.getLogger(JobObserverList.class);
	
	protected List<JobObserver> observers=new ArrayList<>();

	public void addObserver(JobObserver obs) {
		observers.add(obs);
	}
	
	public void finishedSuccsessfuly() {
		for(JobObserver o: observers)
			o.finishedSuccsessfuly();
	}
	
	public void started() {
		for(JobObserver o: observers)
			o.started();
	}
	
	public void finishedWithFailure(Exception failureCause) {
		log.error("Job failed ", failureCause);
		for(JobObserver o: observers)
			o.finishedWithFailure(failureCause);
	}

	@Override
	public void signalResourceFailure(String uri, Exception resourceException) {
		for(JobObserver o: observers)
			o.signalResourceFailure(uri, resourceException);
		
	}

	@Override
	public void signalResourceSuccess(String uri) {
		for(JobObserver o: observers)
			o.signalResourceSuccess(uri);
	}
	
}