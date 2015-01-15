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
package fr.ird.jeeb.lib.util;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.StringTokenizer;


/**	A Serializable vertex with 3D double coordinates..
*	@author F. de Coligny - april 2008
*/
public class Vertex3d extends Vertex2d implements Serializable {
	public double z;

	
	/**	Constructor 1.
	*/
	public Vertex3d (double x, double y, double z) {
		super (x, y);
		this.z = z;
	}
	
	/**	Constructor 2.
	*/
	public Vertex3d (Vertex3d original) {
		this (original.x, original.y, original.z);
	}
	
	/**	This constructor tries to build a Vertex3d with the given String. 
	*	Failure causes an exception to be thrown. Waited format is "(a, b, c)"
	*	with a, b and c of type double.
	*/
	public Vertex3d (String s) throws Exception {
		s = s.trim ();
		if (s.charAt (0) != '(' || s.charAt (s.length ()-1) != ')') {throw new Exception ();}
		s = s.substring (1, s.length () -1).trim ();

		StringTokenizer st = new StringTokenizer (s, ", ");
		if (st.countTokens () != 3) {throw new Exception ();}

		String t1 = st.nextToken ().trim ();
		String t2 = st.nextToken ().trim ();
		String t3 = st.nextToken ().trim ();

		if (!Check.isDouble (t1)) {throw new Exception ("Vertex3d (String): not a number: "+t1);}
		if (!Check.isDouble (t2)) {throw new Exception ("Vertex3d (String): not a number: "+t2);}
		if (!Check.isDouble (t3)) {throw new Exception ("Vertex3d (String): not a number: "+t3);}

		x = Check.doubleValue (t1);
		y = Check.doubleValue (t2);
		z = Check.doubleValue (t3);
	}
	
	public static Vertex3d convert (Vertex2d v2) {return new Vertex3d (v2.x, v2.y, 0d);}
	
	public Object clone () {
		return new Vertex3d (x, y, z);
	}
	
	/**	Convenient method.
	 */
	public Vertex3d copy () {
		return (Vertex3d) clone ();
	}
	
	public boolean equals (Vertex3d other) {
		return x == other.x && y == other.y && z == other.z;
	}
	
	public String toString () {
		NumberFormat nf = DefaultNumberFormat.getInstance ();
		
		StringBuffer b = new StringBuffer ();
		b.append ("(");
		b.append (nf.format(x));
		b.append (", ");
		b.append (nf.format(y));
		b.append (", ");
		b.append (nf.format(z));
		b.append (")");
	
		return b.toString ();
	}

}

