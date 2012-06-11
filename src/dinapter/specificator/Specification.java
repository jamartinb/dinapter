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

import java.util.Collection;

/**
 * An Specification is a match between communication actions (SENDs and
 * RECEIVEs) from two different components. It's divided in a set of rules which
 * may be matched several times during the life of the components. Specification
 * usually represents the guidelines of the adaptation between two components
 * with behavioural incompabilities.
 * <p>
 * Some Specifications are supposed to adapt better than others and this fact is
 * evaluated using <b>costs and heuristics</b>. In one hand <b>cost</b> is a
 * quantification of the adaptation mistakes that the Specification has for
 * sure. In the other hand <b>heuristic</b> is the presumption of how badly
 * this Specification will be if we continue evolving it adding new rules.
 * Notice that this system of evaluationg Specifications quality fits
 * particularly well with several AI techniques such as A*.
 * 
 * @see <a
 *      href="http://www.cs.dartmouth.edu/brd/Teaching/AI/Lectures/Summaries/search.html">Sumary
 *      of search AI techniques</a>
 * @see <a href="http://theory.stanford.edu/~amitp/GameProgramming/">Explanation
 *      and thoughs about A*</a>
 * @author José Antonio Martín Baena
 * @version $Revision: 449 $ - $Date: 2007-02-01 20:05:11 +0100 (jue, 01 feb
 *          2007) $
 */
public interface Specification<R extends Rule> extends Comparable {
	/**
	 * Event id for rules changes.
	 */
	public static final String RULES_CHANGE = "rules";

	/**
	 * Event id for children needed property changes.
	 */
	public static final String CHILDREN_NEEDED_CHANGE = "childrenNeeded";

	/**
	 * Event id for children ready property changes.
	 */
	public static final String CHILDREN_READY_CHANGE = "childrenReady";

	/**
	 * Event id for left path changes.
	 */
	public static final String LEFT_PATH_CHANGED = "leftPath";

	/**
	 * Event id for right path changes.
	 */
	public static final String RIGHT_PATH_CHANGED = "rightPath";

	/**
	 * Event id for working rule changes.
	 */
	public static final String WORKING_RULE_CHANGED = "workingRule";

	/**
	 * Event id for merged status changes.
	 */
	public static final String MERGE_STATUS = "merged";

	/**
	 * It returns the collection of rules within the Specification.
	 * 
	 * @return The collection of rules.
	 */
	public Collection<R> getRules();

	public boolean equals(Object otherRule);

	public int hashCode();

	/**
	 * It returns the rule which may be actively being working on in order to
	 * extend this Specification.
	 * 
	 * @return The working rule of this Specification.
	 */
	public R getWorkingRule();

	public void setWorkingRule(R rule);

	/**
	 * It returns whether this specification needs its children to be generated
	 * or not.
	 * 
	 * @return Whether children generation is needed.
	 */
	public boolean isChildrenNeeded();

	/**
	 * It sets whether this specification needs its children to be generated or
	 * not. <b>An specification must be not children ready before needed them.</b>
	 * 
	 * @param childrenNeeded
	 *            Whether children are needed or not.
	 */
	public void setChildrenNeeded(boolean needed);

	/**
	 * It returns the children ready status.
	 * 
	 * @return The children ready status.
	 * @see dinapter.specificator.DefaultSpecification#isChildrenReady()
	 */
	public boolean isChildrenReady();

	/**
	 * It sets a new children ready status.
	 * 
	 * @param childrenReady
	 *            The new children ready status.
	 * @see dinapter.specificator.DefaultSpecification#setChildrenReady(boolean)
	 */
	public void setChildrenReady(boolean ready);

	/**
	 * It returns the left path of this specification.
	 * 
	 * @return The left path of this specification.
	 */
	public Object[] getLeftPath();

	/**
	 * It returns the right path of this specification.
	 * 
	 * @return The right path of this specification.
	 */
	public Object[] getRightPath();

	/**
	 * It sets the right path of this specification.
	 * 
	 * @param rightPath
	 *            New right path of the specification.
	 */
	public void setRightPath(Object[] path);

	/**
	 * It sets the left path of this specification.
	 * 
	 * @param path
	 *            The new left path to set.
	 */
	public void setLeftPath(Object[] path);

	/**
	 * It sets the copied status.
	 * 
	 * @param copiedSpecification
	 *            New copied status.
	 */
	public void setCopiedSpecification(boolean copied);

	/**
	 * It returns the copied status.
	 * 
	 * @return The copied status.
	 */
	public boolean isCopiedSpecification();

	/**
	 * It returns an array with the rules. This array should not be modified.
	 * 
	 * @return An array with the rules.
	 */
	public Object[] getRulesArray();

	/**
	 * It returns whether this specification is a merge result or not.
	 * 
	 * @return Whether this specification is merge result.
	 */
	public boolean isMerged();

	/**
	 * It sets whether this specification comes from a merge.
	 * 
	 * @param merged
	 *            New merge status.
	 */
	public void setMerged(boolean merged);

	/**
	 * It returns the count all the actions (even when they're repeated).
	 * 
	 * @return The count all the actions (even when they're repeated).
	 */
	public int getActionsCount();

	/**
	 * Convenience method for getting an array of all actions (whithout
	 * repetition) within the specification rules.
	 * 
	 * @return All action within the specification rules.
	 */
	public Object[] getActions();
	
	/**
     * It sets the cut status.
     * @param cuttedSpecification The new cut status.
     */
    public void setCuttedSpecification(boolean cutted);
    
    /**
     * It returns the cut status.
     * @return The cut status.
     */
    public boolean isCuttedSpecification();
	
	/**
     * It returns whether the specifications are equivalent or not. Two specifications
     * are equivalent when they contain the same set of rules and the same
     * working rule.
     * @param spec the specification to compare.
     * @return <code>true</code> if the given specifications are equivalent or <code>false</code> otherwise.
     */
    public <S extends Specification> boolean isEquivalent(S spec);
}
