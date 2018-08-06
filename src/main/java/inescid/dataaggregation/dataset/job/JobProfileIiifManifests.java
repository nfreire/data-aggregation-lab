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

public class JobProfileIiifManifests extends JobWorker {
	
	@Override
	public void run() {
		running=true;
		try {
			ManifestMetadataProfiler profiler=new ManifestMetadataProfiler(((IiifDataset)dataset).getUri()
					, Global.getDataRepository(), Global.getPublicationRepository().getProfileFolder(dataset));
			profiler.process();			
			successful=true;
		} catch (Exception e) {
			failureCause=e;
		}
		running=false;
	}
	
	

}
