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

import jess.JessException;
import jess.Rete;
import jess.WorkingMemoryMarker;

import org.apache.log4j.Logger;

import dinapter.behavior.BehaviorGraph;

/**
 * This is an Specificator which relies on a Jess engine in order to produce a valid Specification.
 * Jess is an expert system very similar to clip. It depends on a set of rules that define its
 * behavior. This rules are supposed to be in a file that must be loaded through the {@link #init(String)}
 * method.
 * <p>
 * This class implements Runnable in order to be used within a Thread and generally improve
 * its usability.
 * @see <a href="http://herzberg.ca.sandia.gov/jess/">Jess site</a>
 * @author José Antonio Martín Baena
 * @version $Revision: 449 $ - $Date: 2007-02-01 20:05:11 +0100 (jue, 01 feb 2007) $
 */
public abstract class JessSpecificator<G extends BehaviorGraph, S extends Specification>
		extends AbstractSpecificator<G, S> implements Runnable {

	/**
     * Event id used for marker change events. 
	 */
    public static final String MARKER_CHANGE = "marker";
    
    /**
     * Event id used for reset events.
     */
	public static final String RESET_EVENT = "reset";
    
	private static final Logger log = Logger.getLogger(JessSpecificator.class);
    
    /**
     * This is the Jess internal engine.
     */
	protected final Rete engine = new Rete();
    
    /**
     * This marker stores the engine status where reset may roll back to.
     */
	private WorkingMemoryMarker marker = null;
	
    /**
     * It instantiates this class. Afterwards {@link #init(String)} and {@link #run()}
     * are intended to be called.
     */
	public JessSpecificator() {
	}
	
    /**
     * It initialize the instance and its inner Jess engine. It sets a marker for
     * the engine status and rests to it.
     * @param filename File of rules to load.
     * @throws JessException Jess exception thrown.
     */
	protected void init(String filename) throws JessException {
		log.debug("Reseting Rete");
		engine.reset();
		log.debug("Batching file");
		engine.batch(filename);
		log.debug("Setting mark");
		setMarker();
		reset();
	}

	/* (non-Javadoc)
	 * @see dinapter.specificator.Specificator#start()
	 */
	public void run() {
		log.info("Specification process starting...");
		final long start = System.currentTimeMillis();
		try {
			log.debug("Adding components");
			engine.add(leftComponent);
			engine.add(rightComponent);
			engine.addAll(leftComponent.getAllNodes());
			engine.addAll(rightComponent.getAllNodes());
			specifyAdaptor();
			for (int i = 0; i < 3; i++)
				System.gc();
			log.debug("Running engine");
			runEngine();
		} catch (JessException e) {
			setSuccessfullyCompleted(false);
			throw new RuntimeException(e);
		} finally {
			final long end = System.currentTimeMillis();
			double time = end - start;
			final double hours = Math.floor(time / 3600000);
			time = time % 3600000;
			final double minutes = Math.floor(time / 60000);
			time = time %   60000;
			final double seconds  = Math.floor(time / 1000);
			final double miliseconds = time % 1000;
			log.info(String.format("Time spent: %02.0f:%02.0f:%02.0f.%03.0f", hours,minutes,seconds,miliseconds));
		}
	}
	
    /**
     * It actually starts the Jess engine.
     * @throws JessException Problems starting Jess.
     */
	protected void runEngine() throws JessException {
		System.gc();
		engine.run();
	}
	
    /**
     * This method must initialize any other Specificator resources.
     * @throws JessException Jess exception thrown.
     */
	public abstract void specifyAdaptor() throws JessException;

    /** 
     * It resets the Specificator in order to process another
     * adaptation. It rolls back to the Jess marker and it fires
     * an event noticing this.
     * @throws JessException Exception thrown by Jess.
     */
	public void reset() throws JessException {
		log.debug("Reset");
		if (marker == null)
			engine.reset();
		else
			engine.resetToMark(marker);
		// @toreview Reset is not actually a property so this event system is not adecuate.
		eventSupport.firePropertyChange(RESET_EVENT, null, null);
	}
	
    /**
     * It sets the marker which may be used to reset Jess.
     * It fires and event.
     * @see #reset()
     * @see #setMarker()
     * @param marker Jess marker.
     */
	protected void setMarker(WorkingMemoryMarker marker) {
		WorkingMemoryMarker old = marker;
		this.marker = marker;
		eventSupport.firePropertyChange(MARKER_CHANGE,old,marker);
	}
	
    /**
     * It sets the marker with current Jess status.
     */
	protected void setMarker() {
		setMarker(engine.mark());
	}
}
