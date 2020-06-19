package inescid.dataaggregation.casestudies.coreference.old;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.springframework.web.client.ResourceAccessException;

import inescid.dataaggregation.casestudies.coreference.Consts;
import inescid.dataaggregation.casestudies.coreference.EuropeanaSameAsSets;
import inescid.dataaggregation.casestudies.coreference.RepositoryOfSameAs;
import inescid.dataaggregation.casestudies.coreference.SameAsSets;
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

public class ScriptCoreferenceIngesterEuropeanaVocabUris {
	
	File urisFile;
	RepositoryOfSameAs repoSameAs;
	SameAsSets sameAsSets;
	String datasetId;
	
	public ScriptCoreferenceIngesterEuropeanaVocabUris(File urisFile, String repoFolder, String datasetId) {
		this.urisFile = urisFile;
		repoSameAs = new RepositoryOfSameAs(new File(repoFolder));
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

		ScriptCoreferenceIngesterEuropeanaVocabUris corefFinder=new ScriptCoreferenceIngesterEuropeanaVocabUris(new File(csvUrisFile), repoFolder, datasetIdOut);
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

		ThreadedRunner runner=new ThreadedRunner(urisByHost.keySet().size());
		for(final Entry<String, ArrayList<String>> hostUris : urisByHost.entrySet()) {
			runner.run(new Runnable() {
				public void run() {
					try {
						checkUris(hostUris.getKey(), hostUris.getValue(),setsCntByHost);
					} catch (Exception e) {
						System.err.println("Failed for "+hostUris.getKey());
						e.printStackTrace();
					}
				}
			});
		}
		System.out.println("waiting termination");
		runner.awaitTermination(0);
		System.out.println("closing");
		repoSameAs.close();
		
		for(String host: setsCntByHost.keySet()) {
			System.out.println(host+" - "+setsCntByHost.get(host));
		}
	}
	
	protected void checkUris(String host, Iterable<String> uris, MapOfInts<String> setsCntByHost) {
		System.out.println("Checking "+host);
		int cnt=0;
		for(String uri: uris) {
			cnt++;
//			System.out.println(host+" - "+cnt);
			if(cnt == 500 && setsCntByHost.get(host)==null) {
				System.out.println("Nothing found in first 500 URIs from "+host+" - quiting");
				break;
			}
			if(cnt % 100 == 0)
				System.out.println("Processed "+host+": "+cnt+" - "+setsCntByHost.get(host)+ " coref sets");
			Set<String> uriSet=checkAndGetResourceSameAs(uri);
			if(!uriSet.isEmpty()) {
				uriSet.add(uri);
//				sameAsSets.putSameAsSet(uriSet);
				sameAsSets.addSet(uriSet);
				sameAsSets.commit();
				setsCntByHost.incrementTo(host);
			}

		}
		System.out.println("Finished "+host+" - "+cnt+" URIs - "+setsCntByHost.get(host)+ " coref sets");
	}

	public static Set<String> checkAndGetResourceSameAs(String uri) {
		Resource ent=null;
		try {
			ent = RdfUtil.readRdfResourceFromUri(uri);
		} catch (AccessException e) {
		} catch (Exception e) {
			System.err.println("Error in uri: "+uri);
			e.printStackTrace();
		}
		if(ent==null) 
			return Collections.EMPTY_SET;
		Set<String> uriSet=new HashSet<String>();
		List<Statement> sameAsStms = checkAndGetResourceSameAsStatements(uri);
		for(Statement s: sameAsStms) {
			if(s.getObject().isURIResource()) 
				uriSet.add(s.getObject().asResource().getURI());
			else
				System.out.println(s);
		}
		return uriSet;
	}
	public static List<Statement> checkAndGetResourceSameAsStatements(String uri) {
		Resource ent=null;
		try {
			ent = RdfUtil.readRdfResourceFromUri(uri);
		} catch (AccessException e) {
		} catch (Exception e) {
			System.err.println("Error in uri: "+uri);
			e.printStackTrace();
		}
		if(ent==null) 
			return Collections.EMPTY_LIST;
		return RdfUtil.listProperties(ent, Owl.sameAs, Skos.exactMatch, Skos.closeMatch, Schemaorg.sameAs);
	}

}
