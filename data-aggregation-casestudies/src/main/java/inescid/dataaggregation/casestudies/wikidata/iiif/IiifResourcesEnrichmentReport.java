package inescid.dataaggregation.casestudies.wikidata.iiif;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Model;

import inescid.dataaggregation.dataset.profile.UsageProfiler;
import inescid.util.datastruct.MapOfInts;
import inescid.util.datastruct.MapOfMaps;

public class IiifResourcesEnrichmentReport {
	int idNotFoundInEuropeana=0;
	ArrayList<String> wdAccessFailure=new ArrayList<>();
	MapOfMaps<String, String, AbstractMap.SimpleEntry<String, Collection<String>>> enrichements=new MapOfMaps<>();
	MapOfMaps<String, String, String> enrichementsByWikimedia=new MapOfMaps<>();
	MapOfMaps<String, String, AbstractMap.SimpleEntry<String, Collection<String>>> existingInEuropeana=new MapOfMaps<>();
	MapOfMaps<String, String, String> existingInEuropeanaFromWikimedia=new MapOfMaps<>();
	MapOfInts<String> wikidataCollectionsWithIiifManifests=new MapOfInts<>();
	Map<String, UsageProfiler> edmUsageProfilers=new HashMap<String, UsageProfiler>();
	Map<String, UsageProfiler> iiifUsageProfilers=new HashMap<String, UsageProfiler>();
	
	public void wdAccessFailure(String uri) {
		wdAccessFailure.add(uri);
	}

	public void notLinkedToEuropeana(String wdCol, String uri) {
		wikidataCollectionsWithIiifManifests.incrementTo(wdCol);
	}

	public void idNotFoundInEuropeana(String europeanaObjectUri) {
		idNotFoundInEuropeana++;
	}

	public void enrichements(String europeanaCollection, String europeanaObjectUri, String wikidataEntityUri, Collection<String> iiifManifests) {
		enrichements.put(europeanaCollection, europeanaObjectUri, new AbstractMap.SimpleEntry<String, Collection<String>>(wikidataEntityUri, iiifManifests));
	} 
	
	public void enrichementsByWikimedia(String europeanaCollection, String europeanaObjectUri, String wikidataEntityUri) {
		enrichementsByWikimedia.put(europeanaCollection, europeanaObjectUri, wikidataEntityUri);
	} 
	
	public void existingInEuropeana(String europeanaCollection, String europeanaObjectUri, String wikidataEntityUri, Collection<String> iiifManifests) {
		existingInEuropeana.put(europeanaCollection, europeanaObjectUri, new AbstractMap.SimpleEntry<String, Collection<String>>(wikidataEntityUri, iiifManifests));
	}
	
	public void existingInEuropeanaFromWikimedia(String europeanaCollection, String europeanaObjectUri, String wikidataEntityUri) {
		existingInEuropeanaFromWikimedia.put(europeanaCollection, europeanaObjectUri, wikidataEntityUri);
	}

	public void profileEdm(String europeanaCollection, Model rdfEdm) {
		UsageProfiler usageProfiler = edmUsageProfilers.get(europeanaCollection);
		if(usageProfiler==null) {
			usageProfiler=new UsageProfiler();
			edmUsageProfilers.put(europeanaCollection, usageProfiler);
		}
		usageProfiler.collect(rdfEdm);
	} 
	
	public void profileIiifManifest(String europeanaCollection, Model rdfIiif) {
		UsageProfiler usageProfiler = iiifUsageProfilers.get(europeanaCollection);
		if(usageProfiler==null) {
			usageProfiler=new UsageProfiler();
			iiifUsageProfilers.put(europeanaCollection, usageProfiler);
		}
		usageProfiler.collect(rdfIiif);
	} 
	
}
