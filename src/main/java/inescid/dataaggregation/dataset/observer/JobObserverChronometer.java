package inescid.dataaggregation.dataset.observer;

import java.util.Date;

import inescid.dataaggregation.dataset.job.JobObserver;
import inescid.util.MapOfInts;
import inescid.util.StatisticCalcMean;

public class JobObserverChronometer implements JobObserver {
	boolean measureFailures=false;
	
	StatisticCalcMean resourceRequests=new StatisticCalcMean();
	long jobStart;
	long jobTime;
	long resourceStart;

	public JobObserverChronometer() {
	}
	
	public JobObserverChronometer(boolean measureFailures) {
		this.measureFailures = measureFailures;
	}

	@Override
	public void signalResourceFailure(String uri, Exception resourceException) {
		long time = new Date().getTime();
		if (measureFailures && resourceStart>0) 
				resourceRequests.enter(time-resourceStart);
		resourceStart=time;
	}

	@Override
	public void signalResourceSuccess(String uri) {
	}

	@Override
	public void finishedWithFailure(Exception failureCause) {
		long time = new Date().getTime();
		jobTime=time-jobStart;
	}

	@Override
	public void finishedSuccsessfuly() {
		long time = new Date().getTime();
		jobTime=time-jobStart;
	}
	
	

}
