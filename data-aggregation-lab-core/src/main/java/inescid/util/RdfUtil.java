package inescid.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.RDFReader;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotException;

import inescid.dataaggregation.crawl.http.HttpRequest;
import inescid.dataaggregation.crawl.http.HttpResponse;
import inescid.dataaggregation.crawl.http.UrlRequest;
import inescid.dataaggregation.data.model.Owl;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Rdfs;
import inescid.dataaggregation.data.model.Schemaorg;
import inescid.dataaggregation.data.model.Skos;


public class RdfUtil {
	public static class Templates {
		public void iterateStatements() {
//			for (StmtIterator props = resource.listProperties() ; props.hasNext() ; ) {
//				Statement nextStatement = props.nextStatement();
//			}
			throw new RuntimeException("this is a template, should not be used in execution");
		}
	}
	public static class Jena {
		public static Resource createResource() {
			return ResourceFactory.createResource();
		}
		public static Resource createResource(String uri) {
			return ResourceFactory.createResource(uri);
		}
		public static Model createModel() {
			return ModelFactory.createDefaultModel();
		}
		public static Property createProperty(String uri) {
			return ResourceFactory.createProperty(uri);
		}
		public static Statement createStatement(Resource sub, Property pred, RDFNode obj) {
			return ResourceFactory.createStatement(sub, pred, obj);
		}
		public static Statement createStatement(String subjectUri, Property pred, RDFNode obj) {
			return ResourceFactory.createStatement(createResource(subjectUri), pred, obj);
		}
		public static Resource getResourceIfExists(String uri, Model model) {
			Resource createResource = model.createResource(uri);
			StmtIterator stms = model.listStatements(createResource, null, (RDFNode) null);
			if(stms.hasNext())
				return createResource;
			else
				return null;
		}
		public static Statement createStatementAddToModel(Model model, Resource subject, Property pred,
				RDFNode object) {
			Statement st = model.createStatement(subject, pred, object);
			model.add(st);
			return st;
		}
		public static Statement createStatementAddToModel(Model model, String subjectUri, Property pred,
				RDFNode object) {
			Statement st = model.createStatement(model.createResource(subjectUri), pred, object);
			model.add(st);
			return st;
		}
		public static Statement createStatement(Triple triple) {
			if(triple.getObject().isBlank())
				return createStatement(createResource(triple.getSubject().getURI() ), 
						createProperty(triple.getPredicate().getURI()),  
						createResource(triple.getObject().getBlankNodeId().getLabelString()) );
			if(triple.getObject().isURI())
				return createStatement(createResource(triple.getSubject().getURI() ), 
						createProperty(triple.getPredicate().getURI()),  
						createResource(triple.getObject().getURI()) );
			if(triple.getObject().isLiteral())
				return createStatement(createResource(triple.getSubject().getURI() ), 
						createProperty(triple.getPredicate().getURI()),  
						createLiteral(triple.getObject())
						);
			return null;
		}
		private static RDFNode createLiteral(Node n) {
			if(n.getLiteralLanguage()!=null)
				return createLiteral(n.getLiteralValue(), n.getLiteralLanguage());
			if(n.getLiteralDatatype()!=null)
				return createLiteral(n.getLiteralValue(), n.getLiteralDatatype());
			return createLiteral(n.getLiteralValue()); 
		}
		private static RDFNode createLiteral(Object literalValue, String literalLanguage) {
			return ResourceFactory.createLangLiteral(literalValue.toString(), literalLanguage);
		}
		private static RDFNode createLiteral(Object literalValue, RDFDatatype rdfDatatype) {
			return ResourceFactory.createTypedLiteral(literalValue.toString(), rdfDatatype);
		}
		private static RDFNode createLiteral(Object literalValue) {
			return ResourceFactory.createPlainLiteral(literalValue.toString());
		}
	}
	
	
	public static final String CONTENT_TYPES_ACCEPT_HEADER=Lang.RDFXML.getContentType().getContentType()+", "+Lang.TURTLE.getContentType().getContentType()+", "+Lang.JSONLD.getContentType().getContentType();
	
