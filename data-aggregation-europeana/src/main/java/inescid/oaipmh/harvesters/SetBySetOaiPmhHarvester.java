/* DnbHarvester.java - created on 12 de Abr de 2012, Copyright (c) 2011 The European Library, all rights reserved */
package inescid.oaipmh.harvesters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.oclc.oai.harvester2.verb.ListSets;
import org.w3c.dom.DOMException;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import inescid.oaipmh.OaiPmhRecord;
import inescid.oaipmh.OaipmhHarvest;

/**
 * A harvester that harvests oaipmh repositories by requesting one single day at a time.
 * This method of harvesting is necessary when oaipmh servers are not very stable, or have limits on the amount of records that can be sent in one oai-pmh request. 
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 12 de Abr de 2012
 */
public abstract class SetBySetOaiPmhHarvester {
    /** number of attempts before skipping harvesting a day due to errors */
    private int maximumRetries=3;

    String baseUrl;
    String metadataFormat;
    
    List<String> sets;
    
    boolean detectDuplicates=false;
    Set<String> harvestedIds=new HashSet<String>();
    
    /**
     * Creates a new instance of this class.
     * @param baseUrl
     * @param metadataFormat
     */
    public SetBySetOaiPmhHarvester(String baseUrl, String metadataFormat) {
        try {
            this.baseUrl = baseUrl;
            this.metadataFormat = metadataFormat;
            
            ListSets listSets=new ListSets(baseUrl);
            NodeList nodeList = listSets.getNodeList("//oai20:setSpec");
            sets=new ArrayList<String>(nodeList.getLength());
            for (int i=0; i < nodeList.getLength(); i++) {
                sets.add(nodeList.item(i).getNodeValue());
            }
        } catch (DOMException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (SAXException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (TransformerException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }



    /**
     */
    public void harvestSetBySet() {
        int setRetry=0;
        for(int setIdx=0; setIdx<sets.size(); setIdx++) {
            String set=sets.get(setIdx);
            if(harvestSet(set)) try {
                setStarted(set);
                OaipmhHarvest harvest=new OaipmhHarvest(baseUrl, null, null, metadataFormat, set);
                
                while (harvest.hasNext()) {
                    OaiPmhRecord r=harvest.next();
                    try {
                        if(detectDuplicates) {
                            if (!harvestedIds.contains(r.getIdentifier())) {                            
                                handleRecord(r);
                                harvestedIds.add(r.getIdentifier());
                            }
                        }else
                            handleRecord(r);
                    } catch (Exception e) {
                        errorOnRecord(r, e);
                    }
                }
            
                setRetry=0;
                setEnded(set);
            } catch (Exception e) {
                setRetry++;
                if(setRetry<=maximumRetries) {
                    setRestarted(set);
                    setIdx--;
                }else {
                    //proceed to next day;
                    setFailed(set);
                }
            }
        }  
        harvestSetBySetEnded();
    }


    
    /**
     * @param set
     * @return true if the set should be harveste, false if it should be skipped
     */
    protected boolean harvestSet(String set) {
        return true;
    }

    /**
     * Invoked when an error occurs in a harvest of a set
     * 
     * @param set
     */
    protected void setFailed(String set) {        
    }
    
    /**
     * Invoked when an error occurs in a harvest of a set and it is restarted
     * 
     * @param set
     */
    protected void setRestarted(String set) {
    }
    
    /**
     * Invoked when a set is started
     * @param set
     */
    protected void setStarted(String set) {
    }

    /**
     * Invoked when a set ends
     * @param set
     */
    protected void setEnded(String set) {
    }

    /**
     * Invoked when the complete harvest ends
     */
    protected void harvestSetBySetEnded() {
    }
    
    private void errorOnRecord(OaiPmhRecord rec, Exception e) {
        System.err.println(rec.getIdentifier());
        e.printStackTrace();
    }
    
    /**
     * Processes a harvested record
     * 
     * @param r
     */
    protected abstract void handleRecord(OaiPmhRecord r);

    /**
     * Sets the maximumRetries
     * @param maximumRetries the maximumDayRetries to set
     */
    public void setMaximumSetRetries(int maximumRetries) {
        this.maximumRetries = maximumRetries;
    }



    /**
     * Sets the detectDuplicates
     * @param detectDuplicates the detectDuplicates to set
     */
    public void setDetectDuplicates(boolean detectDuplicates) {
        this.detectDuplicates = detectDuplicates;
    }
}
