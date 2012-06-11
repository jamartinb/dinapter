package jsearchdemo;

public class DefaultWeighedEdge extends DefaultEdge implements WeighedEdge{
    private int _weight;

    /**
     * Constructs a weighed edge.
     *
     * @param begin the begin node of the edge. Cannot be <tt>null</tt>.
     * @param end the end node of the edge. Cannot be <tt>null</tt>.
     * @param weight the weight of the edge.
     */
    public DefaultWeighedEdge(Node begin, Node end, int weight) {
        super(begin,end);
        _weight = weight;
    }


    /**
     * Retrieves the weight of the edge.
     */
    public int getWeight() {
        return _weight;
    }

    /**
     * Sets the weight of the edge.
     */
    public void setWeight(int w) {
	    _weight = w;
    }
}
