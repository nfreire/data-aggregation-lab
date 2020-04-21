package inescid.dataaggregation.crawl.sitemap;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import crawlercommons.sitemaps.SiteMapURL;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.observer.JobObserverStdout;

public class ScriptConvertEuropeanaSitemapToSchemaOrgApi {
	private static Pattern recIdPattern=Pattern.compile("/record/(.*)\\.html$");
	
	
	public static void main(String[] args) {
		try {
			String sitemapUrl="https://www.europeana.eu/portal/europeana-sitemap-index-hashed.xml";
//			String baseUrlForSitemaps="http://localhost:8080/data-aggregation-lab/static/data-external/europeana-dataset-sitemap/";
			String baseUrlForSitemaps="http://192.92.149.69:8983//data-aggregation-lab/static/data-external/europeana-dataset-sitemap/";

			File sitemapsFolder=new File("src/data/europeana-dataset-sitemap");
			if(!sitemapsFolder.exists())
				sitemapsFolder.mkdirs();
			SitemapWriter writer=new SitemapWriter(sitemapsFolder, baseUrlForSitemaps);
			
			SitemapResourceCrawler crawler=new SitemapResourceCrawler(sitemapUrl, null, new CrawlResourceHandler() {
				@Override
				public void handleUrl(SiteMapURL subSm) {
					try {
						Matcher m = recIdPattern.matcher(subSm.getUrl().toString());
						if (m.find()) {
//					https://search-api-test.eanadev.org/api/v2/record/00000/047326BDAC04C0A54F14020E1E45637E923EC80E.schema.jsonld?wskey=api2demo	
//https://www.europeana.eu/portal/record/00000/047326BDAC04C0A54F14020E1E45637E923EC80E.html
							String schemaUrl=String.format("https://search-api-test.eanadev.org/api/v2/record/%s.schema.jsonld?wskey=api2demo", m.group(1));
							writer.writeUri(schemaUrl);
						} else 
							System.out.println("WARN: could not match record id: "+subSm.getUrl().toString());
					} catch (IOException e) {
						throw new RuntimeException(e.getMessage(), e);
					}
				}
			}, new JobObserverStdout());
			crawler.run();
			writer.end();
			System.out.println("All done. exiting.");
			if(crawler.getRunError()!=null)
				crawler.getRunError().printStackTrace();
//			Global.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
		
	}

}
