/* OaipmhRecord.java - created on 21 de Fev de 2011, Copyright (c) 2011 The European Library, all rights reserved */
package inescid.oaipmh;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import inescid.oaipmh.harvesters.XmlUtil;

/**
 * Models a metadata record harvested by OAI-PMH
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 21 de Fev de 2011
 */
public class OaiPmhRecord {
    /** String identifier */
    protected String  identifier;
    /** Document metadataDom */
    protected Element metadataDom;
    /** boolean deleted */
    protected boolean deleted = false;

//    protected Element oaiDom;
    
    /** OaiPmhRecord provenanceDom */
    protected Element provenanceDom;
    
    // provenance is not used at the moment

    /**
     * Creates a new instance of this class.
     */
    public OaiPmhRecord() {
    }

    /**
     * Creates a new instance of this class.
     * 
     * @param metadataDom
     * @param deleted
     */
    public OaiPmhRecord(Element metadataDom, boolean deleted) {
        super();
        this.metadataDom = metadataDom;
        this.deleted = deleted;
    }

    /**
     * Creates a new instance of this class.
     * @param node
     */
    public OaiPmhRecord(Node node) {
    	if ("header".equals(node.getLocalName())) {
        	parseHeader(node);
    	}else if ("ListIdentifiers".equals(node.getLocalName()) || "record".equals(node.getLocalName())) {
	        NodeList nodes = node.getChildNodes();
	        for (int j = 0; j < nodes.getLength(); j++) {
	            Node candidate = nodes.item(j);
	            if ("header".equals(candidate.getLocalName())) {
	            	parseHeader(candidate);
	            } else if ("metadata".equals(candidate.getLocalName())) {
	                NodeList childNodes = candidate.getChildNodes();
	                for (int idx = 0; idx < childNodes.getLength(); idx++) {
	                    Node childNode = childNodes.item(idx);
	                    if (childNode instanceof Element) {
	                        setMetadata((Element)childNode);
	                        break;
	                    }
	                }
	            } else if ("about".equals(candidate.getLocalName())) {
	                setProvenance((Element)candidate.getFirstChild());
	            }
	        }
    	}
    	
    	
    }

    public void parseHeader(Node node) {
    		NodeList childNodes = node.getChildNodes();
            for (int idx = 0; idx < childNodes.getLength(); idx++) {
                Node childNode = childNodes.item(idx);
                if (childNode instanceof Element 
//                		&& childNode.getLocalName().equals("identifier")
                		) {
                	String text=XmlUtil.getText(childNode);
                    setIdentifier(text);
                    break;
                }
            }
            Node statusNode = node.getAttributes().getNamedItem("status");
            if (statusNode != null) {
                setDeleted(statusNode.getNodeValue().equals("deleted"));
            } else {
                setDeleted(false);
            }
    }
    /**
     * @return true if the record was deleted
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * @return the metadata DOM
     */
    public Element getMetadata() {
        return metadataDom;
    }

    /**
     * @param metadataDom
     */
    public void setMetadata(Element metadataDom) {
        this.metadataDom = metadataDom;
    }

    /**
     * @param deleted
     */
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * @return the OAI identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Returns the provenanceDom.
     * @return the provenanceDom
     */
    public Element getProvenance() {
        return provenanceDom;
    }

    /**
     * Sets the provenanceDom to the given value.
     * @param provenanceDom the provenanceDom to set
     */
    public void setProvenance(Element provenanceDom) {
        this.provenanceDom = provenanceDom;
    }
    
    
    
}
