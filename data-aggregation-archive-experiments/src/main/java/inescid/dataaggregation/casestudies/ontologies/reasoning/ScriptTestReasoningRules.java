package inescid.dataaggregation.casestudies.ontologies.reasoning;

import java.io.File;
import java.io.StringWriter;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.riot.Lang;
import org.apache.jena.tdb2.TDB2Factory;

import inescid.dataaggregation.casestudies.wikidata.RdfRegWikidata;
import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.data.RegRdf;
import inescid.dataaggregation.data.RegRdfs;
import inescid.dataaggregation.data.reasoning.AlignmentReasoner;
import inescid.dataaggregation.data.reasoning.ReasonerUtil;
import inescid.dataaggregation.dataset.Global;
import inescid.util.RdfUtil;
import inescid.util.RdfUtil.Jena;

public class ScriptTestReasoningRules {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if(args==null || args.length<1) {
			System.out.println("Missing parameter: folder for storing data");
			System.exit(0);
		}
		File dataFolder=new File(args[0]);
		String httpCacheFolder = new File(dataFolder, Settings.HTTP_CHACHE_FOLDER).getAbsolutePath();
		String tripleStoreFolder = new File(dataFolder, Settings.TRIPLE_STORE_JOINED_FOLDER).getAbsolutePath();

		Global.init_componentHttpRequestService();
		Global.init_componentDataRepository(httpCacheFolder);

		CachedHttpRequestService rdfCache = new CachedHttpRequestService();
		rdfCache.setRequestRetryAttempts(1);

		final Dataset dataset = TDB2Factory.connectDataset(tripleStoreFolder);
		dataset.begin(ReadWrite.READ);
		
//		final Model modelMdl = dataset.getNamedModel(Settings.WD_REASONING_MODEL_DS);
		final Model modelMdl = dataset.getNamedModel(Settings.WD_REASONING_MODEL_ALIGN_META_DS);
		
		GenericRuleReasoner reasoner = ReasonerUtil
				.instanciateRuleBased("inescid/dataaggregation/casestudies/ontologies/reasoning/test-rdfs-owl-ontologies-rules-small.txt", GenericRuleReasoner.FORWARD_RETE);
//		.instanciateRuleBased("inescid/dataaggregation/casestudies/ontologies/reasoning/test-rdfs-owl-ontologies-rules-small.txt", GenericRuleReasoner.FORWARD_RETE);
//		.instanciateRuleBased("inescid/dataaggregation/casestudies/ontologies/reasoning/test-rdfs-owl-ontologies-rules.txt", GenericRuleReasoner.HYBRID);
		
		Model testMdl = Jena.createModel();
		Resource testSubject = testMdl.createResource("http://nuno.pt/pintura");
//		Jena.createStatementAddToModel(testMdl, RdfRegWikidata.GLAM, RegRdfs.subClassOf, RdfRegWikidata.INSTITUTION);
		Jena.createStatementAddToModel(testMdl, testSubject, RegRdf.type, Jena.createResource("http://wikiba.se/ontology#Entity"));
		Jena.createStatementAddToModel(testMdl, testSubject, RdfRegWikidata.INSTANCE_OF, RdfRegWikidata.GLAM);
//		Jena.createStatementAddToModel(testMdl, "http://nuno.pt/pintura", RdfRegWikidata.INSTANCE_OF, RegSchemaorg.VisualArtwork);
//		Jena.createStatementAddToModel(testMdl, "http://nuno.pt/pintura", RdfRegWikidata.INSTANCE_OF, RegSchemaorg.Person);
//		Jena.createStatementAddToModel(testMdl, "http://nuno.pt/pintura", RdfRegWikidata.INSTANCE_OF, RegSchemaorg.VisualArtwork);
//		Jena.createStatementAddToModel(testMdl, "http://nuno.pt/pintura", RegRdf.type, RegSchemaorg.VisualArtwork);
		
		AlignmentReasoner alignReasoner=new AlignmentReasoner(modelMdl, reasoner);
		
		InfModel infered = alignReasoner.infer(testSubject);
		Model deductionsModel = infered.getDeductionsModel();
		System.out.println("WD ent stms: " + testMdl.size());
		System.out.println("Deduction stms: " + deductionsModel.size());
		System.out.println("################## WD ent model #########################");		
		RdfUtil.printOutRdf(testMdl.listStatements(testSubject, null, (RDFNode) null));
		System.out.println("################## deductions model #########################");		
		RdfUtil.printOutRdf(deductionsModel.listStatements(testSubject, null, (RDFNode) null));
		
//		System.out.println("Reasoning...");
//		InfModel infered = ReasonerUtil.infer(reasoner,modelMdl, testMdl);
//		Model deductionsModel = infered.getDeductionsModel();
//		System.out.println("Deduction stms: " + deductionsModel.size());
//		System.out.println("################## infered model #########################");		
//		StringWriter w;
//		w = new StringWriter();
//		RdfUtil.writeRdf(deductionsModel.listStatements(deductionsModel.createResource(testSubject.getURI()), null, (RDFNode) null), Lang.TURTLE, w);
////		RdfUtil.writeRdf(deductionsModel.listStatements(), Lang.TURTLE, w);
//		System.out.println(w.toString());
////		for(Statement s: deductionsModel.listStatements(testSubject, null, (RDFNode) null).toList()) {	
////			System.out.println(s);
////			for(Derivation d: IteratorUtils.asIterable(infered.getDerivation(s))) {
////				StringWriter w = new StringWriter();
////				d.printTrace(new PrintWriter(w), false);
////				System.out.println(w.toString());
////			}
////		}
//
//		
//		//		StringWriter w;
////		w = new StringWriter();
////		RdfUtil.writeRdf(infered, Lang.TURTLE, w);
////		System.out.println(w.toString());
	}

}
