package loCommonDialogs;

/*************************************************************************
*
*  The Contents of this file are made available subject to the terms of
*  the BSD license.
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
*  3. Neither the name of Sun Microsystems, Inc. nor the names of its
*     contributors may be used to endorse or promote products derived
*     from this software without specific prior written permission.
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

import com.sun.star.awt.Point;
import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.Rectangle;
import com.sun.star.awt.XButton;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XFixedText;
import com.sun.star.awt.XReschedule;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XToolkit;
import com.sun.star.awt.XTopWindow;
import com.sun.star.awt.XUnitConversion;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.beans.XMultiPropertySet;
import com.sun.star.container.XNameAccess;
import com.sun.star.container.XNameContainer;
import com.sun.star.frame.XModel;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.MeasureUnit;

public class loInputBox implements AutoCloseable {
	
	protected XMultiServiceFactory		m_xMSFDialogModel;
	protected XNameContainer			m_xDlgModelNameContainer;
	protected XControlContainer			m_xDlgContainer;
	protected XControl					m_xDialogControl;
	protected XDialog					xDialog;
	protected XWindowPeer				m_xWindowPeer	= null;
	protected XComponent				m_xComponent 	= null;
	protected XComponentContext 		xContext		= null;
	protected XMultiComponentFactory	xMCF			= null;
	
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
	protected int dialogwidth		= (2*margin) + fieldwidth;
	protected int dialogheight		= btnvertpos + btnheight + margin;
	protected int dialogxpos 		= 0;
	protected int dialogypos 		= 0;

	// Control Return Value Storage
	protected XFixedText		guiLabel;
	protected XTextComponent	guiEditBox;
	protected XButton			guiOKBtn;
	protected XButton			guiCancelBtn;
	
	public loInputBox() {
		xContext = getContext();
		xMCF = xContext.getServiceManager();
		createDialog(xMCF, xContext);
		
		initialize (
			new String[] { "Height", "Moveable", "Name", "PositionX", "PositionY", "Step", "TabIndex", "Title", "Width" },
			new Object[] { dialogheight, true, "MyTestDialog", dialogxpos, dialogypos, 0, (short)0, "LibreOffice", dialogwidth }
		);
   
		// add dialog controls
		try {
			guiLabel	 = insertFixedText(margin, margin, labelwidth, labelheight, 0, "Input something!");
			guiEditBox	 = insertEditField(margin, margin+labelheight+gap, fieldwidth, fieldheight);
			guiOKBtn	 = insertButton(OKhorizpos,     btnvertpos, btnwidth, btnheight, "OK",     (short) PushButtonType.OK_value);
			guiCancelBtn = insertButton(Cancelhorizpos, btnvertpos, btnwidth, btnheight, "Cancel", (short) PushButtonType.CANCEL_value);
		} catch (com.sun.star.uno.Exception e) {
			e.printStackTrace(System.err);
		}
       
		getWindowPeer();
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
	
	public String getText() {
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
   
	public XButton insertButton(int _nPosX, int _nPosY, int _nWidth, int _nHeight, String _sLabel, short _nPushButtonType) throws com.sun.star.uno.Exception {
		XMultiPropertySet xMPSet = _insertPreProc("Button", "com.sun.star.awt.UnoControlButtonModel");
		xMPSet.setPropertyValues(
			new String[]  {"Height", "Label", "PositionX", "PositionY", "PushButtonType", "Width" },	// Remember: Alphabetical Order!
			new Object[] {_nHeight, _sLabel, _nPosX, _nPosY, _nPushButtonType, _nWidth});
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

	protected void createDialog(XMultiComponentFactory _xMCF, XComponentContext _xContext) {
		try {
			Object oDialogModel =  _xMCF.createInstanceWithContext("com.sun.star.awt.UnoControlDialogModel", _xContext);
			
			// The XMultiServiceFactory of the dialogmodel is needed to instantiate the controls...
			m_xMSFDialogModel = UnoRuntime.queryInterface(XMultiServiceFactory.class, oDialogModel);
			
			// The named container is used to insert the created controls into...
			m_xDlgModelNameContainer = UnoRuntime.queryInterface(XNameContainer.class, oDialogModel);
			
			// create the dialog...
			Object oUnoDialog = _xMCF.createInstanceWithContext("com.sun.star.awt.UnoControlDialog", _xContext);
			m_xDialogControl = UnoRuntime.queryInterface(XControl.class, oUnoDialog);
			
			// The scope of the control container is public...
			m_xDlgContainer = UnoRuntime.queryInterface(XControlContainer.class, oUnoDialog);
			
			UnoRuntime.queryInterface(XTopWindow.class, m_xDlgContainer);
			
			// link the dialog and its model...
			XControlModel xControlModel = UnoRuntime.queryInterface(XControlModel.class, oDialogModel);
			m_xDialogControl.setModel(xControlModel);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	} 
	
	private void centerBox(XModel xDoc) {
		XWindow loWindow = xDoc.getCurrentController().getFrame().getContainerWindow();
		Rectangle loWindowRect = loWindow.getPosSize();
		
		Point ptWinSizePixels = new Point(loWindowRect.Width,loWindowRect.Height);
		
		// The following two lines of code were inspired by code at
		// https://github.com/qt-haiku/LibreOffice/blob/master/toolkit/qa/complex/toolkit/UnitConversion.java
		// where an XWindowPeer is used as the Object in the queryInterface call.
		//
		// They work great, but I've been unable to find where XWindowPeer or XWindow implementation or inheritance
		// of the XUnitConversion interface is documented...
		XUnitConversion m_xConversion = UnoRuntime.queryInterface(XUnitConversion.class, loWindow);
		Point ptWinSizeDialog = m_xConversion.convertPointToLogic(ptWinSizePixels, MeasureUnit.APPFONT);
		
		dialogxpos = (int)( (ptWinSizeDialog.X / 2.0) - (dialogwidth  / 2.0) );
		dialogypos = (int)( (ptWinSizeDialog.Y / 2.0) - (dialogheight / 2.0) );
		
		guiEditBox.setText(Integer.toString(dialogxpos) + "," + Integer.toString(dialogypos) + " -- " + ptWinSizeDialog.X + "," + ptWinSizeDialog.Y);

		XControlModel oDialogModel = m_xDialogControl.getModel();
		XMultiPropertySet xMPSet = UnoRuntime.queryInterface(XMultiPropertySet.class, oDialogModel);
		try {
			xMPSet.setPropertyValues( new String[]{"PositionX", "PositionY"},new Object[]{dialogxpos, dialogypos});
		} catch (Exception e) {
			// Do nothing. Dialog will be positioned wherever it was previously.
		}
	}
   
	private XComponentContext getContext() {
		if (xContext == null) {
	        try {
	            // get the remote office component context
	            xContext = com.sun.star.comp.helper.Bootstrap.bootstrap();
	            if( xContext != null )
	                System.out.println("Connected to a running office ...");
	        }
	        catch( Exception e) {
	            e.printStackTrace(System.err);
	            System.exit(1);
	        }
        }
        return xContext;
	}
	
	private XWindowPeer getWindowPeer() {
		if (m_xWindowPeer == null) {
			try {
				XWindow xWindow = UnoRuntime.queryInterface(XWindow.class, m_xDlgContainer);
				xWindow.setVisible(false);
				Object tk = xMCF.createInstanceWithContext("com.sun.star.awt.Toolkit", xContext);
				XToolkit xToolkit = UnoRuntime.queryInterface(XToolkit.class, tk);
				UnoRuntime.queryInterface(XReschedule.class, xToolkit);
				m_xDialogControl.createPeer(xToolkit, null);
				m_xWindowPeer = m_xDialogControl.getPeer();
			} catch( Exception e) {
				e.printStackTrace(System.err);
				return null;
			}
		}
		return m_xWindowPeer;
	}
	
	private void initialize(String[] PropertyNames, Object[] PropertyValues) {
		try {
			XMultiPropertySet xMultiPropertySet = UnoRuntime.queryInterface(XMultiPropertySet.class, m_xDlgModelNameContainer);
			xMultiPropertySet.setPropertyValues(PropertyNames, PropertyValues);
		} catch (com.sun.star.uno.Exception ex) {
			ex.printStackTrace(System.err);
		}
	}
	
	private String createUniqueName(XNameAccess _xElementContainer, String _sElementName) {
		int i=1;
		while ( _xElementContainer.hasByName(_sElementName + Integer.toString(i)) )
			++i;
		return _sElementName + Integer.toString(i);
	}
}
