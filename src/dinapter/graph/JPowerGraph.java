/*
 * This file is part of Dinapter.
 *
 *  Dinapter is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Dinapter is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  (C) Copyright 2007 José Antonio Martín Baena
 *  
 *  José Antonio Martín Baena <jose.antonio.martin.baena@gmail.com>
 *  Ernesto Pimentel Sánchez <ernesto@lcc.uma.es>
 */
/**
 * 
 */
package dinapter.graph;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.jpowergraph.DefaultEdge;
import net.sourceforge.jpowergraph.DefaultGraph;
import net.sourceforge.jpowergraph.Edge;
import net.sourceforge.jpowergraph.Node;
import net.sourceforge.jpowergraph.layout.Layouter;
import net.sourceforge.jpowergraph.layout.SpringLayoutStrategy;
import net.sourceforge.jpowergraph.lens.CursorLens;
import net.sourceforge.jpowergraph.lens.LegendLens;
import net.sourceforge.jpowergraph.lens.LensSet;
import net.sourceforge.jpowergraph.lens.NodeSizeLens;
import net.sourceforge.jpowergraph.lens.RotateLens;
import net.sourceforge.jpowergraph.lens.TooltipLens;
import net.sourceforge.jpowergraph.lens.TranslateLens;
import net.sourceforge.jpowergraph.lens.ZoomLens;
import net.sourceforge.jpowergraph.manipulator.contextandtooltip.ContextMenuAndToolTipManipulator;
import net.sourceforge.jpowergraph.manipulator.contextandtooltip.DefaultContextMenuListener;
import net.sourceforge.jpowergraph.manipulator.contextandtooltip.DefaultToolTipListener;
import net.sourceforge.jpowergraph.manipulator.dragging.DraggingManipulator;
import net.sourceforge.jpowergraph.manipulator.edgecreator.DefaultEdgeCreatorListener;
import net.sourceforge.jpowergraph.manipulator.edgecreator.EdgeCreatorManipulator;
import net.sourceforge.jpowergraph.manipulator.selection.DefaultNodeSelectionModel;
import net.sourceforge.jpowergraph.manipulator.selection.SelectionManipulator;
import net.sourceforge.jpowergraph.painters.LineEdgePainter;
import net.sourceforge.jpowergraph.pane.JGraphPane;
import net.sourceforge.jpowergraph.pane.JGraphScrollPane;
import net.sourceforge.jpowergraph.pane.JGraphViewPane;
import net.sourceforge.jpowergraph.viewcontrols.RotateControlPanel;
import net.sourceforge.jpowergraph.viewcontrols.ZoomControlPanel;

/**
 * This graph is implemented using the graph library <i>JPowerGraph</i>
 * @see <a href="http://jpowergraph.sourceforge.net">JPowerGraph web page</a>
 * @author José Antonio Martín Baena
 * @version $Revision: 451 $ $Date: 2007-02-06 10:52:47 +0100 (mar, 06 feb 2007) $
 */
public class JPowerGraph<N extends Node, E extends Edge> extends DefaultGraph implements ModifiableGraph<N> {
	
	private static final int LINE_NUMBER_WEIGHT = 1;
	private static final int LINE_LENGTH_WEIGHT = 1;
	
	private class JPowerEdge extends DefaultEdge {
		private final int length;
		
		public JPowerEdge(N from, N to) {
			super(from,to);
			String [] labels = (from.getLabel()+to.getLabel()).split("\n"); 
			int height = labels.length * LINE_NUMBER_WEIGHT;
			int width = 0;
			for (String label:labels) {
				if (label.length() > width) {
					width = label.length();
				}
			}
			width *= LINE_LENGTH_WEIGHT;
			this.length = Math.max(height, width);
		}
		
		public double getLength() {
			return length;
		}
	}
	
	private EdgeFactory<N,DefaultEdge> defaultEdgeFactory = new EdgeFactory<N,DefaultEdge>() {
		public DefaultEdge getEdge(N from, N to) {
			return new JPowerEdge(from,to);
		}
	};
	
	private N endNode = null;
	
	/**
	 * It instantiates this class.
	 */
	public JPowerGraph() {
	}

	/* (non-Javadoc)
	 * @see dinapter.graph.Graph#containsNode(java.lang.Object)
	 */
	public boolean containsNode(N node) {
		return getNodes().contains(node);
	}

