package inescid.dataaggregation.casestudies.wikidata;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class RdfRegWikidata {
	public static String NsBd="http://www.bigdata.com/rdf#";
	public static String NsWikibase="http://wikiba.se/ontology#";
	public static String NsWdt="http://www.wikidata.org/prop/direct/";
	public static String NsWdtn="http://www.wikidata.org/prop/direct-normalized/";
	public static String NsWd="http://www.wikidata.org/entity/";

	public static final Property IIIF_MANIFEST = ResourceFactory.createProperty(NsWdt+"P6108");
	public static final Property EUROPEANAID = ResourceFactory.createProperty(NsWdt+"P727");
//	public static final Resource PROPERTY = ResourceFactory.createResource(NsOre+"Proxy");
//	public static final Resource ENTITY = ResourceFactory.createResource(NsOre+"Proxy");
	public static final Property COLLECTION = ResourceFactory.createProperty(NsWdt+"P195");
	public static final Property INSTANCE_OF = ResourceFactory.createProperty(NsWdt+"P31");
	public static final Property SUBCLASS_OF = ResourceFactory.createProperty(NsWdt+"P279");
	public static final Property SUBPROPERTY_OF = ResourceFactory.createProperty(NsWdt+"P1647");
	
	public static final Property EQUIVALENT_PROPERTY = ResourceFactory.createProperty(NsWdt+"P1628");
	public static final Property EQUIVALENT_CLASS = ResourceFactory.createProperty(NsWdt+"P1709");
	public static final Property EXACT_MATCH = ResourceFactory.createProperty(NsWdt+"P2888");

	public static final Resource INSTITUTION = ResourceFactory.createResource(NsWd+"Q178706");
	public static final Resource GLAM = ResourceFactory.createResource(NsWd+"Q1030034");

	public static final Property RIGHTS_STATEMENT  = ResourceFactory.createProperty(NsWdt+"P6426");
	public static final Resource CREATIVE_WORK = ResourceFactory.createResource(NsWd+"Q178706");
}
