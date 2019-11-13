package inescid.dataaggregation.dataset.profile.completeness;

import inescid.dataaggregation.data.RdfReg;
import inescid.dataaggregation.data.RegEdm;

public class Dqc10PointRatingSpec {
	public static CompletenessSpecResources spec;
	public static int TOTAL_CRITERIA=16;
	
	static {
		spec=new CompletenessSpecResources();
		
		CompletenessSpecOfProperties maxScoreChoSpec=spec.addResource(RegEdm.ProvidedCHO);
		CompletenessSpecOfProperties maxScoreAggSpec=spec.addResource(RdfReg.ORE_AGGREGATION);
		maxScoreChoSpec.addProperty(RdfReg.DC_DESCRIPTION);
		maxScoreChoSpec.addOneOfProperties(RdfReg.DC_TITLE, RdfReg.DCTERMS_ALTERNATIVE);
		maxScoreChoSpec.addOneOfProperties(RdfReg.DC_CREATOR, RdfReg.DCTERMS_PUBLISHER, RdfReg.DC_CONTRIBUTOR);
		maxScoreChoSpec.addProperty(RdfReg.DC_TYPE);
		maxScoreChoSpec.addProperty(RdfReg.DC_IDENTIFIER);
		maxScoreChoSpec.addProperty(RdfReg.DC_LANGUAGE);
		maxScoreChoSpec.addOneOfProperties(RdfReg.DCTERMS_TEMPORAL, RdfReg.DC_COVERAGE);
		maxScoreChoSpec.addOneOfProperties(RdfReg.DCTERMS_SPATIAL, RdfReg.DC_COVERAGE);
		maxScoreChoSpec.addProperty(RdfReg.DC_SUBJECT);
		maxScoreChoSpec.addOneOfProperties(RdfReg.DC_DATE, RdfReg.DCTERMS_CREATED, RdfReg.DCTERMS_ISSUED);
		maxScoreChoSpec.addProperty(RdfReg.DCTERMS_EXTENT);
		maxScoreChoSpec.addOneOfProperties(RdfReg.DC_FORMAT, RdfReg.DCTERMS_MEDIUM);
		maxScoreChoSpec.addOneOfProperties(RdfReg.DC_SOURCE, RdfReg.DCTERMS_PROVENANCE);
		maxScoreChoSpec.addProperty(RdfReg.DC_RIGHTS);
		maxScoreChoSpec.addOneOfProperties(RdfReg.DCTERMS_RELATION, RdfReg.DCTERMS_IS_PART_OF, RdfReg.DCTERMS_HAS_PART, RegEdm.isNextInSequence);
		
		CompletenessSpecOfProperties maxScoreProxySpec=spec.addResource(RdfReg.ORE_PROXY);
		maxScoreProxySpec.copyFrom(maxScoreChoSpec);

		maxScoreAggSpec.addProperty(RegEdm.rights);
	}
	
}
