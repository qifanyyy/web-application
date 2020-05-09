import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

class Util {
    static String getTextValueFromTagInElement(Element element, String tagName) {
        String ret = null;
        NodeList nodeList = element.getElementsByTagName(tagName);
        Element tagElement;
        Node textNode;
        if (nodeList.getLength() > 0 &&
                (tagElement = (Element) nodeList.item(0)) != null &&
                (textNode = tagElement.getFirstChild()) != null
        ) {
            ret = textNode.getNodeValue();
        }
        return ret;
    }

    static Integer getIntValueFromTagInElement(Element element, String tagName) {
        String strValue = getTextValueFromTagInElement(element, tagName);
        if (strValue == null || strValue.length() == 0) return null;
        return Integer.parseInt(strValue);
    }

    static Element getDocumentElementFromXmlUri(String uri) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(uri);
        return document.getDocumentElement();
    }
}
