package inescid.dataaggregation.casestudies.wikidata;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
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

import inescid.dataaggregation.casestudies.wikidata.WikidataSparqlClient.UriHandler;
import inescid.dataaggregation.casestudies.wikidata.evaluation.Dqc10PointRatingCalculatorNoRights;
import inescid.dataaggregation.casestudies.wikidata.evaluation.EdmValidation;
import inescid.dataaggregation.casestudies.wikidata.evaluation.ValidatorForNonPartners;
import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.crawl.http.CachedHttpRequestService.HttpResponse;
import inescid.dataaggregation.data.ContentTypes;
import inescid.dataaggregation.dataset.GlobalCore;
import inescid.dataaggregation.dataset.convert.EdmUtil;
import inescid.dataaggregation.dataset.convert.RdfReg;
import inescid.dataaggregation.dataset.convert.SchemaOrgToEdmDataConverter;
import inescid.dataaggregation.dataset.profile.ClassUsageStats;
import inescid.dataaggregation.dataset.profile.UsageProfiler;
import inescid.dataaggregation.dataset.profile.completeness.Dqc10PointRatingCalculator;
import inescid.dataaggregation.dataset.validate.Validator.Schema;
import inescid.dataaggregation.store.Repository;
import inescid.europeanaapi.EuropeanaApiClient;
import inescid.util.AccessException;
import inescid.util.EdmRdfUtil;
import inescid.util.HttpUtil;
import inescid.util.MapOfInts;
import inescid.util.RdfUtil;

public class ScriptExportSamples {
	enum DataDumps {
		WIKIDATA_EDM, EUROPEANA_EDM, WIKIDATA_ONTOLOGY, WIKIDATA_SCHEMAORG
	};

	public static void main(String[] args) throws Exception {
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
				"Settings:\n-OutpputFolder:%s\n-Cache:%s\n------------------------\n",
				outputFolder.getPath(), httpCacheFolder);

		GlobalCore.init_componentHttpRequestService();
		GlobalCore.init_componentDataRepository(httpCacheFolder);

		Repository dataRepository = GlobalCore.getDataRepository();

		
		dataRepository.exportDatasetToZip(DataDumps.WIKIDATA_ONTOLOGY.name(), new File(httpCacheFolder, "wikidata-subdataset-ontology.zip"), ContentTypes.TURTLE);
		dataRepository.exportDatasetToZip(DataDumps.WIKIDATA_EDM.name(), new File(httpCacheFolder, "wikidata-subdataset-edm.zip"), ContentTypes.TURTLE);
		dataRepository.exportDatasetToZip(DataDumps.EUROPEANA_EDM.name(), new File(httpCacheFolder, "europeana-subdataset-edm.zip"), ContentTypes.TURTLE);
		
	}


}