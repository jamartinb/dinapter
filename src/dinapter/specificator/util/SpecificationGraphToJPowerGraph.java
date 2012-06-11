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
package dinapter.specificator.util;

import java.util.HashMap;
import java.util.Map;

import dinapter.specificator.JPowerSpecification;
import dinapter.specificator.JPowerSpecificatorGraph;
import dinapter.specificator.JSearchSpecification;
import dinapter.specificator.Rule;
import dinapter.specificator.SpecificatorGraph;

public class SpecificationGraphToJPowerGraph {
	
	public <R extends Rule, S extends JSearchSpecification<R>>
			JPowerSpecificatorGraph<R, JPowerSpecification<R>> 
			copyToJPowerSpecificatorGraph(SpecificatorGraph<S> origin) {
		JPowerSpecificatorGraph<R, JPowerSpecification<R>> toReturn =
			new JPowerSpecificatorGraph<R, JPowerSpecification<R>>();
		Map<JSearchSpecification<R>, JPowerSpecification<R>> converted =
			new HashMap<JSearchSpecification<R>, JPowerSpecification<R>>(origin.getAllNodes().size());
		for (S specification:origin.getAllNodes()) {
			converted.put(specification, copyToJPowerSpecification(specification));
		}
		toReturn.setStartNode(converted.get(origin.getStartNode()));
		for (S specification:origin.getAllNodes()) {
			for (S child:origin.getChildren(specification)) {
				toReturn.addEdge(converted.get(specification), converted.get(child));
			}
		}
		return toReturn;
	}
	
	private <R extends Rule> JPowerSpecification<R> copyToJPowerSpecification(JSearchSpecification<R> from) {
		JPowerSpecification<R> toReturn = new JPowerSpecification<R>(from.getRules(),from.getWorkingRule());
		toReturn.setHeuristic(from.getHeuristic());
        toReturn.setCost(from.getCost());
        toReturn.setChildrenNeeded(from.isChildrenNeeded());
        toReturn.setChildrenReady(from.isChildrenReady());
        toReturn.setLeftPath(from.getLeftPath());
        toReturn.setRightPath(from.getRightPath());
        toReturn.setCopiedSpecification(from.isCopiedSpecification());
        toReturn.setSolution(from.isSolution());
        toReturn.setBestSolution(from.isBestSolution());
        toReturn.setAcumulatedCost(from.getAcumulatedCost());
		return toReturn;
	}
}
