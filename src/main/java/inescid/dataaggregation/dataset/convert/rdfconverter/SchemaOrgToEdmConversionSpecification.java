package inescid.dataaggregation.dataset.convert.rdfconverter;

import java.util.Set;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import inescid.dataaggregation.dataset.convert.RdfReg;

public class SchemaOrgToEdmConversionSpecification {
	public static final RdfConversionSpecification spec;
	
//	public SchemaOrgToEdmConversionSpecification() {
	static {
		spec=new RdfConversionSpecification();
		
		spec.setRootResourceTypeMapping(RdfReg.SCHEMAORG_CREATIVE_WORK, RdfReg.EDM_PROVIDED_CHO, RdfReg.ORE_AGGREGATION);
		spec.setRootResourceTypeMapping(RdfReg.SCHEMAORG_VISUAL_ARTWORK, RdfReg.EDM_PROVIDED_CHO, RdfReg.ORE_AGGREGATION);
		spec.setRootResourceTypeMapping(RdfReg.SCHEMAORG_BOOK, RdfReg.EDM_PROVIDED_CHO, RdfReg.ORE_AGGREGATION);
		spec.setRootResourceTypeMapping(RdfReg.SCHEMAORG_IMAGE_OBJECT, RdfReg.EDM_PROVIDED_CHO, RdfReg.ORE_AGGREGATION);
		
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
		spec.setTypeMapping(RdfReg.SCHEMAORG_PERSON, RdfReg.EDM_AGENT);
		spec.setTypeMapping(RdfReg.SCHEMAORG_AUDIO_OBJECT, RdfReg.EDM_WEB_RESOURCE);
		spec.setTypeMapping(RdfReg.SCHEMAORG_IMAGE_OBJECT, RdfReg.EDM_WEB_RESOURCE);
		spec.setTypeMapping(RdfReg.SCHEMAORG_WEB_PAGE, RdfReg.EDM_WEB_RESOURCE);
		spec.setTypeMapping(RdfReg.SCHEMAORG_MEDIA_OBJECT, RdfReg.EDM_WEB_RESOURCE);
		spec.setTypeMapping(RdfReg.SCHEMAORG_PLACE, RdfReg.EDM_PLACE);
		spec.setTypeMapping(RdfReg.SCHEMAORG_POSTAL_ADDRESS, RdfReg.VCARD_ADDRESS);
		
		ResourceTypeConversionSpecification pchoMapping = spec.getTypePropertiesMapping(RdfReg.EDM_PROVIDED_CHO);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_ABOUT, RdfReg.DC_SUBJECT);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_KEYWORDS, RdfReg.DC_SUBJECT);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_IS_PART_OF, RdfReg.DCTERMS_IS_PART_OF);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_NAME, RdfReg.DC_TITLE);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_IN_LANGUAGE, RdfReg.DC_LANGUAGE);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_DATE_CREATED, RdfReg.DCTERMS_CREATED);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_DESCRIPTION, RdfReg.DC_DESCRIPTION);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_CONTENT_LOCATION, RdfReg.DC_SUBJECT);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_CREATOR, RdfReg.DC_CREATOR);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_AUTHOR, RdfReg.DC_CREATOR);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_CONTRIBUTOR, RdfReg.DC_CONTRIBUTOR);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_SAME_AS, RdfReg.OWL_SAME_AS);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_LOCATION_CREATED, RdfReg.DC_DESCRIPTION);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_EXAMPLE_OF_WORK, RdfReg.EDM_REALIZES);
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
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_SPATIAL_COVERAGE, RdfReg.EDM_CURRENT_LOCATION);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_TEMPORAL_COVERAGE, RdfReg.DCTERMS_TEMPORAL_COVERAGE);
		pchoMapping.putPropertyMappingFromReferencedResource(RdfReg.SCHEMAORG_HEIGHT, RdfReg.SCHEMAORG_DISTANCE, RdfReg.SCHEMAORG_NAME, RdfReg.DCTERMS_EXTENT);
		pchoMapping.putPropertyMappingFromReferencedResource(RdfReg.SCHEMAORG_WIDTH, RdfReg.SCHEMAORG_DISTANCE, RdfReg.SCHEMAORG_NAME, RdfReg.DCTERMS_EXTENT);
		pchoMapping.putPropertyMapping(RdfReg.RDF_TYPE, RdfReg.EDM_HAS_TYPE);
		pchoMapping.putPropertyMapping(RdfReg.SCHEMAORG_ADDITIONAL_TYPE, RdfReg.EDM_HAS_TYPE);
		 
		ResourceTypeConversionSpecification aggregationMapping = spec.getTypePropertiesMapping(RdfReg.ORE_AGGREGATION);
		aggregationMapping.putPropertyMapping(RdfReg.SCHEMAORG_PROVIDER, RdfReg.EDM_PROVIDER);
		aggregationMapping.putPropertyMapping(RdfReg.SCHEMAORG_ASSOCIATED_MEDIA, RdfReg.EDM_IS_SHOWN_BY);
		aggregationMapping.putPropertyMapping(RdfReg.SCHEMAORG_AUDIO, RdfReg.EDM_IS_SHOWN_BY);
		aggregationMapping.putPropertyMapping(RdfReg.SCHEMAORG_URL, RdfReg.EDM_IS_SHOWN_AT);
		aggregationMapping.putPropertyMapping(RdfReg.SCHEMAORG_IMAGE, RdfReg.EDM_IS_SHOWN_BY);
		aggregationMapping.putPropertyMapping(RdfReg.SCHEMAORG_THUMBNAIL_URL, RdfReg.EDM_OBJECT);
