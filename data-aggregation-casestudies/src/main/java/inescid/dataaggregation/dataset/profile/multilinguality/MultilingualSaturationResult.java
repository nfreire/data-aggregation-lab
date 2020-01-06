package inescid.dataaggregation.dataset.profile.multilinguality;

import java.util.Collection;
import java.util.HashSet;

public class MultilingualSaturationResult {
	int langTagCount;
	int languagesCount;
	
	public MultilingualSaturationResult(ShapesDetectionReport report) {
		Collection<ShapeInstance> allInstances = report.getAllInstances();
		langTagCount=allInstances.size();
		HashSet<String> langSet=new HashSet<String>();
		for(ShapeInstance inst: allInstances) {
			if(inst.getValueNode().isLiteral())
				langSet.add(inst.getValueNode().getLiteralLanguage());
		}
		languagesCount=langSet.size();
	}
	public int getLangTagCount() {
		return langTagCount;
	}
	public void setLangTagCount(int langTagCount) {
		this.langTagCount = langTagCount;
	} 
	public int getLanguagesCount() {
		return languagesCount;
	}
	public void setLanguagesCount(int languagesCount) {
		this.languagesCount = languagesCount;
	}
	
	
}
