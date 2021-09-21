package loCommonDialogs;

import java.util.logging.Level;

import javax.xml.bind.DatatypeConverter;

import com.sun.star.awt.FontDescriptor;
import com.sun.star.awt.FontWeight;
import com.sun.star.awt.ImageScaleMode;
import com.sun.star.awt.Point;
import com.sun.star.awt.Rectangle;
import com.sun.star.awt.XButton;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XFixedText;
import com.sun.star.awt.XLayoutConstrains;
import com.sun.star.awt.XReschedule;
import com.sun.star.awt.XStyleSettings;
import com.sun.star.awt.XStyleSettingsSupplier;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XToolkit;
import com.sun.star.awt.XUnitConversion;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XMultiPropertySet;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XNameAccess;
import com.sun.star.container.XNameContainer;
import com.sun.star.frame.XModel;
import com.sun.star.graphic.XGraphic;
import com.sun.star.graphic.XGraphicProvider;
import com.sun.star.io.XInputStream;
import com.sun.star.io.XOutputStream;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.style.VerticalAlignment;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.MeasureUnit;

import dlgutils.DlgLogger;

// Note : Consequences of Level.SEVERE problems are expected to later cause an exception elsewhere
//        (e.g. a NullPointerException) which should be caught and handled by the dialog caller.

public abstract class loDialogBox implements AutoCloseable {

	static short textalign_left = 0;
	static short textalign_center = 1;
	static short textalign_right = 2;
	
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

	// Dialog and Control Size & Position Variables provided with arbitrary default values
	// and usable by derived classes to facilitate providing consistent dialog appearance.
	// All values are in dialog units (i.e. APPFONT units)
	protected int iconsize			= 26;					// Icon height/width
	protected int padding			= 4;					// Padding used to space dialog objects from the inner edge of the dialog
	protected int gap				= 4;					// Gap used between dialog objects to space them apart
	protected int fieldwidth		= 120;					// Edit Field width; by convention, this should always be >= btngap+(2*btnwidth)
	protected int fieldheight		= 12;					// Edit Field height
	protected int fieldborderwidth	= 3;					// Width of the border around the Edit Field
	protected int labelwidth		= fieldwidth;			// Label width
	protected int labelheight		= 24;					// Label height
	protected int labelborderwidth	= 1;					// Width of the border around the Label
	protected int labelposX 		= padding+iconsize+gap-labelborderwidth;	// Label horizontal position (must recalc in derived class if any of the addend value is changed)
	protected int labelposY			= padding;				// Label vertical position
	protected int btnwidth			= 32;					// Width of a Button
	protected int btnheight			= 14;					// Height of a Button
	protected int btnvertpos		= padding+iconsize;		// Vertical position of button(s)
	protected int btnborderwidth 	= 1;					// Assumed width of the border around the Label
	
	// Dialog initialization values (in dialog units)
	
	protected final int mindialogwidth  = 107;	// These minimum values appear to be either a bug or an undocumented feature.
	protected final int mindialogheight = 107;	// They are easily demonstrable by previewing a dialog with lesser values
												// in the LibreOffice Dialog Editor (at least in LibreOffice 6.x).
												//
												// This bug is fixed in at least as early as LibreOffice 7.1.
												// Earlier versions have not bee checked. 
	
	// Note: A dialog's width and height cannot be changed after the dialog has been executed
	protected int dialogwidth  = mindialogwidth;
	protected int dialogheight = mindialogheight;
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
	
	// To avoid memory leaks, close must be called (i.e. as `super.close()`) at the end of any overriding close method in a derived class
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
	
	protected XFixedText insertFixedText(short _Align, int _nPosX, int _nPosY, int _nWidth, int _nHeight, int _nStep, String _sLabel) throws com.sun.star.uno.Exception {
		XMultiPropertySet xMPSet = _insertPreProc("Label", "com.sun.star.awt.UnoControlFixedTextModel");
		xMPSet.setPropertyValues(
			new String[] {"Align", "Border", "Height", "Label", "NoLabel", "MultiLine", "PositionX", "PositionY", "Step", "VerticalAlign", "Width"},		// Remember: Alphabetical Order!
			new Object[] {_Align, (short)0, _nHeight, _sLabel, true, true, _nPosX, _nPosY, _nStep, VerticalAlignment.MIDDLE, _nWidth});
		
		XFixedText xFixedText = (XFixedText) _insertPostProc(XFixedText.class, xMPSet);
		return xFixedText;
	}
	
