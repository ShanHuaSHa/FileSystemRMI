package Server;

import java.io.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;


import Server.Impl.Block;
import Server.Impl.BlockManager;
import Server.Impl.File;
import Server.Impl.FileManager;

/**
 * 服务器端初始化文件和块管理者及实体
 */
public class IniTool {
    public void ini_BlockSystem(String path, HashMap<BlockManager,ArrayList<Block>> blockSystem) throws RemoteException {
        java.io.File file = new java.io.File(path);
        java.io.File[] files = file.listFiles();
        if (file.exists() && files != null &&files.length > 0) {
            if (null == file.listFiles()) {
                return;
            }

            for (java.io.File f : files) {
                if (f.isDirectory()){
                    BlockManager blockManager = new BlockManager(f.getName().substring(3));
                    ArrayList<Block> blocks = new ArrayList<>();
                    blockSystem.put(blockManager,blocks);

                    for (java.io.File f1 : f.listFiles()){
                        String pa = f1.getAbsolutePath();
                        String managerId = pa.substring(0,pa.lastIndexOf("\\"));
                        managerId = managerId.substring(managerId.lastIndexOf("\\") + 1);
                        String blockId = pa.substring(pa.lastIndexOf("\\") + 1,pa.lastIndexOf("."));

                        Block block = new Block();
                        block.ini(managerId,blockId,blockManager);


                        if (blockSystem.get(blockManager).size() == 0){
                            blockSystem.get(blockManager).add(block);
                        }else {
                            boolean isExist = false;
                            for (Block b : blockSystem.get(blockManager)){
                                if (b.getBmId().equals(managerId) && b.getId().equals(blockId))
                                    isExist = true;
                            }

                            if (!isExist)
                                blockSystem.get(blockManager).add(block);
                        }
                    }
                }
            }

        }
    }


    void ini_FileSystem(String path, HashMap<FileManager,ArrayList<File>> fileSystem) throws RemoteException {
        java.io.File file = new java.io.File(path);
        java.io.File[] files = file.listFiles();
        if (file.exists() && files != null &&files.length > 0) {
            if (null == file.listFiles()) {
                return;
            }

            for (java.io.File f : files) {
                if (f.isDirectory()){
                    FileManager fileManager = new FileManager(f.getName().substring(3));
                    ArrayList<File> tempFiles = new ArrayList<>();
                    fileSystem.put(fileManager,tempFiles);

//                    System.out.println(f.getName());
                    for (java.io.File f1 : f.listFiles()){
                        String pa = f1.getAbsolutePath();
                        String managerId = pa.substring(0,pa.lastIndexOf("\\"));
                        managerId = managerId.substring(managerId.lastIndexOf("\\") + 1);

                        String fileId = pa.substring(pa.lastIndexOf("\\") + 1,pa.lastIndexOf("."));
                        File file1 = new File(managerId,fileId);
                        file1.open(managerId,fileId);
                        fileSystem.get(fileManager).add(file1);
//                        System.out.println(f1.getName());
                    }
                }
            }

        }
    }


    public void copyFile(String sourceFile,String targetFile) {
        FileReader fr=null;
        FileWriter fw=null;
        try {
            fr=new FileReader(sourceFile);
            fw=new FileWriter(targetFile);
            int len=0;
            while((len=fr.read())!=-1)
            {
                fw.write((char)len);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally
        {
            if(fr!=null)
            {
                try {
                    fr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(fw!=null)
            {
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }




}
