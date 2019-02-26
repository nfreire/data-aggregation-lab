package inescid.dataaggregation.dataset.profile.completeness;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.bouncycastle.jcajce.util.AlgorithmParametersUtils;

import com.mchange.v1.util.SimpleMapEntry;

import eu.europeana.pf.alg.AlgorithmUtils;
import eu.europeana.pf.alg.TierClassifierAlgorithm;
import inescid.dataaggregation.dataset.convert.RdfReg;

public class TiersDqcCompletenessCalculator {
	public static int TOTAL_TIERS=3;
	
	TierClassifierAlgorithm tierClassifier;
	
	
	public TiersDqcCompletenessCalculator() {
		TierClassifierAlgorithm[] metadataAlgorithms = AlgorithmUtils.getMetadataAlgorithms();
		tierClassifier=metadataAlgorithms[metadataAlgorithms.length-1];//the last one returned is the combined one
	}
	public double calculate(Model rdfModelOfRec) {
		return tierClassifier.classify(rdfModelOfRec) + Dqc10PointRatingCalculator.calculate(rdfModelOfRec);
	}
}
