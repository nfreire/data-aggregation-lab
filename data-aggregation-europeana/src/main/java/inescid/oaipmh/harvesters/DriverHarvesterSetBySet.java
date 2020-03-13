/* DnbHarvester.java - created on 12 de Abr de 2012, Copyright (c) 2011 The European Library, all rights reserved */
package inescid.oaipmh.harvesters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.w3c.dom.Element;

import inescid.oaipmh.OaiPmhRecord;

/**
 * 
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 12 de Abr de 2012
 */
public class DriverHarvesterSetBySet extends SetBySetOaiPmhHarvester {
    private static final Pattern XML_DECLARATION=Pattern.compile("^<\\?xml [^\\?]*\\?>[\\r\\n]*"); 
    
    BufferedWriter recWriter=null;
    int recCount=0;
    
    /**
     * Creates a new instance of this class.
     * @param baseUrl
     * @param metadataFormat
     * @param set
     */
    public DriverHarvesterSetBySet(String baseUrl, String metadataFormat) {
        super(baseUrl, metadataFormat);
    }

    public static void main(String[] args) throws Exception {
        DriverHarvesterSetBySet harvester=new DriverHarvesterSetBySet("http://oai.driver.research-infrastructures.eu/", "DMF");
        harvester.harvestSetBySet();
    }

    
    @Override
    protected void setStarted(String set) {
        try {
            if(recWriter!=null) {
                recWriter.write("</metadataExport>");          
                recWriter.close();
                recWriter=null;
            }
            if(recWriter==null) {
                recWriter=new BufferedWriter(new FileWriterWithEncoding("driver-"+set+".xml", "UTF-8"));
                recWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                recWriter.write("<metadataExport>\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    protected boolean harvestSet(String set) {
        File file = new File("driver-"+set+".xml");
        return !file.exists() || file.length()==0;
    }
    
    
    @Override
    protected void harvestSetBySetEnded() {
        try {
            if(recWriter!=null) {
                recWriter.write("</metadataExport>");          
                recWriter.close();
                recWriter=null;
            }
            System.out.println(recCount+" harvested int total.");
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    @Override
    protected void handleRecord(OaiPmhRecord r) {
        try {
            if (!r.isDeleted()) {
                ((Element)(r.getMetadata())).setAttribute("oaiId", r.getIdentifier());
                String xmlRec = XML_DECLARATION.matcher(XmlUtil.writeDomToString(r.getMetadata())).replaceFirst("");
                recWriter.write(xmlRec);
//                recWriter.write("</metadataRecord>\n" );
                recCount++;
                if(recCount % 1000 == 0)
                    System.out.println(recCount+" harvested.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    
    
}
