package wiki.ganhua.wallet.arweave.util;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.PrimitiveArrayUtil;
import com.alibaba.fastjson.JSON;
import com.nimbusds.jose.util.Base64URL;
import lombok.SneakyThrows;
import wiki.ganhua.wallet.arweave.model.ArWallet;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * Ar加解密函数 工具
 * Ar encryption and decryption function tool
 * @author Ganhua
 * @date 2022/2/17
 */
public class CryptoUtils {

    public static byte[] toBigEndianBytes(BigInteger n){
        byte[] bytes = n.toByteArray();
        if (bytes.length > 1 && bytes[0] == (byte)0){
            return PrimitiveArrayUtil.remove(bytes,0);
        }
        return new byte[0];
    }

    public static String toJson(ArWallet wallet){
        Dict json = Dict.create().set("kty", "RSA")
                .set("e", encode(wallet.getPublicKey().getPublicExponent()))
                .set("n", encode(wallet.getPublicKey().getModulus()))
                .set("d", encode(wallet.getPrivateKey().getPrivateExponent()))
                .set("p", encode(wallet.getPrivateKey().getPrimeP()))
                .set("q", encode(wallet.getPrivateKey().getPrimeQ()))
                .set("dp", encode(wallet.getPrivateKey().getPrimeExponentP()))
                .set("dq", encode(wallet.getPrivateKey().getPrimeExponentQ()))
                .set("qi", encode(wallet.getPrivateKey().getCrtCoefficient()));
        return JSON.toJSONString(json);
    }

    public static byte[] decode(String originalInput) {
        return Base64.getUrlDecoder().decode(originalInput);
    }

    public static String base64(byte[] bytes){
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String encode(String byteStr){
        return Base64.getUrlEncoder().withoutPadding().encodeToString(byteStr.getBytes());
    }

    public static String encode(BigInteger bigInt){
        return Base64URL.encode(bigInt).toString();
    }

    public static BigInteger base64Dec(String base64){
        return new Base64URL(base64).decodeToBigInteger();
    }

    @SneakyThrows
    public static byte[] sha256(byte[] bytes){
        MessageDigest instance = MessageDigest.getInstance("SHA-256");
        instance.update(bytes);
        return instance.digest();
    }

    public static byte[] getSha384StrJava(byte[] data){
        MessageDigest messageDigest;
        byte[] encodeStr = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-384");
            messageDigest.update(data);
            encodeStr = messageDigest.digest();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encodeStr;
    }

    public static byte[] deepHashStr(String str) throws Exception {
        byte[] result;
        byte[] data = decode(str);
        String length = "" + data.length;
        String blob = "blob";
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.write(blob.getBytes());
        os.write(length.getBytes());
        byte[] tag = os.toByteArray();
        byte[] tagHash = getSha384StrJava(tag);
        byte[] dataHash = getSha384StrJava(data);
        os = new ByteArrayOutputStream();
        os.write(tagHash);
        os.write(dataHash);
        byte[] finalContent = os.toByteArray();
        result = getSha384StrJava(finalContent);
        return result;
    }

    public static byte[] deepHashChunk(List<Object> data, byte[] acc) throws  Exception{
        if (data.isEmpty()) {
            return acc;
        }
        byte[] result;
        byte[] dHash;
        if (data.get(0) instanceof String){
            String str = (String) data.get(0);
            dHash = deepHashStr(str);
        } else{
            Object[] value = (Object[]) data.get(0);
            ArrayList<Object> dData = new ArrayList<>(Arrays.asList(value));
            dHash = deepHash(dData);
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.write(acc);
        os.write(dHash);
        result = os.toByteArray();
        result = getSha384StrJava(result);
        List<Object> subData = data.subList(1,data.size());
        result = deepHashChunk(subData,result);
        return result;
    }


    public static byte[] deepHash(List<Object> data) throws Exception{
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String listSizeInString = "" + data.size();
        String key = "list";
        os.write(key.getBytes());
        os.write(listSizeInString.getBytes());
        byte[] tag = os.toByteArray();
        byte[] tagHash = getSha384StrJava(tag);
        return deepHashChunk(data,tagHash);
    }

}
