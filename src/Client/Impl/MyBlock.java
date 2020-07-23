package Client.Impl;

import Client.Inter.MyIBlock;

public class MyBlock implements MyIBlock {
    private String id;
    private int size;
    private String bmId;
    private byte[] content;


    public MyBlock(String id,String bmId,byte[] bytes){
        this.id = id;
        this.bmId = bmId;
        content = bytes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getBmId() {
        return bmId;
    }


    public byte[] getContent() {
        return content;
    }

    public String toString(){
        StringBuilder data = new StringBuilder();
        for (byte a : content){
            data.append((char) a);
        }
        return "bmID:" + this.bmId + "  id:" + this.id + "  data:" + data.toString();
    }

}