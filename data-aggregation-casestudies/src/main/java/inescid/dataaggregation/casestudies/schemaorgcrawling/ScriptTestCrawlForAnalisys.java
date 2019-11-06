package inescid.dataaggregation.casestudies.schemaorgcrawling;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;

import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.crawl.ld.DatasetDescription;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.store.Repository;

public class ScriptTestCrawlForAnalisys {
	public static void main(String[] args) throws Exception {
		//experience workflow settings
		boolean reuseLastTestUris=true;
		boolean reuseLastCrawling=false;
		File urisFile = new File("src/data/schemaorgcrawling/uris-NLF.txt");
		String crawledTestUrisRepositoryDataset = "crawled-NLF";
		String collectionDescriptionUri=null;
//		File urisFile = new File("src/data/schemaorgcrawling/uris-KB-alba.txt");
//		String crawledTestUrisRepositoryDataset = "crawled-KB-alba";
//		String collectionDescriptionUri="http://data.bibliotheken.nl/id/dataset/rise-alba";

		String httpCacheFolder = "c://users/nfrei/desktop/data/HttpRepository";

		Global.init_componentHttpRequestService();
		Global.init_componentDataRepository(httpCacheFolder);
		CachedHttpRequestService rdfCache = new CachedHttpRequestService();
		rdfCache.setRequestRetryAttempts(1);
		Repository dataRepository = Global.getDataRepository();
		SchemaOrgLodCrawler crawler=new SchemaOrgLodCrawler();

		if(!reuseLastCrawling)
			dataRepository.clear(crawledTestUrisRepositoryDataset);
			
		List<String> testUris = null;
		if(urisFile.exists() && urisFile.length()>0) {
			testUris = FileUtils.readLines(urisFile, Global.UTF8);
		} else {
			testUris = new DatasetDescription(collectionDescriptionUri).listRootResources();
			FileUtils.writeLines(urisFile, Global.UTF8.toString(), testUris);
		}			
		int totalUrisCrawled=0;
		CrawlResultStats stats=new CrawlResultStats();
		for(String uri : testUris) {
			CrawlResult crawlResult;
			if(reuseLastCrawling && dataRepository.contains(crawledTestUrisRepositoryDataset, uri)) {
				crawlResult = CrawlResult.deSerialize(dataRepository.getContent(crawledTestUrisRepositoryDataset, uri));				
			} else {
				crawlResult = crawler.crawlSchemaorgForCho(uri);
				dataRepository.save(crawledTestUrisRepositoryDataset, uri, crawlResult.serialize());
			}
			stats.addToStats(crawlResult);
			totalUrisCrawled++;
			if(totalUrisCrawled%10 == 0) {
				System.out.println("Mid stats at "+totalUrisCrawled+" - "+new String(stats.serialize()));
				FileUtils.writeByteArrayToFile(new File("target/CrawlAnalisys-mid.csv"), stats.serialize());
			}
		};
		System.out.println("Final stats - "+new String(stats.serialize()));
		FileUtils.writeByteArrayToFile(new File("target/CrawlAnalisys.csv"), stats.serialize());
	}
}
