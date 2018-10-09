package inescid.dataaggregation.dataset.profile;

import java.io.IOException;
import java.util.HashSet;

import org.apache.commons.csv.CSVPrinter;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import inescid.util.RdfUtil;
import opennlp.tools.util.StringUtil;

public class ProfileOfExternalLinks implements ProfileOfInterface{
	double linkageToInternalRatio;
	double linkageToExternalRatio;
	double literalRatio;
	Calc calc;
	
	public class Calc{
		double linkageToInternalCount;
		double linkageToExternalCount;
		double literalCount;
		
		public void finish() {
			double total= linkageToInternalCount +linkageToExternalCount + literalCount;
			linkageToInternalRatio=linkageToInternalCount / total;
			linkageToExternalRatio=linkageToExternalCount / total;
			literalRatio=literalCount / total;
		}

		public void add(RDFNode rdfNode) {
			if(rdfNode.isAnon())
				linkageToInternalCount++;
			else if (rdfNode.isURIResource()) {
				StmtIterator sts = rdfNode.getModel().listStatements((Resource)rdfNode, (Property)null,  (RDFNode)null);
				if(sts.hasNext())
					linkageToInternalCount++;
				else
					linkageToExternalCount++;
			} else if (rdfNode.isLiteral())
				literalCount++;
		}
	}
	
	public ProfileOfExternalLinks() {
		calc=new Calc();
	}
	
	

	public ProfileOfExternalLinks(double linkageToInternalRatio, double linkageToExternalRatio, double literalRatio) {
		super();
		this.linkageToInternalRatio = linkageToInternalRatio;
		this.linkageToExternalRatio = linkageToExternalRatio;
		this.literalRatio = literalRatio;
	}



	@Override
	public void finish() {
		calc.finish();
		calc=null;
	}

	@Override
	public void eventInstanceStart(Resource resource) {
		
	}

	@Override
	public void eventInstanceEnd(Resource resource) {
	}

	@Override
	public void eventProperty(Statement statement) {
		calc.add(statement.getObject());
	}


}
