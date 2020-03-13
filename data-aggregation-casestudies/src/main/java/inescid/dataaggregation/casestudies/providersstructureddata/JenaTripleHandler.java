package inescid.dataaggregation.casestudies.providersstructureddata;

import org.apache.any23.extractor.ExtractionContext;
import org.apache.any23.writer.TripleHandler;
import org.apache.any23.writer.TripleHandlerException;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.eclipse.rdf4j.model.Literal;

public class JenaTripleHandler implements TripleHandler {
	/**
	 * Acts as an Any23 TripleHandler and writes/converts triples to a Jena Model or Graph.
	 */

	private Graph graph;

	public JenaTripleHandler(Model m) {
		this(m.getGraph());
	}

	public JenaTripleHandler(Graph g) {
		graph = g;
	}
	
	@Override
	public void receiveTriple(Resource subject, IRI predicate, Value object, IRI graphIri, ExtractionContext context)
			throws TripleHandlerException {
		try {
			graph.add(new Triple(
				valueToNode(subject),
				valueToNode(predicate),
				valueToNode(object)
			));
		} catch(Exception e) {
			throw new TripleHandlerException("Error while converting triple", e);
		}
	}

	// Stubs

	@Override
	public void startDocument(IRI documentIRI) throws TripleHandlerException {}

	@Override
	public void openContext(ExtractionContext context) throws TripleHandlerException {}

	@Override
	public void receiveNamespace(String prefix, String uri, ExtractionContext context) throws TripleHandlerException {}

	@Override
	public void closeContext(ExtractionContext context) throws TripleHandlerException {}

	@Override
	public void endDocument(IRI documentIRI) throws TripleHandlerException {}

	@Override
	public void setContentLength(long contentLength) {}

	@Override
	public void close() throws TripleHandlerException {}
	
	
	public static Node valueToNode(Value val) {
		/**
		 * Convert a RDF4J Value to a Jena Node.
		 */
		if (val instanceof IRI) {
			return NodeFactory.createURI(val.toString());
		} else if (val instanceof Literal) {
			TypeMapper tm = TypeMapper.getInstance();
			Literal litval = (Literal)val;
			return NodeFactory.createLiteral(
					litval.getLabel(),
					litval.getLanguage().orElse(null),
					tm.getSafeTypeByName(litval.getDatatype().toString()));
		} else {
			throw new RuntimeException("UnknownValueTypeException"+val.getClass());
		}
	}
}