	protected XTextComponent insertEditField(short _Align, int _nPosX, int _nPosY, int _nWidth, int _nHeight) throws com.sun.star.uno.Exception {
		XMultiPropertySet xMPSet = _insertPreProc("TextField", "com.sun.star.awt.UnoControlEditModel");
		xMPSet.setPropertyValues(
			new String[] {"Align", "Border", "Height", "PositionX", "PositionY", "Text", "Width"},		// Remember: Alphabetical Order!
			new Object[] {_Align, (short)1, _nHeight, _nPosX, _nPosY, "MyText", _nWidth});
		return (XTextComponent) _insertPostProc(XTextComponent.class, xMPSet);
	}
   
	protected XButton insertButton(int _nPosX, int _nPosY, int _nWidth, int _nHeight, String _sLabel, short _nPushButtonType, boolean _bDefaultButton) throws com.sun.star.uno.Exception {
		XMultiPropertySet xMPSet = _insertPreProc("Button", "com.sun.star.awt.UnoControlButtonModel");
		xMPSet.setPropertyValues(
			new String[] {"DefaultButton", "Height", "Label", "PositionX", "PositionY", "PushButtonType", "Width" },	// Remember: Alphabetical Order!
			new Object[] {_bDefaultButton, _nHeight, _sLabel, _nPosX, _nPosY, _nPushButtonType, _nWidth});
		return (XButton) _insertPostProc(XButton.class, xMPSet);
	}

	// URL-based Image
	protected XControl insertImage(int _nPosX, int _nPosY, int _nWidth, int _nHeight, String _ImageURL) throws com.sun.star.uno.Exception {
		XMultiPropertySet xMPSet = _insertPreProc("Image", "com.sun.star.awt.UnoControlImageControlModel");
		short _mode = ImageScaleMode.ISOTROPIC;
		xMPSet.setPropertyValues(
			new String[] {"Border", "Height", "ImageURL", "PositionX", "PositionY", "ScaleMode", "Width"},	// Remember: Alphabetical Order!
			new Object[] {(short)0, _nHeight, _ImageURL, _nPosX, _nPosY, _mode, _nWidth});
		return (XControl) _insertPostProc(XControl.class, xMPSet);
	}
	
	// Hexbinary-based Image
	protected XControl insertImage(int _nPosX, int _nPosY, int _nWidth, int _nHeight, byte[] _ImageHexbinary, String imgtype) throws com.sun.star.uno.Exception {
		XMultiPropertySet xMPSet = _insertPreProc("Image", "com.sun.star.awt.UnoControlImageControlModel");
		short _mode = ImageScaleMode.NONE;
		
		XGraphic xGraphic = getGraphic(_ImageHexbinary, imgtype);
		
		xMPSet.setPropertyValues(
			new String[] {"Border", "Graphic", "Height", "PositionX", "PositionY", "ScaleMode", "Width"},	// Remember: Alphabetical Order!
			new Object[] {(short)0, xGraphic, _nHeight, _nPosX, _nPosY, _mode, _nWidth});
		return (XControl) _insertPostProc(XControl.class, xMPSet);
	}
	
