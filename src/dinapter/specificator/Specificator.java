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

import dinapter.behavior.BehaviorGraph;

/**
 * This must be implemented by all classes in charge of generate an
 * specification of adaptation between two components with behavioural
 * incompabilities.
 * @author José Antonio Martín Baena
 * @version $Revision: 449 $ - $Date: 2007-02-01 20:05:11 +0100 (jue, 01 feb 2007) $
 */
public interface Specificator<G extends BehaviorGraph, S extends Specification> extends Runnable {
    /**
     * Event id of specification change.
     */
	public static final String SPECIFICATION_CHANGE = "specification";
    
    /**
     * Event id of left component change.
     */
	public static final String LEFT_COMPONENT_CHANGE = "leftComponent";
    
    /**
     * Event id of right component change.
     */
	public static final String RIGHT_COMPONENT_CHANGE = "rightComponent";
    
    /**
     * Event id used when any of the components has been changed.
     */
	public static final String COMPONENTS_CHANGE = "components";
	
    /**
     * It sets the two components with behavior mismatch to adapt.
     * @param leftBehavior Left component.
     * @param rightBehavior Right component.
     */
	public void setComponents(G leftBehavior, G rightBehavior);
    
    /**
     * It returns the solution specification which represents the
     * adaptation to be made between the components.
     * @return The solution specification or <code>null</code> if there is none so far.
     */
	public S getSpecification();
    
    /**
     * It adds a generic property change listener.
     * @param listener Listener to be added.
     */
	public void addPropertyChangeListener(PropertyChangeListener listener);
    
    /**
     * It adds a property change listener of a single property.
     * @param propertyName Property to be listened.
     * @param listener Listener to be added.
     */
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);
    
    /**
     * It removes a generic property change listener.
     * @param listener Property listener to be removed.
     */
	public void removePropertyChangeListener(PropertyChangeListener listener);
    
    /**
     * It removes a property change listener from the given property.
     * @param propertyName Property the listener is going to be removed from.
     * @param listener Listener to be removed.
     */
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);
}	
