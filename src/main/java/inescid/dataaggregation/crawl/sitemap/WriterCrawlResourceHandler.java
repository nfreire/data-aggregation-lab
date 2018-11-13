package inescid.dataaggregation.crawl.sitemap;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.io.output.FileWriterWithEncoding;

import crawlercommons.sitemaps.SiteMapURL;

public class WriterCrawlResourceHandler extends CrawlResourceHandler{
	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WriterCrawlResourceHandler.class);
	
	protected Writer writer;
	
	public WriterCrawlResourceHandler(Writer writer) {
		this.writer=writer;
	}

	public WriterCrawlResourceHandler(File destFile, boolean append) throws IOException {
		this(new FileWriterWithEncoding(destFile, "UTF8", append));
	}
	
	@Override
	public void handleUrl(SiteMapURL url) {
		try {
			writer.write(url.getUrl().toString());
			writer.write('\n');
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	@Override
	public void close() {
		super.close();
		try {
			writer.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}
}
