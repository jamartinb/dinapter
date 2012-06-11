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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JFrame;

import org.junit.Before;
import org.junit.Test;

import dinapter.behavior.JPowerBehaviorNode;

import static org.junit.Assert.*;
import static dinapter.behavior.BehaviorNode.BehaviorNodeType.*;

public class SpecificatorGUITest {
	
	protected JPowerSpecificatorBuilder<Object> builder;
	protected Map<String,JPowerBehaviorNode<Object>> actions;
	
	public SpecificatorGUITest() {}
	
	@Before
	public void setUp() throws Exception {
		builder = new JPowerSpecificatorBuilder<Object>();
		builder.setWrappingEnabled(true);
		actions = new HashMap<String, JPowerBehaviorNode<Object>>(11);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpecificatorGUITest test = new SpecificatorGUITest();
		try {
			test.setUp();
		} catch (Exception e) {
			throw new RuntimeException("Exception thrown inside setUp method",e);
		}
		test.jPowerSpecificatorTest();
	}
	
	@Test
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void jPowerSpecificatorTest() {
		put("login",builder.createAction("login", SEND, "username","password"));
		put("download", builder.createAction("download",SEND, "filename"));
		put("data", builder.createAction("data",RECEIVE, "fileData"));
		put("user", builder.createAction("user", RECEIVE, "username"));
		put("pass", builder.createAction("pass",RECEIVE, "password"));
		put("getFile", builder.createAction("getFile", RECEIVE, "filename"));
		put("result", builder.createAction("result", SEND, "fileData"));
		put("noSuchFile", builder.createAction("noSuchFile", SEND));
		put("quit", builder.createAction("quit",RECEIVE));
		put("rejected", builder.createAction("rejected",SEND));
		put("connected", builder.createAction("connected", SEND));
		JPowerSpecification<DefaultRule<JPowerBehaviorNode<Object>>> a, b;
		LinkedList<DefaultRule<JPowerBehaviorNode<Object>>> rules 
			= new LinkedList<DefaultRule<JPowerBehaviorNode<Object>>>();
		builder.setLeftSide(get("login"));
		rules.add(builder.getRule());
		a = builder.createSpecification();
		b = builder.createSpecification(rules);
		builder.link(a, b);
		builder.setRightSide(get("user"));
		rules.add(builder.getRule());
		a = builder.createSpecification(rules.getLast());
		builder.link(b, a);
		builder.setRightSide(get("user"),get("pass"));
		rules.add(builder.getRule());
		b = builder.createSpecification(builder.getRule());
		builder.link(a,b);
		rules.clear();
		rules.add(builder.getRule());
		builder.createRule();
		builder.setRightSide(get("connected"));
		rules.add(builder.getRule());
		a = builder.createSpecification(rules);
		builder.link(b,a);
		builder.setRightSide(get("connected"),get("getFile"));
		rules.removeLast();
		rules.add(builder.getRule());
		b = builder.createSpecification(rules);
		builder.link(a,b);
		// After pushXXX methods...
		builder.pushLeft(get("download"));
		builder.closeRule();
		builder.pushRight(get("result"));
		builder.pushLeft(get("data"));
		builder.closeRule();
		builder.pushRight(get("quit"));
		builder.closeRule();
		builder.pushRight(get("rejected"));
		builder.pushLeft(get("download"));
		builder.pushLeft(get("data"));
		builder.pushRight(get("quit"));
		builder.closeRule();
		builder.pushRight(get("noSuchFile"));
		builder.pushLeft(get("data"));
		builder.pushLeft(get("quit"));
		builder.closeRule();
		// End of real specification process
		
		// Testing setWorkingSpecification
		builder.setWorkingSpecification(builder.getGraph().getStartNode());
		builder.pushRight(get("quit"));
		builder.pushLeft(get("login"));
		builder.setWorkingSpecification(builder.getGraph().getStartNode());
		builder.pushRight(get("rejected"));
		// End
		
		JFrame frame = new JFrame();
		frame.getContentPane().add(builder.getGraphView());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 500);
		frame.pack();
		frame.setVisible(true);
		assertEquals(3, builder.getGraph().getOutgoingEdges(builder.getGraph().getStartNode()).size());
	}

	/**
	 * @param key
	 * @return
	 * @see java.util.Map#get(java.lang.Object)
	 */
	private JPowerBehaviorNode<Object> get(String key) {
		return actions.get(key);
	}

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	private JPowerBehaviorNode<Object> put(String key, JPowerBehaviorNode<Object> value) {
		return actions.put(key, value);
	}
}
