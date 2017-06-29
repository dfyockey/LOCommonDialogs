package loCommonDialogs;

import com.sun.star.awt.MessageBoxButtons;
import com.sun.star.awt.MessageBoxType;
import com.sun.star.frame.XModel;

public class loErrorBox {
	
	// Use nested instantiation to hide public show method of "base" class loMessageBox
	loMessageBox MsgBox = new loMessageBox();
	
	public short show(XModel xDoc, String messageBoxTitle, String message) {
		return MsgBox.show(xDoc, MessageBoxType.ERRORBOX, MessageBoxButtons.BUTTONS_OK, messageBoxTitle, message);
	}
}
