package inescid.dataaggregation.dataset.profile.completeness;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import crawlercommons.sitemaps.SiteMapURL;
import inescid.dataaggregation.crawl.http.HttpRequest;
import inescid.dataaggregation.crawl.http.UrlRequest;
import inescid.dataaggregation.crawl.sitemap.CrawlResourceHandler;
import inescid.dataaggregation.crawl.sitemap.SitemapResourceCrawler;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.observer.JobObserverStdout;

public class ScriptConvertEuropeanaSitemapToUrisList {
	private static Pattern recIdPattern = Pattern.compile("/record/(.*)\\.html$");

	public static void main(String[] args) {
		try {
			Global.init_developement();
//			
			String sitemapUrl="https://sitemap-test.eanadev.org/sitemap/index.xml";
//			String sitemapUrl="https://www.europeana.eu/portal/europeana-sitemap-index-hashed.xml";
////			String baseUrlForSitemaps="http://localhost:8080/data-aggregation-lab/static/data-external/europeana-dataset-sitemap/";
//			String baseUrlForSitemaps="http://192.92.149.69:8983//data-aggregation-lab/static/data-external/europeana-dataset-sitemap/";

			String prefetchedSitemap=null;
			{
				HttpRequest sitemapRequest = new HttpRequest(new UrlRequest(sitemapUrl));
				Global.getHttpRequestService().fetch(sitemapRequest);
				if (sitemapRequest.getResponseStatusCode() != 200) 
					throw new IOException(sitemapUrl);
				prefetchedSitemap=sitemapRequest.getResponseContentAsString();
				
				prefetchedSitemap=prefetchedSitemap.replace("https://www.europeana.eu/portal",
						"https://sitemap-test.eanadev.org/sitemap");
			}
			
			File urisOutputFolder=new File("src/data/europeana-dataset-uris");
			if(!urisOutputFolder.exists())
				urisOutputFolder.mkdirs();
			
			HashMap<String, Writer> uriWritersByProvider=new HashMap<>();
			
			SitemapResourceCrawler crawler=new SitemapResourceCrawler(sitemapUrl, null, new CrawlResourceHandler() {
				@Override
				public void handleUrl(SiteMapURL subSm) {
					try {
						Matcher m = recIdPattern.matcher(subSm.getUrl().toString());
						if (m.find()) {
//					https://search-api-test.eanadev.org/api/v2/record/00000/047326BDAC04C0A54F14020E1E45637E923EC80E.schema.jsonld?wskey=api2demo	
//https://www.europeana.eu/portal/record/00000/047326BDAC04C0A54F14020E1E45637E923EC80E.html
							String uri=String.format("http://data.europeana.eu/item/%s", m.group(1));
							Writer w=getWriterFor(m.group(1).substring(0, m.group(1).indexOf('/')));
							w.write(uri);
							w.write('\n');
						} else 
							System.out.println("WARN: could not match record id: "+subSm.getUrl().toString());
					} catch (IOException e) {
						throw new RuntimeException(e.getMessage(), e);
					}
				}

				private Writer getWriterFor(String providerId) throws IOException {
					Writer writer = uriWritersByProvider.get(providerId);
					if(writer==null) {
						writer=new BufferedWriter(new FileWriter(new File(urisOutputFolder, providerId+".txt")));
						uriWritersByProvider.put(providerId, writer);
					}
					return writer;
				}
			}, new JobObserverStdout());

			if(prefetchedSitemap!=null)
				crawler.run(prefetchedSitemap);
			else
				crawler.run();
			
			for(Writer w: uriWritersByProvider.values())
				w.close();
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
