package inescid.dataaggregation.tests;

import java.util.AbstractMap.SimpleEntry;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.function.library.date;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.model.Tier;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.profile.tiers.EpfTiersCalculator;
import inescid.dataaggregation.dataset.profile.tiers.TiersCalculation;
import inescid.europeanaapi.AccessException;
import inescid.europeanaapi.EuropeanaApiClient;
import inescid.util.MapOfInts;
import inescid.util.RdfUtil;
import inescid.util.ThreadedRunner;
import inescid.util.europeana.EdmRdfUtil;

public class ScriptEvaluateQualityOfIngestedDatasets {
	
	public static void main(String[] args) throws Exception {
		boolean testing=false;
		
		String httpCacheFolder=null;
		if(args!=null)  
			if(args.length>=1) 
				httpCacheFolder = args[0];
		
		if(httpCacheFolder==null) {
			httpCacheFolder="target/http-cache";
			testing=true;
		}
		
		Global.init_componentDataRepository(httpCacheFolder);
		Global.init_componentHttpRequestService();
		Global.init_enableComponentHttpRequestCache();
		
		String[] datasets= testing ?
				new String[] {"10_KB_RiseOfLiteracy_Centsprenten", "50_KB_RiseOfLiteracy_Kinderboeken"} :
					new String[] {"10_KB_RiseOfLiteracy_Centsprenten", "16_RoL_KB_AlbaAmicorum", 
							"50_KB_RiseOfLiteracy_Kinderboeken", //KB
							"2064501_Ag_IRE_UCD_IIIF",//UDC
							"9200579_Ag_UK_WellcomeCollection_IIIF" // Wellcome libraries 
							};	
		
		ThreadedRunner threadedRunner=new ThreadedRunner(3);			
		try {
//			Map<String, SimpleEntry<MapOfInts<String>, MapOfInts<String>>> pfResults= new HashMap<String, SimpleEntry<MapOfInts<String>,MapOfInts<String>>>();
			Map<String, MapOfInts<Tier>[]> pfResults= new HashMap<String, MapOfInts<Tier>[]>();
			EuropeanaApiClient europeanaApi=new EuropeanaApiClient("QdEDkmksy");
			for(String kbDs: datasets) {
				int retrieved=-1;
				MapOfInts<MediaTier> contentTierStats=new MapOfInts<MediaTier>();
				MapOfInts<MetadataTier> metadataTierStats=new MapOfInts<MetadataTier>();
				MapOfInts<MetadataTier> languageTierStats=new MapOfInts<MetadataTier>();
				MapOfInts<MetadataTier> contextualTierStats=new MapOfInts<MetadataTier>();
				MapOfInts<MetadataTier> enablingTierStats=new MapOfInts<MetadataTier>();
				pfResults.put(kbDs, new MapOfInts[] {
					contentTierStats, metadataTierStats, languageTierStats, contextualTierStats, enablingTierStats});
				List<String> recIds = europeanaApi.listDatasetRecordsIds(kbDs);
				System.out.println(kbDs+" "+ recIds.size());
				for(int i=0; i<recIds.size(); i++) {
					if(i!=0 && i%1000==0)
						System.out.println(" "+ i);

					if(testing && i>10)
						break;
						
					final String rId = recIds.get(i);
					threadedRunner.run(new Runnable() {
						@Override
						public void run() {
							try {
								String recordRdfXml = EdmRdfUtil.getRecordRdfXml(rId);
								TiersCalculation tiers = EpfTiersCalculator.calculate(recordRdfXml);
								contentTierStats.incrementTo(tiers.getContent());
								metadataTierStats.incrementTo(tiers.getMetadata());
								languageTierStats.incrementTo(tiers.getLanguage());
								enablingTierStats.incrementTo(tiers.getEnablingElements());
								contextualTierStats.incrementTo(tiers.getContextualClass());
							} catch (Exception e) {
								System.err.println("error in:"+rId);
								e.printStackTrace();
							}
						}
					});
				}
			}
			threadedRunner.awaitTermination(5);
			for(Entry<String, MapOfInts<Tier>[]> dsResult: pfResults.entrySet()) {
				System.out.println(dsResult.getKey()+"\n");
				for(MapOfInts<Tier> stat: dsResult.getValue()) {
					System.out.println(" - " + stat.getSortedEntries().get(0).getKey().getClass().getName() );
					for(Entry<Tier, SimpleEntry<Integer, Double>> v: stat.getSortedEntriesWithPercent()) {
						System.out.printf("    - %s : %d (%.1f)\n", v.getKey(), v.getValue().getKey(), v.getValue().getValue());
					}
				}
				System.out.println();
			}
		} catch (AccessException e) {
			System.err.println(e.getResponse());
			System.err.println(e.getExceptionSummary());
			e.printStackTrace();
		} finally { 
		}	
	}
	
	
}
