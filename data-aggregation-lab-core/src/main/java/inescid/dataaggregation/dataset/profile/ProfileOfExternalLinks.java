package inescid.dataaggregation.dataset.profile;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.StringUtils;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import inescid.util.datastruct.MapOfMapsOfInts;

public class ProfileOfExternalLinks implements ProfileOfInterface, Serializable{
	private static final long serialVersionUID = 1L;
	double linkageToInternalRatio;
	double linkageToExternalRatio;
	double literalRatio;
	ProfileOfLinkedDomains domains=new ProfileOfLinkedDomains();
	
	Calc calc;

	public class ProfileOfLinkedDomains implements Serializable{ 
		private static final long serialVersionUID = 1L;
		MapOfMapsOfInts<String, String> stats=new MapOfMapsOfInts<String, String>();
		
		public void addLink(String uri, Property prop) {
			if(StringUtils.isEmpty(uri) || !uri.startsWith("http") || prop==null)
				return;
			URI uriObj;
			try {
				uriObj = new URI(uri);
			} catch (URISyntaxException e) {
				return;
			}
			stats.incrementTo(uriObj.getHost(), prop.getURI());
		}

		public List<Entry<String,List<Entry<String,Integer>>>> getSortedEntries() {
			return stats.getSortedEntries();
		}
		
	}

	
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

		public void add(Statement st) {
			RDFNode rdfNode=st.getObject();
			if(rdfNode.isAnon())
				linkageToInternalCount++;
			else if (rdfNode.isURIResource()) {
				StmtIterator sts = rdfNode.getModel().listStatements((Resource)rdfNode, (Property)null,  (RDFNode)null);
				if(sts.hasNext())
					linkageToInternalCount++;
				else {
					linkageToExternalCount++;
					domains.addLink(rdfNode.asResource().getURI(), st.getPredicate());
				}
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
		calc.add(statement);
	}



	public void toCsv(CSVPrinter csv) throws IOException {
		csv.print(linkageToInternalRatio*100);		
		csv.print(linkageToExternalRatio*100);
		int domainsCnt=0;
		for(Entry<String, List<Entry<String, Integer>>> e : domains.getSortedEntries()) {
			String value=String.format("%s [", e.getKey());
			for(Entry<String, Integer> e2 : e.getValue()) {
				if(domainsCnt!=0)  value += ", ";
				value += String.format("%s - %d", e2.getKey(), e2.getValue());
			}
			value += "]";
			csv.print(value);
			domainsCnt++;
			if(domainsCnt>=10)
				break;
		}
	}
}
