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

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import dinapter.behavior.JPowerBehaviorGraphBuilder;
import dinapter.behavior.JPowerBehaviorNode;
import dinapter.behavior.BehaviorNode.BehaviorNodeType;

/**
 * @author José Antonio Martín Baena
 * @version $Revision: 453 $ - $Date: 2007-02-06 19:08:32 +0100 (mar, 06 feb 2007) $
 */
public class JPowerSpecificatorGraphTest 
	extends SpecificatorGraphTestAbstract
		<JPowerBehaviorNode<Object>
		, DefaultRule<JPowerBehaviorNode<Object>>
		, JPowerSpecification<DefaultRule<JPowerBehaviorNode<Object>>>
		, JPowerSpecificatorGraph<DefaultRule<JPowerBehaviorNode<Object>>, JPowerSpecification<DefaultRule<JPowerBehaviorNode<Object>>>>
		, JPowerSpecificatorGraph.JPowerSpecificatorEdge> {
	
	protected JPowerBehaviorGraphBuilder<Object> behaviorNodeBuilder;
	
	private static int counter = 0;
	
	@Before
	public void setUp() throws Exception {
		behaviorNodeBuilder = new JPowerBehaviorGraphBuilder<Object>();
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see dinapter.specificator.SpecificatorGraphTestAbstract#createGraph()
	 */
	@Override
	protected JPowerSpecificatorGraph<DefaultRule<JPowerBehaviorNode<Object>>, JPowerSpecification<DefaultRule<JPowerBehaviorNode<Object>>>> createGraph() {
		return new JPowerSpecificatorGraph<DefaultRule<JPowerBehaviorNode<Object>>, JPowerSpecification<DefaultRule<JPowerBehaviorNode<Object>>>>();
	}

	/* (non-Javadoc)
	 * @see dinapter.specificator.SpecificatorGraphTestAbstract#createNode(dinapter.behavior.BehaviorNode.BehaviorNodeType)
	 */
	@Override
	protected JPowerBehaviorNode<Object> createNode(BehaviorNodeType type) {
		// Just to force different node creation;
		// @toreview What about syncrhonization?
		return behaviorNodeBuilder.createNode("Node-"+(counter++),type);
	}

	/* (non-Javadoc)
	 * @see dinapter.specificator.SpecificatorGraphTestAbstract#createRule(java.util.List, java.util.List)
	 */
	@Override
	protected DefaultRule<JPowerBehaviorNode<Object>> createRule(List<JPowerBehaviorNode<Object>> leftSide, List<JPowerBehaviorNode<Object>> rightSide) {
		return new DefaultRule<JPowerBehaviorNode<Object>>(leftSide, rightSide);
	}

	/* (non-Javadoc)
	 * @see dinapter.specificator.SpecificatorGraphTestAbstract#createSpecification(java.util.List)
	 */
	@Override
	protected JPowerSpecification<DefaultRule<JPowerBehaviorNode<Object>>> createSpecification(List<DefaultRule<JPowerBehaviorNode<Object>>> rules) {
        JPowerSpecification<DefaultRule<JPowerBehaviorNode<Object>>> specification =
            new JPowerSpecification<DefaultRule<JPowerBehaviorNode<Object>>>(rules);
        specification.setCostReady(true);
        return specification;
	}

	/* (non-Javadoc)
	 * @see dinapter.specificator.SpecificatorGraphTestAbstract#getStartNode(dinapter.specificator.SpecificatorGraph)
	 */
	@Override
	protected JPowerSpecification<DefaultRule<JPowerBehaviorNode<Object>>> getStartNode(JPowerSpecificatorGraph<DefaultRule<JPowerBehaviorNode<Object>>, JPowerSpecification<DefaultRule<JPowerBehaviorNode<Object>>>> graph) {
		return graph.getStartNode();
	}

	/* (non-Javadoc)
	 * @see dinapter.specificator.SpecificatorGraphTestAbstract#runTest()
	 */
	@Override
	@Test
	public void runTest() {
		assertTrue(true);
	}

	/* (non-Javadoc)
	 * @see dinapter.specificator.SpecificatorGraphTestAbstract#setHeuristic(dinapter.specificator.Rule, int)
	 */
	@Override
	protected DefaultRule<JPowerBehaviorNode<Object>> setHeuristic(DefaultRule<JPowerBehaviorNode<Object>> rule, int heuristic) {
		rule.setHeuristic(heuristic);
		return rule;
	}

	/* (non-Javadoc)
	 * @see dinapter.specificator.SpecificatorGraphTestAbstract#setHeuristic(dinapter.specificator.Specification, int)
	 */
	@Override
	protected JPowerSpecification<DefaultRule<JPowerBehaviorNode<Object>>> setHeuristic(JPowerSpecification<DefaultRule<JPowerBehaviorNode<Object>>> specification, int heuristic) {
		specification.setHeuristic(heuristic);
		return specification;
	}

	/* (non-Javadoc)
	 * @see dinapter.specificator.SpecificatorGraphTestAbstract#setStartNode(dinapter.specificator.SpecificatorGraph, dinapter.specificator.Specification)
	 */
	@Override
	protected JPowerSpecificatorGraph<DefaultRule<JPowerBehaviorNode<Object>>, JPowerSpecification<DefaultRule<JPowerBehaviorNode<Object>>>> setStartNode(JPowerSpecificatorGraph<DefaultRule<JPowerBehaviorNode<Object>>, JPowerSpecification<DefaultRule<JPowerBehaviorNode<Object>>>> graph, JPowerSpecification<DefaultRule<JPowerBehaviorNode<Object>>> node) {
		graph.setStartNode(node);
		return graph;
	}
}
