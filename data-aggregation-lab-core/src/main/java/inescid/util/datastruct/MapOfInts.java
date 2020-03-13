/*
 * Created on Oct 12, 2004
 *
 */
package inescid.util.datastruct;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

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
		public List<K> getSortedKeysByInts(){
			List<K> ret=new ArrayList<K>(size());
			for(Entry<K, Integer> ns: getSortedEntries()) {
				ret.add(ns.getKey());
			}
			return ret;
		}
		public List<K> getSortedKeys(){
			List<K> ret=new ArrayList<K>(keySet());
			Collections.sort((List<Comparable>)ret);
			return ret;
		}

		public void incrementToAll(Iterable<K> addToThese) {
			for(K k: addToThese) 
				incrementTo(k);
		}
		
		public static void writeCsv(MapOfInts<?> sets, Writer csvWrite) throws IOException {
			CSVPrinter printer=new CSVPrinter(csvWrite, CSVFormat.DEFAULT);
			for(Entry<?, Integer>  r: sets.entrySet()) {
				printer.printRecord(r.getKey().toString(), r.getValue().toString());
			}
			printer.close();
		}

		public void addToAll(MapOfInts<K> otherMap) {
			for(java.util.Map.Entry<K, Integer> e: otherMap.entrySet()) 
				addTo(e.getKey(), e.getValue());
		}

}
