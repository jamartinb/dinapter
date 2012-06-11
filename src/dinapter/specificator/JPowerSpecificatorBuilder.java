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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dinapter.behavior.JPowerBehaviorGraphBuilder;
import dinapter.behavior.JPowerBehaviorNode;
import dinapter.behavior.BehaviorNode.BehaviorNodeType;

/**
 * Utility class which easies the generation of actions (or BehaviorNodes)
 * , behavior graphs, rules, specifications and specifications graphs. It does so
 * delegating on a set of other utility classes such as {@link JPowerBehaviorGraphBuilder}
 * , {@link DefaultRuleBuilder} as well as its parent class.
 * <p>
 * It's particularly interesting the concept of <b>working specification</b> and <b>action push</b>.
 * A <b>working specification</b> is the specification the new specifiation is going to be based on.
 * The new specification will contain all the rules from its <i>parent specification</i> but its
 * <i>parent specification</i> working rule which may be extended with the new action or closed. In the
 * later case a new working rule is generated containing only the new action. This process of
 * generating a new specification based on a parent one and adding a new action is called
 * <b>action push</b>. Wether the working rule may be closed or extended is decided with {@link #closeRule()}.
 * @author José Antonio Martín Baena
 * @version $Revision: 449 $ - $Date: 2007-02-01 20:05:11 +0100 (jue, 01 feb 2007) $
 * @param <A> The class of the arguments of the behavior actions.
 */
