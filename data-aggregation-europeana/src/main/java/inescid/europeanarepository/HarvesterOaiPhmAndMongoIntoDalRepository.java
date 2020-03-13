/* DnbHarvester.java - created on 12 de Abr de 2012, Copyright (c) 2011 The European Library, all rights reserved */
package inescid.europeanarepository;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import eu.europeana.corelib.edm.utils.EdmUtils;
import eu.europeana.corelib.mongo.server.impl.EdmMongoServerImpl;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.web.exception.EuropeanaException;
import inescid.dataaggregation.crawl.http.HttpRequest;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.store.Repository;
import inescid.oaipmh.HarvestException;
import inescid.oaipmh.OaiPmhRecord;
import inescid.oaipmh.OaipmhHarvest;
import inescid.oaipmh.OaipmhHarvestForIdentifiersOnly;
import inescid.util.ThreadedRunner;
import inescid.util.XmlUtil;

/**
 * Runs a simple harvest, counting how many records are obtained.
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 12 de Abr de 2012
 */
public class HarvesterOaiPhmAndMongoIntoDalRepository {
    private static final Pattern XML_DECLARATION=Pattern.compile("^<\\?xml [^\\?]*\\?>[\\r\\n]*"); 
    private static final Pattern EUROPEANA_IDENTIFIER=Pattern.compile("(/[^/]+/[^/]+)$"); 
    private static final String DATE_FORMAT="yyyy-MM-dd"; 

    Repository dataRepository;
    String datasetId;
	MongoClient mongoClient ;
	EdmMongoServerImpl mongoEdm;
    
    BufferedWriter recWriter=null;
    
    int recordCount=0;
    int recordErrorCount=0;
    int recordDeletedCount=0;

    public HarvesterOaiPhmAndMongoIntoDalRepository(Repository dataRepository, String datasetId) {
    	this.dataRepository = dataRepository;
    	this.datasetId= datasetId;

		mongoClient = new MongoClient(new MongoClientURI(
                "mongodb://rnd-2.eanadev.org:27017/admin"));
		mongoEdm=new EdmMongoServerImpl(mongoClient, "metis-publish-production-2", false);
//		EdmMongoServerImpl edmMongo=new EdmMongoServerImpl(cli, "metis-preview-production-2", false);
//		EdmMongoServerImpl edmMongo=new EdmMongoServerImpl(cli, "europeana_production_publish_1", false);
	}

	public void run(String baseUrl, String metadataFormat, String set, Date from, Date until) throws HarvestException {
        recordCount=0;
        recordErrorCount=0;
        recordDeletedCount=0;        
        ThreadedRunner runner=new ThreadedRunner(5);
        final boolean overwriteExistingRecords = from!=null;
        OaipmhHarvestForIdentifiersOnly harvest=new OaipmhHarvestForIdentifiersOnly(baseUrl, 
        		from==null ? null : new SimpleDateFormat(DATE_FORMAT).format(from), 
				until==null ? null : new SimpleDateFormat(DATE_FORMAT).format(until), metadataFormat, set);
        
        int reportinterval=1000;
        while (harvest.hasNext()) {
            final OaiPmhRecord r=harvest.next();
            if(!r.isDeleted()) {
                recordCount++;
                try {
                	runner.run(new Runnable() {
						public void run() {
							try {
								writeRecord(r, overwriteExistingRecords);
							} catch (Exception e) {
								throw new RuntimeException(e.getMessage(), e);
							}
						}
					});
				} catch (InterruptedException e) {
					System.err.println("Interrupted. exiting...");
					break;
				} catch (ExecutionException e) {
					recordErrorCount++;
					System.err.println("Error in rec: "+r.getIdentifier());
					e.printStackTrace();
				}
            }else 
                recordDeletedCount++;
            
            reportinterval--;
            if (reportinterval<=0) {
            	System.out.println("Harvest progress:");
            	System.out.println("count: "+recordCount);
            	System.out.println("count del.: "+recordDeletedCount);
            	System.out.println("count error: "+recordErrorCount);
            	reportinterval=1000;
            }
        }
    }

    
    private void writeRecord(OaiPmhRecord r, boolean overwriteExistingRecords) throws IOException, InterruptedException {
    	if(!overwriteExistingRecords && dataRepository.contains(datasetId, r.getIdentifier())) {
    		
    			System.out.println("exists "+r.getIdentifier());
    			return;
    		}
    	Matcher matcher = EUROPEANA_IDENTIFIER.matcher(r.getIdentifier());
		if(!matcher.find()) 
		{
			System.out.println("pattern not found "+r.getIdentifier());
			return;
		}
    	FullBeanImpl fullBean=null;
		try {
			fullBean = (FullBeanImpl) mongoEdm.getFullBean(matcher.group(1));
		} catch (EuropeanaException e) {
			throw new IOException(e);
		}		
		
		if(fullBean!=null)
			dataRepository.save(datasetId, r.getIdentifier(), EdmUtils.toEDM(fullBean));
    }
    
    /**
     * Returns the recordCount.
     * @return the recordCount
     */
    public int getRecordCount() {
        return recordCount;
    }

    /**
     * Returns the recordDeletedCount.
     * @return the recordDeletedCount
     */
    public int getRecordDeletedCount() {
        return recordDeletedCount;
    }
    
    public static void main(String[] args) throws Exception {
    	String repoFolder = "c://users/nfrei/desktop/data/EuropeanaRepository";
    	String oaiBaseUrl = "https://oai-pmh.eanadev.org/oai"; //"https://api.europeana.eu/oai/record"
    	String datasetId = "data.europeana.eu";
		
		if(args!=null) {
			if(args.length>=1) {
				repoFolder = args[0];
				if(args.length>=2) { 
					oaiBaseUrl = args[1];
					if(args.length>=3) 
						datasetId = args[2];
				}
			}
		}
		
		Global.init_componentDataRepository(repoFolder);

				Global.init_componentHttpRequestService();
//		HttpsUtil.initSslTrustingHostVerifier();
		
		Global.init_enableComponentHttpRequestCache();
		Repository dataRepository = Global.getDataRepository();
    	
		HarvesterOaiPhmAndMongoIntoDalRepository harvester=new HarvesterOaiPhmAndMongoIntoDalRepository(dataRepository, datasetId);
        harvester.run(oaiBaseUrl, "edm", null, null, null);
        System.out.printf("Harvested %d records, and found %d deleted records.\n", harvester.getRecordCount(), harvester.getRecordDeletedCount());
        Global.shutdown();
    }
    
}
