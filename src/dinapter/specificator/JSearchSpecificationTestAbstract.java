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

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

import dinapter.behavior.BehaviorNode;

public abstract class JSearchSpecificationTestAbstract<B extends BehaviorNode , R extends Rule<B> , S extends Specification<R>> extends SpecificationTestAbstract<B,R,S>{
	
	protected static final Class<Integer> INTEGER_CLASS = Integer.TYPE;
	protected static final Class<Boolean> BOOLEAN_CLASS = Boolean.TYPE;
	
	public abstract int getDefaultHeuristic();
	public abstract int getDefaultCost();
	public abstract int getNoCost();

	@Test
	public void testGetHeuristic() {
		testGet(getDefaultHeuristic(), "Heuristic");
	}

	@Test
	public void testSetHeuristic() {
		testSet(getDefaultHeuristic()+7, "Heuristic", INTEGER_CLASS);
	}

	@Test
	public void testGetCost() {
		testGet(getDefaultCost(),"Cost");
	}

	@Test
	public void testSetCost() {
		testSet(getDefaultCost()+11,"Cost",INTEGER_CLASS);
	}

	@Test
	public void testGetAcumulatedCost() {
		testGet(getNoCost(), "AcumulatedCost");
	}

	@Test
	public void testSetAcumulatedCost() {
		testSet(getDefaultCost()+13,"AcumulatedCost",INTEGER_CLASS);
	}

	@Test
	public void testIsBestSolution() {
		testGet(false, "BestSolution");
	}

	@Test
	public void testSetBestSolution() {
		testSet(true, "BestSolution", BOOLEAN_CLASS);
	}

	@Test
	public void testIsSolution() {
		testGet(false,"Solution");
	}

	@Test
	public void testSetSolution() {
		testSet(true,"Solution",BOOLEAN_CLASS);
	}

	@Test
	public void testIsCostReady() {
		testGet(false,"CostReady");
	}

	@Test
	public void testSetCostReady() {
		testSet(true,"CostReady",BOOLEAN_CLASS);
	}
	
	protected void testGet(Object defaultValue, String method) {
		Class clazz = b.getClass();
		boolean is = isGet(clazz,method);
		try {
			for (S spec:specifications) {
				assertEquals(defaultValue, invokeGet(method, clazz, is, spec));
			}
		} catch (Exception e) {
			fail("Thrown exception: "+e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private Object invokeGet(String method, Class clazz, boolean is, S spec) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return clazz.getMethod((is?"is":"get")+method, new Class[0]).invoke(spec, new Object[0]);
	}
	
	@SuppressWarnings("unchecked")
	protected void testSet(Object value, String method, Class... argumentsClasses) {
		Class clazz = b.getClass();
		boolean is = isGet(clazz,method);
		try {
			for (S spec:specifications) {
				assertNotSame(value, invokeGet(method, clazz, is, spec));
				clazz.getMethod("set"+method, argumentsClasses).invoke(spec,value);
				assertEquals(value, invokeGet(method, clazz, is, spec));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private boolean isGet(Class clazz, String method) {
		for (Method met:clazz.getMethods())
			if (met.getName().equals("is"+method))
				return true;
		return false;
	}
}
