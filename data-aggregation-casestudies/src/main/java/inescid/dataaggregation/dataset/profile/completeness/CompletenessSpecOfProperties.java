package inescid.dataaggregation.dataset.profile.completeness;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.jena.rdf.model.Property;

public class CompletenessSpecOfProperties {
	public Set<Property> properties;
	public Map<Property, Set<Property>> oneOfProperties;
	protected Set<Set<Property>> oneOfPropertiesGroups;
	
	public CompletenessSpecOfProperties() {
		properties=new  HashSet<>();
		oneOfProperties=new  HashMap<>();
		oneOfPropertiesGroups=new  HashSet<>();
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
		oneOfPropertiesGroups.add(group);
	}

	public boolean usesProperty(Property predicate) {
		return properties.contains(predicate) ||  oneOfProperties.containsKey(predicate);
	}

	public Set<Set<Property>> groupsOfProperties() {
		return oneOfPropertiesGroups;
	}

	public void copyFrom(CompletenessSpecOfProperties maxScoreChoSpec) {
//		public Set<Property> properties;
//		public Map<Property, Set<Property>> oneOfProperties;
//		protected Set<Set<Property>> oneOfPropertiesGroups;
		properties.addAll(maxScoreChoSpec.properties);
		for(Entry<Property, Set<Property>> entry : maxScoreChoSpec.oneOfProperties.entrySet()) {
			HashSet<Property> mySet=new HashSet<>(entry.getValue());
			oneOfProperties.put(entry.getKey(), mySet);
		}
		for(Set<Property> grp : maxScoreChoSpec.oneOfPropertiesGroups) {
			HashSet<Property> mySet=new HashSet<>(grp);
			oneOfPropertiesGroups.add(mySet);
		}
	}
}
