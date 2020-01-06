package inescid.dataaggregation.dataset.profile.multilinguality;

import org.apache.jena.graph.Node;
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.sparql.path.Path;

public class ShapeInstance {
	Shape shape;
	Node focusNode; 
	Path path; 
	Node valueNode;
	Constraint constraint;
	
	public ShapeInstance(Shape shape, Node focusNode, Path path, Node valueNode, Constraint constraint) {
		super();
		this.shape = shape;
		this.focusNode = focusNode;
		this.path = path;
		this.valueNode = valueNode;
		this.constraint = constraint;
//		System.out.println(this);
	}

	public Shape getShape() {
		return shape;
	}

	public Node getFocusNode() {
		return focusNode;
	}

	public Path getPath() {
		return path;
	}

	public Node getValueNode() {
		return valueNode;
	}

	public Constraint getConstraint() {
		return constraint;
	}

	@Override
	public String toString() {
		return "ShapeInstance [shape=" + shape.getShapeNode().toString() + ", focusNode=" + focusNode.toString() + ", path=" + path.toString() + ", valueNode="
				+ valueNode.toString() + ", constraint=" + constraint.toString() + "]";
	}
	
	

}
