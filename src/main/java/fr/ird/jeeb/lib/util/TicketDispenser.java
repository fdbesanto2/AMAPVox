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


/**	Returns next int each time it's asked to.
*	@author F. de Coligny - december 2000
*/
public class TicketDispenser implements Serializable {
	private int ticket;
	
	/**	Used to get next value.
	*/
	public int getNext () {return ++ticket;}
	public int next () {return getNext ();}		// fc - 17.9.2008 - next = getNext 
	
	/**	To check current value.
	*/
	public int getCurrentValue () {return ticket;}
	
	/**	To change current value. Can be used to initialize the TicketDispenser
	*	to a given value.
	*/
	public void setCurrentValue (int v) {ticket = v;}
	
}

