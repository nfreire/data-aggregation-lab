package inescid.dataaggregation.dataset.convert.rdfconverter;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Property;

public class DerivedPropertyConversionSpecification {

	PropertyMappingSpecification derivedProperty;
	Map<String, String> literalMapping=new HashMap<>();
	Map<String, String> uriMapping=new HashMap<>();
	
	public DerivedPropertyConversionSpecification(Property derivedProperty) {
		super();
		this.derivedProperty = new PropertyMappingSpecification(derivedProperty);
	}

	public void putLiteralMapping(String from, String to) {
		literalMapping.put(from, to);
	}
	
	public void putUriMapping(String from, String to) {
		uriMapping.put(from, to);
	}

	public PropertyMappingSpecification getDerivedProperty() {
		return derivedProperty;
	}

	public Map<String, String> getLiteralMapping() {
		return literalMapping;
	}

	public Map<String, String> getUriMapping() {
		return uriMapping;
	}
	public String getUriMapping(String uri) {
		return uriMapping.get(uri);
	}
	public String getLiteralMapping(String literal) {
		return uriMapping.get(literal);
	}
}
