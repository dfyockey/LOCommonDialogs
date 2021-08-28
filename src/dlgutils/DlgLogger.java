package dlgutils;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.sun.star.text.XTextDocument;

import loCommonDialogs.loErrorBox;
//import tkutils.LanguageBundle;

public class DlgLogger {
	
	private static Logger logger;
	private static XTextDocument Doc;
	private static LanguageBundle dlgtxt = new LanguageBundle();	
	
	private static Handler init (String classname) {
		Handler fh = null;
		
		try {
			logger = Logger.getLogger(classname);
			try {
				fh = new FileHandler("%h/" + dlgtxt.msg("Application_Name") + "%g.log", 1000000, 5, true);	// "%h" refers to the value of the "user.home" system property.
			} catch (java.io.IOException e) {
				fh = new FileHandler("%t/" + dlgtxt.msg("Application_Name") + "%g.log", 1000000, 5, true);	// "%t" refers to the system temporary directory.
				// In Windows 7, at least, this refers to C:\Users\<User>\Appdata\Local\Temp,
				// where "C:" is the drive on which Windows resides, and <User> is the current Windows account username.
			}
			fh.setFormatter(new SimpleFormatter());
			logger.addHandler(fh);
			logger.setLevel(Level.FINE);
		} catch (SecurityException|IOException e) {
			if (fh != null) {
				fh.close();
				fh = null;
			}
			
			if (Doc != null) {
				// Use a plain XMessageBoxFactory-based loErrorBox for this unlikely, low-level error
				// to avoid need for an XComponentContext required for loDialogBox-based boxes.
				loErrorBox errorbox = new loErrorBox();
				errorbox.show(Doc, dlgtxt.msg("Application_Name"), "Debug Logging Failed. If an error box follows, please note the error for future reference.");
			}
		}
		
		return fh;
	}
	
	private static void log (XTextDocument xTextDoc, String classname, java.util.logging.Level level, String msg, Throwable e) {
		Doc = xTextDoc;
		
		Handler fh = init(classname);
		if (fh != null) {
			
			if (msg != null)
				logger.log(level, msg);
			else
				logger.log(level, e.getMessage(), e);
			
			fh.close();
		}
	}
	
	public static void log (XTextDocument xTextDoc, String classname, java.util.logging.Level level, String msg) {
		log (xTextDoc, classname, level, msg, null);
	}
	
	public static void log (XTextDocument xTextDoc, String classname, java.util.logging.Level level, Throwable e) {
		log (xTextDoc, classname, level, null, e);
	}
	
	public static void log (XTextDocument xTextDoc, String classname, java.util.logging.Level level, String methodname, String dialogtitle, String dialogmsg) {
		log (xTextDoc, classname, level, classname + "." + methodname + " : " + dialogtitle + " : " + dialogmsg);
	}
}
