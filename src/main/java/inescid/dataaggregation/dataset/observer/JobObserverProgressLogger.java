package inescid.dataaggregation.dataset.observer;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.job.JobObserver;

public class JobObserverProgressLogger implements JobObserver {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
			.getLogger(JobObserverProgressLogger.class);
	
	private static final String dateFormatSecs="EEE hh:mm:ss";
	private static final String dateFormatNoSecs="EEE hh:mm";
	
	File reportFile;
	int loggingIntervalInSeconds=60;
	JobObserver[] observersToLog;
	long lastLog=0;

	DateFormat logDateFormat;

	public JobObserverProgressLogger(File reportFile, JobObserver... observersToLog) {
		this(reportFile, 60, observersToLog);
	}
	public JobObserverProgressLogger(File reportFile, int loggingIntervalInSeconds, JobObserver... observersToLog) {
		this.reportFile = reportFile;
		this.observersToLog = observersToLog;
		if(!reportFile.getParentFile().exists())
			reportFile.getParentFile().mkdirs();
		else if(reportFile.exists())
			reportFile.delete();
		this.loggingIntervalInSeconds = loggingIntervalInSeconds;
		if(loggingIntervalInSeconds>60)
			logDateFormat=new SimpleDateFormat(dateFormatNoSecs);
		else
			logDateFormat=new SimpleDateFormat(dateFormatSecs);			
	}
	
	@Override
	public void started() {
		lastLog=new Date().getTime();
	}
	
	@Override
	public void signalResourceFailure(String uri, Exception resourceException) {
		checkIntervalAndLog();
		
	}

	@Override
	public void signalResourceSuccess(String uri) {
		checkIntervalAndLog();
	}

	@Override
	public void finishedWithFailure(Exception failureCause) {
		log();
	}

	@Override
	public void finishedSuccsessfuly() {
		log();
	}
	
	private void checkIntervalAndLog() {
		Date now = new Date();
		long secsElapsed=(now.getTime() - lastLog)/1000;
		if(secsElapsed>=loggingIntervalInSeconds) 
			log();
	}
	private void log() {
		try {
			Date now = new Date();
			StringBuilder sb=new StringBuilder();
			sb.append(String.format("- %s -\n", logDateFormat.format(now)));
			for(JobObserver obs: observersToLog) {
				sb.append(String.format("-- %s --\n%s\n", obs.getClass().getSimpleName(), obs.toString()));
			}
			FileUtils.write(reportFile, sb.toString(), Global.UTF8, true);
			lastLog=now.getTime();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

}
