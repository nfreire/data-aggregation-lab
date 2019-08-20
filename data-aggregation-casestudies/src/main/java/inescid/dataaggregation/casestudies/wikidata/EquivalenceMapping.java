package inescid.dataaggregation.casestudies.wikidata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import inescid.dataaggregation.casestudies.wikidata.WikidataSparqlClient.UriHandler;
import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.data.RdfReg;
import inescid.dataaggregation.data.RdfRegRdf;
import inescid.dataaggregation.data.RdfRegRdfs;
import inescid.util.AccessException;
import inescid.util.datastruct.MapOfLists;

public class EquivalenceMapping {
	final CachedHttpRequestService rdfCache;
	final HashSet<String> alreadyChecked=new HashSet<String>();
	final ArrayList<String> equivalencesNotFound=new ArrayList<String>();
	final boolean acceptNonSchemaOrg;
	public EquivalenceMapping(CachedHttpRequestService rdfCache, boolean acceptNonSchemaOrg) {
		this.rdfCache = rdfCache;
		this.acceptNonSchemaOrg= acceptNonSchemaOrg;
	}
	
	final MapOfLists<String, String> wdEntPropEquivalences = new MapOfLists<String, String>();
	final MapOfLists<String, String> wdEntPropEquivalencesSuperclasses = new MapOfLists<String, String>();
	final MapOfLists<String, String> wdEntPropEquivalencesSuperclassesNonSchemaOrg = new MapOfLists<String, String>();
	final MapOfLists<String, String> wdEntPropEquivalencesNonSchemaOrg = new MapOfLists<String, String>();
	
	public void analyzeEntity(Resource wdResource, Resource leafResource) throws AccessException, InterruptedException, IOException {
		if (alreadyChecked.contains(wdResource.getURI())) {
			if(! wdResource.getURI().equals(leafResource.getURI())) {
				ArrayList<String> eqs = getEquivalence(wdResource.getURI(), true);
				ArrayList<String> equivalence = eqs;
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
				if(wdResource.getURI().equals(leafResource.getURI())) {
					wdEntPropEquivalences.put(leafResource.getURI(), objUri);
				} else {
					wdEntPropEquivalences.put(wdResource.getURI(), objUri);
					wdEntPropEquivalencesSuperclasses.put(leafResource.getURI(), objUri);
				}
				foundEquivalent=true;
			}else if(acceptNonSchemaOrg && objUri.equals(RdfReg.FOAF_PAGE.getURI())) {
				//TODO: this is just to test, for now. Remove next line
				objUri=RdfReg.SCHEMAORG_URL.getURI();
				
				if(wdResource.getURI().equals(leafResource.getURI())) {
					wdEntPropEquivalencesNonSchemaOrg.put(leafResource.getURI(), objUri);
				} else {
					wdEntPropEquivalencesNonSchemaOrg.put(wdResource.getURI(), objUri);
					wdEntPropEquivalencesSuperclassesNonSchemaOrg.put(leafResource.getURI(), objUri);
				}
			}
		}
		if(!foundEquivalent) {
			propStms = wdResource.listProperties(RdfRegWikidata.SUBCLASS_OF);
			for (Statement st : propStms.toList()) {
//				System.out.println(st);
				String objUri = st.getObject().asNode().getURI();
				Resource wdScResource = MetadataAnalyzerOfCulturalHeritage.fetchresource(objUri, rdfCache);
					analyzeEntity(wdScResource, leafResource);
				if(! wdResource.getURI().equals(leafResource.getURI())) {
					ArrayList<String> eqs=getEquivalence(wdScResource.getURI(), true);
					if(eqs!=null) 
						putEquivalenceOfSuperclass(wdResource.getURI(), eqs);
				}
			}	
			
			if(wdResource.getURI().equals(leafResource.getURI())) {
				if (! wdEntPropEquivalencesSuperclasses.containsKey(leafResource.getURI()) 
//						&& ! wdEntPropEquivalencesSuperclassesNonSchemaOrg.containsKey(leafResource.getURI())
						)
					equivalencesNotFound.add(leafResource.getURI());
//			} else {
//				ArrayList<String> eqSuper = wdEntPropEquivalencesSuperclasses.get(leafResource.getURI());
//				ArrayList<String> eqsSuperNonSchemaOrg = wdEntPropEquivalencesSuperclassesNonSchemaOrg.get(leafResource.getURI());
//				if (eqSuper!=null) {
//					wdEntPropEquivalencesSuperclasses.putAll(wdResource.getURI(), eqSuper);
//				} else if (eqsSuperNonSchemaOrg==null) {
//					wdEntPropEquivalencesSuperclassesNonSchemaOrg.putAll(wdResource.getURI(), eqsSuperNonSchemaOrg);					
//				} else
//					equivalencesNotFound.add(wdResource.getURI());
			}
		}
	}

