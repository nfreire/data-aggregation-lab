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

public class MultilingualSaturationShacl {
	static Shapes shapes;
	
	static {
		try {
			String shapesSrfString = IOUtils.toString(MultilingualSaturationShacl.class.getResourceAsStream("multilingual-saturation-shapes.ttl"), "UTF-8");
			Graph shapesGraph = RdfUtil.readRdf(shapesSrfString, Lang.TURTLE).getGraph();
			shapes = Shapes.parse(shapesGraph);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	public static MultilingualSaturationResult calculate(Model edm) throws Exception {
	    Graph dataGraph = edm.getGraph();

	    ShapesDetectionReport report = ShapeDetectorProc.simpleValidatation(shapes, dataGraph, false);
	    MultilingualSaturationResult result=new MultilingualSaturationResult(report);
	    
	    return result;
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
