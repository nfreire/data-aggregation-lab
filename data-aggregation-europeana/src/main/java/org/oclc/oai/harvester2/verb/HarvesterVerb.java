/**
 Copyright 2006 OCLC, Online Computer Library Center
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package org.oclc.oai.harvester2.verb;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xpath.internal.XPathAPI;

import inescid.oaipmh.HarvestException;

/**
 * HarvesterVerb is the parent class for each of the OAI verbs.
 * 
 * @author Jefffrey A. Young, OCLC Online Computer Library Center
 */
public abstract class HarvesterVerb {
    private static Logger logger = Logger.getLogger(HarvesterVerb.class);
    static {
        BasicConfigurator.configure();
    }
    
    /* Primary OAI namespaces */
    public static final String SCHEMA_LOCATION_V2_0 = "http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd";
    public static final String SCHEMA_LOCATION_V1_1_GET_RECORD = "http://www.openarchives.org/OAI/1.1/OAI_GetRecord http://www.openarchives.org/OAI/1.1/OAI_GetRecord.xsd";
    public static final String SCHEMA_LOCATION_V1_1_IDENTIFY = "http://www.openarchives.org/OAI/1.1/OAI_Identify http://www.openarchives.org/OAI/1.1/OAI_Identify.xsd";
    public static final String SCHEMA_LOCATION_V1_1_LIST_IDENTIFIERS = "http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers.xsd";
    public static final String SCHEMA_LOCATION_V1_1_LIST_METADATA_FORMATS = "http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats.xsd";
    public static final String SCHEMA_LOCATION_V1_1_LIST_RECORDS = "http://www.openarchives.org/OAI/1.1/OAI_ListRecords http://www.openarchives.org/OAI/1.1/OAI_ListRecords.xsd";
    public static final String SCHEMA_LOCATION_V1_1_LIST_SETS = "http://www.openarchives.org/OAI/1.1/OAI_ListSets http://www.openarchives.org/OAI/1.1/OAI_ListSets.xsd";
    private Document doc = null;
    private String schemaLocation = null;
    private String requestURL = null;
    private static HashMap builderMap = new HashMap();
    private static Element namespaceElement = null;
    private static DocumentBuilderFactory factory = null;
    private static TransformerFactory xformFactory = TransformerFactory.newInstance();
    
