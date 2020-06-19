package inescid.dataaggregation.casestudies.coreference;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class ScriptCoreferenceIngesterEuropeanaVocabUrisFromRdfFile {
	
	File urisFile;
	RepositoryOfSameAs repoSameAs;
	SameAsSets sameAsSets;
	String datasetId;
	
	public ScriptCoreferenceIngesterEuropeanaVocabUrisFromRdfFile(File urisFile, String repoFolder, String datasetId) throws IOException {
		repoSameAs=new RepositoryOfSameAs(new File(repoFolder));
		this.urisFile = urisFile;
		this.datasetId = datasetId;
		sameAsSets=repoSameAs.getSameAsSet(datasetId);
	}

	public static void main(String[] args) throws Exception {
    	String csvUrisFile = "c://users/nfrei/desktop/data/coreference/agents.coref.csv";
    	String repoFolder = "c://users/nfrei/desktop/data/coreference";
//    	String repoFolderOut = "c://users/nfrei/desktop/data/coreference-updates";
    	String datasetIdOut;
//    	datasetId ="wikidata";
//    	datasetId ="data.bnf.fr";
//    	datasetId ="dbpedia";
//    	datasetIdX ="wikidata";
    	datasetIdOut =Consts.europeanaProviders_datasetId;
    	
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

		ScriptCoreferenceIngesterEuropeanaVocabUrisFromRdfFile corefFinder=new ScriptCoreferenceIngesterEuropeanaVocabUrisFromRdfFile(new File(csvUrisFile), repoFolder, datasetIdOut);
		corefFinder.runIngest();
				
		System.out.println("FININSHED TEST OF URIS");
	}


	private void runIngest() throws Exception {
		MapOfInts<String> setsCntByHost=new MapOfInts<String>();
		MapOfLists<String, String> urisByHost=new MapOfLists<String, String>();

		for(String uri: EuropeanaSameAsSets.getEuropeanaVocabUris(urisFile, true)) {
			Set<String> uriSet=new HashSet<String>();
			uriSet.add(uri);
			String uriClean= null;
			if(uri.contains(" ")) 
				uriClean=uri.replaceAll(" ", "");
			if(uri.endsWith("/") || uri.endsWith("#"))
				uriClean=uri.replaceAll("[#/]$", "");
			if(uriClean!=null) {
				uriSet.add(uriClean);
				uri=uriClean;
			}
			if(uri.startsWith("http://catalogue.bnf.fr")) {
				uriSet.add("https://data"+uri.substring("http://catalogue".length()));
			} else if(uri.startsWith("http://viaf.org/viaf/viaf/")) {
				uriSet.add("http://viaf.org/viaf/"+uri.substring("http://viaf.org/viaf/viaf/".length()));
			} else if(uri.startsWith("http://imslp.org/")) {
				uriSet.add("https"+uri.substring("http".length()));
			}
			for(String uriInSet : uriSet){
				Matcher matcher = Consts.HOST_PATTERN.matcher(uriInSet);
				if(!matcher.find()) continue;
				String host = matcher.group(1);
				if(!Consts.RDF_HOSTS.contains(host))
//				if(!rdfHosts.contains(host) || sameAsSets.getUriIndex().containsKey(uri))
					continue;
				urisByHost.put(host, uri);
			}
		}
		
		File repoFolder=repoSameAs.getHomeFolder();
		ArrayList<File> searchTargets=new ArrayList<File>();
		searchTargets.add(new File(repoFolder, Consts.europeanaProviders_datasetId+"."+Consts.RDF_SERIALIZATION.getFileExtensions().get(0)));
//		searchTargets.add(new File(repoFolder, Consts.europeanaProviders2nd_datasetId+"."+Consts.RDF_SERIALIZATION.getFileExtensions().get(0)));
//		searchTargets.add(new File(repoFolder, Consts.wikidata_datasetId+"."+Consts.RDF_SERIALIZATION.getFileExtensions().get(0)));
//		searchTargets.add(new File(repoFolder, Consts.dbPedia_datasetId+"."+Consts.RDF_SERIALIZATION.getFileExtensions().get(0)));
//		searchTargets.add(new File(repoFolder, Consts.dataBnfFr_datasetId+"."+Consts.RDF_SERIALIZATION.getFileExtensions().get(0)));
//		searchTargets.add(new File(repoFolder, Consts.gnd_datasetId+"."+Consts.RDF_SERIALIZATION.getFileExtensions().get(0)));
//		searchTargets.add(new File(repoFolder, Consts.getty_datasetId+"."+Consts.RDF_SERIALIZATION.getFileExtensions().get(0)));
		for(Iterator<File> it=searchTargets.iterator() ; it.hasNext() ; ) {
			if(!it.next().exists())
				it.remove();
		}

		HashSet<String> allUrisSet=new HashSet<>(urisByHost.valuesOfAllLists());
		
		for(File targetFile: searchTargets) {
			System.out.println("FILE START: "+targetFile.getName());
			FileInputStream fis=new FileInputStream(targetFile);
			RDFDataMgr.parse(new StreamRDFBase() {
				public void triple(Triple triple) {
					String subjUri = triple.getSubject().getURI();
					if(allUrisSet.contains(subjUri)) {
						String objUri = triple.getObject().getURI();
						sameAsSets.addSameAs(subjUri, objUri);
					}
				}
			}, fis, Consts.RDF_SERIALIZATION);
		}
		
		System.out.println("closing");
		repoSameAs.close();
		
		for(String host: setsCntByHost.keySet()) {
			System.out.println(host+" - "+setsCntByHost.get(host));
		}
	}
	
}
