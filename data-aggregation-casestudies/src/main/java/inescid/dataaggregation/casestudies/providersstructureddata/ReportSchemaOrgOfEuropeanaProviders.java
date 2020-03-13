package inescid.dataaggregation.casestudies.providersstructureddata;

import java.util.HashMap;

import inescid.util.datastruct.MapOfInts;

public class ReportSchemaOrgOfEuropeanaProviders {

	class ReportOfProvider {
		int testedPages=0;
		int withSchemaOrg=0;
		int errors=0;
		MapOfInts<String> structuredDataPredicates=new MapOfInts<String>();
	}
	
	HashMap<String, ReportOfProvider> providersReports=new HashMap<String, ReportSchemaOrgOfEuropeanaProviders.ReportOfProvider>();
	
	public ReportOfProvider getProvider(String providerId) {
		 ReportOfProvider reportOfProvider = providersReports.get(providerId);
		 if(reportOfProvider==null) {
			 reportOfProvider=new ReportOfProvider();
			 providersReports.put(providerId, reportOfProvider);
		 }
		 return reportOfProvider;
	}


	
}
