package inescid.dataaggregation.dataset.convert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.regex.Pattern;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import inescid.dataaggregation.dataset.Global;

public class RdfDeserializer {

	public static Model fromBytes(byte[] serializedRdf, String baseUri) {
		String mime=detectMimeType(serializedRdf);
		if(mime!=null)
			return fromMimeType(serializedRdf, baseUri, mime);
		throw new IllegalArgumentException("Mime-type detection not possible for "+baseUri);
	}
	
	public static Model fromMimeType(String serializedRdf, String baseUri, String mimeType) {
		if(mimeType.equals("application/ld+json")) 
			return fromJsonLd(serializedRdf, baseUri);
		if(mimeType.equals("application/rdf+xml")) 
			return fromRdfXml(serializedRdf,  baseUri);
		throw new IllegalArgumentException("Unsupported mime: "+mimeType);
	}
	
	public static Model fromMimeType(byte[] serializedRdf, String baseUri, String mimeType) {
		if(mimeType.equals("application/ld+json")) 
			return fromJsonLd(new String(serializedRdf, Global.UTF8), baseUri);
		if(mimeType.equals("application/rdf+xml")) 
			return fromRdfXml(serializedRdf,  baseUri);
		throw new IllegalArgumentException("Unsupported mime: "+mimeType);
	}
	
	static final Pattern XML_DECLARATION_PATTERN=Pattern.compile("^\\s*<\\?xml\\s");
	static final Pattern JSON_BRACES_PATTERN=Pattern.compile("^\\s*\\{.*\\}\\s*$",Pattern.DOTALL);
	public static String detectMimeType(byte[] rdfBytes) {
		String s=new String(rdfBytes, Global.UTF8);
		if(XML_DECLARATION_PATTERN.matcher(s).find())
			return "application/rdf+xml";
		if(JSON_BRACES_PATTERN.matcher(s).matches())
			return "application/ld+json";
		return null;
	}
	
//		 "RDF/XML", "N-TRIPLE", "TURTLE" (or "TTL") and "N3". null represents the default language, "RDF/XML". "RDF/XML-ABBREV" is a synonym for 
	
	public static Model fromJsonLd(String serializedRdf, String baseUri) {
		Model modelRdf = ModelFactory.createDefaultModel();
		StringReader mdReader = new StringReader(serializedRdf);
		modelRdf.read(mdReader, baseUri, "JSON-LD");
		mdReader.close();

//		RDFDataMgr.read(modelRdf, new StringReader(serializedRdf), uriOfMainResource, Lang.JSONLD);
		
		return modelRdf;
	}

	public static Model fromRdfXml(String serializedRdf, String baseUri) {
		Model modelRdf = ModelFactory.createDefaultModel();
		StringReader mdReader = new StringReader(serializedRdf);
		modelRdf.read(mdReader, baseUri, "RDF/XML");
		mdReader.close();
		return modelRdf;
	}
	
	public static Model fromRdfXml(byte[] serializedRdf, String baseUri) {
		try {
			ByteArrayInputStream is = new ByteArrayInputStream(serializedRdf);
			Model fromRdfXml = fromRdfXml(is, baseUri);
			is.close();
			return fromRdfXml;
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	public static Model fromRdfXml(InputStream serializedRdf, String baseUri) {
		Model modelRdf = ModelFactory.createDefaultModel();
		modelRdf.read(serializedRdf, baseUri, "RDF/XML");
		return modelRdf;
	}
	
	
}
