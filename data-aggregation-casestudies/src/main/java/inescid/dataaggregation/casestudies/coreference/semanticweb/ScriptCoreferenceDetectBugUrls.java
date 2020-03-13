package inescid.dataaggregation.casestudies.coreference.semanticweb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

import inescid.dataaggregation.casestudies.coreference.Consts;
import inescid.util.datastruct.MapOfInts;

public class ScriptCoreferenceDetectBugUrls {
	File repoFolder;

	public ScriptCoreferenceDetectBugUrls(String repoFolder) throws IOException {
		this.repoFolder=new File(repoFolder);		
	}

	public static void main(String[] args) throws Exception {
    	String repoFolder = "c://users/nfrei/desktop/data/coreference-semanticweb";

		if(args!=null) {
			if(args.length>=1) {
				repoFolder = args[0];
			}
		}
//		Global.init_componentDataRepository(repoFolder);

		ScriptCoreferenceDetectBugUrls corefFinder=new ScriptCoreferenceDetectBugUrls(repoFolder);
		corefFinder.runSearchBadUris(Consts.viaf_datasetId);
	}

	private void runSearchBadUris(String filename) throws Exception {
		
		File targetFile=new File(repoFolder, filename+"."+Consts.RDF_SERIALIZATION.getFileExtensions().get(0));
		System.out.println("FILE START: "+targetFile.getName());
		FileInputStream fis=new FileInputStream(targetFile);
		
		final MapOfInts<String> targetUriCount=new MapOfInts<String>();
		RDFDataMgr.parse(new StreamRDFBase() {
			long cntProcessed=0;
			public void triple(Triple triple) {
				cntProcessed++;
				if(cntProcessed < 1000000) {
					targetUriCount.incrementTo(triple.getObject().getURI());
				}
			}
		}, fis, Consts.RDF_SERIALIZATION);

		double average=(double)targetUriCount.total() / (double)targetUriCount.size();

		List<String> sortedKeysByInts = targetUriCount.getSortedKeysByInts();
		for(int i=0 ; i < 10 ; i++) {
			String uri=sortedKeysByInts.get(i);
			Integer cnt = targetUriCount.get(uri);
			if(cnt > 5 * average)
				System.out.println(uri+ " - "+cnt);
		}
//		for(int i=0 ; i < 10 ; i++) {
//			String uri=sortedKeysByInts.get(i);
//			System.out.println(uri+ " - "+targetUriCount.get(uri));
//		}
		targetUriCount.clear();

		
		
		
		
		
		System.out.println("FININSHED: "+targetFile.getName());
	}
}
