/* loDialogBoxButtons.java -- part of LOCommonDialogs
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

public class loDialogBoxButtons {
	
	// Values duplicated from com.sun.star.awt.MessageBoxButtons Constant Group 
	public long BUTTONS_OK 						= 1;		// specifies a dialog box with "OK" button.
	public long BUTTONS_OK_CANCEL				= 2;		// specifies a dialog box with "OK" and "CANCEL" button.
	public long BUTTONS_YES_NO 					= 3;		// specifies a dialog box with "YES" and "NO" button.
	public long BUTTONS_YES_NO_CANCEL			= 4;		// specifies a dialog box with "YES", "NO" and "CANCEL" button.
	public long BUTTONS_RETRY_CANCEL 			= 5;		// specifies a dialog box with "RETRY" and "CANCEL" button.
	public long BUTTONS_ABORT_IGNORE_RETRY 		= 6;		// specifies a dialog box with "ABORT", "IGNORE" and "RETRY" button.
	public long DEFAULT_BUTTON_OK 				= 0x10000;	// specifies that OK is the default button.
	public long DEFAULT_BUTTON_CANCEL			= 0x20000;	// specifies that CANCEL is the default button.
	public long DEFAULT_BUTTON_RETRY			= 0x30000;	// specifies that RETRY is the default button.
	public long DEFAULT_BUTTON_YES				= 0x40000;	// specifies that YES is the default button.
	public long DEFAULT_BUTTON_NO				= 0x50000;	// specifies that NO is the default button.
	public long DEFAULT_BUTTON_IGNORE			= 0x60000;	// specifies that IGNORE is the default button.
	
	// Additional Values
	public long BUTTONS_LABEL1_LABEL2_CANCEL	= 7;		// specifies a dialog box with two custom labeled buttons and "CANCEL"button.
	public long DEFAULT_BUTTON_LABEL1			= 0x70000;	// specifies that LABEL1 is the default button.
	public long DEFAULT_BUTTON_LABEL2			= 0x80000;	// specifies that LABEL2 is the default button.
	
}
