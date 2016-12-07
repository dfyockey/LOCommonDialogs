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

import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.FocusChangeReason;
import com.sun.star.awt.FocusEvent;
import com.sun.star.awt.KeyEvent;
import com.sun.star.awt.MouseEvent;
import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.TextEvent;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.XButton;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XFixedText;
import com.sun.star.awt.XFocusListener;
import com.sun.star.awt.XKeyListener;
import com.sun.star.awt.XMouseListener;
import com.sun.star.awt.XPointer;
import com.sun.star.awt.XReschedule;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XTextListener;
import com.sun.star.awt.XToolkit;
import com.sun.star.awt.XTopWindow;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.beans.XMultiPropertySet;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XNameAccess;
import com.sun.star.container.XNameContainer;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;


//Anregung von DV:
//Position und Weite als Parameter uebergeben

public class loInputBox implements XTextListener, XActionListener, XFocusListener, XMouseListener, XKeyListener {
   protected XComponentContext m_xContext = null;
   protected com.sun.star.lang.XMultiComponentFactory m_xMCF;
   protected XMultiServiceFactory m_xMSFDialogModel;
   protected XNameContainer m_xDlgModelNameContainer;
   protected XControlContainer m_xDlgContainer;
//   protected XNameAccess m_xDlgModelNameAccess;
   protected XControl m_xDialogControl;
   protected XDialog xDialog;

   protected XWindowPeer m_xWindowPeer = null;

   protected XFrame m_xFrame = null;
   protected XComponent m_xComponent = null;
   
   private static int margin 	  = 8;
   private static int fieldwidth  = 120;	// Should be >= btngap+(2*btnwidth) for asthetic reasons
   private static int fieldheight = 12;
   private static int labelwidth  = fieldwidth;
   private static int labelheight = 8; 
   private static int btnwidth    = 32;
   private static int btnheight   = 14;
   private static int gap		  = 3;
   private static int btnvertpos  = margin + labelheight + fieldheight + (2*gap);
   private static int OKhorizpos  = margin + fieldwidth - (2*btnwidth) - gap;
   private static int Cancelhorizpos = margin + fieldwidth - btnwidth;
   private static int dialogwidth    = (2*margin) + fieldwidth;
   private static int dialogheight   = btnvertpos + btnheight + margin;
   
   private static XTextComponent guiEditBox;
 
   
   /**
    * Creates a new instance of UnoDialogSample
    */
   public loInputBox(XComponentContext _xContext, XMultiComponentFactory _xMCF) {
       m_xContext = _xContext;
       m_xMCF = _xMCF;
       createDialog(m_xMCF);
   }


   public static void main(String args[]) {
	   loInputBox oUnoDialogSample = null;

       try {
           XComponentContext xContext = com.sun.star.comp.helper.Bootstrap.bootstrap();
           if(xContext != null )
               System.out.println("Connected to a running office ...");
           XMultiComponentFactory xMCF = xContext.getServiceManager();
           oUnoDialogSample = new loInputBox(xContext, xMCF);
           oUnoDialogSample.initialize( new String[] {"Height", "Moveable", "Name","PositionX","PositionY", "Step", "TabIndex","Title","Width"},
                   new Object[] { dialogheight, Boolean.TRUE, "MyTestDialog", Integer.valueOf(102),Integer.valueOf(41), Integer.valueOf(0), Short.valueOf((short) 0), "LibreOffice", dialogwidth});

           // add dialog controls
           oUnoDialogSample.insertFixedText(oUnoDialogSample, margin, margin, labelwidth, labelheight, 0, "Input something!");
           guiEditBox = oUnoDialogSample.insertEditField(oUnoDialogSample, oUnoDialogSample, margin, margin+labelheight+gap, fieldwidth, fieldheight);
           oUnoDialogSample.insertButton(oUnoDialogSample, OKhorizpos,     btnvertpos, btnwidth, btnheight, "OK",     (short) PushButtonType.OK_value);
           oUnoDialogSample.insertButton(oUnoDialogSample, Cancelhorizpos, btnvertpos, btnwidth, btnheight, "Cancel", (short) PushButtonType.CANCEL_value);
           oUnoDialogSample.createWindowPeer();
           
           oUnoDialogSample.xDialog = UnoRuntime.queryInterface(XDialog.class, oUnoDialogSample.m_xDialogControl);
           System.out.println("Execute dialog...");
           short dlgResult = oUnoDialogSample.executeDialog();
           System.out.println("Dialog executed.");
           System.out.println(guiEditBox.getText());
           System.out.println(Integer.toString(dlgResult));
       }catch( Exception e ) {
           System.err.println( e + e.getMessage());
           e.printStackTrace();
       } finally{
           //make sure always to dispose the component and free the memory!
           if (oUnoDialogSample != null){
               if (oUnoDialogSample.m_xComponent != null){
                   oUnoDialogSample.m_xComponent.dispose();
               }
           }
       }

       System.exit( 0 );
   }


