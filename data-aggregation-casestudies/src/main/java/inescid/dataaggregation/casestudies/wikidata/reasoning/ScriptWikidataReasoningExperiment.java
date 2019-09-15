package inescid.dataaggregation.casestudies.wikidata.reasoning;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Consumer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;

import inescid.dataaggregation.casestudies.wikidata.RdfRegWikidata;
import inescid.dataaggregation.casestudies.wikidata.ScriptMetadataAnalyzerOfCulturalHeritage;
import inescid.dataaggregation.casestudies.wikidata.SparqlClientWikidata;
import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.crawl.http.HttpResponse;
import inescid.dataaggregation.data.RdfReg;
import inescid.dataaggregation.data.RegOwl;
import inescid.dataaggregation.data.RegRdf;
import inescid.dataaggregation.data.RegRdfs;
import inescid.dataaggregation.data.RegSkos;
import inescid.dataaggregation.data.reasoning.DataModelReasoner;
import inescid.dataaggregation.data.reasoning.ReasonerByRulesAndSchemas;
import inescid.dataaggregation.dataset.Global;
import inescid.util.AccessException;
import inescid.util.RdfUtil;
import inescid.util.RdfUtil.Jena;

public class ScriptWikidataReasoningExperiment {
 	
	public static void main(String[] args) throws Exception {
		String httpCacheFolder = "c://users/nfrei/desktop/data/HttpRepository";

		Global.init_componentHttpRequestService();
		Global.init_componentDataRepository(httpCacheFolder);

		CachedHttpRequestService rdfCache = new CachedHttpRequestService();
		rdfCache.setRequestRetryAttempts(1);
				
		ScriptMetadataAnalyzerOfCulturalHeritage.Files.init(new File("c://users/nfrei/desktop/data/wikidata-results-latest"));
		File wdChoProfileCsv = ScriptMetadataAnalyzerOfCulturalHeritage.Files.wdChoProfile;
		File wdMetamodelProfileCsv = ScriptMetadataAnalyzerOfCulturalHeritage.Files.schemaOrgEquivalences;
		
		//iterate all model resources and reason to get the schema.org alignments
		HashSet<String> modelUris=new HashSet<String>();
		CSVParser parser=new CSVParser(new FileReader(wdMetamodelProfileCsv), CSVFormat.DEFAULT);
		parser.forEach(rec -> {
			if(rec.size()>0) {
				String fld=rec.get(0);
				if(fld.startsWith("http://www.wikidata")) 
					{ modelUris.add(fld); 
//					System.out.println(fld);
					}
				
			}});
		parser.close();
		
		HashSet<String> choUris=new HashSet<String>();
		parser=new CSVParser(new FileReader(wdChoProfileCsv), CSVFormat.DEFAULT);
		parser.forEach(rec -> {
			if(rec.size()>0) {
				String fld=rec.get(0);
				if(fld.startsWith("http://www.wikidata")) choUris.add(fld);
			}});
		parser.close();
		
		Model schemasModel = null;
		{//setup the metamodel reasoner
			ReasonerByRulesAndSchemas reasoner=new ReasonerByRulesAndSchemas();
			reasoner.loadSchemas("inescid/dataaggregation/data/reasoning/wikibase.owl", "inescid/dataaggregation/data/reasoning/owl_def.owl");
	//		reasoner.loadSchemas("inescid/dataaggregation/data/reasoning/wikibase.owl", "inescid/dataaggregation/data/reasoning/owl_def.owl", "inescid/dataaggregation/data/reasoning/schemaorg.owl", "inescid/dataaggregation/data/reasoning/edm.owl");
			
			Model wikidataSchemaAlignments=Jena.createModel();
			Jena.createStatementAddToModel(wikidataSchemaAlignments, wikidataSchemaAlignments.createResource("http://wikiba.se/ontology#Property"), RegRdfs.subClassOf, RegRdf.Property);
			Jena.createStatementAddToModel(wikidataSchemaAlignments, wikidataSchemaAlignments.createResource("http://wikiba.se/ontology#Entity"), RegRdfs.subClassOf, RegRdfs.Class);
			Jena.createStatementAddToModel(wikidataSchemaAlignments, wikidataSchemaAlignments.createResource("http://wikiba.se/ontology#Entity"), RegRdfs.subClassOf, RegOwl.Thing);
			Jena.createStatementAddToModel(wikidataSchemaAlignments, RdfRegWikidata.EQUIVALENT_CLASS, RegOwl.equivalentProperty, RegOwl.equivalentClass);
			Jena.createStatementAddToModel(wikidataSchemaAlignments, RdfRegWikidata.EQUIVALENT_PROPERTY, RegOwl.equivalentProperty, RegOwl.equivalentProperty);
			Jena.createStatementAddToModel(wikidataSchemaAlignments, RdfRegWikidata.SUBCLASS_OF, RegOwl.equivalentProperty, RegRdfs.subClassOf);;
			Jena.createStatementAddToModel(wikidataSchemaAlignments, RdfRegWikidata.SUBPROPERTY_OF, RegOwl.equivalentProperty, RegRdfs.subPropertyOf);
			Jena.createStatementAddToModel(wikidataSchemaAlignments, RdfRegWikidata.INSTANCE_OF, RegOwl.equivalentProperty, RegRdf.type);
			Jena.createStatementAddToModel(wikidataSchemaAlignments, RdfRegWikidata.NARROWER_EXTERNAL_CLASS, RegOwl.equivalentProperty, RegSkos.narrowMatch);
			Jena.createStatementAddToModel(wikidataSchemaAlignments, RdfRegWikidata.BROADER_CONCEPT, RegOwl.equivalentProperty, RegSkos.broadMatch);
			Jena.createStatementAddToModel(wikidataSchemaAlignments, RdfRegWikidata.MAPPING_RELATION_TYPE, RegOwl.equivalentProperty, RegSkos.mappingRelation);
			Jena.createStatementAddToModel(wikidataSchemaAlignments, RdfRegWikidata.EXTERNAL_SUBPROPERTY, RegOwl.equivalentProperty, RegSkos.broadMatch);//there is no superProperty in rdfs
			Jena.createStatementAddToModel(wikidataSchemaAlignments, RdfRegWikidata.EXTERNAL_SUPERPROPERTY, RegOwl.equivalentProperty, RegRdfs.subPropertyOf);
			reasoner.loadSchemas(wikidataSchemaAlignments);
			
			reasoner.initReasoner();
			InfModel inferedModel = reasoner.infer();
			
			RdfUtil.writeRdf(inferedModel, Lang.TURTLE, new FileOutputStream("c:/users/nfrei/desktop/ontologies-infered.ttl"));
			System.out.println("EXPORTED");
			
			Model wdModelRdf = Jena.createModel();
			wdModelRdf.add(inferedModel);
			inferedModel.close();
			reasoner=null;
			System.out.println("ADDED infered");
			
			modelUris.forEach(uri -> {
				try {
					HttpResponse recResponse = rdfCache.fetchRdf(uri);
					if(recResponse.isSuccess()) {
						Model readRdf = RdfUtil.readRdf(recResponse);
						if(readRdf!=null)
							 wdModelRdf.add(readRdf);
					}
				} catch (AccessException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

			System.out.println("ADDED WDs");
			System.out.println(wdModelRdf.size()+ " stmts");

			DataModelReasoner reasonerDm=new DataModelReasoner(); 
			InfModel infered = reasonerDm.infer(wdModelRdf);
			System.out.println("INFERED");
			
			System.out.println(infered.getDeductionsModel().size());
//			reasoner.initReasoner();
//			schemasModel = reasoner.getSchemasModel();
		}		
//		ReasonerByRulesAndSchemas reasonerChos=new ReasonerByRulesAndSchemas();
//		reasonerChos.setRulesClasspath("inescid/dataaggregation/casestudies/wikidata/reasoning/WikidataModelReasoner-rules.txt");
//		reasonerChos.loadSchemas(schemasModel);
//		reasonerChos.initReasoner();
//		
//		choUris.forEach(uri -> {
//			try {
//				HttpResponse recResponse = rdfCache.fetchRdf(uri);
//				if(recResponse.isSuccess()) {
//					Model readRdf = RdfUtil.readRdf(recResponse);
//					if(readRdf==null) return;
//					InfModel inferedModel = reasonerChos.infer(readRdf);
//					System.out.println("### INFERED ### "+uri);
////					System.out.println( RdfUtil.statementsToString(inferedModel.getDeductionsModel()));
//					StringBuilder sb=new StringBuilder();
//					for(Statement st : inferedModel.getDeductionsModel().listStatements().toList()) {
////						System.err.println(st);
//						if(st.getSubject().getURI()!=null && st.getSubject().getURI().startsWith(uri)
//								&& !(st.getPredicate().equals(RdfRegRdf.type) && st.getObject().isAnon())) 
////							if(st.getSubject().getURI()!=null && 
////							!st.getSubject().getURI().startsWith("http://www.w3.org/2002/07/owl") &&
////							!st.getSubject().getURI().startsWith("http://www.w3.org/2000/01/rdf-schema") &&
////							!st.getSubject().getURI().startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns"))
//						sb.append(st.toString()).append('\n');
//					}
//					System.out.println(sb.toString());	
//				}
//			} catch (AccessException e) {
//				e.printStackTrace();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}			
//		});
//		parser.close();		
	}
	
	
}
