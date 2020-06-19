package inescid.util.europeana;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.RDFReader;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import crawlercommons.fetcher.FetchedResult;
import inescid.dataaggregation.crawl.http.HttpRequest;
import inescid.dataaggregation.crawl.http.UrlRequest;
import inescid.dataaggregation.crawl.http.UrlRequest.HttpMethod;
import inescid.dataaggregation.data.model.RdfReg;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.dataset.Global;
import inescid.util.AccessException;

public class EdmRdfUtil {

	public static Resource getEuropeanaAggregationResource(Model cho) {
		ResIterator aggregations = cho.listResourcesWithProperty(Rdf.type, Edm.EuropeanaAggregation);
		if (aggregations.hasNext()) {
			return aggregations.next();
		} else
			return null;
	}

	public static Resource getAggregationResource(Model cho) {
		ResIterator aggregations = cho.listResourcesWithProperty(Rdf.type, Ore.Aggregation);
		if (aggregations.hasNext()) {
			return aggregations.next();
		} else
			return null;
	}
	
	public static Resource getProvidedChoResource(Model cho) {
		ResIterator aggregations = cho.listResourcesWithProperty(Rdf.type, Edm.ProvidedCHO);
		if (aggregations.hasNext()) {
			return aggregations.next();
		} else
			return null;
	}
	
	
    public static RDFNode getPropertyOfAggregation(Model cho, Property prop) {
    	Resource agg = getAggregationResource(cho);
    	if (agg!=null) {
    		 Statement propStm = agg.getProperty(prop);
    		 if(propStm!=null)
    			return propStm.getObject(); 
    	}
    	return null;
    }
    
    
    public static RDFNode getPropertyOfProvidedCho(Model cho, Property prop) {
    	Resource agg = getProvidedChoResource(cho);
    	if (agg!=null) {
    		Statement propStm = agg.getProperty(prop);
    		if(propStm!=null)
    			return propStm.getObject(); 
    	}
    	return null;
    }
//	ResIterator aggregations = cho.getModel().listResourcesWithProperty(RdfRegRdf.type, RdfReg.Ore.Aggregation);
//	while(aggregations.hasNext()) {
//		Element resourceToXml = resourceToXml(aggregations.next());
//		if(resourceToXml!=null)
//			rootEl.appendChild(resourceToXml);
//	}	
    
    
	public static Model getRecord(String europeanaApiRecordId) throws AccessException {
		String urlStr = recordUriFromApiId(europeanaApiRecordId);
		return getRecordByUri(urlStr);
	}
	
	public static String getRecordRdfXml(String europeanaApiRecordId) throws AccessException {
		String urlStr = recordUriFromApiId(europeanaApiRecordId);
		return getRecordRdfXmlByUri(urlStr);
	}
	
	public static Model parseEdmRdfXml(String edmRdfXml) throws AccessException {
		StringReader mdReader = new StringReader(edmRdfXml);
		Model ldModelRdf = ModelFactory.createDefaultModel();
		RDFReader reader = ldModelRdf.getReader("RDF/XML");
		reader.setProperty("allowBadURIs", "true");
		reader.read(ldModelRdf, mdReader, null);
		mdReader.close();
		return ldModelRdf;
	}
	
	public static Model getRecordByUri(String uriOfCho) throws AccessException {
		String md = getRecordRdfXmlByUri(uriOfCho);
		return parseEdmRdfXml(md);
	}
	public static String getRecordRdfXmlByUri(String uriOfCho) throws AccessException {
		HttpRequest req = new HttpRequest(new UrlRequest(uriOfCho, HttpMethod.GET, "Accept", "application/rdf+xml", 2));
		try {
			Global.getHttpRequestService().fetch(req);
			return req.getResponseContentAsString();
		} catch (IOException | InterruptedException e) {
			throw buildAccessException(req, e);	
		}
	}


	public static String recordUriFromApiId(String europeanaApiRecordId) {
		return "http://data.europeana.eu/item" + (europeanaApiRecordId.startsWith("/") ? "" : "/") + europeanaApiRecordId;
	}
	
	private static AccessException buildAccessException(HttpRequest con, Exception e) {
		if(e instanceof InterruptedException)
			return new AccessException(con.getUrl(), true);
		if(con.getResponse()==null)
			return new AccessException(con.getUrl(), e);
		String body = null;
		try {
			if(con.getResponseContent()!=null)
				body=con.getResponseContentAsString();
		} catch (IOException e2) { /*ignore*/ }
		return new AccessException("HTTP-status:"+con.getResponseStatusCode() +" ; "+con.getUrl(), String.valueOf(con.getResponseStatusCode()), body);
	}

}
