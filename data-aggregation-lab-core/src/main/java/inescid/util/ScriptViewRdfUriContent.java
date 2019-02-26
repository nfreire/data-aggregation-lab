package inescid.util;

import java.io.IOException;

import inescid.dataaggregation.crawl.http.HttpRequest;
import inescid.dataaggregation.dataset.GlobalCore;

public class ScriptViewRdfUriContent {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			GlobalCore.init_developement();
//			HttpRequest req = HttpUtil.makeRequest("http://www.wikidata.org/prop/P84", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://www.wikidata.org/prop/direct/P84", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
			HttpRequest req = HttpUtil.makeRequest("http://www.wikidata.org/entity/P84", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
			String content = req.getContent().asString();
			System.out.println(content);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

}
