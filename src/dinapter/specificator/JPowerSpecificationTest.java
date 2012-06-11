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
package dinapter.specificator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import dinapter.behavior.JPowerBehaviorNode;
import dinapter.behavior.BehaviorNode.BehaviorNodeType;

/**
 * @author José Antonio Martín Baena
 *
 */
public class JPowerSpecificationTest {
	
	protected List<JPowerSpecification<DefaultRule<JPowerBehaviorNode<Object>>>> specifications;
	protected List<DefaultRule<JPowerBehaviorNode<Object>>> rulesList;
	protected static final int RULE_HEURISTIC = 1880;
	protected PropertyChangeEvent event = null;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		specifications = new ArrayList<JPowerSpecification<DefaultRule<JPowerBehaviorNode<Object>>>>(2);
		List<DefaultRule<JPowerBehaviorNode<Object>>> emptyList = new ArrayList<DefaultRule<JPowerBehaviorNode<Object>>>(0);
		rulesList = new ArrayList<DefaultRule<JPowerBehaviorNode<Object>>>(2);
		List<JPowerBehaviorNode<Object>> behaviorNodeList = new ArrayList<JPowerBehaviorNode<Object>>(1);
		behaviorNodeList.add(new JPowerBehaviorNode<Object>(BehaviorNodeType.SEND));
		rulesList.add(new DefaultRule<JPowerBehaviorNode<Object>>(behaviorNodeList, new ArrayList<JPowerBehaviorNode<Object>>(0)));
		rulesList.add(new DefaultRule<JPowerBehaviorNode<Object>>(behaviorNodeList, behaviorNodeList));
		rulesList.get(0).setHeuristic(RULE_HEURISTIC);
		rulesList.get(1).setHeuristic(RULE_HEURISTIC+2);
		specifications.add(new JPowerSpecification<DefaultRule<JPowerBehaviorNode<Object>>>(emptyList));
		specifications.add(new JPowerSpecification<DefaultRule<JPowerBehaviorNode<Object>>>(rulesList));
		specifications.add(new JPowerSpecification<DefaultRule<JPowerBehaviorNode<Object>>>(rulesList));
		specifications = java.util.Collections.unmodifiableList(specifications);
		event = null;
	}

	/**
	 * Test method for {@link dinapter.specificator.Specification#hashCode()}.
	 */
	@Test
	public void testHashCodeEqual() {
		assertEquals(specifications.get(0).hashCode(), specifications.get(0).hashCode());
	}
	
	/**
	 * Test method for {@link dinapter.specificator.Specification#hashCode()}.
	 */
	@Test
	public void testHashCodeDifferent() {
		assertTrue(specifications.get(0).hashCode() != specifications.get(1).hashCode());
	}

	/**
	 * Test method for {@link dinapter.specificator.JPowerSpecification#JPowerSpecification(java.util.List)}.
	 */
	@Test
	public void testDefaultAdaptorSpecification() {
		List<DefaultRule<JPowerBehaviorNode<Object>>> oneElementList = new ArrayList<DefaultRule<JPowerBehaviorNode<Object>>>(0);
		List<JPowerBehaviorNode<Object>> behaviorNodeList = new ArrayList<JPowerBehaviorNode<Object>>(1);
		behaviorNodeList.add(new JPowerBehaviorNode<Object>(BehaviorNodeType.RECEIVE));
		oneElementList.add(new DefaultRule<JPowerBehaviorNode<Object>>(behaviorNodeList, new ArrayList<JPowerBehaviorNode<Object>>(0)));
		JSearchSpecification<DefaultRule<JPowerBehaviorNode<Object>>> test = new SimpleSpecification<DefaultRule<JPowerBehaviorNode<Object>>>(oneElementList,oneElementList.get(oneElementList.size()-1));
		assertEquals(oneElementList, test.getRules());
		
	}

	/**
	 * Test method for {@link dinapter.specificator.Specification#getRules()}.
	 */
	@Test
	public void testGetRules() {
		assertEquals(rulesList, specifications.get(1).getRules());
	}

	/**
	 * Test method for {@link dinapter.specificator.Specification#getHeuristic()}.
	 */
	@Test
	public void testGetHeuristic() {
		assertEquals(JSearchSpecification.DEFAULT_HEURISTIC, specifications.get(0).getHeuristic());
	}
	
	/**
	 * Test method for {@link dinapter.specificator.Specification#getHeuristic()}.
	 * @d-eprecated Heuristic calculation goes beyond simple Specification class.
	 *-/
	@Deprecated
	@Test
	public void testGetEvaluatedHeuristic() {
		assertEquals(RULE_HEURISTIC, specifications.get(1).getHeuristic());
	}*/
	
	/*
	 * There are no evaluated cost anymore.
	@Deprecated
	@Test
	public void testGetCostEvaluated() {
		assertEquals(RULE_HEURISTIC, specifications.get(1).getCost());
	}*/
	
	@Test
	public void testGetCostDefault() {
		assertEquals(JSearchSpecification.DEFAULT_HEURISTIC, specifications.get(0).getCost());
	}
	
	@Test
	public void testSetCost() {
		final int cost = Math.round(Math.round(Math.random()*100));
		specifications.get(1).setCost(cost);
		assertEquals(cost, specifications.get(1).getCost());
	}

	/**
	 * Test method for {@link dinapter.specificator.Specification#setHeuristic(int)}.
	 */
	@Test
	public void testSetHeuristic() {
		final int heuristic = 007;
		specifications.get(0).setHeuristic(heuristic);
		assertEquals(heuristic, specifications.get(0).getHeuristic());
	}

	/**
	 * Test method for {@link dinapter.specificator.Specification#compareTo(java.lang.Object)}.
	 */
	@Test
	public void testCompareTo() {
		specifications.get(0).setHeuristic(0);
		specifications.get(1).setHeuristic(10);
		assertEquals(-10,specifications.get(0).compareTo(specifications.get(1)));
	}

	/**
	 * Test method for {@link dinapter.specificator.Specification#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		assertEquals(specifications.get(1), specifications.get(1));
	}
	
	/**
	 * Test method for {@link dinapter.specificator.Specification#equals(java.lang.Object)}.
	 */
	@Test
	public void testNotSameObject() {
		assertNotSame(specifications.get(0), specifications.get(1));
	}
	
	// @optional Test all events behavior. (Also in JPowerSpecification).
	
	// @optional Implement same test in JPowerSpecification.
	@Test
	public void testChildrenNeededEvent() {
		specifications.get(0).addPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				event = evt;
			}
		});
		specifications.get(0).setChildrenNeeded(true);
		synchronized (this) {
			if (event == null)
				try {
					wait(500);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
		}
		assertNotSame(null, event);	
	}
}
