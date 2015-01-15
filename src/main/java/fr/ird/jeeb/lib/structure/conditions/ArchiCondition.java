/* 
 * Copyright (C) 2006-2009  Sebastien Griffon
 * 
 * This file is part of Sketch.
 * 
 * Sketch is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Sketch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Sketch.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.ird.jeeb.lib.structure.conditions;

import fr.ird.jeeb.lib.structure.ArchiNode;

public interface ArchiCondition {
	
	public boolean isCorrect (ArchiNode node);
}
