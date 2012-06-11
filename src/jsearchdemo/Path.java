package jsearchdemo;
import dinapter.specificator.JSearchSpecification;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Path implements Cloneable
{
    private LinkedList _edges = new LinkedList();
    private LinkedList _nodes = new LinkedList();
    private Graph _graph;

    
    /**
     * Constructs a path.
     *
     * @param abstractGraph The graph to which this path belongs. Cannot be
     *              <tt>null</tt>.
     * @param start The start node of the path. Cannot be <tt>null</tt>.
     */ 
    public Path(Graph abstractGraph, Node start) {
        // assert graph != null && start != null;
        _graph = abstractGraph;
        _nodes.add(start);
    }
    
    
    /**
     * Returns all the paths which can be constructed by adding a 
     * child node to the last node of this path. The loop paths 
     * are automatically removed.
     *
     * @return The list of children paths.
     */
    public List getChildren() {
        LinkedList childrenPaths = new LinkedList();
        List childrenEdges = _graph.getOutgoingEdges(getEndNode());
        for (Iterator it = childrenEdges.iterator(); it.hasNext(); ) {
            Edge e = (Edge) it.next();
            /* Loop check */
            if (!contains(e.getBeginNode()) 
                    || !contains(e.getEndNode())) {
                Path p = (Path) clone();
                p.addEdge(e);
                childrenPaths.add(p);
            }
        }

        return childrenPaths;
    }

    
    /**
     * Retrieves the last node of this path.
     */
    public Node getEndNode() {
        return (Node) _nodes.getLast();
    }


    /**
     * Checks if a given node is in this path.
     *
     * @param n The node to check. 
     * @return <tt>true</tt> if the node is in the path, <tt>false</tt>
     *         otherwise.
     */
    public boolean contains(Node n) {
        if (n==null) return false;
        return _nodes.contains(n);
    }
    
    public boolean finished() {
    	for (Object node:_nodes)
    		if (((JSearchSpecification)node).isSolution())
    			return true;
    	return false;
    }


    /**
     * Adds an edge to the end of this path.
     *
     * @param e the edge to add.
     */
    protected void addEdge(Edge e) {
        if (e == null) return;
        if (getEndNode().equals(e.getBeginNode()))
            _nodes.add(e.getEndNode());
        else if (getEndNode().equals(e.getEndNode())) 
            _nodes.add(e.getBeginNode());
        else {
            throw new IllegalArgumentException("Path.addEdge(): Invalid edge");
        }
        _edges.add(e);
    }


    /**
     * Retrieves all the edges in this path.
     *
     * @return A list of edges.
     */
    public List getEdges() {
        return (List) _edges.clone();
    }


    /**
     * Returns the textual representation of the path.
     */
    public String toString() {
        return toString(false,false,false);
    }


    /**
     * Returns the textual representation of the path, with specified 
     * information.
     */
    public String toString(boolean heuristic, boolean cost, boolean f_value) {
        String s = new String("<B>");
        for (Iterator it = _nodes.iterator(); it.hasNext(); ) {
            s += it.next().toString();
        }
        s += "</B>";
        
        int h = 0;
        if (getEndNode() instanceof HeuristicNode) {
            h = ((HeuristicNode) getEndNode()).getHeuristic();
        }
        
        int c = 0;
        for (Iterator it = getEdges().iterator(); it.hasNext(); ) {
            Edge e = (Edge) it.next();
            if (e instanceof WeighedEdge) 
                c += ((WeighedEdge) e).getWeight();
        }

        if (heuristic || cost || f_value) {
            s += " <FONT SIZE=-1><I>(";
            if (heuristic) 
                s += "H:" + h + (cost || f_value ? "," : "");
            if (cost) 
                s += "C:" + c + (f_value ? "," : "");
            if (f_value) 
                s += "F:" + (c+h);
            s += ")</I></FONT>";
        }
        return s;
    }


    /**
     * Creates a copy of this path.
     */
    public Object clone() {
        Path p = new Path(_graph,(Node) _nodes.getFirst());
        p._nodes = (LinkedList) _nodes.clone();
        p._edges = (LinkedList) _edges.clone();
        return p;
    }
}
