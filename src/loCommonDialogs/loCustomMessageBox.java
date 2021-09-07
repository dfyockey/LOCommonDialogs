package loCommonDialogs;

import java.util.logging.Level;

import com.sun.star.awt.FontDescriptor;
import com.sun.star.awt.FontWeight;
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

// Due to the current inability to match label width to text width and image control to icon size,
// Centered Buttons, which deemphasize margin discrepancies, are preferred over Right-Justified buttons.

public class loCustomMessageBox extends loDialogBox implements AutoCloseable {
	
	public static int iconMessage	= 0;
	public static int iconWarning	= 1;
	public static int iconUsrError	= 2;
	public static int iconSysError	= 3;
	
	public static boolean showCancelBtn = true;
	public static boolean hideCancelBtn = false;
	
	// Dialog and Control Size & Position Values
	private int labelvertpos, labelhorizpos;
	private int btnvertpos, okbtnhpos, cancelbtnhpos;
	private int vmargin;
	private int gap;
	
	// Control Instance Storage
	private XFixedText	guiLabel;
	private XButton		guiOKBtn;
	private XButton		guiCancelBtn;
	private XControl	guiIcon;
	
	public loCustomMessageBox(XComponentContext xComponentContext) {
		super(xComponentContext);
		gap				= margin;
		vmargin			= margin;		// Amount to offset everything from the top
		iconsize		= 28;
		dialogwidth		= 175;
		labelwidth		= dialogwidth - iconsize - (2*margin) - (2*gap);
		labelheight		= iconsize;
		labelvertpos	= vmargin;
		labelhorizpos	= margin + iconsize + gap;
		//btnvertpos		= dialogheight - btnheight - margin - 3;	// 3 is a fudge factor
		btnvertpos		= vmargin + labelheight;
		
		dialogheight	= vmargin + labelheight + labelborderwidth*2 + btnheight + 2 + vmargin;	// 2 = button border width?
		
		// Centered Buttons
		okbtnhpos		= dialogwidth/2 - btnwidth - gap/2;
		cancelbtnhpos	= dialogwidth/2 + gap/2;
		
		// Right-Justified Buttons
//		okbtnhpos		= dialogwidth - margin - 2*btnwidth - gap;
//		cancelbtnhpos	= dialogwidth - margin - btnwidth;		
		
		initBox();
	}
	
	// loDialogBox Abstract Method Definition
	protected void initBox() {
		xMCF = xContext.getServiceManager();
		createDialog(xMCF, xContext);

		initialize (
				new String[] { "Height", "Moveable", "Name", "PositionX", "PositionY", "Sizeable", "Step", "TabIndex", "Title", "Width" },
				new Object[] { dialogheight, true, "loInputBox", dialogxpos, dialogypos, true, 0, (short)0, "loInputBox", dialogwidth }
		);
		
		// add dialog controls
		try {
			// Message Icon (Default)
			String msgicon = new ImageProc("/images/message.svg").getURL();
			guiIcon = insertImage(margin, vmargin, iconsize, iconsize, msgicon);
			
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
	
	/*
	 * Implement dialog resizing on show, with accompanying control repositioning, in a future version...
	 *
	 * DON'T DELETE THIS!
	 * 
	public short show(XModel xDoc, String title, String message, String rawhexPng, int dlgWidth) {
		return show(xDoc, title, message, rawhexPng, dlgWidth, dialogheight);
	}
	
	public short show(XModel xDoc, String title, String message, String rawhexPng, int dlgWidth, int dlgHeight) {
		XControlModel xDialogModel = m_xDialogControl.getModel();
		XPropertySet xDialogProps = UnoRuntime.queryInterface(XPropertySet.class, xDialogModel);
		try {
			xDialogProps.setPropertyValue("Width", dlgWidth);
			xDialogProps.setPropertyValue("Height", dlgHeight);
		} catch (Exception e) {
			// nop -- default dimensions will be used
		}
		
		return show(xDoc, title, message, rawhexPng);
	}
	*/
	
	public short show(XModel xDoc, String title, String message, String subtext, int iconIndex) {
		return show(xDoc, title, message, subtext, iconIndex, false);
	}
	
	public short show(XModel xDoc, String title, String message, String subtext, int iconIndex, boolean cancelbtn) {
		
		String icontype = "";

		configButtons(cancelbtn);
		
		switch (iconIndex) {
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
		
		return show(xDoc, title, message, subtext, iconURL);
	}
	
	public short show(XModel xDoc, String title, String message, String subtext, String iconURL) {
		return show(xDoc, title, message, subtext, iconURL, false);
	}
	
	public short show(XModel xDoc, String title, String message, String subtext, String iconURL, boolean cancelbtn) {
		// Use MessageBoxType.ERRORBOX for a System Error, MessageBoxType.INFOBOX for a User Error, or MessageBoxType.WARNINGBOX for a Warning
		
		// NOTE: Since guiLabel2 has been removed from the dialog, String subtext is ignored.
		//       The argument is retained for compatibility with existing method calls,
		//       but should be removed in a future version.
		
		// Configure Warning Text to the current Application Font and at size 12pt and BOLD
		//// Get Label XPropertySet interface
		XPropertySet xLabelProps = getControlProps(guiLabel);
		
		//// Get FontDescriptor for Application Font
		FontDescriptor appFontDescriptor = getAppFontDescriptor(xDoc);
		appFontDescriptor.Height = 10;
		appFontDescriptor.Weight = FontWeight.BOLD;
		
		if (!iconURL.isEmpty())
			configIcon(guiIcon, iconURL);
		
		try {
			xLabelProps.setPropertyValue("Label", message);
			xLabelProps.setPropertyValue("FontDescriptor", appFontDescriptor);
			
//			appFontDescriptor.Height = 10;
//			appFontDescriptor.Weight = FontWeight.NORMAL;
			
		} catch (Exception e) {
			DlgLogger.log(null, loDialogBox.class.getName(), Level.WARNING, e);
			e.printStackTrace(System.err);
			// One or more fonts will just be wrong.
		}
		
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
				xCancelBtnWindow.setVisible(true);
				xCancelBtnProps.setPropertyValue("PositionX", dialogwidth/2 + gap/2);
				
				// Right-Justified Buttons
//				xOKBtnProps.setPropertyValue("PositionX", dialogwidth - margin - 2*btnwidth - gap);
//				xCancelBtnWindow.setVisible(true);
//				xCancelBtnProps.setPropertyValue("PositionX", dialogwidth - margin - btnwidth);
				
			} else {
				xOKBtnProps.setPropertyValue("PositionX", dialogwidth/2 - btnwidth/2);
				xCancelBtnWindow.setVisible(false);
			}
		} catch (Exception e) {
			DlgLogger.log(null, loDialogBox.class.getName(), Level.WARNING, e);
			// nop - buttons will just be misplaced.
		}
	}	
}
