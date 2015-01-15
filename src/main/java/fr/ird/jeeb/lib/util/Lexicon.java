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

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;


/**	A translation dictionary build with multiple ResourceBundle
 *  A lexicon has only one language
 *	It gives a translation for a given key in the current language.
 *	Can be augmented with label files with addBundle ().
 *	@author F. de Coligny - S. Dufour-Kowalski
 */
public class Lexicon extends ResourceBundle {
	
	private Locale lang;
	private Map<String, String> mergedBundle;
	

	/** Contruct from a Lexicon for a particular Locale	
	*/
	public Lexicon (Locale lang) {
		
		this.lang = lang;
		this.mergedBundle = new HashMap<String, String>();
		
	}
	
	
	@Override 
	public Locale getLocale() {return lang;}

	
	@Override
	public Enumeration<String> getKeys() {
		return Collections.enumeration(this.mergedBundle.keySet());
	}
	
	
	@Override
	protected Object handleGetObject(String key) {
		return this.mergedBundle.get(key);
	}

	
	/**	Add a bundle in the lexicon 
	*	@param baseName : name of the resource (eg sapin.SapModels)
	*/
	public void loadBundle (String baseName) {
		
		try {
			ResourceBundle bundle;
			bundle = ResourceBundle.getBundle(baseName, this.lang, 
					  ClassLoader.getSystemClassLoader());  // also reads bundles in jar files (tested with capsis.commongui)
			
			for(String key : bundle.keySet()) {
				String value = bundle.getString(key);
				this.mergedBundle.put(key, value);
 			}
				
		} catch (MissingResourceException e) {
			// The warnings may be numerous, written in Log depending on an option
			if (Settings.getProperty ("translator.warning.log", true)) {  // default value = true
				Log.println (Log.WARNING, "Lexicon.addBundle ()", 
						"Resource not found: " + baseName + ":" + e.toString());
			}
			
		} catch (Exception e) {
			Log.println (Log.ERROR, "Lexicon.addBundle ()", e.toString());
			
		}
		
	}

	
	public void loadBundle (Properties bundle) {
		
		for(Object key : bundle.keySet()) {
 			String value = (String) bundle.get(key);
 			this.mergedBundle.put((String)key, value);
		}
		
	}

	
}



