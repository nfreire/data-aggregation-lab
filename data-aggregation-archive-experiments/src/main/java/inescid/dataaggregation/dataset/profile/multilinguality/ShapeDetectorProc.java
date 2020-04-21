package inescid.dataaggregation.dataset.profile.multilinguality;

import java.util.*;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.engine.ShaclPaths;
import org.apache.jena.shacl.engine.Target;
import org.apache.jena.shacl.lib.G;
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.shacl.parser.NodeShape;
import org.apache.jena.shacl.parser.PropertyShape;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.vocabulary.RDF;

public class ShapeDetectorProc {
	    private static IndentedWriter out  = IndentedWriter.stdout;

	    public static ShapesDetectionReport simpleValidatation(Graph shapesGraph, Graph data) {
	        return simpleValidatation(shapesGraph, data, false);
	    }

	    public static ShapesDetectionReport simpleValidatation(Graph shapesGraph, Graph data, boolean verbose) {
	        Shapes shapes = Shapes.parse(shapesGraph);
	        return simpleValidatation(shapes, data, verbose);
	    }

	    public static ShapesDetectionReport simpleValidatation(Shapes shapes, Graph data, boolean verbose) {
	        int x = out.getAbsoluteIndent();
	        try {
	            ShapeDetectionContext vCxt = new ShapeDetectionContext(shapes, data);
	            vCxt.setVerbose(verbose);
	            return simpleValidatation(vCxt, shapes, data);
	        //} catch (ShaclParseException ex) {
	        } finally { out.setAbsoluteIndent(x); }
	    }

	    public static ShapesDetectionReport simpleValidatation(ShapeDetectionContext vCxt, Iterable<Shape> shapes, Graph data) {
	        //vCxt.setVerbose(true);
	        for ( Shape shape : shapes ) {
	            simpleValidatation(vCxt, data, shape);
	        }
	        if ( vCxt.isVerbose() )
	            out.ensureStartOfLine();
	        return vCxt.getReport();
	    }

	    public static void simpleValidatation(ShapeDetectionContext vCxt, Graph data, Shape shape) {
	        simpleValidatationInternal(vCxt, data, null, shape);
	    }
	    
	    // ---- Single node.
	    
	    public static ShapesDetectionReport simpleValidatationNode(Shapes shapes, Graph data, Node node, boolean verbose) {
	        int x = out.getAbsoluteIndent();
	        try {
	            ShapeDetectionContext vCxt = new ShapeDetectionContext(shapes, data);
	            vCxt.setVerbose(verbose);
	            return simpleValidatationNode(vCxt, shapes, node, data);
	        //} catch (ShaclParseException ex) {
	        } finally { out.setAbsoluteIndent(x); }
	    }

	    
	    private static ShapesDetectionReport simpleValidatationNode(ShapeDetectionContext vCxt, Shapes shapes, Node node, Graph data) {
	        //vCxt.setVerbose(true);
	        for ( Shape shape : shapes ) {
	            simpleValidatationNode(vCxt, data, node, shape);
	        }
	        if ( vCxt.isVerbose() )
	            out.ensureStartOfLine();
	        return vCxt.getReport();

	    }

	    private static void simpleValidatationNode(ShapeDetectionContext vCxt, Graph data, Node node, Shape shape) {
	        simpleValidatationInternal(vCxt, data, node, shape);
	    }

	    // --- Top of process
	    private static void simpleValidatationInternal(ShapeDetectionContext vCxt, Graph data, Node node, Shape shape) {
	        Collection<Node> focusNodes = getFocusNodes(data, shape);
	        if ( node != null ) { 
	            if ( ! focusNodes.contains(node) )
	                return ;
	            focusNodes = Collections.singleton(node);
	        }
	        
	        if ( vCxt.isVerbose() ) {
	            out.println(shape.toString());
	            out.printf("N: FocusNodes(%d): %s\n", focusNodes.size(), focusNodes);
	            out.incIndent();
	        }

	        for ( Node focusNode : focusNodes ) {
	            if ( vCxt.isVerbose() )
	                out.println("F: "+focusNode);
	            validateShape(vCxt, data, shape, focusNode);
	        }
	        if ( vCxt.isVerbose() ) {
	            out.decIndent();
	        }
	    }

	    // Recursion for shapes of shapes. "shape-expecting constraint parameters"
	    public static void execValidateShape(ShapeDetectionContext vCxt, Graph data, Shape shape, Node focusNode) {
	        validateShape(vCxt, data, shape, focusNode);
	    }

