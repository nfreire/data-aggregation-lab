package inescid.dataaggregation.casestudies.coreference.old;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import inescid.dataaggregation.wikidata.RdfRegWikidata;
import inescid.dataaggregation.wikidata.WikidataUtil;
import inescid.dataaggregation.wikidata.SparqlClientWikidata;
import inescid.dataaggregation.data.model.Owl;
import inescid.dataaggregation.dataset.Global;
import inescid.util.AccessException;
import inescid.util.HttpUtil;
import inescid.util.RdfUtil;
import inescid.util.SparqlClient;
import inescid.util.SparqlClient.Handler;
import inescid.util.datastruct.MapOfInts;

public class ScriptCheckEuropeanaVocabHosts {
	File urisFile;
	
	public ScriptCheckEuropeanaVocabHosts(File urisFile) {
		this.urisFile = urisFile;
	}

	public static void main(String[] args) throws Exception {
    	String csvUrisFile = "c://users/nfrei/desktop/data/coreference/agents.csv";
    	
		if(args!=null) {
			if(args.length>=1) {
				csvUrisFile = args[0];
			}
		}
		Global.init_componentHttpRequestService();

		ScriptCheckEuropeanaVocabHosts corefFinder=new ScriptCheckEuropeanaVocabHosts(new File(csvUrisFile));
		corefFinder.runTest();
				
		System.out.println("FININSHED TEST OF URIS");
	}


	private void runTest() throws Exception {
		final Pattern hostPattern=Pattern.compile("^https?://([^/]+)/");
		
		Set<String> withData=new HashSet<String>();
		Set<String> withoutData=new HashSet<String>();
		MapOfInts<String> checks=new MapOfInts<String>();
		MapOfInts<String> errors=new MapOfInts<String>();
		
		int cnt=0;
		BufferedReader fr=new BufferedReader(new FileReader(urisFile));
		while(fr.ready()) {
			String uri = fr.readLine().trim();
			cnt++;
			if(cnt % 10000 == 0)
				System.out.println("Processed: "+cnt);
			Matcher matcher = hostPattern.matcher(uri);
			if(matcher.find()) {
				String host=matcher.group(1);
				if(withData.contains(host) || withoutData.contains(host))
					continue;
				if(RdfUtil.isUriResolvable(uri)) {
					checks.remove(host);
					withData.add(host);
				} else {
					checks.incrementTo(host);
					if(checks.get(host)>=10) {
						checks.remove(host);
						withoutData.add(host);
					}
				}
			}
		}
		
		System.out.println("\nWITH DATA:");
		for(String host: withData) {
			System.out.println(host);
		}
		System.out.println("\nWITHOUT DATA:");
		for(String host: withoutData) {
			System.out.println(host);
		}
		for(String host: checks.keySet()) {
			System.out.println(host);
		}
	}
	

	
}
