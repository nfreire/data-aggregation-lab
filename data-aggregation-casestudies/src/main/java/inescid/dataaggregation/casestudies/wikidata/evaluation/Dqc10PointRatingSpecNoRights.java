package inescid.dataaggregation.casestudies.wikidata.evaluation;

import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Dc;
import inescid.dataaggregation.data.model.DcTerms;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;
import inescid.dataaggregation.dataset.profile.completeness.CompletenessSpecOfProperties;
import inescid.dataaggregation.dataset.profile.completeness.CompletenessSpecResources;

public class Dqc10PointRatingSpecNoRights {
	public static CompletenessSpecResources spec;
	public static int TOTAL_CRITERIA=15;
	
	static {
		spec=new CompletenessSpecResources();
		
		CompletenessSpecOfProperties maxScoreChoSpec=spec.addResource(Edm.ProvidedCHO);
//		CompletenessSpecOfProperties maxScoreAggSpec=spec.addResource(RdfReg.ORE_AGGREGATION);
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
		
		CompletenessSpecOfProperties maxScoreProxySpec=spec.addResource(Ore.Proxy);
		maxScoreProxySpec.copyFrom(maxScoreChoSpec);

//		maxScoreAggSpec.addProperty(RdfRegEdm.RIGHTS);
	}
	
}
