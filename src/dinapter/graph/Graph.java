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

import java.util.Collection;


/**
 * A graph.
 * @param <N> Nodes of the graph.
 * @author José Antonio Martín Baena
 * @version $Revision: 451 $ - $Date: 2007-02-06 10:52:47 +0100 (mar, 06 feb 2007) $
 */
public interface Graph<N> {
    /**
     * It returns the children of the given node.
     * @param node Parent node.
     * @return Children of the parent node.
     */
	public Collection<N> getChildren(N node);
    
    /**
     * It returns the parents of the given node.
     * @param node Child node.
     * @return Parent nodes.
     */
	public Collection<N> getParents(N node);
    
    /**
     * It returns the start node of the graph. It gives an
     * starting point to explore the rest of the graph.
     * @return The start node of the graph.
     */
	public N getStartNode();
    
    /**
     * It returns the end node of the graph it there is any.
     * This method is very unlikely to be used and hence very
     * unlikely to be implemented but eventually it may be
     * usefull to know when the graph has been fully explored.
     * @return The end node of the graph.
     */
	public N getEndNode();
    
    /**
     * It returns whether the given node is contained by the graph.
     * @param node Node to be checked.
     * @return Whether the node is contained by the graph.
     */
	public boolean containsNode(N node);
    
    /**
     * It returns all the nodes contained by the graph.
     * @return All the nodes contained by the graph.
     */
	public Collection<N> getAllNodes();
	
	/**
	 * It returns whether two graphs are isomorphic or not.
	 * @param graph The other graph to compare.
	 * @return <code>true</code> if the graphs are isomorphic.
	 */
	public boolean isIsomorphism(Graph<? super N> graph);
}
