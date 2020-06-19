package inescid.europeana.dataprocessing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RiotException;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import eu.europeana.corelib.edm.utils.EdmUtils;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.tiers.model.MetadataTier;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.dataset.profile.tiers.EpfTiersCalculator;
import inescid.dataaggregation.dataset.profile.tiers.TiersCalculation;
import inescid.europeana.dataprocessing.LangTagsResult.IN;
import inescid.europeana.dataprocessing.LangTagsResult.SOURCE;
import inescid.europeanarepository.EdmMongoServer;
import inescid.europeanarepository.EdmMongoServer.Handler;
import inescid.util.RdfUtil;
import inescid.util.datastruct.MapOfInts;

public class ProcessRepositoryForLangTagsAndTiers {

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
		
		// INIT OPERATIONS
		LangTagsHandler langTagsHandler=new LangTagsHandler(outputFolder);
		MetadataTiersHandler tiersHandler=new MetadataTiersHandler(outputFolder);
		
		// INIT OPERATIONS - END

		final ProgressTrackerOnFile tracker=new ProgressTrackerOnFile(new File(outputFolder, ProcessRepositoryForLangTagsAndTiers.class.getSimpleName()+"_progress.txt"));
		final int offset=tracker.getTokenAsInt();
		System.out.println("Starting at mongo offset "+offset);

