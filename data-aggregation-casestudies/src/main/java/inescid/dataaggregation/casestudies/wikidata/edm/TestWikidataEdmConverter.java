package inescid.dataaggregation.casestudies.wikidata.edm;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;

import inescid.dataaggregation.casestudies.wikidata.ScriptMetadataAnalyzerOfCulturalHeritage.Files;
import inescid.dataaggregation.wikidata.RdfRegWikidata;
import inescid.dataaggregation.wikidata.WikidataUtil;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.store.Repository;
import inescid.util.AccessException;
import inescid.util.RdfUtil;
import inescid.util.datastruct.CsvDataPersistReader;

public class TestWikidataEdmConverter {

	public static void main(String[] args) throws Exception {
		File outputFolder = new File("c://users/nfrei/desktop/data/wikidata-to-edm");		
		String httpCacheFolder = "c://users/nfrei/desktop/data/HttpRepositoryWikidataStudy";
		final int SAMPLE_RECORDS;

		if (args.length > 0)
			httpCacheFolder = args[0];
		if (args.length > 1)
			SAMPLE_RECORDS = Integer.parseInt(args[1]);
		else
			SAMPLE_RECORDS = 10;

		System.out.printf(
				"Settings:\n-Cache:%s\n-Records:%d\n------------------------\n",
				httpCacheFolder, SAMPLE_RECORDS);
		Global.init_componentHttpRequestService();
		Global.init_componentDataRepository(httpCacheFolder);
		Global.init_enableComponentHttpRequestCache();
		Repository dataRepository = Global.getDataRepository();
		WikidataUtil.setDataRepository(dataRepository);

		final HashMap<String, String> wikidataEuropeanaIdsMap = new HashMap<>();
		CsvDataPersistReader reader=new CsvDataPersistReader(java.nio.file.Files.newBufferedReader(
				new File(outputFolder, "wd_to_europeana_id_map.csv").toPath()
				, StandardCharsets.UTF_8));
		reader.read(wikidataEuropeanaIdsMap);
		
		WikidataEdmConverter conv=new WikidataEdmConverter(
				new File("src/data/wikidata/wikidata_edm_mappings_classes.csv"), 
				new File("src/data/wikidata/wikidata_edm_mappings.csv"),
				new File("src/data/wikidata/wikidata_edm_mappings_hierarchy.csv"),
				new File("c://users/nfrei/desktop/data/wikidata-to-edm/triplestore-wikidata"),
				new File("../data-aggregation-lab-core/src/main/resources/owl/edm.owl")
				);
		conv.enableUnmappedLogging();
		
		int[] cnt=new int[] {0};
		wikidataEuropeanaIdsMap.forEach((wdResourceUri, europeanaId) -> {
			if(SAMPLE_RECORDS>0 && cnt[0]>SAMPLE_RECORDS) return;
			try {
				Resource wdResource = WikidataUtil.fetchResource(wdResourceUri);
//				System.out.println(wdResource);
				Resource edm = conv.convert(wdResource);
				
				if(SAMPLE_RECORDS>0) 
					FileUtils.write(new File(outputFolder, URLEncoder.encode(wdResourceUri, StandardCharsets.UTF_8.toString())+".ttl"),  
							RdfUtil.writeRdfToString(edm.getModel(), Lang.TURTLE), StandardCharsets.UTF_8);
				cnt[0]++;
			} catch (AccessException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		
		System.out.println(conv.unmappedToCsv());
	}
	
	
}

