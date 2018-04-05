package loCommonDialogs;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.sun.star.awt.FontDescriptor;
import com.sun.star.awt.FontPitch;
import com.sun.star.awt.FontSlant;
import com.sun.star.awt.FontWeight;
import com.sun.star.awt.ImageScaleMode;
import com.sun.star.awt.Point;
import com.sun.star.awt.Rectangle;
import com.sun.star.awt.Size;
import com.sun.star.awt.XButton;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XDevice;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XFixedText;
import com.sun.star.awt.XFont;
import com.sun.star.awt.XGraphics;
import com.sun.star.awt.XLayoutConstrains;
import com.sun.star.awt.XReschedule;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XToolkit;
import com.sun.star.awt.XUnitConversion;
import com.sun.star.awt.XView;
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
import com.sun.star.rendering.XTextLayout;
import com.sun.star.style.VerticalAlignment;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.MeasureUnit;

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

	// Dialog and Control Size & Position Default Values
	// usable by derived classes to facilitate providing
	// consistent dialog appearance
	protected int iconsize		= 16;
	protected int margin		= 4;
	protected int fieldwidth	= 120;	// Should be >= btngap+(2*btnwidth)
	protected int fieldheight	= 12;
	protected int fieldborderwidth = 3;	// Width of the border around an edit field
	protected int labelwidth	= fieldwidth;
	protected int labelheight	= 12;
	protected int labelborderwidth = 1;	// Width of the border around a label
	protected int btnwidth		= 32;
	protected int btnheight		= 14;
	protected int gap			= 3;
	
	// Dialog initialization values (in dialog units)
	
	protected final int mindialogwidth  = 107;	// These minimum values appear to be either a bug or an undocumented feature.
	protected final int mindialogheight = 107;	// They are easily demonstrable by previewing a dialog with lesser values
												// in the LibreOffice Dialog Editor.
	
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
		System.out.println("Hello again from loDialogBox!");
		
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
        
        // FontDescriptor needs to go in HERE!!!
        
        //XPropertySet xFixedTextProps = UnoRuntime.queryInterface(XPropertySet.class, oFTModel);
        //FontDescriptor fontDescriptor = (FontDescriptor) xFixedTextProps.getPropertyValue("FontDescriptor");
        //fontDescriptor.Slant = FontSlant.ITALIC;
        
//        FontDescriptor fontDescriptor = initFont(oFTModel);
        
