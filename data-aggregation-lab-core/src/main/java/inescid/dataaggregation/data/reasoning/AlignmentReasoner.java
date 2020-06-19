package inescid.dataaggregation.data.reasoning;

import java.util.HashSet;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;

import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Rdfs;
import inescid.util.RdfUtil;
import inescid.util.RdfUtil.Jena;

public class AlignmentReasoner {
	final Model schema;
	final Reasoner reasoner;
	
	public AlignmentReasoner(Model schema, Reasoner reasoner) {
		this.schema = schema;
		this.reasoner = reasoner;
	}
	
	Model subSchema;
	public InfModel infer(Resource resource) {
		subSchema=Jena.createModel();
		HashSet<Resource> propsAndUris=new HashSet<Resource>();
		for(Statement st: IteratorUtils.asIterable(resource.listProperties())) {
			propsAndUris.add(st.getPredicate());
			if(st.getObject().isURIResource())
				propsAndUris.add((Resource)st.getObject());
		}
//		System.out.println(propsAndUris);
		for(Resource r: propsAndUris) {
			collect(r, subSchema);
//			System.out.println(r.getURI());
//			RdfUtil.printOutRdf(subSchema);
		}
//		System.out.println(RdfUtil.statementsToString(subSchema));
		return ReasonerUtil.infer(reasoner, subSchema, resource.getModel());
	}
	public Model getSubSchemaOfLastInference() {
		return subSchema;
	}

	private void collect(Resource r, Model subSchema) {
		if(RdfUtil.contains(r.getURI(), subSchema))
			return;
		if(! RdfUtil.contains(r.getURI(), schema))
			return;
		StmtIterator allProps = schema.createResource(r.getURI()).listProperties();
			subSchema.add(allProps);
			for(Statement st: IteratorUtils.asIterable(schema.createResource(r.getURI()).listProperties())) {
				if(st.getObject().isURIResource())
					collect((Resource)st.getObject(), subSchema);
				collect(st.getPredicate(), subSchema);
//				System.out.println(st);
			}
//			for(Statement st: IteratorUtils.asIterable(r.listProperties(RegRdfs.subClassOf))) {
//				collect((Resource)st.getObject(), subSchema);
//			}
//			for(Statement st: IteratorUtils.asIterable(r.listProperties(RegRdfs.subPropertyOf))) {
//				collect((Resource)st.getObject(), subSchema);
//			}
//			for(Statement st: IteratorUtils.asIterable(r.listProperties(RegRdf.type))) {
//				collect((Resource)st.getObject(), subSchema);
//			}
	}
}
