package inescid.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.TDB2Factory;

import inescid.util.SparqlClient;

public class TriplestoreJenaTbd2 extends SparqlClient implements Closeable {
	public TriplestoreJenaTbd2(File dbFolder) {
		this(TDB2Factory.connectDataset(dbFolder.getAbsolutePath()));
	}
	
	private TriplestoreJenaTbd2(Dataset dataset) {
		super(dataset, "");
		dataset.begin(ReadWrite.READ);
	}
	
	public TriplestoreJenaTbd2(File dbFolder, String preffixes) {
		this(dbFolder);
		this.queryPrefix=preffixes;
	}

	public void close() {
		dataset.end();
		dataset.close();
	}
	
	public static void main(String[] args) throws Exception {
		// Make a TDB-backed dataset
		  String directory = "C:\\Users\\nfrei\\Desktop\\data\\wikidata-to-edm\\triplestore-wikidata" ;
		  Dataset ds = TDB2Factory.connectDataset(directory) ;
	       Txn.executeWrite(ds, ()->{
	    	   try {
//				InputStream r = Files.newInputStream(new File("C:\\Users\\nfrei\\Desktop\\data\\wikidata-to-edm\\wikidata.rt").toPath());
//				    RDFDataMgr.read(ds, r, Lang.RDFTHRIFT);
//				    r.close();
				    InputStream r = Files.newInputStream(new File("C:\\Users\\nfrei\\Desktop\\data\\wikidata-to-edm\\wikidata2.rt").toPath());
				    RDFDataMgr.read(ds, r, Lang.RDFTHRIFT);
				    r.close();
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
	       }) ;
	       
//		  Txn.executeRead(ds, ()->{
//			  
//			  try {
//				FileOutputStream fos = new FileOutputStream(new File("C:\\Users\\nfrei\\Desktop\\data\\wikidata-to-edm\\wikidata.trig"));
//				   RDFDataMgr.write(fos, ds, Lang.TRIG) ;
//				   fos.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//	       }) ;		 
		  
//		  Dataset dsW = DatasetFactory.wrap(ds.getDefaultModel()) ;
		  
//		  Txn.executeRead(ds, ()->{
//			  try (QueryExecution qExec = QueryExecutionFactory.create("select ?s (<http://www.wikidata.org/prop/direct/P1268> AS ?p) ?o WHERE { ?s <http://www.wikidata.org/prop/direct/P2888> ?o. FILTER regex(str(?o), \"http://schema.org\")}", ds) ) {
//				  ResultSet rs = qExec.execSelect() ;
//				  ResultSetFormatter.out(rs) ;
//			  }
//		  }) ;		 
//		  ds.close();
		  
		  
		  
	}
}
