package inescid.dataaggregation.casestudies.coreference.semanticweb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamOps;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFBase;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import inescid.dataaggregation.casestudies.coreference.Consts;
import inescid.dataaggregation.casestudies.coreference.CoreferenceDebugger;
import inescid.dataaggregation.wikidata.RdfRegWikidata;
import inescid.dataaggregation.wikidata.WikidataUtil;
import inescid.dataaggregation.wikidata.SparqlClientWikidata;
import inescid.dataaggregation.crawl.distribution.DistributionDownloadRepository;
import inescid.dataaggregation.data.model.Owl;
import inescid.dataaggregation.data.model.Schemaorg;
import inescid.dataaggregation.data.model.Skos;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.store.Repository;
import inescid.util.RdfUtil.Jena;
import inescid.util.SparqlClient;
import inescid.util.SparqlClient.Handler;
import inescid.util.ThreadedRunner;

public class ScriptStatementHarvestFromFileDistribution {
	File repoFolder;
	String datasetId;
	ArrayList<String> fileDistributions;
	DistributionDownloadRepository repo;
	Repository dataRepository;

	public ScriptStatementHarvestFromFileDistribution(String repoFolder, String datasetId,
			ArrayList<String> fileDistributions) {
		super();
		this.repoFolder = new File(repoFolder);
		this.datasetId = datasetId;
		this.fileDistributions = fileDistributions;
		Repository dataRepository=new Repository();
		dataRepository.init(repoFolder);
		repo=new DistributionDownloadRepository(dataRepository, datasetId);
	}

	public static void main(String[] args) throws Exception {
    	String repoFolder = "c://users/nfrei/desktop/data/coreference-semanticweb";
    	
    	String datasetId;
//    	datasetId =Consts.viaf_datasetId;
//    	datasetId =Consts.idLocGov_datasetId;
    	datasetId =Consts.bne_datasetId;
    	ArrayList<String> fileDistributions=new ArrayList<String>();
    	String testFileDistribution="http://datos.bne.es/datadumps/autoridades.nt.bz2";
//    	String testFileDistribution="http://viaf.org/viaf/data/viaf-20200203-clusters-rdf.nt.gz";
    	
    	Global.init_componentHttpRequestService();
    	
    	if(args!=null) {
			if(args.length>=1) {
				repoFolder = args[0];
				if(args.length>=2) {
					datasetId = args[1];
					if(args.length>=3) {
						for(int i=2; i<args.length;i++)
							fileDistributions.add(args[i]);
					}				
				}				
			}
    	} 
    	if (fileDistributions.isEmpty())
    		fileDistributions.add(testFileDistribution);
    	
    	System.out.println();

		ScriptStatementHarvestFromFileDistribution corefFinder=new ScriptStatementHarvestFromFileDistribution(repoFolder, datasetId,
				fileDistributions);
		corefFinder.run();
		System.out.println("FININSHED: "+datasetId);
	}

	private void run() throws Exception {
		HashSet<String> predicatesToIngest=new HashSet<String>() {{
			add(Owl.sameAs.getURI());
			add(Skos.exactMatch.getURI());
			add(Skos.closeMatch.getURI());
			add(Schemaorg.sameAs.getURI());
		}};
		FileOutputStream fos=new FileOutputStream(new File(repoFolder, datasetId+"."+Consts.RDF_SERIALIZATION.getFileExtensions().get(0)));
		StreamRDF writer = StreamRDFWriter.getWriterStream(fos, Consts.RDF_SERIALIZATION) ;
		
		try {
			for(String distUrl: fileDistributions) {
				System.out.println("FILE START: "+distUrl);
				repo.streamRdf(distUrl, new StreamRDFBase() {
					int cnt=0;
					@Override
					public void triple(Triple triple) {
						if(predicatesToIngest.contains(triple.getPredicate().getURI())) {
							writer.triple(triple);
							cnt++;
							if(cnt % 1000 ==0) System.out.println(cnt);
						}
					}
				});
			}
		}finally {
			fos.close();
		}
	}
	
}
