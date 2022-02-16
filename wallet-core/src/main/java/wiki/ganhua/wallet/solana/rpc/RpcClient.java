package wiki.ganhua.wallet.solana.rpc;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.ejlchina.okhttps.HTTP;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.fastjson.FastjsonMsgConvertor;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import wiki.ganhua.exception.RpcException;
import wiki.ganhua.util.RpcUtil;
import wiki.ganhua.wallet.RpcPar;

import java.util.concurrent.TimeUnit;

/**
 * solana rpc 请求
 *
 * @author Ganhua
 * @date 2022/2/15
 */
@Component
public class RpcClient {

    private static String[] rpcUrl;

    private static HTTP SOL_HTTP;

    private static volatile Integer RPC_NUMBER = 0;

    @Value("${solana.rpc-url}")
    public void setRpcUrl(String[] rpcUrl) {
        RpcClient.rpcUrl = rpcUrl;
        SOL_HTTP = setHttp(rpcUrl[RPC_NUMBER]);
    }

    private static HTTP setHttp(String url){
        return HTTP.builder().baseUrl(url)
                .bodyType("json")
                .addMsgConvertor(new FastjsonMsgConvertor())
                .config((OkHttpClient.Builder builder) -> {
                    builder.connectTimeout(10, TimeUnit.SECONDS);
                    builder.writeTimeout(10, TimeUnit.SECONDS);
                    builder.readTimeout(25, TimeUnit.SECONDS);
                })
                .build();
    }

    /**
     * sol rpc request
     * @return 不验证id id验证将考虑
     */
    public static <T> T call(RpcPar param, Class<T> clazz){
        HttpResult result = SOL_HTTP.sync("").addHeader("Content-Type", "application/json")
                .setBodyPara(param).post();
        RpcResponse<T> rpcResponse = JSONObject.parseObject(result.getBody().toString(), new TypeReference<>(clazz) {});
        if (!rpcResponse.getId().equals(param.getId().toString())){
            throw new RpcException("solana error danger","the interface return ID is different from the system incoming ID");
        }
        if (rpcResponse.getError() != null) {
            synchronized (RpcClient.class){
                RPC_NUMBER = RpcUtil.pollingNum(RPC_NUMBER,rpcUrl.length);
                SOL_HTTP = setHttp(rpcUrl[RPC_NUMBER]);
            }
            throw new RpcException("solana request exception",rpcResponse.getError().getMessage());
        }
        return rpcResponse.getResult();
    }
}