   protected void createDialog(XMultiComponentFactory _xMCF) {
       try {
           Object oDialogModel =  _xMCF.createInstanceWithContext("com.sun.star.awt.UnoControlDialogModel", m_xContext);

           // The XMultiServiceFactory of the dialogmodel is needed to instantiate the controls...
           m_xMSFDialogModel = UnoRuntime.queryInterface(XMultiServiceFactory.class, oDialogModel);

           // The named container is used to insert the created controls into...
           m_xDlgModelNameContainer = UnoRuntime.queryInterface(XNameContainer.class, oDialogModel);

           // create the dialog...
           Object oUnoDialog = _xMCF.createInstanceWithContext("com.sun.star.awt.UnoControlDialog", m_xContext);
           m_xDialogControl = UnoRuntime.queryInterface(XControl.class, oUnoDialog);

           // The scope of the control container is public...
           m_xDlgContainer = UnoRuntime.queryInterface(XControlContainer.class, oUnoDialog);

           UnoRuntime.queryInterface(XTopWindow.class, m_xDlgContainer);

           // link the dialog and its model...
           XControlModel xControlModel = UnoRuntime.queryInterface(XControlModel.class, oDialogModel);
           m_xDialogControl.setModel(xControlModel);
       } catch (com.sun.star.uno.Exception exception) {
           exception.printStackTrace(System.err);
       }
   }






   public short executeDialog() {
       if (m_xWindowPeer == null) {
           createWindowPeer();
       }
       xDialog = UnoRuntime.queryInterface(XDialog.class, m_xDialogControl);
       m_xComponent = UnoRuntime.queryInterface(XComponent.class, m_xDialogControl);
       return xDialog.execute();
   }



/*
   public XItemListener getRoadmapItemStateChangeListener(){
       return new RoadmapItemStateChangeListener(m_xMSFDialogModel);
   }
*/

   public void initialize(String[] PropertyNames, Object[] PropertyValues) {
       try{
           XMultiPropertySet xMultiPropertySet = UnoRuntime.queryInterface(XMultiPropertySet.class, m_xDlgModelNameContainer);
           xMultiPropertySet.setPropertyValues(PropertyNames, PropertyValues);
       } catch (com.sun.star.uno.Exception ex) {
           ex.printStackTrace(System.err);
       }}



   /**
    * create a peer for this
    * dialog, using the given
    * peer as a parent.
    */
   private XWindowPeer createWindowPeer(XWindowPeer _xWindowParentPeer) {
       try{
           if (_xWindowParentPeer == null){
               XWindow xWindow = UnoRuntime.queryInterface(XWindow.class, m_xDlgContainer);
               xWindow.setVisible(false);
               Object tk = m_xMCF.createInstanceWithContext("com.sun.star.awt.Toolkit", m_xContext);
               XToolkit xToolkit = UnoRuntime.queryInterface(XToolkit.class, tk);
               UnoRuntime.queryInterface(XReschedule.class, xToolkit);
               m_xDialogControl.createPeer(xToolkit, _xWindowParentPeer);
               m_xWindowPeer = m_xDialogControl.getPeer();
               return m_xWindowPeer;
           }
       } catch (com.sun.star.uno.Exception exception) {
           exception.printStackTrace(System.err);
       }
       return null;
   }






