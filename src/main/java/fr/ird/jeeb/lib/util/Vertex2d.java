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
package fr.ird.jeeb.lib.util;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.StringTokenizer;


/**	A Serializable vertex with 2D double coordinates. 
*	@author F. de Coligny - april 2008
*/
public class Vertex2d implements Serializable {
	public double x;
	public double y;
	
	
	/**	Default constructor (mainly for subclasses)
	*/
	public Vertex2d () {
		this (0, 0);
	}
	
	/**	Constructor to dupplicate a Vertex2d
	*/
	public Vertex2d (Vertex2d v2) {		// fc - 10.10.2008
		this (v2.x, v2.y);
	}
	
	/**	Constructor to change a Vertex3d into a Vertex2d
	*/
	public Vertex2d (Vertex3d v3) {		// fc - 10.10.2008
		this (v3.x, v3.y);
	}
	
	/**	Constructor
	*/
	public Vertex2d (double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	/**	This constructor tries to build a Vertex2d with the given String. 
	*	Failure causes an exception to be thrown. Waited format is "(a, b)"
	*	with a and b of type double.
	*/
	public Vertex2d (String s) throws Exception {
		s = s.trim ();
		if (s.charAt (0) != '(' || s.charAt (s.length ()-1) != ')') {throw new Exception ();}
		s = s.substring (1, s.length () -1).trim ();

		StringTokenizer st = new StringTokenizer (s, ", ");
		if (st.countTokens () != 2) {throw new Exception ();}

		String t1 = st.nextToken ().trim ();
		String t2 = st.nextToken ().trim ();
		
		if (!Check.isDouble (t1)) {throw new Exception ("Vertex2d (String): not a number: "+t1);}
		if (!Check.isDouble (t2)) {throw new Exception ("Vertex2d (String): not a number: "+t2);}
		
		x = Check.doubleValue (t1);
		y = Check.doubleValue (t2);
	}
	
	public static Vertex2d convert (Vertex3d v3) {return new Vertex2d (v3.x, v3.y);}

	public Object clone () {
		return new Vertex2d (x, y);
	}
	
	public boolean equals (Vertex2d other) {
		return x == other.x && y == other.y;
	}
	
	public String toString () {
		NumberFormat nf = NumberFormat.getNumberInstance (Locale.ENGLISH);
		nf.setMaximumFractionDigits (2);
		nf.setGroupingUsed (false);
		
		StringBuffer b = new StringBuffer ();
		b.append ("(");
		b.append (nf.format(x));
		b.append (", ");
		b.append (nf.format(y));
		b.append (")");
	
		return b.toString ();
	}

}

