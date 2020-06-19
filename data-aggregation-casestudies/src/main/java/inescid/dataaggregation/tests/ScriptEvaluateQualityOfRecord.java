package inescid.dataaggregation.tests;

import org.apache.jena.rdf.model.Model;

import inescid.dataaggregation.crawl.http.HttpRequestService;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.profile.tiers.EpfTiersCalculator;
import inescid.dataaggregation.dataset.profile.tiers.TiersCalculation;
import inescid.util.HttpUtil;
import inescid.util.RdfUtil;

public class ScriptEvaluateQualityOfRecord {

	public static void main(String[] args) throws Exception {
		Global.init_componentHttpRequestService();
//		String uri="http://data.europeana.eu/item/2021008/M011_9858";
		String uri="http://data.europeana.eu/item/0943109/libvar_ADV_12243";
//		String uri="http://data.europeana.eu/item/0943109/libvar_ADV_11639";
		Model edm = RdfUtil.readRdfFromUri(uri);
		
		RdfUtil.printOutRdf(edm);
		
		String edmXml=HttpUtil.makeRequest(uri, "Accept", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER).getResponseContentAsString();
//		TiersCalculation calculate = EpfTiersCalculator.calculateOnInternalEdm(edm.createResource(uri)); 
		TiersCalculation calculate = EpfTiersCalculator.calculate(edmXml); 
		
		System.out.println(calculate.getEnablingElements());
		System.out.println(calculate.getContextualClass());
		System.out.println(calculate.getLanguage());
		System.out.println(calculate.getMetadata());
		
	}
}
