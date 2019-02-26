package inescid.dataaggregation.casestudies.wikidata;

import java.util.List;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import inescid.dataaggregation.casestudies.wikidata.WikidataSparqlClient.UriHandler;
import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.dataset.convert.RdfReg;
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
		if (alreadyChecked.contains(wdResource.getURI()))
			return;
		alreadyChecked.add(wdResource.getURI());
		
		boolean foundEquivalent=false;
		StmtIterator propStms = wdResource.listProperties(RdfRegWikidata.EQUIVALENT_CLASS);
		for (Statement st : propStms.toList()) {
//			System.out.println(st);
			String objUri = st.getObject().asNode().getURI();
			if(objUri.startsWith(RdfReg.NsSchemaOrg)) {
				wdEntPropEquivalences.put(leafResource.getURI(), objUri);
				foundEquivalent=true;
			}
		}
		if(!foundEquivalent) {
			propStms = wdResource.listProperties(RdfRegWikidata.SUBCLASS_OF);
			for (Statement st : propStms.toList()) {
//				System.out.println(st);
				String objUri = st.getObject().asNode().getURI();
				Resource wdScResource = MetadataAnalyzerOfCulturalHeritage.fetchresource(objUri, rdfCache);
				
//				SimpleEntry<byte[], List<Entry<String, String>>> fetched = rdfCache.fetchRdf(objUri);
//				if (fetched == null || fetched.getKey() == null || fetched.getKey().length == 0) {
//					System.out.printf("Access to %s failed\n", objUri);
//				} else {
//					Model rdfWikidata = RdfUtil.readRdf(fetched.getKey(),
//							RdfUtil.fromMimeType(HttpUtil.getHeader("Content-Type", fetched.getValue())));
//					if(rdfWikidata.size()==0)
//						continue;
//					
					analyzeEntity(wdScResource, leafResource);
				}	
		}
	}

	public void putEquivalence(String uriWd, String otherUri) {
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

		if (alreadyChecked.contains(propUri))
			return;
		alreadyChecked.add(propUri);
//		System.out.println("Analyzing "+propUri);

		
		try {
			Resource wdPropResource = MetadataAnalyzerOfCulturalHeritage.fetchresource(propUri, rdfCache);
//				System.out.println(new String(propFetched.getKey()));
//				System.out.println(propFetched.getValue());
//				for (Statement st : rdfWikidata.listStatements().toList()) {
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
//						
//						
//
//						Resource wdSubclassResource = MetadataAnalyzerOfCulturalHeritage.fetchresource(objUri, rdfCache);
////						SimpleEntry<byte[], List<Entry<String, String>>> fetched = rdfCache.fetchRdf(objUri);
////						if (fetched == null || fetched.getKey() == null || fetched.getKey().length == 0) {
////							System.out.printf("Access to %s failed\n", objUri);
////						} else {
////							Model rdfSubclassWikidata = RdfUtil.readRdf(fetched.getKey(),
////									RdfUtil.fromMimeType(HttpUtil.getHeader("Content-Type", fetched.getValue())));
////							Resource wdSubclassResource = rdfSubclassWikidata.getResource(objUri);
////							if(rdfSubclassWikidata.size()==0)
////								continue;
//							
//							StmtIterator subPropEqStms = wdSubclassResource.listProperties(RdfRegWikidata.EQUIVALENT_PROPERTY);
//							for (Statement stEq : subPropEqStms.toList()) {
//			//					System.out.println(st);
//								String eqResUri = stEq.getObject().asNode().getURI();
//								if(eqResUri.startsWith(RdfReg.NsSchemaOrg)) {
//									wdEntPropEquivalencesSuperclasses.put(propUri, eqResUri);
//								}
//							}
					}
			}
		} catch (Exception e) {
			System.out.printf("Access to %s failed\n", propUri);
			e.printStackTrace(System.out);
		}
	}

	public ArrayList<String> getEquivalence(String objUri) {
		ArrayList<String> ret=wdEntPropEquivalences.get(objUri);
		if(ret==null)
			ret=wdEntPropEquivalencesSuperclasses.get(objUri);
		return ret;
	}

	@Override
	public String toString() {
		return "EquivalenceMapping [wdEntPropEquivalences=" + wdEntPropEquivalences
				+ ", wdEntPropEquivalencesSuperclasses=" + wdEntPropEquivalencesSuperclasses + "]";
	}
	
	
	
	
}
