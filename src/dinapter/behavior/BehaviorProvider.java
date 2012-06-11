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
 * BehaviorProvider.java
 *
 * Created on 6 de marzo de 2007, 17:41
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dinapter.behavior;

import java.io.File;

/**
 *
 * @author José Antonio Martín Baena
 */
public interface BehaviorProvider<G extends BehaviorGraph> {
    public G getBehaviorGraph();
    public G getBehaviorGraph(File file) throws Exception;
}