//		ResourceTypeConversionSpecification aggregationMapping = spec.getTypeMapping(RdfReg.EDM_AGENT);
		DerivedPropertyConversionSpecification edmTypeSpec = new DerivedPropertyConversionSpecification(RdfReg.EDM_TYPE);
		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_CREATIVE_WORK.getURI(), "TEXT");
		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_VISUAL_ARTWORK.getURI(), "TEXT");
		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_WEB_PAGE.getURI(), "TEXT");
		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_MEDIA_OBJECT.getURI(), "TEXT");
		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_BOOK.getURI(), "TEXT");
		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_IMAGE_OBJECT.getURI(), "IMAGE");
		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_AUDIO_OBJECT.getURI(), "SOUND");
//		edmTypeSpec.putUriMapping("", "VIDEO");
//		edmTypeSpec.putUriMapping("", "3D");
		aggregationMapping.putDerivedProperty(RdfReg.RDF_TYPE, edmTypeSpec);

		ResourceTypeConversionSpecification webResourceMapping = spec.getTypePropertiesMapping(RdfReg.EDM_WEB_RESOURCE);
		webResourceMapping.putPropertyMapping(RdfReg.SCHEMAORG_ENCODING_FORMAT, RdfReg.DC_FORMAT);
		webResourceMapping.putPropertyMapping(RdfReg.SCHEMAORG_NAME, RdfReg.DC_DESCRIPTION);
		webResourceMapping.putPropertyMapping(RdfReg.SCHEMAORG_DESCRIPTION, RdfReg.DC_DESCRIPTION);
		webResourceMapping.putPropertyMapping(RdfReg.SCHEMAORG_HEIGHT, RdfReg.DCTERMS_EXTENT);
		webResourceMapping.putPropertyMapping(RdfReg.SCHEMAORG_WIDTH, RdfReg.DCTERMS_EXTENT);
		webResourceMapping.putPropertyMapping(RdfReg.SCHEMAORG_FILE_FORMAT, RdfReg.DC_FORMAT);
		webResourceMapping.addPropertyMappingToUri(RdfReg.SCHEMAORG_CONTENT_URL );
		webResourceMapping.putPropertyMapping(RdfReg.RDF_TYPE, RdfReg.DC_TYPE);

		ResourceTypeConversionSpecification organizationMapping = spec.getTypePropertiesMapping(RdfReg.FOAF_ORGANIZATION);
		organizationMapping.putPropertyMapping(RdfReg.SCHEMAORG_NAME, RdfReg.SKOS_PREF_LABEL);
		organizationMapping.putPropertyMapping(RdfReg.SCHEMAORG_ALTERNATE_NAME, RdfReg.SKOS_ALT_LABEL);
		organizationMapping.putPropertyMapping(RdfReg.SCHEMAORG_DESCRIPTION, RdfReg.SKOS_NOTE);
		organizationMapping.putPropertyMapping(RdfReg.SCHEMAORG_SAME_AS, RdfReg.OWL_SAME_AS);
		
		ResourceTypeConversionSpecification agentMapping = spec.getTypePropertiesMapping(RdfReg.EDM_AGENT);
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
		
		
		ResourceTypeConversionSpecification placeMapping = spec.getTypePropertiesMapping(RdfReg.EDM_PLACE);
		placeMapping.putPropertyMapping(RdfReg.SCHEMAORG_NAME, RdfReg.SKOS_PREF_LABEL);
		placeMapping.putPropertyMapping(RdfReg.SCHEMAORG_ALTERNATE_NAME, RdfReg.SKOS_ALT_LABEL);
		placeMapping.putPropertyMapping(RdfReg.SCHEMAORG_SAME_AS, RdfReg.OWL_SAME_AS);
		placeMapping.putPropertyMappingFromReferencedResource(RdfReg.SCHEMAORG_GEO, RdfReg.SCHEMAORG_GEO_COORDINATES, RdfReg.SCHEMAORG_LATITUDE, RdfReg.WGS84_LAT);
		placeMapping.putPropertyMappingFromReferencedResource(RdfReg.SCHEMAORG_GEO, RdfReg.SCHEMAORG_GEO_COORDINATES, RdfReg.SCHEMAORG_LONGITUDE, RdfReg.WGS84_lONG);
//		placeMapping.putPropertyMappingFromReferencedResource(RdfReg.SCHEMAORG_ADDRESS, RdfReg.SCHEMAORG_POSTAL_ADDRESS, RdfReg.SCHEMAORG_ADDRESS_REGION, RdfReg.WGS84_lONG);

		ResourceTypeConversionSpecification addressMapping = spec.getTypePropertiesMapping(RdfReg.VCARD_ADDRESS);
		addressMapping.putPropertyMapping(RdfReg.SCHEMAORG_ADDRESS_REGION, RdfReg.VCARD_REGION);
	}

}
