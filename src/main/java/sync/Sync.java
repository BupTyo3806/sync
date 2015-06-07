package sync;


import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Синхронизация двух папок.
 *
 * @author Aleksey Ivashin
 */
public class Sync {

    public static void main(String[] args) {
        try {
            ParseXML.Parse("config.xml");
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        String host = ParseXML.getContent("host");
        int port = Integer.parseInt(ParseXML.getContent("port"));
        Thread s = new Server(port);
        // запуск потока экземпляра класса Server
        s.start();
        Thread c = new Client(host, port);
        // запуск потока экземпляра класса Client
        c.start();
        /*Thread t = new Thread(new SyncFolder());
        t.start();
        System.out.println("Run!");*/
    }
}






