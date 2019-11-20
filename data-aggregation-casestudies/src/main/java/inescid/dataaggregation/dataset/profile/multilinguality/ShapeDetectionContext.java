package inescid.dataaggregation.dataset.profile.multilinguality;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.engine.exec.TripleValidator;
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.shacl.validation.ReportItem;
import org.apache.jena.sparql.path.Path;

public class ShapeDetectionContext extends ValidationContext {
	ShapesDetectionReport report=new ShapesDetectionReport();
	
	public ShapeDetectionContext(Shapes shapes, Graph data) {
		super(shapes, data);
	}

	public ShapeDetectionContext(ValidationContext vCxt) {
		super(vCxt);
	}

    public void reportEntry(String message, Shape shape, Node focusNode, Path path, Node valueNode, Constraint constraint) {
        report.add(new ShapeInstance(shape, focusNode, path, valueNode, constraint));
    }

    public void reportEntry(ReportItem item, TripleValidator validator, Triple triple) {
    	report.add(new ShapeInstance(validator.getShape(), validator.getFocusNode(triple), validator.getPath(), item.getValue(), validator.getConstraint()));
    }

    public ValidationReport generateReport() {
        throw new IllegalArgumentException("Method is not applicable to this class");
    }

	public ShapesDetectionReport getReport() {
		return report;
	}
	
	
}
