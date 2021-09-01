package loCommonDialogs;

import java.util.logging.Level;

import com.sun.star.awt.FontDescriptor;
import com.sun.star.awt.FontWeight;
import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.XButton;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XFixedText;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XModel;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import dlgutils.DlgLogger;

public class loChoiceBox extends loDialogBox implements AutoCloseable {

	short btnclicked = 0;
	
	// Dialog and Control Size & Position Values
	private int labelposX;
	private int labelposY;
	private int btnvertpos;
	private int Btn2horizpos;
	private int Btn1horizpos;
	private int Cancelhorizpos;
	
	// Control Instance Storage
	private XFixedText	guiLabel;
	private XButton		guiChoiceBtn2;
	private XButton		guiChoiceBtn1;
	private XControl	guiIcon;
	@SuppressWarnings("unused")
	private XButton		guiCancelBtn;
	
	public loChoiceBox(XComponentContext xComponentContext) {
		// w = dialog width, h = dialog height, 0 for either = minimum value
		super(xComponentContext);
		iconsize		= 32;
		
		dialogwidth		= 235;
		
		labelwidth		= dialogwidth - (2*margin) - iconsize - gap;
		labelheight		= iconsize;
		labelposX		= margin + iconsize;
		labelposY		= margin;
		
		btnvertpos		= margin + labelheight;	
		Btn2horizpos	= (dialogwidth - (3*btnwidth) - (2*gap)) / 2;
		Btn1horizpos	= Btn2horizpos + (btnwidth + gap);
		Cancelhorizpos	= Btn1horizpos + (btnwidth + gap);
		
		dialogheight	= margin + iconsize + btnheight + margin + 3;	// 3 is a fudge factor
		
		initBox();
	}
	
	protected void initBox() {
		xMCF = xContext.getServiceManager();
		createDialog(xMCF, xContext);

		initialize (
				new String[] { "Height", "Moveable", "Name", "PositionX", "PositionY", "Step", "TabIndex", "Title", "Width" },
				new Object[] { dialogheight, true, "loInputBox", dialogxpos, dialogypos, 0, (short)0, "loInputBox", dialogwidth }
		);
		
		// add dialog controls
		try {
			guiLabel = insertFixedText(loDialogBox.textalign_center, labelposX, labelposY, labelwidth, labelheight, 0, "Input something!");

			String iconNone = "";
			guiIcon = insertImage(margin, margin, iconsize, iconsize, iconNone);
			
			//// In the following, return values from com.sun.star.awt.MessageBoxResults are used for choice buttons
			//// to make loChoiceBox a drop-in replacement for a XMessageBox-based Yes/No/Cancel messagebox.
			
			// Button 2
			guiChoiceBtn2 = insertButton(Btn2horizpos, btnvertpos, btnwidth, btnheight, "Choice 2", (short) PushButtonType.STANDARD_value, false );
			guiChoiceBtn2.setActionCommand("2");	// String is converted to (short)2 on click, equal to com.sun.star.awt.MessageBoxResults.YES
			ActionListenerImpl xBtn2Listener = new ActionListenerImpl();
			guiChoiceBtn2.addActionListener(xBtn2Listener);
			
			// Button 1
			guiChoiceBtn1 = insertButton(Btn1horizpos, btnvertpos, btnwidth, btnheight, "Choice 1", (short) PushButtonType.STANDARD_value, false );
			guiChoiceBtn1.setActionCommand("3");	// String is converted to (short)3 on click, equal to com.sun.star.awt.MessageBoxResults.NO
			ActionListenerImpl xBtn1Listener = new ActionListenerImpl();
			guiChoiceBtn1.addActionListener(xBtn1Listener);
						
			// Button 0
			guiCancelBtn = insertButton(Cancelhorizpos, btnvertpos, btnwidth, btnheight, "Cancel", (short) PushButtonType.CANCEL_value, true);
			
		} catch (com.sun.star.uno.Exception e) {
			DlgLogger.log(null, loDialogBox.class.getName(), Level.SEVERE, e);
			e.printStackTrace(System.err);
		}
		
		m_xWindowPeer = getWindowPeer();
		xDialog = UnoRuntime.queryInterface(XDialog.class, m_xDialogControl);
	}
	
	public short show(XModel xDoc, String title, String labeltext, String btnlabel2, String btnlabel1, String ImageUrl) {

		if (ImageUrl != "")
			configIcon(guiIcon, ImageUrl);
		
		guiLabel.setText(labeltext);
		guiChoiceBtn2.setLabel(btnlabel2);
		guiChoiceBtn1.setLabel(btnlabel1);

		// Get Label XPropertySet interface
		XPropertySet xLabelProps = getControlProps(guiLabel);

		// Get FontDescriptor
		FontDescriptor appFontDescriptor = getAppFontDescriptor(xDoc);
		appFontDescriptor.Height = 10;
		appFontDescriptor.Weight = FontWeight.BOLD;
		
		try {
			// Config Label's font
			xLabelProps.setPropertyValue("FontDescriptor", appFontDescriptor);
		} catch (Exception e) {
			DlgLogger.log(null, loDialogBox.class.getName(), Level.WARNING, e);
			e.printStackTrace(System.err);
			// Font will just be wrong.
		}
		
		btnclicked = 0;
			super.show(xDoc, title);	// If one of the choice buttons is clicked, the associated ActionListenerImpl changes btnclicked accordingly.
		return btnclicked;
	}
	
	public class ActionListenerImpl extends com.sun.star.lib.uno.helper.WeakBase implements XActionListener
	{
		public ActionListenerImpl() {}
	 
		// XEventListener
		public void disposing (com.sun.star.lang.EventObject source) {}
		 
		// XActionListener
		public void actionPerformed (com.sun.star.awt.ActionEvent rEvent ) {
			btnclicked = Short.valueOf(rEvent.ActionCommand);
			xDialog.endExecute();
		}
	};
}
