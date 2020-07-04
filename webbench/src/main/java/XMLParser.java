import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class XMLParser {

    private Document doc = null;

    public XMLParser(String xmlFile) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        doc = builder.parse(xmlFile);
        doc.getDocumentElement().normalize();
    }

    public Document getDocument() {
        return this.doc;
    }
}
