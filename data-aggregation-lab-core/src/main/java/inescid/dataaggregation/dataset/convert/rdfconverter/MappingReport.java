package inescid.dataaggregation.dataset.convert.rdfconverter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public  class MappingReport {
		Map<Resource, Map<Resource,Integer>> usageCounts=new HashMap<>(); 
		Map<Resource, Resource[]> rdfTypeMaps=new HashMap<>(); 
		Map<Resource, Map<Property, Set<Property>>> propertiesMaps=new HashMap<>(); //properties, propertiesMerge and derived properties
//		Map<ImmutablePair<Property, Resource>, ResourceTypeConversionSpecification> propertiesMappingFromReferencedResources;
	
		public String toString() {
			StringBuilder sb=new StringBuilder();
			for(Entry<Resource, Resource[]> e: rdfTypeMaps.entrySet()) {
				System.out.print(e.getKey()+" ("+usageCounts.get(e.getKey())+" uses) -");
				if(e.getValue().length==0) {
					System.out.println(" NOT MAPPED");
				} else {
					for(Resource r: e.getValue()) {
						System.out.print(" "+r);
					}
					System.out.println();
				}
				Map<Property, Set<Property>> map = propertiesMaps.get(e.getKey());
				for(Entry<Property, Set<Property>> e2: map.entrySet()) {
					System.out.print("    # "+e2.getKey()+" ("+usageCounts.get(e2.getKey())+"  uses) -");
					if(e2.getValue().size()==0) {
						System.out.println(" NOT MAPPED");
					} else {
						for(Property r: e2.getValue()) {
							System.out.print(" "+r);
						}
						System.out.println();
					}
				}
				System.out.println();
			}
			System.out.println();
			
			return sb.toString();
		}
		
		public String toCsv() {
			try {
				StringBuilder sb=new StringBuilder();
				CSVPrinter printer=new CSVPrinter(sb, CSVFormat.DEFAULT);
				printer.printRecord("Resource","Property","Usage count","Mapped/converted to");
				for(Entry<Resource, Resource[]> e: rdfTypeMaps.entrySet()) {
					printer.print(e.getKey());
					printer.print("");
					printer.print(usageCounts.get(e.getKey()).get(e.getKey()));
					if(e.getValue().length==0) {
						printer.print("-");
					} else {
						for(Resource r: e.getValue()) {
							printer.print(r);
						}
					}
					printer.println();
					Map<Property, Set<Property>> map = propertiesMaps.get(e.getKey());
					for(Entry<Property, Set<Property>> e2: map.entrySet()) {
						printer.print("");
						printer.print(e2.getKey());
						printer.print(usageCounts.get(e.getKey()).get(e2.getKey()));
						if(e2.getValue().size()==0) {
							printer.print("-");
						} else {
							for(Property r: e2.getValue()) {
								printer.print(r);
							}
						}
						printer.println();
					}
					printer.println();
				}
				printer.close();
				return sb.toString();
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}

		public void addPropertyMapInReferencedResource(Property srcSchema, Property trgEdm) {
			for(Entry<Resource, Resource[]> e: rdfTypeMaps.entrySet()) {
				Map<Property, Set<Property>> map = propertiesMaps.get(e.getKey());
				Set<Property> propsAtTrg = map.get(srcSchema);
				if(propsAtTrg!=null)
					propsAtTrg.add(trgEdm);
			}
		}

		public Map<Resource, Resource[]> getRdfTypeMaps() {
			return rdfTypeMaps;
		}

		public Map<Resource, Map<Resource, Integer>> getUsageCounts() {
			return usageCounts;
		}

		public Map<Resource, Map<Property, Set<Property>>> getPropertiesMaps() {
			return propertiesMaps;
		}
		
		
	
	}