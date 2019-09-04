package inescid.dataaggregation.dataset.convert.rdfconverter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Resource;

import inescid.dataaggregation.dataset.convert.FilterOfReferencedResource;
import inescid.util.RdfUtil.Jena;

public class RdfConversionSpecification {
	ConversionHandler conversionHandler;
	
	Map<Resource, Resource> typesMapping=new HashMap<>();

	Map<Resource, Resource[]> rootResourcesTypeMapping=new HashMap<>();
	
	Map<Resource, ResourceTypeConversionSpecification> typePropertiesMappings=new HashMap<>();

	FilterOfReferencedResource filterOfReferencedResource;
	
//	public Map<Resource, Resource> getRootResourcesTypeMapping() {
//		return rootResourcesTypeMapping;
//	}

	public Set<Resource> getRootResourceTypes() {
		return rootResourcesTypeMapping.keySet();
	}
	
	public Resource[] getRootResourceTypeMapping(Resource rdfType) {
		Resource[] map = rootResourcesTypeMapping.get(rdfType);
		return map;
	}
	public void setRootResourceTypeMapping(Resource rdfType, Resource... resources) {
		typesMapping.put(rdfType, resources[0] );
		rootResourcesTypeMapping.put(rdfType, resources);
	}

	public Resource getTypeMapping(Resource rdfType) {
		Resource map = typesMapping.get(rdfType);
//		if(map==null) {
//			rootResourcesTypeMapping.get(rdfType);
//		}
		return map;
	}
	public void setTypeMapping(Resource srcType, Resource trgType) {
		typesMapping.put(srcType, trgType);
	}

//	public Map<Resource, ResourceTypeConversionSpecification> getTypeMappings() {
//		return typePropertiesMappings;
//	}

	public ResourceTypeConversionSpecification getTypePropertiesMapping(Resource rdfType) {
		ResourceTypeConversionSpecification map = typePropertiesMappings.get(rdfType);
		if(map==null) {
			map=new ResourceTypeConversionSpecification(rdfType);
			typePropertiesMappings.put(rdfType, map);
		}
		return map;
	}

	public Resource[] getRootResourceTypeMapping(String rdfTypeUriRef) {
		return rootResourcesTypeMapping.get(Jena.createResource(rdfTypeUriRef));
	}

	public FilterOfReferencedResource getFilterOfReferencedResource() {
		return filterOfReferencedResource;
	}

	public void setFilterOfReferencedResource(FilterOfReferencedResource filterOfReferencedResource) {
		this.filterOfReferencedResource = filterOfReferencedResource;
	}

	public ConversionHandler getConversionHandler() {
		return conversionHandler;
	}

	public void setConversionHandler(ConversionHandler conversionHandler) {
		this.conversionHandler = conversionHandler;
	}

	
	

}
