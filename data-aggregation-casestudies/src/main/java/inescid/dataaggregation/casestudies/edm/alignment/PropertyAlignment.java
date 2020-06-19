package inescid.dataaggregation.casestudies.edm.alignment;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class PropertyAlignment implements Serializable, Cloneable {
	
	String propertyUri;
	
//	Set<String> equivalences;
//	Set<String> broaderThan;
	Set<String> alignedWith; // properties' URIs
	
	ValueType allowedValue;
	
	int maxCardinality;
//	int minCardinality;
	
	Set<String> targetClasses; // properties' URIs
	
//	boolean isLeaf=true; //not needed, in these cases Resource should not be an allowed value

	public PropertyAlignment(Property property, ValueType allowedValue) {
		super();
		this.propertyUri = property.getURI();
		this.allowedValue = allowedValue;
	}
	public PropertyAlignment(String propertyUri, ValueType allowedValue) {
		super();
		this.propertyUri = propertyUri;
		this.allowedValue = allowedValue;
	}

	public ValueType getAllowedValue() {
		return allowedValue;
	}

	public int getMaxCardinality() {
		return maxCardinality;
	}

	public String getPropertyUri() {
		return propertyUri;
	}

	public void addAlignment(Property prop) {
		addAlignment(prop.getURI());
	}
	public void addAlignment(String propUri) {
		alignedWith.add(propUri);
	}
	public void addRangeClass(Resource... classes) {
		if(targetClasses==null)
			targetClasses=new HashSet<String>(1);
		for(Resource r: classes)
			targetClasses.add(r.getURI());
	}
	public PropertyAlignment copyTo(ValueType valueType) {
		try {
			PropertyAlignment copy=(PropertyAlignment) clone();
			copy.allowedValue=valueType;
			return copy;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
