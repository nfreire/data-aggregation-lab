package inescid.dataaggregation.dataset.convert.rdfconverter;

import org.apache.jena.rdf.model.Property;

public class PropertyMappingSpecification {
	Property property;
	boolean mapToValueAlways=false;
	public PropertyMappingSpecification(Property property) {
		super();
		this.property = property;
	}
	public PropertyMappingSpecification(Property property, boolean mapToValueAlways) {
		super();
		this.property = property;
		this.mapToValueAlways = mapToValueAlways;
	}
	public Property getProperty() {
		return property;
	}
	public boolean isMapToValueAlways() {
		return mapToValueAlways;
	}
	public void setMapToValueAlways(boolean mapToValueAlways) {
		this.mapToValueAlways = mapToValueAlways;
	}
	public void setProperty(Property property) {
		this.property = property;
	}
	
	
}
