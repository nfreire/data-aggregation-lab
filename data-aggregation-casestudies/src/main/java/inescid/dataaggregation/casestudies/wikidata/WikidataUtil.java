package inescid.dataaggregation.casestudies.wikidata;

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

import inescid.dataaggregation.data.RegRdf;
import inescid.dataaggregation.store.Repository;
import inescid.util.AccessException;
import inescid.util.RdfUtil;

public class WikidataUtil {
	private static Repository dataRepository;
	
	public static void setDataRepository(Repository repo) {
		dataRepository=repo;
	}

	public static Resource fetchResource(String wdResourceUri) throws AccessException, InterruptedException, IOException {
		try {
			if(dataRepository.contains("wikidata-resource", wdResourceUri)) {
				File file = dataRepository.getFile("wikidata-resource", wdResourceUri);
				Model rdfEdmAtEuropeana = RdfUtil.readRdf(FileUtils.readFileToByteArray(file), Lang.TURTLE);
				return rdfEdmAtEuropeana.createResource(wdResourceUri);
			} else { 
				Resource wdResource = RdfUtil.readRdfResourceFromUri(wdResourceUri);
				removeOtherResources(wdResource.getModel(), wdResourceUri);
				removeNonTruthyStatements(wdResource.getModel());
				addRdfTypesFromP31(wdResource.getModel());
				dataRepository.save("wikidata-resource", wdResourceUri, RdfUtil.writeRdf(wdResource.getModel(), Lang.TURTLE), "Content-Type",
						Lang.TURTLE.getContentType().getContentType());
				return wdResource;
			}
		} catch (RiotException e) {
			throw new AccessException(wdResourceUri, e);
		}
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

	private static void addRdfTypesFromP31(Model rdfWikidata) {
		for (StmtIterator stmts = rdfWikidata.listStatements(null, RdfRegWikidata.INSTANCE_OF, (RDFNode) null); stmts
				.hasNext();) {
			Statement stm = stmts.next();
			rdfWikidata.add(rdfWikidata.createStatement(stm.getSubject(), RegRdf.type, stm.getObject()));
		}
	}
	
	public static String convertWdUriToCanonical(String uri) {
		if(uri.startsWith("http://www.wikidata.org/prop")) 
			uri="http://www.wikidata.org/entity/"+uri.substring(uri.lastIndexOf('/')+1);
		return uri;
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