	public static Resource findResource(Resource startResource, Property... propertiesToFollow) {
		Resource curRes=startResource;
		for(int i=0; i<propertiesToFollow.length; i++) {
			Statement propStm = curRes.getProperty(propertiesToFollow[i]);
			if(propStm==null)
				return null;
			curRes=(Resource) propStm.getObject();
		}
		return curRes;
	}

	public static List<Resource> findResources(Resource startResource, Property... propertiesToFollow) {
		Resource curRes=startResource;
		List<Resource> ret=new ArrayList<Resource>();
		for(int i=0; i<propertiesToFollow.length; i++) {
			Statement propStm = curRes.getProperty(propertiesToFollow[i]);
			if(propStm==null)
				return null;
			ret.add((Resource) propStm.getObject());
		}
		return ret;
	}

	public static String getUriOrId(Resource srcResource) {
		return srcResource.isURIResource() ? srcResource.getURI() : srcResource.getId().getBlankNodeId().toString();
	}
	
	public static String getUriIfResource(RDFNode srcResource) {
		return srcResource.isURIResource() ? srcResource.asResource().getURI() : null;
	}

	public static String getUriOrLiteralValue(RDFNode resource) {
		return resource.isURIResource() ? resource.asResource().getURI() : (resource.isLiteral() ? resource.asLiteral().getString() : null);
	}
	
	public static boolean contains(String uri, Model inModel) {
		return exists(uri, inModel);
	}
	public static boolean exists(String uri, Model inModel) {
		return inModel.contains(inModel.getResource(uri), null);
	}
	
	public static Lang fromMimeType(String mimeType) {
		if(mimeType==null)
			return null;
		if (mimeType.contains(";"))
			mimeType=mimeType.substring(0, mimeType.indexOf(';')).trim();
		if (mimeType.contains(","))
			mimeType=mimeType.substring(0, mimeType.indexOf(',')).trim();
		return RDFLanguages.contentTypeToLang(mimeType);
	}

