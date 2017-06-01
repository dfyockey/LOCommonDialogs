package loCommonDialogs;

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
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XFixedText;
import com.sun.star.awt.XTextComponent;
import com.sun.star.beans.XMultiPropertySet;
import com.sun.star.container.XNameAccess;
import com.sun.star.frame.XModel;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

public class loInputBox extends loDialogBox implements AutoCloseable {
		
	// Dialog and Control Size & Position Values
	protected int margin			= 8;
	protected int fieldwidth		= 120;	// Should be >= btngap+(2*btnwidth)
	protected int fieldheight		= 12;
	protected int labelwidth		= fieldwidth;
	protected int labelheight		= 8;
	protected int btnwidth			= 32;
	protected int btnheight			= 14;
	protected int gap				= 3;
	protected int btnvertpos		= margin + labelheight + fieldheight + (2*gap);
	protected int OKhorizpos		= margin + fieldwidth - (2*btnwidth) - gap;
	protected int Cancelhorizpos	= margin + fieldwidth - btnwidth;

	// Control Return Value Storage
	protected XFixedText		guiLabel;
	protected XTextComponent	guiEditBox;
	protected XButton			guiOKBtn;
	protected XButton			guiCancelBtn;
	
	public loInputBox() {
		super();
		initBox();
	}
	
	public loInputBox(XComponentContext xComponentContext) {
		super(xComponentContext);
		initBox();
	}
	
	protected void initBox() {
		xMCF = xContext.getServiceManager();
		createDialog(xMCF, xContext);
		
		dialogwidth  = (2*margin) + fieldwidth;
		dialogheight = btnvertpos + btnheight + margin;
		dialogxpos   = 0;
		dialogypos   = 0;
		
		initialize (
				new String[] { "Height", "Moveable", "Name", "PositionX", "PositionY", "Step", "TabIndex", "Title", "Width" },
				new Object[] { dialogheight, true, "loInputBox", dialogxpos, dialogypos, 0, (short)0, "loInputBox", dialogwidth }
		);
		
		// add dialog controls
		try {
			guiLabel	 = insertFixedText(margin, margin, labelwidth, labelheight, 0, "Input something!");
			guiEditBox	 = insertEditField(margin, margin+labelheight+gap, fieldwidth, fieldheight);
			guiOKBtn	 = insertButton(OKhorizpos,     btnvertpos, btnwidth, btnheight, "OK",     (short) PushButtonType.OK_value,		true );
			guiCancelBtn = insertButton(Cancelhorizpos, btnvertpos, btnwidth, btnheight, "Cancel", (short) PushButtonType.CANCEL_value, false);
		} catch (com.sun.star.uno.Exception e) {
			e.printStackTrace(System.err);
		}
		
		m_xWindowPeer = getWindowPeer();
		xDialog = UnoRuntime.queryInterface(XDialog.class, m_xDialogControl);
	}
	
	public short show(XModel xDoc, String title, String labeltext, String edittext) {
		xDialog.setTitle(title);
		guiLabel.setText(labeltext);
		guiEditBox.setText(edittext);
		
		getWindowPeer();
	    xDialog		 = UnoRuntime.queryInterface(XDialog.class, m_xDialogControl);
	    m_xComponent = UnoRuntime.queryInterface(XComponent.class, m_xDialogControl);

		centerBox(xDoc);
	    
	    return xDialog.execute();
	}
	
	public String gettext() {
		return guiEditBox.getText();
	}

	public void close() throws Exception {
		// Dispose the component and free the memory...
        if (m_xComponent != null){
            m_xComponent.dispose();
        }		
	}	

	//////////////////////////////////////////////////////////////////////
	//////////  Control Insert Methods  //////////////////////////////////
	
	private XMultiPropertySet _insertPreProc(String controlname, String fullmodel) throws com.sun.star.uno.Exception {
		// create a unique name by means of an own implementation...
		String sName = createUniqueName(m_xDlgModelNameContainer, controlname);
		
		// create a controlmodel at the multiservicefactory of the dialog model...
        Object oFTModel = m_xMSFDialogModel.createInstance(fullmodel);
        XMultiPropertySet xFTModelMPSet = UnoRuntime.queryInterface(XMultiPropertySet.class, oFTModel);
        
        xFTModelMPSet.setPropertyValues(new String[]{"Name"},new Object[]{sName});
        
        // add the model to the NameContainer of the dialog model
        m_xDlgModelNameContainer.insertByName(sName, oFTModel);
        
        return xFTModelMPSet;
	}
	
	private XFixedText insertFixedText(int _nPosX, int _nPosY, int _nWidth, int _nHeight, int _nStep, String _sLabel) throws com.sun.star.uno.Exception {
		XMultiPropertySet xMPSet = _insertPreProc("Label", "com.sun.star.awt.UnoControlFixedTextModel");
		xMPSet.setPropertyValues(
			new String[] {"Height", "Label", "PositionX", "PositionY", "Step", "Width"},		// Remember: Alphabetical Order!
			new Object[] { _nHeight, _sLabel, _nPosX, _nPosY, _nStep, _nWidth});
		return (XFixedText) _insertPostProc(XFixedText.class, xMPSet);
	}
   
	private XTextComponent insertEditField(int _nPosX, int _nPosY, int _nWidth, int _nHeight) throws com.sun.star.uno.Exception {
		XMultiPropertySet xMPSet = _insertPreProc("TextField", "com.sun.star.awt.UnoControlEditModel");
		xMPSet.setPropertyValues(
			new String[] {"Height", "PositionX", "PositionY", "Text", "Width"},		// Remember: Alphabetical Order!
			new Object[] { _nHeight, _nPosX, _nPosY, "MyText", _nWidth});
		return (XTextComponent) _insertPostProc(XTextComponent.class, xMPSet);
	}
   
	public XButton insertButton(int _nPosX, int _nPosY, int _nWidth, int _nHeight, String _sLabel, short _nPushButtonType, boolean _bDefaultButton) throws com.sun.star.uno.Exception {
		XMultiPropertySet xMPSet = _insertPreProc("Button", "com.sun.star.awt.UnoControlButtonModel");
		xMPSet.setPropertyValues(
			new String[] {"DefaultButton", "Height", "Label", "PositionX", "PositionY", "PushButtonType", "Width" },	// Remember: Alphabetical Order!
			new Object[] {_bDefaultButton, _nHeight, _sLabel, _nPosX, _nPosY, _nPushButtonType, _nWidth});
		return (XButton) _insertPostProc(XButton.class, xMPSet);
	}
   
	private Object _insertPostProc(Class<?> c, XMultiPropertySet xMPSet) {
		// Return the interface for the specified class
		Object[] sName = xMPSet.getPropertyValues( new String[]{"Name"});
		XControl xControl = m_xDlgContainer.getControl((String)sName[0]);
		return UnoRuntime.queryInterface(c, xControl);
	}
	
	//////////////////////////////////////////////////////////////////////
	//////////  Utility Methods  /////////////////////////////////////////

	private String createUniqueName(XNameAccess _xElementContainer, String _sElementName) {
		int i=1;
		while ( _xElementContainer.hasByName(_sElementName + Integer.toString(i)) )
			++i;
		return _sElementName + Integer.toString(i);
	}
}
