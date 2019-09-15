package inescid.dataaggregation.crawl.ld;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import inescid.dataaggregation.data.RdfReg;
import inescid.dataaggregation.data.RegEdm;
import inescid.dataaggregation.data.RegSchemaorg;
import inescid.util.datastruct.MapOfSets;

public class RulesSchemaorgCrawlGraphOfCho {
	public enum AllowedValue {
		REFERENCE, RESOURCE, LITERAL;
	}
		
//	public enum AllowedValues {
//		NONE, REFERENCE, RESOURCE, LITERAL, REFERENCE_OR_RESOURCE, ANY;
//
//		public AllowedValues combine(AllowedValues allowedValues) {
//			if(allowedValues==null) return this;
//			if (this==ANY || this==allowedValues) return this;
//			if (isRestrictive(allowedValues)) 
//				return this;
//			return allowedValues;
//		}
//
//		private boolean isRestrictive(AllowedValues allowedValues) {
//			if (allowedValues==ANY || this==NONE) return false;
//			switch (this) {
//			case REFERENCE_OR_RESOURCE:
//				return false;
//			case RESOURCE:
//				return allowedValues!=REFERENCE_OR_RESOURCE && allowedValues!=REFERENCE;
//			case REFERENCE:
//				return allowedValues!=REFERENCE_OR_RESOURCE && allowedValues!=RESOURCE;
//			case LITERAL:
//				return allowedValues==NONE;
//			}
//			return true;
//		}
//	}
	
