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
import inescid.dataaggregation.dataset.Global;

public class LinkedDataUtil {

	public static final List<String> SUPORTED_ENCODINGS = new ArrayList<String>();

	private static final String DEFAULT_ACCEPT;
	
	static {
		SUPORTED_ENCODINGS.add("application/rdf+xml");
		SUPORTED_ENCODINGS.add("application/ld+json");
//		SUPORTED_ENCODINGS.add("application/x-turtle");
		SUPORTED_ENCODINGS.add("text/turtle");
		DEFAULT_ACCEPT=StringUtils.join(SUPORTED_ENCODINGS,", ");
	}
	
	public static Resource getResource(String resourceUri) throws HttpRequestException, InterruptedException {
		Model dsModel = readRdf(getResourceRdfBytes(resourceUri));
		if(!dsModel.containsResource(ResourceFactory.createResource(resourceUri)))
			throw new HttpRequestException(resourceUri, "Response to dataset RDF resource did not contain a RDF description of the resource");
		return dsModel.createResource(resourceUri);
	}
	private static Content getResourceRdfBytes(String resourceUri) throws HttpRequestException, InterruptedException {
		try {
			return HttpUtil.makeRequest(resourceUri, DEFAULT_ACCEPT).getContent();
		} catch (IOException e) {
			throw new HttpRequestException(resourceUri, e);
		}
	}
	private static HttpRequest makeResourceRequest(String resourceUri) throws HttpRequestException, InterruptedException {
		return HttpUtil.makeRequest(resourceUri, DEFAULT_ACCEPT);
	}
	
	private static Model readRdf(Content content) {
		Model model = null;
		Lang rdfLang=null;
		if (content.getType()!=null && content.getType().getMimeType()!=null){
			rdfLang=RdfUtil.fromMimeType(content.getType().getMimeType());
			if (rdfLang!=null) {
	//		byte[] fileBytes = FileUtils.readFileToByteArray(schemaorgFile);
				model = ModelFactory.createDefaultModel();
				model.read(content.asStream(), null, rdfLang.getName());
			} else {
				for(Lang l: new Lang[] { Lang.RDFXML, Lang.TURTLE, Lang.JSONLD}) {
					try {
						model = ModelFactory.createDefaultModel();
						model.read(content.asStream(), null, rdfLang.getName());
						break;
					} catch (Exception e){
						//ignore and try another reader
					}
				}
			}
		}
		return model;
	}

	public static Resource getAndStoreResource(String resourceUri, File storeFile) throws IOException, InterruptedException, HttpRequestException {
		Content resourceRdfBytes = getResourceRdfBytes(resourceUri);
		Model dsModel = readRdf(resourceRdfBytes);
		FileUtils.writeByteArrayToFile(storeFile, resourceRdfBytes.asBytes(), false);		
		Resource resource = dsModel.getResource(resourceUri);
		if(resource==null)
			throw new HttpRequestException(resourceUri, "Response to dataset RDF resource did not contain a RDF description of the resource");
		return resource;
	}

	public static List<Header> getAndStoreResourceWithHeaders(String resourceUri, File storeFile) throws IOException, InterruptedException, HttpRequestException {
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