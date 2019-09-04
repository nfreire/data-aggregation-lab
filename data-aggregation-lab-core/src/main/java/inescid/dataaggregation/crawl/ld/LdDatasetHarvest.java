package inescid.dataaggregation.crawl.ld;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.http.Header;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import inescid.dataaggregation.data.RdfReg;
import inescid.dataaggregation.dataset.LodDataset;
import inescid.dataaggregation.store.Repository;
import inescid.util.AccessException;
import inescid.util.DatasetLog;
import inescid.util.HttpUtil;
import inescid.util.RdfUtil;

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
			Resource  dsResource = RdfUtil.readRdfResourceFromUri(dataset.getUri());
//			StmtIterator voidRootResources = dsResource.getModel().listStatements(dsResource, RdfReg.VOID_ROOT_RESOURCE, (String)null);
//			StmtIterator voidRootResources = dsResource.getModel().listStatements(dsResource, null, (String)null);
			
			StmtIterator voidRootResources = dsResource.listProperties(RdfReg.VOID_ROOT_RESOURCE);
			if (voidRootResources!=null && voidRootResources.hasNext()) {
				harvestRootResources(dataset.getUri(), voidRootResources);
			} else { //try a Distribution of the dataset
				throw new RuntimeException("TODO");
			}
		} catch (InterruptedException | IOException e) {
			datasetLog.logHarvestIssue(dataset.getUri(), "Dataset harvest failed");
		}
		return start;
	}

	private void harvestRootResources(String datasetUri, StmtIterator voidRootResources) throws IOException {
		int harvestedCnt=0;
		while(voidRootResources.hasNext()) {
			Statement st=voidRootResources.next();
			RDFNode rootResource = st.getObject();
			if(rootResource.isURIResource()) {
				if(harvestResource(datasetUri, rootResource.asNode().getURI()))
					harvestedCnt++;
			} else {
				System.out.println("unsupported RDFNode for void:rootResource: "+rootResource.getClass().getCanonicalName());
			}
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
				List<Header> headers=HttpUtil.getStoreAndReturnHeaders(uriOfRec, rdfResourceFile);
				repository.saveMeta(datasetUri, uriOfRec, HttpUtil.convertHeaderStruct(headers));
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
