package inescid.europeana;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;

import crawlercommons.sitemaps.SiteMapURL;
import inescid.dataaggregation.crawl.http.HttpRequest;
import inescid.dataaggregation.crawl.sitemap.CrawlResourceHandler;
import inescid.dataaggregation.crawl.sitemap.SitemapResourceCrawler;
import inescid.dataaggregation.data.DataModelRdfOwl;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.observer.JobObserverStdout;
import inescid.util.HttpUtil;
import inescid.util.RdfUtil;

public class TestSchemaOrgApi {
	private static Pattern recIdPattern=Pattern.compile("/item/(.*)$");

	
	public static void main(String[] args) throws Exception {
		Global.init_componentHttpRequestService();
		Model schemaorgOwl = RdfUtil.readRdfFromUri("https://schema.org/docs/schemaorg.owl");
		AtomicInteger countOk=new AtomicInteger(0);
		AtomicInteger countErrorNetwork=new AtomicInteger(0);
		AtomicInteger countErrorSchema=new AtomicInteger(0);
		
		
		File progressFile=new File("sitemap-progress.token"); 
		File reportFile=new File("sitemap-errors.txt"); 
		String[] token=new String[] {null};
		if(progressFile.exists()) {
			token[0]=FileUtils.readFileToString(progressFile, StandardCharsets.UTF_8);
			System.out.println("RESUMING AFTER: "+token[0]);
		} else {
			System.out.println("STARTING");
			if (reportFile.exists())
				reportFile.delete();
		}
		BufferedWriter errorsLog=Files.newBufferedWriter(reportFile.toPath(), StandardCharsets.UTF_8
				, StandardOpenOption.CREATE, StandardOpenOption.APPEND); 
		
		final DataModelRdfOwl validator=new DataModelRdfOwl(schemaorgOwl);
		
			SitemapResourceCrawler crawler=new SitemapResourceCrawler(
					"https://www.europeana.eu/sitemap-record-index.xml", null, new CrawlResourceHandler() {
				@Override
				public void handleUrl(SiteMapURL subSm) {
					String recId=subSm.getUrl().toString();
					try {
						if(countErrorNetwork.intValue()>=100) {
							errorsLog.close();
							System.out.println("Exiting. Too many errors");
							System.out.printf("OK-%d  -  e.net-%d  -  e.sch-%d\n", countOk.intValue(), countErrorNetwork.intValue(), countErrorSchema.intValue());
							System.exit(0);
						}
						
						Matcher m = recIdPattern.matcher(recId);
						if (m.find()) {
							recId = m.group(1);

							if(token[0]!=null && recId.equals(token[0])) {
								token[0]=null;
								return;
							} else if(token[0]!=null)
								return;
							
//							String schemaUrl=String.format("https://search-api-test.eanadev.org/api/v2/record/%s.schema.jsonld?wskey=H5fwCqB5p", recId);
//							String schemaUrl=String.format("https://search-api-test.eanadev.org/api/v2/record/%s.schema.jsonld?wskey=zMSWiBqsJ", recId);
							String schemaUrl=String.format("http://search-api-acceptance.eanadev.org/api/v2/record/%s.schema.jsonld?wskey=api2demo", recId);
//							String schemaUrl=String.format("http://search-api-acceptance.eanadev.org/api/v2/record/%s.schema.jsonld?wskey=H5fwCqB5p", recId);
//							String schemaUrl=String.format("http://search-api-acceptance.eanadev.org/api/v2/record/%s.schema.jsonld?wskey=zMSWiBqsJ", recId);
							
//							System.out.println(schemaUrl);
//							Model schemaorg = RdfUtil.readRdfFromUri(schemaUrl);
							HttpRequest rdfReq = HttpUtil.makeRequest(schemaUrl);
							if(rdfReq.getResponseStatusCode()!=200) {
								errorsLog.append("--- "+recId+"\n");
								errorsLog.append("HTTP status: "+rdfReq.getResponseStatusCode()+"\n");
								errorsLog.append("HTTP status: "+rdfReq.getResponseContentAsString()+"\n");	
								countErrorNetwork.incrementAndGet();
								return;
							}
							
							Model schemaorg = RdfUtil.readRdf(rdfReq.getResponseContent(), Lang.JSONLD);
							
							List<String> errors = validator.validate(schemaorg);
							if(!errors.isEmpty()) {
//								System.out.println(schemaUrl);
//								System.out.println(recId);
								countErrorSchema.incrementAndGet();
//								System.out.println(schemaUrl+" --- "+errors);
								errorsLog.append("--- "+recId+"\n");
								errorsLog.append(errors.toString()+"\n");
								errorsLog.flush();
								return;
							}
							
//							System.out.println(errors);
							
							countOk.incrementAndGet();
							if(countOk.intValue()==10 || countOk.intValue()%200==0) {
								System.out.printf("OK-%d  -  e.net-%d  -  e.sch-%d\n", countOk.intValue(), countErrorNetwork.intValue(), countErrorSchema.intValue());
								FileUtils.write(progressFile, recId, StandardCharsets.UTF_8);
							}
						} else 
							System.out.println("WARN: could not match record id: "+subSm.getUrl().toString());
					} catch (Exception e) {
						try {
							e.printStackTrace();
							errorsLog.append("--- "+recId+"\n");
							errorsLog.append(e.getMessage());
							errorsLog.flush();
						} catch (IOException e1) {
							throw new RuntimeException(e.getMessage(), e);
						}
						countErrorNetwork.incrementAndGet();
					}
				}
			}, new JobObserverStdout());
			crawler.run();
			System.out.println("All done. exiting.");
			errorsLog.close();
			if(crawler.getRunError()!=null)
				crawler.getRunError().printStackTrace();
	}
	
	
}
