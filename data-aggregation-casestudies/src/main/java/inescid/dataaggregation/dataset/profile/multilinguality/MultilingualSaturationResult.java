package inescid.dataaggregation.dataset.profile.multilinguality;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;
import inescid.dataaggregation.data.model.Skos;

public class MultilingualSaturationResult {
	public static final Set<String> CHO_TYPES=new HashSet<String>() {{
		add(Ore.Proxy.getURI());
		add(Edm.ProvidedCHO.getURI());
	}};
	public static final Set<String> CONTEXT_TYPES=new HashSet<String>() {{
		add(Edm.Agent.getURI());
		add(Edm.TimeSpan.getURI());
		add(Edm.Place.getURI());
		add(Skos.Concept.getURI());
	}};
	
	
	int statements=0;
	int langTagCount=0;
	HashSet<String> languages=new HashSet<String>();
	Map<String, MultilingualSaturationResult> byProperty=new HashMap<String, MultilingualSaturationResult>();
	Map<String, MultilingualSaturationResult> byClass=new HashMap<String, MultilingualSaturationResult>();
		
	public MultilingualSaturationResult() {
	}
	
	public void addLangTag(String lang, String typeClass, String property) {
		if(typeClass!=null && !(CONTEXT_TYPES.contains(typeClass) || CHO_TYPES.contains(typeClass)))
			return;
		statements++;
		if(!StringUtils.isEmpty(lang)) {
			langTagCount++;
			languages.add(lang);
		}
		if(typeClass!=null) {
			MultilingualSaturationResult classResult = byClass.get(typeClass);
			if(classResult==null) {
				classResult=new MultilingualSaturationResult();
				byClass.put(typeClass, classResult);
			}
			classResult.addLangTag(lang, null, null);
		}
		if(property!=null) {
			MultilingualSaturationResult propertyResult = byProperty.get(property);
			if(propertyResult==null) {
				propertyResult=new MultilingualSaturationResult();
				byProperty.put(property, propertyResult);
			}
			propertyResult.addLangTag(lang, null, null);
		}
	}
	
	public int getLangTagCount() {
		return langTagCount;
	}
	public void setLangTagCount(int langTagCount) {
		this.langTagCount = langTagCount;
	} 
	public int getLanguagesCount() {
		return languages.size();
	}
	public int getStatementsCount() {
		return statements;
	}

//	public void setLanguagesCountOfClass(String typeClass, int count) {
//		MultilingualSaturationResult classResult = byClass.get(typeClass);
//		if(classResult==null) {
//			classResult=new MultilingualSaturationResult();
//			byClass.put(typeClass, classResult);
//		}
//		classResult.languagesCount=count;
//	}
	
//	public void setLanguagesCountOfProperty(String property, int count) {
//		MultilingualSaturationResult propertyResult = byProperty.get(property);
//		if(propertyResult==null) {
//			propertyResult=new MultilingualSaturationResult();
//			byProperty.put(property, propertyResult);
//		}
//		propertyResult.languagesCount=count;
//	}
	
	
	public MultilingualSaturationResult getContextResult() {
		MultilingualSaturationResult combined=new MultilingualSaturationResult();
		for(String clsUri: CONTEXT_TYPES) {
			combined.add(byClass.get(clsUri));
		}
		return combined;
	}
	public MultilingualSaturationResult getChoResult() {
		MultilingualSaturationResult combined=new MultilingualSaturationResult();
		for(String clsUri: CHO_TYPES) {
			combined.add(byClass.get(clsUri));
		}
		return combined;
	}

	private void add(MultilingualSaturationResult multilingualSaturationResult) {
		if (multilingualSaturationResult==null) return;
		statements+=multilingualSaturationResult.statements;
		langTagCount+=multilingualSaturationResult.langTagCount;
		languages.addAll(multilingualSaturationResult.languages);
	}
	
	
}
