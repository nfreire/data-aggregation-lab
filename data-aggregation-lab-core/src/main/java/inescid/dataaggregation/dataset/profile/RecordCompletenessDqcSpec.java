package inescid.dataaggregation.dataset.profile;

import java.util.HashMap;
import java.util.Map;

import inescid.dataaggregation.data.model.RdfReg;
import inescid.dataaggregation.data.model.Dc;
import inescid.dataaggregation.data.model.DcTerms;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;

public class RecordCompletenessDqcSpec {
	public enum Dimension {Descriptiveness,	Searchability,	Contextualisation,	Identification,	Browsing,	Viewing,	ReUsability,	Multilinguality};

	public Map<Dimension, CompletenessSpecResources> dimensions;
	public CompletenessSpecResources maxScoreSpec;
	public CompletenessSpecResources minScoreSpec;

	
	public RecordCompletenessDqcSpec() {
		dimensions=new HashMap<>();
		maxScoreSpec=new CompletenessSpecResources();
		minScoreSpec=new CompletenessSpecResources();
		
		CompletenessSpecProperties maxScoreChoSpec=maxScoreSpec.addResource(Edm.ProvidedCHO);
		CompletenessSpecProperties maxScoreAggSpec=maxScoreSpec.addResource(Ore.Aggregation);
		maxScoreChoSpec.addProperty(Dc.description);
		maxScoreChoSpec.addOneOfProperties(Dc.title, DcTerms.alternative);
		maxScoreChoSpec.addOneOfProperties(Dc.creator, DcTerms.publisher, Dc.contributor);
		maxScoreChoSpec.addProperty(Dc.type);
		maxScoreChoSpec.addProperty(Dc.identifier);
		maxScoreChoSpec.addProperty(Dc.language);
		maxScoreChoSpec.addOneOfProperties(DcTerms.temporal, Dc.coverage);
		maxScoreChoSpec.addOneOfProperties(DcTerms.spatial, Dc.coverage);
		maxScoreChoSpec.addProperty(Dc.subject);
		maxScoreChoSpec.addOneOfProperties(Dc.date, DcTerms.created, DcTerms.issued);
		maxScoreChoSpec.addProperty(DcTerms.extent);
		maxScoreChoSpec.addOneOfProperties(Dc.format, DcTerms.medium);
		maxScoreChoSpec.addOneOfProperties(Dc.source, DcTerms.provenance);
		maxScoreChoSpec.addProperty(Dc.rights);
		maxScoreChoSpec.addOneOfProperties(DcTerms.relation, DcTerms.isPartOf, DcTerms.hasPart, Edm.isNextInSequence);
		maxScoreChoSpec.addProperty(Edm.rights);
		
//		maxScoreAggSpec.addProperty(Dc.RIGHTS);
		maxScoreAggSpec.addProperty(Edm.rights);
		
		CompletenessSpecProperties minScoreChoSpec=maxScoreSpec.addResource(Edm.ProvidedCHO);
		CompletenessSpecProperties minScoreAggSpec=maxScoreSpec.addResource(Ore.Aggregation);
		
		
	}
}
