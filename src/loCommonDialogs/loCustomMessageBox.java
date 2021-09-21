package loCommonDialogs;

import java.util.logging.Level;

import com.sun.star.awt.FontDescriptor;
import com.sun.star.awt.FontWeight;
import com.sun.star.awt.Point;
import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XButton;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XFixedText;
import com.sun.star.awt.XLayoutConstrains;
import com.sun.star.awt.XUnitConversion;
import com.sun.star.awt.XWindow;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XModel;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.MeasureUnit;

import dlgutils.DlgLogger;
import dlgutils.ImageProc;

public class loCustomMessageBox extends loDialogBox implements AutoCloseable {
	
	public static int iconMessage	= 0;
	public static int iconWarning	= 1;
	public static int iconUsrError	= 2;
	public static int iconSysError	= 3;
	
	public static boolean showCancelBtn = true;
	public static boolean hideCancelBtn = false;
	
	// Control Position Values
	private int btnvertpos, okbtnhpos, cancelbtnhpos;
	
	// Control Instance Storage
	private XFixedText	guiLabel;
	private XButton		guiOKBtn;
	private XButton		guiCancelBtn;
	private XControl	guiIcon;
	
	public loCustomMessageBox(XComponentContext xComponentContext) {
		super(xComponentContext);
		initBox();
	}
	
	// loDialogBox Abstract Method Definition
	protected void initBox() {
		xMCF = xContext.getServiceManager();
		createDialog(xMCF, xContext);

		initialize (
				new String[] { "Height", "Moveable", "Name", "PositionX", "PositionY", "Sizeable", "Step", "TabIndex", "Title", "Width" },
				new Object[] { dialogheight, true, "loCommonMessageBox", dialogxpos, dialogypos, false, 0, (short)0, "loCommonMessageBox", dialogwidth }
		);
		
		// add dialog controls
		try {
			guiIcon = insertImage(padding, padding, iconsize, iconsize, "");
			
			guiLabel 	 = insertFixedText(textalign_left, labelhorizpos, labelvertpos, labelwidth, labelheight, 0, "");
			guiOKBtn 	 = insertButton(okbtnhpos, btnvertpos, btnwidth, btnheight, "OK", (short) PushButtonType.OK_value, true);
			guiCancelBtn = insertButton(cancelbtnhpos, btnvertpos, btnwidth, btnheight, "Cancel", (short) PushButtonType.CANCEL_value, true);
		} catch (com.sun.star.uno.Exception e) {
			DlgLogger.log(null, loDialogBox.class.getName(), Level.SEVERE, e);
			e.printStackTrace(System.err);
		}
		
		m_xWindowPeer = getWindowPeer();
		xDialog = UnoRuntime.queryInterface(XDialog.class, m_xDialogControl);
	}
	
	// Convenience method for show with no cancel button
	public short show(XModel xDoc, String title, String message, int iconIndex) {
		return show(xDoc, title, message, iconIndex, false);
	}
	
	public short show(XModel xDoc, String title, String message, int iconIndex, boolean cancelbtn) {
		
		String icontype = "";

		configButtons(cancelbtn);
		
		switch (iconIndex) {
			case 0:
				// Message Icon
				icontype = "message";
				break;
			case 1:
				// Warning Icon
				icontype = "warning";
				break;
			case 2:
				// User Error Icon
				icontype = "usrerror";
				break;
			case 3:
				// System Error Icon
				icontype = "syserror";
				break;
			default:
				break;
		}
		
		String iconURL = "";
		if ( !icontype.isEmpty() ) {
			iconURL = new ImageProc("/images/" + icontype + ".svg").getURL();
		}
		
		return show(xDoc, title, message, iconURL, cancelbtn);
	}
	
	// Convenience method for show with no cancel button
	public short show(XModel xDoc, String title, String message, String iconURL) {
		return show(xDoc, title, message, iconURL, false);
	}	
	
