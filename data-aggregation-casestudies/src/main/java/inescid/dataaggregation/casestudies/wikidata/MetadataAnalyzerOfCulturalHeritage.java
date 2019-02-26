package inescid.dataaggregation.casestudies.wikidata;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.mapdb.DataInput2.ByteArray;

import inescid.dataaggregation.dataset.convert.SchemaOrgToEdmDataConverter;
import inescid.dataaggregation.casestudies.wikidata.WikidataSparqlClient.UriHandler;
import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.crawl.http.HttpRequest;
import inescid.dataaggregation.dataset.GlobalCore;
import inescid.dataaggregation.dataset.convert.RdfReg;
import inescid.dataaggregation.dataset.profile.ClassUsageStats;
import inescid.dataaggregation.dataset.profile.UsageProfiler;
import inescid.dataaggregation.dataset.profile.completeness.Dqc10PointRatingCalculator;
import inescid.dataaggregation.store.Repository;
import inescid.europeanaapi.EuropeanaApiClient;
import inescid.util.AccessException;
import inescid.util.HttpUtil;
import inescid.util.RdfUtil;
import inescid.util.RdfUtil.Jena;
import inescid.util.datastruct.MapOfInts;
import inescid.util.datastruct.MapOfLists;

public class MetadataAnalyzerOfCulturalHeritage {

