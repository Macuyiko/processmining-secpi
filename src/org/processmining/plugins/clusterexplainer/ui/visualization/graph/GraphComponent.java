package org.processmining.plugins.clusterexplainer.ui.visualization.graph;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;

public class GraphComponent extends JComponent {
	private static final long serialVersionUID = -6769670684721859253L;
	private Graph<GraphVertex, GraphEdge> graph;
	private VisualizationViewer<GraphVertex, GraphEdge> vv;
	private Layout<GraphVertex, GraphEdge> layout;
	private DefaultModalGraphMouse<GraphVertex, GraphEdge> graphMouse;

	public GraphComponent(Graph<GraphVertex, GraphEdge> g) {
		this.setLayout(new BorderLayout());
		Dimension preferredSize = new Dimension(400, 400);
		
		graph = g;
		layout = new FRLayout2<GraphVertex, GraphEdge>(graph);
		
		VisualizationModel<GraphVertex, GraphEdge> visualizationModel = new DefaultVisualizationModel<GraphVertex, GraphEdge>(layout, preferredSize);
		vv = new VisualizationViewer<GraphVertex, GraphEdge>(visualizationModel, preferredSize);
		vv.setBackground(Color.black);
		vv.setForeground(Color.white);
		
		graphMouse = new DefaultModalGraphMouse<GraphVertex, GraphEdge>();

		vv.setGraphMouse(graphMouse);

        Transformer<GraphVertex, Paint> vertexColor = new Transformer<GraphVertex, Paint>() {
            public Paint transform(GraphVertex v) {
                return (Color) v.getProperty("fillcolor", Color.white);
            }
        };
        Transformer<GraphVertex, Shape> vertexShape = new Transformer<GraphVertex, Shape>() {
            public Shape transform(GraphVertex v) {
            	Shape shape = (Shape) v.getProperty("shape", null);
                Integer size = (Integer) v.getProperty("size", 20);
                Ellipse2D circle = new Ellipse2D.Double(-size/2d, -size/2d, size, size);
                if (shape == null)
                	return circle;
                return shape;
            }
        };
        Transformer<GraphVertex, String> vertexLabel = new Transformer<GraphVertex, String>() {
            public String transform(GraphVertex v) {
                return (String) v.getProperty("label", "");
            }
        };
        
        Transformer<GraphEdge, Paint> edgeFillColor = new Transformer<GraphEdge, Paint>() {
            public Paint transform(GraphEdge e) {
                return (Color) e.getProperty("fillcolor", new Color(1f, 1f, 1f, 0f));
            }
        };
        Transformer<GraphEdge, Paint> edgeColor = new Transformer<GraphEdge, Paint>() {
            public Paint transform(GraphEdge e) {
                return (Color) e.getProperty("color", new Color(1f, 1f, 1f, .5f));
            }
        };
        Transformer<GraphEdge, String> edgeLabel = new Transformer<GraphEdge, String>() {
            public String transform(GraphEdge v) {
                return (String) v.getProperty("label", "");
            }
        };
        Transformer<GraphEdge, Stroke> edgeStroke = new Transformer<GraphEdge, Stroke>() {
            public Stroke transform(GraphEdge v) {
                return (Stroke) v.getProperty("stroke", new BasicStroke(1f));
            }
        };

        vv.getRenderContext().setVertexFillPaintTransformer(vertexColor);
        vv.getRenderContext().setVertexShapeTransformer(vertexShape);
        vv.getRenderContext().setVertexLabelTransformer(vertexLabel);
		
        vv.getRenderContext().setEdgeDrawPaintTransformer(edgeColor);
        vv.getRenderContext().setEdgeFillPaintTransformer(edgeFillColor);
        vv.getRenderContext().setEdgeLabelTransformer(edgeLabel);
        vv.getRenderContext().setEdgeStrokeTransformer(edgeStroke);
        
		GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
		this.add(gzsp, BorderLayout.CENTER);

		graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		
		final JButton switchModeButton = new JButton("TRANSFORMING");
		this.add(switchModeButton, BorderLayout.SOUTH);
		switchModeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if ("TRANSFORMING".equals(switchModeButton.getText())) {
					graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
					switchModeButton.setText("PICKING");
				} else if ("PICKING".equals(switchModeButton.getText())) {
					graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
					switchModeButton.setText("TRANSFORMING");
				}
			}
		});
	}

	public Set<GraphVertex> getSelectedVertices() {
		Set<GraphVertex> picked = new HashSet<GraphVertex>(vv.getPickedVertexState().getPicked());
        return picked;
	}
}
