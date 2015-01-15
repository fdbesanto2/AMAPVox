/**
 * 
 */
package fr.ird.jeeb.lib.util;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Utility class to manage Settings
 * @author sdufour
 *
 */
public class Settings {
	
	static OrderedProperties p = new OrderedProperties();
	
	/** Return all properties */
	static public OrderedProperties getProperties() {
		
		return p;
	}
	
	/** Set all properties */
	static public void addProperties(Properties props) {
		
		p.putAll(props);
		
	}
	
	/** Test if a property exists */
	static public boolean hasProperty(String key) {
		
		return p.getProperty(key) != null;
	}
	
	/**	Use this method at early stage when the application is starting to reload a property
	*	file saved at previous run time (at first time, writes an info in log: not found). 
	*	Use savePropertyFile () from time to time to save all the system properties. 
	*	See setProperty () and getProperty () methods.
	*/
	static public void loadPropertyFile (String fileName) {
		try {
			FileInputStream f = new FileInputStream (fileName);
			p.load(f);
			Log.println (Log.INFO, "Settings.loadPropertyFile ()",
					"Property file loaded: "+fileName);
		} catch (FileNotFoundException e) {
			Log.println (Log.INFO, "Settings.loadPropertyFile ()",
					"Property file does not exist, created: "+fileName);
		} catch (Exception e) {
			Log.println (Log.WARNING, "Settings.loadPropertyFile ()",
					"Exception, could not load property file: "+fileName, e);
		}
	}

	/**	Saves the system properties in the property file which was set by loadPropertyFile ()
	*	at application starting time, see loadPropertyFile (). Use this method periodicaly to save
	*	the system properties for next run.
	*/
	static public void savePropertyFile (String fileName) {
		
		try {
			FileOutputStream out = new FileOutputStream (fileName);
			p.store (out, fileName);
			out.close ();
			
		} catch (Exception e) {
			Log.println (Log.ERROR, "Settings.savePropertyFile ()", 
					"Could not save property file: "+fileName
					+", ensure Settings.loadPropertyFile (fileName) was called at application starting time", e);
		}
		
		
		
	}
	
	/**	Use this method at early stage when the application is starting to
	*	set the name of the property file where savePropertyFile () 
	*	(the one with no arguments) will save the p object.
	*/
	static public void setDefaultPropertyFile (String fileName) {
		p.setProperty ("default.property.file", fileName);
	}
	
	/**	Saves the properties in the default.property.file.
	*	Set default.property.file before with setDefaultPropertyFile (String fileName).
	*/
	static public void savePropertyFile () throws Exception {
		
		String fileName = p.getProperty ("default.property.file");
		if (fileName == null) {throw new Exception ("Settings.savePropertyFile () error: default.property.file is null."
				+ " Use setDefaultPropertyFile (String fileName)");}
		savePropertyFile(fileName);
		
	}
	
	/**	Saves a property of type boolean.
	*	The property will be saved into a file and reloaded at next run.
	*	See getProperty (String, boolean), loadPropertyFile () and savePropertyFile ().
	*/
	static public void setProperty (String name, boolean value) {
		p.setProperty (name, ""+value);
	}
	
	/**	Saves a property of type String.
	*	The property will be saved into a file and reloaded at next run.
	*	See getProperty (String, String), loadPropertyFile () and savePropertyFile ().
	*/
	static public void setProperty (String name, String value) {
		p.setProperty (name, value);
	}
	
	/**	Saves a property of type int.
	*	The property will be saved into a file and reloaded at next run.
	*	See getProperty (String, int), loadPropertyFile () and savePropertyFile ().
	*/
	static public void setProperty (String name, int value) {
		p.setProperty (name, ""+value);
	}
	
	/**	Saves a property of type double.
	*	The property will be saved into a file and reloaded at next run.
	*	See getProperty (String, double), loadPropertyFile () and savePropertyFile ().
	*/
	static public void setProperty (String name, double value) {
		p.setProperty (name, ""+value);
	}
	
	/**	Saves a property of type Color.
	*	The property will be saved into a file and reloaded at next run.
	*	See getProperty (String, Color), loadPropertyFile () and savePropertyFile ().
	*/
	static public void setProperty (String name, Color value) {
		StringBuffer b = new StringBuffer ();
		b.append (value.getRed ());
		b.append (',');
		b.append (value.getGreen ());
		b.append (',');
		b.append (value.getBlue ());
		b.append (',');
		b.append (value.getAlpha ());
		p.setProperty (name, b.toString ());
	}
	
	/**	Returns the saved value for the boolean property with the given name.
	*	If no value found, returns the given default value.
	*	See setProperty (String, boolean), loadPropertyFile () and savePropertyFile ().
	*/
	static public boolean getProperty (String name, boolean defaultValue) {
		try {
			String v = p.getProperty (name);
			if (v == null) {throw new Exception ();}	// needed, else, returns false
			return new Boolean (v).booleanValue ();
		} catch (Exception e) {
			return defaultValue;
		}
	}
	
	/**	Returns the saved value for the String property with the given name.
	*	If no value found, returns the given default value.
	*	See setProperty (String, String), loadPropertyFile () and savePropertyFile ().
	*/
	static public String getProperty (String name, String defaultValue) {
		try {
			String v = p.getProperty (name);
			if (v == null) {throw new Exception ();}	// default value must be returned
			return v;
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**	Returns the saved value for the int property with the given name.
	*	If no value found, returns the given default value.
	*	See setProperty (String, int), loadPropertyFile () and savePropertyFile ().
	*/
	static public int getProperty (String name, int defaultValue) {
		try {
			String v = p.getProperty (name);
			return Integer.parseInt (v);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**	Returns the saved value for the double property with the given name.
	*	If no value found, returns the given default value.
	*	See setProperty (String, double), loadPropertyFile () and savePropertyFile ().
	*/
	static public double getProperty (String name, double defaultValue) {
		try {
			String v = p.getProperty (name);
			return Double.parseDouble (v);
		} catch (Exception e) {
			return defaultValue;
		}
	}
	
	/**	Returns the saved value for the Color property with the given name.
	*	If no value found, returns the given default value.
	*	See setProperty (String, Color), loadPropertyFile () and savePropertyFile ().
	*/
	static public Color getProperty (String name, Color defaultValue) {
		try {
			String v = p.getProperty (name);
			StringTokenizer st = new StringTokenizer (v, ",");
			int r = Check.intValue (st.nextToken ());
			int g = Check.intValue (st.nextToken ());
			int b = Check.intValue (st.nextToken ());
			int a = Check.intValue (st.nextToken ());
			return new Color (r, g, b, a);
		} catch (Exception e) {
			return defaultValue;
		}
	}

}
