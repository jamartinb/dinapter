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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This class provides an implementation for the {@link Graph#containsNode(Object)} method.
 * @author José Antonio Martín Baena
 *
 * @param <N> The class of the nodes of the graph.
 */
public abstract class AbstractGraph<N> implements Graph<N> {

	@Override
	public boolean containsNode(N node) {
		return getAllNodes().contains(node);
	}
	
	/**
	 * It returns whether two graphs are isomorphic or not.
	 * @param graph The other graph to compare.
	 * @return <code>true</code> if the graphs are isomorphic.
	 */
	public boolean isIsomorphism(Graph<? super N> graph) {
		Set<N> toExplore = null;
		boolean toReturn = 
			(getStartNode() == graph.getStartNode())
			&& (getEndNode() == graph.getEndNode())
			&& (toExplore = (new HashSet<N>(getAllNodes()))).equals(new HashSet<Object>(graph.getAllNodes()));
		if (toReturn) {
			Iterator<N> iter = toExplore.iterator();
			while (toReturn && iter.hasNext()) {
				N node = iter.next();
				toReturn &= graph.containsNode(node);
				if (toReturn) {
					Collection<N> children = getChildren(node);
					Collection<?> otherChildren = graph.getChildren(node);
					toReturn &= (new HashSet<N>(children)).equals(new HashSet<Object>(otherChildren));
				}
			}
		}
		return toReturn;
	}

}
