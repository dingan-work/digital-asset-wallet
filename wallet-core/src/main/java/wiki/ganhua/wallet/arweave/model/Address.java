package wiki.ganhua.wallet.arweave.model;

import cn.hutool.core.codec.Base64Encoder;
import com.nimbusds.jose.util.Base64URL;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import wiki.ganhua.wallet.arweave.util.CryptoUtils;

import java.math.BigInteger;
import java.security.interfaces.RSAKey;

/**
 * @author Ganhua
 * @date 2022/2/17
 */
@Getter
@Setter
@NoArgsConstructor
public class Address {

    private final int length = 32;

    private byte[] bytes;

    public Address(byte[] bytes){
        this.bytes = bytes;
    }

    @SneakyThrows
    public static Address ofModulus(BigInteger n){
       return new Address(CryptoUtils.sha256(Base64URL.encode(n).decode()));
    }

    public static Address ofKey(RSAKey k){
        return ofModulus(k.getModulus());
    }

    @Override
    public String toString() {
        return Base64Encoder.encodeUrlSafe(this.bytes);
    }
}
