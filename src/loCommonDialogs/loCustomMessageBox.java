package loCommonDialogs;

import java.util.logging.Level;

import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XButton;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XFixedText;
import com.sun.star.awt.XWindow;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XModel;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

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
	private int okbtnhpos, cancelbtnhpos;
	
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
			
			guiLabel 	 = insertFixedText(textalign_left, labelposX, labelposY, labelwidth, labelheight, 0, "");
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
		XPropertySet xLabelProps = formatLabelText(xDoc, guiLabel, message);
		sizeLabel(xLabelProps, guiLabel, message);
		calcLabelAndBtnVertPos();
		setLabelVertPos(xLabelProps);
		
		// Calculate Dialog Width and Height
		dialogwidth = (2*padding) + iconsize + labelwidth + gap;
		dialogheight = btnvertpos + btnheight + padding;
		
		setDialogSize();
		
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
