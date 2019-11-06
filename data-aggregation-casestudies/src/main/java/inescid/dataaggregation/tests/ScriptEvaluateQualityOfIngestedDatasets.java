package inescid.dataaggregation.tests;

import java.util.AbstractMap.SimpleEntry;
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

import inescid.dataaggregation.data.RdfReg;
import inescid.europeanaapi.AccessException;
import inescid.europeanaapi.EuropeanaApiClient;
import inescid.util.MapOfInts;
import inescid.util.RdfUtil;
import inescid.util.ThreadedRunner;
import inescid.util.europeana.EdmRdfUtil;

public class ScriptEvaluateQualityOfIngestedDatasets {
	
	public static void main(String[] args) throws Exception {
		ThreadedRunner threadedRunner=new ThreadedRunner(3);			
		try {
			Map<String, SimpleEntry<MapOfInts<String>, MapOfInts<String>>> pfResults= new HashMap<String, SimpleEntry<MapOfInts<String>,MapOfInts<String>>>();
			EuropeanaApiClient europeanaApi=new EuropeanaApiClient("QdEDkmksy");
			for(String kbDs: new String[] {"10_KB_RiseOfLiteracy_Centsprenten", "16_RoL_KB_AlbaAmicorum", 
					"50_KB_RiseOfLiteracy_Kinderboeken", //KB
					"2064501_Ag_IRE_UCD_IIIF",//UDC
					"9200579_Ag_UK_WellcomeCollection_IIIF" // Wellcome libraries 
					}) {
				int retrieved=-1;
				MapOfInts<String> contentTierStats=new MapOfInts<String>();
				MapOfInts<String> metadataTierStats=new MapOfInts<String>();
				pfResults.put(kbDs, new SimpleEntry<MapOfInts<String>, MapOfInts<String>>(contentTierStats, metadataTierStats));
				List<String> recIds = europeanaApi.listDatasetRecordsIds(kbDs);
				System.out.println(kbDs+" "+ recIds.size());
				for(int i=0; i<recIds.size(); i++) {
					if(i!=0 && i%1000==0)
						System.out.println(" "+ i);
					final String rId = recIds.get(i);
					threadedRunner.run(new Runnable() {
						@Override
						public void run() {
							try {
								Model recMdl = europeanaApi.getRecord(rId);
								Resource agg = EdmRdfUtil.getEuropeanaAggregationResource(recMdl);
								for (StmtIterator qAnnStms=agg.listProperties(RdfReg.DQV_HAS_QUALITY_ANNOTATION) ; qAnnStms.hasNext() ; ) {
									Statement stm = qAnnStms.next();
									Resource qAnnotRes = stm.getObject().asResource();
		//							for (StmtIterator stms=qAnnotRes.listProperties() ; stms.hasNext() ; ) {
		//	 							Statement stm2 = stms.next();
		//								System.out.println(stm2);							
		//							}
									String qVal = RdfUtil.getUriOrLiteralValue(qAnnotRes.getProperty(RdfReg.OA_HAS_BODY).getObject());
									if(qVal.contains("contentTier"))
										contentTierStats.incrementTo(qVal);
									else
										metadataTierStats.incrementTo(qVal);
								}
							} catch (AccessException e) {
								System.err.println("error in:"+rId+"\n"+e.getExceptionSummary());
								e.printStackTrace();
							} catch (Exception e) {
								System.err.println("error in:"+rId);
								e.printStackTrace();
							}
						}
					});
				}
			}
			threadedRunner.shutdown();
			for(Entry<String, SimpleEntry<MapOfInts<String>, MapOfInts<String>>> dsResult: pfResults.entrySet()) {
				System.out.println(dsResult.getKey()+"\n - Content:");
				for(Entry<String, SimpleEntry<Integer, Double>> v: dsResult.getValue().getKey().getSortedEntriesWithPercent()) {
					System.out.printf("    - %s : %d (%.1f)\n", v.getKey(), v.getValue().getKey(), v.getValue().getValue());
				}
				for(Entry<String, SimpleEntry<Integer, Double>> v: dsResult.getValue().getValue().getSortedEntriesWithPercent()) {
					System.out.printf("    - %s : %d (%.1f)\n", v.getKey(), v.getValue().getKey(), v.getValue().getValue());
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
