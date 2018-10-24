package inescid.dataaggregation.crawl.sitemap;

import java.io.IOException;
import java.net.URL;

import org.apache.http.client.fluent.Content;

import crawlercommons.sitemaps.AbstractSiteMap;
import crawlercommons.sitemaps.SiteMap;
import crawlercommons.sitemaps.SiteMapIndex;
import crawlercommons.sitemaps.SiteMapParser;
import crawlercommons.sitemaps.SiteMapURL;
import crawlercommons.sitemaps.UnknownFormatException;
import inescid.dataaggregation.crawl.http.HttpRequest;
import inescid.dataaggregation.crawl.http.UrlRequest;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.job.JobObserver;
import inescid.util.DevelopementSingleton;

public class SitemapResourceCrawler {

	
	String sitemapUrl;
	String robotsTxtUrl;
	CrawlResourceHandler handler;
	JobObserver observer;
	
	Exception runError=null;
	
	public SitemapResourceCrawler(String sitemapUrl, String robotsTxtUrl, CrawlResourceHandler handler, JobObserver observer) {
		super();
		this.sitemapUrl = sitemapUrl;
		this.handler = handler;
		this.robotsTxtUrl = robotsTxtUrl;
		this.observer = observer;
	}
	
	public Exception getRunError() {
		return runError;
	}
	
	public void run() {
		try {
//			if(robotsTxtUrl!=null)
//				session.setRobotsTxtRules(robotsTxtUrl);
			fetchSitemap(sitemapUrl);
			handler.close();
		} catch (Exception e) {
			runError=e;
		}
	}
	
	
	
	private void fetchSitemap(String sitemapUrl) throws Exception {
		UrlRequest ldReq=new UrlRequest(sitemapUrl);
		HttpRequest sitemapRequest = new HttpRequest(ldReq);
		Global.getHttpRequestService().fetch(sitemapRequest);
		if (sitemapRequest.getResponseStatusCode() != 200) 
			throw new IOException(sitemapUrl);
		AbstractSiteMap siteMap;
		siteMap = parseSiteMap(sitemapRequest.getContent(), sitemapUrl);
		if (siteMap.isIndex()) {
			SiteMapIndex smIdx=(SiteMapIndex) siteMap;
			for(AbstractSiteMap subSm : smIdx.getSitemaps()) {
				if(DevelopementSingleton.DEVEL_TEST) {	
					if(DevelopementSingleton.RESOURCE_HARVEST_CNT > 5) break;
				}
				fetchSitemap(subSm.getUrl().toString());
			}
		} else {
			SiteMap smIdx=(SiteMap) siteMap;
			for(SiteMapURL subSm : smIdx.getSiteMapUrls()) {
				if(DevelopementSingleton.DEVEL_TEST) {	
					DevelopementSingleton.RESOURCE_HARVEST_CNT++;
					if(DevelopementSingleton.stopHarvest()) break;
				}
				try {
					handler.handleUrl(subSm);
				} catch (Exception e) {
					observer.signalResourceFailure(subSm.getUrl().toString(), e);
				}
			}
		}
	}

	private static AbstractSiteMap parseSiteMap(Content content, String url) throws IOException, UnknownFormatException {
		SiteMapParser parser=new SiteMapParser(false);
		AbstractSiteMap siteMap = parser.parseSiteMap(content.getType().getMimeType(), content.asBytes(), new URL(url));
		return siteMap;
	}
	
}
