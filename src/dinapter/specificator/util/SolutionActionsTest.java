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
package dinapter.specificator.util;

import static dinapter.behavior.BehaviorNode.BehaviorNodeType.EXIT;
import static dinapter.behavior.BehaviorNode.BehaviorNodeType.PICK;
import static dinapter.behavior.BehaviorNode.BehaviorNodeType.RECEIVE;
import static dinapter.behavior.BehaviorNode.BehaviorNodeType.SEND;
import static dinapter.behavior.BehaviorNode.BehaviorNodeType.START;
import static dinapter.behavior.BehaviorNode.BehaviorNodeType.IF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;
import net.sourceforge.jpowergraph.Edge;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import data.FTPExample;
import dinapter.behavior.JPowerBehaviorGraph;
import dinapter.behavior.JPowerBehaviorGraphBuilder;
import dinapter.behavior.JPowerBehaviorNode;
import dinapter.behavior.BehaviorNode.BehaviorNodeType;

public class SolutionActionsTest {
	
	protected SolutionActions<JPowerBehaviorNode<Object>, JPowerBehaviorGraph<Object,JPowerBehaviorNode<Object>,Edge>> 
		solutionActions = null;
	
	protected JPowerBehaviorGraph<Object, JPowerBehaviorNode<Object>, Edge>
			client = null
			, server = null
			, serverSmall = null
			, serverTiny = null;
	
	protected JPowerBehaviorGraphBuilder<Object> behaviorBuilder = null;
	
	protected ArrayList<JPowerBehaviorNode<Object>> actions = null; 

	@Before
	public void setUp() throws Exception {
		solutionActions = new SolutionActions<JPowerBehaviorNode<Object>, JPowerBehaviorGraph<Object,JPowerBehaviorNode<Object>,Edge>>();
		FTPExample provider = new FTPExample();
		client = provider.getFtpClient();
		server = provider.getFtpServer();
		serverSmall = provider.getSimpleFtpServer();
		serverTiny = provider.getVerySimpleFtpServer();
		behaviorBuilder = new JPowerBehaviorGraphBuilder<Object>();
		actions = new ArrayList<JPowerBehaviorNode<Object>>(4);
		actions.add(behaviorBuilder.createNode("queso", BehaviorNodeType.SEND));
		actions.add(behaviorBuilder.createNode("jamón", BehaviorNodeType.RECEIVE));
		actions.add(behaviorBuilder.createNode("sol", BehaviorNodeType.SEND));
		actions.add(behaviorBuilder.createNode("playa", BehaviorNodeType.RECEIVE));
	}

	@After
	public void tearDown() throws Exception {
		solutionActions = null;
		client = null;
		serverSmall = null;
		serverTiny = null;
		server = null;
		behaviorBuilder = null;
		actions = null;
		System.gc();
	}

	@Test
	public void testGetRequiredActionsBG1() {
		Set<JPowerBehaviorNode<Object>> actionsNeeded = new HashSet<JPowerBehaviorNode<Object>>();
		JPowerBehaviorNode<Object> action = client.getStartNode();
		while (action != client.getEndNode()) {
			if ((action.getType() == BehaviorNodeType.SEND)
					|| (action.getType() == BehaviorNodeType.RECEIVE)) {
				actionsNeeded.add(action);
			}
			action = client.getChildren(action).iterator().next();
		}
		Set<Set<JPowerBehaviorNode<Object>>> expected = new HashSet<Set<JPowerBehaviorNode<Object>>>();
		expected.add(actionsNeeded);
		Assert.assertEquals(expected, solutionActions.getMinimalRequiredActions(client));
	}
	
