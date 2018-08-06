package inescid.dataaggregation.dataset.profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import inescid.util.MapOfInts;

import java.util.Set;

public class UsageStats {
	
	public class ClassUsageStats {
		MapOfInts<String> propertiesStats=new MapOfInts();
		MapOfInts<String> propertiesObjectStats=new MapOfInts();
		int classUseCount=0;
		
		public MapOfInts<String> getPropertiesStats() {
			return propertiesStats;
		}
		
		public MapOfInts<String> getPropertiesObjectStats() {
			return propertiesObjectStats;
		}
		
		public int getClassUseCount() {
			return classUseCount;
		}

		public void incrementClassUseCount() {
			classUseCount++;
		}
	}
	
	/**
	 * Map of Class URIs -> Property URIs -> #uses
	 */
	Map<String, ClassUsageStats> stats=new HashMap<>();

	public ClassUsageStats getClassStats(String classURI) {
		ClassUsageStats ret=stats.get(classURI);
		if(ret==null) {
			ret=new ClassUsageStats();
			stats.put(classURI, ret);
		}
		return ret;
	}
	
	public String toString() {
		StringBuilder sb=new StringBuilder();
		for(String cls: stats.keySet()) {
			ClassUsageStats clsStats = stats.get(cls);
			sb.append("Class: ").append(clsStats.getClassUseCount()).append(" - ").append(cls).append("\n");
			for(Entry<String, Integer> prop: clsStats.getPropertiesStats().getSortedEntries()) {
				sb.append(String.format("  %5d - %s\n", prop.getValue(), prop.getKey()));
			}
		}
		return sb.toString();
	}
	public String toCsv() {
		StringBuilder sb=new StringBuilder();
		sb.append("class,class count\n");
//		sb.append("class,class count,property,property count,edm mapping class,edm mapping property,mapping notes\n");
		ArrayList<String> classesSorted = new ArrayList<String>(stats.keySet());
		Collections.sort(classesSorted);
		for(String cls: classesSorted) {
			ClassUsageStats clsStats = stats.get(cls);
			sb.append(cls).append(",").append(clsStats.getClassUseCount());
			sb.append("\n");
		}		
		
		sb.append("\n");
		
		for(String cls: classesSorted) {
			ClassUsageStats clsStats = stats.get(cls);
			sb.append(cls);
			sb.append("\nproperty,property count,as range of property count\n");
	//		sb.append("class,class count,property,property count,edm mapping class,edm mapping property,mapping notes\n");
			Set<String> allProps=new HashSet<>(clsStats.getPropertiesStats().keySet());
			allProps.addAll(clsStats.getPropertiesObjectStats().keySet());
			
			ArrayList<String> propsSorted = new ArrayList<String>(allProps);
			Collections.sort(propsSorted);
			for(String prop: propsSorted) {
				Integer cntSubject = clsStats.getPropertiesStats().get(prop);
				Integer cntObject = clsStats.getPropertiesObjectStats().get(prop);
				sb.append(String.format("%s,%d,%d\n", prop, cntSubject == null ? 0 : cntSubject, cntObject == null ? 0 : cntObject));
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public String toCsvCombined(UsageStats combineWith) {
		StringBuilder sb=new StringBuilder();
		sb.append("Class URI,class count A,class count B\n");
//		sb.append("class,class count,property,property count,edm mapping class,edm mapping property,mapping notes\n");
		Set<String> classes = new HashSet<>(stats.keySet());
		classes.addAll(combineWith.stats.keySet()); 
		ArrayList<String> classesSorted = new ArrayList<String>(classes);
		Collections.sort(classesSorted);
		for(String cls: classesSorted) {
			ClassUsageStats clsStats = stats.get(cls);
			if(clsStats==null) clsStats=new ClassUsageStats();
			ClassUsageStats clsStatsOther = combineWith.stats.get(cls);
			if(clsStatsOther==null) clsStatsOther=new ClassUsageStats();
			sb.append(prefixNamespace(cls)).append(",").append(clsStats.getClassUseCount());
			sb.append(",").append(clsStatsOther.getClassUseCount());
			sb.append("\n");
		}		
		
		sb.append("\n");
		
		for(String cls: classesSorted) {
			ClassUsageStats clsStats = stats.get(cls);
			if(clsStats==null) clsStats=new ClassUsageStats();
			ClassUsageStats clsStatsOther = combineWith.stats.get(cls);
			if(clsStatsOther==null) clsStatsOther=new ClassUsageStats();
			sb.append(prefixNamespace(cls));
			sb.append("\nproperty,property count A,property count B\n");
			//		sb.append("class,class count,property,property count,edm mapping class,edm mapping property,mapping notes\n");
			Set<String> allProps=new HashSet<>(clsStats.getPropertiesStats().keySet());
			allProps.addAll(clsStats.getPropertiesObjectStats().keySet());
			allProps.addAll(clsStatsOther.getPropertiesStats().keySet());
			allProps.addAll(clsStatsOther.getPropertiesObjectStats().keySet());
			
			ArrayList<String> propsSorted = new ArrayList<String>(allProps);
			Collections.sort(propsSorted);
			sb.append("Usage as subject\n");
			for(String prop: propsSorted) {
				Integer cntSubject = clsStats.getPropertiesStats().get(prop);
				Integer cntSubjectOther = clsStatsOther.getPropertiesStats().get(prop);
				if(cntSubject!=null || cntSubjectOther!=null)
					sb.append(String.format("%s,%d,%d\n", prefixNamespace(prop), cntSubject == null ? 0 : cntSubject
							, cntSubjectOther == null ? 0 : cntSubjectOther));
			}
			sb.append("Usage as object\n");
			for(String prop: propsSorted) {
				Integer cntObject = clsStats.getPropertiesObjectStats().get(prop);
				Integer cntObjectOther = clsStatsOther.getPropertiesObjectStats().get(prop);
				if(cntObject!=null || cntObjectOther!=null)
				sb.append(String.format("%s,%d,%d\n", prefixNamespace(prop), cntObject == null ? 0 : cntObject
						, cntObjectOther == null ? 0 : cntObjectOther));
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	private String prefixNamespace(String prop) {
		if(prop.startsWith("http://schema.org/"))
			return "schema:"+prop.substring("http://schema.org/".length());
		if(prop.startsWith("http://purl.org/dc/elements/1.1/"))
			return "dc:"+prop.substring("http://purl.org/dc/elements/1.1/".length());
		if(prop.startsWith("http://purl.org/dc/terms/"))
			return "dcterms:"+prop.substring("http://purl.org/dc/terms/".length());
		if(prop.startsWith("http://www.europeana.eu/schemas/edm/"))
			return "edm:"+prop.substring("http://www.europeana.eu/schemas/edm/".length());
		if(prop.startsWith("http://rdvocab.info/ElementsGr2/"))
			return "rdagr2:"+prop.substring("http://rdvocab.info/ElementsGr2/".length());
		if(prop.startsWith("http://www.openarchives.org/ore/terms/"))
			return "ore:"+prop.substring("http://www.openarchives.org/ore/terms/".length());
		if(prop.startsWith("http://www.w3.org/2004/02/skos/core#"))
			return "skos:"+prop.substring("http://www.w3.org/2004/02/skos/core#".length());
		if(prop.startsWith("http://www.w3.org/2002/07/owl#"))
			return "owl:"+prop.substring("http://www.w3.org/2002/07/owl#".length());
		if(prop.startsWith("http://www.w3.org/2000/01/rdf-schema#"))
			return "rdfs:"+prop.substring("http://www.w3.org/2000/01/rdf-schema#".length());
		if(prop.startsWith("http://dp.la/terms/"))
			return "dpla:"+prop.substring("http://dp.la/terms/".length());
		if(prop.startsWith("http://www.w3.org/2003/01/geo/wgs84_pos#"))
			return "wgs84pos:"+prop.substring("http://www.w3.org/2003/01/geo/wgs84_pos#".length());
		if(prop.startsWith("http://xmlns.com/foaf/0.1/"))
			return "foaf:"+prop.substring("http://xmlns.com/foaf/0.1/".length());
		return prop;
	}
	
}