	protected XControl insertFixedLine(int _nPosX, int _nPosY, int _nWidth, int _nHeight) throws com.sun.star.uno.Exception {
		XMultiPropertySet xMPSet = _insertPreProc("Line", "com.sun.star.awt.UnoControlFixedLineModel");
		xMPSet.setPropertyValues(
				new String[] {"Enabled", "Height", "PositionX", "PositionY", "Width"},	// Remember: Alphabetical Order!
				new Object[] {true, _nHeight, _nPosX, _nPosY, _nWidth});
		return (XControl) _insertPostProc(XControl.class, xMPSet);
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
	        	DlgLogger.log(null, loDialogBox.class.getName(), Level.SEVERE, e);
	            e.printStackTrace(System.err);
	        }
        }
        return xContext;
	}
	
	// With very small changes to this class, createDialog could just use protected variables xMFC and xContext
	// rather than requiring these values to be passed as arguments, or they could be passed and assigned here.
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
			
			//initFixedText(_xMCF, _xContext);
		} catch (Exception e) {
			DlgLogger.log(null, loDialogBox.class.getName(), Level.SEVERE, e);
			e.printStackTrace(System.err);
		}
	}
	
	protected XGraphic getGraphic(byte[] _rawimage, String imgtype) throws com.sun.star.uno.Exception {
		
		Object oPipe = xMCF.createInstanceWithContext("com.sun.star.io.Pipe", xContext);
		
		// Nonintuitivly, we use an *XOutputStream* to write the image info *into* the Pipe...
		XOutputStream xOStream = UnoRuntime.queryInterface(XOutputStream.class, oPipe);
		xOStream.writeBytes(_rawimage);
		xOStream.closeOutput();
		
		XInputStream xIStream = UnoRuntime.queryInterface(XInputStream.class, oPipe);
		
        PropertyValue[] mediaProps = new PropertyValue[2];
        mediaProps[0] = new PropertyValue();
        mediaProps[0].Name = "InputStream";
        mediaProps[0].Value = xIStream;
        mediaProps[1] = new PropertyValue();
        mediaProps[1].Name = "MimeType";
        mediaProps[1].Value = "image/" + imgtype;
	
		// Instantiate a GraphicProvider
		Object oGraphicProvider = xMCF.createInstanceWithContext("com.sun.star.graphic.GraphicProvider", xContext);
		XGraphicProvider xGraphicProvider = UnoRuntime.queryInterface(XGraphicProvider.class, oGraphicProvider);
		
		XGraphic xGraphic = xGraphicProvider.queryGraphic(mediaProps);
		
		return xGraphic;
	}
	
	  // creates a UNO graphic object that can be used to be assigned 
	  // to the property "Graphic" of a controlmodel
	  // (from https://wiki.openoffice.org/wiki/Documentation/DevGuide/GUI/Command_Button) 
	  public XGraphic getGraphic(String ImageUrl){
	  XGraphic xGraphic = null;
	  try{
	      // create a GraphicProvider at the global service manager...
	      Object oGraphicProvider = xMCF.createInstanceWithContext("com.sun.star.graphic.GraphicProvider", xContext);
	      XGraphicProvider xGraphicProvider = (XGraphicProvider) UnoRuntime.queryInterface(XGraphicProvider.class, oGraphicProvider);
	      // create the graphic object
	      PropertyValue[] aPropertyValues = new PropertyValue[1];
	      PropertyValue aPropertyValue = new PropertyValue();
	      aPropertyValue.Name = "URL";
	      aPropertyValue.Value = ImageUrl;
	      aPropertyValues[0] = aPropertyValue;
	      xGraphic = xGraphicProvider.queryGraphic(aPropertyValues);
	      return xGraphic;
	  }catch (com.sun.star.uno.Exception ex){
	      throw new java.lang.RuntimeException("cannot happen...");
	  }}
	
	protected void initialize(String[] PropertyNames, Object[] PropertyValues) {
		try {
			XMultiPropertySet xMultiPropertySet = UnoRuntime.queryInterface(XMultiPropertySet.class, m_xDlgModelNameContainer);
			xMultiPropertySet.setPropertyValues(PropertyNames, PropertyValues);
		} catch (com.sun.star.uno.Exception ex) {
			DlgLogger.log(null, loDialogBox.class.getName(), Level.WARNING, ex);
			ex.printStackTrace(System.err);
			// nop - Dialog miscellany such as size, position, name, etc will just be wrong.
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
				
				// Create "child" window with the desktop window of xToolkit as parent...
				m_xDialogControl.createPeer(xToolkit, null);
				
				m_xWindowPeer = m_xDialogControl.getPeer();
			} catch( Exception e) {
				DlgLogger.log(null, loDialogBox.class.getName(), Level.SEVERE, e);
				e.printStackTrace(System.err);
				return null;
			}
		}
		return m_xWindowPeer;
	}
	
	protected void centerBox(XModel xDoc) {
		XWindow loWindow = xDoc.getCurrentController().getFrame().getContainerWindow();

		// XUnitConversion is provided by the window component referred to by XWindow (i.e. the 'ContainerWindow')
		// as evidenced by examination of information in the well-known MRI Object Inspection Tool.
		XUnitConversion m_xConversion = UnoRuntime.queryInterface(XUnitConversion.class, loWindow);

		Rectangle loWindowRect = loWindow.getPosSize();
		
		Point ptWinSizePixels = new Point(loWindowRect.Width,loWindowRect.Height);
		
		Point ptWinSizeDialog = m_xConversion.convertPointToLogic(ptWinSizePixels, MeasureUnit.APPFONT);
		
		dialogxpos = (int)( (ptWinSizeDialog.X / 2.0) - (dialogwidth  / 2.0) );
		dialogypos = (int)( (ptWinSizeDialog.Y / 2.0) - (dialogheight / 2.0) );

		XControlModel oDialogModel = m_xDialogControl.getModel();
		XMultiPropertySet xMPSet = UnoRuntime.queryInterface(XMultiPropertySet.class, oDialogModel);
		try {
			xMPSet.setPropertyValues( new String[]{"PositionX", "PositionY"},new Object[]{dialogxpos, dialogypos});
		} catch (Exception e) {
			DlgLogger.log(null, loDialogBox.class.getName(), Level.WARNING, e);
			// nop - Dialog will be positioned at position 0,0 or wherever it was previously.
		}
	}
	
	private String createUniqueName(XNameAccess _xElementContainer, String _sElementName) {
		int i=1;
		while ( _xElementContainer.hasByName(_sElementName + Integer.toString(i)) )
			++i;
		return _sElementName + Integer.toString(i);
	}
	
	protected void configIconFromHexBinary (XControl guiIcon, String rawhexPng) {
		XPropertySet xIconProps = null;
		XGraphic 	 xGraphic	= null;
		
		if ( rawhexPng != null ) {
			byte[] hexbinaryIcon = DatatypeConverter.parseHexBinary(rawhexPng);
			
			// If getGraphic throws, just continue; the default icon will be used.
			try {
				xGraphic = getGraphic(hexbinaryIcon, "png");
				
				//// Get Label XPropertySet interface
				xIconProps = getControlProps(guiIcon);
				
				if (xIconProps != null)
					xIconProps.setPropertyValue("Graphic", xGraphic);				
			} catch (Exception e) {
				DlgLogger.log(null, loDialogBox.class.getName(), Level.WARNING, e);
				// nop - There'll just be no custom icon.
			}
		}
	}
	
	protected void configIcon (XControl guiIcon, String ImageUrl) {
		XPropertySet xIconProps = null;
		XGraphic 	 xGraphic	= null;
		
		if ( ImageUrl != null ) {
			
			// If getGraphic throws, just continue; the default icon will be used.
			try {
				xGraphic = getGraphic(ImageUrl);
				
				//// Get Label XPropertySet interface
				xIconProps = getControlProps(guiIcon);
				
				if (xIconProps != null)
					xIconProps.setPropertyValue("Graphic", xGraphic);				
			} catch (Exception e) {
				DlgLogger.log(null, loDialogBox.class.getName(), Level.WARNING, e);
				// nop - There'll just be no custom icon.
			}
		}
	}
	
	protected XPropertySet formatLabelText (XModel xDoc, XFixedText guiLabel, String labeltext) {
		//// Get and adjust FontDescriptor for Label Font
		FontDescriptor labelFontDescriptor = getLabelFontDescriptor(xDoc);
		labelFontDescriptor.Weight = FontWeight.BOLD;
		
		//// Set Label Properties
		XPropertySet xLabelProps = getControlProps(guiLabel);
		try {
			xLabelProps.setPropertyValue("Label", labeltext);
			xLabelProps.setPropertyValue("FontDescriptor", labelFontDescriptor);
		} catch (Exception e) {
			DlgLogger.log(null, loDialogBox.class.getName(), Level.WARNING, e);
			e.printStackTrace(System.err);
			// One or more fonts will just be wrong.
		}
		
		return xLabelProps;
	}
	
	protected XPropertySet sizeLabel (XModel xDoc, XFixedText guiLabel, String labeltext) {
		// Set Label Width and Height to accommodate its text
		
		XPropertySet xLabelProps = formatLabelText(xDoc, guiLabel, labeltext);
		
		//// Get Label size in pixels necessary to contain its text
		XLayoutConstrains xLayoutConstrains = UnoRuntime.queryInterface(XLayoutConstrains.class, guiLabel);
		com.sun.star.awt.Size s = xLayoutConstrains.getPreferredSize();
		
		//// Convert the Label size in pixels to the size in dialog units (i.e. APPFONT units)
		XWindow guiLabelWindow = UnoRuntime.queryInterface(XWindow.class, guiLabel);
		XUnitConversion m_xConversion = UnoRuntime.queryInterface(XUnitConversion.class, guiLabelWindow);
		Point ptLabelPixels   = new Point(s.Width, s.Height);
		Point ptLabelDlgUnits = m_xConversion.convertPointToLogic(ptLabelPixels, MeasureUnit.APPFONT);
		labelwidth  = ptLabelDlgUnits.X;
		labelheight = ptLabelDlgUnits.Y;
		
		try {
			xLabelProps.setPropertyValue("Width", labelwidth);
			xLabelProps.setPropertyValue("Height", labelheight);
		} catch (Exception e) {
			DlgLogger.log(null, loDialogBox.class.getName(), Level.WARNING, e);
			e.printStackTrace(System.err);
			// Label dimensions will just be wrong.
		}
		
		return xLabelProps;
	}
	
	protected void calcLabelAndBtnVertPos () {
		// set the Label and Button vertical positions
		
		//// Vertically position the Label and Button(s) relative to the Icon
		if ( labelheight < iconsize ) {
			// Vertically center the Label relative to the Icon
			labelposY = padding + (iconsize / 2 - labelheight / 2);
			btnvertpos   = padding + iconsize + gap/2;
		} else {
			// Vertically position the Label at the same position as the Icon
			labelposY = padding;
			btnvertpos   = labelposY + labelheight;
		}
	}
	
	protected void setLabelVertPos (XPropertySet xLabelProps) {
		try {		
			xLabelProps.setPropertyValue("PositionY", labelposY);
		} catch (Exception e) {
			DlgLogger.log(null, loDialogBox.class.getName(), Level.WARNING, e);
			e.printStackTrace(System.err);
			// Label dimensions will just be wrong.
		}
	}
	
	protected void setDialogSize () {
		XControlModel xDialogModel = m_xDialogControl.getModel();
		XPropertySet xDialogProps = UnoRuntime.queryInterface(XPropertySet.class, xDialogModel);
		try {
			xDialogProps.setPropertyValue("Width", dialogwidth);
			xDialogProps.setPropertyValue("Height", dialogheight);
		} catch (Exception e) {
			// nop -- default dimensions will be used
		}		
	}
	
	protected XPropertySet getControlProps (Object guiObject) {
		XControl xControl = UnoRuntime.queryInterface(XControl.class, guiObject);
		XControlModel xIconControlModel = xControl.getModel();
		return UnoRuntime.queryInterface(XPropertySet.class, xIconControlModel);
	}
	
	// I can't remember, but I may have discovered that XStyleSettings provides the method `getApplicationFont()` from use of MRI (the UNO Object Inspection Tool),
	// as suggested by jimk at https://ask.libreoffice.org/t/documentation-for-xstylesettings-supporting-getters/68158/2
	protected FontDescriptor getAppFontDescriptor (XModel xDoc) {
		XStyleSettingsSupplier xStyleSettingsSupplier = UnoRuntime.queryInterface(XStyleSettingsSupplier.class, xDoc.getCurrentController().getFrame().getContainerWindow());
		XStyleSettings xStyleSettings = xStyleSettingsSupplier.getStyleSettings();
		return xStyleSettings.getApplicationFont();
	}
	
	protected FontDescriptor getLabelFontDescriptor (XModel xDoc) {
		XStyleSettingsSupplier xStyleSettingsSupplier = UnoRuntime.queryInterface(XStyleSettingsSupplier.class, xDoc.getCurrentController().getFrame().getContainerWindow());
		XStyleSettings xStyleSettings = xStyleSettingsSupplier.getStyleSettings();
		return xStyleSettings.getLabelFont();
	}
}