    //Nuno Freire: the original bytes of the response are kept here. 
    //They may be usefull in case of error recovery.
    private byte[] responseBytes = null;
    File saveNextResponseIn=null;

    
    static {
    	try {
	        /* Load DOM Document */
	        factory = DocumentBuilderFactory
	        .newInstance();
	        factory.setNamespaceAware(true);
	        Thread t = Thread.currentThread();
	        DocumentBuilder builder = factory.newDocumentBuilder();
	        builderMap.put(t, builder);
	        
	        DOMImplementation impl = builder.getDOMImplementation();
	        Document namespaceHolder = impl.createDocument(
	                "http://www.oclc.org/research/software/oai/harvester",
	                "harvester:namespaceHolder", null);
	        namespaceElement = namespaceHolder.getDocumentElement();
	        namespaceElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
	                "xmlns:harvester",
	        "http://www.oclc.org/research/software/oai/harvester");
	        namespaceElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
	                "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
	        namespaceElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
	                "xmlns:oai20", "http://www.openarchives.org/OAI/2.0/");
	        namespaceElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
	                "xmlns:oai11_GetRecord",
	        "http://www.openarchives.org/OAI/1.1/OAI_GetRecord");
	        namespaceElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
	                "xmlns:oai11_Identify",
	        "http://www.openarchives.org/OAI/1.1/OAI_Identify");
	        namespaceElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
	                "xmlns:oai11_ListIdentifiers",
	        "http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers");
	        namespaceElement
	        .setAttributeNS("http://www.w3.org/2000/xmlns/",
	                "xmlns:oai11_ListMetadataFormats",
	        "http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats");
	        namespaceElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
	                "xmlns:oai11_ListRecords",
	        "http://www.openarchives.org/OAI/1.1/OAI_ListRecords");
	        namespaceElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
	                "xmlns:oai11_ListSets",
	        "http://www.openarchives.org/OAI/1.1/OAI_ListSets");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    /**
     * Get the OAI response as a DOM object
     * 
     * @return the DOM for the OAI response
     */
    public Document getDocument() {
        return doc;
    }
    
    /**
     * Get the xsi:schemaLocation for the OAI response
     * 
     * @return the xsi:schemaLocation value
     */
    public String getSchemaLocation() {
        return schemaLocation;
    }
    
    /**
     * Get the OAI errors
     * @return a NodeList of /oai:OAI-PMH/oai:error elements
     * @throws TransformerException
     */
    public NodeList getErrors() throws TransformerException {
        NodeList tkStr = getNodeList("//OAI-PMH/error");
        if (tkStr==null || tkStr.getLength()==0) 
            tkStr = getNodeList("/oai20:OAI-PMH/oai20:error");
        return tkStr;
    }
    
    /**
     * Get the OAI request URL for this response
     * @return the OAI request URL as a String
     */
    public String getRequestURL() {
        return requestURL;
    }
    
    /**
     * Mock object creator (for unit testing purposes)
     */
    public HarvesterVerb() {
    }
    
    /**
     * Performs the OAI request
     * 
     * @param requestURL
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws TransformerException
     */
    public HarvesterVerb(String requestURL, File saveNextResponseIn) throws
    ParserConfigurationException, SAXException, TransformerException, IOException {
        this.saveNextResponseIn = saveNextResponseIn;
        harvest(requestURL);
    }
    /**
     * Performs the OAI request
     * 
     * @param requestURL
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws TransformerException
     */
    public HarvesterVerb(String requestURL) throws
    ParserConfigurationException, SAXException, TransformerException, IOException {
        harvest(requestURL);
    }
    
    /**
     * Preforms the OAI request
     * 
     * @param requestURL
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws TransformerException
     */
    public void harvest(String requestURL) throws IOException,
    ParserConfigurationException, SAXException, TransformerException {
        this.requestURL = requestURL;
        logger.debug("requestURL=" + requestURL);
        InputStream in = null;
        URL url = new URL(requestURL);
        HttpURLConnection con = null;
        int responseCode = 0;
        do {
            con = (HttpURLConnection) url.openConnection();
            // FIXME: changing timeouts
            con.setConnectTimeout(480000);
            con.setReadTimeout(3600000);
//            con.setConnectTimeout(60000);
//            con.setReadTimeout(240000);
            con.setRequestProperty("User-Agent", "OAIHarvester/2.0");
            con.setRequestProperty("Accept-Encoding",
            "compress, gzip, identify");
            try {
                responseCode = con.getResponseCode();
                logger.debug("responseCode=" + responseCode);
            } catch (FileNotFoundException e) {
                // assume it's a 503 response
                logger.info(requestURL, e);
                responseCode = HttpURLConnection.HTTP_UNAVAILABLE;
            }
            
            if (responseCode == HttpURLConnection.HTTP_UNAVAILABLE) {
                long retrySeconds = con.getHeaderFieldInt("Retry-After", -1);
                if (retrySeconds == -1) {
                    long now = (new Date()).getTime();
                    long retryDate = con.getHeaderFieldDate("Retry-After", now);
                    retrySeconds = retryDate - now;
                }
                if (retrySeconds == 0) { // Apparently, it's a bad URL
                    throw new FileNotFoundException("Bad URL?");
                }
                System.err.println("Server response: "+responseCode+ " Retry-After="
                        + retrySeconds);
                

                in = con.getInputStream();
        		String respBody=IOUtils.toString(in);
        		System.err.println(respBody);
                if(saveNextResponseIn!=null) 
                        FileUtils.writeByteArrayToFile(saveNextResponseIn, responseBytes);
        		InputSource data;
        		data = new InputSource(new ByteArrayInputStream(responseBytes));
                
                
                if (retrySeconds > 0) {
                    try {
                        Thread.sleep(retrySeconds * 1000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } while (responseCode == HttpURLConnection.HTTP_UNAVAILABLE);
        String contentEncoding = con.getHeaderField("Content-Encoding");
        logger.debug("contentEncoding=" + contentEncoding);
        if ("compress".equals(contentEncoding)) {
            ZipInputStream zis = new ZipInputStream(con.getInputStream());
            zis.getNextEntry();
            in = zis;
        } else if ("gzip".equals(contentEncoding)) {
            in = new GZIPInputStream(con.getInputStream());
        } else if ("deflate".equals(contentEncoding)) {
            in = new InflaterInputStream(con.getInputStream());
        } else {
            in = con.getInputStream();
        }
        
		responseBytes=IOUtils.toByteArray(in);
        if(saveNextResponseIn!=null) 
                FileUtils.writeByteArrayToFile(saveNextResponseIn, responseBytes);
		InputSource data;
		data = new InputSource(new ByteArrayInputStream(responseBytes));
        
        Thread t = Thread.currentThread();
        DocumentBuilder builder = (DocumentBuilder) builderMap.get(t);
        if (builder == null) {
            builder = factory.newDocumentBuilder();
            builderMap.put(t, builder);
        }
//        doc = builder.parse(data);
		try {
			doc = builder.parse(data);
		} catch (SAXException firstSaxException) {
			try {
				//Here we can try to recover the xml from known typical problems

				//Recover from invalid characters
				//we assume this is UTF-8...
				String xmlString=new String(responseBytes, "UTF-8");
				xmlString=removeInvalidXMLCharacters(xmlString);
				
				int atempts=0;
				SAXException lastSaxException=firstSaxException;
				while(lastSaxException!=null && atempts<100) {
		        	Pattern invalidCharPattern=Pattern.compile("Character reference \"&#x?([abcdef0-9]{1,8});?\"", Pattern.CASE_INSENSITIVE);
		        	Matcher m=invalidCharPattern.matcher(lastSaxException.getMessage());
		        	if(m.find()) {
			        	//Error on line 1203 of document file:///data/repox/[temp]OAI-PMH_Requests/oai.driver.research-infrastructures.eu-ALL/recordsRequest-1130.xml : Character reference "&#dbc0" is an invalid XML character. Nested exception: 
			        	//Character reference "&#dbc0" is an invalid XML character.
			        	String charPattern = "\\&\\#"+m.group(1)+"\\;";
						Matcher replaceCharMatcher=Pattern.compile(charPattern).matcher(xmlString);
						if(replaceCharMatcher.find())
							xmlString=replaceCharMatcher.replaceAll(" ");
						else {
							charPattern = "\\&\\#"+Integer.parseInt( m.group(1), 16)+"\\;";
							xmlString=xmlString.replaceAll(charPattern, " ");
						}
			        	atempts++;
		        	}else {
		        		//other kind of error throw the exception
		        		throw firstSaxException;
		        	}

		        	lastSaxException=null;
		        	try {
						data = new InputSource(new ByteArrayInputStream(xmlString.getBytes("UTF-8")));
						doc = builder.parse(data);
					}catch (SAXException reocurringSaxException) {
						lastSaxException=reocurringSaxException;
					}
				}
			} catch (Exception e2) {
				//the recovered version did not work either. Throw the original exception
				throw firstSaxException;
			}
		}
		
        StringTokenizer tokenizer = new StringTokenizer(
                getSingleString("/*/@xsi:schemaLocation"), " ");
        StringBuffer sb = new StringBuffer();
        while (tokenizer.hasMoreTokens()) {
            if (sb.length() > 0)
                sb.append(" ");
            sb.append(tokenizer.nextToken());
        }
        this.schemaLocation = sb.toString();
    }
    
    
    
    /**
     * Get the String value for the given XPath location in the response DOM
     * 
     * @param xpath
     * @return a String containing the value of the XPath location.
     * @throws TransformerException
     */
    public String getSingleString(String xpath) throws TransformerException {
        return getSingleString(getDocument(), xpath);
    }
    

    /**
     * @param node
     * @param xpath
     * @return
     * @throws TransformerException
     */
    public String getSingleString(Node node, String xpath)
    throws TransformerException {
        return XPathAPI.eval(node, xpath, namespaceElement).str();
    }
    
    /**
     * Get a NodeList containing the nodes in the response DOM for the specified
     * xpath
     * @param xpath
     * @return the NodeList for the xpath into the response DOM
     * @throws TransformerException
     */
    public NodeList getNodeList(String xpath) throws TransformerException {
        return XPathAPI.selectNodeList(getDocument(), xpath, namespaceElement);
    }
    
    public String toString() {
        // Element docEl = getDocument().getDocumentElement();
        // return docEl.toString();
        Source input = new DOMSource(getDocument());
        StringWriter sw = new StringWriter();
        Result output = new StreamResult(sw);
        try {
        	Transformer idTransformer = xformFactory.newTransformer();
            idTransformer.setOutputProperty(
                    OutputKeys.OMIT_XML_DECLARATION, "yes");
            idTransformer.transform(input, output);
            return sw.toString();
        } catch (TransformerException e) {
            return e.getMessage();
        }
    }
    
    /**
     * This method ensures that the output String has only valid XML unicode characters as specified by the
     * XML 1.0 standard. For reference, please see the
     * standard. This method will return an empty String if the input is null or empty.
     *
     * @author Nuno Freire
     * @param  s - The String whose non-valid characters we want to replace.
     * @return The in String, where non-valid characters are replace by spaces.
     */
    private static String removeInvalidXMLCharacters(String s) {

        StringBuilder out = new StringBuilder();                // Used to hold the output.
    	int codePoint;                                          // Used to reference the current character.
		int i=0;
    	while(i<s.length()) {
    		codePoint = s.codePointAt(i);                       // This is the unicode code of the character.
			if ((codePoint == 0x9) ||          				    // Consider testing larger ranges first to improve speed. 
					(codePoint == 0xA) ||
					(codePoint == 0xD) ||
					((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||
					((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) ||
					((codePoint >= 0x10000) && (codePoint <= 0x10FFFF))) {
				out.append(Character.toChars(codePoint));
			}else {
				out.append(' ');				
			}
			i+= Character.charCount(codePoint);                 // Increment with the number of code units(java chars) needed to represent a Unicode char.  
    	}
    	return out.toString();
    }

    /**
     * Returns the responseBytes.
     * @return the responseBytes
     */
    public byte[] getResponseBytes() {
        return responseBytes;
    } 
}
