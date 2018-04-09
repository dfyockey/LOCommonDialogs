package loCommonDialogs;

import javax.xml.bind.DatatypeConverter;

import com.sun.star.awt.FontDescriptor;
import com.sun.star.awt.FontWeight;

// Title  : loInputBox - Java class to display a simple inputbox in a LibreOffice document
// Author : David Yockey
// Email  : software@diffengine.net
//
// Based on UnoDialogSample.java, located at http://api.libreoffice.org/examples/DevelopersGuide/GUI/UnoDialogSample.java

/*************************************************************************
*
*  The Contents of this file are made available subject to the terms of
*  the 3-clause BSD license.
*  
*  Copyright 2016 David Yockey
*
*  Copyright 2000, 2010 Oracle and/or its affiliates.
*  All rights reserved.
*
*  Redistribution and use in source and binary forms, with or without
*  modification, are permitted provided that the following conditions
*  are met:
*  1. Redistributions of source code must retain the above copyright
*     notice, this list of conditions and the following disclaimer.
*  2. Redistributions in binary form must reproduce the above copyright
*     notice, this list of conditions and the following disclaimer in the
*     documentation and/or other materials provided with the distribution.
*  3. Neither the name of the Author, the name of Sun Microsystems, Inc.,
*     nor the names of its contributors may be used to endorse or promote
*     products derived from this software without specific prior written
*     permission.
*
*  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
*  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
*  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
*  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
*  COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
*  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
*  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
*  OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
*  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
*  TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
*  USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
*************************************************************************/


/*
// USAGE INFORMATION
//
// Instantiate loInputBox, and then call show() with appropriate values
// for its parameters to display an inputbox.  Upon return, call gettext()
// to retrieve text typed in the inputbox's edit box.
//
// Do NOT call close(). It will be called automatically when necessary.

 * Return Values:
 * 
 * Returns 0 on Cancel, 1 on OK
*/


import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XButton;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XFixedText;
import com.sun.star.awt.XStyleSettings;
import com.sun.star.awt.XStyleSettingsSupplier;
import com.sun.star.awt.XTextComponent;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XModel;
import com.sun.star.graphic.XGraphic;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

public class loInputBox extends loDialogBox implements AutoCloseable {
		
	// Dialog and Control Size & Position Values
	protected int fieldvertpos;
	protected int fieldhorizpos;
	protected int btnvertpos;
	protected int OKhorizpos;		//= dialogwidth/2 - btnwidth - gap/2; //margin + fieldwidth - 2*btnwidth - gap;
	protected int Cancelhorizpos;	//= dialogwidth/2 + gap/2; //margin + fieldwidth - btnwidth;
	protected int labelposX;
	protected int labelposY;
	
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
		
		iconsize		= 32;
		dialogwidth		= 200;
		OKhorizpos		= dialogwidth/2 - btnwidth - gap/2;
		Cancelhorizpos	= dialogwidth/2 + gap/2;
		btnvertpos		= margin + iconsize + fieldheight + 4*gap;
		fieldwidth		= dialogwidth/4 - (2*margin);
		fieldvertpos	= margin + iconsize + gap;
		fieldhorizpos	= dialogwidth/2 - fieldwidth/2;
		labelwidth		= dialogwidth - (2*margin) - iconsize - gap;
		labelposX		= margin + iconsize + gap;
		labelposY		= margin + iconsize/2 - labelheight/2;
		dialogheight	= btnvertpos + btnheight + margin;
		dialogxpos  	= 0;
		dialogypos  	= 0;
		
		initialize (
				new String[] { "Height", "Moveable", "Name", "PositionX", "PositionY", "Step", "TabIndex", "Title", "Width" },
				new Object[] { dialogheight, true, "loInputBox", dialogxpos, dialogypos, 0, (short)0, "loInputBox", dialogwidth }
		);
		
		// add dialog controls
		try {
			String rawhexMessage = "";
			byte[] hexbinaryMessage = DatatypeConverter.parseHexBinary(rawhexMessage);
			guiIcon = insertImage(margin, margin, iconsize, iconsize, hexbinaryMessage, "png");
						
			guiLabel	 = insertFixedText(textalign_center, labelposX, labelposY, labelwidth, labelheight, 0, "Input something!");
			guiEditBox	 = insertEditField(textalign_center, fieldhorizpos, fieldvertpos, fieldwidth, fieldheight);
			guiOKBtn	 = insertButton(OKhorizpos,     btnvertpos, btnwidth, btnheight, "OK",     (short) PushButtonType.OK_value,		true );
			guiCancelBtn = insertButton(Cancelhorizpos, btnvertpos, btnwidth, btnheight, "Cancel", (short) PushButtonType.CANCEL_value, false);
		} catch (com.sun.star.uno.Exception e) {
			e.printStackTrace(System.err);
		}
		
		m_xWindowPeer = getWindowPeer();
		xDialog = UnoRuntime.queryInterface(XDialog.class, m_xDialogControl);
	}
	
	public short show(XModel xDoc, String title, String labeltext, String edittext, String rawhexPng) {
		XPropertySet xIconProps = null;
		XGraphic 	 xGraphic	= null;
		
		// Configure Icon
		if ( rawhexPng != null ) {
			byte[] hexbinaryIcon = DatatypeConverter.parseHexBinary(rawhexPng);
			
			// If getGraphic throws, just continue; the default icon will be used.
			try {
				xGraphic = getGraphic(hexbinaryIcon, "png");
				
				//// Get Label XPropertySet interface
				XControl xIconControl = UnoRuntime.queryInterface(XControl.class, guiIcon);
				XControlModel xIconControlModel = xIconControl.getModel();
				xIconProps = UnoRuntime.queryInterface(XPropertySet.class, xIconControlModel);
				
				if (xIconProps != null)
					xIconProps.setPropertyValue("Graphic", xGraphic);				
			} catch (Exception e) {
				// nop - there'll just be no custom icon
			}
		}
		
		// Configure Inputbox Text to the current Application Font and at size 12pt and BOLD
		//// Get Label XPropertySet interface
		XControl xControl = UnoRuntime.queryInterface(XControl.class, guiLabel);
		XControlModel xControlModel = xControl.getModel();
		XPropertySet xLabelProps = UnoRuntime.queryInterface(XPropertySet.class, xControlModel);
		
		//// Get FontDescriptor for Application Font
		XStyleSettingsSupplier xStyleSettingsSupplier = UnoRuntime.queryInterface(XStyleSettingsSupplier.class, xDoc.getCurrentController().getFrame().getContainerWindow());
		XStyleSettings xStyleSettings = xStyleSettingsSupplier.getStyleSettings();
		FontDescriptor appFontDescriptor = xStyleSettings.getApplicationFont();
		appFontDescriptor.Height = 12;
		appFontDescriptor.Weight = FontWeight.BOLD;
		
		try {
			xLabelProps.setPropertyValue("FontDescriptor", appFontDescriptor);
		} catch (Exception e) {
			// nop - text just won't be bold
		}
		
		guiLabel.setText(labeltext);
		guiEditBox.setText(edittext);
		
		return super.show(xDoc, title);
	}
	
	public String gettext() {
		return guiEditBox.getText();
	}

	public void close() {
		// Dispose the component and free the memory...
        if (m_xComponent != null){
            m_xComponent.dispose();
        }
	}
}
