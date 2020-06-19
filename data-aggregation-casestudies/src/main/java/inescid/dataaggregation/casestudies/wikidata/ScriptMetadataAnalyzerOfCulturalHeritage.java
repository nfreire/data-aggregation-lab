package inescid.dataaggregation.casestudies.wikidata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;

import inescid.dataaggregation.wikidata.RdfRegWikidata;
import inescid.dataaggregation.wikidata.SparqlClientWikidata;
import inescid.dataaggregation.wikidata.WikidataUtil;
import inescid.dataaggregation.casestudies.wikidata.evaluation.Dqc10PointRatingCalculatorNoRights;
import inescid.dataaggregation.casestudies.wikidata.evaluation.EdmValidation;
import inescid.dataaggregation.casestudies.wikidata.evaluation.ValidatorForNonPartners;
import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.crawl.http.HttpResponse;
import inescid.dataaggregation.data.ContentTypes;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;
import inescid.dataaggregation.data.model.Owl;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Rdfs;
import inescid.dataaggregation.data.model.Schemaorg;
import inescid.dataaggregation.data.validation.EdmXmlValidator.Schema;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.convert.SchemaOrgToEdmDataConverter;
import inescid.dataaggregation.dataset.convert.rdfconverter.ConversionSpecificationAnalyzer;
import inescid.dataaggregation.dataset.profile.ClassUsageStats;
import inescid.dataaggregation.dataset.profile.UsageProfiler;
import inescid.dataaggregation.dataset.profile.completeness.Dqc10PointRatingCalculator;
import inescid.dataaggregation.store.Repository;
import inescid.europeanaapi.EuropeanaApiClient;
import inescid.opaf.data.EdmUtil;
import inescid.util.AccessException;
import inescid.util.MapOfInts;
import inescid.util.RdfUtil;
import inescid.util.SparqlClient.Handler;
import inescid.util.datastruct.CsvDataPersistReader;
import inescid.util.datastruct.CsvDataPersistWriter;
import inescid.util.googlesheets.GoogleSheetsCsvUploader;

public class ScriptMetadataAnalyzerOfCulturalHeritage {
	enum DataDumps {
		WIKIDATA_EDM, EUROPEANA_EDM, WIKIDATA_ONTOLOGY, WIKIDATA_SCHEMAORG
	};

	public static class Files {
		public static File defaultOutputFolder=new File("c://users/nfrei/desktop/data/wikidata-cho-results-march-2020");
		
		public static File edmValidation;
		public static File edmValidationNonPartner;
		public static File wdChoProfile;
		public static File wdChoProfileNonWdProps;
		public static File wdChoProfilePersist;
		public static File wdChoSchemaOrgProfile;
		public static File wdChoSchemaOrgValuesFolder;
		public static File wdSchemaOrgConversionToEdmAnalysisReport;
		public static File wdSchemaOrgConversionToEdmAnalysisSummary;
		public static File wdChoProfileValuesFolder;
		public static File wdMetamodelProfile;
		public static File wdMetamodelProfileNonWdProps;
		public static File wdMetamodelProfilePersist;
		public static File wdMetamodelValuesFolder;
		public static File schemaOrgEquivalences;
		public static File wdDatasetOntologyZip;
		public static File wdDatasetEdmZip;
		public static File europeanaDatasetEdmZip;
		public static File wdUnconvertableToSchemaorg;
		public static File wdCompletenessEdm;
		public static File wdEuropeanaCollectionCounts;
		public static File wdEuropeanaIdMap;

		public static File wdContextEntitiesEdm;
		public static File wdLanguageEdm;
		public static File wdLangTagsEdm;
		
		public static void init(File outputFolder) {
			wdChoProfile=new File(outputFolder, "wd_ch_profile.csv");
			wdChoProfileNonWdProps=new File(outputFolder, "wd_ch_profile_non_wd_props.csv");
			wdChoProfilePersist=new File(outputFolder, "wd_ch_profile.obj");
			wdChoProfileValuesFolder=new File(outputFolder, "wd_ch_profile-property_values_distribution");
			wdChoSchemaOrgProfile=new File(outputFolder, "wd_ch_schemaorg_profile.csv");
			wdChoSchemaOrgValuesFolder=new File(outputFolder, "wd_ch_schemaorg_profile_property_values_distribution");
			wdSchemaOrgConversionToEdmAnalysisReport=new File(outputFolder, "wd_ch_schemaorg_conversion_edm_anailsys_report.csv");
			wdSchemaOrgConversionToEdmAnalysisSummary=new File(outputFolder, "wd_ch_schemaorg_conversion_edm_anailsys_summary.csv");
			edmValidation= new File(outputFolder, "edm-validation.csv");
			edmValidationNonPartner=new File(outputFolder, "edm-validation-nonpartner.csv");
			wdMetamodelProfile=new File(outputFolder, "wd_metamodel_profile.csv");
			wdMetamodelProfileNonWdProps=new File(outputFolder, "wd_metamodel_profile_non_wd_props.csv");
			wdMetamodelProfilePersist=new File(outputFolder, "wd_metamodel_profile.obj");
			wdMetamodelValuesFolder=new File(outputFolder, "wd_metamodel_profile-property_values_distribution");
			schemaOrgEquivalences=new File(outputFolder, "wd_schemaOrg_equivalences.csv");
			wdDatasetOntologyZip=new File(outputFolder, "wikidata-subdataset-ontology.zip");
			wdDatasetEdmZip=new File(outputFolder, "wikidata-subdataset-edm.zip");
			europeanaDatasetEdmZip=new File(outputFolder, "europeana-subdataset-edm.zip");
			wdUnconvertableToSchemaorg=new File(outputFolder, "wikidata-unconvertable-to-schemaorg.csv");
			wdCompletenessEdm=new File(outputFolder, "wikidata-completeness-edm.csv");
			wdEuropeanaCollectionCounts=new File(outputFolder, "collections_wd_and_europeana_counts.csv");
			wdEuropeanaIdMap=new File(outputFolder, "wd_to_europeana_id_map.csv");
			wdContextEntitiesEdm=new File(outputFolder, "wikidata-context-entities-edm.csv");
			wdLanguageEdm=new File(outputFolder, "wikidata-languages-saturation-edm.csv");
			wdLangTagsEdm=new File(outputFolder, "wikidata-langtags-saturation-edm.csv");
		}
		
	}
	
