package inescid.dataaggregation.dataset.job;

import java.util.Calendar;

import javax.xml.bind.JAXBElement.GlobalScope;

import eu.europeana.research.iiif.crawl.CollectionCrawler;
import eu.europeana.research.iiif.crawl.ManifestHarvester;
import eu.europeana.research.iiif.discovery.ProcesssingAlgorithm;
import eu.europeana.research.iiif.discovery.demo.TimestampCrawlingHandler;
import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker;
import inescid.dataaggregation.crawl.ld.LdDatasetHarvest;
import inescid.dataaggregation.crawl.ld.LdGlobals;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.IiifDataset;
import inescid.dataaggregation.dataset.IiifDataset.IiifCrawlMethod;
import inescid.dataaggregation.dataset.LodDataset;

public class JobHarvestLod extends JobWorker implements Runnable {
	Integer sampleSize;

	public JobHarvestLod() {
	}
	
	
	public JobHarvestLod(int sampleSize) {
		this.sampleSize = sampleSize;
	}
	
	@Override
	public void run() {
		failureCause=new Exception("Harvesting of LOD dataset not implemented");
		
		running=true;
		try {
			TimestampTracker timestampTracker=Global.getTimestampTracker();
			LodDataset lodDataset=(LodDataset)dataset;
			LdDatasetHarvest harvest=new LdDatasetHarvest(lodDataset, Global.getDataRepository(), true/*skip existing*/);
			harvest.setSampleSize(sampleSize);
			Calendar startOfCrawl=harvest.startProcess();
			timestampTracker.setDatasetTimestamp(lodDataset.getUri(), startOfCrawl);
			timestampTracker.commit();
			successful=true;
		} catch (Exception e) {
			failureCause=e;
		}
		running=false;
	}

}
