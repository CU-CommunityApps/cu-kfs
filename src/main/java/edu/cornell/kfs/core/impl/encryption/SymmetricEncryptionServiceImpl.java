// Created on Jun 6, 2008

package edu.cornell.kfs.core.impl.encryption;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.encryption.EncryptionService;
import org.kuali.kfs.kew.api.WorkflowRuntimeException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

/**
 * Generic implementation of KEW encryption service
 *
 * @author Aaron Hamid (arh14 at cornell dot edu)
 * Updated for KFS compatibility by Chad Hagstrom (cah292)
 */
public class SymmetricEncryptionServiceImpl implements InitializingBean, EncryptionService {
    public static final String CHARSET = "UTF-8";

    protected String keyAlgorithm;
    protected String cipherAlgorithm;

    /* only used with PBE */
    protected int iterations = 0;
    protected byte[] salt;

    protected transient SecretKey key;

    /**
     * Whether the encryption is enabled _FOR KEW/KSB_
     */
    protected boolean isEnabled = false;

    private String hashAlgorithm = "SHA";

    public SymmetricEncryptionServiceImpl() {
        if (key != null) {
            throw new RuntimeException("The secret key must be kept secret. Storing it in the Java source code is a really bad idea.");
        }
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    /**
     * @see EncryptionService#isEnabled()
     */
    public boolean isEnabled() {
        return isEnabled;
    }

    @Required
    public void setKey(String key) throws UnsupportedEncodingException {
        this.key = new SecretKeySpec(key.getBytes(CHARSET), keyAlgorithm);
    }

    public void setKeyIterations(int iterations) {
        this.iterations = iterations;
    }
    
    public void setKeySaltBytes(byte[] salt) throws UnsupportedEncodingException {
        this.salt = salt;
    }

    public void setKeySalt(String salt) throws UnsupportedEncodingException {
        this.salt = Base64.decodeBase64(salt.getBytes(CHARSET));
    }

    @Required
    public void setCipherAlgorithm(String algorithm) {
        this.cipherAlgorithm = algorithm;
    }
    
    @Required
    public void setKeyAlgorithm(String algorithm) {
        this.keyAlgorithm = algorithm;
    }

    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws NoSuchAlgorithmException, InvalidKeySpecException {
        // no need to generate secret key from spec...we're using the SecretKey shortcut
    }

    public String encrypt(Object valueToHide) throws GeneralSecurityException {
        if (valueToHide == null) {
            return "";
        }

        try {
            byte[] input = valueToHide.toString().getBytes(CHARSET);
            byte[] encrypted = encryptBytes(input);
            byte[] encoded = Base64.encodeBase64(encrypted);
            return new String(encoded, CHARSET);
        } catch (UnsupportedEncodingException uee) {
            throw new WorkflowRuntimeException(uee);
        }
    }

    public byte[] encryptBytes(byte[] cleartext) throws GeneralSecurityException {
        if (cleartext == null) {
            return null;
        }
        if (cleartext.length == 0) {
            return new byte[0];
        }

        // Initialize the cipher for encryption
        Cipher cipher = Cipher.getInstance(cipherAlgorithm);
        // if a salt has been set, then this is a PBE-style key/cipher
        AlgorithmParameterSpec params = null;
        if (salt != null) {
            params = new PBEParameterSpec(salt, iterations);
        }
        cipher.init(Cipher.ENCRYPT_MODE, key, params);

        // Encrypt the cleartext
        return cipher.doFinal(cleartext);
    }
   
    public String decrypt(String ciphertext) throws GeneralSecurityException {
        if (StringUtils.isBlank(ciphertext)) {
            return "";
        }

        try {
            // un-Base64 encode the encrypted data
            byte[] input = ciphertext.getBytes(CHARSET);
            byte[] decoded = Base64.decodeBase64(input);
            byte[] cleartext = decryptBytes(decoded);

            return new String(cleartext, CHARSET);
            
        } catch (UnsupportedEncodingException e) {
            throw new WorkflowRuntimeException(e);
        }
    }
    
    public byte[] decryptBytes(byte[] encrypted) throws GeneralSecurityException {
        if (encrypted == null) {
            return null;
        }
        if (encrypted.length == 0) {
            return new byte[0];
        }
        
        // Initialize the same cipher for decryption
        Cipher cipher = Cipher.getInstance(cipherAlgorithm);
        AlgorithmParameterSpec params = null;
        // if a salt has been set, then this is a PBE-style key/cipher
        if (salt != null) {
            params = new PBEParameterSpec(salt, iterations);
        }
        cipher.init(Cipher.DECRYPT_MODE, key, params);

        return cipher.doFinal(encrypted);
    }
    
    /**
     * @param hashAlgorithm the algorithm to use for one-way hashing
     */
    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    public String hash(Object valueToHide) throws GeneralSecurityException {
        if (valueToHide == null || StringUtils.isEmpty(valueToHide.toString())) {
            return "";
        }
        try {
            byte[] input = valueToHide.toString().getBytes(CHARSET);
            byte[] digest = hash(input);
            byte[] encoded = Base64.encodeBase64(digest);
            return new String(encoded, CHARSET);
        } catch (UnsupportedEncodingException ex) {
            // should never happen
            throw new GeneralSecurityException("Error obtaining string bytes", ex);
        }
    }
    
    public byte[] hash(byte[] input) throws GeneralSecurityException {
        if (input == null) {
            return null;
        }
        if (input.length == 0) {
            return new byte[0];
        }

        MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
        return md.digest(input);
    }
}