	protected static class GoogleSheetsUploads {
		static String credentialspath="C:\\Users\\nfrei\\.credentials\\Data Aggregation Lab-b1ec5c3705fc.json";
		static String spreadsheetId="1i2cK_QwUPNPxdU2qQXRnhY-nry_HP3OKUHJS8hApoRI";
		
		static void updateCsvsAtGoogle() throws IOException {
			GoogleSheetsCsvUploader.update(spreadsheetId, "WD-Europeana collections", Files.wdEuropeanaCollectionCounts);
			GoogleSheetsCsvUploader.update(spreadsheetId, "WD CHO's RDF profile", Files.wdChoProfile);
			GoogleSheetsCsvUploader.update(spreadsheetId, "WD CHO's RDF profile non WD", Files.wdChoProfileNonWdProps);
			GoogleSheetsCsvUploader.update(spreadsheetId, "WD Metamodel's RDF profile", Files.wdMetamodelProfile);
			GoogleSheetsCsvUploader.update(spreadsheetId, "WD Metamodel's RDF profile non WD", Files.wdMetamodelProfileNonWdProps);
			GoogleSheetsCsvUploader.update(spreadsheetId, "WD-Schema equivalences", Files.schemaOrgEquivalences);
			GoogleSheetsCsvUploader.update(spreadsheetId, "WD CHO's Schema.org RDF profile", Files.wdChoSchemaOrgProfile);
			GoogleSheetsCsvUploader.update(spreadsheetId, "WD CHO's EDM conversion analysis", Files.wdSchemaOrgConversionToEdmAnalysisReport);
			GoogleSheetsCsvUploader.update(spreadsheetId, "WD CHO's EDM conversion summary", Files.wdSchemaOrgConversionToEdmAnalysisSummary);
			GoogleSheetsCsvUploader.update(spreadsheetId, "WD CHOs unconvertable to Schema", Files.wdUnconvertableToSchemaorg);
			GoogleSheetsCsvUploader.update(spreadsheetId, "WD validation of EDM", Files.edmValidationNonPartner);
			GoogleSheetsCsvUploader.update(spreadsheetId, "WD compleness of EDM", Files.wdCompletenessEdm);
		}
		static void updateCsvsAtGoogleForFinalEdm() throws IOException {
			GoogleSheetsCsvUploader.update(spreadsheetId, "WD context of EDM", Files.wdContextEntitiesEdm);
			GoogleSheetsCsvUploader.update(spreadsheetId, "WD lang tags EDM", Files.wdLangTagsEdm);
			GoogleSheetsCsvUploader.update(spreadsheetId, "WD languages EDM", Files.wdLanguageEdm);
		}
		static void updateCsvsAtGoogleForSchemaEdmConversion() throws IOException {
			GoogleSheetsCsvUploader.update(spreadsheetId, "WD CHO's EDM conversion summary", Files.wdSchemaOrgConversionToEdmAnalysisSummary);
		}

		public static void init() {
			Global.init_componentGoogleApi(credentialspath);
		}

		public static void main(String[] args) throws IOException {
			//upload pre-generated CSV
			Files.init(Files.defaultOutputFolder);
			init();
			updateCsvsAtGoogle();
		}
	}
	
	
	
