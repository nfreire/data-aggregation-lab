package inescid.dataaggregation.dataset.job;

import eu.europeana.research.iiif.crawl.CollectionCrawler;
import eu.europeana.research.iiif.discovery.ProcesssingAlgorithm;
import eu.europeana.research.iiif.discovery.demo.TimestampCrawlingHandler;
import eu.europeana.research.iiif.discovery.syncdb.InMemoryTimestampStore;
import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker;
import eu.europeana.research.iiif.profile.ManifestMetadataProfiler;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.IiifDataset;
import inescid.dataaggregation.dataset.IiifDataset.IiifCrawlMethod;

public class JobProfileSchemaOrg extends JobWorker {
	
	@Override
	public void run() {
		running=true;
		try {
			RdfDataUsageProfilerSchemaorgEdm profiler=new RdfDataUsageProfilerSchemaorgEdm(Global.getDataRepository());
			profiler.process(dataset, Global.getPublicationRepository().getProfileFolder(dataset), 0);			
			successful=true;
		} catch (Exception e) {
			failureCause=e;
		}
		running=false;
	}
	
	

}
