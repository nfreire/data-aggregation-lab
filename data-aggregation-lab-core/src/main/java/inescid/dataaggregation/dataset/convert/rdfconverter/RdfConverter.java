package inescid.dataaggregation.dataset.convert.rdfconverter;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import inescid.dataaggregation.data.DataModelRdfOwl;
import inescid.dataaggregation.data.RdfsClassHierarchy;
import inescid.dataaggregation.data.model.Rdf;
import inescid.util.RdfUtil;

/**
 * @author nfrei
 *
 *	This class is not thread-safe
 *
 */
public class RdfConverter {
	
	private static final Charset UTF8=Charset.forName("UTF8");
	
	Map<String, Map<ResourceTypeConversionSpecification, Resource>> nodesMapped=new HashMap<String, Map<ResourceTypeConversionSpecification, Resource>>();
	
	RdfConversionSpecification spec;
	RdfsClassHierarchy classHierarcgy;
	
	Resource mainTargetResource=null;
	
	HashSet<String> reportedMappingMissing=new HashSet<String>();
	
	public RdfConverter(RdfConversionSpecification spec) {
		this.spec=spec;
	}
	
	public RdfConverter(RdfConversionSpecification spec, RdfsClassHierarchy classHierarcgy) {
		this.spec=spec;
		this.classHierarcgy=classHierarcgy;
	}
	
	public Resource convert(Resource srcRoot) {
		Model source=srcRoot.getModel();
		nodesMapped.clear();
		mainTargetResource=null;
		Model targetModelRdf = ModelFactory.createDefaultModel();		
		
		boolean firstType=true;
		Resource resType=null;

//		StmtIterator propTypesStms = srcRoot.listProperties(RdfRegRdf.type);
		StmtIterator propTypesStms = source.listStatements(srcRoot, Rdf.type, (RDFNode) null);
		
		Resource[] rootResourceTypeMapping = null;
		while (propTypesStms.hasNext()) {
			Statement typeStm = propTypesStms.next();
			resType=typeStm.getObject().asResource();
			rootResourceTypeMapping = spec.getRootResourceTypeMapping(resType);
			if (rootResourceTypeMapping!=null)
				break;
		}
		if (rootResourceTypeMapping==null && classHierarcgy!=null) {
			propTypesStms = source.listStatements(srcRoot, Rdf.type, (RDFNode) null);
			TYPES: while (propTypesStms.hasNext()) {
				Statement typeStm = propTypesStms.next();
				resType=typeStm.getObject().asResource();
				for (String sc:classHierarcgy.getSuperClassesOf(resType.getURI())) {
					rootResourceTypeMapping = spec.getRootResourceTypeMapping(sc);
					if (rootResourceTypeMapping!=null) {
						break TYPES;					
					}
				}
			}
		}
		if (rootResourceTypeMapping==null) {
			String msg="No mapping found for Entity(ies)";
			propTypesStms = source.listStatements(srcRoot, Rdf.type, (RDFNode) null);
			while (propTypesStms.hasNext()) {
				Statement typeStm = propTypesStms.next();
				msg+=" "+typeStm.getObject().asResource().getURI();
			} 
			if (!reportedMappingMissing.contains(msg)) {
				System.out.println(msg);
				reportedMappingMissing.add(msg);
			}
			return null;
//			return convert(source);
		}		
		
		for (Resource trgType: rootResourceTypeMapping) {
			String uri=srcRoot.getURI();
			if(firstType)
				firstType=false;
			else
				uri+="#"+getElementName(trgType.getURI());
				
			ResourceTypeConversionSpecification trgResourceMap = spec.getTypePropertiesMapping(trgType);
			
			Resource trgResource=convert(srcRoot, source, targetModelRdf, uri, trgResourceMap, spec);
			if (mainTargetResource==null)
				mainTargetResource=trgResource;
		} 
//	StmtIterator choStms = targetModelRdf.listStatements();
//	while (choStms.hasNext()) 
//		System.out.println(choStms.next());

		finalizeConversion(targetModelRdf, srcRoot);
		return mainTargetResource;

	}

