package wiki.ganhua.util;

/**
 * @author Ganhua
 * @date 2022/2/16
 */
public class RpcUtil {

    /**
     * poll subscripts in the entire array length
     * @param rpcLen array length
     */
    public static int pollingNum(int originalNum,int rpcLen){
        if (originalNum>=rpcLen-1){
            originalNum = 0;
            return originalNum;
        }
        return ++originalNum;
    }

}
