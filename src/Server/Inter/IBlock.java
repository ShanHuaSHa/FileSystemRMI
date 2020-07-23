package Server.Inter;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IBlock extends Remote {
    IId getIndexId()throws RemoteException;
    IBlockManager getBlockManager()throws RemoteException;
    byte[] read() throws IOException;
    int blockSize()throws RemoteException;

    boolean isValid () throws RemoteException;
    String getId() throws RemoteException;
    String getBlockManagerId() throws RemoteException;
}








