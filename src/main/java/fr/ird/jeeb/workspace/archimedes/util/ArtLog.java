package fr.ird.jeeb.workspace.archimedes.util;

import java.io.File;

import fr.ird.jeeb.lib.util.Log;
//import capsis.kernel.PathManager;

/**
 * A log system for Art.
 * 
 * @author F. de Coligny -
 * 
 */
public class ArtLog {

	static private boolean initDone;
	static private boolean couldNotInit;

	/**
	 * This must be called once before the first ArtLog.print (...) to set the
	 * log directory.
	 * 
	 * @param logDir
	 *            : this directory will contain the log file
	 */
	static public void init(File logDir) {
		try {
			Log.initLogger(logDir, "Art");
			initDone = true;
		} catch (Exception e) {
			couldNotInit = true;
		}
	}

	/**
	 * Writes the given message in logDir/Art.log
	 */
	static public void println(String message) {

		if (couldNotInit) {
			System.out.println(message);
			return;
		}

		// If init (logDir) was not called (forgotten)
                /*
		try {
			if (!initDone)
				init(new File(PathManager.getDir("var")));
		} catch (Exception e) {
			couldNotInit = true;
		}
                        */
		if (couldNotInit) {
			System.out.println(message);
			return;
		} else {
			Log.println(message);
		}
	}

}
