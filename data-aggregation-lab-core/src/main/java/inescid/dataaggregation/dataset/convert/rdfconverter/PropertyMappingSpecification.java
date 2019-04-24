package inescid.dataaggregation.dataset.convert.rdfconverter;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class PropertyMappingSpecification {
	Property property;
	boolean mapToValueAlways=false;
	Resource mapToResource=null;
	
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
	public Resource getMapToResource() {
		return mapToResource;
	}
	public void setMapToResource(Resource mapToResource) {
		this.mapToResource = mapToResource;
	}
	
	
}
