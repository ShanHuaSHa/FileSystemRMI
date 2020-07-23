package Server;

import Server.Error.ErrorCode;
import Server.Impl.Block;
import Server.Impl.BlockManager;
import Server.Impl.File;
import Server.Impl.FileManager;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;

public class RpcServer {
    public static Registry registry;
    static {
        try {
            //超时设置
            LocateRegistry.createRegistry(1099);
            registry = LocateRegistry.getRegistry();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        try {
            IniTool tool = new IniTool();
            //服务器开启，初始化所有的fileManager和blockManager
            HashMap<BlockManager,ArrayList<Block>> blockSystem = new HashMap<>();
            tool.ini_BlockSystem("to/blockManager",blockSystem);

            HashMap<FileManager,ArrayList<File>> fileSystem = new HashMap<>();
            tool.ini_FileSystem("to/fileManager",fileSystem);
            System.out.println("文件系统初始化成功");

            for (HashMap.Entry<FileManager, ArrayList<File>> entry : fileSystem.entrySet()) {
                registry.bind("rmi://localhost:1099/fm-" + entry.getKey().getId(),entry.getKey());
                System.out.println("rmi://localhost:1099/fm-" + entry.getKey().getId()+ "已上线");
                for (File f : entry.getValue()) {
                    registry.bind("rmi://localhost:1099/fm-" + entry.getKey().getId() + "/" + f.getfileId(), f);
                    System.out.println("rmi://localhost:1099/fm-" + entry.getKey().getId() + "/" + f.getfileId() + "已上线");
                }
            }


            for (HashMap.Entry<BlockManager, ArrayList<Block>> entry : blockSystem.entrySet()) {
                registry.bind("rmi://localhost:1099/" + entry.getKey().getId(), entry.getKey());
                System.out.println("rmi://localhost:1099/" + entry.getKey().getId()+ "已上线");
                for (Block b : entry.getValue()) {
                    registry.bind("rmi://localhost:1099/" + entry.getKey().getId() + "/" + b.getId(), b);
                    System.out.println("rmi://localhost:1099/" + entry.getKey().getId() + "/" + b.getId()+ "已上线");
                }
            }

            System.out.println("AlphaFileSystem 绑定成功！");


            //测试部分管理者下线
//            try {
//                Thread.sleep(4000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            registry.unbind("rmi://localhost:1099/bm-01");
//            registry.unbind("rmi://localhost:1099/bm-02");
//            System.out.println("bm-01和bm-02已下线");
//
//
//
//            try {
//                Thread.sleep(4000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            registry.unbind("rmi://localhost:1099/fm-1");
//            System.out.println("fm-1已下线");
//
//
//
//            //重新上线
//            try {
//                Thread.sleep(4000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            for (HashMap.Entry<FileManager, ArrayList<File>> entry : fileSystem.entrySet()) {
//                if (entry.getKey().getId().equals("1")){
//                    registry.bind("rmi://localhost:1099/fm-" + entry.getKey().getId(),entry.getKey());
//                    System.out.println("rmi://localhost:1099/fm-" + entry.getKey().getId()+ "已上线");
//                }
//            }
//
//            for (HashMap.Entry<BlockManager, ArrayList<Block>> entry : blockSystem.entrySet()) {
//                if (entry.getKey().getId().equals("bm-01") || entry.getKey().getId().equals("bm-02")){
//                    registry.bind("rmi://localhost:1099/" + entry.getKey().getId(),entry.getKey());
//                    System.out.println("rmi://localhost:1099/" + entry.getKey().getId()+ "已上线");
//                }
//            }


        } catch (RemoteException e) {
            System.out.println("创建远程对象发生异常！");
//            e.printStackTrace();
            throw new ErrorCode(ErrorCode.SERVER_FAULT);

        } catch (AlreadyBoundException e) {
            System.out.println("发生重复绑定对象异常！");
//            e.printStackTrace();
            throw new ErrorCode(ErrorCode.REBIND_FAULT);
        }
//        catch (NotBoundException e) {
////            e.printStackTrace();
//            throw new ErrorCode(ErrorCode.OFFLINE_FAULT);
//        }
//        catch (MalformedURLException e) {
//            System.out.println("发生URL畸形异常！");
//            e.printStackTrace();
//        }
    }
}
