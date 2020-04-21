package inescid.dataaggregation.casestudies.wikidata.edm;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;

import inescid.dataaggregation.casestudies.wikidata.evaluation.Dqc10PointRatingCalculatorNoRights;
import inescid.dataaggregation.casestudies.wikidata.evaluation.EdmValidation;
import inescid.dataaggregation.casestudies.wikidata.evaluation.ValidatorForNonPartners;
import inescid.dataaggregation.casestudies.wikidata.WikidataUtil;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.profile.completeness.Dqc10PointRatingCalculator;
import inescid.dataaggregation.dataset.profile.multilinguality.MultilingualSaturation;
import inescid.dataaggregation.dataset.profile.multilinguality.MultilingualSaturationResult;
import inescid.dataaggregation.dataset.validate.Validator.Schema;
import inescid.dataaggregation.store.Repository;
import inescid.util.AccessException;
import inescid.util.MapOfInts;
import inescid.util.RdfUtil;
import inescid.util.datastruct.CsvDataPersistReader;
import inescid.util.googlesheets.GoogleSheetsCsvUploader;

public class ScriptEvaluateWikidataEdmConverter {
	
	public static class Const {
		public static File fileWdEuropeanaIdMap;
		public static File folderWdTripleStore;
		public static File wdCompletenessEdm;
		public static File edmValidation;
		public static File edmValidationNonPartner;
		public static File unmappedReport;
		public static String repoDatasetEdmEuropeana="wikidata-edm-at-europeana";
		public static String repoDatasetWikidata="wikidata-resource";
		
		public static void init(File outputFolder) {
			fileWdEuropeanaIdMap=new File(outputFolder, "wd_to_europeana_id_map.csv");
			folderWdTripleStore=new File(outputFolder, "triplestore-wikidata");
			wdCompletenessEdm=new File(outputFolder, "wikidata-completeness-edm.csv");
			edmValidation= new File(outputFolder, "edm-validation.csv");
			edmValidationNonPartner=new File(outputFolder, "edm-validation-nonpartner.csv");
			unmappedReport=new File(outputFolder, "unmapped-report.csv");
		}
	}

		protected static class GoogleSheetsUploads {
			static String credentialspath="C:\\Users\\nfrei\\.credentials\\Data Aggregation Lab-b1ec5c3705fc.json";
			static String spreadsheetId="1mF7Dgb5kDVPrq453hUgnpyYrAX6QVSs95Zs36Qw4b0Y";
			
			static void updateCsvsAtGoogle() throws IOException {
				System.out.println("Uploading to Google spreadsheet at:\nhttps://docs.google.com/spreadsheets/d/"+spreadsheetId);
				GoogleSheetsCsvUploader.update(spreadsheetId, "WD-Europeana - Completeness", Const.wdCompletenessEdm);
				GoogleSheetsCsvUploader.update(spreadsheetId, "WD - Validation", Const.edmValidation);
				GoogleSheetsCsvUploader.update(spreadsheetId, "WD - Validation Ext.", Const.edmValidationNonPartner);
				GoogleSheetsCsvUploader.update(spreadsheetId, "WD - Unmappable", Const.unmappedReport);
			}

			public static void init() {
				Global.init_componentGoogleApi(credentialspath);
			}
		}
		
