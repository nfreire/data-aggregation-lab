package inescid.dataaggregation.casestudies.coreference.semanticweb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDFBase;
import org.mozilla.universalchardet.prober.SBCSGroupProber;

import inescid.dataaggregation.casestudies.coreference.Consts;
import inescid.dataaggregation.data.model.Owl;
import inescid.dataaggregation.data.model.Schemaorg;
import inescid.dataaggregation.data.model.Skos;
import inescid.util.datastruct.MapOfMapsOfInts;

public class AgentVocabsIntelinkingStudy {
	
	public static void main(String[] args) throws Exception {
    	String repoFolder = "c://users/nfrei/desktop/data/coreference-semanticweb";
    	if(args!=null) {
			if(args.length>=1) 
				repoFolder = args[0];
    	}
		new AgentVocabsIntelinkingStudy(new File(repoFolder)).run();
	}
	
	public static class AgentVocabulary {
		final String domain;
		File sameAsStatements;
		public AgentVocabulary(String domain, File sameAsStatements) {
			super();
			this.domain = domain;
			this.sameAsStatements = sameAsStatements;
		}
	}

	public class LinkPredicateCounts {
		String predicate;
		MapOfMapsOfInts<AgentVocabulary, AgentVocabulary> vocabFromToCounts; 
		
		public LinkPredicateCounts(String predicate) {
			super();
			this.predicate = predicate;
			vocabFromToCounts=new MapOfMapsOfInts<AgentVocabulary, AgentVocabulary>();
		}
	}
	
	File homeFolder;
	Map<String, AgentVocabulary> agentVocabs;//k:host
	Map<String, LinkPredicateCounts> linkCounts=new HashMap<String, LinkPredicateCounts>();//k:uri predicate
	
	public AgentVocabsIntelinkingStudy(File homeFolder) {
		this.homeFolder = homeFolder;
		agentVocabs=getStudiedVocabs(homeFolder);
	}
	

	public void run() throws IOException {
		for(String vocabHost: this.agentVocabs.keySet()) {
			System.out.println("Getting counts of "+vocabHost);
			AgentVocabulary vocab = agentVocabs.get(vocabHost);
			FileInputStream fis=new FileInputStream(vocab.sameAsStatements);
			RDFDataMgr.parse(new StreamRDFBase() {
				public void triple(Triple triple) {
					String  targetHost=Util.getHost(triple.getObject().getURI());
					AgentVocabulary targetVoc = agentVocabs.get(targetHost);
					if(targetVoc!=null && !vocab.equals(targetVoc)) {
						String uriPredicate = triple.getPredicate().getURI();
						LinkPredicateCounts counts = linkCounts.get(uriPredicate);	
						if(counts==null) {
							counts=new LinkPredicateCounts(uriPredicate);
							linkCounts.put(uriPredicate, counts);
						}
						counts.vocabFromToCounts.incrementTo(vocab, targetVoc);					
					}
				}
			}, fis, Consts.RDF_SERIALIZATION);
			fis.close();
			
		}
		saveResults();
	}
	
	private void saveResults() throws IOException {
		LinkPredicateCounts allMatchesCounts= new LinkPredicateCounts("all_matches");
		LinkPredicateCounts noCloseMatchCounts= new LinkPredicateCounts("no_close_match");
		for(String linkTypeUri : linkCounts.keySet()) {
			boolean isCloseMatch=linkTypeUri.equals(Skos.closeMatch.getURI());
			LinkPredicateCounts linkType = linkCounts.get(linkTypeUri);
			for(Entry<AgentVocabulary, List<Entry<AgentVocabulary, Integer>>> src : linkType.vocabFromToCounts.getSortedEntries()) {
				for(Entry<AgentVocabulary, Integer> count : src.getValue()) {
					allMatchesCounts.vocabFromToCounts.addTo(src.getKey(), count.getKey(), count.getValue());					
					if(!isCloseMatch)
						noCloseMatchCounts.vocabFromToCounts.addTo(src.getKey(), count.getKey(), count.getValue());					
				}
			}
		}
		
		StringBuffer sb=new StringBuffer();
		CSVPrinter writer=new CSVPrinter(sb, CSVFormat.DEFAULT);
		for(String linkTypeUri : linkCounts.keySet()) {
			LinkPredicateCounts linkType = linkCounts.get(linkTypeUri);
			for(Entry<AgentVocabulary, List<Entry<AgentVocabulary, Integer>>> src : linkType.vocabFromToCounts.getSortedEntries()) {
				for(Entry<AgentVocabulary, Integer> count : src.getValue()) {
					writer.printRecord(linkType.predicate, src.getKey().domain, count.getKey().domain, count.getValue());
				}
			}
		}
		writer.println();
		writer.println();
		for(Entry<AgentVocabulary, List<Entry<AgentVocabulary, Integer>>> src : allMatchesCounts.vocabFromToCounts.getSortedEntries()) {
			for(Entry<AgentVocabulary, Integer> count : src.getValue()) {
				writer.printRecord(allMatchesCounts.predicate, src.getKey().domain, count.getKey().domain, count.getValue());
			}
		}
		
		writer.println();
		writer.println();
		for(Entry<AgentVocabulary, List<Entry<AgentVocabulary, Integer>>> src : noCloseMatchCounts.vocabFromToCounts.getSortedEntries()) {
			for(Entry<AgentVocabulary, Integer> count : src.getValue()) {
				writer.printRecord(noCloseMatchCounts.predicate, src.getKey().domain, count.getKey().domain, count.getValue());
			}
		}
		
		writer.close();
		FileUtils.write(new File(homeFolder, "agent-vocab-interlinking.csv"), sb.toString(), "UTF-8");
	}


	public static Map<String, AgentVocabulary> getStudiedVocabs(File homeFolder) {
		Map<String, AgentVocabulary> agentVocabs=new HashMap<String, AgentVocabsIntelinkingStudy.AgentVocabulary>();
		AgentVocabulary v = new AgentVocabulary("viaf.org", new File(homeFolder, "viaf.rt"));
		agentVocabs.put(v.domain, v);
		v = new AgentVocabulary("datos.bne.es", new File(homeFolder, "datos.bne.es.rt"));
		agentVocabs.put(v.domain, v);
		v = new AgentVocabulary("id.loc.gov", new File(homeFolder, "id.loc.gov.rt"));
		agentVocabs.put(v.domain, v);
		v = new AgentVocabulary("data.bnf.fr", new File(homeFolder, "data.bnf.fr.rt"));
		agentVocabs.put(v.domain, v);
		v = new AgentVocabulary("vocab.getty.edu", new File(homeFolder, "getty.rt"));
		agentVocabs.put(v.domain, v);
		v = new AgentVocabulary("d-nb.info", new File(homeFolder, "gnd.rt"));
		agentVocabs.put(v.domain, v);
		v = new AgentVocabulary("dbpedia.org", new File(homeFolder, "dbpedia.rt"));
		agentVocabs.put(v.domain, v);
		v = new AgentVocabulary("www.wikidata.org", new File(homeFolder, "wikidata.rt"));
		agentVocabs.put(v.domain, v);
		return agentVocabs;
	}
}
