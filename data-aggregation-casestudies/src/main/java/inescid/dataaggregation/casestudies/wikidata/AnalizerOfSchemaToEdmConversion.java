package inescid.dataaggregation.casestudies.wikidata;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import inescid.dataaggregation.data.model.Schemaorg;
import inescid.dataaggregation.dataset.convert.rdfconverter.MappingReport;
import inescid.util.MapOfInts;

public class AnalizerOfSchemaToEdmConversion {
//	unmapped types: uses count;
	MapOfInts<String> unmappedTypes=new MapOfInts<String>();
//	unmapped properties: uses count;
//	MapOfInts<String> unmappedProperties=new MapOfInts<String>();
	Map<String, MapOfInts<String>> unmappedProperties=new HashMap<String,  MapOfInts<String>>();
	
	public AnalizerOfSchemaToEdmConversion(MappingReport rpt) {
		try {
			StringBuilder sb=new StringBuilder();
			CSVPrinter printer=new CSVPrinter(sb, CSVFormat.DEFAULT);
			printer.printRecord("Resource","Property","Usage count","Mapped/converted to");
			for(Entry<Resource, Resource[]> e: rpt.getRdfTypeMaps().entrySet()) {
				if(!e.getKey().getURI().startsWith(Schemaorg.NS)) continue;
				if(e.getValue().length==0) {
					unmappedTypes.put(e.getKey().getURI(), rpt.getUsageCounts().get(e.getKey()).get(e.getKey()));
					continue;
				}
				Map<Property, Set<Property>> map = rpt.getPropertiesMaps().get(e.getKey());
				for(Entry<Property, Set<Property>> e2: map.entrySet()) {
					if(!e2.getKey().getURI().startsWith(Schemaorg.NS)) continue;
					if(e2.getValue().size()==0) {
						MapOfInts<String> prop = unmappedProperties.get(e.getKey());
						if(prop==null) {
							prop=new MapOfInts<String>();
							unmappedProperties.put(e.getKey().getURI(), prop);
						}
						prop.addTo(e2.getKey().getURI(), rpt.getUsageCounts().get(e.getKey()).get(e2.getKey()));
					}
				}
				printer.println();
			}
			printer.close();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public String toCsv() {
		StringBuilder sb=new StringBuilder();
		sb.append("Resource/Property,Usage count\n");
		sb.append(unmappedTypes.toCsv());
		for(String type: unmappedProperties.keySet()){
			sb.append(",\n"+type+"\n");
			sb.append(unmappedProperties.get(type).toCsv());
		}
		return sb.toString();
	}
}
