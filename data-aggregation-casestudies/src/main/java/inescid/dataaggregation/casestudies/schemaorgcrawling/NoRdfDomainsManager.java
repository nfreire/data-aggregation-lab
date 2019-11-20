package inescid.dataaggregation.casestudies.schemaorgcrawling;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;

import org.apache.jena.rdf.model.Resource;

import inescid.opaf.data.profile.MapOfInts;
import inescid.util.RdfUtil.Jena;

public class NoRdfDomainsManager {
	private static int minNegativeSamples=100;
	
	HashSet<String> blackList=new HashSet<String>();
	HashSet<String> whiteList=new HashSet<String>();
	
	MapOfInts<String> blackListCandidates=new MapOfInts<String>();
	
	public void reportFailure(String uri) {
		String domain=uriToDomain(uri);
		if(domain==null)
			return;
		blackListCandidates.incrementTo(domain);
		if(blackListCandidates.get(domain)>=minNegativeSamples) {
			blackList.add(domain);
			blackListCandidates.remove(domain);
			System.out.println("WARNING: "+domain+" blacklisted. No RDF.");
		}			
	}
	
	public void reportSuccess(String uri) {
		String domain=uriToDomain(uri);
		whiteList.add(domain);
		blackListCandidates.remove(domain);
	}
	
	public boolean isBlackListed(String uri) {
		String domain = uriToDomain(uri);
		if(domain==null)
			return false;
		return blackList.contains(domain);
	}
	
	
	private String uriToDomain(String uri) {
		try {
			return new URI(uri).getHost();
		} catch (URISyntaxException e) {
			return "invalid_uri_syntax";
		}
	}
}
