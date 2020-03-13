package inescid.dataaggregation.casestudies.coreference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFBase;
import org.apache.jena.sparql.core.Quad;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

public class ScriptCoreferenceFinderInRdfFiles {
	RepositoryOfSameAs repoSameAs;
	EuropeanaSameAsSets sameAsSetsEuropeana;
	String datasetIdEuropeana;
	File repoFolder;

	public ScriptCoreferenceFinderInRdfFiles(String repoFolder, String urisCsvPath, String datasetIdEuropeana) throws IOException {
		this.repoFolder=new File(repoFolder);		
		repoSameAs = new RepositoryOfSameAs(this.repoFolder);
		this.datasetIdEuropeana = datasetIdEuropeana;
		
		sameAsSetsEuropeana=new EuropeanaSameAsSets(urisCsvPath, repoSameAs.getSameAsSet(datasetIdEuropeana));
	}

	public static void main(String[] args) throws Exception {
    	String csvUrisFile = "c://users/nfrei/desktop/data/coreference/agents.coref.csv";
    	String repoFolder = "c://users/nfrei/desktop/data/coreference";
    	String datasetIdEuropeana=Consts.europeanaProviders_datasetId;
//    	String csvTestUrisFile = null;
    	String csvTestUrisFile = "c://users/nfrei/desktop/data/coreference/agents.coref-testUris.csv";

		if(args!=null) {
			if(args.length>=1) {
				csvUrisFile = args[0];
				if(args.length>=2) {
					repoFolder = args[1];
					if(args.length>=3) {
						csvTestUrisFile = args[2];
					}
				}
			}
		}
//		Global.init_componentDataRepository(repoFolder);

		if(Consts.DEBUG && csvTestUrisFile!=null) {
			CoreferenceDebugger.init(new File(csvTestUrisFile));
			File useFile = new File(repoFolder, datasetIdEuropeana+"-mvstore.bin");
			File bckFile = new File(repoFolder, datasetIdEuropeana+"-mvstore2.bin");			
			FileUtils.copyFile(bckFile, useFile);
		} else
			Consts.DEBUG=false;
		
		ScriptCoreferenceFinderInRdfFiles corefFinder=new ScriptCoreferenceFinderInRdfFiles(repoFolder, csvUrisFile, datasetIdEuropeana);
		corefFinder.runSearchCoreferences();
		corefFinder.close();
				
		System.out.println("FININSHED: "+datasetIdEuropeana);
	}

	private void close() {
		repoSameAs.close();		
	}

	private void runSearchCoreferences() throws Exception {
		int cntProcessed=0;
		int cntCoref=0;
		
		ArrayList<File> searchTargets=new ArrayList<File>();
		searchTargets.add(new File(repoFolder, Consts.europeanaProviders2nd_datasetId+"."+Consts.RDF_SERIALIZATION.getFileExtensions().get(0)));
		searchTargets.add(new File(repoFolder, Consts.wikidata_datasetId+"."+Consts.RDF_SERIALIZATION.getFileExtensions().get(0)));
		searchTargets.add(new File(repoFolder, Consts.dbPedia_datasetId+"."+Consts.RDF_SERIALIZATION.getFileExtensions().get(0)));
		searchTargets.add(new File(repoFolder, Consts.dataBnfFr_datasetId+"."+Consts.RDF_SERIALIZATION.getFileExtensions().get(0)));
		searchTargets.add(new File(repoFolder, Consts.gnd_datasetId+"."+Consts.RDF_SERIALIZATION.getFileExtensions().get(0)));
		searchTargets.add(new File(repoFolder, Consts.getty_datasetId+"."+Consts.RDF_SERIALIZATION.getFileExtensions().get(0)));
		for(Iterator<File> it=searchTargets.iterator() ; it.hasNext() ; ) {
			if(!it.next().exists())
				it.remove();
		}

			CoreferenceDebugger.INSTANCE.debug(sameAsSetsEuropeana, "0-"+datasetIdEuropeana);
		
    	 boolean[] changed=new boolean[] {true};
    	 int cicle=0;
    	 while(changed[0]) {
	    	 changed[0]=false;
	    	 cicle++;
	    	 System.out.println("ITERATION START: "+cicle);
	    	 
//	    	 Set<String> allUris = new HashSet<String>(10000);
//	    	 for(String uri: allUrisIndexEuropeana.keySet()) 
//	    		 allUris.add(uri);

 			for(File targetFile: searchTargets) {
 			    MVStore.TxCounter txCounter = sameAsSetsEuropeana.getStore().registerVersionUsage();
 				try {
	 				System.out.println("FILE START: "+targetFile.getName());
	 				FileInputStream fis=new FileInputStream(targetFile);
	 				RDFDataMgr.parse(new StreamRDFBase() {
						public void triple(Triple triple) {
							boolean added = sameAsSetsEuropeana.addIfOverlap(triple.getSubject().getURI(), triple.getObject().getURI());
							changed[0]=changed[0] || added;
						}
					}, fis, Consts.RDF_SERIALIZATION);
	 				sameAsSetsEuropeana.getStore().commit();
 				} finally {
 	 			    sameAsSetsEuropeana.getStore().deregisterVersionUsage(txCounter);
 				}
 				
				CoreferenceDebugger.INSTANCE.debug(sameAsSetsEuropeana, cicle+"-"+targetFile.getName());
 			}
    	 }
    	 System.out.println("FINAL: "+cntProcessed +" checked; - "+cntCoref+" corefs");
	     sameAsSetsEuropeana.getUriIndex().getStore().commit();		
	}
}
