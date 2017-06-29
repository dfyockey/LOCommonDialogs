package loCommonDialogs;

import com.sun.star.awt.MessageBoxButtons;
import com.sun.star.awt.MessageBoxType;
import com.sun.star.frame.XModel;

public class loErrorBox extends loMessageBox {
	public short show(XModel xDoc, String messageBoxTitle, String message) {
		return super.show(xDoc, MessageBoxType.ERRORBOX, MessageBoxButtons.BUTTONS_OK, messageBoxTitle, message);
	}
}
