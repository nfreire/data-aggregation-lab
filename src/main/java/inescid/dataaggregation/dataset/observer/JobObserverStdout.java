package inescid.dataaggregation.dataset.observer;

import inescid.dataaggregation.dataset.job.JobObserver;

public class JobObserverStdout implements JobObserver {
	boolean printSuccesses=false;

	public JobObserverStdout() {
	}
	
	public JobObserverStdout(boolean printSuccesses) {
		this.printSuccesses = printSuccesses;
	}

	
	@Override
	public void started() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void signalResourceFailure(String uri, Exception resourceException) {
		System.out.println("RESOURCE FAILURE: "+uri);
		resourceException.printStackTrace(System.out);
	}

	@Override
	public void signalResourceSuccess(String uri) {
		if(printSuccesses)
			System.out.println("RESOURCE SUCCESS: "+uri);
	}

	@Override
	public void finishedWithFailure(Exception failureCause) {
		System.out.println("JOB FAILURE:");
		failureCause.printStackTrace(System.out);
	}

	@Override
	public void finishedSuccsessfuly() {
		System.out.println("JOB SUCCESS");
	}
	
	

}
