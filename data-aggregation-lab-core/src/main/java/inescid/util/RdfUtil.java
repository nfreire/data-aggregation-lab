package inescid.util;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;


public class RdfUtil {
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
	}
	
	
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
