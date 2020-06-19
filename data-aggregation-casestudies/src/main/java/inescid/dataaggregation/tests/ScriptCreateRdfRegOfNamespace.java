package inescid.dataaggregation.tests;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;

import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.dataset.Global;
import inescid.util.RdfUtil;

public class ScriptCreateRdfRegOfNamespace {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		http://www.wikidata.org/entity.json
		try { 
			Global.init_developement();
			String nsLocation = null;

			String name="Ebucore";
			String ns = "http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#";

			
//			public static String NsDcmiType="http://purl.org/dc/dcmitype/";
//			public static String NsCc="http://creativecommons.org/ns#";
//			public static String NsSvcs="http://rdfs.org/sioc/services#";
//			public static String NsDoap="http://usefulinc.com/ns/doap#";
//			public static String NsOwl="http://www.w3.org/2002/07/owl#";
//			public static String NsRdaGr2="http://rdvocab.info/ElementsGr2/"; 
//			public static String NsFoaf="http://xmlns.com/foaf/0.1/";
//			public static String NsVcard="http://www.w3.org/2006/vcard/ns#";
//			public static String NsSchemaOrg="http://schema.org/";
//			public static String NsVoid="http://rdfs.org/ns/void#";
//			public static String NsDqv="http://www.w3.org/ns/dqv#";
//			public static String NsOa="http://www.w3.org/ns/oa#";
//			public static String NsDcat="http://www.w3.org/ns/dcat#";
//			public static String NsProv="http://www.w3.org/ns/prov#";
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			PrintStream o = new PrintStream(os);
			o.printf("package inescid.dataaggregation.data.model;\r\n" + 
					"\r\n" + 
					"import org.apache.jena.rdf.model.Property;\r\n" + 
					"import org.apache.jena.rdf.model.Resource;\r\n" + 
					"import org.apache.jena.rdf.model.ResourceFactory;\r\n" + 
					"\r\n" + 
					"public final class %s {\r\n" + 
					"	public static String PREFIX=\"%s\";\r\n" +
					"	public static String NS=\"%s\";\r\n\r\n", name, name.toLowerCase(), ns);
			
			Model rdf = null;
			if(nsLocation==null)
				rdf = RdfUtil.readRdfFromUri(ns);
			else
				rdf = RdfUtil.readRdfFromUri(nsLocation);
			for (ResIterator listSubjects = rdf.listSubjects() ; listSubjects.hasNext() ; ) {
				Resource r = listSubjects.nextResource();
				String uri = r.getURI();
				if(uri!=null && uri.startsWith(ns) && !uri.equals(ns)) {
					String elName=uri.substring(ns.length());
					if(Character.isUpperCase(elName.charAt(0)))
						o.printf("	public static final Resource %s = ResourceFactory.createResource(\"%s\");\r\n", elName, uri); 
					else
						o.printf("	public static final Property %s = ResourceFactory.createProperty(\"%s\");\r\n", elName, uri); 
				}
			}
			o.print("}");
			System.out.println(new String(os.toByteArray(), "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		Global.shutdown();
	}

}
