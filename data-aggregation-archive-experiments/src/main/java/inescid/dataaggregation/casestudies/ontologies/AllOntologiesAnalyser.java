package inescid.dataaggregation.casestudies.ontologies;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import inescid.dataaggregation.dataset.profile.ClassUsageStats;
import inescid.dataaggregation.dataset.profile.UsageProfiler;
import inescid.util.datastruct.MapOfSets;

public class AllOntologiesAnalyser {
	public UsageProfiler allOntologiesProfiler=new UsageProfiler();
	public UsageProfiler allOntologiesProfilerDataElements=new UsageProfiler();
	public HashMap<String, OntologyAnalyzer> ontologies=new HashMap<String, OntologyAnalyzer>();
	
	MapOfSets<String, String> classesUsage=new MapOfSets<>(); 
	MapOfSets<String, String> propertiesUsage=new MapOfSets<>(); 
	MapOfSets<String, String> dataElementClassUsage=new MapOfSets<>(); 
	MapOfSets<String, String> dataElementPropertyUsage=new MapOfSets<>(); 
	float namespaceResolvable=0;
	float ontologyExists=0;
	float rdfResourceForNamespaceExists=0;
	float withDataElements=0;
	float dataElementsCount=0;
	
	public void runAnalysis() {
		for(OntologyAnalyzer oAn : ontologies.values()) {
			namespaceResolvable+=oAn.report.namespaceResolvable ? 1 : 0;
			ontologyExists+=oAn.report.ontologyExists ? 1 : 0;
			rdfResourceForNamespaceExists+=oAn.report.rdfResourceForNamespaceExists ? 1 : 0;
			withDataElements+=oAn.report.dataElementResources>0 ? 1 : 0;
			dataElementsCount+=oAn.report.dataElementResources;
			if(oAn.report.ontologyExists) {
				ClassUsageStats oStats = oAn.report.profileOfOntology.getClassesStats().values().iterator().next();
				for(String prop: oStats.getPropertiesStats().keySet()){
					propertiesUsage.put(prop, oAn.namespace);
				}
				classesUsage.put(oAn.report.profileOfOntology.getClassesStats().keySet().iterator().next(), 
						oAn.namespace);
				for(String cls: oAn.report.profileOfDataElements.getClassesStats().keySet()){
					System.out.println("----------");
					System.out.println(oAn.toString());
					System.out.println(cls);
					System.out.println(oAn.namespace);
					dataElementClassUsage.put(cls, oAn.namespace);
					ClassUsageStats classUsageStats = oAn.report.profileOfDataElements.getClassesStats().get(cls);
					for(String prop: classUsageStats.getPropertiesStats().keySet()){
						dataElementPropertyUsage.put(prop, oAn.namespace);
					}
				}
			}
		}
	}
	
	public void addOntology(OntologyAnalyzer o) {
		ontologies.put(o.namespace, o);
	}
	
	public String toCsv(String[][] ontologiesUris) throws IOException {
		StringBuilder sb=new StringBuilder();
		CSVPrinter prt = new CSVPrinter(sb, CSVFormat.DEFAULT);
		prt.printRecord("Ontologies namespaces");
		prt.printRecord("Namespace(s)", "Location of definition(s)");
		
		for(int i=0; i<ontologiesUris.length; i++) {
			String[] ontUriGroup=ontologiesUris[i];
			String namespaces="";
			String defs="";
			for(int ig=1; ig<ontUriGroup.length; ig++) {
				String ontUri=ontUriGroup[ig];
				ig++;
				String ontDef=ontUriGroup[ig];
				if(namespaces.isEmpty())
					namespaces=ontUri;
				else
					namespaces+="\n"+ontUri;
				if(ontDef!=null) {
					if(defs.isEmpty())
						defs=ontDef;
					else
						defs+="\n"+ontDef;
				}
			}
			prt.printRecord(namespaces, defs);
		}
		
		prt.printRecord("Ontologies", ontologies.size());
		prt.printRecord("namespaceResolvable", namespaceResolvable, String.format("%.2f", namespaceResolvable / ontologies.size()));
		prt.printRecord("ontologyExists", ontologyExists, String.format("%.2f", ontologyExists / ontologies.size()));
		prt.printRecord("rdfResourceForNamespaceExists", rdfResourceForNamespaceExists, String.format("%.2f", rdfResourceForNamespaceExists / ontologies.size()));
		prt.printRecord("withDataElements", withDataElements, "dataElementsCount", dataElementsCount, String.format("%.2f", withDataElements / ontologies.size()));
		prt.printRecord("#Ontologies' classes");
		prt.printRecord("URI", "usage count");
		ArrayList<Entry<String, HashSet<String>>> sorted=new ArrayList(classesUsage.entrySet());
		Comparator<Entry<String, HashSet<String>>> comparator = new Comparator<Entry<String, HashSet<String>>>() {
			@Override
			public int compare(Entry<String, HashSet<String>> o1, Entry<String, HashSet<String>> o2) {
				return o2.getValue().size() - o1.getValue().size();
			}
		};
		Collections.sort(sorted, comparator);
		for(Entry<String, HashSet<String>> uri: sorted) 
			prt.printRecord(uri.getKey(), uri.getValue().size() , String.format("%.2f", (float)uri.getValue().size() / ontologies.size()), "("+uri.getValue().size()+") "+String.format("%.0f%%", (float)uri.getValue().size() / ontologies.size()*100));
		prt.printRecord("# Ontologies' properties");
		prt.printRecord("URI", "usage count");
		sorted=new ArrayList(propertiesUsage.entrySet());
		Collections.sort(sorted, comparator);
		for(Entry<String, HashSet<String>> uri: sorted) 
			prt.printRecord(uri.getKey(), uri.getValue().size() , String.format("%.2f", (float)uri.getValue().size() / ontologies.size()), "("+uri.getValue().size()+") "+String.format("%.0f%%", (float)uri.getValue().size() / ontologies.size()*100));
		prt.printRecord("#Data elements' classes");
		prt.printRecord("URI", "usage count");
		sorted=new ArrayList(dataElementClassUsage.entrySet());
		Collections.sort(sorted, comparator);
		for(Entry<String, HashSet<String>> uri: sorted) 
			prt.printRecord(uri.getKey(), uri.getValue().size() , String.format("%.2f", (float)uri.getValue().size() / ontologies.size()), "("+uri.getValue().size()+") "+String.format("%.0f%%", (float)uri.getValue().size() / ontologies.size()*100));
		prt.printRecord("#Data elements' properties");
		prt.printRecord("URI", "usage count");
		sorted=new ArrayList(dataElementPropertyUsage.entrySet());
		Collections.sort(sorted, comparator);
		for(Entry<String, HashSet<String>> uri: sorted) 
			prt.printRecord(uri.getKey(), uri.getValue().size() , String.format("%.2f", (float)uri.getValue().size() / ontologies.size()), "("+uri.getValue().size()+") "+String.format("%.0f%%", (float)uri.getValue().size() / ontologies.size()*100));
		prt.close();
		return sb.toString();
	}
}