	public static void main(String[] args) throws Exception {
		File outputFolder = new File("c://users/nfrei/desktop/data/wikidata-to-edm");		
		String httpCacheFolder = "c://users/nfrei/desktop/data/HttpRepositoryWikidataStudy";
		final int SAMPLE_RECORDS;

		if (args.length > 0)
			httpCacheFolder = args[0];
		if (args.length > 1)
			SAMPLE_RECORDS = Integer.parseInt(args[1]);
		else
			SAMPLE_RECORDS = -10;
//		SAMPLE_RECORDS = 10;

		System.out.printf(
				"Settings:\n-Cache:%s\n-Records:%d\n------------------------\n",
				httpCacheFolder, SAMPLE_RECORDS);
		Const.init(outputFolder);
		Global.init_componentHttpRequestService();
		Global.init_componentDataRepository(httpCacheFolder);
		Global.init_enableComponentHttpRequestCache();
		Repository dataRepository = Global.getDataRepository();
		WikidataUtil.setDataRepository(dataRepository);
		GoogleSheetsUploads.init();
		
		final HashMap<String, String> wikidataEuropeanaIdsMap = new HashMap<>();
		CsvDataPersistReader reader=new CsvDataPersistReader(java.nio.file.Files.newBufferedReader(
				Const.fileWdEuropeanaIdMap.toPath()
				, StandardCharsets.UTF_8));
		reader.read(wikidataEuropeanaIdsMap);
		
		WikidataEdmConverter conv=new WikidataEdmConverter(
				new File("src/data/wikidata/wikidata_edm_mappings_classes.csv"), 
				new File("src/data/wikidata/wikidata_edm_mappings.csv"),
				new File("src/data/wikidata/wikidata_edm_mappings_hierarchy.csv"),
				Const.folderWdTripleStore,
				new File("../data-aggregation-lab-core/src/main/resources/owl/edm.owl")
				);
		conv.enableUnmappedLogging();
		
		ArrayList<Triple<String, Double, Double>> completnesses = new ArrayList<>();
		ArrayList<Triple<String, MultilingualSaturationResult, MultilingualSaturationResult>> langSaturation = new ArrayList<>();
		EdmValidation validation = new EdmValidation(Const.edmValidation);
		EdmValidation validationForNonPartners = new EdmValidation(	Const.edmValidationNonPartner,
				new ValidatorForNonPartners(Global.getValidatorResourceFolder(), Schema.EDM, "edm:dataProvider",
						"edm:provider", "edm:rights and exists(edm:rights/@rdf:resource)"));
		
		int[] cnt=new int[] {0};
		wikidataEuropeanaIdsMap.forEach((wdResourceUri, europeanaId) -> {
			
			if(SAMPLE_RECORDS>0 && cnt[0]>SAMPLE_RECORDS) return;
			try {
				Resource wdResource = WikidataUtil.fetchResource(wdResourceUri);
//				System.out.println(wdResource);
				conv.reset();
				Resource edmWd = conv.convert(wdResource);
								
				Model rdfEdmAtEuropeana;
				
				if(dataRepository.contains(Const.repoDatasetEdmEuropeana, wdResourceUri)) {
					File file = dataRepository.getFile(Const.repoDatasetEdmEuropeana, wdResourceUri);
					rdfEdmAtEuropeana = RdfUtil.readRdf(FileUtils.readFileToByteArray(file), Lang.TURTLE);
				} else { 
					rdfEdmAtEuropeana = RdfUtil.readRdfFromUri("http://data.europeana.eu/item/"+europeanaId);
					dataRepository.save(Const.repoDatasetEdmEuropeana, wdResourceUri, RdfUtil.writeRdf(rdfEdmAtEuropeana, Lang.TURTLE), "Content-Type",
							Lang.TURTLE.getContentType().getContentType());
				}				
				
				double completeness = Dqc10PointRatingCalculatorNoRights.calculate(edmWd.getModel());
				double completenessEuropeana = Dqc10PointRatingCalculator.calculate(rdfEdmAtEuropeana);
				completnesses.add(new ImmutableTriple<String, Double, Double>(wdResourceUri, completeness, completenessEuropeana));
				validation.evaluateValidation(wdResourceUri, edmWd);
				validationForNonPartners.evaluateValidation(wdResourceUri, edmWd);
				
				MultilingualSaturationResult wdLangSat=MultilingualSaturation.calculate(edmWd.getModel());
				MultilingualSaturationResult europeanaLangSat=MultilingualSaturation.calculate(rdfEdmAtEuropeana);
				langSaturation.add(new ImmutableTriple<>(wdResourceUri, wdLangSat, europeanaLangSat));
				
				if(SAMPLE_RECORDS>0) 
					FileUtils.write(new File(outputFolder, URLEncoder.encode(wdResourceUri, StandardCharsets.UTF_8.toString())+".ttl"),  
							RdfUtil.writeRdfToString(edmWd.getModel(), Lang.TURTLE), StandardCharsets.UTF_8);
				cnt[0]++;
			} catch (AccessException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
			
		
		{
			FileWriterWithEncoding fileWriter = new FileWriterWithEncoding(Const.wdCompletenessEdm, Global.UTF8);
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
			FileWriterWithEncoding fileWriter = new FileWriterWithEncoding(Const.wdLanguageSaturationEdm, Global.UTF8);
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
		
		
		validation.finalize();
		validationForNonPartners.finalize();
		
		
		
		FileUtils.write(Const.unmappedReport, conv.unmappedToCsv(), StandardCharsets.UTF_8);
		
		GoogleSheetsUploads.updateCsvsAtGoogle();
	}
	
	
}

