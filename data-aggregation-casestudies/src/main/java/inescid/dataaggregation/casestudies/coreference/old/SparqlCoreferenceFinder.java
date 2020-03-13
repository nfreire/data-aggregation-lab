package inescid.dataaggregation.casestudies.coreference.old;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import inescid.dataaggregation.casestudies.coreference.RepositoryOfSameAs;

public class SparqlCoreferenceFinder {
	RepositoryOfSameAs repoSameAs;
	inescid.dataaggregation.casestudies.coreference.SameAsSets sameAsSetsX;
	inescid.dataaggregation.casestudies.coreference.SameAsSets sameAsSetsY;
	inescid.dataaggregation.casestudies.coreference.SameAsSets sameAsSetsOut;
	String datasetIdX;
	String datasetIdY;
	String datasetIdOut;

	public SparqlCoreferenceFinder(String repoFolder, String datasetIdX, String datasetIdY, String datasetIdOut) {
		repoSameAs = new RepositoryOfSameAs(new File(repoFolder));
		this.datasetIdX = datasetIdX;
		this.datasetIdY = datasetIdY;
		this.datasetIdOut = datasetIdOut;
	}

	private void init() {
		sameAsSetsX=repoSameAs.getSameAsSet(datasetIdX);
		sameAsSetsY=repoSameAs.getSameAsSet(datasetIdY);
		sameAsSetsOut=repoSameAs.getSameAsSet(datasetIdOut);
	}
	
	public static void main(String[] args) throws Exception {
    	String repoFolder = "c://users/nfrei/desktop/data/coreference";
//    	String repoFolderOut = "c://users/nfrei/desktop/data/coreference-updates";
    	String datasetIdX;
    	String datasetIdY;
//    	datasetId ="wikidata";
//    	datasetId ="data.bnf.fr";
//    	datasetId ="dbpedia";
    	datasetIdX ="wikidata";
//    	datasetIdY ="dbpedia";
    	datasetIdY ="data.bnf.fr";
    	
		if(args!=null) {
			if(args.length>=1) {
				repoFolder = args[0];
				if(args.length>=2) {
					datasetIdX = args[1];
					if(args.length>=3) 
						datasetIdY = args[2];
				}
			}
		}
//		Global.init_componentDataRepository(repoFolder);

		SparqlCoreferenceFinder corefFinder=new SparqlCoreferenceFinder(repoFolder, datasetIdX, datasetIdY, datasetIdX+"-enriched");
		corefFinder.init();
		corefFinder.runSearchCoreferences();
		corefFinder.close();
				
		System.out.println("FININSHED: "+datasetIdX +" - " + datasetIdY);
	}

	private void close() {
		repoSameAs.close();		
	}

	private void runSearchCoreferences() throws Exception {
		int cntProcessed=0;
		int cntCoref=0;
		
		MVMap<String, Set<String>> uriIndexX = sameAsSetsX.getUriIndex();
		MVMap<String, Set<String>> uriIndexY = sameAsSetsY.getUriIndex();
		MVMap<String, Set<String>> uriIndexOut = sameAsSetsOut.getUriIndex();
				
	     MVStore.TxCounter txCounter = uriIndexX.getStore().registerVersionUsage();
	     try {
	    	 for(String uri: uriIndexX.keySet()) {
//	 			System.out.println(uri);
	 			Set<String> ySet = uriIndexY.get(uri);
	 			if(ySet!=null) { 
	 				HashSet<String> union=new HashSet<String>(ySet);
		 			if(uriIndexOut.containsKey(uri))
		 				union.addAll(uriIndexOut.get(uri));
		 			else
		 				union.addAll(uriIndexX.get(uri));
	 				if(union.size()>ySet.size()) {
	 					sameAsSetsOut.putSameAsSet(union);					
	 					cntCoref++;
	 				}
	 			}
	 			cntProcessed++;
	 			if(cntProcessed % 50000 == 0) { 
	 				System.out.println(cntProcessed +" checked; - "+cntCoref+" corefs");
	 				uriIndexOut.getStore().commit();		
	 			}
	 		};
	     } finally {
	    	 uriIndexX.getStore().deregisterVersionUsage(txCounter);
	     }
		
		uriIndexOut.getStore().commit();		
	}
	

	
}