	public RulesSchemaorgCrawlGraphOfCho() {
		choClasses=new HashSet<>();
		mappingsByClass=new HashMap<>();
		
		choClasses.add(RegSchemaorg.CreativeWork);
		choClasses.add(RegSchemaorg.VisualArtwork);
		choClasses.add(RegSchemaorg.Painting);
		choClasses.add(RegSchemaorg.Book);
		choClasses.add(RegSchemaorg.ImageObject);
//		choClasses.add(RdfRegSchemaorg.NewsPaper);
		choClasses.add(RegSchemaorg.Periodical);
		choClasses.add(RegSchemaorg.Photograph);
		choClasses.add(RegSchemaorg.CreativeWorkSeries);
		choClasses.add(RegSchemaorg.Sculpture);
//		choClasses.add(RegSchemaorg.Chapter);
		
		MappingAllowedForClass choAggMap=new MappingAllowedForClass();
		choAggMap.allowAnyForProperty(RegSchemaorg.contributor);
		choAggMap.allowAnyForProperty(RegSchemaorg.about);
		choAggMap.allowAnyForProperty(RegSchemaorg.contributor);
		choAggMap.allowAnyForProperty(RegSchemaorg.creator);
		choAggMap.allowProperty(RegSchemaorg.description, AllowedValue.LITERAL);
		choAggMap.allowAnyForProperty(RegSchemaorg.inLanguage);
		choAggMap.allowAnyForProperty(RegSchemaorg.publisher);
		choAggMap.allowProperty(RegSchemaorg.name, AllowedValue.LITERAL);
		choAggMap.allowProperty(RegSchemaorg.dateCreated, AllowedValue.LITERAL);
		choAggMap.allowAnyForProperty(RegSchemaorg.hasPart);
		choAggMap.allowAnyForProperty(RegSchemaorg.exampleOfWork);
		choAggMap.allowAnyForProperty(RegSchemaorg.isPartOf);
		choAggMap.allowProperty(RegSchemaorg.datePublished, AllowedValue.LITERAL);
		choAggMap.allowAnyForProperty(RegSchemaorg.mentions);
		choAggMap.allowAnyForProperty(RegSchemaorg.spatialCoverage);
		choAggMap.allowAnyForProperty(RegSchemaorg.temporalCoverage);
		choAggMap.allowAnyForProperty(RegSchemaorg.isBasedOn);
		choAggMap.allowAnyForProperty(RegSchemaorg.sameAs);
		choAggMap.allowAnyForProperty(RegSchemaorg.provider);
		choAggMap.allowAnyForProperty(RegSchemaorg.url);
		choAggMap.allowAnyForProperty(RegSchemaorg.associatedMedia);
		choAggMap.allowAnyForProperty(RegSchemaorg.thumbnailUrl);
		choAggMap.allowAnyForProperty(RegSchemaorg.contentLocation);
		choAggMap.allowAnyForProperty(RegSchemaorg.identifier);
		choAggMap.allowAnyForProperty(RegSchemaorg.image);
		choAggMap.allowAnyForProperty(RegSchemaorg.genre);
		choAggMap.allowAnyForProperty(RegSchemaorg.pagination);
		choAggMap.allowAnyForProperty(RegSchemaorg.material);
		choAggMap.allowAnyForProperty(RegSchemaorg.author);
		choAggMap.allowAnyForProperty(RegSchemaorg.height);
		choAggMap.allowAnyForProperty(RegSchemaorg.numberOfPages);
		choAggMap.allowAnyForProperty(RegSchemaorg.width);
		choAggMap.allowProperty(RegSchemaorg.artform, AllowedValue.LITERAL);
		choAggMap.allowProperty(RegSchemaorg.artMedium, AllowedValue.LITERAL);
		for(Resource r:choClasses) 
			mappingsByClass.put(r, choAggMap);
		
		MappingAllowedForClass webresMap=new MappingAllowedForClass();
		webresMap.allowProperty(RegSchemaorg.name, AllowedValue.LITERAL);
		webresMap.allowProperty(RegSchemaorg.thumbnailUrl, AllowedValue.REFERENCE);
		webresMap.allowProperty(RegSchemaorg.uploadDate, AllowedValue.LITERAL);
		webresMap.allowProperty(RegSchemaorg.contentUrl, AllowedValue.REFERENCE);
		webresMap.allowAnyForProperty(RegSchemaorg.creator);
		webresMap.allowProperty(RegSchemaorg.description, AllowedValue.LITERAL);
		webresMap.allowAnyForProperty(RegSchemaorg.encodesCreativeWork);
		webresMap.allowProperty(RegSchemaorg.dateCreated, AllowedValue.LITERAL);
		webresMap.allowAnyForProperty(RegSchemaorg.hasPart);
		webresMap.allowProperty(RegSchemaorg.datePublished, AllowedValue.LITERAL);
		webresMap.allowAnyForProperty(RegSchemaorg.isPartOf);
		webresMap.allowLiteralOrReferenceForProperty(RegSchemaorg.license);
		webresMap.allowAnyForProperty(RegSchemaorg.sameAs);
		webresMap.allowAnyForProperty(RegSchemaorg.encodingFormat);
		webresMap.allowAnyForProperty(RegSchemaorg.height);
		webresMap.allowAnyForProperty(RegSchemaorg.width);
		webresMap.allowAnyForProperty(RegSchemaorg.duration);
		webresMap.allowAnyForProperty(RegSchemaorg.bitrate);
		
		mappingsByClass.put(RegSchemaorg.ImageObject, webresMap);
		mappingsByClass.put(RegSchemaorg.MediaObject, webresMap);
		mappingsByClass.put(RegSchemaorg.AudioObject, webresMap);
		mappingsByClass.put(RegSchemaorg.VideoObject, webresMap);
		mappingsByClass.put(RegSchemaorg.WebPage, webresMap);
		

		MappingAllowedForClass entMap=new MappingAllowedForClass();
		
		entMap.allowProperty(RegSchemaorg.url, AllowedValue.REFERENCE);
		entMap.allowProperty(RegSchemaorg.image, AllowedValue.REFERENCE);
		entMap.allowProperty(RegSchemaorg.name, AllowedValue.LITERAL);
		entMap.allowProperty(RegSchemaorg.alternateName, AllowedValue.LITERAL);
		entMap.allowProperty(RegSchemaorg.description, AllowedValue.LITERAL);
		
		entMap.allowAnyForProperty(RegSchemaorg.birthDate);
		entMap.allowAnyForProperty(RegSchemaorg.deathDate);
		entMap.allowAnyForProperty(RegSchemaorg.birthPlace);
		entMap.allowAnyForProperty(RegSchemaorg.deathPlace);
		entMap.allowAnyForProperty(RegSchemaorg.foundingDate);
		entMap.allowAnyForProperty(RegSchemaorg.dissolutionDate);
//		entMap.put(RegSchemaorg.jobTitle, AllowedValue.LITERAL);
		entMap.allowAnyForProperty(RegSchemaorg.gender);
		entMap.allowAnyForProperty(RegSchemaorg.sameAs);

		entMap.allowAnyForProperty(RegSchemaorg.geo);
		entMap.allowProperty(RegSchemaorg.containsPlace, AllowedValue.REFERENCE);
		entMap.allowProperty(RegSchemaorg.containedInPlace, AllowedValue.REFERENCE);

		mappingsByClass.put(RegSchemaorg.Person, entMap);
		mappingsByClass.put(RegSchemaorg.Organization, entMap);
		mappingsByClass.put(RegSchemaorg.Place, entMap);

//		RegSchemaorg.Thing for concepts

		MappingAllowedForClass geoMap=new MappingAllowedForClass();
		geoMap.allowProperty(RegSchemaorg.latitude, AllowedValue.LITERAL);
		geoMap.allowProperty(RegSchemaorg.longitude, AllowedValue.LITERAL);
		geoMap.allowProperty(RegSchemaorg.elevation, AllowedValue.LITERAL);
		mappingsByClass.put(RegSchemaorg.GeoCoordinates, geoMap);

		MappingAllowedForClass thingMap=new MappingAllowedForClass();
		thingMap.allowAnyForProperty(RegSchemaorg.sameAs);
		thingMap.allowProperty(RegSchemaorg.url, AllowedValue.REFERENCE);
		thingMap.allowProperty(RegSchemaorg.image, AllowedValue.REFERENCE);
		thingMap.allowProperty(RegSchemaorg.name, AllowedValue.LITERAL);
		thingMap.allowProperty(RegSchemaorg.alternateName, AllowedValue.LITERAL);
		thingMap.allowProperty(RegSchemaorg.description, AllowedValue.LITERAL);
		mappingsByClass.put(RegSchemaorg.Thing, geoMap);
		
	}
	
