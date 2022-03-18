package wiki.ganhua.wallet.tron;

import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;
import wiki.ganhua.bean.WalletResult;
import wiki.ganhua.service.WalletService;
import wiki.ganhua.util.codec.Base58;
import wiki.ganhua.wallet.tron.crypto.SECP256K1;

/**
 * tron 接口相关 service
 *
 * @author Ganhua
 * @date 2022/2/16
 */
public abstract class BaseTronService implements WalletService {

    @Override
    public WalletResult createWalletAddress() {
        SECP256K1.KeyPair kp = SECP256K1.KeyPair.generate();
        SECP256K1.PublicKey pubKey = kp.getPublicKey();
        Keccak.Digest256 digest = new Keccak.Digest256();
        digest.update(pubKey.getEncoded(), 0, 64);
        byte[] raw = digest.digest();
        byte[] rawAdd = new byte[21];
        rawAdd[0] = 0x41;
        System.arraycopy(raw, 12, rawAdd, 1, 20);
        return new WalletResult(Base58.encode58Check(rawAdd),Hex.toHexString(kp.getPrivateKey().getEncoded()));
    }
}
