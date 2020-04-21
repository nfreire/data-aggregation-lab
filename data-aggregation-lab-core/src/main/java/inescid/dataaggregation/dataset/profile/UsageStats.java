package inescid.dataaggregation.dataset.profile;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class UsageStats implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Map of Class URIs -> Property URIs -> #uses
	 */
	Map<String, ClassUsageStats> stats=new HashMap<>();
	public UsageStats() {
	}

	public ClassUsageStats getClassStats(String classURI) {
		ClassUsageStats ret=stats.get(classURI);
		if(ret==null) {
			ret=new ClassUsageStats();
			stats.put(classURI, ret);
		}
		return ret;
	}
	
	public Map<String, ClassUsageStats> getClassesStats() {
		return stats;
	}

	public void finish() {
		for(ClassUsageStats cls: stats.values()) 
			cls.finish();
	}

	public void exportCsvOfValueDistributions(File outputFolder) throws IOException {
		if(!outputFolder.exists())
			outputFolder.mkdirs();
		for(Entry<String, ClassUsageStats> cls: stats.entrySet()) 
			cls.getValue().toCsvOfValueDistribution(cls.getKey(), outputFolder);
	}
	
	
	public String toString() {
		StringBuilder sb=new StringBuilder();
		for(String cls: stats.keySet()) {
			ClassUsageStats clsStats = stats.get(cls);
			sb.append("Class: ").append(clsStats.getClassUseCount()).append(" - ").append(cls).append("\n");
			for(Entry<String, Integer> prop: clsStats.getPropertiesStats().getSortedEntries()) {
				sb.append(String.format("  %5d - %s\n", prop.getValue(), prop.getKey()));
			}
			for(Entry<String, Integer> prop: clsStats.getPropertiesObjectStats().getSortedEntries()) {
				sb.append(String.format("  %5d - %s (as object)\n", prop.getValue(), prop.getKey()));
			}
		}
		return sb.toString();
	}
	public String toCsv() {
		return toCsv(Collections.EMPTY_MAP);
	}
	public String toCsv(Map<String, String> uriLabels) {
		try {
			StringBuilder sbCsv=new StringBuilder();
			CSVPrinter csv=new CSVPrinter(sbCsv, CSVFormat.DEFAULT);
			csv.printRecord("class","class label","class count");
			ArrayList<String> classesSorted = new ArrayList<String>(stats.keySet());
			Collections.sort(classesSorted);
			for(String cls: classesSorted) {
				ClassUsageStats clsStats = stats.get(cls);
				csv.printRecord(cls, uriLabels.get(cls), clsStats.getClassUseCount());
			}		

			for(String cls: classesSorted) {
				csv.printRecord("","");
				ClassUsageStats clsStats = stats.get(cls);
				clsStats.toCsv(cls, csv, uriLabels);
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