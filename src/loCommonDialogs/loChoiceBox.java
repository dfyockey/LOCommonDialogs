package loCommonDialogs;

import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.XButton;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XFixedText;
import com.sun.star.frame.XModel;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

public class loChoiceBox extends loDialogBox implements AutoCloseable {

	short btnclicked = 0;
	
	// Dialog and Control Size & Position Values
	private int btnvertpos		= margin + labelheight + (2*gap);
	private int Btn2horizpos	= dialogwidth - margin - (3*btnwidth) - (2*gap);
	private int Btn1horizpos	= dialogwidth - margin - (2*btnwidth) - gap;
	private int Cancelhorizpos	= dialogwidth - margin - btnwidth;
	
	// Control Instance Storage
	private XFixedText	guiLabel;
	private XButton		guiChoiceBtn2;
	private XButton		guiChoiceBtn1;
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
			
			guiLabel	 = insertFixedText(margin, margin, labelwidth, labelheight, 0, "Input something!");
			
			// Button 2
			guiChoiceBtn2 = insertButton(Btn2horizpos, btnvertpos, btnwidth, btnheight, "Choice 2", (short) PushButtonType.STANDARD_value, false );
			guiChoiceBtn2.setActionCommand("2");
			ActionListenerImpl xBtn2Listener = new ActionListenerImpl();
			guiChoiceBtn2.addActionListener(xBtn2Listener);
			
			// Button 1
			guiChoiceBtn1 = insertButton(Btn1horizpos, btnvertpos, btnwidth, btnheight, "Choice 1", (short) PushButtonType.STANDARD_value, false );
			guiChoiceBtn1.setActionCommand("1");
			ActionListenerImpl xBtn1Listener = new ActionListenerImpl();
			guiChoiceBtn1.addActionListener(xBtn1Listener);
						
			// Button 0
			guiCancelBtn = insertButton(Cancelhorizpos, btnvertpos, btnwidth, btnheight, "Cancel", (short) PushButtonType.CANCEL_value, true);
			
		} catch (com.sun.star.uno.Exception e) {
			e.printStackTrace(System.err);
		}
		
		m_xWindowPeer = getWindowPeer();
		xDialog = UnoRuntime.queryInterface(XDialog.class, m_xDialogControl);
	}
	
	public short show(XModel xDoc, String title, String labeltext, String btnlabel2, String btnlabel1) {
		guiLabel.setText(labeltext);
		guiChoiceBtn2.setLabel(btnlabel2);
		guiChoiceBtn1.setLabel(btnlabel1);
		btnclicked = 0;
		
		short Result = super.show(xDoc, title);
		System.out.println(btnclicked);
		return Result;
	}
	
	public class ActionListenerImpl extends com.sun.star.lib.uno.helper.WeakBase implements XActionListener
	{
		public ActionListenerImpl() {}
	 
		// XEventListener
		public void disposing (com.sun.star.lang.EventObject source) {}
		 
		// XActionListener
		public void actionPerformed (com.sun.star.awt.ActionEvent rEvent ) {
			btnclicked = Short.valueOf(rEvent.ActionCommand);
			//System.out.println(btnclicked);
			xDialog.endExecute();
		}
	};
	
	public void close() throws Exception {
		// Dispose the component and free the memory...
        if (m_xComponent != null){
            m_xComponent.dispose();
        }		
	}
}
