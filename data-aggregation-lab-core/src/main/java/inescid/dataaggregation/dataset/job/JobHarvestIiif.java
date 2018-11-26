package inescid.dataaggregation.dataset.job;

import java.util.Calendar;

import eu.europeana.research.iiif.crawl.CollectionCrawler;
import eu.europeana.research.iiif.crawl.ManifestHarvester;
import eu.europeana.research.iiif.discovery.ProcesssingAlgorithm;
import eu.europeana.research.iiif.discovery.demo.TimestampCrawlingHandler;
import eu.europeana.research.iiif.discovery.syncdb.InMemoryTimestampStore;
import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.GlobalCore;
import inescid.dataaggregation.dataset.IiifDataset;
import inescid.dataaggregation.dataset.IiifDataset.IiifCrawlMethod;
import inescid.dataaggregation.dataset.DatasetProfile;

public class JobHarvestIiif extends JobWorker implements Runnable {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
			.getLogger(JobHarvestIiif.class);
	
	Integer sampleSize;

	public JobHarvestIiif() {
	}
	
	public JobHarvestIiif(int sampleSize) {
		this.sampleSize = sampleSize;
	}

	@Override
	public void runJob() throws Exception {
			TimestampTracker timestampTracker=GlobalCore.getTimestampTracker();
			IiifDataset iiifDataset=(IiifDataset)dataset;
			Calendar startOfCrawl=null;
			try {
				if(iiifDataset.getCrawlMethod()==IiifCrawlMethod.COLLECTION) {
					CollectionCrawler harvester=new CollectionCrawler(timestampTracker, iiifDataset.getUri());
					harvester.setSampleSize(sampleSize);
					startOfCrawl = harvester.crawl(false);
				} else if(iiifDataset.getCrawlMethod()==IiifCrawlMethod.DISCOVERY) {
					ProcesssingAlgorithm iiifDiscovery = new ProcesssingAlgorithm(timestampTracker, new TimestampCrawlingHandler()); 
					iiifDiscovery.setSampleSize(sampleSize);
					startOfCrawl=iiifDiscovery.startProcess(iiifDataset.getUri(), false);
				}
				ManifestHarvester harvester=new ManifestHarvester(GlobalCore.getDataRepository(), timestampTracker, iiifDataset.getUri());
				harvester.harvest();
				timestampTracker.setDatasetTimestamp(iiifDataset.getUri(), startOfCrawl);
				timestampTracker.commit();
				finishedSuccsessfuly();
			} catch (Exception e) {
				log.warn(e.getMessage(), e);
				timestampTracker.setDatasetLastError(iiifDataset.getUri(), startOfCrawl);				
				timestampTracker.commit();
				finishedWithFailure(e);
			}
	}
	
	

}
