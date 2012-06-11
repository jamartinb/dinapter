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
package dinapter.graph;


import net.sourceforge.jpowergraph.DefaultEdge;
import net.sourceforge.jpowergraph.DefaultNode;

import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit tests for {@link JPowerGraph}.
 * @author José Antonio Martín Baena
 * @version $Revision: 453 $ - $Date: 2007-02-06 19:08:32 +0100 (mar, 06 feb 2007) $
 */
public class JPowerGraphTest extends ModifiableGraphTestAbstract<DefaultNode, JPowerGraph<DefaultNode, DefaultEdge>> {

	/* (non-Javadoc)
	 * @see dinapter.graph.ModifiableGraphTestAbstract#createGraph()
	 */
	@Override
	protected JPowerGraph<DefaultNode,DefaultEdge> createGraph() {
		return new JPowerGraph<DefaultNode, DefaultEdge>();
	}

	/* (non-Javadoc)
	 * @see dinapter.graph.ModifiableGraphTestAbstract#createNode()
	 */
	@Override
	protected DefaultNode createNode() {
		return new DefaultNode();
	}
	
	// Just to run the test.
	@Test
	public void runTest() {
		Assert.assertTrue(true);
	}

	@Override
	@Test
	public void testIncludeGraph() {
	//	Assert.fail("This method is not implemented in this class");
	}
}
