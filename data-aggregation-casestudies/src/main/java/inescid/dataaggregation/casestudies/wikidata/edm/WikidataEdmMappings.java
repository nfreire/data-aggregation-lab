package inescid.dataaggregation.casestudies.wikidata.edm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.riot.Lang;

import inescid.dataaggregation.wikidata.RdfRegWikidata;
import inescid.dataaggregation.wikidata.WikidataUtil;
import inescid.dataaggregation.wikidata.WikidataLabels;
import inescid.dataaggregation.wikidata.SparqlClientWikidata;
import inescid.dataaggregation.data.RdfsClassHierarchy;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.dataset.convert.ClassMappings;
import inescid.dataaggregation.dataset.convert.SchemaOrgToEdmClassMappings;
import inescid.dataaggregation.dataset.profile.tiers.EpfTiersCalculator;
import inescid.util.AccessException;
import inescid.util.RdfUtil;
import inescid.util.SparqlClient;
import inescid.util.TriplestoreJenaTbd2;
import inescid.util.datastruct.MapOfMaps;
import inescid.util.SparqlClient.Handler;

public class WikidataEdmMappings {
	ClassMappings edmClassMappings=new ClassMappings();
	RdfsClassHierarchy hierarchy=new RdfsClassHierarchy();
	
	public WikidataEdmMappings(File csvFileClasses, File csvFileProperties, File csvClassHierarchy, File wikidataTbd2Folder) throws IOException, AccessException, InterruptedException {
		init(csvFileClasses, csvFileProperties);
		boolean hierachyExists=csvClassHierarchy.exists();
		if(hierachyExists)
			hierarchy=RdfsClassHierarchy.fromCsv(csvClassHierarchy);
		else {
			loadSubClassesAndSubProperties(wikidataTbd2Folder);
			FileUtils.write(csvClassHierarchy, hierarchy.toCsv(), StandardCharsets.UTF_8);
		}
		
//		final RdfDomainRangeValidatorWithOwl validator;
//		validator=new RdfDomainRangeValidatorWithOwl(RdfUtil.readRdfFromUri(RegEdm.NS));
		
		SchemaOrgToEdmClassMappings schemaOrgToEdmClassMappings=new SchemaOrgToEdmClassMappings();
		
		InputStream schemaorgOwlIs = WikidataEdmMappings.class.getClassLoader().getResourceAsStream("owl/schemaorg.owl");
		RdfsClassHierarchy schemaClassHierarchy = new RdfsClassHierarchy(RdfUtil.readRdf(schemaorgOwlIs, Lang.RDFXML));
		schemaorgOwlIs.close();
		
		SparqlClient sparqlCl= wikidataTbd2Folder==null ? SparqlClientWikidata.INSTANCE : 
			new TriplestoreJenaTbd2(wikidataTbd2Folder, SparqlClientWikidata.PREFFIXES, ReadWrite.READ);
		
		//Equivalent Classes
		sparqlCl.query("select ?s (<http://www.wikidata.org/prop/direct/P1709> AS ?p) ?o WHERE { ?s <http://www.wikidata.org/prop/direct/P1709> ?o. }", new Handler() {
			public boolean handleSolution(QuerySolution solution) throws Exception {
				String wdClsUri = solution.getResource("s").getURI();
				String edmType = getEdmType(wdClsUri);
				if(edmType==null) {
					String equivalentClsUri = solution.getResource("o").getURI();
					ArrayList<String> superClassesOf = new ArrayList<String>(schemaClassHierarchy.getSuperClassesOf(equivalentClsUri));
					superClassesOf.add(0, equivalentClsUri);
					for(String eqCls : superClassesOf) {
						edmType=schemaOrgToEdmClassMappings.GetClassMappping().get(eqCls);
						if(edmType!=null) {
							edmClassMappings.putClassMapping(wdClsUri, edmType);
//							System.out.println("Found mapping via schema.org equivalence: "+ wdClsUri+" "+equivalentClsUri+" "+edmType);
							return true;
						}
					}
				}
				return true;
			}
		});
		
		//Equivalent Properties
		sparqlCl.query("select ?s (<http://www.wikidata.org/prop/direct/P1628> AS ?p) ?o WHERE { ?s <http://www.wikidata.org/prop/direct/P1628> ?o. }", new Handler() {
			public boolean handleSolution(QuerySolution solution) throws Exception {
				String wdPropUri = solution.getResource("s").getURI();
				String schemaPropUri = solution.getResource("o").getURI();
				if(!edmClassMappings.getAllPropertiesMapped().contains(wdPropUri)) { 
					ArrayList<String> superPropsOf = new ArrayList<String>(schemaClassHierarchy.getSuperPropertiesOf(schemaPropUri));
					superPropsOf.add(0, schemaPropUri);
					for(String eqProp : superPropsOf) {
						if(schemaOrgToEdmClassMappings.getAllPropertiesMapped().contains(eqProp)) {
							for(String schemaClsUri : schemaOrgToEdmClassMappings.GetClassMappping().keySet()) {
								String edmClsUri=schemaOrgToEdmClassMappings.GetClassMappping().get(schemaClsUri);
								String edmPropUri=schemaOrgToEdmClassMappings.get(edmClsUri, eqProp);
								if(edmPropUri!=null) {
									edmClassMappings.put(edmClsUri, wdPropUri, edmPropUri);
//									System.out.println("Found mapping via schema.org equivalence: "+ eqProp+": "+edmClsUri+" "+wdPropUri+" "+edmPropUri);
									return true;
								}
							}
						}
					}
				}
				return true;
			}
		});
	}
	
