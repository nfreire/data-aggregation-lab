package inescid.dataaggregation.dataset.profile.multilinguality;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;

import eu.europeana.ld.jena.JenaUtils;
import inescid.util.RdfUtil;

public class SparqlTest {
	public static void main(String ...args) {
	    String DATA = 
	    		"@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\r\n" + 
	    		"@prefix edm:   <http://www.europeana.eu/schemas/edm/> .\n" +
	    		"@prefix dc:   <http://purl.org/dc/elements/1.1/> .\n" +
	    		"@prefix ex:   <http://example.com/ns#> .\n" +
	    		"ex:choInstance a edm:ProvidedCHO;\n" + 
	    		"dc:title \"title of cho no lang\";\n" + 
	    		"dc:description \"description of cho\"@en;\n" + 
	    		"dc:description \"descrição do cho\"@pt;\n" + 
	    		"dc:description \"descrição do cho no lang\";\n" + 
	    		" .";

	    Model dataMdl = RdfUtil.readRdf(DATA, Lang.TURTLE);

//	    Shapes shapes = Shapes.parse(shapesGraph);
//	    ValidationReport report = ShaclValidator.get().validate(shapes, dataGraph);
//	    ShLib.printReport(report);
//	    System.out.println();
//	    RDFDataMgr.write(System.out, report.getModel(), Lang.TTL);
	}
}


//
//
//ex:Mountain
//ex:prefLabel "Mountain"@en ;
//ex:prefLabel "Hill"@en-NZ ;
//ex:prefLabel "Maunga"@mi .
//
//ex:Berg
//ex:prefLabel "Berg" ;
//ex:prefLabel "Berg"@de ;
//ex:prefLabel ex:BergLabel .
