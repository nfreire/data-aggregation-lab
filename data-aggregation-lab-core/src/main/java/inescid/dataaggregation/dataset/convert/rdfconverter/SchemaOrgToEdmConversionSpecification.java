package inescid.dataaggregation.dataset.convert.rdfconverter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import inescid.dataaggregation.data.RdfReg;
import inescid.dataaggregation.data.RdfRegEdm;
import inescid.dataaggregation.data.RdfRegRdf;
import inescid.dataaggregation.dataset.convert.FilterOfReferencedResource;

public class SchemaOrgToEdmConversionSpecification {
	public static final RdfConversionSpecification spec;
	
	private static class EdmTypePreferedValueHandler implements ConversionHandler {
		private static Set<String> propertiesProcessed=new HashSet<String>() {{
			add(RdfRegEdm.type.getURI());
		}};
		@Override
		public Set<String> propertiesProcessed() {
			return propertiesProcessed;
		}

		@Override
		public void handleConvertedResult(Resource source, Resource target) {
			Statement firstType=null;
			ArrayList<Statement> typeCnt=new ArrayList<Statement>();
			StmtIterator cwStms = target.listProperties(RdfRegEdm.type);
			while (cwStms.hasNext()) {
				Statement st = cwStms.next();
				typeCnt.add(st);
				if(st.getObject().isLiteral()) {
					if(st.getObject().asLiteral().getString().equals("TEXT")) 
						firstType=st;
					else if (firstType==null)
						firstType=st;
				}
			}
			if(!typeCnt.isEmpty()) {
				for (Statement st: typeCnt) {
					if(!st.equals(firstType)) 
						target.getModel().remove(st);
				}
			}
		}
		
	}
	
//	public SchemaOrgToEdmConversionSpecification() {
	static {
		spec=new RdfConversionSpecification();
		
		spec.setRootResourceTypeMapping(RdfReg.SCHEMAORG_CREATIVE_WORK, RdfRegEdm.ProvidedCHO, RdfReg.ORE_AGGREGATION);
		spec.setRootResourceTypeMapping(RdfReg.SCHEMAORG_VISUAL_ARTWORK, RdfRegEdm.ProvidedCHO, RdfReg.ORE_AGGREGATION);
		spec.setRootResourceTypeMapping(RdfReg.SCHEMAORG_PAINTING, RdfRegEdm.ProvidedCHO, RdfReg.ORE_AGGREGATION);
		spec.setRootResourceTypeMapping(RdfReg.SCHEMAORG_BOOK, RdfRegEdm.ProvidedCHO, RdfReg.ORE_AGGREGATION);
		spec.setRootResourceTypeMapping(RdfReg.SCHEMAORG_IMAGE_OBJECT, RdfRegEdm.ProvidedCHO, RdfReg.ORE_AGGREGATION);
		spec.setRootResourceTypeMapping(RdfReg.SCHEMAORG_NEWSPAPER, RdfRegEdm.ProvidedCHO, RdfReg.ORE_AGGREGATION);
		spec.setRootResourceTypeMapping(RdfReg.SCHEMAORG_PERIODICAL, RdfRegEdm.ProvidedCHO, RdfReg.ORE_AGGREGATION);
		spec.setRootResourceTypeMapping(RdfReg.SCHEMAORG_PHOTOGRAPH, RdfRegEdm.ProvidedCHO, RdfReg.ORE_AGGREGATION);
		spec.setRootResourceTypeMapping(RdfReg.SCHEMAORG_CREATIVE_WORK_SERIES, RdfRegEdm.ProvidedCHO, RdfReg.ORE_AGGREGATION);
		spec.setRootResourceTypeMapping(RdfReg.SCHEMAORG_SCULPTURE, RdfRegEdm.ProvidedCHO, RdfReg.ORE_AGGREGATION);
		
//		Article
//		Blog 
//		Book
//		Clip
//		Comment
//		Conversation
//		Course
//		CreativeWorkSeason
//		CreativeWorkSeries
//		DataCatalog
//		Dataset
//		DigitalDocument
//		Episode
//		Game
//		Map
//		MediaObject
//		Menu
//		MenuSection
//		Message
//		Movie
//		MusicComposition
//		MusicPlaylist
//		MusicRecording
//		Painting
//		Photograph
//		PublicationIssue
//		PublicationVolume
//		Question
//		Recipe
//		Review
//		Sculpture
//		Series
//		SoftwareApplication
//		SoftwareSourceCode
//		TVSeason
//		TVSeries
//		VisualArtwork
//		WebPage
//		WebPageElement
//		WebSite
		
		spec.setTypeMapping(RdfReg.SCHEMAORG_THING, RdfReg.SKOS_CONCEPT);
		spec.setTypeMapping(RdfReg.SCHEMAORG_ORGANIZATION, RdfReg.FOAF_ORGANIZATION);
		spec.setTypeMapping(RdfReg.SCHEMAORG_PERSON, RdfRegEdm.Agent);
		spec.setTypeMapping(RdfReg.SCHEMAORG_AUDIO_OBJECT, RdfRegEdm.WebResource);
		spec.setTypeMapping(RdfReg.SCHEMAORG_IMAGE_OBJECT, RdfRegEdm.WebResource);
		spec.setTypeMapping(RdfReg.SCHEMAORG_WEB_PAGE, RdfRegEdm.WebResource);
		spec.setTypeMapping(RdfReg.SCHEMAORG_MEDIA_OBJECT, RdfRegEdm.WebResource);
		spec.setTypeMapping(RdfReg.SCHEMAORG_PLACE, RdfRegEdm.Place);
		spec.setTypeMapping(RdfReg.SCHEMAORG_POSTAL_ADDRESS, RdfReg.VCARD_ADDRESS);
		
		ResourceTypeConversionSpecification pchoMapping = spec.getTypePropertiesMapping(RdfRegEdm.ProvidedCHO);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_ABOUT, RdfReg.DC_SUBJECT);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_KEYWORDS, RdfReg.DC_SUBJECT);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_IS_PART_OF, RdfReg.DCTERMS_IS_PART_OF);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_HAS_PART, RdfReg.DCTERMS_HAS_PART);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_NAME, RdfReg.DC_TITLE);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_IN_LANGUAGE, RdfReg.DC_LANGUAGE);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_DATE_CREATED, RdfReg.DCTERMS_CREATED);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_DESCRIPTION, RdfReg.DC_DESCRIPTION);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_CONTENT_LOCATION, RdfReg.DC_SUBJECT);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_CREATOR, RdfReg.DC_CREATOR);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_AUTHOR, RdfReg.DC_CREATOR);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_CONTRIBUTOR, RdfReg.DC_CONTRIBUTOR);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_PUBLISHER, RdfReg.DCTERMS_PUBLISHER);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_MATERIAL, RdfReg.DCTERMS_MEDIUM);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_SAME_AS, RdfReg.OWL_SAME_AS);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_LOCATION_CREATED, RdfReg.DCTERMS_SPATIAL);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_EXAMPLE_OF_WORK, RdfRegEdm.realizes);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_MAIN_ENTITY_OF_PAGE, RdfReg.DCTERMS_TABLE_OF_CONTENTS);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_COPYRIGHT_HOLDER, RdfReg.DC_RIGHTS);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_GENRE, RdfReg.DC_TYPE);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_LICENSE, RdfReg.DC_RIGHTS);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_HEIGHT, RdfReg.DCTERMS_EXTENT);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_WIDTH, RdfReg.DCTERMS_EXTENT);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_SAME_AS, RdfReg.OWL_SAME_AS);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_ART_MEDIUM, RdfReg.DC_DESCRIPTION);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_ARTWORK_SURFACE, RdfReg.DCTERMS_MEDIUM);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_ARTFORM, RdfReg.DC_TYPE);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_SPATIAL_COVERAGE, RdfRegEdm.currentLocation);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_LOCATION, RdfReg.DCTERMS_SPATIAL);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_TEMPORAL_COVERAGE, RdfReg.DCTERMS_TEMPORAL_COVERAGE);
		pchoMapping.putPropertyMappingFromReferencedResource(RdfReg.SCHEMAORG_HEIGHT, RdfReg.SCHEMAORG_DISTANCE, RdfReg.SCHEMAORG_NAME, RdfReg.DCTERMS_EXTENT);
		pchoMapping.putPropertyMappingFromReferencedResource(RdfReg.SCHEMAORG_WIDTH, RdfReg.SCHEMAORG_DISTANCE, RdfReg.SCHEMAORG_NAME, RdfReg.DCTERMS_EXTENT);
		pchoMapping.putPropertyMapping(RdfRegRdf.type, RdfRegEdm.hasType);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_ADDITIONAL_TYPE, RdfRegEdm.hasType);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_IDENTIFIER, RdfReg.DC_IDENTIFIER, true);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_NUMBER_OF_PAGES, RdfReg.DCTERMS_EXTENT);		
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_PAGINATION, RdfReg.DC_DESCRIPTION);		
		DerivedPropertyConversionSpecification edmTypeSpec = new DerivedPropertyConversionSpecification(RdfRegEdm.type);
		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_CREATIVE_WORK.getURI(), "TEXT");
		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_VISUAL_ARTWORK.getURI(), "IMAGE");
		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_PAINTING.getURI(), "IMAGE");
		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_PHOTOGRAPH.getURI(), "IMAGE");
		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_WEB_PAGE.getURI(), "TEXT");
		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_MEDIA_OBJECT.getURI(), "IMAGE");
		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_BOOK.getURI(), "TEXT");
		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_NEWSPAPER.getURI(), "TEXT");
		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_PERIODICAL.getURI(), "TEXT");
		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_IMAGE_OBJECT.getURI(), "IMAGE");
		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_CREATIVE_WORK_SERIES.getURI(), "IMAGE");
		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_SCULPTURE.getURI(), "IMAGE");
