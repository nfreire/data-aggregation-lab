package inescid.dataaggregation.dataset.profile.completeness;

import org.apache.jena.rdf.model.Model;

import eu.europeana.pf.alg.AlgorithmUtils;
import eu.europeana.pf.alg.TierClassifierAlgorithm;

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
