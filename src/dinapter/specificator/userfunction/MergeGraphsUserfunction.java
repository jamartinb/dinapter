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
 * MergeGraphsUserfunction.java
 *
 * Created on 29 de mayo de 2007, 16:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dinapter.specificator.userfunction;

import org.apache.log4j.Logger;

import dinapter.Dinapter;
import dinapter.behavior.BehaviorGraph;
import dinapter.behavior.BehaviorNode;
import dinapter.specificator.DefaultRule;
import dinapter.specificator.JSearchSpecification;
import dinapter.specificator.JSearchSpecificator;
import dinapter.specificator.Rule;
import dinapter.specificator.SpecificatorBuilder;
import dinapter.specificator.SpecificatorGraph;
import jess.Context;
import jess.JessException;
import jess.Rete;
import jess.Userfunction;
import jess.Value;
import jess.ValueVector;
import jsearchdemo.HeuristicNode;

/**
 * <p>
 * This is the <code>userfunction</code> used by Jess to finally merge to
 * {@link SpecificatorGraph}s into a new one.
 * </p>
 * <h2>Method description</h2>
 * <ul>
 * <li>
 * <p>It is created a <code>startNode</code> with no cost (it doesn't make sense
 * an <code>startNode</code> with cost) and an heursitic like:</p>
 * <p><center><code>h = parent_accumulated_cost + remaining_actions_heuristic + new_action_cost_and_closed_valuations + parent_heuristic</code></center></p>
 * </li>
 * <li>
 * <p>The parent is copied right after with <code>h=0</code> and a cost and an
 * accumulated cost of:</p>
 * <p><center><code>g = ac = parent_accumulated_cost + new_action_cost_and_closed_valuations - one_action_cost</code></p>
 * </li>
 * <li>The merged specification is included.</li>
 * <li>The <code>startNode</code> and the copied
 * <code>parentSpecification</code> are marked as <code>copied</code> and
 * <code>ready</code> for not being modified.</li>
 * <li>This new graph is queued for processing</li>
 * </ul>
 * 
 * @author José Antonio Martín Baena
 */
