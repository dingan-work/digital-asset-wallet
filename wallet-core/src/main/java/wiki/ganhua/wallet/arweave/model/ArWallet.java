package wiki.ganhua.wallet.arweave.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;

/**
 * AR 钱包类别
 *
 * @author Ganhua
 * @date 2022/2/17
 */
@Getter
@Setter
public class ArWallet {

    public static final BigInteger PUBLIC_EXPONENT_USED_BY_ARWEAVE = new BigInteger("65537");

    private Owner owner;

    private Address address;

    private byte[] asPKCS8;

    private RSAPublicKey publicKey;

    private RSAPrivateCrtKey privateKey;

    public ArWallet(RSAPublicKey pub, RSAPrivateCrtKey priv) {
        this.publicKey = pub;
        this.privateKey = priv;
        this.owner = new Owner(pub.getModulus());
        this.address = Address.ofKey(pub);
        this.asPKCS8 = priv.getEncoded();
    }
}
