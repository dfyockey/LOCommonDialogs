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

import com.sun.star.awt.PushButtonType;
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
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.beans.XMultiPropertySet;
import com.sun.star.container.XNameAccess;
import com.sun.star.container.XNameContainer;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

public class loInputBox {
	
	protected static XMultiServiceFactory	m_xMSFDialogModel;
	protected static XNameContainer			m_xDlgModelNameContainer;
	protected static XControlContainer		m_xDlgContainer;
	protected static XControl				m_xDialogControl;
	protected static XDialog				xDialog;
	protected static XWindowPeer			m_xWindowPeer	= null;
	protected static XComponent				m_xComponent 	= null;
	protected static XComponentContext 		xContext		= null;
	protected static XMultiComponentFactory xMCF			= null;
	
	// Dialog and Control Size & Position Values
	protected static int margin			= 8;
	protected static int fieldwidth		= 120;	// Should be >= btngap+(2*btnwidth)
	protected static int fieldheight	= 12;
	protected static int labelwidth		= fieldwidth;
	protected static int labelheight	= 8;
	protected static int btnwidth		= 32;
	protected static int btnheight		= 14;
	protected static int gap			= 3;
	protected static int btnvertpos		= margin + labelheight + fieldheight + (2*gap);
	protected static int OKhorizpos		= margin + fieldwidth - (2*btnwidth) - gap;
	protected static int Cancelhorizpos	= margin + fieldwidth - btnwidth;
	protected static int dialogwidth	= (2*margin) + fieldwidth;
	protected static int dialogheight	= btnvertpos + btnheight + margin;

	// Control Return Value Storage
	protected static XFixedText		guiLabel;
	protected static XTextComponent	guiEditBox;
	protected static XButton		guiOKBtn;
	protected static XButton		guiCancelBtn;
	
	public loInputBox() {
		xContext = getContext();
		xMCF = xContext.getServiceManager();
		createDialog(xMCF, xContext);
			   
		initialize (
			new String[] { "Height", "Moveable", "Name", "PositionX", "PositionY", "Step", "TabIndex", "Title", "Width" },
			new Object[] {
							dialogheight, Boolean.TRUE, "MyTestDialog", Integer.valueOf(102),Integer.valueOf(41),
							Integer.valueOf(0), Short.valueOf((short) 0), "LibreOffice", dialogwidth
						 }
		);
		   
		// add dialog controls
		try {
			guiLabel		= insertFixedText(margin, margin, labelwidth, labelheight, 0, "Input something!");
			guiEditBox	= insertEditField(margin, margin+labelheight+gap, fieldwidth, fieldheight);
			guiOKBtn		= insertButton(OKhorizpos,     btnvertpos, btnwidth, btnheight, "OK",     (short) PushButtonType.OK_value);
			guiCancelBtn = insertButton(Cancelhorizpos, btnvertpos, btnwidth, btnheight, "Cancel", (short) PushButtonType.CANCEL_value);
		} catch (com.sun.star.uno.Exception e) {
			e.printStackTrace(System.err);
		}
		       
		getWindowPeer();
		xDialog = UnoRuntime.queryInterface(XDialog.class, m_xDialogControl);
	}

   
	public static short show(loInputBox hBox, String title, String labeltext, String edittext){
		xDialog.setTitle(title);
		guiLabel.setText(labeltext);
		guiEditBox.setText(edittext);
		
		getWindowPeer();
	    xDialog		 = UnoRuntime.queryInterface(XDialog.class, m_xDialogControl);
	    m_xComponent = UnoRuntime.queryInterface(XComponent.class, m_xDialogControl);
	    
	    return xDialog.execute();
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
	
	public static XWindowPeer getWindowPeer() {
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
	
	public void initialize(String[] PropertyNames, Object[] PropertyValues) {
		try{
			XMultiPropertySet xMultiPropertySet = UnoRuntime.queryInterface(XMultiPropertySet.class, m_xDlgModelNameContainer);
			xMultiPropertySet.setPropertyValues(PropertyNames, PropertyValues);
		} catch (com.sun.star.uno.Exception ex) {
			ex.printStackTrace(System.err);
		}
	}
	   
	public static String createUniqueName(XNameAccess _xElementContainer, String _sElementName) {
		int i=1;
		while ( _xElementContainer.hasByName(_sElementName + Integer.toString(i)) )
			++i;
		return _sElementName + Integer.toString(i);
	}
   
	//////////////////////////////////////////////////////////////////////

   public static void main(String args[]) {
	   loInputBox oUnoDialogSample = null;

       try {
    	   oUnoDialogSample = new loInputBox();
           short dlgResult = show(oUnoDialogSample, "LibreOffice InputBox", "Type in some text:", "Type the text here");
           System.out.println(oUnoDialogSample.guiEditBox.getText());
           System.out.println(Integer.toString(dlgResult));
       } catch ( Exception e ) {
           System.err.println( e + e.getMessage());
           e.printStackTrace();
       } finally{
           //make sure always to dispose the component and free the memory!
           if (oUnoDialogSample != null){
               if (loInputBox.m_xComponent != null){
                   loInputBox.m_xComponent.dispose();
               }
           }
       }

       System.exit( 0 );
   }
}
