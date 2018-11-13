package inescid.dataaggregation.crawl.sitemap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.io.output.FileWriterWithEncoding;

public class SitemapWriter {
	static final int CHOS_PER_SITEMAP_FILE=10000;
	
	static final DatatypeFactory xmlDateFactory;
	static {
		try {
			xmlDateFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
		File destFolder;
//		String baseFilename;
		String baseUrl;
		
		BufferedWriter urlsetWriter;
		BufferedWriter indexWriter;
		Integer writtenCountDataset=0;
		Integer writtenCountUrlset=0;
		Integer writtenCountTotal=0;
		Integer urlsetCount=0;
		
//		<?xml version="1.0" encoding="UTF-8"?>
//		<sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
//		   <sitemap>
//		      <loc>http://www.example.com/sitemap1.xml.gz</loc>
//		      <lastmod>2004-10-01T18:23:17+00:00</lastmod>
//		   </sitemap>
//		   <sitemap>
//		      <loc>http://www.example.com/sitemap2.xml.gz</loc>
//		      <lastmod>2005-01-01</lastmod>
//		   </sitemap>
//		</sitemapindex>
		
		
		public SitemapWriter(File destFolder, String baseUrl) throws IOException {
			this.destFolder=destFolder;
			this.baseUrl = baseUrl;
			indexWriter=new BufferedWriter(new FileWriterWithEncoding(new File(destFolder, "sitemap.xml"), "UTF-8", false));
			indexWriter.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			indexWriter.append("<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
			writeUrlsetHead();
		}

		private void writeUrlsetHead() throws IOException {
			if(urlsetWriter!=null) {
				urlsetWriter.append("</urlset>\n");
				urlsetWriter.close();				
			}
			urlsetCount++;
			String urlsetFilename = "sitemap"+urlsetCount+".xml";
			urlsetWriter=new BufferedWriter(new FileWriterWithEncoding(new File(destFolder, urlsetFilename), "UTF-8", false));
			urlsetWriter.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			urlsetWriter.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

			indexWriter.append("<sitemap>\n<loc>"+baseUrl+urlsetFilename+"</loc>\n");
			indexWriter.append("<lastmod>"+ xmlDateFactory.newXMLGregorianCalendar(new GregorianCalendar()).toString()+"</lastmod>\n");
			indexWriter.append("</sitemap>\n");
			indexWriter.flush();
		}
		public void writeUri(String uri) throws IOException {
			if(writtenCountUrlset>CHOS_PER_SITEMAP_FILE){
				writeUrlsetHead();
				writtenCountUrlset=0;
			}
			urlsetWriter.append("<url><loc>"+uri+"</loc></url>\n");
			writtenCountDataset++;
			writtenCountUrlset++;
			writtenCountTotal++;
		}

		public int getWrittenCountDataset() {
			return writtenCountDataset;
		}

		public int getWrittenCountTotal() {
			return writtenCountTotal;
		}

		public void resetDatasetCounter() {
			writtenCountDataset=0;
		}

		public void end() throws IOException {
			urlsetWriter.append("</urlset>\n");
			urlsetWriter.close();				

			indexWriter.append("</sitemapindex>\n");
			indexWriter.close();				
		}
	}