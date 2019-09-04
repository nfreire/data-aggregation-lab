package inescid.dataaggregation.casestudies.wikidata;

import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.crawl.http.HttpResponse;
import inescid.util.AccessException;
import inescid.util.RdfUtil;

public class WikidataRdfUtil {

	public static Resource fetchresource(String resourceUri, CachedHttpRequestService rdfCache)
			throws AccessException, InterruptedException, IOException {
		HttpResponse propFetched = rdfCache.fetchRdf(resourceUri);
		if (!propFetched.isSuccess()) {
			throw new AccessException(resourceUri);
		} else {
			Model rdfWikidata = RdfUtil.readRdf(propFetched.body,
					RdfUtil.fromMimeType(propFetched.getHeader("Content-Type")));
			if (rdfWikidata.size() == 0)
				throw new AccessException(resourceUri, "No data found");
			Resource wdPropResource = rdfWikidata.getResource(resourceUri);
			return wdPropResource;
		}
	}
}
