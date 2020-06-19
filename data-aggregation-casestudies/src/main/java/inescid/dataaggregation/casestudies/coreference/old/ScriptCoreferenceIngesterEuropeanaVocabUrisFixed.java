package inescid.dataaggregation.casestudies.coreference.old;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.springframework.web.client.ResourceAccessException;

import inescid.dataaggregation.casestudies.coreference.Consts;
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

public class ScriptCoreferenceIngesterEuropeanaVocabUrisFixed {
	
	File urisFile;
	RepositoryOfSameAs repoSameAs;
	SameAsSets sameAsSets;
	String datasetId;
	
	public ScriptCoreferenceIngesterEuropeanaVocabUrisFixed(File urisFile, String repoFolder, String datasetId) {
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
//    	datasetIdOut = Consts.europeanaProvidersFixed_datasetId;
    	datasetIdOut = null;
    	
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

		ScriptCoreferenceIngesterEuropeanaVocabUrisFixed corefFinder=new ScriptCoreferenceIngesterEuropeanaVocabUrisFixed(new File(csvUrisFile), repoFolder, datasetIdOut);
		corefFinder.runIngest();
				
		System.out.println("FININSHED ingestin fixes");
	}


	private void runIngest() throws Exception {
		BufferedReader fr=new BufferedReader(new FileReader(urisFile));
		CSVParser csvReader=new CSVParser(fr, CSVFormat.DEFAULT);
		for(CSVRecord rec : csvReader) {
			String uri= rec.get(0);
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
				uriSet.add("http://data"+uri.substring("http://catalogue".length()));
			} else if(uri.startsWith("http://viaf.org/viaf/viaf/")) {
				uriSet.add("http://viaf.org/viaf/"+uri.substring("http://viaf.org/viaf/viaf/".length()));
			} else if(uri.startsWith("http://imslp.org/")) {
				uriSet.add("https"+uri.substring("http".length()));
			}
			if(uriSet.size()>1)	{
				sameAsSets.addSet(uriSet);
				System.out.println(uriSet);
				sameAsSets.commit();
			}
		}
		csvReader.close();
		fr.close();
		repoSameAs.close();
	}
	

}
