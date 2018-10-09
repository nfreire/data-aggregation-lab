package inescid.dataaggregation.dataset.profile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import inescid.util.MapOfInts;

import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

public class UsageStats {
	
	public class ClassUsageStats implements ProfileOfInterface {
		MapOfInts<String> propertiesStats=new MapOfInts();
		HashMap<String, PropertyProfiler> propertiesProfiles=new HashMap<>();
		MapOfInts<String> propertiesObjectStats=new MapOfInts();
		int classUseCount=0;
		
//		public HashMap<String, PropertyProfiler> getPropertiesProfiles() {
//			return propertiesProfiles;
//		}
		private MapOfInts<String> getPropertiesStats() {
			return propertiesStats;
		}
		
		public MapOfInts<String> getPropertiesObjectStats() {
			return propertiesObjectStats;
		}
		
		public int getClassUseCount() {
			return classUseCount;
		}

		private void incrementClassUseCount() {
			classUseCount++;
		}
		
		@Override
		public void eventInstanceStart(Resource resource) {
			incrementClassUseCount();
			for (PropertyProfiler prof: propertiesProfiles.values()) {
				prof.eventInstanceStart(resource);
			}
		}
		
		@Override
		public void eventInstanceEnd(Resource resource) {
			for (PropertyProfiler prof: propertiesProfiles.values()) {
				prof.eventInstanceEnd(resource);
			}
		}
		
		@Override
		public void eventProperty(Statement st) {
			getPropertiesStats().incrementTo(st.getPredicate().getURI());
			PropertyProfiler propertyProfiler = propertiesProfiles.get(st.getPredicate().getURI());
			if(propertyProfiler==null) {
				propertyProfiler=new PropertyProfiler();
				propertiesProfiles.put(st.getPredicate().getURI(), propertyProfiler);
			}
			propertyProfiler.eventProperty(st);
		}

		@Override
		public void finish() {
			for (PropertyProfiler prof: propertiesProfiles.values()) {
				prof.finish();
			}
		}

		public void toCsv(String classUri, CSVPrinter csv) throws IOException {
			Set<String> allProps=new HashSet<>(getPropertiesStats().keySet());
			allProps.addAll(getPropertiesObjectStats().keySet());
			ArrayList<String> propsSorted = new ArrayList<String>(allProps);
			Collections.sort(propsSorted);

			csv.printRecord(classUri);
			csv.print("property");
			csv.print("property count");
			csv.print("as range of property count");
			propertiesProfiles.values().iterator().next().printCsvHeaders(csv);
			csv.println();
			
			for(String prop: propsSorted) {
				Integer cntSubject = getPropertiesStats().get(prop);
				Integer cntObject = getPropertiesObjectStats().get(prop);
				csv.print(prop);
				csv.print(cntSubject == null ? 0 : cntSubject);
				csv.print(cntObject == null ? 0 : cntObject);
				
				PropertyProfiler propertyProfiler = propertiesProfiles.get(prop);
				if(propertyProfiler!=null)
					propertyProfiler.toCsv(csv);				
				csv.println();
			}
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
	

	public void finish() {
		for(ClassUsageStats cls: stats.values()) 
			cls.finish();
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
		try {
			StringBuilder sbCsv=new StringBuilder();
			CSVPrinter csv=new CSVPrinter(sbCsv, CSVFormat.DEFAULT);
			csv.printRecord("class","class count");
			ArrayList<String> classesSorted = new ArrayList<String>(stats.keySet());
			Collections.sort(classesSorted);
			for(String cls: classesSorted) {
				ClassUsageStats clsStats = stats.get(cls);
				csv.printRecord(cls,clsStats.getClassUseCount());
			}		

			for(String cls: classesSorted) {
				csv.printRecord("","");
				ClassUsageStats clsStats = stats.get(cls);
				clsStats.toCsv(cls, csv);
			}
			csv.close();
			return sbCsv.toString();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public String toCsvCombined(UsageStats combineWith) {
		try {
			StringBuilder sbCsv = new StringBuilder();
			CSVPrinter csv = new CSVPrinter(sbCsv, CSVFormat.DEFAULT);
			csv.printRecord("Class URI","class count A","class count B");
			// sb.append("class,class count,property,property count,edm mapping class,edm
			// mapping property,mapping notes\n");
			Set<String> classes = new HashSet<>(stats.keySet());
			classes.addAll(combineWith.stats.keySet());
			ArrayList<String> classesSorted = new ArrayList<String>(classes);
			Collections.sort(classesSorted);
			for (String cls : classesSorted) {
				ClassUsageStats clsStats = stats.get(cls);
				if (clsStats == null)
					clsStats = new ClassUsageStats();
				ClassUsageStats clsStatsOther = combineWith.stats.get(cls);
				if (clsStatsOther == null)
					clsStatsOther = new ClassUsageStats();
				csv.printRecord(prefixNamespace(cls),clsStats.getClassUseCount(),clsStatsOther.getClassUseCount());
			}

			csv.printRecord("","");

			for (String cls : classesSorted) {
				ClassUsageStats clsStats = stats.get(cls);
				if (clsStats == null)
					clsStats = new ClassUsageStats();
				ClassUsageStats clsStatsOther = combineWith.stats.get(cls);
				if (clsStatsOther == null)
					clsStatsOther = new ClassUsageStats();
				csv.printRecord(prefixNamespace(cls));
				csv.printRecord("property","property count A","property count B");
				Set<String> allProps = new HashSet<>(clsStats.getPropertiesStats().keySet());
				allProps.addAll(clsStats.getPropertiesObjectStats().keySet());
				allProps.addAll(clsStatsOther.getPropertiesStats().keySet());
				allProps.addAll(clsStatsOther.getPropertiesObjectStats().keySet());

				ArrayList<String> propsSorted = new ArrayList<String>(allProps);
				Collections.sort(propsSorted);
				csv.printRecord("Usage as subject");
				for (String prop : propsSorted) {
					Integer cntSubject = clsStats.getPropertiesStats().get(prop);
					Integer cntSubjectOther = clsStatsOther.getPropertiesStats().get(prop);
					if (cntSubject != null || cntSubjectOther != null)
						csv.printRecord(prefixNamespace(prop), cntSubject == null ? 0 : cntSubject, cntSubjectOther == null ? 0 : cntSubjectOther);
				}
				csv.printRecord("Usage as object");
				for (String prop : propsSorted) {
					Integer cntObject = clsStats.getPropertiesObjectStats().get(prop);
					Integer cntObjectOther = clsStatsOther.getPropertiesObjectStats().get(prop);
					if (cntObject != null || cntObjectOther != null)
						csv.printRecord(prefixNamespace(prop), cntObject == null ? 0 : cntObject, cntObjectOther == null ? 0 : cntObjectOther);
				}
				csv.printRecord("","");
			}
			return sbCsv.toString();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
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