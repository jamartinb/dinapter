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
package dinapter.behavior;

import static dinapter.behavior.BehaviorNode.BehaviorNodeType.EXIT;
import static dinapter.behavior.BehaviorNode.BehaviorNodeType.RECEIVE;
import static dinapter.behavior.BehaviorNode.BehaviorNodeType.SEND;
import static dinapter.behavior.BehaviorNode.BehaviorNodeType.START;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sourceforge.jpowergraph.Edge;
import net.sourceforge.jpowergraph.layout.Layouter;
import net.sourceforge.jpowergraph.layout.SpringLayoutStrategy;
import net.sourceforge.jpowergraph.lens.CursorLens;
import net.sourceforge.jpowergraph.lens.LegendLens;
import net.sourceforge.jpowergraph.lens.LensSet;
import net.sourceforge.jpowergraph.lens.NodeFilterLens;
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
import net.sourceforge.jpowergraph.painters.ShapeNodePainter;
import net.sourceforge.jpowergraph.pane.JGraphPane;
import net.sourceforge.jpowergraph.pane.JGraphScrollPane;
import net.sourceforge.jpowergraph.pane.JGraphViewPane;
import net.sourceforge.jpowergraph.viewcontrols.RotateControlPanel;
import net.sourceforge.jpowergraph.viewcontrols.ZoomControlPanel;
import dinapter.behavior.BehaviorNode.BehaviorNodeType;

/**
 * @author José Antonio Martín Baena
 * @version $Revision: 389 $ - $Date: 2006-12-24 03:50:09 +0100 (dom, 24 dic 2006) $
 */
