package inescid.dataaggregation.dataset.profile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.csv.CSVPrinter;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import inescid.util.datastruct.MapOfInts;

public class ClassUsageStats implements ProfileOfInterface {
		MapOfInts<String> propertiesStats=new MapOfInts<String>();
		HashMap<String, PropertyProfiler> propertiesProfiles=new HashMap<>();
		MapOfInts<String> propertiesObjectStats=new MapOfInts<String>();
		int classUseCount=0;
		
//		public HashMap<String, PropertyProfiler> getPropertiesProfiles() {
//			return propertiesProfiles;
//		}
		MapOfInts<String> getPropertiesStats() {
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

		public HashMap<String, PropertyProfiler> getPropertiesProfiles() {
			return propertiesProfiles;
		}
	}