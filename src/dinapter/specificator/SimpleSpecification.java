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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

/**
 * This default implementation of {@link JSearchSpecification} provides event support and
 * some additional flags to allow further synchronization like {@link #childrenNeeded}
 * and {@link #childrenReady}. It is supposed that specifications are not fully generated
 * so these flags represents the current status of the specification and its direct children.
 * An specification is <i>children ready</i> when all its direct children have been
 * generated and are accesible through an {@link SpecificatorGraph}. But before to be
 * <i>children ready</i> it must be flaged to be needed. This is done with
 * <i>children needed</i>. <u>NO children will be generated when they aren't needed</u>.
 * <p>
 * This class implements HeuristicNode and Node because they aren't relatives in the
 * original implementation of jsearchdemo.
 * <p>
 * This class is intended as an low-footprint version of {@link JPowerSpecification}. 
 *
 * @author José Antonio Martín Baena
 * @version $Revision$ - $Date: 2009-01-09 12:18:01 +0100 (vie, 09 jan 2009) $
 */
public class SimpleSpecification<R extends Rule> implements JSearchSpecification<R> {
	
	private static final Logger log = Logger.getLogger(SimpleSpecification.class);
    
    private static final long serialVersionUID = -3154133945196976151L;
    
    /* ----- Debugging constants ------ */
    private static final boolean SHOW_PATHS = true;
    private static final boolean DISABLE_STRING_CACHE = false;
    private static final boolean SHOW_ACTIONS_SET = false;
    private static final boolean SHOW_LOG = false;
    private static final boolean ENABLE_LOG = false;
    /* -------------------------------- */
    
    /**
     * This supports property change events.
     */
    protected final PropertyChangeSupport eventSupport = new PropertyChangeSupport(this);
    
    private List<List<Object>> additionalLeftPaths = null;
    private List<List<Object>> additionalRightPaths = null;
    private int lastLeftUpdate = 0;
    private int lastRightUpdate = 0;
    private boolean childrenNeeded = false;
    private boolean childrenReady = false;
    private final Collection<R> rules;
    private int heuristic = DEFAULT_HEURISTIC;
    private int cost = DEFAULT_COST;
    private Object[] leftPath = new Object[0];
    private Object[] rightPath = new Object[0];
    private R workingRule = null;
    private boolean copiedSpecification = false;
    private int acumulatedCost = NO_COST;
    private boolean bestSolution = false;
    private boolean solution = false;
    private boolean cuttedSpecification = false;
    private boolean merged = false;
    private boolean costReady = false;
    private StringBuffer toolTipLog = null;
    private static final String EMPTY_STRING = "";
    // @todo This cache should use a weak reference.
    private String toStringCache = null;
    private Object [] actionsCache = null;
    private Object [] rulesCache = null;
    
    /**
     * It instantiates this class whith the given collection of rules.
     * @param rules Rules which compose the specification.
     * @deprecated It's better to use the other constructor.
     */
    @Deprecated
    public SimpleSpecification(Collection<R> rules) {
    	this(rules,((rules instanceof List) && !rules.isEmpty())?((List<R>)rules).get(rules.size()-1):null);
    	if (!rules.isEmpty() && !(rules instanceof List))
    		log.warn("Instantiated a SimpleSpecification with unknown working rule!");
    }
    