   /**
    * Creates a peer for this
    * dialog, using the active OO frame
    * as the parent window.
    */
   public XWindowPeer createWindowPeer() {
       return createWindowPeer(null);
   }







   private XFixedText insertFixedText(XMouseListener _xMouseListener, int _nPosX, int _nPosY, int _nWidth, int _nHeight, int _nStep, String _sLabel){
       XFixedText xFixedText = null;
       try{
           // create a unique name by means of an own implementation...
           String sName = createUniqueName(m_xDlgModelNameContainer, "Label");

           // create a controlmodel at the multiservicefactory of the dialog model...
           Object oFTModel = m_xMSFDialogModel.createInstance("com.sun.star.awt.UnoControlFixedTextModel");
           XMultiPropertySet xFTModelMPSet = UnoRuntime.queryInterface(XMultiPropertySet.class, oFTModel);
           // Set the properties at the model - keep in mind to pass the property names in alphabetical order!

           xFTModelMPSet.setPropertyValues(
                   new String[] {"Height", "Name", "PositionX", "PositionY", "Step", "Width"},
                   new Object[] { _nHeight, sName, Integer.valueOf(_nPosX), Integer.valueOf(_nPosY), Integer.valueOf(_nStep), Integer.valueOf(_nWidth)});
           // add the model to the NameContainer of the dialog model
           m_xDlgModelNameContainer.insertByName(sName, oFTModel);

           // The following property may also be set with XMultiPropertySet but we
           // use the XPropertySet interface merely for reasons of demonstration
           XPropertySet xFTPSet = UnoRuntime.queryInterface(XPropertySet.class, oFTModel);
           xFTPSet.setPropertyValue("Label", _sLabel);

           // reference the control by the Name
           XControl xFTControl = m_xDlgContainer.getControl(sName);
           xFixedText = UnoRuntime.queryInterface(XFixedText.class, xFTControl);
           XWindow xWindow = UnoRuntime.queryInterface(XWindow.class, xFTControl);
           xWindow.addMouseListener(_xMouseListener);
       } catch (com.sun.star.uno.Exception ex) {
           /* perform individual exception handling here.
            * Possible exception types are:
            * com.sun.star.lang.IllegalArgumentException,
            * com.sun.star.lang.WrappedTargetException,
            * com.sun.star.container.ElementExistException,
            * com.sun.star.beans.PropertyVetoException,
            * com.sun.star.beans.UnknownPropertyException,
            * com.sun.star.uno.Exception
            */
           ex.printStackTrace(System.err);
       }
       return xFixedText;
   }




