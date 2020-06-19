package inescid.dataaggregation.casestudies.edm.alignment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Date;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RiotException;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import eu.europeana.corelib.edm.utils.EdmUtils;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import inescid.europeana.dataprocessing.ProgressTrackerOnFile;
import inescid.europeanarepository.EdmMongoServer;
import inescid.europeanarepository.EdmMongoServer.Handler;
import inescid.util.RdfUtil;

public class ProcessRepositoryGetUris {
	
	private static boolean DEBUG=false;
	
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

		File mapsFile = new File(outputFolder, "context_uris.mvstore.bin");
		if (!mapsFile.getParentFile().exists())
			mapsFile.getParentFile().mkdirs();
		MVStore mvStore = new MVStore.Builder().fileName(mapsFile.getPath()).open();

		MVMap<String, String> urisConcept = mvStore.openMap("Concept");
		MVMap<String, String> urisPlace = mvStore.openMap("Place");
		MVMap<String, String> urisAgent = mvStore.openMap("Agent");
		MVMap<String, String> urisTimespan = mvStore.openMap("Timespan");
		final ProgressTrackerOnFile tracker=new ProgressTrackerOnFile(new File(outputFolder, ProcessRepositoryGetUris.class.getSimpleName()+"_progress.txt"));
		final int offset=tracker.getTokenAsInt();
		System.out.println("Starting at mongo offset "+offset);
		