	private void finalizeConversion(Model targetModelRdf, Resource srcRoot) {
		if (mainTargetResource!=null) {
			deduplicateStatements(targetModelRdf);
			if(spec.getConversionHandler()!=null)
				spec.getConversionHandler().handleConvertedResult(srcRoot, mainTargetResource);
		}
	}

	public Resource convert(Model ldModelRdf) {
		nodesMapped.clear();
		mainTargetResource=null;
		Model targetModelRdf = ModelFactory.createDefaultModel();
//		if(source.getContentType().equals("application/json") && source.getFormat().equals("application/ld+json")) {
//			Model ldModelRdf = ModelFactory.createDefaultModel();

		Set<Resource> rootTypes=spec.getRootResourceTypes();
			
			for(Resource resType: rootTypes) {
				ResIterator roots = ldModelRdf.listSubjectsWithProperty(Rdf.type, resType);
				while(roots.hasNext()) {
					Resource srcRoot = roots.next();
					boolean firstType=true;
					for (Resource trgType: spec.getRootResourceTypeMapping(resType)) {
						String uri=srcRoot.getURI();
						if(firstType)
							firstType=false;
						else
							uri+="#"+getElementName(trgType.getURI());
							
						ResourceTypeConversionSpecification trgResourceMap = spec.getTypePropertiesMapping(trgType);
						
						Resource trgResource=convert(srcRoot, ldModelRdf, targetModelRdf, uri, trgResourceMap, spec);
						if (mainTargetResource==null)
							mainTargetResource=trgResource;
					} 
					finalizeConversion(targetModelRdf, srcRoot);
					return mainTargetResource;
				}
				
//				StmtIterator choStms = targetModelRdf.listStatements();
//				while (choStms.hasNext()) 
//					System.out.println(choStms.next());
			}
			
			return null;
	}

