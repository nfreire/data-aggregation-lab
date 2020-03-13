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

public class ScriptTMPGetSizeOfCrawledSeeds {
	
	public static void main(String[] args) throws Exception {
		File urisFolder = new File("src/data/schemaorgcrawling");
		String httpCacheFolder = "c://users/nfrei/desktop/data/HttpRepository";
		Global.init_componentDataRepository(httpCacheFolder);
		Global.init_componentHttpRequestService();
		Repository dataRepository = Global.getDataRepository();
		
		for(File urisFile : urisFolder.listFiles()) {
			if(!urisFile.getName().startsWith("uris-") || !urisFile.getName().endsWith(".txt")) {
				System.out.println("Skipping file in uris folder: "+urisFile.getName());
				continue;
			}
			String crawledTestUrisRepositoryDataset="crawled-"+urisFile.getName().substring(5, urisFile.getName().length()-4);
			System.out.println(crawledTestUrisRepositoryDataset+": "+ dataRepository.getSize(crawledTestUrisRepositoryDataset));
		}
	}
}
