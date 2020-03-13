/* OaipmhHarvestByListIdentifiers.java - created on 25/02/2015, Copyright (c) 2011 The European Library, all rights reserved */
package inescid.oaipmh;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.oclc.oai.harvester2.verb.GetRecord;
import org.oclc.oai.harvester2.verb.ListIdentifiers;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * 
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 25/02/2015
 */
public class OaipmhHarvestByListIdentifiers extends OaipmhHarvest{

    /**
     * Creates a new instance of this class.
     * @param baseURL
     * @param from
     * @param until
     * @param metadataPrefix
     * @param setSpec
     * @throws HarvestException
     */
    public OaipmhHarvestByListIdentifiers(String baseURL, String from, String until,
                                          String metadataPrefix, String setSpec)
                                                                                throws HarvestException {
        super(baseURL, from, until, metadataPrefix, setSpec);
    }

    /**
     * Creates a new instance of this class.
     * @param baseURL
     * @param metadataPrefix
     * @param setSpec
     * @throws HarvestException
     */
    public OaipmhHarvestByListIdentifiers(String baseURL, String metadataPrefix, String setSpec)
                                                                                                throws HarvestException {
        super(baseURL, metadataPrefix, setSpec);
    }

    /**
     * Creates a new instance of this class.
     * @param baseURL
     * @param resumptionToken
     */
    public OaipmhHarvestByListIdentifiers(String baseURL, String resumptionToken) {
        super(baseURL, resumptionToken);
    }

    
    protected synchronized void fetchFirstRecords() throws HarvestException {
        int tries = 0;
        while (true) {
            tries++;
            ListIdentifiers listRecords = null;
            try {
                long start = System.currentTimeMillis();
                listRecords = new ListIdentifiers(baseURL, from, until, setSpec, metadataPrefix);
                movingDiff = System.currentTimeMillis() - start;
                log(String.format(
                        "Load first records from " + listRecords.getRequestURL() + " in %.3f sec.",
                        movingDiff / 1000.0));

                processListIdentifiersResponse(listRecords);
                
                return;
            } catch (java.io.CharConversionException e) {
                String recoveredResumptionToken = recoverResumptionToken(getRequestURL(baseURL, from, until, setSpec, metadataPrefix));
                if(recoveredResumptionToken!=null && !recoveredResumptionToken.isEmpty())
                    nextResumptionToken=recoveredResumptionToken;
                else {
                    // cannot continue, because we can't find
                    // the next token => next token is null
                    nextResumptionToken = null;
                    throw new HarvestException("Failed <" + getRequestURL(baseURL, from, until, setSpec, metadataPrefix), e);
                }
            } catch (TransformerException e) {
                String recoveredResumptionToken = recoverResumptionToken(getRequestURL(baseURL, from, until, setSpec, metadataPrefix));
                if(recoveredResumptionToken!=null && !recoveredResumptionToken.isEmpty())
                    nextResumptionToken=recoveredResumptionToken;
                else {
                    // cannot continue, because we can't find
                    // the next token => next token is null
                    nextResumptionToken = null;
                    throw new HarvestException("Failed <" + getRequestURL(baseURL, from, until, setSpec, metadataPrefix), e);
                }
            } catch (ParserConfigurationException e) {
                String recoveredResumptionToken = recoverResumptionToken(getRequestURL(baseURL, from, until, setSpec, metadataPrefix));
                if(recoveredResumptionToken!=null && !recoveredResumptionToken.isEmpty())
                    nextResumptionToken=recoveredResumptionToken;
                else {
                    // cannot continue, because we can't find
                    // the next token => next token is null
                    nextResumptionToken = null;
                    throw new HarvestException("Failed <" + getRequestURL(baseURL, from, until, setSpec, metadataPrefix), e);
                }
            } catch (NoSuchFieldException e) {
                String recoveredResumptionToken = recoverResumptionToken(getRequestURL(baseURL, from, until, setSpec, metadataPrefix));
                if(recoveredResumptionToken!=null && !recoveredResumptionToken.isEmpty())
                    nextResumptionToken=recoveredResumptionToken;
                else {
                    // cannot continue, because we can't find
                    // the next token => next token is null
                    nextResumptionToken = null;
                    throw new HarvestException("Failed <" + getRequestURL(baseURL, from, until, setSpec, metadataPrefix), e);
                }
            } catch (SAXException e) {
                String recoveredResumptionToken = recoverResumptionToken(getRequestURL(baseURL, from, until, setSpec, metadataPrefix));
                if(recoveredResumptionToken!=null && !recoveredResumptionToken.isEmpty())
                    nextResumptionToken=recoveredResumptionToken;
                else {
                    // cannot continue, because we can't find
                    // the next token => next token is null
                    nextResumptionToken = null;
                    throw new HarvestException("Failed <" + getRequestURL(baseURL, from, until, setSpec, metadataPrefix), e);
                }
            } catch (IOException e) {
                if (tries > 3) throw new HarvestException("Failed <" + getRequestURL(baseURL, nextResumptionToken), e);
                try {
                    long time = 15000 * (tries * tries * tries);
                    log("Failed <" + getRequestURL(baseURL, from, until, setSpec, metadataPrefix) + " " + e.getClass().getName()+" "+ e.getMessage()+" " +
                                        "> going to retry in: " + time / 1000 + " sec.");
                    Thread.sleep(time);
                } catch (InterruptedException e1) {
                    // ignore
                }
            }
        }
    }