	public class MappingAllowedForClass {
		private MapOfSets<Property, AllowedValue> map=new MapOfSets<>();
		public void allowProperty(Property prop, AllowedValue allowed) {
			map.put(prop, allowed);
		}
		public void allowAnyForProperty(Property prop) {
			map.putAll(prop, AllowedValue.values());			
		}
		public void allowReferenceOrResourceForProperty(Property prop) {
			map.putAll(prop, AllowedValue.REFERENCE, AllowedValue.RESOURCE);			
		}
		public void allowLiteralOrReferenceForProperty(Property prop) {
			map.putAll(prop, AllowedValue.LITERAL, AllowedValue.REFERENCE);			
		}
		public Set<AllowedValue> getProperty(Property prop) {
			return map.get(prop);
		}
	}
	
	HashSet<Resource> choClasses;
	HashMap<Resource, MappingAllowedForClass> mappingsByClass;

	public boolean isMappable(Resource cls) {
		return mappingsByClass.containsKey(cls);
	}
	public MappingAllowedForClass getMapping(Resource cls) {
		return mappingsByClass.get(cls);
	}
	public boolean isCho(Resource cls) {
		return choClasses.contains(cls);
	}
	public static Set<AllowedValue> getPossibleMaps(Property prop, Map<Resource, MappingAllowedForClass> mappableTypes) {
//		System.out.println(prop);
		Set<AllowedValue> av=new HashSet();
		for(java.util.Map.Entry<Resource, MappingAllowedForClass> map: mappableTypes.entrySet()) {
			Set<AllowedValue> allowed = map.getValue().getProperty(prop);
			if(allowed!=null) 
				av.addAll(allowed);
//			System.out.println(map.getKey() +" "+av);
		}
		return av;
	}
}
