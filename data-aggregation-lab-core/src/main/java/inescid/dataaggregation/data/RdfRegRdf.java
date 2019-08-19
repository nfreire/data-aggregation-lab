package inescid.dataaggregation.data;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public final class RdfRegRdf {
	public static String NS="http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static String PREFIX="rdf";

	public static final Property type = ResourceFactory.createProperty(NS+"type");
	public static final Property value = ResourceFactory.createProperty(NS+"value");
	public static final Property first = ResourceFactory.createProperty(NS+"first");
	public static final Property rest = ResourceFactory.createProperty(NS+"rest");
	public static final Property subject = ResourceFactory.createProperty(NS+"subject");
	public static final Property predicate = ResourceFactory.createProperty(NS+"predicate");
	public static final Property object = ResourceFactory.createProperty(NS+"object");

	public static final Resource Property = ResourceFactory.createResource(NS+"Property");
	public static final Resource Statement = ResourceFactory.createResource(NS+"Statement");
	
}