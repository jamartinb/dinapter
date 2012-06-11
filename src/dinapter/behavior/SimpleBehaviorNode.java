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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleBehaviorNode<A> implements BehaviorNode<A> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -976606974159098173L;
	private static final String RECEIVE_STRING = "?";
	private static final String SEND_PREFIX = "!";
        private static final List EMPTY_ARGUMENT_LIST = Collections.unmodifiableList(new ArrayList<Object>(0));
	//private static final String TO_STRING_PREFIX = "DefaultBehaviorNode-";
	//private static final String START_STRING = "START";
	//private static final String END_STRING = "END";
	protected List<A> argumentsList = null;
	protected final BehaviorNodeType type;
	protected final Object description;
	private boolean namePostprocessing = true;
	
	@SuppressWarnings("unchecked")
	public SimpleBehaviorNode(BehaviorNodeType type) {
		this(type,null,(List<A>)EMPTY_ARGUMENT_LIST);
	}
	
	public SimpleBehaviorNode(BehaviorNodeType type, List<A> arguments) {
		this(type,null,arguments);
	}

	public SimpleBehaviorNode(BehaviorNodeType type, Object description, List<A> arguments) {
		super();
		this.type = type;
		// ESCA-JAVA0256:
		this.argumentsList = arguments;
		this.description = description;
	}

        @SuppressWarnings("unchecked")
	public List<A> getArguments() {
		if (argumentsList == null)
			argumentsList = (List<A>)EMPTY_ARGUMENT_LIST;
		// ESCA-JAVA0259:
		return argumentsList;
	}

	public BehaviorNodeType getType() {
		return type;
	}

	public String toString() {
		StringBuffer toReturn = new StringBuffer();
		if (description==null) {
			if (getType().equals(BehaviorNodeType.SEND) || getType().equals(BehaviorNodeType.RECEIVE))
				toReturn.append(getType()+"-"+this.hashCode());
			else
				toReturn.append(getType());
		} else {
			if (isNamePostprocessing()) {
				toReturn.append(description.toString());
				if (getType().equals(BehaviorNodeType.SEND))
					toReturn.append(SEND_PREFIX);
				else if (getType().equals(BehaviorNodeType.RECEIVE))
					toReturn.append(RECEIVE_STRING);
				toReturn.append("(");
				if (!getArguments().isEmpty()) {
					for (A arg:getArguments())
						toReturn.append(arg.toString()+", ");
					toReturn.setLength(toReturn.length()-2);
				} else {
					toReturn.append(" ");
				}
				toReturn.append(")");
			} else
				toReturn.append(description.toString());
		}
		return toReturn.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 *-/
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;//super.hashCode();
		result = PRIME * result + ((argumentsList == null) ? 0 : argumentsList.hashCode());
		result = PRIME * result + ((description == null) ? 0 : description.hashCode());
		result = PRIME * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/-* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 *-/
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof BehaviorNode))
			return false;
		final BehaviorNode other = (BehaviorNode) obj;
		if (description == null) {
			if (other.getDescription() != null)
				return false;
		} else {
			if (!description.equals(other.getDescription()))
				return false;
		}
		if (argumentsList == null) {
			if (other.getArguments() != null)
				return false;
		} else if (!argumentsList.equals(other.getArguments()))
			return false;
		if (type == null) {
			if (other.getType() != null)
				return false;
		} else if (!type.equals(other.getType()))
			return false;
		return true;
	}*/

	/**
	 * @return the namePostprocessing
	 */
	public boolean isNamePostprocessing() {
		return namePostprocessing;
	}

	/**
	 * @param namePostprocessing the namePostprocessing to set
	 */
	public void setNamePostprocessing(boolean namePostprocessing) {
		this.namePostprocessing = namePostprocessing;
	}

	/**
	 * @return the description
	 */
	public Object getDescription() {
		return description;
	}

}
