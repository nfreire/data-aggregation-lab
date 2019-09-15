package inescid.dataaggregation.dataset.job;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;

import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.Dataset.DatasetType;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.IiifDataset;
import inescid.dataaggregation.store.PublicationRepository;

public class JobPublishSeeAlso extends JobWorker {
	
	protected JobPublishSeeAlso(Job job, Dataset dataset) {
		super(job, dataset);
	}

	@Override
	public void runJob()  throws Exception {
			PublicationRepository repository=Global.getPublicationRepository();
			if(dataset.getType()==DatasetType.IIIF) {
				File targetZipFile = repository.getExportSeeAlsoZipFile(dataset);
				ZipArchiveExporter ziper=new ZipArchiveExporter(targetZipFile);
				List<Entry<String, File>> allDatasetManifestSeeAlsoFiles = Global.getDataRepository().getAllDatasetResourceFiles(((IiifDataset)dataset).getSeeAlsoDatasetUri());
				for(Entry<String, File> manifEntry: allDatasetManifestSeeAlsoFiles) {
					ziper.addFile(manifEntry.getValue().getName());
					FileInputStream fis = new FileInputStream(manifEntry.getValue());
					IOUtils.copy(fis, ziper.outputStream());
					fis.close();
				}
				ziper.close();
			} else 
				throw new RuntimeException("Not implemented: "+dataset.getType());
	}
	
	

}
