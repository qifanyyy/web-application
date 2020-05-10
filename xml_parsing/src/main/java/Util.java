import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;

class Util {
    private static Transformer TRANSFORMER;
    private static final int XML_HEADER_LEN = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>".length();

    static {
        try {
            TRANSFORMER = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Nullable
    static String getTextValueFromTagInElement(@NotNull Element element, @NotNull String tagName) {
        String ret = null;
        NodeList nodeList = element.getElementsByTagName(tagName);
        Element tagElement = (Element) nodeList.item(0);
        Node textNode;
        if (nodeList.getLength() > 0 && (textNode = tagElement.getFirstChild()) != null) {
            ret = textNode.getNodeValue();
        }
        return ret;
    }

    @Nullable
    static Integer getIntValueFromTagInElement(@NotNull Element element, @NotNull String tagName) {
        String strValue = getTextValueFromTagInElement(element, tagName);
        if (strValue == null || strValue.length() == 0) return null;
        return Integer.parseInt(strValue);
    }

    static Element getDocumentElementFromXmlUri(@NotNull String uri)
            throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(uri);
        return document.getDocumentElement();
    }

    static String nodeToXmlFormatString(@NotNull Node node) throws TransformerException {
        DOMSource source = new DOMSource(node);
        StreamResult result = new StreamResult(new StringWriter());
        TRANSFORMER.transform(source, result);
        return result.getWriter().toString().substring(XML_HEADER_LEN);
    }
}
