package wiki.ganhua.wallet.solana.model;

import org.bitcoinj.core.Base58;
import wiki.ganhua.wallet.solana.SolAccount;
import wiki.ganhua.wallet.solana.rpc.SolMessage;
import wiki.ganhua.wallet.solana.rpc.TransactionInstruction;
import wiki.ganhua.wallet.solana.util.ShortvecEncoding;
import wiki.ganhua.wallet.solana.util.TweetNaclFast;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * sol交易
 * @author Ganhua
 */
public class SolTransaction {

    public static final int SIGNATURE_LENGTH = 64;

    private final SolMessage messgae;
    private final List<String> signatures;
    private byte[] serializedMessage;
    private PublicKey feePayer;

    public SolTransaction() {
        this.messgae = new SolMessage();
        this.signatures = new ArrayList<String>();
    }

    public SolTransaction addInstruction(TransactionInstruction instruction) {
        messgae.addInstruction(instruction);
        return this;
    }

    public void setRecentBlockHash(String recentBlockhash) {
        messgae.setRecentBlockHash(recentBlockhash);
    }

    public void setFeePayer(PublicKey feePayer) {
        this.feePayer = feePayer;
    }

    public void sign(SolAccount signer) {
        sign(Collections.singletonList(signer));
    }

    public void sign(List<SolAccount> signers) {
        if (signers.size() == 0) {
            throw new IllegalArgumentException("No signers");
        }
        if (feePayer == null) {
            feePayer = signers.get(0).getPublicKey();
        }
        messgae.setFeePayer(feePayer);
        serializedMessage = messgae.serialize();
        for (SolAccount signer : signers) {
            TweetNaclFast.Signature signatureProvider = new TweetNaclFast.Signature(new byte[0], signer.getSecretKey());
            byte[] signature = signatureProvider.detached(serializedMessage);
            signatures.add(Base58.encode(signature));
        }
    }

    public byte[] serialize() {
        int signaturesSize = signatures.size();
        byte[] signaturesLength = ShortvecEncoding.encodeLength(signaturesSize);
        ByteBuffer out = ByteBuffer
                .allocate(signaturesLength.length + signaturesSize * SIGNATURE_LENGTH + serializedMessage.length);
        out.put(signaturesLength);
        for (String signature : signatures) {
            byte[] rawSignature = Base58.decode(signature);
            out.put(rawSignature);
        }
        out.put(serializedMessage);
        return out.array();
    }

    public String getSignature() {
        if (signatures.size() > 0) {
            return signatures.get(0);
        }
        return null;
    }

}
