package Client.Impl;

import Client.RpcClient;
import Client.Inter.MyIFile;
import Server.Inter.IBlock;
import Server.Inter.IFile;

import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import Client.Error.ErrorCode;

import static Client.RpcClient.*;

public class MyFile implements MyIFile {
    private int index = 0;
    private String fileId;
    private String fileManagerId;
    private int blockSize;
    private long fileSize;
    private ArrayList<String> logicBlockList = new ArrayList<>();

    public String getFileId(){
        return fileId;
    }


    //判断请求块是否在内存中
    public boolean isMiss(String blockId){
        if (blockBuffer.size() == 0){
            return true;
        }

        for (MyBlock b : blockBuffer){
            if (b.getId().equals(blockId)){
                return false;
            }
        }
        return true;
    }


    //获取缓存中的块
    public MyBlock getBlock(String blockId) {
        for (MyBlock b : blockBuffer) {
            if (b.getId().equals(blockId)) {
                blockBuffer.remove(b);
                blockBuffer.add(b);
                return b;
            }
        }
        throw new ErrorCode(ErrorCode.CACHE_FAULT);
    }



    //更新缓存
    public void updateBuffer(MyBlock block){
        if (blockBuffer.size() == BUFFER_LENGTH){
            blockBuffer.remove(0);
        }

        //如果之前已经出现过
        for (MyBlock b : blockBuffer) {
            if (b.getId().equals(block.getId())) {
                blockBuffer.remove(b);
                blockBuffer.add(b);
                return;
            }
        }

        //不然就加在最后
        blockBuffer.add(block);
    }


    public ArrayList<String> getLogicBlockList() {
        return logicBlockList;
    }

    public long getFileSize() {
        return fileSize;
    }

    //将logic block转变成block manager和block的映射
    public HashMap<String,String> getMap(String logicBlock){
        HashMap<String,String> map = new HashMap<>();
        if (logicBlock == null || logicBlock.equals("")){
            return map;
        }

        String temp = logicBlock;

        while(!temp.equals("")) {
            map.put(temp.substring(temp.indexOf("[") + 1, temp.indexOf(",")),temp.substring(temp.indexOf(",") + 1, temp.indexOf("]")));
            temp = temp.substring(temp.indexOf("]") + 1);
        }
        return map;
    }


    private int getTotalNum(int length){
        int totalBlockNum = 0;

        if (index % blockSize != 0){
            if ((blockSize - index % blockSize) != 0 ){
                totalBlockNum++;
                length -= blockSize - index % blockSize;
            }
        }

        totalBlockNum += length / blockSize;

        if (length % blockSize != 0){
            totalBlockNum++;
        }

        return totalBlockNum;
    }

    //初始化
    public MyFile(ArrayList<String> metaFileList) {
        fileSize = Integer.parseInt(metaFileList.get(0).substring(metaFileList.get(0).indexOf(":") + 1));
        blockSize = Integer.parseInt(metaFileList.get(1).substring(metaFileList.get(1).indexOf(":") + 1));
        fileManagerId = metaFileList.get(2).substring(metaFileList.get(2).indexOf(":") + 1);
        fileId = metaFileList.get(3).substring(metaFileList.get(3).indexOf(":") + 1);

        for (int i = 4; i < metaFileList.size(); i++) {
            logicBlockList.add(metaFileList.get(i));
        }

    }


