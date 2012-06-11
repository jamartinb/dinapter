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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import jsearchdemo.Node;

import org.apache.log4j.Logger;

import dinapter.graph.Graph;
import dinapter.graph.MapGraph;
import dinapter.graph.ModifiableGraph;

/**
 * @author José Antonio Martín Baena
 * @version $Revision: 453 $ - $Date: 2007-02-06 19:08:32 +0100 (mar, 06 feb 2007) $
 */
public class BridgeSpecificatorGraph<R extends Rule, N extends JSearchSpecification<R>>
		implements SpecificatorGraph<N>, jsearchdemo.Graph {
    
    private static final Logger log = Logger.getLogger(BridgeSpecificatorGraph.class);
    private final ModifiableGraph<N> innerGraph;
    
    /**
     *
     */
    public BridgeSpecificatorGraph() {
        this(new MapGraph<N>());
    }
    
    public BridgeSpecificatorGraph(ModifiableGraph<N> innerGraph) {
        this.innerGraph = innerGraph;
    }
    
        /* (non-Javadoc)
         * @see jsearchdemo.Graph#getChildren(jsearchdemo.Node)
         */
    @SuppressWarnings("unchecked")
    public List getChildren(Node n) {
        // Good as log as JPowerGraph returns Lists.
        return Arrays.asList(innerGraph.getChildren((N)n).toArray());
    }
    
        /* (non-Javadoc)
         * @see dinapter.graph.JPowerGraph#getEndNode()
         */
    @SuppressWarnings("unchecked")
    public N getEndNode() {
        return (N)getGoal();
    }
    
    public void setEndNode(N node) {
        innerGraph.setEndNode(node);
    }
    
        /* (non-Javadoc)
         * @see jsearchdemo.Graph#getGoal()
         */
    public jsearchdemo.Node getGoal() {
        return innerGraph.getEndNode();
    }
    
        /* (non-Javadoc)
         * @see jsearchdemo.Graph#getOutgoingEdges(jsearchdemo.Node)
         */
    @SuppressWarnings("unchecked")
    public List getOutgoingEdges(jsearchdemo.Node n) {
        if (!containsNode((N)n)) {
            String message = "This graph doesn't contain this node:\n"+n+" ("+(n!=null?n.hashCode():"")+")";
            throw new IllegalArgumentException(message);
        }
        N node = (N)n;
        Collection<N> children = getChildren(node);
        synchronized (n) {
            if (children.isEmpty() && !node.isChildrenNeeded() && !node.isChildrenReady())
                node.setChildrenNeeded(true);
            try {
                while (node.isChildrenNeeded()) {
                	if (log.isTraceEnabled()) {
                		log.trace("Waiting for children of node: "+node);
                	}
                    node.wait();
                }
            } catch (InterruptedException e) {
                log.error("Children wait interrupted");
                throw new RuntimeException("Children wait interrupted",e);
            }
        }
        // We request the children again once
        children = getChildren(node);
        // We wait untill all the costs and heuristics are calculated
        try {
	        for (N child:children) {
	        	synchronized (child) {
	        		while (!child.isCostReady()) {
	        			if (log.isTraceEnabled()) {
	        				log.trace("Waiting for children cost or heuristic: "+child);
	        			}
	        			child.wait();
	        		}
	        	}
	        }
        } catch (InterruptedException e) {
        	String message = "Children wait for cost or heuristic was interrupted"; 
        	log.error(message,e);
        	throw new RuntimeException(message,e);
        }
        List outgoingEdges = new ArrayList(children.size());
        for (N to:children)
            outgoingEdges.add(new WeighedEdge(node,to));
        return outgoingEdges;
        
    }

    public void removeEdge(N from, N to) {
        innerGraph.removeEdge(from,to);
    }

    public N getStart() {
        return innerGraph.getStartNode();
    }
    
    public void setStartNode(N node) {
        innerGraph.setStartNode(node);
    }

    public Collection<N> getAllNodes() {
        return innerGraph.getAllNodes();
    }

    public void addEdge(N from, N to) {
        innerGraph.addEdge(from,to);
    }

    public void removeNode(N node) {
        innerGraph.removeNode(node);
    }

    public Collection<N> getParents(N node) {
        return innerGraph.getParents(node);
    }

    public Collection<N> getChildren(N node) {
        return innerGraph.getChildren(node);
    }

    public boolean containsNode(N node) {
        return innerGraph.containsNode(node);
    }

    public N getStartNode() {
        return innerGraph.getStartNode();
    }

	@Override
	public <V extends N, G extends Graph<V>> void includeGraph(G toInclude) {
		innerGraph.includeGraph(toInclude);
		
	}

	@Override
	public boolean isIsomorphism(Graph<? super N> graph) {
		return innerGraph.isIsomorphism(graph);
	}
}
