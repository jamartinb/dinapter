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

import java.util.List;

import dinapter.behavior.BehaviorNode.BehaviorNodeType;

/**
 * This is an utility class for building behavior graphs using the jgraph library.
 * @author José Antonio Martín Baena
 * @version $Revision: 453 $ - $Date: 2007-02-06 19:08:32 +0100 (mar, 06 feb 2007) $
 */
public class DefaultBehaviorGraphBuilder<A> 
	extends AbstractBehaviorGraphBuilder<A, SimpleBehaviorNode<A>, DefaultBehaviorGraph<SimpleBehaviorNode<A>>> {
	
	/* (non-Javadoc)
	 * @see dinapter.graph.GraphBuilder#createNewGraph()
	 */
	public DefaultBehaviorGraph<SimpleBehaviorNode<A>> createNewGraph() {
		return new DefaultBehaviorGraph<SimpleBehaviorNode<A>>();
	}

	public DefaultBehaviorGraphBuilder() {
		super();
	}
	
	public SimpleBehaviorNode<A> createNode(Object description, BehaviorNodeType type, List<A> arguments) {
		return new SimpleBehaviorNode<A>(type,description,arguments);
	}
}