	private Set<Set<JPowerBehaviorNode<Object>>> getSolutionsBG2() {
		Set<JPowerBehaviorNode<Object>> solutionA, solutionB;
		solutionA = new HashSet<JPowerBehaviorNode<Object>>();
		solutionB = new HashSet<JPowerBehaviorNode<Object>>();
		for (JPowerBehaviorNode<Object> action:server.getAllNodes()) {
			if (action.getDescription() == null) {
				continue;
			}
			solutionB.add(action);
			if (action.getDescription().equals("connected")) {
				solutionA.add(action);
			} else if (action.getDescription().equals("user")) {
				solutionA.add(action);
			} else if (action.getDescription().equals("rejected")) {
				solutionA.add(action);
			} else if (action.getDescription().equals("quit")) {
				solutionA.add(action);
			}
		}
		Set<Set<JPowerBehaviorNode<Object>>> expected = new HashSet<Set<JPowerBehaviorNode<Object>>>();
		expected.add(solutionA);
		expected.add(solutionB);
		return expected;
	}

	@Test
	public void testGetRequiredActionsBG2() {
		Assert.assertEquals(
				getSolutionsBG2()
				, solutionActions.getMinimalRequiredActions(server));
	}
	
	@Test
	public void testGetPossibleRequiredActionsBG2() {
		Set<Set<JPowerBehaviorNode<Object>>> solutions = getSolutionsBG2();
		Set<JPowerBehaviorNode<Object>> otherSolution = new HashSet<JPowerBehaviorNode<Object>>();
		for (Set<JPowerBehaviorNode<Object>> solution:solutions) {
			otherSolution.addAll(solution);
		}
		solutions.add(otherSolution);
		Assert.assertEquals(solutions, solutionActions.getPossibleRequiredActions(server));
	}
	
	@Test
	public void testGetRequiredActionsBG3() {
		Set<JPowerBehaviorNode<Object>> solutionA, solutionB;
		solutionA = new HashSet<JPowerBehaviorNode<Object>>();
		solutionB = new HashSet<JPowerBehaviorNode<Object>>();
		for (JPowerBehaviorNode<Object> action:serverSmall.getAllNodes()) {
			if (action.getDescription() == null) {
				continue;
			}
			solutionB.add(action);
			if (action.getDescription().equals("connected")) {
				solutionA.add(action);
			} else if (action.getDescription().equals("user")) {
				solutionA.add(action);
			} else if (action.getDescription().equals("quit")) {
				solutionA.add(action);
			}
		}
		Set<Set<JPowerBehaviorNode<Object>>> expected = new HashSet<Set<JPowerBehaviorNode<Object>>>();
		expected.add(solutionA);
		expected.add(solutionB);
		
		Assert.assertEquals(expected, solutionActions.getMinimalRequiredActions(serverSmall));		
	}
	
	@Test
	public void testGetRequiredActionsBG4() {
		Set<JPowerBehaviorNode<Object>> solutionA, solutionB;
		solutionA = new HashSet<JPowerBehaviorNode<Object>>();
		solutionB = new HashSet<JPowerBehaviorNode<Object>>();
		for (JPowerBehaviorNode<Object> action:serverTiny.getAllNodes()) {
			if (action.getDescription() == null) {
				continue;
			}
			solutionB.add(action);
			if (!action.getDescription().equals("getFile") && !action.getDescription().equals("result")) {
				solutionA.add(action);
			}
		}
		Set<Set<JPowerBehaviorNode<Object>>> expected = new HashSet<Set<JPowerBehaviorNode<Object>>>();
		expected.add(solutionA);
		expected.add(solutionB);
		
		Assert.assertEquals(expected, solutionActions.getMinimalRequiredActions(serverTiny));		
	}
	
	@Test
	public void testGetRequiredActionsBG5() {
		Set<JPowerBehaviorNode<Object>> solutionA, solutionB; //, solutionC;
		solutionA = new HashSet<JPowerBehaviorNode<Object>>();
		solutionB = new HashSet<JPowerBehaviorNode<Object>>();
		//solutionC = new HashSet<JPowerBehaviorNode<Object>>();
		JPowerBehaviorGraph<Object, JPowerBehaviorNode<Object>, Edge> component = createBG5();
		for (JPowerBehaviorNode<Object> action:component.getAllNodes()) {
			if (action.getDescription() == null) {
				continue;
			}
			//solutionC.add(action);
			if (!action.getDescription().toString().contains("x")) {
				solutionA.add(action);
			}
			if (!action.getDescription().toString().contains("y")) {
				solutionB.add(action);
			}
		}
		Set<Set<JPowerBehaviorNode<Object>>> expected = new HashSet<Set<JPowerBehaviorNode<Object>>>();
		expected.add(solutionA);
		expected.add(solutionB);
		//expected.add(solutionC);
		
		Assert.assertEquals(expected, solutionActions.getMinimalRequiredActions(component));		
	}

