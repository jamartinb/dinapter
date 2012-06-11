package jsearchdemo;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * The base class for a search algorithm.
 * Each search algorithm is constructed with a graph as its target. 
 *
 * @author Remko Tron&ccedil;on
 */
public abstract class Algorithm 
{
    private LinkedList _listeners = new LinkedList();
    private Graph _graph;
    private Queue _queue = new Queue();

    /**
     * Basic constructor.
     * Sets target graph of the algorithm and initializes the queue with
     * the start node of the graph.
     * 
     * @param abstractGraph the graph on which the algorithm is applied.
     *              Cannot be <tt>null</tt>
     */
    public Algorithm(Graph abstractGraph) {
        //assert graph != null;
        _graph = abstractGraph;
        
        Path p = new Path(abstractGraph,getGraph().getStart());
        _queue.addFront(p);
    }

    
    /**
     * Executes one loop of the algorithm.
     * If the algorithm is finished, nothing is done.
     */ 
    public void step() { 
        if (!finished()) 
            doStep();
    }

    /**
     * Executes one loop of the algorithm.
     */
    protected abstract void doStep();
    
    /**
     * Checks if the algorithm is finished.
     *
     * @return <tt>true</tt> if the algorithm is finished (i.e. the queue is 
     *                  empty or the goal is reached), false otherwise.
     */
    public boolean finished() {
        return (getQueue().isEmpty() ||
            getQueue().contains(getGraph().getGoal()));
    } 
    
    
    /**
     * Retrieves the graph the algorithm is working on.
     */
    public Graph getGraph() { 
        return _graph; 
    }
    
    
    /**
     * Retrieves the current queue of the algorithm.
     */
    public Queue getQueue() { 
        return _queue; 
    }

    /**
     * Retrieves the textual representation of the state of the algorithm.
     */
    public String getStateString() {
        return getQueue().toString();
    }
    

    /*public void addListener(AlgorithmListener l) {
        _listeners.add(l);
    }

    
    protected void notifyListeners() {
        for (Iterator it = _listeners.iterator(); it.hasNext(); ) {
            ((AlgorithmListener) it.next()).queueChanged();
        }
    }*/
}
