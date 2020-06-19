package inescid.dataaggregation.casestudies.edm.alignment;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Property;

public class ClassAlignment {
	
	String classUri;
	
	Map<String,PropertyAlignment> properties;
	
//	Set<String> equivalences;
//	Set<String> broaderThan;
	Set<String> alignedWith; // classes' URIs

	public String getClassUri() {
		return classUri;
	}

	public PropertyAlignment getPropertyAlignment(Property prop) {
		return getPropertyAlignment(prop.getURI());
	}
	
	public PropertyAlignment getPropertyAlignment(String extPropUri) {
		return properties.get(extPropUri);
	}

	public Set<String> getAlignedWith() {
		return alignedWith;
	}
	
	public void addAlignedWith(String... clsUris) {
		if(alignedWith==null)
			alignedWith=new HashSet<String>();
		for (String uri: clsUris)
			alignedWith.add(uri);
	}
	

	public PropertyAlignment addProperty(Property prop, ValueType value) {
		return addProperty(prop.getURI(), value);
	}

	public PropertyAlignment addProperty(String propUri, ValueType value) {
		PropertyAlignment newAlig = new PropertyAlignment(propUri, value);
		properties.put(propUri, newAlig);
		return newAlig;
	}

	public void addProperty(PropertyAlignment prop) {
		properties.put(prop.getPropertyUri(), prop);
	}

	
	
}
