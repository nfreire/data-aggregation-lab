package inescid.dataaggregation.casestudies.iiifuniverse;

import java.io.File;

import eu.europeana.research.iiif.crawl.IiifCollectionTree;
import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.store.Repository;

public class MetadataUsageCrawler {

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
				"Settings:\n-OutpputFolder:%s\n-Cache:%s\n-Records:%d\n------------------------\n",
				outputFolder.getPath(), httpCacheFolder, SAMPLE_RECORDS);

		Global.init_componentHttpRequestService();
		Global.init_componentDataRepository(httpCacheFolder);

		Repository dataRepository = Global.getDataRepository();

		CachedHttpRequestService rdfCache = new CachedHttpRequestService();
		rdfCache.setRequestRetryAttempts(1);

		IiifCollectionTree universe=new IiifCollectionTree("http://ryanfb.github.io/iiif-universe/iiif-universe.json");
		universe.fetchFromPresentationApi(false);
		for(IiifCollectionTree subCol : universe.getSubcollections()) {
			subCol.getLabel();
			System.out.println(subCol.getCollectionUri());
			System.out.println(subCol.getLabel());
		}
		System.out.println(universe.getSubcollections().size()+" total top collections");
	}


}