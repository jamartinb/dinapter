package jsearchdemo;
/**
 * A node for the <i>Farmer, Fox, Goose, Grain</i> problem.
 * The node represents a state of the world. The state is represented by one
 * integer, where each bit means that the object on the corresponding position is
 * either on the left or the right side of the river. (e.g. 
 * <tt>farmer|fox<tt> represents the state where the farmer and the fox are on 
 * the right bank, and all the other objects are on the left bank.
 *
 * @see FFGGGraph
 * @author Remko Tron&ccedil;on
 */
public class FFGGNode implements Node
{
    private char _state = 0;

    /**
     * The representation for the farmer.
     */
    public static final char farmer = 0x1;

    /**
     * The representation for the fox.
     */
    public static final char fox = 0x2;

    /**
     * The representation for the goose.
     */
    public static final char goose = 0x4;

    /**
     * The representation for the grain.
     */
    public static final char grain = 0x8;

    
    /**
     * Transports the given objects to the other side of the river
     *
     * @param objects the objects to transport
     */
    public void transport(int objects) {
        _state ^= objects;
    }

    
    /**
     * Checks if objects are on the same side.
     *
     * @param objects the objects to check
     * @return <tt>true</tt> if the objects are on the same bank of the river,
     *         <tt>false</tt> otherwise.
     */
    public boolean onSameSide(int objects) {
        return ((_state & objects) == 0 || (~_state & objects) == 0);
    }


    /**
     * Creates a node representing the same state as the current.
     */
    public Object clone() {
        FFGGNode n = new FFGGNode();
        n._state = _state;
        return n;
    }


    /**
     * Compares this node to another node for equality.
     *
     * @param o the object to compare with. 
     * @return <tt>true</tt> if the other node is a FFGGNode representing the
     *         same state as the current node.
     */
    public boolean equals(Object o) {
        try {
            return _state == ((FFGGNode) o)._state;
        }
        catch (ClassCastException e) {
            return false;
        }
    }

    
    public int compareTo(Object o) {
        if (o == null) 
            return -1;
        try {
            return _state - ((FFGGNode) o)._state;
        }
        catch (ClassCastException e) {
            return -1;
        }
    }


    public String toString() {
        String s = new String("[");
        if ((_state & farmer) == 0) s += "Fa";
        if ((_state & fox) == 0) s += "Fo";
        if ((_state & goose) == 0) s += "Go";
        if ((_state & grain) == 0) s += "Gr";
        s += "|";
        if ((_state & farmer) != 0) s += "Fa";
        if ((_state & fox) != 0) s += "Fo";
        if ((_state & goose) != 0) s += "Go";
        if ((_state & grain) != 0) s += "Gr";
        s += "]";
        return s;
    }
}
