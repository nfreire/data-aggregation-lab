package inescid.util.europeana;

import inescid.util.SparqlClient;
import inescid.util.SparqlClient.Handler;

public class EuropeanaSparqlClient {
	private static final SparqlClient client;
	
	public static final String BASE_URL="http://sparql.europeana.eu/"; 
	
	static {
		client=new SparqlClient(BASE_URL, EdmReg.nsPrefixes);
	}
	
	public static int query(String queryString, Handler handler) {
		return client.query(queryString, handler);
	}
}