	public static void main(String[] args) {
		String httpCacheFolder = "c://users/nfrei/desktop/HttpRepository";
		File outputFolder = new File("c://users/nfrei/desktop");
		final int SAMPLE_RECORDS;

		if (args.length > 0)
			outputFolder = new File(args[0]);
		if (args.length > 1)
			httpCacheFolder = args[1];
		if (args.length > 2)
			SAMPLE_RECORDS = Integer.parseInt(args[2]);
		else
			SAMPLE_RECORDS = 100;

		if (!outputFolder.exists())
			outputFolder.mkdirs();

		final HashSet<String> europeanaIdsBroken=new HashSet<String>();
		try {
			File brokenLinksReportFile = new File("src/data/wikidata/wikidata_broken_links_to_europeana.csv");
			if(brokenLinksReportFile.exists()) {
				List<String> lines = FileUtils.readLines(brokenLinksReportFile, "UTF-8");
				lines.remove(0);
				for(String l : lines) {
					String[] split = l.split(",");
					if(split[2].equals("404"))
						europeanaIdsBroken.add(split[1]);
				}
			}
		} catch (IOException e1) {
			System.err.println();
			e1.printStackTrace();
		}
		
		System.out.printf("Settings:\n-OutpputFolder:%s\n-Cache:%s\n-Records:%d\n-Broken EuropenaIDs:%d\n------------------------\n",
				outputFolder.getPath(), httpCacheFolder, SAMPLE_RECORDS, europeanaIdsBroken.size());

		GlobalCore.init_componentHttpRequestService();
		GlobalCore.init_componentDataRepository(httpCacheFolder);

		Repository dataRepository = GlobalCore.getDataRepository();	
		
		CachedHttpRequestService rdfCache = new CachedHttpRequestService();
		rdfCache.setRequestRetryAttempts(1);
		
		UsageProfiler chEntitiesProfile = new UsageProfiler();

		WikidataSparqlClient.querySolutions("SELECT ?item ?europeana WHERE {" +
//                "  ?item wdt:"+RdfRegWikidata.IIIF_MANIFEST+" ?x ." + 
				"  ?item wdt:" + RdfRegWikidata.EUROPEANAID.getLocalName() + " ?europeana .", new UriHandler() {

					int stop = SAMPLE_RECORDS+1;

					@Override
					public boolean  handleSolution(QuerySolution solution) throws AccessException, InterruptedException, IOException {
						String europeanaId=solution.getLiteral("europeana").getString();
						if(europeanaIdsBroken.contains(europeanaId)) {
//							System.out.println("Skipping broken "+europeanaId);
							return true;
						}
						Resource resourceResult = solution.getResource("item");
						String uri=resourceResult.getURI();
						Resource resource=fetchresource(uri, rdfCache);
							
							removeOtherResources(resource.getModel(), uri);
							removeNonTruthyStatements(resource.getModel());
							addRdfTypesFromP31(resource.getModel());
							
							chEntitiesProfile.collect(resource.getModel(), uri);
//							System.out.println(fetched.getValue());
//							System.out.println(new String(fetched.getKey()));
//							System.out.println("Statements for " + uri);
//							System.out.println(RdfUtil.printStatements(rdfWikidata));
						stop--;
						return stop != 0;
//						return true;
					}

					
				});
		chEntitiesProfile.finish();

//		System.out.println(chEntitiesProfile);

		UsageProfiler wdEntPropProfile = new UsageProfiler();
		final EquivalenceMapping wdEntPropEquivalences = new EquivalenceMapping(rdfCache);
//		final MapOfLists<String, String> wdEntPropEquivalences = new MapOfLists<String, String>();
//		final MapOfLists<String, String> wdEntPropEquivalencesSuperclasses = new MapOfLists<String, String>();

		//TMP HACK
		wdEntPropEquivalences.putEquivalence(RdfRegWikidata.CREATIVE_WORK.getURI(), RdfReg.SCHEMAORG_CREATIVE_WORK.getURI());
		
		// harvest all wd entities and properties used, and profile them
		for (Entry<String, ClassUsageStats> entry : chEntitiesProfile.getUsageStats().getClassesStats().entrySet()) {
			String wdResourceUri = entry.getKey();

			if(!wdResourceUri.startsWith("http://www.wikidata.org/")) {
				String[] wdEqUri=new String[1];
				int query = WikidataSparqlClient.query("SELECT ?item WHERE { ?item wdt:"+
				RdfRegWikidata.EQUIVALENT_CLASS.getLocalName()+" <"+wdResourceUri+"> .", new UriHandler() {
					public boolean handleUri(String uri) throws Exception {
						wdEqUri[0]=uri;
						return false;
					}
				});
				wdResourceUri=wdEqUri[0];
				if(wdResourceUri==null)
					continue;
			}
			
			try {

				Resource wdResource=fetchresource(wdResourceUri, rdfCache);
				
					wdEntPropProfile.collect(wdResource.getModel());

//					System.out.println(new String(fetched.getKey()));
//					System.out.println(fetched.getValue());
//					System.out.println("Statements for " + wdResourceUri);
//					System.out.println(RdfUtil.printStatements(rdfWikidata));
					wdEntPropEquivalences.analyzeEntity(wdResource, wdResource);
					
//					typeProperties = rdfWikidata.listStatements(wdResource, RdfRegWikidata.EQUIVALENT_PROPERTY, (RDFNode)null);
//					for(Statement st : typeProperties.toList()) {
////						System.out.println(st);
//						String objUri = st.getObject().asNode().getURI();
//						wdEntPropEquivalences.put(wdResourceUri, objUri);
//					}
				for (String propUri : entry.getValue().getPropertiesProfiles().keySet()) {
					wdEntPropEquivalences.analyzeProperty(propUri, propUri);
//					if(!propUri.startsWith("http://www.wikidata.org/")) {
//						String[] wdEqUri=new String[1];
//						WikidataSparqlClient.query("SELECT ?item WHERE { ?item wdt:"+RdfRegWikidata.EQUIVALENT_PROPERTY.getLocalName()+" <"+wdResourceUri+"> .", new UriHandler() {
//							public boolean handleUri(String uri) throws Exception {
//								wdEqUri[0]=uri;
//								return false;
//							}
//						});
//						propUri=wdEqUri[0];
//						if(propUri==null)
//							continue;
//					}
//					if(!propUri.startsWith(RdfRegWikidata.NsWd)) 
//						propUri="http://www.wikidata.org/entity/"+propUri.substring(propUri.lastIndexOf('/')+1);
//					System.out.println("Analyzing "+propUri);
//					try {
//						
//						SimpleEntry<byte[], List<Entry<String, String>>> propFetched = rdfCache.fetchRdf(propUri);
//						if (propFetched == null || propFetched.getKey() == null || propFetched.getKey().length == 0) {
//							System.out.printf("Access to %s failed\n", propUri);
//						} else {
////							System.out.println(new String(propFetched.getKey()));
////							System.out.println(propFetched.getValue());
//							
//							Model rdfWikidata = RdfUtil.readRdf(propFetched.getKey(),
//									RdfUtil.fromMimeType(HttpUtil.getHeader("Content-Type", propFetched.getValue())));
//							if(rdfWikidata.size()==0)
//								continue;
//							wdEntPropProfile.collect(rdfWikidata);
////							System.out.println(RdfUtil.printStatements(rdfWikidata));
//
//							Resource wdPropResource = rdfWikidata.getResource(propUri);
////							for (Statement st : rdfWikidata.listStatements().toList()) {
//								StmtIterator typeProperties = rdfWikidata.listStatements(wdPropResource,
//										RdfRegWikidata.EQUIVALENT_PROPERTY, (RDFNode) null);
//								for (Statement st : typeProperties.toList()) {
////							System.out.println(st);
//							
//								String objUri = st.getObject().asNode().getURI();
//								if(objUri.startsWith(RdfReg.NsSchemaOrg) || objUri.startsWith(RdfReg.NsRdf))
//									wdEntPropEquivalences.put(propUri, objUri);
//							}
////							typeProperties = rdfWikidata.listStatements(wdPropResource, RdfRegWikidata.EQUIVALENT_CLASS, (RDFNode)null);
////							for(Statement st : typeProperties.toList()) {
//////							System.out.println(st);
////								String objUri = st.getObject().asNode().getURI();
////								wdEntPropEquivalences.put(wdResourceUri, objUri);
////							}
//						}
//					} catch (Exception e) {
//						System.out.printf("Access to %s failed\n", propUri);
//						e.printStackTrace(System.out);
//					}
				}
			} catch (Exception e) {
				System.out.printf("Access to %s failed\n", wdResourceUri);
				e.printStackTrace(System.out);
			}
		}
		wdEntPropProfile.finish();

//		System.out.println(wdEntPropProfile.printShort());
		System.out.println(wdEntPropEquivalences);
		
		ArrayList<Triple<String, Double, Double>> completnesses=new ArrayList<>();
		
		//Get wikidata entities again, and convert properties to schema.org, convert to EDM and store
		
		WikidataSparqlClient.querySolutions("SELECT ?item ?europeana WHERE {" +
//              "  ?item wdt:"+RdfRegWikidata.IIIF_MANIFEST+" ?x ." + 
				"  ?item wdt:" + RdfRegWikidata.EUROPEANAID.getLocalName() + " ?europeana .", new UriHandler() {
					int stop = SAMPLE_RECORDS+1;
					@Override
					public boolean  handleSolution(QuerySolution solution) throws AccessException, InterruptedException, IOException {
						String europeanaId=solution.getLiteral("europeana").getString();
						if(europeanaIdsBroken.contains(europeanaId)) {
//							System.out.println("Skipping broken "+europeanaId);()
							return true;
						}
						Resource resource = solution.getResource("item");
						String uri=resource.getURI();
						

						Resource wdResource=fetchresource(uri, rdfCache);
								Model rdfWikidata = wdResource.getModel();
							removeOtherResources(rdfWikidata, uri);
							removeNonTruthyStatements(rdfWikidata);
//					System.out.println(RdfUtil.printStatements(rdfWikidata));
//							System.out.println("--- "+uri +" ---");
							
							rdfWikidata.add(Jena.createStatement(wdResource, RdfRegWikidata.INSTANCE_OF, RdfRegWikidata.CREATIVE_WORK));
							
							for (Statement st : rdfWikidata.listStatements().toList()) {
								String predUri = st.getPredicate().getURI().toString();
								
								if(predUri.startsWith("http://www.wikidata.org/")) {
									if(!predUri.startsWith(RdfRegWikidata.NsWd)) 
										predUri=RdfRegWikidata.NsWd+predUri.substring(predUri.lastIndexOf('/')+1);
									
									ArrayList<String> mappingsToSchema = wdEntPropEquivalences.getEquivalence(predUri);
									if(mappingsToSchema!=null && !mappingsToSchema.isEmpty()) {
										Statement newSt=rdfWikidata.createStatement(st.getSubject(), rdfWikidata.createProperty(mappingsToSchema.get(0)), st.getObject());
//										System.out.println("replacing "+predUri+" -> "+mappingsToSchema.get(0));
										rdfWikidata.remove(st);
										st=newSt;
									}
								}
								
								if(st.getObject().isURIResource()) {
									String objUri = st.getObject().asResource().getURI();
									if(objUri.startsWith("http://www.wikidata.org/")) {
		//								replace objuri by mapping, if exists	
										ArrayList<String> mappingsToSchema = wdEntPropEquivalences.getEquivalence(objUri);
										if(mappingsToSchema!=null && !mappingsToSchema.isEmpty()) {
											st.getSubject().addProperty(st.getPredicate(), rdfWikidata.createResource(mappingsToSchema.get(0)));
//											System.out.println("replacing "+objUri+" -> "+mappingsToSchema.get(0));
											rdfWikidata.remove(st);
										}
									}
								}
							}
					
//					System.out.println(RdfUtil.printStatements(rdfWikidata));
//							System.out.println("---------------------------------------------------");
						
							SchemaOrgToEdmDataConverter edmConverter=new SchemaOrgToEdmDataConverter();
							Resource rdfEdm = edmConverter.convert(rdfWikidata.createResource(uri), null);
							
							EuropeanaApiClient europeanaApiClient=new EuropeanaApiClient("pSZnyqunm");
							
							ByteArrayOutputStream edmOutBytes=new ByteArrayOutputStream();
							RdfUtil.writeRdf(rdfEdm.getModel(), Lang.TURTLE, edmOutBytes);
							edmOutBytes.close();
							try {
								dataRepository.save("wikidata-edm", uri, edmOutBytes.toByteArray(), "Content-Type", Lang.TURTLE.getContentType().getContentType());
							} catch (Exception e) {
								System.out.printf("Writing EDM for %s failed\n", uri);
								e.printStackTrace(System.out);
							}

							Model rdfEdmAtEuropeana;
							try {
								rdfEdmAtEuropeana = europeanaApiClient.getRecord(europeanaId);
								dataRepository.save("wikidata-edm-at-europeana", uri, edmOutBytes.toByteArray(), "Content-Type", Lang.TURTLE.getContentType().getContentType());

								edmOutBytes=new ByteArrayOutputStream();
								RdfUtil.writeRdf(rdfEdmAtEuropeana, Lang.TURTLE, edmOutBytes);
								edmOutBytes.close();
								try {
									dataRepository.save("wikidata-edm-at-europeana", uri, edmOutBytes.toByteArray(), "Content-Type", Lang.TURTLE.getContentType().getContentType());
								} catch (Exception e) {
									System.out.printf("Writing EDM of Europeana for %s failed\n", uri);
									e.printStackTrace(System.out);
								}
							} catch (inescid.europeanaapi.AccessException e) {
								throw new AccessException(e.getAddress(),e);
							}

							
							double completeness = Dqc10PointRatingCalculator.calculate(rdfEdm.getModel());
							double completenessEuropeana = Dqc10PointRatingCalculator.calculate(rdfEdmAtEuropeana);
							completnesses.add(new ImmutableTriple(uri, completeness,completenessEuropeana));
						stop--;
						return stop != 0;
					}
				}

				);
		
		for(Triple<String, Double, Double> recComp: completnesses) {
			System.out.printf("%s - %.2f - %.2f (Europeana)\n", recComp.getLeft(), recComp.getMiddle(), recComp.getRight());			
		}
	}

