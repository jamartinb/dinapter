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
package dinapter.specificator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;

import net.sourceforge.jpowergraph.Node;
import net.sourceforge.jpowergraph.layout.Layouter;
import net.sourceforge.jpowergraph.layout.SpringLayoutStrategy;
import net.sourceforge.jpowergraph.lens.CursorLens;
import net.sourceforge.jpowergraph.lens.LegendLens;
import net.sourceforge.jpowergraph.lens.LensSet;
import net.sourceforge.jpowergraph.lens.NodeFilterLens;
import net.sourceforge.jpowergraph.lens.RotateLens;
import net.sourceforge.jpowergraph.lens.TooltipLens;
import net.sourceforge.jpowergraph.lens.TranslateLens;
import net.sourceforge.jpowergraph.lens.ZoomLens;
import net.sourceforge.jpowergraph.manipulator.contextandtooltip.ContextMenuAndToolTipManipulator;
import net.sourceforge.jpowergraph.manipulator.contextandtooltip.DefaultContextMenuListener;
import net.sourceforge.jpowergraph.manipulator.contextandtooltip.ToolTipListener;
import net.sourceforge.jpowergraph.manipulator.dragging.DraggingManipulator;
import net.sourceforge.jpowergraph.manipulator.edgecreator.DefaultEdgeCreatorListener;
import net.sourceforge.jpowergraph.manipulator.edgecreator.EdgeCreatorManipulator;
import net.sourceforge.jpowergraph.manipulator.selection.DefaultNodeSelectionModel;
import net.sourceforge.jpowergraph.manipulator.selection.HighlightingManipulator;
import net.sourceforge.jpowergraph.manipulator.selection.SelectionManipulator;
import net.sourceforge.jpowergraph.painters.LineEdgePainter;
import net.sourceforge.jpowergraph.painters.ShapeNodePainter;
import net.sourceforge.jpowergraph.pane.JGraphPane;
import net.sourceforge.jpowergraph.pane.JGraphScrollPane;
import net.sourceforge.jpowergraph.pane.JGraphViewPane;
import net.sourceforge.jpowergraph.viewcontrols.RotateControlPanel;
import net.sourceforge.jpowergraph.viewcontrols.ZoomControlPanel;
import dinapter.graph.AbstractGraphBuilder;

/**
 * @author José Antonio Martín Baena
 * @version $Revision: 421 $ - $Date: 2007-01-15 11:58:51 +0100 (lun, 15 ene 2007) $
 */
public class JPowerSpecificatorGraphBuilder<R extends Rule> extends AbstractGraphBuilder<JPowerSpecification<R>,JPowerSpecificatorGraph<R, JPowerSpecification<R>>> {
	public static final int LAYOUTER_NODES_LIMIT = 50;
	
	private static final Map<JPowerSpecificatorGraph<?, ?>, JGraphViewPane> viewMap = new HashMap<JPowerSpecificatorGraph<?,?>, JGraphViewPane>();
	private static final Map<JPowerSpecificatorGraph<?, ?>, Layouter> layouterMap = new HashMap<JPowerSpecificatorGraph<?,?>, Layouter>();

	protected static class MultilineNoderPainter extends ShapeNodePainter {
		protected FontMetrics fontMetrics = null;

		public MultilineNoderPainter(int theShape, Color theBackgroundColor, Color theBorderColor, Color theTextColor) {
			super(theShape, theBackgroundColor, theBorderColor, theTextColor);
		}

		public MultilineNoderPainter(int theShape) {
			super(theShape);
		}

		
		@Override
		public void getNodeScreenBounds(JGraphPane graphPane, Node node, int size, Rectangle nodeScreenRectangle) {
			//System.err.println("["+width+"x"+height+"]");
			//System.err.println(node.getLabel());
			nodeScreenRectangle.setBounds
				(getBounds(graphPane, node));
		}
		