public class JPowerBehaviorGraphBuilder<A> 
	extends AbstractBehaviorGraphBuilder<A, JPowerBehaviorNode<A>, JPowerBehaviorGraph<A, JPowerBehaviorNode<A>, Edge>> {
	public class StartNode extends JPowerBehaviorNode<A> {
		public StartNode(BehaviorNodeType type, Object description, List<A> arguments) {
			super(type, description, arguments);
		}
	}
	public class EndNode extends JPowerBehaviorNode<A> {
		public EndNode(BehaviorNodeType type, Object description, List<A> arguments) {
			super(type, description, arguments);
		}
	}
	public class SendNode extends JPowerBehaviorNode<A> {
		public SendNode(BehaviorNodeType type, Object description, List<A> arguments) {
			super(type, description, arguments);
		}
	}
	public class ReceiveNode extends JPowerBehaviorNode<A> {
		public ReceiveNode(BehaviorNodeType type, Object description, List<A> arguments) {
			super(type, description, arguments);
		}
	}
	public class SpecialNode extends JPowerBehaviorNode<A> {
		public SpecialNode(BehaviorNodeType type, Object description, List<A> arguments) {
			super(type, description, arguments);
		}

		/* (non-Javadoc)
		 * @see dinapter.behavior.JPowerBehaviorNode#getNodeType()
		 */
		@Override
		public String getNodeType() {
			return "Others";
		}
	}
	
	public final Class [] nodeClasses = {StartNode.class, EndNode.class, ReceiveNode.class, SendNode.class, SpecialNode.class};

	private JGraphViewPane view = null;
	private boolean wrappingEnabled = false;

	/**
	 * 
	 */
	public JPowerBehaviorGraphBuilder() {
		super();
 	}

	/* (non-Javadoc)
	 * @see dinapter.behavior.AbstractBehaviorGraphBuilder#createNode(java.lang.Object, dinapter.behavior.BehaviorNode.BehaviorNodeType, java.util.List)
	 */
	@Override
	public JPowerBehaviorNode<A> createNode(Object description, BehaviorNodeType type,
			List<A> arguments) {
		JPowerBehaviorNode<A> toReturn = null;
		if (wrappingEnabled) {
			if (type.equals(START))
				toReturn = new StartNode(type,description,arguments);
			else if (type.equals(EXIT))
				toReturn = new EndNode(type,description,arguments);
			else if (type.equals(SEND))
				toReturn = new SendNode(type,description,arguments);
			else if (type.equals(RECEIVE))
				toReturn = new ReceiveNode(type,description,arguments);
			else
				toReturn = new SpecialNode(type,description,arguments);
		} else
			toReturn = new JPowerBehaviorNode<A>(type,description,arguments);
		return toReturn;
	}

	/* (non-Javadoc)
	 * @see dinapter.graph.GraphBuilder#createNewGraph()
	 */
	public JPowerBehaviorGraph<A, JPowerBehaviorNode<A>, Edge> createNewGraph() {
		setGraph(new JPowerBehaviorGraph<A, JPowerBehaviorNode<A>, Edge>());
		return getGraph();
	}
	
	/* (non-Javadoc)
	 * @see dinapter.graph.AbstractGraphBuilder#setGraph(dinapter.graph.ModifiableGraph)
	 */
	@Override
	public void setGraph(JPowerBehaviorGraph<A, JPowerBehaviorNode<A>, Edge> graph) {
		super.setGraph(graph);
		view = null;
	}

	/**
	 * @return the view
	 */
	public JGraphViewPane getGraphView() {
		if (view == null)
			view = createNewView();
		return view;
	}
	
	private JGraphViewPane createNewView() {
		JPowerBehaviorGraph<A, JPowerBehaviorNode<A>, Edge> graph = getGraph();
		JGraphPane jGraphPane = new JGraphPane(graph);
		TranslateLens m_translateLens=new TranslateLens();
        ZoomLens m_zoomLens=new ZoomLens();
        RotateLens m_rotateLens = new RotateLens();
        CursorLens m_draggingLens = new CursorLens();
        TooltipLens m_tooltipLens = new TooltipLens();
        LegendLens m_legendLens = new LegendLens();
        NodeSizeLens m_nodeSizeLens = new NodeSizeLens();
        NodeFilterLens m_nodeFilterLens = new NodeFilterLens
        	(new ArrayList<Class>(Arrays.asList(nodeClasses)));
        LensSet lensSet = new LensSet();
        lensSet.addLens(m_rotateLens);
        lensSet.addLens(m_translateLens);
        lensSet.addLens(m_zoomLens);
        lensSet.addLens(m_draggingLens);
        lensSet.addLens(m_tooltipLens);
        lensSet.addLens(m_legendLens);
        lensSet.addLens(m_nodeSizeLens);
        lensSet.addLens(m_nodeFilterLens);
        jGraphPane.setLens(lensSet);
        
        jGraphPane.addManipulator(new SelectionManipulator(new DefaultNodeSelectionModel(graph), -1, MouseEvent.CTRL_MASK));
        jGraphPane.addManipulator(new DraggingManipulator(m_draggingLens, -1));
        jGraphPane.addManipulator(new EdgeCreatorManipulator(m_draggingLens, new DefaultEdgeCreatorListener(graph)));
        jGraphPane.addManipulator(new ContextMenuAndToolTipManipulator
        		(jGraphPane, 
        		 new DefaultContextMenuListener(graph, m_legendLens, m_zoomLens, ZoomControlPanel.DEFAULT_ZOOM_LEVELS, m_rotateLens, RotateControlPanel.DEFAULT_ROTATE_ANGLES)
        		, new DefaultToolTipListener()
        		, m_tooltipLens));
        
        Color light_blue = new Color(102, 204, 255);
        Color dark_blue = new Color(0, 153, 255);
        Color light_red = new Color(255, 102, 102);
        Color dark_red = new Color(204, 51, 51);
        Color light_green = new Color(153, 255, 102);
        Color dark_green = new Color(0, 204, 0);
        Color black = Color.BLACK;
        Color gray = Color.GRAY;
        Color other = Color.orange;
        Color light_other = other.brighter();
                
        jGraphPane.setDefaultEdgePainter(new LineEdgePainter(gray, gray, gray));
        
        jGraphPane.setNodePainter(nodeClasses[2], new ShapeNodePainter(ShapeNodePainter.ELLIPSE, light_other, other, black));
        jGraphPane.setNodePainter(nodeClasses[3], new ShapeNodePainter(ShapeNodePainter.ELLIPSE, light_blue, dark_blue, black));
        jGraphPane.setNodePainter(nodeClasses[1], new ShapeNodePainter(ShapeNodePainter.RECTANGLE, light_red, dark_red, black));
        jGraphPane.setNodePainter(nodeClasses[0], new ShapeNodePainter(ShapeNodePainter.TRIANGLE, light_green, dark_green, black));
        jGraphPane.setAntialias(true);
        
        Layouter m_layouter = new Layouter(new SpringLayoutStrategy(graph));
        m_layouter.start();
        
        JGraphScrollPane scroll = new JGraphScrollPane(jGraphPane, m_translateLens);
        return view = new JGraphViewPane(scroll, m_zoomLens, m_rotateLens, m_draggingLens, m_tooltipLens, m_legendLens, m_nodeSizeLens);
	}

	/**
	 * @return the wrappingEnabled
	 */
	public boolean isWrappingEnabled() {
		return wrappingEnabled;
	}

	/**
	 * @param enableWrapping the wrappingEnabled to set
	 */
	public void setWrappingEnabled(boolean enableWrapping) {
		this.wrappingEnabled = enableWrapping;
	}
}
