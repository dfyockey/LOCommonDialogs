/* LanguageBundle.java -- part of LOCommonDialogs
 * 
 * LOCommonDialogs - Dialogs for LibreOffice providing commonly needed functionality
 * Copyright © 2016-2018, 2021 David Yockey
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package dlgutils;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageBundle {
	private static Locale 			currentLocale 	= new Locale("user.country","user.language");
	private static ResourceBundle	messages		= ResourceBundle.getBundle("DlgLogger_Config", currentLocale);
	
	public String msg (String key) { return messages.getString(key); }
}
