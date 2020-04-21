package inescid.dataaggregation.dataset.convert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import inescid.dataaggregation.data.RdfReg;
import inescid.dataaggregation.data.RegEdm;
import inescid.dataaggregation.data.RegOwl;
import inescid.dataaggregation.data.RegRdf;
import inescid.dataaggregation.data.RegSchemaorg;
import inescid.dataaggregation.data.RegSkos;
import inescid.dataaggregation.dataset.convert.FilterOfReferencedResource;

public class SchemaOrgToEdmClassMappings extends ClassMappings {

	public SchemaOrgToEdmClassMappings() {
		for(Resource r: RegSchemaorg.creativeWorkClasses) {
			putClassMapping(r.getURI(), RegEdm.ProvidedCHO.getURI());
		}

		putClassMapping(RegSchemaorg.Thing.getURI(), RegSkos.Concept.getURI());
		putClassMapping(RegSchemaorg.Organization.getURI(), RegEdm.Agent.getURI());
		putClassMapping(RegSchemaorg.Person.getURI(), RegEdm.Agent.getURI());
		putClassMapping(RegSchemaorg.AudioObject.getURI(), RegEdm.WebResource.getURI());
		putClassMapping(RegSchemaorg.ImageObject.getURI(), RegEdm.WebResource.getURI());
		putClassMapping(RegSchemaorg.WebPage.getURI(), RegEdm.WebResource.getURI());
		putClassMapping(RegSchemaorg.MediaObject.getURI(), RegEdm.WebResource.getURI());
		putClassMapping(RegSchemaorg.Place.getURI(), RegEdm.Place.getURI());
		putClassMapping(RegSchemaorg.PostalAddress.getURI(), RdfReg.VCARD_ADDRESS.getURI());
		
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.about.getURI(), RdfReg.DC_SUBJECT.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.keywords.getURI(), RdfReg.DC_SUBJECT.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.contentLocation.getURI(), RdfReg.DC_SUBJECT.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.isPartOf.getURI(), RdfReg.DCTERMS_IS_PART_OF.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.hasPart.getURI(), RdfReg.DCTERMS_HAS_PART.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.name.getURI(), RdfReg.DC_TITLE.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.name.getURI(), RdfReg.DC_TITLE.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.inLanguage.getURI(), RegEdm.language.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.dateCreated.getURI(), RdfReg.DCTERMS_CREATED.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.description.getURI(), RdfReg.DC_DESCRIPTION.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.creator.getURI(), RdfReg.DC_CREATOR.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.author.getURI(), RdfReg.DC_CREATOR.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.contributor.getURI(), RdfReg.DC_CONTRIBUTOR.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.publisher.getURI(), RdfReg.DCTERMS_PUBLISHER.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.material.getURI(), RdfReg.DCTERMS_MEDIUM.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.sameAs.getURI(), RegOwl.sameAs.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.locationCreated.getURI(), RdfReg.DCTERMS_SPATIAL.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.exampleOfWork.getURI(), RegEdm.realizes.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.mainEntityOfPage.getURI(), RdfReg.DC_SUBJECT.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.copyrightHolder.getURI(), RdfReg.DC_RIGHTS.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.genre.getURI(), RdfReg.DC_TYPE.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.license.getURI(), RdfReg.DC_RIGHTS.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.height.getURI(), RdfReg.DCTERMS_EXTENT.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.width.getURI(), RdfReg.DCTERMS_EXTENT.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.artMedium.getURI(), RdfReg.DC_DESCRIPTION.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.artworkSurface.getURI(), RdfReg.DCTERMS_MEDIUM.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.artform.getURI(), RdfReg.DC_TYPE.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.spatialCoverage.getURI(), RegEdm.currentLocation.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.spatial.getURI(), RdfReg.DCTERMS_SPATIAL.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.temporalCoverage.getURI(), RdfReg.DCTERMS_TEMPORAL_COVERAGE.getURI());
//		pchoMapping.putPropertyMappingFromReferencedResource(RdfReg.SCHEMAORG_HEIGHT, RdfReg.SCHEMAORG_DISTANCE, RdfReg.SCHEMAORG_NAME, RdfReg.DCTERMS_EXTENT);
//		pchoMapping.putPropertyMappingFromReferencedResource(RdfReg.SCHEMAORG_WIDTH, RdfReg.SCHEMAORG_DISTANCE, RdfReg.SCHEMAORG_NAME, RdfReg.DCTERMS_EXTENT);
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.additionalType.getURI(),  RegEdm.hasType.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.identifier.getURI(), RdfReg.DC_IDENTIFIER.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.numberOfPages.getURI(), RdfReg.DCTERMS_EXTENT.getURI());
		put(RegEdm.ProvidedCHO.getURI(), RegSchemaorg.pagination.getURI(), RdfReg.DC_DESCRIPTION.getURI());
