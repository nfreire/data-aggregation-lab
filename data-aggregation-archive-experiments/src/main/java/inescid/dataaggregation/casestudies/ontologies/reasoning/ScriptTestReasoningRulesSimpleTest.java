package inescid.dataaggregation.casestudies.ontologies.reasoning;

import java.io.InputStream;
import java.io.StringWriter;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.riot.Lang;

import inescid.dataaggregation.casestudies.wikidata.RdfRegWikidata;
import inescid.dataaggregation.data.RegOwl;
import inescid.dataaggregation.data.RegRdf;
import inescid.dataaggregation.data.RegRdfs;
import inescid.dataaggregation.data.RegSchemaorg;
import inescid.dataaggregation.data.RegSkos;
import inescid.dataaggregation.data.reasoning.ReasonerUtil;
import inescid.util.RdfUtil;
import inescid.util.RdfUtil.Jena;

public class ScriptTestReasoningRulesSimpleTest {

	public static void main(String[] args) throws Exception {
		
		GenericRuleReasoner reasoner = ReasonerUtil
				.instanciateRuleBased("inescid/dataaggregation/casestudies/ontologies/reasoning/test-rdfs-owl-ontologies-rules-small.txt");
//		.instanciateRuleBased("inescid/dataaggregation/casestudies/ontologies/reasoning/test-rdfs-owl-ontologies-rules.txt");
		
//
//		systemResourceAsStream = ClassLoader
//				.getSystemResourceAsStream("inescid/dataaggregation/data/reasoning/owl_def.owl");
//		Model owlMdl = RdfUtil.readRdf(systemResourceAsStream);
//		systemResourceAsStream.close();
//		modelMdl.add(owlMdl.listStatements(null, RegRdfs.subClassOf, (RDFNode) null));
//		modelMdl.add(owlMdl.listStatements(null, RegRdfs.subPropertyOf, (RDFNode) null));
//		modelMdl.add(owlMdl.listStatements(null, RegRdf.type, RegRdfs.Class));
//		modelMdl.add(owlMdl.listStatements(null, RegRdf.type, RegRdf.Property));

		Model metaAlignMdl = Jena.createModel();
		InputStream systemResourceAsStream = ClassLoader
				.getSystemResourceAsStream("inescid/dataaggregation/data/reasoning/schemaorg.owl");
		Model schemaorgMdl = RdfUtil.readRdf(systemResourceAsStream);
		systemResourceAsStream.close();
		System.out.println("Schema.org.owl triples:"+schemaorgMdl.size());
		
		metaAlignMdl.add(schemaorgMdl.listStatements(null, RegRdfs.subClassOf, (RDFNode) null));
		metaAlignMdl.add(schemaorgMdl.listStatements(null, RegRdfs.subPropertyOf, (RDFNode) null));
//		metaAlignMdl.add(schemaorgMdl.listStatements(null, RegRdf.type, RegRdfs.Class));
//		metaAlignMdl.add(schemaorgMdl.listStatements(null, RegRdf.type, RegRdf.Property));
		System.out.println("Schema.org.owl hierarchy triples:"+metaAlignMdl.size());

		//		Jena.createStatementAddToModel(metaAlignMdl, "http://wikiba.se/ontology#Property", RegRdfs.subClassOf, RegRdf.Property);
//		Jena.createStatementAddToModel(metaAlignMdl, "http://wikiba.se/ontology#Entity", RegRdfs.subClassOf, RegRdfs.Class);
//		Jena.createStatementAddToModel(metaAlignMdl, "http://wikiba.se/ontology#Entity", RegRdfs.subClassOf, RegOwl.Thing);

		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.EQUIVALENT_CLASS, RegOwl.equivalentProperty,
				RegOwl.equivalentClass);
		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.EQUIVALENT_PROPERTY, RegOwl.equivalentProperty,
				RegOwl.equivalentProperty);
		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.SUBCLASS_OF, RegOwl.equivalentProperty,
				RegRdfs.subClassOf);
		;
		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.SUBPROPERTY_OF, RegOwl.equivalentProperty,
				RegRdfs.subPropertyOf);
		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.INSTANCE_OF, RegOwl.equivalentProperty,
				RegRdf.type);
		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.NARROWER_EXTERNAL_CLASS, RegOwl.equivalentProperty,
				RegSkos.narrowMatch);
		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.BROADER_CONCEPT, RegOwl.equivalentProperty,
				RegSkos.broadMatch);
		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.MAPPING_RELATION_TYPE, RegOwl.equivalentProperty,
				RegSkos.mappingRelation);
		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.EXTERNAL_SUBPROPERTY, RegOwl.equivalentProperty,
				RegSkos.broadMatch);// there is no superProperty in rdfs
		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.EXTERNAL_SUPERPROPERTY, RegOwl.equivalentProperty,
				RegRdfs.subPropertyOf);
		
		Model testMdl = Jena.createModel();
		Resource testSubject = testMdl.createResource("http://nuno.pt/glam");
		Jena.createStatementAddToModel(testMdl, RdfRegWikidata.GLAM, RegRdfs.subClassOf, RdfRegWikidata.INSTITUTION);
		Jena.createStatementAddToModel(testMdl, testSubject, RegRdf.type, Jena.createResource("http://wikiba.se/ontology#Entity"));
		Jena.createStatementAddToModel(testMdl, testSubject, RdfRegWikidata.INSTANCE_OF, RdfRegWikidata.GLAM);

		Jena.createStatementAddToModel(testMdl, RdfRegWikidata.INSTITUTION, RdfRegWikidata.EQUIVALENT_CLASS, RegSchemaorg.Organization);

		//		Jena.createStatementAddToModel(testMdl, "http://nuno.pt/pintura", RdfRegWikidata.INSTANCE_OF, RegSchemaorg.VisualArtwork);
//		Jena.createStatementAddToModel(testMdl, "http://nuno.pt/pintura", RdfRegWikidata.INSTANCE_OF, RegSchemaorg.Person);
//		Jena.createStatementAddToModel(testMdl, "http://nuno.pt/pintura", RdfRegWikidata.INSTANCE_OF, RegSchemaorg.VisualArtwork);
//		Jena.createStatementAddToModel(testMdl, "http://nuno.pt/pintura", RegRdf.type, RegSchemaorg.VisualArtwork);
		
		InfModel infered = ReasonerUtil.infer(reasoner,metaAlignMdl, testMdl);
		Model deductionsModel = infered.getDeductionsModel();
//		System.out.println("Deduction stms: " + infered.getDeductionsModel().size());
//		System.out.println("################## infered model #########################");		
//		StringWriter w;
//		w = new StringWriter();
//		RdfUtil.writeRdf(infered, Lang.TURTLE, w);
//		System.out.println(w.toString());
//		System.out.println("################## deductions model #########################");		
//		w = new StringWriter();
//		RdfUtil.writeRdf(infered.getDeductionsModel(), Lang.TURTLE, w);
//		System.out.println(w.toString());

		
		System.out.println("################## infered model #########################");		
		StringWriter w;
		w = new StringWriter();
		RdfUtil.writeRdf(infered.listStatements(infered.createResource(testSubject.getURI()), null, (RDFNode) null), Lang.TURTLE, w);
		RdfUtil.writeRdf(deductionsModel.listStatements(deductionsModel.createResource(testSubject.getURI()), null, (RDFNode) null), Lang.TURTLE, w);
		System.out.println(w.toString());
		
	}

}
