package inescid.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.fluent.Content;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;

import inescid.dataaggregation.crawl.http.HttpRequest;
import inescid.dataaggregation.crawl.http.UrlRequest;
import inescid.dataaggregation.dataset.GlobalCore;

public class LinkedDataUtil {

	public static final List<String> SUPORTED_ENCODINGS = new ArrayList<String>();

	private static final String DEFAULT_ACCEPT;
	
	static {
		SUPORTED_ENCODINGS.add(MimeType.RDF_XML.id());
		SUPORTED_ENCODINGS.add(MimeType.JSONLD.id());
//		SUPORTED_ENCODINGS.add("application/x-turtle");
		SUPORTED_ENCODINGS.add(MimeType.TURTLE.id());
		DEFAULT_ACCEPT=StringUtils.join(SUPORTED_ENCODINGS,", ");
	}
	
	public static Resource getResource(String resourceUri) throws AccessException, InterruptedException {
		Model dsModel = RdfUtil.readRdf(getResourceRdfBytes(resourceUri).asStream());
		if(!dsModel.containsResource(ResourceFactory.createResource(resourceUri)))
			throw new AccessException(resourceUri, "Response to dataset RDF resource did not contain a RDF description of the resource");
		return dsModel.createResource(resourceUri);
	}
	private static Content getResourceRdfBytes(String resourceUri) throws AccessException, InterruptedException {
		try {
			return HttpUtil.makeRequest(resourceUri, DEFAULT_ACCEPT).getContent();
		} catch (IOException e) {
			throw new AccessException(resourceUri, e);
		}
	}
	private static HttpRequest makeResourceRequest(String resourceUri) throws AccessException, InterruptedException {
		return HttpUtil.makeRequest(resourceUri, DEFAULT_ACCEPT);
	}
	


	public static Resource getAndStoreResource(String resourceUri, File storeFile) throws IOException, InterruptedException, AccessException {
		Content resourceRdfBytes = getResourceRdfBytes(resourceUri);
		Model dsModel = RdfUtil.readRdf(resourceRdfBytes.asStream());
		FileUtils.writeByteArrayToFile(storeFile, resourceRdfBytes.asBytes(), false);		
		Resource resource = dsModel.getResource(resourceUri);
		if(resource==null)
			throw new AccessException(resourceUri, "Response to dataset RDF resource did not contain a RDF description of the resource");
		return resource;
	}

	public static List<Header> getAndStoreResourceWithHeaders(String resourceUri, File storeFile) throws IOException, InterruptedException, AccessException {
		HttpRequest resourceRequest = makeResourceRequest(resourceUri);
		FileUtils.writeByteArrayToFile(storeFile, resourceRequest.getContent().asBytes(), false);		
		List<Header> meta=new ArrayList<>(5);
		for(String headerName: new String[] { "Content-Type", "Content-Encoding", "Content-Disposition", "Link"/*, "Content-MD5"*/}) {
			for(Header h : resourceRequest.getResponse().getHeaders(headerName))
				meta.add(h);
		}
		return meta;
	}
}