	public Resource convert(Model source, String rootResourceUri) {
		if(rootResourceUri==null)
			return convert(source);
//		if(source.getContentType().equals("application/json") && source.getFormat().equals("application/ld+json")) {
//			Model ldModelRdf = ModelFactory.createDefaultModel();
		boolean rootResourceUriExists=source.contains(ResourceFactory.createResource(rootResourceUri), null, (RDFNode) null);
		if(! rootResourceUriExists)
			return convert(source);

		Resource srcRoot = source.getResource(rootResourceUri);
		return convert(srcRoot);
		
	}
	
	
	private Resource convert(Resource srcResource, Model ldModelRdf, Model targetModelRdf, String uri, ResourceTypeConversionSpecification trgResourceMap, RdfConversionSpecification spec) {
		String srcResUriOrId = RdfUtil.getUriOrId(srcResource);
		if(nodesMapped.containsKey(srcResUriOrId)) {
			Map<ResourceTypeConversionSpecification, Resource> typeMapped = nodesMapped.get(srcResUriOrId);
			if(typeMapped.containsKey(trgResourceMap)) 
				return typeMapped.get(trgResourceMap);
		}
		Resource trgResource=null;
		if(uri!=null)
			trgResource=targetModelRdf.createResource(uri, trgResourceMap.getType());
		else if(srcResource.isURIResource())
			trgResource=targetModelRdf.createResource(srcResource.getURI(), trgResourceMap.getType());
		else {
			if(trgResourceMap.getPropertiesMappingToUri().isEmpty()) {
				trgResource=targetModelRdf.createResource();
				trgResource.addProperty(Rdf.type, trgResourceMap.getType());
			} else {
				for(Property p: trgResourceMap.getPropertiesMappingToUri()) {
					StmtIterator cwStms = ldModelRdf.listStatements(srcResource, p, (RDFNode) null);
					while (cwStms.hasNext()) {
						Statement st = cwStms.next();
						if(st.getObject().isURIResource()) {
							trgResource=targetModelRdf.createResource(st.getObject().asResource().getURI(), trgResourceMap.getType());
							break;
						}
					}
				}
				if(trgResource==null) {
					trgResource=targetModelRdf.createResource();
					trgResource.addProperty(Rdf.type, trgResourceMap.getType());
				}
			}
		}
		
		//add resource to mapped resources to avoid infinit recursion in some cases
		Map<ResourceTypeConversionSpecification, Resource> resTypeMapped = nodesMapped.get(srcResUriOrId);
		if(resTypeMapped==null) {
			resTypeMapped=new HashMap<>();
			nodesMapped.put(srcResUriOrId, resTypeMapped);
		}
		resTypeMapped.put(trgResourceMap, trgResource);
		
		StmtIterator cwStms = ldModelRdf.listStatements(srcResource, (Property) null, (RDFNode) null);
		while (cwStms.hasNext()) {
			Statement st = cwStms.next();
//			System.out.println(st);
			PropertyMappingSpecification propMap = trgResourceMap.getPropertyMapping(st.getPredicate());
			if(propMap!=null) {
				if(st.getObject().isLiteral()) {
					Property[] propertyMerge = trgResourceMap.getPropertyMerge(st.getPredicate());
					if(propertyMerge!=null) {
						String mergedLiteral=st.getObject().asLiteral().getString();
						for(Property mergeProp: propertyMerge) {
							StmtIterator mergeStms = ldModelRdf.listStatements(srcResource, mergeProp, (RDFNode) null);
							while (mergeStms.hasNext()) {
								Statement mergest = mergeStms.next();
								if(mergest.getObject().isLiteral()) {
									mergedLiteral+=", "+ mergest.getObject().asLiteral().getString();
								}
							}
						}
						trgResource.addProperty(propMap.getProperty(), targetModelRdf.createLiteral(mergedLiteral));	
					} else
						trgResource.addProperty(propMap.getProperty(), st.getObject());	
				} else {
					boolean hasSubMap=false;
					StmtIterator propTypesStms = ldModelRdf.listStatements(st.getObject().asResource(), Rdf.type, (RDFNode) null);
					boolean hasStatements=propTypesStms.hasNext();
					while (propTypesStms.hasNext()) {
						Statement typeStm = propTypesStms.next();
//						typeStm.getObject().asResource()
						ResourceTypeConversionSpecification propertyMappingFromReferencedResource = trgResourceMap.getPropertyMappingFromReferencedResource(st.getPredicate(), typeStm.getObject().asResource());
						if (propertyMappingFromReferencedResource!=null) {
							if(spec.filterOfReferencedResource==null || !spec.filterOfReferencedResource.filterOut(st)) {
								hasSubMap=true;
								convert(trgResource, st.getObject().asResource(), ldModelRdf, propertyMappingFromReferencedResource);
							}
						}
					}
					if(!hasSubMap) {
						if(st.getObject().isURIResource() && propMap.isMapToValueAlways()) {
							trgResource.addProperty(propMap.getProperty(), targetModelRdf.createLiteral(st.getObject().asResource().getURI()));								
						} else {
							boolean isTargetAnon=true;
							if(st.getObject().isURIResource()) {
								trgResource.addProperty(propMap.getProperty(), st.getObject());
								isTargetAnon=false;
							}
							if (hasStatements) {
								propTypesStms = ldModelRdf.listStatements(st.getObject().asResource(), Rdf.type, (RDFNode) null);
								while (propTypesStms.hasNext()) {
									Statement typeStm = propTypesStms.next();
									if(typeStm.getObject().equals(Rdf.type))
										continue;
									Resource convertedType=spec.getTypeMapping((Resource) typeStm.getObject());
									if(convertedType==null) {
										String msg=("No mapping found for Entity "+typeStm.getObject());
										if (!reportedMappingMissing.contains(msg)) {
											System.out.println(msg);
											reportedMappingMissing.add(msg);
										}
										continue;
									}
									ResourceTypeConversionSpecification trgSubResourceMap = spec.getTypePropertiesMapping(convertedType);
									
									Resource objRes=st.getObject().asResource();
	//								String uriOrId=null;
	//								if (objRes.getURI()==null) {
	//									uriOrId= objRes.getId().getBlankNodeId().getLabelString();
	//									trgResource.addProperty(propMap, st.getObject());									
	//								} else
	//									objRes.getURI();
									if(trgSubResourceMap!=null && !srcResource.equals(objRes)) {
										if(spec.filterOfReferencedResource==null || !spec.filterOfReferencedResource.filterOut(st)) {
											Resource createdTrgSubResource=convert(objRes, ldModelRdf, targetModelRdf, null, trgSubResourceMap, spec);
											if(isTargetAnon && createdTrgSubResource!=null) 
												trgResource.addProperty(propMap.getProperty(),createdTrgSubResource);
										}
									} 
	//								(Resource srcResource, Model ldModelRdf, Model targetModelRdf, String uri, ResourceTypeConversionSpecification trgResourceMap, RdfConversionSpecification spec) {
	
								}
							}
						}
					}
				}
			} 
			DerivedPropertyConversionSpecification specOfDerived = trgResourceMap.getDerivedPropertyMapping(st.getPredicate());
			if(specOfDerived!=null) {
				if(st.getObject().isURIResource() && specOfDerived.getDerivedProperty().isMapToValueAlways()) {
					trgResource.addProperty(specOfDerived.getDerivedProperty().getProperty(),targetModelRdf.createLiteral(st.getObject().asResource().getURI()));
				}else {
					if(st.getObject().isURIResource()) {
						uri = st.getObject().asNode().getURI();
						String val = specOfDerived.getUriMapping(uri);
						if(val!=null)
							trgResource.addProperty(specOfDerived.getDerivedProperty().getProperty(),val);
					} else {
						String val = specOfDerived.getLiteralMapping(st.getObject().asLiteral().getString());
						if(val!=null)
							trgResource.addProperty(specOfDerived.getDerivedProperty().getProperty(),val);
					}
				}
//			} else if(!st.getPredicate().equals(RdfRegRdf.type)){
//				String msg=("No mapping found for Property "+st.getPredicate()+ " in " + trgResourceMap.getType());
//				if (!reportedMappingMissing.contains(msg)) {
//					System.out.println(msg);
//					reportedMappingMissing.add(msg);
//				}
////				System.out.println("No mapping found for Property "+st.getPredicate()+ " in " + trgResourceMap.getType());
////				System.out.println("Property Value:"+st.getObject());
			}
		}	
		return trgResource;
	}

