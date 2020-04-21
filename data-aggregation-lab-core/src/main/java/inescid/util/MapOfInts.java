/*
 * Created on Oct 12, 2004
 *
 */
package inescid.util;

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 * @param <K>
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 11 de Abr de 2013
 */
public class MapOfInts<K> extends Hashtable<K, Integer> implements Serializable{
	
		private static final long serialVersionUID=1;	
		
		//Hashtable<K,Object[]> hashtable;
//		Hashtable<K,Integer> hashtable;
//		int listInitialCapacity=-1;
		
		/**
		 * Creates a new instance of this class.
		 */
		public MapOfInts(){
			super();
		}

		/**
		 * Creates a new instance of this class.
		 * @param initialCapacity
		 */
		public MapOfInts(int initialCapacity){
			super(initialCapacity);
		}

		
		/**
		 * @return sum of all ints
		 */
		public int total() {
			int total=0;
			for(K key: keySet()) {
				total+=get(key);
			}
			return total;
		}
		
		

		/**
		 * @param key
		 * @param value
		 */
		public void addTo(K key, Integer value){
			Integer v=get(key);
			if(v!=null) {
				put(key,value+v);
			} else {
				put(key,value);
			}
		}
		
		/**
		 * @param key
		 */
		public void incrementTo(K key){
			Integer v=get(key);
			if(v!=null) {
				put(key,1+v);
			} else {
				put(key,1);
			}
		}

		/**
		 * @param key
		 * @param value
		 */
		public void subtractTo(K key, Integer value){
			Integer v=get(key);
			if(v!=null) {
				put(key, value-v);
			} else {
				put(key, -value);
			}
		}
		
		/**
		 * @param key
		 */
		public void decrementTo(K key){
			Integer v=get(key);
			if(v!=null) {
				put(key,v-1);
			} else {
				put(key,-1);
			}
		}
		
		public List<Entry<K, Integer>> getSortedEntries(){
			ArrayList<Entry<K, Integer>> ret=new ArrayList<>(entrySet());
			Collections.sort(ret, new Comparator<Entry<K, Integer>>() {
				@Override
				public int compare(java.util.Map.Entry<K, Integer> o1, java.util.Map.Entry<K, Integer> o2) {
					return o2.getValue() - o1.getValue();
				}
			});
			return ret;
		}

		public String toCsv(Map<K, String> labels) {
			try {
				StringBuilder sbCsv=new StringBuilder();
				CSVPrinter csv=new CSVPrinter(sbCsv, CSVFormat.DEFAULT);
//				csv.printRecord("key","value");
				for(java.util.Map.Entry<K, Integer> cls: getSortedEntries()) {
					csv.printRecord(cls.getKey().toString(), cls.getValue(), labels==null? null : labels.get(cls.getKey().toString()));
				}		
				csv.close();
				return sbCsv.toString();
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		public String toCsv() {
			return toCsv(null);
		}

		public List<SimpleEntry<K, SimpleEntry<Integer, Double>>> getSortedEntriesWithPercent() {
			List<java.util.Map.Entry<K, Integer>> sortedEntries = getSortedEntries();
			double total=total();
			ArrayList<SimpleEntry<K, SimpleEntry<Integer, Double>>> ret=new ArrayList<>(sortedEntries.size());
			for(Entry<K, Integer> entry : sortedEntries) {
				ret.add(new SimpleEntry<K, SimpleEntry<Integer, Double>>(entry.getKey()
						, new SimpleEntry<>(entry.getValue(), (double) entry.getValue() / total)));
			}
			return ret;
		}

		public StatisticCalcMean getStatistics() {
			StatisticCalcMean stats=new StatisticCalcMean();
			for(K k: keySet()) {
				if(!(k instanceof Number))
					throw new IllegalArgumentException("Keys must be numbers to support this method");
				int cnt=get(k);
				for(int i=0 ; i<cnt; i++) 
					stats.enter(((Number)k).doubleValue());
			}
			return stats;
		}
}
