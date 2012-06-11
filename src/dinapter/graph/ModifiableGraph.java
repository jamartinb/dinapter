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
 * Author:  José Antonio Martín Baena
 * email:   jose.antonio.martin.baena@gmail.com
 *
 * This code is under the GPL License for any non-comercial project
 * after a notification is sent to the authors. For any other kind of
 * project an individual license must be obtained from the authors.
 *
 * This software is available as it is without any warranty or responsability
 * being held by the author. If you use this is at your own risk.
 *
 * Hope this code is usefull for you. Enjoy it!. ;)
 */
package dinapter.graph;


/**
 * This interfaces extends graph functionality providing new methods for modifying
 * the graph.
 * @author José Antonio Martín Baena
 * @version $Revision: 451 $ $Date: 2007-02-06 10:52:47 +0100 (mar, 06 feb 2007) $
 */
public interface ModifiableGraph<N> extends Graph<N> {
    /**
     * It removes the given node from the graph. All the edges going or coming
     * from the node are removed too. Notice that orphan nodes may arise.
     * @param node Node to be removed.
     */
	public void removeNode(N node);
    
    /**
     * It adds an edge between the given two nodes..
     * <bold>NOTE</bold> that only one edge is allowed between the same pair of nodes.
     * @param from Node the edge will be coming from.
     * @param to Node the edge will be going to.
     */
	public void addEdge(N from, N to);
    
    /**
     * It removes the edge between the given nodes. Notice that
     * orphan nodes may arise.
     * @param from Node the edge was coming from.
     * @param to Node the edge was going to.
     */
	public void removeEdge(N from, N to);
        
    /**
     * It sets the initial node of the graph. This only makes
     * this node easier to retrieve.
     * @param node new start node of the graph.
     */    
    public void setStartNode(N node);
    
    /**
     * It sets the start node of the graph. This only makes this
     * node easier to retrieve.
     * @param node new end node of the graph.
     */
    public void setEndNode(N node);
    
    /**
     * It adds all the nodes and edges of the given graph. If the graphs
     * contain some common nodes they will be treated as a single node and
     * both graphs will be connected.
     * @param toInclude the graph to be added.
     */
    public <V extends N, G extends Graph<V>> void includeGraph(G toInclude);
}
