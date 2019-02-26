package inescid.dataaggregation.dataset.job;

import eu.europeana.research.iiif.crawl.CollectionCrawler;
import eu.europeana.research.iiif.discovery.ProcesssingAlgorithm;
import eu.europeana.research.iiif.discovery.demo.TimestampCrawlingHandler;
import eu.europeana.research.iiif.discovery.syncdb.InMemoryTimestampStore;
import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker;
import eu.europeana.research.iiif.profile.ManifestMetadataProfiler;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.GlobalCore;
import inescid.dataaggregation.dataset.IiifDataset;
import inescid.dataaggregation.dataset.IiifDataset.IiifCrawlMethod;
import inescid.dataaggregation.dataset.convert.rdfconverter.ConversionSpecificationAnalyzer;
import inescid.dataaggregation.dataset.profile.RdfDataUsageProfilerSchemaorgEdm;

public class JobProfileSchemaOrg extends JobWorker {
	
	protected JobProfileSchemaOrg(Job job, Dataset dataset) {
		super(job, dataset);
	}

	@Override
	public void runJob() throws Exception {
			RdfDataUsageProfilerSchemaorgEdm profiler=new RdfDataUsageProfilerSchemaorgEdm(GlobalCore.getDataRepository());
			profiler.process(dataset, GlobalCore.getPublicationRepository().getProfileFolder(dataset), 0);	
			
			ConversionSpecificationAnalyzer conv=new ConversionSpecificationAnalyzer();
			conv.process(dataset, GlobalCore.getPublicationRepository());
			
	}
	
	

}
