package inescid.dataaggregation.casestudies.wikidata;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.jena.query.QuerySolution;
import inescid.dataaggregation.wikidata.SparqlClientWikidata;

import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.crawl.http.HttpRequest;
import inescid.dataaggregation.dataset.Global;
import inescid.util.AccessException;
import inescid.util.HttpUtil;
import inescid.util.SparqlClient.Handler;
import inescid.dataaggregation.wikidata.RdfRegWikidata;
import inescid.dataaggregation.wikidata.WikidataUtil;

public class ScriptEuropeanaLinkChecker {
	public static void main(String[] args) throws Exception {
		String httpCacheFolder = "c://users/nfrei/desktop/HttpRepository";
		final File outputFolder;
		final int SAMPLE_RECORDS;

		if (args.length > 0)
			outputFolder = new File(args[0]);
		else
			outputFolder = new File("c://users/nfrei/desktop");
		if (args.length > 1)
			httpCacheFolder = args[1];
		if (args.length > 2)
			SAMPLE_RECORDS = Integer.parseInt(args[2]);
		else
//			SAMPLE_RECORDS = 3;
			SAMPLE_RECORDS = -1;

		final File reportFile = new File(outputFolder, "wikidata_broken_links_to_europeana_"+new SimpleDateFormat("dd-MM-yyyy").format(new Date())+".csv");
		if (!outputFolder.exists())
			outputFolder.mkdirs();
		
		HashSet<String> europeanaIdsAlreadyChecked=new HashSet<String>();
		if(reportFile.exists()) {
			List<String> lines = FileUtils.readLines(reportFile, "UTF-8");
			lines.remove(0);
			for(String l : lines) {
				String[] split = l.split(",");
				if(split[2].equals("404"))
					europeanaIdsAlreadyChecked.add(split[1]);
			}
		}
		System.out.println("already known missing: "+europeanaIdsAlreadyChecked.size());
		FileUtils.write(reportFile,"Wikidata entity,EuropeanaID,HTTP status code\n", "UTF-8", false);

		System.out.printf("Settings:\n-OutpputFolder:%s\n-Cache:%s\n-Records:%d\n------------------------\n",
				outputFolder.getPath(), httpCacheFolder, SAMPLE_RECORDS);

		Global.init_componentHttpRequestService();
		Global.init_componentDataRepository(httpCacheFolder);

		CachedHttpRequestService rdfCache = new CachedHttpRequestService();
		rdfCache.setRequestRetryAttempts(1);

		SparqlClientWikidata.query("SELECT ?item ?europeana  WHERE {" +
//                "  ?item wdt:"+RdfRegWikidata.IIIF_MANIFEST+" ?x ." + 
				"  ?item wdt:" + RdfRegWikidata.EUROPEANAID.getLocalName() + " ?europeana . }", new Handler() {
					int stop = SAMPLE_RECORDS+1;
					public boolean handleSolution(QuerySolution solution) throws AccessException, InterruptedException, IOException {
						String wdId=solution.getResource("item").getLocalName();
						String europeanaId=solution.getLiteral("europeana").getString();
						if(europeanaIdsAlreadyChecked.contains(europeanaId)) {
							FileUtils.write(reportFile,wdId+","+europeanaId+",404\n", "UTF-8", true);
						} else {
	//						String europeanaUrl="http://data.europeana.eu/item/"+europeanaId;
							String europeanaUrl="https://www.europeana.eu/portal/en/record/"+europeanaId+".html";
	
							Thread.sleep(100);
							HttpRequest res = HttpUtil.makeHeadRequest(europeanaUrl);
						      boolean ok=(res.getResponseStatusCode() == HttpURLConnection.HTTP_OK);
						      if (!ok) {
						    	  System.out.println("broken: "+europeanaUrl+ " - "+res.getResponseStatusCode());
								FileUtils.write(reportFile,wdId+","+europeanaId+","+res.getResponseStatusCode()+"\n", "UTF-8", true);
						      }
						}						
						stop--;
						return stop != 0;
					}
				});
	}
}
