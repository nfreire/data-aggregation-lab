package inescid.util.europeana;

import inescid.util.SparqlClient;
import inescid.util.SparqlClient.Handler;

public class SparqlClientOfEuropeana {
	public static final SparqlClient INSTANCE;
	
	public static final String BASE_URL="http://sparql.europeana.eu/"; 
	
	static {
		INSTANCE=new SparqlClient(BASE_URL, EdmReg.nsPrefixes);
	}
	
	public static int query(String queryString, Handler handler) {
		return INSTANCE.query(queryString, handler);
	}
}
