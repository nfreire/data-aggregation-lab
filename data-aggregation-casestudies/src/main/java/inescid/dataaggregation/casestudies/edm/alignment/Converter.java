package inescid.dataaggregation.casestudies.edm.alignment;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import inescid.dataaggregation.data.RdfLiteralExtractor;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.wikidata.WikidataLabels;
import inescid.util.AccessException;
import inescid.util.RdfUtil;
import inescid.util.RdfUtil.Jena;
import inescid.util.datastruct.MapOfInts;

public class Converter {
	private static final Resource UNSUPPORTED_TYPE=Jena.createResource();
		
	Alignment mapping;
	
	Model toModel;
	MultiKeyMap<String, Resource> convertedResources;
	
	public Converter(Alignment mapping) throws IOException, AccessException, InterruptedException {
		this.mapping = mapping;
	}

	MapOfInts<String> unmappedClasses;
	Map<String,MapOfInts<String>> unmappedProperties;

	public void enableUnmappedLogging() {
		unmappedProperties=new HashMap<>();
		unmappedClasses=new MapOfInts<String>();
	}
	
	private void reset() {
		 convertedResources=new MultiKeyMap<String, Resource>();
	}
	
	public Resource convert(Resource wdRes) {
		reset();
		toModel = Jena.createModel();
		convertResource(wdRes);
		if(!RdfUtil.exists(wdRes.getURI(), toModel)) 
			toModel.close();
		return toModel.createResource(wdRes.getURI());
	}

	private void convertResource(Resource wdRes) {
		if(!wdRes.isURIResource()) throw new IllegalArgumentException("Cannot convert an anonymous resource");
		
		for(Resource type : RdfUtil.getTypes(wdRes) ) {
			 List<ClassAlignment> alignments = mapping.getAlignmentsFor(type.getURI());
			 if(!alignments.isEmpty()) {
				 for(ClassAlignment alignment: alignments) {
					 if(!convertedResources.containsKey(wdRes.getURI(), alignment.getClassUri()))
						 convert(wdRes, alignment);
				 }
			 } else if(unmappedClasses!=null)
					unmappedClasses.incrementTo(type.getURI());
		}
	}
	
	private Resource convert(Resource srcRes, ClassAlignment alignment) {
		Resource trgRes;
		if (srcRes.isAnon()) {
			trgRes=toModel.createResource(srcRes.getId());
			toModel.add(toModel.createStatement(trgRes, Rdf.type, toModel.createResource(alignment.getClassUri())));
		}else
			trgRes=toModel.createResource(srcRes.getURI(), toModel.createResource(alignment.getClassUri()));

		convertedResources.put(srcRes.getURI(), alignment.getClassUri(), trgRes);

//		System.out.println("Converting "+wdRes.getURI());
		for(Statement st: srcRes.listProperties().toList()) {
			if(st.getPredicate().equals(Rdf.type)) continue;

			PropertyAlignment propAlignment = alignment.getPropertyAlignment(st.getPredicate().getURI());
			if(propAlignment!=null) {
				if(convertedResources.containsKey(st.getObject())) {
					Resource o = convertedResources.get(st.getObject());
					if(o!=UNSUPPORTED_TYPE)
						trgRes.addProperty(toModel.createProperty(propAlignment.propertyUri), o);
				} else {
					convertStatement(st, propAlignment, trgRes);
				}
			} else if(unmappedProperties!=null) {
				MapOfInts<String> unmappedInType = unmappedProperties.get(alignment.getClassUri());
				if(unmappedInType==null) {
					unmappedInType=new MapOfInts<String>();
					unmappedProperties.put(alignment.getClassUri(), unmappedInType);
				}
				unmappedInType.incrementTo(st.getPredicate().getURI());
			}
		}
		if(trgRes.listProperties().toList().isEmpty()) {
			toModel.removeAll(trgRes, null, null);
			convertedResources.put(srcRes.getURI(), alignment.getClassUri(), UNSUPPORTED_TYPE);
			trgRes = null;
		}
		return trgRes;
	}
	
	private void convertStatement(Statement st, PropertyAlignment toProperty, Resource toResource) {
		RDFNode obj=st.getObject();
		Property predicate = toModel.createProperty(toProperty.getPropertyUri());
		if(obj.isResource()) {
			if(toProperty.getAllowedValue().allowsResource()) {
				convertResource(obj.asResource());
				if(RdfUtil.exists(obj.asResource().getURI(), toModel)) 
					toResource.addProperty(predicate, toModel.createResource(obj.asResource().getURI()));
				else if(toProperty.getAllowedValue().allowsLiteral()) 
					createStatementResourceConvertedToLiteral(toResource, toProperty, obj.asResource());
			}else if(toProperty.getAllowedValue().allowsUri() && !obj.isAnon()) {
				toResource.addProperty(predicate, toModel.createResource(obj.asResource().getURI()));
			}else {//toLiteral
				createStatementResourceConvertedToLiteral(toResource, toProperty, obj.asResource());				
			}
		} else { //is literal 
			if(toProperty.getAllowedValue().allowsLiteral()) 
				toResource.addProperty(predicate, obj.inModel(toModel));
		}
	}
			
	private void createStatementResourceConvertedToLiteral(Resource toResource, PropertyAlignment toProperty, Resource obj) {
		List<Literal> literals=RdfLiteralExtractor.extract(
				obj.asResource()
				, toProperty.getMaxCardinality()>1 ? RdfLiteralExtractor.Option.WITH_ALT_LABELS :  null);
		int count=0;
		for(Literal l: literals) {
			toResource.addLiteral(toModel.createProperty(toProperty.getPropertyUri()), l.inModel(toModel));
			count++;
			if(toProperty.getMaxCardinality() <= count)
				break;
		}
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