	public WikidataEdmMappings(File csvFileClasses, File csvFileProperties) throws IOException {
		init(csvFileClasses, csvFileProperties);
	}
	
	
	public List<String> getMappingsTo(String trgType, String trgProp) {
		ArrayList<String> mappings=new ArrayList<String>();
		Map<String, String> allPropertiesMappedFor = edmClassMappings.getAllPropertiesMappedFor(trgType);
		for(String wdProp: allPropertiesMappedFor.keySet()) {
			if(trgProp.equals(allPropertiesMappedFor.get(wdProp)))
				mappings.add(wdProp);
		}
		return mappings;
	}
	
	private void init(File csvFileClasses, File csvFileProperties) throws IOException {
		hierarchy=new RdfsClassHierarchy();
		Reader csvReader=new InputStreamReader(new FileInputStream(csvFileProperties), "UTF-8");
		BufferedReader csvBufferedReader = new BufferedReader(csvReader);
		try {
			csvBufferedReader.readLine(); //skip heading line
			while(csvBufferedReader.ready()) {
				String mappingLine = csvBufferedReader.readLine();
				String[] split = mappingLine.split(",");
				if (split.length>=3 && !StringUtils.isBlank(split[0]) 
						&& !StringUtils.isBlank(split[1]) 
						&& !StringUtils.isBlank(split[2])) {
					String wdProp=split[0].trim();
					if(wdProp.startsWith("P") || wdProp.startsWith("Q") )
						wdProp="http://www.wikidata.org/entity/"+wdProp;
					else if(wdProp.startsWith("http://www.wikidata")) 
//						else if(!wdProp.startsWith("http://www.wikidata")) 
//							continue;
//						else
						wdProp=WikidataUtil.convertWdUriToCanonical(wdProp);
					else
						wdProp=normalizetoUri(wdProp);
					String edmProp=normalizetoUri(split[1].trim());
				
					for(int i=2; i<split.length; i++)
						edmClassMappings.put(normalizetoUri(split[i].trim()), wdProp, edmProp);
				}
			}
		} finally {
			csvBufferedReader.close();
		}
		csvReader=new InputStreamReader(new FileInputStream(csvFileClasses), "UTF-8");
		csvBufferedReader = new BufferedReader(csvReader);
		try {
			csvBufferedReader.readLine(); //skip heading line
			while(csvBufferedReader.ready()) {
				String mappingLine = csvBufferedReader.readLine();
				String[] split = mappingLine.split(",");
				if (split.length>=2) {
					String wdCls=split[0].trim();
					String edmCls=split[1].trim();
					if (!StringUtils.isEmpty(wdCls) && !StringUtils.isEmpty(edmCls)) {
						if(!StringUtils.startsWithIgnoreCase(edmCls, "http")) {
							int idx = edmCls.indexOf(':');
							if(idx>0) {
								String ns=Edm.NS_EXTERNAL_PREFERRED_BY_PREFIXES.get(edmCls.substring(0, idx));
								if(ns==null)
									throw new RuntimeException("unknown prefix "+edmCls.substring(0, idx)+" in "+edmCls);
								edmCls=ns+edmCls.substring(idx+1);
							} else
								throw new RuntimeException("unknown prefix in "+edmCls);
						}
						if(edmCls.contains(" "))
							throw new RuntimeException("white space at"+edmCls);						
						edmClassMappings.putClassMapping(wdCls, edmCls);
					}
				}
			}
		} finally {
			csvBufferedReader.close();
		}
		System.out.println(edmClassMappings.size() + " wikidata edm mappings loaded.");
	}

	private String normalizetoUri(String edmProp) {
		if(edmProp.contains(" "))
			edmProp=edmProp.substring(0, edmProp.indexOf(' '));
		if(!StringUtils.startsWithIgnoreCase(edmProp, "http")) {
			int idx = edmProp.indexOf(':');
			if(idx>0) {
				String pref = edmProp.substring(0, idx);
				String ns=Edm.NS_EXTERNAL_PREFERRED_BY_PREFIXES.get(pref);
				if(ns==null) {
					if(pref.equals("schema"))
						ns="http://schema.org/";
					else if(pref.equals("wdt"))
						ns=RdfRegWikidata.NsWdt;
					else
						throw new RuntimeException("unknown prefix "+pref+" in "+edmProp);
				}
				edmProp=ns+edmProp.substring(idx+1);
			} else
				throw new RuntimeException("unknown prefix in "+edmProp);
		}
		return edmProp;
	}

