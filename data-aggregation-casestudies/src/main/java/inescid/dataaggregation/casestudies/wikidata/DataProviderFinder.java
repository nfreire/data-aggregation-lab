package inescid.dataaggregation.casestudies.wikidata;

import java.util.Map;

import inescid.europeanaapi.AccessException;
import inescid.europeanaapi.EuropeanaApiClient;

public class DataProviderFinder {

	public static void findEuropeanaDataProviders() throws AccessException {
		EuropeanaApiClient europeanaApi=new EuropeanaApiClient("QdEDkmksy");
		Map<String, String> dataProviders = europeanaApi.listProviderIdsAndNames();
		
		System.out.println(dataProviders);
	}
	
	
	public static void main(String[] args) throws Exception{
		findEuropeanaDataProviders();
	}
}
