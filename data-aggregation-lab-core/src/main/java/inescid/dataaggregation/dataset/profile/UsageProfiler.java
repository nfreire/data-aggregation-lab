package inescid.dataaggregation.dataset.profile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Rdfs;

public class UsageProfiler implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	int maxWarns=5;
	
//	boolean optionProfileObjectsOfTriples=true;
	boolean optionCountRdfType=true;
	
	UsageStats usageStats=new UsageStats();
	int collectedCount=0;
	
	public void collect(Model model, String fromUri) {
		collect(model.getResource(fromUri));
	}
	public void collect(Resource r) {
		if(r==null) return;
		ArrayList<ClassUsageStats> classesOfSubject=new ArrayList<>(3);
		ArrayList<String> classesOfSubjectUris=new ArrayList<>(3);
		for(Statement st : r.listProperties(Rdf.type).toList()) {
//			System.out.println(st);
			String clsUri = st.getObject().asNode().getURI();
			ClassUsageStats classStats = usageStats.getClassStats(clsUri);
			classStats.eventInstanceStart(r);
			classesOfSubject.add(classStats);
		
			classesOfSubjectUris.add(clsUri);
		}
		
		if(classesOfSubject.isEmpty()) {
			classesOfSubject.add(usageStats.getClassStats(Rdfs.Resource.getURI()));
			ClassUsageStats classStats = usageStats.getClassStats(Rdfs.Resource.getURI());
			classStats.eventInstanceStart(r);
		} else if(classesOfSubjectUris.size()>1 && maxWarns>0) {
//			System.out.print("WARN: resource has multiple types: "+ classesOfSubjectUris);
//			maxWarns--;
//			if(maxWarns==0)
//				System.out.print("(last warning)");
//			System.out.println();
		}
//		r.listProperties(RdfRegRdf.type);
		StmtIterator properties = r.listProperties();
		for(Statement st : properties.toList()) {
			if(!optionCountRdfType && st.getPredicate().equals(Rdf.type)) continue;
			for(ClassUsageStats stat : classesOfSubject)
				stat.eventProperty(st);
		}
		for(ClassUsageStats stat : classesOfSubject)
			stat.eventInstanceEnd(r);
		
		StmtIterator propertiesRangeOf = r.getModel().listStatements(null, null, r);
		for(Statement st : propertiesRangeOf.toList()) {
			if(st.getPredicate().equals(Rdf.type)) continue;
			for(ClassUsageStats stat : classesOfSubject)
				stat.getPropertiesObjectStats().incrementTo(st.getPredicate().getURI());
		}
		classesOfSubject.clear();
		classesOfSubjectUris.clear();
		collectedCount++;
	}
	
	public void collect(Model model) {
		ResIterator subjs = model.listSubjects();
		
		for(Resource r: subjs.toList()) {
//			System.out.println("URI-"+RdfUtil.getUriOrId(r));
			collect(r);
		}
	}


	public UsageStats getUsageStats() {
		return usageStats;
	}


	public void finish() {
		usageStats.finish();
	}
	
	@Override
	public String toString() {
		return getUsageStats().toString();
	}
	
	public String printSummary() {
		StringBuilder sb=new StringBuilder();
		//harvest all wd entities and properties used, and profile them
		sb.append("["+collectedCount+" resources - Usage profile: "+ usageStats.getClassesStats().size()).append(" classes, ");
		int propCnt=0;
		HashSet<String> uniqueProps=new HashSet<String>();
		for (Entry<String, ClassUsageStats> entry : getUsageStats().getClassesStats().entrySet() ) {
			propCnt+=entry.getValue().getPropertiesStats().size();
			uniqueProps.addAll(entry.getValue().getPropertiesStats().keySet());
		}
		sb.append(propCnt).append(" properties. ").append(uniqueProps.size()).append(" unique properties]");
		return sb.toString();
	}
	public String printShort() {
		StringBuilder sb=new StringBuilder();
		//harvest all wd entities and properties used, and profile them
		sb.append("["+collectedCount+" resources - Usage profile: "+usageStats.getClassesStats().size()).append(" classes\n");
		for (Entry<String, ClassUsageStats> entry : getUsageStats().getClassesStats().entrySet() ) {
			sb.append(entry.getKey()).append(" ").append( entry.getValue().getPropertiesStats().size()).append(" props.\n");
		}		
		sb.append("]");
		return sb.toString();
	}
//	public void setOptionProfileObjectsOfTriples(boolean b) {
//		optionProfileObjectsOfTriples=b;
//	}
	public void persist(File csvFile ) throws IOException {
		FileUtils.write(csvFile , getUsageStats().toCsv(),
				"UTF-8");
	}
}
