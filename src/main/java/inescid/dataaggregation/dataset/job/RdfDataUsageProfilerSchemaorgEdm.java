package inescid.dataaggregation.dataset.job;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.Dataset.DatasetType;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.convert.RdfDeserializer;
import inescid.dataaggregation.dataset.convert.SchemaOrgToEdmDataConverter;
import inescid.dataaggregation.dataset.profile.UsageProfiler;
import inescid.dataaggregation.dataset.store.Repository;
import inescid.util.googlesheets.GoogleSheetsCsvUploader;

public class RdfDataUsageProfilerSchemaorgEdm {

	static final Charset UTF8 = Charset.forName("UTF8");
	Repository dataRepository;
	
	UsageProfiler profilerSchemaorg=new UsageProfiler();
	UsageProfiler profilerEdm=new UsageProfiler();
	
	public RdfDataUsageProfilerSchemaorgEdm(Repository dataRepository) {
		this.dataRepository = dataRepository;
	}

	public void process(Dataset dataset, File profileFolder, final int maxProfiledRecords) throws IOException {
//	public void run(File repositoryFolder, String dataProvider) throws Exception {
		SchemaOrgToEdmDataConverter converter = new SchemaOrgToEdmDataConverter();
		converter.setDataProvider(dataset.getOrganization());
		converter.setProvider(dataset.getOrganization());
		
//		DataConverter schemaOrgToEdmConverter = DataConversionManager.getInstance().getConverter(new DataSpec("application/ld+json", "http://www.w3.org/1999/02/22-rdf-syntax-ns#", "http://schema.org/"), new DataSpec("application/xml", "http://edm", null));
//		((SchemaOrgToEdmDataConverter)schemaOrgToEdmConverter).setDataProvider(dataProvider);
		int cnt=0;
		int cntProfiled=0;
		
		List<Entry<String, File>> allDatasetResourceFiles = (dataset.getType()==DatasetType.IIIF ? 					
				Global.getDataRepository()
				.getAllDatasetResourceFiles(Global.SEE_ALSO_DATASET_PREFIX+dataset.getUri())
				: Global.getDataRepository()
				.getAllDatasetResourceFiles(dataset.getUri()));
		for (Entry<String, File> seeAlsoFile : allDatasetResourceFiles) {
			try {
				byte[] schemaOrgBytes = FileUtils.readFileToByteArray(seeAlsoFile.getValue());
				Model ldModelRdf = RdfDeserializer.fromBytes(schemaOrgBytes, seeAlsoFile.getKey());
				Resource edmRdf = getEdmRecord(converter, seeAlsoFile.getKey(), ldModelRdf);
	
				profilerSchemaorg.collect(ldModelRdf);
				profilerEdm.collect(edmRdf.getModel());
				
				cntProfiled++;
				ldModelRdf.removeAll();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(cnt % 50 == 0) 
				System.out.println("\nProgress: "+ cnt+" records ("+cntProfiled + " profiled)\n"+profilerSchemaorg.getUsageStats()+ " profiled)\n"+profilerSchemaorg.getUsageStats().toCsv()+"\n");
			else if(cnt % 10 == 0) 
				System.out.print(cnt);
			else
				System.out.print(".");
		}
		

		String csvEdm = profilerEdm.getUsageStats().toCsv();
		File edmFile = new File(profileFolder, "edm-profile.csv");
		FileUtils.write(edmFile, csvEdm, Global.UTF8);
		String csvSchemaOrg = profilerSchemaorg.getUsageStats().toCsv();
		File schemaorgFile = new File(profileFolder, "schema.org-profile.csv");
		FileUtils.write(schemaorgFile, csvSchemaOrg, Global.UTF8);
		
		String edmSheetTitle=edmFile.getName().substring(0, edmFile.getName().lastIndexOf('.'));
		String schemaorgSheetTitle=schemaorgFile.getName().substring(0, schemaorgFile.getName().lastIndexOf('.'));

		File sheetsIdFile = new File(profileFolder, "google-sheet-id.txt");
		String spreadsheetId=null;
		if(sheetsIdFile.exists()) {
			 spreadsheetId=FileUtils.readFileToString(sheetsIdFile, Global.UTF8);
		} else {
			spreadsheetId=GoogleSheetsCsvUploader.create("Dataset Profiling - "+dataset.getTitle(), edmSheetTitle);
			FileUtils.write(sheetsIdFile, spreadsheetId, Global.UTF8);
		}
		GoogleSheetsCsvUploader.update(spreadsheetId, edmSheetTitle, edmFile);			
		GoogleSheetsCsvUploader.update(spreadsheetId, schemaorgSheetTitle, schemaorgFile);			
	}

	private Resource getEdmRecord(SchemaOrgToEdmDataConverter converter, String resUri, Model sourceModel) {
		try {
			Resource mainTargetResource = converter.convert(sourceModel.createResource(resUri), null);
			return mainTargetResource;
		} catch (Exception e) {
			System.err.println(resUri);
			e.printStackTrace();
			return null;
		}
	}
	
	public UsageProfiler getSchemaorgProfile() {
		return profilerSchemaorg;
	}	
	public UsageProfiler getEdmProfile() {
		return profilerEdm;
	}	
}
