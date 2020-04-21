package inescid.dataaggregation.casestudies.ontologies;

import inescid.dataaggregation.dataset.profile.UsageStats;

public class ReportOfOntology {
	public boolean namespaceResolvable;
	public boolean ontologyExists;
	public boolean rdfResourceForNamespaceExists;
	public UsageStats profileOfOntology;
	public UsageStats profileOfDataElements;
	public int dataElementResources;
	@Override
	public String toString() {
		return "ReportOfOntology [namespaceResolvable=" + namespaceResolvable + ", ontologyExists=" + ontologyExists
				+ ", rdfResourceForNamespaceExists=" + rdfResourceForNamespaceExists
				+ ", dataElementResources=" + dataElementResources + ", profile=" +  (profileOfOntology==null ? "no data" : "\n"+profileOfOntology.toString())  + "]";
	}
	
}
