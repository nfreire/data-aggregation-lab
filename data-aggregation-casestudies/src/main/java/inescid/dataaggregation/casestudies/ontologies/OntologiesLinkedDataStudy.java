package inescid.dataaggregation.casestudies.ontologies;

import java.io.File;
import java.io.PrintStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.store.Repository;

public class OntologiesLinkedDataStudy {

	public static void main(String[] args) {
		try {
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

			System.out.printf(
					"Settings:\n-OutpputFolder:%s\n-Cache:%s\n-Records:%d\n------------------------\n",
					outputFolder.getPath(), httpCacheFolder, SAMPLE_RECORDS);

			Global.init_componentHttpRequestService();
			Global.init_componentDataRepository(httpCacheFolder);

			Repository dataRepository = Global.getDataRepository();

			CachedHttpRequestService rdfCache = new CachedHttpRequestService();
			rdfCache.setRequestRetryAttempts(1);
			String[][] ontologiesUris=new String[][] {
				new String[] {"Dublin Core Metadata Element Set",
						"http://purl.org/dc/elements/1.1/", null},
				new String[] {"DCMI Metadata Terms",
						"http://purl.org/dc/terms/", null},
				new String[] {"The DBpedia Ontology",
						"http://dbpedia.org/ontology/", null},
//				new String[] {"http://wikiba.se/ontology#", null},
				new String[] {"BIBFRAME vocabulary",
						"http://id.loc.gov/ontologies/bibframe/", null},
				new String[] {"RDA Vocabularies",
						"http://rdaregistry.info/Elements/c/", "http://rdaregistry.info/Elements/c.ttl",
				"http://rdaregistry.info/Elements/a/", "http://rdaregistry.info/Elements/a.ttl",
				"http://rdaregistry.info/Elements/e/", "http://rdaregistry.info/Elements/e.ttl",
				"http://rdaregistry.info/Elements/i/", "http://rdaregistry.info/Elements/i.ttl",
				"http://rdaregistry.info/Elements/m/", "http://rdaregistry.info/Elements/m.ttl",
				"http://rdaregistry.info/Elements/n/", "http://rdaregistry.info/Elements/n.ttl",
				"http://rdaregistry.info/Elements/p/", "http://rdaregistry.info/Elements/p.ttl",
				"http://rdaregistry.info/Elements/t/", "http://rdaregistry.info/Elements/t.ttl",
				"http://rdaregistry.info/Elements/w/", "http://rdaregistry.info/Elements/w.ttl",
				"http://rdaregistry.info/Elements/x/", "http://rdaregistry.info/Elements/x.ttl",
				"http://rdaregistry.info/Elements/u/", "http://rdaregistry.info/Elements/u.ttl"},
				new String[] {"FOAF Vocabulary",
						"http://xmlns.com/foaf/0.1/", null},
				new String[] {"CIDOC Conceptual Reference Model",
						"http://www.cidoc-crm.org/cidoc-crm/", "http://www.cidoc-crm.org/sites/default/files/CIDOCCRM_ecrm.owl"},
				new String[] {"Europeana Data Model",
						"http://www.europeana.eu/schemas/edm/", null},
				new String[] {"LIDO Terminology",
						"http://terminology.lido-schema.org/identifier_type", null,
				"http://terminology.lido-schema.org/recordMetadataDate_type", null,
				"http://terminology.lido-schema.org/recordType", null,
				"http://terminology.lido-schema.org/repositorySet_type", null,
				"http://terminology.lido-schema.org/resourceRepresentation_type", null,
				"http://terminology.lido-schema.org/termMaterialsTech_type", null},
				new String[] {"Wikidata Ontology",
						"http://www.wikidata.org/entity/", null,
						"http://www.wikidata.org/entity/statement/", null,
						"http://www.wikidata.org/value/", null,
						"http://www.wikidata.org/prop/direct/", null,
						"http://www.wikidata.org/prop/", null,
						"http://www.wikidata.org/prop/statement/", null,
						"http://www.wikidata.org/prop/qualifier/", null}, 
				new String[] {"GeoNames Ontology",
						"http://www.geonames.org/ontology#", null},
				new String[] {"GND Ontology",
						"http://d-nb.info/standards/elementset/gnd#", null},
				// http://terminology.lido-schema.org URIs are resolvable to owl, but need to use sparql to locate them
					//				"http://www.wikidata.org/entity/", //no owl (use sparql)
			};
			
			
			File ontologyDetailsOutFolder=new File(outputFolder, "ontology_details");
			if(!ontologyDetailsOutFolder.exists())
				ontologyDetailsOutFolder.mkdir();
			File ontologyDetailsIndexRawDataFile = new File(ontologyDetailsOutFolder, "OntologyResultsCsvIndex.html");
			PrintStream ps = new PrintStream(ontologyDetailsIndexRawDataFile, "UTF-8");
			ps.println("<HTML><BODY>");
			ps.println("<H3>RDF Data Profile reports</H3>");
			ps.println("<p>Report date: "+new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date())+"</p>");
			ps.println("<p>Reports:</p>");
			ps.println("<ul>");			
			
			AllOntologiesAnalyser allAnaliser=new AllOntologiesAnalyser();
			for(int i=0; i<ontologiesUris.length; i++) {
				String[] ontUriGroup=ontologiesUris[i];
				OntologyAnalyzer analyser=new OntologyAnalyzer(rdfCache);
				analyser.title=ontUriGroup[0];
				for(int ig=1; ig<ontUriGroup.length; ig++) {
					String ontUri=ontUriGroup[ig];
					ig++;
					String ontDef=ontUriGroup[ig];
					try {
						analyser.runAnalyzis(ontUri, ontDef, allAnaliser.allOntologiesProfiler, allAnaliser.allOntologiesProfilerDataElements);
					} catch (Exception e) {
						System.out.println("No data for ontology "+ontUri+" Exc: "+e.getMessage());
						e.printStackTrace();
					}
				}
				try {
					String ontFn = "ontology-profile-"+URLEncoder.encode(analyser.title, StandardCharsets.UTF_8.name())+"-ns.csv";
					FileUtils.write(new File(ontologyDetailsOutFolder, ontFn), analyser.report.profileOfOntology.toCsv(null), "UTF8");
					String elsFn = "ontology-profile-"+URLEncoder.encode(analyser.title, StandardCharsets.UTF_8.name())+"-elements.csv";
					FileUtils.write(new File(ontologyDetailsOutFolder, elsFn), analyser.report.profileOfDataElements.toCsv(null), "UTF8");
					ps.printf("<li>%s<br />&nbsp;&nbsp;&nbsp;- <a href=\"%s\">ontology RDF resource</a>", 
							analyser.title, ontFn);
					ps.printf("<br />&nbsp;&nbsp;&nbsp;- <a href=\"%s\">ontology subelements RDF resources</a></li>", 
						elsFn);
				} catch (NullPointerException e) {
					//not crawlable, skip it
				}
				
				System.out.println(analyser.namespace);
				System.out.println(analyser.report.ontologyExists);
				System.out.println();
				allAnaliser.addOntology(analyser);
			}
			ps.println("</ul>");			
			ps.println("\n</BODY></HTML>");		
			ps.close();

			allAnaliser.runAnalysis();
			System.out.println("\n\n########################\n### ALL ONTOLOGIES ###\n########################");
			System.out.println(allAnaliser);
			System.out.println("\n\n########################\n### ALL ONTOLOGIES PROFILE ###\n########################");
			System.out.println(allAnaliser.allOntologiesProfiler);
			System.out.println("\n\n########################\n### ALL ONTOLOGIES DATA ELEMENTS PROFILE ###\n########################");
			System.out.println(allAnaliser.allOntologiesProfilerDataElements);
			
			FileUtils.write(new File(outputFolder, "all-ontologies-profile.txt"), allAnaliser.allOntologiesProfiler.toString(), "UTF8");
			FileUtils.write(new File(outputFolder, "all-ontologies-elements-profile.txt"), allAnaliser.allOntologiesProfilerDataElements.toString(), "UTF8");
			
			FileUtils.write(new File(outputFolder, "all-ontologies-global-analyzer.csv"), allAnaliser.toCsv(ontologiesUris), "UTF8");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
