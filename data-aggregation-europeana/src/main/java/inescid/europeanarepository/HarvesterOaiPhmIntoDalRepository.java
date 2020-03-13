/* DnbHarvester.java - created on 12 de Abr de 2012, Copyright (c) 2011 The European Library, all rights reserved */
package inescid.europeanarepository;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.store.Repository;
import inescid.oaipmh.HarvestException;
import inescid.oaipmh.OaiPmhRecord;
import inescid.oaipmh.OaipmhHarvest;
import inescid.util.XmlUtil;

/**
 * Runs a simple harvest, counting how many records are obtained.
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 12 de Abr de 2012
 */
public class HarvesterOaiPhmIntoDalRepository {
    private static final Pattern XML_DECLARATION=Pattern.compile("^<\\?xml [^\\?]*\\?>[\\r\\n]*"); 

    Repository dataRepository;
    String datasetId;
    
    BufferedWriter recWriter=null;
    
    int recordCount=0;
    int recordErrorCount=0;
    int recordDeletedCount=0;

    public HarvesterOaiPhmIntoDalRepository(Repository dataRepository, String datasetId) {
    	this.dataRepository = dataRepository;
    	this.datasetId= datasetId;
	}

	public void run(String baseUrl, String metadataFormat, String set) throws HarvestException {
        recordCount=0;
        recordErrorCount=0;
        recordDeletedCount=0;        
        
        OaipmhHarvest harvest=new OaipmhHarvest(baseUrl, null, null, metadataFormat, set);
        
        int reportinterval=1000;
        while (harvest.hasNext()) {
            OaiPmhRecord r=harvest.next();
            if(!r.isDeleted()) {
                recordCount++;
                try {
					writeRecord(r);
				} catch (IOException e) {
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

    
    private void writeRecord(OaiPmhRecord r) throws IOException {
        String xmlRec = XmlUtil.writeDomToString(r.getMetadata());
        dataRepository.save(datasetId, r.getIdentifier(), xmlRec);
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

		//		Global.init_componentHttpRequestService();
//		HttpsUtil.initSslTrustingHostVerifier();
		
		Global.init_enableComponentHttpRequestCache();
		Repository dataRepository = Global.getDataRepository();
    	
		HarvesterOaiPhmIntoDalRepository harvester=new HarvesterOaiPhmIntoDalRepository(dataRepository, datasetId);
        harvester.run(oaiBaseUrl, "edm", null);
        System.out.printf("Harvested %d records, and found %d deleted records.\n", harvester.getRecordCount(), harvester.getRecordDeletedCount());
    }
    
}
