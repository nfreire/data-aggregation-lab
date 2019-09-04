/*
 * Created on Oct 12, 2004
 *
 */
package inescid.util.datastruct;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Nuno Freire
 *
 */
public class MapOfMapsOfInts<K1, K2> implements Serializable{
		private static final long serialVersionUID=1;	
		Map<K1, MapOfInts<K2>> map;
		
		public MapOfMapsOfInts(){
			map=new HashMap<K1, MapOfInts<K2>>();
		}

		public MapOfMapsOfInts(int initialCapacity){
			map=new HashMap<K1, MapOfInts<K2>>(initialCapacity);
		}

		public MapOfMapsOfInts(int initialCapacity, int loadFactor){
			map=new HashMap<K1, MapOfInts<K2>>(initialCapacity,loadFactor);
		}
		
		public void put(K1 key, K2 key2, Integer value){
			MapOfInts<K2> recs=get(key);
			if (recs==null){
				recs=new MapOfInts<K2>();
				map.put(key,recs);
			}
			recs.put(key2,value);
		}
		
		public void putAll(K1 key, MapOfInts<K2> subMap){
			MapOfInts<K2> recs=get(key);
			if (recs==null){				
				map.put(key,subMap);
			}else {
				recs.putAll(subMap);
			}
		}

		public void remove(K1 key, K2 key2){
			MapOfInts<K2> recs=get(key);
			recs.remove(key2);
			if (recs.size()==0)
				map.remove(key);
		}
		
		public void remove(K1 key){
			map.remove(key);
		}
		
		public MapOfInts<K2> get(K1 key){
			return map.get(key);
		}

		public Integer get(K1 key, K2 key2){
			MapOfInts<K2> midMap=get(key);
			if (midMap==null)
				return null;
			return midMap.get(key2);
		}
		
		public Set<K1> keySet(){
			return map.keySet();
		}
		
		public int sizeTotal() {
			int size=0;
			for(K1 key: map.keySet()) {
				MapOfInts<K2> recs=get(key);
				size+=recs.size();
			}
			return size;
		}
		
		public int size() {
			return map.size();
		}
		
		
		
		public boolean containsKey(K1 key, K2 key2){
			MapOfInts<K2> midMap=get(key);
			if (midMap==null)
				return false;
			return midMap.containsKey(key2);
		}
		
		
		public ArrayList<Integer> valuesOfAllMaps() {
			ArrayList<Integer> ret=new ArrayList<Integer>(sizeTotal());
			for(K1 key: map.keySet()) {
				ret.addAll(get(key).values());
			}
			return ret;
		}
		
		
		/**
		 * @param key
		 * @param value
		 */
		public void addTo(K1 key, K2 key2, Integer value){
			Integer v=get(key, key2);
			if(v!=null) {
				put(key, key2,value+v);
			} else {
				put(key, key2,value);
			}
		}
		
		/**
		 * @param key
		 */
		public void incrementTo(K1 key, K2 key2){
			Integer v=get(key, key2);
			if(v!=null) {
				put(key, key2,1+v);
			} else {
				put(key, key2,1);
			}
		}

		/**
		 * @param key
		 * @param value
		 */
		public void subtractTo(K1 key, K2 key2, Integer value){
			Integer v=get(key, key2);
			if(v!=null) {
				put(key, key2, value-v);
			} else {
				put(key, key2, -value);
			}
		}
		
		/**
		 * @param key
		 */
		public void decrementTo(K1 key, K2 key2){
			Integer v=get(key, key2);
			if(v!=null) {
				put(key, key2,v-1);
			} else {
				put(key, key2,-1);
			}
		}
		

		public List<Entry<K1, MapOfInts<K2>>> getSortedEntriesByKey1(){
			ArrayList<Entry<K1, MapOfInts<K2>>> ret=new ArrayList<>(map.entrySet());
			Collections.sort(ret, new Comparator<Entry<K1, MapOfInts<K2>>>() {
				@Override
				public int compare(java.util.Map.Entry<K1, MapOfInts<K2>> o1, java.util.Map.Entry<K1, MapOfInts<K2>> o2) {
					return o2.getValue().total() - o1.getValue().total();
				}
			});
			return ret;
		}
		
		public List<Entry<K1, List<Entry<K2,Integer>>>> getSortedEntries(){
			ArrayList<Entry<K1, List<Entry<K2,Integer>>>>ret=new ArrayList<Map.Entry<K1,List<Entry<K2,Integer>>>>(map.size()) ;
			List<Entry<K1, MapOfInts<K2>>> domainsSorted=getSortedEntriesByKey1();
			for(Entry<K1, MapOfInts<K2>> e1: domainsSorted) {
				ret.add(new AbstractMap.SimpleEntry<K1, List<Entry<K2,Integer>>>(e1.getKey(), e1.getValue().getSortedEntries()));
			}
			return ret;
		}
		
		
		/**
		 * toString methode: creates a String representation of the object
		 * @return the String representation
		 * @author info.vancauwenberge.tostring plugin

		 */
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("Index[");
			buffer.append(super.toString());
			buffer.append("]");
			return buffer.toString();
		}

}
