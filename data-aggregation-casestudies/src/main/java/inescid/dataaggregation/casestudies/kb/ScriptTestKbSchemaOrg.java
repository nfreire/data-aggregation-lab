package inescid.dataaggregation.casestudies.kb;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.mortbay.log.Log;
import org.w3c.dom.Document;

import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.GlobalCore;
import inescid.dataaggregation.dataset.IiifDataset;
import inescid.dataaggregation.dataset.Dataset.DatasetType;
import inescid.dataaggregation.dataset.convert.EdmRdfToXmlSerializer;
import inescid.dataaggregation.dataset.convert.RdfDeserializer;
import inescid.dataaggregation.dataset.convert.RdfReg;
import inescid.dataaggregation.dataset.convert.SchemaOrgToEdmDataConverter;
import inescid.dataaggregation.dataset.job.Job;
import inescid.dataaggregation.dataset.job.ZipArchiveExporter;
import inescid.dataaggregation.store.PublicationRepository;
import inescid.util.LinkedDataUtil;
import inescid.util.XmlUtil;

public class ScriptTestKbSchemaOrg {

	
	public static void main(String[] args) throws Exception {
		List<Entry<String, File>> testDs=new ArrayList<>();
		testDs.add(new SimpleEntry<String, File>("http://data.bibliotheken.nl/id/alba/p398494843", new File("C:\\Users\\nfrei\\Desktop\\KB_ALBA_Item_1900_A_002_example.xml") ));
		testDs.add(new SimpleEntry<String, File>("http://data.bibliotheken.nl/id/alba/p418098271", new File("C:\\Users\\nfrei\\Desktop\\KB_ALBA_Item_1900_A_002_example.xml") ));
		testDs.add(new SimpleEntry<String, File>("http://data.bibliotheken.nl/id/alba/p418098344", new File("C:\\Users\\nfrei\\Desktop\\KB_ALBA_Item_1900_A_002_example.xml") ));
	
		ScriptTestKbSchemaOrg test=new ScriptTestKbSchemaOrg();
		test.dataProvider="KB";
		test.provider="KB";
		test.transformToEdmInternal=false;
		test.runJob(testDs);
	}
	
	
	
	boolean transformToEdmInternal;
	String provider;
	String dataProvider;

	public void runJob(List<Entry<String, File>> allDatasetResourceFiles) throws Exception {
			SchemaOrgToEdmDataConverter converter = new SchemaOrgToEdmDataConverter();
			converter.setDataProvider(dataProvider);
			converter.setProvider(provider);
			converter.setDatasetRights("http://example.org/rights-test/CC0");
			
			for (Entry<String, File> schemaOrgFile : allDatasetResourceFiles) {
				try {
					byte[] schemaOrgBytes = FileUtils.readFileToByteArray(schemaOrgFile.getValue());
					byte[] edmBytes = getEdmRecord(converter, schemaOrgFile.getKey(), schemaOrgBytes);
					if(edmBytes!=null) {
						String edmFilename = URLEncoder.encode(schemaOrgFile.getKey(), "UTF8")+".edm.xml";
						FileUtils.writeByteArrayToFile(new File(edmFilename), edmBytes);
					}
				} catch (Exception e) {
					System.err.println("Failed to convert resource: "+schemaOrgFile.getKey() );
					e.printStackTrace();
				}
			}
	}

	private byte[] getEdmRecord(SchemaOrgToEdmDataConverter converter, String resUri, byte[] sourceRdfBytes) {
//			String debug=null;
//			try {
//				debug = new String(sourceRdfBytes, "utf8");
//			} catch (UnsupportedEncodingException e) {
//				e.printStackTrace();
//			}
//			System.out.println(debug);
		
		
			Model fromRdfXml = RdfDeserializer.fromBytes(sourceRdfBytes, resUri);
			Resource mainTargetResource = converter.convert(fromRdfXml.createResource(resUri), null);
			EdmRdfToXmlSerializer xmlSerializer = new EdmRdfToXmlSerializer(mainTargetResource);
			Document edmDom = xmlSerializer.getXmlDom();
			String domString = XmlUtil.writeDomToString(edmDom);
			
			
			System.out.println(domString);
			
			return domString.getBytes(GlobalCore.UTF8);
	}
}
