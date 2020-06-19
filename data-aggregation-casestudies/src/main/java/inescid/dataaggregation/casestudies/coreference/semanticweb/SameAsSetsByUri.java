package inescid.dataaggregation.casestudies.coreference.semanticweb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDFBase;
import org.mapdb.DBException.GetVoid;

import inescid.dataaggregation.casestudies.coreference.Consts;
import inescid.dataaggregation.casestudies.coreference.semanticweb.AgentVocabsIntelinkingStudy.AgentVocabulary;
import inescid.dataaggregation.casestudies.coreference.semanticweb.AgentVocabsIntelinkingStudy.LinkPredicateCounts;
import inescid.dataaggregation.data.model.Skos;
import inescid.util.datastruct.MapOfInts;
import inescid.util.datastruct.MapOfSets;

public class SameAsSetsByUri {
	MapOfSets<String, String> uriIndex=null;
	
	public SameAsSetsByUri(List<String> uris, int from, int to) throws IOException {
		uriIndex=new MapOfSets<String, String>();
		for(int i=from; i<to; i++)
			uriIndex.put(uris.get(i),uris.get(i));
	}
	
	public SameAsSetsByUri(File csvFile) throws IOException {
		BufferedReader reader = Files.newBufferedReader(csvFile.toPath(), StandardCharsets.UTF_8);
		uriIndex=MapOfSets.readCsv(reader);
		reader.close();
//		for(Entry<String, Set<String>> entry : uriIndex.entrySet()) {
//			if(entry.getValue().isEmpty())
//				entry.getValue().add(entry.getKey());
//		}
	}
	
	public void saveAsCsv(File csvFile) throws IOException {
		MapOfSets.writeCsv(uriIndex, new FileWriterWithEncoding(csvFile, StandardCharsets.UTF_8));
	}

	public int searchLinksIn(Iterable<AgentVocabulary> targets, boolean includeCloseMatches) throws IOException {
		HashSet<String> allUrisLinked=new HashSet<String>();
		for(Set<String> cluster: uriIndex.values()) 
			allUrisLinked.addAll(cluster);
		for(AgentVocabulary vocab : targets) {
			if(!vocab.sameAsStatements.exists()) {
				System.out.println("no file "+vocab.sameAsStatements.getName());
				continue;
			}
			FileInputStream fis=new FileInputStream(vocab.sameAsStatements);
			RDFDataMgr.parse(new StreamRDFBase() {
				public void triple(Triple triple) {
					if(!includeCloseMatches && triple.getPredicate().getURI().equals(Skos.closeMatch.getURI()))
						return;
					if(disregardedUris!=null && 
							(disregardedUris.contains(triple.getSubject().getURI()) || disregardedUris.contains(triple.getObject().getURI())))
						return;
					addLink(allUrisLinked, triple.getSubject().getURI(), triple.getObject().getURI());
				}
			}, fis, Consts.RDF_SERIALIZATION);
			fis.close();
		}
		int growth= -allUrisLinked.size();
		for(Set<String> cluster: uriIndex.values()) 
			allUrisLinked.addAll(cluster);
		growth += allUrisLinked.size();
		return growth;
	}
	
	private void addLink(HashSet<String> allUrisLinked, String uriS, String uriO) {
		boolean sExists=allUrisLinked.contains(uriS);
		boolean oExists=allUrisLinked.contains(uriO);
		if(sExists || oExists) {
			for(Set<String> cluster: uriIndex.values()) {
				if(sExists && cluster.contains(uriS))
					cluster.add(uriO);
				else if(oExists && cluster.contains(uriO))
					cluster.add(uriS);
			}
		}
	}

	public Set<String> getUris() {
		return uriIndex.keySet();
	}

	public Collection<Set<String>> getSetsValues() {
		return uriIndex.values();
	}
	
	public Set<Entry<String, Set<String>>> getEntrySet() {
		return uriIndex.entrySet();
	}

	public MapOfInts<String> getTargetHostsStats() {
		MapOfInts<String> linkedToVocabCounts=new MapOfInts<String>();//K:host
		for(Entry<String, Set<String>> uriEntry: getEntrySet()) {
			String hostOfProviderUri=Util.getHost(uriEntry.getKey());
			HashSet<String> hostsSet=new HashSet<String>();
			for(String uri: uriEntry.getValue()) {
				String host = Util.getHost(uri);
				if(host!=null && !host.equals(hostOfProviderUri)) {
					hostsSet.add(host);
				}
			}
			linkedToVocabCounts.incrementToAll(hostsSet);
		}
		return linkedToVocabCounts;
	}
	
	public void searchLinkingErrors() {
		for(Entry<String, Set<String>> uriEntry: getEntrySet()) {
			String hostOfProviderUri=Util.getHost(uriEntry.getKey());
			HashSet<String> hostsSet=new HashSet<String>();
			for(String uri: uriEntry.getValue()) {
				String host = Util.getHost(uri);
				if(!host.equals(hostOfProviderUri)) {
					hostsSet.add(host);
				}
			}
			if(uriEntry.getValue().size() - hostsSet.size()>3)
				System.out.println(uriEntry.getKey()+" - "+ uriEntry.getValue().size()+"-"+ hostsSet.size());
		}
	}
	
	
	HashSet<String> disregardedUris=null;
	public void setDisregardedUris(HashSet<String> badUris) {
		disregardedUris=badUris;
	}
	
	
}



