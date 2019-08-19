package inescid.dataaggregation.dataset.convert;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Resource;

import inescid.dataaggregation.data.RdfReg;
import inescid.dataaggregation.data.RdfRegEdm;
import inescid.dataaggregation.data.RdfRegRdf;

public class EdmReg {

	
	
	public static final Set<Resource> edmClasses=Collections.unmodifiableSet(new HashSet<Resource>() {
		private static final long serialVersionUID = 1L;
	{
		add(RdfRegEdm.ProvidedCHO); 	
		add(RdfRegEdm.WebResource); 	
		add(RdfRegEdm.Agent); 	
		add(RdfReg.FOAF_ORGANIZATION); 	
		add(RdfRegEdm.TimeSpan); 	
		add(RdfRegEdm.Place); 	
		add(RdfRegEdm.Event); 	
		add(RdfRegEdm.PhysicalThing); 	
		add(RdfReg.ORE_AGGREGATION); 	
		add(RdfReg.SKOS_CONCEPT); 	
		add(RdfReg.SKOS_CONCEPT_SCHEME); 	
		add(RdfReg.CC_LICENSE); 	
		add(RdfReg.SVCS_SERVICE); 	
	}});

	public static boolean isEdmClass(Resource object) {
		return edmClasses.contains(object);
	}

	public static final Map<String, String> nsPrefixes=Collections.unmodifiableMap(new HashMap<String, String>(){
		private static final long serialVersionUID = 1L;
	{
		put(RdfReg.NsCc, "cc");
		put(RdfReg.NsDc, "dc");
		put(RdfReg.NsDcterms, "dcterms");
		put(RdfRegEdm.NS, RdfRegEdm.PREFIX);
		put(RdfReg.NsOre, "ore");
		put(RdfRegRdf.NS, RdfRegRdf.PREFIX);
		put(RdfReg.NsRdfs, "rdfs");
		put(RdfReg.NsSkos, "skos");
		put(RdfReg.NsSvcs, "svcs");
		put(RdfReg.NsDoap, "doap");
		put(RdfReg.NsWgs84, "wgs84_pos");
		put(RdfReg.NsOwl, "owl");
		put(RdfReg.NsRdaGr2, "rdaGr2");
		put(RdfReg.NsFoaf, "foaf");
//		put("cc", RdfReg.NsCc);
//		put("dc", RdfReg.NsDc);
//		put("dcterms", RdfReg.NsDcterms);
//		put("edm", RdfReg.NsEdm);
//		put("ore", RdfReg.NsOre);
//		put("rdf", RdfReg.NsRdf);
//		put("rdfs", RdfReg.NsRdfs);
//		put("skos", RdfReg.NsSkos);
//		put("svcs", RdfReg.NsSvcs);
//		put("doap", RdfReg.NsDoap);
//		put("wgs84_pos", RdfReg.NsWgs84);
//		put("owl", RdfReg.NsOwl);
//		put("rdaGr2", RdfReg.NsRdaGr2);
//		put("foaf", RdfReg.NsFoaf);
	}});
}
