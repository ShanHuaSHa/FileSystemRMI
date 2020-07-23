package Server.Inter;

import Server.Impl.Id;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IBlockManager extends Remote {
    IBlock getBlock(Id indexIId) throws RemoteException;
    IBlock newBlock(String blockId,byte[] b) throws IOException , RemoteException;

    default IBlock newEmptyBlock(int blockSize) throws IOException, RemoteException {
        return null;
    }
}