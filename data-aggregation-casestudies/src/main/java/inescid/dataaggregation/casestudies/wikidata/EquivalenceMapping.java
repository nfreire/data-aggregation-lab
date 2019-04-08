package inescid.dataaggregation.casestudies.wikidata;

import java.util.List;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import inescid.dataaggregation.casestudies.wikidata.WikidataSparqlClient.UriHandler;
import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.dataset.convert.RdfReg;
import inescid.dataaggregation.dataset.profile.ClassUsageStats;
import inescid.util.AccessException;
import inescid.util.HttpUtil;
import inescid.util.RdfUtil;
import inescid.util.datastruct.MapOfLists;

public class EquivalenceMapping {
	final CachedHttpRequestService rdfCache;
	final HashSet<String> alreadyChecked=new HashSet<String>();

	public EquivalenceMapping(CachedHttpRequestService rdfCache) {
		this.rdfCache = rdfCache;
	}
	
	final MapOfLists<String, String> wdEntPropEquivalences = new MapOfLists<String, String>();
	final MapOfLists<String, String> wdEntPropEquivalencesSuperclasses = new MapOfLists<String, String>();
	
	public void analyzeEntity(Resource wdResource, Resource leafResource) throws AccessException, InterruptedException, IOException {
		if (alreadyChecked.contains(wdResource.getURI())) {
			if(! wdResource.getURI().contentEquals(leafResource.getURI())) {
				ArrayList<String> equivalence = getEquivalence(wdResource.getURI(), true);
				if(equivalence!=null) {
					for(String eq: equivalence)
						putEquivalenceOfSuperclass(leafResource.getURI(), eq);
				}
			}
			return;
		}
		alreadyChecked.add(wdResource.getURI());
		
		boolean foundEquivalent=false;
		StmtIterator propStms = wdResource.listProperties(RdfRegWikidata.EQUIVALENT_CLASS);
		for (Statement st : propStms.toList()) {
//			System.out.println(st);
			String objUri = st.getObject().asNode().getURI();
			if(objUri.startsWith(RdfReg.NsSchemaOrg)) {
				if(wdResource.getURI().contentEquals(leafResource.getURI())) {
					wdEntPropEquivalences.put(leafResource.getURI(), objUri);
				} else {
					wdEntPropEquivalences.put(wdResource.getURI(), objUri);
					wdEntPropEquivalencesSuperclasses.put(leafResource.getURI(), objUri);
				}
				foundEquivalent=true;
			}
		}
		if(!foundEquivalent) {
			propStms = wdResource.listProperties(RdfRegWikidata.SUBCLASS_OF);
			for (Statement st : propStms.toList()) {
//				System.out.println(st);
				String objUri = st.getObject().asNode().getURI();
				Resource wdScResource = MetadataAnalyzerOfCulturalHeritage.fetchresource(objUri, rdfCache);
					analyzeEntity(wdScResource, leafResource);
			}	
		}
	}

	public void putEquivalence(String uriWd, String otherUri) {
		wdEntPropEquivalences.put(uriWd, otherUri);
	}
	
	public void putEquivalenceOfSuperclass(String uriWd, String otherUri) {
		wdEntPropEquivalences.put(uriWd, otherUri);
	}

