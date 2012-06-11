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

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

import javax.swing.JFrame;

import jess.Console;
import jess.Context;
import jess.Filter;
import jess.JessException;
import jess.QueryResult;
import jess.Rete;
import jess.Userfunction;
import jess.Value;
import jess.ValueVector;
import jsearchdemo.AStarAlgorithm;
import net.sourceforge.jpowergraph.Edge;

import org.apache.log4j.Logger;

import data.FTPExample;
import dinapter.behavior.JPowerBehaviorGraph;
import dinapter.behavior.JPowerBehaviorNode;

/**
 * This is the main class of Dinapter project and it centralize the control of the specification process.
 * 
 * @author José Antonio Martín Baena
 * @version $Revision: 486 $ - $Date: 2007-03-08 19:37:33 +0100 (jue, 08 mar 2007) $
 */
public class JPowerJSearchSpecificator<A> 
		extends	JessSpecificator<JPowerBehaviorGraph<A,JPowerBehaviorNode<A>,Edge>, JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>>> {
	
    /**
     * The address of the clip file which contains the rules of the <i>expert system</i>. It is <code>rules/rules.clp</code> by default.
     */
    public static final String DEFAULT_SPECIFICATOR_RULES_FILE = "rules/rules.clp";
    
    /**
     * This avoids graphs with too many nodes not to be displayed.
     */
    private static final boolean DISABLE_NODE_LIMIT = true;
    
    /**
     * Limit of solution graphs to display at the end of the process.
     */
    private static final int GRAPHS_TO_DISPLAY_LIMIT = 5;
    
    /**
     * Safe limit of active graphs during the process by default. It can be modified using {@link #setComponents(JPowerBehaviorGraph, JPowerBehaviorGraph, int)}
     */
    private int searchLimit = 100;
    
    /**
     * This variable contains the cost of the best solution found so far dropping any path with higher f value.
     * It can be used to manually set an initial threashold of f values to consider.
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
    
    // @todo Close rules apropiately on loops.
    // @todo Handle merges finding partial solutions (other branches cut) and merging them.
	
	private static final Logger log = Logger.getLogger(JPowerJSearchSpecificator.class);
    private static final Logger reteLog = Logger.getLogger("dinapter.specificator.ReteLog");
	private Thread jessThread, jSearchThread = null;
	private JessException innerException = null;
    public final Rete engine;
    
    /**
     * It contains the queue of specificator graphs to be stepped. It automatically sort them in ascending f value order.
     */
    private final PriorityBlockingQueue<AStarAlgorithm> searchQueue = new PriorityBlockingQueue<AStarAlgorithm>(searchLimit,new Comparator<AStarAlgorithm>(){
        public int compare(AStarAlgorithm o1, AStarAlgorithm o2) {
            return AStarAlgorithm.computeFValue(o1.getQueue().getFirst()) - AStarAlgorithm.computeFValue(o2.getQueue().getFirst());
        }
    });
	
    /**
     * Builder used for generating all graphs, specifications and rules. No assumptions can be made about its current
     * graph or working specification.
     */
	protected final JPowerSpecificatorBuilder<A> builder
		= new JPowerSpecificatorBuilder<A>();
	
	/**
	 * It instantiate the class automatically loading the default file of rules.
     * @see #DEFAULT_SPECIFICATOR_RULES_FILE
	 */
	public JPowerJSearchSpecificator() {
		this(DEFAULT_SPECIFICATOR_RULES_FILE);
	}
	
    /**
     * It instantiate the class loading the given file of rules.
     * @param filename File containing the rules of the <i>expert system</i> to use.
     */
	public JPowerJSearchSpecificator(String filename) {
		super();
        this.engine = super.engine;
		builder.setChildrenReady(false);
		try {
			init(DEFAULT_SPECIFICATOR_RULES_FILE);
			engine.addUserfunction(new Userfunction() {
				/* (non-Javadoc)
				 * @see jess.Userfunction#call(jess.ValueVector, jess.Context)
				 * (split-search ?action ?side ?specification ?specificatorGraph)
				 */
				@SuppressWarnings("unchecked")
				public Value call(ValueVector vv, Context context) throws JessException {
					JPowerBehaviorNode<A> node = (JPowerBehaviorNode<A>) vv.get(2).javaObjectValue(context);
					JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>> specification
						= (JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>>) vv.get(4).javaObjectValue(context);
					String side = vv.get(3).stringValue(context);
					JPowerBehaviorNode<A> switchAction = (JPowerBehaviorNode<A>) vv.get(1).javaObjectValue(context);
					JPowerSpecificatorGraph<DefaultRule<JPowerBehaviorNode<A>>, JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>>> toReturn =
						splitSearch(switchAction,node, side.equals("left"), specification);	
					return new Value(toReturn);
				}

				/* (non-Javadoc)
				 * @see jess.Userfunction#getName()
				 */
				public String getName() {
					return "splitSearch";
				}
			});
			engine.addUserfunction(new Userfunction() {
				public Value call(ValueVector vv, Context context) throws JessException {
					String type = vv.get(1).symbolValue(context);
					String message = "";
                    Logger log = reteLog;
					for (int i = 2; i < vv.size();i++) {
						if (vv.get(i).isNumeric(context))
							message += vv.get(i).floatValue(context);
                        else if (vv.get(i).isLexeme(context)) {
                            String value = vv.get(i).stringValue(context); 
                            message += "crlf".equals(value)?"\n":value;
                        } else
							message += vv.get(i).javaObjectValue(context);
					}
					if (type.equals("info"))
						log.info(message);
					else if (type.equals("debug"))
						log.debug(message);
					else if (type.equals("error"))
						log.error(message);
					else if (type.equals("warn"))
						log.warn(message);
                    else if (type.equals("trace"))
                        log.trace(message);
                    else if (type.equals("fatal"))
                        log.fatal(message);
					else
						throw new JessException("log","Log type unknown:",type);
					return null;
				}
				public String getName() {
					return "log";
				}
			});
			engine.addUserfunction(new Userfunction() {
				@SuppressWarnings("unchecked")
				public Value call(ValueVector vv, Context context) throws JessException {
					JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>>
						specification,parent;
					parent = (JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>>)
						vv.get(1).javaObjectValue(context);
					specification = (JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>>)
						vv.get(2).javaObjectValue(context);
					return new Value(forkGraph(parent,specification));
				}
				public String getName() {
					return "forkGraph";
				}
			});
			engine.addUserfunction(new Userfunction() {
                Set graphDisplayed = new HashSet();
                
                @SuppressWarnings("unchecked")
                public Value call(ValueVector vv, Context context) throws JessException {
                    String name = vv.get(1).stringValue(context);
                    JPowerSpecificatorGraph<DefaultRule<JPowerBehaviorNode<A>>, JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>>> graph = (JPowerSpecificatorGraph<DefaultRule<JPowerBehaviorNode<A>>, JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>>>) vv.get(2).javaObjectValue(context);
                    if (!graphDisplayed.contains(graph)) {
                        graphDisplayed.add(graph);
                        displayGraph(name,graph);
                    }
                    return null;
                }
                public String getName() {
                    return "displayGraph";
                }
            });
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
		log.info("Creating and adding new graphs");
		builder.createRule();
		builder.setGraph(builder.createNewGraph());
		builder.getGraph().setStartNode(builder.createSpecification());
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
	}

	/* (non-Javadoc)
	 * @see dinapter.specificator.JessSpecificator#specifyAdaptor()
	 */
	@Override
	public void specifyAdaptor() throws JessException {
		JPowerSpecificatorGraph<DefaultRule<JPowerBehaviorNode<A>>, JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>>> 
			specificator = builder.getGraph();
		addSearch(specificator);
        jSearchThread = new JSearchThread();
        jSearchThread.start();
	}

	/* (non-Javadoc)
	 * @see dinapter.specificator.JessSpecificator#run()
	 */
	@Override
    @SuppressWarnings("unchecked")
	protected void runEngine() throws JessException {
		final Thread mainSearchThread = jSearchThread; 
		jessThread = new Thread("Jess-Thread") {
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
		};
		jessThread.start();
		try {
			jessThread.join();
			jSearchThread.join();
			if (innerException != null)
				log.info("<========= Specification process ended ========>");
		} catch (InterruptedException e) {
			log.error("Interrupted when waiting for main threads to end",e);
		} finally {
			if (innerException != null)
				setSuccessfullyCompleted(false);
			if (isSuccessfullyCompleted())
				log.info("<< Specification process complete successfully >>");
			else
				log.error("<< Specification process canceled! >>");
		}
		if (innerException != null) {
			try {
				if (engine.fetch("bad-graph") != null)
					displayGraph("Problematic graph", 
							(JPowerSpecificatorGraph<DefaultRule<JPowerBehaviorNode<A>>, JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>>>)
							engine.fetch("bad-graph").javaObjectValue(engine.getGlobalContext()));
			} catch (RuntimeException e) {
				log.error("Problem showing problematic graph",e);
			}
			throw(new JessException("runEngine","Exception thrown",innerException));
		}
		log.info("Goodbye!");
	}

	/* (non-Javadoc)
	 * @see dinapter.specificator.AbstractSpecificator#setComponents(dinapter.behavior.BehaviorGraph, dinapter.behavior.BehaviorGraph)
	 */
	@Override
	public void setComponents(JPowerBehaviorGraph<A, JPowerBehaviorNode<A>, Edge> leftBehavior, JPowerBehaviorGraph<A, JPowerBehaviorNode<A>, Edge> rightBehavior) {
		leftBehavior.setSide("left");
		rightBehavior.setSide("right");
		super.setComponents(leftBehavior, rightBehavior);
	}
	
    /**
     * It sets the components to adapt and the limit of active graphs to use during the process.
     * @param leftBehavior A component to adapt.
     * @param rightBehavior The other component to adapt
     * @param searchThreadsLimit Limit of active graphs to use during the process.
     */
	public void setComponents(JPowerBehaviorGraph<A, JPowerBehaviorNode<A>, Edge> leftBehavior, JPowerBehaviorGraph<A, JPowerBehaviorNode<A>, Edge> rightBehavior, int searchThreadsLimit) {
		try {
			setSearchLimit(searchThreadsLimit);
		} catch (JessException e) {
			throw new RuntimeException("Exception thrown when setting seach threads limit.",e);
		}
		setComponents(leftBehavior, rightBehavior);
	}

    /**
     * It adds an Specification and all its rules to the engine.
     * @param specification Specification to add.
     * @throws JessException Exception thrown by Jess when adding the instances.
     */
	private void addSpecification(JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>> specification)
			throws JessException {
		for (DefaultRule<JPowerBehaviorNode<A>> rule:specification.getRules())
			engine.add(rule);
		engine.add(specification);
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
	private JPowerSpecificatorGraph<DefaultRule<JPowerBehaviorNode<A>>, JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>>> 
		splitSearch(
			JPowerBehaviorNode<A> switchAction
			, JPowerBehaviorNode<A> nodeToPush
			, boolean left
			, JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>> specification) {
        
        // * Creating the new Graph *
		JPowerSpecificatorGraph<DefaultRule<JPowerBehaviorNode<A>>, JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>>>
			splittedGraph = builder.createNewGraph();
		builder.setGraph(splittedGraph);
        
        // * Start node of the Graph *
		splittedGraph.setStartNode(builder.createSpecification());
		splittedGraph.getStartNode().setChildrenReady(true); // No more children.
		splittedGraph.getStartNode().setHeuristic(specification.getHeuristic()+specification.getAcumulatedCost()); // Same as copied Specification.
		splittedGraph.getStartNode().setCopiedSpecification(true); // No heuristic or cost change allowed.
        
        // * Copied-link parent node *
		JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>> copied = copySpecification(specification);
        // These two following sentences avoid ''(add-close-rule)'' to fire.
		copied.setChildrenNeeded(false); // No more children.
		copied.setChildrenReady(true);
		copied.setCost(specification.getAcumulatedCost()); // Just to keep old acumulated cost.
        copied.setHeuristic(0); // Old heuristic is not valid anymore.
		builder.link(splittedGraph.getStartNode(), copied);
        
        // * Creating and linking of the real new node *
        builder.closeRule(); // Close previous rule.
		if (left)
			builder.pushLeft(nodeToPush);
		else
			builder.pushRight(nodeToPush);
        
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
        
        // * Adding graph and new specs to Rete *
		addSearch(splittedGraph);
		log.debug("Splitted graph in two");
		return splittedGraph;
	}
    
    /**
     * Delegate method which actually generates a new specificator graph which 
     * merges a couple of specifications.
     * @param parent One of the specifications to merge.
     * @param specification Already merged specification.
     * @return Newly created specificator graph containing the merge result.
     * @throws JessException Exception thrown by Jess. Problem with the underline engine.
     */
	private JPowerSpecificatorGraph<DefaultRule<JPowerBehaviorNode<A>>, JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>>>
			forkGraph(JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>> parent,
					  JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>> specification)
			throws JessException{
        // * Created new graph *
		JPowerSpecificatorGraph<DefaultRule<JPowerBehaviorNode<A>>, JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>>>
		graph = builder.createNewGraph();
		builder.setGraph(graph);
        
        // * Start node of the newly created graph *
		graph.setStartNode(builder.createSpecification());
		//graph.getStartNode().setHeuristic(parent.getHeuristic()+parent.getAcumulatedCost()); // Same as parent Specification // Moved some lines below
		graph.getStartNode().setChildrenReady(true);
		graph.getStartNode().setCopiedSpecification(true); // No heuristic or cost change allowed.
        
        // * Cost calculations *
        /* Check out that parent must be the merged Specification which has its rules at
         *  the bottom because of (working-rule-heuristic) behavior. */
        // @tofix This is indeterministic and buggy (It DOES matter the order to merge). Perhaps just counting new actions merged...
        int cost = parent.getAcumulatedCost() + 
            (specification.getActions().length - parent.getActions().length - 1)
            *engine.eval("?*INITIAL_COST*").intValue(engine.getGlobalContext()); // - initialization cost.
        for (DefaultRule rule:specification.getRules()) {
            if (!parent.getRules().contains(rule))
                cost += rule.getHeuristic();
        }
        // @toreview This is not accurate but will be enought
        graph.getStartNode().setHeuristic(cost + engine.eval("?*INITIAL_COST*").intValue(engine.getGlobalContext()) + 
                (engine.eval("?*ACTIONS_TO_ADAPT*").intValue(engine.getGlobalContext()) - specification.getActions().length)
                *engine.eval("?*REMAINING_ACTIONS_PENALIZATION*").intValue(engine.getGlobalContext()));
        
        // * Copied parent node *
		builder.link(graph.getStartNode(), copySpecification(parent));
		builder.getWorkingSpecification().setCost(cost);
        builder.getWorkingSpecification().setHeuristic(0); // Old heuristic is not valid anymore.
        builder.getWorkingSpecification().setAcumulatedCost(cost);
        builder.getWorkingSpecification().setChildrenNeeded(false);
		builder.getWorkingSpecification().setChildrenReady(true);
        
        // * Link and add new node *
		builder.link(builder.getWorkingSpecification(), specification);
		addSearch(graph);
		return graph;
	}
	
    /**
     * It adds an specificator graph to Jess if it's not already in it.
     * @param graph The graph to add.
     * @throws JessException Exception thrown by Jess.
     * @see #addSpecification(JPowerSpecification)
     */
	private void addGraph(JPowerSpecificatorGraph<DefaultRule<JPowerBehaviorNode<A>>, JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>>>
			graph) throws JessException {
		for (JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>> toAdd:graph.getAllNodes())
			addSpecification(toAdd);
		engine.add(graph);
	}
	
    /**
     * It copies an specification.
     * @param from Specifciation to copy.
     * @return Copy of the specification.
     */
	private JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>> copySpecification(JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>> from) {
		JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>> toReturn = new JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>>(from.getRules());
		toReturn.setHeuristic(from.getHeuristic());
		toReturn.setCost(from.getCost());
		toReturn.setChildrenNeeded(from.isChildrenNeeded());
		toReturn.setChildrenReady(from.isChildrenReady());
		toReturn.setLeftPath(from.getLeftPath());
		toReturn.setRightPath(from.getRightPath());
		toReturn.setCopiedSpecification(true);
		toReturn.setAcumulatedCost(from.getAcumulatedCost());
		return toReturn;
	}
    
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
            log.debug("Starting JSearchThread");
            int activeGraph = 0;
            try {
                int fValue = 0;
                while (!Thread.interrupted() && !canceled && !searchQueue.isEmpty()
                        &&(activeGraph < getSearchLimit())
                        &&((fValue = AStarAlgorithm.computeFValue(searchQueue.peek().getQueue().getFirst())) <= solutionCost)) {
                    if (fValue > maxFValue)
                        log.info("Looking for solutions with maximum f value ="+(maxFValue = fValue));
                    AStarAlgorithm algorithm = searchQueue.take();
                    if (!engine.containsObject(algorithm.getGraph())) {
                        log.trace("Adding a new graph");
                        activeGraph++;
                        addGraph((JPowerSpecificatorGraph<DefaultRule<JPowerBehaviorNode<A>>, JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>>>)
                                algorithm.getGraph());
                    }
                    log.debug("Stepping. Active graphs = "+activeGraph);
                    algorithm.step();
                    if (!algorithm.finished()) {
                        fValue = AStarAlgorithm.computeFValue(algorithm.getQueue().getFirst());
                        if (fValue <= solutionCost) {
                            log.trace("New graph f value = "+fValue);
                            searchQueue.add(algorithm);
                        } else
                            log.debug("Graph discarded because of its bad f value ("+fValue+")");
                    } else if (!algorithm.getQueue().isEmpty()) {
                        activeGraph--;
                        fValue = AStarAlgorithm.computeFValue(algorithm.getQueue().getFirst());
                        /* ---- Some tests here --- */
                        JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>> spec
                            = (JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>>) algorithm.getQueue().getFirst().getEndNode();
                        if (AStarAlgorithm.computeCost(algorithm.getQueue().getFirst()) != spec.getAcumulatedCost())
                            throw new RuntimeException("Acumulated cost is not equal to calculated cost");
                        if (fValue != spec.getAcumulatedCost() + spec.getHeuristic())
                            throw new RuntimeException("f value is not as estimated");
                        /* ------------------------ */
                        if (fValue < solutionCost) {
                            solutionCost = fValue;
                            log.info("Solution found. Cost="+solutionCost);
                        } else
                            log.info("Worse solution found. Cost="+fValue);
                    } else {
                        activeGraph--;
                        log.debug("Graph fully processed");
                    }
                    if (activeGraph > maxQueuedPaths)
                        maxQueuedPaths = activeGraph;
                    yield(); // We encourage Jess to respond to new changes.
                }
            } catch (InterruptedException e) {
                // Whatever that must be done has been done within the interrupt method.
            } catch (JPowerSpecificatorGraph.ChildrenWaitInterruptedException e) {
                // Whatever that must be done has been done within the interrupt method.
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

    /**
     * It adds a specificator graph to the queue of graphs to be stepped.
     * @param specificator Specificator graph to be added.
     */
	protected void addSearch(JPowerSpecificatorGraph<DefaultRule<JPowerBehaviorNode<A>>, JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>>> specificator) {
        searchQueue.add(new AStarAlgorithm(specificator));
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
        JPowerJSearchSpecificator<Object> engine = new JPowerJSearchSpecificator<Object>();
        FTPExample componentsBuilder = new FTPExample();
        String example = null;
        if ((args.length == 0) || (args[0].equalsIgnoreCase("small"))) {
            example = "small";
            if (args.length == 0)
                log.warn("No argument found. First argument must be one of: \"tiny\", \"simple\" or \"full\". \"simple\" will be used by default.");
            engine.setComponents(componentsBuilder.getFtpClient(), componentsBuilder.getSimpleFtpServer(),200);
        } else if (args[0].equalsIgnoreCase("tiny"))
            engine.setComponents(componentsBuilder.getFtpClient(), componentsBuilder.getVerySimpleFtpServer(),2);
        else if (args[0].equalsIgnoreCase("full"))
            engine.setComponents(componentsBuilder.getFtpClient(), componentsBuilder.getFtpServer(),2000);
        else {
            String errorMessage = "Example name argument not found. First argument must be one of: \"tiny\", \"simple\" or \"full\".";
            log.fatal(errorMessage);
            System.exit(1);
        }
		final Console console = new Console("Jess engine console",engine.engine);
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
        int graphsCounter = engine.getCountOf(JPowerSpecificatorGraph.class);
        int specificationsCounter = engine.getCountOf(JPowerSpecification.class);
        int expandedNodes = specificationsCounter;
		String message = "There are:\n"+
							 "\tSpecificator Graphs = "+graphsCounter+"/"+
                             engine.maxQueuedPaths+" = Max. active Graphs\n"+
							 "\tSpecification Nodes = "+specificationsCounter;
		try {
            expandedNodes = engine.engine.eval("?*EXPANDED_NODES*").intValue(engine.engine.getGlobalContext());
            float processQuality = 100-(50*expandedNodes/specificationsCounter)-(50*(engine.maxQueuedPaths-1)/graphsCounter); //+(elapsedTime/10000)
			message += "/"+expandedNodes+" = Expanded Nodes";
            message += "\n\tResults Quality = "+processQuality+"%";
		} catch (JessException e) {
			log.error("Error showing expanded nodes count",e);
		}
		log.info(message);
		JPowerSpecificatorGraph<DefaultRule<JPowerBehaviorNode<Object>>, JPowerSpecification<DefaultRule<JPowerBehaviorNode<Object>>>>
			closestGraph = null;
		try {
			if (engine.engine.fetch("closest-specification-graph") != null) {
				engine.displayGraph("Closest Specification Graph", closestGraph = (JPowerSpecificatorGraph<DefaultRule<JPowerBehaviorNode<Object>>, JPowerSpecification<DefaultRule<JPowerBehaviorNode<Object>>>>)
						engine.engine.fetch("closest-specification-graph").javaObjectValue(engine.engine.getGlobalContext()));
			}
		} catch (JessException e) {
			log.error("Couldn't display closest specification graph",e);
		}
		try {
			QueryResult result = engine.engine.runQueryStar("query-solution-specifications", new ValueVector());
			if (result.next()) {
					int counter = 0;
					int bestCounter = 0;
					int displayed = 0;
					Set graphShown = new HashSet();
					graphShown.add(closestGraph);
					String solution = "";
					do {
						if (result.getBoolean("best")) {
							solution += "\n"+result.getObject("specification").toString()+"\n"
									      +"----------------- Graph #"+(displayed > GRAPHS_TO_DISPLAY_LIMIT?"-":"<="+(displayed+1))+" ----------------------";
							bestCounter++;
							JPowerSpecificatorGraph<DefaultRule<JPowerBehaviorNode<Object>>, JPowerSpecification<DefaultRule<JPowerBehaviorNode<Object>>>>
								graph = (JPowerSpecificatorGraph<DefaultRule<JPowerBehaviorNode<Object>>, JPowerSpecification<DefaultRule<JPowerBehaviorNode<Object>>>>)
								result.getObject("graph");
							if ((displayed <= GRAPHS_TO_DISPLAY_LIMIT) && (!graphShown.contains(graph))) {
								engine.displayGraph("Best Solution #"+(++displayed), graph);
								graphShown.add(graph);
							}
						}
						counter++;
					} while (result.next());
					solution = " *** "+bestCounter+" out of "+counter+" solutions found *** "+solution;
					if (displayed > GRAPHS_TO_DISPLAY_LIMIT)
						log.warn("Limit of Specification Graphs to display reached.");
					log.info(solution);
			} else
				log.error(" *** No solution found *** ");
		} catch (JessException e) {
			log.error("Couldn't show the results",e);
		}
	}
	
    /**
     * It displays the given specificator graph. If {@link #DISABLE_NODE_LIMIT} allows it
     * graphs with too many nodes are ommited by this method.
     * <p>
     * <b>WARNING:</b> Displaying same graph more than once will not run properly. 
     * @param name Name of the frame which may display the graph.
     * @param graph Graph to be displayed.
     */
	private void displayGraph(String name, JPowerSpecificatorGraph<DefaultRule<JPowerBehaviorNode<A>>, JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>>> graph) {
		if (!DISABLE_NODE_LIMIT && (graph.getAllNodes().size() > 50))
			log.warn("Not displaying \""+name+"\" because it's too heavy ("+graph.getAllNodes().size()+" nodes)");
		else {
			if (graph.getAllNodes().size() > 20)
				log.warn("Graph \""+name+"\" display may be slow because of its nodes amount ("+graph.getAllNodes().size()+")");
			JFrame frame = new JFrame(name);
			builder.setGraph(graph);
			frame.getContentPane().add(builder.getGraphView());
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(800, 500);
			frame.pack();
			frame.setVisible(true);
		}
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
            JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>> solution = null, firstSolution = null;
            while (result.next()
                    && !((JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>>)
                            result.getObject("specification")).isBestSolution()) {
                if (firstSolution == null)
                    firstSolution = (JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>>)
                        result.getObject("specification");
                if (result.getBoolean("best"))
                    solution = (JPowerSpecification<DefaultRule<JPowerBehaviorNode<A>>>)
                        result.getObject("specification");
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
}