	    private static void validateShape(ShapeDetectionContext vCxt, Graph data, Shape shape, Node focusNode) {
	        if ( shape.deactivated() )
	            return;
	        if ( vCxt.isVerbose() )
	            out.println("S: "+shape);

	        Path path;
	        Set<Node> vNodes;
	        if ( shape instanceof NodeShape ) {
	            path = null;
	            vNodes = null;
	        } else if ( shape instanceof PropertyShape ) {
	            PropertyShape propertyShape = (PropertyShape)shape;
	            path = propertyShape.getPath();
	            vNodes = ShaclPaths.valueNodes(data, focusNode, propertyShape.getPath());
	        } else {
	            if ( vCxt.isVerbose() )
	                out.println("Z: "+shape);
	            return;
	        }

	        // Constraints of this shape.
	        for ( Constraint c : shape.getConstraints() ) {
	            if ( vCxt.isVerbose() )
	                out.println("C: "+c);
	            evalConstraint(vCxt, data, shape, focusNode, path, vNodes, c);
	        }

	        // Follow sh:property (sh:node behaves as a constraint).
	        validationPropertyShapes(vCxt, data, shape.getPropertyShapes(), focusNode);
	        if ( vCxt.isVerbose() )
	            out.println();
	    }

	    private static void validationPropertyShapes(ShapeDetectionContext vCxt, Graph data, List<PropertyShape> propertyShapes, Node focusNode) {
	        if ( propertyShapes == null )
	            return;
	        for ( PropertyShape propertyShape : propertyShapes ) {
	            validationPropertyShape(vCxt, data, propertyShape, focusNode);
	        }
	    }

	    // XXX This is *nearly* validationShape.
	    private static void validationPropertyShape(ShapeDetectionContext vCxt, Graph data, PropertyShape propertyShape, Node focusNode) {
	        //validateShape(vCxt, data, propertyShape, focusNode);
	        // sh:property got us here.
	        if ( propertyShape.deactivated() )
	            return;
	        if ( vCxt.isVerbose() )
	            out.println("P: "+propertyShape);

	        Set<Node> vNodes = ShaclPaths.valueNodes(data, focusNode, propertyShape.getPath());

	        // DRY with validateShape.
	        for ( Constraint c : propertyShape.getConstraints() ) {
	            if ( vCxt.isVerbose() )
	                out.println("C: "+focusNode+" :: "+c);
	            // Pass vNodes here.
	            evalConstraint(vCxt, data, propertyShape, focusNode, propertyShape.getPath(), vNodes, c);
	        }
	        vNodes.forEach(vNode->{
	            validationPropertyShapes(vCxt, data, propertyShape.getPropertyShapes(), vNode);
	        });
	    }

	    private static void evalConstraint(ShapeDetectionContext vCxt, Graph data, Shape shape, Node focusNode, Path path, Set<Node> pathNodes, Constraint c) {
	        if ( path == null ) {
	            if ( pathNodes != null )
	                throw new InternalErrorException("Path is null but pathNodes is not null");
	            c.validateNodeShape(vCxt, data, shape, focusNode);
	            return;
	        }
	        if ( pathNodes == null )
	            throw new InternalErrorException("Path is not null but pathNodes is null");
	        c.validatePropertyShape(vCxt, data, shape, focusNode, path, pathNodes);
	    }

	    private static Collection<Node> getFocusNodes(Graph data, Shape shape) {
	        Collection<Node> acc = new HashSet<>();
	        shape.getTargets().forEach(target->
	            acc.addAll(getFocusNodes(data, shape, target)));
	        return acc;
	    }

	    private static Collection<Node> getFocusNodes(Graph data, Shape shape, Target target) {
	        Node targetObj = target.getObject();
	        switch(target.getTargetType()) {
	            case targetClass:
	                return G.listPO(data, RDF.Nodes.type, targetObj);
	            case targetNode:
	                return Collections.singletonList(targetObj);
	            case targetObjectsOf:
	                return G.setSP(data, null, targetObj);
	            case targetSubjectsOf:
	                return G.setPO(data, targetObj, null);
	            case implicitClass:
	                // Instances of the class and its subtypes.
	                return G.listAllNodesOfType(data, targetObj);
	            default:
	                return Collections.emptyList();
	        }
	    }

}
