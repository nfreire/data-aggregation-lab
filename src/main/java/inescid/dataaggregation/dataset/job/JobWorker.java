package inescid.dataaggregation.dataset.job;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import eu.europeana.research.iiif.crawl.ManifestSeeAlsoHarvester;
import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker;
import eu.europeana.research.iiif.profile.SeeAlsoProfile;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.DatasetProfile;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.IiifDataset;
import inescid.dataaggregation.dataset.detection.DataProfileDetector;
import inescid.dataaggregation.dataset.detection.DataTypeResult;
import inescid.dataaggregation.dataset.observer.JobObserverList;

public abstract class JobWorker extends JobObserverList implements Runnable{
	private Exception failureCause;
	private boolean running=false;
	private boolean successful=false;
	Dataset dataset;

	public Thread start() {
		Thread thread = new Thread(this);
		thread.start();
		running=true;
		return thread;
	}


	public Exception getFailureCause() {
		return failureCause;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isSuccessful() {
		return successful;
	}
	
	public boolean isFaiure() {
		return failureCause!=null;
	}

	public void setDataset(Dataset dataset) {
		this.dataset=dataset;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void addObserver(JobObserver obs) {
		observers.add(obs);
	}
	
	public void finishedSuccsessfuly() {
		successful=true;
		super.finishedSuccsessfuly();
		running = false;
	}
	
	public void finishedWithFailure(Exception failureCause) {
		this.failureCause=failureCause;
		running = false;
		super.finishedWithFailure(failureCause);
	}
	
	@Override
	public final void run() {
		try {
			started();
			runJob();
			finishedSuccsessfuly();
		} catch (Exception e) {
			finishedWithFailure(e);
		}
		
	}


	abstract protected void runJob() throws Exception ;
}