package fr.ird.jeeb.lib.util;

import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;
import java.util.Vector;

public class OrderedProperties extends Properties {
	 
	private static final long serialVersionUID = 1L;

	/** Override keys method to store key in alphabetical order */
    @Override
	public Enumeration<Object> keys() {
        TreeSet<Object> t = new TreeSet<Object>(super.keySet());
        Vector<Object> v = new Vector<Object>(t);
        return v.elements();
    }
}
