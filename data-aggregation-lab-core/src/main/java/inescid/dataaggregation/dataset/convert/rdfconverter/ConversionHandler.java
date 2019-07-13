package inescid.dataaggregation.dataset.convert.rdfconverter;

import java.util.Set;

import org.apache.jena.rdf.model.Resource;

public interface ConversionHandler {

	public Set<String> propertiesProcessed();
	
	public void handleConvertedResult(Resource source, Resource target);
}
