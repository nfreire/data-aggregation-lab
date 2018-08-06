package inescid.dataaggregation.dataset.job;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
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
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.IiifDataset;
import inescid.dataaggregation.dataset.Dataset.DatasetType;
import inescid.dataaggregation.dataset.IiifDataset.IiifCrawlMethod;
import inescid.dataaggregation.dataset.convert.EdmRdfToXmlSerializer;
import inescid.dataaggregation.dataset.convert.RdfDeserializer;
import inescid.dataaggregation.dataset.convert.RdfReg;
import inescid.dataaggregation.dataset.convert.SchemaOrgToEdmDataConverter;
import inescid.dataaggregation.dataset.store.PublicationRepository;
import inescid.dataaggregation.dataset.view.management.HarvestIiifSeeAlsoForm;
import inescid.util.LinkedDataUtil;
import inescid.util.XmlUtil;

public class JobConvertSchemaOrgToEdm extends JobWorker implements Runnable {

	boolean transformToEdmInternal;
	String provider;
	String dataProvider;

	public JobConvertSchemaOrgToEdm() {
	}

	@Override
	public void run() {
		running = true;
		try {
			SchemaOrgToEdmDataConverter converter = new SchemaOrgToEdmDataConverter();
			converter.setDataProvider(dataset.getOrganization());
			converter.setProvider(dataset.getOrganization());

			Resource  dsResource = LinkedDataUtil.getResource(dataset.getUri());
			StmtIterator licenseProperties = dsResource.listProperties(RdfReg.SCHEMAORG_LICENSE);
			if (licenseProperties!=null && licenseProperties.hasNext()) {
				while(licenseProperties.hasNext()) {
					Statement st=licenseProperties.next();
					if(st.getObject().isURIResource())
						converter.setDatasetRights(st.getObject().asNode().getURI());
				}
			} 
			
			PublicationRepository repository = Global.getPublicationRepository();
			File targetZipFile = repository.getExportEdmZipFile(dataset);
			targetZipFile.getParentFile().mkdirs();
			ZipArchiveExporter ziper = new ZipArchiveExporter(targetZipFile);
			
			List<Entry<String, File>> allDatasetResourceFiles = (dataset.getType()==DatasetType.IIIF ? 					
					Global.getDataRepository()
					.getAllDatasetResourceFiles(Global.SEE_ALSO_DATASET_PREFIX+dataset.getUri())
					: Global.getDataRepository()
					.getAllDatasetResourceFiles(dataset.getUri()));
			for (Entry<String, File> seeAlsoFile : allDatasetResourceFiles) {
				byte[] schemaOrgBytes = FileUtils.readFileToByteArray(seeAlsoFile.getValue());
				byte[] edmBytes = getEdmRecord(converter, seeAlsoFile.getKey(), schemaOrgBytes);
				ziper.addFile(seeAlsoFile.getValue().getName()+".edm.xml");
				ByteArrayInputStream fis = new ByteArrayInputStream(edmBytes);
				IOUtils.copy(fis, ziper.outputStream());
				fis.close();
			}
			ziper.close();
			successful = true;
		} catch (Exception e) {
			failureCause = e;
		}
		running = false;
	}

	private byte[] getEdmRecord(SchemaOrgToEdmDataConverter converter, String resUri, byte[] sourceRdfBytes) {
		try {
			Model fromRdfXml = RdfDeserializer.fromBytes(sourceRdfBytes, resUri);
			Resource mainTargetResource = converter.convert(fromRdfXml.createResource(resUri), null);
			EdmRdfToXmlSerializer xmlSerializer = new EdmRdfToXmlSerializer(mainTargetResource);
			Document edmDom = xmlSerializer.getXmlDom();
			String domString = XmlUtil.writeDomToString(edmDom);
			return domString.getBytes(Global.UTF8);
		} catch (Exception e) {
			System.err.println(resUri);
			e.printStackTrace();
			return null;
		}
	}

}
