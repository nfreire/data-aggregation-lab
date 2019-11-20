package inescid.dataaggregation.dataset.profile.multilinguality;

public class MultilingualSaturationResult {
	int langTagCount;
	int languagesCount;
	
	public MultilingualSaturationResult(ShapesDetectionReport report) {
		langTagCount=report.getAllInstances().size();
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
