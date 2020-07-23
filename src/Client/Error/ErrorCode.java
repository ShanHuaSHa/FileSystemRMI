package Client.Error;

import java.util.HashMap;
import java.util.Map;

public class ErrorCode extends RuntimeException {
    public static final int IO_EXCEPTION = 1;
    public static final int CHECKSUM_CHECK_FAILED = 2;
    public static final int WRITE_FILE_FAULT = 3;
    public static final int REMOTE_CALL_FAULT = 4;
    public static final int FILE_MANAGER_NOT_ONLINE = 5;
    public static final int BLOCK_MANAGER_NOT_ONLINE = 6;
    public static final int CACHE_FAULT = 7;
    public static final int FILE_NOT_EXIST = 8;
    public static final int REBIND_FAULT = 9;
    public static final int TIMEOUT_FAULT = 10;



    public static final int UNKNOWN = 1000;


    private static final Map<Integer, String> ErrorCodeMap = new HashMap<>();
    static {
        ErrorCodeMap.put(IO_EXCEPTION, "IO错误");
        ErrorCodeMap.put(CHECKSUM_CHECK_FAILED, "block的check sum错误，文件受损");
        ErrorCodeMap.put(REMOTE_CALL_FAULT, "远程请求连接失败");
        ErrorCodeMap.put(WRITE_FILE_FAULT, "写操作失败");
        ErrorCodeMap.put(FILE_MANAGER_NOT_ONLINE, "你所访问的File管理者未上线");
        ErrorCodeMap.put(BLOCK_MANAGER_NOT_ONLINE, "你所访问的Block管理者未上线");
        ErrorCodeMap.put(FILE_NOT_EXIST, "访问一个不存在的文件");
        ErrorCodeMap.put(CACHE_FAULT, "本地缓存错误");
        ErrorCodeMap.put(REBIND_FAULT, "重复绑定错误");
        ErrorCodeMap.put(TIMEOUT_FAULT, "超时错误");

        ErrorCodeMap.put(UNKNOWN, "神秘错误");
    }

    public static String getErrorText(int errorCode) {
        return ErrorCodeMap.getOrDefault(errorCode, "invalid");
    }
    private int errorCode;

    public ErrorCode(int errorCode) {
        super(String.format("error code '%d' \"%s\"", errorCode, getErrorText(errorCode)));
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getMessage(){
        return String.format("error code '%d' \"%s\"", errorCode, getErrorText(errorCode));
    }
}
