package inescid.dataaggregation.dataset.convert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.jena.rdf.model.Resource;

import inescid.util.datastruct.MapOfMaps;

public class ClassMappings {

	Map<String, String> classMapping=new HashMap<>();
	MapOfMaps<String, String, String> propertyMappingsByClass=new MapOfMaps<>();
	
	public void put(String edmClsUri, String wdProp, String edmProp) {
		propertyMappingsByClass.put(edmClsUri, wdProp, edmProp);
	}
	public int size() {
		return propertyMappingsByClass.sizeTotal();
	}
	public Set<String> getAllPropertiesMapped() {
		return propertyMappingsByClass.key2ndSet();
	}
	public String get(String edmClsUri, String wdPropUri) {
		return propertyMappingsByClass.get(edmClsUri, wdPropUri);
	}
	

	public void putClassMapping(String sourceCls, String targetCls) {
		classMapping.put(sourceCls, targetCls);
	}
	public Map<String, String> GetClassMappping() {
		return classMapping;
	}
	
	public Map<String, String> getAllPropertiesMappedFor(String edmClsUri) {
		return propertyMappingsByClass.get(edmClsUri);
	}
	public List<String> getClassesMappedTo(String edmClass) {
		List<String> ret=new ArrayList<String>();
		for(Entry<String, String> mapping: classMapping.entrySet()) {
			if(mapping.getValue().equals(edmClass))
				ret.add(mapping.getKey());
		}
		return ret;
	}
}