	public static Model readRdf(String content) {
//		System.out.println(content);
		return readRdf(new StringReader(content));
	}
	public static Model readRdf(byte[] content) {
		RiotException lastException=null;
		Model model = null;
		for(Lang l: new Lang[] { Lang.RDFXML, Lang.TURTLE, Lang.JSONLD}) {
			ByteArrayInputStream in=new ByteArrayInputStream(content);
			try {
				model = ModelFactory.createDefaultModel();
				RDFReader reader = model.getReader(l.getName());
				reader.setProperty("allowBadURIs", "true");
				reader.read(model, in, null);
				lastException=null;
				break;
			} catch (RiotException e){
				lastException=e;
			} catch (Exception e){
				lastException = new RiotException(e);
			}
		}
		if(lastException!=null)
			throw lastException;
		return model;
	}
	public static Model readRdf(Reader content) {
		Model model = null;
		for(Lang l: new Lang[] { Lang.RDFXML, Lang.TURTLE, Lang.JSONLD}) {
			try {
				model = ModelFactory.createDefaultModel();
				RDFReader reader = model.getReader(l.getName());
				reader.setProperty("allowBadURIs", "true");
				reader.read(model, content, null);
				break;
			} catch (Exception e){
				e.printStackTrace();
				//ignore and try another reader
			}
		}
		return model;
	}
	public static Model readRdf(InputStream content) {
		try {
			return readRdf(IOUtils.toByteArray(content));
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	public static Model readRdf(String content, Lang l) {
		if(l==null)	return readRdf(content);
		return readRdf(new StringReader(content), l);
	}
	public static Model readRdf(Reader content, Lang l) {
		if(l==null)	return readRdf(content);

		String contentStr;
		try {
			contentStr = IOUtils.toString(content);
		} catch (IOException e1) {
			throw new RuntimeException(e1.getMessage(), e1);
		}	
		
		Model model = ModelFactory.createDefaultModel();
		RDFReader reader = model.getReader(l.getName());
		reader.setProperty("allowBadURIs", "true");
		try {
			reader.read(model, new StringReader(contentStr), null);
			return model;
		} catch (RiotException e) {
			if(e.getMessage().startsWith("Bad character in IRI (space)")) {
//					System.err.println(asStringRdf);
					for(boolean found=true; found; ) {
						Matcher m=(l.equals(Lang.RDFXML) ? patternSpaceInIri : patternSpaceInIriTtl).matcher(contentStr);
						if(m.find()) {
							contentStr=m.replaceAll("$1%20$3");
							found=true;
						}else
							found=false;
					}
//					System.err.println(asStringRdf);
					reader.read(model, new StringReader(contentStr), null);
					return model;
			}else if(e.getMessage().contains("] Illegal character in IRI")) {
//					 (codepoint 0x7C, '|'): 
				Matcher m1 = patternIllegalChar.matcher(e.getMessage());
				if(m1.find()) {
					for(boolean found=true; found; ) {
						String charPattern= l.equals(Lang.RDFXML) ?
								patternCharInIriPre+"\\"+m1.group(1)+patternCharInIriSuf :
									patternCharInIriPreTtl+"\\"+m1.group(1)+patternCharInIriSufTtl;
						Pattern patternCharInIri=Pattern.compile(charPattern);
						Matcher m=patternCharInIri.matcher(contentStr);
						if(m.find()) {
							contentStr=m.replaceAll("$1%20$3");
							found=true;
						}else
							found=false;
					}
//					System.err.println(asStringRdf);
					reader.read(model, new StringReader(contentStr), null);
					return model;
				}
				throw e;
			} else
				throw e;
		}
	}

	final static String patternCharInIriPre="((resource|about)\\s*=\\s*\"[^\"]*)";
	final static String patternCharInIriSuf="([^\"]*\")";
	final static String patternCharInIriPreTtl="(<[^>]*)(";
	final static String patternCharInIriSufTtl=")([^>]*>)";
	final static Pattern patternSpaceInIri=Pattern.compile(patternCharInIriPre+"\\s"+patternCharInIriSuf);
	final static Pattern patternSpaceInIriTtl=Pattern.compile(patternCharInIriPreTtl+" "+patternCharInIriSufTtl);
	final static Pattern patternIllegalChar=Pattern.compile("Illegal character in IRI \\([^\\)]*'(.)'");
	
	
	public static Model readRdf(InputStream content, Lang l) throws RiotException {
		try {
			byte[] byteArray = IOUtils.toByteArray(content);			
			if(l==null)	return readRdf(byteArray);
			
			Model model = ModelFactory.createDefaultModel();
			RDFReader reader = model.getReader(l.getName());
			try {
				reader.read(model, new ByteArrayInputStream(byteArray), null);
				return model;
			} catch (RiotException e) {
				if(e.getMessage().startsWith("Bad character in IRI (space)")) {
						String asStringRdf=new String(byteArray, StandardCharsets.UTF_8);
//						System.err.println(asStringRdf);
						for(boolean found=true; found; ) {
							Matcher m=(l.equals(Lang.RDFXML) ? patternSpaceInIri : patternSpaceInIriTtl).matcher(asStringRdf);
							if(m.find()) {
								asStringRdf=m.replaceAll("$1%20$3");
								found=true;
							}else
								found=false;
						}
//						System.err.println(asStringRdf);
						reader.read(model, new StringReader(asStringRdf), null);
						return model;
				}else if(e.getMessage().contains("] Illegal character in IRI")) {
//						 (codepoint 0x7C, '|'): 
					Matcher m1 = patternIllegalChar.matcher(e.getMessage());
					if(m1.find()) {
						String asStringRdf=new String(byteArray, StandardCharsets.UTF_8);
//						System.err.println(asStringRdf);
						for(boolean found=true; found; ) {
							String charPattern= l.equals(Lang.RDFXML) ?
									patternCharInIriPre+"\\"+m1.group(1)+patternCharInIriSuf :
										patternCharInIriPreTtl+"\\"+m1.group(1)+patternCharInIriSufTtl;
							Pattern patternCharInIri=Pattern.compile(charPattern);
							Matcher m=patternCharInIri.matcher(asStringRdf);
							if(m.find()) {
								asStringRdf=m.replaceAll("$1%20$3");
								found=true;
							}else
								found=false;
						}
//						System.err.println(asStringRdf);
						reader.read(model, new StringReader(asStringRdf), null);
						return model;
					}
					throw e;
				} else
					throw e;
			}
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
//	public static Model readRdf(InputStream content, Lang l) throws RiotException {
//		try {
//			if(l==null)	return readRdf(IOUtils.toByteArray(content));
//		} catch (IOException e) {
//			throw new RuntimeException(e.getMessage(), e);
//		}
//		try {
//			Model model = ModelFactory.createDefaultModel();
//			RDFReader reader = model.getReader(l.getName());
//			reader.setProperty("allowBadURIs", "true");
//			reader.read(model, content, null);
//			return model;
//		} catch (RiotException e) {
//			if(e.getMessage().startsWith("Bad character in IRI (space)")) {
//				System.err.println("Bad IRI: ");
//				System.err.println(e.getMessage());
//				try {
//					Pattern patternSpaceInIri=Pattern.compile("((resource|about)\\s*=\\s*\"[^\"]+)\\s([^\"]*\")");
//					String asStringRdf=new String(IOUtils.toByteArray(content), StandardCharsets.UTF_8);
//					Matcher m=patternSpaceInIri.matcher(asStringRdf);
//					System.err.println(m.replaceAll("$1%20$2"));
//					
//					throw e;
//				} catch (IOException e2) {
//					throw new RuntimeException(e.getMessage(), e2);
//				}
//			} else
//				throw e;
//		}
//	}
	
	
	
	public static Model readRdf(byte[] content, Lang l) throws RiotException {
		if(l==null)	return readRdf(content);
		ByteArrayInputStream in=new ByteArrayInputStream(content);
		Model model = readRdf(in, l);
		try {
			in.close();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return model;
	}

//	public static final String UTF8_BOM = "\uFEFF";
	public static Model readRdfFromUri(String uri) throws AccessException, InterruptedException, IOException {
		HttpRequest rdfReq = HttpUtil.makeRequest(uri, "Accept", CONTENT_TYPES_ACCEPT_HEADER);
//		if(rdfString.startsWith(UTF8_BOM)) {
//			rdfString = rdfString.substring(1);
//	    }
//		System.out.println(rdfString);
		try {
			return readRdf(rdfReq.getResponse());
		} catch (RiotException e) {
			throw new AccessException(uri, e);
		}
	}
	public static Model readRdfFromUriLowTimeout(String uri) throws AccessException, InterruptedException, IOException {
		UrlRequest urlReq=new UrlRequest(uri, "Accept", CONTENT_TYPES_ACCEPT_HEADER);
		urlReq.setConnectionTimeout(2000);
		urlReq.setSocketTimeout(4000);
		HttpRequest rdfReq = HttpUtil.makeRequest(urlReq);
//		if(rdfString.startsWith(UTF8_BOM)) {
//			rdfString = rdfString.substring(1);
//	    }
//		System.out.println(rdfString);
		return readRdf(rdfReq.getResponse());
	}
	public static boolean isUriResolvable(String uri) throws AccessException, InterruptedException, IOException {
		Model readRdfFromUri=null;
		try {
			UrlRequest urlReq=new UrlRequest(uri, "Accept", CONTENT_TYPES_ACCEPT_HEADER);
			urlReq.setConnectionTimeout(3000);
			urlReq.setSocketTimeout(6000);
			HttpRequest rdfReq = HttpUtil.makeRequest(urlReq);
			readRdfFromUri = readRdf(rdfReq.getResponse());
			return RdfUtil.contains(uri, readRdfFromUri) 
					|| (!rdfReq.getUrl().equals(uri) && RdfUtil.contains(rdfReq.getUrl(), readRdfFromUri)) 
					|| RdfUtil.contains(switchHttps(uri), readRdfFromUri)
					|| (!rdfReq.getUrl().equals(uri) && RdfUtil.contains(switchHttps(rdfReq.getUrl()), readRdfFromUri));
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			return false;
		}
	}
	private static String switchHttps(String uri) {
		return uri.startsWith("http:") ? "https"+uri.substring(4) : "http"+uri.substring(5);
	}

	public static Resource readRdfResourceFromUri(String resourceUri) throws AccessException, InterruptedException, IOException {
		try {
			URI.create(resourceUri);
		} catch (Exception e) {
			try {
				URI.create(resourceUri.trim());
				resourceUri=resourceUri.trim();
			} catch (Exception e2) {
				throw new AccessException(resourceUri, "Invalid URI "+resourceUri, e);
			} 
		}
	
		UrlRequest urlReq=new UrlRequest(resourceUri, "Accept", CONTENT_TYPES_ACCEPT_HEADER);
		urlReq.setConnectionTimeout(2000);
		urlReq.setSocketTimeout(4000);
		HttpRequest rdfReq = HttpUtil.makeRequest(urlReq);
		Model readRdfFromUri = readRdf(rdfReq.getResponse());
		if(RdfUtil.contains(resourceUri, readRdfFromUri) || (!rdfReq.getUrl().equals(resourceUri) && RdfUtil.contains(rdfReq.getUrl(), readRdfFromUri)) ) {
			return readRdfFromUri.createResource(resourceUri);
		}
		throw new AccessException(resourceUri, "Response to dataset RDF resource did not contain a RDF description of the resource");
	}

	public static void writeRdf(Model model, Lang l, OutputStream out) {
		model.write(out, l.getName());
	}		
	public static void writeRdf(Model model, Lang l, Writer out) {
		model.write(out, l.getName());
	}		
	public static byte[] writeRdf(Model model, Lang l) {
		ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
		RdfUtil.writeRdf(model, l, outBytes);
		try {
			outBytes.close();
		} catch (IOException e) { /* does not happen with byte array */ }
		return outBytes.toByteArray();
	}		
	public static String writeRdfToString(Model model, Lang l) {
		StringWriter writer=new StringWriter();
		RdfUtil.writeRdf(model, l, writer);
		try {
			writer.close();
		} catch (IOException e) { /* does not happen with string writer */ }
		return writer.toString();
	}		
	
	public static String statementsToString(Model rdf) {
		return statementsToString(rdf.listStatements());
	}
	
	public static String statementsToString(StmtIterator stms) {
		return statementsToString(stms.toList());
	}
	
	public static String statementsToString(Collection<Statement> stm) {
		StringBuilder sb=new StringBuilder();
		for(Statement st : stm) 
			sb.append(st.toString()).append('\n');
		return sb.toString();
	}
	public static String statementsOfNamespaceToString(Model rdf, String ns) {
		StringBuilder sb=new StringBuilder();
		StmtIterator typeProperties = rdf.listStatements();
		for(Statement st : typeProperties.toList()) 
			if(st.getPredicate().getNameSpace().equals(ns))
				sb.append(st.toString()).append('\n');
		return sb.toString();
	}
	public static String statementsOfNamespaceToString(Resource rdf, String ns) {
		StringBuilder sb=new StringBuilder();
		StmtIterator typeProperties = rdf.listProperties();
		for(Statement st : typeProperties.toList()) 
			if(st.getPredicate().getNameSpace().equals(ns))
				sb.append(st.toString()).append('\n');
		return sb.toString();
	}

	public static Set<Resource> findResourceWithProperties(Model model, Property propA, RDFNode valuePropA,
			Property propB, RDFNode valuePropB) {
		Set<Resource> matching=null;
		StmtIterator stms = model.listStatements(null, propA, valuePropA);
		if(!stms.hasNext())
			return Collections.emptySet();
		matching=new HashSet<>();
		while(stms.hasNext()) {
			Statement st=stms.next();
			matching.add(st.getSubject());
		}
		if(propB==null && valuePropB==null)
			return matching;
		Set<Resource> ret=null;
		for(Resource r: matching) {
			stms = model.listStatements(r, propB, valuePropB);
			while(stms.hasNext()) {
				if(ret==null)
					ret=new HashSet<>();
				Statement st=stms.next();
				ret.add(r);
			}
		}
		return ret==null ? Collections.emptySet() : ret;
	}
	public static Resource findFirstResourceWithProperties(Model model, Property propA, RDFNode valuePropA,
			Property propB, RDFNode valuePropB) {
		Set<Resource> matching=null;
		StmtIterator stms = model.listStatements(null, propA, valuePropA);
		if(!stms.hasNext())
			return null;
		matching=new HashSet<>();
		while(stms.hasNext()) {
			Statement st=stms.next();
			matching.add(st.getSubject());
		}
		for(Resource r: matching) {
			stms = model.listStatements(r, propB, valuePropB);
			if(stms.hasNext()) {
				return r;
			}
		}
		return null;
	}

	public static Model readRdf(HttpResponse rdf) throws RiotException {
		Model model = readRdf(rdf.getBody(), fromMimeType(rdf.getHeader("Content-Type")));
		if (model.size() == 0)
			return null;
		return model;
	}
	public static Model readRdf(HttpResponse rdf, Lang l) throws RiotException {
		Model model = readRdf(rdf.getBody(), l);
		if (model.size() == 0)
			return null;
		return model;
	}

	public static List<Statement> getAllStatementsAboutAndReferingResource(Model model, String resourceUri) {
		ArrayList<Statement> ret=new ArrayList<Statement>(model.getResource(resourceUri).listProperties().toList()); 
		ret.addAll(model.listStatements(null, null, resourceUri).toList());
		return ret;
	}

	public static void writeRdf(StmtIterator listStatements, Lang l, StringWriter w) {
		Model m=Jena.createModel().add(listStatements);
		m.write(w, l.getName());
	}

	public static String writeRdf(StmtIterator listStatements) {
		StringWriter w = new StringWriter();
		Model m=Jena.createModel().add(listStatements);
		m.write(w, Lang.TURTLE.getName());
		return (w.toString());
	}
	public static void printOutRdf(StmtIterator listStatements) {
		System.out.println(writeRdf(listStatements));
	}

	public static void printOutRdf(Model mdl) {
		System.out.println(writeRdf(mdl.listStatements()));
	}

	public static List<Statement> listProperties(Resource ent, Property... properties) {
		List<Statement> stms=new ArrayList<Statement>();
		for(Property p : properties) {
			StmtIterator sameAsStms = ent.listProperties(p);
			for(Statement s: sameAsStms.toList()) 
				stms.add(s);
		}
		return stms;
	}

	public static List<Resource> getTypes(Resource r) {
		List<Resource> ret=new ArrayList<Resource>();
		for(Statement st: r.listProperties(Rdf.type).toList()) {
			if(st.getObject().isURIResource())
				ret.add(st.getObject().asResource());
		}
		return ret;
	}
	
	public static Literal getLabelForResource(Resource from) {
		return getLabelForResource(from, null);
	}
	public static Literal getLabelForResource(Resource from, String language) {
		Literal bestChoice=null;
		for(Statement st : from.listProperties(Rdfs.label).toList()){
			if(!st.getObject().isLiteral()) continue;
			Literal lit = st.getObject().asLiteral();
			if(language==null) {
				return lit;
			} else {
				if(lit.getLanguage().equals(language))
					return lit;
				bestChoice= bestChoice == null ? lit : bestChoice; 
			}
		}
		if(bestChoice!=null)
			return bestChoice;
		for(Statement st : from.listProperties(Skos.prefLabel).toList()){
			if(!st.getObject().isLiteral()) continue;
			Literal lit = st.getObject().asLiteral();
			if(language==null) {
				return lit;
			} else {
				if(lit.getLanguage().equals(language))
					return lit;
				bestChoice= bestChoice == null ? lit : bestChoice; 
			}
		}
		if(bestChoice!=null)
			return bestChoice;
		for(Statement st : from.listProperties(Schemaorg.name).toList()){
			if(!st.getObject().isLiteral()) continue;
			Literal lit = st.getObject().asLiteral();
			if(language==null) {
				return lit;
			} else {
				if(lit.getLanguage().equals(language))
					return lit;
				bestChoice= bestChoice == null ? lit : bestChoice; 
			}
		}
		return bestChoice;
	}

	public static Model readRdf(File rdfFile, Lang lang) throws RiotException, IOException {
		return readRdf(FileUtils.readFileToByteArray(rdfFile), lang);
	}
	
}
