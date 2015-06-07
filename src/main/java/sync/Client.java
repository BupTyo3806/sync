package sync;

/**
 * Created by Alexey on 17.05.2015.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Класс реализует Client Socket.
 * Класс создан на основе класса Thread и для работы с ним необходимо перегрузить
 * метод run(), который выполняется при запуске потока.
 */
public class Client extends Thread {

    private String host;
    private int port;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        Socket fromserver = null;
        BufferedReader in = null;
        PrintWriter out = null;
        Scanner stdin = null;
        try {
            System.out.println("Connecting to " + host + ":" + port);

            fromserver = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(fromserver.getInputStream()));
            out = new PrintWriter(fromserver.getOutputStream(), true);
            stdin = new Scanner(System.in);
            String fuser, fserver;
            String login = ParseXML.getContent("login");
            String password = ParseXML.getContent("password");
            out.println(login);
            out.println(password);
            //System.out.println("Print sync or exit");
            while ((fuser = stdin.nextLine()) != null) {
                if (fuser.equalsIgnoreCase("sync")) {
                    out.println(fuser);
                } else
                if (fuser.equalsIgnoreCase("exit")) {
                    break;
                } else {
                    System.out.println("Print sync or exit");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
                in.close();
                stdin.close();
                fromserver.close();
            } catch (IOException ex) {
                // ignore
            }
        }
    }
}

