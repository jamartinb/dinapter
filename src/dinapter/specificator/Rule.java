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
package dinapter.specificator;

import java.util.List;

import dinapter.behavior.BehaviorNode;

/**
 * A rule stablishes a direct match between actions between two components.
 * A rule means that <u>a sequence of actions from one component must be
 * translated to another sequence of actions in the other component</u>. These
 * two sequences are the sides of the rule. Rules may also be <i>interleaved</i> or
 * <i>required</i>.
 * <p>Both adaptation rule sides must contain BehaviorNodes which belong
 * to BehaviorNodeType.SEND or BehaviorNodeType.RECEIVE.</p>
 * <p>It's <b>highly recommended</b> to implement equals and hashCode methods</p>
 * @author José Antonio Martín Baena
 * @version $Revision: 449 $ - $Date: 2007-02-01 20:05:11 +0100 (jue, 01 feb 2007) $
 */
public interface Rule<N extends BehaviorNode> {
    /**
     * It returns the left side of the rule.
     * @return The left side of the rule.
     */
	public List<N> getLeftSide();
    
    /**
     * It returns the right side of the rule.
     * @return The right side of the rule.
     */
	public List<N> getRightSide();
    
    /**
     * It returns whether this rule is required or not.
     * @return Whether the rule is required.
     */
	public boolean isRequired();
    
    /**
     * It returns whether the rule may be interleaved.
     * @return Whether the rule may be interleaved.
     */
	public boolean isInterleaved();
    
    /**
     * @see Object#equals(Object)
     */
	public boolean equals(Object o);
    
    /**
     * @see Object#hashCode()
     */
	public int hashCode();
}
