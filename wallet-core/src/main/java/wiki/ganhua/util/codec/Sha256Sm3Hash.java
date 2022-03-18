package wiki.ganhua.util.codec;

import org.bouncycastle.crypto.digests.SM3Digest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha256Sm3Hash {

    private static boolean isEckey = true;

    public static byte[] hash(byte[] input) {
        return hash(input, 0, input.length);
    }

    public static byte[] hash(byte[] input, int offset, int length) {
        if (isEckey) {
            MessageDigest digest = newDigest();
            digest.update(input, offset, length);
            return digest.digest();
        } else {
            SM3Digest digest = newSM3Digest();
            digest.update(input, offset, length);
            byte[] eHash = new byte[digest.getDigestSize()];
            digest.doFinal(eHash, 0);
            return eHash;
        }
    }

    public static MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException var1) {
            throw new RuntimeException(var1);
        }
    }

    public static SM3Digest newSM3Digest() {
        return new SM3Digest();
    }

}
