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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dinapter.behavior.BehaviorNode;

/**
 * This is an utility class which easies the generation of rules.
 * @author José Antonio Martín Baena
 * @version $Revision: 449 $ - $Date: 2007-02-01 20:05:11 +0100 (jue, 01 feb 2007) $
 */
public abstract class AbstractRuleBuilder<N extends BehaviorNode, R extends Rule<N>> implements RuleBuilder<N, R> {
	private R buildingRule = null;
	private List<N> leftSide, rightSide;
	private boolean required = false;
	private boolean interleaved = false;
	
	/**
     * It resets the builder for creating a new rule.
	 * New rules conserve previous required and interleaved arguments as default values.
	 */
	public void createRule() {
		ruleChanged();
		leftSide = null;
		rightSide = null;
	}
	
    /**
     * It sets next rule left sided actions.
     * @param nodes Actions to be in the left side of the rule.
     */
	public void setLeftSide(N... nodes) {
		ruleChanged();
		leftSide = Arrays.asList(nodes);
	}
	
    /**
     * It sets next rule left sided actions.
     * @param nodes Actions to be in the left side of the rule.
     */
    @SuppressWarnings("unchecked")
	public void setLeftSide(List<N> nodes) {
		ruleChanged();
		// ESCA-JAVA0256:
		leftSide = nodes;
	}
	
	/**
     * It sets next rule right sided actions. 
     * @param nodes Actions to be in the right side of the rule.
	 */
    public void setRightSide(N... nodes) {
		ruleChanged();
		rightSide = Arrays.asList(nodes);
	}
	
    /**
     * It sets next rule right sided actions. 
     * @param nodes Actions to be in the right side of the rule.
     */
    @SuppressWarnings("unchecked")
	public void setRightSide(List<N> nodes) {
		ruleChanged();
		// ESCA-JAVA0256:
		rightSide = nodes;
	}
	
	/**
     * It flags next rule to be required. 
     * @param required Wehter next rule is going to be required or not.
	 */
    public void setRequired(boolean required) {
		ruleChanged();
		this.required = required;
	}
	
    /**
     * Called anytime any factor of the rule has changed. It automatically
     * forgets any previously generated rule.
     */
	protected void ruleChanged() {
		buildingRule = null;
	}
	
	/**
     * It flags next rule to be interleaved 
     * @param interleaved Whether or not the next rule is interleaved.
	 */
    public void setInterleaved(boolean interleaved) {
		this.interleaved = interleaved;
	}
	
    /**
     * It actually creates the new rule using all the arguments previously
     * setted using the other methods. This abstract method is intended to
     * be overriden and generate a precise class of rules.
     * @param leftSide Left side of the rule.
     * @param rightSide Right side of the rule.
     * @param required Whether the rule is required.
     * @param interleaved Whether the rule is interleaved.
     * @return The newly created rule.
     */
	protected abstract R createRule(List<N> leftSide,List<N> rightSide, boolean required, boolean interleaved);
	
    /**
     * It returns the newly created rule or it actually creates a new one
     * if any argument has been changed.
     * @return The new rule.
     */
	public R getRule() {
		if (buildingRule == null) {
			buildingRule = createRule(
					(leftSide == null)?new ArrayList<N>(0):leftSide
					,(rightSide == null)?new ArrayList<N>(0):rightSide
					,required,interleaved);
		}
		return buildingRule;
	}
}
