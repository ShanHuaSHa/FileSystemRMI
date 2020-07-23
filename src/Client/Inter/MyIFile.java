package Client.Inter;


public interface MyIFile {
    int MOVE_CURR = 0;
    int MOVE_HEAD = 1;
    int MOVE_TAIL = 2;

    byte[] read(int length);
    void write(byte[] b);
    default long pos(){
        return move(0, MOVE_CURR);
    }
    long move(long offset, int where);
    void close() ;
    long getSize();
    void setSize(long newSize) ;


}
