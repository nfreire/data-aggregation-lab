package inescid.dataaggregation.casestudies.ontologies.reasoning;

import java.io.File;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.tdb2.TDB2Factory;

import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.data.reasoning.ReasonerStabilizer;
import inescid.dataaggregation.data.reasoning.ReasonerUtil;
import inescid.dataaggregation.dataset.Global;

public class ScriptBuildWikidataMetamodelReasoner {

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
		String tripleStoreFolder = new File(dataFolder, Settings.TRIPLE_STORE_FOLDER).getAbsolutePath();

		Global.init_componentHttpRequestService();
		Global.init_componentDataRepository(httpCacheFolder);

		CachedHttpRequestService rdfCache = new CachedHttpRequestService();
		rdfCache.setRequestRetryAttempts(1);

		final Dataset dataset = TDB2Factory.connectDataset(tripleStoreFolder);
		dataset.begin(ReadWrite.WRITE);
		
		final Model modelMdl = dataset.getNamedModel(Settings.WD_REASONING_MODEL_DS);
		final Model reasoningMdl = dataset.getNamedModel(Settings.WD_REASONING_MODEL_ALIGN_META_DS);
		
		System.out.println("Copying "+Settings.WD_REASONING_MODEL_DS+" to "+Settings.WD_REASONING_MODEL_ALIGN_META_DS);
		reasoningMdl.removeAll();
		reasoningMdl.add(modelMdl);

		System.out.println("Stabelizing");
		GenericRuleReasoner reasoner = ReasonerUtil
				.instanciateRuleBased("inescid/dataaggregation/casestudies/ontologies/reasoning/test-rdfs-owl-ontologies-rules-small.txt");
//		.instanciateRuleBased("inescid/dataaggregation/casestudies/ontologies/reasoning/test-rdfs-owl-ontologies-rules.txt");
		
		ReasonerStabilizer stabilizer=new ReasonerStabilizer(reasoner, reasoningMdl);
		stabilizer.runElaborateStabelization();
		
		dataset.commit();
		dataset.end();
		
	}

}
