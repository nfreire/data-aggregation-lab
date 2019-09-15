package inescid.dataaggregation.crawl.sitemap;

import crawlercommons.sitemaps.SiteMapURL;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.observer.JobObserverStdout;

public class TestCrawlSitemap {
	
	public static void main(String[] args) {
		try {
			Global.init_developement();
			
//			String sitemapUrl="http://dams.llgc.org.uk/iiif/newspapers/sitemap.xml";
//			String sitemapUrl="https://data.ucd.ie/sitemap_test1.xml";
			String sitemapUrl="https://www.annefrank.org/sitemap.xml";
			
			
			SitemapResourceCrawler crawler=new SitemapResourceCrawler(sitemapUrl, null, new CrawlResourceHandler() {
				@Override
				public void handleUrl(SiteMapURL subSm) {
					System.out.print(subSm.getUrl());	
				}
			}, new JobObserverStdout());
			crawler.run();
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
