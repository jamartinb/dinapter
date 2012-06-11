package jsearchdemo;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

public abstract class AbstractGraph implements Graph
{
    /* (non-Javadoc)
	 * @see jsearchdemo.Graph#getStartNode()
	 */
    public abstract Node getStart();
    /* (non-Javadoc)
	 * @see jsearchdemo.Graph#getGoalNode()
	 */
    public abstract Node getGoal();
    /* (non-Javadoc)
	 * @see jsearchdemo.Graph#getOutgoingEdges(jsearchdemo.Node)
	 */
    public abstract List getOutgoingEdges(Node n);

    /* (non-Javadoc)
	 * @see jsearchdemo.Graph#getChildren(jsearchdemo.Node)
	 */
    public List getChildren(Node n) {
        //assert n != null;

        LinkedList l = new LinkedList();

        for (Iterator it = getOutgoingEdges(n).iterator(); it.hasNext(); ) {
            Edge e = (Edge) it.next();
            if (e.getEndNode().equals(n)) 
                l.add(e.getBeginNode());
            else 
                l.add(e.getEndNode());
        }

        return l;
    }
}
