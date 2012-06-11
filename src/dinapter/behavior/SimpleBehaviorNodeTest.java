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
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import dinapter.behavior.BehaviorNode.BehaviorNodeType;

/**
 * @author José Antonio Martín Baena
 *
 */
public class SimpleBehaviorNodeTest {
	
	protected SimpleBehaviorNode<Object> defaultNode;
	protected List<Object> defaultArguments;
	protected static final BehaviorNode.BehaviorNodeType DEFAULT_TYPE = BehaviorNode.BehaviorNodeType.SEND;
	protected static final List<Object> DEFAULT_ARGUMENTS;
	
	static {
		final Object [] DEFAULT_ARGUMENTS_ARRAY = {1,"prueba",new Object()};
		DEFAULT_ARGUMENTS = Collections.unmodifiableList(Arrays.asList(DEFAULT_ARGUMENTS_ARRAY)); 
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		defaultNode = new SimpleBehaviorNode<Object>(DEFAULT_TYPE, DEFAULT_ARGUMENTS);
	}

	/**
	 * Test method for {@link dinapter.behavior.SimpleBehaviorNode#SimpleBehaviorNode(dinapter.behavior.BehaviorNode.BehaviorNodeType)}.
	 */
	@Test
	public void testDefaultBehaviorNodeBehaviorNodeType() {
		boolean assertion = true;
		for (BehaviorNodeType type:BehaviorNodeType.values()) {
			SimpleBehaviorNode<Object> node = new SimpleBehaviorNode<Object>(type);
			assertion &= (node.getType() == type);
		}
		assertTrue(assertion);
	}

	/**
	 * Test method for {@link dinapter.behavior.SimpleBehaviorNode#getArguments()}.
	 */
	@Test
	public void testGetArguments() {
		assertEquals(DEFAULT_ARGUMENTS.toArray(),defaultNode.getArguments().toArray());
	}

	/**
	 * Test method for {@link dinapter.behavior.SimpleBehaviorNode#getType()}.
	 */
	@Test
	public void testGetType() {
		assertEquals(DEFAULT_TYPE, defaultNode.getType());
	}
	
	/**
	 * Test method for {@link dinapter.behavior.SimpleBehaviorNode#toString()}.
	 */
	@Test
	public void testToString() {
		defaultNode.toString(); // Needed by jgraph Tree uses.
		assertTrue(true);
	}
	
	/**
	 * Test method for {@link dinapter.behavior.SimpleBehaviorNode#toString()}.
	 */
	@Test
	public void testDifferent() {
		assertNotSame(defaultNode, new SimpleBehaviorNode<Object>(BehaviorNodeType.FLOW));
	}
	
	/**
	 * Test method for {@link dinapter.behavior.SimpleBehaviorNode#toString()}.
	 */
	@Test
	public void testEqual() {
		assertEquals(defaultNode, defaultNode);//new SimpleBehaviorNode<Object>(DEFAULT_TYPE,null,DEFAULT_ARGUMENTS));
	}
	
	/**
	 * Test method for {@link dinapter.behavior.SimpleBehaviorNode#toString()}.
	 */
	@Test
	public void testSame() {
		assertEquals(defaultNode, defaultNode);
	}
	
	/**
	 * Test method for {@link dinapter.behavior.SimpleBehaviorNode#toString()}.
	 */
	@Test
	public void testDifferentHashcode() {
		assertNotSame(defaultNode.hashCode(), new SimpleBehaviorNode<Object>(BehaviorNodeType.FLOW).hashCode());
	}
	
	/**
	 * Test method for {@link dinapter.behavior.SimpleBehaviorNode#toString()}.
	 */
	@Test
	public void testEqualHashcode() {
		assertEquals(defaultNode.hashCode(), defaultNode.hashCode());//new SimpleBehaviorNode<Object>(DEFAULT_TYPE,null,DEFAULT_ARGUMENTS).hashCode());
	}
	
	/**
	 * Test method for {@link dinapter.behavior.SimpleBehaviorNode#toString()}.
	 */
	@Test
	public void testSameHashcode() {
		assertEquals(defaultNode.hashCode(), defaultNode.hashCode());
	}
}
