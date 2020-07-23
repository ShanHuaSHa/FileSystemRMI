package Server.Impl;

import Server.Error.ErrorCode;
import Server.Inter.IFileManager;
import Server.RpcServer;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Objects;

public class FileManager extends UnicastRemoteObject implements IFileManager, Remote, Serializable {
    private String id;
    private ArrayList<File> fileList = new ArrayList<>();
    private static final long serialVersionUID = 43L;
    private int blockSize = 8;

    public FileManager(String id) throws RemoteException {
        super();
        this.id = id;
    }

    public String getId(){
        return this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileManager that = (FileManager) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public File getFile(String fileId) {
        for (File f : fileList){
            if (f.getFileId().equals(fileId)){
                return f;
            }
        }
        return null;
    }

    @Override
    public File newFile(String fileId) throws RemoteException {
        java.io.File file = new java.io.File("to/fileManager/fm-" + this.id + "/" + fileId + ".meta");
        File newFile = new File(this.id,fileId);
        try {
            if (file.createNewFile()){
                ArrayList<String> tempFile = new ArrayList<>();

                tempFile.add("size:0");
                tempFile.add("block size:" + blockSize);
                tempFile.add("manager:fm-" + this.id);
                tempFile.add("id:" + fileId);

                newFile.close(tempFile);

                newFile.open("fm-" + this.id,fileId);
                RpcServer.registry.bind("rmi://localhost:1099/fm-" + this.id + "/" + fileId,newFile);
                fileList.add(newFile);
            }
        } catch (IOException e) {
//            e.printStackTrace();
            throw new ErrorCode(ErrorCode.CREAT_FILE_FAULT);
        } catch (AlreadyBoundException e) {
//            e.printStackTrace();
            throw new ErrorCode(ErrorCode.REBIND_FAULT);
        }

        return newFile;
    }
}
