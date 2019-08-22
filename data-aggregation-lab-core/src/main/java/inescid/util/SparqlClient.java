package inescid.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Resource;

public class SparqlClient {
	public static abstract class Handler {
		//return true to continue to next URI, false to abort
		public boolean handleSolution(QuerySolution solution) throws Exception { return true; };
	}
	
	protected final String baseUrl; 
	protected final String queryPrefix;

	public SparqlClient(String baseUrl, String queryPrefix) {
		super();
		this.baseUrl = baseUrl;
		this.queryPrefix = queryPrefix;
	}
	
	public SparqlClient(String baseUrl, Map<String, String> queryPrefixes) {
		super();
		this.baseUrl = baseUrl;
		String tmp="";		
		for(Entry<String, String> ns : queryPrefixes.entrySet()) {
			tmp+=String.format("PREFIX %s: <%s>\n", ns.getKey(), ns.getValue());
		}
		queryPrefix=tmp;
	}
	
	public int query(String queryString, Handler handler) {
		int wdCount=0;
		String fullQuery = queryPrefix + queryString;
//        System.out.println(fullQuery);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(this.baseUrl, fullQuery);
		try {
			ResultSet results = qexec.execSelect();
//            ResultSetFormatter.out(System.out, results, query);
			while(results.hasNext()) {
				Resource resource = null;
				try {
					QuerySolution hit = results.next();
					if (!handler.handleSolution(hit)) {
						System.err.println("RECEIVED HANDLER ABORT");
						break;
					}
					wdCount++;
				} catch (Exception e) {
					System.err.println("Error on record: "+(resource==null ? "?" : resource.getURI()));
					e.printStackTrace();
					System.err.println("PROCEEDING TO NEXT URI");
				}
			}
			System.err.printf("QUERY FINISHED - %d resources\n", wdCount);            
		} catch (Exception ex) {
			System.err.println("Error on query: "+fullQuery);
			ex.printStackTrace();
		} finally {
			qexec.close();
		}
		return wdCount;
	}

}
