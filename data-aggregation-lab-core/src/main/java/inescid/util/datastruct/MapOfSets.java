/*
 * Created on Oct 12, 2004
 *
 */
package inescid.util.datastruct;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.util.Set;

/**
 * @author Nuno Freire
 *
 */
public class MapOfSets<K,V> implements Serializable {
		Hashtable<K,Set<V>> hashtable;
		
		
		public MapOfSets(){
			hashtable=new Hashtable<K, Set<V>>();
		}

		public MapOfSets(int initialCapacity){
			hashtable=new Hashtable<K, Set<V>>(initialCapacity);
		}

		public MapOfSets(int initialCapacity, int loadFactor){
			hashtable=new Hashtable<K, Set<V>>(initialCapacity,loadFactor);
		}
		
		public boolean contains(K k,V v) {
			Set<V> vs=get(k);
			if(vs==null)
				return false;
			return vs.contains(v); 
		}
		
		public void put(K key, V value){
			Set<V> recs=hashtable.get(key);
			if (recs==null){
				recs=new HashSet<V>();
				hashtable.put(key,recs);
			}
			recs.add(value);
		}

		public void putAll(K key, Collection<V> values){
			Set<V> recs=hashtable.get(key);
			if (recs==null){
				recs=new HashSet<V>();
				hashtable.put(key,recs);
			}
			recs.addAll(values);
		}
		public void putAll(K key, V... values){
			Set<V> recs=hashtable.get(key);
			if (recs==null){
				recs=new HashSet<V>();
				hashtable.put(key,recs);
			}
			for(V v : values)
				recs.add(v);
		}
		
		public void remove(K key, V value){
			Set recs=hashtable.get(key);
			recs.remove(value);
			if (recs.size()==0)
				hashtable.remove(key);
		}
		
		public void remove(K key){
			hashtable.remove(key);
		}
		
		public Set<V> get(K key){
			return hashtable.get(key);
		}
		
		
		public int size() {
			return hashtable.size();
		}

		public int sizeOfKeysAndSets() {
			int totalSize=hashtable.size();
			for(Set s:hashtable.values()) {
				totalSize+=s.size();
			}
			return totalSize;
		}
		
		
		public Set<K> keySet() {
			return hashtable.keySet();
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

		public Set<Entry<K, Set<V>>> entrySet() {
			return hashtable.entrySet();
		}

		public boolean containsKey(K key) {
			return hashtable.containsKey(key);
		}

		public void set(K key, Set<V> valuesSet) {
			hashtable.put(key, valuesSet);
		}

		public void clear() {
			hashtable.clear();
		}

		public static MapOfSets<String, String> readCsv(Reader csvReader) throws IOException {
			MapOfSets<String, String> ret=new MapOfSets<String, String>();
			CSVParser parser=new CSVParser(csvReader, CSVFormat.DEFAULT);
			for(CSVRecord r: parser) {
				String key=r.get(0);
				for(int i=1; i<r.size(); i++) 
					ret.put(key, r.get(i));
			}
			parser.close();
			return ret;
		}

		
		public static void writeCsv(MapOfSets<?,?> sets, Appendable csvWrite) throws IOException {
			CSVPrinter printer=new CSVPrinter(csvWrite, CSVFormat.DEFAULT);
			for(Entry<?, ?>  r: sets.entrySet()) {
				printer.print(r.getKey().toString());
				for(Object v: (Set)r.getValue()) {
					printer.print(v.toString());
				}
				printer.println();
			}
			printer.close();
		}

		public Collection<Set<V>> values() {
			return hashtable.values();
		}

}
