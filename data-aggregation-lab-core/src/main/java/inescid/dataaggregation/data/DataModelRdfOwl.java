package inescid.dataaggregation.data;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import inescid.dataaggregation.dataset.Global;
import inescid.util.RdfUtil;
import inescid.util.RdfUtil.Jena;
import inescid.util.datastruct.MapOfLists;
import inescid.util.datastruct.MapOfSets;

public class DataModelRdfOwl {
	Model owl;
	
	Set<String> allProperties=new HashSet<String>();
	MapOfSets<String, String> rangeOfProperty=new MapOfSets<String, String>();
	MapOfSets<String, String> domainOfProperty=new MapOfSets<String, String>();
	Set<String> literalProperty=new HashSet<String>();
	MapOfSets<String, String> superClasses=new MapOfSets<String, String>();
	Map<String, String> equivalences=new HashMap<>();
	boolean acceptUnknown=false;
	
	public DataModelRdfOwl(Model owl) {
		super();
		this.owl = owl;
		
		for(Resource r: owl.listResourcesWithProperty(RegRdf.type, RegOwl.Class).toList()) {
			for(Statement scSt : r.listProperties(RegRdfs.subClassOf).toList()) {
				if (!scSt.getObject().isURIResource()) continue;
				superClasses.put(r.getURI(), scSt.getObject().asResource().getURI());
			}	
			for(Statement st : r.listProperties(RegOwl.equivalentClass).toList()) {
				if (!st.getObject().isURIResource()) continue;
				equivalences.put(st.getObject().asResource().getURI(), r.getURI());
//				equivalences.put(r.getURI(), st.getObject().asResource().getURI());
			}
		}		
		for(boolean added=true; added; ) {
			added=false;
			for(String cls:	superClasses.keySet()) {
				int before=superClasses.get(cls).size();
				for(String superCls : new ArrayList<String>(superClasses.get(cls))) {
					Set<String> superSuperClasses = superClasses.get(superCls);
					if(superSuperClasses!=null)
						superClasses.putAll(cls, superSuperClasses);
				}
				added=added || before!=superClasses.get(cls).size();
			}
		}		

		HashSet<Resource> resources=new HashSet<Resource>(owl.listResourcesWithProperty(RegRdf.type, owl.createProperty("http://www.w3.org/2002/07/owl#datatypeProperty")).toList());
		for(Resource r: resources) {
			allProperties.add(r.getURI());
			Statement domainSt = r.getProperty(RegRdfs.domain);
			if(domainSt != null ) {
				Resource domainClass = domainSt.getObject().asResource();
				Statement unionSt = domainClass.getProperty(RegOwl.unionOf);
				if(unionSt != null && domainClass.isAnon()) {
					for(RDFNode node : unionSt.getObject().asResource().as(RDFList.class).asJavaList()) {
						domainOfProperty.put(r.getURI(), node.asResource().getURI());
					}
				} else {
					System.out.println("Not anom domain "+r.getURI());
				}
			}
			literalProperty.add(r.getURI());
		}
		resources.clear();
		
		resources.addAll(owl.listResourcesWithProperty(RegRdf.type, RegOwl.ObjectProperty).toList());
		for(Resource r: resources) {
			allProperties.add(r.getURI());
			Statement domainSt = r.getProperty(RegRdfs.domain);
			if(domainSt != null ) {
				Resource domainClass = domainSt.getObject().asResource();
				Statement unionSt = domainClass.getProperty(RegOwl.unionOf);
				if(unionSt==null)
					domainOfProperty.put(r.getURI(), domainClass.getURI());
				else if(domainClass.isAnon()) {
					for(RDFNode node : unionSt.getObject().asResource().as(RDFList.class).asJavaList()) {
						domainOfProperty.put(r.getURI(), node.asResource().getURI());
					}
				} else {
					System.out.println("Not anom domain "+r.getURI());
				}
			}
			Statement rangeSt = r.getProperty(RegRdfs.range);
			if(rangeSt != null ) {
				Resource domainClass = rangeSt.getObject().asResource();
				Statement unionSt = domainClass.getProperty(RegOwl.unionOf);
				if(unionSt==null) {
					rangeOfProperty.put(r.getURI(), domainClass.getURI());					
				}else if(domainClass.isAnon()) {
					for(RDFNode node : unionSt.getObject().asResource().as(RDFList.class).asJavaList()) {
						rangeOfProperty.put(r.getURI(), node.asResource().getURI());
					}
				} else {
					System.out.println("Not anom domain "+r.getURI());
				}
			} else {
				literalProperty.add(r.getURI());				
			}
			for(Statement st : r.listProperties(RegOwl.equivalentProperty).toList()) {
//				equivalences.put(r.getURI(), st.getObject().asResource().getURI());
				equivalences.put(st.getObject().asResource().getURI(), r.getURI());
			}

		}

		for(String prop: rangeOfProperty.keySet()) {
			Set<String> set = rangeOfProperty.get(prop);
			for(String rngCls: set.toArray(new String[set.size()])) {
				if(superClasses.containsKey(rngCls))
					rangeOfProperty.putAll(prop, superClasses.get(rngCls));
			}
		}
		
	}

