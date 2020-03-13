package inescid.dataaggregation.casestudies.coreference.old;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import inescid.dataaggregation.casestudies.coreference.Consts;
import inescid.dataaggregation.casestudies.coreference.EuropeanaSameAsSets;
import inescid.dataaggregation.casestudies.coreference.RepositoryOfSameAs;
import inescid.dataaggregation.casestudies.coreference.SameAsSets;
import inescid.dataaggregation.dataset.Global;

public class ScriptCoreferenceIngesterEuropeana2ndLevel {
	RepositoryOfSameAs repoSameAs;
	EuropeanaSameAsSets sameAsSetsEuropeana;
	SameAsSets sameAsSetsOut;
	String datasetIdEuropeana;
	String datasetIdOut;

	public ScriptCoreferenceIngesterEuropeana2ndLevel(String repoFolder, String urisCsvPath, String datasetIdEuropeana, String datasetIdOut) throws IOException {
		repoSameAs = new RepositoryOfSameAs(new File(repoFolder));
		this.datasetIdEuropeana = datasetIdEuropeana;
		this.datasetIdOut = datasetIdOut;
		
		sameAsSetsEuropeana=new EuropeanaSameAsSets(urisCsvPath, repoSameAs.getSameAsSet(datasetIdEuropeana));
		sameAsSetsOut=repoSameAs.getSameAsSet(datasetIdOut);
	}

	public static void main(String[] args) throws Exception {
    	String csvUrisFile = "c://users/nfrei/desktop/data/coreference/agents.coref.csv";
    	String repoFolder = "c://users/nfrei/desktop/data/coreference";
    	String datasetIdOut=Consts.europeanaProviders2nd_datasetId;
    	String datasetIdEuropeana=Consts.europeanaProviders_datasetId;

		if(args!=null) {
			if(args.length>=1) {
				csvUrisFile = args[0];
				if(args.length>=2) {
					repoFolder = args[1];
					if(args.length>=3) {
						datasetIdOut = args[2];
					}
				}
			}
		}
		Global.init_componentHttpRequestService();

		ScriptCoreferenceIngesterEuropeana2ndLevel corefFinder=new ScriptCoreferenceIngesterEuropeana2ndLevel(repoFolder, csvUrisFile, datasetIdEuropeana, datasetIdOut);
		corefFinder.runIngestCoreferences();
		corefFinder.close();
				
		System.out.println("FININSHED: "+datasetIdEuropeana +" - " + datasetIdOut);
	}

	private void close() {
		repoSameAs.close();		
	}

	private void runIngestCoreferences() throws Exception {
		int cntProcessed=0;
		int cntCoref=0;
		
		sameAsSetsOut.clear();
		sameAsSetsOut.commit();
		
		MVMap<String, Set<String>> uriIndexEuropeana = sameAsSetsEuropeana.getUriIndex();
		MVMap<String, Set<String>> uriIndexOut = sameAsSetsOut.getUriIndex();
		
	     MVStore.TxCounter txCounter = uriIndexEuropeana.getStore().registerVersionUsage();
	     try {
	    	 for(String uri: sameAsSetsEuropeana.keySetOfVocabUris()) {
	    		Set<String> ySet = sameAsSetsEuropeana.getUriSet(uri);
	    		Set<String> foundSet=new HashSet();
 				for(String uriInSet: ySet) {
 					if(uriInSet.equals(uri))
 						continue;
 					Set<String> uriSet=ScriptCoreferenceIngesterEuropeanaVocabUris.checkAndGetResourceSameAs(uriInSet);
 					if(uriSet!=null)
 						foundSet.addAll(uriSet);
 				}
 				if(!foundSet.isEmpty()) {
 					int ySize=ySet.size();
 					ySet.addAll(foundSet);
 					cntCoref+=ySet.size()-ySize;
 					sameAsSetsOut.addSet(ySet);
 				}

    			 cntProcessed++;
    			 if(cntProcessed % 100 == 0) { 
    				 System.out.println(cntProcessed +" checked; - "+cntCoref+" corefs");
    				 uriIndexOut.getStore().commit();		
    			 }
	    	 }
	    	 System.out.println("FINAL: "+cntProcessed +" checked; - "+cntCoref+" corefs");
	     } finally {
	    	 uriIndexEuropeana.getStore().deregisterVersionUsage(txCounter);
	     }
		
		uriIndexOut.getStore().commit();		
	}
	

	
}
