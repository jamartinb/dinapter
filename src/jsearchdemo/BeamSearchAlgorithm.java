package jsearchdemo;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;


/**
 * Beam search algorithm.
 *
 * @author Remko Tron&ccedil;on
 */
public class BeamSearchAlgorithm extends Algorithm
{
    private int _width;

    
    /**
     * Constructs the Beam Search algorithm with a given graph, and
     * a given width.
     */
    public BeamSearchAlgorithm(Graph abstractGraph, int width) {
        super(abstractGraph);
        _width = width;
    }
    
    
    public void doStep() {
        LinkedList list = new LinkedList();
        List paths = getQueue().removeAll();

        /* Create paths to all the children */
        for (Iterator it = paths.iterator(); it.hasNext(); ) {
            list.addAll(((Path) it.next()).getChildren());
        }

        /* Sort new list */
        Collections.sort(list,new PathComparator());

        /* Add width best paths to the queue */
        int i = 0;
        for (Iterator it = list.iterator(); i < _width && it.hasNext(); i++) 
            getQueue().addBack((Path) it.next());
    }


    private int computeHeuristic(Node n) {
        int h = 0;
        try {
            h = ((HeuristicNode) n).getHeuristic();
        }
        catch (ClassCastException e) { }
        return h;
    }

    
    /**
     * Retrieves the textual representation of the state of the algorithm.
     */
    public String getStateString() {
        return getQueue().toString(true,false,false);
    }
    

    private class PathComparator implements Comparator
    {
        public int compare(Object o1, Object o2) {
            return computeHeuristic(((Path) o1).getEndNode()) - 
                        computeHeuristic(((Path) o2).getEndNode());
        }
    }
}
