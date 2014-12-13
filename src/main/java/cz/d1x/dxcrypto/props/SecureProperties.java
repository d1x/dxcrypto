package cz.d1x.dxcrypto.props;

import cz.d1x.dxcrypto.encryption.EncryptionAlgorithm;
import cz.d1x.dxcrypto.encryption.EncryptionException;

import java.util.Properties;

/**
 * Extension of {@link java.util.Properties} that allows storing and reading encrypted values by given encryption
 * algorithm. For recognition whether value is encrypted or not, default or given suffix is added at the end of
 * encrypted value.
 *
 * @author Zdenek Obst, zdenek.obst-at-gmail.com
 */
public class SecureProperties extends Properties {

    private static final String DEFAULT_ENCRYPTED_SUFFIX = "xa3s"; // is unambiguous, "x" never appears in HEX/Base64 representation

    private final EncryptionAlgorithm encryptionAlgorithm;
    private final String encryptedPropertySuffix;

    /**
     * Creates a new properties that will use given encryption algorithm.
     * Default {@link #DEFAULT_ENCRYPTED_SUFFIX} suffix will be used for recognition whether value is encrypted or not.
     *
     * @param encryptionAlgorithm algorithm used for encryption
     */
    public SecureProperties(EncryptionAlgorithm encryptionAlgorithm) {
        this(encryptionAlgorithm, DEFAULT_ENCRYPTED_SUFFIX);
    }

    /**
     * Creates a new properties that will use given encryption algorithm.
     * Default {@link #DEFAULT_ENCRYPTED_SUFFIX} suffix will be used for recognition whether value is encrypted or not.
     *
     * @param defaults            initial values for properties
     * @param encryptionAlgorithm algorithm used for encryption
     */
    public SecureProperties(Properties defaults, EncryptionAlgorithm encryptionAlgorithm) {
        this(defaults, encryptionAlgorithm, DEFAULT_ENCRYPTED_SUFFIX);
    }

    /**
     * Creates a new properties that will use given encryption algorithm and given suffix will be used for recognition
     * whether value is encrypted or not.
     *
     * @param encryptionAlgorithm     algorithm used for encryption
     * @param encryptedPropertySuffix suffix used for recognition of encrypted values (will be trimmed)
     */
    public SecureProperties(EncryptionAlgorithm encryptionAlgorithm, String encryptedPropertySuffix) {
        this(null, encryptionAlgorithm, encryptedPropertySuffix);
    }

    /**
     * Creates a new properties that will use given encryption algorithm and given suffix will be used for recognition
     * whether value is encrypted or not.
     *
     * @param defaults                initial values for properties
     * @param encryptionAlgorithm     algorithm used for encryption
     * @param encryptedPropertySuffix suffix used for recognition of encrypted values (will be trimmed)
     * @throws IllegalArgumentException possible exception if given suffix is null or 0-length
     */
    public SecureProperties(Properties defaults, EncryptionAlgorithm encryptionAlgorithm, String encryptedPropertySuffix) {
        super(defaults);
        this.encryptionAlgorithm = encryptionAlgorithm;
        if (encryptedPropertySuffix == null || encryptedPropertySuffix.trim().isEmpty()) {
            throw new IllegalArgumentException("Encryption suffix must be non-null and must have at least one character after trimming");
        }
        this.encryptedPropertySuffix = encryptedPropertySuffix.trim();
    }

    /**
     * Have the same functionality as {@link #setProperty(String, String)} but the value gets encrypted before it gets
     * stored within properties. The value gets automatically decrypted when {@link #getProperty(String)} or
     * {@link #getProperty(String, String)} is called.
     *
     * @param key   key of the property to be set
     * @param value value to be encrypted and set
     * @return previous value of property if was set
     */
    public synchronized Object setEncryptedProperty(String key, String value) {
        String encryptedValue = encryptionAlgorithm.encrypt(value) + encryptedPropertySuffix;
        return super.setProperty(key, encryptedValue);
    }

    /**
     * Gets original property value under given key. It means that encrypted properties are returned in its
     * encrypted form. If you want retrieve property already decrypted, use {@link #getProperty(String)} or
     * {@link #getProperty(String, String)} instead.
     *
     * @param key key of the property to be read
     * @return value of given property or null if not present
     */
    public String getOriginalProperty(String key) {
        return super.getProperty(key);
    }

    /**
     * {@inheritDoc}
     * <p>
     * If the property ends with given suffix (specified in constructor or default), it gets decrypted by defined
     * encryption algorithm. Note that {@link EncryptionException} can be thrown if encrypted property cannot be
     * decrypted by given algorithm.
     * </p>
     *
     * @param key key of the property to be read
     * @return property value or null if not ste
     */
    @Override
    public String getProperty(String key) {
        return getPlainOrEncrypted(key, super.getProperty(key));
    }

    /**
     * {@inheritDoc}
     * <p>
     * If the property ends with given suffix (specified in constructor or default), it gets decrypted by defined
     * encryption algorithm. Note that {@link EncryptionException} can be thrown if encrypted property cannot be
     * decrypted by given algorithm.
     * </p>
     *
     * @param key          key of the property to be read
     * @param defaultValue default value if property is not set
     * @return property value or default if not set
     */
    @Override
    public String getProperty(String key, String defaultValue) {
        return getPlainOrEncrypted(key, super.getProperty(key, defaultValue));
    }

    /**
     * Checks whether property under given key is encrypted.
     *
     * @param key key of property to be checked
     * @return true if property under given key is encrypted (unset properties are considered as not encrypted),
     * otherwise false
     */
    public boolean isEncrypted(String key) {
        String propertyValue = super.getProperty(key);
        return propertyValue != null && propertyValue.endsWith(encryptedPropertySuffix);
    }

    /**
     * Validates whether given expected value is equal to the value in the properties under given key.
     *
     * @param key           key of property
     * @param expectedValue expected value of property
     * @return true if value of expected and the one in properties are equal, otherwise false.
     */
    public boolean validateValue(String key, String expectedValue) {
        String propertyValue = getProperty(key);
        return (propertyValue != null) ? propertyValue.equals(expectedValue) : (expectedValue == null);
    }

    private String getPlainOrEncrypted(String key, String value) {
        if (isEncrypted(key)) {
            String withoutSuffix = value.substring(0, value.length() - encryptedPropertySuffix.length());
            return encryptionAlgorithm.decrypt(withoutSuffix);
        } else {
            return value;
        }
    }
}