   private XTextComponent insertEditField(XTextListener _xTextListener, XFocusListener _xFocusListener, int _nPosX, int _nPosY, int _nWidth, int _nHeight){
       XTextComponent xTextComponent = null;
       try{
           // create a unique name by means of an own implementation...
           String sName = createUniqueName(m_xDlgModelNameContainer, "TextField");

           // create a controlmodel at the multiservicefactory of the dialog model...
           Object oTFModel = m_xMSFDialogModel.createInstance("com.sun.star.awt.UnoControlEditModel");
           XMultiPropertySet xTFModelMPSet = UnoRuntime.queryInterface(XMultiPropertySet.class, oTFModel);

           // Set the properties at the model - keep in mind to pass the property names in alphabetical order!
           xTFModelMPSet.setPropertyValues(
                   new String[] {"Height", "Name", "PositionX", "PositionY", "Text", "Width"},
                   new Object[] { _nHeight, sName, Integer.valueOf(_nPosX), Integer.valueOf(_nPosY), "MyText", Integer.valueOf(_nWidth)});

           // The controlmodel is not really available until inserted to the Dialog container
           m_xDlgModelNameContainer.insertByName(sName, oTFModel);
           XPropertySet xTFModelPSet = UnoRuntime.queryInterface(XPropertySet.class, oTFModel);

           // The following property may also be set with XMultiPropertySet but we
           // use the XPropertySet interface merely for reasons of demonstration
//           xTFModelPSet.setPropertyValue("EchoChar", Short.valueOf((short) '*'));
           XControl xTFControl = m_xDlgContainer.getControl(sName);

           // add a textlistener that is notified on each change of the controlvalue...
           xTextComponent = UnoRuntime.queryInterface(XTextComponent.class, xTFControl);
           XWindow xTFWindow = UnoRuntime.queryInterface(XWindow.class, xTFControl);
           xTFWindow.addFocusListener(_xFocusListener);
           xTextComponent.addTextListener(_xTextListener);
           xTFWindow.addKeyListener(this);
       } catch (com.sun.star.uno.Exception ex) {
           /* perform individual exception handling here.
            * Possible exception types are:
            * com.sun.star.lang.IllegalArgumentException,
            * com.sun.star.lang.WrappedTargetException,
            * com.sun.star.container.ElementExistException,
            * com.sun.star.beans.PropertyVetoException,
            * com.sun.star.beans.UnknownPropertyException,
            * com.sun.star.uno.Exception
            */
           ex.printStackTrace(System.err);
       }
       return xTextComponent;
   }

   
   /** makes a String unique by appending a numerical suffix
    * @param _xElementContainer the com.sun.star.container.XNameAccess container
    * that the new Element is going to be inserted to
    * @param _sElementName the StemName of the Element
    */
   public static String createUniqueName(XNameAccess _xElementContainer, String _sElementName) {
       boolean bElementexists = true;
       int i = 1;
       String BaseName = _sElementName;
       _sElementName = BaseName + Integer.toString(i);
       while (bElementexists) {
           bElementexists = _xElementContainer.hasByName(_sElementName);
           if (bElementexists) {
               i += 1;
               _sElementName = BaseName + Integer.toString(i);
           }
       }
       return _sElementName;
   }



   public XButton insertButton(XActionListener _xActionListener, int _nPosX, int _nPosY, int _nWidth, int _nHeight, String _sLabel, short _nPushButtonType){
       XButton xButton = null;
       try{
           // create a unique name by means of an own implementation...
           String sName = createUniqueName(m_xDlgModelNameContainer, "CmdButton");

           // create a controlmodel at the multiservicefactory of the dialog model...
           Object oButtonModel = m_xMSFDialogModel.createInstance("com.sun.star.awt.UnoControlButtonModel");
           XMultiPropertySet xButtonMPSet = UnoRuntime.queryInterface(XMultiPropertySet.class, oButtonModel);
           // Set the properties at the model - keep in mind to pass the property names in alphabetical order!
           xButtonMPSet.setPropertyValues(
                   new String[]  {"Height", "Label", "Name", "PositionX", "PositionY", "PushButtonType", "Width" } ,
                   new Object[] {_nHeight, _sLabel, sName, Integer.valueOf(_nPosX), Integer.valueOf(_nPosY), Short.valueOf(_nPushButtonType), Integer.valueOf(_nWidth)});

           // add the model to the NameContainer of the dialog model
           System.out.println(sName);
           m_xDlgModelNameContainer.insertByName(sName, oButtonModel);
           XControl xButtonControl = m_xDlgContainer.getControl(sName);
           xButton = UnoRuntime.queryInterface(XButton.class, xButtonControl);
           // An ActionListener will be notified on the activation of the button...
           xButton.addActionListener(_xActionListener);
           System.out.println("Added _xActionListener");
       } catch (com.sun.star.uno.Exception ex) {
           /* perform individual exception handling here.
            * Possible exception types are:
            * com.sun.star.lang.IllegalArgumentException,
            * com.sun.star.lang.WrappedTargetException,
            * com.sun.star.container.ElementExistException,
            * com.sun.star.beans.PropertyVetoException,
            * com.sun.star.beans.UnknownPropertyException,
            * com.sun.star.uno.Exception
            */
           ex.printStackTrace(System.err);
       }
       return xButton;
   }



