Usage documentation included within source file loMessageBox.java

/*
// USAGE INFORMATION
// 
// Instantiate loMessageBox, and then call show() with appropriate values
// for its parameters to display a messagebox. 
//  
// Reproduction of information from the LibreOffice 5.0 SDK API Reference
// is included here for user convenience to explain parameters passed to
// and values returned from LibreOffice 5.0 SDK API functions.

 * Parameter messageBoxType:
 * 
 * enum com.sun.star.awt.MessageBoxType
 * 
   MESSAGEBOX  A normal message box.
   INFOBOX     A message box to inform the user about a certain event.
               (Ignores messageBoxButtons param, instead using BUTTONS_OK)
   WARNINGBOX  A message to warn the user about a certain problem.
   ERRORBOX    A message box to provide an error message to the user.
   QUERYBOX    A message box to query information from the user.

 * Parameter messageBoxButtons:
 * 
 * com::sun::star::awt::MessageBoxButtons Constant Group
 *
   const long BUTTONS_OK                 = 1 specifies a message with "OK" button.
   const long BUTTONS_OK_CANCEL          = 2 specifies a message box with "OK" and "CANCEL" button.
   const long BUTTONS_YES_NO             = 3 specifies a message box with "YES" and "NO" button.
   const long BUTTONS_YES_NO_CANCEL      = 4 specifies a message box with "YES", "NO" and "CANCEL" button.
   const long BUTTONS_RETRY_CANCEL       = 5 specifies a message box with "RETRY" and "CANCEL" button. 
   const long BUTTONS_ABORT_IGNORE_RETRY = 6 specifies a message box with "ABORT", "IGNORE" and "RETRY" button.
   const long DEFAULT_BUTTON_OK          = 0x10000 specifies that OK is the default button. 
   const long DEFAULT_BUTTON_CANCEL      = 0x20000 specifies that CANCEL is the default button. 
   const long DEFAULT_BUTTON_RETRY       = 0x30000 specifies that RETRY is the default button. 
   const long DEFAULT_BUTTON_YES         = 0x40000 specifies that YES is the default button.
   const long DEFAULT_BUTTON_NO          = 0x50000 specifies that NO is the default button. 
   const long DEFAULT_BUTTON_IGNORE      = 0x60000 specifies that IGNORE is the default button.
	
 * Return Values:
 * 
 * Returns -1 if an error occurs while trying to display the XMessageBox.
 * Otherwise, returns a value from com::sun::star::awt::MessageBoxResults Constant Group
 *
   const short CANCEL = 0  The user canceled the XMessageBox, by pressing "Cancel" or "Abort" button. 
   const short OK     = 1  The user pressed the "Ok" button. 
   const short YES    = 2  The user pressed the "Yes" button. 
   const short NO     = 3  The user pressed the "No" button. 
   const short RETRY  = 4  The user pressed the "Retry" button. 
   const short IGNORE = 5  The user pressed the "Ignore" button.
*/
