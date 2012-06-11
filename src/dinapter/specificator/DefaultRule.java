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
 * Author:  José Antonio Martín Baena
 * email:   jose.antonio.martin.baena@gmail.com
 *
 * This code is under the GPL License for any non-comercial project
 * after a notification is sent to the authors. For any other kind of
 * project an individual license must be obtained from the authors.
 *
 * This software is available as it is without any warranty or responsability
 * being held by the author. If you use this is at your own risk.
 *
 * Hope this code is usefull for you. Enjoy it!. ;)
 */
package dinapter.specificator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import dinapter.behavior.BehaviorNode;

/**
 * @author José Antonio Martín Baena
 */
public class DefaultRule<N extends BehaviorNode> implements Rule<N>, jsearchdemo.HeuristicNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2676302099829378370L;
	
	private static final Logger log = Logger.getLogger(DefaultRule.class);
	
	private final List<N> leftSide;
	private final List<N> rightSide;
	private final boolean required;
	private final boolean interleaved;
	public static final int NO_HEURISTIC = -1;
	protected int heuristic = NO_HEURISTIC;
	private final int HASH_CODE;


	public DefaultRule(List<N> leftSide, List<N> rightSide, boolean required
			, boolean interleaved) {
		checkActions(leftSide,rightSide);
		this.leftSide = Collections.unmodifiableList(leftSide);
		this.rightSide = Collections.unmodifiableList(rightSide);
		this.required = required;
		this.interleaved = interleaved;
		final int PRIME = 31;
		int result = 1;//super.hashCode();
		result = PRIME * result + (interleaved ? 1231 : 1237);
		result = PRIME * result + ((leftSide == null) ? 0 : leftSide.hashCode());
		result = PRIME * result + (required ? 1231 : 1237);
		result = PRIME * result + ((rightSide == null) ? 0 : rightSide.hashCode());
		HASH_CODE = result;
	}
	
	public DefaultRule(List<N> leftSide, List<N> rightSide) {
		this(leftSide, rightSide, false, false);
	}
	
	public DefaultRule(N [] leftSide, N... rightSide) {
		this(leftSide==null?new ArrayList<N>(0):Arrays.asList(leftSide),
			 rightSide==null?new ArrayList<N>(0):Arrays.asList(rightSide));
	}
	
	/* (non-Javadoc)
	 * @see jsearchdemo.HeuristicNode#getHeuristic()
	 */
	public int getHeuristic() {
		return heuristic;
	}

	public Object [] getLeftSideArray() {
		return leftSide.toArray();
	}
	
	/* (non-Javadoc)
	 * @see dinapter.specificator.Rule#getLeftSide()
	 */
	public List<N> getLeftSide() {
		// ESCA-JAVA0259:
		return leftSide;
	}

	public Object [] getRightSideArray() {
		return rightSide.toArray();
	}
	
	/* (non-Javadoc)
	 * @see dinapter.specificator.Rule#getRightSide()
	 */
	public List<N> getRightSide() {
		// ESCA-JAVA0259:
		return rightSide;
	}

	/* (non-Javadoc)
	 * @see dinapter.specificator.Rule#isInterleaved()
	 */
	public boolean isInterleaved() {
		return interleaved;
	}

	/* (non-Javadoc)
	 * @see dinapter.specificator.Rule#isRequired()
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * @param heuristic the heuristic to set
	 */
	public void setHeuristic(int heuristic) {
		this.heuristic = heuristic;
	}
	
	protected void checkActions(List<N> leftSide, List<N> rightSide) {
		checkActions(leftSide, "Left");
		checkActions(rightSide, "Right");
	}
	
	protected void checkActions(List<N> actions, String side) {
		for (N action:actions) {
			if ((action.getType() != BehaviorNode.BehaviorNodeType.SEND) &&
				(action.getType() != BehaviorNode.BehaviorNodeType.RECEIVE)) {
				throw new IllegalArgumentException("Actions inside Rule must be SEND or RECEIVE type. "
						+side+" side action \""+action+"\" was of type "+action.getType());
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return HASH_CODE;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof DefaultRule))
			return false;
		final DefaultRule other = (DefaultRule) obj;
		if (interleaved != other.interleaved)
			return false;
		if (leftSide == null) {
			if (other.leftSide != null)
				return false;
		} else if (!leftSide.equals(other.leftSide))
			return false;
		if (required != other.required)
			return false;
		if (rightSide == null) {
			if (other.rightSide != null)
				return false;
		} else if (!rightSide.equals(other.rightSide))
			return false;
		return true;
	}
	
	public String toString() {
		StringBuffer toReturn = new StringBuffer();
		if (log.isDebugEnabled()) {
			toReturn.append((getHeuristic()!=NO_HEURISTIC)?"[h="+getHeuristic()+"]":"");
		}
		toReturn.append(toString(getLeftSide()));
		toReturn.append(" ");
		char symbol;
		if (isRequired())
			symbol = '◆';
		else
			symbol = '◊';
		toReturn.append(symbol);
		if (isInterleaved())
			toReturn.append(symbol);
		toReturn.append(" ");
		toReturn.append(toString(getRightSide())); 
		return toReturn.toString();
	}
	
	protected String toString(List<N> side) {
		if (side.isEmpty())
			return ";";
		StringBuffer toReturn = new StringBuffer();
		for (N action:side) {
			toReturn.append(action+"; ");
		}
		toReturn.delete(toReturn.length()-2, toReturn.length());
		return toReturn.toString();
	}
	
}