		protected Rectangle getBounds(JGraphPane graphPane, Node node) {
			Point nodePoint=graphPane.getScreenPointForNode(node);
	        if (fontMetrics == null){
	            fontMetrics = graphPane.getFontMetrics(graphPane.getFont());
	        }
			int width = 0;
			int height = 0;
			int lines = 0;
			for (String line:node.getLabel().split("\n")) {
				lines++;
				int aux = fontMetrics.stringWidth(line);
				if (aux > width)
					width = aux;
			}
			width += 7;
			height += (fontMetrics.getAscent() + fontMetrics.getDescent())*lines + 5;
			return new Rectangle(nodePoint.x - width/2
					,nodePoint.y - height/2
					, width, height);
		}

		@Override
		public void paintNode(JGraphPane graphPane, Graphics2D g, Node node, int size) {
	        HighlightingManipulator highlightingManipulator=(HighlightingManipulator)graphPane.getManipulator(HighlightingManipulator.NAME);
	        boolean isHighlighted=highlightingManipulator!=null && highlightingManipulator.getHighlightedNode()==node;
	        SelectionManipulator selectionManipulator=(SelectionManipulator)graphPane.getManipulator(SelectionManipulator.NAME);
	        boolean isSelected=selectionManipulator!=null && selectionManipulator.getNodeSelectionModel().isNodeSelected(node);
	        DraggingManipulator draggingManipulator=(DraggingManipulator)graphPane.getManipulator(DraggingManipulator.NAME);
	        boolean isDragging=draggingManipulator!=null && draggingManipulator.getDraggedNode()==node;
	        if (fontMetrics == null){
	            fontMetrics = graphPane.getFontMetrics(graphPane.getFont());
	        }
	        Point nodePoint=graphPane.getScreenPointForNode(node);
			int width=20;
            int height=5;
            int textX=0;
            int textY=0;
            String label=node.getLabel();
            if (label!=null) {
            	Rectangle bounds = getBounds(graphPane, node);
                width+=bounds.width;
                height+=bounds.height;
                textX=bounds.x;
                textY=bounds.y + fontMetrics.getAscent();
            }
            else {
                width+=40;
                height+=20;
            }
            Color oldColor=g.getColor();
            g.setColor(getBackgroundColor(isHighlighted,isSelected,isDragging));
            g.fillRect(nodePoint.x-width/2,nodePoint.y-height/2,width,height);
            if (label!=null) {
                Font oldFont=g.getFont();
                g.setFont(graphPane.getFont());
                g.setColor(getTextColor(isHighlighted,isSelected,isDragging));
                for (String line:label.split("\n")) {
                		g.drawString(line,textX,textY);
                		textY += fontMetrics.getAscent() + fontMetrics.getDescent();
            	}
                g.setFont(oldFont);
            }
            g.setColor(getBorderColor(isHighlighted,isSelected,isDragging));
            g.drawRect(nodePoint.x-width/2,nodePoint.y-height/2,width,height);
            g.setColor(oldColor);
		}
	}
	
	protected static class SpecificationToolTipListener implements ToolTipListener {

		public boolean addNodeToolTipItems(Node theNode, JComponent theComponent, Color backgroundColor) {
			String message = "<html>";
			JPowerSpecification spec = (JPowerSpecification) theNode;
			message +=     "<b>  Node hashCode:</b> "+spec.hashCode();
			message += "<br><b> Rules hashCode:</b> "+(spec.getRules().isEmpty()?"-":spec.getRules().hashCode());
			message += "<br><b>Acumulated cost:</b> "+spec.getAcumulatedCost();
			message += "<br><b>      Node cost:</b> "+spec.getCost();
			message += "<br><b> Node Heuristic:</b> "+spec.getHeuristic();
			message += "<br><b>   Node F Value:</b> "+(spec.getHeuristic()+spec.getAcumulatedCost());
			if (spec.isCuttedSpecification())
				message += "<br><b>       Node cut:</b> TRUE";
			if (spec.isCopiedSpecification())
				message += "<br><b>    Node copied:</b> TRUE";
			if (spec.isChildrenNeeded())
				message += "<br><b>Children needed:</b> TRUE";
			if (!spec.isChildrenReady())
				message += "<br><b> Children ready:</b> FALSE";
			if (spec.isMerged())
				message += "<br><b>         Merged:</b> TRUE";
			if (spec.isSolution())
				message += "<br><b>    Is Solution:</b> TRUE";
			if (spec.isBestSolution())
				message += "<br><b>  Best Solution:</b> TRUE";
			message += "<br>-------------------------------<br>";
			message +=     "<b> Left Path:</b> "+spec.pathToString(true);
			message += "<br><b>Right Path:</b> "+spec.pathToString(false);
			message += "<br>-------------------------------<br>";
			message += spec.getToolTipLog();
			message += "</html>";
			theComponent.setLayout(new BorderLayout());
			theComponent.add(new JLabel(message));
			return true;
		}