	/*
	@Test
	public void testGetRequiredActionsBGLinkedListOfBB() {
		fail("Not yet implemented"); // @todo
	}

	@Test
	public void testGetRequiredActionsBGLinkedListOfB() {
		fail("Not yet implemented"); // @todo
	}
	*/
	
	@Test
	public void testCartesianProductListOfSetOfSetOfT() {
		/*
		 * queso(0)
		 * jamón(1) sol(2) playa(3)
		 * -----
		 * playa(3)
		 * jamón(1) queso(0)
		 * -----
		 * sol(2)
		 * =====
		 * queso(0) playa(3) sol(2)
		 * queso(0) jamón(1) sol(2)
		 * jamón(1) sol(2) playa(3)
		 * jamón(1) sol(2) playa(3) queso(0)
		 */
		int [][] setsConfiguration = {
				{0},
				{1,2,3},
				{3},
				{1,0},
				{2},
				{0,3,2},
				{0,1,2},
				{1,2,3},
				{1,2,3,0}
		};
		
		Set<Set<JPowerBehaviorNode<Object>>> a,b,c,expected;
		a = new HashSet<Set<JPowerBehaviorNode<Object>>>();
		b = new HashSet<Set<JPowerBehaviorNode<Object>>>();
		c = new HashSet<Set<JPowerBehaviorNode<Object>>>();
		expected = new HashSet<Set<JPowerBehaviorNode<Object>>>();
		for (int i = 0; i < setsConfiguration.length; i++) {
			HashSet<JPowerBehaviorNode<Object>> set = new HashSet<JPowerBehaviorNode<Object>>();
			for (int j:setsConfiguration[i]) {
				set.add(actions.get(j));
			}
			if (i < 2) {
				a.add(set);
			} else if (i < 4) {
				b.add(set);
			} else if (i < 5) {
				c.add(set);
			} else {
				expected.add(set);
			}
		}
		LinkedList<Set<Set<JPowerBehaviorNode<Object>>>> products = new LinkedList<Set<Set<JPowerBehaviorNode<Object>>>>();
		products.add(a);
		products.add(b);
		products.add(c);
		
		Assert.assertEquals(expected, SolutionActions.cartesianProduct(products));
	}

	@Test
	public void testCartesianProductSetOfSetOfTSetOfSetOfT() {
		/*
		JPowerBehaviorNode [] set1A = {queso};
		JPowerBehaviorNode [] set1B = {jamón, sol, playa};
		JPowerBehaviorNode [] set2C = {playa};
		JPowerBehaviorNode [] set2D = {jamón,queso};
		JPowerBehaviorNode [] solA = {queso,playa};
		JPowerBehaviorNode [] solB = {queso,jamón};
		JPowerBehaviorNode [] solC = {jamón,sol,playa};
		JPowerBehaviorNode [] solD = {jamón,sol,playa,queso};
		*/
		int [][] setsConfiguration = {
				{0},
				{1,2,3},
				{3},
				{1,0},
				{0,3},
				{0,1},
				{1,2,3},
				{1,2,3,0}
		};
		
		Set<Set<JPowerBehaviorNode<Object>>> a,b,expected;
		a = new HashSet<Set<JPowerBehaviorNode<Object>>>();
		b = new HashSet<Set<JPowerBehaviorNode<Object>>>();
		expected = new HashSet<Set<JPowerBehaviorNode<Object>>>();
		for (int i = 0; i < setsConfiguration.length; i++) {
			HashSet<JPowerBehaviorNode<Object>> set = new HashSet<JPowerBehaviorNode<Object>>();
			for (int j:setsConfiguration[i]) {
				set.add(actions.get(j));
			}
			if (i < 2) {
				a.add(set);
			} else if (i < 4) {
				b.add(set);
			} else {
				expected.add(set);
			}
		}
		
		Assert.assertEquals(expected, SolutionActions.cartesianProduct(a, b));
	}
	
