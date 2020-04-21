package inescid.dataaggregation.casestudies.wikidata;

import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.store.Repository;

public class ScriptRemoveFromRepository {

	
	public static void main(String[] args) throws Exception {
		String httpCacheFolder = "c://users/nfrei/desktop/data/HttpRepositoryWikidataStudy";
		Global.init_componentDataRepository(httpCacheFolder);
		Repository dataRepository = Global.getDataRepository();

		System.out.println(
		dataRepository.remove("http-cache", "http://data.europeana.eu/item/2058811/DAI__06bc30755050072a87459b504e00dbf9__artifact__cho")
				);
		System.out.println(
		dataRepository.remove("http-cache", "http://data.europeana.eu/item/2063629/AUS_280_007")
				);
		System.out.println(
		dataRepository.remove("http-cache", "http://data.europeana.eu/item/2022806/33F51D61BCBA073CCBABA613D788D64A39CBF350")
				);
	}
}
