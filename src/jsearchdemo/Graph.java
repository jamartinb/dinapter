/**
 * Author:  José Antonio Martín Baena
 * email:   jose.antonio.martin.baena@gmail.com
 *
 * This code is under the GPL License for any non-comercial project
 * after a notification is sent to the authors. For any other kind of
 * project an individual license must be obtained from the authors.
 *
 * This software is available as it is without any warranty or responsability
 * being held by the author. If you use this is at your own risk.
 *
 * Hope this code is usefull for you. Enjoy it!. ;)
 */
package jsearchdemo;

import java.util.List;

/**
 * @author José Antonio Martín Baena
 *
 */
public interface Graph {

	public Node getStart();

	public Node getGoal();

	public List getOutgoingEdges(Node n);

	/**
	 * Retrieves the children of the given node.
	 *
	 * @param n The node to retrieve the children from. Cannot be 
	 *           <tt>null</tt>
	 * @return The list of children nodes.
	 */
	public List getChildren(Node n);

}
