package wiki.ganhua.wallet.arweave.rpc;

import cn.hutool.core.convert.Convert;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import com.ejlchina.okhttps.HTTP;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.SHttpTask;
import com.ejlchina.okhttps.fastjson.FastjsonMsgConvertor;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import wiki.ganhua.exception.RpcException;
import wiki.ganhua.util.RpcUtil;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AR相关rpc接口 // TODO: rpcutil可考虑将所有币种的整合一下
 *
 * @author Ganhua
 * @date 2022/2/18
 */
public class RpcClient {

    private static String[] rpcUrl;

    private static HTTP AR_HTTP;

    private static volatile Integer RPC_NUMBER = 0;

    @Value("${solana.rpc-url}")
    public static void setRpcUrl(String[] rpcUrl) {
        RpcClient.rpcUrl = rpcUrl;
        AR_HTTP = setHttp(rpcUrl[RPC_NUMBER]);
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
    public static <T> T call(Class<T> clazz, String url, String method, Map<String, Object> params){
        SHttpTask syncHttpTask = AR_HTTP.sync(url).addHeader("Content-Type", "application/json");
        if (method.equalsIgnoreCase(HTTP.GET) && params!=null){
            syncHttpTask.addUrlPara(params);
        }else{
            syncHttpTask.setBodyPara(params);
        }
        HttpResult result = syncHttpTask.request(method);
        if (!result.isSuccessful()) {
            synchronized (RpcClient.class){
                RPC_NUMBER = RpcUtil.pollingNum(RPC_NUMBER,rpcUrl.length);
                AR_HTTP = setHttp(rpcUrl[RPC_NUMBER]);
            }
            throw new RpcException("ar request exception",result.getStatus()+result.getBody().toString());
        }
        String res = result.getBody().toString();
        if (JSONValidator.from(res).validate()){
            return JSONObject.parseObject(res,clazz);
        }
        return Convert.convert(clazz,res);
    }

}
