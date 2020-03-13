package inescid.europeana.dataprocessing.old;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RiotException;

import eu.europeana.corelib.edm.utils.EdmUtils;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import inescid.europeana.dataprocessing.EdmMeasurementSet;
import inescid.europeana.dataprocessing.EdmMeasurementSet.EdmMeasurement;
import inescid.europeanarepository.EdmMongoServer;
import inescid.europeanarepository.EdmMongoServer.FullBeanHandler;
import inescid.util.RdfUtil;

public class ProcessRepositoryMongo {

public static void main(String[] args) throws Exception {
    	String outputFolder = "c://users/nfrei/desktop/data/";
		
		if(args!=null) {
			if(args.length>=1) {
				outputFolder = args[0];
			}
		}
		
//		Global.init_componentDataRepository(repoFolder);
//		Global.init_enableComponentHttpRequestCache();
//		Repository repository = Global.getDataRepository();
		EdmMongoServer edmMongo=new EdmMongoServer("mongodb://rnd-2.eanadev.org:27017/admin", "metis-preview-production-2");
		
		EdmMeasurementSet measurements=EdmMeasurementSet.getMeasurementSetA();
		
		BufferedWriter csvBuffer = Files.newBufferedWriter(new File(outputFolder, "edm-measurements.csv").toPath(), StandardCharsets.UTF_8);
		CSVPrinter csvOut=new CSVPrinter(csvBuffer, CSVFormat.DEFAULT);
		csvOut.print("URI"); 
		for(String header : measurements.getHeaders()) {
			csvOut.print(header);
		}
		csvOut.println();
		
		edmMongo.forEachFullBean(new FullBeanHandler() {
			Date start=new Date();
			int tmpCnt=0;
			int okRecs=0;
			public void handle(FullBeanImpl fb) {
				String uri=fb.getAbout();
				try {
					String edmRdfXml = EdmUtils.toEDM(fb);
					tmpCnt++;
					if(tmpCnt % 1000 == 0 || tmpCnt == 10) {
						csvOut.flush();
						csvBuffer.flush();
						Date now=new Date();
						long elapsedPerRecord=(now.getTime()-start.getTime())/tmpCnt;
						double recsMinute=60000/elapsedPerRecord;
						double minutesToEnd=(58000000-tmpCnt)/recsMinute;
						System.out.printf("%d recs. (%d ok) - %d recs/min - %d mins. to end\n",tmpCnt, okRecs, (int) recsMinute, (int)minutesToEnd);
					}
					Model readRdf = RdfUtil.readRdf(edmRdfXml, org.apache.jena.riot.Lang.RDFXML);
					csvOut.print(uri.substring(1));
					for(EdmMeasurementSet.EdmMeasurement measurement : measurements) {
						try {
							String[] csvResult = measurement.getCsvResult(readRdf.getResource(uri), edmRdfXml);
							for(String m:csvResult) 
								csvOut.print(m);
						} catch (Exception e) {
							System.err.println("Error in: "+uri);
							System.err.println(e.getMessage());
						}
					}
					okRecs++;
					csvOut.println();
				} catch (RiotException e) {
					System.err.println("Error reading RDF: "+uri);
					System.err.println(e.getMessage());
//					e.printStackTrace();
				} catch (IOException e) {
					System.err.println("Error reading from repository: "+uri);
					System.err.println(e.getMessage());
//					e.printStackTrace();
				}
			}
		});
		csvOut.close();
		csvBuffer.close();
	}

	
	
}
