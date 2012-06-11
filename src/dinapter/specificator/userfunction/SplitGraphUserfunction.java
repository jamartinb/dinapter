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
/*
 * SplitGraphUserfunction.java
 *
 * Created on 29 de mayo de 2007, 16:31
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dinapter.specificator.userfunction;

import org.apache.log4j.Logger;

import jess.Context;
import jess.JessException;
import jess.Userfunction;
import jess.Value;
import jess.ValueVector;
import jsearchdemo.HeuristicNode;
import dinapter.behavior.BehaviorGraph;
import dinapter.behavior.BehaviorNode;
import dinapter.specificator.JSearchSpecification;
import dinapter.specificator.JSearchSpecificator;
import dinapter.specificator.Rule;
import dinapter.specificator.SpecificatorBuilder;
import dinapter.specificator.SpecificatorGraph;

/**
 *
 * @author arkangel
 */
public class SplitGraphUserfunction<A,B extends BehaviorNode<A>,BG extends BehaviorGraph<B>,R extends Rule<B> & HeuristicNode,S extends JSearchSpecification<R>,SG extends SpecificatorGraph<S> & jsearchdemo.Graph>
        implements Userfunction {
    
	private static final Logger log = Logger.getLogger(SplitGraphUserfunction.class);
    protected final JSearchSpecificator<A,B,BG,R,S,SG> specificator;
    protected final SpecificatorBuilder<B,R,S,SG> builder;
    
    /** Creates a new instance of SplitGraphUserfunction */
    public SplitGraphUserfunction(JSearchSpecificator<A,B,BG,R,S,SG> specificator, SpecificatorBuilder<B,R,S,SG> builder) {
        this.specificator = specificator;
        this.builder = builder;
    }
    
    public String getName() {
        return "splitGraph";
    }
    
    @SuppressWarnings("unchecked")
	public Value call(ValueVector vv, Context context) throws JessException {
        B node = (B) vv.get(2).javaObjectValue(context);
        S specification = (S) vv.get(4).javaObjectValue(context);
        String side = vv.get(3).stringValue(context);
        B switchAction = (B) vv.get(1).javaObjectValue(context);
        SG toReturn = splitGraph(switchAction,node, side.equals("left"), specification);
        return new Value(toReturn);
    }
    
    /**
     * Delegate method in charge of actually generate a new graph because of a <i>SWITCH</i> node.
     * @param switchAction <i>SWITCH</i> action which caused this fork.
     * @param nodeToPush Node to push after the <i>SWITCH</i> action.
     * @param left <code>true</code> if the actions belong to the left component. <code>false</code> otherwise.
     * @param specification Parent specification where new actions are going to be added to.
     * @return Newly created specificator graph.
     */
    @SuppressWarnings("unchecked")
    protected SG splitGraph( B switchAction, B nodeToPush, boolean left, S specification) {
        
        // * Creating the new Graph *
        SG splittedGraph = builder.createNewGraph();
        builder.setGraph(splittedGraph);
        
        // * Start node of the Graph *
        splittedGraph.setStartNode(builder.createSpecification());
        splittedGraph.getStartNode().setChildrenReady(true); // No more children.
        splittedGraph.getStartNode().setCost(0);
        splittedGraph.getStartNode().setHeuristic(
        		specification.getHeuristic()
        		+(specification.getAcumulatedCost()==JSearchSpecification.NO_COST?
        				0
        				:specification.getAcumulatedCost())); // Same as copied Specification.
        splittedGraph.getStartNode().setCopiedSpecification(true); // No heuristic or cost change allowed.
        
        // * Copied-link parent node *
        S copied = builder.copySpecification(specification);
        // These two following sentences avoid ''(add-close-rule)'' to fire.
        copied.setChildrenNeeded(false); // No more children.
        copied.setChildrenReady(true);
        copied.setCost(
        		specification.getAcumulatedCost()==JSearchSpecification.NO_COST?
        				0
        				:specification.getAcumulatedCost()); // Just to keep old acumulated cost.
        copied.setHeuristic(0); // Old heuristic is not valid anymore.
        builder.link(splittedGraph.getStartNode(), copied);
        
        // * Creating and linking of the real new node *
        builder.closeRule(); // Close previous rule.
        if (left)
            builder.pushLeft(nodeToPush);
        else
            builder.pushRight(nodeToPush);
        
        // Ignore created specification with no new rules.
        // It is not needed the same when merging graphs because those are retracted by (retract-equivalent-merges)
        // @toreview: Ignoring repeated rules makes impossible to support indeterministic behaviors.
        /*		O			-------O-------
         * 		|a!			|a?			  |a?
         * 		O			O			  O
         */
        if (builder.getWorkingSpecification().getActionsCount() <= specification.getActionsCount()) {
        	builder.getGraph().removeNode(builder.getWorkingSpecification());
        	return null;
        }
        
        // * New node paths calculations *
        Object [] oldPath = left?specification.getLeftPath():specification.getRightPath();
        Object [] newPath = new Object[oldPath.length+2];
        System.arraycopy(oldPath, 0, newPath, 0, oldPath.length);
        newPath[newPath.length-2] = switchAction;
        newPath[newPath.length-1] = nodeToPush;
        if (left)
            builder.getWorkingSpecification().setLeftPath(newPath);
        else
            builder.getWorkingSpecification().setRightPath(newPath);
        log.trace("Splitted: "+builder.getWorkingSpecification());
        // * Adding graph and new specs to Rete *
        specificator.addSearch(splittedGraph,builder.getWorkingSpecification());
        //log.debug("Splitted graph in two");
        return splittedGraph;
    }
}
