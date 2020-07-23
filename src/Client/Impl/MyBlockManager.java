package Client.Impl;

import Client.RpcClient;
import Client.Inter.MyIBlockManager;
import java.io.*;
import java.rmi.NotBoundException;
import java.util.ArrayList;

import Client.Error.ErrorCode;
import Server.Inter.IBlockManager;

public class MyBlockManager implements MyIBlockManager {
    private String id;
    private ArrayList<MyBlock> blockList = new ArrayList<>();
    public String getId(){
        return id;
    }

    public MyBlockManager(String id) {
        super();
        this.id = id;
    }


    public void addBlock(MyBlock block){
        blockList.add(block);
    }


    public MyBlock getBlock(String id){
        for (MyBlock b : blockList){
            if (b.getId().equals(id)){
                return b;
            }
        }
        return null;
    }

    public void newBlock(MyBlock block) throws ErrorCode {
        try {
            IBlockManager blockManager = (IBlockManager) RpcClient.registry.lookup("rmi://localhost:1099/" + block.getBmId());
            blockManager.newBlock(block.getId(),block.getContent());
        } catch (IOException e) {
//            e.printStackTrace();
            throw new ErrorCode(ErrorCode.WRITE_FILE_FAULT);
        } catch (NotBoundException e) {
            e.printStackTrace();
            throw new ErrorCode(ErrorCode.BLOCK_MANAGER_NOT_ONLINE);
        }
    }


}
