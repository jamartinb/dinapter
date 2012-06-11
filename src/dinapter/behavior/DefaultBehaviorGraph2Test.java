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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static dinapter.behavior.BehaviorNode.*;
import dinapter.graph.ModifiableGraphTestAbstract;

/**
 * Additional JUnit tests of {@link DefaultBehaviorGraph}.
 * @author José Antonio Martín Baena
 * @version $Revision: 453 $ - $Date: 2007-02-06 19:08:32 +0100 (mar, 06 feb 2007) $
 */
public class DefaultBehaviorGraph2Test 
	extends ModifiableGraphTestAbstract<SimpleBehaviorNode<String>, DefaultBehaviorGraph<SimpleBehaviorNode<String>>> {
	
	protected static int counter = 0;
	private static final Object mutex = new Object();

	/* (non-Javadoc)
	 * @see dinapter.graph.ModifiableGraphTestAbstract#createGraph()
	 */
	@Override
	protected DefaultBehaviorGraph<SimpleBehaviorNode<String>> createGraph() {
		return new DefaultBehaviorGraph<SimpleBehaviorNode<String>>();
	}

	/* (non-Javadoc)
	 * @see dinapter.graph.ModifiableGraphTestAbstract#createNode()
	 */
	@Override
	protected SimpleBehaviorNode<String> createNode() {
		List<String> arguments = new ArrayList<String>(1);
		synchronized (mutex) {
			counter++;
			arguments.add("argument"+counter);
			return new SimpleBehaviorNode<String>(BehaviorNodeType.START,"action"+counter,arguments);
		}
	}
	
	@Test
	public void runTest() {
		assertTrue(true);
	}

}