	public static void main(String[] args) throws Exception {
		File outputFolder = Files.defaultOutputFolder;		
		String httpCacheFolder = "c://users/nfrei/desktop/data/HttpRepositoryWikidataStudy";
		final int SAMPLE_RECORDS;
		boolean saveRecordsToRepository=true;

		if (args.length > 0)
			outputFolder = new File(args[0]);
		if (args.length > 1)
			httpCacheFolder = args[1];
		if (args.length > 2)
			SAMPLE_RECORDS = Integer.parseInt(args[2]);
		else
			SAMPLE_RECORDS = -1;

		if (!outputFolder.exists())
			outputFolder.mkdirs();

		final HashSet<String> europeanaIdsBroken = new HashSet<String>();
		try {
			File brokenLinksReportFile = new File("src/data/wikidata/wikidata_broken_links_to_europeana.csv");
			if (brokenLinksReportFile.exists()) {
				List<String> lines = FileUtils.readLines(brokenLinksReportFile, "UTF-8");
				lines.remove(0);
				for (String l : lines) {
					String[] split = l.split(",");
					if (split[2].equals("404"))
						europeanaIdsBroken.add(split[1]);
				}
			}
		} catch (IOException e1) {
			System.err.println();
			e1.printStackTrace();
		}

		System.out.printf(
				"Settings:\n-OutpputFolder:%s\n-Cache:%s\n-Records:%d\n-Broken EuropenaIDs:%d\n------------------------\n",
				outputFolder.getPath(), httpCacheFolder, SAMPLE_RECORDS, europeanaIdsBroken.size());
		Files.init(outputFolder);
		Global.init_componentHttpRequestService();
		Global.init_componentDataRepository(httpCacheFolder);
		Global.init_enableComponentHttpRequestCache();
		GoogleSheetsUploads.init();

		Repository dataRepository = Global.getDataRepository();
		WikidataUtil.setDataRepository(dataRepository);
//		CachedHttpRequestService rdfCache = new CachedHttpRequestService();
//		rdfCache.setRequestRetryAttempts(1);

		EdmValidation validation = new EdmValidation(Files.edmValidation);
		EdmValidation validationForNonPartners = new EdmValidation(	Files.edmValidationNonPartner,
				new ValidatorForNonPartners(Global.getValidatorResourceFolder(), Schema.EDM, "edm:dataProvider",
						"edm:provider", "edm:rights and exists(edm:rights/@rdf:resource)"));
		MapOfInts<String> wikidataCollectionsSampled=new MapOfInts<String>();
		MapOfInts<String> europeanaCollectionsSampled=new MapOfInts<String>();
		final UsageProfiler chEntitiesProfile ;

		final HashMap<String, String> wikidataEuropeanaIdsMap = new HashMap<>();

		if (Files.wdEuropeanaIdMap.exists()) {
			CsvDataPersistReader reader=new CsvDataPersistReader(java.nio.file.Files.newBufferedReader(Files.wdEuropeanaIdMap.toPath(), StandardCharsets.UTF_8));
			reader.read(wikidataEuropeanaIdsMap);
		} else {
			SparqlClientWikidata.query("SELECT ?item ?europeana WHERE {" +
	//                "  ?item wdt:"+RdfRegWikidata.IIIF_MANIFEST+" ?x ." + 
					"  ?item wdt:" + RdfRegWikidata.EUROPEANAID.getLocalName() + " ?europeana .}", new Handler() {
	
						int stop = SAMPLE_RECORDS;
	
						public boolean handleSolution(QuerySolution solution)
								throws AccessException, InterruptedException, IOException {
							String europeanaId = solution.getLiteral("europeana").getString();
							if (europeanaIdsBroken.contains(europeanaId)) {
	//							System.out.println("Skipping broken "+europeanaId);
								return true;
							}
							Resource resourceResult = solution.getResource("item");
							String uri = resourceResult.getURI();
							wikidataEuropeanaIdsMap.put(uri, europeanaId);
	
							stop--;
							return stop != 0;
	//						return true;
						}
	
					});
			CsvDataPersistWriter writer=new CsvDataPersistWriter(java.nio.file.Files.newBufferedWriter(Files.wdEuropeanaIdMap.toPath(), StandardCharsets.UTF_8));
			writer.write(wikidataEuropeanaIdsMap);
		}
		System.out.println("Got " + wikidataEuropeanaIdsMap.size() + " wikidata europeana id pairs");
		
		final AtomicInteger cnt = new AtomicInteger(0);
		if (Files.wdChoProfilePersist.exists()) {
			FileInputStream fileInputStream = new FileInputStream(Files.wdChoProfilePersist);
			ObjectInputStream in=new ObjectInputStream(fileInputStream);
			chEntitiesProfile=(UsageProfiler) in.readObject();
			in.close();
			fileInputStream.close();
		} else {
			chEntitiesProfile = new UsageProfiler();
			// Profile properties
			wikidataEuropeanaIdsMap.keySet().forEach((uri) -> {
				try {
//					Resource resource = WikidataRdfUtil.fetchresource(uri, rdfCache);
//					removeOtherResources(resource.getModel(), uri);
//					removeNonTruthyStatements(resource.getModel());
//					addRdfTypesFromP31(resource.getModel());

//					Use this for the correct number of rdf:type
//					Resource resource = WikidataUtil.fetchResource(uri, false);					
					//Use this for running the experiment
					Resource resource = WikidataUtil.fetchResource(uri);	
					chEntitiesProfile.collect(resource);
					
					
	//		System.out.println(fetched.getValue());
	//		System.out.println(new String(fetched.getKey()));
	//		System.out.println("Statements for " + uri);
	//		System.out.println(RdfUtil.printStatements(rdfWikidata));
				} catch (AccessException | InterruptedException | IOException e) {
					System.err.println("Exception in " + uri);
					e.printStackTrace();
					wikidataEuropeanaIdsMap.remove(uri);
				}
				if (cnt.incrementAndGet() % 500 == 0)
					System.out.println("progress " + cnt);
			});
			chEntitiesProfile.finish();
			
			FileOutputStream fileOutputStream = new FileOutputStream(Files.wdChoProfilePersist);
			ObjectOutputStream in=new ObjectOutputStream(fileOutputStream);
			in.writeObject(chEntitiesProfile);
			in.close();
			fileOutputStream.close();
			
			
//			FileUtils.write(Files.wdChoProfile, chEntitiesProfile.getUsageStats().toCsv(),
//					"UTF-8");
//			System.exit(0);
		}
		
//		System.out.println(chEntitiesProfile);
		System.out.println("WD instances: "+chEntitiesProfile.printSummary());

		final UsageProfiler wdMetamodelProfile;
		
		
		final HashMap<String, String> wdMetamodelLabels;
		final HashMap<String, String> wdCollectionLabels= new HashMap<String, String>();
		final HashMap<String, String> europeanaCollectionLabels= new HashMap<String, String>();
//		final EquivalenceMapping wdEntPropEquivalences = new EquivalenceMapping(rdfCache, true); using foaf:page
		final EquivalenceMapping wdEntPropEquivalences;
		HashSet<String> existingEntsSet = new HashSet<String>();
		HashSet<String> existingPropsSet = new HashSet<String>();
//		UsageProfiler wdMetamodelProfile = new UsageProfiler();
//		HashMap<String, String> wdMetamodelLabels = new HashMap<String, String>();
////		final EquivalenceMapping wdEntPropEquivalences = new EquivalenceMapping(rdfCache, true); using foaf:page
//		final EquivalenceMapping wdEntPropEquivalences = new EquivalenceMapping(rdfCache, false);
//		HashSet<String> existingEntsSet = new HashSet<String>();
//		HashSet<String> existingPropsSet = new HashSet<String>();

		if(Files.wdMetamodelProfilePersist.exists()) {
			FileInputStream fileInputStream = new FileInputStream(Files.wdMetamodelProfilePersist);
			ObjectInputStream in=new ObjectInputStream(fileInputStream);
			wdMetamodelProfile=(UsageProfiler) in.readObject();
			wdMetamodelLabels=(HashMap<String, String>) in.readObject();
			wdEntPropEquivalences=(EquivalenceMapping) in.readObject();
			existingEntsSet=(HashSet<String>) in.readObject();
			existingPropsSet=(HashSet<String>) in.readObject();
			in.close();
			fileInputStream.close();
		} else {
		    wdMetamodelProfile = new UsageProfiler();
			wdMetamodelLabels = new HashMap<String, String>();
			wdEntPropEquivalences = new EquivalenceMapping(false);

			// TMP HACK
	//		wdEntPropEquivalences.putEquivalence(RdfRegWikidata.CREATIVE_WORK.getURI(), RdfReg.SCHEMAORG_CREATIVE_WORK.getURI());
	
			// harvest all wd entities and properties used, and profile them
			for (Entry<String, ClassUsageStats> entry : chEntitiesProfile.getUsageStats().getClassesStats().entrySet()) {
				String wdResourceUri = entry.getKey();
				wdResourceUri=WikidataUtil.convertWdUriToCanonical(wdResourceUri);
	
				if (!wdResourceUri.startsWith("http://www.wikidata.org/")) {
					String[] wdEqUri = new String[1];
					SparqlClientWikidata.query("SELECT ?item WHERE { ?item wdt:"
							+ RdfRegWikidata.EQUIVALENT_CLASS.getLocalName() + " <" + wdResourceUri + "> . }",
							new Handler() {
								public boolean handleUri(String uri) throws Exception {
									wdEqUri[0] = uri;
									return false;
								}
							});
					wdResourceUri = wdEqUri[0];
					if (wdResourceUri == null)
						continue;
				}
	
				try {
					existingEntsSet.add(wdResourceUri);
					Resource wdResource = WikidataUtil.fetchResource(wdResourceUri, false);
//					Resource wdResource = WikidataRdfUtil.fetchresource(wdResourceUri, rdfCache);
					String wdLabel = WikidataUtil.getLabelFor(wdResource);
					wdMetamodelLabels.put(wdResourceUri, wdLabel);
					
					
//					removeOtherResources(wdResource.getModel(), wdResourceUri);
//					removeNonTruthyStatements(wdResource.getModel());
//					addRdfTypesFromP31(wdResource.getModel());
	
	//				System.out.println( RdfUtil.printStatementsOfNamespace(wdResource, RdfReg.NsRdf) );
					wdMetamodelProfile.collect(wdResource);
//					wdMetamodelProfile.collect(wdResource.getModel());
	
	//					System.out.println(new String(fetched.getKey()));
	//					System.out.println(fetched.getValue());
	//					System.out.println("Statements for " + wdResourceUri);
	//					System.out.println(RdfUtil.printStatements(rdfWikidata));
					
					WikidataUtil.addRdfTypesFromP31(wdResource.getModel());
					wdEntPropEquivalences.analyzeEntity(wdResource, wdResource);
	
					for (String origPropUri : entry.getValue().getPropertiesProfiles().keySet()) {
						String propUri=WikidataUtil.convertWdUriToCanonical(origPropUri);
						if(existingPropsSet.contains(propUri))
							continue;
						Resource propRes = wdEntPropEquivalences.analyzeProperty(propUri, propUri);
						existingPropsSet.add(propUri);
						if(propRes!=null) {
							String labelFor = WikidataUtil.getLabelFor(propRes);
							wdMetamodelLabels.put(propUri, labelFor);
							wdMetamodelLabels.put(origPropUri, labelFor);
//							wdMetamodelLabels.put(origPropUri, getWdLabel(propRes));
							
							propRes=WikidataUtil.fetchResource(propUri, false);
							wdMetamodelProfile.collect(propRes);
						}
					}
				} catch (Exception e) {
					System.out.printf("Access to %s failed\n", wdResourceUri);
					e.printStackTrace(System.out);
				}
			}
			wdMetamodelProfile.finish();
			wdEntPropEquivalences.finish();
	
			
			
			for (Entry<String, ClassUsageStats> entry : wdMetamodelProfile.getUsageStats().getClassesStats().entrySet()) {
				String wdResourceUri = entry.getKey();
				wdResourceUri=WikidataUtil.convertWdUriToCanonical(wdResourceUri);
				
//				Resource wdResource = WikidataRdfUtil.fetchresource(wdResourceUri, rdfCache);
				Resource wdResource = WikidataUtil.fetchResource(wdResourceUri);
				
				String wdLabel = WikidataUtil.getLabelFor(wdResource);
				wdMetamodelLabels.put(wdResourceUri, wdLabel);
			
				for (String origPropUri : entry.getValue().getPropertiesProfiles().keySet()) {
					try {
						String propUri=WikidataUtil.convertWdUriToCanonical(origPropUri);
//					Resource propRes = WikidataRdfUtil.fetchresource(EquivalenceMapping.convertWdPropertyUri(propUri), rdfCache);
						Resource propRes = WikidataUtil.fetchResource(WikidataUtil.convertWdUriToCanonical(propUri));
						if(propRes!=null) {
							String labelFor = WikidataUtil.getLabelFor(propRes);
							wdMetamodelLabels.put(origPropUri, labelFor);
							wdMetamodelLabels.put(propUri, labelFor);
						}
						} catch (Exception e) {
						//ignore
					}
				}
			}

			wdEntPropEquivalences.setEquivalenceOfSuperclass(RdfRegWikidata.NsWd+"P180", Schemaorg.about.getURI());
			
			FileOutputStream fileOutputStream = new FileOutputStream(Files.wdMetamodelProfilePersist);
			ObjectOutputStream in=new ObjectOutputStream(fileOutputStream);
			in.writeObject(wdMetamodelProfile);
			in.writeObject(wdMetamodelLabels);
			in.writeObject(wdEntPropEquivalences);
			in.writeObject(existingEntsSet);
			in.writeObject(existingPropsSet);
			in.close();
			fileOutputStream.close();
		}
		System.out.println("WD metamodel: "+wdMetamodelProfile.printSummary());
		
		MapOfInts<String> nonWdPropertiesUsed=new MapOfInts<String>();
		{
			Map<String, ClassUsageStats> clsStats=chEntitiesProfile.getUsageStats().getClassesStats();
			for(String clsUri: clsStats.keySet()) {
				ClassUsageStats classUsageStats = clsStats.get(clsUri);
				for(String propUri: classUsageStats.getPropertiesStats().keySet()) {
					if(!propUri.startsWith("http://www.wikidata.org"))
						nonWdPropertiesUsed.addTo(propUri, classUsageStats.getPropertiesStats().get(propUri));
				}
			}		
		}		
		MapOfInts<String> nonWdPropertiesUsedInWdMetamodel=new MapOfInts<String>();
		{
			Map<String, ClassUsageStats> clsStats=wdMetamodelProfile.getUsageStats().getClassesStats();
			for(String clsUri: clsStats.keySet()) {
				ClassUsageStats classUsageStats = clsStats.get(clsUri);
				for(String propUri: classUsageStats.getPropertiesStats().keySet()) {
					if(!propUri.startsWith("http://www.wikidata.org"))
						nonWdPropertiesUsedInWdMetamodel.addTo(propUri, classUsageStats.getPropertiesStats().get(propUri));
				}
			}		
		}		
		FileUtils.write(Files.wdChoProfile, chEntitiesProfile.getUsageStats().toCsv(wdMetamodelLabels),
				"UTF-8");
		FileUtils.write(Files.wdChoProfileNonWdProps, "Property,count\n"+nonWdPropertiesUsed.toCsv(),
				"UTF-8");
		chEntitiesProfile.getUsageStats().exportCsvOfValueDistributions(Files.wdChoProfileValuesFolder);
		
		FileUtils.write(Files.wdMetamodelProfile, wdMetamodelProfile.getUsageStats().toCsv(wdMetamodelLabels),
				"UTF-8");
		FileUtils.write(Files.wdMetamodelProfileNonWdProps, "Property,count\n"+nonWdPropertiesUsedInWdMetamodel.toCsv(),
				"UTF-8");
		wdMetamodelProfile.getUsageStats().exportCsvOfValueDistributions(Files.wdMetamodelValuesFolder);
		
		int existingEntsEqs = 0;
		int existingEntsEqsSuper = 0;
		int existingPropsEqs = 0;
		int existingPropsEqsSuper = 0;
		
		HashSet<String> missingEqsEnts=new HashSet<String>();
		HashSet<String> missingEqsProps=new HashSet<String>();
		
		
		//for paper
		int justSeries=0;
		int justSeriesAndThing=0;
		int justThing=0;
		int superEqs=0;
		for (String entUri : existingEntsSet) {
			if (wdEntPropEquivalences.getEquivalence(entUri, false) == null &&
					wdEntPropEquivalences.getEquivalence(entUri, true)!=null) {
				ArrayList<String> eqs = wdEntPropEquivalences.getEquivalence(entUri, true);
				superEqs++;
				if(eqs.size()<=2) {
					boolean hasThing=eqs.contains(Schemaorg.Thing.getURI());
					boolean hasSeries=eqs.contains(Schemaorg.Series.getURI());
					if(hasSeries && hasThing && eqs.size()==2) 
						justSeriesAndThing++;
					else if(hasSeries && eqs.size()==1) 
						justSeries++;
					else if(hasThing && eqs.size()==1) 
						justThing++;
				}
			}
		}
		System.out.println("/// SUPER EQS CLASSES ");
		System.out.println("Existing classes "+existingEntsSet.size());
		System.out.println("Super eqs "+superEqs);
		System.out.println("series eqs "+justSeries);
		System.out.println("thing eqs "+justThing);
		System.out.println("series+thing eqs "+justSeriesAndThing);
		
		
		for (String entUri : existingEntsSet) {
			if (wdEntPropEquivalences.getEquivalence(entUri, false) != null)
				existingEntsEqs++;
			else if (wdEntPropEquivalences.getEquivalence(entUri, true) != null)
				existingEntsEqsSuper++;
			else 
				missingEqsEnts.add(entUri);
//				System.out.println("No eq for " + entUri);
		}
		for (String propUri : existingPropsSet) {
			if (wdEntPropEquivalences.getEquivalence(propUri, false) != null)
				existingPropsEqs++;
			else if (wdEntPropEquivalences.getEquivalence(propUri, true) != null)
				existingPropsEqsSuper++;
			else
				missingEqsProps.add(propUri);
//				System.out.println("No eq for " + propUri);
		}

		System.out.println("existingEntsEqs "+ existingEntsEqs+ " - existingEntsEqsSuper "+ existingEntsEqsSuper +
				" - existingPropsEqs "+existingPropsEqs + " - existingPropsEqsSuper "+existingPropsEqsSuper);

		HashSet<String> missingEqs=new HashSet<String>(missingEqsEnts);
		missingEqs.addAll(missingEqsProps);
		
		System.out.println("missing ents "+ missingEqsEnts.size() + " - missing props "+ missingEqsProps.size() +
				" - merged "+missingEqs.size() + " - by sum "+(missingEqsEnts.size()+missingEqsProps.size()));
		
		wdEntPropEquivalences.setEquivalencesNotFound(missingEqs);
		
//		write csv
//		FileUtils.write(new File(outputFolder, "wd_schemaOrg_equivalences_stats.csv"),
//				"Existing ents., " + existingEntsSet.size() + ", eqs.," + existingEntsEqs + ",Existing ent eqs. generic,"
//						+ existingEntsEqsSuper + "\n" + "Existing props," + existingPropsSet.size() + ", eqs.,"
//						+ existingPropsEqs + ",Existing props eqs. generic," + existingPropsEqsSuper + "\n"
//						+ wdEntPropEquivalences.toCsv(),
//				"UTF-8");
		FileUtils.write(Files.schemaOrgEquivalences,
				wdEntPropEquivalences.toCsv(wdMetamodelLabels), "UTF-8");

//		System.out.println(wdEntPropProfile.printShort());
		System.out.println(wdEntPropEquivalences);

		ArrayList<Triple<String, Double, Double>> completnesses = new ArrayList<>();

		// Get wikidata entities again, and convert properties to schema.org, convert to
		// EDM and store
		HashMap<String, String> unconvertableWikidataChos=new HashMap<>();
		SchemaOrgToEdmDataConverter edmConverter = new SchemaOrgToEdmDataConverter();
		cnt.set(0);
		
		UsageProfiler schemaorgProfile=new UsageProfiler();
		wikidataEuropeanaIdsMap.forEach((uri, europeanaId) -> {
			try {
				Resource wdResource = WikidataUtil.fetchResource(uri);
				Model rdfWikidata = wdResource.getModel();
				WikidataUtil.addRdfTypesFromP31(rdfWikidata);

				boolean hasWdCol=false;
				for(Statement wdCol: wdResource.listProperties(RdfRegWikidata.COLLECTION).toList()) {
					String colUri=RdfUtil.getUriOrLiteralValue(wdCol.getObject());
					if(!wikidataCollectionsSampled.containsKey(colUri));
						wdCollectionLabels.put(colUri, WikidataUtil.getLabelFor(colUri));
					wikidataCollectionsSampled.incrementTo(colUri);
					hasWdCol=true;
				} 
				if(!hasWdCol)
					wikidataCollectionsSampled.incrementTo("collection missing");
				
//					System.out.println(RdfUtil.printStatements(rdfWikidata));
//							System.out.println("--- "+uri +" ---");
//							rdfWikidata.add(Jena.createStatement(wdResource, RdfRegWikidata.INSTANCE_OF, RdfRegWikidata.CREATIVE_WORK));
				for (Statement st : rdfWikidata.listStatements().toList()) {
					if (st.getObject().isURIResource()) {
						String objUri = st.getObject().asResource().getURI();
						if (objUri.startsWith("http://www.wikidata.org/")) {
//								replace objuri by mapping, if exists	
							ArrayList<String> mappingsToSchema = wdEntPropEquivalences.getEquivalence(objUri, true);
							if (mappingsToSchema != null && !mappingsToSchema.isEmpty()) {
								for(String typeUri: mappingsToSchema) {
									Statement newSt = rdfWikidata.createStatement(st.getSubject(),
											st.getPredicate(), rdfWikidata.createResource(typeUri));
									st = newSt;
									rdfWikidata.add(st);
									break;
								}
							}
						}
					}

					String predUri = st.getPredicate().getURI().toString();
					if (predUri.startsWith("http://www.wikidata.org/")) {
//									if(!predUri.startsWith(RdfRegWikidata.NsWd)) 
//										predUri=RdfRegWikidata.NsWd+predUri.substring(predUri.lastIndexOf('/')+1);
						ArrayList<String> mappingsToSchema = wdEntPropEquivalences.getEquivalence(predUri, true);
						if (mappingsToSchema != null && !mappingsToSchema.isEmpty()) {
							for(String typeUri: mappingsToSchema) {
								Statement newSt = rdfWikidata.createStatement(st.getSubject(),
										rdfWikidata.createProperty(typeUri), st.getObject());
	//										System.out.println("replacing "+predUri+" -> "+mappingsToSchema.get(0));
								rdfWikidata.add(newSt);
							}
						}
						rdfWikidata.remove(st);
					}

				}

				schemaorgProfile.collect(wdResource.getModel());
//					System.out.println(RdfUtil.printStatementsOfNamespace(rdfWikidata, RdfReg.NsRdf));
//							System.out.println("---------------------------------------------------");

				Resource rdfWikidataEdm = edmConverter.convert(rdfWikidata.createResource(uri), null);

				if(rdfWikidataEdm==null) {
					unconvertableWikidataChos.put(uri, europeanaId);
				} else {
					String eCol=europeanaId.substring(0, europeanaId.indexOf('/'));
					europeanaCollectionsSampled.incrementTo(eCol);
					
					Model rdfEdmAtEuropeana;
					
					if(dataRepository.contains("wikidata-edm-at-europeana", uri)) {
						File file = dataRepository.getFile("wikidata-edm-at-europeana", uri);
						rdfEdmAtEuropeana = RdfUtil.readRdf(FileUtils.readFileToByteArray(file), Lang.TURTLE);
					} else { 
						rdfEdmAtEuropeana = RdfUtil.readRdfFromUri("http://data.europeana.eu/item/"+europeanaId);
						dataRepository.save("wikidata-edm-at-europeana", uri, RdfUtil.writeRdf(rdfEdmAtEuropeana, Lang.TURTLE), "Content-Type",
								Lang.TURTLE.getContentType().getContentType());
					}

					if(!europeanaCollectionLabels.containsKey(eCol))
						europeanaCollectionLabels.put(eCol, getDataProvider(rdfEdmAtEuropeana));

					if (saveRecordsToRepository) {
						//					Resource wdResourceOrig = WikidataRdfUtil.fetchresource(uri, rdfCache);
						Model rdfWikidataOrig = WikidataUtil.fetchResource(uri).getModel();
						WikidataUtil.addRdfTypesFromP31(rdfWikidataOrig);
	//					removeOtherResources(rdfWikidataOrig, uri);
	//					removeNonTruthyStatements(rdfWikidataOrig);
						dataRepository.save(DataDumps.WIKIDATA_ONTOLOGY.name(), uri, RdfUtil.writeRdf(rdfWikidataOrig, Lang.TURTLE), "Content-Type",
								Lang.TURTLE.getContentType().getContentType());
						
						dataRepository.save(DataDumps.WIKIDATA_EDM.name(), uri, RdfUtil.writeRdf(rdfWikidataEdm.getModel(), Lang.TURTLE), "Content-Type",
								Lang.TURTLE.getContentType().getContentType());
						
						dataRepository.save(DataDumps.EUROPEANA_EDM.name(), uri, RdfUtil.writeRdf(rdfEdmAtEuropeana, Lang.TURTLE), "Content-Type",
								Lang.TURTLE.getContentType().getContentType());
					}
					
					//					RDFNode euCol = EdmRdfUtil.getPropertyOfAggregation(rdfEdmAtEuropeana, RdfReg.EDM_DATASET_NAME);
//					if(euCol!=null) {
//						europeanaCollectionsSampled.incrementTo(RdfUtil.getUriOrLiteralValue(euCol));
//					} else
//						europeanaCollectionsSampled.incrementTo("collection missing");
					
					double completeness = Dqc10PointRatingCalculatorNoRights.calculate(rdfWikidataEdm.getModel());
					double completenessEuropeana = Dqc10PointRatingCalculator.calculate(rdfEdmAtEuropeana);
					completnesses.add(new ImmutableTriple<String, Double, Double>(uri, completeness, completenessEuropeana));
					validation.evaluateValidation(uri, rdfWikidataEdm);
					validationForNonPartners.evaluateValidation(uri, rdfWikidataEdm);
				}
			} catch (AccessException e) {
				System.err.println("Exception in " + e.getAddress());
				e.printStackTrace();
			} catch (Exception e) {
				System.err.println("Exception in " + uri);
				e.printStackTrace();
			}
			if (cnt.incrementAndGet() % 500 == 0)
				System.out.println("progress " + cnt);
		});

		schemaorgProfile.finish();
		FileUtils.write(Files.wdChoSchemaOrgProfile, schemaorgProfile.getUsageStats().toCsv(),
				"UTF-8");
		schemaorgProfile.getUsageStats().exportCsvOfValueDistributions(Files.wdChoSchemaOrgValuesFolder);
		ConversionSpecificationAnalyzer conv=new ConversionSpecificationAnalyzer();
		conv.process(Files.wdChoSchemaOrgProfile, Files.wdSchemaOrgConversionToEdmAnalysisReport);
		AnalizerOfSchemaToEdmConversion analizerOfSchemaToEdmConversion=new AnalizerOfSchemaToEdmConversion(conv.getRpt());
		FileUtils.write(Files.wdSchemaOrgConversionToEdmAnalysisSummary, analizerOfSchemaToEdmConversion.toCsv(), "UTF-8");
		
		validation.finalize();
		validationForNonPartners.finalize();
		
		{
			FileWriterWithEncoding fileWriter = new FileWriterWithEncoding(Files.wdUnconvertableToSchemaorg, Global.UTF8);
			CSVPrinter csvPrinter=new CSVPrinter(fileWriter, CSVFormat.DEFAULT);
			csvPrinter.printRecord("Wikidata URI","Europeana ID");			
			unconvertableWikidataChos.forEach((uri, europeanaId) -> {
				try {
					csvPrinter.printRecord(uri, europeanaId);
				} catch (IOException e) {
					e.printStackTrace();
				}			
			});
			fileWriter.close();	
			csvPrinter.close();
		}
		
		{
			FileWriterWithEncoding fileWriter = new FileWriterWithEncoding(Files.wdCompletenessEdm, Global.UTF8);
			CSVPrinter csvPrinter=new CSVPrinter(fileWriter, CSVFormat.DEFAULT);
			csvPrinter.printRecord("URI","Completeness at Wikidata","Completeness at Europeana");	
			MapOfInts<Float> completenessDistributionWd=new MapOfInts<>();
			MapOfInts<Float> completenessDistributionEuropeana=new MapOfInts<>();
			for (Triple<String, Double, Double> recComp : completnesses) {
				csvPrinter.printRecord(recComp.getLeft(), recComp.getMiddle(),
						recComp.getRight());
				BigDecimal bd = new BigDecimal(recComp.getMiddle()).setScale(2, BigDecimal.ROUND_HALF_UP);
				completenessDistributionWd.incrementTo(bd.floatValue());
				bd = new BigDecimal(recComp.getRight()).setScale(2, BigDecimal.ROUND_HALF_UP);
				completenessDistributionEuropeana.incrementTo(bd.floatValue());
			}
			csvPrinter.println();			
			csvPrinter.printRecord("COMPLETENESS SCORE DISTRIBUTION AT WIKIDATA");			
			csvPrinter.printRecord("COMPLETENESS SCORE","RECORD COUNT");			
			for(Entry<Float, Integer> msg :  completenessDistributionWd.entrySet()) {
				csvPrinter.printRecord(msg.getKey(), msg.getValue());			
			}
			csvPrinter.println();			
			csvPrinter.printRecord("COMPLETENESS SCORE DISTRIBUTION AT EUROPEANA");			
			csvPrinter.printRecord("COMPLETENESS SCORE","RECORD COUNT");			
			for(Entry<Float, Integer> msg :  completenessDistributionEuropeana.entrySet()) {
				csvPrinter.printRecord(msg.getKey(), msg.getValue());			
			}
			
			fileWriter.close();	
			csvPrinter.close();
		}
		{
			FileUtils.write(Files.wdEuropeanaCollectionCounts, 
					"Recs/collection in europeana\n"+europeanaCollectionsSampled.toCsv(europeanaCollectionLabels)+
					"\n,\nRecs/collection in wikidata\n"+wikidataCollectionsSampled.toCsv(wdCollectionLabels),
					"UTF-8");
		}
		GoogleSheetsUploads.updateCsvsAtGoogle();
	}

