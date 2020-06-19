package inescid.dataaggregation.casestudies.coreference.semanticweb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
import org.apache.jena.riot.Lang;
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
import inescid.util.RdfUtil.Jena;
import inescid.util.SparqlClient;
import inescid.util.SparqlClient.Handler;
import inescid.util.ThreadedRunner;
import inescid.util.datastruct.MapOfInts;
import inescid.util.datastruct.MapOfLists;

public class ScriptExportInvalidViafUris {
	File repoFolder;
	
	
	public ScriptExportInvalidViafUris(String repoFolder) throws IOException {
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
		ScriptExportInvalidViafUris corefFinder=new ScriptExportInvalidViafUris(repoFolder);
		corefFinder.runStudy(startAt);
				
		System.out.println("FININSHED TEST OF URIS");
	}


	private void runStudy(Integer startAt) throws Exception {
//		List<String> urisList = FileUtils.readLines(new File(repoFolder, "providers-resolvable-uris.txt"), StandardCharsets.UTF_8);
//		urisList.addAll(FileUtils.readLines(new File(repoFolder, "providers-indirectly-resolvable-uris.txt"), StandardCharsets.UTF_8));
		
		Map<String, AgentVocabulary> studiedVocabs = AgentVocabsIntelinkingStudy.getStudiedVocabs(repoFolder);
		
		Model invalidSameAs=Jena.createModel();
		
		HashSet<String> invalidUris=new HashSet<String>();
		invalidUris.add("http://data.bnf.fr/#spatialThing");
		invalidUris.add("http://data.bnf.fr/#owl:Thing");
		invalidUris.add("http://data.bnf.fr/#foaf:Organization");
		invalidUris.add("http://data.bnf.fr/#foaf:Person");
			
		FileInputStream fis=new FileInputStream(studiedVocabs.get("viaf.org").sameAsStatements);
		RDFDataMgr.parse(new StreamRDFBase() {
			long cntInvalid=0;
			public void triple(Triple triple) {
				if(invalidUris.contains(triple.getObject().getURI())) {
					Statement s=Jena.createStatement(triple);
					invalidSameAs.add(s);
					cntInvalid++;
					System.out.println(cntInvalid);
				}
			}
		}, fis, Consts.RDF_SERIALIZATION);
		fis.close();

		Writer w=Files.newBufferedWriter(new File(repoFolder,"viaf-statements.nt").toPath() , StandardCharsets.UTF_8);
		RdfUtil.writeRdf(invalidSameAs, Lang.NTRIPLES, w);
		w.close();
	}
	
}
