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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dinapter.behavior.BehaviorNode;

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
public abstract class SpecificatorBuilderFacade<A,N extends BehaviorNode<A>
        ,R extends Rule<N>
        ,S extends Specification<R>
        ,G extends SpecificatorGraph<S>>
        implements SpecificatorBuilder<N,R,S,G> {
    private final RuleBuilder<N,R> ruleBuilder;
    private final SpecificatorGraphBuilder<R,S,G> specificatorGraphBuilder;
    private S workingSpecification = null;
    private boolean closedRule = false;
    
    /**
     * A singleton of an empty rule.
     */
    protected final R EMPTY_RULE;
    
    /**
     * It instantiates this class.
     */
    public SpecificatorBuilderFacade() {
        specificatorGraphBuilder = createGraphBuilder();
        ruleBuilder = createRuleBuilder();
        EMPTY_RULE = getRule();;
        createRule();
    }
    
    public abstract SpecificatorGraphBuilder<R,S,G> createGraphBuilder();
    
    public abstract RuleBuilder<N,R> createRuleBuilder();
    
    /**
     * @see dinapter.specificator.AbstractRuleBuilder#createRule()
     */
    public void createRule() {
        ruleBuilder.createRule();
    }
    
    /**
     * @see dinapter.specificator.AbstractRuleBuilder#getRule()
     */
    public R getRule() {
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
    public void setLeftSide(N... nodes) {
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
    public void setRightSide(N... nodes) {
        ruleBuilder.setRightSide(nodes);
    }
    
    /**
     * It pushes an action to the left side of the working specification.
     * @param node Action to push.
     */
    public void pushLeft(N node) {
        pushSide(true, node);
    }
    
    /**
     * It pushes an action to the right side of the working specification.
     * @param node Action to push.
     */
    public void pushRight(N node) {
        pushSide(false,node);
    }
    
    @SuppressWarnings("unchecked")
    private void pushSide(boolean left, N node) {
        ArrayList<R> rules = null;
        createRule();
        R workingRule = getWorkingRule();
        ArrayList<N> actions = null;
        if (closedRule) {
            actions = new ArrayList<N>(1);
        } else {
            actions = new ArrayList<N>(workingRule.getLeftSide().size()+1);
            Collection<N> side
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
        rules = new ArrayList<R>(getWorkingSpecification().getRules().size()+((closedRule)?1:0));
        Collection<R> newRules;
        if (closedRule||getWorkingSpecification().getRules().isEmpty())
        	newRules = getWorkingSpecification().getRules();
        else {
        	if (getWorkingSpecification().getRules() instanceof List)
        		newRules = new ArrayList<R>(getWorkingSpecification().getRules());
        	else if (getWorkingSpecification().getRules() instanceof Set)
        		newRules = new HashSet<R>(getWorkingSpecification().getRules());
        	else
        		throw new RuntimeException("The rules collection is not a List nor a Set.");
        	newRules.remove(workingRule);
        }
        rules.addAll(newRules);
        rules.add(getRule());
        S specification;
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
        // This links together equivalent specifications within the same graph.
        S aux = getEquivalentSpecification(specification);
        if (aux != null)
            specification = aux;
        link(getWorkingSpecification(), specification);
        closedRule = false;
    }
    
    /**
     * It returns an equivalent specification within the current SpecificatorGraph.
     * @param specification Specification it may be compared against.
     * @return An equivalent specification within the graph or <code>null</code> if there is none.
     */
    public S getEquivalentSpecification(S specification) {
        for (S spec:getGraph().getAllNodes()) {
            if (!spec.equals(specification) && spec.isEquivalent(specification))
                return spec;
        }
        return null;
    }
    
    /**
     * It returns the working rule of the working specification.
     * @return The working rule of the working specification.
     */
    public R getWorkingRule() {
    	R toReturn = getWorkingSpecification().getWorkingRule();
    	return (toReturn == null)?EMPTY_RULE:toReturn;
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
    public void setWorkingSpecification(S spec) {
        if ((spec != null) && !getGraph().getAllNodes().contains(spec))
            throw new IllegalArgumentException("Working specification to set is not in the current SpecificatorGraph. (Size="+getGraph().getAllNodes().size()+")");
        closedRule = false;
        workingSpecification = spec;
    }
    
    /**
     * It returns the current working specification.
     * @return The current working specification.
     */
    public S getWorkingSpecification() {
        return workingSpecification;
    }
    
    /**
     * @see dinapter.specificator.AbstractRuleBuilder#setLeftSide(java.util.List)
     */
    public void setLeftSide(List<N> nodes) {
        ruleBuilder.setLeftSide(nodes);
    }
    
    /**
     * @see dinapter.specificator.AbstractRuleBuilder#setRightSide(java.util.List)
     */
    public void setRightSide(List<N> nodes) {
        ruleBuilder.setRightSide(nodes);
    }
    
        /* (non-Javadoc)
         * @see dinapter.graph.AbstractGraphBuilder#link(java.lang.Object, java.lang.Object)
         */
    public void link(S from, S to) {
        // @todo This check is an overhead but could be convenient.
        if (!to.getRules().isEmpty() // <- SWITCH-SWITCH necesitan que dos nodos vacíos se enlacen. El segundo para mantener el accumulatedCost.
        		&& (getEquivalentSpecification(to) != null))
            throw new IllegalArgumentException("Trying to link an equivalent specification: "+to);
        specificatorGraphBuilder.link(from, to);
        setWorkingSpecification(to);
    }
    
        /* (non-Javadoc)
         * @see dinapter.specificator.JPowerSpecificatorGraphBuilder#setGraph(dinapter.specificator.JPowerSpecificatorGraph)
         */
    public void setGraph(G graph) {
        specificatorGraphBuilder.setGraph(graph);
        setWorkingSpecification(null);
    }
    
    public S createSpecification(R... rules) {
        return specificatorGraphBuilder.createSpecification(rules);
    }
    
    public S createSpecification(Collection<R> rules, R workingRule) {
        return specificatorGraphBuilder.createSpecification(rules, workingRule);
    }
    
    public S createSpecification(List<R> rules) {
    	return specificatorGraphBuilder.createSpecification(rules);
    }
    
    public S createSpecification() {
    	return specificatorGraphBuilder.createSpecification();
    }
    
    public G getGraph() {
        return specificatorGraphBuilder.getGraph();
    }
    
    public G createNewGraph() {
        return specificatorGraphBuilder.createNewGraph();
    }
    
    public S copySpecification(S from) {
    	return specificatorGraphBuilder.copySpecification(from);
    }
}
