package Client.Inter;

import Client.Impl.MyBlock;

public interface MyIBlockManager {
    void addBlock(MyBlock block);
    void newBlock(MyBlock block);
    MyBlock getBlock(String id);
}
