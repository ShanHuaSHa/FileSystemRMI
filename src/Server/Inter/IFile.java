package Server.Inter;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface IFile extends Remote {


    String getFileId() throws RemoteException;
    ArrayList<String> getTempFile() throws RemoteException;

    void open(String fileManagerId, String fileId) throws RemoteException;
    void close(ArrayList<String> strings)  throws RemoteException;
    long size() throws RemoteException;
}

