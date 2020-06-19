package inescid.dataaggregation.casestudies.wikidata;

import java.io.File;

import inescid.dataaggregation.casestudies.wikidata.ScriptMetadataAnalyzerOfCulturalHeritage.Files;
import inescid.dataaggregation.data.ContentTypes;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.store.Repository;

public class ScriptExportSamples {
	public enum DataDumps {
		WIKIDATA_EDM, EUROPEANA_EDM, WIKIDATA_ONTOLOGY, WIKIDATA_SCHEMAORG
	};

	public static void main(String[] args) throws Exception {
		File outputFolder = Files.defaultOutputFolder;		
		String httpCacheFolder = "c://users/nfrei/desktop/data/HttpRepositoryWikidataStudy";

		if (args.length > 0)
			outputFolder = new File(args[0]);
		if (args.length > 1)
			httpCacheFolder = args[1];

		if (!outputFolder.exists())
			outputFolder.mkdirs();

		System.out.printf(
				"Settings:\n-OutpputFolder:%s\n-Cache:%s\n------------------------\n",
				outputFolder.getPath(), httpCacheFolder);

		Global.init_componentHttpRequestService();
		Global.init_componentDataRepository(httpCacheFolder);

		Repository dataRepository = Global.getDataRepository();

		
		dataRepository.exportDatasetToZip(DataDumps.WIKIDATA_ONTOLOGY.name(), new File(outputFolder, "wikidata-subdataset-ontology.zip"), ContentTypes.TURTLE);
		dataRepository.exportDatasetToZip(DataDumps.WIKIDATA_EDM.name(), new File(outputFolder, "wikidata-subdataset-edm.zip"), ContentTypes.TURTLE);
		dataRepository.exportDatasetToZip(DataDumps.EUROPEANA_EDM.name(), new File(outputFolder, "europeana-subdataset-edm.zip"), ContentTypes.TURTLE);
		
	}


}