package jsearchdemo;
/**
 * A class to represent a basic edge.
 * Each edge has a begin- and an endnode.
 *
 * @author Remko Tron&ccedil;on
 */
public class DefaultEdge implements Edge
{
    private Node _begin;
    private Node _end;

    
    /**
     * Constructs an edge.
     *
     * @param begin the begin node of the edge. Cannot be <tt>null</tt>.
     * @param end the end node of the edge. Cannot be <tt>null</tt>.
     */
    public DefaultEdge(Node begin, Node end) {
        //assert (begin != null && end != null);
        _begin = begin;
        _end = end;    
    }
    

    /* (non-Javadoc)
	 * @see jsearchdemo.Edge#getBeginNode()
	 */
    public Node getBeginNode() {
        return _begin;
    }


    /* (non-Javadoc)
	 * @see jsearchdemo.Edge#getEndNode()
	 */
    public Node getEndNode() {
        return _end;
    }


    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((_begin == null) ? 0 : _begin.hashCode());
		result = PRIME * result + ((_end == null) ? 0 : _end.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof DefaultEdge))
			return false;
		final DefaultEdge other = (DefaultEdge) obj;
		if (_begin == null) {
			if (other._begin != null)
				return false;
		} else if (!_begin.equals(other._begin))
			return false;
		if (_end == null) {
			if (other._end != null)
				return false;
		} else if (!_end.equals(other._end))
			return false;
		return true;
	}


    /**
     * Returns the string representation of this edge.
     */
    public String toString() {
        return _begin.toString() + "-" + _end.toString();
    }
}
