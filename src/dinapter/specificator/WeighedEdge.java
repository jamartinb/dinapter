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
 * WeighedEdge.java
 *
 * Created on 17 de mayo de 2007, 16:18
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dinapter.specificator;

/**
 *
 * @author arkangel
 */
public class WeighedEdge<N extends JSearchSpecification> extends jsearchdemo.DefaultEdge implements jsearchdemo.WeighedEdge, jsearchdemo.Edge {
    
    /** Creates a new instance of WeighedEdge */
    public WeighedEdge(N from, N to) {
        super(from,to);
    }
    
    @SuppressWarnings("unchecked")
	public int getWeight() {
        N end = (N)getEndNode();
        synchronized (end) {
            try {
                while (!end.isCostReady())
                    end.wait();
                return end.getCost();
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted when waiting for a Specification to become cost ready",e);
            }
        }
    }
    
    public void setWeight(int w) {
        throw new UnsupportedOperationException("You cannot set the weight of this edge.");
    }
    
}
