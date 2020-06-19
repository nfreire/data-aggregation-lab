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
import eu.europeana.corelib.solr.entity.AggregationImpl;
import inescid.dataaggregation.casestudies.coreference.SameAsSets;
import inescid.europeanarepository.EdmMongoServer;
import inescid.europeanarepository.EdmMongoServer.Handler;
import inescid.util.RdfUtil;

public class ProcessRepositoryGetShownAt {

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

		File mapsFile = new File(outputFolder, "shownAt.mvstore.bin");
		if (!mapsFile.getParentFile().exists())
			mapsFile.getParentFile().mkdirs();
		MVStore mvStore = new MVStore.Builder().fileName(mapsFile.getPath()).open();

		MVMap<String, String> idToCsv = mvStore.openMap(mapsFile.getName());
		final ProgressTrackerOnFile tracker=new ProgressTrackerOnFile(new File(outputFolder, ProcessRepositoryGetShownAt.class.getSimpleName()+"_progress.txt"));
		final int offset=tracker.getTokenAsInt();
		System.out.println("Starting at mongo offset "+offset);
		
		try {
			edmMongo.forEach(AggregationImpl.class, new Handler<AggregationImpl>() {
				Date start = new Date();
				int recCnt = offset;
				int okRecs = 0;

				StringBuilder recCsv = new StringBuilder();

				public boolean handle(AggregationImpl fb) {
					try {
						String uri = fb.getAbout();
						String recId = fb.getAbout();
						if(recId.startsWith("/"))
							recId=recId.substring(1);
						recCnt++;
						if (recCnt % 1000 == 0 || recCnt == 10) {
	//						csvOut.flush();
	//						csvBuffer.flush();
							Date now = new Date();
							long elapsedPerRecord = (now.getTime() - start.getTime()) / recCnt;
							double recsMinute = 60000;
							if(elapsedPerRecord!=0)
								recsMinute = 60000 / elapsedPerRecord;
							double minutesToEnd = (58000000 - recCnt) / recsMinute;
							System.out.printf("%d recs. (%d ok) - %d recs/min - %d mins. to end\n", recCnt, okRecs,
									(int) recsMinute, (int) minutesToEnd);
						}
						if (idToCsv.containsKey(recId))
							return true;
						
						try {
							CSVPrinter csvOut = new CSVPrinter(recCsv, CSVFormat.DEFAULT);
							csvOut.print(fb.getEdmIsShownAt());
							csvOut.print(fb.getEdmIsShownBy());
							if(fb.getHasView()!=null)
								for(String v:fb.getHasView())
									csvOut.print(v);
							csvOut.close();
						} catch (IOException e) {
							e.printStackTrace();
							throw new RuntimeException(e.getMessage(), e);
						}
	//					System.out.println(recCsv.toString());
						idToCsv.put(recId, recCsv.toString());
						recCsv.setLength(0);
						okRecs++;
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
		} finally {
			mvStore.close();
		}
//		csvOut.close();
//		csvBuffer.close();
	}

}
