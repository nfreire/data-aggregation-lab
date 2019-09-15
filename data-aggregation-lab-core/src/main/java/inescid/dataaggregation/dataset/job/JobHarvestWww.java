package inescid.dataaggregation.dataset.job;

import java.io.File;
import java.util.Calendar;

import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker;
import inescid.dataaggregation.crawl.www.WwwDatasetHarvest;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.DatasetProfile;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.WwwDataset;
import inescid.dataaggregation.dataset.WwwDataset.Microformat;
import inescid.dataaggregation.dataset.detection.DataProfileDetector;
import inescid.dataaggregation.dataset.detection.DataTypeResult;
import inescid.dataaggregation.dataset.observer.JobObserverChronometer;
import inescid.dataaggregation.dataset.observer.JobObserverErrorMeter;
import inescid.dataaggregation.dataset.observer.JobObserverProgressLogger;

public class JobHarvestWww extends JobWorker {
	Integer sampleSize;

	public JobHarvestWww(Job job, Dataset dataset) {
		super(job, dataset);
	}
	
	public JobHarvestWww(Job job, Dataset dataset, int sampleSize) {
		super(job, dataset);
		this.sampleSize = sampleSize;
	}
	
	@Override
	public void runJob() throws Exception {
		File reportsFolder = Global.getPublicationRepository().getReportsFolder(dataset);
		JobObserverChronometer obsChono=new JobObserverChronometer(new File(reportsFolder, "LogHarvestingTime.txt"));
		JobObserverErrorMeter obsError=new JobObserverErrorMeter(new File(reportsFolder, "LogHarvestingErrors.txt"));
		JobObserverProgressLogger obsProgress=new JobObserverProgressLogger(new File(reportsFolder, "LogProgress.txt"),60*5, obsChono, obsError);
		addObserver(obsError);
		addObserver(obsChono);
		addObserver(obsProgress);
//		addObserver(new JobObserverStdout(true));
		started();
		
			TimestampTracker timestampTracker=Global.getTimestampTracker();
			WwwDataset wwwDataset=(WwwDataset)dataset;
			WwwDatasetHarvest harvest=new WwwDatasetHarvest(wwwDataset, Global.getDataRepository(), true/*skip existing*/, this);
			harvest.setSampleSize(sampleSize);
			Calendar startOfCrawl=harvest.startProcess();
			if(wwwDataset.getMicroformat()==Microformat.SCHEMAORG) 
				wwwDataset.setDataProfile(DatasetProfile.SCHEMA_ORG.toString());
			DataTypeResult detected = DataProfileDetector.detect(dataset.getUri(), Global.getDataRepository());
			if(detected!=null) {
				if(detected.format!=null && dataset.getDataFormat()==null) {
					dataset.setDataFormat(detected.format.toString());
				}
				if(detected.profile!=null && dataset.getDataProfile()==null) {
					dataset.setDataProfile(detected.profile.toString());
				}
			}
			if(harvest.getRunError()!=null) {
				timestampTracker.setDatasetLastError(wwwDataset.getUri(), startOfCrawl);
				timestampTracker.commit();
				throw  harvest.getRunError();
			}else {
				timestampTracker.setDatasetTimestamp(wwwDataset.getUri(), startOfCrawl);
				timestampTracker.commit();
			}
			
	}

}