	public short show(XModel xDoc, String title, String message, String iconURL, boolean cancelbtn) {

		// Configure Icon
		
		if (!iconURL.isEmpty())
			configIcon(guiIcon, iconURL);
		
		
		// Configure Label
		
		//// Get and adjust FontDescriptor for Label Font
		FontDescriptor labelFontDescriptor = getLabelFontDescriptor(xDoc);
		labelFontDescriptor.Weight = FontWeight.BOLD;

		//// Set Label Properties
		XPropertySet xLabelProps = getControlProps(guiLabel);
		try {
			xLabelProps.setPropertyValue("Label", message);
			xLabelProps.setPropertyValue("FontDescriptor", labelFontDescriptor);
		} catch (Exception e) {
			DlgLogger.log(null, loDialogBox.class.getName(), Level.WARNING, e);
			e.printStackTrace(System.err);
			// One or more fonts will just be wrong.
		}
		
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
		
		//// Set Label Width and Height to accommodate its text, and
		//// set the Label and Button vertical positions
		try {
			xLabelProps.setPropertyValue("Width", labelwidth);
			xLabelProps.setPropertyValue("Height", labelheight);
			
			// Vertically position the Label relative to the Icon
			if ( ptLabelDlgUnits.Y < iconsize ) {
				// Vertically center the Label relative to the Icon
				labelvertpos = padding + (iconsize / 2 - labelheight / 2);
				btnvertpos   = padding + iconsize + gap/2;
			} else {
				// Vertically position the Label at the same position as the Icon
				labelvertpos = padding;
				btnvertpos   = labelvertpos + ptLabelDlgUnits.Y;
			}
			xLabelProps.setPropertyValue("PositionY", labelvertpos);
		} catch (Exception e) {
			DlgLogger.log(null, loDialogBox.class.getName(), Level.WARNING, e);
			e.printStackTrace(System.err);
			// Label dimensions will just be wrong.
		}		
		
		// Calculate and Set Dialog Width and Height

		dialogwidth = (2*padding) + iconsize + ptLabelDlgUnits.X + gap;
		dialogheight = btnvertpos + btnheight + padding;
		
		XControlModel xDialogModel = m_xDialogControl.getModel();
		XPropertySet xDialogProps = UnoRuntime.queryInterface(XPropertySet.class, xDialogModel);
		try {
			xDialogProps.setPropertyValue("Width", dialogwidth);
			xDialogProps.setPropertyValue("Height", dialogheight);
		} catch (Exception e) {
			// nop -- default dimensions will be used
		}
		
		configButtons(cancelbtn);
		
		return super.show(xDoc, title);
	}
	
	private void configButtons (boolean cancelbtn) {
		XControl xCancelBtnControl = UnoRuntime.queryInterface(XControl.class, guiCancelBtn);
		XPropertySet xCancelBtnProps = UnoRuntime.queryInterface(XPropertySet.class, xCancelBtnControl.getModel());
		
		XWindow xCancelBtnWindow = UnoRuntime.queryInterface(XWindow.class, guiCancelBtn);
		
		XControl xOKBtnControl = UnoRuntime.queryInterface(XControl.class, guiOKBtn);
		XPropertySet xOKBtnProps = UnoRuntime.queryInterface(XPropertySet.class, xOKBtnControl.getModel());
		
		try {
			if (cancelbtn) {
				
				// Centered Buttons
				xOKBtnProps.setPropertyValue("PositionX", dialogwidth/2 - btnwidth - gap/2);
				xOKBtnProps.setPropertyValue("PositionY", btnvertpos );
				xCancelBtnWindow.setVisible(true);
				xCancelBtnProps.setPropertyValue("PositionY", btnvertpos );
				xCancelBtnProps.setPropertyValue("PositionX", dialogwidth/2 + gap/2);
				
				// Right-Justified Buttons
//				xOKBtnProps.setPropertyValue("PositionX", dialogwidth - padding - 2*btnwidth - gap);
//				xCancelBtnWindow.setVisible(true);
//				xCancelBtnProps.setPropertyValue("PositionX", dialogwidth - padding - btnwidth);
				
			} else {
				// Centered Buttons
				xOKBtnProps.setPropertyValue("PositionX", dialogwidth/2 - btnwidth/2);
				xOKBtnProps.setPropertyValue("PositionY", btnvertpos );
				xCancelBtnWindow.setVisible(false);
				
				// Right-Justified Buttons
//				xOKBtnProps.setPropertyValue("PositionX", dialogwidth - padding - btnwidth);
//				xOKBtnProps.setPropertyValue("PositionY", btnvertpos );
//				xCancelBtnWindow.setVisible(false);
				
			}
		} catch (Exception e) {
			DlgLogger.log(null, loDialogBox.class.getName(), Level.WARNING, e);
			// nop - buttons will just be misplaced.
		}
	}
	
	/* DEPRECATED METHODS
	 * 		retained temporarily for backward compatibility
	 */
	public short show(XModel xDoc, String title, String message, String subtext, int iconIndex) {
		return show(xDoc, title, message, iconIndex, false);
	}
	
	public short show(XModel xDoc, String title, String message, String subtext, int iconIndex, boolean cancelbtn) {
		return show(xDoc, title, message, iconIndex, cancelbtn);
	}
	
	public short show(XModel xDoc, String title, String message, String subtext, String iconURL) {
		return show(xDoc, title, message, iconURL, false);
	}
	
	public short show(XModel xDoc, String title, String message, String subtext, String iconURL, boolean cancelbtn) {
		return show(xDoc, title, message, iconURL, cancelbtn);
	}
}
