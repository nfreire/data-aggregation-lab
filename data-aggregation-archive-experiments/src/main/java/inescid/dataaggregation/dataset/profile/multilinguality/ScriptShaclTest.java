package inescid.dataaggregation.dataset.profile.multilinguality;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ClassPathUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;

import eu.europeana.ld.jena.JenaUtils;
import inescid.dataaggregation.data.RdfReg;
import inescid.dataaggregation.data.RegSkos;
import inescid.util.RdfUtil;

public class ScriptShaclTest {
	public static void main(String ...args) throws Throwable {
//	    String SHAPES =
//	    		"@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\r\n" + 
//	    		"@prefix sh:   <http://www.w3.org/ns/shacl#> .\n" +
//	    		"@prefix edm:   <http://www.europeana.eu/schemas/edm/> .\n" +
//	    		"@prefix dc:   <http://purl.org/dc/elements/1.1/> .\n" +
//	    		"@prefix skos:   <http://www.w3.org/2004/02/skos/core#> .\n" +
//	    		"@prefix ore:   <http://www.openarchives.org/ore/terms/> .\n" +
//	    		"@prefix mls:   <http://www.europeana.eu/example/multilingualSaturationShapes#> .\n" +
//	    		"mls:withoutLangTagChoShape\n" + 
//	    		"a sh:NodeShape ;\n" + 
//	    		"sh:targetClass edm:ProvidedCHO, ore:Proxy;\n" + 
////	    		"	sh:property [\n" + 
////	    		"		sh:path [ sh:alternativePath ( dc:title dc:description ) ] ;\n" + 
//	    		"	sh:property [\n" + 
//	    		"		sh:path [ sh:alternativePath ( dc:title dc:description ) ] ;\n" + 
//	    		"		sh:severity sh:Info ;\n" + 
//			    		"sh:not [" +
//			    		"		sh:languageIn ( \"*\" ) ;\n" +
//	    		"	],\n" + 
//	    		"	[\n" + 
//	    		"		sh:path  dc:subject/skos:Label ;\n" + 
//	    		"		sh:severity sh:Info ;\n" + 
//	    		"sh:not [" +
//	    		"		sh:languageIn ( \"*\" ) ;\n" +
//		"	]\n"+
//	    		"]" +
//	    		
//	    		
//			    		
////	    		"	] " + 
////	    		"sh:and (" +
//	    		"	]" + 
////	    		"	)" + 
//	    		" .";
		
		
		String SHAPES = IOUtils.toString(ScriptShaclTest.class.getResourceAsStream("multilingual-saturation-shapes.ttl"), "UTF-8");
	    String DATA = 
	    		"@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\r\n" + 
	    		"@prefix edm:   <http://www.europeana.eu/schemas/edm/> .\n" +
	    		"@prefix skos:   <http://www.w3.org/2004/02/skos/core#> .\n" +
	    		"@prefix dc:   <http://purl.org/dc/elements/1.1/> .\n" +
	    		"@prefix ex:   <http://example.com/ns#> .\n" +
	    		"ex:choInstance a edm:ProvidedCHO;\n" + 
	    		"dc:title \"title of cho no lang\";\n" + 
	    		"dc:description \"description of cho\"@en;\n" + 
	    		"dc:description \"descrição do cho\"@pt;\n" + 
	    		"dc:description \"descrição do cho no lang\";\n" + 
	    		"dc:subject [ "
	    		+ "a skos:Concept ;"
	    		+ "skos:prefLabel \"Nome do conceito\"@pt;\n"
	    		+ "skos:altLabel \"Nome alternativo  do conceito\"@pt;\n"
	    		+ "]" 
	    		+ " .";

	    Graph shapesGraph = RdfUtil.readRdf(SHAPES, Lang.TURTLE).getGraph();

	    Graph dataGraph = RdfUtil.readRdf(DATA, Lang.TURTLE).getGraph();

	    Shapes shapes = Shapes.parse(shapesGraph);

	    ValidationReport report = ShaclValidator.get().validate(shapes, dataGraph);
	    
	    System.out.println(
	    report.getEntries().size()
	    		);
	    ShLib.printReport(report);
	    System.out.println();
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
