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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.PriorityBlockingQueue;

import jess.Console;
import jess.Fact;
import jess.Filter;
import jess.JessException;
import jess.QueryResult;
import jess.RU;
import jess.Rete;
import jess.Value;
import jess.ValueVector;
import jsearchdemo.AStarAlgorithm;
import jsearchdemo.HeuristicNode;
import jsearchdemo.Node;
import net.innig.collect.HashMultiMap;
import net.innig.collect.MultiMap;
import net.sourceforge.jpowergraph.Edge;

import org.apache.log4j.Logger;

import data.FTPExample;
import dinapter.Dinapter;
import dinapter.behavior.BehaviorGraph;
import dinapter.behavior.BehaviorNode;
import dinapter.behavior.JPowerBehaviorGraph;
import dinapter.behavior.JPowerBehaviorNode;
import dinapter.specificator.userfunction.LogUserfunction;
import dinapter.specificator.userfunction.MergeGraphsUserfunction;
import dinapter.specificator.userfunction.SplitGraphUserfunction;
import dinapter.specificator.util.SolutionActions;

/**
 * This is the main class of Dinapter project and it centralize the control of the specification process.
 *
 * @author José Antonio Martín Baena
 * @version $Revision: 486 $ - $Date: 2007-03-08 19:37:33 +0100 (jue, 08 mar 2007) $
 */
