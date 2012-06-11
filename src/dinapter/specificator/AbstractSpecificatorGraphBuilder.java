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
 * AbstractSpecificatorGraphBuilder.java
 *
 * Created on 28 de mayo de 2007, 15:33
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dinapter.specificator;

import dinapter.graph.AbstractGraphBuilder;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author arkangel
 */
public abstract class AbstractSpecificatorGraphBuilder<R extends Rule,S extends Specification<R>,G extends SpecificatorGraph<S>> 
        extends AbstractGraphBuilder<S,G> implements SpecificatorGraphBuilder<R,S,G> {
    
    public S createSpecification(R... rules) {
        return createSpecification(Arrays.asList(rules));
    }
    
	@SuppressWarnings("unchecked")
	public S copySpecification(S from) {
		S toReturn = createSpecification(from.getRules(),from.getWorkingRule());
        toReturn.setChildrenNeeded(from.isChildrenNeeded());
        toReturn.setChildrenReady(from.isChildrenReady());
        toReturn.setLeftPath(from.getLeftPath());
        toReturn.setRightPath(from.getRightPath());
        toReturn.setCopiedSpecification(true);
        if ((from instanceof JSearchSpecification) && (toReturn instanceof JSearchSpecification)) {
            JSearchSpecification spec = (JSearchSpecification)toReturn;
            JSearchSpecification fro = (JSearchSpecification)from;
            spec.setAcumulatedCost(fro.getAcumulatedCost());
            spec.setHeuristic(fro.getHeuristic());
            spec.setCost(fro.getCost());
        }
        return toReturn;
	}

	public S createSpecification(List<R> rules) {
		return createSpecification(rules, rules.isEmpty()?null:rules.get(rules.size()-1));
	}
}
