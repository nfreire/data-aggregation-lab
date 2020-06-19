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

import inescid.dataaggregation.casestudies.wikidata.ScriptMetadataAnalyzerOfCulturalHeritage.GoogleSheetsUploads;
import inescid.dataaggregation.casestudies.wikidata.evaluation.Dqc10PointRatingCalculatorNoRights;
import inescid.dataaggregation.casestudies.wikidata.evaluation.EdmValidation;
import inescid.dataaggregation.casestudies.wikidata.evaluation.ValidatorForNonPartners;
import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.crawl.http.HttpResponse;
import inescid.dataaggregation.data.ContentTypes;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Dc;
import inescid.dataaggregation.data.model.DcTerms;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;
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

public class ScriptCompareResultingEdm {
	
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
		ScriptMetadataAnalyzerOfCulturalHeritage.Files.init(outputFolder);
		Global.init_componentDataRepository(httpCacheFolder);

		Repository dataRepository = Global.getDataRepository();

		CachedHttpRequestService rdfCache = new CachedHttpRequestService();
		rdfCache.setRequestRetryAttempts(1);

		final HashMap<String, String> wikidataEuropeanaIdsMap = new HashMap<>();

		CsvDataPersistReader reader=new CsvDataPersistReader(java.nio.file.Files.newBufferedReader(ScriptMetadataAnalyzerOfCulturalHeritage.Files.wdEuropeanaIdMap.toPath(), StandardCharsets.UTF_8));
		reader.read(wikidataEuropeanaIdsMap);
		System.out.println("Got " + wikidataEuropeanaIdsMap.size() + " wikidata europeana id pairs");

		ArrayList<Triple<String, Integer, Integer>> linkedEntities = new ArrayList<>();
		ArrayList<Triple<String, Integer, Integer>> langTags = new ArrayList<>();
		ArrayList<Triple<String, Integer, Integer>> langs = new ArrayList<>();
		wikidataEuropeanaIdsMap.forEach((uri, europeanaId) -> {
			try {
				if(!dataRepository.contains(ScriptMetadataAnalyzerOfCulturalHeritage.DataDumps.WIKIDATA_EDM.name(), uri))
					return;
				byte[] content = dataRepository.getContent(ScriptMetadataAnalyzerOfCulturalHeritage.DataDumps.WIKIDATA_EDM.name(), uri);
				Model wdEdmModel = RdfUtil.readRdf(content, Lang.TURTLE);
				content = dataRepository.getContent(ScriptMetadataAnalyzerOfCulturalHeritage.DataDumps.EUROPEANA_EDM.name(), uri);
				Model europeanaModel = RdfUtil.readRdf(content, Lang.TURTLE);
				
				int linkedEntitiesWd = countContextEntitiesWd(uri, wdEdmModel);
				int linkedEntitiesEuropeana = countContextEntitiesEuropeana(europeanaModel);
				linkedEntities.add(new ImmutableTriple<String, Integer, Integer>(uri, linkedEntitiesWd, linkedEntitiesEuropeana));

				MultilingualSaturationResult langWd = getMultilingualSaturation(wdEdmModel).getChoResult();
				MultilingualSaturationResult langEuropeana = getMultilingualSaturation(europeanaModel).getChoResult();

				langTags.add(new ImmutableTriple<String, Integer, Integer>(uri, langWd.getLangTagCount(), langEuropeana.getLangTagCount()));
				langs.add(new ImmutableTriple<String, Integer, Integer>(uri, langWd.getLanguagesCount(), langEuropeana.getLanguagesCount()));
			} catch (RiotException | IOException e) {
				System.err.println("ERROR in URI: "+uri);
				e.printStackTrace();
			}
		});
		
		
		for(Object[] wrt: new Object[][] {
			{ScriptMetadataAnalyzerOfCulturalHeritage.Files.wdContextEntitiesEdm, "Entities", linkedEntities},
			{ScriptMetadataAnalyzerOfCulturalHeritage.Files.wdLangTagsEdm , "Lang tags", langTags},
			{ScriptMetadataAnalyzerOfCulturalHeritage.Files.wdLanguageEdm, "Languages", langs}
		}) {
			FileWriterWithEncoding fileWriter = new FileWriterWithEncoding((File)wrt[0], Global.UTF8);
			CSVPrinter csvPrinter=new CSVPrinter(fileWriter, CSVFormat.DEFAULT);
			csvPrinter.printRecord("URI",wrt[1]+" at Wikidata",wrt[1]+" at Europeana");	
			MapOfInts<Integer> distributionWd=new MapOfInts<>();
			MapOfInts<Integer> distributionEuropeana=new MapOfInts<>();
			for (Triple<String, Integer, Integer> recComp : (ArrayList<Triple<String, Integer, Integer>>)wrt[2]) {
				csvPrinter.printRecord(recComp.getLeft(), recComp.getMiddle(),
						recComp.getRight());
				distributionWd.incrementTo(recComp.getMiddle());
				distributionEuropeana.incrementTo(recComp.getRight());
			}
			csvPrinter.println();			
			csvPrinter.println();			
			csvPrinter.printRecord(wrt[1]+" DISTRIBUTION AT WIKIDATA");			
			csvPrinter.printRecord(wrt[1],"RECORD COUNT");			
			for(Entry<Integer, Integer> msg :  distributionWd.entrySet()) {
				csvPrinter.printRecord(msg.getKey(), msg.getValue());			
			}
			StatisticCalcMean statistics = distributionWd.getStatistics();
			csvPrinter.printRecord("Median:", statistics.getMean(), "Std. Dev.:", statistics.getStandardDeviation());			
			csvPrinter.println();			
			csvPrinter.println();			

			csvPrinter.printRecord(wrt[1]+" DISTRIBUTION AT EUROPEANA");			
			csvPrinter.printRecord(wrt[1],"RECORD COUNT");			
			for(Entry<Integer, Integer> msg :  distributionEuropeana.entrySet()) {
				csvPrinter.printRecord(msg.getKey(), msg.getValue());			
			}
			statistics = distributionEuropeana.getStatistics();
			csvPrinter.printRecord("Median:", statistics.getMean(), "Std. Dev.:", statistics.getStandardDeviation());			
			
			fileWriter.close();	
			csvPrinter.close();
		}

