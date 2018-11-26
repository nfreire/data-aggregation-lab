package inescid.dataaggregation.dataset.job;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import inescid.dataaggregation.dataset.detection.ContentTypes;
import inescid.dataaggregation.dataset.detection.DataProfileDetector;
import inescid.dataaggregation.dataset.detection.DataTypeResult;
import inescid.dataaggregation.store.PublicationRepository;

public class JobPublish extends JobWorker {
	
	@Override
	public void runJob() throws Exception {
			PublicationRepository repository=GlobalCore.getPublicationRepository();
//			if(dataset.getType()==DatasetType.IIIF) {
				File targetZipFile = repository.getExportZipFile(dataset);
				if(!targetZipFile.getParentFile().exists())
					targetZipFile.getParentFile().mkdirs();
				ZipArchiveExporter ziper=new ZipArchiveExporter(targetZipFile);
				List<Entry<String, File>> allDatasetManifestFiles = GlobalCore.getDataRepository().getAllDatasetResourceFiles(dataset.getUri());
//				DataProfileDetector detector=new DataProfileDetector();
//				DataTypeResult detected=null;
//				for(Entry<String, File> manifEntry: allDatasetManifestFiles) {
//					detected=detector.detect(manifEntry.getValue());
//					if(detected!=null)
//						break;
//				}
				ContentTypes format=dataset.getDataFormat()==null ? null : ContentTypes.fromMime(dataset.getDataFormat());
				String fileExtension=format!=null ? "."+format.getFilenameExtension() : "";
				for(Entry<String, File> manifEntry: allDatasetManifestFiles) {
					ziper.addFile(manifEntry.getValue().getName()+fileExtension);
					FileInputStream fis = new FileInputStream(manifEntry.getValue());
					IOUtils.copy(fis, ziper.outputStream());
					fis.close();
				}
				ziper.close();
//			} else 
//				throw new RuntimeException("Not implemented: "+dataset.getType());
	}
	
	

}
