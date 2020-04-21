package inescid.dataaggregation.casestudies.ontologies;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.riot.Lang;

import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.crawl.ld.LdCrawlerGeneric;
import inescid.dataaggregation.data.RdfReg;
import inescid.dataaggregation.data.RegEdm;
import inescid.dataaggregation.data.RegRdf;
import inescid.dataaggregation.data.RegRdfs;
import inescid.dataaggregation.dataset.Global;
import inescid.util.RdfUtil;
import inescid.util.RdfUtil.Jena;

public class ScriptCrawlEdmOwl {

	public static void main(String[] args) throws Exception {
//		File outputFolder = Files.defaultOutputFolder;		
		String httpCacheFolder = "c://users/nfrei/desktop/data/HttpRepository";

//		if (!outputFolder.exists())
//			outputFolder.mkdirs();

//		System.out.printf(
//				"Settings:\n-OutpputFolder:%s\n-Cache:%s\n-Records:%d\n-Broken EuropenaIDs:%d\n------------------------\n",
//				outputFolder.getPath(), httpCacheFolder, SAMPLE_RECORDS, europeanaIdsBroken.size());
//		Files.init(outputFolder);
		Global.init_componentHttpRequestService();
		Global.init_componentDataRepository(httpCacheFolder);

		CachedHttpRequestService rdfCache = new CachedHttpRequestService();
		rdfCache.setRequestRetryAttempts(1);
		
		LdCrawlerGeneric c=new LdCrawlerGeneric(rdfCache);
		c.setFrontier(new LdCrawlerGeneric.Frontier() {
			public boolean isToCrawl(String sourceUri, String uri, int depth) {
				return sourceUri.startsWith(RegEdm.NS) && !uri.startsWith(RegRdf.NS) && !uri.startsWith(RegRdfs.NS) && !uri.startsWith(RdfReg.NsOwl);
			}
			
			public boolean isToCrawl(String sourceUri, Property predicate, int depth) {
				return sourceUri.startsWith(RegEdm.NS) && !predicate.getURI().startsWith(RegRdf.NS) && !predicate.getURI().startsWith(RegRdfs.NS) && !predicate.getURI().startsWith(RdfReg.NsOwl);
			}
			
			public boolean isCrawlPredicates() {
				return true;
			}
		});
		final Model aggregatedModel=Jena.createModel(); 
		final int[] crawledCount=new int[] {0};
		c.setResourceHandler(new LdCrawlerGeneric.ResourceHandler() {
			public void handle(String uri, Model model) {
				System.out.println("Crawled "+uri);
				aggregatedModel.add(model);
				crawledCount[0]++;
			}
		});
		
		c.startCrawl(RegEdm.NS);
		System.out.println("Finished. Crawled URIs: "+crawledCount[0]);
		byte[] rdfOut = RdfUtil.writeRdf(aggregatedModel, Lang.RDFXML);
		FileUtils.writeByteArrayToFile(new File("src/data/edm_owl_crawled_model.owl"), rdfOut);
		System.out.println("Wrote crawled model to "+"src/data/edm_owl_crawled_model.owl");
	}
	
	
}
