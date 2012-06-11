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
package dinapter.behavior;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import dinapter.behavior.BehaviorNode.BehaviorNodeType;


public class DefaultBehaviorNode2Test 
	extends BehaviorNodeTestAbstract<SimpleBehaviorNode<Object>> {

	/* (non-Javadoc)
	 * @see dinapter.behavior.BehaviorNodeTestAbstract#createNode(dinapter.behavior.BehaviorNode.BehaviorNodeType, java.lang.Object, java.util.List)
	 */
	@Override
	protected SimpleBehaviorNode<Object> createNode(BehaviorNodeType type, Object description, List<Object> arguments) {
		return new SimpleBehaviorNode<Object>(type,description,arguments);
	}
	
	@Test
	public void runTest() {
		Assert.assertTrue(true);
	}
}