	public void analyzeProperty(String propUri, String leafPropertyUri) {
//		public void analyzeProperty(String propUri, String entityResourceUri) {
		if(!propUri.startsWith("http://www.wikidata.org/")) {
			String[] wdEqUri=new String[1];
			WikidataSparqlClient.query("SELECT ?item WHERE { ?item wdt:"+RdfRegWikidata.EQUIVALENT_PROPERTY.getLocalName()+" <"+propUri+"> .", new UriHandler() {
//				WikidataSparqlClient.query("SELECT ?item WHERE { ?item wdt:"+RdfRegWikidata.EQUIVALENT_PROPERTY.getLocalName()+" <"+entityResourceUri+"> .", new UriHandler() {
				public boolean handleUri(String uri) throws Exception {
					wdEqUri[0]=uri;
					return false;
				}
			});
			propUri=wdEqUri[0];
			if(propUri==null)
				return;
		}
		if(!propUri.startsWith(RdfRegWikidata.NsWd)) 
			propUri="http://www.wikidata.org/entity/"+propUri.substring(propUri.lastIndexOf('/')+1);

		if (alreadyChecked.contains(propUri)) {	
			if(getEquivalence(propUri, true) != null && ! propUri.equals(leafPropertyUri)) {
				ArrayList<String> equivalence = getEquivalence(propUri, true);
				if(equivalence!=null) {
					for(String eq: equivalence)
						putEquivalenceOfSuperclass(leafPropertyUri, eq);
				}
			}
			return;
		}
		alreadyChecked.add(propUri);
//		System.out.println("Analyzing "+propUri);

		
		try {
			Resource wdPropResource = MetadataAnalyzerOfCulturalHeritage.fetchresource(propUri, rdfCache);
				boolean foundEquivalent=false;
				StmtIterator typeProperties = wdPropResource.listProperties(RdfRegWikidata.EQUIVALENT_PROPERTY);
				for (Statement st : typeProperties.toList()) {
//				System.out.println(st);
					String objUri = st.getObject().asNode().getURI();
					if(objUri.startsWith(RdfReg.NsSchemaOrg) || objUri.startsWith(RdfReg.NsRdf)) {
						wdEntPropEquivalences.put(leafPropertyUri, objUri);
						foundEquivalent=true;
					}
				}
				if(!foundEquivalent) {			
					StmtIterator propStms = wdPropResource.listProperties(RdfRegWikidata.SUBCLASS_OF);
					for (Statement st : propStms.toList()) {
//						System.out.println(st);
						String objUri = st.getObject().asNode().getURI();
						
						analyzeProperty(objUri, leafPropertyUri);
					}
					propStms = wdPropResource.listProperties(RdfRegWikidata.SUBPROPERTY_OF);
					for (Statement st : propStms.toList()) {
//						System.out.println(st);
						String objUri = st.getObject().asNode().getURI();
						
						analyzeProperty(objUri, leafPropertyUri);
						foundEquivalent=true;
					}
				}
		} catch (Exception e) {
			System.out.printf("Access to %s failed\n", propUri);
			e.printStackTrace(System.out);
		}
	}

	public ArrayList<String> getEquivalence(String objUri, boolean acceptSuperEquivalences) {
		ArrayList<String> ret=wdEntPropEquivalences.get(objUri);
		if(ret==null && acceptSuperEquivalences)
			ret=wdEntPropEquivalencesSuperclasses.get(objUri);
		return ret;
	}

	@Override
	public String toString() {
		return "EquivalenceMapping [wdEntPropEquivalences=" + wdEntPropEquivalences
				+ ", wdEntPropEquivalencesSuperclasses=" + wdEntPropEquivalencesSuperclasses + "]";
	}
	
	public String toCsv() {
		ArrayList<String> sorted=new ArrayList<String>(wdEntPropEquivalences.keySet());
		sorted.addAll(wdEntPropEquivalencesSuperclasses.keySet());
		Collections.sort(sorted);
		try {
			StringBuilder sbCsv=new StringBuilder();
			CSVPrinter csv=new CSVPrinter(sbCsv, CSVFormat.DEFAULT);
			csv.printRecord("URI","Equivalences","Equivalences (generic)");
			int eqCnt=0;
			int eqGenCnt=0;
			for(String uri: sorted) {
				boolean eqExists=wdEntPropEquivalences.containsKey(uri);
				boolean eqGenExists=!eqExists && wdEntPropEquivalencesSuperclasses.containsKey(uri);
				if (eqExists)
					eqCnt++;
				else
					eqGenCnt++;
				csv.printRecord(uri, eqExists, eqGenExists);
			}		
			csv.printRecord("Total URIs", "Equivalences","Equivalences (generic)");
			csv.printRecord(sorted.size(), eqCnt, eqGenCnt);
			csv.close();
			return sbCsv.toString();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	
	
}
