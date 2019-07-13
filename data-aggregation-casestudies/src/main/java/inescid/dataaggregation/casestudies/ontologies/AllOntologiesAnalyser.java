package inescid.dataaggregation.casestudies.ontologies;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.jena.rdf.model.Property;

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
	
	public void runAnalysis() {
		for(OntologyAnalyzer oAn : ontologies.values()) {
			namespaceResolvable+=oAn.report.namespaceResolvable ? 1 : 0;
			ontologyExists+=oAn.report.ontologyExists ? 1 : 0;
			rdfResourceForNamespaceExists+=oAn.report.rdfResourceForNamespaceExists ? 1 : 0;
			withDataElements+=oAn.report.dataElementResources>0 ? 1 : 0;
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
	
	public String toCsv() throws IOException {
		StringBuilder sb=new StringBuilder();
		CSVPrinter prt = new CSVPrinter(sb, CSVFormat.DEFAULT);
		prt.printRecord("Ontologies", ontologies.size());
		prt.printRecord("namespaceResolvable", namespaceResolvable, String.format("%.2f", namespaceResolvable / ontologies.size()));
		prt.printRecord("ontologyExists", ontologyExists, String.format("%.2f", ontologyExists / ontologies.size()));
		prt.printRecord("rdfResourceForNamespaceExists", rdfResourceForNamespaceExists, String.format("%.2f", rdfResourceForNamespaceExists / ontologies.size()));
		prt.printRecord("withDataElements", withDataElements, String.format("%.2f", withDataElements / ontologies.size()));
		prt.printRecord("#Ontologies' classes");
		prt.printRecord("URI", "usage count");
		for(String uri: classesUsage.keySet()) 
			prt.printRecord(uri, classesUsage.get(uri).size() , String.format("%.2f", (float)classesUsage.get(uri).size() / ontologies.size()));
		prt.printRecord("# Ontologies' properties");
		prt.printRecord("URI", "usage count");
		for(String uri: propertiesUsage.keySet()) 
			prt.printRecord(uri, propertiesUsage.get(uri).size(), String.format("%.2f", (float)propertiesUsage.get(uri).size() / ontologies.size()));
		prt.printRecord("#Data elements' classes");
		prt.printRecord("URI", "usage count");
		for(String uri: dataElementClassUsage.keySet()) 
			prt.printRecord(uri, dataElementClassUsage.get(uri).size(), String.format("%.2f", (float)dataElementClassUsage.get(uri).size() / ontologies.size()));
		prt.printRecord("#Data elements' properties");
		prt.printRecord("URI", "usage count");
		for(String uri: dataElementPropertyUsage.keySet()) 
			prt.printRecord(uri, dataElementPropertyUsage.get(uri).size(), String.format("%.2f", (float)dataElementPropertyUsage.get(uri).size() / ontologies.size()));
		prt.close();
		return sb.toString();
	}
}
