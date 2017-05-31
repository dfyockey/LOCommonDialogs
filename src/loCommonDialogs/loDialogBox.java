package loCommonDialogs;

import com.sun.star.awt.Point;
import com.sun.star.awt.Rectangle;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XReschedule;
import com.sun.star.awt.XToolkit;
import com.sun.star.awt.XUnitConversion;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.beans.XMultiPropertySet;
import com.sun.star.container.XNameContainer;
import com.sun.star.frame.XModel;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.MeasureUnit;

public class loDialogBox implements AutoCloseable {
	
	private XComponentContext		xContext	  = null;
	private XMultiComponentFactory	xMCF		  = null;
	private XWindowPeer				m_xWindowPeer = null;
	private XDialog					xDialog		  = null;
	private XComponent				m_xComponent  = null;
	
	// Variables set by createDialog method
	private XMultiServiceFactory m_xMSFDialogModel;
	private XNameContainer		 m_xDlgModelNameContainer;
	private XControlContainer	 m_xDlgContainer;
	private XControl			 m_xDialogControl;
	
	// Dialog initialization values
	private int dialogwidth	 = 200;
	private int dialogheight = 75;
	private int dialogxpos 	 = 0;
	private int dialogypos 	 = 0;
	
	
	public loDialogBox(String dialogname) {
		xContext = getContext();
		initDialogBox(dialogname);
	}
	
	public loDialogBox(String dialogname, XComponentContext xComponentContext) {
		xContext = xComponentContext;
		initDialogBox(dialogname);
	}
	
	private void initDialogBox(String dialogname) {
		xMCF = xContext.getServiceManager();
		createDialog(xMCF, xContext);
		
		initialize (
			new String[] { "Height", "Moveable", "Name", "PositionX", "PositionY", "Step", "TabIndex", "Title", "Width" },
			new Object[] { dialogheight, true, dialogname, dialogxpos, dialogypos, 0, (short)0, "loDialogBox", dialogwidth }
		);
       
		m_xWindowPeer = getWindowPeer();
		xDialog = UnoRuntime.queryInterface(XDialog.class, m_xDialogControl);		
	}
	
	public short show(XModel xDoc, String title) {
		xDialog.setTitle(title);
		
		getWindowPeer();
	    //xDialog	= UnoRuntime.queryInterface(XDialog.class, m_xDialogControl);
	    
	    // The following line sets m_xComponent for use in the close() method
	    m_xComponent = UnoRuntime.queryInterface(XComponent.class, m_xDialogControl);

		centerBox(xDoc);
	    
	    return xDialog.execute();
	}
	
	public void close() throws Exception {
		// Dispose the component and free the memory...
        if (m_xComponent != null){
            m_xComponent.dispose();
            m_xComponent = null;
        }
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
	
	private void initialize(String[] PropertyNames, Object[] PropertyValues) {
		try {
			XMultiPropertySet xMultiPropertySet = UnoRuntime.queryInterface(XMultiPropertySet.class, m_xDlgModelNameContainer);
			xMultiPropertySet.setPropertyValues(PropertyNames, PropertyValues);
		} catch (com.sun.star.uno.Exception ex) {
			ex.printStackTrace(System.err);
		}
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
	
	private void centerBox(XModel xDoc) {
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
}