	private static void removeOtherResources(Model rdfWikidata, String keepUri) {
		Resource keep=rdfWikidata.createResource(keepUri);
		for(StmtIterator stmts = rdfWikidata.listStatements(); stmts.hasNext() ; ) {
			Statement stm=stmts.next();
			if (!( stm.getSubject().equals(keep) || stm.getObject().equals(keep)) )
				stmts.remove();
		}
	}
	
	private static void removeNonTruthyStatements(Model rdfWikidata) {
		for(StmtIterator stmts = rdfWikidata.listStatements(); stmts.hasNext() ; ) {
			Statement stm=stmts.next();
			if ( stm.getPredicate().getNameSpace().startsWith("http://www.wikidata.org/") &&
					!(stm.getPredicate().getNameSpace().startsWith(RdfRegWikidata.NsWd) || 
					stm.getPredicate().getNameSpace().startsWith(RdfRegWikidata.NsWdt) ||
					stm.getPredicate().getNameSpace().startsWith(RdfRegWikidata.NsWdtn)) 
					)
				stmts.remove();
		}
	}
	
	private static void addRdfTypesFromP31(Model rdfWikidata) {
		for(StmtIterator stmts = rdfWikidata.listStatements(null, RdfRegWikidata.INSTANCE_OF, (RDFNode)null); stmts.hasNext() ; ) {
			Statement stm=stmts.next();
			rdfWikidata.add(rdfWikidata.createStatement(stm.getSubject(), RdfReg.RDF_TYPE, stm.getSubject()));
		}
	}

	public static Resource fetchresource(String resourceUri, CachedHttpRequestService rdfCache) throws AccessException, InterruptedException, IOException {
		SimpleEntry<byte[], List<Entry<String, String>>> propFetched = rdfCache.fetchRdf(resourceUri);
		if (propFetched == null || propFetched.getKey() == null || propFetched.getKey().length == 0) {
			throw new AccessException(resourceUri);
		} else {
			Model rdfWikidata = RdfUtil.readRdf(propFetched.getKey(),
					RdfUtil.fromMimeType(HttpUtil.getHeader("Content-Type", propFetched.getValue())));
			if(rdfWikidata.size()==0)
				throw new AccessException(resourceUri, "No data found");
			Resource wdPropResource = rdfWikidata.getResource(resourceUri);
			return wdPropResource;
		}
	}
}