	@Test
	public void testGetAllPossibleJoins() {
		/*
		JPowerBehaviorNode [] set1A = {queso [0]};
		JPowerBehaviorNode [] set1B = {jamón [1], sol [2], playa [3]};
		JPowerBehaviorNode [] set2C = {playa};
		JPowerBehaviorNode [] set2D = {jamón,queso};
		JPowerBehaviorNode [] solA = {queso,jamón,sol,playa};
		JPowerBehaviorNode [] solB = {queso,playa};
		JPowerBehaviorNode [] solC = {jamón,queso};
		JPowerBehaviorNode [] solD = {jamón,sol,playa};
		JPowerBehaviorNode [] solE = {playa,jamón,queso};
		JPowerBehaviorNode [] sol1A = {queso [0]};
		JPowerBehaviorNode [] sol2C = {playa};
		*/
		int [][] setsConfiguration = {
				{0},
				{1,2,3},
				{3},
				{1,0},
				{0,1,2,3},
				{0,3},
				{1,0},
				{1,2,3},
				{3,1,0},
				{0},
				{3}
		};
		
		Set<Set<JPowerBehaviorNode<Object>>> a,b,expected;
		a = new HashSet<Set<JPowerBehaviorNode<Object>>>();
		b = new HashSet<Set<JPowerBehaviorNode<Object>>>();
		expected = new HashSet<Set<JPowerBehaviorNode<Object>>>();
		for (int i = 0; i < setsConfiguration.length; i++) {
			HashSet<JPowerBehaviorNode<Object>> set = new HashSet<JPowerBehaviorNode<Object>>();
			for (int j:setsConfiguration[i]) {
				set.add(actions.get(j));
			}
			if (i < 2) {
				a.add(set);
			} else if (i < 4) {
				b.add(set);
			} else {
				expected.add(set);
			}
		}
		a.addAll(b);
		List<Set<JPowerBehaviorNode<Object>>> sets = new LinkedList<Set<JPowerBehaviorNode<Object>>>(a);
		
		Assert.assertEquals(expected, SolutionActions.getAllPossibleJoins(sets));
	}
	
	@Test
	public void testCartesianProductSingleSet() {
		/*
		JPowerBehaviorNode [] set1 = {jamón, sol, playa};
		JPowerBehaviorNode [] set2C = {playa};
		JPowerBehaviorNode [] set2D = {jamón,queso};
		JPowerBehaviorNode [] solC = {jamón,sol,playa};
		JPowerBehaviorNode [] solD = {jamón,sol,playa,queso};
		*/
		int [][] setsConfiguration = {
				{1,2,3},
				{3},
				{1,0},
				{1,2,3},
				{1,2,3,0}
		};
		
		Set<Set<JPowerBehaviorNode<Object>>> a,b,expected;
		a = new HashSet<Set<JPowerBehaviorNode<Object>>>();
		b = new HashSet<Set<JPowerBehaviorNode<Object>>>();
		expected = new HashSet<Set<JPowerBehaviorNode<Object>>>();
		for (int i = 0; i < setsConfiguration.length; i++) {
			HashSet<JPowerBehaviorNode<Object>> set = new HashSet<JPowerBehaviorNode<Object>>();
			for (int j:setsConfiguration[i]) {
				set.add(actions.get(j));
			}
			if (i < 1) {
				a.add(set);
			} else if (i < 3) {
				b.add(set);
			} else {
				expected.add(set);
			}
		}
		
		Assert.assertEquals(expected, SolutionActions.cartesianProduct(a, b));
	}
	
