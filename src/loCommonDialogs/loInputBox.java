/* loInputBox.java -- part of LOCommonDialogs
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

// loInputBox - Java class to display a simple inputbox in a LibreOffice document

/*
// USAGE INFORMATION
//
// Instantiate loInputBox, and then call show() with appropriate values
// for its parameters to display an inputbox.  Upon return, call gettext()
// to retrieve text typed in the inputbox's edit box.

 * Return Values:
 * 
 * Returns 0 on Cancel, 1 on OK
*/

import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XButton;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XFixedText;
import com.sun.star.awt.XTextComponent;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XModel;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import dlgutils.DlgLogger;

public class loInputBox extends loDialogBox implements AutoCloseable {
		
	// Dialog and Control Size & Position Values
	protected int fieldvertpos;
	protected int fieldhorizpos;
	protected int OKhorizpos;
	protected int Cancelhorizpos;
	
	// Control Return Value Storage
	private XFixedText		guiLabel;
	private XTextComponent	guiEditBox;
	
	private XButton			guiOKBtn;
	private XButton			guiCancelBtn;
	
	private XControl		guiIcon;
	
	public loInputBox(XComponentContext xComponentContext) {
		super(xComponentContext);
		initBox();
	}
	
	protected void initBox() {
		xMCF = xContext.getServiceManager();
		createDialog(xMCF, xContext);

		initialize (
				new String[] { "Height", "Moveable", "Name", "PositionX", "PositionY", "Step", "TabIndex", "Title", "Width" },
				new Object[] { dialogheight, true, "loInputBox", dialogxpos, dialogypos, 0, (short)0, "loInputBox", dialogwidth }
		);
		
		// add dialog controls
		try {
			guiIcon = insertImage(padding, padding, iconsize, iconsize, "");
						
			guiLabel	 = insertFixedText(textalign_left, labelposX, labelposY, labelwidth, labelheight, 0, "Input something!");
			guiEditBox	 = insertEditField(textalign_center, fieldhorizpos, fieldvertpos, fieldwidth, fieldheight);
			guiOKBtn	 = insertButton(OKhorizpos,     btnvertpos, btnwidth, btnheight, "OK",     (short) PushButtonType.OK_value,		true );
			guiCancelBtn = insertButton(Cancelhorizpos, btnvertpos, btnwidth, btnheight, "Cancel", (short) PushButtonType.CANCEL_value, false);
		} catch (com.sun.star.uno.Exception e) {
			DlgLogger.log(null, loDialogBox.class.getName(), Level.SEVERE, e);
			e.printStackTrace(System.err);
		}
		
		m_xWindowPeer = getWindowPeer();
		xDialog = UnoRuntime.queryInterface(XDialog.class, m_xDialogControl);
	}
	
	// Convenience method for show with editwidth equal to btnwidth.
	public short show(XModel xDoc, String title, String labeltext, String edittext, String ImageUrl) {
		return show(xDoc, title, labeltext, edittext, 0, ImageUrl);
	}
	
	public short show(XModel xDoc, String title, String labeltext, String edittext, int editwidth, String ImageUrl) {
		// Set editwidth to 0 to use the default field width equal to btnwidth.
		
		// Configure Icon
		if (ImageUrl != "")
			configIcon(guiIcon, ImageUrl);
		
		// Configure Label
		XPropertySet xLabelProps = formatLabelText(xDoc, guiLabel, labeltext);
		sizeLabel(xLabelProps, guiLabel, labeltext);
		setLabelVertPos(xLabelProps);
		
		// Set Field dimensions
		fieldheight = labelheight;								// Use the configured label height here for consistency
		fieldwidth	= (editwidth != 0) ? editwidth : btnwidth;
		
		calcFieldAndBtnVertPos();
		
		calcDialogSize ();
		setDialogSize();
		
		configField();
		configButtons();
		
		guiEditBox.setText(edittext);
		
		return super.show(xDoc, title);
	}
	
	protected void setLabelVertPos (XPropertySet xLabelProps) {
		if ( labelheight < iconsize ) {
			// Vertically center the Label relative to the Icon
			labelposY = padding + (iconsize / 2 - labelheight / 2);
		} else {
			// Vertically position the Label at the same position as the Icon
			labelposY = padding;
		}
		
		try {		
			xLabelProps.setPropertyValue("PositionY", labelposY);
		} catch (Exception e) {
			DlgLogger.log(null, loDialogBox.class.getName(), Level.WARNING, e);
			e.printStackTrace(System.err);
			// Label dimensions will just be wrong.
		}		
	}
	
	private void calcFieldAndBtnVertPos () {
		if ( labelheight < iconsize )
			fieldvertpos = padding + iconsize + gap;
		else
			fieldvertpos = labelposY + labelheight + gap;
		
		btnvertpos = fieldvertpos + fieldheight + fieldborderwidth*2 + gap;
	}
	
	private void calcDialogSize () {
		int contentwidth = iconsize + gap + labelwidth;
		
		if ( fieldwidth > contentwidth )
			contentwidth = fieldwidth;
		
		dialogwidth = (2*padding) + contentwidth;
		dialogwidth += dialogwidth % 2;
		dialogheight = btnvertpos + btnheight + padding;
	}
	
	protected void configField () {
		XPropertySet xEditProps = getControlProps(guiEditBox);
		fieldhorizpos = dialogwidth/2 - fieldwidth/2;
		try {
			xEditProps.setPropertyValue("Width", fieldwidth);
			xEditProps.setPropertyValue("Height", fieldheight);
			xEditProps.setPropertyValue("PositionX", fieldhorizpos);
			xEditProps.setPropertyValue("PositionY", fieldvertpos);
		} catch (Exception e) {
			DlgLogger.log(null, loDialogBox.class.getName(), Level.WARNING, e);
			e.printStackTrace(System.err);
			// Label dimensions will just be wrong.
		}		
	}
	
	private void configButtons () {
		XPropertySet xCancelBtnProps = getControlProps(guiCancelBtn);
		XPropertySet xOKBtnProps	 = getControlProps(guiOKBtn);
				
		try {
			xOKBtnProps.setPropertyValue("PositionX", dialogwidth/2 - btnwidth - gap/2);
			xOKBtnProps.setPropertyValue("PositionY", btnvertpos );
			xCancelBtnProps.setPropertyValue("PositionX", dialogwidth/2 + gap/2);
			xCancelBtnProps.setPropertyValue("PositionY", btnvertpos );
		} catch (Exception e) {
			DlgLogger.log(null, loDialogBox.class.getName(), Level.WARNING, e);
			// nop - buttons will just be misplaced.
		}
	}
	
	public String gettext() {
		return guiEditBox.getText();
	}
	
	public void settext(String s) {
		guiEditBox.setText(s);
	}
}
