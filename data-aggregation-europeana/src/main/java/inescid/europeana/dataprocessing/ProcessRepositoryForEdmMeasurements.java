package inescid.europeana.dataprocessing;

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
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import eu.europeana.corelib.edm.utils.EdmUtils;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import inescid.dataaggregation.casestudies.coreference.SameAsSets;
import inescid.europeanarepository.EdmMongoServer;
import inescid.europeanarepository.EdmMongoServer.FullBeanHandler;
import inescid.util.RdfUtil;

public class ProcessRepositoryForEdmMeasurements {

	public static void main(String[] args) throws Exception {
		String outputFolder = "c://users/nfrei/desktop/data/";

		if (args != null) {
			if (args.length >= 1) {
				outputFolder = args[0];
			}
		}

//		Global.init_componentDataRepository(repoFolder);
//		Global.init_enableComponentHttpRequestCache();
//		Repository repository = Global.getDataRepository();
		EdmMongoServer edmMongo = new EdmMongoServer("mongodb://rnd-2.eanadev.org:27017/admin",
				"metis-preview-production-2");

		EdmMeasurementSet measurements = EdmMeasurementSet.getMeasurementSetA();

		File mapsFile = new File(outputFolder, "edm-measurements.mvstore.bin");
		if (!mapsFile.getParentFile().exists())
			mapsFile.getParentFile().mkdirs();
		MVStore mvStore = new MVStore.Builder().fileName(mapsFile.getPath()).open();

		MVMap<String, String> idToCsv = mvStore.openMap(mapsFile.getName());

		final ProgressTrackerOnFile tracker=new ProgressTrackerOnFile(new File(outputFolder, ProcessRepositoryForEdmMeasurements.class.getSimpleName()+"_progress.txt"));
		final int offset=tracker.getTokenAsInt();
		System.out.println("Starting at mongo offset "+offset);

		try {
			edmMongo.forEachFullBean(new FullBeanHandler() {
				Date start = new Date();
				int recCnt = offset;
				int okRecs = 0;

				StringBuilder recCsv = new StringBuilder();

				public void handle(FullBeanImpl fb) {
					try {
						String uri = fb.getAbout();
						String recId = fb.getAbout().substring(1);
						recCnt++;
						if (recCnt % 1000 == 0 || recCnt == 10) {
	//						csvOut.flush();
	//						csvBuffer.flush();
							Date now = new Date();
							long elapsedPerRecord = (now.getTime() - start.getTime()) / recCnt;
							double recsMinute = 60000 / (elapsedPerRecord==0 ? 1 : elapsedPerRecord);
							double minutesToEnd = (58000000 - recCnt) / recsMinute;
							System.out.printf("%d recs. (%d ok) - %d recs/min - %d mins. to end\n", recCnt, okRecs,
									(int) recsMinute, (int) minutesToEnd);
						}
						if (idToCsv.containsKey(recId))
							return;
	
						try {
							String edmRdfXml = EdmUtils.toEDM(fb);
							CSVPrinter csvOut = new CSVPrinter(recCsv, CSVFormat.DEFAULT);
							Model readRdf = RdfUtil.readRdf(edmRdfXml, org.apache.jena.riot.Lang.RDFXML);
							for (EdmMeasurementSet.EdmMeasurement measurement : measurements) {
								try {
									String[] csvResult = measurement.getCsvResult(readRdf.getResource(uri), edmRdfXml);
									for (String m : csvResult)
										csvOut.print(m);
								} catch (Exception e) {
									System.err.println("Error in: " + uri);
									System.err.println(e.getMessage());
								}
							}
							csvOut.close();
							idToCsv.put(recId, recCsv.toString());
							recCsv.setLength(0);
	
							okRecs++;
						} catch (RiotException e) {
							System.err.println("Error reading RDF: " + uri);
							System.err.println(e.getMessage());
	//					e.printStackTrace();
						} catch (IOException e) {
							System.err.println("Error reading from repository: " + uri);
							System.err.println(e.getMessage());
	//					e.printStackTrace();
						}
					}finally {
						try {
							tracker.track(recCnt);
						} catch (IOException e) {
							e.printStackTrace();
							throw new RuntimeException(e.getMessage(), e);
						}
					}
				}
			}, offset);
		} finally {
			mvStore.close();
		}
//		csvOut.close();
//		csvBuffer.close();
	}

}
