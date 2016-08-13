package cz.d1x.dxcrypto.encrytion.bc;


import cz.d1x.dxcrypto.common.ByteArray;
import cz.d1x.dxcrypto.encryption.EncryptionEngine;
import cz.d1x.dxcrypto.encryption.bc.BouncyCastleFactories;
import cz.d1x.dxcrypto.encryption.crypto.CryptoFactories;
import cz.d1x.dxcrypto.encryption.key.DerivedKeyParams;
import cz.d1x.dxcrypto.encryption.key.EncryptionKeyFactory;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class BCEncryptionAlgorithmsTest {

    private final CryptoFactories CRYPTO_FACTORIES = new CryptoFactories();
    private final BouncyCastleFactories BC_FACTORIES = new BouncyCastleFactories();
    private final Random RANDOM = new Random();

    @Test
    public void aes128HaveSameOutputsAsCryptoForStringBasedPasswords() {
        int keySize = 128;
        int blockSize = 128;

        EncryptionKeyFactory<ByteArray, DerivedKeyParams> cryptoKF = CRYPTO_FACTORIES.derivedKeyFactory();
        EncryptionKeyFactory<ByteArray, DerivedKeyParams> bcKF = BC_FACTORIES.derivedKeyFactory();
        ByteArray key = compareTwoKeyFactories(cryptoKF, bcKF, keySize);

        EncryptionEngine cryptoEngine = CRYPTO_FACTORIES.aes().newEngine(key);
        EncryptionEngine bcEngine = BC_FACTORIES.aes().newEngine(key);
        byte[] iv = new byte[blockSize / 8];
        RANDOM.nextBytes(iv);
        compareTwoEngines(cryptoEngine, bcEngine, iv);
    }

    @Test
    public void aes256HaveSameOutputsAsCryptoForStringBasedPasswords() {
        int keySize = 256;
        int blockSize = 128;

        EncryptionKeyFactory<ByteArray, DerivedKeyParams> cryptoKF = CRYPTO_FACTORIES.derivedKeyFactory();
        EncryptionKeyFactory<ByteArray, DerivedKeyParams> bcKF = BC_FACTORIES.derivedKeyFactory();
        ByteArray key = compareTwoKeyFactories(cryptoKF, bcKF, keySize);

        EncryptionEngine cryptoEngine = CRYPTO_FACTORIES.aes256().newEngine(key);
        EncryptionEngine bcEngine = BC_FACTORIES.aes256().newEngine(key);
        byte[] iv = new byte[blockSize / 8];
        RANDOM.nextBytes(iv);
        compareTwoEngines(cryptoEngine, bcEngine, iv);
    }

    private ByteArray compareTwoKeyFactories(EncryptionKeyFactory<ByteArray, DerivedKeyParams> factory1,
                                             EncryptionKeyFactory<ByteArray, DerivedKeyParams> factory2,
                                             int keySize) {
        byte[] keyPass = "s3ce3t-keyPass".getBytes(StandardCharsets.UTF_8);
        byte[] salt = new byte[17];
        RANDOM.nextBytes(salt);
        int iterations = 27;
        DerivedKeyParams keyParams = new DerivedKeyParams(keyPass, salt, iterations, keySize);

        ByteArray key1 = factory1.newKey(keyParams);
        ByteArray key2 = factory2.newKey(keyParams);

        Assert.assertArrayEquals("Keys should equal", key1.getValue(), key2.getValue());
        return key1; // doesn't matter, they are equal
    }

    private void compareTwoEngines(EncryptionEngine engine1, EncryptionEngine engine2, byte[] initVector) {
        byte[] input = new byte[RANDOM.nextInt(200)];
        RANDOM.nextBytes(input);

        byte[] output1 = engine1.encrypt(input, initVector);
        byte[] output2 = engine2.encrypt(input, initVector);

        Assert.assertArrayEquals("Encrypted outputs should equal", output1, output2);

        byte[] output1Back = engine1.decrypt(output1, initVector);
        byte[] output2Back = engine2.decrypt(output2, initVector);

        Assert.assertArrayEquals("Input and decrypted output should equal", output1Back, input);
        Assert.assertArrayEquals("Input and decrypted output should equal", output2Back, input);
    }
}