	private static String getDataProvider(Model rdfEdmAtEuropeana) {
		List<Resource> aggs = rdfEdmAtEuropeana.listResourcesWithProperty(Rdf.type, Ore.Aggregation).toList();
		if(aggs==null || aggs.isEmpty())
			return "no data provider";
		Resource aggRes = aggs.get(0);
		Statement prov = aggRes.getProperty(Edm.dataProvider);
		if(prov==null)
			return "no data provider";
		String provAgg=prov.getObject().asLiteral().getValue().toString();
		
		prov = aggRes.getProperty(Edm.provider);
		if(prov!=null)
			provAgg+=" -- "+prov.getObject().asLiteral().getValue().toString();
		
		return provAgg;
	}

//	private String getLabel(String uri) throws AccessException, InterruptedException, IOException {
//		uri=convertWdPropertyUri(uri);
//		Resource resource = ScriptMetadataAnalyzerOfCulturalHeritage.WikidataRdfUtil.fetchresource(uri, rdfCache);
//		StmtIterator labelProps = resource.listProperties(RdfRegRdfs.label);
//		String label=null;
//		for (Statement st : labelProps.toList()) {
//			String lang = st.getObject().asLiteral().getLanguage();
//			if(lang.equals("en"))
//				return st.getObject().asLiteral().getString();
//			if(label==null) 
//				label=st.getObject().asLiteral().getString();
//		}
//		return label;
//	}


	
}