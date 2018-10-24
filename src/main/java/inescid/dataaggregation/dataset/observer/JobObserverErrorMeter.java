package inescid.dataaggregation.dataset.observer;

import inescid.dataaggregation.dataset.job.JobObserver;
import inescid.util.MapOfInts;

public class JobObserverErrorMeter implements JobObserver {
	boolean processAccessExceptions=false;
	
	MapOfInts<Class> errorsByExceptionClass=new MapOfInts<>();
	int successes=0;

	public JobObserverErrorMeter() {
	}
	
	public JobObserverErrorMeter(boolean processAccessExceptions) {
		this.processAccessExceptions = processAccessExceptions;
	}

	@Override
	public void signalResourceFailure(String uri, Exception resourceException) {
		errorsByExceptionClass.addTo(resourceException.getClass(), null);
	}

	@Override
	public void signalResourceSuccess(String uri) {
		successes++;
	}

	@Override
	public void finishedWithFailure(Exception failureCause) {
	}

	@Override
	public void finishedSuccsessfuly() {
		System.out.println("JOB SUCCESS");
	}
	
	

}
