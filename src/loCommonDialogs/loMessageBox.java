/* loMessageBox.java -- part of LOCommonDialogs
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

package loCommonDialogs;

import java.util.logging.Level;

// loMessageBox - Java class to display a standard messagebox in a LibreOffice document

/*
// USAGE INFORMATION
// 
// Instantiate loMessageBox, and then call show() with appropriate values
// for its parameters to display a messagebox. 
//  
// Reproduction of information from the LibreOffice 5.0 SDK API Reference
// is included here for user convenience to explain parameters passed to
// and values returned from LibreOffice 5.0 SDK API functions.

 * Parameter messageBoxType:
 * 
 * enum com.sun.star.awt.MessageBoxType
 * 
	MESSAGEBOX	A normal message box.
	INFOBOX		A message box to inform the user about a certain event. (Ignores messageBoxButtons param, instead using BUTTONS_OK)
	WARNINGBOX	A message to warn the user about a certain problem.
	ERRORBOX	A message box to provide an error message to the user.
	QUERYBOX	A message box to query information from the user.

 * Parameter messageBoxButtons:
 * 
 * com.sun.star.awt.MessageBoxButtons Constant Group
 *
	const long 	BUTTONS_OK 					= 1			specifies a message with "OK" button.
	const long 	BUTTONS_OK_CANCEL			= 2			specifies a message box with "OK" and "CANCEL" button.
	const long 	BUTTONS_YES_NO 				= 3			specifies a message box with "YES" and "NO" button.
	const long 	BUTTONS_YES_NO_CANCEL		= 4			specifies a message box with "YES", "NO" and "CANCEL" button.
	const long 	BUTTONS_RETRY_CANCEL 		= 5			specifies a message box with "RETRY" and "CANCEL" button. 
	const long 	BUTTONS_ABORT_IGNORE_RETRY 	= 6			specifies a message box with "ABORT", "IGNORE" and "RETRY" button.
	const long 	DEFAULT_BUTTON_OK 			= 0x10000	specifies that OK is the default button. 
	const long 	DEFAULT_BUTTON_CANCEL		= 0x20000	specifies that CANCEL is the default button. 
	const long 	DEFAULT_BUTTON_RETRY		= 0x30000	specifies that RETRY is the default button. 
	const long 	DEFAULT_BUTTON_YES			= 0x40000	specifies that YES is the default button.
	const long 	DEFAULT_BUTTON_NO			= 0x50000	specifies that NO is the default button. 
	const long 	DEFAULT_BUTTON_IGNORE		= 0x60000	specifies that IGNORE is the default button.
	
 * Return Values:
 * 
 * Returns -1 if an error occurs while trying to display the XMessageBox.
 * Otherwise, returns a value from com::sun::star::awt::MessageBoxResults Constant Group
 *
	const short 	CANCEL	= 0		The user canceled the XMessageBox, by pressing "Cancel" or "Abort" button. 
	const short 	OK		= 1		The user pressed the "Ok" button. 
	const short 	YES		= 2		The user pressed the "Yes" button. 
	const short 	NO		= 3		The user pressed the "No" button. 
	const short 	RETRY	= 4		The user pressed the "Retry" button. 
	const short 	IGNORE	= 5		The user pressed the "Ignore" button.
*/

import com.sun.star.awt.MessageBoxType;
import com.sun.star.awt.XMessageBox;
import com.sun.star.awt.XMessageBoxFactory;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.frame.XModel;
import com.sun.star.uno.UnoRuntime;

import dlgutils.DlgLogger;

public class loMessageBox {
	public short show(XModel xDoc, MessageBoxType messageBoxType, int messageBoxButtons, String messageBoxTitle, String message) {
		try {
	        // Get the parent window and access to the window toolkit of the parent window
			XWindow parentWindow = xDoc.getCurrentController().getFrame().getContainerWindow();
			XWindowPeer parentWindowPeer = (XWindowPeer) UnoRuntime.queryInterface(XWindowPeer.class, parentWindow);
		
			// Initialize the message box factory
			XMessageBoxFactory messageBoxFactory = (XMessageBoxFactory) UnoRuntime.queryInterface(XMessageBoxFactory.class,parentWindowPeer.getToolkit());
			XMessageBox box = messageBoxFactory.createMessageBox(parentWindowPeer, messageBoxType, messageBoxButtons, messageBoxTitle, message);
			return box.execute();
		} catch (Exception e) {
			DlgLogger.log(null, loDialogBox.class.getName(), Level.SEVERE, e);
			return -1;
		}
    }
}
