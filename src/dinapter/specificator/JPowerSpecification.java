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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import net.sourceforge.jpowergraph.DefaultNode;

import org.apache.log4j.Logger;

import dinapter.graph.JPowerGraph;

/**
 * This specification is intended to be displayed using a {@link JPowerGraph} and 
 * most of its functionality is ported from {@link SimpleSpecification}. It also
 * contains <b>paths</b>, which are the sequence of actions that lead to this 
 * specification; <b><i>merge status</i></b>, which represents whether this
 * specification has been generated from other two or by its own; <b>solution</b>
 * and <b>best solution</b> status, which marks this specification to solve
 * a particular adaptation; and <b>acumulated cost</b>, which is the cost
 * acumulated till this specification.
 * <p>
 * An important difference between this class and {@link SimpleSpecification} is
 * that it provides cost synchronization mechanisms with another new flag: <b>cost
 * ready</b>. It meaning is similar to {@link #isChildrenReady()} but applied to
 * cost calculations.
 * <p>
 * Other new concepts are <b>cut</b> and <b>copied</b> specifications. The former
 * is if this specification has been <i>disabled</i> because of another <i>equivalent</i>
 * specification. The later means that this specification has been copied from another one.
 * @see SimpleSpecification
 * @author José Antonio Martín Baena
 * @version $Revision: 451 $ - $Date: 2007-02-06 10:52:47 +0100 (mar, 06 feb 2007) $
 */
