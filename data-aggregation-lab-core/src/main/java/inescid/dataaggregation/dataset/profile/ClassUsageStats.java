package inescid.dataaggregation.dataset.profile;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Rdfs;
import inescid.util.StatisticCalcMean;
import inescid.util.datastruct.MapOfInts;

public class ClassUsageStats implements ProfileOfInterface, Serializable {
	private static final long serialVersionUID = 1L;
	
	public enum Sort { COUNT, URI };
		MapOfInts<String> namespacesStats=new MapOfInts<String>();
		MapOfInts<String> propertiesStats=new MapOfInts<String>();
		HashMap<String, PropertyProfiler> propertiesProfiles=new HashMap<>();
		MapOfInts<String> propertiesObjectStats=new MapOfInts<String>();
		int classUseCount=0;
		
//		public HashMap<String, PropertyProfiler> getPropertiesProfiles() {
//			return propertiesProfiles;
//		}
		public MapOfInts<String> getPropertiesStats() {
			return propertiesStats;
		}

		public MapOfInts<String> getNamespacesStats() {
			return namespacesStats;
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
			getNamespacesStats().incrementTo(st.getPredicate().getNameSpace());
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

		public void toCsv(String classUri, CSVPrinter csv, Map<String, String> uriLabels) throws IOException {
			Set<String> allProps=new HashSet<>(getPropertiesStats().keySet());
			allProps.addAll(getPropertiesObjectStats().keySet());
			ArrayList<String> propsSorted = new ArrayList<String>(allProps);
			Collections.sort(propsSorted);

			csv.printRecord(classUri);
			csv.print("property");
			csv.print("property label");
			csv.print("property count");
			csv.print("as range of property count");
			propertiesProfiles.values().iterator().next().printCsvHeaders(csv);
			csv.println();
			
			for(String prop: propsSorted) {
				Integer cntSubject = getPropertiesStats().get(prop);
				Integer cntObject = getPropertiesObjectStats().get(prop);
				csv.print(prop);
				csv.print( uriLabels==null ? "" : uriLabels.get(prop));
				
				csv.print(cntSubject == null ? 0 : cntSubject);
				csv.print(cntObject == null ? 0 : cntObject);
				
				PropertyProfiler propertyProfiler = propertiesProfiles.get(prop);
				if(propertyProfiler!=null)
					propertyProfiler.toCsv(csv);				
				csv.println();
			}
		}
		public void toCsv(CSVPrinter csv, Map<String, String> uriLabels, boolean withNamespaces, Sort sort) throws IOException {
			ArrayList<String> propsSorted=new ArrayList<String>(getPropertiesStats().size());
			if(sort==null || sort==Sort.URI) {
				propsSorted.addAll(getPropertiesStats().getSortedKeys());
			} else {
				propsSorted.addAll(getPropertiesStats().getSortedKeysByInts());
				for(String propObj: getPropertiesObjectStats().keySet()) {
					if(!propsSorted.contains(propObj))
						propsSorted.add(propObj);
				}
			}

			if(withNamespaces) {
				csv.printRecord("namespace", "namespace count");
				List<String> nssSorted = null;
				if(sort==null || sort==Sort.URI) {
					nssSorted=getNamespacesStats().getSortedKeys();
				} else 
					nssSorted=getNamespacesStats().getSortedKeysByInts();
				for(String ns: nssSorted) {
					Integer cntSubject = getNamespacesStats().get(ns);
					csv.printRecord(ns, cntSubject);
				}
			}

			csv.print("property");
			csv.print("property label");
			csv.print("property count");
			csv.print("as range of property count");
			if(!propertiesProfiles.isEmpty())
				propertiesProfiles.values().iterator().next().printCsvHeaders(csv);
			csv.println();
			
			for(String prop: propsSorted) {
				Integer cntSubject = getPropertiesStats().get(prop);
				Integer cntObject = getPropertiesObjectStats().get(prop);
				csv.print(prop);
				csv.print( uriLabels==null ? "" : uriLabels.get(prop));
				
				csv.print(cntSubject == null ? 0 : cntSubject);
				csv.print(cntObject == null ? 0 : cntObject);
				
				PropertyProfiler propertyProfiler = propertiesProfiles.get(prop);
				if(propertyProfiler!=null)
					propertyProfiler.toCsv(csv);				
				csv.println();
			}			
		}
		public String toCsv(Map<String, String> uriLabels, boolean withNamespaces, Sort sort) throws IOException {
			StringBuilder sb=new StringBuilder();
			CSVPrinter csv=new CSVPrinter(sb, CSVFormat.DEFAULT);
			toCsv(csv, uriLabels, withNamespaces, sort);
			return sb.toString();
		}
		public void toCsvOfValueDistribution(String classUri, File outputFolder) throws IOException {
			Set<String> allProps=new HashSet<>(getPropertiesStats().keySet());
			allProps.addAll(getPropertiesObjectStats().keySet());
			ArrayList<String> propsSorted = new ArrayList<String>(allProps);
			for(String prop: allProps) {
				PropertyProfiler propertyProfiler = propertiesProfiles.get(prop);
				if(propertyProfiler!=null) {
					propertyProfiler.toCsvOfValueDistribution(classUri, prop, outputFolder);				
				}
			}
		}
		
		public HashMap<String, PropertyProfiler> getPropertiesProfiles() {
			return propertiesProfiles;
		}

		public void collect(Resource r) {
			if(r==null) return;
			eventInstanceStart(r);
			StmtIterator properties = r.listProperties();
			for(Statement st : properties.toList()) {
				eventProperty(st);
			}
			eventInstanceEnd(r);
			
			StmtIterator propertiesRangeOf = r.getModel().listStatements(null, null, r);
			for(Statement st : propertiesRangeOf.toList()) {
				if(st.getPredicate().equals(Rdf.type)) continue;
					getPropertiesObjectStats().incrementTo(st.getPredicate().getURI());
			}
		}
	}