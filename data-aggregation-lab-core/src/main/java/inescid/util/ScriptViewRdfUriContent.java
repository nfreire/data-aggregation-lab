package inescid.util;

import java.io.IOException;

import inescid.dataaggregation.crawl.http.HttpRequest;
import inescid.dataaggregation.dataset.GlobalCore;
import inescid.dataaggregation.dataset.convert.RdfReg;

public class ScriptViewRdfUriContent {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		http://www.wikidata.org/entity.json
		try { 
			GlobalCore.init_developement();
//			HttpRequest req = HttpUtil.makeRequest("http://www.wikidata.org/prop/P84", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://www.wikidata.org/prop/direct/P84", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://www.wikidata.org/entity/P84", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://www.cidoc-crm.org/cidoc-crm/", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://www.wikidata.org/entity/", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://www.wikidata.org/entity/statement/", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://data.bibliotheken.nl/doc/dataset/rise-alba", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://hdl.handle.net/10107/3100021", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://terminology.lido-schema.org/", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://terminology.lido-schema.org/identifier_type", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
			HttpRequest req = HttpUtil.makeRequest("http://terminology.lido-schema.org/lido00007", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://www.europeana.eu/schemas/edm/", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://purl.org/dc/elements/1.1/", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://purl.org/dc/terms/", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://purl.org/dc/elements/1.1/title", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://rdaregistry.info/Elements/c.ttl", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
			String content = req.getContent().asString();
			System.out.println(content);
			System.out.println("HTTP code: "+req.getResponseStatusCode());
			System.out.println("Content type: "+req.getResponseHeader("Content-Type"));
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

}