//		edmTypeSpec.putUriMapping("", "VIDEO");
//		edmTypeSpec.putUriMapping("", "3D");
		pchoMapping.putDerivedProperty(RdfRegRdf.type, edmTypeSpec);
		 
		ResourceTypeConversionSpecification aggregationMapping = spec.getTypePropertiesMapping(RdfReg.ORE_AGGREGATION);
		aggregationMapping.putPropertyMapping(RdfReg.SCHEMAORG_PROVIDER, RdfRegEdm.provider);
		aggregationMapping.putPropertyMapping(RdfReg.SCHEMAORG_ASSOCIATED_MEDIA, RdfRegEdm.isShownBy);
		aggregationMapping.putPropertyMapping(RdfReg.SCHEMAORG_AUDIO, RdfRegEdm.isShownBy);
		aggregationMapping.putPropertyMapping(RdfReg.SCHEMAORG_URL, RdfRegEdm.isShownAt);
		aggregationMapping.putPropertyMapping(RdfReg.SCHEMAORG_IMAGE, RdfRegEdm.isShownBy);
		aggregationMapping.putPropertyMapping(RdfReg.SCHEMAORG_THUMBNAIL_URL, RdfRegEdm.object);
		// next one is commented because it is not supported in the rdfConverter at this time
		//		aggregationMapping.putPropertyMappingFromReferencedResource(RdfReg.SCHEMAORG_IMAGE, RdfReg.SCHEMAORG_IMAGE_OBJECT, RdfReg.SCHEMAORG_CONTENT_URL, RdfRegEdm.HAS_VIEW);
