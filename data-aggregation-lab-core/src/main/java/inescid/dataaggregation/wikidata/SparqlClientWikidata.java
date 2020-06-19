package inescid.dataaggregation.wikidata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.query.QuerySolution;

import inescid.util.SparqlClient;
import inescid.util.SparqlClient.Handler;
import inescid.dataaggregation.wikidata.RdfRegWikidata;
import inescid.dataaggregation.wikidata.WikidataUtil;

public class SparqlClientWikidata {
	
	public static final String ENDPOINT_URL="https://query.wikidata.org/sparql"; 
	public static final String PREFFIXES="PREFIX bd: <http://www.bigdata.com/rdf#>\n" +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"PREFIX wikibase: <http://wikiba.se/ontology#>\n" +
            "PREFIX wdt: <http://www.wikidata.org/prop/direct/>\n" +
            "PREFIX wd: <http://www.wikidata.org/entity/>\n"; 
	public static final SparqlClient INSTANCE=new SparqlClient(ENDPOINT_URL, PREFFIXES); 
	
	public static void setDebug(boolean d) {
		INSTANCE.setDebug(d);
	}
	
	
	public static int query(String queryString, final Handler handler) {
		return INSTANCE.query(queryString, handler);
	}
	
	public static int queryWithPaging(String queryString, int resultsPerPage, String orderVariableName, Handler handler) throws Exception {
		return INSTANCE.queryWithPaging(queryString, resultsPerPage, orderVariableName, handler);
	}
	
	public static List<String> getAllSubclassesOfCreativeWork (){
		final List<String> uris=new ArrayList<String>(1000);
		query("SELECT ?subClass WHERE { ?subClass wdt:P279+ wd:Q17537576 .}", new Handler() {
			@Override
			public boolean handleSolution(QuerySolution solution) throws Exception {
				uris.add(solution.getResource("subClass").getURI());
				return true;
			}
		});
		return uris;
	}
	
	public static Set<String> getAllSubClasses(String wdEntityUri){
		final HashSet<String> uris=new HashSet<String>();
		final HashSet<String> newUris=new HashSet<String>();
		newUris.add(wdEntityUri);
		while (!newUris.isEmpty()) {	
			ArrayList<String> tmp = new ArrayList<>(newUris);
			newUris.clear();
			for(String uri: tmp) {
//				if(uris.contains(uri)) continue;
				System.out.println(uri);
				query("SELECT ?subClass WHERE { ?subClass wdt:P279 <"+ WikidataUtil.convertWdUriToQueryableUri(uri)+"> .}", new Handler() {
					@Override
					public boolean handleSolution(QuerySolution solution) throws Exception {
						boolean exists=uris.add(solution.getResource("subClass").getURI());
						if(exists)
							newUris.add(solution.getResource("subClass").getURI());
						return true;
					}
				});
			}
		}
		return uris;
	}
	
	
	
	
	
	public static List<String> getAllSuperclasses(String wdEntityUri){
		final List<String> uris=new ArrayList<String>(1);
		query("SELECT ?superClass WHERE { <"+wdEntityUri+"> wdt:P279+ ?superClass .}", new Handler() {
			@Override
			public boolean handleSolution(QuerySolution solution) throws Exception {
				uris.add(solution.getResource("superClass").getURI());
				return true;
			}
		});
		return uris;
	}
	
	public static List<String> getAllSubProperties(String wdPropertyUri){
		final List<String> uris=new ArrayList<String>();
		query("SELECT ?subClass WHERE { ?subClass wdt:P1647+ <"+WikidataUtil.convertWdUriToQueryableUri(wdPropertyUri)+"> .}", new Handler() {
			@Override
			public boolean handleSolution(QuerySolution solution) throws Exception {
				uris.add(solution.getResource("subClass").getURI());
				return true;
			}
		});
		return uris;
	}
	
	public static List<String> getAllSuperProperties(String wdPropertyUri){
		final List<String> uris=new ArrayList<String>();
		query("SELECT ?superClass WHERE { <"+WikidataUtil.convertWdUriToQueryableUri(wdPropertyUri)+"> wdt:P1647+ ?superClass .}", new Handler() {
			@Override
			public boolean handleSolution(QuerySolution solution) throws Exception {
				uris.add(solution.getResource("superClass").getURI());
				return true;
			}
		});
		return uris;
	}
	
	
//	public static abstract class UriHandler {
//		//return true to continue to next URI, false to abort
//		public boolean handleUri(String uri) throws Exception { return true; };
//		//return true to continue to next URI, false to abort
//		public boolean handleSolution(QuerySolution solution) throws Exception { return true; };
//	}
//	
//	static String QUERY_PREFIX="PREFIX bd: <http://www.bigdata.com/rdf#>\n" +
//			"PREFIX wikibase: <http://wikiba.se/ontology#>\n" +
//            "PREFIX wdt: <http://www.wikidata.org/prop/direct/>\n" +
//            "PREFIX wd: <http://www.wikidata.org/entity/>\n";
//	
//	static String QUERY_SUFFIX="\n}";
////	static String QUERY_SUFFIX="\n  SERVICE wikibase:label { bd:serviceParam wikibase:language \"[AUTO_LANGUAGE],en\". } }";
//	
//	//returns number of uris that where processed
//	public static int query(String queryString, final UriHandler handler) {
//		return querySolutions(queryString, new UriHandler() {
//			@Override
//			public boolean handleSolution(QuerySolution solution) throws Exception {
//				Resource resource = solution.getResource("item");
//				if (!handler.handleUri(resource.getURI())) {
//					System.err.println("RECEIVED HANDLER ABORT");
//					return false;
//				}
//				return true;
//			}
//		});
//	}
//	
//		//returns number of uris that where processed
//	public static int querySolutions(String queryString, UriHandler handler) {
//		int wdCount=0;
//		String fullQuery = QUERY_PREFIX + queryString + QUERY_SUFFIX;
////        System.out.println(fullQuery);
//		QueryExecution qexec = QueryExecutionFactory.sparqlService("https://query.wikidata.org/sparql", fullQuery);
//		try {
//			ResultSet results = qexec.execSelect();
////            ResultSetFormatter.out(System.out, results, query);
//			while(results.hasNext()) {
//				Resource resource = null;
//				try {
//					QuerySolution hit = results.next();
//					if (!handler.handleSolution(hit)) {
//						System.err.println("RECEIVED HANDLER ABORT");
//						break;
//					}
//					wdCount++;
//				} catch (Exception e) {
//					System.err.println("Error on record: "+(resource==null ? "?" : resource.getURI()));
//					e.printStackTrace();
//					System.err.println("PROCEEDING TO NEXT URI");
//				}
//			}
//			System.err.printf("QUERY FINISHED - %d resources\n", wdCount);            
//		} catch (Exception ex) {
//			System.err.println("Error on query: "+fullQuery);
//			ex.printStackTrace();
//		} finally {
//			qexec.close();
//		}
//		return wdCount;
//	}

}
