package inescid.dataaggregation.dataset.convert.rdfconverter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import inescid.dataaggregation.data.model.RdfReg;
import inescid.dataaggregation.data.model.Schemaorg;
import inescid.dataaggregation.data.model.Skos;
import inescid.dataaggregation.data.model.Wgs84;
import inescid.dataaggregation.data.model.Dc;
import inescid.dataaggregation.data.model.DcTerms;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Foaf;
import inescid.dataaggregation.data.model.Ore;
import inescid.dataaggregation.data.model.Owl;
import inescid.dataaggregation.data.model.RdaGr2;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.dataset.convert.FilterOfReferencedResource;

public class SchemaOrgToEdmConversionSpecification {
	public static final RdfConversionSpecification spec;
	
	private static class EdmTypePreferedValueHandler implements ConversionHandler {
		private static Set<String> propertiesProcessed=new HashSet<String>() {{
			add(Edm.type.getURI());
		}};
		@Override
		public Set<String> propertiesProcessed() {
			return propertiesProcessed;
		}

		@Override
		public void handleConvertedResult(Resource source, Resource target) {
			Statement firstType=null;
			ArrayList<Statement> typeCnt=new ArrayList<Statement>();
			StmtIterator cwStms = target.listProperties(Edm.type);
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
		
		spec.setRootResourceTypeMapping(Schemaorg.CreativeWork, Edm.ProvidedCHO, Ore.Aggregation);
		spec.setRootResourceTypeMapping(Schemaorg.VisualArtwork, Edm.ProvidedCHO, Ore.Aggregation);
		spec.setRootResourceTypeMapping(Schemaorg.Painting, Edm.ProvidedCHO, Ore.Aggregation);
		spec.setRootResourceTypeMapping(Schemaorg.Book, Edm.ProvidedCHO, Ore.Aggregation);
		spec.setRootResourceTypeMapping(Schemaorg.ImageObject, Edm.ProvidedCHO, Ore.Aggregation);
		spec.setRootResourceTypeMapping(RdfReg.Schemaorg_NEWSPAPER, Edm.ProvidedCHO, Ore.Aggregation);
		spec.setRootResourceTypeMapping(RdfReg.Schemaorg_DRAWING, Edm.ProvidedCHO, Ore.Aggregation);
		spec.setRootResourceTypeMapping(RdfReg.Schemaorg_MANUSCRIPT, Edm.ProvidedCHO, Ore.Aggregation);
		spec.setRootResourceTypeMapping(RdfReg.Schemaorg_POSTER, Edm.ProvidedCHO, Ore.Aggregation);
		spec.setRootResourceTypeMapping(Schemaorg.Movie, Edm.ProvidedCHO, Ore.Aggregation);
		spec.setRootResourceTypeMapping(Schemaorg.TVEpisode, Edm.ProvidedCHO, Ore.Aggregation);
		spec.setRootResourceTypeMapping(Schemaorg.ScholarlyArticle, Edm.ProvidedCHO, Ore.Aggregation);
		spec.setRootResourceTypeMapping(Schemaorg.Periodical, Edm.ProvidedCHO, Ore.Aggregation);
		spec.setRootResourceTypeMapping(Schemaorg.Photograph, Edm.ProvidedCHO, Ore.Aggregation);
		spec.setRootResourceTypeMapping(Schemaorg.CreativeWorkSeries, Edm.ProvidedCHO, Ore.Aggregation);
		spec.setRootResourceTypeMapping(Schemaorg.Sculpture, Edm.ProvidedCHO, Ore.Aggregation);
		
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
		
		spec.setTypeMapping(Schemaorg.Thing, Skos.Concept);
		spec.setTypeMapping(Schemaorg.Organization, Foaf.Organization);
		spec.setTypeMapping(Schemaorg.Person, Edm.Agent);
		spec.setTypeMapping(Schemaorg.AudioObject, Edm.WebResource);
		spec.setTypeMapping(Schemaorg.ImageObject, Edm.WebResource);
		spec.setTypeMapping(Schemaorg.WebPage, Edm.WebResource);
		spec.setTypeMapping(Schemaorg.MediaObject, Edm.WebResource);
		spec.setTypeMapping(Schemaorg.Place, Edm.Place);
		spec.setTypeMapping(Schemaorg.PostalAddress, RdfReg.VCARD_ADDRESS);
		
		ResourceTypeConversionSpecification pchoMapping = spec.getTypePropertiesMapping(Edm.ProvidedCHO);
		pchoMapping.putPropertyMapping(Schemaorg.about, Dc.subject);
		pchoMapping.putPropertyMapping(Schemaorg.keywords, Dc.subject);
		pchoMapping.putPropertyMapping(Schemaorg.isPartOf, DcTerms.isPartOf);
		pchoMapping.putPropertyMapping(Schemaorg.hasPart, DcTerms.hasPart);
		pchoMapping.putPropertyMapping(Schemaorg.name, Dc.title);
		pchoMapping.putPropertyMapping(Schemaorg.inLanguage, Dc.language);
		pchoMapping.putPropertyMapping(Schemaorg.dateCreated, DcTerms.created);
		pchoMapping.putPropertyMapping(Schemaorg.datePublished, DcTerms.issued);
		pchoMapping.putPropertyMapping(Schemaorg.description, Dc.description);
		pchoMapping.putPropertyMapping(Schemaorg.contentLocation, Dc.subject);
		pchoMapping.putPropertyMapping(Schemaorg.creator, Dc.creator);
		pchoMapping.putPropertyMapping(Schemaorg.author, Dc.creator);
		pchoMapping.putPropertyMapping(Schemaorg.contributor, Dc.contributor);
		pchoMapping.putPropertyMapping(Schemaorg.editor, Dc.contributor);
		pchoMapping.putPropertyMapping(Schemaorg.illustrator, Dc.contributor);
		pchoMapping.putPropertyMapping(Schemaorg.director, Dc.contributor);
		pchoMapping.putPropertyMapping(Schemaorg.publisher, DcTerms.publisher);
		pchoMapping.putPropertyMapping(Schemaorg.material, DcTerms.medium);
		pchoMapping.putPropertyMapping(Schemaorg.sameAs, Owl.sameAs);
		pchoMapping.putPropertyMapping(Schemaorg.locationCreated, DcTerms.spatial);
		pchoMapping.putPropertyMapping(Schemaorg.exampleOfWork, Edm.realizes);
		pchoMapping.putPropertyMapping(Schemaorg.mainEntityOfPage, DcTerms.tableOfContents);
		pchoMapping.putPropertyMapping(Schemaorg.copyrightHolder, Dc.rights);
		pchoMapping.putPropertyMapping(Schemaorg.genre, Dc.type);
		pchoMapping.putPropertyMapping(Schemaorg.license, Dc.rights);
		pchoMapping.putPropertyMapping(Schemaorg.height, DcTerms.extent);
		pchoMapping.putPropertyMapping(Schemaorg.width, DcTerms.extent);
		pchoMapping.putPropertyMapping(Schemaorg.sameAs, Owl.sameAs);
		pchoMapping.putPropertyMapping(Schemaorg.artMedium, Dc.description);
		pchoMapping.putPropertyMapping(Schemaorg.artworkSurface, DcTerms.medium);
		pchoMapping.putPropertyMapping(Schemaorg.artform, Dc.type);
		pchoMapping.putPropertyMapping(Schemaorg.spatialCoverage, Edm.currentLocation);
		pchoMapping.putPropertyMapping(Schemaorg.spatial, DcTerms.spatial);
		pchoMapping.putPropertyMapping(Schemaorg.temporalCoverage, DcTerms.temporal);
		pchoMapping.putPropertyMappingFromReferencedResource(Schemaorg.height, Schemaorg.Distance, Schemaorg.name, DcTerms.extent);
		pchoMapping.putPropertyMappingFromReferencedResource(Schemaorg.width, Schemaorg.Distance, Schemaorg.name, DcTerms.extent);
		pchoMapping.putPropertyMapping(Rdf.type, Edm.hasType);
		pchoMapping.putPropertyMapping(Schemaorg.additionalType, Edm.hasType);
		pchoMapping.putPropertyMapping(Schemaorg.identifier, Dc.identifier, true);
		pchoMapping.putPropertyMapping(Schemaorg.numberOfPages, DcTerms.extent);		
		pchoMapping.putPropertyMapping(Schemaorg.pagination, Dc.description);		
		DerivedPropertyConversionSpecification edmTypeSpec = new DerivedPropertyConversionSpecification(Edm.type);
		edmTypeSpec.putUriMapping(Schemaorg.CreativeWork.getURI(), "TEXT");
		edmTypeSpec.putUriMapping(Schemaorg.VisualArtwork.getURI(), "IMAGE");
		edmTypeSpec.putUriMapping(Schemaorg.Painting.getURI(), "IMAGE");
		edmTypeSpec.putUriMapping(Schemaorg.Photograph.getURI(), "IMAGE");
		edmTypeSpec.putUriMapping(Schemaorg.WebPage.getURI(), "TEXT");
		edmTypeSpec.putUriMapping(Schemaorg.MediaObject.getURI(), "IMAGE");
		edmTypeSpec.putUriMapping(Schemaorg.Book.getURI(), "TEXT");
		edmTypeSpec.putUriMapping(RdfReg.Schemaorg_NEWSPAPER.getURI(), "TEXT");
		edmTypeSpec.putUriMapping(RdfReg.Schemaorg_MANUSCRIPT.getURI(), "TEXT");
		edmTypeSpec.putUriMapping(RdfReg.Schemaorg_DRAWING.getURI(), "IMAGE");
		edmTypeSpec.putUriMapping(RdfReg.Schemaorg_POSTER.getURI(), "IMAGE");
		edmTypeSpec.putUriMapping(Schemaorg.ScholarlyArticle.getURI(), "TEXT");
		edmTypeSpec.putUriMapping(Schemaorg.Periodical.getURI(), "TEXT");
		edmTypeSpec.putUriMapping(Schemaorg.ImageObject.getURI(), "IMAGE");
		edmTypeSpec.putUriMapping(Schemaorg.CreativeWorkSeries.getURI(), "IMAGE");
		edmTypeSpec.putUriMapping(Schemaorg.Movie.getURI(), "VIDEO");
		edmTypeSpec.putUriMapping(Schemaorg.TVEpisode.getURI(), "VIDEO");
		edmTypeSpec.putUriMapping(Schemaorg.Sculpture.getURI(), "IMAGE");
		
//		edmTypeSpec.putUriMapping("", "VIDEO");
//		edmTypeSpec.putUriMapping("", "3D");
		pchoMapping.putDerivedProperty(Rdf.type, edmTypeSpec);
		 
		ResourceTypeConversionSpecification aggregationMapping = spec.getTypePropertiesMapping(Ore.Aggregation);
		aggregationMapping.putPropertyMapping(Schemaorg.provider, Edm.provider);
		aggregationMapping.putPropertyMapping(Schemaorg.associatedMedia, Edm.isShownBy);
		aggregationMapping.putPropertyMapping(Schemaorg.audio, Edm.isShownBy);
		aggregationMapping.putPropertyMapping(Schemaorg.url, Edm.isShownAt);
		aggregationMapping.putPropertyMapping(Schemaorg.image, Edm.isShownBy);
		aggregationMapping.putPropertyMapping(Schemaorg.thumbnailUrl, Edm.object);
		// next one is commented because it is not supported in the rdfConverter at this time
		//		aggregationMapping.putPropertyMappingFromReferencedResource(Schemaorg.IMAGE, Schemaorg.IMAGE_OBJECT, Schemaorg.CONTENT_URL, RdfRegEdm.HAS_VIEW);
//		ResourceTypeConversionSpecification aggregationMapping = spec.getTypeMapping(RdfRegEdm.AGENT);

		ResourceTypeConversionSpecification webResourceMapping = spec.getTypePropertiesMapping(Edm.WebResource);
		webResourceMapping.putPropertyMapping(Schemaorg.encodingFormat, Dc.format);
		webResourceMapping.putPropertyMapping(Schemaorg.name, Dc.description);
		webResourceMapping.putPropertyMapping(Schemaorg.description, Dc.description);
		webResourceMapping.putPropertyMapping(Schemaorg.height, DcTerms.extent);
		webResourceMapping.putPropertyMapping(Schemaorg.width, DcTerms.extent);
		webResourceMapping.putPropertyMapping(Schemaorg.fileFormat, Dc.format);
		webResourceMapping.addPropertyMappingToUri(Schemaorg.contentUrl );
		webResourceMapping.putPropertyMapping(Rdf.type, Dc.type);

		ResourceTypeConversionSpecification organizationMapping = spec.getTypePropertiesMapping(Foaf.Organization);
		organizationMapping.putPropertyMapping(Schemaorg.name, Skos.prefLabel);
		organizationMapping.putPropertyMapping(Schemaorg.alternateName, Skos.altLabel);
		organizationMapping.putPropertyMapping(Schemaorg.description, Skos.note);
		organizationMapping.putPropertyMapping(Schemaorg.sameAs, Owl.sameAs);
		
		ResourceTypeConversionSpecification agentMapping = spec.getTypePropertiesMapping(Edm.Agent);
		agentMapping.putPropertyMapping(Schemaorg.name, Skos.prefLabel);
		agentMapping.putPropertyMapping(Schemaorg.alternateName, Skos.altLabel);
		agentMapping.putPropertyMapping(Schemaorg.sameAs, Owl.sameAs);
		agentMapping.putPropertyMapping(RdfReg.Schemaorg_JOB_TITLE, RdaGr2.professionOrOccupation);
		agentMapping.putPropertyMapping(Schemaorg.birthDate, RdaGr2.dateOfBirth);
		agentMapping.putPropertyMapping(Schemaorg.deathDate, RdaGr2.dateOfDeath);
		agentMapping.putPropertyMapping(Schemaorg.givenName, Skos.prefLabel);
		agentMapping.putPropertyMerge(Schemaorg.givenName, Schemaorg.familyName);
		
		ResourceTypeConversionSpecification conceptMapping = spec.getTypePropertiesMapping(Skos.Concept);
		conceptMapping.putPropertyMapping(Schemaorg.name, Skos.prefLabel);
		conceptMapping.putPropertyMapping(Schemaorg.alternateName, Skos.altLabel);
		conceptMapping.putPropertyMapping(Schemaorg.sameAs, Owl.sameAs);
		
		ResourceTypeConversionSpecification placeMapping = spec.getTypePropertiesMapping(Edm.Place);
		placeMapping.putPropertyMapping(Schemaorg.name, Skos.prefLabel);
		placeMapping.putPropertyMapping(Schemaorg.alternateName, Skos.altLabel);
		placeMapping.putPropertyMapping(Schemaorg.sameAs, Owl.sameAs);
		placeMapping.putPropertyMappingFromReferencedResource(Schemaorg.geo, Schemaorg.GeoCoordinates, Schemaorg.latitude, Wgs84.lat);
		placeMapping.putPropertyMappingFromReferencedResource(Schemaorg.geo, Schemaorg.GeoCoordinates, Schemaorg.longitude, Wgs84.long_);
//		placeMapping.putPropertyMappingFromReferencedResource(Schemaorg.ADDRESS, Schemaorg.POSTAL_ADDRESS, Schemaorg.ADDRESS_REGION, Wgs84.lONG);

		ResourceTypeConversionSpecification addressMapping = spec.getTypePropertiesMapping(RdfReg.VCARD_ADDRESS);
		addressMapping.putPropertyMapping(Schemaorg.addressRegion, RdfReg.VCARD_REGION);
		
		spec.setFilterOfReferencedResource(new FilterOfReferencedResource() {
			public boolean filterOut(Statement reference) {
				return reference.getPredicate().equals(Schemaorg.hasPart) ||
						reference.getPredicate().equals(Schemaorg.isPartOf);
			}
		});
		
		spec.setConversionHandler(new EdmTypePreferedValueHandler());
		
	}

}
