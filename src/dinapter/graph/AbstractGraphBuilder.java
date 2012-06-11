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
 * This class easies the implementation of {@link GraphBuilder} providing implementation for
 * some basic methods.
 * @author José Antonio Martín Baena
 * @version $Revision: 451 $ - $Date: 2007-02-06 10:52:47 +0100 (mar, 06 feb 2007) $
 */
public abstract class AbstractGraphBuilder<N,G extends ModifiableGraph<N>> implements GraphBuilder<N,G> {
    
    /**
     * Current building graph. This graph must be the target for all changes and it's 
     * automatically replaced on a new graph creation.
     */
	protected G buildingGraph = null;

	/**
     * It returns current graph or it creates a new one if it doesn't already exists.
     * Once returned the graph may still be modified by the builder until it's replaced
     * by a new graph creation.
	 * @see dinapter.graph.GraphBuilder#getGraph()
     * @return Current building graph.
	 */
	public G getGraph() {
		if (buildingGraph == null)
			setGraph(createNewGraph());
		return buildingGraph;
	}

    /**
     * It links two nodes within a graph. It is implemented using
     * graph's {@link ModifiableGraph#addEdge(Object, Object)} method.
     * @param from Node the vector comes from.
     * @param to Node the vector goes to.
     */
	public void link(N from, N to) {
		getGraph().addEdge(from, to);
	}
	
	/**
     * It replaces the current building graph. 
     * @param graph New building graph to set.
	 */
    public void setGraph(G graph) {
		buildingGraph = graph;
	}
}
