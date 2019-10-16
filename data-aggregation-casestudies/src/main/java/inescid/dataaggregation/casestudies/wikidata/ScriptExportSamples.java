package inescid.dataaggregation.casestudies.wikidata;

import java.io.File;

import inescid.dataaggregation.data.ContentTypes;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.store.Repository;

public class ScriptExportSamples {
	public enum DataDumps {
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

		Global.init_componentHttpRequestService();
		Global.init_componentDataRepository(httpCacheFolder);

		Repository dataRepository = Global.getDataRepository();

		
		dataRepository.exportDatasetToZip(DataDumps.WIKIDATA_ONTOLOGY.name(), new File(httpCacheFolder, "wikidata-subdataset-ontology.zip"), ContentTypes.TURTLE);
		dataRepository.exportDatasetToZip(DataDumps.WIKIDATA_SCHEMAORG.name(), new File(httpCacheFolder, "wikidata-subdataset-schemaorg.zip"), ContentTypes.TURTLE);
		dataRepository.exportDatasetToZip(DataDumps.WIKIDATA_EDM.name(), new File(httpCacheFolder, "wikidata-subdataset-edm.zip"), ContentTypes.TURTLE);
		dataRepository.exportDatasetToZip(DataDumps.EUROPEANA_EDM.name(), new File(httpCacheFolder, "europeana-subdataset-edm.zip"), ContentTypes.TURTLE);
		
	}


}