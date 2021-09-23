package loCommonDialogs;

import java.util.logging.Level;

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
	
	// Button Position Values
	private int Btn2horizpos;
	private int Btn1horizpos;
	private int Cancelhorizpos;
	
	// Control Instance Storage
	private XFixedText	guiLabel;
	private XButton		guiChoiceBtn2;
	private XButton		guiChoiceBtn1;
	private XControl	guiIcon;
	private XButton		guiCancelBtn;
	
	public loChoiceBox(XComponentContext xComponentContext) {
		super(xComponentContext);
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
			guiLabel = insertFixedText(loDialogBox.textalign_left, labelposX, labelposY, labelwidth, labelheight, 0, "Input something!");

			String iconNone = "";
			guiIcon = insertImage(padding, padding, iconsize, iconsize, iconNone);
			
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
		
		guiChoiceBtn2.setLabel(btnlabel2);
		guiChoiceBtn1.setLabel(btnlabel1);
		
		// Configure Label
		XPropertySet xLabelProps = formatLabelText(xDoc, guiLabel, labeltext);
		sizeLabel(xLabelProps, guiLabel, labeltext);
		calcLabelAndBtnVertPos();
		setLabelVertPos(xLabelProps);
		
		// Calculate Dialog Width and Height
		dialogwidth = (2*padding) + iconsize + labelwidth + gap;
		dialogwidth += dialogwidth % 2;
		dialogheight = btnvertpos + btnheight + padding;
		
		setDialogSize();
		
		configButtons();
		
		btnclicked = 0;
			super.show(xDoc, title);	// If one of the choice buttons is clicked, the associated ActionListenerImpl changes btnclicked accordingly.
		return btnclicked;
	}
	
	private void configButtons () {
		try {
			XControl xChoiceBtn2Control = UnoRuntime.queryInterface(XControl.class, guiChoiceBtn2);
			XPropertySet xChoiceBtn2Props = UnoRuntime.queryInterface(XPropertySet.class, xChoiceBtn2Control.getModel());
			
			XControl xChoiceBtn1Control = UnoRuntime.queryInterface(XControl.class, guiChoiceBtn1);
			XPropertySet xChoiceBtn1Props = UnoRuntime.queryInterface(XPropertySet.class, xChoiceBtn1Control.getModel());
			
			XControl xCancelBtnControl = UnoRuntime.queryInterface(XControl.class, guiCancelBtn);
			XPropertySet xCancelBtnProps = UnoRuntime.queryInterface(XPropertySet.class, xCancelBtnControl.getModel());
			
			Btn2horizpos	= (dialogwidth - (3*btnwidth) - (2*gap)) / 2;
			Btn1horizpos	= Btn2horizpos + (btnwidth + gap);
			Cancelhorizpos	= Btn1horizpos + (btnwidth + gap);
			
			xChoiceBtn2Props.setPropertyValue("PositionX", Btn2horizpos);
			xChoiceBtn2Props.setPropertyValue("PositionY", btnvertpos);
			
			xChoiceBtn1Props.setPropertyValue("PositionX", Btn1horizpos);
			xChoiceBtn1Props.setPropertyValue("PositionY", btnvertpos);
			
			xCancelBtnProps.setPropertyValue("PositionX", Cancelhorizpos);
			xCancelBtnProps.setPropertyValue("PositionY", btnvertpos);
			
		} catch (Exception e) {
			DlgLogger.log(null, loDialogBox.class.getName(), Level.WARNING, e);
			// nop - buttons will just be misplaced.
		}
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
