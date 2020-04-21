package inescid.dataaggregation.tests;

import java.io.File;
import java.net.URLEncoder;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.w3c.dom.Document;

import inescid.dataaggregation.crawl.http.HttpRequest;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.convert.RdfDeserializer;
import inescid.dataaggregation.dataset.convert.SchemaOrgToEdmDataConverter;
import inescid.util.HttpUtil;
import inescid.util.RdfUtil;
import inescid.util.XmlUtil;
import inescid.util.europeana.EdmRdfToXmlSerializer;

public class ScriptTestKbSchemaOrg {

	
	public static void main(String[] args) throws Exception {
		List<Entry<String, File>> testDs=new ArrayList<>();
//		testDs.add(new SimpleEntry<String, File>("http://data.bibliotheken.nl/id/alba/p398494843", new File("C:\\Users\\nfrei\\Desktop\\KB_ALBA_Item_1900_A_002_example.xml") ));
//		testDs.add(new SimpleEntry<String, File>("http://data.bibliotheken.nl/id/alba/p418098271", new File("C:\\Users\\nfrei\\Desktop\\KB_ALBA_Item_1900_A_002_example.xml") ));
//		testDs.add(new SimpleEntry<String, File>("http://data.bibliotheken.nl/id/alba/p418098344", new File("C:\\Users\\nfrei\\Desktop\\KB_ALBA_Item_1900_A_002_example.xml") ));
		testDs.add(new SimpleEntry<String, File>("http://data.bibliotheken.nl/id/alba/p419099018", null));
		testDs.add(new SimpleEntry<String, File>("http://data.bibliotheken.nl/id/alba/p421349735", null ));
	
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
		Global.init_developement();
		
			SchemaOrgToEdmDataConverter converter = new SchemaOrgToEdmDataConverter();
			converter.setDataProvider(dataProvider);
			converter.setProvider(provider);
			converter.setDatasetRights("http://example.org/rights-test/CC0");
			
			for (Entry<String, File> schemaOrgFile : allDatasetResourceFiles) {
				try {
					System.out.println(schemaOrgFile.getKey());
					byte[] schemaOrgBytes;
					if(schemaOrgFile.getValue()==null) {
						File schemaFile=new File(URLEncoder.encode(schemaOrgFile.getKey(), "UTF8")+".schema.xml");
						if(schemaFile.exists())
							schemaOrgBytes = FileUtils.readFileToByteArray(schemaFile);
						else {
							HttpRequest req = HttpUtil.makeRequest(schemaOrgFile.getKey(), RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
							schemaOrgBytes = req.getResponseContent();
							FileUtils.writeByteArrayToFile(schemaFile, schemaOrgBytes);
						}
					}else 
						schemaOrgBytes = FileUtils.readFileToByteArray(schemaOrgFile.getValue());
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
			Global.shutdown();
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
			
			return domString.getBytes(Global.UTF8);
	}
}
