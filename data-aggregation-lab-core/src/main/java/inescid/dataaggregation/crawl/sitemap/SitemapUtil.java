package inescid.dataaggregation.crawl.sitemap;

import java.io.IOException;
import java.net.URL;

import crawlercommons.sitemaps.AbstractSiteMap;
import crawlercommons.sitemaps.SiteMapParser;
import crawlercommons.sitemaps.UnknownFormatException;

public class SitemapUtil {


	public static AbstractSiteMap parseSiteMap(String mimeType, byte[] bytes, String url) throws IOException, UnknownFormatException {
		SiteMapParser parser=new SiteMapParser(false);
		AbstractSiteMap siteMap = parser.parseSiteMap(mimeType, bytes, new URL(url));
		return siteMap;
	}
	
	public static AbstractSiteMap parseSiteMap(String content, String url) throws IOException, UnknownFormatException {
		SiteMapParser parser=new SiteMapParser(false);
		AbstractSiteMap siteMap = parser.parseSiteMap("application/xml", content.getBytes("UTF-8"), new URL(url));
		return siteMap;
	}
}