		public void removeNodeToolTipItems(Node theNode, JComponent theComponent) {
			theComponent.removeAll();
		}
	}
	
	public class StartSpecification extends JPowerSpecification<R> {
		private static final long serialVersionUID = 7229370562299826695L;

		public StartSpecification() {
			super(new ArrayList<R>(0));
		}
		
		public StartSpecification(Collection<R> rules) {
			super(rules);
			if (!rules.isEmpty())
				throw new IllegalArgumentException("Start specification should be empty ruled.");
		}

		/* (non-Javadoc)
		 * @see net.sourceforge.jpowergraph.DefaultNode#getNodeType()
		 */
		@Override
		public String getNodeType() {
			return "Start";
		}
	}

	protected Class<?> [] nodeClasses = {StartSpecification.class, JPowerSpecification.class};
	
	private JGraphViewPane view = null;
	private boolean wrappingEnabled = false;
	private boolean childrenReady = true;
        
        private static final Collection EMPTY_RULE_COLLECTION = Collections.EMPTY_LIST;
	
	/* (non-Javadoc)
	 * @see dinapter.graph.GraphBuilder#createNewGraph()
	 */
	public JPowerSpecificatorGraph<R, JPowerSpecification<R>> createNewGraph() {
		return new JPowerSpecificatorGraph<R, JPowerSpecification<R>>();
	}

	public JPowerSpecification<R> createSpecification(R... rules) {
		JPowerSpecification<R> toReturn = null;
		if (wrappingEnabled && (rules.length == 0))
			toReturn = new StartSpecification();
		else
			toReturn = new JPowerSpecification<R>(Arrays.asList(rules));
		toReturn.setChildrenReady(isChildrenReady());
		return toReturn;
	}
	
    @SuppressWarnings("unchecked")
    public JPowerSpecification<R> createSpecification() {
        return createSpecification((Collection<R>)EMPTY_RULE_COLLECTION, null);
    }
	
	public JPowerSpecification<R> createSpecification(Collection<R> rules, R workingRule) {
		// @toreview I don't really like to change whatever collection I receive to a list.
		JPowerSpecification<R> toReturn = new JPowerSpecification<R>(new ArrayList<R>(rules));
		if (!rules.isEmpty())
			toReturn.setWorkingRule(workingRule);
		toReturn.setChildrenReady(isChildrenReady());
		return toReturn;
	}
	
	public JPowerSpecification<R> createSpecification(List<R> rules) {
		JPowerSpecification<R> toReturn = new JPowerSpecification<R>(new ArrayList<R>(rules));
		toReturn.setChildrenReady(isChildrenReady());
		return toReturn;
	}
	
	/**
	 * @return the view
	 */
	public JGraphViewPane getGraphView() {
		if (view == null) {
			if ((view = viewMap.get(getGraph())) == null) {
				view = createNewView();
				viewMap.put(getGraph(), view);
			}
		}
		return view;
	}
	