	public void putEquivalence(String uriWd, String otherUri) {
		wdEntPropEquivalences.put(uriWd, otherUri);
	}
	
	public void putEquivalenceOfSuperclass(String uriWd, String otherUri) {
		wdEntPropEquivalencesSuperclasses.put(uriWd, otherUri);
	}
	public void putEquivalenceOfSuperclass(String uriWd, Collection<String> otherUris) {
		wdEntPropEquivalencesSuperclasses.putAll(uriWd, otherUris);
	}

	public void analyzeProperty(String propUriParam, String leafPropertyUri) {
//		public void analyzeProperty(String propUri, String entityResourceUri) {
		if(!propUriParam.startsWith("http://www.wikidata.org/")) {
			String[] wdEqUri=new String[1];
			WikidataSparqlClient.query("SELECT ?item WHERE { ?item wdt:"+RdfRegWikidata.EQUIVALENT_PROPERTY.getLocalName()+" <"+propUriParam+"> .", new UriHandler() {
//				WikidataSparqlClient.query("SELECT ?item WHERE { ?item wdt:"+RdfRegWikidata.EQUIVALENT_PROPERTY.getLocalName()+" <"+entityResourceUri+"> .", new UriHandler() {
				public boolean handleUri(String uri) throws Exception {
					wdEqUri[0]=uri;
					return false;
				}
			});
			propUriParam=wdEqUri[0];
			if(propUriParam==null)
				return;
		}
		String propUri=convertWdPropertyUri(propUriParam);

		if (alreadyChecked.contains(propUri)) {	
//			ArrayList<String> propEquivalence = getEquivalence(leafPropertyUri, true);
//			if(propEquivalence != null && ! propUriParam.equals(leafPropertyUri)) {
//				ArrayList<String> equivalence = propEquivalence;
//				if(equivalence!=null) {
//					for(String eq: equivalence)
//						putEquivalenceOfSuperclass(leafPropertyUri, eq);
//				}
//			}
//			
			if(! propUriParam.equals(leafPropertyUri)) {
				ArrayList<String> eqs = getEquivalence(propUriParam, true);
				ArrayList<String> equivalence = eqs;
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
					if(objUri.startsWith(RdfReg.NsSchemaOrg) || objUri.startsWith(RdfRegRdf.NS)) {
						if(propUriParam.equals(leafPropertyUri)) {
							wdEntPropEquivalences.put(leafPropertyUri, objUri);
						} else {
							wdEntPropEquivalences.put(propUriParam, objUri);
							wdEntPropEquivalencesSuperclasses.put(leafPropertyUri, objUri);
						}
						foundEquivalent=true;
					}else if(acceptNonSchemaOrg && objUri.equals(RdfReg.FOAF_PAGE.getURI())) {
						//TODO: this is just to test, for now. Remove next line
						objUri=RdfReg.SCHEMAORG_URL.getURI();
						if(objUri.startsWith(RdfReg.NsSchemaOrg) || objUri.startsWith(RdfRegRdf.NS)) {
							if(propUriParam.equals(leafPropertyUri)) {
								wdEntPropEquivalences.put(leafPropertyUri, objUri);
							} else {
								wdEntPropEquivalences.put(propUriParam, objUri);
								wdEntPropEquivalencesSuperclasses.put(leafPropertyUri, objUri);
							}
						}
					}
				}
				if(!foundEquivalent) {			
//					StmtIterator propStms = wdPropResource.listProperties(RdfRegWikidata.SUBCLASS_OF);
//					for (Statement st : propStms.toList()) {
////						System.out.println(st);
//						String objUri = st.getObject().asNode().getURI();
//						
//						analyzeProperty(objUri, leafPropertyUri);
//					}
					StmtIterator propStms = wdPropResource.listProperties(RdfRegWikidata.SUBPROPERTY_OF);
					for (Statement st : propStms.toList()) {
//						System.out.println(st);
						String objUri = st.getObject().asNode().getURI();
						
//						analyzeProperty(objUri, leafPropertyUri);
//						foundEquivalent=true;
						
						Resource wdScResource = MetadataAnalyzerOfCulturalHeritage.fetchresource(objUri, rdfCache);
						analyzeProperty(toDirectProp(objUri), leafPropertyUri);
					if(! propUriParam.equals(leafPropertyUri)) {
						ArrayList<String> eqs=getEquivalence(wdScResource.getURI(), true);
						if(eqs!=null) 
							putEquivalenceOfSuperclass(propUriParam, eqs);
					}

					if(propUriParam.equals(leafPropertyUri)) {
						if (! wdEntPropEquivalencesSuperclasses.containsKey(leafPropertyUri) &&
								! wdEntPropEquivalencesSuperclassesNonSchemaOrg.containsKey(leafPropertyUri))
							equivalencesNotFound.add(leafPropertyUri);
					}
				}
				
				}
				
				
		} catch (Exception e) {
			System.out.printf("Access to %s failed\n", propUri);
			e.printStackTrace(System.out);
		}
	}