		GoogleSheetsUploads.init();
		GoogleSheetsUploads.updateCsvsAtGoogleForFinalEdm();
		
	}

	
	private static final Property[] entitiesProperties=new Property[] {
			Dc.contributor, Dc.creator, Dc.coverage, Dc.subject,
			DcTerms.contributor, DcTerms.creator, DcTerms.coverage, DcTerms.subject,
			DcTerms.provenance, DcTerms.publisher, DcTerms.spatial, DcTerms.temporal,
			Edm.currentLocation
	};
	
	private static int countContextEntitiesWd(String wdUri, Model model) {
		int cnt=0;
		Resource choWd = model.createResource(wdUri);
		for(Statement st: RdfUtil.listProperties(choWd, entitiesProperties)) {
			if(st.getObject().isURIResource() && st.getObject().asResource().getURI().startsWith("http://www.wikidata.org/")) {
				cnt++;
//				System.out.println(st);
			}
		}
		return cnt;
	}

	private static int countContextEntitiesEuropeana(Model model) {
		int cnt=0;
		List<Resource> chos = model.listResourcesWithProperty(Rdf.type, Edm.ProvidedCHO).toList();
		chos.addAll(model.listResourcesWithProperty(Rdf.type, Ore.Proxy).toList());
//		chos.addAll(model.listResourcesWithProperty(RegRdf.type, RdfReg.ORE_AGGREGATION).toList());
		for(Resource choRes: chos) {
			for(Statement st: RdfUtil.listProperties(choRes, entitiesProperties)) {
//				if(st.getPredicate().equals(RegRdf.type) || 
//						st.getPredicate().equals(RegEdm.europeanaProxy) ||	
//						st.getPredicate().getURI().startsWith(RdfReg.NsOre) ||	
//						st.getPredicate().equals(RegEdm.europeanaProxy) ||	
//						st.getPredicate().equals(RegEdm.object) ||	
//						st.getPredicate().equals(RegEdm.isShownAt) ||	
//						st.getPredicate().equals(RegEdm.isShownBy) ||	
//						st.getPredicate().equals(RegEdm.aggregatedCHO) 	
//						) continue;
				if(st.getObject().isURIResource()) {
//					System.out.println(st);
					cnt++;
				}
			}
		}
		return cnt;
	}
	
	
	private static MultilingualSaturationResult getMultilingualSaturation(Model m) {
		MultilingualSaturationResult score;
		try {
			return MultilingualSaturation.calculate(m);
	//		score = MultilingualSaturationShacl.calculate(edmCho.getModel());
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	};	
}