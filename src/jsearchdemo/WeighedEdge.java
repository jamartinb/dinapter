package jsearchdemo;
/**
 * A class to represent weighed edges.
 *
 * @author Remko Tron&ccedil;on
 */
public interface WeighedEdge extends Edge {
    /**
     * Retrieves the weight of the edge.
     */
    public int getWeight();

    /**
     * Sets the weight of the edge.
     */
    public void setWeight(int w);
}
