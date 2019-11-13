package inescid.util.europeana;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import inescid.util.XPathUtil;
import inescid.util.XmlNsUtil;
import inescid.util.XmlUtil;

public class EdmXmlUtil {
	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EdmXmlUtil.class);
	
	
	public static String getIssuedDate(Document edm) {
		try {
			NodeList types = XPathUtil.queryDom(XmlNsUtil.xpathEdmPrefixMap, "//edm:ProvidedCHO/dcterms:issued", edm);
			if (types.getLength()>0) 
				return XmlUtil.getElementText(((Element)types.item(0))).trim();
			return null;
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
    public static List<Element> getPropertyElementsOfCho(Document edm, String namespace, String elementName) {
		try {
			NodeList subNodes = XPathUtil.queryDom(XmlNsUtil.xpathEdmPrefixMap, "//edm:ProvidedCHO/"+XmlNsUtil.getPreffix(namespace)+":"+elementName, edm);
	        int sz = subNodes.getLength();
	        ArrayList<Element> elements = new ArrayList<Element>(sz);
	        for (int idx = 0; idx < sz; idx++) {
	            Node node = subNodes.item(idx);
	            elements.add((Element)node);
	        }
	        return elements;
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
    }
    public static Element getPropertyElementOfCho(Document edm, String namespace, String elementName) {
    	try {
    		NodeList subNodes = XPathUtil.queryDom(XmlNsUtil.xpathEdmPrefixMap, "//edm:ProvidedCHO/"+XmlNsUtil.getPreffix(namespace)+":"+elementName, edm);
    		int sz = subNodes.getLength();
    		for (int idx = 0; idx < sz; idx++) {
    			Node node = subNodes.item(idx);
    			return (Element)node;
    		}
    		return null;
    	} catch (XPathExpressionException e) {
    		throw new RuntimeException(e.getMessage(), e);
    	}
    }
	
    public static Iterable<Element> getPropertyElementsOfAggregation(Document edm, String namespace, String elementName) {
    	try {
    		NodeList subNodes = XPathUtil.queryDom(XmlNsUtil.xpathEdmPrefixMap, "//ore:Aggregation/"+XmlNsUtil.getPreffix(namespace)+":"+elementName, edm);
    		int sz = subNodes.getLength();
    		ArrayList<Element> elements = new ArrayList<Element>(sz);
    		for (int idx = 0; idx < sz; idx++) {
    			Node node = subNodes.item(idx);
    			elements.add((Element)node);
    		}
    		return elements;
    	} catch (XPathExpressionException e) {
    		throw new RuntimeException(e.getMessage(), e);
    	}
    }
    public static Element getPropertyElementOfAggregation(Document edm, String namespace, String elementName) {
    	try {
    		NodeList subNodes = XPathUtil.queryDom(XmlNsUtil.xpathEdmPrefixMap, "//ore:Aggregation/"+XmlNsUtil.getPreffix(namespace)+":"+elementName, edm);
    		int sz = subNodes.getLength();
    		for (int idx = 0; idx < sz; idx++) {
    			Node node = subNodes.item(idx);
    			return (Element)node;
    		}
    		return null;
    	} catch (XPathExpressionException e) {
    		throw new RuntimeException(e.getMessage(), e);
    	}
    }
    
	
	
	public static String getEdmRights(Document edm) {
		Element rights;
		try {
			rights = XPathUtil.queryDomForElement(XmlNsUtil.xpathEdmPrefixMap, "//ore:Aggregation/edm:rights", edm);
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		String rightsTxt=null;
		if (rights!=null) { 
			rightsTxt=rights.getAttributeNS(XmlNsUtil.RDF, "resource");
			if(StringUtils.isEmpty(rightsTxt))
				rightsTxt=XmlUtil.getElementText(rights);
			if(StringUtils.isEmpty(rightsTxt))
				rightsTxt=rights.getAttribute("resource");
		}
		return rightsTxt;
	}

	public static Element getEdmRightsElement(Document edm) {
		try {
			NodeList hits = XPathUtil.queryDom(XmlNsUtil.xpathEdmPrefixMap, "//ore:Aggregation/edm:rights", edm);
			if (hits.getLength()>0) 
				return (Element)hits.item(0);
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return null;
	}

	public static Element getAggregationElement(Document edm) {
		try {
			NodeList hits = XPathUtil.queryDom(XmlNsUtil.xpathEdmPrefixMap, "//ore:Aggregation", edm);
			if (hits.getLength()>0) 
				return (Element)hits.item(0);
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return null;
	}
	
	public static Element getProvidedChoElement(Document edm) {
		try {
			NodeList hits = XPathUtil.queryDom(XmlNsUtil.xpathEdmPrefixMap, "//edm:ProvidedCHO", edm);
			if (hits.getLength()>0) 
				return (Element)hits.item(0);
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return null;
	}
	
	public static List<Integer> getIssuedDates(Document edmDom) {
		String issuedDateStr = getIssuedDate(edmDom);
		List<Integer> ret=new ArrayList<>(2);
		if(issuedDateStr!=null) {
			if(issuedDateStr.length()==10) {//yyyy-MM-dd
				try {
					ret.add(Integer.parseInt(issuedDateStr.substring(0, 4)));
				} catch (NumberFormatException e) {
					log.info(issuedDateStr,e);
				}
			} else if(issuedDateStr.length()==22) {//yyyy-MM-dd - yyyy-MM-dd
				try {
					ret.add(Integer.parseInt(issuedDateStr.substring(0, 4)));
					ret.add(Integer.parseInt(issuedDateStr.substring(13, 17)));
				} catch (NumberFormatException e) {
					log.info(issuedDateStr,e);
				}
				
			}
		}
		return ret;
	}

	public static List<Element> getWebResources(Document edm) {
		List<Element> ret=new ArrayList<>(3);
		try {
			NodeList hits = XPathUtil.queryDom(XmlNsUtil.xpathEdmPrefixMap, "//edm:WebResource", edm);
			for (int idx = 0; idx < hits.getLength(); idx++) {
				Node node = hits.item(idx);
				ret.add((Element)node);
			}
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return ret;
	}

	public static void checkAndSetEdmNamespacePreffixes(Document edmDom) {
		Element el = edmDom.getDocumentElement();
		for (Entry<String, String> nsEntry: XmlNsUtil.xpathEdmPrefixMap.entrySet()) {
			String nsPrefDecl = el.getAttribute("xmlns:"+nsEntry.getKey());
			if(StringUtils.isEmpty(nsPrefDecl)) 
				el.setAttribute("xmlns:"+nsEntry.getKey(), nsEntry.getValue());
		}
	}

	public static boolean isIiifAtEuropeana(Document issueEdmDom) {
		Element edmObject =EdmXmlUtil.getPropertyElementOfAggregation(issueEdmDom, XmlNsUtil.EDM, "object");
		if (edmObject==null)
			return false;
		String firstIssueWebResUri = edmObject.getAttributeNS(XmlNsUtil.RDF, "resource");
		return firstIssueWebResUri!=null && firstIssueWebResUri.startsWith("https://iiif.europeana.eu");
	}

	public static void removeIsShownBy(Document edmDom) {
		Element isShownBy =EdmXmlUtil.getPropertyElementOfAggregation(edmDom, XmlNsUtil.EDM, "isShownBy");
		if (isShownBy!=null) {
			XmlUtil.removeResource(edmDom, isShownBy.getAttributeNS(XmlNsUtil.RDF, "resource"));
			isShownBy.getParentNode().removeChild(isShownBy);
		}		
	}
	
	public static void removeIsShownAt(Document edmDom) {
		Element isShownAt =EdmXmlUtil.getPropertyElementOfAggregation(edmDom, XmlNsUtil.EDM, "isShownAt");
		if (isShownAt!=null) {
			XmlUtil.removeResource(edmDom, isShownAt.getAttributeNS(XmlNsUtil.RDF, "resource"));
			isShownAt.getParentNode().removeChild(isShownAt);
		}		
	}
}
