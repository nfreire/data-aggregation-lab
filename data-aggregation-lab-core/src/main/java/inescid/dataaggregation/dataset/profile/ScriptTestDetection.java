package inescid.dataaggregation.dataset.profile;

import java.io.File;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang3.StringUtils;

import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.DatasetProfile;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.detection.DataProfileDetector;
import inescid.dataaggregation.dataset.detection.DataTypeResult;
import inescid.util.datastruct.MapOfInts;

public class ScriptTestDetection {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
			.getLogger(ScriptTestDetection.class);
	
	public static void main(String[] args) {
		try {
			Global.init_developement();
			DataProfileDetector dtc=new DataProfileDetector();
			
			Dataset dataset=null;
//					GlobalCore.getDatasetRegistryRepository().getDatasetByUri("http://192.92.149.69:8983/data-aggregation-lab/static/data-external/europeana-dataset-sitemap/sitemap.xml");
			
			File reportFolder = Global.getPublicationRepository().getReportsFolder(dataset);
			if(!reportFolder.exists())
				reportFolder.mkdirs();
			File reportFile = new File(reportFolder, "SintaxValidation.txt");
			FileWriterWithEncoding fileWriter = new FileWriterWithEncoding(reportFile, Global.UTF8);
			CSVPrinter csvPrinter=new CSVPrinter(fileWriter, CSVFormat.DEFAULT);
			
			List<Entry<String, File>> allDatasetResourceFiles = 			
					Global.getDataRepository().getAllDatasetResourceFiles(dataset.getUri());
			
			int validationFails=0;
			int emptyFiles=0;
			int detectionInconclusive=0;
			int detectionOfSchemaOrg=0;
			MapOfInts<String> countByContentType=new MapOfInts<>();
			
			for (Entry<String, File> seeAlsoFile : allDatasetResourceFiles) {
				try {
					String contentType=null;
					List<Entry<String, String>> headers = Global.getDataRepository().getMeta(dataset.getUri(), seeAlsoFile.getKey());
					for(Entry<String, String> h: headers) {
						if(h.getKey().equals("Content-Type")) {
							contentType=h.getValue();
							break;
						}
					}
					countByContentType.incrementTo(StringUtils.isEmpty(contentType) ? "empty" : contentType);
					
					if(seeAlsoFile.getValue().length()==0)
						emptyFiles++;
					else {
						DataTypeResult detected = dtc.detect(seeAlsoFile.getValue());
						if(detected==null) {
							detectionInconclusive++;						
						} else if(detected.format==null){
							validationFails++;
						} else if(detected.profile!=null && detected.profile==DatasetProfile.SCHEMA_ORG){
							detectionOfSchemaOrg++;
						} else {
							detectionInconclusive++;						
						}
					}
				} catch (Exception e) {
					log.warn("Failed to validate resource: "+seeAlsoFile.getKey(), e);
				}
			}
			
//			int validationFails=0;
//			int emptyFiles=0;
//			int detectionInconclusive=0;
//			int detectionOfSchemaOrg=0;
//			MapOfInts<String> countByContentType=new MapOfInts<>();

			csvPrinter.print("validationFails");
			csvPrinter.print(validationFails);
			csvPrinter.println();
			csvPrinter.print("emptyFiles");
			csvPrinter.print(emptyFiles);
			csvPrinter.println();
			csvPrinter.print("detectionInconclusive");
			csvPrinter.print(detectionInconclusive);
			csvPrinter.println();
			csvPrinter.print("detectionOfSchemaOrg");
			csvPrinter.print(detectionOfSchemaOrg);
			csvPrinter.println();
			csvPrinter.print("countByContentType");
			csvPrinter.println();
			for(Entry<String, Integer> m: countByContentType.entrySet()) { 
				csvPrinter.print(m.getKey());
				csvPrinter.print(m.getValue());
				csvPrinter.println();
			}
			csvPrinter.close();
			fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

}