		try {
			edmMongo.forEach(FullBeanImpl.class, new Handler<FullBeanImpl>() {
				Date start = new Date();
				int recCnt = offset;
				int okRecs = 0;
				
				public boolean handle(FullBeanImpl fb) {
					try {
						String recId = fb.getAbout().substring(1);
						recCnt++;
						if (recCnt % 10000 == 0 || recCnt == 10) {
	//						csvOut.flush();
	//						csvBuffer.flush();
							Date now = new Date();
							long elapsedPerRecord = (now.getTime() - start.getTime()) / recCnt;
							double recsMinute = 60000 / (elapsedPerRecord==0 ? 1 : elapsedPerRecord);						
							double hoursToEnd = (double)(58000000 - recCnt) /60d / recsMinute;
							double minutesToEnd = ((double)(58000000 - recCnt) / recsMinute) % 60;
//							double minutesToEnd = (double)(58000000 - recCnt) / recsMinute;
							System.out.printf("%d recs. (%d ok) - %d recs/min - %d:%d to end\n", recCnt, okRecs,
									(int) recsMinute, (int) hoursToEnd, (int) minutesToEnd);
						}
						
						// CHECK PROCESSED ALREADY

						// CHECK PROCESSED ALREADY - END
						
						try {
							String edmRdfXml = EdmUtils.toEDM(fb);
							edmRdfXml=Normalizer.normalize(edmRdfXml, Form.NFC);
							
							// CALL OPERATIONS
							Model edm = RdfUtil.readRdf(edmRdfXml, org.apache.jena.riot.Lang.RDFXML);
							
							langTagsHandler.handle(recId, edm, recCnt);
							tiersHandler.handle(recId, edmRdfXml, recCnt);
							// CALL OPERATIONS - END
							okRecs++;
							
							//DEBUG !!!!!!!!!!!!!!
//							if(okRecs>1000)
//								return false;
							
						} catch (RiotException e) {
							System.err.println("Error reading RDF: " + recId);
							System.err.println(e.getMessage());
						} catch (Exception e) {
							System.err.println("Error: " + recId);
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
		} finally {
			// CLOSE OPERATIONS
			langTagsHandler.finalize();
			tiersHandler.finalize();
			// CLOSE OPERATIONS - END
		}
//		csvOut.close();
//		csvBuffer.close();
	}

	
	public static class LangTagsHandler {
		LangTagsResult overalResult=new LangTagsResult();
		String outputFolder;
		
		public LangTagsHandler(String outputFolder) {
			super();
			this.outputFolder = outputFolder;
		}
		public void handle(String choUri, Model edm, int recCnt) {
			HashSet<Resource> processedResources=new HashSet<Resource>();
			
			LangTagsResult res=new LangTagsResult();
			for(Resource proxy:edm.listResourcesWithProperty(Rdf.type, Ore.Proxy).toList()) {
				processedResources.add(proxy);
				Statement europeanaProxySt = proxy.getProperty(Edm.europeanaProxy);
				SOURCE src=europeanaProxySt!=null && europeanaProxySt.getObject().asLiteral().getBoolean() ? SOURCE.EUROPEANA : SOURCE.PROVIDER;
				for(Statement st: proxy.listProperties().toList()) {
					if(st.getObject().isResource()) {
						processInnerResource(res, st.getObject().asResource(), src, processedResources);
					}else if(st.getObject().isLiteral()) {
						String lang = st.getObject().asLiteral().getLanguage();
						if(!StringUtils.isEmpty(lang))
							res.inc(src, lang, LangTagsResult.IN.CHO);
					}
				}
			}
			overalResult.add(res);
			if (recCnt % 10000 == 0) {
				try {
					FileUtils.write(new File(outputFolder, "langtags-europeana-cho.csv.recs.txt"), String.valueOf(recCnt), StandardCharsets.UTF_8);
					finalize();
				} catch (IOException e) {
					System.err.println("Warning");
					e.printStackTrace();
				}
			}
		}
		
		public void finalize() throws IOException {
			BufferedWriter writer=Files.newBufferedWriter(new File(outputFolder, "langtags-europeana-cho.csv").toPath(), StandardCharsets.UTF_8);
			MapOfInts.writeCsv(overalResult.getTagsInEuropeana(),writer);
			writer.close();
			writer=Files.newBufferedWriter(new File(outputFolder, "langtags-europeana-context.csv").toPath(), StandardCharsets.UTF_8);
			MapOfInts.writeCsv(overalResult.getTagsInEuropeanaContext(),writer);
			writer.close();

			writer=Files.newBufferedWriter(new File(outputFolder, "langtags-provider-cho.csv").toPath(), StandardCharsets.UTF_8);
			MapOfInts.writeCsv(overalResult.getTagsInProvider(),writer);
			writer.close();
			writer=Files.newBufferedWriter(new File(outputFolder, "langtags-provider-context.csv").toPath(), StandardCharsets.UTF_8);
			MapOfInts.writeCsv(overalResult.getTagsInProviderContext(),writer);
			writer.close();
			
			LangTagsResult mergedResult=new LangTagsResult();
			mergedResult.getTagsInEuropeana().addToAll(overalResult.getTagsInEuropeana());
			mergedResult.getTagsInEuropeana().addToAll(overalResult.getTagsInEuropeanaContext());
			writer=Files.newBufferedWriter(new File(outputFolder, "langtags-europeana.csv").toPath(), StandardCharsets.UTF_8);
			MapOfInts.writeCsv(mergedResult.getTagsInEuropeana(),writer);
			writer.close();
			
			mergedResult.getTagsInProvider().addToAll(overalResult.getTagsInProvider());
			mergedResult.getTagsInProvider().addToAll(overalResult.getTagsInProviderContext());
			writer=Files.newBufferedWriter(new File(outputFolder, "langtags-provider.csv").toPath(), StandardCharsets.UTF_8);
			MapOfInts.writeCsv(mergedResult.getTagsInProvider(),writer);
			writer.close();
		}

		private void processInnerResource(LangTagsResult res, Resource resource, SOURCE src, HashSet<Resource> processedResources) {
			LangTagsResult.IN in=IN.CONTEXT;
			Statement type = resource.getProperty(Rdf.type);
			if(type!=null && (type.getObject().equals(Edm.ProvidedCHO) || type.getObject().equals(Ore.Proxy)))
				in=IN.CHO;
			if(processedResources.contains(resource))
				return;
			processedResources.add(resource);
			for(Statement st: resource.listProperties().toList()) {
				if(st.getObject().isResource()) {
					processInnerResource(res, st.getObject().asResource(), src, processedResources);
				}else if(st.getObject().isLiteral()) {
					String lang = st.getObject().asLiteral().getLanguage();
					if(!StringUtils.isEmpty(lang))
						res.inc(src, lang, in);
				}
			}
		}
	}

	public static class MetadataTiersHandler {
		HashMap<String, MapOfInts<MetadataTier>> statsByColByTier=new HashMap<String, MapOfInts<MetadataTier>>();
		HashMap<String, MapOfInts<MetadataTier>> statsByColByTierLang=new HashMap<String, MapOfInts<MetadataTier>>();
		HashMap<String, MapOfInts<MetadataTier>> statsByColByTierContext=new HashMap<String, MapOfInts<MetadataTier>>();
		HashMap<String, MapOfInts<MetadataTier>> statsByColByTierEnabling=new HashMap<String, MapOfInts<MetadataTier>>();

		String outputFolder;
		
		public MetadataTiersHandler(String outputFolder) {
			super();
			this.outputFolder = outputFolder;
		}
		
		public void handle(String choUri, String edmRdfXml, int recCnt) {
			try {
				TiersCalculation tiersResult = EpfTiersCalculator.calculate(edmRdfXml);
				String eCol=choUri.substring(0, choUri.lastIndexOf('/'));
				eCol=eCol.substring(eCol.lastIndexOf('/')+1);
				
				MapOfInts<MetadataTier> colStats = statsByColByTier.get(eCol);
				if(colStats==null ) {
					colStats=new MapOfInts<MetadataTier>();
					statsByColByTier.put(eCol, colStats);
					statsByColByTierLang.put(eCol, new MapOfInts<MetadataTier>());
					statsByColByTierContext.put(eCol, new MapOfInts<MetadataTier>());
					statsByColByTierEnabling.put(eCol, new MapOfInts<MetadataTier>());
				}
				colStats.incrementTo(tiersResult.getMetadata());
				statsByColByTierLang.get(eCol).incrementTo(tiersResult.getLanguage());
				statsByColByTierContext.get(eCol).incrementTo(tiersResult.getContextualClass());
				statsByColByTierEnabling.get(eCol).incrementTo(tiersResult.getEnablingElements());
			} catch (Exception e) {
				System.err.println("Error in URI (skipped) "+choUri);
				e.printStackTrace();
			}
			if (recCnt % 10000 == 0) {
				try {
					FileUtils.write(new File(outputFolder, "tiers.csv.recs.txt"), String.valueOf(recCnt), StandardCharsets.UTF_8);
					finalize();
				} catch (IOException e) {
					System.err.println("Warning");
					e.printStackTrace();
				}
			}
		}
		
		public void finalize() throws IOException {
			StringBuilder sb=new StringBuilder();
			CSVPrinter csv=new CSVPrinter(sb, CSVFormat.DEFAULT);
			
			ArrayList<String> cols=new ArrayList<>(statsByColByTier.keySet());
			Collections.sort(cols);
			for(String col: cols) {
				MapOfInts<MetadataTier> colStats = statsByColByTier.get(col);
				csv.printRecord("Collection:", col);
				csv.print("Metadata Tier/Subtier");
				for(MetadataTier t: MetadataTier.values()) 
					csv.print(t.name());
				csv.println();
				
				csv.print("Metadata Tier");
				for(MetadataTier t: MetadataTier.values()) {
					Integer cnt = colStats.get(t);
					csv.print(cnt);
				}
				csv.println();
				colStats = statsByColByTierEnabling.get(col);
				csv.print(" - Enabling elements");
				for(MetadataTier t: MetadataTier.values()) {
					Integer cnt = colStats.get(t);
					csv.print(cnt);
				}
				csv.println();
				colStats = statsByColByTierLang.get(col);
				csv.print(" - Language");
				for(MetadataTier t: MetadataTier.values()) {
					Integer cnt = colStats.get(t);
					csv.print(cnt);
				}
				csv.println();
				colStats = statsByColByTierContext.get(col);
				csv.print(" - Context");
				for(MetadataTier t: MetadataTier.values()) {
					Integer cnt = colStats.get(t);
					csv.print(cnt);
				}
				csv.println();
			}
			
			FileUtils.write(new File(outputFolder, "tiers.csv"), sb.toString(), StandardCharsets.UTF_8);
		}
	}
	
	
}
