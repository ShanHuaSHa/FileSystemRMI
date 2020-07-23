package Client.Inter;

import Server.Impl.Id;
import Server.Inter.IFile;

import java.rmi.RemoteException;

public interface MyIFileManager {
    MyIFile getFile(String id);
    MyIFile newFile(String fileId);

}