   public void textChanged(TextEvent textEvent) {
       try {
           // get the control that has fired the event,
           XControl xControl = UnoRuntime.queryInterface(XControl.class, textEvent.Source);
           XControlModel xControlModel = xControl.getModel();
           XPropertySet xPSet = UnoRuntime.queryInterface(XPropertySet.class, xControlModel);
           String sName = (String) xPSet.getPropertyValue("Name");
           // just in case the listener has been added to several controls,
           // we make sure we refer to the right one
           if (sName.equals("TextField1")){
               String sText = (String) xPSet.getPropertyValue("Text");
               System.out.println(sText);
               // insert your code here to validate the text of the control...
           }
       }catch (com.sun.star.uno.Exception ex){
           /* perform individual exception handling here.
            * Possible exception types are:
            * com.sun.star.lang.WrappedTargetException,
            * com.sun.star.beans.UnknownPropertyException,
            * com.sun.star.uno.Exception
            */
           ex.printStackTrace(System.err);
       }
   }


   public void disposing(EventObject rEventObject) {
   }


   public void actionPerformed(ActionEvent rEvent) {
	   System.out.println("Something was clicked...");
       try{
           // get the control that has fired the event,
           XControl xControl = UnoRuntime.queryInterface(XControl.class, rEvent.Source);
           XControlModel xControlModel = xControl.getModel();
           XPropertySet xPSet = UnoRuntime.queryInterface(XPropertySet.class, xControlModel);
           String sName = (String) xPSet.getPropertyValue("Name");
           // just in case the listener has been added to several controls,
           // we make sure we refer to the right one
           if (sName.equals("CmdButton1")) {
        	   System.out.println("Click!");
           }
       }catch (com.sun.star.uno.Exception ex){
           /* perform individual exception handling here.
            * Possible exception types are:
            * com.sun.star.lang.WrappedTargetException,
            * com.sun.star.beans.UnknownPropertyException,
            * com.sun.star.uno.Exception
            */
           ex.printStackTrace(System.err);
       }
   }


   public void focusLost(FocusEvent _focusEvent) {
       short nFocusFlags = _focusEvent.FocusFlags;
       int nFocusChangeReason = nFocusFlags & FocusChangeReason.TAB;
       if (nFocusChangeReason == FocusChangeReason.TAB) {
           // get the window of the Window that has gained the Focus...
           // Note that the xWindow is just a representation of the controlwindow
           // but not of the control itself
           XWindow xWindow = UnoRuntime.queryInterface(XWindow.class, _focusEvent.NextFocus);
       }
   }


   public void focusGained(FocusEvent focusEvent) {
   }

   public void mouseReleased(MouseEvent mouseEvent) {
   }

   public void mousePressed(MouseEvent mouseEvent) {
   }

   public void mouseExited(MouseEvent mouseEvent) {
   }

   public void mouseEntered(MouseEvent _mouseEvent) {
       try {
           // retrieve the control that the event has been invoked at...
           XControl xControl = UnoRuntime.queryInterface(XControl.class, _mouseEvent.Source);
           Object tk = m_xMCF.createInstanceWithContext("com.sun.star.awt.Toolkit", m_xContext);
           XToolkit xToolkit = UnoRuntime.queryInterface(XToolkit.class, tk);
           // create the peer of the control by passing the windowpeer of the parent
           // in this case the windowpeer of the control
           xControl.createPeer(xToolkit, m_xWindowPeer);
           // create a pointer object "in the open countryside" and set the type accordingly...
           Object oPointer = this.m_xMCF.createInstanceWithContext("com.sun.star.awt.Pointer", this.m_xContext);
           XPointer xPointer = UnoRuntime.queryInterface(XPointer.class, oPointer);
           xPointer.setType(com.sun.star.awt.SystemPointer.REFHAND);
           // finally set the created pointer at the windowpeer of the control
           xControl.getPeer().setPointer(xPointer);
       } catch (com.sun.star.uno.Exception ex) {
           throw new java.lang.RuntimeException("cannot happen...", ex);
       }
   }



   public void keyReleased(KeyEvent keyEvent) {
   }

   public void keyPressed(KeyEvent keyEvent) {
   }

}
