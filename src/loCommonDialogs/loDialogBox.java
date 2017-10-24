package loCommonDialogs;

import com.sun.star.awt.Point;
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

public abstract class loDialogBox implements AutoCloseable {
	
	protected XComponentContext		 xContext		= null;
	protected XMultiComponentFactory xMCF			= null;
	protected XWindowPeer			 m_xWindowPeer 	= null;
	protected XDialog				 xDialog		= null;
	protected XComponent			 m_xComponent	= null;
	
	// Variables set by createDialog method
	protected XMultiServiceFactory 	m_xMSFDialogModel;
	protected XNameContainer		m_xDlgModelNameContainer;
	protected XControlContainer		m_xDlgContainer;
	protected XControl				m_xDialogControl;

	// Dialog and Control Size & Position Default Values
	// usable by derived classes to facilitate providing
	// consistent dialog appearance
	protected int margin		= 8;
	protected int fieldwidth	= 120;	// Should be >= btngap+(2*btnwidth)
	protected int fieldheight	= 12;
	protected int fieldborderwidth = 3;	// Width of the border around an edit field
	protected int labelwidth	= fieldwidth;
	protected int labelheight	= 8;
	protected int labelborderwidth = 1;	// Width of the border around a label
	protected int btnwidth		= 32;
	protected int btnheight		= 14;
	protected int gap			= 3;
	
	// Dialog initialization values
	protected int dialogwidth  = 200;
	protected int dialogheight = 75;
	protected int dialogxpos   = 0;
	protected int dialogypos   = 0;
	
	
	public loDialogBox() {
		xContext = getContext();
	}
	
	public loDialogBox(XComponentContext xComponentContext) {
		xContext = xComponentContext;
	}
	
	// ABSTRACT METHOD
	protected abstract void initBox();
	
	public short show(XModel xDoc, String title) {
		xDialog.setTitle(title);
		
		getWindowPeer();
	    
	    // The following line sets m_xComponent for use in the close() method
	    m_xComponent = UnoRuntime.queryInterface(XComponent.class, m_xDialogControl);

		centerBox(xDoc);
	    
	    return xDialog.execute();
	}
	
	public void close() {
		// Dispose the component and free the memory...
        if (m_xComponent != null){
            m_xComponent.dispose();
            m_xComponent = null;
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
	
	protected XFixedText insertFixedText(int _nPosX, int _nPosY, int _nWidth, int _nHeight, int _nStep, String _sLabel) throws com.sun.star.uno.Exception {
		XMultiPropertySet xMPSet = _insertPreProc("Label", "com.sun.star.awt.UnoControlFixedTextModel");
		xMPSet.setPropertyValues(
			new String[] {"Border", "Height", "Label", "PositionX", "PositionY", "Step", "Width"},		// Remember: Alphabetical Order!
			new Object[] {(short)0, _nHeight, _sLabel, _nPosX, _nPosY, _nStep, _nWidth});
		return (XFixedText) _insertPostProc(XFixedText.class, xMPSet);
	}
	
	protected XTextComponent insertEditField(int _nPosX, int _nPosY, int _nWidth, int _nHeight) throws com.sun.star.uno.Exception {
		XMultiPropertySet xMPSet = _insertPreProc("TextField", "com.sun.star.awt.UnoControlEditModel");
		xMPSet.setPropertyValues(
			new String[] {"Border", "Height", "PositionX", "PositionY", "Text", "Width"},		// Remember: Alphabetical Order!
			new Object[] {(short)1, _nHeight, _nPosX, _nPosY, "MyText", _nWidth});
		return (XTextComponent) _insertPostProc(XTextComponent.class, xMPSet);
	}
   
	protected XButton insertButton(int _nPosX, int _nPosY, int _nWidth, int _nHeight, String _sLabel, short _nPushButtonType, boolean _bDefaultButton) throws com.sun.star.uno.Exception {
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
			
			// link the dialog and its model...
			XControlModel xControlModel = UnoRuntime.queryInterface(XControlModel.class, oDialogModel);
			m_xDialogControl.setModel(xControlModel);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
	
	protected void initialize(String[] PropertyNames, Object[] PropertyValues) {
		try {
			XMultiPropertySet xMultiPropertySet = UnoRuntime.queryInterface(XMultiPropertySet.class, m_xDlgModelNameContainer);
			xMultiPropertySet.setPropertyValues(PropertyNames, PropertyValues);
		} catch (com.sun.star.uno.Exception ex) {
			ex.printStackTrace(System.err);
		}
	}
	
	protected XWindowPeer getWindowPeer() {
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
	
	protected void centerBox(XModel xDoc) {
		XWindow loWindow = xDoc.getCurrentController().getFrame().getContainerWindow();
		Rectangle loWindowRect = loWindow.getPosSize();
		
		Point ptWinSizePixels = new Point(loWindowRect.Width,loWindowRect.Height);
		
		// The following two lines of code, using XWindow loWindow, were inspired by code at
		// https://github.com/qt-haiku/LibreOffice/blob/master/toolkit/qa/complex/toolkit/UnitConversion.java
		// where an XWindowPeer is used as the Object in the queryInterface call.
		//
		// These lines work great, but I've been unable to find where XWindowPeer or XWindow implementation
		// or inheritance of the XUnitConversion interface is documented...
		XUnitConversion m_xConversion = UnoRuntime.queryInterface(XUnitConversion.class, loWindow);
		Point ptWinSizeDialog = m_xConversion.convertPointToLogic(ptWinSizePixels, MeasureUnit.APPFONT);
		
		dialogxpos = (int)( (ptWinSizeDialog.X / 2.0) - (dialogwidth  / 2.0) );
		dialogypos = (int)( (ptWinSizeDialog.Y / 2.0) - (dialogheight / 2.0) );

		XControlModel oDialogModel = m_xDialogControl.getModel();
		XMultiPropertySet xMPSet = UnoRuntime.queryInterface(XMultiPropertySet.class, oDialogModel);
		try {
			xMPSet.setPropertyValues( new String[]{"PositionX", "PositionY"},new Object[]{dialogxpos, dialogypos});
		} catch (Exception e) {
			// Do nothing. Dialog will be positioned at position 0,0 or wherever it was previously.
		}
	}
	
	private String createUniqueName(XNameAccess _xElementContainer, String _sElementName) {
		int i=1;
		while ( _xElementContainer.hasByName(_sElementName + Integer.toString(i)) )
			++i;
		return _sElementName + Integer.toString(i);
	}
}
