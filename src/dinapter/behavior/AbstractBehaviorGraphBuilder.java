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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import dinapter.behavior.BehaviorNode.BehaviorNodeType;
import dinapter.graph.AbstractGraphBuilder;
import dinapter.graph.Graph;
import dinapter.graph.ModifiableGraph;

/**
 * Convenient abstract class which unifies all node creation into a single method: {@link #createNode(Object, dinapter.behavior.BehaviorNode.BehaviorNodeType, List)}.
 * @author José Antonio Martín Baena
 * @version $Revision: 451 $ - $Date: 2007-02-06 10:52:47 +0100 (mar, 06 feb 2007) $
 */
public abstract class AbstractBehaviorGraphBuilder<A, N extends BehaviorNode<A>, G extends ModifiableGraph<N>> 
	extends AbstractGraphBuilder<N,G> 
	implements BehaviorGraphBuilder<N,G,A> {
	
	/**
     * It instantiates this class. 
	 */
    public AbstractBehaviorGraphBuilder() {
		super();
	}

	/* (non-Javadoc)
	 * @see dinapter.behavior.GraphBuilder#createNode(dinapter.behavior.BehaviorNode.BehaviorNodeType, A[])
	 */
	public N createNode(BehaviorNodeType type, A... arguments) {
		return createNode(null,type,Arrays.asList(arguments));
	}

	/* (non-Javadoc)
	 * @see dinapter.behavior.GraphBuilder#createNode(java.lang.Object, dinapter.behavior.BehaviorNode.BehaviorNodeType, A[])
	 */
	public N createNode(Object description, BehaviorNodeType type,
			A... arguments) {
		return createNode(description,type,Arrays.asList(arguments));
	}
	
	/**
     * It creates a new behavior node with the given type and description and no arguments. 
     * @param description Behavior node description.
     * @param type Behavior node type.
     * @return The newly created behavior node.
	 */
    public N createNode(Object description, BehaviorNodeType type) {
		return createNode(description, type, new ArrayList<A>(0));
	}
	
    /**
     * It creates a new behavior ndoe with the given type, description and list of arguments.
     * @param description Behavior node description.
     * @param type Behavior node type.
     * @param arguments Arguments of the behavior node.
     * @return The newly created behavior node.
     */
	public abstract N createNode
		(Object description, BehaviorNodeType type, List<A> arguments);

	/* (non-Javadoc)
	 * @see dinapter.behavior.BehaviorGraphBuilder#copyGraph(dinapter.graph.Graph)
	 */
	@Override
	public <C extends N> G copyGraph(Graph<C> toCopy) {
		HashMap<C, N> nodes = new HashMap<C, N>(toCopy.getAllNodes().size());
		createNewGraph();
		for (C node:toCopy.getAllNodes()) {
			nodes.put(node,createNode(node.getDescription(), node.getType(), node.getArguments()));
		}
		for (C node:toCopy.getAllNodes()) {
			for (C child:toCopy.getChildren(node)) {
				if (!nodes.containsKey(child)) {
					throw new RuntimeException("Exception trying to copy the behavioral graph. (node:"+child+")");
				}
				link(nodes.get(node), nodes.get(child));
			}
		}
		getGraph().setStartNode(nodes.get(toCopy.getStartNode()));
		getGraph().setEndNode(nodes.get(toCopy.getEndNode()));
		return getGraph();
	}
}
