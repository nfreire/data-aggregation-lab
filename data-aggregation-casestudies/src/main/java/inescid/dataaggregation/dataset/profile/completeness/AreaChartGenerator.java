package inescid.dataaggregation.dataset.profile.completeness;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.AreaRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.util.ExportUtils;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import inescid.util.datastruct.MapOfInts;

public class AreaChartGenerator {
	private static final int chartImageWidth=550;
	private static final int chartImageHeight=200;
	
	public static void generateChart(List<Double> scores, String datasetName, File chartsFolder) throws FileNotFoundException, IOException {
		CategoryDataset ds = createDataset(scores, 5);
		
        JFreeChart chart = createChart(ds, datasetName, scores.size());
        ExportUtils.writeAsPNG(chart, chartImageWidth, chartImageHeight, new File(chartsFolder, URLEncoder.encode(datasetName, "UTF-8") + ".png"));
	}
	public static void generateChartOldCompleteness(List<Integer> scores, String datasetName, File chartsFolder) throws FileNotFoundException, IOException {
		CategoryDataset ds = createDatasetOldCompleteness(scores);
		
		JFreeChart chart = createChartOldCompleteness(ds, datasetName, scores.size());
		ExportUtils.writeAsPNG(chart, chartImageWidth, chartImageHeight, new File(chartsFolder, URLEncoder.encode(datasetName, "UTF-8") + "_old_completeness.png"));
	}
	

	 private static CategoryDataset createDataset(List<Double> scores, int subTierIntervals) {
		DecimalFormat categFormat = new DecimalFormat("#.##");
		
		MapOfInts<String> categories=new MapOfInts<>();
		double discreteInterval=1f / subTierIntervals;
		for(Double score : scores) {
			double tier=Math.floor(score);
			double discreteCateg = Math.floor( (score - tier) / discreteInterval );
			discreteCateg = tier + discreteCateg * discreteInterval;
			categories.incrementTo(categFormat.format(discreteCateg));
		}
		 
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for(int tier=0; tier<TiersDqcCompletenessCalculator.TOTAL_TIERS; tier++ ) {
			for(int tierInterval=0; tierInterval<subTierIntervals; tierInterval++ ) {
				String categ = categFormat.format(tier+tierInterval*discreteInterval);
				Integer count = categories.get(categ);
				if(count==null) count=0;
				dataset.addValue(count, "Score", categ);
			}
		}
		dataset.addValue(0, "Score", categFormat.format(TiersDqcCompletenessCalculator.TOTAL_TIERS));
        return dataset;
     } 
	 private static CategoryDataset createDatasetOldCompleteness(List<Integer> scores) {
//		 DecimalFormat categFormat = new DecimalFormat("##");
		 
		 MapOfInts<String> categories=new MapOfInts<>();
		 for(Integer score : scores) {
			 categories.incrementTo(score.toString());
		 }
		 
		 DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		 for(int score=0; score<=10; score++ ) {
				 String categ = String.valueOf(score);
				 Integer count = categories.get(categ);
				 if(count==null) count=0;
				 dataset.addValue(count, "Score", categ);
		 }
		 return dataset;
	 } 

	 
    private static JFreeChart createChartOldCompleteness(CategoryDataset dataset, String datasetName, int datasetSize) {
	        JFreeChart chart = ChartFactory.createAreaChart(
	            null, null, 
	                "Record count" /* y-axis label */, dataset);
//	        JFreeChart chart = ChartFactory.createAreaChart(
//	        		"Current Completeness Measure", null, 
//	        		"Record count" /* y-axis label */, dataset);
//	        chart.addSubtitle(new TextTitle("Dataset "+datasetName));
	        chart.setBackgroundPaint(Color.WHITE);
	    	chart.removeLegend();
	        CategoryPlot plot = (CategoryPlot) chart.getPlot();
			
	        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
	        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
	        rangeAxis.setRange(new Range(0, datasetSize));
	        //	        CategoryAxis categAxis = plot.getDomainAxis();
	        
//	        categAxis.setMaximumCategoryLabelWidthRatio(categAxis.getMaximumCategoryLabelWidthRatio()*1.5f);
//	        categAxis.setCategoryMargin(categAxis.getMaximumCategoryLabelWidthRatio()*1.5);
//	        Font tickLabelFont = categAxis.getTickLabelFont();
//	        System.out.println(tickLabelFont.getSize());
//	        categAxis.setTickLabelFont(new Font(tickLabelFont.getFontName(), tickLabelFont.getStyle(), tickLabelFont.getSize()-2));
	        
	        
	        CategoryItemRenderer renderer = plot.getRenderer();
//	        renderer.setDrawBarOutline(false);
//	        chart.getLegend().setFrame(BlockBorder.NONE);
	        renderer.setSeriesPaint(0, Color.BLUE);
	        return chart;
	    }
	    private static JFreeChart createChart(CategoryDataset dataset, String datasetName, int datasetSize) {
//	    	JFreeChart chart = ChartFactory.createAreaChart(
//	    			"DQC/PF Completeness Measure", null, 
//	    			"Record count" /* y-axis label */, dataset);
//	    	chart.addSubtitle(new TextTitle("Dataset "+datasetName));
	    	JFreeChart chart = ChartFactory.createAreaChart(
	    			null, null, 
	    			"Record count" /* y-axis label */, dataset);
	    	chart.setBackgroundPaint(Color.WHITE);
	    	chart.removeLegend();
	    	CategoryPlot plot = (CategoryPlot) chart.getPlot();
	    	
	    	NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
	    	rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
	    	CategoryAxis categAxis = plot.getDomainAxis();
//	        categAxis.setMaximumCategoryLabelWidthRatio(categAxis.getMaximumCategoryLabelWidthRatio()*1.5f);
//	        categAxis.setCategoryMargin(categAxis.getMaximumCategoryLabelWidthRatio()*1.5);
//	        Font tickLabelFont = categAxis.getTickLabelFont();
//	        System.out.println(tickLabelFont.getSize());
//	        categAxis.setTickLabelFont(new Font(tickLabelFont.getFontName(), tickLabelFont.getStyle(), tickLabelFont.getSize()-2));
	    	
	    	
	    	AreaRenderer renderer = (AreaRenderer) plot.getRenderer();
	        rangeAxis.setRange(new Range(0, datasetSize));
//	        renderer.setDrawBarOutline(false);
//	    	chart.getLegend().setFrame(BlockBorder.NONE);
	    	return chart;
	    }
	
	    
	    private static double round(double value, int places) {
	        BigDecimal bd = new BigDecimal(Double.toString(value));
	        bd = bd.setScale(places, RoundingMode.HALF_UP);
	        return bd.doubleValue();
	    }
}
