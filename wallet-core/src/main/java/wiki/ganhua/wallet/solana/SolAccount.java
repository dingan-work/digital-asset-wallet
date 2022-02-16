package wiki.ganhua.wallet.solana;

import org.bitcoinj.crypto.*;
import wiki.ganhua.wallet.solana.model.PublicKey;
import wiki.ganhua.wallet.solana.util.TweetNaclFast;

import java.util.List;

/**
 * @author Ganhua
 * @date 2022/2/14
 */
public class SolAccount {
    private final TweetNaclFast.Signature.KeyPair keyPair;

    public SolAccount() {
        this.keyPair = TweetNaclFast.Signature.keyPair();
    }

    public SolAccount(byte[] secretKey) {
        this.keyPair = TweetNaclFast.Signature.keyPair_fromSecretKey(secretKey);
    }

    private SolAccount(TweetNaclFast.Signature.KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    public static SolAccount fromMnemonic(List<String> words, String passphrase) {
        byte[] seed = MnemonicCode.toSeed(words, passphrase);
        DeterministicKey masterPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed);
        DeterministicHierarchy deterministicHierarchy = new DeterministicHierarchy(masterPrivateKey);
        DeterministicKey child = deterministicHierarchy.get(HDUtils.parsePath("M/501H/0H/0/0"), true, true);
        TweetNaclFast.Signature.KeyPair keyPair = TweetNaclFast.Signature.keyPair_fromSeed(child.getPrivKeyBytes());
        return new SolAccount(keyPair);
    }

    public PublicKey getPublicKey() {
        return new PublicKey(keyPair.getPublicKey());
    }

    public byte[] getSecretKey() {
        return keyPair.getSecretKey();
    }
}
