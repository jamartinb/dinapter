package jsearchdemo;
import java.util.List;
import java.util.Comparator;

/**
 * Greedy search algorithm.
 *
 * @author Remko Tron&ccedil;on
 */
public class GreedySearchAlgorithm extends Algorithm
{
    public GreedySearchAlgorithm(Graph abstractGraph) {
        super(abstractGraph);
    }
    
    
    public void doStep() {
        Path path = getQueue().removeFirst();
        List children = path.getChildren();
        getQueue().addFront(children);
        getQueue().sort(new PathComparator());
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