public class JPowerSpecificatorBuilder<A> extends JPowerSpecificatorGraphBuilder<DefaultRule<JPowerBehaviorNode<A>>>
            implements SpecificatorBuilder<JPowerBehaviorNode<A>
                                            ,DefaultRule<JPowerBehaviorNode<A>>
                                            ,JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>>
                                            ,JPowerSpecificatorGraph<DefaultRule<JPowerBehaviorNode<A>>,JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>>>>{
	private final JPowerBehaviorGraphBuilder<A> actionsBuilder = new JPowerBehaviorGraphBuilder<A>();
	private final DefaultRuleBuilder<JPowerBehaviorNode<A>> ruleBuilder = new DefaultRuleBuilder<JPowerBehaviorNode<A>>();
	private JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>> workingSpecification = null;
	private boolean closedRule = false;
    
    /**
     * A singleton of an empty rule.
     */
	protected final DefaultRule<JPowerBehaviorNode<A>> EMPTY_RULE;

	/**
	 * It instantiates this class. 
	 */
	public JPowerSpecificatorBuilder() {
		super();
		EMPTY_RULE = getRule();
		createRule();
	}

	/**
	 * @see dinapter.behavior.AbstractBehaviorGraphBuilder#createNode(dinapter.behavior.BehaviorNode.BehaviorNodeType, A[])
	 * @deprecated Is not for this builder to create behavior nodes.
	 */
        @Deprecated
	public JPowerBehaviorNode<A> createAction(BehaviorNodeType type, A... arguments) {
		return actionsBuilder.createNode(type, arguments);
	}

	/**
	 * @see dinapter.behavior.AbstractBehaviorGraphBuilder#createNode(java.lang.Object, dinapter.behavior.BehaviorNode.BehaviorNodeType, A[])ç
	 * @deprecated Is not for this builder to create behavior nodes.
	 */
        @Deprecated
	public JPowerBehaviorNode<A> createAction(Object description, BehaviorNodeType type, A... arguments) {
		return actionsBuilder.createNode(description, type, arguments);
	}

	/**
	 * @see dinapter.behavior.JPowerBehaviorGraphBuilder#createNode(java.lang.Object, dinapter.behavior.BehaviorNode.BehaviorNodeType, java.util.List)
	 * @deprecated Is not for this builder to create behavior nodes.
	 */
        @Deprecated
	public JPowerBehaviorNode<A> createAction(Object description, BehaviorNodeType type, List<A> arguments) {
		return actionsBuilder.createNode(description, type, arguments);
	}

	/**
	 * @see dinapter.behavior.AbstractBehaviorGraphBuilder#createNode(java.lang.Object, dinapter.behavior.BehaviorNode.BehaviorNodeType)
	 * @deprecated Is not for this builder to create behavior nodes.
	 */
        @Deprecated
	public JPowerBehaviorNode<A> createAction(Object description, BehaviorNodeType type) {
		return actionsBuilder.createNode(description, type);
	}

	/**
	 * @see dinapter.specificator.AbstractRuleBuilder#createRule()
	 */
	public void createRule() {
		ruleBuilder.createRule();
	}

	/**
	 * @see dinapter.specificator.AbstractRuleBuilder#getRule()
	 */
	public DefaultRule<JPowerBehaviorNode<A>> getRule() {
		return ruleBuilder.getRule();
	}

	/**
	 * @see dinapter.specificator.AbstractRuleBuilder#setInterleaved(boolean)
	 */
	public void setInterleaved(boolean interleaved) {
		ruleBuilder.setInterleaved(interleaved);
	}

	/**
	 * @see dinapter.specificator.AbstractRuleBuilder#setLeftSide(N[])
	 */
	public void setLeftSide(JPowerBehaviorNode<A>... nodes) {
		ruleBuilder.setLeftSide(nodes);
	}

	/**
	 * @see dinapter.specificator.AbstractRuleBuilder#setRequired(boolean)
	 */
	public void setRequired(boolean required) {
		ruleBuilder.setRequired(required);
	}

	/**
	 * @see dinapter.specificator.AbstractRuleBuilder#setRightSide(N[])
	 */
	public void setRightSide(JPowerBehaviorNode<A>... nodes) {
		ruleBuilder.setRightSide(nodes);
	}
	
    /**
     * It pushes an action to the left side of the working specification.
     * @param node Action to push.
     */
	public void pushLeft(JPowerBehaviorNode<A> node) {
		pushSide(true, node);
	}
	
    /**
     * It pushes an action to the right side of the working specification.
     * @param node Action to push.
     */
	public void pushRight(JPowerBehaviorNode<A> node) {
		pushSide(false,node);
	}
	
	@SuppressWarnings("unchecked")
	private void pushSide(boolean left, JPowerBehaviorNode<A> node) {
		ArrayList<DefaultRule<JPowerBehaviorNode<A>>> rules = null;
		createRule();
		DefaultRule<JPowerBehaviorNode<A>> workingRule = getWorkingRule();
		ArrayList<JPowerBehaviorNode<A>> actions = null;
		if (closedRule) {
			actions = new ArrayList<JPowerBehaviorNode<A>>(1);
		} else {
			actions = new ArrayList<JPowerBehaviorNode<A>>(workingRule.getLeftSide().size()+1);
			Collection<JPowerBehaviorNode<A>> side 
				= left?workingRule.getLeftSide():workingRule.getRightSide();
			actions.addAll(side);
		}
		actions.add(node);
		if (left) {
			setLeftSide(actions);
			if (!closedRule)
				setRightSide(workingRule.getRightSide());
		} else {
			setRightSide(actions);
			if (!closedRule)
				setLeftSide(workingRule.getLeftSide());
		}
		rules = new ArrayList<DefaultRule<JPowerBehaviorNode<A>>>(getWorkingSpecification().getRules().size()+((closedRule)?1:0));
		rules.addAll(closedRule||getWorkingSpecification().getRules().isEmpty()
				?getWorkingSpecification().getRules()
				:((List<DefaultRule<JPowerBehaviorNode<A>>>)getWorkingSpecification().getRules()).subList(0, getWorkingSpecification().getRules().size()-1));
		rules.add(getRule());
		JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>> specification, aux;
		specification = createSpecification(rules);
		Object [] oldPath = left?getWorkingSpecification().getLeftPath():getWorkingSpecification().getRightPath();
		Object [] newPath = new Object[oldPath.length+1];
		System.arraycopy(oldPath, 0, newPath, 0, oldPath.length);
		newPath[newPath.length-1] = node;
		if (left) {
			specification.setLeftPath(newPath);
			specification.setRightPath(getWorkingSpecification().getRightPath());
		} else {
			specification.setRightPath(newPath);
			specification.setLeftPath(getWorkingSpecification().getLeftPath());
		}
		aux = getEquivalentSpecification(specification);
		if (aux != null)
			specification = aux;
		link(getWorkingSpecification(), specification);
		closedRule = false;
	}
	
    /**
     * It returns wheter two specifications are equivalent or not. Two specifications
     * are equivalent when they contain the same set of rules and the same
     * working rule.
     * @param aSpec An specification to compare.
     * @param bSpec The other specification to compare.
     * @return <code>true</code> if the given specifications are equivalent or <code>false</code> otherwise.
     */
	public boolean equivalentSpecifications(JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>> aSpec, JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>> bSpec) {
		return aSpec.getRules().equals(bSpec.getRules());
	}
	
    /**
     * It returns an equivalent specification within the current SpecificatorGraph.
     * @param specification Specification it may be compared against.
     * @return An equivalent specification within the graph or <code>null</code> if there is none.
     */
	public JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>> getEquivalentSpecification(
			JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>> specification) {
		for (JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>> spec:getGraph().getAllNodes()) {
			if (!specification.equals(spec) && equivalentSpecifications(specification, spec))
				return spec;
		}
		return null;
	}
	
    /**
     * It returns the working rule of the working specification.
     * @return The working rule of the working specification.
     */
	public DefaultRule<JPowerBehaviorNode<A>> getWorkingRule() {
		try {
			List<DefaultRule<JPowerBehaviorNode<A>>> rules
				= (List<DefaultRule<JPowerBehaviorNode<A>>>)getWorkingSpecification().getRules();
			if (rules.isEmpty())
				return EMPTY_RULE;
			return rules.get(rules.size()-1);
		} catch (ClassCastException e) {
			throw new RuntimeException("Working specification doesn't contain rules inside a list.",e);
		}
	}
	
    /**
     * It flags that the next specification to be generated will close the working
     * rule of its parent specification. This flag will be reseted if the
     * working specification is changed afterwards.
     */
	public void closeRule() {
		closedRule = true;
	}
	
    /**
     * It sets the working specification next specification to generate will be based on.
     * @param spec New working specification.
     */
	public void setWorkingSpecification(JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>> spec) {
		if ((spec != null) && !getGraph().getAllNodes().contains(spec))
			throw new IllegalArgumentException("Working specification to set is not in the current SpecificatorGraph.");
		closedRule = false;
		workingSpecification = spec;
	}
	
    /**
     * It returns the current working specification.
     * @return The current working specification.
     */
	public JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>> getWorkingSpecification() {
		if (workingSpecification == null)
			setWorkingSpecification(createSpecification());
		return workingSpecification;
	}

	/**
	 * @see dinapter.specificator.AbstractRuleBuilder#setLeftSide(java.util.List)
	 */
	public void setLeftSide(List<JPowerBehaviorNode<A>> nodes) {
		ruleBuilder.setLeftSide(nodes);
	}

	/**
	 * @see dinapter.specificator.AbstractRuleBuilder#setRightSide(java.util.List)
	 */
	public void setRightSide(List<JPowerBehaviorNode<A>> nodes) {
		ruleBuilder.setRightSide(nodes);
	}

	/* (non-Javadoc)
	 * @see dinapter.graph.AbstractGraphBuilder#link(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void link(JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>> from, JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>> to) {
		// @todo This check is an overhead but could be convenient.
		JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>> equivalent;
        if ((equivalent = getEquivalentSpecification(to)) != null)
			throw new IllegalArgumentException("Trying to link an equivalent specification: "+to+"\n------------ existing: ----------\n"+equivalent);
		super.link(from, to);
		setWorkingSpecification(to);
	}

	/* (non-Javadoc)
	 * @see dinapter.specificator.JPowerSpecificatorGraphBuilder#setGraph(dinapter.specificator.JPowerSpecificatorGraph)
	 */
	@Override
	public void setGraph(JPowerSpecificatorGraph<DefaultRule<JPowerBehaviorNode<A>>, JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>>> graph) {
		super.setGraph(graph);
		setWorkingSpecification(null);
	}

        public JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>> 
                createNode(DefaultRule<JPowerBehaviorNode<A>>... rules) {
            return createSpecification(rules);
        }

        /*public JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>> 
                createSpecification(List<DefaultRule<JPowerBehaviorNode<A>>> rules) {
            return createSpecification((Collection<DefaultRule<JPowerBehaviorNode<A>>>) rules);
        }*/

    public JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>> copySpecification(JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>> from) {
        JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>> toReturn
                = createSpecification(from.getRules(),from.getWorkingRule());
        toReturn.setHeuristic(from.getHeuristic());
        toReturn.setCost(from.getCost());
        toReturn.setChildrenNeeded(from.isChildrenNeeded());
        toReturn.setChildrenReady(from.isChildrenReady());
        toReturn.setLeftPath(from.getLeftPath());
        toReturn.setRightPath(from.getRightPath());
        toReturn.setCopiedSpecification(true);
        toReturn.setAcumulatedCost(from.getAcumulatedCost());
        return toReturn;
    }
}