    protected synchronized void fetchNextIfEmpty() throws HarvestException {
        try {
            if (records.isEmpty()) {
                if (nextResumptionToken == null || "".equals(nextResumptionToken)) { return; }
                processListIdentifiersResponse(resumeListIdentifiersWithRetry());
            }
        } catch (HarvestException e) {
            throw e;
        } catch (Throwable e) {
            throw new HarvestException(getRequestURL(baseURL, nextResumptionToken), e);
        }
    }

    protected void processListIdentifiersResponse(ListIdentifiers listIdentifiers) throws HarvestException,
            TransformerException, NoSuchFieldException, DOMException, IOException, ParserConfigurationException, SAXException {
        NodeList errors = listIdentifiers.getErrors();
        if (errors != null && errors.getLength() > 0) {
            int length = errors.getLength();
            for (int i = 0; i < length; ++i) {
                Node item = errors.item(i);
                Node code = item.getAttributes().getNamedItem("code");
                if (code != null && "noRecordsMatch".equals(code.getNodeValue())) {
                    // only in this case we do not treat it as an error.
                } else {
                    throw new HarvestException(listIdentifiers.getRequestURL() + " returned error:" +
                                               item.getNodeValue());
                }
            }
        } else {
//            String xpath = "//ListIdentifiers/header";
//            String uri0 = listIdentifiers.getDocument().getDocumentElement().getNamespaceURI();
//            if ("http://www.openarchives.org/OAI/2.0/".equals(uri0)) {
//                xpath = "//oai20:ListIdentifiers/oai20:header";
//            }
            String xpath = "//ListIdentifiers/header";
            String uri0 = listIdentifiers.getDocument().getDocumentElement().getNamespaceURI();
            if ("http://www.openarchives.org/OAI/2.0/".equals(uri0)) {
                xpath = "//oai20:ListIdentifiers/oai20:header";
            }
            NodeList nodeList = listIdentifiers.getNodeList(xpath);

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node headerNode = nodeList.item(i);

                OaiPmhRecord record = new OaiPmhRecord(headerNode);
//                OaiPmhRecord record = new OaiPmhRecord();
//                NodeList childNodes = headerNode.getChildNodes();
//                for (int idx = 0; idx < childNodes.getLength(); idx++) {
//                    Node childNode = childNodes.item(idx);
//                    if (childNode instanceof Element && childNode.getLocalName().equals("identifier")) {
//                        record.setIdentifier(childNode.getNodeValue());
//                        break;
//                    }
//                }
//                Node statusNode = headerNode
//                        .getAttributes().getNamedItem("status");
//                if (statusNode != null) {
//                    record.setDeleted(statusNode.getNodeValue().equals("deleted"));
//                } else {
//                    record.setDeleted(false);
//                }
//                
                if(!record.isDeleted())
                    record=getRecord(record);
                records.offer(record);
            }

            nextResumptionToken = listIdentifiers.getResumptionToken();
            
            
            if (this.completeListSize == -1) {
                String listSizeXpath = "/oai20:OAI-PMH/oai20:ListIdentifiers/oai20:resumptionToken/@completeListSize";
                if ("http://www.openarchives.org/OAI/2.0/".equals(uri0))
                    listSizeXpath = "//oai20:ListIdentifiers/oai20:resumptionToken/@completeListSize";
                String sizeStr = listIdentifiers.getSingleString(listSizeXpath);
                if (sizeStr != null && !sizeStr.isEmpty()) try {
                    completeListSize = Integer.parseInt(sizeStr);
                } catch (NumberFormatException e) {
                    // just ignore
                }
            }

        }
    }

    /**
     * @param record
     * @throws TransformerException 
     * @throws SAXException 
     * @throws ParserConfigurationException 
     * @throws IOException 
     * @throws HarvestException 
     * @throws DOMException 
     */
    public static OaiPmhRecord getRecord(String baseURL, String recordIdentifier, String metadataPrefix) throws IOException, ParserConfigurationException, SAXException, TransformerException, DOMException, HarvestException {
    	int attemps=0;
    	while(attemps<3) try {
    		attemps++;
			GetRecord getRecord=new GetRecord(baseURL, recordIdentifier, metadataPrefix);
			NodeList errors = getRecord.getErrors();
			if (errors != null && errors.getLength() > 0) {
			    Node item = errors.item(0);
			    throw new HarvestException(getRecord.getRequestURL() + " returned error:" +
			            item.getNodeValue());
			} else {
			    String xpath = "//GetRecord/record";
			    String uri0 = getRecord.getDocument().getDocumentElement().getNamespaceURI();
			    if ("http://www.openarchives.org/OAI/2.0/".equals(uri0)) {
			        xpath = "//oai20:GetRecord/oai20:record";
			    }
			    
			    NodeList nodeList = getRecord.getNodeList(xpath);
			    
			    if (nodeList.getLength()==1) {
			        Node recNode = nodeList.item(0);
			        return new OaiPmhRecord(recNode);
			    }
			    return null;
			}
    	} catch (IOException e) {
    		if(attemps>=3)
    			throw e;
    	} catch (ParserConfigurationException e) {
    		if(attemps>=3)
    			throw e;
    	} catch (TransformerException e) {
    		if(attemps>=3)
    			throw e;
    	} catch (SAXException e) {
    		if(attemps>=3)
    			throw e;
    	} catch (DOMException e) {
    		if(attemps>=3)
    			throw e;
		} catch (HarvestException e) {
			if(attemps>=3)
				throw e;
		}
    	return null;        
    }
    
    protected OaiPmhRecord getRecord(OaiPmhRecord record) throws IOException, ParserConfigurationException, SAXException, TransformerException, DOMException, HarvestException {
        return getRecord(baseURL, record.getIdentifier(), metadataPrefix);
    }

    /**
     * Executes a list records request and in case of failure, it waits for some seconds and tries
     * again.
     * 
     * @return a ListRecords
     * @throws Exception
     *             if the maximum number of retries was reached
     */
    protected ListIdentifiers resumeListIdentifiersWithRetry() throws Exception {
        int tries = 0;
        while (true) {
            tries++;
            ListIdentifiers listRecords = null;
            try {
                long start = System.currentTimeMillis();
                listRecords = new ListIdentifiers(baseURL, nextResumptionToken);
                movingDiff = (System.currentTimeMillis() - start);
                
                long prevBatchRecords = totalRetrievedRecords - movingCount;
                double currentAverage = (prevBatchRecords * 1000.0) / movingDiff;
                movingAverage = (movingAverage * movingSteps + currentAverage) / (movingSteps + 1.0); 

                log(listRecords.getRequestURL() +
                        String.format(" took %.3f sec. Average: %.3f/sec, (Last m=%.3f/sec, %d, %d)",
                                movingDiff / 1000.0, movingAverage, currentAverage, prevBatchRecords, movingSteps));

                movingCount = totalRetrievedRecords;
                movingSteps++;
                return listRecords;
            } catch (SAXParseException e) {
                String recoveredResumptionToken = recoverResumptionToken(getRequestURL(baseURL, nextResumptionToken));
                System.out.println("");
                if(recoveredResumptionToken!=null && !recoveredResumptionToken.isEmpty())
                    nextResumptionToken=recoveredResumptionToken;
                else {
                    // cannot continue, because we can't find
                    // the next token => next token is null
                    nextResumptionToken = null;
                    throw e;
                }
            } catch (java.io.CharConversionException e) {
                String recoveredResumptionToken = recoverResumptionToken(getRequestURL(baseURL, nextResumptionToken));
                if(recoveredResumptionToken!=null && !recoveredResumptionToken.isEmpty())
                    nextResumptionToken=recoveredResumptionToken;
                else {
                    // cannot continue, because we can't find
                    // the next token => next token is null
                    nextResumptionToken = null;
                    throw e;
                }
            } catch (IOException e) {
                System.out.println("IOException block");
                if (tries > 3) throw e;
                try {
                    long time = 15000 * (tries * tries * tries);
                    log("Failed <" + getRequestURL(baseURL, nextResumptionToken) + " " + e.getClass().getName()+" "+ e.getMessage()+" " +
                                        "> going to retry in: " + time / 1000 + " sec.");
                    Thread.sleep(time);
                } catch (InterruptedException e1) {
                    // ignore
                }
            }
        }
    }


    /**
     * Construct the query portion of the http request (resumptionToken version)
     * 
     * @param baseURL
     * @param resumptionToken
     * @return
     */
    protected static String getRequestURL(String baseURL, String resumptionToken) {
        StringBuffer requestURL = new StringBuffer(baseURL);
        requestURL.append("?verb=ListIdentifiers");
        try {
            requestURL.append("&resumptionToken=").append(resumptionToken == null ? "" : 
                    URLEncoder.encode(resumptionToken, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Could not encode '" + resumptionToken == null ? "" : resumptionToken + "' into UTF-8!", e);
        }
        return requestURL.toString();
    }
}
