package inescid.util.europeana;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import inescid.dataaggregation.data.RdfReg;
import inescid.dataaggregation.data.RegEdm;
import inescid.dataaggregation.data.RegRdf;

public class EdmRdfUtil {
	

	public static Resource getEuropeanaAggregationResource(Model cho) {
		ResIterator aggregations = cho.listResourcesWithProperty(RegRdf.type, RegEdm.EuropeanaAggregation);
		if (aggregations.hasNext()) {
			return aggregations.next();
		} else
			return null;
	}

	public static Resource getAggregationResource(Model cho) {
		ResIterator aggregations = cho.listResourcesWithProperty(RegRdf.type, RdfReg.ORE_AGGREGATION);
		if (aggregations.hasNext()) {
			return aggregations.next();
		} else
			return null;
	}
	
	public static Resource getProvidedChoResource(Model cho) {
		ResIterator aggregations = cho.listResourcesWithProperty(RegRdf.type, RegEdm.ProvidedCHO);
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