	/* (non-Javadoc)
	 * @see dinapter.graph.Graph#getChildren(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public Collection<N> getChildren(N node) {
		if (!containsNode(node)) {
			String message = "This graph doesn't contain this node\n"+node+" ("+node.hashCode()+")";
			message += "\n\tnot in {";
			for (N n:getAllNodes())
				message += "\n"+n+" ("+n.hashCode()+")";
			message += "\t}";
			throw new IllegalArgumentException(message);
		}
		List<N> toReturn = new ArrayList<N>(node.getEdgesFrom().size());
		for (Edge edge:(List<Edge>)node.getEdgesFrom()) {
			toReturn.add((N)edge.getTo());
		}
		return toReturn;
	}

	/* (non-Javadoc)
	 * @see dinapter.graph.Graph#getEndNode()
	 */
	@SuppressWarnings("unchecked")
	public N getEndNode() {
		if (endNode != null) {
			return endNode;
		} else {
			return (N)getNodes().get(getNodes().size()-1);
		}
	}
        
        public void setEndNode(N node) {
            endNode = node;
        }
                

	/* (non-Javadoc)
	 * @see dinapter.graph.Graph#getParents(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public Collection<N> getParents(N node) {
		List<N> toReturn = new ArrayList<N>(node.getEdgesFrom().size());
		for (Edge edge:(List<Edge>)node.getEdgesTo()) {
			toReturn.add((N)edge.getFrom());
		}
		return toReturn;
	}

	/* (non-Javadoc)
	 * @see dinapter.graph.Graph#getStartNode()
	 */
	@SuppressWarnings("unchecked")
	public N getStartNode() {
		return (N)getNodes().get(0);
	}

	/* (non-Javadoc)
	 * @see dinapter.graph.ModifiableGraph#addEdge(java.lang.Object, java.lang.Object)
	 */
	public void addEdge(N from, N to) {
		List<N> nodes = new ArrayList<N>(2);
		nodes.add(from);
		nodes.add(to);
		List<E> edges = new ArrayList<E>(1);
		edges.add(createEdge(from, to));
		addElements(nodes, edges);
	}

	/* (non-Javadoc)
	 * @see dinapter.graph.ModifiableGraph#removeEdge(java.lang.Object, java.lang.Object)
	 */
	public void removeEdge(N from, N to) {
		E edge = getEdge(from, to);
		if (edge != null) {
			List<N> nodes = new ArrayList<N>(0);
			List<E> edges = new ArrayList<E>(1);
			edges.add(edge);
			deleteElements(nodes, edges);
		}
	}

	/* (non-Javadoc)
	 * @see dinapter.graph.ModifiableGraph#removeNode(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public void removeNode(N node) {
		List<E> edges = new LinkedList<E>();
		for (E edge:(Collection<E>)node.getEdgesFrom())
			edges.add(edge);
		for (E edge:(Collection<E>)node.getEdgesTo())
			edges.add(edge);
		List<N> nodes = new ArrayList<N>(0);
		nodes.add(node);
		deleteElements(nodes, edges);
	}
	
    /**
     * It returns the edge factory which can be used for this graph edges creation.
     * @return This graph edge factory.
     */
	@SuppressWarnings("unchecked")
	protected EdgeFactory<N, E> getEdgeFactory() {
		try {
			return (EdgeFactory<N,E>)defaultEdgeFactory;
		} catch (ClassCastException e) {
			throw new UnsupportedOperationException("The edge factory for this graph ("+getClass().getName()+") does not support proper edges",e);
		}
	}
	
    /**
     * It creates an edge between the given nodes.
     * @param from Node the edge will be coming from.
     * @param to Node the edge will be going to.
     * @return The newly created edge.
     */
	@SuppressWarnings("unchecked")
	protected E createEdge(N from, N to) {
		return ((EdgeFactory<N,E>)getEdgeFactory()).getEdge(from, to);
	}
	
    /**
     * It returns the edge between the given nodes if there is any.
     * @param from Node the edge is comming from.
     * @param to Node the edge is going to.
     * @return The edge between the given nodes or <code>null</code> if there is none.
     */
	@SuppressWarnings("unchecked")
	public E getEdge(N from, N to) {
		E toReturn = null;
		for (E edge:(Collection<E>)from.getEdgesFrom()) {
			if (to.equals(edge.getTo())) {
				toReturn = edge;
				break;
			}
		}
		return toReturn;
	}

    /**
     * It sets the graph starting node. The first available node is used if 
     * this method is not used. 
     * It's required that the given start node is already in the graph.
     * @param node New start node to be setted.
     */
	@SuppressWarnings("unchecked")
	public void setStartNode(N node) {
		if (containsNode(node)) {
			//throw new IllegalArgumentException("You cannot set a start node which is not in the graph (Node="+node+")");
			getNodes().remove(node);
		}
		getNodes().add(0, node);
	}

	/* (non-Javadoc)
	 * @see dinapter.graph.Graph#getAllNodes()
	 */
	@SuppressWarnings("unchecked")
	public Collection<N> getAllNodes() {
		return (List<N>)getNodes();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String toReturn;
		if (getAllNodes().isEmpty())
			toReturn = "This graph is empty";
		else {
			StringBuffer string = new StringBuffer("This graph contains these nodes:");
			for (N node:getAllNodes()) {
				string.append("\n\t"+node);
				if (node != null)
					string.append(" (hashCode="+node.hashCode()+")");
			}
			string.append("\n---- End of Graph contents ----");
			toReturn = string.toString();
		}
		return toReturn;
	}
	
