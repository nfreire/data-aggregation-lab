package inescid.dataaggregation.dataset.convert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import inescid.dataaggregation.data.model.RdfReg;
import inescid.dataaggregation.data.model.Dc;
import inescid.dataaggregation.data.model.DcTerms;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;
import inescid.dataaggregation.data.model.Owl;
import inescid.dataaggregation.data.model.RdaGr2;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Schemaorg;
import inescid.dataaggregation.data.model.Skos;
import inescid.dataaggregation.data.model.Wgs84;
import inescid.dataaggregation.dataset.convert.FilterOfReferencedResource;

public class SchemaOrgToEdmClassMappings extends ClassMappings {

	public SchemaOrgToEdmClassMappings() {
		for(Resource r: Schemaorg.creativeWorkClasses) {
			putClassMapping(r.getURI(), Edm.ProvidedCHO.getURI());
		}

		putClassMapping(Schemaorg.Thing.getURI(), Skos.Concept.getURI());
		putClassMapping(Schemaorg.Organization.getURI(), Edm.Agent.getURI());
		putClassMapping(Schemaorg.Person.getURI(), Edm.Agent.getURI());
		putClassMapping(Schemaorg.AudioObject.getURI(), Edm.WebResource.getURI());
		putClassMapping(Schemaorg.ImageObject.getURI(), Edm.WebResource.getURI());
		putClassMapping(Schemaorg.WebPage.getURI(), Edm.WebResource.getURI());
		putClassMapping(Schemaorg.MediaObject.getURI(), Edm.WebResource.getURI());
		putClassMapping(Schemaorg.Place.getURI(), Edm.Place.getURI());
		putClassMapping(Schemaorg.PostalAddress.getURI(), RdfReg.VCARD_ADDRESS.getURI());
		
		put(Edm.ProvidedCHO.getURI(), Schemaorg.about.getURI(), Dc.subject.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.keywords.getURI(), Dc.subject.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.contentLocation.getURI(), Dc.subject.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.isPartOf.getURI(), DcTerms.isPartOf.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.hasPart.getURI(), DcTerms.hasPart.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.name.getURI(), Dc.title.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.name.getURI(), Dc.title.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.inLanguage.getURI(), Edm.language.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.dateCreated.getURI(), DcTerms.created.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.description.getURI(), Dc.description.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.creator.getURI(), Dc.creator.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.author.getURI(), Dc.creator.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.contributor.getURI(), Dc.contributor.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.publisher.getURI(), DcTerms.publisher.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.material.getURI(), DcTerms.medium.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.sameAs.getURI(), Owl.sameAs.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.locationCreated.getURI(), DcTerms.spatial.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.exampleOfWork.getURI(), Edm.realizes.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.mainEntityOfPage.getURI(), Dc.subject.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.copyrightHolder.getURI(), Dc.rights.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.genre.getURI(), Dc.type.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.license.getURI(), Dc.rights.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.height.getURI(), DcTerms.extent.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.width.getURI(), DcTerms.extent.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.artMedium.getURI(), Dc.description.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.artworkSurface.getURI(), DcTerms.medium.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.artform.getURI(), Dc.type.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.spatialCoverage.getURI(), Edm.currentLocation.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.spatial.getURI(), DcTerms.spatial.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.temporalCoverage.getURI(), DcTerms.temporal.getURI());
//		pchoMapping.putPropertyMappingFromReferencedResource(RdfReg.SCHEMAORG_HEIGHT, RdfReg.SCHEMAORG_DISTANCE, RdfReg.SCHEMAORG_NAME, DcTerms.EXTENT);
//		pchoMapping.putPropertyMappingFromReferencedResource(RdfReg.SCHEMAORG_WIDTH, RdfReg.SCHEMAORG_DISTANCE, RdfReg.SCHEMAORG_NAME, DcTerms.EXTENT);
		put(Edm.ProvidedCHO.getURI(), Schemaorg.additionalType.getURI(),  Edm.hasType.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.identifier.getURI(), Dc.identifier.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.numberOfPages.getURI(), DcTerms.extent.getURI());
		put(Edm.ProvidedCHO.getURI(), Schemaorg.pagination.getURI(), Dc.description.getURI());
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
		 
//		ResourceTypeConversionSpecification aggregationMapping = spec.getTypePropertiesMapping(Ore.Aggregation);
		// next one is commented because it is not supported in the rdfConverter at this time
		//		aggregationMapping.putPropertyMappingFromReferencedResource(RdfReg.SCHEMAORG_IMAGE, RdfReg.SCHEMAORG_IMAGE_OBJECT, RdfReg.SCHEMAORG_CONTENT_URL, RdfRegEdm.HAS_VIEW);

//		put(Ore.Aggregation.getURI(), RegSchemaorg.provider.getURI(), RegEdm.provider.getURI());
		put(Ore.Aggregation.getURI(), Schemaorg.provider.getURI(), Edm.dataProvider.getURI());
		put(Ore.Aggregation.getURI(), Schemaorg.associatedMedia.getURI(), Edm.isShownBy.getURI());
		put(Ore.Aggregation.getURI(), Schemaorg.audio.getURI(), Edm.isShownBy.getURI());
		put(Ore.Aggregation.getURI(), Schemaorg.url.getURI(), Edm.isShownAt.getURI());
		put(Ore.Aggregation.getURI(), Schemaorg.image.getURI(), Edm.isShownBy.getURI());
		put(Ore.Aggregation.getURI(), Schemaorg.thumbnailUrl.getURI(), Edm.object.getURI());

		put(Edm.WebResource.getURI(), Schemaorg.encodingFormat.getURI(), Dc.format.getURI());
		put(Edm.WebResource.getURI(), Schemaorg.name.getURI(), Dc.description.getURI());
		put(Edm.WebResource.getURI(), Schemaorg.description.getURI(), Dc.description.getURI());
		put(Edm.WebResource.getURI(), Schemaorg.height.getURI(), DcTerms.extent.getURI());
		put(Edm.WebResource.getURI(), Schemaorg.width.getURI(), DcTerms.extent.getURI());
		put(Edm.WebResource.getURI(), Schemaorg.fileFormat.getURI(), Dc.format.getURI());
//		put(RegEdm.WebResource.getURI(), RegSchemaorg.contentUrl.getURI(), RegRdf.about.getURI());
		put(Edm.WebResource.getURI(), Schemaorg.contentUrl.getURI(), Owl.sameAs.getURI());
//		webResourceMapping.putPropertyMapping(RegRdf.type, Dc.TYPE);

		put(Edm.Agent.getURI(), Schemaorg.name.getURI(), Skos.prefLabel.getURI());
		put(Edm.Agent.getURI(), Schemaorg.alternateName.getURI(), Skos.altLabel.getURI());
		put(Edm.Agent.getURI(), Schemaorg.description.getURI(), Skos.note.getURI());
		put(Edm.Agent.getURI(), Schemaorg.sameAs.getURI(), Owl.sameAs.getURI());
		put(Edm.Agent.getURI(), Schemaorg.title.getURI(), RdaGr2.professionOrOccupation.getURI());
//		put(RegEdm.Agent.getURI(), RegSchemaorg.jobTitle.getURI(), RdfReg.RDAGR2_PROFESSION_OR_OCCUPATION.getURI());
		put(Edm.Agent.getURI(), Schemaorg.birthDate.getURI(), RdaGr2.dateOfBirth.getURI());
		put(Edm.Agent.getURI(), Schemaorg.deathDate.getURI(), RdaGr2.dateOfDeath.getURI());
		put(Edm.Agent.getURI(), Schemaorg.givenName.getURI(), Skos.altLabel.getURI());
		put(Edm.Agent.getURI(), Schemaorg.familyName.getURI(), Skos.altLabel.getURI());

		put(Skos.Concept.getURI(), Schemaorg.name.getURI(), Skos.prefLabel.getURI());
		put(Skos.Concept.getURI(), Schemaorg.alternateName.getURI(), Skos.altLabel.getURI());
		put(Skos.Concept.getURI(), Schemaorg.description.getURI(), Skos.note.getURI());
		put(Skos.Concept.getURI(), Schemaorg.sameAs.getURI(), Owl.sameAs.getURI());
		
		put(Edm.Place.getURI(), Schemaorg.name.getURI(), Skos.prefLabel.getURI());
		put(Edm.Place.getURI(), Schemaorg.alternateName.getURI(), Skos.altLabel.getURI());
		put(Edm.Place.getURI(), Schemaorg.description.getURI(), Skos.note.getURI());
		put(Edm.Place.getURI(), Schemaorg.sameAs.getURI(), Owl.sameAs.getURI());
		put(Edm.Place.getURI(), Schemaorg.latitude.getURI(), Wgs84.lat.getURI());
		put(Edm.Place.getURI(), Schemaorg.longitude.getURI(), Wgs84.long_.getURI());

//		ResourceTypeConversionSpecification addressMapping = spec.getTypePropertiesMapping(RdfReg.VCARD_ADDRESS);
//		addressMapping.putPropertyMapping(RdfReg.SCHEMAORG_ADDRESS_REGION, RdfReg.VCARD_REGION);
		
	}

}