    //写操作
    public void write(byte[] bytes){
        RpcClient.getManager_block();//最开始时请求，确定上线的block manager

        //指针超过文件大小，将指针移到文件末尾
        if (index > fileSize){
            move(0,MOVE_TAIL);
        }

        int length = bytes.length;
        int beginBlock = index / blockSize;//index所在的block

        int totalBlockNum = getTotalNum(length);//总的需要修改的block数量


//        System.out.println("指针为：" + index + "; 写入长度为：" + length + "; 开始的block：" + beginBlock + "; 共操作的block数：" + totalBlockNum +"; ");

        HashMap<Integer, HashMap<String,String>> blocks = new HashMap<>();


        if (index + length > fileSize){//因为写入而使文件扩大时，先设置新的文件大小（主要是添加了新的行）
            setSize(index + length);
        }


        //得到需要更改的logic block
        for (int i = beginBlock;i < beginBlock + totalBlockNum;i++){
            String line = logicBlockList.get(i);
            String logicBlock = line.substring(line.indexOf(":") + 1);
            HashMap<String, String> blockMap = getMap(logicBlock);
            blocks.put(i,blockMap);
        }



        int byteNum = 0;
        int numBlock = 0;//成功创建的block数量

        for (HashMap.Entry<Integer, HashMap<String,String>> entry : blocks.entrySet()) {
            int random = (int) (Math.random() * 3 + 1);


            int count = 0;//本行logic block 成功写入的副本
//            System.out.println("行数" + entry.getKey());

            int temp = 0;//输入数组的游标

            while (random > 0){
                if (entry.getValue().size() == 0){
                    temp = writeBlock(numBlock,totalBlockNum,bytes,new byte[blockSize],byteNum,length,count,entry.getKey());
                    count++;
                    random--;
                    continue;
                }


                for (HashMap.Entry<String,String> entry1 : entry.getValue().entrySet()) {
//                    System.out.println(entry1.getKey() + "," + entry1.getValue());
                    String blockId = entry1.getValue();
                    try {
                        if (isMiss(blockId)) {//缓存未命中
                            if (isNotContained(blockManager, entry1.getKey())) {
                                continue;
                            }
                            IBlock block = (IBlock) registry.lookup("rmi://localhost:1099/" + entry1.getKey() + "/" + entry1.getValue());

                            if (block.isValid()) {
//                                MyBlock myBlock = new MyBlock(blockId, block.getBlockManagerId(), block.read());
                                temp = writeBlock(numBlock, totalBlockNum,bytes,block.read(),byteNum,length,count,entry.getKey());
                                break;
//                            System.out.println(entry1.getKey() + "," + entry1.getValue() + " 未命中");
                            }
//                        System.out.println(entry1.getKey() + "," + entry1.getValue() + " 文件损坏");
                        } else {
                            MyBlock block = getBlock(blockId);
                            temp = writeBlock(numBlock, totalBlockNum,  bytes,block.getContent(),byteNum,length,count,entry.getKey());
                            break;
//                        System.out.println(entry1.getKey() + "," + entry1.getValue() + "命中");
                        }

                    } catch (NotBoundException e) {
//                        e.printStackTrace();
                        throw new ErrorCode(ErrorCode.BLOCK_MANAGER_NOT_ONLINE);
                    } catch (IOException e) {
//                        e.printStackTrace();
                        throw new ErrorCode(ErrorCode.IO_EXCEPTION);
                    }
                }

                count++;
                random--;
            }


            byteNum += temp;
            numBlock++;
        }

        //对成功写入数据之后的更新（向服务器请求创建新的block文件）
        updateData();
    }


    //创建一个新的块，更新该处的logic block
    private void updateLogicBlockList(int count,String blockManagerId,String blockId,int row){
        if (count == 0){
            logicBlockList.remove(row);
            logicBlockList.add(row,row + ":[" + blockManagerId + "," + blockId + "]");
        }else {
            StringBuilder stringBuilder = new StringBuilder(logicBlockList.get(row));
            stringBuilder.append("[");
            stringBuilder.append(blockManagerId);
            stringBuilder.append(",");
            stringBuilder.append(blockId);
            stringBuilder.append("]");

            logicBlockList.remove(row);
            logicBlockList.add(row,stringBuilder.toString());
        }
    }


    private int writeBlock(int num,int total,byte[] src,byte[] old,int byteNum,int length,int count,int row){
        if (num == 0) {
            if (length <= blockSize - index % blockSize) {
                byteNum = creatBlock(src,old,byteNum,length,count,row,0);
            } else {
                byteNum = creatBlock(src,old,byteNum,blockSize - index % blockSize,count,row,0);
            }

        } else if (total - num == 1) {
            byteNum = creatBlock(src,old,byteNum,length - byteNum,count,row,1);
        } else {
            byteNum = creatBlock(src,old,byteNum,blockSize,count,row,2);
        }
        return byteNum;
    }