	public void loadSubClassesAndSubProperties(File wikidataTbd2Folder) {
		SparqlClient sparqlCl= wikidataTbd2Folder==null ? SparqlClientWikidata.INSTANCE : 
			new TriplestoreJenaTbd2(wikidataTbd2Folder, SparqlClientWikidata.PREFFIXES, ReadWrite.READ);
//		sparqlCl.setDebug(true);
		for(String r: edmClassMappings.GetClassMappping().keySet()) {
			for(String scUri : getAllSubClasses(r, sparqlCl)) {
				hierarchy.setSuperClass(scUri, r);				
			}
		}
		for(String r: edmClassMappings.getAllPropertiesMapped()) {
			for(String scUri : getAllSubProperties(r, sparqlCl)) {
				hierarchy.setSuperProperty(scUri, r);				
			}
		}
		hierarchy.calculateHierarchy();

		if(sparqlCl instanceof TriplestoreJenaTbd2)
			((TriplestoreJenaTbd2)sparqlCl).close();
	}
	
	
	private Set<String> getAllSubProperties(String wdPropertyUri, SparqlClient sparqlCl) {
		final HashSet<String> uris=new HashSet<String>();
		sparqlCl.query("SELECT ?subClass WHERE { ?subClass wdt:P1647+ <"+WikidataUtil.convertWdUriToQueryableUri(wdPropertyUri)+"> .}", new Handler() {
			@Override
			public boolean handleSolution(QuerySolution solution) throws Exception {
				uris.add(solution.getResource("subClass").getURI());
				return true;
			}
		});
		return uris;
	}
	private Set<String> getAllSubClasses(String wdEntityUri, SparqlClient sparqlCl) {
		final HashSet<String> uris=new HashSet<String>();
		sparqlCl.query("SELECT ?subClass WHERE { ?subClass wdt:P279+ <"+WikidataUtil.convertWdUriToQueryableUri(wdEntityUri)+"> .}", new Handler() {
			@Override
			public boolean handleSolution(QuerySolution solution) throws Exception {
				uris.add(solution.getResource("subClass").getURI());
				return true;
			}
		});
		return uris;
	}
	
	

//	private Set<String> getAllSubClasses(String wdEntityUri, SparqlClient sparqlCl) {
//		final HashSet<String> uris=new HashSet<String>();
//		final HashSet<String> newUris=new HashSet<String>();
//		newUris.add(wdEntityUri);
//		while (!newUris.isEmpty()) {	
//			ArrayList<String> tmp = new ArrayList<>(newUris);
//			newUris.clear();
//			for(String uri: tmp) {
////					if(uris.contains(uri)) continue;
//				System.out.println(uri);
//				sparqlCl.query("SELECT ?subClass WHERE { ?subClass wdt:P279 <"+ WikidataUtil.convertWdUriToQueryableUri(uri)+"> .}", new Handler() {
//					@Override
//					public boolean handleSolution(QuerySolution solution) throws Exception {
//						boolean exists=uris.add(solution.getResource("subClass").getURI());
//						if(exists)
//							newUris.add(solution.getResource("subClass").getURI());
//						return true;
//					}
//				});
//			}
//		}
//		return uris;
//	}

	public static void main(String[] args) throws Exception {
		WikidataEdmMappings wdMappings=new WikidataEdmMappings(
				new File("src/data/wikidata/wikidata_edm_mappings_classes.csv"), 
				new File("src/data/wikidata/wikidata_edm_mappings.csv"));
	}

	public String getEdmType(String uri) {
		String edmType = edmClassMappings.GetClassMappping().get(uri);
		if(edmType==null) { 
			for(String s: hierarchy.getSuperClassesOf(uri)) {
				edmType = edmClassMappings.GetClassMappping().get(s);
				if(edmType!=null) 
					break;
			}
		}
		return edmType;
	}

	public String getEdmProperty(String edmClsUri, String propUri) {
		String edmProp = edmClassMappings.get(edmClsUri, WikidataUtil.convertWdUriToCanonical(propUri));
		if(edmProp==null) { 
			for(String s: hierarchy.getSuperPropertiesOf(propUri)) {
				edmProp = edmClassMappings.get(edmClsUri, s);
				if(edmProp!=null) 
					break;
			}
		}
		return edmProp;
	}
	
	public List<String> getClassesMappedTo(String edmClass) {
		return edmClassMappings.getClassesMappedTo(edmClass);
	}
		
//	public String getFromWdId(String id) {
//		String edmProp=wikidataToEdmPropertyMap.get("https://www.wikidata.org/wiki/Property:"+id);
//		if(edmProp==null) //try http instead of https
//			edmProp=wikidataToEdmPropertyMap.get("http://www.wikidata.org/wiki/Property:"+id);
//		if(edmProp==null) //try with variants
//			edmProp=wikidataToEdmPropertyMap.get("http://www.wikidata.org/entity/"+id);
//		if(edmProp==null) //try with variants
//			edmProp=wikidataToEdmPropertyMap.get("http://www.wikidata.org/prop/"+id);
//		return edmProp;
//	}
}
