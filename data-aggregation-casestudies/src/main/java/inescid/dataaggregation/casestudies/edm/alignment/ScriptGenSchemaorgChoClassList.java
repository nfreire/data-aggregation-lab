package inescid.dataaggregation.casestudies.edm.alignment;

import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;

import inescid.dataaggregation.casestudies.wikidata.edm.WikidataEdmMappings;
import inescid.dataaggregation.data.RdfsClassHierarchy;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.RdfReg;
import inescid.dataaggregation.data.model.Schemaorg;
import inescid.dataaggregation.data.model.Skos;
import inescid.util.RdfUtil;

public class ScriptGenSchemaorgChoClassList {
	
	
	public static void main(String[] args) throws Exception {
//		InputStream schemaorgOwlIs = ScriptGenSchemaorgChoClassList.class.getClassLoader().getResourceAsStream("schemaorg.owl");
		InputStream schemaorgOwlIs = new FileInputStream("../data-aggregation-lab-core/src/main/resources/owl/schemaorg.owl");
		RdfsClassHierarchy schemaClassHierarchy = new RdfsClassHierarchy(RdfUtil.readRdf(schemaorgOwlIs, Lang.RDFXML));
		schemaorgOwlIs.close();
		
		
		for(Resource r: new Resource[] {Schemaorg.CreativeWork, Schemaorg.Person, Schemaorg.Organization 
				, Schemaorg.AudioObject, Schemaorg.ImageObject, Schemaorg.WebPage, Schemaorg.MediaObject
				, Schemaorg.Place}) {
			
			System.out.println();
			System.out.println("### "+r.getURI());
			
			
			int i=0;
			for(String sc: schemaClassHierarchy.getSubClassesOf(r)) {
				i++;
				System.out.print("\""+sc+"\", ");
				if(i%3 ==0)
					System.out.println();
			}
		}
		
		
		
	}
	

	
	
	
}
