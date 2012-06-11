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

import java.awt.Color;
import java.util.List;

import jsearchdemo.DefaultWeighedEdge;
import jsearchdemo.Node;
import jsearchdemo.WeighedEdge;
import net.sourceforge.jpowergraph.DefaultEdge;
import net.sourceforge.jpowergraph.Edge;
import net.sourceforge.jpowergraph.painters.ShapeNodePainter;
import net.sourceforge.jpowergraph.pane.JGraphPane;

import org.apache.log4j.Logger;

import dinapter.graph.EdgeFactory;
import dinapter.graph.JPowerGraph;
import dinapter.specificator.JPowerSpecificatorGraphBuilder.MultilineNoderPainter;

/**
 * @author José Antonio Martín Baena
 * @version $Revision: 453 $ - $Date: 2007-02-06 19:08:32 +0100 (mar, 06 feb 2007) $
 */
public class JPowerSpecificatorGraph<R extends Rule, N extends JPowerSpecification<R>> 
	extends JPowerGraph<N,JPowerSpecificatorGraph.JPowerSpecificatorEdge> 
	implements SpecificatorGraph<N>, jsearchdemo.Graph {
	
	protected class JPowerSpecificatorGraphEdgeFactory 
			implements EdgeFactory<N, JPowerSpecificatorGraph.JPowerSpecificatorEdge> {
		public JPowerSpecificatorEdge getEdge(N from, N to) {
			return new JPowerSpecificatorEdge(from,to,0);
		}
	}
    
    public static class ChildrenWaitInterruptedException extends RuntimeException {
        private static final long serialVersionUID = 3057894250541293591L;

        public ChildrenWaitInterruptedException() {
            super();
        }

        public ChildrenWaitInterruptedException(String message, Throwable cause) {
            super(message, cause);
        }

        public ChildrenWaitInterruptedException(String message) {
            super(message);
        }

        public ChildrenWaitInterruptedException(Throwable cause) {
            super(cause);
        }
    }
	
	private static final Logger log = Logger.getLogger(JPowerSpecificatorGraph.class);
	private final EdgeFactory<N, JPowerSpecificatorGraph.JPowerSpecificatorEdge> edgeFactory
		= new JPowerSpecificatorGraphEdgeFactory();
	
	public class JPowerSpecificatorEdge extends DefaultEdge implements Edge, WeighedEdge {
		private final DefaultWeighedEdge innerWeighedEdge;

		/* (non-Javadoc)
		 * @see net.sourceforge.jpowergraph.DefaultEdge#getLength()
		 */
		@Override
		public double getLength() {
			String [] lines = (getFrom().getLabel()+getTo().getLabel()).split("\n");
			double length = Math.max(80,(lines.length + 1) * 6);
			for (String line:lines)
				length = Math.max(length,line.length() * 5);
			return length/2;
		}

		public JPowerSpecificatorEdge(JPowerSpecification<R> from, JPowerSpecification<R> to, int weight) {
			super(from,to);
			innerWeighedEdge = new DefaultWeighedEdge(from,to,weight);
		}

		/**
		 * @return
		 * @see jsearchdemo.DefaultEdge#getBeginNode()
		 */
		public jsearchdemo.Node getBeginNode() {
			return innerWeighedEdge.getBeginNode();
		}

		/**
		 * @return
		 * @see jsearchdemo.DefaultEdge#getEndNode()
		 */
		public jsearchdemo.Node getEndNode() {
			return innerWeighedEdge.getEndNode();
		}

		/**
		 * @return
		 * @see jsearchdemo.DefaultWeighedEdge#getWeight()
		 */
		public int getWeight() {
            JPowerSpecification end = (JPowerSpecification)getEndNode();
            synchronized (end) {
                try {
                    while (!end.isCostReady())
                        end.wait();
                    return end.getCost();
                } catch (InterruptedException e) {
                    throw new RuntimeException("Interrupted when waiting for a Specification to become cost ready",e);
                }
            }
		}

		/**
		 * @param w
		 * @see jsearchdemo.DefaultWeighedEdge#setWeight(int)
		 */
		public void setWeight(int w) {
			throw new UnsupportedOperationException("The weight cannot be setted, destination node cost used instead.");
			//innerWeighedEdge.setWeight(w);
		}
	}

	/**
	 * 
	 */
	public JPowerSpecificatorGraph() {
	}

	/* (non-Javadoc)
	 * @see jsearchdemo.Graph#getChildren(jsearchdemo.Node)
	 */
	@SuppressWarnings("unchecked")
	public List getChildren(Node n) {
		// Good as log as JPowerGraph returns Lists.
		return (List)super.getChildren((N)n);
	}
	
	/* (non-Javadoc)
	 * @see dinapter.graph.JPowerGraph#getEndNode()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public N getEndNode() {
		return (N)getGoal();
	}

	/* (non-Javadoc)
	 * @see jsearchdemo.Graph#getGoal()
	 */
	public jsearchdemo.Node getGoal() {
		// @optional Implement this.
		return null;
	}
	
	/*
	@Deprecated("Using destination node cost instead")
	protected int getEdgeWeight(N from, N to) {
		// @t-odo Implement a proper weighting method.
		return 1;
	}*/

	/* (non-Javadoc)
	 * @see jsearchdemo.Graph#getOutgoingEdges(jsearchdemo.Node)
	 */
	@SuppressWarnings("unchecked")
	public List getOutgoingEdges(jsearchdemo.Node n) {
		if (!containsNode((N)n)) {
			String message = "This graph doesn't contain this node:\n"+n+" ("+(n!=null?n.hashCode():"")+")";
			/*
			message += "\tnot in";
			for (N node:getAllNodes())
				message += "\n"+node+" ("+(node!=null?node.hashCode():"")+")";*/
			throw new IllegalArgumentException(message);
		}
		JPowerSpecification<R> node = (JPowerSpecification<R>)n;
		List toReturn = node.getEdgesFrom();
		synchronized (node) {
			if (toReturn.isEmpty() && !node.isChildrenNeeded() && !node.isChildrenReady())
				node.setChildrenNeeded(true);
			try {
				while (node.isChildrenNeeded()) {
					log.trace("Waiting for children of node: "+node);
					node.wait();
				}
			} catch (InterruptedException e) {
				log.error("Children wait interrupted");
                throw new ChildrenWaitInterruptedException("Children wait interrupted",e);
			}
		}
		return node.getEdgesFrom();
	}

	/* (non-Javadoc)
	 * @see dinapter.graph.JPowerGraph#getEdgeFactory()
	 */
	@Override
	protected EdgeFactory<N, JPowerSpecificatorGraph.JPowerSpecificatorEdge> getEdgeFactory() {
		return edgeFactory;
	}

    public Node getStart() {
        return getStartNode();
    }

	@Override
	protected void customizeJGraphPane(JGraphPane graphPane) {
		Color light_blue = new Color(102, 204, 255);
        Color dark_blue = new Color(0, 153, 255);
        Color black = Color.BLACK;
        
		graphPane.setNodePainter(
				JPowerSpecification.class
				,new MultilineNoderPainter(ShapeNodePainter.RECTANGLE, light_blue, dark_blue, black));
	}
}
