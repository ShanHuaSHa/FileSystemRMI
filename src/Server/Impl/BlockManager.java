package Server.Impl;

import Server.RpcServer;
import Server.Error.ErrorCode;
import Server.Inter.IBlock;
import Server.Inter.IBlockManager;

import java.io.File;
import java.io.*;
import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Objects;

public class BlockManager extends UnicastRemoteObject implements IBlockManager, Remote {

    private String id;
    private ArrayList<Block> blockList = new ArrayList<Block>();
    private int blockSize = 8;


    public String getId(){
        return id;
    }

    public BlockManager(String id) throws RemoteException {
        super();
        this.id = "bm-" + id;
    }

    @Override
    public Block getBlock(Id indexIId) throws RemoteException {
        for (Block block:blockList){
            if (block.getIndexId()==indexIId)
                return block;
        }

        return null;
    }

    private void addBlock(Block block){
        blockList.add(block);
    }



    Block getBlock(String id) throws RemoteException {
        Block block = new Block();
        block.ini(this.id,id,this);
        return block;
    }


    @Override
    public Block newBlock(String id,byte[] b) throws ErrorCode {
        File metaFile = new File("to/blockManager/" + this.id + "/" + id + ".meta");
        File dataFile = new File("to/blockManager/" + this.id + "/" + id + ".data");

        try {
            if (metaFile.createNewFile()) {

                OutputStream os = new FileOutputStream(metaFile);
                PrintWriter pw = new PrintWriter(os);
                pw.println("size:" + blockSize);

                //校验码！！
                CheckNum checkNum = new CheckNum();
                long checkCode = checkNum.getCheckCode(b);
                pw.println("checkNum:" + checkCode);

                pw.close();
                os.close();
            }



            if (dataFile.createNewFile()) {
                OutputStream os1 = new FileOutputStream(dataFile);
                os1.write(b);
                os1.close();

                Block block = new Block();
                block.ini(this.id, id,this);
                this.addBlock(block);


                RpcServer.registry.bind("rmi://localhost:1099/" + this.id + "/" + id,block);;
                System.out.println("rmi://localhost:1099/" + this.id + "/" + id);
                return block;
            }
        } catch (IOException e) {
//            e.printStackTrace();
            throw new ErrorCode(ErrorCode.CREATE_BLOCK_FAULT);
        } catch (AlreadyBoundException e) {
//            e.printStackTrace();
            throw new ErrorCode(ErrorCode.REBIND_FAULT);
        }

        throw new ErrorCode(ErrorCode.CREATE_BLOCK_FAULT);
    }

    @Override
    public IBlock newEmptyBlock(int blockSize) {
        return null;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockManager that = (BlockManager) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
