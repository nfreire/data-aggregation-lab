package inescid.dataaggregation.wikidata;

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
	
	public static final Property NARROWER_EXTERNAL_CLASS = ResourceFactory.createProperty(NsWdt+"P3950");
	public static final Property BROADER_CONCEPT = ResourceFactory.createProperty(NsWdt+"P4900");
	public static final Property MAPPING_RELATION_TYPE = ResourceFactory.createProperty(NsWdt+"P4390");
	public static final Property EXTERNAL_SUPERPROPERTY = ResourceFactory.createProperty(NsWdt+"P2235");
	public static final Property EXTERNAL_SUBPROPERTY = ResourceFactory.createProperty(NsWdt+"P2236");
	public static final Property COORDINATE_LOCATION = ResourceFactory.createProperty(NsWdt+"P625");
	
	public static final Property EQUIVALENT_PROPERTY = ResourceFactory.createProperty(NsWdt+"P1628");
	public static final Property EQUIVALENT_CLASS = ResourceFactory.createProperty(NsWdt+"P1709");
	public static final Property EXACT_MATCH = ResourceFactory.createProperty(NsWdt+"P2888");

	public static final Resource INSTITUTION = ResourceFactory.createResource(NsWd+"Q178706");
	public static final Resource GLAM = ResourceFactory.createResource(NsWd+"Q1030034");

	public static final Property RIGHTS_STATEMENT  = ResourceFactory.createProperty(NsWdt+"P6426");
	public static final Resource CREATIVE_WORK = ResourceFactory.createResource(NsWd+"Q178706");

	public static final Property IMAGE = ResourceFactory.createProperty(NsWdt+"P18");
	public static final Property ICON = ResourceFactory.createProperty(NsWdt+"P2910");
	public static final Property LOGO_IMAGE = ResourceFactory.createProperty(NsWdt+"P154");
	public static final Property COAT_OF_ARMS = ResourceFactory.createProperty(NsWdt+"P94");
	public static final Property SEAL_IMAGE = ResourceFactory.createProperty(NsWdt+"P158");
	public static final Property FLAG_IMAGE = ResourceFactory.createProperty(NsWdt+"P41");
	public static final Property COMEMORATIVE_PLAQUE = ResourceFactory.createProperty(NsWdt+"P1801");
	public static final Property PLACE_NAME_SIGN = ResourceFactory.createProperty(NsWdt+"P");
	public static final Property MONOGRAM = ResourceFactory.createProperty(NsWdt+"P1543");
	public static final Property IMAGE_OF_TOMBSTONE = ResourceFactory.createProperty(NsWdt+"P1442");
	public static final Property SIGNATURE = ResourceFactory.createProperty(NsWdt+"P109");
	public static final Property COLLAGE_IMAGE = ResourceFactory.createProperty(NsWdt+"P2716");
	public static final Property SECTIONAL_VIEW = ResourceFactory.createProperty(NsWdt+"P2715");
	public static final Property NIGHTTIME_VIEW = ResourceFactory.createProperty(NsWdt+"P3451");
	public static final Property PANORAMA_VIEW = ResourceFactory.createProperty(NsWdt+"P4291");
	public static final Property PHOTOSHERE_IMAGE = ResourceFactory.createProperty(NsWdt+"P4640");
	public static final Property WINTER_VIEW = ResourceFactory.createProperty(NsWdt+"P5252");
	public static final Property IMAGE_OF_INTERIOR = ResourceFactory.createProperty(NsWdt+"P5775");
}
