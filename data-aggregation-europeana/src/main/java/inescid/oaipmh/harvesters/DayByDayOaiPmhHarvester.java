/* DnbHarvester.java - created on 12 de Abr de 2012, Copyright (c) 2011 The European Library, all rights reserved */
package inescid.oaipmh.harvesters;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import inescid.oaipmh.HarvestException;
import inescid.oaipmh.OaiPmhRecord;
import inescid.oaipmh.OaipmhHarvest;

/**
 * A harvester that harvests oaipmh repositories by requesting one single day at a time.
 * This method of harvesting is necessary when oaipmh servers are not very stable, or have limits on the amount of records that can be sent in one oai-pmh request. 
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 12 de Abr de 2012
 */
public abstract class DayByDayOaiPmhHarvester {
    /** number of attempts before skipping harvesting a day due to errors */
    private int maximumDayRetries=3;

    String baseUrl;
    String metadataFormat;
    String set;
    
    /**
     * Creates a new instance of this class.
     * @param baseUrl
     * @param metadataFormat
     * @param set
     */
    public DayByDayOaiPmhHarvester(String baseUrl, String metadataFormat, String set) {
        this.baseUrl = baseUrl;
        this.metadataFormat = metadataFormat;
        this.set = set;
    }



    /**
     * @param startDate
     * @param endDate
     */
    public void harvestDayByDay(Date startDate, Date endDate) {
        Date endDateVar=endDate;
        if (endDateVar==null)
            endDateVar=new Date();
        if(!startDate.before(endDateVar))
            throw new IllegalArgumentException("startDate must be before endDate");
        
        Calendar stopHarvestDate=GregorianCalendar.getInstance();
        stopHarvestDate.setTime(endDateVar);
        
        Calendar dayToHarvest=GregorianCalendar.getInstance();
        dayToHarvest.setTime(startDate);
        
        int dayRetry=0;
        while(dayToHarvest.before(stopHarvestDate)) {
            Date day=dayToHarvest.getTime();
            String from=new SimpleDateFormat("yyyy-MM-dd").format(day);
            dayToHarvest.add(Calendar.DAY_OF_YEAR, 1);
            String until=new SimpleDateFormat("yyyy-MM-dd").format(dayToHarvest.getTime());
            try {
                dayStarted(day);
                OaipmhHarvest harvest=new OaipmhHarvest(baseUrl, from, until, metadataFormat, set);
                
                while (harvest.hasNext()) {
                    OaiPmhRecord r=harvest.next();
                    try {
                        handleRecord(r);
                    } catch (Exception e) {
                        errorOnRecord(r, e);
                    }
                }
            
                dayRetry=0;
                dayEnded(day);
            } catch (Exception e) {
                dayRetry++;
                if(dayRetry<=maximumDayRetries) {
                    dayToHarvest.add(Calendar.DAY_OF_YEAR, -1);
                    dayRestarted(day);
                }else {
                    //proceed to next day;
                    dayFailed(day);
                }
            }
        }  
        harvestDayByDayEnded();
    }


    /**
     * @param endDate
     * @throws HarvestException
     */
    public void harvestUntil(Date endDate) throws HarvestException{
        String until=new SimpleDateFormat("yyyy-MM-dd").format(endDate);
        OaipmhHarvest harvest=new OaipmhHarvest(baseUrl, null, until, metadataFormat, set);
        while (harvest.hasNext()) {
            OaiPmhRecord r=harvest.next();
            try {
                handleRecord(r);
            } catch (Exception e) {
                errorOnRecord(r, e);
            }
        }
            
    }
    
    /**
     * Invoked when an error occurs in a specific day harvest
     * @param day
     */
    protected void dayFailed(Date day) {        
    }
    
    /**
     * Invoked when an error occured in a specific day harvest, and it is restarted from the beggining
     * 
     * @param day
     */
    protected void dayRestarted(Date day) {
    }
    
    /**
     * Invoked when an specific day harvest starts
     * 
     * @param day
     */
    protected void dayStarted(Date day) {
    }

    /**
     * Invoked when an specific day harvest ends
     * @param day
     */
    protected void dayEnded(Date day) {
    }

    /**
     * Invoked when an specific the harvest ends
     * 
     */
    protected void harvestDayByDayEnded() {
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
     * Sets the maximumDayRetries
     * @param maximumDayRetries the maximumDayRetries to set
     */
    public void setMaximumDayRetries(int maximumDayRetries) {
        this.maximumDayRetries = maximumDayRetries;
    }
}
