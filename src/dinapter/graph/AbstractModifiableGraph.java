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
package dinapter.graph;

/**
 * This class provides implementation for some simple methods of {@link ModifiableGraph}.
 * 
 * @author José Antonio Martín Baena
 *
 * @param <N> The class of the nodes of the graph.
 */
public abstract class AbstractModifiableGraph<N> extends AbstractGraph<N> implements
		ModifiableGraph<N> {

	private N startNode = null;
    private N endNode = null;
    
	@Override
	public <V extends N, G extends Graph<V>> void includeGraph(G toInclude) {
		for (V node:toInclude.getAllNodes()) {
			includeNodeAndEdges(node, toInclude);
		}
	}
	
	private <V extends N, G extends Graph<V>> void includeNodeAndEdges(V node, G toInclude) {
		for (V child:toInclude.getChildren(node)) {
			addEdge(node, child);
		}
	}

	@Override
	public void setEndNode(N node) {
		endNode = node;
	}

	@Override
	public void setStartNode(N node) {
		startNode = node;
	}

	@Override
	public N getEndNode() {
		return endNode;
	}

	@Override
	public N getStartNode() {
		return startNode;
	}
	
}
