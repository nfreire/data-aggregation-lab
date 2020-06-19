package inescid.dataaggregation.casestudies.wikidata.edm;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;

import inescid.dataaggregation.wikidata.SparqlClientWikidata;
import inescid.dataaggregation.wikidata.RdfRegWikidata;
import inescid.dataaggregation.wikidata.WikidataUtil;
import inescid.dataaggregation.data.RdfsClassHierarchy;
import inescid.util.SparqlClient;
import inescid.util.TriplestoreJenaTbd2;
import inescid.util.SparqlClient.Handler;

public class TestGetWikidataHierarchy {

	
	public static void main(String[] args) throws Exception {
		File wikidataTbd2Folder=new File("c://users/nfrei/desktop/data/wikidata-to-edm/triplestore-wikidata");
		RdfsClassHierarchy hierarchy=new RdfsClassHierarchy();
		
		SparqlClient sparqlCl= wikidataTbd2Folder==null ? SparqlClientWikidata.INSTANCE : 
			new TriplestoreJenaTbd2(wikidataTbd2Folder, SparqlClientWikidata.PREFFIXES, ReadWrite.READ);
		
//		String r="http://www.wikidata.org/entity/Q68295960";
		String r="http://www.wikidata.org/entity/Q327333";
		
		
		sparqlCl.query("SELECT ?p ?o  WHERE { <"+WikidataUtil.convertWdUriToQueryableUri(r)+"> ?p ?o.}", new Handler() {
			@Override
			public boolean handleSolution(QuerySolution solution) throws Exception {
				System.out.print(solution.getResource("p").getURI()+" ");
				System.out.println(solution.getResource("o").getURI());
				return true;
			}
		});
		
		
//		
//		
//		
//		String r="http://www.wikidata.org/entity/Q43229";
////		String r="http://www.wikidata.org/entity/Q68295960";
//			for(String scUri : getAllSubClasses(r, sparqlCl)) {
//				hierarchy.setSuperClass(scUri, r);				
//			}
////		for(String r: edmClassMappings.getWikidataPropertiesMapped()) {
////			for(String scUri : getAllSubProperties(r, sparqlCl)) {
////				hierarchy.setSuperProperty(scUri, r);				
////			}
////		}
//		hierarchy.calculateHierarchy();

		if(sparqlCl instanceof TriplestoreJenaTbd2)
			((TriplestoreJenaTbd2)sparqlCl).close();
	}
	
	private static Set<String> getAllSubClasses(String wdEntityUri, SparqlClient sparqlCl) {
		final HashSet<String> uris=new HashSet<String>();
		
		System.out.println(WikidataUtil.convertWdUriToQueryableUri(wdEntityUri));
		
		sparqlCl.query("SELECT ?subClass WHERE { ?subClass wdt:P279+ <"+WikidataUtil.convertWdUriToQueryableUri(wdEntityUri)+"> .}", new Handler() {
			@Override
			public boolean handleSolution(QuerySolution solution) throws Exception {
				uris.add(solution.getResource("subClass").getURI());
				System.out.println(solution.getResource("subClass").getURI());
				return true;
			}
		});
		return uris;
	}
}
