package inescid.dataaggregation.crawl.ld;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.jena.iri.IRI;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import inescid.dataaggregation.dataset.LodDataset;
import inescid.dataaggregation.dataset.convert.RdfReg;
import inescid.dataaggregation.dataset.store.Repository;
import inescid.util.DatasetLog;
import inescid.util.LinkedDataUtil;
import inescid.util.ListOnTxtFile;
import inescid.util.RdfResourceAccessException;

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

	public Calendar startProcess() throws RdfResourceAccessException {
		Calendar start=new GregorianCalendar();
		try {
			Resource  dsResource = LinkedDataUtil.getResource(dataset.getUri());
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
//		List<String> list=new ArrayList<>(LdGlobals.repository.getAllDatasetResourceUris(datasetUri));
//		list.clear();
//		list.openForWrite();
		List<String> list=new ArrayList<>(1000);
		while(voidRootResources.hasNext()) {
			Statement st=voidRootResources.next();
			RDFNode rootResource = st.getObject();
			if(rootResource.isURIResource()) {
				list.add(rootResource.asNode().getURI());
			} else {
				System.out.println("unsupported RDFNode for void:rootResource: "+rootResource.getClass().getCanonicalName());
			}
			if(sampleSize!=null && sampleSize>0 && list.size()>=sampleSize)
				break;
		}
//		list.close();
		
		//harvest resources without paralelism
//		list.openForRead();
		for (String uriOfRec : list) {
//			while(list.hasNext()) {
//				String uriOfRec=list.next();
			File rdfResourceFile = repository.getFile(datasetUri, uriOfRec);
			if(skipExistingResources && rdfResourceFile.exists()) {
				datasetLog.logSkippedRdfResource();
				continue;
			}
			int retries=retriesMaxAttempts;
			while (retries>=0) {
				try {
					LinkedDataUtil.getAndStoreResource(uriOfRec, rdfResourceFile);
					datasetLog.logHarvestSuccess();
					break;
				} catch (InterruptedException e) {
					break;
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
		}
		datasetLog.logFinish();
//		list.close();
	}

	public void setSampleSize(Integer sampleSize) {
		this.sampleSize=sampleSize;
	}

	
}
