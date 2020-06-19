package inescid.dataaggregation.crawl.ld;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import inescid.dataaggregation.data.model.RdfReg;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Schemaorg;
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
		
		choClasses.add(Schemaorg.CreativeWork);
		choClasses.add(Schemaorg.VisualArtwork);
		choClasses.add(Schemaorg.Painting);
		choClasses.add(Schemaorg.Book);
		choClasses.add(Schemaorg.ImageObject);
//		choClasses.add(RdfRegSchemaorg.NewsPaper);
		choClasses.add(Schemaorg.Periodical);
		choClasses.add(Schemaorg.Photograph);
		choClasses.add(Schemaorg.CreativeWorkSeries);
		choClasses.add(Schemaorg.Sculpture);
//		choClasses.add(RegSchemaorg.Chapter);
		
		MappingAllowedForClass choAggMap=new MappingAllowedForClass();
		choAggMap.allowAnyForProperty(Schemaorg.contributor);
		choAggMap.allowAnyForProperty(Schemaorg.about);
		choAggMap.allowAnyForProperty(Schemaorg.contributor);
		choAggMap.allowAnyForProperty(Schemaorg.creator);
		choAggMap.allowProperty(Schemaorg.description, AllowedValue.LITERAL);
		choAggMap.allowAnyForProperty(Schemaorg.inLanguage);
		choAggMap.allowAnyForProperty(Schemaorg.publisher);
		choAggMap.allowProperty(Schemaorg.name, AllowedValue.LITERAL);
		choAggMap.allowProperty(Schemaorg.dateCreated, AllowedValue.LITERAL);
		choAggMap.allowAnyForProperty(Schemaorg.hasPart);
		choAggMap.allowAnyForProperty(Schemaorg.exampleOfWork);
		choAggMap.allowAnyForProperty(Schemaorg.isPartOf);
		choAggMap.allowProperty(Schemaorg.datePublished, AllowedValue.LITERAL);
		choAggMap.allowAnyForProperty(Schemaorg.mentions);
		choAggMap.allowAnyForProperty(Schemaorg.spatialCoverage);
		choAggMap.allowAnyForProperty(Schemaorg.temporalCoverage);
		choAggMap.allowAnyForProperty(Schemaorg.isBasedOn);
		choAggMap.allowAnyForProperty(Schemaorg.sameAs);
		choAggMap.allowAnyForProperty(Schemaorg.provider);
		choAggMap.allowAnyForProperty(Schemaorg.url);
		choAggMap.allowAnyForProperty(Schemaorg.associatedMedia);
		choAggMap.allowAnyForProperty(Schemaorg.thumbnailUrl);
		choAggMap.allowAnyForProperty(Schemaorg.contentLocation);
		choAggMap.allowAnyForProperty(Schemaorg.identifier);
		choAggMap.allowAnyForProperty(Schemaorg.image);
		choAggMap.allowAnyForProperty(Schemaorg.genre);
		choAggMap.allowAnyForProperty(Schemaorg.pagination);
		choAggMap.allowAnyForProperty(Schemaorg.material);
		choAggMap.allowAnyForProperty(Schemaorg.author);
		choAggMap.allowAnyForProperty(Schemaorg.height);
		choAggMap.allowAnyForProperty(Schemaorg.numberOfPages);
		choAggMap.allowAnyForProperty(Schemaorg.width);
		choAggMap.allowProperty(Schemaorg.artform, AllowedValue.LITERAL);
		choAggMap.allowProperty(Schemaorg.artMedium, AllowedValue.LITERAL);
		choAggMap.allowReferenceOrResourceForProperty(Schemaorg.license);
		for(Resource r:choClasses) 
			mappingsByClass.put(r, choAggMap);
		
		MappingAllowedForClass webresMap=new MappingAllowedForClass();
		webresMap.allowProperty(Schemaorg.name, AllowedValue.LITERAL);
		webresMap.allowProperty(Schemaorg.thumbnailUrl, AllowedValue.REFERENCE);
		webresMap.allowProperty(Schemaorg.uploadDate, AllowedValue.LITERAL);
		webresMap.allowProperty(Schemaorg.contentUrl, AllowedValue.REFERENCE);
		webresMap.allowAnyForProperty(Schemaorg.creator);
		webresMap.allowProperty(Schemaorg.description, AllowedValue.LITERAL);
		webresMap.allowAnyForProperty(Schemaorg.encodesCreativeWork);
		webresMap.allowProperty(Schemaorg.dateCreated, AllowedValue.LITERAL);
		webresMap.allowAnyForProperty(Schemaorg.hasPart);
		webresMap.allowProperty(Schemaorg.datePublished, AllowedValue.LITERAL);
		webresMap.allowAnyForProperty(Schemaorg.isPartOf);
		webresMap.allowLiteralOrReferenceForProperty(Schemaorg.license);
		webresMap.allowAnyForProperty(Schemaorg.sameAs);
		webresMap.allowAnyForProperty(Schemaorg.encodingFormat);
		webresMap.allowAnyForProperty(Schemaorg.height);
		webresMap.allowAnyForProperty(Schemaorg.width);
		webresMap.allowAnyForProperty(Schemaorg.duration);
		webresMap.allowAnyForProperty(Schemaorg.bitrate);
		
		mappingsByClass.put(Schemaorg.ImageObject, webresMap);
		mappingsByClass.put(Schemaorg.MediaObject, webresMap);
		mappingsByClass.put(Schemaorg.AudioObject, webresMap);
		mappingsByClass.put(Schemaorg.VideoObject, webresMap);
		mappingsByClass.put(Schemaorg.WebPage, webresMap);
		

		MappingAllowedForClass entMap=new MappingAllowedForClass();
		
		entMap.allowProperty(Schemaorg.url, AllowedValue.REFERENCE);
		entMap.allowProperty(Schemaorg.image, AllowedValue.REFERENCE);
		entMap.allowProperty(Schemaorg.name, AllowedValue.LITERAL);
		entMap.allowProperty(Schemaorg.alternateName, AllowedValue.LITERAL);
		entMap.allowProperty(Schemaorg.description, AllowedValue.LITERAL);
		
		entMap.allowAnyForProperty(Schemaorg.birthDate);
		entMap.allowAnyForProperty(Schemaorg.deathDate);
		entMap.allowAnyForProperty(Schemaorg.birthPlace);
		entMap.allowAnyForProperty(Schemaorg.deathPlace);
		entMap.allowAnyForProperty(Schemaorg.foundingDate);
		entMap.allowAnyForProperty(Schemaorg.dissolutionDate);
