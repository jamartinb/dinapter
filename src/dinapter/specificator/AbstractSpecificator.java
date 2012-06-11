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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import dinapter.behavior.BehaviorGraph;

/**
 * This an abstract class which all Specificators may extends. It supports events and 
 * components and solution storage.
 * @author José Antonio Martín Baena
 * @version $Revision: 449 $ - $Date: 2007-02-01 20:05:11 +0100 (jue, 01 feb 2007) $
 */
public abstract class AbstractSpecificator<G extends BehaviorGraph, S extends Specification> 
		implements Specificator<G,S> {
	
    /**
     * Event id for successfully completed property.
     * @see #isSuccessfullyCompleted()
     */
	public static final String SUCCESSFULLY_COMPLETED = "successfullyCompleted";
	
	protected final PropertyChangeSupport eventSupport = new PropertyChangeSupport(this);
	private S finalSpecification = null;
    
    /**
     * Components to be adapted.
     */
	protected G leftComponent, rightComponent = null;
	private boolean successfullyCompleted = false;


	/* (non-Javadoc)
	 * @see dinapter.specificator.Specificator#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		eventSupport.addPropertyChangeListener(listener);
	}

	/* (non-Javadoc)
	 * @see dinapter.specificator.Specificator#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
			eventSupport.addPropertyChangeListener(propertyName, listener);
	}

	/* (non-Javadoc)
	 * @see dinapter.specificator.Specificator#getSpecification()
	 */
	public S getSpecification() {
		return finalSpecification;
	}
	
    /**
     * It sets the solution Specification of the adaptation process.
     * It fires an event notifying this.
     * @param specification Solution specification.
     */
	protected void setSpecification(S specification) {
		S oldValue = finalSpecification;
		finalSpecification = specification;
		eventSupport.firePropertyChange(SPECIFICATION_CHANGE, oldValue, specification);
	}

	/* (non-Javadoc)
	 * @see dinapter.specificator.Specificator#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		eventSupport.removePropertyChangeListener(listener);
	}

	/* (non-Javadoc)
	 * @see dinapter.specificator.Specificator#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		eventSupport.removePropertyChangeListener(propertyName, listener);
	}

	/* (non-Javadoc)
	 * @see dinapter.specificator.Specificator#setComponents(dinapter.behavior.BehaviorGraph, dinapter.behavior.BehaviorGraph)
	 */
	public void setComponents(G leftBehavior, G rightBehavior) {
		G oldLeft = leftComponent;
		G oldRight = rightComponent;
		leftComponent = leftBehavior;
		rightComponent = rightBehavior;
		eventSupport.firePropertyChange(LEFT_COMPONENT_CHANGE, oldLeft, leftComponent);
		eventSupport.firePropertyChange(RIGHT_COMPONENT_CHANGE, oldRight, rightComponent);
		eventSupport.firePropertyChange(COMPONENTS_CHANGE, null, null);
	}

	/**
     * It returns wether the process has been successfully completed
     * or not.
	 * @return The success of the process.
	 */
	public boolean isSuccessfullyCompleted() {
		return successfullyCompleted;
	}

	/**
     * It sets the success status of the process.
	 * @param successfullyCompleted The success status of the process.
	 */
	protected void setSuccessfullyCompleted(boolean successfullyCompleted) {
		boolean old = successfullyCompleted;
		this.successfullyCompleted = successfullyCompleted;
		eventSupport.firePropertyChange(SUCCESSFULLY_COMPLETED,old,successfullyCompleted);
	}
}
