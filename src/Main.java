import Server.Impl.*;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;


public class Main {
    public static void main(String[] args) throws IOException {

        CheckNum checkNum = new CheckNum();
        byte[] temp = {'5','4','3','2','1'};
        System.out.println(checkNum.getCheckCode(temp));


//        Block block1 = new Block();
//        block1.ini("01" ,"1");
//        Block block2 = new Block();
//        block2.ini("01" ,"1");
//
//        if (block1.equals(block2)){
//            System.out.println("22222");
//        }




//        HashMap<BlockManager,ArrayList<Block>> blockSystem = new HashMap<>();
//        ini_BlockSystem("to/blockManager",blockSystem);
//        print_blockSystem(blockSystem);

//        HashMap<FileManager,ArrayList<MyFile>> Server.Inter = new HashMap<>();
//        ini_FileSystem("to/fileManager",Server.Inter);
//        print_fileSystem(Server.Inter);


//        for (HashMap.Entry<FileManager, ArrayList<MyFile>> entry : Server.Inter.entrySet()) {
//            if (entry.getKey().getId().equals("1")){
//                System.out.println(entry.getKey().getId());
//                System.out.println(entry.getValue().get(0).getfileId());
//                print(entry.getValue().get(0).read(5));
//            }
//        }

//        Tool tool = new Tool();
//        tool.copyFile("to/blockManager/bm-01/1.meta","to/blockManager/bm-02/2.meta");

//        byte[] ss = new byte[2];
//        if (ss[0] == 0){
//            System.out.println("gogogo");
//        }


    }


    private static void print(byte[] temp){
        for (byte b : temp) {
            if (b != 0) {
                System.out.print((char) b);
            }else {
                System.out.print(0);
            }
        }
        System.out.println();
        System.out.println();

    }
    private static void print_blockSystem( HashMap<BlockManager,ArrayList<Block>> blockSystem){
        for (HashMap.Entry<BlockManager, ArrayList<Block>> entry : blockSystem.entrySet()) {
            System.out.println("blockManager's id is " + entry.getKey().getId());
            ArrayList<Block> temp = entry.getValue();
            System.out.println("它管理的block");
            for (Block b :temp){
                System.out.println(b.getId());
            }
            System.out.println();
        }
    }

    private static void print_fileSystem( HashMap<FileManager,ArrayList<File>> fileSystem) throws RemoteException {
        for (HashMap.Entry<FileManager, ArrayList<File>> entry : fileSystem.entrySet()) {
            System.out.println("fileManager's id is " + entry.getKey().getId());
            ArrayList<File> temp = entry.getValue();
            System.out.println("它管理的file:");
            for (File f :temp){
                System.out.println(f.getfileId());

//                print(f.read(5));
            }
            System.out.println();
        }
    }
}

