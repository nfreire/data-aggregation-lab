package inescid.dataaggregation.casestudies.edm.alignment;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;

import inescid.dataaggregation.casestudies.wikidata.edm.WikidataEdmMappings;
import inescid.dataaggregation.data.RdfsClassHierarchy;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.RdfReg;
import inescid.dataaggregation.data.model.Schemaorg;
import inescid.dataaggregation.data.model.Skos;
import inescid.dataaggregation.dataset.convert.SchemaOrgToEdmClassMappings;
import inescid.dataaggregation.wikidata.SparqlClientWikidata;
import inescid.util.RdfUtil;
import inescid.util.SparqlClient;
import inescid.util.TriplestoreJenaTbd2;
import inescid.util.SparqlClient.Handler;

public class ScriptGenWikidataChoClassList {
	
	
	public static void main(String[] args) throws Exception {
		
//		File wikidataTbd2Folder=new File("c://users/nfrei/desktop/data/wikidata-to-edm/triplestore-wikidata");
//		
//		SparqlClient sparqlCl= new TriplestoreJenaTbd2(wikidataTbd2Folder, SparqlClientWikidata.PREFFIXES, ReadWrite.READ);
//
//		SchemaOrgToEdmClassMappings schemaOrgToEdmClassMappings=new SchemaOrgToEdmClassMappings();
//		
//		InputStream schemaorgOwlIs = new FileInputStream("../data-aggregation-lab-core/src/main/resources/owl/schemaorg.owl");
//		RdfsClassHierarchy schemaClassHierarchy = new RdfsClassHierarchy(RdfUtil.readRdf(schemaorgOwlIs, Lang.RDFXML));
//		schemaorgOwlIs.close();
//	
//		//Equivalent Classes
//		sparqlCl.query("select ?s (<http://www.wikidata.org/prop/direct/P1709> AS ?p) ?o WHERE { ?s <http://www.wikidata.org/prop/direct/P1709> ?o. }", new Handler() {
//			public boolean handleSolution(QuerySolution solution) throws Exception {
//				String wdClsUri = solution.getResource("s").getURI();
//				String edmType = getEdmType(wdClsUri);
//				if(edmType==null) {
//					String equivalentClsUri = solution.getResource("o").getURI();
//					ArrayList<String> superClassesOf = new ArrayList<String>(schemaClassHierarchy.getSuperClassesOf(equivalentClsUri));
//					superClassesOf.add(0, equivalentClsUri);
//					for(String eqCls : superClassesOf) {
//						edmType=schemaOrgToEdmClassMappings.GetClassMappping().get(eqCls);
//						if(edmType!=null) {
//							edmClassMappings.putClassMapping(wdClsUri, edmType);
////							System.out.println("Found mapping via schema.org equivalence: "+ wdClsUri+" "+equivalentClsUri+" "+edmType);
//							return true;
//						}
//					}
//				}
//				return true;
//			}
//		});
//		
//		//Equivalent Properties
//		sparqlCl.query("select ?s (<http://www.wikidata.org/prop/direct/P1628> AS ?p) ?o WHERE { ?s <http://www.wikidata.org/prop/direct/P1628> ?o. }", new Handler() {
//			public boolean handleSolution(QuerySolution solution) throws Exception {
//				String wdPropUri = solution.getResource("s").getURI();
//				String schemaPropUri = solution.getResource("o").getURI();
//				if(!edmClassMappings.getAllPropertiesMapped().contains(wdPropUri)) { 
//					ArrayList<String> superPropsOf = new ArrayList<String>(schemaClassHierarchy.getSuperPropertiesOf(schemaPropUri));
//					superPropsOf.add(0, schemaPropUri);
//					for(String eqProp : superPropsOf) {
//						if(schemaOrgToEdmClassMappings.getAllPropertiesMapped().contains(eqProp)) {
//							for(String schemaClsUri : schemaOrgToEdmClassMappings.GetClassMappping().keySet()) {
//								String edmClsUri=schemaOrgToEdmClassMappings.GetClassMappping().get(schemaClsUri);
//								String edmPropUri=schemaOrgToEdmClassMappings.get(edmClsUri, eqProp);
//								if(edmPropUri!=null) {
//									edmClassMappings.put(edmClsUri, wdPropUri, edmPropUri);
////									System.out.println("Found mapping via schema.org equivalence: "+ eqProp+": "+edmClsUri+" "+wdPropUri+" "+edmPropUri);
//									return true;
//								}
//							}
//						}
//					}
//				}
//				return true;
//			}
//		});
//		
//		
////		InputStream schemaorgOwlIs = ScriptGenSchemaorgChoClassList.class.getClassLoader().getResourceAsStream("schemaorg.owl");
//		InputStream schemaorgOwlIs = new FileInputStream("../data-aggregation-lab-core/src/main/resources/owl/schemaorg.owl");
//		RdfsClassHierarchy schemaClassHierarchy = new RdfsClassHierarchy(RdfUtil.readRdf(schemaorgOwlIs, Lang.RDFXML));
//		schemaorgOwlIs.close();
//		
//		
//		for(Resource r: new Resource[] {Schemaorg.CreativeWork, Schemaorg.Person, Schemaorg.Organization 
//				, Schemaorg.AudioObject, Schemaorg.ImageObject, Schemaorg.WebPage, Schemaorg.MediaObject
//				, Schemaorg.Place}) {
//			
//			System.out.println();
//			System.out.println("### "+r.getURI());
//			
//			
//			int i=0;
//			for(String sc: schemaClassHierarchy.getSubClassesOf(r)) {
//				i++;
//				System.out.print("\""+sc+"\", ");
//				if(i%3 ==0)
//					System.out.println();
//			}
//		}
//		
//		
		
	}
	

	
	
	
}
