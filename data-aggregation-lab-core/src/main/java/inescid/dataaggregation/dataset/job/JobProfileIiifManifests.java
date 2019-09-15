package inescid.dataaggregation.dataset.job;

import eu.europeana.research.iiif.profile.ManifestMetadataProfiler;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.IiifDataset;

public class JobProfileIiifManifests extends JobWorker {
	
	protected JobProfileIiifManifests(Job job, Dataset dataset) {
		super(job, dataset);
	}

	@Override
	public void runJob() throws Exception {
			ManifestMetadataProfiler profiler=new ManifestMetadataProfiler(((IiifDataset)dataset)
					, Global.getDataRepository(), Global.getPublicationRepository().getProfileFolder(dataset));
			profiler.process(true);			
	}
	
	

}
