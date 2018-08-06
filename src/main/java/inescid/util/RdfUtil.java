package inescid.util;

import org.apache.http.entity.ContentType;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RIOT;
import org.apache.jena.riot.lang.RiotParsers;
import org.apache.jena.riot.system.RiotChars;
import org.apache.jena.util.iterator.ExtendedIterator;


public class RdfUtil {
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

	public static String getUriOrId(Resource srcResource) {
		return srcResource.isURIResource() ? srcResource.getURI() : srcResource.getId().getBlankNodeId().toString();
	}

	public static String getUriOrLiteralValue(Resource resource) {
		return resource.isURIResource() ? resource.getURI() : (resource.isLiteral() ? resource.asLiteral().getString() : null);
	}
	
	public static Lang fromMimeType(String mimeType) {
		return RDFLanguages.contentTypeToLang(mimeType);
	}
	
}
