package Client.Impl;

import Client.Inter.MyIFileManager;
import Server.Inter.IFileManager;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Objects;

import static Client.RpcClient.registry;

public class MyFileManager implements MyIFileManager {

    private String id;
    private ArrayList<MyFile> fileList = new ArrayList<>();

    public MyFileManager(String id){
        this.id = id;
    }

    public String getId(){
        return this.id;
    }



    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


    public MyFile getFile(String fileId) {
        for (MyFile file:fileList){
            if (file.getFileId().equals(fileId)){
                return file;
            }
        }
        return null;
    }

    public MyFile newFile(String id){
        try {
            IFileManager fileManager = (IFileManager) registry.lookup("rmi://localhost:1099/" + this.id);
            fileManager.newFile(id);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
//        File newFile = new File();
//        newFile.open(this.id,id);
//        fileList.add(newFile);
        return null;
    }
}