    private int creatBlock(byte[] src,byte[] old,int begin,int length,int count,int row,int flat){
        byte[] temp = new byte[blockSize];

        switch (flat){
            case 0:
                System.arraycopy(old,begin,temp,0,index % blockSize);
                System.arraycopy(src,begin,temp,index % blockSize,length);
                if (length + index % blockSize != blockSize){
                    System.arraycopy(old,index % blockSize + length,temp,index % blockSize + length,blockSize - index % blockSize - length);
                }
                break;
            case 1:
                System.arraycopy(src,begin % blockSize,temp,0,length);
                System.arraycopy(old,length,temp,length,blockSize - length);
                break;
            case 2:
                System.arraycopy(src,begin % blockSize,temp,0,length);
                break;
        }

        int random = (int)(Math.random() * blockManager.size());


        String blockManagerId = blockManager.get(random);
        String blockId = UUID.randomUUID().toString().replaceAll("-","");

        MyBlock myBlock = new MyBlock(blockId,blockManagerId,temp);

//        System.out.println(myBlock.toString());

        //将新建的块写入数组中，方便之后进行一次性进行缓存区的更换
        writeBlock.add(myBlock);

        //更新logic block
        updateLogicBlockList(count,blockManagerId,blockId,row);
        return length;
    }

    //向服务器创建block
    private void updateData(){
        for (MyBlock block : writeBlock){
//            System.out.println(block.getBmId());
            MyBlockManager myBlockManager = new MyBlockManager(block.getBmId());
            myBlockManager.newBlock(block);
        }


        //如果都成功，才更新本地buffer
        for (MyBlock block : writeBlock){
            updateBuffer(block);
        }

        //最后清除写缓存
        writeBlock.clear();
    }


//    void print(byte[] temp){
//        for (byte b : temp){
//            System.out.print((char)b);
//        }
//        System.out.println();
//    }


    //关闭文件时，更新meta文件。写操作中，如果没有关闭文件，则修改不生效
    public void close(){
        try {
            IFile file = (IFile) RpcClient.registry.lookup("rmi://localhost:1099/" + this.fileManagerId + "/" + this.fileId);
            ArrayList<String> temp = new ArrayList<>();
            temp.add("size:" + fileSize);
            temp.add("block size:" + blockSize);
            temp.add("manager:" + this.fileManagerId );
            temp.add("id:" + this.fileId);

            temp.addAll(logicBlockList);
            file.close(temp);
        } catch (NotBoundException e) {
//            e.printStackTrace();
            throw new ErrorCode(ErrorCode.FILE_MANAGER_NOT_ONLINE);
        } catch (RemoteException e) {
//            e.printStackTrace();
            throw new ErrorCode(ErrorCode.REMOTE_CALL_FAULT);
        }
    }

    @Override
    public long getSize() {
        return fileSize;
    }

