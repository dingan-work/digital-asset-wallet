package wiki.ganhua.wallet.tron.crypto;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9IntegerConverter;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;
import wiki.ganhua.wallet.tron.crypto.tuwenitypes.Bytes;
import wiki.ganhua.wallet.tron.crypto.tuwenitypes.Bytes32;
import wiki.ganhua.wallet.tron.crypto.tuwenitypes.MutableBytes;
import wiki.ganhua.wallet.tron.crypto.tuwenitypes.UInt256;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import java.util.Objects;

/**
 * Adapted from the BitcoinJ ECKey (Apache 2 License) implementation:
 * https://github.com/bitcoinj/bitcoinj/blob/master/core/src/main/java/org/bitcoinj/core/ECKey.java
 * Adapted from the web3j (Apache 2 License) implementations:
 * https://github.com/web3j/web3j/crypto/src/main/java/org/web3j/crypto/*.java
 */
public class SECP256K1 {

    public static final String ALGORITHM = "ECDSA";
    public static final String CURVE_NAME = "secp256k1";
    public static final String PROVIDER = "BC";

    public static final ECDomainParameters CURVE;
    public static final BigInteger HALF_CURVE_ORDER;

    private static final KeyPairGenerator KEY_PAIR_GENERATOR;
    private static final BigInteger CURVE_ORDER;