//		DerivedPropertyConversionSpecification edmTypeSpec = new DerivedPropertyConversionSpecification(RegEdm.type);
//		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_CREATIVE_WORK.getURI(), "TEXT");
//		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_VISUAL_ARTWORK.getURI(), "IMAGE");
//		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_PAINTING.getURI(), "IMAGE");
//		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_PHOTOGRAPH.getURI(), "IMAGE");
//		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_WEB_PAGE.getURI(), "TEXT");
//		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_MEDIA_OBJECT.getURI(), "IMAGE");
//		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_BOOK.getURI(), "TEXT");
//		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_NEWSPAPER.getURI(), "TEXT");
//		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_PERIODICAL.getURI(), "TEXT");
//		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_IMAGE_OBJECT.getURI(), "IMAGE");
//		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_CREATIVE_WORK_SERIES.getURI(), "IMAGE");
//		edmTypeSpec.putUriMapping(RdfReg.SCHEMAORG_SCULPTURE.getURI(), "IMAGE");
//		edmTypeSpec.putUriMapping("", "VIDEO");
//		edmTypeSpec.putUriMapping("", "3D");
//		pchoMapping.putDerivedProperty(RegRdf.type, edmTypeSpec);
		 
//		ResourceTypeConversionSpecification aggregationMapping = spec.getTypePropertiesMapping(RdfReg.ORE_AGGREGATION);
//		aggregationMapping.putPropertyMapping(RdfReg.SCHEMAORG_PROVIDER, RegEdm.provider);
//		aggregationMapping.putPropertyMapping(RdfReg.SCHEMAORG_ASSOCIATED_MEDIA, RegEdm.isShownBy);
//		aggregationMapping.putPropertyMapping(RdfReg.SCHEMAORG_AUDIO, RegEdm.isShownBy);
//		aggregationMapping.putPropertyMapping(RdfReg.SCHEMAORG_URL, RegEdm.isShownAt);
//		aggregationMapping.putPropertyMapping(RdfReg.SCHEMAORG_IMAGE, RegEdm.isShownBy);
//		aggregationMapping.putPropertyMapping(RdfReg.SCHEMAORG_THUMBNAIL_URL, RegEdm.object);
		// next one is commented because it is not supported in the rdfConverter at this time
		//		aggregationMapping.putPropertyMappingFromReferencedResource(RdfReg.SCHEMAORG_IMAGE, RdfReg.SCHEMAORG_IMAGE_OBJECT, RdfReg.SCHEMAORG_CONTENT_URL, RdfRegEdm.HAS_VIEW);


		put(RegEdm.WebResource.getURI(), RegSchemaorg.encodingFormat.getURI(), RdfReg.DC_FORMAT.getURI());
		put(RegEdm.WebResource.getURI(), RegSchemaorg.name.getURI(), RdfReg.DC_DESCRIPTION.getURI());
		put(RegEdm.WebResource.getURI(), RegSchemaorg.description.getURI(), RdfReg.DC_DESCRIPTION.getURI());
		put(RegEdm.WebResource.getURI(), RegSchemaorg.height.getURI(), RdfReg.DCTERMS_EXTENT.getURI());
		put(RegEdm.WebResource.getURI(), RegSchemaorg.width.getURI(), RdfReg.DCTERMS_EXTENT.getURI());
		put(RegEdm.WebResource.getURI(), RegSchemaorg.fileFormat.getURI(), RdfReg.DC_FORMAT.getURI());
