package Server.Impl;

import Server.Inter.IBlock;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import Server.Error.ErrorCode;

public class Block extends UnicastRemoteObject implements IBlock, Serializable {
    private int size = 1 << 3 ;
    private String id;
    private String bmId;
    private static final long serialVersionUID = 41L;

    private byte[] content;
    private BlockManager blockManager;
    private String checkSum;

    public String getBlockManagerId(){
        return this.bmId;
    }



    public boolean isValid(){
        CheckNum checkNum = new CheckNum();
        long checkCode = checkNum.getCheckCode(this.content);
        return this.checkSum.equals(checkCode+"");
    }


    public Block() throws RemoteException {
      super();
    };


    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setBmId(String bmId) {
        this.bmId = bmId;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setBlockManager(BlockManager blockManager) {
        this.blockManager = blockManager;
    }

    public String getCheckNum() {
        return checkSum;
    }

    public void setCheckNum(String checkNum) {
        this.checkSum = checkNum;
    }

    public void ini(String bmId, String id, BlockManager blockManager) {
        this.id = id;
        this.bmId = bmId;
        this.blockManager = blockManager;
        iniData();
    }


    //对block进行初始化
    private void iniData(){
        java.io.File file = new java.io.File("to/blockManager/" + this.getBmId());
        if (!file.exists()) {
            throw new ErrorCode(ErrorCode.NO_THIS_BLOCK_MANAGER_FAULT);
        }else {
            try {
                file.createNewFile();
            } catch (IOException e) {
//                e.printStackTrace();
                throw new ErrorCode(ErrorCode.CREATE_BLOCK_MANAGER_FAULT);
            }
        }

        String line;
        try {
            File file1 = new File("to/blockManager/" + this.getBmId() + "/" + id + ".meta");
            file1.createNewFile();
            BufferedReader br = new BufferedReader(new FileReader(file1));
            line = br.readLine();
            size = Integer.parseInt(line.substring(line.indexOf(":") + 1));
            line = br.readLine();
            br.close();

            checkSum = line.substring(line.indexOf(":") + 1);
            content = this.read();
        } catch (IOException e) {
//            e.printStackTrace();
            throw new ErrorCode(ErrorCode.CREATE_BLOCK_FAULT);
        }


    }




    public String getId(){
        return id;
    }

    @Override
    public Id getIndexId()  {
        return new Id(this.id);
    }

    public String getBmId() {
        return bmId;
    }

    @Override
    public BlockManager getBlockManager(){
        return this.blockManager;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Block block = (Block) o;
        return id.equals(block.id) && bmId.equals(block.bmId);
    }


    @Override
    public byte[] read() throws IOException {
        String path = "to/blockManager/" + getBlockManager().getId() + "/" + id + ".data";
        BufferedReader br = new BufferedReader(new FileReader(path));
        byte[] bytes = new byte[size];

        byte b;
        for (int i = 0 ;i < size;i++){
            if ((b = (byte) br.read()) != -1) {
                bytes[i] = b;
            }else {
                bytes[i] = 0x00;
            }
        }
        return bytes;
    }

    @Override
    public int blockSize(){
        return this.size;
    }
}
