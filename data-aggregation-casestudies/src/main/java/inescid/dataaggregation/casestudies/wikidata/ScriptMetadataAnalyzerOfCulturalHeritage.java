package inescid.dataaggregation.casestudies.wikidata;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
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

import inescid.dataaggregation.casestudies.wikidata.evaluation.Dqc10PointRatingCalculatorNoRights;
import inescid.dataaggregation.casestudies.wikidata.evaluation.EdmValidation;
import inescid.dataaggregation.casestudies.wikidata.evaluation.ValidatorForNonPartners;
import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.crawl.http.HttpResponse;
import inescid.dataaggregation.data.ContentTypes;
import inescid.dataaggregation.data.RegRdf;
import inescid.dataaggregation.data.RegRdfs;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.convert.SchemaOrgToEdmDataConverter;
import inescid.dataaggregation.dataset.convert.rdfconverter.ConversionSpecificationAnalyzer;
import inescid.dataaggregation.dataset.profile.ClassUsageStats;
import inescid.dataaggregation.dataset.profile.UsageProfiler;
import inescid.dataaggregation.dataset.profile.completeness.Dqc10PointRatingCalculator;
import inescid.dataaggregation.dataset.validate.Validator.Schema;
import inescid.dataaggregation.store.Repository;
import inescid.europeanaapi.EuropeanaApiClient;
import inescid.util.AccessException;
import inescid.util.MapOfInts;
import inescid.util.RdfUtil;
import inescid.util.SparqlClient.Handler;
import inescid.util.googlesheets.GoogleSheetsCsvUploader;

public class ScriptMetadataAnalyzerOfCulturalHeritage {
	enum DataDumps {
		WIKIDATA_EDM, EUROPEANA_EDM, WIKIDATA_ONTOLOGY, WIKIDATA_SCHEMAORG
	};

	public static class Files {
		static File defaultOutputFolder=new File("c://users/nfrei/desktop/data/wikidata-results-latest");
		
		public static File edmValidation;
		public static File edmValidationNonPartner;
		public static File wdChoProfile;
		public static File wdChoSchemaOrgProfile;
		public static File wdChoSchemaOrgValuesFolder;
		public static File wdSchemaOrgConversionToEdmAnalysisReport;
		public static File wdChoProfileValuesFolder;
		public static File wdMetamodelProfile;
		public static File wdMetamodelValuesFolder;
		public static File schemaOrgEquivalences;
		public static File wdDatasetOntologyZip;
		public static File wdDatasetEdmZip;
		public static File europeanaDatasetEdmZip;
		public static File wdUnconvertableToSchemaorg;
		public static File wdCompletenessEdm;
		public static File wdEuropeanaCollectionCounts;
		
		public static void init(File outputFolder) {
			wdChoProfile=new File(outputFolder, "wd_ch_profile.csv");
			wdChoProfileValuesFolder=new File(outputFolder, "wd_ch_profile-property_values_distribution");
			wdChoSchemaOrgProfile=new File(outputFolder, "wd_ch_schemaorg_profile.csv");
			wdChoSchemaOrgValuesFolder=new File(outputFolder, "wd_ch_schemaorg_profile_property_values_distribution");
			wdSchemaOrgConversionToEdmAnalysisReport=new File(outputFolder, "wd_ch_schemaorg_conversion_edm_anailsys_report.csv");
			edmValidation= new File(outputFolder, "edm-validation.csv");
			edmValidationNonPartner=new File(outputFolder, "edm-validation-nonpartner.csv");
			wdMetamodelProfile=new File(outputFolder, "wd_metamodel_profile.csv");
			wdMetamodelValuesFolder=new File(outputFolder, "wd_metamodel_profile-property_values_distribution");
			schemaOrgEquivalences=new File(outputFolder, "wd_schemaOrg_equivalences.csv");
			wdDatasetOntologyZip=new File(outputFolder, "wikidata-subdataset-ontology.zip");
			wdDatasetEdmZip=new File(outputFolder, "wikidata-subdataset-edm.zip");
			europeanaDatasetEdmZip=new File(outputFolder, "europeana-subdataset-edm.zip");
			wdUnconvertableToSchemaorg=new File(outputFolder, "wikidata-unconvertable-to-schemaorg.csv");
			wdCompletenessEdm=new File(outputFolder, "wikidata-completeness-edm.csv");
			wdEuropeanaCollectionCounts=new File(outputFolder, "collections_wd_and_europeana_counts.csv");
		}
		
	}
	
	private static class GoogleSheetsUploads {
		static String credentialspath="C:\\Users\\nfrei\\.credentials\\Data Aggregation Lab-b1ec5c3705fc.json";
		static String spreadsheetId="1i2cK_QwUPNPxdU2qQXRnhY-nry_HP3OKUHJS8hApoRI";
		
