package inescid.dataaggregation.casestudies.ontologies.reasoning;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.riot.Lang;
import org.apache.jena.tdb2.TDB2Factory;

import inescid.dataaggregation.casestudies.wikidata.RdfRegWikidata;
import inescid.dataaggregation.casestudies.wikidata.ScriptMetadataAnalyzerOfCulturalHeritage;
import inescid.dataaggregation.casestudies.wikidata.SparqlClientWikidata;
import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.crawl.http.HttpResponse;
import inescid.dataaggregation.data.RdfReg;
import inescid.dataaggregation.data.RegOwl;
import inescid.dataaggregation.data.RegRdf;
import inescid.dataaggregation.data.RegRdfs;
import inescid.dataaggregation.data.RegSchemaorg;
import inescid.dataaggregation.data.RegSkos;
import inescid.dataaggregation.data.reasoning.DataModelReasoner;
import inescid.dataaggregation.data.reasoning.ReasonerByRulesAndSchemas;
import inescid.dataaggregation.data.reasoning.ReasonerUtil;
import inescid.dataaggregation.data.reasoning.ReasonerUtil.ReasonerStabilizer;
import inescid.dataaggregation.dataset.Global;
import inescid.util.AccessException;
import inescid.util.RdfUtil;
import inescid.util.RdfUtil.Jena;
import inescid.util.SparqlClient.Handler;

public class ScriptReasoningInLargeOntologiesExperiment {
 	
