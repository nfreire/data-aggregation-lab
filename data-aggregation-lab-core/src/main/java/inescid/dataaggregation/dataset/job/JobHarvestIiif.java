package inescid.dataaggregation.dataset.job;

import java.util.Calendar;

import eu.europeana.research.iiif.crawl.CollectionCrawler;
import eu.europeana.research.iiif.crawl.ManifestHarvester;
import eu.europeana.research.iiif.discovery.ProcesssingAlgorithm;
import eu.europeana.research.iiif.discovery.demo.TimestampCrawlingHandler;
import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.IiifDataset;
import inescid.dataaggregation.dataset.IiifDataset.IiifCrawlMethod;

public class JobHarvestIiif extends JobWorker implements Runnable {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
			.getLogger(JobHarvestIiif.class);
	
	Integer sampleSize;

	public JobHarvestIiif(Job job, Dataset dataset) {
		super(job, dataset);
	}
	
	public JobHarvestIiif(Job job, Dataset dataset, int sampleSize) {
		super(job, dataset);
		this.sampleSize = sampleSize;
	}

	@Override
	public void runJob() throws Exception {
			TimestampTracker timestampTracker=Global.getTimestampTracker();
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
				ManifestHarvester harvester=new ManifestHarvester(Global.getDataRepository(), timestampTracker, iiifDataset.getUri());
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
