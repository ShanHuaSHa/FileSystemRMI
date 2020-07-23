package Server.Impl;

public class CheckNum {
     public long getCheckCode(byte[] bs){
         long h = 8;
         for (int i = 0;i < 8;i++){
             long lb = 0;
             if (bs.length > i) {
                 lb = (long) bs[i];
             }
             h += (h << 5) + lb + 10000;
         }
         return h;
    }

}
