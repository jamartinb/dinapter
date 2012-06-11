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
 * MapGraph.java
 *
 * Created on 10 de mayo de 2007, 12:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dinapter.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

// @todo A possible enhancement could be a double map: one for parents and other for children.

/**
 * This implementation uses a Map and it doesn't support several edges between the same two nodes.
 * @author José Antonio Martín Baena
 */
public class MapGraph<N> extends AbstractModifiableGraph<N> implements ModifiableGraph<N> {
    
    private final Map<N,Set<N>> map;
    private static final Set NO_CHILDREN_SET = Collections.EMPTY_SET;
    private static final int ARRAY_INITIAL_CAPACITY = 2;
    private static final boolean unconnectedRemoval = true;
    
    /**
     * Creates a new instance of MapGraph
     */
    public MapGraph() {
        this(null);
    }
    
    public MapGraph(Map<N,Set<N>> innerMap) {
        if (innerMap == null)
            map = createMap();
        else
            map = innerMap;
    }
    
    protected Map<N,Set<N>> createMap() {
        return new HashMap<N,Set<N>>();
    }

    @Override
    public void removeEdge(N from, N to) {
        getChildren(from).remove(to);
        if (unconnectedRemoval && (getStartNode() != to) && getParents(to).isEmpty()) {
        	removeNode(to);
        	
        }
    }

    @Override
    public void setStartNode(N node) {
        addNode(node);
        super.setStartNode(node);
    }

    @Override
    public void setEndNode(N node) {
    	addNode(node);
        super.setEndNode(node);
    }

    @Override
    public Collection<N> getAllNodes() {
        return map.keySet();
    }

    @Override
    public void addEdge(N from, N to) {
        Set<N> children;
        if (addNode(from) || (map.get(from) == NO_CHILDREN_SET)) {
            children = new HashSet<N>(ARRAY_INITIAL_CAPACITY);
        } else {
            children = map.get(from);
        }
        children.add(to);
        addNode(to);
        map.put(from,children);
    }
    
    @SuppressWarnings("unchecked")
    private boolean addNode(N node) {
        boolean toReturn;
        boolean empty = map.isEmpty();
        if (toReturn = !map.containsKey(node))
            map.put(node,(Set<N>)NO_CHILDREN_SET);
        if (empty) setStartNode(node);
        return toReturn;
    }

    @Override
    public void removeNode(N node) {
    	for (N to:new HashSet<N>(getChildren(node))) {
    		if (!unconnectedRemoval || containsNode(node)) {
    			removeEdge(node,to);
    		}
        }
        for (N parent:new HashSet<N>(getParents(node))) {
        	if (!unconnectedRemoval || containsNode(parent)) {
        		removeEdge(parent,node);
        	}
        }
        if (!unconnectedRemoval || containsNode(node)) {
        	map.remove(node);
        }
    }

    @Override
    public Collection<N> getParents(N node) {
        List<N> parents = new LinkedList<N>();
        for (N parent:getAllNodes())
            if (getChildren(parent).contains(node)) parents.add(parent);
        return parents;
    }

    @Override
    public Collection<N> getChildren(N node) {
        Collection<N> children = map.get(node);
        if (children == null)
            throw new IllegalArgumentException("That node doesn't belong to this graph.");
        return children;
    }

    @Override
    public boolean containsNode(N node) {
        return map.containsKey(node);
    }
}
