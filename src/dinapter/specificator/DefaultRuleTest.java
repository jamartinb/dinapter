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
package dinapter.specificator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dinapter.behavior.BehaviorNode;
import dinapter.behavior.DefaultBehaviorGraphBuilder;
import dinapter.behavior.SimpleBehaviorNode;
import dinapter.behavior.BehaviorNode.BehaviorNodeType;

/**
 * @author José Antonio Martín Baena
 *
 */
public class DefaultRuleTest {
	
	protected List<BehaviorNode<Object>> leftSide, rightSide;
	protected List<DefaultRule<BehaviorNode<Object>>> rules;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		DefaultBehaviorGraphBuilder<Object> builder = new DefaultBehaviorGraphBuilder<Object>();
		rules =  new ArrayList<DefaultRule<BehaviorNode<Object>>>(2);
		for (int j = 0;j<2;j++) {
			leftSide = new ArrayList<BehaviorNode<Object>>(3);
			rightSide = new ArrayList<BehaviorNode<Object>>(2);
			for (int i = 0;i<5;i++) {
				if (i < 3)
					leftSide.add(builder.createNode(BehaviorNodeType.SEND));
				else
					rightSide.add(builder.createNode(BehaviorNodeType.RECEIVE));
			}
			leftSide = java.util.Collections.unmodifiableList(leftSide);
			rightSide = java.util.Collections.unmodifiableList(rightSide);
			rules.add(new DefaultRule<BehaviorNode<Object>>(leftSide,rightSide,j==0,j==1));
		}
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		rules = null;
		leftSide = null;
		rightSide = null;
		Runtime.getRuntime().gc();
		Runtime.getRuntime().gc();
	}

	/**
	 * Test method for {@link dinapter.specificator.DefaultRule#hashCode()}.
	 */
	@Test
	public void testHashCodeDifferent() {
		assertNotSame(rules.get(0).hashCode(), rules.get(1).hashCode());
	}
	
	/**
	 * Test method for {@link dinapter.specificator.DefaultRule#hashCode()}.
	 */
	@Test
	public void testHashCode() {
		assertEquals(rules.get(1).hashCode(),rules.get(1).hashCode());
	}
	
	@Test
	public void testHashCodeB() {
		assertEquals(rules.get(1).hashCode(),new DefaultRule<BehaviorNode<Object>>(leftSide,rightSide,false,true).hashCode());
	}

	/**
	 * Test method for {@link dinapter.specificator.DefaultRule#getLeftSide()}.
	 */
	@Test
	public void testGetLeftSide() {
		assertEquals(leftSide,rules.get(1).getLeftSide());
	}

	/**
	 * Test method for {@link dinapter.specificator.DefaultRule#getRightSide()}.
	 */
	@Test
	public void testGetRightSide() {
		assertEquals(rightSide,rules.get(1).getRightSide());
	}

	/**
	 * Test method for {@link dinapter.specificator.DefaultRule#isInterleaved()}.
	 */
	@Test
	public void testIsInterleaved() {
		assertFalse(rules.get(0).isInterleaved());
		assertTrue(rules.get(1).isInterleaved());
	}

	/**
	 * Test method for {@link dinapter.specificator.DefaultRule#isRequired()}.
	 */
	@Test
	public void testIsRequired() {
		assertTrue(rules.get(0).isRequired());
		assertFalse(rules.get(1).isRequired());
	}

	/**
	 * Test method for {@link dinapter.specificator.DefaultRule#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		assertEquals(rules.get(1),rules.get(1));
	}

	@Test
	public void testEqualsObjectB() {
		assertEquals(rules.get(1),new DefaultRule<BehaviorNode<Object>>(leftSide,rightSide,false,true));
	}
	
	/**
	 * Test method for {@link dinapter.specificator.DefaultRule#equals(java.lang.Object)}.
	 */
	@Test
	public void testNotEqualsObject() {
		assertNotSame(rules.get(0), rules.get(1));
	}
	
	@Test
	public void testAllowedActions() {
		new DefaultRule<SimpleBehaviorNode>(null,new SimpleBehaviorNode(BehaviorNodeType.SEND));
		new DefaultRule<SimpleBehaviorNode>(null,new SimpleBehaviorNode(BehaviorNodeType.RECEIVE));
		assertTrue(true);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testDisallowedActions() {
		new DefaultRule<SimpleBehaviorNode>(null,new SimpleBehaviorNode(BehaviorNodeType.IF));
	}
}
