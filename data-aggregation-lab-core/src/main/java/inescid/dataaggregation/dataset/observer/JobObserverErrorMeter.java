package inescid.dataaggregation.dataset.observer;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.job.JobObserver;
import inescid.util.datastruct.MapOfInts;

public class JobObserverErrorMeter implements JobObserver {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
			.getLogger(JobObserverErrorMeter.class);
	
	boolean processAccessExceptions=false;
	
	MapOfInts<Class> errorsByExceptionClass=new MapOfInts<>();
	int successes=0;
	
	File reportFile;

	public JobObserverErrorMeter() {
	}
	
	public JobObserverErrorMeter(boolean processAccessExceptions) {
		this.processAccessExceptions = processAccessExceptions;
	}

	public JobObserverErrorMeter(File reportFile) {
		this.reportFile = reportFile;
	}

	@Override
	public void started() {
	}
	
	@Override
	public void signalResourceFailure(String uri, Exception resourceException) {
		errorsByExceptionClass.incrementTo(resourceException.getClass());
	}

	@Override
	public void signalResourceSuccess(String uri) {
		successes++;
	}

	@Override
	public void finishedWithFailure(Exception failureCause) {
			writeReport();
	}

	@Override
	public void finishedSuccsessfuly() {
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
//		MapOfInts<Class> errorsByExceptionClass=new MapOfInts<>();
//		int successes=0;
		int totalErrors = errorsByExceptionClass.total();
		double total=successes+totalErrors;
		if(total==0)
			return "No resources were harvested.";
		
		StringBuilder sb=new StringBuilder();
		sb.append(String.format("Total successful requests: %d (%.1f%%)\n", successes, (double)successes/total*100));
		sb.append(String.format("Total failed requests: %d (%.1f%%)\n", totalErrors, (double)totalErrors/total*100));
		return sb.toString();
	}

}
