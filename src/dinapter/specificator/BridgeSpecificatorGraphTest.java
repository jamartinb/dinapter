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
 * BridgeSpecificatorGraphTest.java
 *
 * Created on 10 de mayo de 2007, 17:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dinapter.specificator;

import dinapter.behavior.BehaviorNode;
import dinapter.behavior.JPowerBehaviorNode;

import java.util.List;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

// @todo

/**
 *
 * @author arkangel
 */
public class BridgeSpecificatorGraphTest
        extends SpecificatorGraphTestAbstract
            <JPowerBehaviorNode<Object>
            ,DefaultRule<JPowerBehaviorNode<Object>>
            ,SimpleSpecification<DefaultRule<JPowerBehaviorNode<Object>>>
            ,BridgeSpecificatorGraph
                <DefaultRule<JPowerBehaviorNode<Object>>
                ,SimpleSpecification<DefaultRule<JPowerBehaviorNode<Object>>>>
            ,WeighedEdge>{
    
    /**
     * Creates a new instance of BridgeSpecificatorGraphTest
     */
    public BridgeSpecificatorGraphTest() {
    }

    @Override
    @Test
    public void runTest() {
        assertTrue(true);
    }

    protected DefaultRule<JPowerBehaviorNode<Object>> createRule(List<JPowerBehaviorNode<Object>> leftSide, List<JPowerBehaviorNode<Object>> rightSide) {
        return new DefaultRule<JPowerBehaviorNode<Object>>(leftSide,rightSide);
    }

    protected BridgeSpecificatorGraph<DefaultRule<JPowerBehaviorNode<Object>>, SimpleSpecification<DefaultRule<JPowerBehaviorNode<Object>>>> createGraph() {
        return new BridgeSpecificatorGraph<DefaultRule<JPowerBehaviorNode<Object>>, SimpleSpecification<DefaultRule<JPowerBehaviorNode<Object>>>>();
    }

    protected JPowerBehaviorNode<Object> createNode(BehaviorNode.BehaviorNodeType type) {
        return new JPowerBehaviorNode<Object>(type);
    }

    protected SimpleSpecification<DefaultRule<JPowerBehaviorNode<Object>>> getStartNode(BridgeSpecificatorGraph<DefaultRule<JPowerBehaviorNode<Object>>, SimpleSpecification<DefaultRule<JPowerBehaviorNode<Object>>>> graph) {
        return graph.getStart();
    }

    protected SimpleSpecification<DefaultRule<JPowerBehaviorNode<Object>>> createSpecification(List<DefaultRule<JPowerBehaviorNode<Object>>> rules) {
        return new SimpleSpecification<DefaultRule<JPowerBehaviorNode<Object>>>(rules,rules.get(rules.size()-1));
    }

    protected BridgeSpecificatorGraph<DefaultRule<JPowerBehaviorNode<Object>>, SimpleSpecification<DefaultRule<JPowerBehaviorNode<Object>>>> setStartNode(BridgeSpecificatorGraph<DefaultRule<JPowerBehaviorNode<Object>>, SimpleSpecification<DefaultRule<JPowerBehaviorNode<Object>>>> graph, SimpleSpecification<DefaultRule<JPowerBehaviorNode<Object>>> node) {
        graph.setStartNode(node);
        return graph;
    }

    protected SimpleSpecification<DefaultRule<JPowerBehaviorNode<Object>>> setHeuristic(SimpleSpecification<DefaultRule<JPowerBehaviorNode<Object>>> specification, int heuristic) {
        specification.setHeuristic(heuristic);
        return specification;
    }

    protected DefaultRule<JPowerBehaviorNode<Object>> setHeuristic(DefaultRule<JPowerBehaviorNode<Object>> rule, int heuristic) {
        rule.setHeuristic(heuristic);
        return rule;
    }
}
