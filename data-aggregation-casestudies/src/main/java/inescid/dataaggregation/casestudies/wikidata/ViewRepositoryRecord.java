package inescid.dataaggregation.casestudies.wikidata;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;

import inescid.dataaggregation.casestudies.wikidata.ScriptMetadataAnalyzerOfCulturalHeritage.DataDumps;
import inescid.dataaggregation.casestudies.wikidata.ScriptMetadataAnalyzerOfCulturalHeritage.Files;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.store.Repository;
import inescid.util.RdfUtil;

public class ViewRepositoryRecord {

	public static void main(String[] args) throws Exception {
		String httpCacheFolder = "c://users/nfrei/desktop/data/HttpRepository-wikidata-chos";
		Global.init_componentDataRepository(httpCacheFolder);
		Repository dataRepository = Global.getDataRepository();
		
//		File file = dataRepository.getFile(DataDumps.EUROPEANA_EDM.name(), "http://data.europeana.eu/item/03919/public_mistral_joconde_fr_ACTION_CHERCHER_FIELD_1_REF_VALUE_1_000PE025604");
		File file = dataRepository.getFile(DataDumps.EUROPEANA_EDM.name(), "http://www.wikidata.org/entity/Q12418");
//		Model rdfEdmAtEuropeana = RdfUtil.readRdf(FileUtils.readFileToByteArray(file), Lang.TURTLE);
		System.out.println(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
		file = dataRepository.getFile(DataDumps.WIKIDATA_EDM.name(), "http://www.wikidata.org/entity/Q12418");
//		Model rdfEdmAtEuropeana = RdfUtil.readRdf(FileUtils.readFileToByteArray(file), Lang.TURTLE);
		System.out.println(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
		
		
		
		file = dataRepository.getFile(DataDumps.EUROPEANA_EDM.name(), "http://www.wikidata.org/entity/Q20643382");
		System.out.println(FileUtils.readFileToString(file, StandardCharsets.UTF_8));

		file = dataRepository.getFile(DataDumps.WIKIDATA_EDM.name(), "http://www.wikidata.org/entity/Q20643382");
//		Model rdfEdmAtEuropeana = RdfUtil.readRdf(FileUtils.readFileToByteArray(file), Lang.TURTLE);
		System.out.println(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
	}
	
}
