package inescid.dataaggregation.dataset.job;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.ZipException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.europeana.research.iiif.crawl.CollectionCrawler;
import eu.europeana.research.iiif.discovery.ProcesssingAlgorithm;
import eu.europeana.research.iiif.discovery.demo.TimestampCrawlingHandler;
import eu.europeana.research.iiif.discovery.syncdb.InMemoryTimestampStore;
import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker;
import eu.europeana.research.iiif.profile.ManifestMetadataProfiler;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.Dataset.DatasetType;
import inescid.dataaggregation.dataset.GlobalCore;
import inescid.dataaggregation.dataset.IiifDataset;
import inescid.dataaggregation.dataset.IiifDataset.IiifCrawlMethod;
import inescid.dataaggregation.store.PublicationRepository;

public class JobPublishSeeAlso extends JobWorker {
	
	@Override
	public void runJob()  throws Exception {
			PublicationRepository repository=GlobalCore.getPublicationRepository();
			if(dataset.getType()==DatasetType.IIIF) {
				File targetZipFile = repository.getExportSeeAlsoZipFile(dataset);
				ZipArchiveExporter ziper=new ZipArchiveExporter(targetZipFile);
				List<Entry<String, File>> allDatasetManifestSeeAlsoFiles = GlobalCore.getDataRepository().getAllDatasetResourceFiles(GlobalCore.SEE_ALSO_DATASET_PREFIX+dataset.getUri());
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
