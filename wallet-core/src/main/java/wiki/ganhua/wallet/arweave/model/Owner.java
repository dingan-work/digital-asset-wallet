package wiki.ganhua.wallet.arweave.model;

import cn.hutool.core.codec.Base64;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import wiki.ganhua.wallet.arweave.util.CryptoUtils;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;

/**
 * @author Ganhua
 * @date 2022/2/17
 */
@Getter
@Setter
@NoArgsConstructor
public class Owner {

    private byte[] bytes;

    private RSAPublicKey publicKey;

    /**
     * 初始化结构
     */
    @SneakyThrows
    public Owner(BigInteger n){
        this.bytes = CryptoUtils.toBigEndianBytes(n);
        this.publicKey = (RSAPublicKey)KeyFactory.getInstance("RSA").generatePublic(
                new RSAPublicKeySpec(n,ArWallet.PUBLIC_EXPONENT_USED_BY_ARWEAVE)
        );
    }

    @Override
    public String toString() {
        return Base64.encodeUrlSafe(this.bytes);
    }

}
