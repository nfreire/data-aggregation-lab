package inescid.dataaggregation.dataset.profile;

import java.util.HashMap;
import java.util.Map;

import inescid.dataaggregation.dataset.convert.RdfReg;

public class RecordCompletenessDqcSpec {
	public enum Dimension {Descriptiveness,	Searchability,	Contextualisation,	Identification,	Browsing,	Viewing,	ReUsability,	Multilinguality};

	public Map<Dimension, CompletenessSpecResources> dimensions;
	public CompletenessSpecResources maxScoreSpec;
	public CompletenessSpecResources minScoreSpec;

	
	public RecordCompletenessDqcSpec() {
		dimensions=new HashMap<>();
		maxScoreSpec=new CompletenessSpecResources();
		minScoreSpec=new CompletenessSpecResources();
		
		CompletenessSpecProperties maxScoreChoSpec=maxScoreSpec.addResource(RdfReg.EDM_PROVIDED_CHO);
		CompletenessSpecProperties maxScoreAggSpec=maxScoreSpec.addResource(RdfReg.ORE_AGGREGATION);
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
		maxScoreChoSpec.addOneOfProperties(RdfReg.DCTERMS_RELATION, RdfReg.DCTERMS_IS_PART_OF, RdfReg.DCTERMS_HAS_PART, RdfReg.EDM_IS_NEXT_IN_SEQUENCE);
		maxScoreChoSpec.addProperty(RdfReg.EDM_RIGHTS);
		
//		maxScoreAggSpec.addProperty(RdfReg.DC_RIGHTS);
		maxScoreAggSpec.addProperty(RdfReg.EDM_RIGHTS);
		
		CompletenessSpecProperties minScoreChoSpec=maxScoreSpec.addResource(RdfReg.EDM_PROVIDED_CHO);
		CompletenessSpecProperties minScoreAggSpec=maxScoreSpec.addResource(RdfReg.ORE_AGGREGATION);
		
		
	}
}
