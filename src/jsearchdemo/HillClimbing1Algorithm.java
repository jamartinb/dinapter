package jsearchdemo;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;


/**
 * Hill Climbing 1 algorithm.
 *
 * @author Remko Tron&ccedil;on
 */
public class HillClimbing1Algorithm extends Algorithm
{
    public HillClimbing1Algorithm(Graph abstractGraph) {
        super(abstractGraph);
    }
    
    
    public void doStep() {
        Path p = getQueue().removeFirst();
        List children = p.getChildren();
        Collections.sort(children,new PathComparator());
        getQueue().addFront(children);
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
            int hv1 = 0;
            try {
                hv1 = ((HeuristicNode) ((Path) o1).getEndNode()).
                        getHeuristic();
            }
            catch (ClassCastException e) { }   
            
            int hv2 = 0;
            try {
                hv2 = ((HeuristicNode) ((Path) o2).getEndNode()).
                        getHeuristic();
            }
            catch (ClassCastException e) { }   
            
            return hv1 - hv2;
        }
    }
}
