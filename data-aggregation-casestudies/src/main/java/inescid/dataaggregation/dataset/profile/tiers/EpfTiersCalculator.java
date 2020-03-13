package inescid.dataaggregation.dataset.profile.tiers;

import java.util.Arrays;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.fullbean.StringToFullBeanConverter;
import eu.europeana.indexing.tiers.ClassifierFactory;
import eu.europeana.indexing.tiers.metadata.ContextualClassClassifier;
import eu.europeana.indexing.tiers.metadata.EnablingElementsClassifier;
import eu.europeana.indexing.tiers.metadata.LanguageClassifier;
import eu.europeana.indexing.tiers.model.CombinedClassifier;
import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.model.TierClassifier;
import eu.europeana.indexing.utils.RdfTierUtils;
import eu.europeana.indexing.utils.RdfWrapper;

public class EpfTiersCalculator {
	
	public static class TiersCalculation {
		private MetadataTier language;
		private MetadataTier enablingElements; 
		private MetadataTier contextualClass; 
		private MetadataTier metadata; 
		private MediaTier content;
		
		public MetadataTier getLanguage() {
			return language;
		}
		public void setLanguage(MetadataTier language) {
			this.language = language;
		}
		public MetadataTier getEnablingElements() {
			return enablingElements;
		}
		public void setEnablingElements(MetadataTier enablingElements) {
			this.enablingElements = enablingElements;
		}
		public MetadataTier getContextualClass() {
			return contextualClass;
		}
		public void setContextualClass(MetadataTier contextualClass) {
			this.contextualClass = contextualClass;
		}
		public MetadataTier getMetadata() {
			return metadata;
		}
		public void setMetadata(MetadataTier metadata) {
			this.metadata = metadata;
		}
		public MediaTier getContent() {
			return content;
		}
		public void setContent(MediaTier content) {
			this.content = content;
		}
	}
		
	public static TiersCalculation calculate(String edmRdf) throws Exception {
		RDF rdf=new StringToFullBeanConverter().convertStringToRdf(edmRdf); 
	    final RdfWrapper rdfWrapper = new RdfWrapper(rdf);
	    TiersCalculation result=new TiersCalculation();
		result.setContent(ClassifierFactory.getMediaClassifier().classify(rdfWrapper));
		result.setMetadata(ClassifierFactory.getMetadataClassifier().classify(rdfWrapper));
		result.setLanguage(new LanguageClassifier().classify(rdfWrapper)); 
		result.setEnablingElements(new EnablingElementsClassifier().classify(rdfWrapper));
		result.setContextualClass(new ContextualClassClassifier().classify(rdfWrapper));
		return result;
	}
}
