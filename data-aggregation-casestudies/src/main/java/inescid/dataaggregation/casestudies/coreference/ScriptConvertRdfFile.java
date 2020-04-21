package inescid.dataaggregation.casestudies.coreference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamOps;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFBase;
import org.apache.jena.riot.system.StreamRDFWriter;

public class ScriptConvertRdfFile {

	public static void main(String[] args) throws Exception {
		File f=new File("C:\\Users\\nfrei\\Desktop\\data\\coreference\\europeana-providers.rt");
		
		Lang from = Lang.RDFTHRIFT;
		Lang to = Lang.NTRIPLES;
		
		FileOutputStream fos=new FileOutputStream(new File(f.getParentFile(), f.getName()+"."+to.getFileExtensions().get(0)));
		StreamRDF writer=StreamRDFWriter.getWriterStream(fos, to) ;			
		
		FileInputStream fis=new FileInputStream(f);
		RDFDataMgr.parse(new StreamRDFBase() {
			ArrayList<Triple> toAddTripleAux=new ArrayList<Triple>(1) {{ add(null); }};;
			public void triple(Triple triple) {
				toAddTripleAux.set(0, triple);
				StreamOps.sendTriplesToStream(toAddTripleAux.iterator(), writer);						
			}
		}, fis, from);
		
		writer.finish();
		fos.close();
	}

}