	private String toDirectProp(String objUri) {
		if(!objUri.startsWith(RdfRegWikidata.NsWdt))
			return RdfRegWikidata.NsWdt+objUri.substring(objUri.lastIndexOf('/')+1);
		return objUri;
	}

	public static String convertWdPropertyUri(String propUri) {
		if(!propUri.startsWith(RdfRegWikidata.NsWd)) 
			propUri="http://www.wikidata.org/entity/"+propUri.substring(propUri.lastIndexOf('/')+1);
		return propUri;
	}

	public ArrayList<String> getEquivalence(String objUri, boolean acceptSuperEquivalences) {
		ArrayList<String> ret=wdEntPropEquivalences.get(objUri);
		if(ret==null && acceptSuperEquivalences)
			ret=wdEntPropEquivalencesSuperclasses.get(objUri);
		if(ret==null && acceptNonSchemaOrg) {
			ret=wdEntPropEquivalencesNonSchemaOrg.get(objUri);
			if(ret==null && acceptSuperEquivalences)
				ret=wdEntPropEquivalencesSuperclassesNonSchemaOrg.get(objUri);
		}
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
	
	public String toCsvDetailed() {
		SortedSet<String> sorted=new TreeSet<String>(wdEntPropEquivalences.keySet());
		sorted.addAll(wdEntPropEquivalencesSuperclasses.keySet());
		try {
			StringBuilder sbCsv=new StringBuilder();
			CSVPrinter csv=new CSVPrinter(sbCsv, CSVFormat.DEFAULT);
			csv.printRecord("EQUIVALENTS FOUND");
			csv.printRecord("Wikidata URI", "Wikidata label","Schema.org URI","Schema.org URI (generic)");
			for(String uri: sorted) {
				if(!uri.startsWith("http://www.wikidata.org"))
					continue;
				ArrayList<String> eqUris = wdEntPropEquivalences.get(uri);
				boolean eqExists = !(eqUris==null || eqUris.isEmpty());
				boolean eqGenExists=!eqExists && wdEntPropEquivalencesSuperclasses.containsKey(uri);
				
				String wdLabel=getLabel(uri);

				if(eqExists) {
					for(String eqU: eqUris)
						csv.printRecord(uri, wdLabel, eqU );
				} else if(eqGenExists)
					for(String eqU: wdEntPropEquivalencesSuperclasses.get(uri))
						csv.printRecord(uri, wdLabel, eqU );
			}		
			csv.println();
			csv.printRecord("EQUIVALENTS NOT FOUND");
			csv.printRecord("Wikidata URI", "Wikidata label");
			Collections.sort(equivalencesNotFound);
			for(String uri: equivalencesNotFound) {
				if(!uri.startsWith("http://www.wikidata.org"))
					continue;
				String wdLabel=getLabel(uri);
				csv.printRecord(uri, wdLabel);
			}		
			csv.close();
			return sbCsv.toString();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (AccessException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private String getLabel(String uri) throws AccessException, InterruptedException, IOException {
		uri=convertWdPropertyUri(uri);
		Resource resource = MetadataAnalyzerOfCulturalHeritage.fetchresource(uri, rdfCache);
		StmtIterator labelProps = resource.listProperties(RdfRegRdfs.label);
		String label=null;
		for (Statement st : labelProps.toList()) {
			String lang = st.getObject().asLiteral().getLanguage();
			if(lang.equals("en"))
				return st.getObject().asLiteral().getString();
			if(label==null) 
				label=st.getObject().asLiteral().getString();
		}
		return label;
	}

	public void finish() {
		Set<String> eqs=new HashSet<String>(wdEntPropEquivalences.keySet());
		eqs.addAll(wdEntPropEquivalencesSuperclasses.keySet());
		
		for(String uri: eqs) {
			ArrayList<String> eqUris = wdEntPropEquivalences.get(uri);
			boolean eqExists = !(eqUris==null || eqUris.isEmpty());
			boolean eqGenExists=!eqExists && wdEntPropEquivalencesSuperclasses.containsKey(uri);
			if(eqExists) {
				HashSet<String> dedup=new HashSet<String>(eqUris);
				wdEntPropEquivalences.remove(uri);
				wdEntPropEquivalences.putAll(uri, dedup);
			} else if(eqGenExists) {
				HashSet<String> dedup=new HashSet<String>(wdEntPropEquivalencesSuperclasses.get(uri));
				wdEntPropEquivalencesSuperclasses.remove(uri);
				wdEntPropEquivalencesSuperclasses.putAll(uri, dedup);
			}
		}	
	}

	public void setEquivalencesNotFound(HashSet<String> missingEqs) {
		equivalencesNotFound.clear();
		equivalencesNotFound.addAll(missingEqs);
	}

	
	
}
