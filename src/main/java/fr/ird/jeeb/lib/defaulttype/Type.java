/* 
* Copyright (C) 2006-2009  Jean-Francois Barczi, Philippe Borianne, 
*    Francois de Coligny, Samuel Dufour and Sebastien Griffon
* 
* This file is part of Jeeb.
* 
* Jeeb is free software: you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as published
* by the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Jeeb is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with Jeeb.  If not, see <http://www.gnu.org/licenses/>.
*/
package fr.ird.jeeb.lib.defaulttype;

import fr.ird.jeeb.lib.util.Namable;

/**	Type describes a general type with a name.
*	@author F. de Coligny - october 2008
*/
public interface Type extends Namable, Comparable {
	
	/**	Name of the type
	*	This is a not translated name. Format is generally 
	*	className.SOME_NAME (e.g. "BuiltinType.GRID" in the BuiltinType class).
	*	This name may be used in equals () and hashcode ().
	*/
	public String getName ();
	
	/**	The name that is shown to the user.
	*	Can be getName () or translator.swap (getName) depending on the use.
	*/
	public String getTranslatedName ();
	
	/**	Sets the name (not translated, e.g. "BuiltinType.GRID")
	*/
	public void setName (String name);
	
	/**	Comparable interface, should rely on getName ().
	*/
	public int compareTo (Object o);

	

}
