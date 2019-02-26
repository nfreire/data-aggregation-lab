package inescid.dataaggregation.casestudies.wikidata;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map.Entry;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Resource;

import inescid.dataaggregation.casestudies.wikidata.WikidataSparqlClient.UriHandler;
import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.crawl.http.HttpRequest;
import inescid.dataaggregation.dataset.GlobalCore;
import inescid.util.AccessException;
import inescid.util.RdfUtil;

public class HarvesterWikidataUris {

	public static void main(String[] args) {
		String httpCacheFolder="c://users/nfrei/desktop/HttpRepository";
		File outputFolder=new File("c://users/nfrei/desktop");
		int SAMPLE_RECORDS=5;

		if(args.length>0)
			outputFolder=new File(args[0]);
		if(args.length>1)
			httpCacheFolder=args[1];
		if(args.length>2)
			SAMPLE_RECORDS=Integer.parseInt(args[2]);

		if(!outputFolder.exists())
			outputFolder.mkdirs();
		
		System.out.printf("Settings:\n-OutpputFolder:%s\n-Cache:%s\n-Records:%d\n------------------------\n",
				outputFolder.getPath(), httpCacheFolder, SAMPLE_RECORDS);
		
		GlobalCore.init_componentHttpRequestService();
		GlobalCore.init_componentDataRepository(httpCacheFolder);
		
		CachedHttpRequestService rdfCache=new CachedHttpRequestService();
		
		WikidataSparqlClient.query("SELECT ?item  WHERE {" + 
//                "  ?item wdt:P6108 ?x ." + 
                "  ?item wdt:P727 ?r .", new UriHandler() {
					@Override
					public boolean handleUri(String uri) throws AccessException, InterruptedException, IOException {
						if(rdfCache.contains(uri)) {
							System.out.printf("%s already exists. Skipping.\n", uri);
						}else {
							SimpleEntry<byte[], List<Entry<String, String>>> fetched = rdfCache.fetchRdf(uri); 
							
							if(fetched==null || fetched.getKey()==null || fetched.getKey().length==0)
								System.out.printf("Access to %s failed\n", uri);
							else
								System.out.printf("Saved %s (%d Kb)\n", uri, fetched.getKey().length / 1024);
						}
						return true;
					}
				}) ;
	}

}
