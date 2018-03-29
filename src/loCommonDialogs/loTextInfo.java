package loCommonDialogs;

import com.sun.star.awt.DeviceInfo;
import com.sun.star.awt.FontDescriptor;
import com.sun.star.awt.FontPitch;
import com.sun.star.awt.Size;
import com.sun.star.awt.XDevice;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XFont;
import com.sun.star.awt.XUnitConversion;
import com.sun.star.awt.XWindow;
import com.sun.star.frame.XModel;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.MeasureUnit;

public class loTextInfo {
	private static XFont xFont;
	public static FontDescriptor fontDescriptor = new FontDescriptor();
	
	public static int getExtent (XModel xDoc, String textstring, FontDescriptor fd) {
		short oldWidth  = fd.Width;
		short oldHeight = fd.Height;
		
		XWindow loWindow = xDoc.getCurrentController().getFrame().getContainerWindow();
		XUnitConversion xUnitConversion = UnoRuntime.queryInterface(XUnitConversion.class, loWindow);
		XDevice	xDevice	 = UnoRuntime.queryInterface(XDevice.class, loWindow);		
		
		// Convert font size from Points to Dialog Units
		fd.Width = fd.Height;	// Necessary because fd.Width = 0
		Size szHeightPoints = new Size(fd.Width, fd.Height);
		Size szHeightPixels = xUnitConversion.convertSizeToPixel(szHeightPoints, MeasureUnit.POINT);
		Size szHeightDialog = xUnitConversion.convertSizeToLogic(szHeightPixels, MeasureUnit.APPFONT);
		
		fd.Width = (short) szHeightDialog.Width;
		fd.Height = (short) szHeightDialog.Height;
		
		xFont = xDevice.getFont(fd);
		int strWidth = xFont.getStringWidth(textstring);
		
		fd.Width  = oldWidth;
		fd.Height = oldHeight;
		
		return strWidth;
	}	
	
	public static int getExtent (XDialog xDialog, String textstring, String fontname, int fontptsize, float fontweight) {
		XWindow loWindow = UnoRuntime.queryInterface(XWindow.class, xDialog);
		
		XUnitConversion xUnitConversion = UnoRuntime.queryInterface(XUnitConversion.class, loWindow);
		XDevice			xDevice			= UnoRuntime.queryInterface(XDevice.class, loWindow);		
		
		// Convert font size from Points to Dialog Units
		Size szHeightPoints = new Size(fontptsize, fontptsize);
		Size szHeightPixels = xUnitConversion.convertSizeToPixel(szHeightPoints, MeasureUnit.POINT);
		Size szHeightDialog = xUnitConversion.convertSizeToLogic(szHeightPixels, MeasureUnit.APPFONT);
		
		fontDescriptor.Name = fontname;
		fontDescriptor.Height = (short) (szHeightDialog.Height);
		fontDescriptor.Width = (short) (szHeightDialog.Width);
		fontDescriptor.Weight = fontweight;
		
		xFont = xDevice.getFont(fontDescriptor);
		return xFont.getStringWidth(textstring);
	}
	
	
	public static int getExtent (XModel xDoc, String textstring, String fontname, int fontptsize, float fontweight) {
		XWindow loWindow = xDoc.getCurrentController().getFrame().getContainerWindow();
		
		XUnitConversion xUnitConversion = UnoRuntime.queryInterface(XUnitConversion.class, loWindow);
		XDevice			xDevice			= UnoRuntime.queryInterface(XDevice.class, loWindow);		
		
		// Convert font size from Points to Dialog Units
		Size szHeightPoints = new Size(fontptsize, fontptsize);
		Size szHeightPixels = xUnitConversion.convertSizeToPixel(szHeightPoints, MeasureUnit.POINT);
		Size szHeightDialog = xUnitConversion.convertSizeToLogic(szHeightPixels, MeasureUnit.APPFONT);
		
		fontDescriptor.Name = fontname;
		fontDescriptor.Height = (short) (szHeightDialog.Height);
		fontDescriptor.Width = (short) (szHeightDialog.Width);
		fontDescriptor.Weight = fontweight;
		
		xFont = xDevice.getFont(fontDescriptor);

/*
		int[][] charExtents = new int [1][100];
		xFont.getStringWidthArray(textstring, charExtents);
		
		for (int i = 0; i < charExtents[0].length; ++i )
			System.out.println(charExtents[0][i]);
		
		System.out.println("StringWidth = " + xFont.getStringWidth(textstring));
*/
		
		return xFont.getStringWidth(textstring);
	}

/*
	public static int getPaddedExtent (XModel xDoc, String textstring, String fontname, int fontptsize, float fontweight) {
		return getExtent (xDoc, textstring, fontname, fontptsize, fontweight) + xFont.getCharWidth('m');
	}
*/
	
	public static int getExtent (XModel xDoc, String textstring, String fontname, int fontptsize, float fontweight, int emnumber) {
		int textExtent = getExtent (xDoc, textstring, fontname, fontptsize, fontweight);
		
		if (emnumber <= 0)
			return textExtent;
		
		int padExtent = 0;
		for (int n=0; n<emnumber; ++n)
			padExtent += xFont.getCharWidth('m');
		
		return textExtent + padExtent;
	}
	
	public static FontDescriptor getFontDescriptor () {
		return fontDescriptor;
	}
}
