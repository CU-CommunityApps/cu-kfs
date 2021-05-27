package edu.cornell.kfs.core.impl.encryption;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.StringUtils;

public class AESEncryptionServiceImpl extends SymmetricEncryptionServiceImpl {
    private static final String CIPHER_ALG = "AES";
    private static final String KEY_ALG = "AES";
    private static final int KEY_SIZE = 128;
    private static final byte CHARCODE_SPACE = 32;

    public AESEncryptionServiceImpl() {
        this.cipherAlgorithm = CIPHER_ALG;
        this.keyAlgorithm = KEY_ALG;
    }

    public static String createAESKey(String password) throws UnsupportedEncodingException {
        if (StringUtils.isBlank(password)) {
            throw new IllegalArgumentException("Password cannot be blank!");
        }
        
        byte[] passwordBytes = password.getBytes(CHARSET);
        
        if (passwordBytes.length == 0) {
            throw new IllegalArgumentException("Password length cannot be 0!");
        }
        
        int keySizeBytes = (KEY_SIZE + 7) / 8;
        byte[] fixedKeyBytes = new byte[keySizeBytes];
        int len = Math.min(passwordBytes.length, keySizeBytes);
        System.arraycopy(passwordBytes, 0, fixedKeyBytes, 0, len);
        // pad with spaces
        for (int i = len; i < keySizeBytes; i++) {
            fixedKeyBytes[i] = CHARCODE_SPACE;
        }
        return new String(fixedKeyBytes, CHARSET);
    }

    @Override
    public void setCipherAlgorithm(String algorithm) {
        throw new UnsupportedOperationException("Cipher algorithm cannot be changed.");
    }

    @Override
    public void setKey(String key) throws UnsupportedEncodingException {
        super.setKey(createAESKey(key));
    }

    @Override
    public void setKeyAlgorithm(String algorithm) {
        throw new UnsupportedOperationException("Key algorithm cannot be changed.");
    }

    @Override
    public void setKeyIterations(int iterations) {
        throw new UnsupportedOperationException("Key iterations are not supported.");
    }

    @Override
    public void setKeySalt(String salt) throws UnsupportedEncodingException {
        throw new UnsupportedOperationException("Key salt is not supported.");
    }

    @Override
    public void setKeySaltBytes(byte[] salt) throws UnsupportedEncodingException {
        throw new UnsupportedOperationException("Key salt is not supported.");
    }
}
