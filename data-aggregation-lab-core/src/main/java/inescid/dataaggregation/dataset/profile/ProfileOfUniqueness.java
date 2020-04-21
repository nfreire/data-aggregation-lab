package inescid.dataaggregation.dataset.profile;

import java.io.Serializable;
import java.util.HashSet;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import opennlp.tools.util.StringUtil;

public class ProfileOfUniqueness implements ProfileOfInterface, Serializable{
	private static final long serialVersionUID = 1L;
	private Calc calc=null;
	double uniqueness;
	
	public class Calc implements Serializable {
		private static final long serialVersionUID = 1L;
		HashSet<String> distinctValues=new HashSet<>();
		int duplicateCount;
		int emptyCount;
		
		public void finish() {
			double tot = distinctValues.size() + duplicateCount + emptyCount;
			uniqueness= tot == 0 ? 0 : (double) distinctValues.size() / tot;
		}

		public void add(String uriOrLiteralValue) {
			if(StringUtil.isEmpty(uriOrLiteralValue))
				emptyCount++;
			else if (!distinctValues.add(uriOrLiteralValue))
				duplicateCount++;
		}
	}
	
	public ProfileOfUniqueness() {
		calc=new Calc();
	}
	
	public ProfileOfUniqueness(double uniqueness) {
		super();
		this.uniqueness = uniqueness;
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
		if(statement.getObject().isLiteral())
			calc.add(statement.getObject().asLiteral().getString());
		else if(statement.getObject().isURIResource())
			calc.add(statement.getObject().asResource().getURI());
	}

}
