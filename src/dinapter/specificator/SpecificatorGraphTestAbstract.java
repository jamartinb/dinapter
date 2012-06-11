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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import jsearchdemo.Graph;
import jsearchdemo.WeighedEdge;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dinapter.behavior.BehaviorNode;
import dinapter.behavior.BehaviorNode.BehaviorNodeType;

/**
 * @author arkangel
 *
 */
public abstract class SpecificatorGraphTestAbstract
	<N extends BehaviorNode
	, R extends Rule<N>
	// S must extends HeuristicNode and Node altogether because of the weird design of jsearchdemo
	, S extends JSearchSpecification<R>
	, G extends SpecificatorGraph<S> & Graph
	, E extends WeighedEdge> {
	
	protected static final int RULE_HEURISTIC = 999;
	protected G graph;
	protected List<S> specs; 

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		graph =  createGraph();
		specs = new LinkedList<S>();
		List<N> empty = java.util.Collections.unmodifiableList(new ArrayList<N>(0));
		for (int i = 0; i < 3; i++) {
			N node;
			node = createNode
				((i%2 ==0)?BehaviorNodeType.SEND:BehaviorNodeType.RECEIVE);
			List<N> nodes = new ArrayList<N>(1);
			nodes.add(node);
			List<R> rules = new ArrayList<R>(1);
			rules.add(createRule(nodes,empty));
			rules.set(0,setHeuristic(rules.get(0),RULE_HEURISTIC));
			S specification = createSpecification(rules);
			specification.setChildrenReady(true);
			specification.setCostReady(true);
			if (i == 2)
				specification = setHeuristic(specification,3);
			specs.add(specification);
			if (i > 0)
				graph.addEdge(specs.get(0), specs.get(i));
		}
		specs = java.util.Collections.unmodifiableList(specs);
	}
	
	protected abstract G createGraph();
	protected abstract S createSpecification(List<R> rules);
	protected abstract R createRule(List<N> leftSide, List<N> rightSide);
	protected abstract N createNode(BehaviorNodeType type);
	protected abstract R setHeuristic(R rule, int heuristic);
	protected abstract S setHeuristic(S specification, int heuristic);
	protected abstract G setStartNode(G graph, S node);
	protected abstract S getStartNode(G graph);
	public abstract void runTest();

	/**
	 * Test method for {@link dinapter.specificator.DefaultSpecificatorGraph#getChildren(jsearchdemo.Node)}.
	 */
	@Test
	public void testGetChildrenNodeAll() {
		assertEquals(new HashSet<S>(specs.subList(1, 3))
				    ,new HashSet<S>(graph.getChildren(specs.get(0))));
	}
	
	/**
	 * Test method for {@link dinapter.specificator.DefaultSpecificatorGraph#getChildren(jsearchdemo.Node)}.
	 */
	@Test
	public void testGetChildrenNodeNone() {
		assertEquals(0, graph.getChildren(specs.get(1)).size(),0);
	}

	/**
	 * Test method for {@link dinapter.specificator.DefaultSpecificatorGraph#getGoalNode()}.
	 * Delegated in testGetEndNode
	@Test
	public void testGetGoalNode() {
		// @to-do Test DefaultSpecificatorGraph#getGoalNode
	}*/

	/**
	 * Test method for {@link dinapter.specificator.DefaultSpecificatorGraph#getOutgoingEdges(jsearchdemo.Node)}.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testGetOutgoingEdgesNodeA() {
		assertEquals(2,graph.getOutgoingEdges(specs.get(0)).size());
		boolean foundA = false;
		boolean foundB = false;
		for (E edge:(List<E>)graph.getOutgoingEdges(specs.get(0))) {
			if (edge.getEndNode().equals(specs.get(1)))
				foundA = true;
			else if (edge.getEndNode().equals(specs.get(2)))
				foundB = true;
			else
				Assert.fail("Found an edge pointing to nowhere known!");
		}
		assertTrue("Not found first specification.",foundA);
		assertTrue("Not found second specification.",foundB);
	}
	
	/**
	 * Test method for {@link dinapter.specificator.DefaultSpecificatorGraph#getOutgoingEdges(jsearchdemo.Node)}.
	 */
	@Test
	public void testGetOutgoingEdgesNodePersistent() {
		assertEquals(graph.getOutgoingEdges(specs.get(0)), graph.getOutgoingEdges(specs.get(0)));
	}
	
	/**
	 * Test method for {@link dinapter.specificator.DefaultSpecificatorGraph#getOutgoingEdges(jsearchdemo.Node)}.
	 */
	@Test
	public void testGetOutgoingEdgesNodeB() {
		assertTrue(graph.getOutgoingEdges(specs.get(1)).isEmpty());
	}
	
	/**
	 * Test method for {@link dinapter.specificator.DefaultSpecificatorGraph#getOutgoingEdges(jsearchdemo.Node)}.
	 */
	@Test
	public void testGetOutgoingEdgesNodeC() {
		assertTrue(graph.getOutgoingEdges(specs.get(2)).isEmpty());
	}
	
	/**
	 * Test method for {@link dinapter.specificator.DefaultSpecificatorGraph#getOutgoingEdges(jsearchdemo.Node)}.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testGetOutgoingEdgesNodeWeight() {
		E edge = (E)graph.getOutgoingEdges(specs.get(0)).get(0);
		assertEquals(((JSearchSpecification)edge.getEndNode()).getCost(),edge.getWeight());
	}

	/**
	 * Test method for {@link dinapter.specificator.DefaultSpecificatorGraph#getEndNode()}.
	 *-/
	@Test
	public void testGetEndNode() {
		fail("Not yet implemented");
	}
	/* @todo It depends on the method implementation */

	/**
	 * Test method for {@link dinapter.specificator.DefaultSpecificatorGraph#getStartNode()}.
	 */
	@Test
	public void testGetStartNode() {
		assertEquals(specs.get(0),getStartNode(graph));
	}

	/**
	 * Test method for {@link dinapter.specificator.DefaultSpecificatorGraph#setStartNode(dinapter.specificator.DefaultRule)}.
	 */
	@Test
	public void testSetStartNode() {
		graph = setStartNode(graph,specs.get(1));
		assertEquals(specs.get(1), getStartNode(graph));
	}

	/**
	 * Test method for {@link dinapter.specificator.DefaultSpecificatorGraph#removeEdge(dinapter.specificator.DefaultRule, dinapter.specificator.DefaultRule)}.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testRemoveEdgeRR() {
		graph.removeEdge(specs.get(0), specs.get(2));
		assertEquals(1, graph.getOutgoingEdges(specs.get(0)).size());
		assertEquals(specs.get(1), ((E)graph.getOutgoingEdges(specs.get(0)).get(0)).getEndNode());
	}

	/**
	 * Test method for {@link dinapter.specificator.DefaultSpecificatorGraph#removeNode(dinapter.specificator.DefaultRule)}.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testRemoveNodeR() {
		/*for (S node:graph.getAllNodes())
			System.out.println(node+" ("+(node!=null?node.hashCode():"")+")");*/
		graph.removeNode(specs.get(2));
		assertEquals(1, graph.getOutgoingEdges(specs.get(0)).size());
		assertEquals(specs.get(1), ((E)graph.getOutgoingEdges(specs.get(0)).get(0)).getEndNode());
	}

	/**
	 * Test method for {@link dinapter.specificator.DefaultSpecificatorGraph#addEdge(dinapter.specificator.DefaultRule, dinapter.specificator.DefaultRule)}.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testAddEdgeRR() {
		graph.addEdge(specs.get(2), specs.get(1));
		assertEquals(1, graph.getOutgoingEdges(specs.get(2)).size());
		assertEquals(specs.get(1), ((E)graph.getOutgoingEdges(specs.get(2)).get(0)).getEndNode());
	}

}
