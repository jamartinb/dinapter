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
import dinapter.graph.Graph;
import dinapter.graph.GraphBuilder;
import java.util.List;


/**
 * This interface states additional methods a <u>behavior</u> graph builder must implement
 * apart from GraphBuilder's. In particular node creation methods.
 * @param <N> Behavior node class that may compose the graph to build.
 * @param <A> Class of the behavior nodes arguments.
 * @param <G> Class of the graph to be buildt.
 * @author José Antonio Martín Baena
 * @version $Revision: 451 $ - $Date: 2007-02-06 10:52:47 +0100 (mar, 06 feb 2007) $
 */
public interface BehaviorGraphBuilder<N extends BehaviorNode<A>,G extends Graph<N>,A> extends GraphBuilder<N,G> {
    /**
     * It creates a behavior node of the given type using the given array of arguments.
     * @param type Type of the behavior node.
     * @param arguments Arguments of the behavior node.
     * @return Newly created behavior node.
     */
	public N createNode(BehaviorNodeType type, A... arguments);
    
    /**
     * It creates a behavior node of the given type, with the given description and using
     * the given array of arguments.
     * @param description Behavior node description.
     * @param type Behavior node type.
     * @param arguments Behavior node arguments.
     * @return Newly created behavior node.
     */
	public N createNode(Object description, BehaviorNodeType type, A... arguments);
        
	/**
     * It creates a behavior node of the given type, with the given description and using
     * the given array of arguments.
     * @param description Behavior node description.
     * @param type Behavior node type.
     * @param arguments Behavior node arguments.
     * @return Newly created behavior node.
     */
	public N createNode(Object description, BehaviorNodeType type, List<A> arguments);
	
	/**
	 * It creates a copy of the given graph. It might replace the current building graph returned by {@link GraphBuilder#getGraph()}.
	 * @param <C> type of the behavior nodes within the graph.
	 * @param toCopy graph to copy.
	 * @return a depth copy of the graph.
	 */
	public <C extends N> G copyGraph(Graph<C> toCopy);
}