    static {
        Security.addProvider(new BouncyCastleProvider());
        final X9ECParameters params = SECNamedCurves.getByName(CURVE_NAME);
        CURVE = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
        CURVE_ORDER = CURVE.getN();
        HALF_CURVE_ORDER = CURVE_ORDER.shiftRight(1);
        try {
            KEY_PAIR_GENERATOR = KeyPairGenerator.getInstance(ALGORITHM, PROVIDER);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        final ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec(CURVE_NAME);
        try {
            KEY_PAIR_GENERATOR.initialize(ecGenParameterSpec);
        } catch (final InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }

    public static class PrivateKey implements java.security.PrivateKey {
        private final Bytes32 encoded;

        private PrivateKey(final Bytes32 encoded) {
            assert encoded != null;
            this.encoded = encoded;
        }

        public static PrivateKey create(final BigInteger key) {
            assert key != null;
            return create(UInt256.valueOf(key).toBytes());
        }

        public static PrivateKey create(final Bytes32 key) {
            return new PrivateKey(key);
        }

        @Override
        public boolean equals(final Object other) {
            if (!(other instanceof PrivateKey)) {
                return false;
            }

            final PrivateKey that = (PrivateKey) other;
            return this.encoded.equals(that.encoded);
        }

        @Override
        public byte[] getEncoded() {
            return encoded.toArrayUnsafe();
        }

        public Bytes32 getEncodedBytes() {
            return encoded;
        }

        public BigInteger getD() {
            return encoded.toUnsignedBigInteger();
        }

        @Override
        public String getAlgorithm() {
            return ALGORITHM;
        }

        @Override
        public String getFormat() {
            return null;
        }

        @Override
        public int hashCode() {
            return encoded.hashCode();
        }

        @Override
        public String toString() {
            return encoded.toString();
        }
    }

    public static class PublicKey implements java.security.PublicKey {
        private static final int BYTE_LENGTH = 64;

        private final Bytes encoded;

        public static PublicKey create(final PrivateKey privateKey) {
            BigInteger privKey = privateKey.getEncodedBytes().toUnsignedBigInteger();

            /*
             * TODO: FixedPointCombMultiplier currently doesn't support scalars longer than the group
             * order, but that could change in future versions.
             */
            if (privKey.bitLength() > CURVE.getN().bitLength()) {
                privKey = privKey.mod(CURVE.getN());
            }

            final ECPoint point = new FixedPointCombMultiplier().multiply(CURVE.getG(), privKey);
            return PublicKey.create(Bytes.wrap(Arrays.copyOfRange(point.getEncoded(false), 1, 65)));
        }

        private static Bytes toBytes64(final byte[] backing) {
            if (backing.length == BYTE_LENGTH) {
                return Bytes.wrap(backing);
            } else if (backing.length > BYTE_LENGTH) {
                return Bytes.wrap(backing, backing.length - BYTE_LENGTH, BYTE_LENGTH);
            } else {
                final MutableBytes res = MutableBytes.create(BYTE_LENGTH);
                Bytes.wrap(backing).copyTo(res, BYTE_LENGTH - backing.length);
                return res;
            }
        }

        public static PublicKey create(final BigInteger key) {
            assert key != null;
            return create(toBytes64(key.toByteArray()));
        }

        public static PublicKey create(final Bytes encoded) {
            return new PublicKey(encoded);
        }

        private PublicKey(final Bytes encoded) {
            assert encoded != null;
            assert encoded.size() == BYTE_LENGTH;
            this.encoded = encoded;
        }

        @Override
        public boolean equals(final Object other) {
            if (!(other instanceof PublicKey)) {
                return false;
            }

            final PublicKey that = (PublicKey) other;
            return this.encoded.equals(that.encoded);
        }

        @Override
        public byte[] getEncoded() {
            return encoded.toArrayUnsafe();
        }

        @Override
        public String getAlgorithm() {
            return ALGORITHM;
        }

        @Override
        public String getFormat() {
            return null;
        }

        @Override
        public int hashCode() {
            return encoded.hashCode();
        }

        @Override
        public String toString() {
            return encoded.toString();
        }
    }

    public static class KeyPair {
        private final PrivateKey privateKey;
        private final PublicKey publicKey;

        public KeyPair(final PrivateKey privateKey, final PublicKey publicKey) {
            assert privateKey != null;
            assert publicKey != null;
            this.privateKey = privateKey;
            this.publicKey = publicKey;
        }

        public static KeyPair create(final PrivateKey privateKey) {
            return new KeyPair(privateKey, PublicKey.create(privateKey));
        }

        public static KeyPair generate() {
            final java.security.KeyPair rawKeyPair = KEY_PAIR_GENERATOR.generateKeyPair();
            final BCECPrivateKey privateKey = (BCECPrivateKey) rawKeyPair.getPrivate();
            final BCECPublicKey publicKey = (BCECPublicKey) rawKeyPair.getPublic();

            final BigInteger privateKeyValue = privateKey.getD();

            // Ethereum does not use encoded public keys like bitcoin - see
            // https://en.bitcoin.it/wiki/Elliptic_Curve_Digital_Signature_Algorithm for details
            // Additionally, as the first bit is a constant prefix (0x04) we ignore this value
            final byte[] publicKeyBytes = publicKey.getQ().getEncoded(false);
            final BigInteger publicKeyValue =
                    new BigInteger(1, Arrays.copyOfRange(publicKeyBytes, 1, publicKeyBytes.length));

            return new KeyPair(PrivateKey.create(privateKeyValue), PublicKey.create(publicKeyValue));
        }

        @Override
        public int hashCode() {
            return Objects.hash(privateKey, publicKey);
        }

        @Override
        public boolean equals(final Object other) {
            if (!(other instanceof KeyPair)) {
                return false;
            }

            final KeyPair that = (KeyPair) other;
            return this.privateKey.equals(that.privateKey) && this.publicKey.equals(that.publicKey);
        }

        public PrivateKey getPrivateKey() {
            return privateKey;
        }

        public PublicKey getPublicKey() {
            return publicKey;
        }
    }

    /** Decompress a compressed public key (x co-ord and low-bit of y-coord). */
    private static ECPoint decompressKey(final BigInteger xBN, final boolean yBit) {
        final X9IntegerConverter x9 = new X9IntegerConverter();
        final byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(CURVE.getCurve()));
        compEnc[0] = (byte) (yBit ? 0x03 : 0x02);
        // TODO: Find a better way to handle an invalid point compression here.
        // Currently ECCurve#decodePoint throws an IllegalArgumentException.
        return CURVE.getCurve().decodePoint(compEnc);
    }

}
