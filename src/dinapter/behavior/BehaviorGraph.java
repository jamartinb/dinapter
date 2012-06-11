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

import dinapter.graph.Graph;

/**
 * This interface forces its graphs to contains nodes which extends the {@link BehaviorNode}
 * interface.
 * @author José Antonio Martín Baena
 * @version $Revision: 451 $ - $Date: 2007-02-06 10:52:47 +0100 (mar, 06 feb 2007) $
 * @param <N> Class of the containing behavior nodes.
 */
public interface BehaviorGraph<N extends BehaviorNode> extends Graph<N> {
    public void setId(Object id);
    public Object getId();
}
