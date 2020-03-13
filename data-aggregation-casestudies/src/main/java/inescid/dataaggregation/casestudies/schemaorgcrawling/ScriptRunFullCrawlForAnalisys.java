package inescid.dataaggregation.casestudies.schemaorgcrawling;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import inescid.dataaggregation.casestudies.wikidata.ScriptMetadataAnalyzerOfCulturalHeritage.Files;
import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.metadata.DatasetDescription;
import inescid.dataaggregation.store.Repository;
import inescid.util.googlesheets.GoogleSheetsCsvUploader;

public class ScriptRunFullCrawlForAnalisys {
	

	private static class GoogleSheetsUploads {
		static String credentialspath="C:\\Users\\nfrei\\.credentials\\Data Aggregation Lab-b1ec5c3705fc.json";
		static String spreadsheetId="1meqmbmHQ31MyjCzM-HdBTrZ1W7zX9iqzQ-3mnMXwRVk";
		
		static void updateCsvAtGoogle(File csvFile, String sheet) throws IOException {
			GoogleSheetsCsvUploader.update(spreadsheetId, sheet, csvFile);
		}

		public static void init() {
			Global.init_componentGoogleApi(credentialspath);
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		//experience workflow settings
		boolean reuseLastCrawling=false;
		boolean alwaysCountDepth=false;
		int maxUrisPerSource=10;
		int maxDepth=2;
//		int maxUrisPerSource=10;
		File urisFolder = new File("src/data/schemaorgcrawling");
		String httpCacheFolder = "c://users/nfrei/desktop/data/HttpRepository";
		
		if(args!=null) {
			if(args.length>=1) {
				urisFolder = new File(args[0]);
				if(args.length>=2) 
					httpCacheFolder = args[1];
				if(args.length>=3) 
					maxUrisPerSource = Integer.parseInt(args[2]);
				if(args.length>=4) 
					reuseLastCrawling = Boolean.parseBoolean(args[3]);
				if(args.length>=5) 
					 alwaysCountDepth = Boolean.parseBoolean(args[4]);
				if(args.length>=6) 
					maxDepth = Integer.parseInt(args[5]);
				if(args.length>=7) {
					GoogleSheetsUploads.credentialspath = args[6];
					System.out.println("debug: "+GoogleSheetsUploads.credentialspath);
				}
			}
		}
		
		
		Global.init_componentDataRepository(httpCacheFolder);
		Global.init_componentHttpRequestService();
//		CachedHttpRequestService rdfCache = new CachedHttpRequestService();
//		rdfCache.setRequestRetryAttempts(1);
		Global.init_enableComponentHttpRequestCache();
		GoogleSheetsUploads.init();
		Repository dataRepository = Global.getDataRepository();
		if(!reuseLastCrawling) {
			for(File urisFile : urisFolder.listFiles()) {
				if(!urisFile.getName().startsWith("uris-") || !urisFile.getName().endsWith(".txt")) {
					System.out.println("Skipping file in uris folder: "+urisFile.getName());
					continue;
				}
				String crawledTestUrisRepositoryDataset="crawled-"+urisFile.getName().substring(5, urisFile.getName().length()-4);
				System.out.println("cleaning: "+crawledTestUrisRepositoryDataset);
				dataRepository.clear(crawledTestUrisRepositoryDataset);
			}
		}
			
		SchemaOrgLodCrawler crawler=new SchemaOrgLodCrawler();
		crawler.setMaxDepth(maxDepth);
		crawler.setAlwaysCountDepth(alwaysCountDepth);
		for(File urisFile : urisFolder.listFiles()) {
			if(!urisFile.getName().startsWith("uris-") || !urisFile.getName().endsWith(".txt")) {
				System.out.println("Skipping file in uris folder: "+urisFile.getName());
				continue;
			}
			String crawledTestUrisRepositoryDataset="crawled-"+urisFile.getName().substring(5, urisFile.getName().length()-4);
			System.out.println("starting: "+crawledTestUrisRepositoryDataset);
			if(!reuseLastCrawling)
				dataRepository.clear(crawledTestUrisRepositoryDataset);
			List<String> testUris = FileUtils.readLines(urisFile, Global.UTF8);
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
				int countValidRdf = stats.getCountValidRdf();
				if(maxUrisPerSource>0 && countValidRdf == maxUrisPerSource) {
					System.out.println("max uris per source reached.");
					break;
				}
				if(countValidRdf!=0 && countValidRdf % 100 == 0) {
					System.out.println("Mid stats at "+countValidRdf +" - "+new String(stats.serialize()));
//					FileUtils.writeByteArrayToFile(new File("target/CrawlAnalisys-mid.csv"), stats.serialize());
				}
			};
			System.out.println("Final stats - "+new String(stats.serialize()));
			File csvFile = new File("target/"+crawledTestUrisRepositoryDataset+"-stats.csv");
			FileUtils.writeByteArrayToFile(csvFile, stats.serialize());
			try {
				GoogleSheetsUploads.updateCsvAtGoogle(csvFile, csvFile.getName().substring(0, csvFile.getName().length()-4));
			} catch (Exception e) {
				System.out.println("WARNING: could not white to google sheets");
				e.printStackTrace(System.out);
			}
		}
	}
}
