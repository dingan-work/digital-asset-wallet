package wiki.ganhua.wallet.arweave.model;

import cn.hutool.core.annotation.Alias;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.nimbusds.jose.util.Base64URL;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.crypto.signers.PSSSigner;
import wiki.ganhua.wallet.arweave.ArWalletApi;
import wiki.ganhua.wallet.arweave.util.CryptoUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Ar 交易bean
 *
 * @author Ganhua
 * @date 2022/2/22
 */
@Getter
@Setter
@NoArgsConstructor
public class ArTransaction {

    private static Random rand;

    static {
        try {
            rand = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private Integer format = 2;

    /**
     * 交易签名的sha256 hash
     */
    private String id;

    /**
     * 账户上一笔交易hash 防止重复交易
     */
    @JSONField(name = "last_tx")
    @Alias("last_tx")
    private String lastTx;

    /**
     * 发送方 JWK的 n模值 完整的base64str
     */
    private String owner;

    /**
     * 交易费用
     */
    private String reward;

    private String data = "";

    /**
     * 发送方标识符 相当于备注 但需要设置为json格式
     */
    private List<Tag> tags;

    /**
     * 目标地址的 SHA-256 hash 可以使用空字符串
     */
    private String target;

    /**
     * 金额 没有可使用空字符串
     */
    private String quantity;

    @JSONField(name = "data_size")
    @Alias("data_size")
    private String dataSize = "0";

    /**
     * 交易签名
     */
    private String signature;

    @JSONField(name = "data_root")
    @Alias("data_root")
    public String dataRoot = "";

    public ArTransaction(String toAddress, BigDecimal quantity,List<Tag> tags) {
        this.tags = Tag.tagsEncode(tags);
        this.target = toAddress;
        this.quantity = quantity.multiply(BigDecimal.TEN.pow(12)).stripTrailingZeros().toPlainString();
    }

    public String updateReward(BigDecimal price, long speedFactor){
        BigDecimal base = BigDecimal.valueOf(100);
        BigDecimal speed = new BigDecimal(speedFactor);
        return (price.multiply((base.add(speed)))).divide(base,0, RoundingMode.DOWN).toString();
    }

    public RSAPrivateCrtKeyParameters getPrivateKey(String privateKeyJson){
        JSONObject json = JSON.parseObject(privateKeyJson);
        BigInteger e = CryptoUtils.base64Dec(json.getString("e"));
        BigInteger n = CryptoUtils.base64Dec(json.getString("n"));
        BigInteger d = CryptoUtils.base64Dec(json.getString("d"));
        BigInteger p = CryptoUtils.base64Dec(json.getString("p"));
        BigInteger q = CryptoUtils.base64Dec(json.getString("q"));
        BigInteger dp = CryptoUtils.base64Dec(json.getString("dp"));
        BigInteger dq = CryptoUtils.base64Dec(json.getString("dq"));
        BigInteger qi = CryptoUtils.base64Dec(json.getString("qi"));
        return new RSAPrivateCrtKeyParameters(n,e,d,p,q,dp,dq,qi);
    }

    @SneakyThrows
    public void signature(String privateKeyJson){
        JSONObject json = JSON.parseObject(privateKeyJson);
        this.lastTx = ArWalletApi.syncGetTransactionAnchor();
        this.owner = new Base64URL(json.getString("n")).toString();
        this.reward = updateReward(ArWalletApi.getTransactionPrice(null,this.target),0);
        ArrayList<Object> dataList = new ArrayList<>();
        String formatStr = CryptoUtils.encode("" + this.format);
        dataList.add(formatStr);
        dataList.add(this.owner);
        dataList.add(this.target);
        String quantityStr = CryptoUtils.base64(this.quantity.getBytes());
        dataList.add(quantityStr);
        String rewardStr = CryptoUtils.base64(this.reward.getBytes());
        dataList.add(rewardStr);
        dataList.add(this.lastTx);
        dataList.add(Tag.tagValue(this.tags));
        String dataSizeStr = CryptoUtils.base64(this.dataSize.getBytes());
        dataList.add(dataSizeStr);
        if (this.dataRoot!=null){
            dataList.add(this.dataRoot);
        }
        byte[] signData = CryptoUtils.deepHash(dataList);
        byte[] signatureStr = sign(privateKeyJson, signData);
        this.id = CryptoUtils.base64(CryptoUtils.sha256(signatureStr));
        this.signature = CryptoUtils.base64(signatureStr);
    }

    @SneakyThrows
    public byte[] sign(String privateKeyJson,byte[] hash){
        JSONObject json = JSON.parseObject(privateKeyJson);
        RSAPrivateCrtKeyParameters privateKey = getPrivateKey(privateKeyJson);
        PSSSigner pssSigner = new PSSSigner(new RSAEngine(), new SHA256Digest(), 32);
        pssSigner.init(true, new ParametersWithRandom(privateKey));
        pssSigner.update(hash, 0, hash.length);
        byte[] result;
        try {
            result = pssSigner.generateSignature();
            RSAKeyParameters pub = new RSAKeyParameters(false,
                    CryptoUtils.base64Dec(json.getString("n")),
                    CryptoUtils.base64Dec(json.getString("e")));
            pssSigner.init(false, pub);
            pssSigner.update(hash, 0, hash.length);
            if(!pssSigner.verifySignature(result)){
                throw new SignatureException("signature verification failed");
            }
        } catch (Exception ex) {
            throw new SignatureException("unable to generate pss signature " + ex.getMessage(), ex);
        }
        return result;
    }

}
