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
package dinapter.behavior;

import java.util.List;
import java.util.ArrayList;

import net.sourceforge.jpowergraph.DefaultNode;

/**
 * @author José Antonio Martín Baena
 * @version $Revision: 397 $ $Date: 2007-01-03 21:33:35 +0100 (mié, 03 ene 2007) $
 */
public class JPowerBehaviorNode<A> extends DefaultNode implements BehaviorNode<A> {
	
	private final SimpleBehaviorNode<A> inner;
	
	public JPowerBehaviorNode(BehaviorNodeType type) {
		this(type,"",new ArrayList<A>());
	}

	public JPowerBehaviorNode(BehaviorNodeType type, Object description, List<A> arguments) {
		super();
		inner = new SimpleBehaviorNode<A>(type,description,arguments);
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.jpowergraph.DefaultNode#getNodeType()
	 */
	@Override
	public String getNodeType() {
		return getType().name();
	}

	/**
	 * @return
	 * @see dinapter.behavior.SimpleBehaviorNode#hashCode()
	 *-/
	public int hashCode() {
		return inner.hashCode();
	}

	/-**
	 * @param obj
	 * @return
	 * @see dinapter.behavior.SimpleBehaviorNode#equals(java.lang.Object)
	 *-/
	public boolean equals(Object obj) {
		return inner.equals(obj);
	}*/

	/*
	 * @see dinapter.behavior.SimpleBehaviorNode#getArguments()
	 */
	public List<A> getArguments() {
		return inner.getArguments();
	}
	
	public Object [] getArgumentsArray() {
		return getArguments().toArray();
	}

	/*
	 * @see dinapter.behavior.SimpleBehaviorNode#getType()
	 */
	public BehaviorNodeType getType() {
		return inner.getType();
	}

	/*
	 * @see dinapter.behavior.SimpleBehaviorNode#isNamePostprocessing()
	 */
	public boolean isNamePostprocessing() {
		return inner.isNamePostprocessing();
	}

	/*
	 * @see dinapter.behavior.SimpleBehaviorNode#setNamePostprocessing(boolean)
	 */
	public void setNamePostprocessing(boolean namePostprocessing) {
		inner.setNamePostprocessing(namePostprocessing);
	}

	/*
	 * @see dinapter.behavior.SimpleBehaviorNode#toString()
	 */
	public String toString() {
		return inner.toString();
	}

	/*
	 * @see dinapter.behavior.SimpleBehaviorNode#getDescription()
	 */
	public Object getDescription() {
		return inner.getDescription();
	}

}
