package loCommonDialogs;

import com.sun.star.awt.MessageBoxType;
import com.sun.star.awt.XMessageBox;
import com.sun.star.awt.XMessageBoxFactory;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.frame.XModel;
import com.sun.star.uno.UnoRuntime;

// Source from which the loMessageBox.show method was distilled:
// - hol.sten's "Java and Java script solution" at https://forum.openoffice.org/en/forum/viewtopic.php?f=45&t=2721
// - LibreOffice 5.0 SDK API Reference

// Usage Info:
// - Returns -1 on error
// - For info on loMessageBox.show parameters,
//	 	see LibreOffice SDK API Reference concerning 'com.sun.star.awt.XMessageBoxFactory.createMessageBox'
// - For info on other return values,
//		see LibreOffice SDK API Reference concerning 'com.sun.star.awt.MessageBoxResults'

public class loMessageBox {
	public short show(XModel xDoc, MessageBoxType messageBoxType, int messageBoxButtons, String messageBoxTitle, String message) {
		try {
	        // Get the parent window and access to the window toolkit of the parent window
			XWindow parentWindow = xDoc.getCurrentController().getFrame().getContainerWindow();
			XWindowPeer parentWindowPeer = (XWindowPeer) UnoRuntime.queryInterface(XWindowPeer.class, parentWindow);
		
			// Initialize the message box factory
			XMessageBoxFactory messageBoxFactory = (XMessageBoxFactory) UnoRuntime.queryInterface(XMessageBoxFactory.class,parentWindowPeer.getToolkit());
			XMessageBox box = messageBoxFactory.createMessageBox(parentWindowPeer, messageBoxType, messageBoxButtons, messageBoxTitle, message) ;
			return box.execute();
		} catch (Exception e) {
			return -1;
		}
    }
}
