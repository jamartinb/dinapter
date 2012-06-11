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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dinapter.behavior.JPowerBehaviorGraphBuilder;
import dinapter.behavior.JPowerBehaviorNode;
import dinapter.behavior.BehaviorNode.BehaviorNodeType;

public class SimpleSpecificationTest
		extends JSearchSpecificationTestAbstract
					<JPowerBehaviorNode<Object>
					,DefaultRule<JPowerBehaviorNode<Object>>
					,SimpleSpecification<DefaultRule<JPowerBehaviorNode<Object>>>> {
	
	protected DefaultRuleBuilder<JPowerBehaviorNode<Object>> ruleBuilder = null;
	protected JPowerBehaviorGraphBuilder<Object> behaviorBuilder = null;
	
	@Before
	@Override
	public void setUp() throws Exception {
		ruleBuilder = new DefaultRuleBuilder<JPowerBehaviorNode<Object>>();
		behaviorBuilder = new JPowerBehaviorGraphBuilder<Object>();
		super.setUp();
	}

	@Override
	public int getDefaultCost() {
		return SimpleSpecification.DEFAULT_COST;
	}

	@Override
	public int getDefaultHeuristic() {
		return SimpleSpecification.DEFAULT_HEURISTIC;
	}

	@Override
	public int getNoCost() {
		return SimpleSpecification.NO_COST;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JPowerBehaviorNode<Object>[] createPath() {
		JPowerBehaviorNode<Object> [] path = new JPowerBehaviorNode[3];
		path[0]=behaviorBuilder.createNode(BehaviorNodeType.SEND);
		path[1]=behaviorBuilder.createNode(BehaviorNodeType.RECEIVE);
		path[2]=behaviorBuilder.createNode(BehaviorNodeType.SEND);
		return path;
	}

	@SuppressWarnings("unchecked")
	@Override
	public DefaultRule<JPowerBehaviorNode<Object>> createRule() {
		ruleBuilder.createRule();
		ruleBuilder.setLeftSide(behaviorBuilder.createNode(BehaviorNodeType.SEND));
		ruleBuilder.setRightSide(behaviorBuilder.createNode(BehaviorNodeType.RECEIVE));
		return ruleBuilder.getRule();
	}

	@Override
	public SimpleSpecification<DefaultRule<JPowerBehaviorNode<Object>>> createSpecification(Collection<DefaultRule<JPowerBehaviorNode<Object>>> rules, DefaultRule<JPowerBehaviorNode<Object>> workingRule) {
		return new SimpleSpecification<DefaultRule<JPowerBehaviorNode<Object>>>(rules,workingRule);
	}

	@Override
	public Set<JPowerBehaviorNode<Object>> getActions(SimpleSpecification<DefaultRule<JPowerBehaviorNode<Object>>> spec) {
		HashSet<JPowerBehaviorNode<Object>> set = new HashSet<JPowerBehaviorNode<Object>>();
		for (DefaultRule<JPowerBehaviorNode<Object>> rule:spec.getRules()) {
			set.addAll(rule.getLeftSide());
			set.addAll(rule.getRightSide());
		}
		return set;
	}
	
	@Test
	public void testNothing() {
		Assert.assertTrue(true);
	}
}
