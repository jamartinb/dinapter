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
package dinapter.behavior;

import net.sourceforge.jpowergraph.Edge;
import net.sourceforge.jpowergraph.Node;
import dinapter.graph.JPowerGraph;

public class JPowerBehaviorGraph<A, N extends BehaviorNode<A> & Node, E extends Edge>
		extends JPowerGraph<N, E> implements BehaviorGraph<N> {
	
	private String side = null;
        private static int id_counter = 0;
        private Object id;

	public JPowerBehaviorGraph() {
		super();
                synchronized (JPowerBehaviorGraph.class) {
                    id = id_counter++;
                }
	}

	/**
	 * @return the side
	 */
	public String getSide() {
            if ((side == null) && (id instanceof String))
                return (String) getId();
            return side;
	}

	/**
	 * @param side the side to set
	 */
	public void setSide(String side) {
		this.side = side;
	}
        
        public void setId(Object id) {
            this.id = id;
        }
        
        public Object getId() {
            return id;
        }
}
