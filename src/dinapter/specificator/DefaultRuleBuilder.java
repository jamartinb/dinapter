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
package dinapter.specificator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dinapter.behavior.BehaviorNode;

public class DefaultRuleBuilder<N extends BehaviorNode> extends AbstractRuleBuilder<N, DefaultRule<N>> {
	
	// @toreview It's strage this behavior but needed so far.
	protected Map<DefaultRule<N>,DefaultRule<N>> rulesPool = new HashMap<DefaultRule<N>,DefaultRule<N>>();
	
	@Override
	public DefaultRule<N> createRule(List<N> leftSide, List<N> rightSide,boolean required,boolean interleaved) {
		DefaultRule<N> toReturn = new DefaultRule<N>(leftSide,rightSide,required,interleaved);
		if (rulesPool.containsKey(toReturn)) // We reuse previously created Rules.
			toReturn = rulesPool.get(toReturn);
		else
			rulesPool.put(toReturn,toReturn);
		return toReturn;
	}
}
