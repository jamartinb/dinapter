package jsearchdemo;
/**
 * A class to represent named nodes.
 *
 * @author Remko Tron&ccedil;on
 */
public class NamedNode implements Node
{
    private String _name;

    /**
     * Creates a named node.
     *
     * @param name The name of the node. Cannot be <tt>null</tt>.
     */
    public NamedNode(String name) {
        //assert name != null;
        _name = name;
    }


    /**
     * Retrieves the name of the node.
     */
    public String getName() {
        return _name;
    }
    
    
    /**
     * Sets the name of the node.
     *
     * @param name The name of the node. Cannot be <tt>null</tt>.
     */
    public void setName(String name) {
	    _name = name;
    }

    public int compareTo(Object o) {
        if (o == null)
            return -1;
        try {
            return _name.compareTo(((NamedNode) o).getName());
        }
        catch (ClassCastException e) {
                return -1;
        }
    }

    
    /**
     * Checks if a given object is equal to this node.
     *
     * @param o the object to compare with.
     */
    public boolean equals(Object o) {
	    if (o == null)
	        return false;
        try {
            return ((NamedNode) o).getName().equals(getName());
        }
        catch (ClassCastException e) {
            return false;
        }
    }


    /**
     * Returns the textual representation of a node
     */
    public String toString() {
        return getName();
    }
}
