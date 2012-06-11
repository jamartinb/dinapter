package jsearchdemo;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import dinapter.Dinapter;
import dinapter.specificator.JSearchSpecification;
import dinapter.specificator.SimpleSpecification;
import dinapter.specificator.Specification;

/**
 * Estimate extended uniform cost algorithm.
 *
 * @author Remko Tron&ccedil;on
 */
public class EEUniformCostAlgorithm extends Algorithm
{
	protected final Comparator pathComparator = new PathComparator();
	
	private static final boolean QUEUE_ADD_FRONT = Boolean.parseBoolean(Dinapter.getProperty("QUEUE_ADD_FRONT"));
	
    public EEUniformCostAlgorithm(Graph abstractGraph) {
        super(abstractGraph);
    }
    
    
    public void doStep() {
        Path path = getQueue().removeFirst();
        List children = path.getChildren();
        // @toreview Adding to the back should force all solutions of same threshold f value to be found but more nodes will be expanded (Not needed if optimistic heuristic).
        if (QUEUE_ADD_FRONT) {
        	getQueue().addFront(children);
        } else {
        	getQueue().addBack(children);
        }
        updateAccumulatedCosts();
        getQueue().sort(pathComparator);
    }


    public boolean finished() {
    	return getQueue().isEmpty() || getQueue().getFirst().finished();
    }

    public static int computeCost(Path p) {
    	JSearchSpecification specification = (JSearchSpecification)p.getEndNode();
    	if (specification.getAcumulatedCost() != JSearchSpecification.NO_COST)
    		return specification.getAcumulatedCost();
    	else {
    		if (specification.getCost() == JSearchSpecification.NO_COST) {
    			throw new RuntimeException("The specification has no cost.\n"+specification);
    		}
    		List edges = p.getEdges();
    		if (edges.isEmpty())
    			return specification.getCost();
    		else {
    			int previousAccumulatedCost = ((JSearchSpecification)((Edge)edges.get(edges.size()-1)).getBeginNode()).getAcumulatedCost();
    			if (previousAccumulatedCost < 0)
    				previousAccumulatedCost = 0;
    			return  previousAccumulatedCost + specification.getCost();
    		}
    	}
    }
    
    public static int computePartialCost(Path path, Node until) {
    	JSearchSpecification specification = (JSearchSpecification)until;
    	if (specification.getAcumulatedCost() != JSearchSpecification.NO_COST)
    		return specification.getAcumulatedCost();
    	else {
    		if (specification.getCost() == JSearchSpecification.NO_COST) {
    			throw new RuntimeException("The specification has no cost.\n"+specification);
    		}
    		List edges = path.getEdges();
    		for (Object objectEdge:edges) {
    			Edge edge = (Edge)objectEdge;
    			if (edge.getEndNode() == until) {
    				int previousAccumulatedCost = ((JSearchSpecification)edge.getBeginNode()).getAcumulatedCost();
    				if (previousAccumulatedCost < 0)
    					previousAccumulatedCost = 0;
    				return previousAccumulatedCost + specification.getCost();
    			}
    		}
    		return specification.getCost();
    	}
    }
        
    
    public static int computeFValue(Path p) {
        int h = 0;
        try {
            h = ((HeuristicNode) p.getEndNode()).getHeuristic();
        } catch (ClassCastException e) {
        	throw new RuntimeException("Needed an HeuristicNode");
        }
        return h + computeCost(p);
    }
                        
    
    /**
     * Retrieves the textual representation of the state of the algorithm.
     */
    public String getStateString() {
        return getQueue().toString(true,true,true);
    }
    
    protected void updateAccumulatedCosts() {
    	for (Object objectPath:getQueue().getPaths()) {
    		Path path = (Path)objectPath;
    		SimpleSpecification lastSpecification = (SimpleSpecification)path.getEndNode(); 
    		if ((lastSpecification.getAcumulatedCost() < 0) || (lastSpecification.getAcumulatedCost() > computeCost(path))) {
    			String message = "accumulatedCost set to "+computeCost(path);
    			if (lastSpecification.getAcumulatedCost() > computeCost(path)) {
    				List edges = path.getEdges();
    				message = "accumulatedCost set from "+lastSpecification.getAcumulatedCost()+" to "+computeCost(path)+"\n" +
    						"and my parent is: "+((Edge)edges.get(edges.size()-1)).getBeginNode();
    			}
    			lastSpecification.log(message);
    			lastSpecification.setAcumulatedCost(computeCost(path));
    		}
    	}
    }


    private class PathComparator implements Comparator
    {
        public int compare(Object o1, Object o2) {
        	int toReturn = computeFValue((Path) o1) - computeFValue((Path) o2);
        	// TieBreaker: Number of actions in rules.
            if ((toReturn == 0) && (o1 instanceof Specification) && (o2 instanceof Specification)) {
            	toReturn = ((Specification)o2).getActions().length - ((Specification)o1).getActions().length;
            }
            return toReturn;
        }
    }
}
