package inescid.dataaggregation.dataset.observer;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.job.JobObserver;
import inescid.util.StatisticCalcMean;

public class JobObserverChronometer implements JobObserver {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
			.getLogger(JobObserverChronometer.class);
	
	boolean measureFailures=false;
	
	StatisticCalcMean resourceRequests=new StatisticCalcMean();
	long jobStart;
	long jobTime=0;
	long resourceStart=-1;
	File reportFile;

	public JobObserverChronometer(File reportFile) {
		this.reportFile = reportFile;
	}
	
	public JobObserverChronometer(boolean measureFailures) {
		this.measureFailures = measureFailures;
	}

	@Override
	public void started() {
		jobStart=new Date().getTime();
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
		long time = new Date().getTime();
		if (resourceStart>0) 
			resourceRequests.enter(time-resourceStart);
		resourceStart=time;
	}

	@Override
	public void finishedWithFailure(Exception failureCause) {
		jobTime=new Date().getTime()-jobStart;
		writeReport();
	}

	@Override
	public void finishedSuccsessfuly() {
		jobTime=new Date().getTime()-jobStart;
		writeReport();
	}
	
	private void writeReport() {
		try {
			if(reportFile==null)
				return;
			if(!reportFile.getParentFile().exists())
				reportFile.getParentFile().mkdirs();
			FileUtils.write(reportFile, this.toString(), Global.UTF8);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public String toString() {
		long elapsedTime=jobTime;
		if(elapsedTime==0)
			elapsedTime=new Date().getTime()-jobStart;
		StringBuilder sb=new StringBuilder();
		sb.append(String.format("Total harvesting time: %d seconds\n", elapsedTime/1000));
		sb.append(String.format("Requests: %.0f (%.1f  requests/second)\n",
				resourceRequests.getCount()+1,
				elapsedTime > 1000 ? (double)(resourceRequests.getCount()+1)/((double)elapsedTime/1000) : 0f ));
		sb.append(String.format("Minimum request time: %.0f milliseconds\n",resourceRequests.getMin()));
		sb.append(String.format("Maximum request time: %.0f milliseconds\n",resourceRequests.getMax()));
		sb.append(String.format("Mean request time: %.0f miliseconds\n", resourceRequests.getMean()));
		return sb.toString();
	}
	
}
