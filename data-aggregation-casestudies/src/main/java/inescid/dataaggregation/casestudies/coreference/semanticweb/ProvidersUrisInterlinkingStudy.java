package inescid.dataaggregation.casestudies.coreference.semanticweb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamOps;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFBase;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.springframework.web.client.ResourceAccessException;

import inescid.dataaggregation.casestudies.coreference.Consts;
import inescid.dataaggregation.casestudies.coreference.semanticweb.AgentVocabsIntelinkingStudy.AgentVocabulary;
import inescid.dataaggregation.wikidata.RdfRegWikidata;
import inescid.dataaggregation.wikidata.WikidataUtil;
import inescid.dataaggregation.wikidata.SparqlClientWikidata;
import inescid.dataaggregation.data.model.Owl;
import inescid.dataaggregation.data.model.Schemaorg;
import inescid.dataaggregation.data.model.Skos;
import inescid.dataaggregation.dataset.Global;
import inescid.util.AccessException;
import inescid.util.HttpUtil;
import inescid.util.RdfUtil;
import inescid.util.SparqlClient;
import inescid.util.SparqlClient.Handler;
import inescid.util.ThreadedRunner;
import inescid.util.datastruct.MapOfInts;
import inescid.util.datastruct.MapOfLists;

public class ProvidersUrisInterlinkingStudy {
	File repoFolder;
	
	
	public ProvidersUrisInterlinkingStudy(String repoFolder) throws IOException {
		this.repoFolder = new File(repoFolder);
	}

	public static void main(String[] args) throws Exception {
    	String repoFolder = "c://users/nfrei/desktop/data/coreference-semanticweb";
    	Integer startAt=0;
    	
		if(args!=null) {
			if(args.length>=1) {
				repoFolder = args[0];
				if(args.length>=2) {
					startAt = Integer.parseInt(args[1]);
				}
			}
		}
		ProvidersUrisInterlinkingStudy corefFinder=new ProvidersUrisInterlinkingStudy(repoFolder);
		corefFinder.runStudy(startAt);
				
		System.out.println("FININSHED TEST OF URIS");
	}


	private void runStudy(Integer startAt) throws Exception {
		final int matchingIterations=4;
		final int urisPerBatch=4000;
		List<String> urisList = FileUtils.readLines(new File(repoFolder, "providers-resolvable-uris.txt"), StandardCharsets.UTF_8);
		urisList.addAll(FileUtils.readLines(new File(repoFolder, "providers-indirectly-resolvable-uris.txt"), StandardCharsets.UTF_8));
		
		Map<String, AgentVocabulary> studiedVocabs = AgentVocabsIntelinkingStudy.getStudiedVocabs(repoFolder);
		
		HashSet<String> badUris=new HashSet<String>();
		for(AgentVocabulary vocab : studiedVocabs.values()) {
			if(!vocab.sameAsStatements.exists()) {
				System.out.println("no file "+vocab.sameAsStatements.getName());
				continue;
			}
			System.out.println(vocab.sameAsStatements.getName());
			final MapOfInts<String> targetUriCount=new MapOfInts<String>();
			FileInputStream fis=new FileInputStream(vocab.sameAsStatements);
			RDFDataMgr.parse(new StreamRDFBase() {
				long cntProcessed=0;
				public void triple(Triple triple) {
					cntProcessed++;
					if(cntProcessed < 1000000) {
						targetUriCount.incrementTo(triple.getObject().getURI());
					}
				}
			}, fis, Consts.RDF_SERIALIZATION);
			fis.close();
			
			double average=(double)targetUriCount.total() / (double)targetUriCount.size();
			List<String> sortedKeysByInts = targetUriCount.getSortedKeysByInts();
			for(int i=0 ; i < 10 ; i++) {
				String uri=sortedKeysByInts.get(i);
				Integer cnt = targetUriCount.get(uri);
				if(cnt > 5 * average) {
					badUris.add(uri);
					System.out.println(uri+ " - "+cnt+"  in avg.: "+average);
				}
			}
		}

		System.out.println("Bad links detected: "+badUris);
//		System.exit(0);
		
		for(int batch=startAt==null ? 0 : startAt ; batch<urisList.size(); batch+=urisPerBatch) {
			for(boolean incluseCloseMatch : new boolean[] { true, false }) {
				SameAsSetsByUri providersLinkSets=new SameAsSetsByUri(urisList, batch, Math.min(urisList.size(), batch+urisPerBatch));	
				providersLinkSets.setDisregardedUris(badUris);
				ArrayList<AgentVocabulary> providersVoc=new ArrayList<AgentVocabsIntelinkingStudy.AgentVocabulary>(1) {{
					add(new AgentVocabulary("providers", new File(repoFolder, "providers.rt")));
				}};
//				String baseFilename="linking-"+(incluseCloseMatch ? "no_close_match" : "all_matches")+"-step_";
				String baseFilename="linking-"+(incluseCloseMatch ? "no_close_match" : "all_matches")+"-batch_"+batch+"-step_";
				System.out.println("At: "+baseFilename+"0");
				providersLinkSets.searchLinksIn(providersVoc, incluseCloseMatch);
				providersLinkSets.saveAsCsv(new File(repoFolder, baseFilename+"0"+".csv"));
				for(int iter=1; iter <= matchingIterations ; iter++) {
					System.out.println("At: "+baseFilename+iter);
					providersLinkSets.searchLinksIn(studiedVocabs.values(), incluseCloseMatch);
					providersLinkSets.saveAsCsv(new File(repoFolder, baseFilename+iter+".csv"));
				}
			}
		}
		System.out.println("closing");
	}
	


}
