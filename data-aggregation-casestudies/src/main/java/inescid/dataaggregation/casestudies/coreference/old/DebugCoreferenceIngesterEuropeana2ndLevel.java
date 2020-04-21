package inescid.dataaggregation.casestudies.coreference.old;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.system.StreamOps;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import inescid.dataaggregation.dataset.Global;

public class DebugCoreferenceIngesterEuropeana2ndLevel {
//	RepositoryOfSameAs repoSameAs;
//	EuropeanaSameAsSets sameAsSetsEuropeana;
//	File testUrisCsv;
//
//	public DebugCoreferenceIngesterEuropeana2ndLevel(String repoFolder, String urisCsvPath, String testUrisCsv) throws IOException {
//		repoSameAs = new RepositoryOfSameAs(new File(repoFolder));
//		this.testUrisCsv=new File(testUrisCsv);
//		if(!this.testUrisCsv.exists())
//			throw new IllegalArgumentException("Does not exist: "+testUrisCsv);
//		sameAsSetsEuropeana=new EuropeanaSameAsSets(urisCsvPath, repoSameAs.getSameAsSet(Consts.europeanaProviders_datasetId));		
//	}
//
//	public static void main(String[] args) throws Exception {
//    	String csvUrisFile = "c://users/nfrei/desktop/data/coreference/agents.coref.csv";
//    	String repoFolder = "c://users/nfrei/desktop/data/coreference";
//    	String testUrisCsv= "c://users/nfrei/desktop/data/coreference/agents.coref-testUris.csv";
//
//		if(args!=null) {
//			if(args.length>=1) {
//				csvUrisFile = args[0];
//				if(args.length>=2) {
//					repoFolder = args[1];
//					if(args.length>=3) {
//						testUrisCsv = args[2];
//					}
//				}
//			}
//		}
//		Global.init_componentHttpRequestService();
//
//		DebugCoreferenceIngesterEuropeana2ndLevel corefFinder=new DebugCoreferenceIngesterEuropeana2ndLevel(repoFolder, csvUrisFile, testUrisCsv);
//		corefFinder.runDebugCoreferences();
//		corefFinder.close();
//				
//		System.out.println("FININSHED: " + testUrisCsv);
//	}
//
//	private void close() {
//		repoSameAs.close();		
//	}
//
//	private void runDebugCoreferences() throws Exception {
//		File vocabCsvFile=new File(testUrisCsv.getParentFile(), "test-01-vocab.csv");
//		File vocab2ndCsvFile=new File(testUrisCsv.getParentFile(), "test-01-vocab2nd.csv");
//		
//		HashSet<String> testUris=new HashSet<String>();
//		CSVParser urisParser=new CSVParser(new FileReader(testUrisCsv), CSVFormat.DEFAULT);
//		for(CSVRecord rec:urisParser) {
//			testUris.add(rec.get(0));
//		}
//		
//		CSVPrinter vocabUrisPrinter=new CSVPrinter(new FileWriter(vocabCsvFile), CSVFormat.DEFAULT);
//		CSVPrinter vocab2ndUrisPrinter=new CSVPrinter(new FileWriter(vocab2ndCsvFile), CSVFormat.DEFAULT);
////		ArrayList<Triple> toAddTripleAux=new ArrayList<Triple>(1) {{ add(null); }};
//		try {
//			int cntProcessed=0;
//			int cntCoref=0;
//
//			for(String uri: testUris) {
//				vocabUrisPrinter.printRecord(uri, sameAsSetsEuropeana.getUriSet(uri).toString());
//			}
//			vocabUrisPrinter.close();
//			
//			
//	    	for(String uri: sameAsSetsEuropeana.keySetOfVocabUris()) {
//	    		Set<String> ySet = sameAsSetsEuropeana.getUriSet(uri);
//	    		if(ySet.size()==1)
//	    			continue;
//				for(String uriInSet: ySet) {
//					if(uriInSet.equals(uri))
//						continue;
//					Matcher matcher = Consts.HOST_PATTERN.matcher(uriInSet);
//					if(!matcher.find() || Consts.SPARQL_INGESTED_HOSTS.contains(matcher.group(1)) ) continue;
//					
//					List<Statement> stms=ScriptCoreferenceIngesterEuropeanaVocabUris.checkAndGetResourceSameAsStatements(uriInSet);
//
//					if(stms!=null) for(Statement s: stms) {
//						Matcher matcher2 = Consts.HOST_PATTERN.matcher(s.getObject().asResource().getURI());
//						if(matcher2.find()) {
//							String linkHost=matcher2.group(1);
//							if(linkHost.equals("id.loc.gov"))
//								newLocUris.add(uriInSet);
//						}
//						
//						Triple t=s.asTriple();
//						toAddTripleAux.set(0, t);
//						StreamOps.sendTriplesToStream(toAddTripleAux.iterator(), writer);
//						cntCoref++;
//					}
//				}
//				 cntProcessed++;
//				 if(cntProcessed % 1000 == 0) { 
//					 System.out.println(cntProcessed +" checked; - "+cntCoref+" corefs");
//					 fos.flush();
//				 }
//	    	 }
//	    	 System.out.println("Checking id.loc.gov");
//	    	 for(String locUri: newLocUris) {
//	    		 List<Statement> uriSet=ScriptCoreferenceIngesterEuropeanaVocabUris.checkAndGetResourceSameAsStatements(locUri);
//				if(uriSet!=null) for(Statement s: uriSet) {
//					Triple t=s.asTriple();
//					toAddTripleAux.set(0, t);
//					StreamOps.sendTriplesToStream(toAddTripleAux.iterator(), writer);
//					cntCoref++;
//				}
//				cntProcessed++;
//				if(cntProcessed % 1000 == 0) { 
//					System.out.println(cntProcessed +" checked; - "+cntCoref+" corefs");
//					fos.flush();
//				}
//	    	 }
//	    	 System.out.println("FINAL: "+cntProcessed +" checked; - "+cntCoref+" corefs");
//		}finally {
//			fos.close();
//		}
//	}
	
}
