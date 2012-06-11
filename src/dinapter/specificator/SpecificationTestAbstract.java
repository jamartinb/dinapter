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
package dinapter.specificator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import dinapter.behavior.BehaviorNode;

public abstract class SpecificationTestAbstract<A extends BehaviorNode, R extends Rule<A>, S extends Specification<R>> {
	
	protected S a0, a1, b, c = null;
	protected List<S> specifications = new ArrayList<S>(4);
	protected List<R> rules = null;
	protected A[] path = null; 

	@Before
	public void setUp() throws Exception {
		rules = new ArrayList<R>(3);
		path = createPath();
		List<R> otherRules = new ArrayList<R>(3);
		for (int i = 0; i < 3; i++)
			rules.add(createRule());
		specifications.add(a0 = createSpecification(rules));
		otherRules.add(rules.get(1));
		otherRules.add(rules.get(0));
		otherRules.add(rules.get(2));
		specifications.add(a1 = createSpecification(otherRules));
		otherRules.set(0, rules.get(2));
		otherRules.set(2, rules.get(1));
		specifications.add(b = createSpecification(otherRules));
		specifications.add(c = createSpecification(new ArrayList<R>(0)));
	}
	
	public abstract R createRule();
	public abstract S createSpecification(Collection<R> rules, R workingRule);
	public abstract A [] createPath();
	public abstract Set<A> getActions(S spec);
	
	public S createSpecification(List<R> rules) {
		S specification = createSpecification(new HashSet<R>(rules),rules.isEmpty()?null:rules.get(rules.size()-1));
		return specification;
	}
	
	@Test
	public void testPathNotEmpty() {
		assertNotNull(path);
		assertNotSame(0, path.length);
	}

	@Test
	public void testGetRules() {
		HashSet<R> rules = new HashSet<R>(this.rules);
		assertEquals(rules, a0.getRules());
		assertEquals(rules, a1.getRules());
		assertEquals(a0.getRules(),a1.getRules()); // Really necesary?
		assertEquals(rules, b.getRules());
		assertNotSame(rules, c.getRules());
	}

	@Test
	public void testHashCodeRules() {
		assertEquals(a1.getRules().hashCode(), a0.getRules().hashCode());
		assertNotSame(a0.getRules().hashCode(), b.getRules().hashCode());
		assertNotSame(a0.getRules().hashCode(), c.getRules().hashCode());
		assertNotSame(b.getRules().hashCode(), c.getRules().hashCode());
	}

	@Test
	public void testGetWorkingRule() {
		assertEquals(rules.get(2), a0.getWorkingRule());
		assertEquals(rules.get(2), a1.getWorkingRule());
		assertEquals(rules.get(1), b.getWorkingRule());
		assertEquals(null, c.getWorkingRule());
	}

	@Test
	public void testIsChildrenNeeded() {
		for (S spec:specifications)
			assertFalse(spec.isChildrenNeeded());
	}

	@Test
	public void testSetChildrenNeeded() {
		for (S spec:specifications) {
			boolean value = spec.isChildrenNeeded();
			spec.setChildrenNeeded(!value);
			assertEquals(!value, spec.isChildrenNeeded());
		}
	}

	@Test
	public void testIsChildrenReady() {
		for (S spec:specifications)
			assertFalse(spec.isChildrenReady());
	}

	@Test
	public void testSetChildrenReady() {
		for (S spec:specifications) {
			boolean value = spec.isChildrenReady();
			spec.setChildrenReady(!value);
			assertEquals(!value, spec.isChildrenReady());
		}
	}

	@Test
	public void testGetLeftPath() {
		for (S spec:specifications)
			assertEquals(0,spec.getLeftPath().length);
	}

	@Test
	public void testGetRightPath() {
		for (S spec:specifications)
			assertEquals(0,spec.getRightPath().length);
	}

	@Test
	public void testSetRightPath() {
		for (S spec:specifications) {
			assertNotSame(path, spec.getRightPath());
			spec.setRightPath(path);
			assertEquals(path, spec.getRightPath());
		}
	}

	@Test
	public void testSetLeftPath() {
		for (S spec:specifications) {
			assertNotSame(path, spec.getLeftPath());
			spec.setLeftPath(path);
			assertEquals(path, spec.getLeftPath());
		}		
	}

	@Test
	public void testSetCopiedSpecification() {
		for (S spec:specifications) {
			assertFalse(spec.isCopiedSpecification());
			spec.setCopiedSpecification(true);
			assertTrue(spec.isCopiedSpecification());
		}
	}

	@Test
	public void testGetRulesArray() {
		for (S spec:specifications) {
			Object [] rules = spec.getRulesArray();
			assertEquals(spec.getRules(), new HashSet<Object>(Arrays.asList(rules)));
		}
	}
	
	@Test
	public void testSetWorkingRule() {
		for (S spec:specifications) {
			R workingRule = spec.getWorkingRule();
			assertEquals(workingRule, spec.getWorkingRule()); // Not necesary?
			Set<R> otherRules = new HashSet<R>(spec.getRules());
			otherRules.remove(workingRule);
			for (R rule:otherRules) {
				assertNotSame(rule, spec.getWorkingRule());
				spec.setWorkingRule(rule);
				assertEquals(rule, spec.getWorkingRule());
			}
		}
	}

	@Test
	public void testSetMerged() {
		for (S spec:specifications) {
			boolean value = spec.isMerged();
			spec.setMerged(!value);
			assertEquals(!value, spec.isMerged());
		}
	}

	@Test
	public void testGetActions() {
		for (S spec:specifications)
			assertEquals(getActions(spec), new HashSet<Object>(Arrays.asList(spec.getActions())));
	}
}
