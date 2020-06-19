package inescid.dataaggregation.wikidata;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RiotException;

import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Rdfs;
import inescid.dataaggregation.store.Repository;
import inescid.util.AccessException;
import inescid.util.LruCache;
import inescid.util.RdfUtil;

public class WikidataUtil {
	private static Repository dataRepository;	
	private static LruCache<String, Resource> cache=null;
	
	public static void setDataRepository(Repository repo) {
		dataRepository=repo;
	}
	public static void enableMemoryCache(int maxSize) {
		cache=new LruCache<String, Resource>(maxSize);
	}

	public static Resource fetchResource(String wdResourceUri) throws AccessException, InterruptedException, IOException {
		return fetchResource(wdResourceUri, true);
	}
	
	public static Resource fetchResource(String wdResourceUri, boolean addRdfTypesFromP31) throws AccessException, InterruptedException, IOException {
		try {
			if(cache != null) { 
				Resource wdChoRes=cache.get(wdResourceUri);
				if(wdChoRes==null) {
					wdChoRes = RdfUtil.readRdfResourceFromUri(wdResourceUri);
					removeOtherResources(wdChoRes.getModel(), wdResourceUri);
					removeNonTruthyStatements(wdChoRes.getModel());
					if(wdChoRes!=null) 
						cache.put(wdResourceUri, wdChoRes);
				}
				if(addRdfTypesFromP31)
					addRdfTypesFromP31(wdChoRes.getModel());
				return wdChoRes;
			}
			if(dataRepository!=null && dataRepository.contains("wikidata-resource", wdResourceUri)) {
				File file = dataRepository.getFile("wikidata-resource", wdResourceUri);
				Model wdResource = RdfUtil.readRdf(FileUtils.readFileToByteArray(file), Lang.TURTLE);
				if(addRdfTypesFromP31)
					addRdfTypesFromP31(wdResource);
				return wdResource.createResource(wdResourceUri);
			} else { 
				Resource wdResource = RdfUtil.readRdfResourceFromUri(wdResourceUri);
				removeOtherResources(wdResource.getModel(), wdResourceUri);
				removeNonTruthyStatements(wdResource.getModel());
				if(dataRepository!=null)
					dataRepository.save("wikidata-resource", wdResourceUri, RdfUtil.writeRdf(wdResource.getModel(), Lang.TURTLE), "Content-Type",
							Lang.TURTLE.getContentType().getContentType());
				if(addRdfTypesFromP31)
					addRdfTypesFromP31(wdResource.getModel());
				return wdResource;
			}
		} catch (RiotException e) {
			throw new AccessException(wdResourceUri, e);
		}
	}
	
	public static String getLabelFor(String wdResourceUri) throws AccessException, InterruptedException, IOException {
		Resource fetchWdResource = WikidataUtil.fetchResource(wdResourceUri);
		if(fetchWdResource==null)
			return "";
		return getLabelFor(fetchWdResource);
	}
	public static String getLabelFor(Resource wdResource) {
		StmtIterator labelProps = wdResource.listProperties(Rdfs.label);
		String label=null;
		for (Statement st : labelProps.toList()) {
			String lang = st.getObject().asLiteral().getLanguage();
			if(lang.equals("en"))
				return st.getObject().asLiteral().getString();
			if(label==null) 
				label=st.getObject().asLiteral().getString();
		}
		return label;
	}	


	private static void removeOtherResources(Model rdfWikidata, String keepUri) {
		Resource keep = rdfWikidata.createResource(keepUri);
		for (StmtIterator stmts = rdfWikidata.listStatements(); stmts.hasNext();) {
			Statement stm = stmts.next();
			if (!(stm.getSubject().equals(keep) || stm.getObject().equals(keep)))
				stmts.remove();
		}
	}

	private static void removeNonTruthyStatements(Model rdfWikidata) {
		for (StmtIterator stmts = rdfWikidata.listStatements(); stmts.hasNext();) {
			Statement stm = stmts.next();
			if (stm.getPredicate().getNameSpace().startsWith("http://www.wikidata.org/")
					&& !(stm.getPredicate().getNameSpace().startsWith(RdfRegWikidata.NsWd)
							|| stm.getPredicate().getNameSpace().startsWith(RdfRegWikidata.NsWdt)
							|| stm.getPredicate().getNameSpace().startsWith(RdfRegWikidata.NsWdtn)))
				stmts.remove();
		}
	}

	public static void addRdfTypesFromP31(Model rdfWikidata) {
		for (StmtIterator stmts = rdfWikidata.listStatements(null, RdfRegWikidata.INSTANCE_OF, (RDFNode) null); stmts
				.hasNext();) {
			Statement stm = stmts.next();
			rdfWikidata.add(rdfWikidata.createStatement(stm.getSubject(), Rdf.type, stm.getObject()));
		}
	}
	
	public static String convertWdUriToCanonical(String uri) {
		if(uri.startsWith("http://www.wikidata.org/prop") || uri.startsWith("https://www.wikidata.org/wiki")) 
			uri="http://www.wikidata.org/entity/"+uri.substring(uri.lastIndexOf('/')+1);
		if(uri.startsWith("P") || uri.startsWith("Q"))
			uri="http://www.wikidata.org/entity/"+uri;			
		return uri;
	}
	
	public static String toEntityId(String uriOrId) {
		if(uriOrId.indexOf('/')>=0)
			return uriOrId.substring(uriOrId.lastIndexOf('/')+1);
		return uriOrId;
	}
	
	public static String convertWdUriToQueryableUri(String uri) {
		if(uri.startsWith("http://www.wikidata.org/prop") && !uri.startsWith("http://www.wikidata.org/prop/direct/")) 
			uri="http://www.wikidata.org/prop/direct/"+uri.substring(uri.lastIndexOf('/')+1);
		return uri;
	}
	
	

//	public static Resource fetchresource(String resourceUri)
//			throws AccessException, InterruptedException, IOException {
//		RefRes in
//		
//		
//		HttpResponse propFetched = RdfUtil.readRdfFromUri(resourceUri);
//		if (!propFetched.isSuccess()) {
//			throw new AccessException(resourceUri);
//		} else {
//			try {
//				Model rdfWikidata = RdfUtil.readRdf(propFetched.getBody(),
//						RdfUtil.fromMimeType(propFetched.getHeader("Content-Type")));
//				if (rdfWikidata.size() == 0)
//					throw new AccessException(resourceUri, "No data found");
//				Resource wdPropResource = rdfWikidata.getResource(resourceUri);
//				return wdPropResource;
//			} catch (RiotException e) {
//				throw new AccessException(resourceUri);
//			}
//		}
//	}
}