	public List<String> validate(Model schemaorg) {
		List<String> errors=new ArrayList<String>();
		for(Resource r: schemaorg.listSubjects().toList()) {
			Set<String> allRdfTypes=getAllRdfTypesOfResource(r);
			for(Statement scSt : r.listProperties().toList()) {
				if(scSt.getPredicate().getURI().equals("http://schema.org/scopedObj") || scSt.getPredicate().equals(RegRdf.type)) continue;
				if(!isValidDomain(allRdfTypes, scSt.getPredicate())) {
					errors.add(r.getURI()+ " not in domain of "+scSt.getPredicate().getURI());
				}else if(!isValidRange(scSt.getObject(), scSt.getPredicate())) {
					errors.add(r.getURI()+ " value not in range of "+scSt.getPredicate().getURI());					
				}
			}			
		}	
		return errors;
	}
	
	private boolean isValidRange(RDFNode object, Property predicate) {
		if(!acceptUnknown && !allProperties.contains(predicate.getURI())) return false;
		Set<String> range = rangeOfProperty.get(predicate.getURI());
//		System.out.println(predicate+" "+range);
		if(range==null) return true;
		if(object.isLiteral()) {
			return range.contains(RegSchemaorg.Text.getURI()) || range.contains(RegSchemaorg.URL.getURI()); 
		} else if(object.isResource()) {
			Set<String> allTypes = getAllRdfTypesOfResource(object.asResource());
			if(object.isURIResource() && range.contains(RegSchemaorg.URL.getURI()))
//				if(allTypes.isEmpty() && object.isURIResource() && range.contains(RegSchemaorg.URL.getURI()))
				return true;
			for(String type: allTypes) { 
				if(range.contains(type)) 
					return true;
//				else {
//					for(String superCls: superClasses.get(type)) {
//						if(range.contains(superCls)) 
//							return true;						
//					}
//				}
			}
		}
		return false;
	}

	private boolean isValidDomain(Set<String> allRdfTypes, Property predicate) {
		if(!acceptUnknown && !allProperties.contains(predicate.getURI())) return false;
		Set<String> domain = domainOfProperty.get(predicate.getURI());
		if(domain==null) return true;
		for(String type: allRdfTypes) {
			if(domain.contains(type)) return true;
			if(superClasses.containsKey(type)) {
				for(String superCls: superClasses.get(type)) {
					if(domain.contains(superCls)) return true;
				}
			}
		}
		return false;
	}

	private Set<String> getAllRdfTypesOfResource(Resource r) {
		Set<String> typesSet = new HashSet<String>();
		for(Statement scSt : r.listProperties(RegRdf.type).toList()) {
			Set<String> typeScs = superClasses.get(scSt.getObject().asResource().getURI());
			if(typeScs!=null)
				typesSet.addAll(typeScs);
			typesSet.add(scSt.getObject().asResource().getURI());
		}
		return typesSet;
	}

	public static void main(String[] args) throws Exception {
//		Global.init_componentHttpRequestService();
//		Model schemaorgOwl = RdfUtil.readRdfFromUri("https://schema.org/docs/schemaorg.owl");
//		new RdfDomainRangeValidatorWithOwl(schemaorgOwl);
		Model edmMdl = RdfUtil.readRdf(new FileInputStream("src/main/resources/owl/edm.owl"));
		DataModelRdfOwl mdl=new DataModelRdfOwl(edmMdl);
		System.out.println(mdl.getEquivalent("http://metadata.net/harmony/abc#Time"));
		System.out.println(mdl.getEquivalent("http://www.cidoc-crm.org/rdfs/cidoc-crm#P4_has_time-span"));
	}

	private String getEquivalent(String uriOfPropertyOrClass) {
		return equivalences.get(uriOfPropertyOrClass);
	}

	public boolean isLiteral(Property predicate) {
		return literalProperty.contains(predicate.getURI());
	}
	
}