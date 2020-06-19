package inescid.dataaggregation.casestudies.wikidata;

import java.io.File;
import java.io.IOException;

import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.crawl.http.HttpResponse;
import inescid.dataaggregation.dataset.Global;
import inescid.util.AccessException;
import inescid.util.SparqlClient.Handler;
import inescid.dataaggregation.wikidata.SparqlClientWikidata;

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
		
		Global.init_componentHttpRequestService();
		Global.init_componentDataRepository(httpCacheFolder);
		
		CachedHttpRequestService rdfCache=new CachedHttpRequestService();
		
		SparqlClientWikidata.query("SELECT ?item  WHERE {" + 
//                "  ?item wdt:P6108 ?x . " + 
                "  ?item wdt:P727 ?r .}", new Handler() {
					public boolean handleUri(String uri) throws AccessException, InterruptedException, IOException {
						if(rdfCache.contains(uri)) {
							System.out.printf("%s already exists. Skipping.\n", uri);
						}else {
							HttpResponse fetched = rdfCache.fetchRdf(uri); 
							
							if(fetched.isSuccess())
								System.out.printf("Access to %s failed\n", uri);
							else
								System.out.printf("Saved %s (%d Kb)\n", uri, fetched.getBody().length / 1024);
						}
						return true;
					}
				}) ;
	}

}
