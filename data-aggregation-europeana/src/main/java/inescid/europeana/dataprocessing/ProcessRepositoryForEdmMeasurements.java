package inescid.europeana.dataprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RiotException;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import com.jmatio.io.stream.BufferedOutputStream;

import eu.europeana.corelib.definitions.edm.entity.WebResource;
import eu.europeana.corelib.edm.model.metainfo.WebResourceMetaInfoImpl;
import eu.europeana.corelib.edm.utils.EdmUtils;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import inescid.europeanarepository.EdmMongoServer;
import inescid.europeanarepository.EdmMongoServer.Handler;
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

		File outCsvFile = new File(outputFolder, "edm-measurements.csv");
		if (!outCsvFile.getParentFile().exists())
			outCsvFile.getParentFile().mkdirs();

		final ProgressTrackerOnFile tracker=new ProgressTrackerOnFile(new File(outputFolder, ProcessRepositoryForEdmMeasurements.class.getSimpleName()+"_progress.txt"));
		final int offset=outCsvFile.exists() ? tracker.getTokenAsInt() : 0;
		System.out.println("Starting at mongo offset "+offset);

		final HashSet<String> processed=new HashSet<String>();
		if(outCsvFile.exists()) {
			if(offset>0) {
				ArrayList<String> lastProcessed=new ArrayList<String>();
				BufferedReader inCsvReader=Files.newBufferedReader(outCsvFile.toPath(), StandardCharsets.UTF_8);
				CSVParser parser=new CSVParser(inCsvReader, CSVFormat.DEFAULT);
				for(CSVRecord r: parser) {
					lastProcessed.add(r.get(0));
					if(lastProcessed.size()>1000)
						lastProcessed.remove(0);
				}
				processed.addAll(lastProcessed);
			}else
				outCsvFile.delete();
		}
			
		final BufferedWriter outCsvWriter=Files.newBufferedWriter(outCsvFile.toPath(), StandardCharsets.UTF_8, 
				outCsvFile.exists() ? StandardOpenOption.APPEND : StandardOpenOption.CREATE );
		final CSVPrinter outCsvPrinter=new CSVPrinter(outCsvWriter, CSVFormat.DEFAULT);

		edmMongo.forEach(FullBeanImpl.class, new Handler<FullBeanImpl>() {
			Date start = new Date();
			int recCnt = offset;
			int okRecs = 0;

			public boolean handle(FullBeanImpl fb) {
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
						try {
							outCsvPrinter.flush();
							outCsvWriter.flush();
						} catch (IOException e) {
							throw new RuntimeException(e.getMessage(), e);
						}
					}
					if (processed.contains(recId))
						return true;

					try {
						String edmRdfXml = EdmUtils.toEDM(fb);
						edmRdfXml=Normalizer.normalize(edmRdfXml, Form.NFC);
						outCsvPrinter.print(recId);
						Model readRdf = RdfUtil.readRdf(edmRdfXml, org.apache.jena.riot.Lang.RDFXML);
						for (EdmMeasurementSet.EdmMeasurement measurement : measurements) {
							try {
								String[] csvResult = measurement.getCsvResult(readRdf.getResource(uri), edmRdfXml);
								for (String m : csvResult)
									outCsvPrinter.print(m);
							} catch (Exception e) {
								System.err.println("Error in: " + uri);
								System.err.println(e.getMessage());
							}
						}
						outCsvPrinter.println();

						okRecs++;
					} catch (RiotException e) {
						System.err.println("Error reading RDF: " + uri);
						System.err.println(e.getMessage());
//					e.printStackTrace();
					} catch (IOException e) {
						System.err.println("Error reading from repository: " + uri);
						System.err.println(e.getMessage());
//					e.printStackTrace();
					} catch (Exception e) {
						System.err.println("Error: " + uri);
						System.err.println(e.getMessage());							
					}
				}finally {
					try {
						tracker.track(recCnt);
					} catch (IOException e) {
						e.printStackTrace();
						throw new RuntimeException(e.getMessage(), e);
					}
				}
				return true;
			}
		}, offset);
		outCsvPrinter.close();
		outCsvWriter.close();
	}

}
