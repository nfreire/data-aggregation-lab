package inescid.util;

import java.io.IOException;

import inescid.dataaggregation.crawl.http.HttpRequest;
import inescid.dataaggregation.dataset.GlobalCore;

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
//			HttpRequest req = HttpUtil.makeRequest("http://www.wikidata.org/entity/", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://data.bibliotheken.nl/doc/dataset/rise-alba", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
			HttpRequest req = HttpUtil.makeRequest("http://hdl.handle.net/10107/3100021", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
			String content = req.getContent().asString();
			System.out.println(content);
			System.out.println("HTTP code: "+req.getResponseStatusCode());
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

}
