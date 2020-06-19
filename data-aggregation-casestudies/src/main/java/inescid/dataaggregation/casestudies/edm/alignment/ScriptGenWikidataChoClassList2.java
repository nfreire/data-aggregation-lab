package inescid.dataaggregation.casestudies.edm.alignment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;

import inescid.dataaggregation.casestudies.wikidata.edm.WikidataEdmConverter;
import inescid.dataaggregation.casestudies.wikidata.edm.WikidataEdmMappings;
import inescid.dataaggregation.casestudies.wikidata.edm.ScriptEvaluateWikidataEdmConverter.Const;
import inescid.dataaggregation.casestudies.wikidata.edm.WikidataEdmConverter.AggregationPropertiesConversion;
import inescid.dataaggregation.data.DataModelRdfOwl;
import inescid.dataaggregation.data.RdfsClassHierarchy;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;
import inescid.dataaggregation.data.model.RdfReg;
import inescid.dataaggregation.data.model.Schemaorg;
import inescid.dataaggregation.data.model.Skos;
import inescid.dataaggregation.dataset.convert.SchemaOrgToEdmClassMappings;
import inescid.dataaggregation.wikidata.SparqlClientWikidata;
import inescid.util.AccessException;
import inescid.util.RdfUtil;
import inescid.util.SparqlClient;
import inescid.util.TriplestoreJenaTbd2;
import inescid.util.RdfUtil.Jena;
import inescid.util.SparqlClient.Handler;

public class ScriptGenWikidataChoClassList2 {
	
	
	public static void main(String[] args) throws Exception {
		File csvFileClasses=new File("../data-aggregation-lab-core/src/main/resources/", "wikidata/wikidata_edm_mappings_classes.csv");
		File csvFileProperties=new File("../data-aggregation-lab-core/src/main/resources/", "wikidata/wikidata_edm_mappings.csv");
		File csvFileHierarchy=new File("../data-aggregation-lab-core/src/main/resources/", "wikidata/wikidata_edm_mappings_hierarchy.csv");
		File wikidataTbd2Folder=new File("C:\\Users\\nfrei\\Desktop\\data\\wikidata-to-edm\\triplestore-wikidata");
		WikidataEdmMappings mapping=new WikidataEdmMappings(csvFileClasses, csvFileProperties, csvFileHierarchy, wikidataTbd2Folder);

		for(Resource edmCls: new Resource[] {Edm.Agent, Edm.Place, Edm.TimeSpan, Skos.Concept, Edm.WebResource, Edm.ProvidedCHO, Ore.Aggregation}) {
			System.out.println(edmCls.getURI());
			for(String wdCls : mapping.getClassesMappedTo(edmCls.getURI())) {
				System.out.print("\""+wdCls+"\",");
			}
			System.out.println();
		}
		
	}
	

	
	
	
}
