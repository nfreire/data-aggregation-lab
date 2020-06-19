package inescid.dataaggregation.casestudies.coreference.old;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.query.QuerySolution;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import inescid.dataaggregation.casestudies.coreference.RepositoryOfSameAs;
import inescid.dataaggregation.casestudies.coreference.SameAsSets;
import inescid.dataaggregation.wikidata.RdfRegWikidata;
import inescid.dataaggregation.wikidata.WikidataUtil;
import inescid.dataaggregation.wikidata.SparqlClientWikidata;
import inescid.dataaggregation.data.model.Owl;
import inescid.dataaggregation.dataset.Global;
import inescid.util.SparqlClient;
import inescid.util.SparqlClient.Handler;
import inescid.util.datastruct.MapOfInts;

public class ScriptSparqlCoreferenceTestRepo {
	RepositoryOfSameAs repoSameAs;
	SameAsSets sameAsSetsX;
	String datasetIdX;

	public ScriptSparqlCoreferenceTestRepo(String repoFolder, String datasetIdX) {
		repoSameAs = new RepositoryOfSameAs(new File(repoFolder));
		this.datasetIdX = datasetIdX;
		sameAsSetsX=repoSameAs.getSameAsSet(datasetIdX);
	}

	public static void main(String[] args) throws Exception {
    	String repoFolder = "c://users/nfrei/desktop/data/coreference";
//    	String repoFolderOut = "c://users/nfrei/desktop/data/coreference-updates";
    	String datasetIdX;
//    	datasetId ="wikidata";
//    	datasetId ="data.bnf.fr";
//    	datasetId ="dbpedia";
//    	datasetIdX ="wikidata";
    	datasetIdX ="wikidata-enriched";
    	
		if(args!=null) {
			if(args.length>=1) {
				repoFolder = args[0];
				if(args.length>=2) {
					datasetIdX = args[1];
				}
			}
		}
//		Global.init_componentDataRepository(repoFolder);

		ScriptSparqlCoreferenceTestRepo corefFinder=new ScriptSparqlCoreferenceTestRepo(repoFolder, datasetIdX);
		corefFinder.runTest();
		corefFinder.close();
				
		System.out.println("FININSHED TEST: "+datasetIdX );
	}

	private void close() {
		repoSameAs.close();		
	}

	private void runTest() throws Exception {
		MapOfInts<String> statsPerDomain=new MapOfInts<String>();
		Set<String> domains = new HashSet<String>(20);

		int cntProcessed=0;
		MVMap<String, Set<String>> uriIndexX = sameAsSetsX.getUriIndex();
		for(String uri: uriIndexX.keySet()) {
			Set<String> ySet = uriIndexX.get(uri);
			domains.clear();
			
//			System.out.println(ySet);
			
			for(String uri2: ySet) { 
				try {
					String host = new URI(uri2).getHost();
					if(host != null)
						domains.add(host);
				} catch (URISyntaxException e) {
					uri2 = uri2.substring(0, uri2.lastIndexOf('/')+1) + URLEncoder.encode(uri2.substring(uri2.lastIndexOf('/')+1), "UTF-8");
					try {
						String host = new URI(uri2).getHost();
						if(host != null)
							domains.add(host);
					} catch (URISyntaxException e2) {
						e2.printStackTrace();
					}
				}
			}
			for(String domain: domains)  
				statsPerDomain.incrementTo(domain);
			
			cntProcessed++;
			if(cntProcessed % 50000 == 0) { 
				System.out.println(cntProcessed +" checked");
			}
		};
		for(String domain: statsPerDomain.getSortedKeysByInts()) {
			System.out.println(domain+" - "+statsPerDomain.get(domain));
		}
	}
	

	
}