public class MergeGraphsUserfunction<A, B extends BehaviorNode<A>, BG extends BehaviorGraph<B>, R extends Rule<B> & HeuristicNode, S extends JSearchSpecification<R>, SG extends SpecificatorGraph<S> & jsearchdemo.Graph>
		implements Userfunction {

	/**
	 * The specificator builder needed to build the merged graph.
	 */
	protected final SpecificatorBuilder<B, R, S, SG> builder;

	/**
	 * The specificator where the merged graph will be added.
	 */
	protected final JSearchSpecificator<A, B, BG, R, S, SG> specificator;

	/**
	 * This tells whether all the actions in the specification count for the
	 * accumulated cost or just the amount of <strong>different</strong>
	 * actions. It will count duplicated actions if this filed is set to
	 * <code>true</code>. This field is used to calculate the heuristic of
	 * the <code>startNode</code> and the cost of the copied
	 * <code>parentNode</code> so it will belong to the accumulated cost of
	 * the merged specification.
	 */
	private static final boolean COST_ALL_OCCURRENCES_COUNT =
		Boolean.parseBoolean(Dinapter.getProperty("COST_ALL_OCCURRENCES_COUNT"));
	
	private static final boolean ENABLE_SPECIFICATION_GRAPH_DISPLAY =
		Dinapter.getProperty(Dinapter.ENABLE_SPECIFICATION_GRAPH_DISPLAY).equalsIgnoreCase("true");

	private Rete engine = null;

	private static final Logger log = Logger
			.getLogger(MergeGraphsUserfunction.class);

	/** Creates a new instance of MergeGraphsUserfunction */
	public MergeGraphsUserfunction(
			JSearchSpecificator<A, B, BG, R, S, SG> specificator,
			SpecificatorBuilder<B, R, S, SG> builder) {
		this.specificator = specificator;
		this.builder = builder;
	}

	public String getName() {
		return "mergeGraphs";
	}

	@SuppressWarnings("unchecked")
	public Value call(ValueVector vv, Context context) throws JessException {
		this.engine = context.getEngine();
		S mergedSpecification, parent, otherParent;
		parent = (S) vv.get(1).javaObjectValue(context);
		otherParent = (S) vv.get(2).javaObjectValue(context);
		mergedSpecification = (S) vv.get(3).javaObjectValue(context);
		return new Value(mergeGraphs(parent, otherParent, mergedSpecification));
	}

	/**
	 * Delegate method which actually generates a new specificator graph which
	 * merges a couple of specifications.
	 * 
	 * @param parent
	 *            One of the specifications to merge.
	 * @param mergedSpecification
	 *            Already merged specification.
	 * @return Newly created specificator graph containing the merge result.
	 * @throws JessException
	 *             Exception thrown by Jess. Problem with the underline engine.
	 */
	protected SG mergeGraphs(S parent, S otherParent, S mergedSpecification)
			throws JessException {
		// * Created new graph *
		SG graph = builder.createNewGraph();
		builder.setGraph(graph);

		// * Start node of the newly created graph *
		graph.setStartNode(builder.createSpecification());
		/* graph.getStartNode().setHeuristic(parent.getHeuristic()+parent.getAcumulatedCost());
		// Same as parent Specification. Moved some lines below */
		graph.getStartNode().setChildrenReady(true);
		graph.getStartNode().setCopiedSpecification(true); // No heuristic or
															// cost change
															// allowed.

		// @todo These cost calculations are very dependent of the heuristic and
		// cost calculations made by Jess. It shouldn't.

		// * Cost calculations *
		// The cost of every action included (minus one because of the
		// intermediary step).
		int cost = 0;
		if (COST_ALL_OCCURRENCES_COUNT)
			cost = mergedSpecification.getActionsCount()
					- parent.getActionsCount();
		else
			cost = mergedSpecification.getActions().length
					- parent.getActions().length;
		cost -= 1; // -1 because of next step to real merged specification.
		cost *= engine.eval("?*INITIAL_COST*").intValue(
				engine.getGlobalContext()); // - initialization cost (or step
											// cost).
		cost += parent.getAcumulatedCost();
		// Plus the heuristic from merged rules later added to the merged
		// specification cost.
		for (R rule : mergedSpecification.getRules()) {
			if (!parent.getRules().contains(rule))
				cost += rule.getHeuristic();
			if (rule.getHeuristic() == DefaultRule.NO_HEURISTIC)
				log.warn("Rule without heuristic in merged specification:\n"+mergedSpecification);
		}
		
		/* Following heuristic calculation may not be accurate so we warn it.
		 * mergeSpecification.getHeuristic could be not calculated or incomplete. */
		if (mergedSpecification.getWorkingRule().getHeuristic() == DefaultRule.NO_HEURISTIC)
			log.warn("Rule without heuristic in merged specification:\n"
					+ mergedSpecification);
		
		// * Heuristic of the start node as close as possible to merged specification f value *
		/* Parent's cost, transition from the parent to the merged, remaining
		 * actions and new working rule heuristic. */
		// @todo This should be specification.getWorkingRule().getHeuristic() but it's used only when it's already calculated.
		graph
				.getStartNode()
				.setHeuristic(
						cost
								+ engine.eval("?*INITIAL_COST*").intValue(
										engine.getGlobalContext())
								+ (engine.eval("?*ACTIONS_TO_ADAPT*").intValue(
										engine.getGlobalContext()) - mergedSpecification
										.getActions().length)
								* engine.eval(
										"?*REMAINING_ACTIONS_PENALIZATION*")
										.intValue(engine.getGlobalContext())
								+ (mergedSpecification.getWorkingRule()
										.getHeuristic() != DefaultRule.NO_HEURISTIC ? mergedSpecification
										.getWorkingRule().getHeuristic()
										: parent.getWorkingRule()
												.getHeuristic()));
		graph.getStartNode().setCost(0);
		graph.getStartNode().setCostReady(true);
		// * Copied parent node *
		S copiedParent = builder.copySpecification(parent);
		builder.link(graph.getStartNode(), copiedParent);
		copiedParent.setCost(cost);
		// Old heuristic is not valid anymore. Overriden by the start node heuristic.
		copiedParent.setHeuristic(0);
		copiedParent.setAcumulatedCost(cost);
		copiedParent.setChildrenNeeded(false);
		copiedParent.setChildrenReady(true);
		copiedParent.setCostReady(true);

		if (log.isDebugEnabled())
			log.debug("%%%%%%%%% Merging (c=" + cost + ";h="
					+ graph.getStartNode().getHeuristic() + ") %%%%%%%%\n"
					+ parent + "\n---------- with ----------\n" + otherParent
					+ "\n__________ result: ________\n" + mergedSpecification);

		// * Link and add new node *
		builder.link(copiedParent, mergedSpecification);
		// -- This adds the other parent as parent specification (just for the records). --
		if (ENABLE_SPECIFICATION_GRAPH_DISPLAY) {
			S copiedOtherPareht = builder.copySpecification(otherParent);
			// We avoid to interfere with the cost calculations.
			copiedOtherPareht.setCuttedSpecification(true);
			builder.link(copiedOtherPareht, mergedSpecification);
		}
		// ================================================================================
		specificator.addSearch(graph, mergedSpecification);

		return graph;
	}
}
