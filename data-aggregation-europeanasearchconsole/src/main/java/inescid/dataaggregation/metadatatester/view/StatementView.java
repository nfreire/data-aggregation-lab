package inescid.dataaggregation.metadatatester.view;

import org.apache.jena.rdf.model.Statement;

import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Schemaorg;

public class StatementView {
		Statement st;
		
		public StatementView(Statement st) {
			super();
			this.st = st;
		}

		public String getPredicate() {
			String predUri = st.getPredicate().toString();
			if(predUri.startsWith(Schemaorg.NS))
				predUri=predUri.substring(Schemaorg.NS.length());
			else if(predUri.startsWith(Rdf.NS))
				predUri="rdf:"+predUri.substring(Rdf.NS.length());
			return predUri;
//			return st.getPredicate().toString();
		}
		
		public String getObject() {
			return st.getObject().toString();
		}		
	}