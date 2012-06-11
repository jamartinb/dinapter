/*
 * This file is part of Dinapter.
 *
 *  Dinapter is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Dinapter is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  (C) Copyright 2007 José Antonio Martín Baena
 *  
 *  José Antonio Martín Baena <jose.antonio.martin.baena@gmail.com>
 *  Ernesto Pimentel Sánchez <ernesto@lcc.uma.es>
 */
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
package dinapter.behavior;

import java.util.List;

// * <li><b><code>TAU</code></b> - Internal action of the component.

/**
 * This class represents the <i>noticeable</i> actions which may compose the 
 * behavior of a software component. These component action are intended to 
 * be inside a behavior graph and hence the name of the class.
 * Every behavior node has:
 * <ul>
 * <li><b>A short description</b>: may be very well a method or action name.
 * <li><b>A type</b>: Which represents the particular kind of action (explained below).
 * <li><b>A list of arguments</b>: The action functionality depends on these arguments.
 * </ul>
 * Behavior nodes are also different in their kind:
 * <ul>
 * <li><b><code>START</code></b> - This is not an action by itself but it represents
 * that the component behavior starts from this point.
 * <li><b><code>SEND</code></b> - This is a synchronized action which sends some
 * arguments calling the given method. It must be complemented by it corresponding...
 * <li><b><code>RECEIVE</code></b> - This synchronized action waits for a call to this 
 * method and requires the specified arguments.
 * <li><b><code>IF</code></b> - This is a bifurcation in the component behavior. The
 * component may choose any of the following actions which must be of <code>SEND</code> type.
 * <li><b><code>PICK</code></b> - This is the complementary action for <code>SWITCH</code>es.
 * Once in this action the component may accept any of the following <code>RECEIVE</code>es.
 * <li><b><code>FLOW</code></b> - All the following actions are performed concurrently. This
 * concurrency may eventually end at a
 * <li><b><code>JOIN</code></b> - It joins all branches from a <code>FLOW</code>.
 * <li><b><code>WHILE</code></b> - <b>DEPRECATED</b>: In cyclic graphs this action isn't needed.
 * <li><b><code>EXIT</code></b> - A component, in order to finish consistently, must end in
 * this kind of action.
 * </ul>
 * @param <A> Class of the arguments of the behavior node.  
 * @author José Antonio Martín Baena
 * @version $Revision: 451 $ - $Date: 2007-02-06 10:52:47 +0100 (mar, 06 feb 2007) $
 */
public interface BehaviorNode<A> {
    
    /**
     * All the different kinds of behavior nodes.
     * @author José Antonio Martín Baena
     * @version $Revision: 451 $ - $Date: 2007-02-06 10:52:47 +0100 (mar, 06 feb 2007) $
     */
	public static enum BehaviorNodeType {START,EXIT,JOIN,FLOW,IF,PICK,WHILE,RECEIVE,SEND};
	
    /**
     * It returns the type of this behavior node.
     * @return The type of this behavior node.
     */
	public BehaviorNodeType getType();
    
    /**
     * It returns the list of arguments of this behavior node.
     * @return The list of arguments of this behavior node.
     */
	public List<A> getArguments();
    
    /**
     * It returns the description of this behavior node.
     * @return The description of this behavior node.
     */
	public Object getDescription();
}
