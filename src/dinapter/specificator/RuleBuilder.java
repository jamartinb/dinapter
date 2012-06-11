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
/*
 * RuleBuilder.java
 *
 * Created on 10 de mayo de 2007, 14:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dinapter.specificator;

import dinapter.behavior.BehaviorNode;
import java.util.List;

/**
 *
 * @author arkangel
 */
public interface RuleBuilder<N extends BehaviorNode, R extends Rule<N>> {
    /**
     * It resets the builder for creating a new rule.
     * New rules conserve previous required and interleaved arguments as default values.
     */
    void createRule();

    /**
     * It returns the newly created rule or it actually creates a new one
     * if any argument has been changed.
     * 
     * @return The new rule.
     */
    R getRule();

    /**
     * It flags next rule to be interleaved 
     * 
     * @param interleaved Whether or not the next rule is interleaved.
     */
    void setInterleaved(boolean interleaved);

    /**
     * It sets next rule left sided actions.
     * 
     * @param nodes Actions to be in the left side of the rule.
     */
    @SuppressWarnings("unchecked")
    void setLeftSide(List<N> nodes);

    /**
     * It sets next rule left sided actions.
     * 
     * @param nodes Actions to be in the left side of the rule.
     */
    void setLeftSide(N... nodes);

    /**
     * It flags next rule to be required. 
     * 
     * @param required Wehter next rule is going to be required or not.
     */
    void setRequired(boolean required);

    /**
     * It sets next rule right sided actions. 
     * 
     * @param nodes Actions to be in the right side of the rule.
     */
    @SuppressWarnings("unchecked")
    void setRightSide(List<N> nodes);

    /**
     * It sets next rule right sided actions. 
     * 
     * @param nodes Actions to be in the right side of the rule.
     */
    void setRightSide(N... nodes);
    
}
