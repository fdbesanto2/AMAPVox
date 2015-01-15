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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;


/**	Translator is a static facade to manage translation
*	It has an active lexicon
*	@author F. de Coligny - S. Dufour-Kowalski
*/
public class Translator {

	
    static public Lexicon activeLexicon; /* Current lexicon */
    static public Lexicon secondLexicon; 
    
    static public Set<String> loadedbundles = new HashSet<String >();
    
    /** System bundle are specific bundle which cannot be edited*/
    static public Set<String> systembundles = new HashSet<String >();
    

    /** Init system translator 
     *  @param locale : language - if null use default locale
     * @throws Exception 
     * */
    public static void initActiveLexicon(Locale local) throws Exception {
    	    	
    	if(local == null) { 
    		local = Locale.getDefault(); 
    	}
    	
    	
    	if(local.equals(Locale.CANADA_FRENCH)) { 
			local = Locale.FRENCH; 
		}
    	
    	Locale.setDefault(local);
    	activeLexicon = new Lexicon(local);
    	
    	// init second lexicon if necessary
    	if( ! local.equals(Locale.ENGLISH) &&
    			! local.equals(Locale.US) &&
    			! local.equals(Locale.FRENCH)
    			) {
    		secondLexicon = new Lexicon(Locale.ENGLISH);
    	}
    	
    }

    
    /**	Use this method to translate a key into a label in the given language.
	*	To be used each time a label is shown on the gui (buttons, title, labels, messages...).
	*	ex: Translator.swap ("Pilot.errorWhileNewScenario");
	*/
	public static String swap (String key) {
		
		try	{
			return activeLexicon.getString (key);
		}
		catch(MissingResourceException e) {}
		catch(NullPointerException e2) {}
		
		// Try with second lexicon
		try {
			if(secondLexicon != null) {
				return secondLexicon.getString (key);
			}
		}
		catch(MissingResourceException e) {}
		catch(NullPointerException e2) {}
		
		return key;
		
	}

	/** Declare a new bundle
	 * @param baseName : resource name
	 */
	public static void addBundle (String baseName) {
		if(activeLexicon == null) { 
			try {
				Translator.initActiveLexicon(null);
			} catch (Exception e) {}
		}
		activeLexicon.loadBundle(baseName);
		if(secondLexicon != null) { secondLexicon.loadBundle(baseName); }
		
		loadedbundles.add(baseName);
	}
	
	
	/** Return active lexicon */
	public static Lexicon getActiveLexicon() {
		return activeLexicon;
	}
	
	
	/** Declare a new system bundle
	 * @param baseName : resource name
	 */
	public static void addSystemBundle (String baseName) {
		addBundle(baseName);
		systembundles.add(baseName);
	}
	
	
	/** Return true is a particular baseName is a system bundle */
	public static boolean isSystem(String baseName) {
		return systembundles.contains(baseName);
	}
	
	
	/** Return if a language is supported */
    public static boolean isSupportedLanguage(String language) {
		
    	for(Locale l : getSupportedLanguages()) {
    		if(language.equals(l.getLanguage())) { return true; }
    	}
		
    	return false;
	}
    
    /** Return the list of supported locale */
    public static List<Locale> getSupportedLanguages() {
		
    	List<Locale> l = new ArrayList<Locale> ();
    	l.add(Locale.FRENCH);
    	l.add(Locale.ENGLISH);
    	l.add(Locale.CHINESE);
    	
		return l;
	}
    
    public static Set<String> getLoadedBundles() {	
		return loadedbundles;
	}

 
}




