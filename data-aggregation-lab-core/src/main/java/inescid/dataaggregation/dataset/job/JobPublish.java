package inescid.dataaggregation.dataset.job;

import java.io.File;

import inescid.dataaggregation.data.ContentTypes;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.GlobalCore;
import inescid.dataaggregation.store.PublicationRepository;

public class JobPublish extends JobWorker {
	
	protected JobPublish(Job job, Dataset dataset) {
		super(job, dataset);
	}

	@Override
	public void runJob() throws Exception {
			PublicationRepository repository=GlobalCore.getPublicationRepository();
//			if(dataset.getType()==DatasetType.IIIF) {
				File targetZipFile = repository.getExportZipFile(dataset);
				if(!targetZipFile.getParentFile().exists())
					targetZipFile.getParentFile().mkdirs();
				ContentTypes format=dataset.getDataFormat()==null ? null : ContentTypes.fromMime(dataset.getDataFormat());
				GlobalCore.getDataRepository().exportDatasetToZip(dataset.getUri(), targetZipFile, format);


//			} else 
//				throw new RuntimeException("Not implemented: "+dataset.getType());
	}
	
	

}
