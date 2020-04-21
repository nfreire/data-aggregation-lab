package inescid.dataaggregation.dataset.profile.multilinguality;

import inescid.util.StatisticCalcMean;

public class MultilingualSaturationStats {
	StatisticCalcMean statsChoLangs=new StatisticCalcMean();
	StatisticCalcMean statsChoTags=new StatisticCalcMean();
	StatisticCalcMean statsContextLangs=new StatisticCalcMean();
	StatisticCalcMean statsContextTags=new StatisticCalcMean();
	StatisticCalcMean statsLangs=new StatisticCalcMean();
	StatisticCalcMean statsTags=new StatisticCalcMean();
	
	public void add(MultilingualSaturationResult result) {
		statsLangs.enter(result.getLanguagesCount());
		statsChoLangs.enter(result.getChoResult().getLanguagesCount());
		statsContextLangs.enter(result.getContextResult().getLanguagesCount());
		statsTags.enter(result.getLangTagCount());
		statsChoTags.enter(result.getChoResult().getLangTagCount());
		statsContextTags.enter(result.getContextResult().getLangTagCount());
	}
	
	
}