//		entMap.put(RegSchemaorg.jobTitle, AllowedValue.LITERAL);
		entMap.allowAnyForProperty(Schemaorg.gender);
		entMap.allowAnyForProperty(Schemaorg.sameAs);

		entMap.allowAnyForProperty(Schemaorg.geo);
		entMap.allowProperty(Schemaorg.containsPlace, AllowedValue.REFERENCE);
		entMap.allowProperty(Schemaorg.containedInPlace, AllowedValue.REFERENCE);

		mappingsByClass.put(Schemaorg.Person, entMap);
		mappingsByClass.put(Schemaorg.Organization, entMap);
		mappingsByClass.put(Schemaorg.Place, entMap);

//		RegSchemaorg.Thing for concepts

		MappingAllowedForClass geoMap=new MappingAllowedForClass();
		geoMap.allowProperty(Schemaorg.latitude, AllowedValue.LITERAL);
		geoMap.allowProperty(Schemaorg.longitude, AllowedValue.LITERAL);
		geoMap.allowProperty(Schemaorg.elevation, AllowedValue.LITERAL);
		mappingsByClass.put(Schemaorg.GeoCoordinates, geoMap);

		MappingAllowedForClass thingMap=new MappingAllowedForClass();
		thingMap.allowAnyForProperty(Schemaorg.sameAs);
		thingMap.allowProperty(Schemaorg.url, AllowedValue.REFERENCE);
		thingMap.allowProperty(Schemaorg.image, AllowedValue.REFERENCE);
		thingMap.allowProperty(Schemaorg.name, AllowedValue.LITERAL);
		thingMap.allowProperty(Schemaorg.alternateName, AllowedValue.LITERAL);
		thingMap.allowProperty(Schemaorg.description, AllowedValue.LITERAL);
		mappingsByClass.put(Schemaorg.Thing, geoMap);
		
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
		public Set<Property> keySet() {
			return map.keySet();
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
	
	
	public String toCsv() throws Exception {
		StringBuilder sb=new StringBuilder();
		CSVPrinter csv=new CSVPrinter(sb, CSVFormat.DEFAULT);
//		HashSet<Resource> choClasses;
//		HashMap<Resource, MappingAllowedForClass> mappingsByClass;
		ArrayList<Resource> sortedClasses=new ArrayList(mappingsByClass.keySet());
		Collections.sort(sortedClasses, new Comparator<Resource>() {
			public int compare(Resource o1, Resource o2) {
				return o1.getURI().compareTo(o2.getURI());
			};
		});
		
		HashSet<Property> dedup=new HashSet<Property>();
		int totalProps=0;
		for(Resource r: sortedClasses) {
			Set<Property> keySet = mappingsByClass.get(r).keySet();
			csv.printRecord(r, keySet.size());
			totalProps+=keySet.size();			
			dedup.addAll(keySet);
		}
		csv.printRecord("Total classes",sortedClasses.size(), "Total properties",totalProps, "Distinct properties", dedup.size());
		
		for(Resource r: sortedClasses) {
			csv.println();
			boolean first=true;
			for(Property m: mappingsByClass.get(r).keySet()) {
				if(first) {
					csv.printRecord(r);
					first=false;
				}else
					csv.print("");			
				csv.print(m.getURI());			
				for(AllowedValue v: mappingsByClass.get(r).getProperty(m)) {
					csv.print(v);			
				}
				csv.println();
			}
		}
		
		csv.println();
		csv.printRecord("Distinct properties");
		for(Property p:dedup) {
			csv.printRecord(p.getURI());			
		}
		
		csv.close();
		return sb.toString();
	}
	
	
	public static void main(String[] args) throws Exception {
		String csvString = new RulesSchemaorgCrawlGraphOfCho().toCsv();
		System.out.println(csvString);
		FileUtils.write(new File("../data-aggregation-casestudies/src/data/schemaorgcrawling/EuropeanaKnownFromSchemaorg.csv"), csvString, "UTF8");
	}
}
