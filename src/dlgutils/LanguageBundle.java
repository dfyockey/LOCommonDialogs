package dlgutils;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageBundle {
	private static Locale 			currentLocale 	= new Locale("user.country","user.language");
	private static ResourceBundle	messages		= ResourceBundle.getBundle("DlgLogger_Config", currentLocale);
	
	public String msg (String key) { return messages.getString(key); }
}
