package inescid.dataaggregation.dataset.profile.multilinguality;

import java.util.AbstractMap;
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
import org.apache.jena.sparql.engine.optimizer.StatsMatcher;
import org.apache.jena.sparql.function.library.date;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.model.Tier;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.profile.tiers.EpfTiersCalculator;
import inescid.europeanaapi.AccessException;
import inescid.europeanaapi.EuropeanaApiClient;
import inescid.util.MapOfInts;
import inescid.util.RdfUtil;
import inescid.util.StatisticCalcMean;
import inescid.util.ThreadedRunner;
import inescid.util.europeana.EdmRdfUtil;

public class ScriptEvaluateMlSaturationOfIngestedDatasets {
	
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
				new String[] {"50_KB_RiseOfLiteracy_Kinderboeken"} :
					new String[] {"10_KB_RiseOfLiteracy_Centsprenten", "16_RoL_KB_AlbaAmicorum", 
							"50_KB_RiseOfLiteracy_Kinderboeken", //KB
//							"2064501_Ag_IRE_UCD_IIIF",//UDC
//							"9200579_Ag_UK_WellcomeCollection_IIIF" // Wellcome libraries 
							};	
					
		
		ThreadedRunner threadedRunner=new ThreadedRunner(3);			
		try {
			
//			Map<String, SimpleEntry<MapOfInts<String>, MapOfInts<String>>> pfResults= new HashMap<String, SimpleEntry<MapOfInts<String>,MapOfInts<String>>>();
			Map<String, Map.Entry<StatisticCalcMean, StatisticCalcMean>> pfResults= new HashMap<>();
			EuropeanaApiClient europeanaApi=new EuropeanaApiClient("QdEDkmksy");
			for(String kbDs: datasets) {
				StatisticCalcMean langTagCountStats = new StatisticCalcMean();
				StatisticCalcMean languagesCountStats = new StatisticCalcMean();
				pfResults.put(kbDs, new AbstractMap.SimpleImmutableEntry<StatisticCalcMean, StatisticCalcMean>(langTagCountStats, languagesCountStats));
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
								Model recordMdl = EdmRdfUtil.getRecord(rId);
								MultilingualSaturationResult result = MultilingualSaturation.calculate(recordMdl);
								langTagCountStats.enter(result.getLangTagCount());
								languagesCountStats.enter(result.getLanguagesCount());
							} catch (Exception e) {
								System.err.println("error in:"+rId);
								e.printStackTrace();
							}
						}
					});
				}
			}
			threadedRunner.awaitTermination(5);
			for(Entry<String, Entry<StatisticCalcMean, StatisticCalcMean>> dsResult: pfResults.entrySet()) {
				System.out.println(dsResult.getKey()+"\n");
				System.out.println("lang tag stats: "+dsResult.getValue().getKey().toString());
				System.out.println("languages stats: "+dsResult.getValue().getValue().toString());
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
