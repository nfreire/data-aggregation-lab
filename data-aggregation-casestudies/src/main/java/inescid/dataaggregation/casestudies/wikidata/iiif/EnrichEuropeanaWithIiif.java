package inescid.dataaggregation.casestudies.wikidata.iiif;

import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

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

import eu.europeana.ld.jena.JenaUtils;
import inescid.dataaggregation.casestudies.wikidata.RdfRegWikidata;
import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.crawl.http.HttpRequest;
import inescid.dataaggregation.dataset.GlobalCore;
import inescid.dataaggregation.dataset.convert.EdmUtil;
import inescid.dataaggregation.dataset.convert.RdfReg;
import inescid.dataaggregation.dataset.profile.UsageProfiler;
import inescid.util.HttpUtil;
import inescid.util.RdfUtil;
import inescid.util.RdfUtil.Jena;
import inescid.util.RetryExec;

public class EnrichEuropeanaWithIiif {

	
	public static void main(String[] args) {
		String httpCacheFolder="c://users/nfrei/desktop/HttpRepository";
		File outputFolder=new File("c://users/nfrei/desktop");
		
		IiifResourcesEnrichmentReport rep=new IiifResourcesEnrichmentReport();
		
		if(args.length>0)
			outputFolder=new File(args[0]);
		if(args.length>1)
			httpCacheFolder=args[1];

		if(!outputFolder.exists())
			outputFolder.mkdirs();
		
		System.out.printf("Settings:\n-OutpputFolder:%s\n-Cache:%s\n------------------------\n",
				outputFolder.getPath(), httpCacheFolder);
		
		GlobalCore.init_componentHttpRequestService();
		GlobalCore.init_componentDataRepository(httpCacheFolder);
		
		CachedHttpRequestService rdfCache=new CachedHttpRequestService();
		rdfCache.setRequestRetryAttempts(3);
		
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
            int wdCount=0;
            while(results.hasNext()) {
            	wdCount++;
//            	if(wdCount>50)
//            		break;
            	if(wdCount % 50 == 0)
            		System.out.println("Progress: "+wdCount+" Wikidata entities processed");

				String wdUri = null;
            	try {
					QuerySolution hit = results.next();
					Resource wdResource = hit.getResource("item");
					wdUri = wdResource.getURI();
					SimpleEntry<byte[], List<Entry<String, String>>> wikidataFetched = rdfCache.fetchRdf(wdUri);
					if(wikidataFetched==null || wikidataFetched.getKey()==null || wikidataFetched.getKey().length==0) {
						System.out.printf("Access to %s failed\n", wdUri);
						rep.wdAccessFailure(wdUri);
            		} else {
						Model rdfWikidata = RdfUtil.readRdf(wikidataFetched.getKey(), RdfUtil.fromMimeType(HttpUtil.getHeader("Content-Type", wikidataFetched.getValue())));
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
								SimpleEntry<byte[], List<Entry<String, String>>> manifestFetched = rdfCache.fetchRdf(manifestUri);
								if(manifestFetched==null || manifestFetched.getKey()==null || manifestFetched.getKey().length==0) {
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
							} if(wdColStm.getObject().isLiteral()) {
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
									} else if(st.getObject().isURIResource()) {
										europeanaObjectUri=st.getObject().asResource().getURI();
									} else
										europeanaObjectUri=null;
									if(europeanaObjectUri!=null){
										SimpleEntry<byte[], List<Entry<String, String>>> europeanaFetched = rdfCache.fetchRdf(europeanaObjectUri);
										if(europeanaFetched==null || europeanaFetched.getKey()==null || europeanaFetched.getKey().length==0) {
											System.out.printf("Access to %s failed\n", europeanaObjectUri);	
											rep.idNotFoundInEuropeana(europeanaObjectUri);
										} else {
//											System.out.printf("Saved %s (%d Kb)\n", europeanaObjectUri, europeanaFetched.getKey().length / 1024);
											Model rdfEdm = RdfUtil.readRdf(europeanaFetched.getKey(), RdfUtil.fromMimeType(HttpUtil.getHeader("Content-Type", europeanaFetched.getValue())));
											
											String europeanaCollection=null;
											Resource agg=RdfUtil.findFirstResourceWithProperties(rdfEdm, RdfReg.RDF_TYPE, RdfReg.EDM_EUROPEANA_AGGREGATION, null, null);
											if(agg==null) {
												System.out.printf("No edm:Aggregation for %s\n", europeanaObjectUri);	
											} else {
												Statement property = agg.getProperty(RdfReg.EDM_DATASET_NAME);
												europeanaCollection=RdfUtil.getUriOrLiteralValue(property.getObject());
											}
											Set<Resource> iiifWebResources=RdfUtil.findResourceWithProperties(rdfEdm, RdfReg.RDF_TYPE, RdfReg.EDM_WEB_RESOURCE, RdfReg.SVCS_HAS_SERVICE, null);
											if(iiifWebResources.isEmpty()) {
												rep.enrichements(europeanaCollection, europeanaObjectUri, wdUri, iiifManifests);
												rep.profileEdm(europeanaCollection, rdfEdm);
												for(String manifestUri : iiifManifests) {
													SimpleEntry<byte[], List<Entry<String, String>>> manifestFetched = rdfCache.fetchRdf(manifestUri);
													Model rdfManifest = RdfUtil.readRdf(manifestFetched.getKey(), Lang.JSONLD);
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
            for(UsageProfiler p: rep.edmUsageProfilers.values())
            	p.finish();
            for(UsageProfiler p: rep.iiifUsageProfilers.values())
            	p.finish();
            System.out.println("Completed: "+wdCount+" Wikidata entities processed");
            IiifResourcesEnrichmentReportViewer view=new IiifResourcesEnrichmentReportViewer(outputFolder, rep);
            view.run();
//            System.out.printf("Harvested %d Wikidata resources\n", wdCount);            
//            System.out.printf("Europeana IDs not found: %d \n", idNotFoundInEuropeana);            
//            System.out.printf("IIIF resources already in Europeana: %d \n", iiifExistsInEuropeana);            
//            System.out.printf("IIIF enrichments: %d \n", iiifNewInEuropeana);            
//            System.out.printf("IIIF enriched data providers:\n");            
//            for(String prov: enrichedProvidersInEuropeana) {
//            	System.out.printf(" - %s\n", prov);                        	
//            }
            System.out.println("Completed report");
        } catch (Exception ex) {
        	ex.printStackTrace();
        } finally {
            qexec.close();
        }
        System.out.println("Job done!");
	}
}
