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
package dinapter.behavior;

import dinapter.behavior.BehaviorNode.BehaviorNodeType;
import dinapter.graph.MapGraph;
import dinapter.graph.ModifiableGraph;

/**
 * Behavior graph implementation using MapGraph.
 * @see <a href="http://www.jgraph.com/">JGraph Site.</a>
 * @author José Antonio Martín Baena
 * @version $Revision: 453 $ - $Date: 2007-02-06 19:08:32 +0100 (mar, 06 feb 2007) $
 */
public class DefaultBehaviorGraph<N extends SimpleBehaviorNode> extends MapGraph<N> implements
		ModifiableGraph<N>, BehaviorGraph<N> {
	
	private static final long serialVersionUID = 7815646824628119600L;
	protected N startNode = null;
        private Object id;
        private static int id_counter = 0;
	
	public DefaultBehaviorGraph() {
                initId();
	}
	
        private void initId() {
            synchronized (DefaultBehaviorGraph.class) {
                id = id_counter++;
            }
        }
	
	public N getEndNode() {
		N endNode = super.getEndNode();
		if (endNode == null)
			throw new RuntimeException("The end node hasn't been set.");
		else
			return endNode;
	}
	
	@Override
	public N getStartNode() {
		N startNode = super.getStartNode();
		if (startNode == null)
			throw new RuntimeException("The start node hasn't been set.");
		else
			return startNode;
	}

	/* (non-Javadoc)
	 * @see dinapter.graph.DefaultGraph#addEdge(org.jgraph.graph.DefaultGraphCell, org.jgraph.graph.DefaultGraphCell)
	 */
	@Override
	public void addEdge(N from, N to) {
		if ((super.getStartNode() == null) 
				&& (BehaviorNodeType.START == from.getType()))
			setStartNode(from);
		if ((super.getEndNode() == null)
				&& (BehaviorNodeType.EXIT == to.getType()))
			setEndNode(to);
		super.addEdge(from, to);
	}

        public Object getId() {
            return id;
        }
        
        public void setId(Object id) {
            this.id = id;
        }
}
