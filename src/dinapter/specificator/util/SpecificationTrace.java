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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jess.JessException;
import jess.QueryResult;
import jess.Rete;
import jess.ValueVector;
import dinapter.specificator.Rule;
import dinapter.specificator.Specification;
import dinapter.specificator.SpecificatorGraph;

public class SpecificationTrace<R extends Rule, S extends Specification<R>, G extends SpecificatorGraph<S>> {
	
	protected final Rete jess;
	
	public SpecificationTrace(Rete rete) {
		this.jess = rete;
	}
	
	@SuppressWarnings("unchecked")
	public G getSpecificationTrace(S specification, G destination) {
		try {
			ValueVector vv = new ValueVector(1);
			vv.add(specification);
			QueryResult result = jess.runQueryStar("query-graphs", vv);
			if (!result.next()) {
				throw new IllegalArgumentException("It couldn't find any graph for the given specification");
			}
			G graph = (G)result.getObject("graph");
			return getSpecificationTrace(graph, destination);
		} catch (JessException e) {
			throw new RuntimeException("Exception thrown by Jess when retrieving the graph.",e);
		}
	}
	
	public G getSpecificationTrace(G graph, G destination) {
		destination.includeGraph(graph);
		if (!getCopiedParentSpecifications(graph).isEmpty()) {
			replaceCopiedSpecifiations(destination, getCopiedParentSpecifications(graph), getParents(graph, getParentGraphs(graph)));
			for (G parentGraph:getParentGraphs(graph)) {
				getSpecificationTrace(parentGraph, destination);
			}
		}
		return destination;
	}
	
	private S getMergedSpecification(G graph) {
		S node = graph.getStartNode();
		Collection<S> children = null;
		if (node != null) {
			for (int i=0; i < 2; i++) {
				children = graph.getChildren(node);
				if (children.size() == 1) {
					node = children.iterator().next();
				} else {
					node = null;
					break;
				}
			}
			if ((node != null) && node.isMerged()) {
				return node;
			} else {
				return null;
			}
		}
		return null;
	}
	
	private Collection<S> getCopiedParentSpecifications(G graph) {
		S mergedSpecification = getMergedSpecification(graph);
		if (mergedSpecification == null) {
			return Collections.emptySet();
		}
		return graph.getParents(mergedSpecification);
	}
	
	@SuppressWarnings("unchecked")
	private Collection<G> getParentGraphs(G graph) {
		ValueVector vv = new ValueVector(1);
		vv.add(graph);
		Set<G> toReturn = new HashSet<G>();
		try {
			QueryResult results = jess.runQueryStar("query-relation", vv);
			while (results.next()) {
				toReturn.add((G)results.getObject("parent"));
			}
		} catch (JessException e) {
			throw new RuntimeException("Exception thrown while retrieving an specification trace.",e);
		}
		return toReturn;
	}
	
	private Collection<S> getParents(G graphWithCopies, Collection<G> parentGraphs) {
		Collection<S> copiedParents = getCopiedParentSpecifications(graphWithCopies);
		Set<S> toReturn = new HashSet<S>();
		for (G parentGraph:parentGraphs) {
			int count = 0;
			for (S specification:parentGraph.getAllNodes()) {
				for (S copiedParent:copiedParents) {
					if (copiedParent.isEquivalent(specification)) {
						toReturn.add(specification);
						count++;
					}
				}
			}
			if (count != 1) {
				throw new RuntimeException("The number of parent specifications found in a parent graph is not 1 ("+count+")");
			}
		}
		return toReturn;
	}
	
	private void replaceCopiedSpecifiations(G graphWithCopies, Collection<S> copies, Collection<S> originals) {
		for (S copy:copies) {
			for (S real:originals) {
				if (copy.isEquivalent(real)) {
					replaceSpecification(graphWithCopies, copy, real);
				}
			}
		}
	}
	
	private void replaceSpecification(G graph, S toReplace, S substitution) {
		Collection<S> parents = graph.getParents(toReplace);
		Collection<S> children = graph.getChildren(toReplace);
		graph.removeNode(toReplace);
		for (S parent:parents) {
			graph.addEdge(parent, substitution);
		}
		for (S child:children) {
			graph.addEdge(substitution, child);
		}
	}
	
}
