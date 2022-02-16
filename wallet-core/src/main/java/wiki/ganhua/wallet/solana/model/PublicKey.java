package wiki.ganhua.wallet.solana.model;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Sha256Hash;
import wiki.ganhua.wallet.solana.util.ByteUtils;
import wiki.ganhua.wallet.solana.util.TweetNaclFast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PublicKey {

    public static final int PUBLIC_KEY_LENGTH = 32;
    private final byte[] pubkey;

    public PublicKey(String pubkey) {
        if (pubkey.length() < PUBLIC_KEY_LENGTH) {
            throw new IllegalArgumentException("Invalid public key input");
        }
        this.pubkey = Base58.decode(pubkey);
    }

    public PublicKey(byte[] pubkey) {

        if (pubkey.length > PUBLIC_KEY_LENGTH) {
            throw new IllegalArgumentException("Invalid public key input");
        }

        this.pubkey = pubkey;
    }

    public static PublicKey readPubkey(byte[] bytes, int offset) {
        byte[] buf = ByteUtils.readBytes(bytes, offset, PUBLIC_KEY_LENGTH);
        return new PublicKey(buf);
    }

    public byte[] toByteArray() {
        return pubkey;
    }

    public String toBase58() {
        return Base58.encode(pubkey);
    }

    public boolean equals(PublicKey pubkey) {
        return Arrays.equals(this.pubkey, pubkey.toByteArray());
    }

    @Override
    public String toString() {
        return toBase58();
    }

    public static PublicKey createProgramAddress(List<byte[]> seeds, PublicKey programId) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (byte[] seed : seeds) {
            if (seed.length > 32) {
                throw new IllegalArgumentException("Max seed length exceeded");
            }
            buffer.write(seed);
        }
        buffer.write(programId.toByteArray());
        buffer.write("ProgramDerivedAddress".getBytes());
        byte[] hash = Sha256Hash.hash(buffer.toByteArray());
        if (TweetNaclFast.is_on_curve(hash) != 0) {
            throw new Exception("Invalid seeds, address must fall off the curve");
        }
        return new PublicKey(hash);
    }

    public static class ProgramDerivedAddress {
        private final PublicKey address;
        private final int nonce;
        public ProgramDerivedAddress(PublicKey address, int nonce) {
            this.address = address;
            this.nonce = nonce;
        }
        public PublicKey getAddress() {
            return address;
        }
        public int getNonce() {
            return nonce;
        }
    }

    public static ProgramDerivedAddress findProgramAddress(List<byte[]> seeds, PublicKey programId) throws Exception {
        int nonce = 255;
        PublicKey address;
        List<byte[]> seedsWithNonce = new ArrayList<>(seeds);
        while (nonce != 0) {
            try {
                seedsWithNonce.add(new byte[] { (byte) nonce });
                address = createProgramAddress(seedsWithNonce, programId);
            } catch (Exception e) {
                seedsWithNonce.remove(seedsWithNonce.size() - 1);
                nonce--;
                continue;
            }
            return new ProgramDerivedAddress(address, nonce);
        }
        throw new Exception("Unable to find a viable program address nonce");
    }

}