	public static void main(String[] args) throws Exception {
		final String WD_REASONING_DS = "wikidata-reasoning";

		String httpCacheFolder = "c://users/nfrei/desktop/data/HttpRepository";
		String tripleStoreFolder = "c://users/nfrei/desktop/data/TripleStore";

		Global.init_componentHttpRequestService();
		Global.init_componentDataRepository(httpCacheFolder);

		CachedHttpRequestService rdfCache = new CachedHttpRequestService();
		rdfCache.setRequestRetryAttempts(1);
		
		
		 // Make a TDB-backed dataset
		 final Dataset dataset = TDB2Factory.connectDataset(tripleStoreFolder) ;
//		  dataset.begin(ReadWrite.READ) ;
//		  Model model = dataset.getNamedModel("wikidata-reasoning");
//		  dataset.end();
		
		//load schema.org owl
		//get all statements of subclassof, and subpropertyof
		InputStream systemResourceAsStream = ClassLoader.getSystemResourceAsStream("inescid/dataaggregation/data/reasoning/schemaorg.owl");
		Model schemaorgMdl= RdfUtil.readRdf(systemResourceAsStream);
		systemResourceAsStream.close();

		systemResourceAsStream = ClassLoader.getSystemResourceAsStream("inescid/dataaggregation/data/reasoning/owl_def.owl");
		Model owlMdl= RdfUtil.readRdf(systemResourceAsStream);
		systemResourceAsStream.close();
		
		owlMdl.add(schemaorgMdl.listStatements(null, RegRdfs.subClassOf, (RDFNode) null));
		owlMdl.add(schemaorgMdl.listStatements(null, RegRdfs.subPropertyOf, (RDFNode) null));
		owlMdl.add(schemaorgMdl.listStatements(null, RegRdf.type, RegRdfs.Class));
		owlMdl.add(schemaorgMdl.listStatements(null, RegRdf.type, RegRdf.Property));

	       dataset.begin(ReadWrite.WRITE) ;
	       Model metaModelMdl=dataset.getNamedModel(WD_REASONING_DS);
	       metaModelMdl.add(owlMdl);
	       dataset.end() ;

		
		//		SparqlClientWikidata.query("PREFIX rdf: <"+RegRdf.NS+">\nSELECT ?sub WHERE { ?sub rdf:type wikibase:Property .}", new Handler() {
//			@Override
//			public boolean handleSolution(QuerySolution solution) throws Exception {
//				uris.add(solution.getResource("sub").getURI());
//				return true;
//			}
//		});		
//		SparqlClientWikidata.query("PREFIX rdf: <"+RegRdf.NS+">\nSELECT ?sub WHERE { ?sub wdt:P31 wikibase:Item .}", new Handler() {
//			@Override
//			public boolean handleSolution(QuerySolution solution) throws Exception {
//				uris.add(solution.getResource("sub").getURI());
//				return true;
//			}
//		});
		
		SparqlClientWikidata.query("SELECT ?s ?o WHERE { ?sub <"+RdfRegWikidata.SUBCLASS_OF.getURI()+"> ?o .}", new Handler() {
			@Override
			public boolean handleSolution(QuerySolution solution) throws Exception {
				metaModelMdl.add(Jena.createStatement(solution.getResource("s"), RdfRegWikidata.SUBCLASS_OF, solution.getResource("o")));
				return true;
			}
		});		
//		SparqlClientWikidata.query("PREFIX rdf: <"+RegRdf.NS+">\nSELECT ?sub WHERE { ?sub rdf:type wikibase:Item .}", new Handler() {
//			@Override
//			public boolean handleSolution(QuerySolution solution) throws Exception {
//				uris.add(solution.getResource("sub").getURI());
//				return true;
//			}
//		});		
		System.out.println("Meta model sizes: "+dataset.getDefaultModel().size());
		
		GenericRuleReasoner reasoner = ReasonerUtil.instanciateRdfsBased("inescid/dataaggregation/casestudies/ontologies/reasoning/test-rdfs-extend.txt");
//		GenericRuleReasoner reasoner = ReasonerUtil.instanciateRdfsBased("inescid/dataaggregation/casestudies/ontologies/reasoning/test-rules.txt");

//		Jena.createStatementAddToModel(metaModelMdl, metaModelMdl.createResource("http://wikiba.se/ontology#Property"), RegRdfs.subClassOf, RegRdf.Property);
//		Jena.createStatementAddToModel(metaModelMdl, metaModelMdl.createResource("http://wikiba.se/ontology#Entity"), RegRdfs.subClassOf, RegRdfs.Class)
//		Jena.createStatementAddToModel(metaModelMdl, metaModelMdl.createResource("http://wikiba.se/ontology#Item"), RegRdfs.subClassOf, RegOwl.Thing);
		Jena.createStatementAddToModel(metaModelMdl, RdfRegWikidata.EQUIVALENT_CLASS, RegOwl.equivalentProperty, RegOwl.equivalentClass);
		Jena.createStatementAddToModel(metaModelMdl, RdfRegWikidata.EQUIVALENT_PROPERTY, RegOwl.equivalentProperty, RegOwl.equivalentProperty);
		Jena.createStatementAddToModel(metaModelMdl, RdfRegWikidata.SUBCLASS_OF, RegOwl.equivalentProperty, RegRdfs.subClassOf);;
		Jena.createStatementAddToModel(metaModelMdl, RdfRegWikidata.SUBPROPERTY_OF, RegOwl.equivalentProperty, RegRdfs.subPropertyOf);
		Jena.createStatementAddToModel(metaModelMdl, RdfRegWikidata.INSTANCE_OF, RegOwl.equivalentProperty, RegRdf.type);
		Jena.createStatementAddToModel(metaModelMdl, RdfRegWikidata.NARROWER_EXTERNAL_CLASS, RegOwl.equivalentProperty, RegSkos.narrowMatch);
		Jena.createStatementAddToModel(metaModelMdl, RdfRegWikidata.BROADER_CONCEPT, RegOwl.equivalentProperty, RegSkos.broadMatch);
		Jena.createStatementAddToModel(metaModelMdl, RdfRegWikidata.MAPPING_RELATION_TYPE, RegOwl.equivalentProperty, RegSkos.mappingRelation);
		Jena.createStatementAddToModel(metaModelMdl, RdfRegWikidata.EXTERNAL_SUBPROPERTY, RegOwl.equivalentProperty, RegSkos.broadMatch);//there is no superProperty in rdfs
		Jena.createStatementAddToModel(metaModelMdl, RdfRegWikidata.EXTERNAL_SUPERPROPERTY, RegOwl.equivalentProperty, RegRdfs.subPropertyOf);
		
//		InfModel inferedPre = ReasonerUtil.infer(reasoner, metaModelMdl);
//		metaModelMdl.add(inferedPre.getDeductionsModel().listStatements());
//
//		System.out.println("Metamodel stms: "+metaModelMdl.size());
//		System.out.println("Deduction stms: "+inferedPre.getDeductionsModel().size());
//
//		inferedPre = ReasonerUtil.infer(reasoner, metaModelMdl);
//		metaModelMdl.add(inferedPre.getDeductionsModel().listStatements());
//		
//		System.out.println("Metamodel stms: "+metaModelMdl.size());
//		System.out.println("Deduction stms: "+inferedPre.getDeductionsModel().size());
//		
//		inferedPre = ReasonerUtil.infer(reasoner, metaModelMdl);
//		metaModelMdl.add(inferedPre.getDeductionsModel().listStatements());
//
//		System.out.println("Metamodel stms: "+metaModelMdl.size());
//		System.out.println("Deduction stms: "+inferedPre.getDeductionsModel().size());
//		
//		inferedPre = ReasonerUtil.infer(reasoner, metaModelMdl);
//		metaModelMdl.add(inferedPre.getDeductionsModel().listStatements());
//		
//		System.out.println("Metamodel stms: "+metaModelMdl.size());
//		System.out.println("Deduction stms: "+inferedPre.getDeductionsModel().size());
//		
//		inferedPre=null;

		Model testMdl=Jena.createModel();
		
		testMdl.add(Jena.createStatement("http://nuno.pt/pintura", RegRdf.type, RegSchemaorg.VisualArtwork));

		testMdl.add(Jena.createStatement("http://nuno.pt/pintura", RdfRegWikidata.INSTANCE_OF, RegSchemaorg.Person));
		
		ReasonerStabilizer reasonerStbl=new ReasonerStabilizer(reasoner, metaModelMdl);
		
		Model infered=reasonerStbl.inferNewDeductions(testMdl);
		StringWriter w=new StringWriter();
		RdfUtil.writeRdf(infered, Lang.TURTLE, w);
		System.out.println(w.toString());
		

//		System.out.println("Metamodel stms: "+metaModelMdl.size());
		System.out.println("Deduction stms: "+infered.size());
	}
	
	
}