//        xFTModelMPSet.setPropertyValues(new String[]{"FontDescriptor", "Name"},new Object[]{fontDescriptor, sName});
        xFTModelMPSet.setPropertyValues(new String[]{"Name"},new Object[]{sName});
        
        // add the model to the NameContainer of the dialog model
        m_xDlgModelNameContainer.insertByName(sName, oFTModel);
        
        return xFTModelMPSet;
	}
	
	protected XFixedText insertFixedText(short _Align, int _nPosX, int _nPosY, int _nWidth, int _nHeight, int _nStep, String _sLabel) throws com.sun.star.uno.Exception {
		XMultiPropertySet xMPSet = _insertPreProc("Label", "com.sun.star.awt.UnoControlFixedTextModel");

/*		

		FontDescriptor fontDescriptor = (FontDescriptor) xMPSet.getPropertyValues(new String[] {"FontDescriptor"})[0];
			fontDescriptor.Height = 26;
			//fontDescriptor.Name = "Comfortaa Light";
			//fontDescriptor.Pitch = FontPitch.FIXED;
			//fontDescriptor.CharacterWidth = 150;
			//fontDescriptor.Name = "Dialog";
			//fontDescriptor.Name = "Liberation Serif";
			fontDescriptor.Name = "Deja Vu Serif Condensed";
			fontDescriptor.Weight = com.sun.star.awt.FontWeight.BOLD;
*/

/*
		java.awt.Font awtFont = new java.awt.Font("Dialog", java.awt.Font.BOLD, 12);
		
		
			JPanel awtFlowLayoutComponents = new JPanel();
			java.awt.FlowLayout awtFlowLayout = new java.awt.FlowLayout();
			awtFlowLayoutComponents.setLayout(awtFlowLayout);
			java.awt.Label awtLabel = new java.awt.Label(_sLabel);
			awtFlowLayoutComponents.add(awtLabel);
			java.awt.Graphics awtGraphics = awtFlowLayoutComponents.getGraphics();
		if (awtGraphics != null) {
			java.awt.FontMetrics awtFontMetrics = awtGraphics.getFontMetrics(awtFont);
			int txtHeight  = awtFontMetrics.getHeight();
			int txtAdvance = awtFontMetrics.stringWidth(_sLabel);
			_nHeight = txtHeight  + 2;
			_nWidth  = txtAdvance + 2;
		} else {
			System.out.println("Oh no! It's NULL!");
		}
*/

		//javafx.scene.text.Font defaultFont = javafx.scene.text.Font.getDefault();
		
		//java.awt.Font awtFont = new JLabel().getFont();
		//System.out.println("awtFont size = " + awtFont.getSize());
		
		//int h = _nHeight * 2;
			
		//int bkColor = 16514043;	// #FBFBFB
		
		xMPSet.setPropertyValues(
			//new String[] {"Align", "Border", "FontDescriptor", "Height", "Label", "MultiLine", "PositionX", "PositionY", "Step", "Width"},		// Remember: Alphabetical Order!
			//new Object[] {_Align, (short)1, fontDescriptor, h, _sLabel, true, _nPosX, _nPosY, _nStep, _nWidth});
		
				
				
			//new String[] {"Align", "BackgroundColor", "Border", "Height", "Label", "MultiLine", "PositionX", "PositionY", "Step", "VerticalAlign", "Width"},		// Remember: Alphabetical Order!
			//new Object[] {_Align, bkColor, (short)0, _nHeight, _sLabel, true, _nPosX, _nPosY, _nStep, VerticalAlignment.MIDDLE, _nWidth});
		
			new String[] {"Align", "Border", "Height", "Label", "MultiLine", "PositionX", "PositionY", "Step", "VerticalAlign", "Width"},		// Remember: Alphabetical Order!
			new Object[] {_Align, (short)0, _nHeight, _sLabel, true, _nPosX, _nPosY, _nStep, VerticalAlignment.MIDDLE, _nWidth});

		
		XFixedText xFixedText = (XFixedText) _insertPostProc(XFixedText.class, xMPSet);
		
		/*
		XView xView = UnoRuntime.queryInterface(XView.class, xFixedText);
		XGraphics xGraphics = xView.getGraphics();
		if (xGraphics == null)
			System.out.println("Null! Bummer!");
		*/
		
		/*
		XTextLayout xTextLayout = UnoRuntime.queryInterface(XTextLayout.class, xDialog);
		if (xTextLayout == null)
			System.out.println("xTextLayout in null");
		else
			System.out.println("OMG! xTextLayout actually has a value!");
		*/
		
		//FontDescriptor labelFontDescriptor = (FontDescriptor) (xMPSet.getPropertyValues(new String[] {"FontDescriptor"})[0]);
		//System.out.println("Font Width = " + labelFontDescriptor.CharacterWidth);
		
//		XLayoutConstrains xLayoutConstrains = UnoRuntime.queryInterface(XLayoutConstrains.class, xFixedText);
//		Size sizeLabel = xLayoutConstrains.getPreferredSize();
		
//		System.out.println("sizeLabel.Width = " + sizeLabel.Width);
		
		//int w = (int) (sizeLabel.Width * (12.0/10.0));
//		int w = sizeLabel.Width;
		/*
		XUnitConversion m_xConversion = UnoRuntime.queryInterface(XUnitConversion.class, xContext);
		Size sizePixels = m_xConversion.convertSizeToPixel(sizeLabel, MeasureUnit.APPFONT);
		
		System.out.println("sizePixels.Width = " + sizePixels.Width);
		
		Size sizeAppFont = m_xConversion.convertSizeToLogic(sizePixels, MeasureUnit.APPFONT);
		
		System.out.println("sizeAppFont.Width = " + sizeAppFont.Width);
				*/
//		xMPSet.setPropertyValues(new String[] {"Width"}, new Object[] {w});

		
		//return (XFixedText) _insertPostProc(XFixedText.class, xMPSet);
		return xFixedText;
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

	protected XControl insertImage(int _nPosX, int _nPosY, int _nWidth, int _nHeight, String _ImageURL) throws com.sun.star.uno.Exception {
		XMultiPropertySet xMPSet = _insertPreProc("Image", "com.sun.star.awt.UnoControlImageControlModel");
		short _mode = ImageScaleMode.NONE;
		xMPSet.setPropertyValues(
			new String[] {"Border", "Height", "ImageURL", "PositionX", "PositionY", "ScaleMode", "Width"},	// Remember: Alphabetical Order!
			new Object[] {(short)0, _nHeight, _ImageURL, _nPosX, _nPosY, _mode, _nWidth});
		return (XControl) _insertPostProc(XControl.class, xMPSet);
	}
	
	protected XControl insertImage(int _nPosX, int _nPosY, int _nWidth, int _nHeight, byte[] _ImageHexbinary, String imgtype) throws com.sun.star.uno.Exception {
		XMultiPropertySet xMPSet = _insertPreProc("Image", "com.sun.star.awt.UnoControlImageControlModel");
		short _mode = ImageScaleMode.NONE;
		
		XGraphic xGraphic = getGraphic(_ImageHexbinary, imgtype);
		
		//int borderColor = 16514043;	// #FBFBFB
		
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
	            e.printStackTrace(System.err);
	            System.exit(1);
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
		
        //PropertyValue[] mediaProps = new PropertyValue[3];
        PropertyValue[] mediaProps = new PropertyValue[2];
        mediaProps[0] = new PropertyValue();
        mediaProps[0].Name = "InputStream";
        mediaProps[0].Value = xIStream;
//        mediaProps[0].Name = "URL";
//        mediaProps[0].Value = "file:///home/David/Pictures/Happy.jpg";        
        //mediaProps[1] = new PropertyValue();
        //mediaProps[1].Name = "OutputStream";
        //mediaProps[1].Value = xOStream;
        mediaProps[1] = new PropertyValue();
        mediaProps[1].Name = "MimeType";
        mediaProps[1].Value = "image/" + imgtype;
	
		// Instantiate a GraphicProvider
		Object oGraphicProvider = xMCF.createInstanceWithContext("com.sun.star.graphic.GraphicProvider", xContext);
		XGraphicProvider xGraphicProvider = UnoRuntime.queryInterface(XGraphicProvider.class, oGraphicProvider);
		
		XGraphic xGraphic = xGraphicProvider.queryGraphic(mediaProps);

		String xGP = (xGraphicProvider!=null)?"OK!":"NULL!";
		String xG = (xGraphic!=null)?"OK!":"NULL!";
		
		System.out.println("xGraphicProvider " + xGP + "  " + "xGraphic " + xG);
		
		return xGraphic;
	}
	
	private FontDescriptor initFont (Object oFTModel) throws com.sun.star.uno.Exception {		
		XPropertySet xFixedTextProps = UnoRuntime.queryInterface(XPropertySet.class, oFTModel);
		FontDescriptor fontDescriptor = (FontDescriptor) xFixedTextProps.getPropertyValue("FontDescriptor");
		
		fontDescriptor.Slant = FontSlant.ITALIC;
		fontDescriptor.Height = 12;
		
		return fontDescriptor;
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
		
		XUnitConversion m_xConversion = UnoRuntime.queryInterface(XUnitConversion.class, loWindow);
		
/*
		XDevice xDevice = UnoRuntime.queryInterface(XDevice.class, loWindow);
		

		Point ptHeightPoints = new Point(26,26);
		Point ptHeightPixels = m_xConversion.convertPointToPixel(ptHeightPoints, MeasureUnit.POINT);
		System.out.println("Font height = " + ptHeightPixels.Y + " pixels.");
		//ptHeightPixels.X += 2;
		Point ptHeightDialog = m_xConversion.convertPointToLogic(ptHeightPixels, MeasureUnit.APPFONT);
		System.out.println("Font height = " + ptHeightDialog.X + "," + ptHeightDialog.Y + " dialog units.");
		
		FontDescriptor fontDescriptor = new FontDescriptor();
		fontDescriptor.Height = (short) (ptHeightDialog.Y);
		fontDescriptor.Width  = (short) (ptHeightDialog.X);
		//fontDescriptor.Name = "Liberation Serif";
		fontDescriptor.Name = "Deja Vu Serif Condensed";
		fontDescriptor.Weight = com.sun.star.awt.FontWeight.BOLD;
		
		XFont xFont = xDevice.getFont(fontDescriptor);

//		long lStrWidth = xFont.getStringWidth("Insert child before or after the current child?");
//		System.out.println("Insert child before or after the current child? = " + lStrWidth + " dialog units.");
//		System.out.println("Char 'm' width = " + xFont.getCharWidth('m') + " dialog units.");

		//String mmm = "mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm";
		//long lStrWidth = xFont.getStringWidth(mmm);
		//System.out.println(mmm + " = " + lStrWidth + " dialog units.");
		//System.out.println("Char 'm' width = " + xFont.getCharWidth('m') + " dialog units.");
*/
		Rectangle loWindowRect = loWindow.getPosSize();
		
		Point ptWinSizePixels = new Point(loWindowRect.Width,loWindowRect.Height);
		
		// The following two lines of code, using XWindow loWindow, were inspired by code at
		// https://github.com/qt-haiku/LibreOffice/blob/master/toolkit/qa/complex/toolkit/UnitConversion.java
		// where an XWindowPeer is used as the Object in the queryInterface call.
		//
		// These lines work great, but I've been unable to find where XWindowPeer or XWindow implementation
		// or inheritance of the XUnitConversion interface is documented...
//		XUnitConversion m_xConversion = UnoRuntime.queryInterface(XUnitConversion.class, loWindow);
		
		
		//Point ptStrWidthPixels = new Point((int) lStrWidth, 0);
		//Point ptStrWidthDialog = m_xConversion.convertPointToLogic(ptStrWidthPixels, MeasureUnit.APPFONT);
		//System.out.println("Insert child before or after the current child? = " + ptStrWidthDialog.X + " dialog units.");
		
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
