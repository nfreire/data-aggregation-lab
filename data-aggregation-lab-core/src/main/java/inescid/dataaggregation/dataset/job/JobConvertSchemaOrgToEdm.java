package inescid.dataaggregation.dataset.job;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.mortbay.log.Log;
import org.w3c.dom.Document;

import eu.europeana.research.iiif.crawl.CollectionCrawler;
import eu.europeana.research.iiif.crawl.ManifestHarvester;
import eu.europeana.research.iiif.crawl.ManifestSeeAlsoHarvester;
import eu.europeana.research.iiif.discovery.ProcesssingAlgorithm;
import eu.europeana.research.iiif.discovery.demo.TimestampCrawlingHandler;
import eu.europeana.research.iiif.discovery.syncdb.InMemoryTimestampStore;
import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker;
import eu.europeana.research.iiif.profile.SeeAlsoProfile;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.GlobalCore;
import inescid.dataaggregation.dataset.IiifDataset;
import inescid.dataaggregation.dataset.Dataset.DatasetType;
import inescid.dataaggregation.dataset.convert.EdmRdfToXmlSerializer;
import inescid.dataaggregation.dataset.convert.RdfDeserializer;
import inescid.dataaggregation.dataset.convert.RdfReg;
import inescid.dataaggregation.dataset.convert.SchemaOrgToEdmDataConverter;
import inescid.dataaggregation.store.PublicationRepository;
import inescid.util.LinkedDataUtil;
import inescid.util.XmlUtil;

public class JobConvertSchemaOrgToEdm extends JobWorker implements Runnable {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
		.getLogger(JobConvertSchemaOrgToEdm.class);
	
	boolean transformToEdmInternal;
	String provider;
	String dataProvider;

	public JobConvertSchemaOrgToEdm() {
	}

	@Override
	public void runJob() throws Exception {
			SchemaOrgToEdmDataConverter converter = new SchemaOrgToEdmDataConverter();
			converter.setDataProvider(dataset.getOrganization());
			converter.setProvider(dataset.getOrganization());

			if(dataset.getType()==DatasetType.LOD) {
				Resource  dsResource = LinkedDataUtil.getResource(dataset.getUri());
				StmtIterator licenseProperties = dsResource.listProperties(RdfReg.SCHEMAORG_LICENSE);
				if (licenseProperties!=null && licenseProperties.hasNext()) {
					while(licenseProperties.hasNext()) {
						Statement st=licenseProperties.next();
						if(st.getObject().isURIResource())
							converter.setDatasetRights(st.getObject().asNode().getURI());
					}
				} 
			}
			
			PublicationRepository repository = GlobalCore.getPublicationRepository();
			File targetZipFile = repository.getExportEdmZipFile(dataset);
			targetZipFile.getParentFile().mkdirs();
			ZipArchiveExporter ziper = new ZipArchiveExporter(targetZipFile);
			
			String edmDatasetUri=dataset.getConvertedEdmDatasetUri();
			
			List<Entry<String, File>> allDatasetResourceFiles = (dataset.getType()==DatasetType.IIIF ? 					
					GlobalCore.getDataRepository()
					.getAllDatasetResourceFiles(((IiifDataset)dataset).getSeeAlsoDatasetUri())
					: GlobalCore.getDataRepository()
					.getAllDatasetResourceFiles(dataset.getUri()));
			for (Entry<String, File> seeAlsoFile : allDatasetResourceFiles) {
				try {
					byte[] schemaOrgBytes = FileUtils.readFileToByteArray(seeAlsoFile.getValue());
					byte[] edmBytes = getEdmRecord(converter, seeAlsoFile.getKey(), schemaOrgBytes);
					if(edmBytes!=null) {
						ziper.addFile(seeAlsoFile.getValue().getName()+".edm.xml");
						ByteArrayInputStream fis = new ByteArrayInputStream(edmBytes);
						IOUtils.copy(fis, ziper.outputStream());
						fis.close();
						GlobalCore.getDataRepository().save(edmDatasetUri, seeAlsoFile.getKey(), edmBytes);
					}
				} catch (Exception e) {
					Log.warn("Failed to convert resource: "+seeAlsoFile.getKey() , e);
//					failureCause = new Exception("On "+seeAlsoFile.getKey(),e);
//					running = false;
//					return;
				}
				
			}
			ziper.close();
	}

	private byte[] getEdmRecord(SchemaOrgToEdmDataConverter converter, String resUri, byte[] sourceRdfBytes) {
		try {
			Model fromRdfXml = RdfDeserializer.fromBytes(sourceRdfBytes, resUri);
			Resource mainTargetResource = converter.convert(fromRdfXml.createResource(resUri), null);
			EdmRdfToXmlSerializer xmlSerializer = new EdmRdfToXmlSerializer(mainTargetResource);
			Document edmDom = xmlSerializer.getXmlDom();
			String domString = XmlUtil.writeDomToString(edmDom);
			return domString.getBytes(GlobalCore.UTF8);
		} catch (Exception e) {
			log.warn("on "+resUri, e);
			return null;
		}
	}

}