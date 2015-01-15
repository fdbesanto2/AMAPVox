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

import java.awt.Color;
import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;


/**	Check proposes a collection of static methods to check and
* 	retrieve some data in a user input String.
*	@author F. de Coligny - january 2000
*/
public class Check {

	
	
	/**	Return true if the String parameter is empty.
	*/
	public static boolean isEmpty (String s) {
		boolean r = false;
		if (s.trim ().length () == 0) {
			r = true;
		}
		return r;
	}

	/**	Return true if the String contains a boolean
	*/
	public static boolean isBoolean (String s) {
		
		return s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false");		
	}

	/**	Return true if the String parameter is an int.
	*/
	public static boolean isInt (String s) {
		boolean r = false;
		try {
			int a = Integer.valueOf (s.trim ()).intValue ();
			r = true;
		} catch (java.lang.NumberFormatException exc) {}
		return r;
	}

	/**	Return true if the String parameter is a double.
	*/
	public static boolean isDouble (String s) {
		boolean r = false;
		try {
			double a = Double.valueOf (s.trim ()).doubleValue ();
			r = true;
		} catch (java.lang.NumberFormatException exc) {}
		return r;
	}
	
	

	/**	Return true if the String parameter is an existing file name.
	*/
	public static boolean isFile (String s) {
		boolean r = false;
		if (new File (s.trim ()).isFile ()) {
			r = true;
		}
		return r;
	}

	/**	Return true if the String parameter is a correct date.
	*/
	public static boolean isDate (String s) {	// fc + vc - 11.4.2006
		try {
			DateFormat df = DateFormat.getDateInstance (DateFormat.SHORT, Locale.FRENCH);
			df.parse (s);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**	Return true if the String parameter is a correct color.
	*/
	public static boolean isColor (String s) {	// sg 25.2.2010
		boolean r = false;
		try {
			Color a = Color.decode(s.trim ());
			r = true;
		} catch (java.lang.NumberFormatException exc) {}
		return r;
	}

	/**	Return true if the String parameter is an existing directory name.
	*/
	public static boolean isDirectory (String s) {
		return new File (s.trim ()).isDirectory ();
	}

	/**	Return the int value of the String parameter, 0 if trouble.
	*/
	public static int intValue (String s) {
		int r = 0;
		try {
			r = Integer.valueOf (s.trim ()).intValue ();
		} catch (java.lang.NumberFormatException exc) {}
		return r;
	}

	/**	Return the double value of the String parameter, 0 if trouble.
	*/
	public static double doubleValue (String s) {
		double r = 0;
		try {
			r= Double.valueOf (s.trim ()).doubleValue ();
		} catch (java.lang.NumberFormatException exc) {}
		return r;
	}

	/**	Return the boolean value of the String parameter, false if trouble.
	*/
	public static boolean booleanValue (String s) {	// fc - 12.1.2006
		boolean r = false;
		try {
			r= Boolean.valueOf (s.trim ()).booleanValue ();
			} catch (Exception exc) {}
		return r;
	}
	
	/**	Return the date value of the String parameter, null if trouble.
	*/
	public static Date dateValue (String s) {
		try {
			DateFormat df = DateFormat.getDateInstance (DateFormat.SHORT, Locale.FRENCH);
			return df.parse (s);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**	Return the color value of the String parameter, false if trouble.
	*/
	public static Color colorValue (String s) {	// sg - 25.2.2010
		Color r = Color.white;
		try {
			r= Color.decode(s.trim());
			} catch (Exception exc) {}
		return r;
	}

	
}
