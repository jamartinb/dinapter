package jsearchdemo;
import java.util.Iterator;
import java.util.List;

import dinapter.specificator.JSearchSpecification;


/**
 * A* algorithm.
 *
 * @author Remko Tron&ccedil;on
 */
public class AStarAlgorithm extends EEUniformCostAlgorithm
{
    public AStarAlgorithm(Graph abstractGraph) {
        super(abstractGraph);
    }
    
    
    public void doStep() {
        /* Execute the estimation extended uniform cost algorithm */
        super.doStep();
        /* Prune queue */
        List paths = getQueue().getPaths();
        for (Iterator i = paths.iterator(); i.hasNext(); ) {
            Path p = (Path) i.next();
            for (Iterator j = paths.iterator(); j.hasNext(); ) {
                Path q = (Path) j.next();
                if (!p.equals(q) && q.contains(p.getEndNode()) 
                        && computeCost(p) >= computePartialCost(q,p.getEndNode())) {
                    getQueue().remove(p); 
                }
            }
        }
    }
}
