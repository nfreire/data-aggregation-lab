package inescid.dataaggregation.crawl.www;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map.Entry;

import org.apache.any23.Any23;
import org.apache.any23.extractor.ExtractionException;
import org.apache.any23.extractor.ExtractorGroup;
import org.apache.any23.extractor.html.EmbeddedJSONLDExtractorFactory;
import org.apache.any23.extractor.html.HTMLMetaExtractorFactory;
import org.apache.any23.writer.NTriplesWriter;
import org.apache.any23.writer.TripleHandlerException;
import org.apache.any23.writer.TurtleWriter;
import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.jena.iri.IRI;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import com.mchange.v1.util.SimpleMapEntry;

import crawlercommons.sitemaps.SiteMapURL;
import inescid.dataaggregation.crawl.http.HttpRequest;
import inescid.dataaggregation.crawl.sitemap.CrawlResourceHandler;
import inescid.dataaggregation.crawl.sitemap.SitemapResourceCrawler;
import inescid.dataaggregation.dataset.LodDataset;
import inescid.dataaggregation.dataset.WwwDataset;
import inescid.dataaggregation.dataset.convert.RdfReg;
import inescid.dataaggregation.store.Repository;
import inescid.util.DatasetLog;
import inescid.util.HttpUtil;
import inescid.util.LinkedDataUtil;
import inescid.util.ListOnTxtFile;
import inescid.util.HttpRequestException;

public class WwwDatasetHarvest {
	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WwwDatasetHarvest.class);
	private static long retriesSleepMicrosecs=20;
	private static int retriesMaxAttempts=1;
	
	WwwDataset dataset;
	boolean skipExistingResources=false;
	DatasetLog datasetLog;
	Repository repository;
	Integer sampleSize;
	
	Any23 any23=null;
	
	public WwwDatasetHarvest(WwwDataset dataset, Repository repository) {
		super();
		this.dataset = dataset;
		datasetLog=new DatasetLog(dataset.getUri());
		this.repository = repository;
		
		switch (dataset.getMicroformat()) {
		case META_ALL:
		case HTML5_META:
			any23=new Any23(new ExtractorGroup(new ArrayList() {{ 
				add(new HTMLMetaExtractorFactory());
			}}));
			break;
		case SCHEMAORG:
			 any23=new Any23(new ExtractorGroup(new ArrayList() {{ 
				add(new EmbeddedJSONLDExtractorFactory());
			}}));
			break;
		case DC:
			any23=new Any23(new ExtractorGroup(new ArrayList() {{ 
				add(new HTMLMetaExtractorFactory());
				add(new EmbeddedJSONLDExtractorFactory());
			}}));
			break;
		default:
			throw new RuntimeException("TODO: "+dataset.getMicroformat());
		}
	}
	
	public WwwDatasetHarvest(WwwDataset dataset, Repository repository, boolean skipExistingResources) {
		this(dataset, repository);
		this.skipExistingResources = skipExistingResources;
	}

	public Calendar startProcess() {
		Calendar start=new GregorianCalendar();
				SitemapResourceCrawler smCrawler=new SitemapResourceCrawler(dataset.getUri(), null, new CrawlResourceHandler() {
					int harvestedCnt=0;
					@Override
					public void handleUrl(SiteMapURL subSm) throws Exception {
						if(sampleSize!=null && sampleSize>0 && harvestedCnt>=sampleSize)
							return;
						if(harvestResource(dataset.getUri(), subSm.getUrl().toString()))
								harvestedCnt++;
					}
				});
				smCrawler.run();
		return start;
	}

	private boolean harvestResource(String datasetUri, String uriOfRec) throws IOException {
		File rdfResourceFile = repository.getFile(datasetUri, uriOfRec);
		if(skipExistingResources && rdfResourceFile.exists()) {
			datasetLog.logSkippedRdfResource();
			return false;
		}
		int retries=retriesMaxAttempts;
		while (retries>=0) {
			try {
				HttpRequest reqRes=HttpUtil.makeRequest(uriOfRec);
				
				int statusCode = reqRes.getResponse().getStatusLine().getStatusCode();
				if(statusCode==200) {
//					reqRes.getContent().asString();				
					try {
						String charset = "UTF8";
						if (reqRes.getContent().getType().getCharset()!=null)
							charset = reqRes.getContent().getType().getCharset().name();
						
//						Jsoup.parse(html)
						ByteArrayOutputStream decodedInput = new ByteArrayOutputStream();
						TurtleWriter triples=new TurtleWriter(decodedInput);
//						NTriplesWriter triples=new NTriplesWriter(decodedInput);
						any23.extract(reqRes.getContent().asString(), reqRes.getUrl(), reqRes.getContent().getType().getMimeType(), charset, 
								triples);			
//								new CountingTripleHandler() {
//							@Override
//							public void receiveTriple(Resource arg0, IRI arg1, Value arg2, IRI arg3, ExtractionContext arg4)
//									throws TripleHandlerException {
//							System.out.println("Triple: "+ arg0.toString());
//							System.out.println("1 : "+ arg1.toString());
//							System.out.println("2: "+ arg2.toString());
//							System.out.println("3: "+ arg3);
//							}});
						triples.close();
						decodedInput.close();
						
						switch (dataset.getMicroformat()) {
						case SCHEMAORG:
						case META_ALL:
							repository.save(datasetUri, reqRes.getUrl(), decodedInput.toByteArray(), new ArrayList<Entry<String, String>>() {{
								add(new SimpleMapEntry("Content-Type", " text/turtle"));}});
							break;
						default:
							throw new RuntimeException("Not implemented yet "+dataset.getMicroformat());
						}
						return true;
					} catch (ExtractionException e) {
						log.error(reqRes.getUrl(), e);
					} catch (TripleHandlerException e) {
						log.error(reqRes.getUrl(), e);
					} catch (Exception e) {
						log.error(reqRes.getUrl(), e);
					}
				} else if(statusCode==304) {
					log.info("not modified, skipped: "+reqRes.getUrl());
					return false;
				} else {
					log.info("Invalid response: "+ statusCode+ " - "+reqRes.getUrl());
				}
			} catch (Exception e) {
				if(retries<1) 
					log.error(uriOfRec, e);
			}
			retries--;
			datasetLog.logHarvestIssue(uriOfRec, null);
			try {
//							log.debug("Harvester sleeping", e);
				Thread.sleep((retriesMaxAttempts-retries)*retriesSleepMicrosecs);
			} catch (InterruptedException ei) {
				log.warn(uriOfRec, ei);
				break;
			}
		}
		return false;
	}

	
	public void setSampleSize(Integer sampleSize) {
		this.sampleSize=sampleSize;
	}

	
}
