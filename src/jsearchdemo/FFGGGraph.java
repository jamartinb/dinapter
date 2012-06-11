package jsearchdemo;
import java.util.LinkedList;
import java.util.List;


/**
 * A graph for the <i>Farmer, Fox, Goose and Grain</i> problem.
 * This graph is dynamic: the children are always explicitly computed
 * upon request.
 *
 * @author Remko Tron&ccedil;on
 */
public class FFGGGraph extends AbstractGraph
{
    private FFGGNode _start = new FFGGNode();
    private FFGGNode _goal;

    
    /**
     * Constructs the FFGG graph.
     * The starting node of the graph is set to the state where all
     * objects (farmer, fox, goose and grain) are on the left bank of the
     * river.
     */
    public FFGGGraph() {
        _goal = new FFGGNode();
        _goal.transport(FFGGNode.farmer | FFGGNode.fox 
                        | FFGGNode.goose | FFGGNode.grain);
    }


    /**
     * Retrieves the start node of the graph.
     * 
     * @return a node representing the state where all objects (farmer, fox,
     *         goose and grain) are on the left bank of the river.
     */
    public Node getStart() {
        return _start;
    }
    

    /**
     * Retrieves the goal node of the graph.
     * 
     * @return a node representing the state where all objects (farmer, fox,
     *         goose and grain) are on the right bank of the river.
     */
    public Node getGoal() {
        return _goal;
    }


    /**
     * Retrieves the outgoing edges of the given node.
     * The edges leaving a node are all the possible, legal moves.
     *
     * @param node the node of which the outgoing edges are to be returned
     * @return a list of edges leaving this node.
     */
    public List getOutgoingEdges(Node node) {
        LinkedList l = new LinkedList();
        try {
            FFGGNode n = (FFGGNode) node;
            if (n.onSameSide(FFGGNode.farmer | FFGGNode.fox) 
                    && !n.onSameSide(FFGGNode.grain | FFGGNode.goose)) {
                FFGGNode nn = (FFGGNode) n.clone();
                nn.transport(FFGGNode.farmer | FFGGNode.fox);
                l.add(new DefaultEdge(node,nn));
            }
            if (n.onSameSide(FFGGNode.farmer | FFGGNode.grain) 
                    && !n.onSameSide(FFGGNode.fox | FFGGNode.goose)) {
                FFGGNode nn = (FFGGNode) n.clone();
                nn.transport(FFGGNode.farmer | FFGGNode.grain);
                l.add(new DefaultEdge(node,nn));
            }
            if (n.onSameSide(FFGGNode.farmer | FFGGNode.goose)) {
                FFGGNode nn = (FFGGNode) n.clone();
                nn.transport(FFGGNode.farmer | FFGGNode.goose);
                l.add(new DefaultEdge(node,nn));
            }
            if (!n.onSameSide(FFGGNode.goose | FFGGNode.grain) 
                    && !n.onSameSide(FFGGNode.fox | FFGGNode.goose)) {
                FFGGNode nn = (FFGGNode) n.clone();
                nn.transport(FFGGNode.farmer);
                l.add(new DefaultEdge(node,nn));
            }
        }
        catch (ClassCastException e) { }

        return l;
    }
}
