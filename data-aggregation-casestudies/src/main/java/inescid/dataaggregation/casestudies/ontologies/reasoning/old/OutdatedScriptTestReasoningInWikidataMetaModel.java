package inescid.dataaggregation.casestudies.ontologies.reasoning.old;

import java.io.File;
import java.io.StringWriter;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.riot.Lang;
import org.apache.jena.tdb2.TDB2Factory;

import inescid.dataaggregation.casestudies.wikidata.RdfRegWikidata;
import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.data.RegRdf;
import inescid.dataaggregation.data.RegSchemaorg;
import inescid.dataaggregation.data.reasoning.ReasonerUtil;
import inescid.dataaggregation.dataset.Global;
import inescid.util.RdfUtil;
import inescid.util.RdfUtil.Jena;

public class OutdatedScriptTestReasoningInWikidataMetaModel {

	public static void main(String[] args) throws Exception {
		final String WD_REASONING_DS = "wikidata-reasoning";
		final String WD_REASONING_STABLE_DS = "wikidata-reasoning-stable";
		final String WD_REASONING_STABLE_REMOVABLE_DS = "wikidata-reasoning-stable-removable";

		if(args==null || args.length<1) {
			System.out.println("Missing parameter: folder for storing data");
			System.exit(0);
		}
		File dataFolder=new File(args[0]);
		String httpCacheFolder = new File(dataFolder, "HttpRepository").getAbsolutePath();
		String tripleStoreFolder = new File(dataFolder, "TripleStore").getAbsolutePath();

		Global.init_componentHttpRequestService();
		Global.init_componentDataRepository(httpCacheFolder);

		CachedHttpRequestService rdfCache = new CachedHttpRequestService();
		rdfCache.setRequestRetryAttempts(1);

		// Make a TDB-backed dataset
		final Dataset dataset = TDB2Factory.connectDataset(tripleStoreFolder);
		final Model metaModelMdl = dataset.getNamedModel(WD_REASONING_DS);
		final Model stableMdl = dataset.getNamedModel(WD_REASONING_STABLE_DS);
		final Model stableMdlUnstableStatements = dataset.getNamedModel(WD_REASONING_STABLE_REMOVABLE_DS);

		GenericRuleReasoner reasoner = ReasonerUtil
				.instanciateRuleBased("inescid/dataaggregation/casestudies/ontologies/reasoning/test-rdfs-owl-ontologies-rules.txt");
//		.instanciateRuleBased("inescid/dataaggregation/casestudies/ontologies/reasoning/test-rdfs-extend.txt");

//		dataset.begin(ReadWrite.WRITE);
//		System.out.println("stabelizing");
//		ReasonerStabilizer reasonerStbl = new ReasonerStabilizer(reasoner, metaModelMdl, stableMdl);
//		stableMdlUnstableStatements.add(reasonerStbl.getUnstableSatements());
		dataset.begin(ReadWrite.READ);

		System.out.println("Metamodel stms: "+metaModelMdl.size());
		System.out.println("Stable Metamodel stms: "+stableMdl.size());
		
		Model testMdl = Jena.createModel();		
		testMdl.add(Jena.createStatement("http://nuno.pt/pintura", RegRdf.type, RegSchemaorg.VisualArtwork));
		testMdl.add(Jena.createStatement("http://nuno.pt/pintura", RdfRegWikidata.INSTANCE_OF, RegSchemaorg.Person));
		Model infered = ReasonerUtil.infer(reasoner,metaModelMdl, testMdl);
//		Model infered = reasonerStbl.inferNewDeductions(testMdl);
		StringWriter w = new StringWriter();
		RdfUtil.writeRdf(infered, Lang.TURTLE, w);
		System.out.println(w.toString());
		
//		dataset.commit();
		dataset.end();

//		System.out.println("Metamodel stms: "+metaModelMdl.size());
		System.out.println("Deduction stms: " + infered.size());
	}

}
