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
package dinapter.graph;

/**
 * This class provides graph builders with a convenient factory for edges creation.
 * @param <N> Nodes between the edges are.
 * @param <E> Class of the edges. 
 * @author José Antonio Martín Baena
 * @version $Revision: 451 $ $Date: 2007-02-06 10:52:47 +0100 (mar, 06 feb 2007) $
 */
public interface EdgeFactory<N, E> {
    /**
     * It creates/returns an edge between the given nodes.
     * @param from Node the edge will be coming from.
     * @param to Node the edge will be going to.
     * @return The edge between the given nodes.
     */
	E getEdge(N from, N to);
}
