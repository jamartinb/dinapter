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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import dinapter.behavior.BehaviorNode.BehaviorNodeType;
import dinapter.graph.Graph;

/**
 * JUnit tests for {@link DefaultBehaviorGraphBuilder}.
 * @author José Antonio Martín Baena
 * @version $Revision: 453 $ - $Date: 2007-02-06 19:08:32 +0100 (mar, 06 feb 2007) $
 */
public class DefaultBehaviorGraphBuilderTest {
	
	protected DefaultBehaviorGraphBuilder<Object> builder;
	protected static final Object [] DEFAULT_ARGUMENTS_ARRAY = {1,"prueba",new Object()};
	protected static final String DEFAULT_NAME = "NodeTestName";
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		builder = new DefaultBehaviorGraphBuilder<Object>();
	}

	/**
	 * Test method for {@link dinapter.behavior.BehaviorGraphBuilder#createNewGraph()}.
	 */
	@Test
	public void testCreateNewGraph() {
		Graph<SimpleBehaviorNode<Object>> a,b;
		a = builder.createNewGraph();
		b = builder.createNewGraph();
		assertNotSame(a, b);
	}

	/**
	 * Test method for {@link BehaviorGraphBuilder#createNode(dinapter.behavior.BehaviorNode.BehaviorNodeType, Object...)}.
	 */
	@Test
	public void testCreateNodeBehaviorNodeTypeAArray() {
		boolean result = true;
		for (BehaviorNodeType type:BehaviorNodeType.values()) {
			SimpleBehaviorNode<Object> n = builder.createNode(type, DEFAULT_ARGUMENTS_ARRAY);
			//System.err.println("Type:"+n.getType()+" ; Arguments.length:"+n.getArguments().size());
			result &= (n.getType() == type) && Arrays.equals(n.getArguments().toArray(),DEFAULT_ARGUMENTS_ARRAY);
		}
		assertTrue(result);
	}

	/**
	 * Test method for {@link dinapter.behavior.DefaultBehaviorGraphBuilder#createNode(java.lang.Object, dinapter.behavior.BehaviorNode.BehaviorNodeType, A[])}.
	 */
	@Test
	public void testCreateNodeObjectBehaviorNodeTypeAArray() {
		boolean result = true;
		for (BehaviorNodeType type:BehaviorNodeType.values()) {
			SimpleBehaviorNode<Object> n = builder.createNode(DEFAULT_NAME,type, DEFAULT_ARGUMENTS_ARRAY);
			assertEquals("BehaviorNode type is not as expected",type, n.getType());
			assertTrue("BehaviorNode name is not as expected (It was:"+n.toString()+")"
					, n.toString().contains(DEFAULT_NAME));
			assertEquals("BehaviorNode was expected with default arguments"
					, Arrays.asList(DEFAULT_ARGUMENTS_ARRAY), n.getArguments());
		}
		assertTrue(result);
	}

	/**
	 * Test method for {@link dinapter.behavior.AbstractBehaviorGraphBuilder#createNode(java.lang.Object, dinapter.behavior.BehaviorNode.BehaviorNodeType)}.
	 */
	@Test
	public void testCreateNodeObjectBehaviorNodeType() {
		for (BehaviorNodeType type:BehaviorNodeType.values()) {
			SimpleBehaviorNode<Object> n = builder.createNode(DEFAULT_NAME,type);
			assertEquals("BehaviorNode type is not as expected",type, n.getType());
			assertTrue("BehaviorNode name is not as expected (Received\""+n.toString()+"\"", n.toString().contains(DEFAULT_NAME));
			assertTrue("BehaviorNode was expected with no arguments", n.getArguments().isEmpty());
		}
	}

	/**
	 * Test method for {@link dinapter.behavior.BehaviorGraphBuilder#getEnd()}.
	 *
	@Test
	public void testGetEndAllwaysSame() {
		SimpleBehaviorNode<Object> end1, end2;
		end1 = builder.getEnd();
		end2 = builder.getEnd();
		assertEquals(end1, end2);
	}
	
	/**
	 * Test method for {@link dinapter.behavior.BehaviorGraphBuilder#getEnd()}.
	 *
	@Test
	public void testGetEnd() {
		assertEquals(BehaviorNodeType.END, builder.getEnd().getType());
	}*/

	/**
	 * Test method for {@link dinapter.behavior.BehaviorGraphBuilder#getGraph()}.
	 */
	@Test
	public void testGetGraph() {
		builder.getGraph();
		assertTrue(true);
	}

	/**
	 * Test method for {@link dinapter.behavior.BehaviorGraphBuilder#getStart()}.
	 *
	@Test
	public void testGetStartType() {
		assertEquals(BehaviorNodeType.START, builder.getStart().getType());
	}
	
	/**
	 * Test method for {@link dinapter.behavior.BehaviorGraphBuilder#getStart()}.
	 *
	@Test
	public void testGetStart() {
		assertEquals(builder.getStart(), builder.getGraph().getStartNode());
	}
	
	/**
	 * Test method for {@link dinapter.behavior.BehaviorGraphBuilder#getStart()}.
	 *
	@Test
	public void testGetStartAllwaysSame() {
		SimpleBehaviorNode<Object> a,b;
		a = builder.getStart();
		b = builder.getStart();
		assertEquals(a, b);
	}*/

	/**
	 * Test method for {@link dinapter.behavior.BehaviorGraphBuilder#link(dinapter.behavior.SimpleBehaviorNode, dinapter.behavior.SimpleBehaviorNode)}.
	 */
	@Test
	public void testLink() {
		final SimpleBehaviorNode<Object> nodeA = 
			builder.createNode(BehaviorNodeType.START);
		final SimpleBehaviorNode<Object> nodeB = 
			builder.createNode(BehaviorNodeType.SEND, "Hola", "y", "adios");
		builder.link(nodeA, nodeB);
		//builder.link(node, builder.getEnd());
		//System.err.println(builder.getGraph().getAllNodes().length);
		assertEquals(nodeB,builder.getGraph().getChildren(nodeA).iterator().next());
	}

	@Test
	public void testFTPClient() {
		SimpleBehaviorNode<Object> a,b;
		a = builder.createNode(BehaviorNodeType.START);
		b = builder.createNode("login",BehaviorNodeType.SEND, "username","password");
		builder.link(a, b);
		a = builder.createNode("get", BehaviorNodeType.SEND,"filename");
		builder.link(b, a);
		b = builder.createNode("data", BehaviorNodeType.RECEIVE,"data");
		builder.link(a, b);
		a = builder.createNode(BehaviorNodeType.EXIT);
		builder.link(b,a);
	}
}
