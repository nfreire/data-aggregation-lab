package inescid.dataaggregation.dataset.job;

import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.convert.rdfconverter.ConversionSpecificationAnalyzer;
import inescid.dataaggregation.dataset.profile.RdfDataUsageProfilerSchemaorgEdm;

public class JobProfileSchemaOrg extends JobWorker {
	
	protected JobProfileSchemaOrg(Job job, Dataset dataset) {
		super(job, dataset);
	}

	@Override
	public void runJob() throws Exception {
			RdfDataUsageProfilerSchemaorgEdm profiler=new RdfDataUsageProfilerSchemaorgEdm(Global.getDataRepository());
			profiler.process(dataset, Global.getPublicationRepository().getProfileFolder(dataset), 0);	
			
			ConversionSpecificationAnalyzer conv=new ConversionSpecificationAnalyzer();
			conv.process(dataset);
			
	}
	
	

}
