package inescid.dataaggregation.dataset.profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Property;

public class CompletenessSpecProperties {
	public List<Property> properties;
	public Map<Property, Set<Property>> oneOfProperties;
	
	public CompletenessSpecProperties() {
		properties=new  ArrayList<>();
		oneOfProperties=new  HashMap<>();
	}

	public void addProperty(Property prop) {
		properties.add(prop);
	}

	public void addOneOfProperties(Property... props) {
		HashSet<Property> group=new HashSet<>();
		for(Property p: props) {
//			properties.add(p);
			group.add(p);
			oneOfProperties.put(p, group);
		}
	}
}