	private void convert(Resource trgResource, Resource src, Model srcModel,
			ResourceTypeConversionSpecification mapping) {
		StmtIterator srcStms = srcModel.listStatements(src, (Property) null, (RDFNode) null);
		while (srcStms.hasNext()) {
			Statement st = srcStms.next();
//			System.out.println(st);
			PropertyMappingSpecification propMap = mapping.getPropertyMapping(st.getPredicate());
			if(propMap!=null) {
				if(st.getObject().isLiteral()) {
					trgResource.addProperty(propMap.getProperty(), st.getObject());								
				} else {
					StmtIterator propTypesStms = srcModel.listStatements(st.getObject().asResource(), Rdf.type, (RDFNode) null);
					while (propTypesStms.hasNext()) {
						Statement typeStm = propTypesStms.next();
						ResourceTypeConversionSpecification propertyMappingFromReferencedResource = mapping.getPropertyMappingFromReferencedResource(st.getPredicate(), typeStm.getObject().asResource());
						if (propertyMappingFromReferencedResource!=null) {
							convert(trgResource, (Resource)st.getObject(), srcModel, propertyMappingFromReferencedResource);
						}
					}
				}
			}
		}
	}

	private void deduplicateStatements(Model model) {
		HashSet<Statement> dedupeSet=new HashSet<>();
		if(ResourceTypeConversionSpecification.DEDUPLICATE_STATEMENTS) {
			StmtIterator srcStms = model.listStatements();
			while (srcStms.hasNext()) {
				Statement st = srcStms.next();
				if (dedupeSet.contains(st))
					model.remove(st);
				else
					dedupeSet.add(st);
			}
		}
	}
	
	private String getElementName(String uri) {
		int lastPath=uri.lastIndexOf('/');
		int lastFrag=uri.lastIndexOf('#');
		return uri.substring(Math.max(lastPath, lastFrag)+1);
	}


}
