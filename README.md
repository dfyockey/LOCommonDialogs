# LOCommonDialogs

LOCommonDialogs is a collection of classes providing dialogs for LibreOffice. These include
classes to display dialogs with customizable icons, customizable button text, and text input,
as well as classes simplifying display of standard XMessageBoxFactory-based dialogs.

The dialog collection includes:

* loChoiceBox with custom icon, two buttons with customizable text for a binary choice, and a 'Cancel' button.
* loCustomMessageBox with an option to select between custom message, warning, user error, and system error icons.
* loInputBox with custom icon and an edit box enabling text input.
* loMessageBox enabling simple display of any standard XMessageBoxFactory-based dialog.
* loErrorBox wrapping loMessageBox to conveniently display a standard XMessageBoxFactory-based dialog of type ERRORBOX.

LOCommonDialogs is *not* an application in itself, but rather a collection of classes for use in or with another application.

## Requirements

LOCommonDialogs requires the following libraries from the LibreOffice SDK:

* juh.jar
* jurt.jar
* ridl.jar
* unoil.jar
* unoloader.jar
* libreoffice.jar

IMPORTANT: Implementation of code for connection to a LibreOffice instance is required.

## Usage

With a connection to LibreOffice established, instantiate a dialog class as a resource in a try-with-resources,
then call the appropriate dialog `show` method in the try block. Required arguments can be determined by examination of the source code.

### Custom Icons

Custom icons must be placed in a particular location depending on the particular application:

* In a stand-alone application, custom icons must be provided in a package named `images` in a source folder separate from application source code.
* In an extension, custom icons must be placed in a directory named `images` in the extension's root directory. Additionally, an ImageProc must be
created using the constructor to pass the extension identifier string so as to make it available to the LOCommonDialogs so the images can be found.

In either case, get the path to an icon to then pass as an argument in a `show` method call as follows:

        String iconPath = new ImageProc().getURL("/images/icon.svg");

Custom icons are preferably `.svg` files, although `.png` or other file types may be used. See the MimeTypes public attribute
of the MediaProperties Service in the LibreOffice SDK API Reference for a list of possible types.

Aside from providing the path to a particular custom icon, four custom icons can be provided for loCustomMessageBoxes. One of the four can be
selected using static values listed in loCustomMessageBox.java without need to provide a custom icon in the `show` mdthod call. The four custom
icons must be of type `.svg`; named `message.svg`, `warning.svg`, `usrerror.svg`, and `syserror.svg`; and be located in the same location as other
custom icon files.

