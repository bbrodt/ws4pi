package com.zarroboogsfound.ws4pi.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {

	private static final Logger INSTANCE;

	/** Set an environment variable for logging format.  This is for 1-line messages. */
	static {
		// %1=datetime %2=methodname %3=loggername %4=level %5=message
		System.setProperty("java.util.logging.SimpleFormatter.format", 
				"%1$tF %1$tT %3$s %4$-7s %5$s%n");
		INSTANCE = Logger.getLogger("DataRunner");
	}

	public static void log(Level level, String msg) {
		INSTANCE.log(level, msg);
	}

	public static void log(Level level, String msg, Object param1) {
		INSTANCE.log(level, msg, param1);
	}

	public static void log(Level level, String msg, Object[] params) {
		INSTANCE.log(level, msg, params);
	}

	public static void log(Level level, String msg, Throwable thrown) {
		INSTANCE.log(level, msg, thrown);
	}

	public static void severe(String msg) {
		INSTANCE.severe(msg);
	}

	public static void severe(String msg, Throwable thrown) {
		INSTANCE.log(Level.SEVERE, msg, thrown);
	}

	public static void severe(Throwable thrown) {
		INSTANCE.log(Level.SEVERE, "Exception: ", thrown);
	}

	public static void warning(String msg) {
		INSTANCE.warning(msg);
	}

	public static void info(String msg) {
		INSTANCE.info(msg);
	}

	public static void config(String msg) {
		INSTANCE.config(msg);
	}
}
