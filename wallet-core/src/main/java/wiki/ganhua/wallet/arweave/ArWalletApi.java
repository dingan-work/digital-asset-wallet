package wiki.ganhua.wallet.arweave;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import com.ejlchina.okhttps.HTTP;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import wiki.ganhua.bean.WalletResult;
import wiki.ganhua.exception.CommonException;
import wiki.ganhua.exception.TransactionException;
import wiki.ganhua.wallet.arweave.model.ArTransaction;
import wiki.ganhua.wallet.arweave.model.ArWallet;
import wiki.ganhua.wallet.arweave.model.Tag;
import wiki.ganhua.wallet.arweave.rpc.ArBlockInfo;
import wiki.ganhua.wallet.arweave.rpc.ArTxStatus;
import wiki.ganhua.wallet.arweave.rpc.RpcClient;
import wiki.ganhua.wallet.arweave.util.CryptoUtils;

import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.List;

/**
 * ar wallet related API
 *
 * @author Ganhua
 * @date 2022/2/17
 */
@Component
@Slf4j
public class ArWalletApi{

    private static final String SC_OK_200_STR = "OK";

    @SneakyThrows
    public WalletResult createWalletAddress() {
        SecureRandom sr = new SecureRandom();
        int keySize = 4096;
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(new RSAKeyGenParameterSpec(keySize, ArWallet.PUBLIC_EXPONENT_USED_BY_ARWEAVE),sr);
        KeyPair kp = kpg.generateKeyPair();
        ArWallet arWallet = new ArWallet((RSAPublicKey) kp.getPublic(), (RSAPrivateCrtKey) kp.getPrivate());
        String privateKey = CryptoUtils.toJson(arWallet);
        return new WalletResult(arWallet.getAddress().toString(),privateKey);
    }

    public BigDecimal getBalance(String address) {
        return RpcClient.call(BigDecimal.class, "/wallet/" + address + "/balance", HTTP.GET, null);
    }

    public String sendTransaction(String toAddress, BigDecimal amount, String privateKey, String fromAddress) {
        List<Tag> tags = ListUtil.of(new Tag("Ganhua", "sendAr"), new Tag("time", DateUtil.today()));
        ArTransaction arTransaction = new ArTransaction(toAddress, amount,tags);
        arTransaction.signature(privateKey);
        String response = RpcClient.call(String.class, "/tx", HTTP.POST, BeanUtil.beanToMap(arTransaction));
        if (response.equals(SC_OK_200_STR)){
            // 查询接口是否成功
            String result = unconfirmedTx(arTransaction.getId());
            if(result.equals(SC_OK_200_STR)){
                return arTransaction.getId();
            }
            throw new TransactionException(result);
        }
        throw new TransactionException("transaction failed");
    }

    /**
     * get the last transaction ID of the address
     */
    public static String getLastTx(String address){
        String txId = RpcClient.call(String.class, "/wallet/" + address + "/last_tx", HTTP.GET, null);
        if (CharSequenceUtil.isBlank(txId)){
            return null;
        }
        return txId;
    }

    /**
     * 获取交易lastTx official recommendation
     */
    public static String syncGetTransactionAnchor(){
        String txId = RpcClient.call(String.class, "/tx_anchor", HTTP.GET, null);
        if (CharSequenceUtil.isBlank(txId)){
            return null;
        }
        return txId;
    }

    /**
     * get transaction fees price
     */
    public static BigDecimal getTransactionPrice(byte[] data, String target){
        String url = "/price/";
        if (data!=null){
            url += data.length;
        }
        url += 0+"/"+target;
        return RpcClient.call(BigDecimal.class, url, HTTP.GET, null);
    }

    /**
     *  query the current transaction status after sending the transaction
     */
    public static String unconfirmedTx(String txId) {
        String response = RpcClient.call(String.class, "/unconfirmed_tx/" + txId, HTTP.GET, null);
        if (JSONValidator.from(response).validate()) {
            JSONObject json = JSONObject.parseObject(response);
            if (json.getString("status") == null) {
                return SC_OK_200_STR;
            }
            return json.getString("error");
        }
        if ("pending".equals(response)){
            try {
                Thread.sleep(5000);
                unconfirmedTx(txId);
            } catch (InterruptedException e) {
                log.error("delayed query transaction status failed");
                unconfirmedTx(txId);
                Thread.currentThread().interrupt();
            }
        }
        return "transaction failed";
    }

    /**
     * query block information
     * @param inDepHash block independent hash
     */
    public static ArBlockInfo getBlockInfo(String inDepHash){
        return RpcClient.call(ArBlockInfo.class, "/block/hash/"+inDepHash, HTTP.GET, null);
    }

    public static ArTxStatus getStatus(String txId){
        String response = RpcClient.call(String.class, "/tx/" + txId + "/status", HTTP.GET, null);
        if (JSONValidator.from(response).validate()) {
            return JSONObject.parseObject(response, ArTxStatus.class);
        }
        throw new CommonException(response);
    }

}
