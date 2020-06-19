package inescid.dataaggregation.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import inescid.dataaggregation.data.model.Owl;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Rdfs;
import inescid.util.datastruct.MapOfSets;

public class RdfsClassHierarchy {
//	Set<String> properties=new HashSet<String>();
//	Set<String> classes=new HashSet<String>();
	MapOfSets<String, String> superClassesOf=new MapOfSets<String, String>();
	MapOfSets<String, String> superPropertiesOf=new MapOfSets<String, String>();

	public RdfsClassHierarchy() {
	}
	
	public RdfsClassHierarchy(Model owl) {
		for(Resource r: owl.listResourcesWithProperty(Rdf.type, Owl.Class).toList()) {
			for(Statement scSt : r.listProperties(Rdfs.subClassOf).toList()) {
				setSuperClass(r.getURI(), scSt.getObject().asResource().getURI());
			}			
		}
		calculateHierarchy();
	}
	
	public void calculateHierarchy() {
		for(boolean added=true; added; ) {
			added=false;
			for(String cls:	superClassesOf.keySet()) {
				int before=superClassesOf.get(cls).size();
				
//				System.out.println(cls);
				if(cls.equals("http://www.wikidata.org/entity/Q17431399")) {
					for(String superCls : new ArrayList<String>(superClassesOf.get(cls))) {
						Set<String> superSuperClasses = superClassesOf.get(superCls);
						System.out.println(superCls+" scs: "+superCls);
						if(superSuperClasses!=null)
							superClassesOf.putAll(cls, superSuperClasses);
					}
					added=added || before!=superClassesOf.get(cls).size();
					System.out.println(added);
				}else {
					for(String superCls : new ArrayList<String>(superClassesOf.get(cls))) {
						Set<String> superSuperClasses = superClassesOf.get(superCls);
						if(superSuperClasses!=null)
							superClassesOf.putAll(cls, superSuperClasses);
					}
					added=added || before!=superClassesOf.get(cls).size();
				}
			}
		}
		for(boolean added=true; added; ) {
			added=false;
			for(String prop: superPropertiesOf.keySet()) {
				int before=superPropertiesOf.get(prop).size();
				for(String superCls : new ArrayList<String>(superPropertiesOf.get(prop))) {
					Set<String> superSuperProps = superPropertiesOf.get(superCls);
					if(superSuperProps!=null)
						superPropertiesOf.putAll(prop, superSuperProps);
				}
				added=added || before!=superPropertiesOf.get(prop).size();
			}
		}		
	}
	
	public Set<String> getSuperClassesOf(Resource cls) {
		return getSuperClassesOf(cls.getURI());
	}
	public Set<String> getSuperClassesOf(String clsUri) {
		Set<String> set = superClassesOf.get(clsUri);
		return set==null ? Collections.EMPTY_SET : set;
	}
	public Set<String> getSubClassesOf(Resource cls) {
		return getSubClassesOf(cls.getURI());
	}
	public Set<String> getSubClassesOf(String clsUri) {
		Set<String> set = new HashSet<String>();
		for(String subcls : superClassesOf.keySet()) {
			if(superClassesOf.get(subcls).contains(clsUri))
				set.add(subcls);
		}
		return set;
	}
	
	public Set<String> getSuperPropertiesOf(String uri) {
		Set<String> set = superPropertiesOf.get(uri);
		return set==null ? Collections.EMPTY_SET : set;
	}
	public Set<String> getSubPropertiesOf(String uri) {
		Set<String> set = new HashSet<String>();
		for(String subPrp : superPropertiesOf.keySet()) {
			if(superPropertiesOf.get(subPrp).contains(uri))
				set.add(subPrp);
		}
		return set;
	}
	public void setSuperClass(String uriSubClass, String uriSuperClass) {
		superClassesOf.put(uriSubClass, uriSuperClass);
	}

	public void setSuperProperty(String uriSubProperty, String uriSuperProperty) {
		superPropertiesOf.put(uriSubProperty, uriSuperProperty);
	}
	
	public String toCsv() {
		try {
			StringBuilder sb=new StringBuilder();
			sb.append("#SuperClasses\n");
			MapOfSets.writeCsv(superClassesOf, sb);
			sb.append("#SuperProperties\n");
			MapOfSets.writeCsv(superPropertiesOf, sb);
			return sb.toString();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	public static RdfsClassHierarchy fromCsv(File csvFile) throws IOException {
		RdfsClassHierarchy ret=new RdfsClassHierarchy();
		StringBuilder sb=new StringBuilder();
		BufferedReader reader = Files.newBufferedReader(csvFile.toPath(), StandardCharsets.UTF_8);
		String l=reader.readLine();
		l=reader.readLine();
		while(l != null && !l.equals("#SuperProperties")) {
			sb.append(l).append("\n");
			l=reader.readLine();
		}
		ret.superClassesOf=MapOfSets.readCsv(new StringReader(sb.toString()));
		sb.setLength(0);
		while(l != null) {
			sb.append(l).append("\n");
			l=reader.readLine();
		}
		ret.superPropertiesOf=MapOfSets.readCsv(new StringReader(sb.toString()));
		return ret;
	}
}
