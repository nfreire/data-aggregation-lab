package inescid.dataaggregation.crawl.www;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map.Entry;

import org.apache.any23.Any23;
import org.apache.any23.extractor.ExtractionException;
import org.apache.any23.extractor.ExtractorGroup;
import org.apache.any23.extractor.html.EmbeddedJSONLDExtractorFactory;
import org.apache.any23.extractor.html.HTMLMetaExtractorFactory;
import org.apache.any23.writer.TripleHandlerException;
import org.apache.any23.writer.TurtleWriter;

import com.mchange.v1.util.SimpleMapEntry;

import crawlercommons.sitemaps.SiteMapURL;
import inescid.dataaggregation.crawl.http.HttpRequest;
import inescid.dataaggregation.crawl.sitemap.CrawlResourceHandler;
import inescid.dataaggregation.crawl.sitemap.SitemapResourceCrawler;
import inescid.dataaggregation.dataset.WwwDataset;
import inescid.dataaggregation.dataset.job.JobObserver;
import inescid.dataaggregation.store.Repository;
import inescid.util.AccessException;
import inescid.util.DatasetLog;
import inescid.util.HttpUtil;
import inescid.util.MimeType;

public class WwwDatasetHarvest {
	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WwwDatasetHarvest.class);
	private static long retriesSleepMicrosecs=20;
	private static int retriesMaxAttempts=1;
	
	WwwDataset dataset;
	boolean skipExistingResources=false;
	DatasetLog datasetLog;
	Repository repository;
	Integer sampleSize;
	JobObserver observer; 
	Exception runError=null;
	
	Any23 any23=null;
	
	public WwwDatasetHarvest(WwwDataset dataset, Repository repository, JobObserver observer) {
		super();
		this.dataset = dataset;
		datasetLog=new DatasetLog(dataset.getUri());
		this.repository = repository;
		this.observer=observer;
		
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
	
	public WwwDatasetHarvest(WwwDataset dataset, Repository repository, boolean skipExistingResources, JobObserver observer) {
		this(dataset, repository, observer);
		this.skipExistingResources = skipExistingResources;
	}

	public Calendar startProcess() {
		Calendar start=new GregorianCalendar();
		SitemapResourceCrawler smCrawler=new SitemapResourceCrawler(dataset.getUri(), null, new CrawlResourceHandler() {
			int harvestedCnt=0;
			@Override
			public void handleUrl(SiteMapURL subSm) {
				try {
					if(sampleSize==null || sampleSize<=0 || harvestedCnt<sampleSize)
						if(harvestResource(dataset.getUri(), subSm.getUrl().toString()))
								harvestedCnt++;
				} catch (InterruptedException e) {
					log.warn(e.getMessage(), e);
				} catch (Exception e) {
					datasetLog.logHarvestIssue(subSm.getUrl().toString(), e);
					observer.signalResourceFailure(subSm.getUrl().toString(), e);
				}
			}
		}, observer);
		smCrawler.setSampleSize(sampleSize);
		smCrawler.run();
		runError=smCrawler.getRunError();
		return start;
	}

	private boolean harvestResource(String datasetUri, String uriOfRec) throws Exception, InterruptedException {
		File rdfResourceFile = repository.getFile(datasetUri, uriOfRec);
		if(skipExistingResources && rdfResourceFile.exists()) {
			datasetLog.logSkippedRdfResource();
			return false;
		}
		int retries=retriesMaxAttempts;
		while (retries>=0) {
			try {
				HttpRequest reqRes=HttpUtil.makeRequest(uriOfRec);
				
				int statusCode = reqRes.getResponse().getStatus();
				if(statusCode==200) {
					try {
						String charset = "UTF8";
						if (reqRes.getCharset()!=null)
							charset = reqRes.getCharset().name();
						
						if (MimeType.isData(reqRes.getMimeType())){
							repository.save(datasetUri, reqRes.getUrl(), reqRes.getResponseContent(), new ArrayList<Entry<String, String>>() {{
								add(new SimpleMapEntry("Content-Type", reqRes.getMimeType()));}});
							return true;
						} else {
	//						Jsoup.parse(html)
							ByteArrayOutputStream decodedInput = new ByteArrayOutputStream();
							TurtleWriter triples=new TurtleWriter(decodedInput);
	//						NTriplesWriter triples=new NTriplesWriter(decodedInput);
							any23.extract(reqRes.getResponseContentAsString(), reqRes.getUrl(), reqRes.getMimeType(), charset, 
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
									add(new SimpleMapEntry("Content-Type", MimeType.TURTLE.id()));}});
								break;
							default:
								throw new RuntimeException("Not implemented yet "+dataset.getMicroformat());
							}
							return true;
						}
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
			} catch (AccessException e) {
				if(retries<1) 
					throw e;
			}
			retries--;
			datasetLog.logHarvestIssue(uriOfRec, (String)null);
//							log.debug("Harvester sleeping", e);
				Thread.sleep((retriesMaxAttempts-retries)*retriesSleepMicrosecs);
		}
		return false;
	}


	public Exception getRunError() {
		return runError;
	}
	
	public void setSampleSize(Integer sampleSize) {
		this.sampleSize=sampleSize;
	}

	
}