//		put(RegEdm.WebResource.getURI(), RegSchemaorg.contentUrl.getURI(), RegRdf.about.getURI());
		put(RegEdm.WebResource.getURI(), RegSchemaorg.contentUrl.getURI(), RegOwl.sameAs.getURI());
//		webResourceMapping.putPropertyMapping(RegRdf.type, RdfReg.DC_TYPE);

		put(RegEdm.Agent.getURI(), RegSchemaorg.name.getURI(), RegSkos.prefLabel.getURI());
		put(RegEdm.Agent.getURI(), RegSchemaorg.alternateName.getURI(), RegSkos.altLabel.getURI());
		put(RegEdm.Agent.getURI(), RegSchemaorg.description.getURI(), RegSkos.note.getURI());
		put(RegEdm.Agent.getURI(), RegSchemaorg.sameAs.getURI(), RegOwl.sameAs.getURI());
		put(RegEdm.Agent.getURI(), RegSchemaorg.title.getURI(), RdfReg.RDAGR2_PROFESSION_OR_OCCUPATION.getURI());
//		put(RegEdm.Agent.getURI(), RegSchemaorg.jobTitle.getURI(), RdfReg.RDAGR2_PROFESSION_OR_OCCUPATION.getURI());
		put(RegEdm.Agent.getURI(), RegSchemaorg.birthDate.getURI(), RdfReg.RDAGR2_BIRTH_DATE.getURI());
		put(RegEdm.Agent.getURI(), RegSchemaorg.deathDate.getURI(), RdfReg.RDAGR2_DEATH_DATE.getURI());
		put(RegEdm.Agent.getURI(), RegSchemaorg.givenName.getURI(), RegSkos.altLabel.getURI());
		put(RegEdm.Agent.getURI(), RegSchemaorg.familyName.getURI(), RegSkos.altLabel.getURI());

		put(RegSkos.Concept.getURI(), RegSchemaorg.name.getURI(), RegSkos.prefLabel.getURI());
		put(RegSkos.Concept.getURI(), RegSchemaorg.alternateName.getURI(), RegSkos.altLabel.getURI());
		put(RegSkos.Concept.getURI(), RegSchemaorg.description.getURI(), RegSkos.note.getURI());
		put(RegSkos.Concept.getURI(), RegSchemaorg.sameAs.getURI(), RegOwl.sameAs.getURI());
		
		put(RegEdm.Place.getURI(), RegSchemaorg.name.getURI(), RegSkos.prefLabel.getURI());
		put(RegEdm.Place.getURI(), RegSchemaorg.alternateName.getURI(), RegSkos.altLabel.getURI());
		put(RegEdm.Place.getURI(), RegSchemaorg.description.getURI(), RegSkos.note.getURI());
		put(RegEdm.Place.getURI(), RegSchemaorg.sameAs.getURI(), RegOwl.sameAs.getURI());
		put(RegEdm.Place.getURI(), RegSchemaorg.latitude.getURI(), RdfReg.WGS84_LAT.getURI());
		put(RegEdm.Place.getURI(), RegSchemaorg.longitude.getURI(), RdfReg.WGS84_lONG.getURI());

//		ResourceTypeConversionSpecification addressMapping = spec.getTypePropertiesMapping(RdfReg.VCARD_ADDRESS);
//		addressMapping.putPropertyMapping(RdfReg.SCHEMAORG_ADDRESS_REGION, RdfReg.VCARD_REGION);
		
	}

}
