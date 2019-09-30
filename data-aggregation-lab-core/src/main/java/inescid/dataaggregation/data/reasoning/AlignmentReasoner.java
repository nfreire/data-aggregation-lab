package inescid.dataaggregation.data.reasoning;

import java.util.HashSet;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;

import eu.europeana.commonculture.lod.crawler.rdf.RdfUtil.Jena;
import inescid.dataaggregation.data.RegRdf;
import inescid.dataaggregation.data.RegRdfs;
import inescid.util.RdfUtil;

public class AlignmentReasoner {
	final Model schema;
	final Reasoner reasoner;
	
	public AlignmentReasoner(Model schema, Reasoner reasoner) {
		this.schema = schema;
		this.reasoner = reasoner;
	}
	
	public InfModel infer(Resource resource) {
		Model subSchema=Jena.createModel();
		HashSet<Resource> propsAndUris=new HashSet<Resource>();
		for(Statement st: IteratorUtils.asIterable(resource.listProperties())) {
			propsAndUris.add(st.getPredicate());
			if(st.getObject().isURIResource())
				propsAndUris.add((Resource)st.getObject());
		}
		System.out.println(propsAndUris);
		for(Resource r: propsAndUris) {
			collect(r, subSchema);
		}
		System.out.println(RdfUtil.statementsToString(subSchema));
		return ReasonerUtil.infer(reasoner, subSchema, resource.getModel());
	}

	private void collect(Resource r, Model subSchema) {
		if(RdfUtil.contains(r.getURI(), subSchema))
			return;
		if(! RdfUtil.contains(r.getURI(), schema))
			return;
		StmtIterator allProps = schema.createResource(r.getURI()).listProperties();
			subSchema.add(allProps);
			for(Statement st: IteratorUtils.asIterable(r.listProperties(RegRdfs.subClassOf))) {
				collect((Resource)st.getObject(), subSchema);
			}
			for(Statement st: IteratorUtils.asIterable(r.listProperties(RegRdfs.subPropertyOf))) {
				collect((Resource)st.getObject(), subSchema);
			}
			for(Statement st: IteratorUtils.asIterable(r.listProperties(RegRdf.type))) {
				collect((Resource)st.getObject(), subSchema);
			}
	}
}
