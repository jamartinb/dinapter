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
 * JSearchSpecification.java
 *
 * Created on 29 de mayo de 2007, 12:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dinapter.specificator;

/**
 * <p>
 * This specification class introduces the notion of the A* algorithm. In particular the
 * values of <it>heuristic</it> and <it>cost</it>.
 * </p>
 * <h2>Cost</h2>
 * In a graph, the cost is how hard is to go one edge further. In this case the cost is
 * stored in the specifications (nodes) and, the sum of them to get from the start to the
 * specification is stored in the {@link #getAcumulatedCost() accumulated cost}.
 * <h2>Heuristic</h2>
 * Is a measure of how far we think the solution might be from the current specification.
 * In order for A* to be optimal this heuristic must be <it>optimistic</it> in his
 * guess.
 * @author José Antonio Martín Baena
 */
public interface JSearchSpecification<R extends Rule> extends Specification<R>, jsearchdemo.Node, jsearchdemo.HeuristicNode {
	
    /**
     * Event id for heuristic changes.
     */
	public static final String HEURISTIC_CHANGE = "heuristic";
    
    /**
     * Event id for cost changes.
     */
	public static final String COST_CHANGE = "cost";

	/**
	 * Initial value when no heuristic has been set.
	 */
	public static final int NO_HEURISTIC = -1;

	/**
	 * Initial value when no cost has been set.
	 */
	public static final int NO_COST = NO_HEURISTIC;

	/**
	 * Default heuristic in newly instantiated specifications.
	 */
	public static final int DEFAULT_HEURISTIC = NO_HEURISTIC;

	/**
	 * Default cost in newly instantiated specifications.
	 */
	public static final int DEFAULT_COST = NO_COST;

	/**
	 * Event id for solution status changes.
	 */
	public static final String SOLUTION_STATUS_CHANGED = "solution";

	/**
	 * Event id for best solution status changes.
	 */
	public static final String BEST_SOLUTION_STATUS_CHANGED = "bestSolution";

	/**
	 * Event id for acumulated cost changes.
	 */
	public static final String ACUMULATED_COST_CHANGED = "acumulatedCost";

	/**
	 * Event id for cost ready status changes.
	 */
	public static final String COST_READY_CHANGED = "costReady";
	
	/**
	 * It returns the cost to get to this specification.
	 * @see #getCost()
	 * @return The accumulated cost to get to this specification.
	 */
    int getAcumulatedCost();

    /**
     * It returns the cost of this Specification.
     * 
     * @return The cost of this Specification
     */
    int getCost();

    /**
     * It sets the cost accumulated to get to this specification
     * @param cost the accumulated cost to get to this specification.
     * @see #getCost()
     */
    void setAcumulatedCost(int cost);

    void setCost(int cost);
    
    public boolean isSolution();
    
    public boolean isBestSolution();
    
    public void setSolution(boolean solution);
    
    public void setBestSolution(boolean bestSolution);
    
    public boolean isCostReady();
    
    public void setCostReady(boolean costReady);
}
