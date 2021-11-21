/* ImageProc.java -- part of LOCommonDialogs
 * 
 * LOCommonDialogs - Dialogs for LibreOffice providing commonly needed functionality
 * Copyright © 2016-2018, 2021 David Yockey
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package dlgutils;

// ImageProc - Image resource path processor for getting a URL to the image resource

import java.net.URL;

public class ImageProc {
	private static String EXTENSION_IDENTIFIER = "";
	private String imageURL;
	
	public ImageProc (String extid) {
		EXTENSION_IDENTIFIER = extid;
	}
	
	public ImageProc () {
	}
	
	public String getURL (String resourcePath) {
		// IMPORTANT: Argument `resourcePath` requires a leading '/'
		
		try {
			URL url = this.getClass().getResource(resourcePath);
			if (url != null) {
				// Assume we're not in an extension and use the resource we found...
				imageURL = url.toString();
			} else {
				// Otherwise, assume we're in an extension and use the appropriate protocol...
				// (see https://wiki.openoffice.org/wiki/Sidebar_for_Developers#Sidebar.xcu)
				imageURL = "vnd.sun.star.extension://" + EXTENSION_IDENTIFIER + resourcePath;
			}
		} catch (Exception e) {
			// No problem if we end up here... We'll just not get an icon in a dialog or whatever.
			imageURL = "";
		}
		
		return imageURL;
	}
}
