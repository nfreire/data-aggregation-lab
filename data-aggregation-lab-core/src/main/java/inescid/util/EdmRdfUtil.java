package inescid.util;

import javax.xml.xpath.XPathExpressionException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import inescid.dataaggregation.dataset.convert.RdfReg;

public class EdmRdfUtil {
	


	public static Resource getAggregationResource(Model cho) {
		ResIterator aggregations = cho.listResourcesWithProperty(RdfReg.RDF_TYPE, RdfReg.ORE_AGGREGATION);
		if (aggregations.hasNext()) {
			return aggregations.next();
		} else
			return null;
	}
	
	public static Resource getProvidedChoResource(Model cho) {
		ResIterator aggregations = cho.listResourcesWithProperty(RdfReg.RDF_TYPE, RdfReg.EDM_PROVIDED_CHO);
		if (aggregations.hasNext()) {
			return aggregations.next();
		} else
			return null;
	}
	
	
    public static RDFNode getPropertyOfAggregation(Model cho, Property prop) {
    	Resource agg = getAggregationResource(cho);
    	if (agg!=null) {
    		 Statement propStm = agg.getProperty(prop);
    		 if(propStm!=null)
    			return propStm.getObject(); 
    	}
    	return null;
    }
    
    
    public static RDFNode getPropertyOfProvidedCho(Model cho, Property prop) {
    	Resource agg = getProvidedChoResource(cho);
    	if (agg!=null) {
    		Statement propStm = agg.getProperty(prop);
    		if(propStm!=null)
    			return propStm.getObject(); 
    	}
    	return null;
    }
//	ResIterator aggregations = cho.getModel().listResourcesWithProperty(RdfReg.RDF_TYPE, RdfReg.ORE_AGGREGATION);
//	while(aggregations.hasNext()) {
//		Element resourceToXml = resourceToXml(aggregations.next());
//		if(resourceToXml!=null)
//			rootEl.appendChild(resourceToXml);
//	}		
}
