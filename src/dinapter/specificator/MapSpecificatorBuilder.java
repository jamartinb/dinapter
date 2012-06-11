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
 * MapSpecificatorBuilder.java
 *
 * Created on 28 de mayo de 2007, 16:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dinapter.specificator;

import dinapter.behavior.JPowerBehaviorNode;
import dinapter.graph.MapGraph;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author arkangel
 */
public class MapSpecificatorBuilder<A>
        extends SpecificatorBuilderFacade
            <A
            ,JPowerBehaviorNode<A>
            //,JPowerBehaviorGraph<A,JPowerBehaviorNode<A>,Edge>
            ,DefaultRule<JPowerBehaviorNode<A>>
            ,SimpleSpecification<DefaultRule<JPowerBehaviorNode<A>>>
            ,BridgeSpecificatorGraph<DefaultRule<JPowerBehaviorNode<A>>,SimpleSpecification<DefaultRule<JPowerBehaviorNode<A>>>>>{
    
    /**
     * Creates a new instance of MapSpecificatorBuilder
     */
    public MapSpecificatorBuilder() {
    }

    public RuleBuilder<JPowerBehaviorNode<A>, DefaultRule<JPowerBehaviorNode<A>>> createRuleBuilder() {
        return new DefaultRuleBuilder<JPowerBehaviorNode<A>>();
    }

    public SpecificatorGraphBuilder<DefaultRule<JPowerBehaviorNode<A>>, SimpleSpecification<DefaultRule<JPowerBehaviorNode<A>>>, BridgeSpecificatorGraph<DefaultRule<JPowerBehaviorNode<A>>, SimpleSpecification<DefaultRule<JPowerBehaviorNode<A>>>>> createGraphBuilder() {
        return new AbstractSpecificatorGraphBuilder
                <DefaultRule<JPowerBehaviorNode<A>>
                ,SimpleSpecification<DefaultRule<JPowerBehaviorNode<A>>>
                ,BridgeSpecificatorGraph<DefaultRule<JPowerBehaviorNode<A>>,SimpleSpecification<DefaultRule<JPowerBehaviorNode<A>>>>>() {
            public BridgeSpecificatorGraph<DefaultRule<JPowerBehaviorNode<A>>, SimpleSpecification<DefaultRule<JPowerBehaviorNode<A>>>> createNewGraph() {
                return new BridgeSpecificatorGraph<DefaultRule<JPowerBehaviorNode<A>>, SimpleSpecification<DefaultRule<JPowerBehaviorNode<A>>>>
                        (new MapGraph<SimpleSpecification<DefaultRule<JPowerBehaviorNode<A>>>>());
            }

            public SimpleSpecification<DefaultRule<JPowerBehaviorNode<A>>> createSpecification(Collection<DefaultRule<JPowerBehaviorNode<A>>> rules, DefaultRule<JPowerBehaviorNode<A>> workingRule) {
            	Collection<DefaultRule<JPowerBehaviorNode<A>>> ruleSet;
            	ruleSet = new HashSet<DefaultRule<JPowerBehaviorNode<A>>>(rules);
            	SimpleSpecification<DefaultRule<JPowerBehaviorNode<A>>> specification
                        = new SimpleSpecification<DefaultRule<JPowerBehaviorNode<A>>>
            				(ruleSet,workingRule);
            	return specification;
            }

			@Override
			public SimpleSpecification<DefaultRule<JPowerBehaviorNode<A>>> createSpecification() {
				final Set<DefaultRule<JPowerBehaviorNode<A>>> EMPTY_SET = Collections.emptySet();
				return new SimpleSpecification<DefaultRule<JPowerBehaviorNode<A>>>(EMPTY_SET,null);
			}
        };
        
        
    }
}
