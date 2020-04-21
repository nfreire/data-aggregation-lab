package inescid.dataaggregation.dataset.profile;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.csv.CSVPrinter;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import opennlp.tools.util.StringUtil;

public class ProfileOfValueDistribution  implements ProfileOfInterface, Serializable{
	private static final long serialVersionUID = 1L;
	List<ValueDistribution> distribution=null;
	int discardValuesUnderCount=2;
	float discardValuesUnderPercentage=0.01f;
	Calc calc;
	
	public class ValueDistribution implements Comparable<ValueDistribution>, Serializable{
		private static final long serialVersionUID = 1L;
		public String value;
		public double distribution;
		public ValueDistribution(String value, double distribution) {
			super();
			this.value = value;
			this.distribution = distribution;
		}
		@Override
		public int compareTo(ValueDistribution o) {
			return - Double.compare(distribution, o.distribution);
		}
		@Override
		public boolean equals(Object obj) {
			return value==((ValueDistribution)obj).value && distribution==((ValueDistribution)obj).distribution;
		}
		@Override
		public String toString() {
			return "ValueDistribution [value=" + value + ", distribution=" + distribution + "]";
		}
		public void toCsv(CSVPrinter csv) throws IOException {
			csv.printRecord(value, distribution*100);
		}
	}
	public class Calc implements Serializable{
		private static final long serialVersionUID = 1L;
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
			distribution=new ArrayList<>();
			for (Entry<String, Integer> v: valueCounts.entrySet()) {
				double cnt = (double)v.getValue();
				double pct = cnt/(double)totalCount;
				if(discardValuesUnderCount>0 && cnt<discardValuesUnderCount)
					continue;
				if(discardValuesUnderPercentage>0 && pct<discardValuesUnderPercentage)
					continue;
				distribution.add(new ValueDistribution(v.getKey(), pct));
			}
			Collections.sort(distribution);
		}
	}

	
	public ProfileOfValueDistribution() {
		calc=new Calc();
	}
	
	public ProfileOfValueDistribution(List<ValueDistribution> distribution) {
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

	public List<ValueDistribution> getDistribution() {
		return distribution;
	}

	public int getDiscardValuesUnderCount() {
		return discardValuesUnderCount;
	}

	public void setDiscardValuesUnderCount(int discardValuesUnderCount) {
		this.discardValuesUnderCount = discardValuesUnderCount;
	}

	public float getDiscardValuesUnderPercentage() {
		return discardValuesUnderPercentage;
	}

	public void setDiscardValuesUnderPercentage(float discardValuesUnderPercentage) {
		this.discardValuesUnderPercentage = discardValuesUnderPercentage;
	}

}
