package inescid.dataaggregation.casestudies.coreference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

public class EuropeanaSameAsSets {
//	HashMap<String, Set<String>> sameAsSetsByUri=new HashMap<String, Set<String>>(); 
	HashSet<String> sameAsSetsByUri=new HashSet<String>(); 
	SameAsSets sameAsSets;
	
	public EuropeanaSameAsSets(String urisFile, SameAsSets sameAsSets) throws IOException {
		this.sameAsSets=sameAsSets;
		for(String uri: getEuropeanaVocabUris(urisFile)) {
//			Set<String> set = sameAsSets.getUriIndex().get(uri);
			sameAsSetsByUri.add(uri);
//			if(set!=null)
//				sameAsSetsByUri.put(uri, set);
//			else 
//				sameAsSetsByUri.put(uri, new HashSet<String>() {{add(uri);}});
		}
	}

	public Set<String> getUriSet(String uri) {
		return sameAsSets.getSameAsSet(uri);
	}
	
	public MVMap<String, Set<String>> getUriIndex() {
		return sameAsSets.getUriIndex();
	}

	public Set<String> keySetOfVocabUris() {
		return sameAsSetsByUri;
	}

	public Set<String> addToUri(String uri, Set<String> foundSet) {
		Set<String> uriSet=new HashSet<String>(getUriSet(uri));
		uriSet.addAll(foundSet);
		uriSet = sameAsSets.addSet(uriSet);
		return uriSet;
	}

	public boolean addIfOverlap(String uriA, String uriB) {
//		//check only when no two Europeana URIs would be merged
//		if(sameAsSetsByUri.containsKey(uriA) && sameAsSetsByUri.containsKey(uriB)) {
//			Set<String> sameAsSetA = sameAsSets.getSameAsSet(uriA);
//			Set<String> sameAsSetB = sameAsSets.getSameAsSet(uriB);
//			Set<String> europeanaKeySet = sameAsSetsByUri.keySet();
//			boolean hasEuropeanaUri=false;
//			for(String uri: sameAsSetA) {
//				if (europeanaKeySet.contains(uri)) {
//					hasEuropeanaUri=true;
//					break;
//				}
//			}
//			if(hasEuropeanaUri) {
//				for(String uri: sameAsSetB) {
//					if (europeanaKeySet.contains(uri)) 
//						return false;
//				}
//			}
//		}

		//TODO: what are these tripples (at VIAF?)
		if(uriA.endsWith("#foaf:Person") || uriB.endsWith("#foaf:Person") 
				|| uriA.endsWith("#owl:Thing") || uriB.endsWith("#owl:Thing") 
				|| uriA.endsWith("#skos:Concept") || uriB.endsWith("#skos:Concept") 
				)
			return false;
		return sameAsSets.addSameAsIfOverlap(uriA, uriB);
	}
	
	
	public static List<String> getEuropeanaVocabUris(String urisCsvFile) throws IOException{
		return getEuropeanaVocabUris(new File(urisCsvFile), true);
	}
	public static List<String> getEuropeanaVocabUris(File urisCsvFile, boolean getOnlyNotLinkedToWikidata) throws IOException{
		List<String> ret=new ArrayList<String>();
		BufferedReader fr=new BufferedReader(new FileReader(urisCsvFile));
		CSVParser csvReader=new CSVParser(fr, CSVFormat.DEFAULT);
		for(CSVRecord rec : csvReader) {
			String uri= rec.get(0);
			String wdUri= rec.get(1);
			if(getOnlyNotLinkedToWikidata && wdUri!=null && !wdUri.isEmpty())
				continue;
			Matcher matcher = Consts.HOST_PATTERN.matcher(uri);
			if(!matcher.find()) continue;
//			String host = matcher.group(1);
//			if(!Consts.RDF_HOSTS.contains(host))
//				continue;
			ret.add(uri);
		}
		csvReader.close();
		fr.close();
		return ret;
	}

	public MVStore getStore() {
		return sameAsSets.getUriIndex().getStore();
	}
}