		try {
			getConcepts(edmMongo, urisConcept);
			getAgent(edmMongo, urisAgent);
			getPlace(edmMongo, urisPlace);
			getTimespan(edmMongo, urisTimespan);
		} finally {
			mvStore.close();
		}
//		csvOut.close();
//		csvBuffer.close();
	}

	private static void getConcepts(EdmMongoServer edmMongo, MVMap<String, String> urisConcept) {
		edmMongo.forEach(ConceptImpl.class, new Handler<ConceptImpl>() {
			Date start = new Date();
			int recCnt = 0;
			int okRecs = 0;

			public boolean handle(ConceptImpl c) {
//					String recId = fb.getAbout().substring(1);
				recCnt++;
				if (recCnt % 10000 == 0 || recCnt == 10) {
					Date now = new Date();
					long elapsedPerRecord = (now.getTime() - start.getTime()) / recCnt;
					double recsMinute = 60000 / (elapsedPerRecord==0 ? 1 : elapsedPerRecord);						
					System.out.printf("%d recs. (%d ok) - %d recs/min\n", recCnt, okRecs,
							(int) recsMinute);
				}
				// CHECK PROCESSED ALREADY
				// CHECK PROCESSED ALREADY - END
				
				try {
						if(c.getAbout().startsWith("http://") && !c.getAbout().contains("#")
								&& !c.getAbout().contains("europeana.eu/")) {
							urisConcept.put(c.getAbout(), "");
						}
						if(c.getExactMatch()!=null)
							for(String sameAs: c.getExactMatch())
								urisConcept.put(sameAs, "");
					// CALL OPERATIONS - END
					okRecs++;
					
					//DEBUG !!!!!!!!!!!!!!
					if(DEBUG && okRecs>10000)
						return false;
				} catch (Exception e) {
					System.err.println("Error: " + c.getAbout());
					e.printStackTrace();					
				}
				return true;
			}
		});
	}
	
	private static void getPlace(EdmMongoServer edmMongo, MVMap<String, String> urisConcept) {
		edmMongo.forEach(PlaceImpl.class, new Handler<PlaceImpl>() {
			Date start = new Date();
			int recCnt = 0;
			int okRecs = 0;
			
			public boolean handle(PlaceImpl c) {
//					String recId = fb.getAbout().substring(1);
				recCnt++;
				if (recCnt % 10000 == 0 || recCnt == 10) {
					Date now = new Date();
					long elapsedPerRecord = (now.getTime() - start.getTime()) / recCnt;
					double recsMinute = 60000 / (elapsedPerRecord==0 ? 1 : elapsedPerRecord);						
					System.out.printf("%d recs. (%d ok) - %d recs/min\n", recCnt, okRecs,
							(int) recsMinute);
				}
				// CHECK PROCESSED ALREADY
				// CHECK PROCESSED ALREADY - END
				
				try {
						if(c.getAbout().startsWith("http://") && !c.getAbout().contains("#")
								&& !c.getAbout().contains("europeana.eu/")) {
							urisConcept.put(c.getAbout(), "");
						}
						if(c.getOwlSameAs()!=null)
							for(String sameAs: c.getOwlSameAs())
								urisConcept.put(sameAs, "");
					// CALL OPERATIONS - END
					okRecs++;
					
					//DEBUG !!!!!!!!!!!!!!
					if(DEBUG && okRecs>10000)
						return false;
				} catch (Exception e) {
					System.err.println("Error: " + c.getAbout());
					e.printStackTrace();					
				}
				return true;
			}
		});
	}

	private static void getAgent(EdmMongoServer edmMongo, MVMap<String, String> urisConcept) {
		edmMongo.forEach(AgentImpl.class, new Handler<AgentImpl>() {
			Date start = new Date();
			int recCnt = 0;
			int okRecs = 0;
			
			public boolean handle(AgentImpl c) {
//					String recId = fb.getAbout().substring(1);
				recCnt++;
				if (recCnt % 10000 == 0 || recCnt == 10) {
					Date now = new Date();
					long elapsedPerRecord = (now.getTime() - start.getTime()) / recCnt;
					double recsMinute = 60000 / (elapsedPerRecord==0 ? 1 : elapsedPerRecord);						
					System.out.printf("%d recs. (%d ok) - %d recs/min\n", recCnt, okRecs,
							(int) recsMinute);
				}
				// CHECK PROCESSED ALREADY
				// CHECK PROCESSED ALREADY - END
				
				try {
					if(c.getAbout().startsWith("http://") && !c.getAbout().contains("#")
							&& !c.getAbout().contains("europeana.eu/")) {
						urisConcept.put(c.getAbout(), "");
					}
					if(c.getOwlSameAs()!=null)
						for(String sameAs: c.getOwlSameAs())
							urisConcept.put(sameAs, "");
					// CALL OPERATIONS - END
					okRecs++;
					
					//DEBUG !!!!!!!!!!!!!!
					if(DEBUG && okRecs>10000)
						return false;
				} catch (Exception e) {
					System.err.println("Error: " + c.getAbout());
					e.printStackTrace();					
				}
				return true;
			}
		});
	}
	private static void getTimespan(EdmMongoServer edmMongo, MVMap<String, String> urisConcept) {
		edmMongo.forEach(TimespanImpl.class, new Handler<TimespanImpl>() {
			Date start = new Date();
			int recCnt = 0;
			int okRecs = 0;
			
			public boolean handle(TimespanImpl c) {
//					String recId = fb.getAbout().substring(1);
				recCnt++;
				if (recCnt % 10000 == 0 || recCnt == 10) {
					Date now = new Date();
					long elapsedPerRecord = (now.getTime() - start.getTime()) / recCnt;
					double recsMinute = 60000 / (elapsedPerRecord==0 ? 1 : elapsedPerRecord);						
					System.out.printf("%d recs. (%d ok) - %d recs/min\n", recCnt, okRecs,
							(int) recsMinute);
				}
				// CHECK PROCESSED ALREADY
				// CHECK PROCESSED ALREADY - END
				
				try {
					if(c.getAbout().startsWith("http://") && !c.getAbout().contains("#")
							&& !c.getAbout().contains("europeana.eu/")) {
						urisConcept.put(c.getAbout(), "");
					}
					if(c.getOwlSameAs()!=null)
						for(String sameAs: c.getOwlSameAs())
							urisConcept.put(sameAs, "");
					// CALL OPERATIONS - END
					okRecs++;
					
					//DEBUG !!!!!!!!!!!!!!
					if(DEBUG && okRecs>10000)
						return false;
				} catch (Exception e) {
					System.err.println("Error: " + c.getAbout());
					e.printStackTrace();					
				}
				return true;
			}
		});
	}
}
