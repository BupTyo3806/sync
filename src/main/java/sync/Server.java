package sync;

/**
 * Created by Alexey on 17.05.2015.
 */

import com.mongodb.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.rmi.*;
import java.rmi.registry.*;

/**
 * Класс реализует Server Socket.
 * Класс создан на основе класса Thread и для работы с ним необходимо перегрузить
 * метод run(), который выполняется при запуске потока.
 */
public class Server extends Thread {

    private int port;

    public Server(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        BufferedReader in = null;
        PrintWriter out = null;
        ServerSocket server = null;
        Socket fromclient = null;
        try {
            server = new ServerSocket(port);
            fromclient = server.accept();
            in = new BufferedReader(new InputStreamReader(
                    fromclient.getInputStream()));
            out = new PrintWriter(fromclient.getOutputStream(), true);
            String login = in.readLine();
            String password = in.readLine();

            Mongo mongo = new Mongo();
            DB db = mongo.getDB("java");
            DBCollection coll = db.getCollection("users");
            DBObject user = coll.findOne();
            if (user.get("login").equals(login) && user.get("password").equals(password)) {
                out.println(true);
            } else {
                out.println(false);
            }

            String input;
            while ((input = in.readLine()) != null) {
                if (input.equalsIgnoreCase("exit")) {
                    break;
                } else if (input.equalsIgnoreCase("sync")) {
                    /*Thread t = new Thread(new SyncFolder());
                    t.start();
                    System.out.println("Sync started!");*/
                    RunSync sync = new RunSyncImpl();
                    Registry reg = LocateRegistry.createRegistry(1099);
                    String serviceName = "TimeService";
                    reg.rebind(serviceName, sync);
                }

            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
                in.close();
                fromclient.close();
                server.close();
            } catch (IOException ex) {
                // ignore
            }
        }
    }
}

