package jsearchdemo;
import java.util.List;


/**
 * A depth first algorithm.
 *
 * @author Remko Tron&ccedil;on
 */
public class DepthFirstAlgorithm extends Algorithm
{
    public DepthFirstAlgorithm(Graph abstractGraph) {
        super(abstractGraph);
    }
    
    
    public void doStep() {
        Path p = getQueue().removeFirst();
        List children = p.getChildren();
        getQueue().addFront(children);
    }
}
