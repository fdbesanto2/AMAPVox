/*
 * Copyright (C) 2006-2009 Jean-Francois Barczi, Philippe Borianne, Francois de Coligny, Samuel
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

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/** Minimal formater class */
class MinimalFormatter extends Formatter {

	public String format (LogRecord record) {
		return record.getMessage () + "\n";
	}

}

/**
 * Custom formater class
 * 
 * @author F. de Coligny - september 2009
 */
class CustomFormatter extends Formatter {

	static private final DateFormat dateFormatter = new SimpleDateFormat ("yyyy.MM.dd HH:mm:ss z");

	public String format (LogRecord record) {
		StringBuffer b = new StringBuffer ();
		// Level
		b.append (record.getLevel ());
		// Date
		b.append (" [");
		b.append (dateFormatter.format (new Date (record.getMillis ())));
		b.append ("] ");
		// Source
		// ClassName may be empty
		StringBuffer s = new StringBuffer (); // 'source'
		String className = record.getSourceClassName ();
		if (className != null && className.trim ().length () != 0) {
			s.append (className);
			s.append ('.');
		}
		// SourceName should always be provided
		String methodName = record.getSourceMethodName ();
		if (methodName != null && methodName.trim ().length () != 0) { // if provided
			if (methodName.indexOf ("(") < 0) { // if no '(', add '()'
				methodName = methodName.replace (')', ' ');
				methodName.trim ();
				methodName += " ()";
			}
			s.append (methodName);
		}
		String source = s.toString ();
		// If source is 'Log.println ()', igore it (not significant)
		if (source != null && source.indexOf ("Log") > 0 && source.indexOf ("println") > 0) {
			source = "";
		}
		b.append (source);
		b.append ("\n");
		// Message
		b.append (record.getMessage ());
		b.append ("\n");
		// StackTrace if any
		if (record.getThrown () != null) {
			b.append ("-> ");
			b.append (record.getThrown ().toString ());
			b.append ("\n");
			StackTraceElement[] lines = record.getThrown ().getStackTrace ();
			for (int i = 0; i < lines.length; i++) {
				b.append ("   at ");
				b.append (lines[i]);
				b.append ("\n");
			}
		}
		b.append ("\n");
		return b.toString ();
	}

}

/**
 * Formatted log.
 * 
 * @author F. de Coligny, S. Dufour - january 2009
 */
public class Log {

	public static final Level WARNING = Level.WARNING;
	public static final Level ERROR = Level.SEVERE;
	public static final Level INFO = Level.INFO;
	public static final Level MISSING = Level.CONFIG;

	/** Default logger */
	private static Logger defaultLog = Logger.getLogger (Log.class.getName ());
	private static Map<String,Logger> map = new HashMap<String,Logger> ();

	/** Default output dir */
	private static File outputDir = new File (System.getProperty ("user.dir"));

	private static boolean overwriteLogs = true;

	/** Return log directory */
	public static String getLogDirectory () {
		return outputDir.getPath ();
	}

	/**
	 * Initialize logger directory : output dir defaultName : name of the default logger
	 * */
	public static Logger initLogger (File directory, String defaultName) {
		Logger l = Logger.getLogger (defaultName);
		outputDir = directory;
		setHandler (l);
		setFormatter (l, true);
		defaultLog = l;

		map = new HashMap<String,Logger> ();
		map.put (defaultName, l);
		return l;
	}

	/**
	 * When true, existing Log files are kept and new ones are created with an appending counter
	 */
	public static boolean isOverwriteLogs () {
		return overwriteLogs;
	}

	/**
	 * When true, existing Log files are kept and new ones are created with an appending counter.
	 * This can be interested in script mode when several scripts must be run and the result Log
	 * evaluated later.
	 */
	public static void setOverwriteLogs (boolean overwriteLogs) {
		Log.overwriteLogs = overwriteLogs;
	}

	/** Define logger handler: By default, use a file in the output directory */
	protected static void setHandler (Logger logger) {

		String name = logger.getName ();
		String filename = getLogFilename (name);

		logger.setUseParentHandlers (false);
		for (Handler h : logger.getHandlers ()) {
			logger.removeHandler (h);
		}

		try {
			// Recreate the file
			FileHandler fileTxt = null;

			if (overwriteLogs) {
				fileTxt = new FileHandler (filename, false);
			} else {
				fileTxt = new FileHandler (filename, 0, 100); // fileName, limit, count
			}

			logger.addHandler (fileTxt);

		} catch (IOException e) {
			logger.addHandler (new ConsoleHandler ());
			defaultLog.warning ("Cannot set log file : " + filename);

		}
	}

	/** Set log format. withContextInfo add date and calling function */
	static public void setFormatter (Logger l, boolean withContextInfo) {

		for (Handler h : l.getHandlers ()) {
			// ~ if(withContextInfo) { h.setFormatter(new SimpleFormatter()); }
			if (withContextInfo) {
				h.setFormatter (new CustomFormatter ());
			} else {
				h.setFormatter (new MinimalFormatter ());
			}
		}
	}

	/** Ensure a logger is created, if not create it */
	protected static Logger createLogger (String name) {

		if (!map.containsKey (name)) {
			Logger l = Logger.getLogger (name);
			map.put (name, l);
			setHandler (l);
			setFormatter (l, false);
		}
		return map.get (name);
	}

	/**
	 * Return a full Filename from a log name if null return default logger
	 * */
	static public String getLogFilename (String name) {
		if (name == null) name = defaultLog.getName ();
		String filename = getLogDirectory () + File.separator + name + ".log";
		return filename;

	}

	/** Return a list of open logger */
	static public Set<String> getNames () {
		return map.keySet ();
	}

	/** Output to standard logger */
	public static void println (Level type, String sourceMethod, String msg, Throwable t) {
		defaultLog.logp (type, "", sourceMethod, msg, t);
	}

	public static void println (Level type, String sourceMethod, String msg) {
		defaultLog.logp (type, "", sourceMethod, msg);
	}

	public static void println (String msg) {
		defaultLog.log (Level.INFO, msg);
	}

	/** Output to a particular logger */
	public static void println (String logname, Level type, String sourceMethod, String msg, Throwable t) {
		Log.createLogger (logname);
		Logger.getLogger (logname).logp (type, "", sourceMethod, msg, t);
	}

	public static void println (String logname, Level type, String sourceMethod, String msg) {
		Log.createLogger (logname);
		Logger.getLogger (logname).logp (type, "", sourceMethod, msg);
	}

	public static void println (String logname, String msg) {
		Log.createLogger (logname);
		Logger.getLogger (logname).log (Level.INFO, msg);
	}

}
