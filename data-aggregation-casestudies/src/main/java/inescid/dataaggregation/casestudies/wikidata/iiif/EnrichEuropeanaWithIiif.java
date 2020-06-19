package inescid.dataaggregation.casestudies.wikidata.iiif;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;

import inescid.dataaggregation.wikidata.RdfRegWikidata;
import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.crawl.http.HttpResponse;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Svcs;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.profile.UsageProfiler;
import inescid.util.RdfUtil;

public class EnrichEuropeanaWithIiif {

	
	public static void main(String[] args) {
//		boolean DEBUGING=true;
		boolean DEBUGING=false;

		File outputFolder=new File("c://users/nfrei/desktop");
		String httpCacheFolder="c://users/nfrei/desktop/HttpRepository";
		
		IiifResourcesEnrichmentReport rep=new IiifResourcesEnrichmentReport();
		
		if(args.length>0) {
			outputFolder=new File(args[0]);
			DEBUGING=false;
		}
		if(args.length>1)
			httpCacheFolder=args[1];

		if(!outputFolder.exists())
			outputFolder.mkdirs();
		
		Global.init_componentHttpRequestService();
		Global.init_componentDataRepository(httpCacheFolder);

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
		
		System.out.printf("Settings:\n-OutpputFolder:%s\n-Cache:%s\n------------------------\n",
				outputFolder.getPath(), httpCacheFolder);
		if(DEBUGING)
			System.out.println("*********** DEBUG MODE RUNNING ************");
		
		CachedHttpRequestService rdfCache=new CachedHttpRequestService();
		rdfCache.setRequestRetryAttempts(3);
		
		final HashSet<String> wikidataUrisAlreadyProcessed=new HashSet<String>();

		int wdCount=0;
		{
			String queryString = "PREFIX bd: <http://www.bigdata.com/rdf#>\n" +
					"PREFIX wikibase: <http://wikiba.se/ontology#>\n" +
	                "PREFIX wdt: <http://www.wikidata.org/prop/direct/>\n" +
	                "PREFIX wd: <http://www.wikidata.org/entity/>\n" +
	                "SELECT ?item  WHERE {" + 
	                "  ?item wdt:P6108 ?x ." + 
	//                "  ?item wdt:P727 ?r ." + 
	//                "  SERVICE wikibase:label { bd:serviceParam wikibase:language \"[AUTO_LANGUAGE],en\". }" + 
	                "}";
	        QueryExecution qexec = QueryExecutionFactory.sparqlService("https://query.wikidata.org/sparql", queryString);
	        try {
	            ResultSet results = qexec.execSelect();
	//            ResultSetFormatter.out(System.out, results, query);
	            while(results.hasNext()) {
	            	wdCount++;
	            	if(DEBUGING && wdCount>500)
	            		break;
	            	if(wdCount % 100 == 0)
	            		System.out.println("Progress: "+wdCount+" Wikidata entities processed");
	
					String wdUri = null;
	            	try {
						QuerySolution hit = results.next();
						Resource wdResource = hit.getResource("item");
						wdUri = wdResource.getURI();
						wikidataUrisAlreadyProcessed.add(wdUri);
						HttpResponse wikidataFetched = rdfCache.fetchRdf(wdUri);
						if(wikidataFetched.isSuccess()) {
							System.out.printf("Access to %s failed\n", wdUri);
							rep.wdAccessFailure(wdUri);
	            		} else {
							Model rdfWikidata = RdfUtil.readRdf(wikidataFetched.getBody(), RdfUtil.fromMimeType(wikidataFetched.getHeader("Content-Type")));
							StmtIterator stms = rdfWikidata.listStatements(null, RdfRegWikidata.IIIF_MANIFEST, (RDFNode)null);
							ArrayList<String> iiifManifests=new ArrayList<>();
							while(stms.hasNext()) {
								Statement st=stms.next();
								String manifestUri;
								if(st.getObject().isLiteral()) {
									manifestUri=st.getString();
								} else if(st.getObject().isURIResource()) {
									manifestUri=st.getObject().asResource().getURI();
								} else
									manifestUri=null;
								if(manifestUri!=null){
									HttpResponse manifestFetched = rdfCache.fetchRdf(manifestUri);
									if(manifestFetched.isSuccess()) {
										System.out.printf("Access to %s failed\n", manifestUri);									
									} else {
	//									System.out.printf("Saved %s (%d Kb)\n", manifestUri, manifestFetched.getKey().length / 1024);
										iiifManifests.add(manifestUri);
									}
								}
							}
							if(!iiifManifests.isEmpty()) {
								String wdCol = null;
								Statement wdColStm = rdfWikidata.getProperty(wdResource , RdfRegWikidata.COLLECTION);
								if(wdColStm==null) {
									wdCol="[without collection data]";
								} else if(wdColStm.getObject().isLiteral()) {
									wdCol="http://data.europeana.eu/item/"+wdColStm.getString();
								} else if(wdColStm.getObject().isURIResource()) 
									wdCol=wdColStm.getObject().asResource().getURI();
								
								stms = rdfWikidata.listStatements(null, RdfRegWikidata.EUROPEANAID, (RDFNode)null);
								if(!stms.hasNext()) {
									rep.notLinkedToEuropeana(wdCol, wdUri);
								} else {
									while(stms.hasNext()) {
										Statement st=stms.next();
										final String europeanaObjectUri;
										if(st.getObject().isLiteral()) {
											europeanaObjectUri="http://data.europeana.eu/item/"+st.getString();
											if(europeanaIdsBroken.contains(st.getString())) 
												continue;
										} else if(st.getObject().isURIResource()) {
											europeanaObjectUri=st.getObject().asResource().getURI();
										} else
											europeanaObjectUri=null;
										if(europeanaObjectUri!=null){
											HttpResponse europeanaFetched = rdfCache.fetchRdf(europeanaObjectUri);
											if(europeanaFetched.isSuccess()) {
												System.out.printf("Access to %s failed\n", europeanaObjectUri);	
												rep.idNotFoundInEuropeana(europeanaObjectUri);
											} else {
	//											System.out.printf("Saved %s (%d Kb)\n", europeanaObjectUri, europeanaFetched.getKey().length / 1024);
												Model rdfEdm = RdfUtil.readRdf(europeanaFetched.getBody(), RdfUtil.fromMimeType(europeanaFetched.getHeader("Content-Type")));
												
												String europeanaCollection=null;
												Resource agg=RdfUtil.findFirstResourceWithProperties(rdfEdm, Rdf.type, Edm.EuropeanaAggregation, null, null);
												if(agg==null) {
													System.out.printf("No edm:Aggregation for %s\n", europeanaObjectUri);	
												} else {
													Statement property = agg.getProperty(Edm.datasetName);
													europeanaCollection=RdfUtil.getUriOrLiteralValue(property.getObject());
												}
												Set<Resource> iiifWebResources=RdfUtil.findResourceWithProperties(rdfEdm, Rdf.type, Edm.WebResource, Svcs.has_service, null);
												if(iiifWebResources.isEmpty()) {
													rep.enrichements(europeanaCollection, europeanaObjectUri, wdUri, iiifManifests);
													rep.profileEdm(europeanaCollection, rdfEdm);
													for(String manifestUri : iiifManifests) {
														HttpResponse manifestFetched = rdfCache.fetchRdf(manifestUri);
														Model rdfManifest = RdfUtil.readRdf(manifestFetched.getBody(), Lang.JSONLD);
														rep.profileIiifManifest(europeanaCollection, rdfManifest);
													}
												} else
													rep.existingInEuropeana(europeanaCollection, europeanaObjectUri, wdUri, iiifManifests);
											}
										}
									}	
								}
							}
						}
					} catch (Exception e) {
						System.err.println("In record: "+wdUri);
						e.printStackTrace();
						System.err.println("PROCEEDING TO NEXT RECORD");
					}
	            }
	        } catch (Exception ex) {
	        	ex.printStackTrace();
	        } finally {
	            qexec.close();
	        }
		}
		
		{

			String queryString = "PREFIX bd: <http://www.bigdata.com/rdf#>\n" +
					"PREFIX wikibase: <http://wikiba.se/ontology#>\n" +
	                "PREFIX wdt: <http://www.wikidata.org/prop/direct/>\n" +
	                "PREFIX wd: <http://www.wikidata.org/entity/>\n" +
	                "SELECT ?item  WHERE {" + 
//	                "  ?item wdt:P6108 ?x ." + 
	                "  ?item wdt:P727 ?r ." + 
	//                "  SERVICE wikibase:label { bd:serviceParam wikibase:language \"[AUTO_LANGUAGE],en\". }" + 
	                "}";
	        QueryExecution qexec = QueryExecutionFactory.sparqlService("https://query.wikidata.org/sparql", queryString);
	        try {
	            ResultSet results = qexec.execSelect();
	//            ResultSetFormatter.out(System.out, results, query);
	            while(results.hasNext()) {
	            	wdCount++;
	            	if(wdCount % 100 == 0) {
	            		System.out.println("Progress: "+wdCount+" Wikidata media entities processed");
	            	} else if(wdCount % 1001 == 0) {
	            		System.out.println("WD media enrichments for Europeana: " +
	            				rep.enrichementsByWikimedia.sizeTotal()
	            				);
	            		System.out.println("WD media existing in Europeana: " +
	            				rep.existingInEuropeanaFromWikimedia.sizeTotal()
	            				);
	            		if(DEBUGING)
	            			break;
	            	}
	            	
					String wdUri = null;
	            	try {
						QuerySolution hit = results.next();
						Resource wdResource = hit.getResource("item");
						wdUri = wdResource.getURI();
						if(wikidataUrisAlreadyProcessed.contains(wdUri))
							continue;
						HttpResponse wikidataFetched = rdfCache.fetchRdf(wdUri);
						if(wikidataFetched.isSuccess()) {
							System.out.printf("Access to %s failed\n", wdUri);
							rep.wdAccessFailure(wdUri);
	            		} else {
							Model rdfWikidata = RdfUtil.readRdf(wikidataFetched.getBody(), RdfUtil.fromMimeType(wikidataFetched.getHeader("Content-Type")));

							StmtIterator stms = rdfWikidata.listStatements(null, RdfRegWikidata.EUROPEANAID, (RDFNode)null);
							if(stms.hasNext()) {
								while(stms.hasNext()) {
									Statement st=stms.next();
									final String europeanaObjectUri;
									if(st.getObject().isLiteral()) {
										europeanaObjectUri="http://data.europeana.eu/item/"+st.getString();
										if(europeanaIdsBroken.contains(st.getString())) 
											continue;
									} else if(st.getObject().isURIResource()) {
										europeanaObjectUri=st.getObject().asResource().getURI();
									} else
										europeanaObjectUri=null;
									if(europeanaObjectUri!=null){
										boolean hasImage=false;
										for(Property imgProperty: new Property[] {RdfRegWikidata.IMAGE, RdfRegWikidata.ICON, 
												RdfRegWikidata.LOGO_IMAGE, RdfRegWikidata.COAT_OF_ARMS, 
												RdfRegWikidata.SEAL_IMAGE, RdfRegWikidata.FLAG_IMAGE
												, RdfRegWikidata.COMEMORATIVE_PLAQUE, RdfRegWikidata.PLACE_NAME_SIGN, RdfRegWikidata.MONOGRAM, 
												RdfRegWikidata.IMAGE_OF_TOMBSTONE, RdfRegWikidata.SIGNATURE, RdfRegWikidata.COLLAGE_IMAGE,
												RdfRegWikidata.SECTIONAL_VIEW, RdfRegWikidata.NIGHTTIME_VIEW, RdfRegWikidata.PANORAMA_VIEW, RdfRegWikidata.PHOTOSHERE_IMAGE, RdfRegWikidata.WINTER_VIEW, 
												RdfRegWikidata.IMAGE_OF_INTERIOR}) {
											String wdCol = null;
											Statement wdColStm = rdfWikidata.getProperty(wdResource , imgProperty);
											if(wdColStm!=null) {
												hasImage=true;
												break;
											}
										}
										if(! hasImage) 
											continue;
										
										HttpResponse europeanaFetched = rdfCache.fetchRdf(europeanaObjectUri);
										if(europeanaFetched.isSuccess()) {
											System.out.printf("Access to %s failed\n", europeanaObjectUri);	
											rep.idNotFoundInEuropeana(europeanaObjectUri);
										} else {
//											System.out.printf("Saved %s (%d Kb)\n", europeanaObjectUri, europeanaFetched.getKey().length / 1024);
											Model rdfEdm = RdfUtil.readRdf(europeanaFetched.getBody(), RdfUtil.fromMimeType(europeanaFetched.getHeader("Content-Type")));
											
											String europeanaCollection=null;
											Resource agg=RdfUtil.findFirstResourceWithProperties(rdfEdm, Rdf.type, Edm.EuropeanaAggregation, null, null);
											if(agg==null) {
												System.out.printf("No edm:Aggregation for %s\n", europeanaObjectUri);	
											} else {
												Statement property = agg.getProperty(Edm.datasetName);
												europeanaCollection=RdfUtil.getUriOrLiteralValue(property.getObject());
											}
											Set<Resource> iiifWebResources=RdfUtil.findResourceWithProperties(rdfEdm, Rdf.type, Edm.WebResource, Svcs.has_service, null);
											if(iiifWebResources.isEmpty()) {
												rep.enrichementsByWikimedia(europeanaCollection, europeanaObjectUri, wdUri);
												rep.profileEdm(europeanaCollection, rdfEdm);																								
											} else
												rep.existingInEuropeanaFromWikimedia(europeanaCollection, europeanaObjectUri, wdUri);
										}
									}
								}	
							}
						}
					} catch (Exception e) {
						System.err.println("In record: "+wdUri);
						e.printStackTrace();
						System.err.println("PROCEEDING TO NEXT RECORD");
					}
	            }
	        } catch (Exception ex) {
	        	ex.printStackTrace();
	        } finally {
	            qexec.close();
	        }
		}

		System.out.println("WD enrichments in Europeana: " +
				rep.enrichementsByWikimedia.sizeTotal()
				);
		System.out.println("WD existing in Europeana: " +
				rep.existingInEuropeanaFromWikimedia.sizeTotal()
				);
		
        for(UsageProfiler p: rep.edmUsageProfilers.values())
        	p.finish();
        for(UsageProfiler p: rep.iiifUsageProfilers.values())
        	p.finish();
        System.out.println("Completed: "+wdCount+" Wikidata entities processed");
        try {
			IiifResourcesEnrichmentReportViewer view=new IiifResourcesEnrichmentReportViewer(outputFolder, rep);
			view.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
//            System.out.printf("Harvested %d Wikidata resources\n", wdCount);            
//            System.out.printf("Europeana IDs not found: %d \n", idNotFoundInEuropeana);            
//            System.out.printf("IIIF resources already in Europeana: %d \n", iiifExistsInEuropeana);            
//            System.out.printf("IIIF enrichments: %d \n", iiifNewInEuropeana);            
//            System.out.printf("IIIF enriched data providers:\n");            
//            for(String prov: enrichedProvidersInEuropeana) {
//            	System.out.printf(" - %s\n", prov);                        	
//            }
        System.out.println("Completed report");
        System.out.println("Job done!");
	}
}
