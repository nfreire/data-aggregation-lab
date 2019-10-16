package inescid.dataaggregation.casestudies.wikidata;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.QuerySolution;

import inescid.util.SparqlClient;
import inescid.util.SparqlClient.Handler;

public class SparqlClientWikidata {
	public static final SparqlClient INSTANCE=new SparqlClient("https://query.wikidata.org/sparql", 
			"PREFIX bd: <http://www.bigdata.com/rdf#>\n" +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"PREFIX wikibase: <http://wikiba.se/ontology#>\n" +
            "PREFIX wdt: <http://www.wikidata.org/prop/direct/>\n" +
            "PREFIX wd: <http://www.wikidata.org/entity/>\n"); 
	
	public static void setDubug(boolean d) {
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
	
//	
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
