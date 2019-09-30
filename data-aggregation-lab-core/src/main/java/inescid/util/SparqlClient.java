package inescid.util;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import inescid.util.RdfUtil.Jena;

public class SparqlClient {
	public static abstract class Handler {
		//return true to continue to next URI, false to abort
		public boolean handleSolution(QuerySolution solution) throws Exception { return true; };
	}
	
	protected final String baseUrl; 
	protected final String queryPrefix;
	protected boolean debug=false;
	
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
		if(debug)
			System.out.println(fullQuery);
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
	
	public int queryWithPaging(String queryString, int resultsPerPage, String orderVariableName, Handler handler) {
		int offsett=0;
		while (true) {
			String fullQuery = String.format("%s%s\n%s" + 
					"LIMIT %d\n" + 
					"OFFSET %d ", queryPrefix, queryString, (orderVariableName ==null ? "" : "ORDER BY ("+orderVariableName+")\n"), resultsPerPage, offsett);
			if(debug)
				System.out.println(fullQuery);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(this.baseUrl, fullQuery);
			try {
				ResultSet results = qexec.execSelect();
	//            ResultSetFormatter.out(System.out, results, query);
				if(!results.hasNext())
					break;
				while(results.hasNext()) {
					Resource resource = null;
					QuerySolution hit = results.next();
					try {
						if (!handler.handleSolution(hit)) {
							System.err.println("RECEIVED HANDLER ABORT");
							break;
						}
					} catch (Exception e) {
						System.err.println("Error on record handler: "+(resource==null ? "?" : resource.getURI()));
						e.printStackTrace();
						System.err.println("PROCEEDING TO NEXT URI");
					}
					offsett++;
				}
				if(debug)
					System.out.printf("QUERY FINISHED - %d resources\n", offsett);            
			} catch (Exception ex) {
				System.err.println("Error on query: "+fullQuery);
				ex.printStackTrace();
			} finally {
				qexec.close();
			}
		}
		return offsett;
	}
	
	public void createAllStatementsAboutAndReferingResource(String resourceUri, Model createInModel) {
		createAllStatementsAboutResource(resourceUri, createInModel);
		createAllStatementsReferingResource(resourceUri, createInModel);
	}	
	public void createAllStatementsAboutResource(String resourceUri, Model createInModel) {
		final Resource subjRes = createInModel.createResource(resourceUri);
		query("SELECT ?p ?o WHERE {<" + resourceUri + "> ?p ?o}", new Handler() {
			@Override
			public boolean handleSolution(QuerySolution solution) throws Exception {
				Resource pRes = solution.getResource("p");
				Resource oRes = null;
				Literal oLit = null;
				try {
					oRes = solution.getResource("o");
				} catch (Exception e) {
					oLit = solution.getLiteral("o");
				}
				createInModel.add(createInModel.createStatement(subjRes, createInModel.createProperty(pRes.getURI()), oRes==null ? oLit : oRes));
				return true;
			}
		});
	}

	public void createAllStatementsReferingResource(String resourceUri, Model createInModel) {
		final Resource subjRes = createInModel.createResource(resourceUri);
		query("SELECT ?s ?p WHERE {?s ?p <" + resourceUri + ">}", new Handler() {
			@Override
			public boolean handleSolution(QuerySolution solution) throws Exception {
				Resource pRes = solution.getResource("p");
				Resource sRes = solution.getResource("s");
				createInModel.add(createInModel.createStatement(sRes, createInModel.createProperty(pRes.getURI()), subjRes));
				return true;
			}
		});
	}
	
	
	public Model getAllStatementsAboutAndReferingResource(String resourceUri) {
		final Model model=Jena.createModel();
		createAllStatementsAboutAndReferingResource(resourceUri, model);
		return model;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	
}
