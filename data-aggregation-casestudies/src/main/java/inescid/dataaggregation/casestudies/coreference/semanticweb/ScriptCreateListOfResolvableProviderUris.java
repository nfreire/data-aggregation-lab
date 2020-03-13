package inescid.dataaggregation.casestudies.coreference.semanticweb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import inescid.dataaggregation.casestudies.coreference.Consts;
import inescid.dataaggregation.casestudies.coreference.EuropeanaSameAsSets;
import inescid.dataaggregation.casestudies.coreference.semanticweb.AgentVocabsIntelinkingStudy.AgentVocabulary;
import inescid.dataaggregation.casestudies.coreference.semanticweb.AgentVocabsIntelinkingStudy.LinkPredicateCounts;

public class ScriptCreateListOfResolvableProviderUris {


	public static void main(String[] args) throws Exception {
    	String repoFolder = "c://users/nfrei/desktop/data/coreference-semanticweb";
    	if(args!=null) {
			if(args.length>=1) 
				repoFolder = args[0];
    	}
		new ScriptCreateListOfResolvableProviderUris(new File(repoFolder)).run();
	}
	
	File homeFolder;
	
	public ScriptCreateListOfResolvableProviderUris(File homeFolder) {
		this.homeFolder = homeFolder;
	}

	public void run() throws IOException {
		int nonResolvable=0;
		int notUri=0;
		int resolvable=0;
		int relativeToCho=0;
		int cnt=0;
		
		HashSet<String> resolvableUris=new HashSet<String>(10000);
		HashSet<String> rdfNonResolvableUris=new HashSet<String>(10000);
		HashSet<String> relativeUris=new HashSet<String>(10000);
		
		for(String uri: getEuropeanaVocabUrisDataCleaned()) {
			cnt++;
			String host = Util.getHost(uri);
			if(host==null)
				notUri++;
			else if(Consts.RDF_HOSTS_RESOLVABLE.contains(host)) {
				if(uri.contains("#")) {
					relativeToCho++;
					relativeUris.add(uri);
				}
				resolvable++;
				resolvableUris.add(uri);
			} else if(Consts.RDF_HOSTS_NON_RESOLVABLE.contains(host)) {
				if(uri.contains("#")) {
					relativeToCho++;
					relativeUris.add(uri);
				}
				resolvable++;
				rdfNonResolvableUris.add(uri);
			} else {
				nonResolvable++;
			}
			if(cnt%10000==0) {
				System.out.println(cnt+" invalid:"+ notUri+" relative:"+ relativeToCho+" resolvable:"+ resolvable+" not_resolv:"+ nonResolvable);
			}
		}
		System.out.println("FINAL RESULTS");
		System.out.println(cnt+" invalid:"+ notUri+" relative:"+ relativeToCho+" resolvable:"+ resolvable+" not_resolv:"+ nonResolvable);

		FileOutputStream writer=new FileOutputStream(new File(homeFolder, "providers-resolvable-uris.txt"));
		for(String uri: resolvableUris) {
			writer.write((uri+"\n").getBytes("UTF-8"));
		}
		writer.close();

		writer=new FileOutputStream(new File(homeFolder, "providers-indirectly-resolvable-uris.txt"));
		for(String uri: rdfNonResolvableUris) {
			writer.write((uri+"\n").getBytes("UTF-8"));
		}
		writer.close();
//		
//		writer=new FileOutputStream(new File(homeFolder, "providers-relative-uris.csv"));
//		for(String uri: relativeUris) {
//			writer.write((uri+"\n").getBytes("UTF-8"));
//		}
//		writer.close();
		
	}
	
	private List<String> getEuropeanaVocabUrisDataCleaned() throws IOException{
		File urisFile=new File(homeFolder, "agents.coref.csv");
		List<String> ret=new ArrayList<String>();
		for(String uri: EuropeanaSameAsSets.getEuropeanaVocabUris(urisFile, false)) {
			String uriClean= null;
			if(uri.contains(" ")) 
				uriClean=uri.replaceAll(" ", "");
			if(uri.endsWith("/") || uri.endsWith("#"))
				uriClean=uri.replaceAll("[#/]$", "");
			if(uriClean!=null) 
				uri=uriClean;
			if(uri.startsWith("http://catalogue.bnf.fr")) {
				uri="https://data"+uri.substring("http://catalogue".length());
			} else if(uri.startsWith("http://viaf.org/viaf/viaf/")) {
				uri="http://viaf.org/viaf/"+uri.substring("http://viaf.org/viaf/viaf/".length());
			} else if(uri.startsWith("http://imslp.org/")) {
				uri="https"+uri.substring("http".length());
			}	
			ret.add(uri);
		}
		return ret;
	}

	
}
