/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package inescid.dataaggregation.data.validation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.common.xmlschema.LSInputImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import inescid.dataaggregation.dataset.Global;

/**
 * EDM Validator class
 */
public class EdmXmlValidator {
	public class SchemaValidationErrorHandler implements ErrorHandler {
		public List<String> messages=new ArrayList<>();
		@Override
		public void warning(SAXParseException exception) throws SAXException {
			
		}
		
		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			
		}
		
		@Override
		public void error(SAXParseException exception) throws SAXException {
			
		}
	};
	
	public enum Schema {EDM, EDM_INTERNAL};
	
    private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(EdmXmlValidator.class);

    protected File resourceFolder;
    protected Schema schema=Schema.EDM;
    protected String schematronXsl=null;
    
    public EdmXmlValidator(File resourceFolder, Schema schema) {
    	this.resourceFolder = resourceFolder;
    	this.schema=schema;
    	try {
			this.schematronXsl=FileUtils.readFileToString(new File(resourceFolder, "schematron/schematron.xsl"), "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
    }

    /**
     * Validate method using JAXP
     *
     * @return The outcome of the Validation
     */
    public ValidationResult validate(String uri, Document doc) {
        SchemaValidationErrorHandler errors=new SchemaValidationErrorHandler();
        try {
//        	XmlUtil.validateXmlOnSchema(new File(resourceFolder, "edmschema/EDM.xsd"), new DOMSource(doc));
        	switch (schema) {
			case EDM:
				getEdmSchemaValidator(new File(resourceFolder, "edmschema/EDM.xsd"), errors).validate(new DOMSource(doc));				
				break;
			case EDM_INTERNAL:
				javax.xml.validation.Validator edmSchemaValidator = getEdmSchemaValidator(new File(resourceFolder, "edmschema/EDM-INTERNAL.xsd"), errors);
				edmSchemaValidator.validate(new DOMSource(doc));				
				break;
			default:
				throw new RuntimeException("Not implemented "+schema);
			}
           
            StringReader reader = null;
            reader = new StringReader(schematronXsl);
            DOMResult result = new DOMResult();
            Transformer transformer = TransformerFactory.newInstance().newTemplates(new StreamSource(reader)).newTransformer();
            transformer.transform(new DOMSource(doc), result);

            
            
            NodeList nresults = result.getNode().getFirstChild().getChildNodes();
            for (int i = 0; i < nresults.getLength(); i++) {
                Node nresult = nresults.item(i);
                if ("failed-assert".equals(nresult.getLocalName())) {
//                	NodeList detailNodes = nresult.getChildNodes();
//                	for (int j = 0; j < detailNodes.getLength(); j++) {
//                		Node detailNode = nresults.item(j);
//                		System.out.println(detailNode.getLocalName());
//                	}
//                	String[] lines = nresult.getTextContent().split("\\s*[\\n\\r]+\\s*");
//                	if(lines.length>3) {
//	                	String msg=lines[3];
//	                	for(int k=4 ; k<lines.length ; k++) {
//	                		msg+=" "+lines[k];
//	                	}
//	                   errors.messages.add(msg);
//                	} else 
                		errors.messages.add(nresult.getAttributes().getNamedItem("test").getTextContent());
//                		errors.messages.add(XmlUtil.writeDomToString((Document) result.getNode()));
//                		errors.messages.add(nresult.getTextContent());
                		
                }
            }
        } catch (Exception e) {
        	errors.messages.add(e.getMessage());
            log.info(e.getMessage(), e);
        }
        if(errors.messages.isEmpty())
        	return new ValidationResult();
        return constructValidationError(uri, errors.messages);
    }

    protected ValidationResult constructValidationError(String recordId, List<String> messages) {
        ValidationResult res = new ValidationResult();
        res.addMessages(messages);
        res.setRecordId(recordId);
        if (StringUtils.isEmpty(res.getRecordId())) {
            res.setRecordId("Missing record identifier for EDM record");
        }
        return res;
    }

    
    public javax.xml.validation.Validator getEdmSchemaValidator(final File xsdFile, SchemaValidationErrorHandler errors) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setFeature("http://apache.org/xml/features/validation/schema-full-checking", false);
            factory.setFeature("http://apache.org/xml/features/honour-all-schemaLocations", true);
//            factory.setErrorHandler(errors);
            factory.setResourceResolver(new LSResourceResolver() {
            	File folder=xsdFile.getParentFile();
				@Override
				public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
					try {
						LSInputImpl input = new LSInputImpl();
						 
						InputStream stream = new FileInputStream(new File(folder, systemId));
 
						input.setPublicId(publicId);
						input.setSystemId(systemId);
						input.setBaseURI(baseURI);
						input.setCharacterStream(new InputStreamReader(stream, Global.UTF8));
						
						return input;
					} catch (FileNotFoundException e) {
						throw new RuntimeException(e.getMessage(), e);
					}
				}
			});
            javax.xml.validation.Validator newValidator = factory.newSchema(new StreamSource(new FileInputStream(xsdFile))).newValidator();
            newValidator.setErrorHandler(errors);
			return newValidator;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
}