	@Test
	public void testCartesianProductSingleAction() {
		/*
		JPowerBehaviorNode [] set1 = {queso};
		JPowerBehaviorNode [] set2C = {playa};
		JPowerBehaviorNode [] set2D = {jamón,queso};
		JPowerBehaviorNode [] solA = {queso,playa};
		JPowerBehaviorNode [] solB = {queso,jamón};
		*/
		int [][] setsConfiguration = {
				{0},
				{3},
				{1,0},
				{0,3},
				{0,1},
		};
		
		Set<Set<JPowerBehaviorNode<Object>>> a,b,expected;
		a = new HashSet<Set<JPowerBehaviorNode<Object>>>();
		b = new HashSet<Set<JPowerBehaviorNode<Object>>>();
		expected = new HashSet<Set<JPowerBehaviorNode<Object>>>();
		for (int i = 0; i < setsConfiguration.length; i++) {
			HashSet<JPowerBehaviorNode<Object>> set = new HashSet<JPowerBehaviorNode<Object>>();
			for (int j:setsConfiguration[i]) {
				set.add(actions.get(j));
			}
			if (i < 1) {
				a.add(set);
			} else if (i < 3) {
				b.add(set);
			} else {
				expected.add(set);
			}
		}
		
		Assert.assertEquals(expected, SolutionActions.cartesianProduct(a, b));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testRemoveSuperSets() {
		Object queso = "queso";
		Object jamón = "jamón";
		Object sol = "sol";
		Object playa = "playa";
		Object [] setA = {queso, sol, jamón};
		Object [] setB = {queso,jamón,sol,playa};
		Object [] setC = {sol, jamón};
		Object [] setD = {sol};
		Set<Set<Object>> expected = new HashSet<Set<Object>>();
		expected.add(new HashSet<Object>(Arrays.asList(setD)));
		Set<Set<Object>> sets = new HashSet<Set<Object>>();
		sets.add(new HashSet<Object>(Arrays.asList(setA)));
		sets.add(new HashSet<Object>(Arrays.asList(setB)));
		sets.add(new HashSet<Object>(Arrays.asList(setC)));
		sets.add((Set<Object>)expected.iterator().next());
		Assert.assertEquals(expected, SolutionActions.removeSuperSets(sets));
	}
	
	private JPowerBehaviorGraph<Object, JPowerBehaviorNode<Object>, Edge> createBG5() {
		JPowerBehaviorNode<Object> a,b,c,f;
		behaviorBuilder.createNewGraph();
		a = behaviorBuilder.createNode(START);
		b = behaviorBuilder.createNode("a", SEND);
		behaviorBuilder.link(a, b);
		a = behaviorBuilder.createNode(IF);
		behaviorBuilder.link(b, a);
		b = behaviorBuilder.createNode("a2",SEND);
		c = behaviorBuilder.createNode("a1",SEND);
		behaviorBuilder.link(a, b);
		behaviorBuilder.link(a, c);
		f = behaviorBuilder.createNode(EXIT);
		behaviorBuilder.link(b, f);
		b = behaviorBuilder.createNode(PICK);
		behaviorBuilder.link(c, b);
		a = behaviorBuilder.createNode("a1y", RECEIVE);
		c = behaviorBuilder.createNode("a1x", RECEIVE);
		behaviorBuilder.link(b, a);
		behaviorBuilder.link(b, c);
		behaviorBuilder.link(a, f);
		a = behaviorBuilder.createNode(IF);
		behaviorBuilder.link(c, a);
		b = behaviorBuilder.createNode("a1x@", SEND);
		c = behaviorBuilder.createNode("a1x#", SEND);
		behaviorBuilder.link(a, b);
		behaviorBuilder.link(a, c);
		behaviorBuilder.link(b, f);
		behaviorBuilder.link(c, f);
		return behaviorBuilder.getGraph();
	}
	
	public static final void main(String [] args) throws Exception {
		SolutionActionsTest test = new SolutionActionsTest();
		test.setUp();
		/*JFrame frame = new JFrame("Test BG5");
		frame.getContentPane().add(test.createBG5().getGraphView());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(800,600));
		frame.pack();
		frame.setVisible(true);*/
		JPowerBehaviorGraph<Object, JPowerBehaviorNode<Object>, Edge> behavior = test.createBG5();
		test.solutionActions.getMinimalRequiredActions(behavior);
	}

}