//		ResourceTypeConversionSpecification aggregationMapping = spec.getTypeMapping(RdfRegEdm.AGENT);

		ResourceTypeConversionSpecification webResourceMapping = spec.getTypePropertiesMapping(RdfRegEdm.WebResource);
		webResourceMapping.putPropertyMapping(RdfReg.SCHEMAORG_ENCODING_FORMAT, RdfReg.DC_FORMAT);
		webResourceMapping.putPropertyMapping(RdfReg.SCHEMAORG_NAME, RdfReg.DC_DESCRIPTION);
		webResourceMapping.putPropertyMapping(RdfReg.SCHEMAORG_DESCRIPTION, RdfReg.DC_DESCRIPTION);
		webResourceMapping.putPropertyMapping(RdfReg.SCHEMAORG_HEIGHT, RdfReg.DCTERMS_EXTENT);
		webResourceMapping.putPropertyMapping(RdfReg.SCHEMAORG_WIDTH, RdfReg.DCTERMS_EXTENT);
		webResourceMapping.putPropertyMapping(RdfReg.SCHEMAORG_FILE_FORMAT, RdfReg.DC_FORMAT);
		webResourceMapping.addPropertyMappingToUri(RdfReg.SCHEMAORG_CONTENT_URL );
		webResourceMapping.putPropertyMapping(RdfRegRdf.type, RdfReg.DC_TYPE);

		ResourceTypeConversionSpecification organizationMapping = spec.getTypePropertiesMapping(RdfReg.FOAF_ORGANIZATION);
		organizationMapping.putPropertyMapping(RdfReg.SCHEMAORG_NAME, RdfReg.SKOS_PREF_LABEL);
		organizationMapping.putPropertyMapping(RdfReg.SCHEMAORG_ALTERNATE_NAME, RdfReg.SKOS_ALT_LABEL);
		organizationMapping.putPropertyMapping(RdfReg.SCHEMAORG_DESCRIPTION, RdfReg.SKOS_NOTE);
		organizationMapping.putPropertyMapping(RdfReg.SCHEMAORG_SAME_AS, RdfReg.OWL_SAME_AS);
		
		ResourceTypeConversionSpecification agentMapping = spec.getTypePropertiesMapping(RdfRegEdm.Agent);
		agentMapping.putPropertyMapping(RdfReg.SCHEMAORG_NAME, RdfReg.SKOS_PREF_LABEL);
		agentMapping.putPropertyMapping(RdfReg.SCHEMAORG_ALTERNATE_NAME, RdfReg.SKOS_ALT_LABEL);
		agentMapping.putPropertyMapping(RdfReg.SCHEMAORG_SAME_AS, RdfReg.OWL_SAME_AS);
		agentMapping.putPropertyMapping(RdfReg.SCHEMAORG_JOB_TITLE, RdfReg.RDAGR2_PROFESSION_OR_OCCUPATION);
		agentMapping.putPropertyMapping(RdfReg.SCHEMAORG_BIRTH_DATE, RdfReg.RDAGR2_BIRTH_DATE);
		agentMapping.putPropertyMapping(RdfReg.SCHEMAORG_DEATH_DATE, RdfReg.RDAGR2_DEATH_DATE);
		agentMapping.putPropertyMapping(RdfReg.SCHEMAORG_GIVEN_NAME, RdfReg.SKOS_PREF_LABEL);
		agentMapping.putPropertyMerge(RdfReg.SCHEMAORG_GIVEN_NAME, RdfReg.SCHEMAORG_FAMILY_NAME);
		
		ResourceTypeConversionSpecification conceptMapping = spec.getTypePropertiesMapping(RdfReg.SKOS_CONCEPT);
		conceptMapping.putPropertyMapping(RdfReg.SCHEMAORG_NAME, RdfReg.SKOS_PREF_LABEL);
		conceptMapping.putPropertyMapping(RdfReg.SCHEMAORG_ALTERNATE_NAME, RdfReg.SKOS_ALT_LABEL);
		conceptMapping.putPropertyMapping(RdfReg.SCHEMAORG_SAME_AS, RdfReg.OWL_SAME_AS);
		
		
		ResourceTypeConversionSpecification placeMapping = spec.getTypePropertiesMapping(RdfRegEdm.Place);
		placeMapping.putPropertyMapping(RdfReg.SCHEMAORG_NAME, RdfReg.SKOS_PREF_LABEL);
		placeMapping.putPropertyMapping(RdfReg.SCHEMAORG_ALTERNATE_NAME, RdfReg.SKOS_ALT_LABEL);
		placeMapping.putPropertyMapping(RdfReg.SCHEMAORG_SAME_AS, RdfReg.OWL_SAME_AS);
		placeMapping.putPropertyMappingFromReferencedResource(RdfReg.SCHEMAORG_GEO, RdfReg.SCHEMAORG_GEO_COORDINATES, RdfReg.SCHEMAORG_LATITUDE, RdfReg.WGS84_LAT);
		placeMapping.putPropertyMappingFromReferencedResource(RdfReg.SCHEMAORG_GEO, RdfReg.SCHEMAORG_GEO_COORDINATES, RdfReg.SCHEMAORG_LONGITUDE, RdfReg.WGS84_lONG);
//		placeMapping.putPropertyMappingFromReferencedResource(RdfReg.SCHEMAORG_ADDRESS, RdfReg.SCHEMAORG_POSTAL_ADDRESS, RdfReg.SCHEMAORG_ADDRESS_REGION, RdfReg.WGS84_lONG);

		ResourceTypeConversionSpecification addressMapping = spec.getTypePropertiesMapping(RdfReg.VCARD_ADDRESS);
		addressMapping.putPropertyMapping(RdfReg.SCHEMAORG_ADDRESS_REGION, RdfReg.VCARD_REGION);
		
		spec.setFilterOfReferencedResource(new FilterOfReferencedResource() {
			public boolean filterOut(Statement reference) {
				return reference.getPredicate().equals(RdfReg.SCHEMAORG_HAS_PART) ||
						reference.getPredicate().equals(RdfReg.SCHEMAORG_IS_PART_OF);
			}
		});
		
		spec.setConversionHandler(new EdmTypePreferedValueHandler());
		
	}

}