		static void updateCsvsAtGoogle() throws IOException {
			GoogleSheetsCsvUploader.update(spreadsheetId, "WD-Europeana collections", Files.wdEuropeanaCollectionCounts);
			GoogleSheetsCsvUploader.update(spreadsheetId, "WD CHO's RDF profile", Files.wdChoProfile);
			GoogleSheetsCsvUploader.update(spreadsheetId, "WD Metamodel's RDF profile", Files.wdMetamodelProfile);
			GoogleSheetsCsvUploader.update(spreadsheetId, "WD-Schema equivalences", Files.schemaOrgEquivalences);
			GoogleSheetsCsvUploader.update(spreadsheetId, "WD CHO's Schema.org RDF profile", Files.wdChoSchemaOrgProfile);
			GoogleSheetsCsvUploader.update(spreadsheetId, "WD CHO's EDM conversion analysis", Files.wdSchemaOrgConversionToEdmAnalysisReport);
			GoogleSheetsCsvUploader.update(spreadsheetId, "WD CHOs unconvertable to Schema", Files.wdUnconvertableToSchemaorg);
			GoogleSheetsCsvUploader.update(spreadsheetId, "WD validation of EDM", Files.edmValidationNonPartner);
			GoogleSheetsCsvUploader.update(spreadsheetId, "WD compleness of EDM", Files.wdCompletenessEdm);
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
		String httpCacheFolder = "c://users/nfrei/desktop/data/HttpRepository";
		final int SAMPLE_RECORDS;

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
		GoogleSheetsUploads.init();

		Repository dataRepository = Global.getDataRepository();

		CachedHttpRequestService rdfCache = new CachedHttpRequestService();
		rdfCache.setRequestRetryAttempts(1);

		EdmValidation validation = new EdmValidation(Files.edmValidation);
		EdmValidation validationForNonPartners = new EdmValidation(	Files.edmValidationNonPartner,
				new ValidatorForNonPartners(Global.getValidatorResourceFolder(), Schema.EDM, "edm:dataProvider",
						"edm:provider", "edm:rights and exists(edm:rights/@rdf:resource)"));
		MapOfInts<String> wikidataCollectionsSampled=new MapOfInts<String>();
		MapOfInts<String> europeanaCollectionsSampled=new MapOfInts<String>();
		UsageProfiler chEntitiesProfile = new UsageProfiler();
		

		final HashMap<String, String> wikidataEuropeanaIdsMap = new HashMap<>();

		SparqlClientWikidata.query("SELECT ?item ?europeana WHERE {" +
//                "  ?item wdt:"+RdfRegWikidata.IIIF_MANIFEST+" ?x ." + 
				"  ?item wdt:" + RdfRegWikidata.EUROPEANAID.getLocalName() + " ?europeana .", new Handler() {

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
		System.out.println("Got " + wikidataEuropeanaIdsMap.size() + " wikidata europeana id pairs");

		// Profile properties
		final AtomicInteger cnt = new AtomicInteger(0);
		wikidataEuropeanaIdsMap.keySet().forEach((uri) -> {
			try {
				Resource resource = WikidataRdfUtil.fetchresource(uri, rdfCache);

				removeOtherResources(resource.getModel(), uri);
				removeNonTruthyStatements(resource.getModel());
				addRdfTypesFromP31(resource.getModel());

				chEntitiesProfile.collect(resource.getModel(), uri);
//		System.out.println(fetched.getValue());
//		System.out.println(new String(fetched.getKey()));
//		System.out.println("Statements for " + uri);
//		System.out.println(RdfUtil.printStatements(rdfWikidata));
				
				Statement wdCol = resource.getProperty(RdfRegWikidata.COLLECTION);
				if(wdCol!=null) {
					wikidataCollectionsSampled.incrementTo(RdfUtil.getUriOrLiteralValue(wdCol.getObject()));
				} else
					wikidataCollectionsSampled.incrementTo("collection missing");
				
			} catch (AccessException | InterruptedException | IOException e) {
				System.err.println("Exception in " + uri);
				e.printStackTrace();
				wikidataEuropeanaIdsMap.remove(uri);
			}
			if (cnt.incrementAndGet() % 500 == 0)
				System.out.println("progress " + cnt);
		});
		chEntitiesProfile.finish();


//		System.out.println(chEntitiesProfile);
		System.out.println(chEntitiesProfile.printSummary());

		UsageProfiler wdMetamodelProfile = new UsageProfiler();
		HashMap<String, String> wdMetamodelLabels = new HashMap<String, String>();
//		final EquivalenceMapping wdEntPropEquivalences = new EquivalenceMapping(rdfCache, true); using foaf:page
		final EquivalenceMapping wdEntPropEquivalences = new EquivalenceMapping(rdfCache, false);
		HashSet<String> existingEntsSet = new HashSet<String>();
		HashSet<String> existingPropsSet = new HashSet<String>();

		// TMP HACK
//		wdEntPropEquivalences.putEquivalence(RdfRegWikidata.CREATIVE_WORK.getURI(), RdfReg.SCHEMAORG_CREATIVE_WORK.getURI());

		// harvest all wd entities and properties used, and profile them
		for (Entry<String, ClassUsageStats> entry : chEntitiesProfile.getUsageStats().getClassesStats().entrySet()) {
			String wdResourceUri = entry.getKey();

			if (!wdResourceUri.startsWith("http://www.wikidata.org/")) {
				String[] wdEqUri = new String[1];
				SparqlClientWikidata.query("SELECT ?item WHERE { ?item wdt:"
						+ RdfRegWikidata.EQUIVALENT_CLASS.getLocalName() + " <" + wdResourceUri + "> .",
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
				Resource wdResource = WikidataRdfUtil.fetchresource(wdResourceUri, rdfCache);
				String wdLabel = getWdLabel(wdResource);
				wdMetamodelLabels.put(wdResourceUri, wdLabel);
				removeOtherResources(wdResource.getModel(), wdResourceUri);
				removeNonTruthyStatements(wdResource.getModel());
				addRdfTypesFromP31(wdResource.getModel());

//				System.out.println( RdfUtil.printStatementsOfNamespace(wdResource, RdfReg.NsRdf) );
				wdMetamodelProfile.collect(wdResource.getModel());

//					System.out.println(new String(fetched.getKey()));
//					System.out.println(fetched.getValue());
//					System.out.println("Statements for " + wdResourceUri);
//					System.out.println(RdfUtil.printStatements(rdfWikidata));
				wdEntPropEquivalences.analyzeEntity(wdResource, wdResource);

				for (String propUri : entry.getValue().getPropertiesProfiles().keySet()) {
					Resource propRes = wdEntPropEquivalences.analyzeProperty(propUri, propUri);
					existingPropsSet.add(propUri);
					if(propRes!=null)
						wdMetamodelLabels.put(propUri, getWdLabel(propRes));
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
			Resource wdResource = WikidataRdfUtil.fetchresource(wdResourceUri, rdfCache);
			String wdLabel = getWdLabel(wdResource);
			wdMetamodelLabels.put(wdResourceUri, wdLabel);
		
			for (String propUri : entry.getValue().getPropertiesProfiles().keySet()) {
				Resource propRes = WikidataRdfUtil.fetchresource(EquivalenceMapping.convertWdPropertyUri(propUri), rdfCache);
				if(propRes!=null)
					wdMetamodelLabels.put(propUri, getWdLabel(propRes));
			}
		}
		
		FileUtils.write(Files.wdChoProfile, chEntitiesProfile.getUsageStats().toCsv(wdMetamodelLabels),
				"UTF-8");
		chEntitiesProfile.getUsageStats().exportCsvOfValueDistributions(Files.wdChoProfileValuesFolder);
		
		FileUtils.write(Files.wdMetamodelProfile, wdMetamodelProfile.getUsageStats().toCsv(wdMetamodelLabels),
				"UTF-8");
		wdMetamodelProfile.getUsageStats().exportCsvOfValueDistributions(Files.wdMetamodelValuesFolder);
		
		int existingEntsEqs = 0;
		int existingEntsEqsSuper = 0;
		int existingPropsEqs = 0;
		int existingPropsEqsSuper = 0;
		
		HashSet<String> missingEqsEnts=new HashSet<String>();
		HashSet<String> missingEqsProps=new HashSet<String>();
		
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
		System.out.println();

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
				Resource wdResource = WikidataRdfUtil.fetchresource(uri, rdfCache);
				Model rdfWikidata = wdResource.getModel();
				removeOtherResources(rdfWikidata, uri);
				removeNonTruthyStatements(rdfWikidata);
				addRdfTypesFromP31(rdfWikidata);
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
					
					EuropeanaApiClient europeanaApiClient = new EuropeanaApiClient("pSZnyqunm");
					
					Model rdfEdmAtEuropeana;
					try {
						if(dataRepository.contains("wikidata-edm-at-europeana", uri)) {
							File file = dataRepository.getFile("wikidata-edm-at-europeana", uri);
							rdfEdmAtEuropeana = RdfUtil.readRdf(FileUtils.readFileToByteArray(file), Lang.TURTLE);
						} else 
							rdfEdmAtEuropeana = europeanaApiClient.getRecord(europeanaId);

						Resource wdResourceOrig = WikidataRdfUtil.fetchresource(uri, rdfCache);
						Model rdfWikidataOrig = wdResource.getModel();
						removeOtherResources(rdfWikidataOrig, uri);
						removeNonTruthyStatements(rdfWikidataOrig);
						dataRepository.save(DataDumps.WIKIDATA_ONTOLOGY.name(), uri, RdfUtil.writeRdf(rdfWikidataOrig, Lang.TURTLE), "Content-Type",
								Lang.TURTLE.getContentType().getContentType());
						
						dataRepository.save(DataDumps.WIKIDATA_EDM.name(), uri, RdfUtil.writeRdf(rdfWikidataEdm.getModel(), Lang.TURTLE), "Content-Type",
								Lang.TURTLE.getContentType().getContentType());
						
						dataRepository.save(DataDumps.EUROPEANA_EDM.name(), uri, RdfUtil.writeRdf(rdfEdmAtEuropeana, Lang.TURTLE), "Content-Type",
								Lang.TURTLE.getContentType().getContentType());
								
					} catch (inescid.europeanaapi.AccessException e) {
						throw new AccessException(e.getAddress(), e);
					}


					
//					RDFNode euCol = EdmRdfUtil.getPropertyOfAggregation(rdfEdmAtEuropeana, RdfReg.EDM_DATASET_NAME);
//					if(euCol!=null) {
//						europeanaCollectionsSampled.incrementTo(RdfUtil.getUriOrLiteralValue(euCol));
//					} else
//						europeanaCollectionsSampled.incrementTo("collection missing");
					
					
					double completeness = Dqc10PointRatingCalculatorNoRights.calculate(rdfWikidataEdm.getModel());
					double completenessEuropeana = Dqc10PointRatingCalculator.calculate(rdfEdmAtEuropeana);
					completnesses.add(new ImmutableTriple(uri, completeness, completenessEuropeana));
					validation.evaluateValidation(uri, rdfWikidataEdm);
					validationForNonPartners.evaluateValidation(uri, rdfWikidataEdm);
				}
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
		
		validation.finalize();
		validationForNonPartners.finalize();
		
		dataRepository.exportDatasetToZip(DataDumps.WIKIDATA_ONTOLOGY.name(), Files.wdDatasetOntologyZip, ContentTypes.TURTLE);
		dataRepository.exportDatasetToZip(DataDumps.WIKIDATA_EDM.name(), Files.wdDatasetEdmZip, ContentTypes.TURTLE);
		dataRepository.exportDatasetToZip(DataDumps.EUROPEANA_EDM.name(),Files.europeanaDatasetEdmZip, ContentTypes.TURTLE);
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
			csvPrinter.printRecord("Completeness at Wikidata","Completeness at Europeana");	
			MapOfInts<Float> completenessDistributionWd=new MapOfInts<>();
			MapOfInts<Float> completenessDistributionEuropeana=new MapOfInts<>();
			for (Triple<String, Double, Double> recComp : completnesses) {
				csvPrinter.printRecord(recComp.getMiddle(),
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
					"Recs/collection in europeana\n"+europeanaCollectionsSampled.toCsv()+
					"\nRecs/collection in wikidata\n"+wikidataCollectionsSampled.toCsv(),
					"UTF-8");
		}
		GoogleSheetsUploads.updateCsvsAtGoogle();
	}

	private static String getWdLabel(Resource wdResource) {
		StmtIterator labelProps = wdResource.listProperties(RegRdfs.label);
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


	private static void removeOtherResources(Model rdfWikidata, String keepUri) {
		Resource keep = rdfWikidata.createResource(keepUri);
		for (StmtIterator stmts = rdfWikidata.listStatements(); stmts.hasNext();) {
			Statement stm = stmts.next();
			if (!(stm.getSubject().equals(keep) || stm.getObject().equals(keep)))
				stmts.remove();
		}
	}

	private static void removeNonTruthyStatements(Model rdfWikidata) {
		for (StmtIterator stmts = rdfWikidata.listStatements(); stmts.hasNext();) {
			Statement stm = stmts.next();
			if (stm.getPredicate().getNameSpace().startsWith("http://www.wikidata.org/")
					&& !(stm.getPredicate().getNameSpace().startsWith(RdfRegWikidata.NsWd)
							|| stm.getPredicate().getNameSpace().startsWith(RdfRegWikidata.NsWdt)
							|| stm.getPredicate().getNameSpace().startsWith(RdfRegWikidata.NsWdtn)))
				stmts.remove();
		}
	}

	private static void addRdfTypesFromP31(Model rdfWikidata) {
		for (StmtIterator stmts = rdfWikidata.listStatements(null, RdfRegWikidata.INSTANCE_OF, (RDFNode) null); stmts
				.hasNext();) {
			Statement stm = stmts.next();
			rdfWikidata.add(rdfWikidata.createStatement(stm.getSubject(), RegRdf.type, stm.getObject()));
		}
	}

}