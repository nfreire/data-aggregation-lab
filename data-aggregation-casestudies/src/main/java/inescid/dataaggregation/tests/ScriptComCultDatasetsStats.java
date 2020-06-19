package inescid.dataaggregation.tests;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;
import inescid.dataaggregation.data.model.Rdf;

public class ScriptComCultDatasetsStats {

	
	public static void main(String[] args) throws Exception {
		String folder="C:\\Users\\nfrei\\Desktop\\ComCult";
		
		for(File gzFile: new File(folder).listFiles()) {
			long cntTriples=0;
			long cntChos=0;
			long cntContexts=0;
			if(!gzFile.getName().endsWith(".gz"))
				continue;
//			System.out.println(gzFile.getName());
			InputStream is = Files.newInputStream(gzFile.toPath());
			GZIPInputStream gis = new GZIPInputStream(is);
			Iterator<Triple> it = RDFDataMgr.createIteratorTriples(gis, Lang.RDFXML, null);
			while(it.hasNext()) {
				Triple t = it.next();
				cntTriples++;
				String pred = t.getPredicate().getURI();
				if(pred.equals(Rdf.type.getURI())) {
					String obj=t.getObject().getURI();
					if(obj.contentEquals(Edm.ProvidedCHO.getURI()))
						cntChos++;
					else if(!obj.contentEquals(Ore.Aggregation.getURI()))
						cntContexts++;
				}
			}
			gis.close();
			is.close();
			System.out.println(gzFile.getName()+","+cntTriples+","+cntChos+","+cntContexts);
		}
		
	}
	
	
	
	
}
