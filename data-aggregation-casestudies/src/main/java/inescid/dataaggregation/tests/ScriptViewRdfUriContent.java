package inescid.dataaggregation.tests;

import org.apache.jena.riot.Lang;

import inescid.dataaggregation.crawl.http.HttpRequest;
import inescid.dataaggregation.data.RdfReg;
import inescid.dataaggregation.dataset.Global;
import inescid.util.HttpUtil;
import inescid.util.RdfUtil;

public class ScriptViewRdfUriContent {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		http://www.wikidata.org/entity.json
		try { 
			Global.init_developement();
			HttpRequest req = null;
//			req = HttpUtil.makeRequest("http://schema.org/", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			req = HttpUtil.makeRequest("http://dbpedia.org/ontology/", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			req = HttpUtil.makeRequest("http://dbpedia.org/resource/classes#", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://www.wikidata.org/prop/direct/P1889", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://www.wikidata.org/entity/P727", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://www.wikidata.org/prop/P84", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://www.wikidata.org/prop/direct/P84", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://www.wikidata.org/entity/P84", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://www.cidoc-crm.org/cidoc-crm/", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			req = HttpUtil.makeRequest(RdfReg.NsOwl, RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			req = HttpUtil.makeRequest("http://www.wikidata.org/entity/Q35120", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://www.wikidata.org/entity/statement/", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://data.bibliotheken.nl/doc/dataset/rise-alba", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://hdl.handle.net/10107/3100021", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://terminology.lido-schema.org/", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://terminology.lido-schema.org/identifier_type", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://id.bnportugal.gov.pt/bib/catbnp/1", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://terminology.lido-schema.org/lido00007", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			req = HttpUtil.makeRequest("http://www.europeana.eu/schemas/edm/", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			req = HttpUtil.makeRequest("http://repo.elte-dh.hu:8080/fcrepo/rest/58/42/ae/1e/5842ae1e-e7f1-411a-a6a2-a2dbb4ea2f80/", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://purl.org/dc/elements/1.1/", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://purl.org/dc/terms/", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://purl.org/dc/elements/1.1/title", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			HttpRequest req = HttpUtil.makeRequest("http://rdaregistry.info/Elements/c.ttl", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			req = HttpUtil.makeRequest("http://id.loc.gov/authorities/names/n79039898", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			req = HttpUtil.makeRequest("https://data.bnf.fr/ark:/12148/cb10048342p", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			req = HttpUtil.makeRequest("http://urn.fi/URN:NBN:fi:bib:me:I00000624700", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
//			req = HttpUtil.makeRequest("http://www.wikidata.org/entity/Q13140720", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
			req = HttpUtil.makeRequest("http://viaf.org/viaf/110233335", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
			String content = req.getContentAsString();
			System.out.println(content);
			System.out.println("HTTP code: "+req.getResponseStatusCode());
			System.out.println("Content type: "+req.getResponseHeader("Content-Type"));
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

}
