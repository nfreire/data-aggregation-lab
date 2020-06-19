package inescid.dataaggregation.crawl.ld;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import inescid.dataaggregation.data.model.RdfReg;
import inescid.dataaggregation.dataset.LodDataset;
import inescid.dataaggregation.dataset.metadata.DatasetDescription;
import inescid.dataaggregation.store.Repository;
import inescid.util.AccessException;
import inescid.util.DatasetLog;
import inescid.util.HttpUtil;
import inescid.util.RdfUtil;
import inescid.util.SparqlClient;
import inescid.util.SparqlClient.Handler;

public class LdDatasetHarvest {
	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LdDatasetHarvest.class);
	private static long retriesSleepMicrosecs=20;
	private static int retriesMaxAttempts=1;
	
	LodDataset dataset;
	boolean skipExistingResources=false;
	DatasetLog datasetLog;
	Repository repository;
	Integer sampleSize;
	
	public LdDatasetHarvest(LodDataset dataset, Repository repository) {
		super();
		this.dataset = dataset;
		datasetLog=new DatasetLog(dataset.getUri());
		this.repository = repository;
	}
	
	public LdDatasetHarvest(inescid.dataaggregation.dataset.LodDataset dataset, Repository repository, boolean skipExistingResources) {
		this(dataset, repository);
		this.skipExistingResources = skipExistingResources;
	}

	public Calendar startProcess() throws AccessException {
		Calendar start=new GregorianCalendar();
		try {
			DatasetDescription desc=new DatasetDescription(dataset.getUri());
			List<String> rootResources = desc.listRootResources();
			if(rootResources==null || rootResources.isEmpty()) {
			    String sparqlEndpoint = desc.getSparqlEndpoint();
			    if(sparqlEndpoint!=null) {
			    	String queryTmp = desc.getSparqlEndpointQuery();	
			    	if (StringUtils.isEmpty(queryTmp))
			    		queryTmp="SELECT DISTINCT ?s  WHERE { ?s ?p ?o }";
			    	final String query = queryTmp;	
			    	SparqlClient sparql=new SparqlClient(sparqlEndpoint, (String)null);
			    	sparql.query(query, new Handler() {
						public boolean handleSolution(QuerySolution solution) throws Exception {
							Iterator<String> varNames = solution.varNames();
							if(varNames.hasNext()) {
								rootResources.add(solution.getResource(varNames.next()).getURI());
							} else 
								throw new Exception("Invalid query: "+query);
							return true;
						}
					});
			    	
			    } else { //try a Distribution of the dataset
			    	throw new RuntimeException("TODO");
			    }
			} else {
				harvestRootResources(dataset.getUri(), rootResources);
			}
		} catch (InterruptedException | IOException e) {
			datasetLog.logHarvestIssue(dataset.getUri(), "Dataset harvest failed");
		}
		return start;
	}

	private void harvestRootResources(String datasetUri, List<String> voidRootResources) throws IOException {
		int harvestedCnt=0;
		for(String uri: voidRootResources) {
			if(harvestResource(datasetUri, uri))
				harvestedCnt++;
			if(sampleSize!=null && sampleSize>0 && harvestedCnt>=sampleSize)
				break;
		}
		datasetLog.logFinish();
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
				List<Entry<String, String>> headers=HttpUtil.getStoreAndReturnHeaders(uriOfRec, rdfResourceFile);
				repository.saveMeta(datasetUri, uriOfRec, headers);
				datasetLog.logHarvestSuccess();
				return true;
			} catch (InterruptedException e) {
				log.debug(uriOfRec, e);
			} catch (Exception e) {
				retries--;
				if(retries<0) {
					log.error(uriOfRec, e);
					datasetLog.logHarvestIssue(uriOfRec, e.getMessage());
				}else {
					log.debug(uriOfRec, e);
					try {
//							log.debug("Harvester sleeping", e);
						Thread.sleep((retriesMaxAttempts-retries)*retriesSleepMicrosecs);
					} catch (InterruptedException ei) {
						log.warn(uriOfRec, ei);
						break;
					}
				}
			}
		}
		return false;
	}

	public void setSampleSize(Integer sampleSize) {
		this.sampleSize=sampleSize;
	}

	
}