public class JSearchSpecificator<A,B extends BehaviorNode<A>,BG extends BehaviorGraph<B>,R extends Rule<B> & HeuristicNode,S extends JSearchSpecification<R>,SG extends SpecificatorGraph<S> & jsearchdemo.Graph>
        extends	JessSpecificator<BG,S> {
	
	private static final String RETE_LOGGER_NAME = "dinapter.specificator.ReteLog";

	/**
	 * It includes into the exploration traces the copied specifications.
	 */
	public static final boolean TRACE_COPIED_SPECIFICATION = false;
    
    /**
     * The address of the clip file which contains the rules of the <i>expert system</i>. It is <code>rules/rules.clp</code> by default.
     */
    public static final String DEFAULT_SPECIFICATOR_RULES_FILE = Dinapter.getProperty("RULES_FILE");
    
    /**
     * <p>If this constant is <code>true</code> some optimizations will be applied that might loose some solutions.</p>
     * <p>This is not recommended to be enabled if you want to find solutions in successive iterations.</p>
     */
    public static final boolean LOOSY_OPTIMIZATIONS_ENABLED = Dinapter.getProperty("LOOSY_OPTIMIZATIONS_ENABLED").equalsIgnoreCase("true"); 
    
    /**
     * Safe limit of active graphs during the process by default. It can be modified using {@link #setComponents(JPowerBehaviorGraph, JPowerBehaviorGraph, int)}
     */
    private int searchLimit = 100;
    
    /**
     * This variable contains the cost of the best solution found so far dropping any path with higher f value.
     * It can be used to manually set an initial threshold of f values to consider.
     */
    private int solutionCost = Integer.MAX_VALUE; // @toreview It cuts solutions with f values above that.
    
    /**
     * Maximum f value considered so far. Used for information purposes.
     */
    private int maxFValue = 0;
    
    /**
     * Maximum number of active graphs during the process. Used for comparing against {@link #searchLimit} and
     * in the calculation of the quality of the process.
     */
    private int maxQueuedPaths = -1;
    
    /**
     * Count of every graph created, even if not used or added to Jess.
     */
    private int graphCounter = 0;
    
    /**
     * Count of how many graphs have been spared because of equivalent specifications.
     */
    private int discardedGraphs = 0;
    
    /**
     * This set contains all relevant specifications created out of Jess.
     */
    private final MultiMap<Collection<R>, R> newSpecificationsRules = new HashMultiMap<Collection<R>, R>();
    
    private final SolutionActions<B, BG> solutionActions = new SolutionActions<B, BG>();
    
    // @todo Close rules appropiately on loops.
    // @todo Handle merges finding partial solutions (other branches cut) and merging them.
    
    private static final Logger log = Logger.getLogger(JSearchSpecificator.class);
    private Thread jessThread, jSearchThread = null;
    private JessException innerException = null;
    public final Rete engine;
    
    /**
     * Time between notifications about how many specifications have been explored since the last check.
     */
    private static final int EXPLORED_SPECIFICATION_INTERVAL = 10000; // ms.
    
    /**
     * It contains the queue of specificator graphs to be stepped. It automatically sort them in ascending f value order.
     */
    private final PriorityBlockingQueue<AStarAlgorithm> searchQueue = new PriorityBlockingQueue<AStarAlgorithm>(searchLimit,new Comparator<AStarAlgorithm>(){
        public int compare(AStarAlgorithm o1, AStarAlgorithm o2) {
        	int comparison = AStarAlgorithm.computeFValue(o1.getQueue().getFirst())
        				- AStarAlgorithm.computeFValue(o2.getQueue().getFirst());
        	// This is a tiebreaker based on the graph already within Jess or not.
        	if (comparison == 0)
        		comparison = (engine.containsObject(o1.getGraph())?0:1)
        				- (engine.containsObject(o2.getGraph())?0:1); 
        	// ---------
        	return comparison;
        }
    });
    
    /**
     * Builder used for generating all graphs, specifications and rules. No assumptions can be made about its current
     * graph or working specification.
     */
    protected SpecificatorBuilder<B,R,S,SG> builder;
    
    /**
     * Management class in charge of stepping each one of the A* algorithms (one per specification
     * graph) of the process.
     * @author José Antonio Martín Baena
     * @version $Revision: 486 $ - $Date: 2007-03-08 19:37:33 +0100 (jue, 08 mar 2007) $
     */
    private class JSearchThread extends Thread {
        public JSearchThread() {
            super("JSearchThread");
        }
        
        private boolean canceled = false;
        
        @SuppressWarnings("unchecked")
        public void run() {
        	if (log.isDebugEnabled())
        		log.debug("Starting JSearchThread");
        	/* The following could be set to 0 but it's like
        	 * it is because it can be another search for 
        	 * different solutions.
        	 */
            int activeGraph = searchQueue.size()-1;
            try {
                int fValue = 0;
                while (!Thread.interrupted() && !canceled && !searchQueue.isEmpty()
                &&(activeGraph < getSearchLimit())
                &&((fValue = AStarAlgorithm.computeFValue(searchQueue.peek().getQueue().getFirst())) <= solutionCost)) {
                    if ((fValue > maxFValue) && log.isInfoEnabled())
                        log.info("Looking for solutions with maximum f value ="+(maxFValue = fValue));
                    AStarAlgorithm algorithm = searchQueue.take();
                    if (!engine.containsObject(algorithm.getGraph())) {
                    	/* If the graph to search is not in Jess we add it and include
                    	 * it like an active graph.
                    	 */
                    	if (log.isDebugEnabled())
                    		log.debug("Adding a new graph");
                        activeGraph++;
                        addGraph((SG)algorithm.getGraph());
                    }
                    if (log.isDebugEnabled())
                    	log.debug("Stepping. Active graphs = "+activeGraph);
                    Node exploringNode = algorithm.getQueue().getFirst().getEndNode(); // To show in next log.trace.
                 // We make an step in the search.
                    algorithm.step();
                    if (log.isTraceEnabled() && !(!TRACE_COPIED_SPECIFICATION && (exploringNode instanceof SimpleSpecification) && ((SimpleSpecification)exploringNode).isCopiedSpecification()))
                    	log.trace("Exploring specification (graph="+algorithm.getGraph().hashCode()+"): "+exploringNode+"\n -------------- ");
                    if (!algorithm.finished()) {
                    	/* If the step hasn't found a solution we keep the search graph if it has 
                    	 * a lower value than any solution already found.
                    	 */
                        fValue = AStarAlgorithm.computeFValue(algorithm.getQueue().getFirst());
                        if (!LOOSY_OPTIMIZATIONS_ENABLED || (fValue <= solutionCost)) {
                        	if (log.isTraceEnabled())
                        		log.trace("New graph f value = "+fValue);
                            searchQueue.add(algorithm);
                        } else if (log.isDebugEnabled()) {
                            log.debug("Graph discarded because of its bad f value ("+fValue+")");
                        }
                    } else if (!algorithm.getQueue().isEmpty()) {
                    	// The step has found a solution
                    	activeGraph--;
                        fValue = AStarAlgorithm.computeFValue(algorithm.getQueue().getFirst());
                        /* ---- Some tests here --- */
                        S spec = (S) algorithm.getQueue().getFirst().getEndNode();
                        int calculated, acumulated;
                        if ((calculated = AStarAlgorithm.computeCost(algorithm.getQueue().getFirst())) 
                        		!= (acumulated = spec.getAcumulatedCost()))
                            throw new RuntimeException("Acumulated cost ("+acumulated+") is not equal to calculated cost ("+calculated+")" +
                            		"\n\tSpecification:"+spec);
                        if (fValue != spec.getAcumulatedCost() + spec.getHeuristic())
                            throw new RuntimeException("f value is not as estimated");
                        /* ------------------------ */
                        if (isIgnored(spec)) {
                        	log.info("Found solution previously ignored. Cost="+solutionCost);
                        } else if (fValue < solutionCost) {
                            solutionCost = fValue;
                            if (log.isInfoEnabled())
                            	log.info("Solution found. Cost="+solutionCost);
                            if (log.isTraceEnabled())
                            	log.trace("Solution: "+algorithm.getQueue().getFirst().getEndNode());
                        } else if (log.isInfoEnabled())
                            log.info("Worse solution found. Cost="+fValue);
                    } else {
                    	// The A* has finished because there're no other paths to explore.
                        activeGraph--;
                        if (log.isDebugEnabled())
                        	log.debug("Graph fully processed");
                    }
                    if (activeGraph > maxQueuedPaths)
                        maxQueuedPaths = activeGraph;
                    yield(); // We encourage Jess to respond to new changes.
                }
            } catch (InterruptedException e) {
                // Whatever that must be done has been done within the interrupt method.
                log.warn("The thread "+Thread.currentThread().getName()+" was interrupted!");
            } catch (JessException e) {
                log.fatal("Exception thrown when trying to add a graph to Jess",e);
                innerException = e;
                canceled = true;
            }
            if (searchQueue.size() >= getSearchLimit()) {
                canceled = true;
                log.error(" ----- Search limit reached ("+searchQueue.size()+">="+getSearchLimit()+")");
            }
            setSolution();
            setSuccessfullyCompleted(!canceled);
            if (!canceled)
                log.info("Testing results...");
            try {
                engine.assertString("(process-complete (successfully "+(isSuccessfullyCompleted()?"TRUE":"FALSE")+"))");
                yield(); // Better chances for Jess to process the new asserted fact.
            } catch (JessException e) {
                log.fatal("Exception thrown when halting Rete",e);
                setSuccessfullyCompleted(false);
            }
        }
        
        @Override
        public void interrupt() {
            log.error("Search interrupted!");
            canceled = true;
            super.interrupt();
        }
    }
   
    private class JessThread extends Thread {
        private final Thread mainSearchThread;
        
        public JessThread(String name, Thread searchThread) {
            super("Jess-Thread");
            mainSearchThread = searchThread;
        }
        
        public void run() {
                try {
                    engine.runUntilHalt();
                    log.debug("Jess engine halted");
                } catch (JessException e) {
                    log.error(e);
                    innerException = e;
                }
                try {
                    mainSearchThread.join(3000);
                } catch (InterruptedException e) {
                    log.warn("Interrupted waiting for main JSearch Thread");
                }
                if (mainSearchThread.isAlive())
                    mainSearchThread.interrupt();
        }
    }
    
    /**
     * Task in charge of notifying how many specifications have been explored.
     */
    private class ExploredSpecificationsTimerTask extends TimerTask {
    	
    	private int previousCount;
    	
    	public ExploredSpecificationsTimerTask(int previousCount) {
    		this.previousCount = previousCount;
    	}

		@Override
		public void run() {
			try {
				int newCount = getExpandedSpecificationsCount();
				log.info((newCount - previousCount)+" specifications explored/"+(EXPLORED_SPECIFICATION_INTERVAL/1000)+"s.");
				previousCount = newCount;
			} catch (JessException e) {
				log.error("Exception thrown while retrieving the number of explored specifications.",e);
			}
		}
    }
    
    /**
     * Task being used for the explored specifications notification.
     */
    private ExploredSpecificationsTimerTask exploredSpecificationsTimerTask = null;
    
    /**
     * Timer in charge of the explored specifications notifications.
     */
    private final Timer exploredSpecificationsTimer = new Timer("Explored Specifications Timer",true);
    
    /**
     * It instantiate the class automatically loading the default file of rules.
     * @see #DEFAULT_SPECIFICATOR_RULES_FILE
     */
    public JSearchSpecificator(SpecificatorBuilder<B,R,S,SG> builder) {
        this(DEFAULT_SPECIFICATOR_RULES_FILE,builder);
    }
    
    /**
     * It instantiate the class loading the given file of rules.
     * @param filename File containing the rules of the <i>expert system</i> to use.
     */
    public JSearchSpecificator(String filename, SpecificatorBuilder<B,R,S,SG> builder) {
        super();
        this.builder = builder;
        this.engine = super.engine;
        try {
            init(filename);
            engine.addUserfunction(new SplitGraphUserfunction<A,B,BG,R,S,SG>(this,builder));
            engine.addUserfunction(new LogUserfunction(RETE_LOGGER_NAME));
            engine.addUserfunction(new MergeGraphsUserfunction<A,B,BG,R,S,SG>(this,builder));
            setMarker();
        } catch (JessException e) {
            throw new RuntimeException("Exception loading jess rule files",e);
        }
    }
    
        /* (non-Javadoc)
         * @see dinapter.specificator.JessSpecificator#reset()
         */
    @Override
    public void reset() throws JessException {
        super.reset();
        newSpecificationsRules.clear();
        log.info("Creating and adding new graphs");
        builder.createRule();
        builder.setGraph(builder.createNewGraph());
        builder.getGraph().setStartNode(builder.createSpecification());
        builder.getGraph().getStartNode().setCost(0);
        builder.setWorkingSpecification(builder.getGraph().getStartNode());
                /* Done in addSearch
        engine.add(builder.getGraph());
                engine.add(builder.getGraph().getStartNode());*/
        engine.add(builder);
        if (jSearchThread != null)
            jSearchThread.interrupt();
        if (jessThread != null) {
            jessThread.interrupt();
        }
        jessThread = null;
        jSearchThread = null;
        innerException = null;
        searchQueue.clear();
        solutionCost = Integer.MAX_VALUE;
        graphCounter = 0;
        maxFValue = 0;
        maxQueuedPaths = -1;
        discardedGraphs = 0;
    }
    
        /* (non-Javadoc)
         * @see dinapter.specificator.JessSpecificator#specifyAdaptor()
         */
    @Override
    public void specifyAdaptor() throws JessException {
    	addMinimalActionsRequired();
        SG specificator = builder.getGraph();
        addSearch(specificator,builder.getGraph().getStartNode());
        startNewJSearchThread();
    }

    private void startNewJSearchThread() {
		jSearchThread = new JSearchThread();
        jSearchThread.start();
	}
    
	/**
	 * <p>It ignores all the current solutions and tries to find 
	 * different ones.</p>
	 * <p>The method {@link #runEngine()} should be called
	 * after in order to continue with the search. This method
	 * is an alternative for {@link #specifyAdaptor()} when the
	 * former has already been called.</p>
	 * @throws JessException An exception sent by the Jess engine.
	 */
    @SuppressWarnings("unchecked")
	protected void skipSolutions() throws JessException {
    	solutionCost = Integer.MAX_VALUE;
    	// --- We add again every solution graph that may be discarded. ---
    	QueryResult solutions = engine.runQueryStar("query-solution-specifications", new ValueVector());
    	// We introduce the graphs in a set to remove duplicates.
    	Set<SG> graphs = new HashSet<SG>();
    	// The query may return duplicated values.
		while (solutions.next()) {
			graphs.add((SG)solutions.getObject("graph"));
    	}
		for (SG graph:graphs) {
			addSearch(graph, null);
		}
    	// ----------------------------------------------------------------
		engine.assertString("(ignore-current-solutions)");
    	engine.run(); // We need that Jess finish all its calculations before continuing.
    	startNewJSearchThread();
    }
    
    /**
     * It ignores all the current solutions and tries to find 
	 * different ones.
	 * @param timeout If Dinapter doesn't find any solution before these seconds, it will stop, reset and return no specifications.
     */
    public void findDifferentSolutions(long timeout) {
    	if (!isSuccessfullyCompleted()) {
    		throw new RuntimeException("The previous search for specifications failed. It can't be continued.");
    	}
    	log.info("Skipping current solutions and searching for new ones.");
		final long start = System.currentTimeMillis();
		try {
    		if (log.isInfoEnabled()) {
    			log.info("Skipping "+engine.countQueryResults("query-best-solution-specifications", new ValueVector())+" solutions.");
    		}
        	skipSolutions();
        	setSuccessfullyCompleted(false);
        	log.debug("Running engine");
        	if (timeout <= 0) {
        		runEngine();
        	} else {
        		// -- Logic to make it stop after the timeout --
        		final JSearchSpecificator self = this;
        		Thread thread = new Thread("FindingMoreSolutions") {
        			public void run() {
        				try {
        					self.runEngine();
        				} catch (JessException e) {
        					self.setSuccessfullyCompleted(false);
        				}
        			}
        		};
        		thread.start();
        		try {
        			thread.join(timeout*1000);
        		} catch (InterruptedException e) {
        			// @TODO: Log something.
        		}
        		if (!isSuccessfullyCompleted()) {
        			try {
        				reset();
        			} catch (JessException e) {
        				// @TODO: Log something;
        			}
        			thread.interrupt();
        			try {
        				thread.join(1000);
        			} catch (InterruptedException e) {
        				// @TODO: Log something.
        			}
        		}
        		// ==========================================
        	}
    	} catch (JessException e) {
    		setSuccessfullyCompleted(false);
			throw new RuntimeException("Exception thrown by Jess when trying to find different solutions.",e);
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
    
        /* (non-Javadoc)
         * @see dinapter.specificator.JessSpecificator#run()
         */
    @Override
    @SuppressWarnings("unchecked")
    protected void runEngine() throws JessException {
        jessThread = new JessThread("Jess-Thread",jSearchThread);
        System.gc();
        startExploredSpecificationsTimer();
        jessThread.start();
        try {
            jessThread.join();
            jSearchThread.join();
            if (innerException != null)
                log.info("<========= Specification process ended ========>");
        } catch (InterruptedException e) {
            log.error("Interrupted when waiting for main threads to end",e);
        } finally {
        	stopExploredSpecificationsTimer();
            if (innerException != null)
                setSuccessfullyCompleted(false);
            if (isSuccessfullyCompleted())
                log.info("<< Specification process complete successfully >>");
            else
                log.error("<< Specification process canceled! >>");
        }
        if (innerException != null) {
            throw(new JessException("runEngine","Exception thrown",innerException));
        }
        log.info("Goodbye!");
    }
    
    private void startExploredSpecificationsTimer() {
    	if (log.isInfoEnabled()) {
	    	try {
	    		exploredSpecificationsTimerTask = new ExploredSpecificationsTimerTask(getExpandedSpecificationsCount());
	    		exploredSpecificationsTimer.scheduleAtFixedRate(exploredSpecificationsTimerTask, EXPLORED_SPECIFICATION_INTERVAL, EXPLORED_SPECIFICATION_INTERVAL);
	    	} catch (JessException e) {
	    		log.error("Exception thrown while retrieving the number of explored specifications.",e);
	    	}
    	}
    }
    
    private void stopExploredSpecificationsTimer() {
    	if (exploredSpecificationsTimerTask != null) {
    		exploredSpecificationsTimerTask.cancel();
    		exploredSpecificationsTimerTask.run();
    		exploredSpecificationsTimerTask = null;
    	}
    }
    
        /* (non-Javadoc)
         * @see dinapter.specificator.AbstractSpecificator#setComponents(dinapter.behavior.BehaviorGraph, dinapter.behavior.BehaviorGraph)
         */
    @Override
    public void setComponents(BG leftBehavior, BG rightBehavior) {
        leftBehavior.setId("left");
        rightBehavior.setId("right");
        super.setComponents(leftBehavior, rightBehavior);
    }
    
    /**
     * It sets the components to adapt and the limit of active graphs to use during the process.
     * @param leftBehavior A component to adapt.
     * @param rightBehavior The other component to adapt
     * @param searchThreadsLimit Limit of active graphs to use during the process.
     */
    public void setComponents(BG leftBehavior, BG rightBehavior, int searchThreadsLimit) {
        try {
            setSearchLimit(searchThreadsLimit);
        } catch (JessException e) {
            throw new RuntimeException("Exception thrown when setting seach threads limit.",e);
        }
        setComponents(leftBehavior, rightBehavior);
    }
    
    private void addRule(R rule) throws JessException {
    	engine.add(rule);
    }
    
    /**
     * It adds an Specification and all its rules to the engine.
     * @param specification Specification to add.
     * @throws JessException Exception thrown by Jess when adding the instances.
     */
    private void addSpecification(S specification)
    throws JessException {
    	// We avoid the test-no-rule... to fire.
    	//Fact addingSpecification = engine.assertString("(addingSpecification)");
        for (R rule:specification.getRules())
            addRule(rule);
        engine.add(specification);
        //engine.retract(addingSpecification);
    }
    
    /**
     * It adds an specificator graph to Jess if it's not already in it.
     * @param graph The graph to add.
     * @throws JessException Exception thrown by Jess.
     * @see #addSpecification(JPowerSpecification)
     */
    private void addGraph(SG graph) throws JessException {
        for (S toAdd:graph.getAllNodes())
            addSpecification(toAdd);
        engine.add(graph);
    }
    
    /**
     * It adds a specificator graph to the queue of graphs to be stepped.
     * The graph will be added only if the <code>newSpecification</code>
     * doesn't exist in the system already. This check won't be performed
     * if the argument is <code>null</code>.
     * @param specificator Specificator graph to be added.
     */
    public void addSearch(SG specificator, S newSpecification) {
    	if ((newSpecification == null) || !existsEquivalentSpecification(newSpecification)) {
    		if (newSpecification != null) {
    			newSpecificationsRules.put(newSpecification.getRules(), newSpecification.getWorkingRule());
    			graphCounter++;
    		}
	    	searchQueue.add(new AStarAlgorithm(specificator));
    	} else {
    		discardedGraphs++;
    		log.debug("Graph with new specification discarded because it exists an equivalent one.");
    	}
    }
    
    /**
     * It returns whether it exists an specification with same rules (and working rule) as the
     * given one.
     * @param Specification to find equivalent ones.
     * @return Whether it exists an equivalent specification or not.
     */
    protected boolean existsEquivalentSpecification(S specification) {
    	// @todo Demonstrate that the path to a specification is irrelevant.
    	try {
    		Collection<R> rules = newSpecificationsRules.get(specification.getRules());
    		if ((rules != null) && rules.contains(specification.getWorkingRule()))
	    		return true;
	    	else {
		    	ValueVector arguments = new ValueVector(2);
		    	arguments.add(specification.getRules());
		    	arguments.add(specification.getWorkingRule());
		    	return (engine.countQueryResults("query-equivalent-specification", arguments)) > 0;
	    	}
    	} catch (JessException e) {
    		throw new RuntimeException("Exception while trying to run a Jess query.",e);
    	}
    }
    
    /**
     * It returns the limit of active graphs.
     * @return The limit of active graphs.
     */
    public int getSearchLimit() {
        return searchLimit;
    }
    
    /**
     * It sets the limit of active graphs.
     * @param searchThreadsLimit The limit of active graphs.
     */
    public void setSearchLimit(int searchThreadsLimit) throws JessException {
        this.searchLimit = searchThreadsLimit;
    }
    
    public int getMaxQueuedGraphs() {
        return maxQueuedPaths;
    }
    
    /**
     * It executes the adaptation process using one of the three couples of example
     * components. It displays a <b>Jess console</b> which provides some process feedback and
     * Jess interaction as well as all kind of logging events in the application
     * standard output.
     * <p>
     * It requires only one argument which describes the example to be adapted. In all
     * of the examples on of the components is the same ({@link FTPExample#getFtpClient()})
     * and only the other is decided by this argument. This unique argument must be one of:
     * <ul>
     * <li><code>tiny</code> - It corresponds to {@link FTPExample#getVerySimpleFtpServer()}.
     * <li><code>small</code> - It corresponds to {@link FTPExample#getSimpleFtpServer()}.
     * <li><code>full</code> - It corresponds to {@link FTPExample#getFtpServer()}.
     * </ul>
     * <b>WARNING:</b> <code>full</code> example may take up to 6 full hours to actually
     * finish. It is recommended to cancel the process when enought solutions are found.
     * <p>
     * The <b>Jess console</b> supports two particulary usefull commands:
     * <ul>
     * <li><code>(cancel)</code> - This cancels the process which may finish with the
     * solutions found so far.
     * <li><code>(exit)</code> - This forces an exit. <u>No results are displayed</u>.
     * </ul>
     * @param args Example to be adapted: "tiny", "small" or "full".
     * @see data.FTPExample
     */
    @SuppressWarnings("unchecked")
    public static final void main(String... args) {
        JSearchSpecificator<Object
                ,JPowerBehaviorNode<Object>
                ,JPowerBehaviorGraph<Object
                ,JPowerBehaviorNode<Object>
                ,Edge>
                ,DefaultRule<JPowerBehaviorNode<Object>>
                ,SimpleSpecification<DefaultRule<JPowerBehaviorNode<Object>>>
                ,BridgeSpecificatorGraph<DefaultRule<JPowerBehaviorNode<Object>>
                ,SimpleSpecification<DefaultRule<JPowerBehaviorNode<Object>>>>> engine
                = new JSearchSpecificator
                <Object
                ,JPowerBehaviorNode<Object>
                ,JPowerBehaviorGraph<Object
                ,JPowerBehaviorNode<Object>
                ,Edge>
                ,DefaultRule<JPowerBehaviorNode<Object>>
                ,SimpleSpecification<DefaultRule<JPowerBehaviorNode<Object>>>
                ,BridgeSpecificatorGraph<DefaultRule<JPowerBehaviorNode<Object>>
                ,SimpleSpecification<DefaultRule<JPowerBehaviorNode<Object>>>>>(new MapSpecificatorBuilder<Object>());
        FTPExample componentsBuilder = new FTPExample();
        String example = null;
        if ((args.length == 0) || (args[0].equalsIgnoreCase("small"))) {
            example = "small";
            if (args.length == 0)
                log.warn("No argument found. First argument must be one of: \"tiny\", \"small\" or \"full\". \"small\" will be used by default.");
            engine.setComponents(componentsBuilder.getFtpClient(), componentsBuilder.getSimpleFtpServer(),200);
        } else if (args[0].equalsIgnoreCase("tiny"))
            engine.setComponents(componentsBuilder.getFtpClient(), componentsBuilder.getVerySimpleFtpServer(),2);
        else if (args[0].equalsIgnoreCase("full"))
            engine.setComponents(componentsBuilder.getFtpClient(), componentsBuilder.getFtpServer(),2000);
        else {
            String errorMessage = "Example name argument not found. First argument must be one of: \"tiny\", \"small\" or \"full\".";
            log.fatal(errorMessage);
            System.exit(1);
        }
        final Console console = new Console("Jess engine console",engine.engine);
        console.addWindowListener(new WindowAdapter(){
        	public void windowClosing(WindowEvent we) {
        		console.dispose();
        	}
        	public void windowClosed(WindowEvent we) {
        		System.exit(0);
        	}
        });
        log.info("Running example \""+(example==null?args[0]:example)+"\"\n"
                +"\tCommands available:\n"
                +"\t\t(cancel) - To finish the process as it is so far.\n"
                +"\t\t  (exit) - To exit the JVM immediately.");
        new Thread() {
            public void run() {
                String [] arguments = {"-nologo"};
                console.execute(arguments);
            }
        }.start();
        engine.run();
        log.info(engine.getStatusMessage());
        log.info(engine.getBestSolutionsMessage());
    }
    
    /**
     * It returns the count of instances of the given class within the Jess engine.
     * @param clazz Class to be counted.
     * @return The count of instances.
     */
    protected int getCountOf(Class clazz) {
        Iterator iter = engine.getObjects(new Filter.ByClass(clazz));
        int count = 0;
        for (;iter.hasNext();iter.next())
            count++;
        return count;
    }
    
    /**
     * It sets the the first best solution Specification found or the
     * first solution found if there're no best solution at all.
     */
    @SuppressWarnings("unchecked")
    protected void setSolution() {
        try {
            QueryResult result = engine.runQueryStar("query-solution-specifications", new ValueVector(0));
            S solution = null, firstSolution = null;
            while ((solution == null) && result.next()) {
                if (firstSolution == null)
                    firstSolution = (S)result.getObject("specification");
                if (result.getBoolean("best"))
                    solution = (S)result.getObject("specification");
            }
            if (solution == null)
                solution = firstSolution;
            setSpecification(solution);
        } catch (JessException e) {
            if (innerException == null)
                innerException = e;
            log.error("Error setting the solution Specification",e);
        }
    }
    
    /**
     * It returns the number of graphs in Jess working memory.
     * @return The number of graphs in Jess working memory.
     * @throws JessException Any problem during Jess data retrieval.
     */
    public int getActiveGraphsCount() throws JessException {
        return engine.eval("?*GRAPH_COUNTER*").intValue(engine.getGlobalContext());
    }
    
    /**
     * It returns the number of specifiations within the Jess working memory.
     * @return The number of specifiations within the Jess working memory.
     * @throws JessException Any problem during Jess data retrieval.
     */
    public int getSpecificationsCount() throws JessException {
        return engine.eval("?*SPECIFICATION_COUNTER*").intValue(engine.getGlobalContext());
    }
    
    /**
     * It returns the number of expanded/explored specifications by the search algorithm.
     * @return The number of expanded/explored specifications by the search algorithm.
     * @throws JessException Any problem during Jess data retrieval.
     */
    public int getExpandedSpecificationsCount() throws JessException {
        return engine.eval("?*EXPANDED_NODES*").intValue(engine.getGlobalContext());
    }
    
    /**
     * It returns the number of rules within Jess working memory.
     * @return The number of rules within Jess working memory.
     * @throws JessException Any problem during Jess data retrieval.
     */
    public int getRulesCount() throws JessException {
    	return engine.eval("?*RULES_COUNTER*").intValue(engine.getGlobalContext());
    }
    
    /**
     * It returns the number of graphs created. This is the biggest graph count because
     * it counts the graph not in Jess memory, nor even queued.
     * @return The number of graphs created.
     */
    public int getGraphCounter() {
    	return graphCounter;
    }
    
    public int getDiscardedGraphCount() {
    	return discardedGraphs;
    }
    
    public double getProcessQuality() throws JessException {
        if ((getSpecificationsCount() == 0) || (getGraphCounter() == 0))
            return 0;
        return 100-(50.*(getExpandedSpecificationsCount()-1)/getSpecificationsCount())-(50.*(getActiveGraphsCount()-1)/getGraphCounter());
    }
    
    @SuppressWarnings("unchecked")
	public List<S> getBestSolutions() {
    	try {
	    	List<S> toReturn = new Vector<S>();
	    	QueryResult result = engine.runQueryStar("query-solution-specifications", new ValueVector());
	    	while (result.next()) {
	    		if (result.getBoolean("best")) {
	    			toReturn.add((S)result.getObject("specification"));
	    		}
	    	}
	    	Collections.sort(toReturn);
	    	return toReturn;
    	} catch (JessException e) {
    		throw new RuntimeException("Exception thrown while retrieving the solutions.",e);
    	}
    }
    
    public String getBestSolutionsMessage() {
    	String solution = null;
    	try {
	        int counter = engine.countQueryResults("query-solution-specifications", new ValueVector());
	        List<S> bestSolutions = getBestSolutions();
	        int bestCounter = bestSolutions.size();
	        if (!bestSolutions.isEmpty()) {
	        	solution = "";
	            int index = 0;
	            for (S bestSolution:bestSolutions) {
	                solution += "\n----------------- Specification #"+index+" ----------------------\n"+bestSolution;
	                index++;
	            } 
	            solution = " *** "+bestCounter+" out of "+counter+" solutions found *** "+solution;
	        } else
	            solution = " *** No solution found *** ";
    	} catch (JessException e) {
    		log.error("The solutions couldn't be displayed",e);
    	}
    	return solution;
    }
    
    public String getStatusMessage() {
    	String message = "The message couldn't be processed";
        try {
            int specificationsCounter = getSpecificationsCount();
            int expandedNodes = specificationsCounter;
            NumberFormat formatter = NumberFormat.getNumberInstance();
            message = "There are:\n"+
            		"\t          Solutions = "+getBestSolutions().size()+"\n"+
            		"\t              Rules = "+getRulesCount()+"\n"+
                    "\tSpecificator Graphs = "+getGraphCounter()+"/"+getActiveGraphsCount()+"/"+
                    getMaxQueuedGraphs()+" = Max. active Graphs\n"+
                    "\t   Graphs discarded = "+getDiscardedGraphCount()+"\n"+
                    "\tSpecification Nodes = "+specificationsCounter;
            expandedNodes = getExpandedSpecificationsCount();
            double processQuality = getProcessQuality();
            message += "/"+expandedNodes+" = Expanded Nodes"+"\n"+
            		"\t       Memory usage = "+formatter.format((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024)+" KB";
            message += "\n\tResults Quality = "+formatter.format(processQuality)+"%";
        } catch (JessException e) {
            log.error("Error showing expanded nodes count",e);
        }
        return message;
    }
    
    private void addMinimalActionsRequired() {
    	/* Set<Set<B>> minimalActions =
    		SolutionActions.removeSuperSets(
		    		SolutionActions.cartesianProduct(
		    				solutionActions.getRequiredActions(leftComponent)
		    				,solutionActions.getRequiredActions(rightComponent))); */
    	Set<Set<B>> minimalActions =
		    		SolutionActions.cartesianProduct(
		    				solutionActions.getPossibleRequiredActions(leftComponent)
		    				,solutionActions.getPossibleRequiredActions(rightComponent));
    	for (Set<B> actions:minimalActions) {
    		log.debug("Possible required actions: "+actions);
    		addMinimalActionsRequired(actions);
    	}
    }
    
    private void addMinimalActionsRequired(Set<B> actions) {
    	ValueVector vv = new ValueVector();
    	for (B action:actions) {
    		vv.add(new Value(action));
    	}
    	try {
	    	Fact fact = new Fact("actions-required",engine);
	    	fact.setSlotValue("actions", new Value(vv, RU.LIST));
	    	engine.assertFact(fact);
    	} catch (JessException e) {
			throw new RuntimeException("Exception thrown while evaluating the minimal required actions to be adapted.",e);
		}
    }
    
    private boolean isIgnored(S specification) throws JessException {
    	ValueVector vv = new ValueVector(1);
    	vv.add(specification);
    	return engine.countQueryResults("query-is-ignored", vv) > 0;
    }
}
