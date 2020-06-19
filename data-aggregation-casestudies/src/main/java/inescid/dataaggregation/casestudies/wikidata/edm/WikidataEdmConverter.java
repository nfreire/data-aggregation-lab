package inescid.dataaggregation.casestudies.wikidata.edm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;

import inescid.dataaggregation.wikidata.RdfRegWikidata;
import inescid.dataaggregation.wikidata.WikidataUtil;
import inescid.dataaggregation.wikidata.WikidataLabels;
import inescid.dataaggregation.data.DataModelRdfOwl;
import inescid.dataaggregation.data.RdfLiteralExtractor;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;
import inescid.dataaggregation.data.model.Owl;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Skos;
import inescid.dataaggregation.data.model.Wgs84;
import inescid.util.AccessException;
import inescid.util.RdfUtil;
import inescid.util.RdfUtil.Jena;
import inescid.util.datastruct.MapOfInts;

public class WikidataEdmConverter {
	private static final Resource UNSUPPORTED_TYPE=Jena.createResource();

	interface CustomizedConversion {
		public boolean isApplicableTo(Statement st, Resource targetType);
		public void apply(Statement st, Resource targetEdmResource);
	}
	public class P625Conversion implements CustomizedConversion {
		private final Pattern valuePattern=Pattern.compile("Point\\(([\\.\\d]+) ([\\.\\d]+)\\)");
		public boolean isApplicableTo(Statement st, Resource targetType) {
			return st.getPredicate().equals(RdfRegWikidata.COORDINATE_LOCATION) 
					&& st.getObject().isLiteral()					
					&& targetType.equals(Edm.Place);
		}
		public void apply(Statement st, Resource toEdmResource) {
			Matcher m = valuePattern.matcher(st.getObject().asLiteral().getString());
			if(m.matches()) {
				Model mdl = toEdmResource.getModel();
				mdl.add(toEdmResource, Wgs84.lat, mdl.createLiteral(m.group(1)));
				mdl.add(toEdmResource, Wgs84.long_, mdl.createLiteral(m.group(2)));
			}
		}
	}
	
	public class AggregationPropertiesConversion implements CustomizedConversion {
		public boolean isApplicableTo(Statement st, Resource targetType) {
			return targetType.equals(Edm.ProvidedCHO)
					&& aggregationMappings.containsKey(WikidataUtil.convertWdUriToCanonical(st.getPredicate().getURI())); 
		}
		public void apply(Statement st, Resource toEdmResource) {
			String edmProp = aggregationMappings.get(WikidataUtil.convertWdUriToCanonical(st.getPredicate().getURI()));
			Property edmPropObj = aggregation.getModel().createProperty(edmProp);
			RDFNode convertObjectOfStatement = convertObjectOfStatement(st, edmPropObj, aggregation);
			if(convertObjectOfStatement!=null)
				aggregation.addProperty(edmPropObj, convertObjectOfStatement);
		}
	}
	
	WikidataEdmMappings mapping;
	Map<String, String> aggregationMappings=new HashMap<>();
	DataModelRdfOwl edmModel; 
	List<CustomizedConversion> customConversions=new ArrayList<CustomizedConversion>() {{
		add(new P625Conversion());
		add(new AggregationPropertiesConversion());
	}};
	
	Model toModel = Jena.createModel();
	Map<Resource, Resource> convertedResources=new HashMap<Resource, Resource>();
	Resource aggregation=null;
	
	public WikidataEdmConverter(File csvFileClasses, File csvFileProperties, File csvFileHierarchy, File wikidataTbd2Folder, File edmOwlFile) throws IOException, AccessException, InterruptedException {
		mapping=new WikidataEdmMappings(csvFileClasses, csvFileProperties, csvFileHierarchy, wikidataTbd2Folder);
		edmModel=new DataModelRdfOwl(RdfUtil.readRdf(edmOwlFile, Lang.RDFXML));
		edmModel.addPropertyForLiteral(Skos.altLabel.getURI());
		edmModel.addPropertyForLiteral(Skos.prefLabel.getURI());
		edmModel.addPropertyForLiteral("http://rdvocab.info/ElementsGr2/professionOrOccupation");
		edmModel.addPropertyForUri(Skos.exactMatch.getURI());
		edmModel.addPropertyForUri(Owl.sameAs.getURI());
		edmModel.addPropertyForLiteral(Edm.provider.getURI());
		edmModel.addPropertyForLiteral(Edm.dataProvider.getURI());
		
		List<String> aggProps = edmModel.getPropertiesForType(Ore.Aggregation);
		for(String aggProp: aggProps) {
			for(String wdProp: mapping.getMappingsTo(Edm.ProvidedCHO.getURI() ,aggProp)) {
				aggregationMappings.put(wdProp, aggProp);
			}
		}
		System.out.println(aggregationMappings);
	}

	MapOfInts<String> unmappedClasses;
	Map<String,MapOfInts<String>> unmappedProperties;

