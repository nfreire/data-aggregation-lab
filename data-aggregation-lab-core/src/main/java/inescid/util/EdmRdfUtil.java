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

import inescid.dataaggregation.data.RdfReg;
import inescid.dataaggregation.data.RdfRegEdm;
import inescid.dataaggregation.data.RdfRegRdf;

public class EdmRdfUtil {
	

	public static Resource getEuropeanaAggregationResource(Model cho) {
		ResIterator aggregations = cho.listResourcesWithProperty(RdfRegRdf.type, RdfRegEdm.EuropeanaAggregation);
		if (aggregations.hasNext()) {
			return aggregations.next();
		} else
			return null;
	}

	public static Resource getAggregationResource(Model cho) {
		ResIterator aggregations = cho.listResourcesWithProperty(RdfRegRdf.type, RdfReg.ORE_AGGREGATION);
		if (aggregations.hasNext()) {
			return aggregations.next();
		} else
			return null;
	}
	
	public static Resource getProvidedChoResource(Model cho) {
		ResIterator aggregations = cho.listResourcesWithProperty(RdfRegRdf.type, RdfRegEdm.ProvidedCHO);
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
//	ResIterator aggregations = cho.getModel().listResourcesWithProperty(RdfRegRdf.type, RdfReg.ORE_AGGREGATION);
//	while(aggregations.hasNext()) {
//		Element resourceToXml = resourceToXml(aggregations.next());
//		if(resourceToXml!=null)
//			rootEl.appendChild(resourceToXml);
//	}		
}
