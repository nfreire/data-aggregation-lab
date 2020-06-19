package inescid.dataaggregation.dataset.profile.multilinguality;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ClassPathUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;

import eu.europeana.ld.jena.JenaUtils;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Skos;
import inescid.util.RdfUtil;

public class MultilingualSaturation {
	public static MultilingualSaturationResult calculate(Model edm) {
		MultilingualSaturationResult result=new MultilingualSaturationResult();
		for(Resource r: edm.listResourcesWithProperty(Rdf.type).toList()) {
			calculate(r, result);
		}
	    return result;
	}
	public static MultilingualSaturationResult calculate(Resource edm) {
		MultilingualSaturationResult result=new MultilingualSaturationResult();
		calculate(edm, result);
		return result;
	}
	private  static void calculate(Resource edm, MultilingualSaturationResult result) {
		String cls=edm.getProperty(Rdf.type).getObject().asResource().getURI();
		for (Statement p: edm.listProperties().toList()) {
			if(p.getObject().isLiteral()) {
				String language = p.getObject().asLiteral().getLanguage();
//					if(!StringUtils.isEmpty(language)) {
				String propUri = p.getPredicate().getURI();
				result.addLangTag(language, cls, propUri);
//					}
			}
		}
	}
}
