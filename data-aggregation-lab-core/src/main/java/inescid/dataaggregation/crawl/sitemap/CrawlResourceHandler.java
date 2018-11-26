package inescid.dataaggregation.crawl.sitemap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crawlercommons.sitemaps.SiteMapURL;

public abstract class CrawlResourceHandler {

	private static Logger log = LoggerFactory.getLogger(CrawlResourceHandler.class);
		
//		protected CrawlingSession session;
		
		public void close() {
		}
		
//		public interface CrawlHandler {
		public abstract void handleUrl(SiteMapURL subSm);
//
//		public void setSession(CrawlingSession session) {
//			this.session = session;
//		}

	}