    //设置文件大小
    public void setSize(long newSize){
        long blockCount = newSize / blockSize;

        if (newSize != 0 && newSize % blockSize != 0)
            blockCount++;

        int length = (int)newSize % blockSize;
        if (length == 0){
            length = 8;
        }

        byte[] bytes = new byte[length];
        boolean isOK = false;

        if (fileSize > newSize){//缩小
            for (int i = logicBlockList.size() - 1; i >= blockCount ;i--){
                logicBlockList.remove(i);
            }

            if (newSize % blockSize != 0){
                String lastLogicBlock = logicBlockList.get(logicBlockList.size() - 1);
                HashMap<String, String> blockMap = getMap(lastLogicBlock);

                if (blockMap.size() == 0) {
                    System.arraycopy(new byte[blockSize],0,bytes,0,length);
                    isOK = true;
                }

                for (HashMap.Entry<String,String> entry : blockMap.entrySet()) {
                    String blockId = entry.getValue();
                    try {
                        if (isMiss(blockId)) {//缓存未命中
                            if (isNotContained(blockManager, entry.getKey())) {
                                continue;
                            }
                            IBlock block = (IBlock) registry.lookup("rmi://localhost:1099/" + entry.getKey() + "/" + entry.getValue());

                            if (block.isValid()) {
                                isOK = true;
                                System.arraycopy(block.read(),0,bytes,0,length);
                                break;
    //                            System.out.println(entry1.getKey() + "," + entry1.getValue() + " 未命中");
                            }
    //                        System.out.println(entry1.getKey() + "," + entry1.getValue() + " 文件损坏");
                        } else {
                            MyBlock block = getBlock(blockId);
                            System.arraycopy(block.getContent(),0,bytes,0,length);
                            isOK = true;
                            break;
                        }
                    } catch (NotBoundException e) {
    //                        e.printStackTrace();
                        throw new ErrorCode(ErrorCode.BLOCK_MANAGER_NOT_ONLINE);
                    } catch (IOException e) {
    //                        e.printStackTrace();
                        throw new ErrorCode(ErrorCode.IO_EXCEPTION);
                    }
                }
            }
        }else {//扩大
            if (logicBlockList.size() == blockCount){//块数不变
                String lastLogicBlock = logicBlockList.get(logicBlockList.size() - 1);
                HashMap<String, String> blockMap = getMap(lastLogicBlock);

                int oldLength = (int)fileSize % blockSize;

                if (blockMap.size() == 0) {
                    System.arraycopy(new byte[blockSize],0,bytes,0,length);
                }

                for (HashMap.Entry<String,String> entry : blockMap.entrySet()) {
                    String blockId = entry.getValue();
                    try {
                        if (isMiss(blockId)) {//缓存未命中
                            if (isNotContained(blockManager, entry.getKey())) {
                                continue;
                            }
                            IBlock block = (IBlock) registry.lookup("rmi://localhost:1099/" + entry.getKey() + "/" + entry.getValue());

                            if (block.isValid()) {
                                System.arraycopy(block.read(),0,bytes,0,oldLength);
                                System.arraycopy(new byte[length - oldLength],0,bytes,oldLength,length - oldLength);
                                isOK = true;
                                break;
                                //                            System.out.println(entry1.getKey() + "," + entry1.getValue() + " 未命中");
                            }
                            //                        System.out.println(entry1.getKey() + "," + entry1.getValue() + " 文件损坏");
                        } else {
                            MyBlock block = getBlock(blockId);
                            System.arraycopy(block.getContent(),0,bytes,0,oldLength);
                            System.arraycopy(new byte[length - oldLength],0,bytes,oldLength,length - oldLength);
                            isOK = true;
                            break;
                        }
                    } catch (NotBoundException e) {
                        //                        e.printStackTrace();
                        throw new ErrorCode(ErrorCode.BLOCK_MANAGER_NOT_ONLINE);
                    } catch (IOException e) {
                        //                        e.printStackTrace();
                        throw new ErrorCode(ErrorCode.IO_EXCEPTION);
                    }
                }
            }
            for (int i = logicBlockList.size(); i < blockCount;i++){
                logicBlockList.add(i + ":");
            }
        }

        if (isOK) {//如果新建了block
            int random = (int) (Math.random() * blockManager.size());

            String blockManagerId = blockManager.get(random);
            String blockId = UUID.randomUUID().toString().replaceAll("-", "1");
            MyBlock myBlock = new MyBlock(blockId, blockManagerId, bytes);
            //将新建的块写入数组中，方便之后进行一次性进行缓存区的更换
            writeBlock.add(myBlock);

            //更新logic block
            updateLogicBlockList(0, blockManagerId, blockId, logicBlockList.size() - 1);

            updateData();
        }

        fileSize = newSize;
    }


    //移动文件指针
    public long move(long offset, int where) {
        if (where == 0){
            index = index + (int)offset;
        }else if (where == 1){
            index = (int) offset;
        }else {
            index = (int) fileSize + (int) offset;
        }

        if (index < 0){
            index = 0;
        }
        return index;
    }

    //读操作
    public byte[] read(int length){
        RpcClient.getManager_block();//向服务器请求确定上线的blockManager

        //指针已经超出文件大小
        if (index > fileSize){
            return new byte[length];
        }

        int beginBlock = index / blockSize;//index所在的block
        int count = 0;//成功读取的block数

        int totalBlockNum = getTotalNum(length);


        HashMap<Integer, HashMap<String,String>> blocks = new HashMap<>();
//        System.out.println("指针为：" + index + "; 读取长度为：" + length + "; 开始的block：" + beginBlock + "; 共读取的block数：" + totalBlockNum +"; ");


        int i = beginBlock;

        while(count < totalBlockNum) {
            if (i >= logicBlockList.size()){
                blocks.put(beginBlock + count,new HashMap<String, String>());
                count++;
                i++;
                continue;
            }

            String line = logicBlockList.get(i);
            String logicBlock = line.substring(line.indexOf(":") + 1);
            HashMap<String, String> blockMap = getMap(logicBlock);
            blocks.put(beginBlock + count,blockMap);
            count++;
            i++;
        }


        return read(blocks, RpcClient.registry,length);
    }


