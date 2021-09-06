package dlgutils;

import java.io.File;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ImageProc {
	String imageURL;
	
	public ImageProc (String resourcePath) {
		// Argument `resourcePath` requires a leading '/'
		
		try {
			URL url = this.getClass().getResource(resourcePath);
			if (url != null) {
				System.out.println("url NOT null!");
				// Use the resource we found...
				imageURL = url.toString();
			} else {
				System.out.println("url IS null!");
				// Otherwise, assume we're in an extension and use the appropriate protocol...
				// (see https://wiki.openoffice.org/wiki/Sidebar_for_Developers#Sidebar.xcu)
				imageURL = "vnd.sun.star.extension://" + getExtensionName() + resourcePath;
			}
		} catch (Exception e) {
			imageURL = "";
		}
		
		System.out.println(imageURL);
	}
	
	public String getURL () {
		return imageURL;
	}
	
	private String getExtensionName () throws Exception {

		// The following is an amalgam of info from
		// https://code2care.org/tutorial/read-and-parse-xml-file-using-java-dom-parser-tutorial
		//    and
		// https://stackoverflow.com/questions/11863038/how-to-get-the-attribute-value-of-an-xml-node-using-java#11863333
		File xmlFile = new File("description.xml");		// No need to check for existence; caller will catch on fail
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder =  documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(xmlFile);
		NodeList nodeList = document.getElementsByTagName("identifier");
		Node currentItem = nodeList.item(0);
		String extension_name = currentItem.getAttributes().getNamedItem("value").getNodeValue();
		
		return extension_name;
	}
}
