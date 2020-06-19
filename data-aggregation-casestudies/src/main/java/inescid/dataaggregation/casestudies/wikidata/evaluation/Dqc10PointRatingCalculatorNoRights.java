package inescid.dataaggregation.casestudies.wikidata.evaluation;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;

import com.mchange.v1.util.SimpleMapEntry;

import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.dataset.profile.completeness.CompletenessSpecOfProperties;

public class Dqc10PointRatingCalculatorNoRights {
	
	public static double calculate(Model rdfModelOfRec) {
		Set<Resource> rootTypes=Dqc10PointRatingSpecNoRights.spec.resources.keySet();

		Set<Entry<Resource, Property>> propertiesFound=new HashSet<>();

		for(Resource resType: rootTypes) {
			CompletenessSpecOfProperties resRatingSpec = Dqc10PointRatingSpecNoRights.spec.resources.get(resType);
			ResIterator roots = rdfModelOfRec.listSubjectsWithProperty(Rdf.type, resType);
			while(roots.hasNext()) {
				Resource srcRoot = roots.next();
				//get all props of srcRoot
				//foreach prop check if it is part of the spec, if it is add to properties found
				StmtIterator listProperties = rdfModelOfRec.listStatements(srcRoot, null, (RDFNode)null);
				while(listProperties.hasNext()) {
					Property predicate = listProperties.next().getPredicate();
					if(resRatingSpec.usesProperty(predicate))
						propertiesFound.add(new SimpleMapEntry(resType, predicate));
				}
			}
		}
		
		int found=0;
		int missing=0;
		for(Resource resType: rootTypes) {
			CompletenessSpecOfProperties resRatingSpec = Dqc10PointRatingSpecNoRights.spec.resources.get(resType);
			for(Property p: resRatingSpec.properties) {
				if(propertiesFound.contains(new SimpleMapEntry(resType, p)))
					found++;
				else
					missing++;
			}
			GROUP: for(Set<Property> group: resRatingSpec.groupsOfProperties()) {
				for(Property p: group) {
					if(propertiesFound.contains(new SimpleMapEntry(resType, p))) {
						found++;
						continue GROUP;
					}
				}
				missing++;
			}
		}
		return (double)found/((double)Dqc10PointRatingSpecNoRights.TOTAL_CRITERIA+1);
	}
}
