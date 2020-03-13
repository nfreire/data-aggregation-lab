package inescid.dataaggregation.casestudies.coreference.old;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import inescid.dataaggregation.casestudies.coreference.Consts;
import inescid.dataaggregation.casestudies.coreference.EuropeanaSameAsSets;
import inescid.dataaggregation.casestudies.coreference.RepositoryOfSameAs;
import inescid.dataaggregation.casestudies.coreference.SameAsSets;

public class ScriptCoreferenceFinder {
	RepositoryOfSameAs repoSameAs;
	EuropeanaSameAsSets sameAsSetsEuropeana;
	String datasetIdEuropeana;

	public ScriptCoreferenceFinder(String repoFolder, String urisCsvPath, String datasetIdEuropeana) throws IOException {
		repoSameAs = new RepositoryOfSameAs(new File(repoFolder));
		this.datasetIdEuropeana = datasetIdEuropeana;
		
		sameAsSetsEuropeana=new EuropeanaSameAsSets(urisCsvPath, repoSameAs.getSameAsSet(datasetIdEuropeana));
	}

	public static void main(String[] args) throws Exception {
    	String csvUrisFile = "c://users/nfrei/desktop/data/coreference/agents.coref.csv";
    	String repoFolder = "c://users/nfrei/desktop/data/coreference";
    	String datasetIdEuropeana=Consts.europeanaProviders_datasetId;

		if(args!=null) {
			if(args.length>=1) {
				csvUrisFile = args[0];
				if(args.length>=2) {
					repoFolder = args[1];
				}
			}
		}
//		Global.init_componentDataRepository(repoFolder);

		ScriptCoreferenceFinder corefFinder=new ScriptCoreferenceFinder(repoFolder, csvUrisFile, datasetIdEuropeana);
		corefFinder.runSearchCoreferences();
		corefFinder.close();
				
		System.out.println("FININSHED: "+datasetIdEuropeana);
	}

	private void close() {
		repoSameAs.close();		
	}

	private void runSearchCoreferences() throws Exception {
		int cntProcessed=0;
		int cntCoref=0;
		
		MVMap<String, Set<String>> uriIndexEuropeana = sameAsSetsEuropeana.getUriIndex();

		ArrayList<SameAsSets> searchTargets=new ArrayList<SameAsSets>();
		searchTargets.add(repoSameAs.getSameAsSet(Consts.wikidata_datasetId));
		searchTargets.add(repoSameAs.getSameAsSet(Consts.europeanaProviders2nd_datasetId));
		searchTargets.add(repoSameAs.getSameAsSet(Consts.dbPedia_datasetId));
		searchTargets.add(repoSameAs.getSameAsSet(Consts.dataBnfFr_datasetId));
		searchTargets.add(repoSameAs.getSameAsSet(Consts.gnd_datasetId));
		searchTargets.add(repoSameAs.getSameAsSet(Consts.getty_datasetId));
		
	     MVStore.TxCounter txCounter = uriIndexEuropeana.getStore().registerVersionUsage();
	     try {
	    	 for(String uri: sameAsSetsEuropeana.keySetOfVocabUris()) {
//	 			System.out.println(uri);
	    		 Set<String> ySet = sameAsSetsEuropeana.getUriSet(uri);
	    		 int originalSize=ySet.size();
	    		 int cicleStartSize=originalSize;
	    		 int cicleEndSize=-1;
	    		 if(originalSize>1)
	    			 System.out.println(uri);
	    		 for(int cicle=1 ; cicleEndSize!=cicleStartSize ; cicle++) {
	    			 cicleStartSize=ySet.size();
	    			 if(cicle>1)
	    				 System.out.println(uri+ "cicle "+cicle);
		 			for(SameAsSets target: searchTargets) {
		 				Set<String> foundSet = new HashSet<String>();
		 				for(String uriInSet: ySet) {
		 					Set<String> setInTarget = target.getUriIndex().get(uriInSet);
		 					if(setInTarget!=null)
		 						foundSet.addAll(setInTarget);
		 				}
		 				if(!foundSet.isEmpty())
		 					ySet=sameAsSetsEuropeana.addToUri(uri, foundSet);
		 			}
		 			cicleEndSize=ySet.size();
	    		 }
	    		 if(cicleEndSize!=originalSize)
	    			 cntCoref++;

    			 cntProcessed++;
    			 if(cntProcessed % 1000 == 0) { 
    				 System.out.println(cntProcessed +" checked; - "+cntCoref+" corefs");
    				 sameAsSetsEuropeana.getUriIndex().getStore().commit();		
    			 }
	    	 }
	    	 System.out.println("FINAL: "+cntProcessed +" checked; - "+cntCoref+" corefs");
	     } finally {
	    	 uriIndexEuropeana.getStore().deregisterVersionUsage(txCounter);
	     }
		
	     sameAsSetsEuropeana.getUriIndex().getStore().commit();		
	}

	
}
