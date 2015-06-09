package sync;

import java.rmi.*;
import java.rmi.server.*;
/**
 * Created by Alexey on 09.06.2015.
 */
public class RunSyncImpl extends UnicastRemoteObject implements RunSync {

    public RunSyncImpl() throws RemoteException {
        super();
    }

    @Override
    public void Start() throws RemoteException {
        Thread t = new Thread(new SyncFolder());
        t.start();
        System.out.println("Sync started!");
    }
}
