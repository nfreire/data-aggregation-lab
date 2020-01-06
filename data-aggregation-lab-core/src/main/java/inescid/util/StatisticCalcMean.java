package inescid.util;

import java.io.Serializable;


/**
 * An object of class StatCalc can be used to compute several simple statistics
for a set of numbers.  Numbers are entered into the dataset using
the enter(double) method.  Methods are provided to return the following
statistics for the set of numbers that have been entered: The number
of items, the sum of the items, the average, the standard deviation,
the maximum, and the minimum.
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 10 de Out de 2012
 */
public class StatisticCalcMean implements Serializable{
	private static final long serialVersionUID=1;	
	
    private double count;   // Number of numbers that have been entered.
    private double mean;  // The mean of all the items that have been entered.
    private double squareSumMean;  // The mean of the squares of all the items.
    private double max = Double.NEGATIVE_INFINITY;  // Largest item seen.
    private double min = Double.POSITIVE_INFINITY;  // Smallest item seen.
 
    /**
     * Add a value
     * 
     * @param num
     */
    public void enter(double num) {
       // Add the number to the dataset.
        mean=(mean*(count/(count+1)) + (num/(count+1))) ;
        squareSumMean=(squareSumMean*(count/(count+1)) + (num*num/(count+1))) ;
       count++;
       if (num > max)
          max = num;
       if (num < min)
          min = num;
    }
 
    /**
     * @return number of items that have been entered.
     */
    public double getCount() {   
       return count;
    }
 
    /**
     * @return the sum of all the items that have been entered.
     */
    public double getSum() {
       return mean*count;
    }
 
    /**
     * @return average of all the items that have been entered. Value is Double.NaN if count == 0.
     */
    public double getMean() {
       return mean;  
    }
 
    /**
     * @return standard deviation of all the items that have been entered. Value will be Double.NaN if count == 0.
     */
    public double getStandardDeviation() {  
       return Math.sqrt( squareSumMean - mean*mean );
    }
 
    /**
     * @return standard deviation of all the items that have been entered. Value will be Double.NaN if count == 0
     */
    public double getVariance() {  
       return squareSumMean - mean*mean ;
    }
    
    /**
     * @return the smallest item that has been entered. Value will be infinity if no items have been entered.
     */
    public double getMin() {
       return min;
    }
    
    /**
     * @return the largest item that has been entered. Value will be -infinity if no items have been entered.
     */
    public double getMax() {
       return max;
    }
 
    @Override
	public String toString() {
		return String.format("cnt-%.0f max-%1.3f min-%1.3f mean-%1.3f", count, max, min, getMean());    
	}
	
	public StatisticCalcMean copy() {
		StatisticCalcMean copy=new StatisticCalcMean();
		copy.count=this.count;
		copy.mean=this.mean;
		copy.squareSumMean=this.squareSumMean;
		copy.max=this.max;
		copy.min=this.min;
		return copy;
	}
	

}
