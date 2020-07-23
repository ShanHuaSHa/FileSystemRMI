package Client;

import Client.Impl.MyFileManager;
import Server.Inter.IFile;
import Client.Impl.MyBlock;
import Client.Impl.MyFile;


import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import Client.Error.ErrorCode;


public class RpcClient {
    public static ArrayList<String> fileManager = new ArrayList<>();
    public static ArrayList<String> blockManager = new ArrayList<>();
    public static ArrayList<String> fileList = new ArrayList<>();
    public static ArrayList<MyBlock> writeBlock = new ArrayList<>();

    public static Registry registry;
    static {
        try {
            System.setProperty("sun.rmi.transport.tcp.responseTimeout", "500");
            registry = LocateRegistry.getRegistry("localhost");

            getManager_block();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }



    public static void getManager_block(){
        String [] s = new String[0];
        try {
            s = registry.list();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        for (String ss :s){
            if (ss.contains("fm")){
                String temp = ss.substring(ss.lastIndexOf("f"));
                if (temp.contains("/"))
                    fileList.add(temp.substring(temp.lastIndexOf("/") + 1));
                else
                    fileManager.add(temp);
            }

            if (ss.contains("bm-")){
                String temp = ss.substring(ss.lastIndexOf("bm-"));
                if (!temp.contains("/"))
                    blockManager.add(temp);
            }
        }
    }


    public static ArrayList<MyBlock> blockBuffer = new ArrayList<>();
    public static int BUFFER_LENGTH = 5;


    public static boolean isNotContained(ArrayList<String> a,String s){
        for (String aa :a){
            if (s.equals(aa)){
                return false;
            }
        }
        return true;
    }

    public static void main(String args[]) {

//        testRead("fm-1","1");//测试读取文件
//        testRead("fm-2","3");//测试读取文件


//        int length = 8;
//        byte[] bytes = new byte[length];
//        for (int i = 0; i < bytes.length;i++){
//            bytes[i] = 'a';
//        }
//        testWrite("fm-2","3",bytes);//测试写已经有了的文件
//        testWrite("fm-3","5",bytes);//测试写未有的文件
    }

    private static void testWrite(String fileManagerId,String fileId,byte[] bytes){
        try {
            if (isNotContained(fileManager, fileManagerId)){
                throw new ErrorCode(ErrorCode.FILE_MANAGER_NOT_ONLINE);
            }


            //服务器端不存在对应的文件，需要新建meta文件。
            if (isNotContained(fileList,fileId)){
                //先在本地写好对应的block对象，之后再请求服务器端的fileManager对象，新建file的meta文件。
                MyFileManager myFileManager = new MyFileManager(fileManagerId);
                myFileManager.newFile(fileId);
            }

            // 再根据在线的blockManager，请求服务，新建对应的block文件。
            IFile fm = (IFile) registry.lookup("rmi://localhost:1099/" + fileManagerId + "/" + fileId);
            ArrayList<String> metaFileList = fm.getTempFile();
            MyFile file = new MyFile(metaFileList);

//            file.setSize(6);//缩小
//            file.setSize(50);//放大

            file.move(8,file.MOVE_HEAD);
            file.write(bytes);

            file.move(0,file.MOVE_HEAD);
            print(file.read((int) file.getSize()));

            file.close();
        }
        catch (NotBoundException e) {
//            e.printStackTrace();
            throw new ErrorCode(ErrorCode.WRITE_FILE_FAULT);
        }
        catch (RemoteException e) {
//            e.printStackTrace();
            throw new ErrorCode(ErrorCode.TIMEOUT_FAULT);
        }
    }

    private static void testRead(String fileManagerId,String fileId){
        try {
            if (isNotContained(fileManager, fileManagerId)){
                throw new ErrorCode(ErrorCode.FILE_MANAGER_NOT_ONLINE);
            }

            // 从Registry中检索远程对象的存根/代理
            IFile fm1_1 = (IFile) registry.lookup("rmi://localhost:1099/" + fileManagerId + "/" + fileId);
            ArrayList<String> metaFileList = fm1_1.getTempFile();
            MyFile file = new MyFile(metaFileList);


//            file.setSize(24);
//            file.move(9, MyFile.MOVE_HEAD);
            print(file.read(6));
            print(file.read(20));
            print(file.read(22));
            file.close();
        }
        catch (NotBoundException e) {
//            e.printStackTrace();
            throw new ErrorCode(ErrorCode.FILE_NOT_EXIST);
        }
        catch (RemoteException e) {
//            e.printStackTrace();
            throw new ErrorCode(ErrorCode.TIMEOUT_FAULT);
        }
    }

    private static void print(byte[] temp){
        for (byte b : temp) {
            if (b != 0) {
                System.out.print((char) b);
            } else {
                System.out.print(0);
            }
        }
        System.out.println();
    }

}

