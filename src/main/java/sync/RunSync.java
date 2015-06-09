package sync;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Alexey on 09.06.2015.
 */
public interface RunSync extends Remote {

    public void Start() throws RemoteException;
}
