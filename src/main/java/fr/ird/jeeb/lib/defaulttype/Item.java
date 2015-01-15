/* 
* Copyright (C) 2006-2010  Jean-Francois Barczi, Philippe Borianne, 
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

import fr.ird.jeeb.lib.util.Vertex3d;



/**	Item describes a general item in a set of items.
*	This abstract item has a unique id, a type and a name.
*	@author F. de Coligny - october 2008
*/
public interface Item extends Comparable {

	/**	A unique id in the set of items.
	*	For example, in a SketchModel, all AbstractItems have a unique itemId 
	*	within the SketchModel.
	*/
	public int getItemId ();
	
	/**	Type of the Item.
	*/
	public Type getType ();
	
	/**	Name of the Item.
	*/
	public String getName ();
	
	public void setItemId (int itemId);
	public void setType (Type type);
	public void setName (String name);
	
	/** Comparable interface.
	*/
	public int compareTo (Object o);
	
	//---------
	public double getX ();
	public double getY ();
	public double getZ ();

	// setX, Y, Z and XYZ should take care of moving the absolute bounding box
	public void setX (double v);
	public void setY (double v);
	public void setZ (double v);
	public void setXYZ (double x, double y, double z);

	// Absolute BoundingBox definition
	public Vertex3d getMin ();
	public Vertex3d getMax ();

}
