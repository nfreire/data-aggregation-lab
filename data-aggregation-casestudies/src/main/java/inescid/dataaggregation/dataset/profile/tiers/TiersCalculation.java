package inescid.dataaggregation.dataset.profile.tiers;

import java.io.Serializable;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.MetadataTier;

public class TiersCalculation implements Serializable {
	private static final long serialVersionUID = 1L;
	
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