	public void enableUnmappedLogging() {
		unmappedProperties=new HashMap<>();
		unmappedClasses=new MapOfInts<String>();
	}
	
	private void reset() {
		 convertedResources=new HashMap<Resource, Resource>();
		 aggregation=null;
	}
	
//	public Model convert(Model wdModel) {
//		return convert(wdModel, Jena.createModel());
//	}
//	public Model convert(Model wdModel, Model toModel) {
//		for(Resource wdRes: wdModel.listSubjects().toList()) {			
//			if(!wdRes.isURIResource()) continue;
//			convert(wdRes, toModel);
//		}
//		return toModel;
//	}
	public Resource convert(Resource wdRes) {
		reset();
		toModel = Jena.createModel();
		Resource converted = convert(wdRes, new ArrayList<Resource>());
		if(converted==null)
			toModel.close();
		return converted;
	}

	private Resource convert(Resource wdRes, List<Resource> typesOfParents) {
		if(!wdRes.isURIResource()) throw new IllegalArgumentException("Cannot convert an anonymous resource");
		
		if(convertedResources.containsKey(wdRes))
			return convertedResources.get(wdRes);
		
		Resource edmType=null;
		for(Resource type : RdfUtil.getTypes(wdRes) ) {
			if(!type.getURI().startsWith("http://www.wikidata")) continue;
			String edmTypeUri = mapping.getEdmType(type.getURI());
			if(edmTypeUri!=null)
				edmType=toModel.createResource(edmTypeUri);
			else if(unmappedClasses!=null)
				unmappedClasses.incrementTo(type.getURI());
		}
		if(edmType!=null) {
			if(typesOfParents.isEmpty() && edmType.equals(Edm.ProvidedCHO)
					&& aggregation==null) {
				aggregation=toModel.createResource(wdRes.getURI()+"#aggregation", Ore.Aggregation);
				aggregation.addProperty(Edm.aggregatedCHO, toModel.createResource(wdRes.getURI()));
			}
			if(typesOfParents.contains(edmType)) {
//				System.out.println("Skipping (repeated type "+edmType.getURI()+"):"+wdRes.getURI());
				return toModel.createResource(wdRes.getURI());
			}
			Resource edmRes = toModel.createResource(wdRes.getURI(), edmType);

//			System.out.println("Converting "+wdRes.getURI());
			for(Statement st: wdRes.listProperties().toList()) {
				if(st.getPredicate().equals(Rdf.type)) continue;
//				if(!st.getPredicate().getURI().startsWith("http://www.wikidata")) continue;
				
				for(CustomizedConversion cc : customConversions) {
					if (cc.isApplicableTo(st, edmType)) {
						cc.apply(st, edmRes);
						continue;
					}
				}
				
				String edmUri = mapping.getEdmProperty(edmType.getURI(), st.getPredicate().getURI());
				if(edmUri!=null) {
					Property edmProp = toModel.createProperty(edmUri);
					if(convertedResources.containsKey(st.getObject())) {
						Resource o = convertedResources.get(st.getObject());
						if(o!=UNSUPPORTED_TYPE)
							edmRes.addProperty(edmProp, o);
					} else {
						RDFNode convertedObj=convertObjectOfStatement(st, edmProp, edmRes);
						if(convertedObj!=null)
							edmRes.addProperty(edmProp, convertedObj);
					}
				} else if(unmappedProperties!=null) {
					MapOfInts<String> unmappedInType = unmappedProperties.get(edmType.getURI());
					if(unmappedInType==null) {
						unmappedInType=new MapOfInts<String>();
						unmappedProperties.put(edmType.getURI(), unmappedInType);
					}
					unmappedInType.incrementTo(WikidataUtil.convertWdUriToCanonical(st.getPredicate().getURI()));
				}
			}
			if(edmRes.listProperties().toList().isEmpty()) 
				toModel.removeAll(edmRes, null, null);
			convertedResources.put(wdRes, edmRes);

			typesOfParents.add(edmType);
			for(Statement st: edmRes.listProperties().toList()) {
				if(st.getObject().isURIResource()) {
					String uri=st.getObject().asResource().getURI();
					if( uri.startsWith("http://www.wikidata.org/") &&
							!RdfUtil.contains(uri, edmRes.getModel())) {
						try {
							Resource innerWdResource = WikidataUtil.fetchResource(uri);
//						conv.reset();
							if(edmModel.isLiteral(st.getPredicate())) {
								List<Literal> literals=RdfLiteralExtractor.extract(innerWdResource, RdfLiteralExtractor.Option.WITH_ALT_LABELS);
								toModel.remove(st);
								for(Literal l: literals) 
									edmRes.addLiteral(st.getPredicate(), l.inModel(toModel));
							} else {
								convert(innerWdResource, typesOfParents);
							}
						} catch (AccessException | InterruptedException | IOException e) {
							System.err.println("WARNING (skipping conversion): Could not get Wikidata resource "+uri);
							continue;
						}
					}
				}
			}
			typesOfParents.remove(typesOfParents.size()-1);
			return edmRes;
		}
		convertedResources.put(wdRes, UNSUPPORTED_TYPE);
		return null;
	}

