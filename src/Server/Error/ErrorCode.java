package Server.Error;

import java.util.HashMap;
import java.util.Map;

public class ErrorCode extends RuntimeException {
    public static final int IO_EXCEPTION = 1;
    public static final int READ_FiLE_FAULT = 2;
    public static final int CREATE_BLOCK_FAULT = 3;
    public static final int CREATE_BLOCK_MANAGER_FAULT = 4;
    public static final int CLOSE_FILE_FAULT = 5;
    public static final int REBIND_FAULT = 6;
    public static final int CREAT_FILE_FAULT = 7;
    public static final int NO_THIS_BLOCK_MANAGER_FAULT = 8;
    public static final int SERVER_FAULT = 9;
    public static final int OFFLINE_FAULT = 10;

    public static final int UNKNOWN = 1000;

    private static final Map<Integer, String> ErrorCodeMap = new HashMap<>();
    static {
        ErrorCodeMap.put(IO_EXCEPTION, "IO 错误");
        ErrorCodeMap.put(READ_FiLE_FAULT, "读取文件失败");
        ErrorCodeMap.put(CREATE_BLOCK_FAULT, "创建 block 失败");
        ErrorCodeMap.put(CREATE_BLOCK_MANAGER_FAULT, "创建 block 失败");
        ErrorCodeMap.put(NO_THIS_BLOCK_MANAGER_FAULT, "没有这个block manager");
        ErrorCodeMap.put(CLOSE_FILE_FAULT, "关闭文件失败");
        ErrorCodeMap.put(REBIND_FAULT, "重复绑定");
        ErrorCodeMap.put(CREAT_FILE_FAULT, "创建文件失败");
        ErrorCodeMap.put(SERVER_FAULT, "服务器启动失败");
        ErrorCodeMap.put(OFFLINE_FAULT, "服务下线失败");

        ErrorCodeMap.put(UNKNOWN, "神秘错误！");
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
