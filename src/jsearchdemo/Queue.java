package jsearchdemo;
import java.util.Iterator;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;


/**
 * A class to represent queues of paths. 
 *
 * @see Path
 * @author Remko Tron&ccedil;on
 */
public class Queue
{
    private LinkedList _queue = new LinkedList();

    
    /**
     * Constructs a new, empty queue.
     */
    public Queue() { }


    /**
     * Retrieves the first path of the queue.
     */
    public Path getFirst() {
        return (Path) _queue.getFirst();
    }

    public int getSize() {
	return _queue.size();
    }


    /**
     * Removes the first path of the queue.
     */
    public Path removeFirst() {
        return (Path) _queue.removeFirst(); 
    }    


    /**
     * Removes all paths from the queue.
     */
    public List removeAll() {
        List l = (List) _queue.clone();
        _queue.clear();
        return l;
    }

    /**
     * Removes a given path from the queue.
     */
    public void remove(Path p) {
        if (p == null) return;
        _queue.remove(p);
    }
    

    /**
     * Adds a list of paths to the front of the queue.
     */
    public void addFront(List l) {
        if (l == null) return;
        _queue.addAll(0,l);
    }


    /**
     * Adds a path to the front of the queue.
     */
    public void addFront(Path p) {
        if ( p == null) return;
        _queue.addFirst(p);
    }


    /**
     * Adds a list of paths to the back of the queue.
     */
    public void addBack(List l) {
        if (l == null) return;
        _queue.addAll(l);
    }


    /**
     * Adds a path to the back of the queue.
     */
    public void addBack(Path p) {
        if (p == null) return;
        _queue.addLast(p);
    }


    /**
     * Retrieves a list of all the paths in the queue.
     */
    public List getPaths() {
        return (List) _queue.clone();
    }


    /**
     * Checks if the queue is empty.
     *
     * @return <tt>true</tt> if the queue contains no paths, <tt>false</tt>
     *         otherwise.
     */
    public boolean isEmpty() {
        return _queue.isEmpty();
    }


    /**
     * Sorts the paths in the queue using a given comparator.
     *
     * @param c The comparator for sorting paths. Cannot be <tt>null</tt>.
     */
    public void sort(Comparator c) {
        // assert c != null;
        Collections.sort(_queue,c);
    }


    /**
     * Returns the textual representation of the queue.
     */
    public String toString() {
        return toString(false,false,false);
    }

    /**
     * Returns the textual representation of the queue, with specified
     * information.
     */
    public String toString(boolean heuristic, boolean cost, boolean f_value) {
        String s = new String();
        for (Iterator it = _queue.iterator(); it.hasNext(); ) {
            s = s.concat(((Path) it.next()).toString(heuristic, cost, f_value)); 
            if (it.hasNext())
                s = s.concat(", ");
        }
        return s;
    } 


    /**
     * Checks if a path in the queue contains the given node.
     */
    public boolean contains(Node n) {
        if (n == null) return false;
        for (Iterator it = getPaths().iterator(); it.hasNext(); ) {
            Path p = (Path) it.next();
            if (p.contains(n))
                return true;
        }
        return false;
    }


    /**
     * Checks if a path in the queue contains the given edge.
     */
    public boolean contains(Edge e) {
        if (e == null) return true;
        for (Iterator it = getPaths().iterator(); it.hasNext(); ) {
            Path p = (Path) it.next();
            if (p.getEdges().contains(e))
                return true;
        }
	    return false;
    }
}