public class JPowerSpecification<R extends Rule> 
	extends DefaultNode 
	// It does implement HeuristicNode and Node altogether because of the weird design of jsearchdemo
	implements JSearchSpecification<R> {
	
    /**
     * This makes paths to be appended to {@link #toString()} output.
     */
	protected static boolean SHOW_PATHS = false;
	
	protected static boolean SHOW_HEADER = false;
	
    private static final Logger log = Logger.getLogger(JPowerSpecification.class);
	
	private static final long serialVersionUID = -754761135388056557L;
	// @toreview DefaultSpecification extends DefaultGraphCell which is innecesary.
	private final SimpleSpecification<R> innerSpecification;
	
    /**
     * This supports property change events.
     */
    protected final PropertyChangeSupport eventSupport = new PropertyChangeSupport(this);
	//private static final Logger log = Logger.getLogger(JPowerSpecification.class);
	
	private boolean merged = false;
	private int acumulatedCost = NO_COST;
	private boolean bestSolution = false;
	private boolean solution = false;
	private boolean copiedSpecification = false;
	private boolean cuttedSpecification = false;
    private boolean costReady = false;
	private Object [] leftPath = new Object[0];
	private Object [] rightPath = new Object[0];
	private final StringBuffer toolTipLog = new StringBuffer("");
	
    /**
     * It instantiates this class with the given collection of rules.
     * @param rules Rules which compose this specification.
     * @param workingRule the working rule of this specification.
     */
	public JPowerSpecification(Collection<R> rules, R workingRule) {
		innerSpecification = new SimpleSpecification<R>(rules,workingRule);
		innerSpecification.eventSupport.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				eventSupport.firePropertyChange(evt.getPropertyName(),evt.getOldValue(),evt.getNewValue());
			}
		});
	}

	/**
     * It instantiates this class with the given collection of rules.
     * @param rules Rules which compose this specification.
     */
	@SuppressWarnings("deprecation")
	public JPowerSpecification(Collection<R> rules) {
		innerSpecification = new SimpleSpecification<R>(rules);
		innerSpecification.eventSupport.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				eventSupport.firePropertyChange(evt.getPropertyName(),evt.getOldValue(),evt.getNewValue());
			}
		});
	}

	public int compareTo(Object arg0) {
		return innerSpecification.compareTo(arg0);
	}
	
	public int getHeuristic() {
		return innerSpecification.getHeuristic();
	}

	public Collection<R> getRules() {
		return innerSpecification.getRules();
	}
	
	/**
     * It return's an array of rules. It's a convenience method.
	 * @return Array of rules.
	 * @see dinapter.specificator.SimpleSpecification#getRules()
	 */
	public Object [] getRulesArray() {
		return innerSpecification.getRules().toArray();
	}

	/**
     * It sets the heuristic of this specification.
	 * @param h Heuristic to be set.
	 * @see dinapter.specificator.SimpleSpecification#setHeuristic(int)
	 */
	public void setHeuristic(int h) {
		innerSpecification.setHeuristic(h);
	}

	public int getCost() {
        return innerSpecification.getCost();
	}

	/**
     * It sets the cost of this specification.
	 * @param cost Cost to be set.
	 * @see dinapter.specificator.SimpleSpecification#setCost(int)
	 */
	public void setCost(int cost) {
		innerSpecification.setCost(cost);
	}

    /**
     * It generates a textual representation of this specification. It uses
     * several flags to display each one of this class properties:
     * <ul>
     * <li><code>S</code> - This specification is a <b>best solution</b>.
     * <li><code>s</code> - This specification is a <b>solution</b>.
     * <li><code>c</code> - This specification is a <b>copy</b> of another.
     * <li><code>x</code> - This specification has been <b>cut</b>.
     * <li><code>n</code> - This specification <b>needs children</b>.
     * <li><code>R</code> - This specification hasn't it children generated.
     * <li><code>m</code> - This specification is a <b>merge</b> result.
     * </ul>
     * Action paths may be appended too if {@link #SHOW_PATHS} is <code>true</code>.
     * @return Textual representation of the specification.
     */
	public String toString() {
		String toReturn = "";
		if (SHOW_HEADER) {
			if (isBestSolution())
				toReturn += "S";
			else if (isSolution())
				toReturn += "s";
			if (!isChildrenReady())
				toReturn += "R";
			if (isMerged())
				toReturn += "m";
			if (isCopiedSpecification())
				toReturn += "c";
			if (isCuttedSpecification())
				toReturn += "x";
			if (isChildrenNeeded())
				toReturn += "n";
			toReturn += " "+hashCode()+(getRules().isEmpty()?"":"/"+getRules().hashCode())+"\n";
			toReturn += (getAcumulatedCost() >= 0)?"("+getAcumulatedCost()+")":"";
		}
		toReturn += innerSpecification.toString();
		if (SHOW_PATHS) {
			if (getLeftPath().length > 0) {
				toReturn += "\n"+pathToString(true);
			}
			if (getRightPath().length > 0) {
				toReturn += "\n"+pathToString(false);
			}
		}
		return toReturn;
	}
	
    /**
     * It returns a String representing one of the two paths
     * of actions.
     * @param left If it's <code>true</code> it returns the
     * left path representation, the right path representation otherwise.
     * @return A string representation of one of the two paths.
     */
	public String pathToString(boolean left) {
		Object [] path = left?getLeftPath():getRightPath();
		String toReturn = "";
		for (int i= 0; i < path.length; i++)
			toReturn += path[i].toString()+"->";
		return toReturn;
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.jpowergraph.DefaultNode#getNodeType()
	 */
	@Override
	public String getNodeType() {
		return "Specification";
	}

	/**
     * It returns whether children are needed or not.
	 * @return Whether children are needed.
	 * @see dinapter.specificator.SimpleSpecification#isChildrenNeeded()
	 */
	synchronized public boolean isChildrenNeeded() {
		return innerSpecification.isChildrenNeeded();
	}

	/**
     * It sets a new children needed status.
	 * @param childrenNeeded The new children needed status.
	 * @see dinapter.specificator.SimpleSpecification#setChildrenNeeded(boolean)
	 */
	synchronized public void setChildrenNeeded(boolean childrenNeeded) {
		innerSpecification.setChildrenNeeded(childrenNeeded);
	}

	public R getWorkingRule() {
		return innerSpecification.getWorkingRule();
	}

    /**
     * It adds a generic property change listener.
     * @param listener Property change listener to add.
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
		eventSupport.addPropertyChangeListener(listener);
	}

    /**
     * It adds a property change listener to the given property.
     * @param propertyName Property to be listened to.
     * @param listener Listener to be added.
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		eventSupport.addPropertyChangeListener(propertyName, listener);
	}

    /**
     * It removes a property change listener.
     * @param listener Listener to be removed.
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
		eventSupport.removePropertyChangeListener(listener);
	}

    /**
     * It removes a property change listener from a single property.
     * @param propertyName Property the listener is going to be removed from.
     * @param listener Listener to be removed.
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		eventSupport.removePropertyChangeListener(propertyName, listener);
	}

	/**
     * It returns the children ready status.
	 * @return The children ready status.
	 * @see dinapter.specificator.SimpleSpecification#isChildrenReady()
	 */
	synchronized public boolean isChildrenReady() {
		return innerSpecification.isChildrenReady();
	}

	/**
     * It sets a new children ready status.
	 * @param childrenReady The new children ready status.
	 * @see dinapter.specificator.SimpleSpecification#setChildrenReady(boolean)
	 */
	synchronized public void setChildrenReady(boolean childrenReady) {
		if (isChildrenNeeded() && childrenReady)
			throw new RuntimeException("You cannot flag a Specification ready without setting its children need off.");
		innerSpecification.setChildrenReady(childrenReady);
		this.notifyAll();
	}
	
	/**
	 * It sets a new children ready status depending on whether this 
     * specification is children needed or not.
	 * @param childrenReady New children ready status.
	 * @deprecated Use discourage because of it intrinsic indeterministic behavior.
	 */
	@Deprecated
	synchronized public void setChildrenReadyIfPossible(boolean childrenReady) {
		if (!(isChildrenNeeded() && childrenReady))
			setChildrenReady(childrenReady);
		else
			log.error("We couldn't flag this Specification childrenReady ("+hashCode()+")");
	}

	/**
     * It returns the copied status.
	 * @return The copied status.
	 */
	public boolean isCopiedSpecification() {
		return copiedSpecification;
	}

	/**
     * It sets the copied status.
	 * @param copiedSpecification New copied status.
	 */
	public void setCopiedSpecification(boolean copiedSpecification) {
		boolean old = this.copiedSpecification;
		this.copiedSpecification = copiedSpecification;
		eventSupport.firePropertyChange("copiedSpecification", old, copiedSpecification);
	}

	/**
     * It returns the cut status.
	 * @return The cut status.
	 */
	public boolean isCuttedSpecification() {
		return cuttedSpecification;
	}

	/**
     * It sets the cut status.
	 * @param cuttedSpecification The new cut status.
	 */
	public void setCuttedSpecification(boolean cuttedSpecification) {
		boolean old = this.cuttedSpecification;
		this.cuttedSpecification = cuttedSpecification;
		eventSupport.firePropertyChange("cuttedSpecification", old, cuttedSpecification);
	}

	/**
     * It returns the left path of this specification.
	 * @return The left path of this specification.
	 */
	public Object[] getLeftPath() {
		// ESCA-JAVA0259:
		return leftPath;
	}

	/**
     * It sets the left path of this specification.
	 * @param path The new left path to set.
	 */
	public void setLeftPath(Object[] path) {
		for (Object o:path)
			if (o == null)
				throw new IllegalArgumentException("Path elements cannot be null");
		Object [] old = this.leftPath;
		// ESCA-JAVA0256:
		this.leftPath = path;
		eventSupport.firePropertyChange(LEFT_PATH_CHANGED, old, path);
	}

	/**
     * It returns the right path of this specification.
	 * @return The right path of this specification.
	 */
	public Object[] getRightPath() {
		// ESCA-JAVA0259:
		return rightPath;
	}

	/**
     * It sets the right path of this specification.
	 * @param path New right path of the specification.
	 */
	public void setRightPath(Object[] path) {
		for (Object o:path)
			if (o == null)
				throw new IllegalArgumentException("Path elements cannot be null");
		Object [] old = this.rightPath;
		// ESCA-JAVA0256:
		this.rightPath = path;
		eventSupport.firePropertyChange(RIGHT_PATH_CHANGED, old, path);
	}

	/**
     * It returns whether this specification is a solution or not.
	 * @return Whether this specification is a solution.
	 */
	public boolean isSolution() {
		return solution;
	}

	/**
     * It sets if this specification is a sonlution.
	 * @param solution Whether this specification is a solution.
	 */
	public void setSolution(boolean solution) {
		boolean old = this.solution;
		this.solution = solution;
		eventSupport.firePropertyChange(SOLUTION_STATUS_CHANGED, old, solution);
	}

	/**
     * It returns whether this specification is a <i>best solution</i>.
	 * @return Whether this specification is a <i>best solution</i>.
	 */
	public boolean isBestSolution() {
		return bestSolution;
	}

	/**
     * It sets the <i>best solution</i> status.
	 * @param bestSolution New <i>best solution</i> status.
	 */
	public void setBestSolution(boolean bestSolution) {
		boolean old = this.bestSolution;
		this.bestSolution = bestSolution;
		eventSupport.firePropertyChange(BEST_SOLUTION_STATUS_CHANGED, old, bestSolution);
	}

	/**
     * It returns the acumulated cost of this specification.
	 * @return The acumulated cost
	 */
	public int getAcumulatedCost() {
		return acumulatedCost;
	}

	/**
     * It sets the acumulated cost of this specification.
	 * @param acumulatedCost The acumulated cost to set
	 */
	public void setAcumulatedCost(int acumulatedCost) {
		int old = this.acumulatedCost;
		this.acumulatedCost = acumulatedCost;
		log("- Acumulated cost: "+old+" -> "+acumulatedCost);
		eventSupport.firePropertyChange(ACUMULATED_COST_CHANGED, old, acumulatedCost);
	}
	
	/**
     * It returns some log information about what have been done on the specification. 
     * @return Specification log information.
	 */
    synchronized public String getToolTipLog() {
		return toolTipLog.toString();
	}
	
	/**
     * It adds a line of log information to this specification. 
     * @param logLine A new line to log.
	 */
	synchronized public void log(String logLine) {
		toolTipLog.append(logLine+"<br>");
	}

    /**
     * It returns whether this specification is a merge result or not.
     * @return Whether this specification is merge result.
     */
	public boolean isMerged() {
		return merged;
	}

    /**
     * It sets whether this specification comes from a merge.
     * @param merged New merge status.
     */
	public void setMerged(boolean merged) {
		boolean old = this.merged;
		this.merged = merged;
		eventSupport.firePropertyChange(MERGE_STATUS, old, merged);
	}
    
    /**
     * Convenience method for getting an array of all actions (whithout repetition)
     * within the specification rules.
     * @return All action whithin the specification rules.
     */
    @SuppressWarnings("unchecked")
    public Object [] getActions() { // Must be sorted
        Set toReturn = new HashSet();
        for (R rule:getRules()) {
            toReturn.addAll(rule.getLeftSide());
            toReturn.addAll(rule.getRightSide());
        }
        return toReturn.toArray();
    }

    /**
     * It returns <code>true</code> if the cost has beel already calculated,
     * <code>false</code> otherwise.
     * @return <code>true</code> if the cost has beel already calculated, <code>false</code> otherwise
     */
    synchronized public boolean isCostReady() {
        return costReady;
    }

    /**
     * It sets the cost ready property.
     * @param costReady New cost ready value.
     */
    synchronized public void setCostReady(boolean costReady) {
        final boolean old = this.costReady; 
        this.costReady = costReady;
        eventSupport.firePropertyChange(COST_READY_CHANGED, old, costReady);
        notifyAll();
    }

	public void setWorkingRule(R rule) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getActionsCount() {
		int count = 0;
		for (R rule:getRules())
			count += rule.getLeftSide().size() + rule.getRightSide().size();
		return count;
	}

	@Override
	public <S extends Specification> boolean isEquivalent(S spec) {
		throw new UnsupportedOperationException();
	}
}
