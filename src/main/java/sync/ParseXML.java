package sync;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Created by Alexey on 02.05.2015.
 */
public class ParseXML {
    public static String path;

    private static Document doc;


    public static void Parse(String path) throws ParserConfigurationException, IOException, SAXException {
        File file = new File(path);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        doc = db.parse(file);
        doc.getDocumentElement().normalize();
    }
    
    public static String getContent(String tag) {
        return doc.getElementsByTagName(tag).item(0).getTextContent();
    }
}

