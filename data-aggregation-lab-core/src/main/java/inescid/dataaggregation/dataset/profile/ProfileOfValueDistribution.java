package inescid.dataaggregation.dataset.profile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import inescid.dataaggregation.dataset.profile.ProfileOfUniqueness.Calc;
import inescid.util.RdfUtil;
import opennlp.tools.util.StringUtil;

public class ProfileOfValueDistribution  implements ProfileOfInterface{
	Map<String, Double> distribution=new HashMap<>();
	Calc calc;
	
	public class Calc{
		SortedMap<String, Integer> valueCounts=new TreeMap<>();
		int totalCount;
		
		public void add(String uriOrLiteralValue) {
			if(!StringUtil.isEmpty(uriOrLiteralValue)) {
				totalCount++;
				Integer count = valueCounts.get(uriOrLiteralValue);
				if(count==null) {
					valueCounts.put(uriOrLiteralValue, 1);
				} else
					valueCounts.put(uriOrLiteralValue, count+1);
			}
		}

		public void finish() {
			distribution=new HashMap<>(valueCounts.size());
			for (Entry<String, Integer> v: valueCounts.entrySet()) {
				distribution.put(v.getKey(), (double)v.getValue()/(double)totalCount);
			}
		}
	}

	
	public ProfileOfValueDistribution() {
		calc=new Calc();
	}
	
	public ProfileOfValueDistribution(SortedMap<String, Double> distribution) {
		super();
		this.distribution = distribution;
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
