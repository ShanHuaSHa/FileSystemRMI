package Server.Impl;

import Server.Error.ErrorCode;
import Server.Inter.IFile;
import Server.Inter.IId;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;


public class File extends UnicastRemoteObject implements IFile,Serializable{
    private String fileId;
    private String fileManagerId;
    private int blockSize;
    private long fileSize;
    private String metaPath;

    private static final long serialVersionUID = 42L;

    public File(String fileManagerId, String fileId) throws RemoteException {
        super();
        metaPath = "to/fileManager/fm-" + fileManagerId + "/" + fileId + ".meta";
    }


    public String getMetaPath(){return metaPath;}


    public void open(String fileManagerId, String fileId) throws RemoteException {
        this.fileManagerId = fileManagerId;
        this.fileId = fileId;
        metaPath = "to/fileManager/" + fileManagerId + "/" + fileId + ".meta";

        try {
            BufferedReader br = new BufferedReader(new FileReader(metaPath));
            String line;
            line = br.readLine();
            fileSize = Integer.parseInt(line.substring(line.indexOf(":")+1));
            line = br.readLine();
            blockSize = Integer.parseInt(line.substring(line.indexOf(":")+1));
        }catch (IOException e){
//            System.out.println("fail");
//            e.printStackTrace();
            throw new ErrorCode(ErrorCode.READ_FiLE_FAULT);
        }
    }


    @Override
    public String getFileId() {
        return fileId;
    }


    public String getfileId(){
        return this.fileId;
    }



    int getFileSize() {
        return (int) fileSize;
    }

    int getBlockSize(){
        return blockSize;
    }

    public ArrayList<String> getTempFile() throws ErrorCode {
//        try {
//            Thread.sleep(4000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


        ArrayList<String> tempFile = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(metaPath));
            String line;
            while((line = br.readLine()) != null){
                tempFile.add(line);
            }
            br.close();
        }catch (IOException e){
            throw new ErrorCode(ErrorCode.READ_FiLE_FAULT);
        }
        return tempFile;
    }


    @Override
    public void close(ArrayList<String> arrayList) {
//        java.io.File file = new java.io.File(metaPath);

        PrintWriter pw = null;
//        RandomAccessFile randomAccessFile ;
            try {
                pw = new PrintWriter(new FileWriter(metaPath));
//                randomAccessFile = new RandomAccessFile(new java.io.File(metaPath),"rwd");
//            pw.println("size:" + fileSize);
//            pw.println("block size:" + blockSize);
//            pw.println("logic block:");

                for (int i = 0;i < arrayList.size();i++){
                    if (i + 1 == arrayList.size()){
                        pw.print(arrayList.get(i));
                    }else {
                        pw.println(arrayList.get(i));
                    }
                }
                pw.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new ErrorCode(ErrorCode.CLOSE_FILE_FAULT);
        }
    }

    @Override
    public long size() {
        return fileSize;
    }




}
