package inescid.dataaggregation.metadatatester.view;

import org.apache.jena.rdf.model.Statement;

import inescid.dataaggregation.data.RegRdf;
import inescid.dataaggregation.data.RegSchemaorg;

public class StatementView {
		Statement st;
		
		public StatementView(Statement st) {
			super();
			this.st = st;
		}

		public String getPredicate() {
			String predUri = st.getPredicate().toString();
			if(predUri.startsWith(RegSchemaorg.NS))
				predUri=predUri.substring(RegSchemaorg.NS.length());
			else if(predUri.startsWith(RegRdf.NS))
				predUri="rdf:"+predUri.substring(RegRdf.NS.length());
			return predUri;
//			return st.getPredicate().toString();
		}
		
		public String getObject() {
			return st.getObject().toString();
		}		
	}