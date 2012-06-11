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

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import dinapter.behavior.BehaviorNode.BehaviorNodeType;

/**
 * JUnit tests for {@link DefaultBehaviorGraph}.
 * @author José Antonio Martín Baena
 * @version $Revision: 453 $ - $Date: 2007-02-06 19:08:32 +0100 (mar, 06 feb 2007) $
 */
public class DefaultBehaviorGraphTest extends SimpleBehaviorNodeTest {
	
	protected DefaultBehaviorGraph<SimpleBehaviorNode> defaultGraph;
	/**
	 * <ul>
	 * <li>First element must be super.defaultNode.</li>
	 * <li>Second element must be of END type.</li>
	 * <li>Third element must be of START type.</li>
	 * </ul>
	 */
	protected SimpleBehaviorNode [] defaultNodes;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		super.setUp();
		defaultNodes = new SimpleBehaviorNode[3];
		defaultNodes[0] = defaultNode;
		defaultNodes[1] = new SimpleBehaviorNode<Object>(BehaviorNodeType.EXIT);
		defaultNodes[2] = new SimpleBehaviorNode<Object>(BehaviorNodeType.START);
		defaultGraph = new DefaultBehaviorGraph<SimpleBehaviorNode>();
	}

	/**
	 * Test method for {@link dinapter.behavior.DefaultBehaviorGraph#addEdge(dinapter.behavior.SimpleBehaviorNode, dinapter.behavior.SimpleBehaviorNode)}.
	 */
	@Test
	public void testAddEdge() {
		defaultGraph.addEdge(defaultNode, defaultNode);
		assertEquals(defaultNode,defaultGraph.getChildren(defaultNode).iterator().next());
	}

	/**
	 * Test method for {@link dinapter.behavior.DefaultBehaviorGraph#removeEdge(dinapter.behavior.SimpleBehaviorNode, dinapter.behavior.SimpleBehaviorNode)}.
	 */
	@Test
	public void testRemoveEdge() {
		defaultGraph.addEdge(defaultNode, defaultNodes[1]);
		defaultGraph.addEdge(defaultNodes[2], defaultNode);
		defaultGraph.addEdge(defaultNodes[1],defaultNodes[2]);
		defaultGraph.removeEdge(defaultNodes[2], defaultNode);
		assertTrue(defaultGraph.getParents(defaultNode).isEmpty());
	}

	/**
	 * Test method for {@link dinapter.behavior.DefaultBehaviorGraph#removeNode(dinapter.behavior.SimpleBehaviorNode)}.
	 */
	@Test
	public void testRemoveNode() {
		defaultGraph.addEdge(defaultNode, defaultNode);
		defaultGraph.removeNode(defaultNode);
		assertTrue(defaultGraph.getParents(defaultNode).isEmpty());
	}

	/**
	 * Test method for {@link dinapter.behavior.DefaultBehaviorGraph#getChildren(dinapter.behavior.SimpleBehaviorNode)}.
	 */
	@Test
	public void testGetChildren() {
		SimpleBehaviorNode [] expected = new SimpleBehaviorNode[2]; 
		System.arraycopy(defaultNodes, 1, expected, 0, 2);
		defaultGraph.addEdge(defaultNode, defaultNodes[1]);
		defaultGraph.addEdge(defaultNode, defaultNodes[2]);
		assertTrue(Arrays.asList(expected).containsAll(defaultGraph.getChildren(defaultNode)));
	}

	/**
	 * Test method for {@link dinapter.behavior.BehaviorGraph#getEndNode()}.
	 */
	@Test
	public void testGetEndNode() {
		defaultGraph.addEdge(defaultNode, defaultNodes[1]);
		assertEquals(defaultNodes[1], defaultGraph.getEndNode());
	}

	/**
	 * Test method for {@link dinapter.behavior.DefaultBehaviorGraph#getParents(dinapter.behavior.SimpleBehaviorNode)}.
	 */
	@Test
	public void testGetParents() {
		SimpleBehaviorNode [] expected = new SimpleBehaviorNode[2]; 
		System.arraycopy(defaultNodes, 1, expected, 0, 2);
		defaultGraph.addEdge(defaultNodes[1], defaultNode);
		defaultGraph.addEdge(defaultNodes[2], defaultNode);
		assertTrue(Arrays.asList(expected).containsAll(defaultGraph.getChildren(defaultNode)));
	}

	/**
	 * Test method for {@link dinapter.behavior.BehaviorGraph#getStartNode()}.
	 */
	@Test
	public void testGetStartNode() {
		defaultGraph.addEdge(defaultNodes[2], defaultNodes[1]);
		assertEquals(defaultNodes[2], defaultGraph.getStartNode());
	}
	
	/**
	 * Test method for {@link dinapter.behavior.BehaviorGraph#getAllNodes()}.
	 */
	@Test
	public void testGetAllNodes() {
		SimpleBehaviorNode [] expected = defaultNodes;
		defaultGraph.addEdge(defaultNode, defaultNodes[1]);
		defaultGraph.addEdge(defaultNode, defaultNodes[2]);
		assertTrue(Arrays.asList(expected).containsAll(defaultGraph.getAllNodes()));
	}
}
