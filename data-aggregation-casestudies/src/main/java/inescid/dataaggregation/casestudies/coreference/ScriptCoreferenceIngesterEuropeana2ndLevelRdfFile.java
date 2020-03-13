package inescid.dataaggregation.casestudies.coreference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.system.StreamOps;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import inescid.dataaggregation.casestudies.coreference.old.ScriptCoreferenceIngesterEuropeanaVocabUris;
import inescid.dataaggregation.dataset.Global;

public class ScriptCoreferenceIngesterEuropeana2ndLevelRdfFile {
	RepositoryOfSameAs repoSameAs;
	EuropeanaSameAsSets sameAsSetsEuropeana;
	String datasetIdEuropeana;
	String datasetIdOut;

	public ScriptCoreferenceIngesterEuropeana2ndLevelRdfFile(String repoFolder, String urisCsvPath, String datasetIdEuropeana, String datasetIdOut) throws IOException {
		repoSameAs = new RepositoryOfSameAs(new File(repoFolder));
		this.datasetIdEuropeana = datasetIdEuropeana;
		this.datasetIdOut=datasetIdOut;
		
		sameAsSetsEuropeana=new EuropeanaSameAsSets(urisCsvPath, repoSameAs.getSameAsSet(datasetIdEuropeana));		
	}

	public static void main(String[] args) throws Exception {
    	String csvUrisFile = "c://users/nfrei/desktop/data/coreference/agents.coref.csv";
    	String repoFolder = "c://users/nfrei/desktop/data/coreference";
    	String datasetIdOut=Consts.europeanaProviders2nd_datasetId;
    	String datasetIdEuropeana=Consts.europeanaProviders_datasetId;

		if(args!=null) {
			if(args.length>=1) {
				csvUrisFile = args[0];
				if(args.length>=2) {
					repoFolder = args[1];
					if(args.length>=3) {
						datasetIdOut = args[2];
					}
				}
			}
		}
		Global.init_componentHttpRequestService();

		ScriptCoreferenceIngesterEuropeana2ndLevelRdfFile corefFinder=new ScriptCoreferenceIngesterEuropeana2ndLevelRdfFile(repoFolder, csvUrisFile, datasetIdEuropeana, datasetIdOut);
		corefFinder.runIngestCoreferences();
		corefFinder.close();
				
		System.out.println("FININSHED: "+datasetIdEuropeana +" - " + datasetIdOut);
	}

	private void close() {
		repoSameAs.close();		
	}

	private void runIngestCoreferences() throws Exception {
		FileOutputStream fos=new FileOutputStream(new File(repoSameAs.homeFolder, datasetIdOut+"."+Consts.RDF_SERIALIZATION.getFileExtensions().get(0)));
		StreamRDF writer = StreamRDFWriter.getWriterStream(fos, Consts.RDF_SERIALIZATION) ;		
		ArrayList<Triple> toAddTripleAux=new ArrayList<Triple>(1) {{ add(null); }};

		try {
			int cntProcessed=0;
			int cntCoref=0;
		
			HashSet<String> newLocUris=new HashSet<String>();
			
	    	 for(String uri: sameAsSetsEuropeana.keySetOfVocabUris()) {
	    		Set<String> ySet = sameAsSetsEuropeana.getUriSet(uri);
	    		if (ySet==null) continue;
				for(String uriInSet: ySet) {
					if(uriInSet.equals(uri))
						continue;
					Matcher matcher = Consts.HOST_PATTERN.matcher(uriInSet);
					if(!matcher.find() ) continue;
					
					List<Statement> stms=ScriptCoreferenceIngesterEuropeanaVocabUris.checkAndGetResourceSameAsStatements(uriInSet);

					if(stms!=null) for(Statement s: stms) {
						Matcher matcher2 = Consts.HOST_PATTERN.matcher(s.getObject().asResource().getURI());
						if(matcher2.find()) {
							String linkHost=matcher2.group(1);
							if(linkHost.equals("id.loc.gov"))
								newLocUris.add(uriInSet);
						}
						
						Triple t=s.asTriple();
						toAddTripleAux.set(0, t);
						StreamOps.sendTriplesToStream(toAddTripleAux.iterator(), writer);
						cntCoref++;
					}
				}
				 cntProcessed++;
				 if(cntProcessed % 1000 == 0) { 
					 System.out.println(cntProcessed +" checked; - "+cntCoref+" corefs");
					 fos.flush();
				 }
	    	 }
	    	 System.out.println("Checking id.loc.gov");
	    	 for(String locUri: newLocUris) {
	    		 List<Statement> uriSet=ScriptCoreferenceIngesterEuropeanaVocabUris.checkAndGetResourceSameAsStatements(locUri);
				if(uriSet!=null) for(Statement s: uriSet) {
					Triple t=s.asTriple();
					toAddTripleAux.set(0, t);
					StreamOps.sendTriplesToStream(toAddTripleAux.iterator(), writer);
					cntCoref++;
				}
				cntProcessed++;
				if(cntProcessed % 1000 == 0) { 
					System.out.println(cntProcessed +" checked; - "+cntCoref+" corefs");
					fos.flush();
				}
	    	 }
	    	 System.out.println("FINAL: "+cntProcessed +" checked; - "+cntCoref+" corefs");
		}finally {
			fos.close();
		}
	}
	
}
