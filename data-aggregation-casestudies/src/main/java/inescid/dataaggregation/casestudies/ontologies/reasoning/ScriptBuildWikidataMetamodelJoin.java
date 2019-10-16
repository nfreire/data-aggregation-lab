package inescid.dataaggregation.casestudies.ontologies.reasoning;

import java.io.File;
import java.io.InputStream;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.tdb2.TDB2Factory;

import inescid.dataaggregation.casestudies.wikidata.RdfRegWikidata;
import inescid.dataaggregation.casestudies.wikidata.SparqlClientWikidata;
import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.data.RegOwl;
import inescid.dataaggregation.data.RegRdf;
import inescid.dataaggregation.data.RegRdfs;
import inescid.dataaggregation.data.RegSkos;
import inescid.dataaggregation.data.reasoning.ReasonerUtil;
import inescid.dataaggregation.dataset.Global;
import inescid.util.RdfUtil;
import inescid.util.RdfUtil.Jena;
import inescid.util.SparqlClient.Handler;

public class ScriptBuildWikidataMetamodelJoin {

	public static void main(String[] args) throws Exception {
		if(args==null || args.length<1) {
			System.out.println("Missing parameter: folder for storing data");
			System.exit(0);
		}
		File dataFolder=new File(args[0]);
		String tripleStoreFolder = new File(dataFolder, Settings.TRIPLE_STORE_FOLDER).getAbsolutePath();
		String tripleStoreJoinedFolder = new File(dataFolder, Settings.TRIPLE_STORE_JOINED_FOLDER).getAbsolutePath();

//		Global.init_componentHttpRequestService();
//		Global.init_componentDataRepository(httpCacheFolder);

//		CachedHttpRequestService rdfCache = new CachedHttpRequestService();
//		rdfCache.setRequestRetryAttempts(1);

		// Make a TDB-backed dataset
		final Dataset dataset = TDB2Factory.connectDataset(tripleStoreFolder);
		dataset.begin(ReadWrite.READ);
		final Model modelPropertiesMdl = dataset.getNamedModel(Settings.WD_REASONING_MODEL_PROPERTIES_DS);
		final Model modelClassesMdl = dataset.getNamedModel(Settings.WD_REASONING_MODEL_CLASSES_DS);
		final Model metaAlignMdl = dataset.getNamedModel(Settings.WD_REASONING_ALIGN_META_DS);
		final Model schemaorgMdl = dataset.getNamedModel(Settings.WD_REASONING_SCHEMAORG_DS);
		final Model owlMdl = dataset.getNamedModel(Settings.WD_REASONING_OWL_DS);
		
		final Dataset datasetJoined = TDB2Factory.connectDataset(tripleStoreJoinedFolder);
		datasetJoined.begin(ReadWrite.WRITE);
		final Model joinedMdl = datasetJoined.getNamedModel(Settings.WD_REASONING_MODEL_ALIGN_META_DS);
		
		if (Settings.RESET_MODELS) {
			joinedMdl.removeAll();
		}
		System.out.println("Cleaning: done");
		
		joinedMdl.add(modelPropertiesMdl);
		System.out.println("Added");
		joinedMdl.add(modelClassesMdl);
		System.out.println("Added");
		joinedMdl.add(schemaorgMdl);
		System.out.println("Added");
		joinedMdl.add(metaAlignMdl);
		System.out.println("Added");
//		joinedMdl.add(owlMdl);
//		System.out.println("Added");
		datasetJoined.commit();
		System.out.println("Committed");
		
		dataset.end();
		datasetJoined.end();
	}

}
