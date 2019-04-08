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

	public static void main(String[] args)  throws Exception {
		File outputFolder = new File("c://users/nfrei/desktop");
		String httpCacheFolder = "c://users/nfrei/desktop/HttpRepository";
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
		chEntitiesProfile.setOptionProfileObjectsOfTriples(false);
		WikidataSparqlClient.querySolutions("SELECT ?item ?europeana WHERE {" +
//                "  ?item wdt:"+RdfRegWikidata.IIIF_MANIFEST+" ?x ." + 
				"  ?item wdt:" + RdfRegWikidata.EUROPEANAID.getLocalName() + " ?europeana .", new UriHandler() {

					int stop = SAMPLE_RECORDS;

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
						if(stop % 100 == 0)
							System.out.println("progress "+stop);
						return stop != 0;
//						return true;
					}

					
				});
		chEntitiesProfile.finish();

		FileUtils.write(new File(httpCacheFolder, "wd_ch_profile.csv"), chEntitiesProfile.getUsageStats().toCsv(), "UTF-8");
		
//		System.out.println(chEntitiesProfile);
		System.out.println(chEntitiesProfile.printSummary());

		UsageProfiler wdEntPropProfile = new UsageProfiler();
		final EquivalenceMapping wdEntPropEquivalences = new EquivalenceMapping(rdfCache);
		HashSet<String> existingEntsSet=new HashSet<String>();
		HashSet<String> existingPropsSet=new HashSet<String>();
		
		
		//TMP HACK
//		wdEntPropEquivalences.putEquivalence(RdfRegWikidata.CREATIVE_WORK.getURI(), RdfReg.SCHEMAORG_CREATIVE_WORK.getURI());
		
		// harvest all wd entities and properties used, and profile them
		for (Entry<String, ClassUsageStats> entry : chEntitiesProfile.getUsageStats().getClassesStats().entrySet()) {
			String wdResourceUri = entry.getKey();

			if(!wdResourceUri.startsWith("http://www.wikidata.org/")) {
				String[] wdEqUri=new String[1];
				 WikidataSparqlClient.query("SELECT ?item WHERE { ?item wdt:"+
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
				existingEntsSet.add(wdResourceUri);
				Resource wdResource=fetchresource(wdResourceUri, rdfCache);
				removeOtherResources(wdResource.getModel(), wdResourceUri);
				removeNonTruthyStatements(wdResource.getModel());
				
//				System.out.println( RdfUtil.printStatementsOfNamespace(wdResource, RdfReg.NsRdf) );
					wdEntPropProfile.collect(wdResource.getModel());

//					System.out.println(new String(fetched.getKey()));
//					System.out.println(fetched.getValue());
//					System.out.println("Statements for " + wdResourceUri);
//					System.out.println(RdfUtil.printStatements(rdfWikidata));
					wdEntPropEquivalences.analyzeEntity(wdResource, wdResource);
					
				for (String propUri : entry.getValue().getPropertiesProfiles().keySet()) {
					wdEntPropEquivalences.analyzeProperty(propUri, propUri);
					existingPropsSet.add(propUri);
				}
			} catch (Exception e) {
				System.out.printf("Access to %s failed\n", wdResourceUri);
				e.printStackTrace(System.out);
			}
		}
		wdEntPropProfile.finish();

		int existingEntsEqs=0;
		int existingEntsEqsSuper=0;
		int existingPropsEqs=0;
		int existingPropsEqsSuper=0;
		for(String entUri: existingEntsSet) {
			if (wdEntPropEquivalences.getEquivalence(entUri, false)!=null)
				existingEntsEqs++;
			else if (wdEntPropEquivalences.getEquivalence(entUri, true)!=null)
				existingEntsEqsSuper++;
			System.out.println("No eq for "+entUri);
		}
		for(String propUri: existingPropsSet) {
			if (wdEntPropEquivalences.getEquivalence(propUri, false)!=null)
				existingPropsEqs++;
			else if (wdEntPropEquivalences.getEquivalence(propUri, true)!=null)
				existingPropsEqsSuper++;
			else
				System.out.println("No eq for "+propUri);
		}
		System.out.println();

//		write csv
		FileUtils.write(new File(httpCacheFolder, "wd_schemaOrg_equivalences.csv"), 
				"Existing ents. "+existingEntsSet.size()+", eqs.," + existingEntsEqs + ",Existing ent eqs. generic," + existingEntsEqsSuper + "\n" +
				"Existing props,"+existingPropsSet.size()+", eqs.," + existingPropsEqs + ",Existing props eqs. generic," + existingPropsEqsSuper + "\n" +
				wdEntPropEquivalences.toCsv(), "UTF-8");
		
		
//		System.out.println(wdEntPropProfile.printShort());
		System.out.println(wdEntPropEquivalences);
		
		ArrayList<Triple<String, Double, Double>> completnesses=new ArrayList<>();
		
		//Get wikidata entities again, and convert properties to schema.org, convert to EDM and store
		
		WikidataSparqlClient.querySolutions("SELECT ?item ?europeana WHERE {" +
//              "  ?item wdt:"+RdfRegWikidata.IIIF_MANIFEST+" ?x ." + 
				"  ?item wdt:" + RdfRegWikidata.EUROPEANAID.getLocalName() + " ?europeana .", new UriHandler() {
					int stop = SAMPLE_RECORDS;
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
							
//							rdfWikidata.add(Jena.createStatement(wdResource, RdfRegWikidata.INSTANCE_OF, RdfRegWikidata.CREATIVE_WORK));
							
							for (Statement st : rdfWikidata.listStatements().toList()) {
								String predUri = st.getPredicate().getURI().toString();
								
								if(predUri.startsWith("http://www.wikidata.org/")) {
//									if(!predUri.startsWith(RdfRegWikidata.NsWd)) 
//										predUri=RdfRegWikidata.NsWd+predUri.substring(predUri.lastIndexOf('/')+1);
									
									ArrayList<String> mappingsToSchema = wdEntPropEquivalences.getEquivalence(predUri, true);
									if(mappingsToSchema!=null && !mappingsToSchema.isEmpty()) {
										Statement newSt=rdfWikidata.createStatement(st.getSubject(), rdfWikidata.createProperty(mappingsToSchema.get(0)), st.getObject());
//										System.out.println("replacing "+predUri+" -> "+mappingsToSchema.get(0));
										rdfWikidata.remove(st);
										st=newSt;
										rdfWikidata.add(st);
									}
								}
								
								if(st.getObject().isURIResource()) {
									String objUri = st.getObject().asResource().getURI();
									if(objUri.startsWith("http://www.wikidata.org/")) {
		//								replace objuri by mapping, if exists	
										ArrayList<String> mappingsToSchema = wdEntPropEquivalences.getEquivalence(objUri, true);
										if(mappingsToSchema!=null && !mappingsToSchema.isEmpty()) {
											st.getSubject().addProperty(st.getPredicate(), rdfWikidata.createResource(mappingsToSchema.get(0)));
//											System.out.println("replacing "+objUri+" -> "+mappingsToSchema.get(0));
											rdfWikidata.remove(st);
											rdfWikidata.add(st);
										}
									}
								}
							}
					
					System.out.println(RdfUtil.printStatementsOfNamespace(rdfWikidata, RdfReg.NsRdf));
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
			rdfWikidata.add(rdfWikidata.createStatement(stm.getSubject(), RdfReg.RDF_TYPE, stm.getObject()));
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