	private JGraphViewPane createNewView() {
		JPowerSpecificatorGraph<R, JPowerSpecification<R>> graph = getGraph();
		JGraphPane jGraphPane = new JGraphPane(graph);
		TranslateLens m_translateLens=new TranslateLens();
        ZoomLens m_zoomLens=new ZoomLens();
        RotateLens m_rotateLens = new RotateLens();
        CursorLens m_draggingLens = new CursorLens();
        TooltipLens m_tooltipLens = new TooltipLens();
        m_tooltipLens.setShowToolTips(false);
        LegendLens m_legendLens = new LegendLens();
        //NodeSizeLens m_nodeSizeLens = new NodeSizeLens();
        NodeFilterLens m_nodeFilterLens = new NodeFilterLens
        	(new ArrayList<Class>(Arrays.asList(nodeClasses)));
        LensSet lensSet = new LensSet();
        lensSet.addLens(m_rotateLens);
        lensSet.addLens(m_translateLens);
        lensSet.addLens(m_zoomLens);
        lensSet.addLens(m_draggingLens);
        lensSet.addLens(m_tooltipLens);
        lensSet.addLens(m_legendLens);
        //lensSet.addLens(m_nodeSizeLens);
        lensSet.addLens(m_nodeFilterLens);
        jGraphPane.setLens(lensSet);
        
        jGraphPane.addManipulator(new SelectionManipulator(new DefaultNodeSelectionModel(graph), -1, MouseEvent.CTRL_MASK));
        jGraphPane.addManipulator(new DraggingManipulator(m_draggingLens, -1));
        jGraphPane.addManipulator(new EdgeCreatorManipulator(m_draggingLens, new DefaultEdgeCreatorListener(graph)));
        jGraphPane.addManipulator(new ContextMenuAndToolTipManipulator
        		(jGraphPane, 
        		 new DefaultContextMenuListener(graph, m_legendLens, m_zoomLens, ZoomControlPanel.DEFAULT_ZOOM_LEVELS, m_rotateLens, RotateControlPanel.DEFAULT_ROTATE_ANGLES)
        		, new JPowerSpecificatorGraphBuilder.SpecificationToolTipListener()
        		, m_tooltipLens));
        
        Color light_blue = new Color(102, 204, 255);
        Color dark_blue = new Color(0, 153, 255);
        //Color light_red = new Color(255, 102, 102);
        //Color dark_red = new Color(204, 51, 51);
        Color light_green = new Color(153, 255, 102);
        Color dark_green = new Color(0, 204, 0);
        Color black = Color.BLACK;
        Color gray = Color.GRAY;
        //Color other = Color.orange;
        //Color light_other = other.brighter();
                
        jGraphPane.setDefaultEdgePainter(new LineEdgePainter(gray, gray, gray));
        
        jGraphPane.setNodePainter(nodeClasses[1], new MultilineNoderPainter(ShapeNodePainter.RECTANGLE, light_blue, dark_blue, black));
        jGraphPane.setNodePainter(nodeClasses[0], new ShapeNodePainter(ShapeNodePainter.TRIANGLE, light_green, dark_green, black));
        jGraphPane.setAntialias(true);
        
        if (getGraph().getAllNodes().size() <= LAYOUTER_NODES_LIMIT) {
	        Layouter m_layouter = new Layouter(new SpringLayoutStrategy(graph));
	        layouterMap.put(getGraph(), m_layouter);
	        m_layouter.start();
        }
        
        JGraphScrollPane scroll = new JGraphScrollPane(jGraphPane, m_translateLens);
        return view = new JGraphViewPane(scroll, m_zoomLens, m_rotateLens, m_draggingLens, m_tooltipLens, m_legendLens, null/*m_nodeSizeLens*/);
	}

	/**
	 * @return the wrappingEnabled
	 */
	public boolean isWrappingEnabled() {
		return wrappingEnabled;
	}

	/**
	 * @param wrappingEnabled the wrappingEnabled to set
	 */
	public void setWrappingEnabled(boolean enableWrapping) {
		this.wrappingEnabled = enableWrapping;
	}

	/**
	 * @return the childrenReady
	 */
	public boolean isChildrenReady() {
		return childrenReady;
	}

	/**
	 * @param childrenReady the childrenReady to set
	 */
	public void setChildrenReady(boolean childrenReady) {
		this.childrenReady = childrenReady;
	}

	/* (non-Javadoc)
	 * @see dinapter.graph.AbstractGraphBuilder#setGraph(dinapter.graph.ModifiableGraph)
	 */
	@Override
	public void setGraph(JPowerSpecificatorGraph<R, JPowerSpecification<R>> graph) {
		super.setGraph(graph);
		view = null;
	}

	/* (non-Javadoc)
	 * @see dinapter.graph.AbstractGraphBuilder#link(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void link(JPowerSpecification<R> from, JPowerSpecification<R> to) {
		super.link(from, to);
		if ((getGraph().getAllNodes().size() >= LAYOUTER_NODES_LIMIT) && (layouterMap.get(getGraph()) != null))
			layouterMap.get(getGraph()).stop();
	}
}
