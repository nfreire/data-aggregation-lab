package inescid.dataaggregation.casestudies.coreference.semanticweb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
import inescid.dataaggregation.store.Repository;
import inescid.util.RdfUtil.Jena;
import inescid.util.SparqlClient;
import inescid.util.SparqlClient.Handler;
import inescid.util.ThreadedRunner;

public class ScriptFixFileDistribution {

	public static void main(String[] args) throws Exception {
		File fileToFix=new File("c://users/nfrei/desktop/data/coreference-semanticweb/viaf/58/http%3A%2F%2Fviaf.org%2Fviaf%2Fdata%2Fviaf-20200203-clusters-rdf.nt.gz");
		File outFile=new File("c://users/nfrei/desktop/data/coreference-semanticweb/http%3A%2F%2Fviaf.org%2Fviaf%2Fdata%2Fviaf-20200203-clusters-rdf.nt.gz");
		
		
		GZIPInputStream in=new GZIPInputStream(new FileInputStream(fileToFix));
		BufferedReader br=new BufferedReader(new InputStreamReader(in, "UTF-8"));
		
		
//		FileOutputStream out=new FileOutputStream(outFile);
		GZIPOutputStream outGz=new GZIPOutputStream(new FileOutputStream(outFile));
		
		int line=0;
		while(br.ready()) {
			line++;
			if(line!=1299181) 
//				if(line!=837130 && line!=1299182) 
				outGz.write((br.readLine()+"\n").getBytes("UTF-8"));
			else
				br.readLine();
		}
		
		outGz.flush();
		outGz.close();
	}

	
}