    private int copy(byte[] src,int byteNum,int length,byte[] bytes,int begin){
        System.arraycopy(src, begin, bytes, byteNum, length);
        return byteNum + length;
    }


    private int copyBlock(int num,int total,byte[] src,int byteNum,int length,byte[] bytes){
        if (num == 0) {
            if (length <= blockSize - index % blockSize) {
                byteNum = copy(src,byteNum,length,bytes,index % blockSize);
            } else {
                byteNum = copy(src,byteNum,blockSize - index % blockSize,bytes,index % blockSize);
            }
        } else if (total - num == 1) {
            byteNum = copy(src,byteNum,length - byteNum,bytes,0);
        } else {
            byteNum = copy(src,byteNum,blockSize,bytes,0);
        }
        return byteNum;
    }
    private byte[] read(HashMap<Integer, HashMap<String,String>> blocks, Registry registry,int length){
        int num = 0;
        int total = blocks.size();


        int byteNum = 0;
        byte[] bytes = new byte[length];

        for (HashMap.Entry<Integer, HashMap<String,String>> entry : blocks.entrySet()) {
            if (total == num){
                break;
            }
//            System.out.println("行号：" + entry.getKey());

            if (entry.getValue().size() == 0){
                byteNum = copyBlock(num,total,new byte[blockSize],byteNum,length,bytes);
                num++;
                continue;
            }

            boolean isOk = false;
            boolean isNotOnline = false;

            for (HashMap.Entry<String,String> entry1 : entry.getValue().entrySet()) {
//                System.out.println(entry1.getKey());
                String blockId = entry1.getValue();
                try {
                    if (isMiss(blockId)){//缓存未命中
                        //该block manager未上线（后面上线的manager我访问不到，先= =）
                        if (isNotContained(blockManager, entry1.getKey())){
                            isNotOnline = true;
                            continue;
                        }
                        IBlock block = (IBlock) registry.lookup("rmi://localhost:1099/" + entry1.getKey() + "/" + entry1.getValue());
                        if (block.isValid()) {
                            MyBlock myBlock = new MyBlock(blockId,block.getBlockManagerId(),block.read());
                            updateBuffer(myBlock);
                            byteNum = copyBlock(num,total,block.read(),byteNum,length,bytes);
//                            System.out.println(entry1.getKey() + "," + entry1.getValue() + " 未命中");
                            num++;
                            isOk = true;
                            break;
                        }
//                        System.out.println(entry1.getKey() + "," + entry1.getValue() + " 文件损坏");
                    }else {
                        MyBlock block = getBlock(blockId);
                        byteNum = copyBlock(num,total,block.getContent(),byteNum,length,bytes);
//                        System.out.println(entry1.getKey() + "," + entry1.getValue() + "命中");
                        num++;
                        isOk = true;
                        break;
                    }
                }
                catch (NotBoundException e) {//最开始上线，之后又下线了的block manager无法访问
                    throw new ErrorCode(ErrorCode.BLOCK_MANAGER_NOT_ONLINE);
                } catch (IOException e) {
                    throw new ErrorCode(ErrorCode.IO_EXCEPTION);
                }
            }

            //说明该logic block的所有block manager都没有上线
            if (isNotOnline){
                throw new ErrorCode(ErrorCode.BLOCK_MANAGER_NOT_ONLINE);
            }

            //说明该行logic block没有一个block的checkNum有效
            if (!isOk){
                throw new ErrorCode(ErrorCode.CHECKSUM_CHECK_FAILED);
            }
        }

        //读完之后，将指针进行移动
        move(length,MOVE_CURR);

        return bytes;
    }
}
