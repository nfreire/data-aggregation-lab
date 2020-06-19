package inescid.europeana.dataprocessing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import inescid.dataaggregation.data.model.RdfReg;
import inescid.dataaggregation.dataset.profile.completeness.TiersDqcCompletenessCalculator;
import inescid.dataaggregation.dataset.profile.multilinguality.MultilingualSaturation;
import inescid.dataaggregation.dataset.profile.multilinguality.MultilingualSaturationResult;
import inescid.dataaggregation.dataset.profile.tiers.EpfTiersCalculator;
import inescid.dataaggregation.dataset.profile.tiers.TiersCalculation;
import inescid.util.RdfUtil;
import inescid.util.europeana.EdmRdfUtil;

public class EdmMeasurementSet implements Iterable<EdmMeasurementSet.EdmMeasurement> {
	public interface EdmMeasurement {
		public abstract String[] getCsvResult(Resource edmCho, String edmRdfXml) throws Exception;
	
		public abstract String[] getHeaders();
	}
	
	ArrayList<EdmMeasurement> measurements=new ArrayList<EdmMeasurementSet.EdmMeasurement>();
	
	public EdmMeasurementSet(EdmMeasurement... measurements) {
		super();
		for(EdmMeasurement m: measurements)
			this.measurements.add(m);
	}
	
	public EdmMeasurementSet(EdmMeasurementSet set, EdmMeasurement... additionalMeasurements) {
		super();
		this.measurements.addAll(set.measurements);
		for(EdmMeasurement m: additionalMeasurements)
			this.measurements.add(m);
	}

	public List<String> getHeaders() {
		List<String> heads=new ArrayList<>();
		for(EdmMeasurement m: measurements) {
			for(String h: m.getHeaders())
				heads.add(h);
		}
		return heads;
	}
	
	
	public static EdmMeasurementSet getMeasurementSetA() {
		return new EdmMeasurementSet(new EdmMeasurement[] {
				createEdmMeasurementCompleteness(),
				createEdmMeasurementMetadataTiers(),
				createEdmMeasurementLanguageSaturation()
			});
	}

	private static EdmMeasurement createEdmMeasurementLanguageSaturation() {
		return new EdmMeasurement() {
			@Override
			public String[] getCsvResult(Resource edmCho, String edmRdfXml) {
				MultilingualSaturationResult score;
				try {
					score = MultilingualSaturation.calculate(edmCho.getModel());
//					score = MultilingualSaturationShacl.calculate(edmCho.getModel());
				} catch (Exception e) {
					throw new RuntimeException(e.getMessage(), e);
				}
				return new String[] {
						String.valueOf(score.getStatementsCount()) ,
						String.valueOf(score.getLanguagesCount()) , String.valueOf(score.getLangTagCount()),
						String.valueOf(score.getChoResult().getStatementsCount()) ,
						String.valueOf(score.getChoResult().getLanguagesCount()) , String.valueOf(score.getChoResult().getLangTagCount()),
						String.valueOf(score.getContextResult().getStatementsCount()) ,
						String.valueOf(score.getContextResult().getLanguagesCount()) , String.valueOf(score.getContextResult().getLangTagCount())
						
				};
			};
			@Override
			public String[] getHeaders() {
				return new String[] {"ls_statements", "ls_languagesCount", "ls_langTagCount","ls_cho_statements", "ls_cho_languagesCount", "ls_cho_langTagCount","ls_ctx_statements", "ls_ctx_languagesCount", "ls_ctx_langTagCount"};
			}
		};
	}
	
	private static EdmMeasurement createEdmMeasurementCompleteness() {
		return new EdmMeasurement() {
			@Override
			public String[] getCsvResult(Resource edmCho, String edmRdfXml) {
				TiersDqcCompletenessCalculator calculator=new TiersDqcCompletenessCalculator();
				double calculate = calculator.calculate(edmCho.getModel());
				return new String[] {String.valueOf(calculate)};
			};
			@Override
			public String[] getHeaders() {
				return new String[] {"completeness"};
			}
		};
	}

	private static EdmMeasurement createEdmMeasurementMetadataTiers() {
		return new EdmMeasurement() {
			@Override
			public String[] getCsvResult(Resource edmCho, String edmRdfXml) throws Exception {
				//Monge does not output the WebResource meta info, so the content tier is obtained from the metadata
				String contentTier="0";
				Model recMdl = edmCho.getModel();
				Resource agg = EdmRdfUtil.getEuropeanaAggregationResource(recMdl);
				for (StmtIterator qAnnStms=agg.listProperties(RdfReg.DQV_HAS_QUALITY_ANNOTATION) ; qAnnStms.hasNext() ; ) {
					Statement stm = qAnnStms.next();
					Resource qAnnotRes = stm.getObject().asResource();
					contentTier = RdfUtil.getUriOrLiteralValue(qAnnotRes.getProperty(RdfReg.OA_HAS_BODY).getObject());
					if(contentTier.contains("contentTier")) {
						contentTier=contentTier.substring("http://www.europeana.eu/schemas/epf/contentTier".length());
						break;
					}
				}
				
				TiersCalculation calculate = EpfTiersCalculator.calculate(edmRdfXml);
				return new String[] {
//						String.valueOf(calculate.getContent().getLevel()),
						contentTier,
						
						String.valueOf(calculate.getMetadata().getLevel()),
						String.valueOf(calculate.getContextualClass().getLevel()),
						String.valueOf(calculate.getEnablingElements().getLevel()),
						String.valueOf(calculate.getLanguage().getLevel())
				};
			};
			@Override
			public String[] getHeaders() {
				return new String[] {"epf_media", "epf_metadata", "epf_metadata_contextual", "epf_metadata_enabling", "epf_metadata_language"};
			}
		};
	}

	@Override
	public Iterator<EdmMeasurement> iterator() {
		return measurements.iterator();
	}

	public int getNumberOfMeasurements() {
		return getHeaders().size();
	}
}
