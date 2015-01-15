/*
 * Copyright (C) 2006-2011 Jean-Francois Barczi, Philippe Borianne, Francois de Coligny, Samuel
 * Dufour and Sebastien Griffon
 * 
 * This file is part of Jeeb.
 * 
 * Jeeb is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * Jeeb is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with Jeeb. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package fr.ird.jeeb.lib.util;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * A default number format with the english dot separator, no grouping, 3 decimals. Built on the
 * Singleton pattern.
 * 
 * @author F. de Coligny - august 2011
 */
public class DefaultNumberFormat {

	// One single instance
	static private NumberFormat nf;

	/**
	 * Private constructor
	 */
	private DefaultNumberFormat () {}

	/**
	 * Use NumberFormat nf = DefaultNumberFormat.getInstance (), then nf.format (number).
	 */
	static public NumberFormat getInstance () {
		if (nf == null) {
			nf = NumberFormat.getNumberInstance (Locale.ENGLISH);
			nf.setGroupingUsed (false);
			nf.setMaximumFractionDigits (3);
		}
		return nf;
	}

}
