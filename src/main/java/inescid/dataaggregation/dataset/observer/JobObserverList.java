package inescid.dataaggregation.dataset.observer;

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
import inescid.dataaggregation.dataset.job.JobObserver;

public class JobObserverList implements JobObserver {
	protected List<JobObserver> observers=new ArrayList<>();

	public void addObserver(JobObserver obs) {
		observers.add(obs);
	}
	
	public void finishedSuccsessfuly() {
		for(JobObserver o: observers)
			o.finishedSuccsessfuly();
	}
	
	public void finishedWithFailure(Exception failureCause) {
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