	private RDFNode convertObjectOfStatement(Statement st, Property toProperty, Resource toResource) {
		RDFNode obj=st.getObject();
		if(WdIdentifierProperties.isIdProperty(st.getPredicate()) ) {
			if(obj.isLiteral()) {
				if(edmModel.isUri(toProperty)) 
					return null;
				RDFNode converted = WdIdentifierProperties.convert(st.getPredicate(), obj.asLiteral());
				return converted.inModel(toModel);
			}else if(obj.isURIResource())
				return toModel.createResource(obj.asResource().getURI());
			return null;
		}
		if(edmModel.isUri(toProperty)) {
			if(!obj.isURIResource())
				return null;
		} else if(edmModel.isLiteral(toProperty)) {
			try {
				if(obj.isLiteral()) 
					return obj.inModel(toModel);
				Resource resource = obj.asResource();
				if(obj.isURIResource())
					resource = WikidataUtil.fetchResource(obj.asResource().getURI());
				List<Literal> literals=RdfLiteralExtractor.extract(
						resource
						, RdfLiteralExtractor.Option.WITH_ALT_LABELS);
				for(Literal l: literals) 
					toResource.addLiteral(st.getPredicate(), l.inModel(toModel));
				return null;
			} catch (AccessException | InterruptedException | IOException e) {
				return null;
			}
		}
		if(obj.isLiteral()) 
			return obj.inModel(toModel);
		if (obj.isURIResource())
			return toModel.createResource(obj.asResource().getURI());
		//is Anon
		return convertAnon(obj.asResource(), convertedResources);
//		RDFNode obj=st.getObject();
//		if(WdIdentifierProperties.isIdProperty(st.getPredicate()) ) {
//			if(obj.isLiteral()) {
//				RDFNode converted = WdIdentifierProperties.convert(st.getPredicate(), obj.asLiteral());
//				return converted.inModel(toModel);
//			}else
//				return toModel.createResource(obj.asResource().getURI());
//		}
//		if(obj.isLiteral()) 
//			return obj.inModel(toModel);
//		if (obj.isURIResource())
//			return toModel.createResource(obj.asResource().getURI());
//		//is Anon
//		return convertAnon(obj.asResource(), toModel, convertedResources);
	}
			
	public RDFNode convertAnon(Resource wdRes, Map<Resource, Resource> convertedResources) {
		Resource edmType=null;
		for(Resource type : RdfUtil.getTypes(wdRes) ) {
			String edmTypeUri = mapping.getEdmType(type.getURI());
			if(edmTypeUri!=null)
				edmType=toModel.createResource(edmTypeUri);
		}
		if(edmType==null) 
			return RdfUtil.getLabelForResource(wdRes);
		Resource edmRes = toModel.createResource(wdRes.getId());
		toModel.add(toModel.createStatement(edmRes, Rdf.type,edmType));
		boolean hasStatements=false;
		for(Statement st: wdRes.listProperties().toList()) {
			if(st.getPredicate().equals(Rdf.type)) continue;
			
			for(CustomizedConversion cc : customConversions) {
				if (cc.isApplicableTo(st, edmType)) {
					cc.apply(st, edmRes);
					continue;
				}
			}
			String edmUri = mapping.getEdmProperty(edmType.getURI(), st.getPredicate().getURI());
			if(edmUri!=null) {
				Property edmProp = toModel.createProperty(edmUri);
				if(convertedResources.containsKey(st.getObject())) {
					edmRes.addProperty(edmProp, convertedResources.get(st.getObject()));
					hasStatements=true;
				} else {
					RDFNode convertedObj=convertObjectOfStatement(st, edmProp, edmRes);
					if(convertedObj!=null) {
						edmRes.addProperty(edmProp, convertedObj);
						hasStatements=true;
					}
				}
			}
		}
		if(!hasStatements) {
			toModel.removeAll(edmRes, null, null);
			return null;
		}
		return edmRes;
	}

	public String unmappedToCsv() {
		WikidataLabels labels=new WikidataLabels();
		for(String uri: unmappedClasses.keySet())
			labels.get(uri);
		for(String edmType: unmappedProperties.keySet()) {
			for(String uri: unmappedProperties.get(edmType).keySet())
				labels.get(uri);
		}		
		try {
			StringWriter csvWrite = new StringWriter();
			csvWrite.append("Unmapped classes\n");
			MapOfInts.writeCsv(unmappedClasses, labels.getMap(), csvWrite);
			
			csvWrite.append("\nUnmapped properties\n");
			for(String edmType: unmappedProperties.keySet()) {
				csvWrite.append("Unmapped in type:,").append(edmType).append("\n");
				MapOfInts.writeCsv(unmappedProperties.get(edmType), labels.getMap(), csvWrite);
			}
			return csvWrite.toString();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
		
			
}