    /**
     * It instantiates this class whith the given collection of rules.
     * @param rules Rules which compose the specification.
     * @param workingRule The working rule of the specification.
     */
    public SimpleSpecification(Collection<R> rules, R workingRule) {
		// ESCA-JAVA0256:
		this.rules = rules;
    	if (!rules.isEmpty())
    		setWorkingRule(workingRule);
    	addPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				toStringCache = null;
			}
        });
    }
    
        /* (non-Javadoc)
         * @see dinapter.specificator.JSearchSpecification#getRules()
         */
    public Collection<R> getRules() {
        // ESCA-JAVA0259:
		return rules;
    }
    
        /* (non-Javadoc)
         * @see dinapter.specificator.Rule#getHeuristic()
         */
    public int getHeuristic() {
        return heuristic;
    }
    
    /**
     * It sets the heuristic. It fires a property event change.
     * @param h New heuristic to set.
     */
    public void setHeuristic(int h) {
        int old = heuristic;
        heuristic = h;
        eventSupport.firePropertyChange(HEURISTIC_CHANGE, old, heuristic);
    }
    
        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
    public int compareTo(Object arg0) {
        if (arg0 == null)
            throw new NullPointerException("Argument was null.");
        if (arg0 instanceof JSearchSpecification)
            return getHeuristic() - ((JSearchSpecification)arg0).getHeuristic();
        else
            throw new IllegalArgumentException("Argument must be instance of JSearchSpecification. It actually is instance of "+arg0.getClass().getCanonicalName());
    }
    
    public String toString() {
    	if (toStringCache != null)
    		return toStringCache;
    	StringBuffer toReturn;
    	if (log.isDebugEnabled()) {
	        toReturn = getStringHeader();
	        toReturn.append("["+(getAcumulatedCost() > 0?"f="+(getAcumulatedCost()+getHeuristic())+";ac="+getAcumulatedCost()+";":"")+"h="+getHeuristic()+";c="+getCost()+"]");
    	} else {
    		toReturn = new StringBuffer();
    	}
        Set<String> rules = new TreeSet<String>();
        for (R rule:getRules())
        	rules.add("\n"+(getWorkingRule().equals(rule)&&log.isDebugEnabled()?"*":"")+rule.toString());
        for (String line:rules)
        	toReturn.append(line);
        if (SHOW_PATHS) {
			if (getLeftPath().length > 0) {
				toReturn.append("\n"+pathToString(true));
			}
			if (getRightPath().length > 0) {
				toReturn.append("\n"+pathToString(false));
			}
			updatePaths();
			appendAditionalPaths(toReturn, additionalLeftPaths);
			appendAditionalPaths(toReturn, additionalRightPaths);
		}
        if (SHOW_ACTIONS_SET)
        	toReturn.append("\n"+getStringActionSet());
        if (SHOW_LOG)
        	toReturn.append("\n"+getToolTipLog().replaceAll("<br>", "\n"));
        if (!log.isDebugEnabled() && !rules.isEmpty()) {
        	toReturn.deleteCharAt(0);
        }
        if (!DISABLE_STRING_CACHE)
        	return toReturn.toString();
        else
        	return toStringCache = toReturn.toString();
    }
    
    private void appendAditionalPaths(StringBuffer buffer, List<List<Object>> additionalPaths) {
    	if (additionalPaths == null) return;
    	for (List<Object> path:additionalPaths) {
    		buffer.append("\n");
    		for (Object activity:path) {
    			buffer.append(activity.toString()+"->");
    		}
    	}
    }
    
    protected StringBuffer getStringHeader() {
    	StringBuffer toReturn = new StringBuffer("");
		if (isBestSolution())
			toReturn.append("S");
		else if (isSolution())
			toReturn.append("s");
		if (isCopiedSpecification())
			toReturn.append("c");
		if (!isCostReady())
			toReturn.append("C");
		if (isCuttedSpecification())
			toReturn.append("x");
		if (isChildrenNeeded())
			toReturn.append("n");
		if (!isChildrenReady())
			toReturn.append("R");
		if (isMerged())
			toReturn.append("m");
		toReturn.append(" "+hashCode()+(getRules().isEmpty()?"":"/"+getRules().hashCode())+"\n");
		return toReturn;
    }
    
    protected String getStringActionSet() {
    	if (getActions().length == 0)
    		return "{}";
    	String toReturn = "{";
    	for (Object action:getActions())
    		toReturn += action+", ";
    	toReturn = toReturn.substring(0, toReturn.length()-2);
    	return toReturn+"}";
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
    
    @SuppressWarnings("unchecked")
    public int getCost() {
    	return cost;
    }
    
    /**
     * It sets the cost. It fires a property changed event.
     * @param cost The new cost to set.
     */
    public void setCost(int cost) {
        int old = this.cost;
        this.cost = cost;
        eventSupport.firePropertyChange(COST_CHANGE, old, cost);
    }
    
    /**
     * It returns whether this specification needs its children
     * to be generated or not.
     * @return Whether children generation is needed.
     */
    synchronized public boolean isChildrenNeeded() {
        return childrenNeeded && !childrenReady;
    }
    
    /**
     * It sets whether this specification needs its children to
     * be generated or not. <b>An specification must be not children
     * ready before needed them.</b>
     * @param childrenNeeded Whether children are needed or not.
     */
    synchronized public void setChildrenNeeded(boolean childrenNeeded) {
        boolean old = this.childrenNeeded;
        this.childrenNeeded = childrenNeeded;
        eventSupport.firePropertyChange(CHILDREN_NEEDED_CHANGE, old, childrenNeeded);
    }
    
    public R getWorkingRule() {
        return workingRule;
    }
    
    public void setWorkingRule(R rule) {
    	if ((rule == null) || !getRules().contains(rule))
    		throw new IllegalArgumentException("The given working rule ("+rule+") is not contained by this Specification.");
    	R old = workingRule;
        workingRule = rule;
        eventSupport.firePropertyChange(WORKING_RULE_CHANGED,old,workingRule);
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
     * @see dinapter.specificator.DefaultSpecification#isChildrenReady()
     */
    synchronized public boolean isChildrenReady() {
        return childrenReady;
    }
    
    /**
     * It sets a new children ready status.
     * @param childrenReady The new children ready status.
     * @see dinapter.specificator.DefaultSpecification#setChildrenReady(boolean)
     */
    synchronized public void setChildrenReady(boolean childrenReady) {
    	boolean old = this.childrenReady;
        if (isChildrenNeeded() && childrenReady)
            throw new RuntimeException("You cannot flag a Specification ready without setting its children need off.");
        this.childrenReady = childrenReady;
        eventSupport.firePropertyChange(CHILDREN_READY_CHANGE, old, childrenReady);
        this.notifyAll();
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
     * @param rightPath New right path of the specification.
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
        eventSupport.firePropertyChange(ACUMULATED_COST_CHANGED, old, acumulatedCost);
    }
    
    /**
     * Convenience method for getting an array of all actions (whithout repetition)
     * within the specification rules.
     * @return All action whithin the specification rules.
     */
    @SuppressWarnings("unchecked")
	public Object [] getActions() { // Must be sorted // Why must it be sorted?
    	if (actionsCache == null) {
	        Set toReturn = new HashSet();
	        for (R rule:getRules()) {
	            toReturn.addAll(rule.getLeftSide());
	            toReturn.addAll(rule.getRightSide());
	        }
	        actionsCache = toReturn.toArray();
    	}
    	// ESCA-JAVA0259:
		return actionsCache;
    }
    
    public boolean isBestSolution() {
        return bestSolution;
    }
    
    public void setBestSolution(boolean bestSolution) {
    	boolean old = this.bestSolution;
        this.bestSolution = bestSolution;
        if (bestSolution && !old && !isSolution()) {
        	setSolution(true);
        }
        eventSupport.firePropertyChange(BEST_SOLUTION_STATUS_CHANGED, old, bestSolution);
    }
    
    public boolean isSolution() {
        return solution;
    }
    
    public void setSolution(boolean solution) {
    	boolean old = this.solution;
        this.solution = solution;
        if (old && !solution && isBestSolution()) {
        	setBestSolution(false);
        }
        eventSupport.firePropertyChange(SOLUTION_STATUS_CHANGED,old,solution);
    }
    
    /**
     * It returns an array with the rules. This array should not be modified.
     * @return An array with the rules.
     */
    public Object [] getRulesArray() {
    	if (rulesCache == null)
    		rulesCache = getRules().toArray();
        // ESCA-JAVA0259:
		return rulesCache;
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
     * It returns <code>true</code> if the cost has been already calculated,
     * <code>false</code> otherwise.
     * @return <code>true</code> if the cost has been already calculated, <code>false</code> otherwise
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
    
    
    /**
     * It returns some log information about what have been done on the specification.
     * @return Specification log information.
     */
    synchronized public String getToolTipLog() {
    	if (toolTipLog == null)
    		return EMPTY_STRING;
    	else
    		return toolTipLog.toString();
    }
    
    /**
     * It adds a line of log information to this specification.
     * @param logLine A new line to log.
     */
    synchronized public void log(String logLine) {
    	if (!ENABLE_LOG)
    		return;
    	if (toolTipLog == null)
    		toolTipLog = new StringBuffer(EMPTY_STRING);
        toolTipLog.append(logLine+"<br>");
        if (SHOW_LOG)
        	toStringCache = null;
    }
    
    /**
     * It returns the count all the actions (even when they're repeated).
     * @return The count all the actions (even when they're repeated).
     */
    public int getActionsCount() {
    	int count = 0;
    	for (R rule:getRules())
    		count += rule.getLeftSide().size() + rule.getRightSide().size();
    	return count;
    }

	@Override
	public <S extends Specification> boolean isEquivalent(S spec) {
        return (((getWorkingRule() == null)
        				&& (spec.getWorkingRule() == null))
        			|| ((getWorkingRule() != null)
        				&& getWorkingRule().equals(spec.getWorkingRule())))
        		&& getRules().equals(spec.getRules());
	}
	
	public void addLeftPath(Object [] path) {
		updateLeftPaths();
		ArrayList<Object> toAdd = new ArrayList<Object>(Arrays.asList(path));
		if (toAdd.equals(Arrays.asList(getLeftPath())))
			return;
		if (additionalLeftPaths == null) {
			additionalLeftPaths = new ArrayList<List<Object>>(1);
		}
		if (additionalLeftPaths.contains(toAdd))
			return;
		additionalLeftPaths.add(toAdd);
	}
	
	public void addRightPath(Object [] path) {
		updateRightPaths();
		ArrayList<Object> toAdd = new ArrayList<Object>(Arrays.asList(path));
		if (toAdd.equals(Arrays.asList(getRightPath())))
			return;
		if (additionalRightPaths == null) {
			additionalRightPaths = new ArrayList<List<Object>>(1);
		}
		if (additionalRightPaths.contains(toAdd))
			return;
		additionalRightPaths.add(toAdd);
	}
	
	private void updatePath(int position, List<List<Object>> additionalPaths, Object [] updatedPath) {
		if (additionalPaths == null) return;
		if (updatedPath.length <= position) {
			// No update is needed.
			return;
		}
		List<Object> toAdd = Arrays.asList(updatedPath).subList(position, updatedPath.length);
		for (List<Object> additionalPath:additionalPaths) {
			additionalPath.addAll(toAdd);
		}
	}
	
	/**
	 * It updates both additional left and right paths. Unlike leftPath and rightPath, additional paths are not updated constantly,
	 * therefore, this method must be called before using them.
	 * 
	 * @see updateLeftPaths
	 * @see updateRightPaths
	 */
	protected void updatePaths() {	
		updateLeftPaths();
		updateRightPaths();
	}

	/**
	 * It updates the right additional paths.
	 */
	protected void updateRightPaths() {
		updatePath(lastRightUpdate,additionalRightPaths, getRightPath());
		lastRightUpdate = getRightPath().length;
	}

	/**
	 * It updates the left additional paths.
	 */
	protected void updateLeftPaths() {
		updatePath(lastLeftUpdate, additionalLeftPaths, getLeftPath());
		lastLeftUpdate = getLeftPath().length;
	}
	
	private Object[][] getAllPaths(boolean left, boolean includeMainPath, Object [] path, List<List<Object>> additionalPaths) {
		Object [][] toReturn;
		if (left) {
			updateLeftPaths();
		} else {
			updateRightPaths();
		}
		if (additionalPaths == null) {
			if (includeMainPath) {
				toReturn = new Object[1][];
				toReturn[0] = path;
			} else {
				toReturn = new Object[0][];
			}
			return toReturn;
		}
		int size = additionalPaths.size();
		if (includeMainPath) {
			size++;
		}
		toReturn = new Object[size][];
		int i = 0;
		if (includeMainPath) {
			toReturn[0] = path;
			i++;
		}
		for (List<Object> additionalPath:additionalPaths) {
			toReturn[i] = additionalPath.toArray();
			i++;
		}
		return toReturn;
	}
	
	/**
	 * It returns all the left traces that leaded to this specification.
	 */
	public Object[][] getLeftPaths() {
		return getAllPaths(true, true, getLeftPath(), additionalLeftPaths);
	}
	
	/**
	 * It returns all the right traces that leaded to this specification.
	 */
	public Object[][] getRightPaths() {
		return getAllPaths(false, true, getRightPath(), additionalRightPaths);
	}

	/**
	 * It returns all the additional left traces that leaded to this specification.
	 */
	public Object[][] getAdditionalLeftPaths() {
		return getAllPaths(true, false, getLeftPath(), additionalLeftPaths);
	}
	
	/**
	 * It returns all the right traces that leaded to this specification.
	 */
	public Object[][] getAdditionalRightPaths() {
		return getAllPaths(false, false, getRightPath(), additionalRightPaths);
	}
	
	/**
	 * It adds the additional paths of the given specification to this one's additional paths.
	 * 
	 * @param spec The specification whose additional paths are going to be copied.
	 */
	public void addAdditionalPaths(SimpleSpecification spec) {
		/* This could have been done using private methods and members but I've
		 * done this this way because additional paths must be "deep-copied" not
		 * just passed along.
		 */
		updatePaths();
		for (Object [] additionalLeftPath:spec.getLeftPaths()) {
			addLeftPath(additionalLeftPath);
		}
		for (Object [] additionalRightPath:spec.getRightPaths()) {
			addRightPath(additionalRightPath);
		}
	}
}