	private JGraphViewPane view = null;
	
	/**
	 * It returns a view of this graph.
	 * @return The view of this graph.
	 */
	public JGraphViewPane getGraphView() {
		if (view == null) {
			view = createNewView();
		}
		return view;	
	}
	
	/**
	 * It creates a new view for this graph.
	 * @return The newly created view for this graph.
	 */
	protected JGraphViewPane createNewView() {
		JGraphPane jGraphPane = new JGraphPane(this);
		TranslateLens m_translateLens=new TranslateLens();
        ZoomLens m_zoomLens=new ZoomLens();
        RotateLens m_rotateLens = new RotateLens();
        CursorLens m_draggingLens = new CursorLens();
        TooltipLens m_tooltipLens = new TooltipLens();
        LegendLens m_legendLens = new LegendLens();
        NodeSizeLens m_nodeSizeLens = new NodeSizeLens();
        /* NodeFilterLens m_nodeFilterLens = new NodeFilterLens
        	(new ArrayList<Class>(Arrays.asList(nodeClasses))); */
        LensSet lensSet = new LensSet();
        lensSet.addLens(m_rotateLens);
        lensSet.addLens(m_translateLens);
        lensSet.addLens(m_zoomLens);
        lensSet.addLens(m_draggingLens);
        lensSet.addLens(m_tooltipLens);
        lensSet.addLens(m_legendLens);
        lensSet.addLens(m_nodeSizeLens);
        //lensSet.addLens(m_nodeFilterLens);
        jGraphPane.setLens(lensSet);
        
        customizeJGraphPane(jGraphPane);
        
        jGraphPane.addManipulator(new SelectionManipulator(new DefaultNodeSelectionModel(this), -1, MouseEvent.CTRL_MASK));
        jGraphPane.addManipulator(new DraggingManipulator(m_draggingLens, -1));
        jGraphPane.addManipulator(new EdgeCreatorManipulator(m_draggingLens, new DefaultEdgeCreatorListener(this)));
        jGraphPane.addManipulator(new ContextMenuAndToolTipManipulator
        		(jGraphPane, 
        		 new DefaultContextMenuListener(this, m_legendLens, m_zoomLens, ZoomControlPanel.DEFAULT_ZOOM_LEVELS, m_rotateLens, RotateControlPanel.DEFAULT_ROTATE_ANGLES)
        		, new DefaultToolTipListener()
        		, m_tooltipLens));
        
        /*
        Color light_blue = new Color(102, 204, 255);
        Color dark_blue = new Color(0, 153, 255);
        Color light_red = new Color(255, 102, 102);
        Color dark_red = new Color(204, 51, 51);
        Color light_green = new Color(153, 255, 102);
        Color dark_green = new Color(0, 204, 0);
        Color black = Color.BLACK;
        Color other = Color.orange;
        Color light_other = other.brighter();
        */
        Color gray = Color.GRAY;
                
        jGraphPane.setDefaultEdgePainter(new LineEdgePainter(gray, gray, gray));
        
        /*
        jGraphPane.setNodePainter(nodeClasses[2], new ShapeNodePainter(ShapeNodePainter.ELLIPSE, light_other, other, black));
        jGraphPane.setNodePainter(nodeClasses[3], new ShapeNodePainter(ShapeNodePainter.ELLIPSE, light_blue, dark_blue, black));
        jGraphPane.setNodePainter(nodeClasses[1], new ShapeNodePainter(ShapeNodePainter.RECTANGLE, light_red, dark_red, black));
        jGraphPane.setNodePainter(nodeClasses[0], new ShapeNodePainter(ShapeNodePainter.TRIANGLE, light_green, dark_green, black));
        */
        jGraphPane.setAntialias(true);
        
        Layouter m_layouter = new Layouter(new SpringLayoutStrategy(this));
        m_layouter.start();
        
        JGraphScrollPane scroll = new JGraphScrollPane(jGraphPane, m_translateLens);
        return new JGraphViewPane(scroll, m_zoomLens, m_rotateLens, m_draggingLens, m_tooltipLens, m_legendLens, m_nodeSizeLens);
	}

	protected void customizeJGraphPane(JGraphPane graphPane) {
	}

	@Override
	public <V extends N, G extends Graph<V>> void includeGraph(G toInclude) {
		throw new UnsupportedOperationException("This method is not implemented");
	}

	@Override
	public boolean isIsomorphism(Graph<? super N> graph) {
		throw new UnsupportedOperationException();
	}

}
