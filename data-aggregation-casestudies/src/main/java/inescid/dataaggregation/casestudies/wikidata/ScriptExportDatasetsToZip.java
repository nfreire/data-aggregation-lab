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
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RiotException;

import inescid.dataaggregation.casestudies.wikidata.ScriptMetadataAnalyzerOfCulturalHeritage.DataDumps;
import inescid.dataaggregation.casestudies.wikidata.ScriptMetadataAnalyzerOfCulturalHeritage.Files;
import inescid.dataaggregation.casestudies.wikidata.ScriptMetadataAnalyzerOfCulturalHeritage.GoogleSheetsUploads;
import inescid.dataaggregation.casestudies.wikidata.evaluation.Dqc10PointRatingCalculatorNoRights;
import inescid.dataaggregation.casestudies.wikidata.evaluation.EdmValidation;
import inescid.dataaggregation.casestudies.wikidata.evaluation.ValidatorForNonPartners;
import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.crawl.http.HttpResponse;
import inescid.dataaggregation.data.ContentTypes;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Rdfs;
import inescid.dataaggregation.data.validation.EdmXmlValidator.Schema;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.convert.SchemaOrgToEdmDataConverter;
import inescid.dataaggregation.dataset.convert.rdfconverter.ConversionSpecificationAnalyzer;
import inescid.dataaggregation.dataset.profile.ClassUsageStats;
import inescid.dataaggregation.dataset.profile.UsageProfiler;
import inescid.dataaggregation.dataset.profile.completeness.Dqc10PointRatingCalculator;
import inescid.dataaggregation.dataset.profile.multilinguality.MultilingualSaturation;
import inescid.dataaggregation.dataset.profile.multilinguality.MultilingualSaturationResult;
import inescid.dataaggregation.store.Repository;
import inescid.europeanaapi.EuropeanaApiClient;
import inescid.util.AccessException;
import inescid.util.MapOfInts;
import inescid.util.RdfUtil;
import inescid.util.SparqlClient.Handler;
import inescid.util.StatisticCalcMean;
import inescid.util.datastruct.CsvDataPersistReader;
import inescid.util.datastruct.CsvDataPersistWriter;
import inescid.util.googlesheets.GoogleSheetsCsvUploader;

public class ScriptExportDatasetsToZip {
	
	public static void main(String[] args) throws Exception {
		File outputFolder = ScriptMetadataAnalyzerOfCulturalHeritage.Files.defaultOutputFolder;		
		String httpCacheFolder = "c://users/nfrei/desktop/data/HttpRepository-wikidata-chos";
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
//		try {
//			File brokenLinksReportFile = new File("src/data/wikidata/wikidata_broken_links_to_europeana.csv");
//			if (brokenLinksReportFile.exists()) {
//				List<String> lines = FileUtils.readLines(brokenLinksReportFile, "UTF-8");
//				lines.remove(0);
//				for (String l : lines) {
//					String[] split = l.split(",");
//					if (split[2].equals("404"))
//						europeanaIdsBroken.add(split[1]);
//				}
//			}
//		} catch (IOException e1) {
//			System.err.println();
//			e1.printStackTrace();
//		}

		System.out.printf(
				"Settings:\n-OutpputFolder:%s\n-Cache:%s\n-Records:%d\n-Broken EuropenaIDs:%d\n------------------------\n",
				outputFolder.getPath(), httpCacheFolder, SAMPLE_RECORDS, europeanaIdsBroken.size());
		ScriptMetadataAnalyzerOfCulturalHeritage.Files.init(outputFolder);
		Global.init_componentDataRepository(httpCacheFolder);

		Repository dataRepository = Global.getDataRepository();

		dataRepository.exportDatasetToZip(DataDumps.WIKIDATA_ONTOLOGY.name(), Files.wdDatasetOntologyZip, ContentTypes.TURTLE);
		dataRepository.exportDatasetToZip(DataDumps.WIKIDATA_EDM.name(), Files.wdDatasetEdmZip, ContentTypes.TURTLE);
		dataRepository.exportDatasetToZip(DataDumps.EUROPEANA_EDM.name(),Files.europeanaDatasetEdmZip, ContentTypes.TURTLE);

		
	}

	
	
}