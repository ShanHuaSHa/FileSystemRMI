package Server.Inter;

import Server.Impl.Id;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IFileManager extends Remote {
    IFile getFile(String id)throws RemoteException;
    IFile newFile(String id) throws RemoteException;
}