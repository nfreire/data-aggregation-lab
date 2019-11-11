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
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.model.TierClassifier;
import eu.europeana.indexing.utils.RdfTierUtils;
import eu.europeana.indexing.utils.RdfWrapper;

public class TiersNuno {
	public static void main(String[] args) throws Exception {
		String edmRdf="...";
		RDF rdf=new StringToFullBeanConverter().convertStringToRdf(edmRdf); 
	    final RdfWrapper rdfWrapper = new RdfWrapper(rdf);
		ClassifierFactory.getMediaClassifier().classify(rdfWrapper);
		ClassifierFactory.getMetadataClassifier().classify(rdfWrapper);
		MetadataTier metadataTierLanguage = new LanguageClassifier().classify(rdfWrapper); 
		MetadataTier metadataTierEnablingElements = new EnablingElementsClassifier().classify(rdfWrapper);
		MetadataTier metadataTierContextualClass = new ContextualClassClassifier().classify(rdfWrapper);
		 
	    
	    
	}
}
