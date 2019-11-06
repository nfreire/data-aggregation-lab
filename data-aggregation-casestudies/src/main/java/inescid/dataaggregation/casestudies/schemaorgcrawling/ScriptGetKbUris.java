package inescid.dataaggregation.casestudies.schemaorgcrawling;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.riot.Lang;
import org.apache.poi.hssf.util.HSSFColor.GOLD;

import com.drew.lang.ByteArrayReader;

import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.crawl.ld.DatasetDescription;
import inescid.dataaggregation.crawl.ld.RulesSchemaorgCrawlGraphOfCho;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.LodDataset;
import inescid.dataaggregation.store.Repository;
import inescid.util.RdfUtil;

public class ScriptGetKbUris {
	/* 
	 init componentes
	get URIs for sample
		call crawl on each URI
	write results
	--
		report of results:
		obtained resources #
		links not followed #
			links not followed by Class #
				
		irrelevant resources that were not mapped later.
		
	-- crawl of a URI
		getCho, 
			check Resorce class
			for each prop of cho 
				if prop is mappable, follow it if mappable to a resource. if just a reference use the URI. if literal, harvest linked resource and keep if schema:name is present
				    harvest the resource (if not just used as ref)
				    	if to be used as literal, keep if schema:name is present (may be further elaborated in future work)
				    	if resource check supported type. 
				    		if supported continue harvesting and apply resource harvest algorithm
				    		if not supported type, try to get a schema:name for use as literal, or discard.
	*/
	
	public static void main(String[] args) throws Exception {
		//experience workflow settings
		boolean reuseLastTestUris=true;
		boolean reuseLastCrawling=false;

		String httpCacheFolder = "c://users/nfrei/desktop/data/HttpRepository";
		File urisFile = null;
		
		String crawledTestUrisRepositoryDataset = "crawled-test-uris";

		Global.init_componentHttpRequestService();
		Global.init_componentDataRepository(httpCacheFolder);
		CachedHttpRequestService rdfCache = new CachedHttpRequestService();
		rdfCache.setRequestRetryAttempts(1);
		Repository dataRepository = Global.getDataRepository();
		SchemaOrgLodCrawler crawler=new SchemaOrgLodCrawler();

		if(!reuseLastCrawling)
			dataRepository.clear(crawledTestUrisRepositoryDataset);
			
		List<String> testUris = null;
		String dsKbUri="http://data.bibliotheken.nl/id/dataset/rise-alba";
		urisFile = new File("target/uris-KB-alba.txt");
		testUris = new DatasetDescription(dsKbUri).listRootResources();
		FileUtils.writeLines(urisFile, Global.UTF8.toString(), testUris);

		dsKbUri="http://data.bibliotheken.nl/id/dataset/rise-centsprenten";
		urisFile = new File("target/uris-KB-centsprenten.txt");
		testUris = new DatasetDescription(dsKbUri).listRootResources();
		FileUtils.writeLines(urisFile, Global.UTF8.toString(), testUris);

		dsKbUri="http://data.bibliotheken.nl/id/dataset/rise-childrensbooks";
		urisFile = new File("target/uris-KB-childrensbooks.txt");
		testUris = new DatasetDescription(dsKbUri).listRootResources();
		FileUtils.writeLines(urisFile, Global.UTF8.toString(), testUris);
		
	}
}
