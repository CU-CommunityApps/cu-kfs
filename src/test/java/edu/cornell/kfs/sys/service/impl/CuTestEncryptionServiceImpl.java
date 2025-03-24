package edu.cornell.kfs.sys.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.function.Function;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.encryption.EncryptionService;
import org.kuali.kfs.sys.KFSConstants;

/**
 * Testing-only encryption service whose encrypt/decrypt methods only perform Base64 encoding/decoding instead,
 * and whose hashing method only returns the object's hash code as a String instead of creating a true hash.
 */
public class CuTestEncryptionServiceImpl implements EncryptionService {

    @Override
    public String decrypt(final String ciphertext) throws GeneralSecurityException {
        if (StringUtils.isBlank(ciphertext)) {
            return KFSConstants.EMPTY_STRING;
        }
        final byte[] textBytes = ciphertext.getBytes(StandardCharsets.UTF_8);
        final byte[] decryptedBytes = decryptBytes(textBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    @Override
    public byte[] decryptBytes(final byte[] ciphertext) throws GeneralSecurityException {
        return convertByteArray(ciphertext, Base64::decodeBase64);
    }

    private byte[] convertByteArray(final byte[] value, final Function<byte[], byte[]> converter) {
        if (value == null) {
            return null;
        } else if (value.length == 0) {
            return value;
        } else {
            return converter.apply(value);
        }
    }

    @Override
    public String encrypt(final Object valueToHide) throws GeneralSecurityException {
        if (valueToHide == null) {
            return KFSConstants.EMPTY_STRING;
        }
        final String stringValueToHide = valueToHide.toString();
        final byte[] textBytes = stringValueToHide.getBytes(StandardCharsets.UTF_8);
        final byte[] encryptedBytes = encryptBytes(textBytes);
        return new String(encryptedBytes, StandardCharsets.UTF_8);
    }

    @Override
    public byte[] encryptBytes(final byte[] valueToHide) throws GeneralSecurityException {
        return convertByteArray(valueToHide, Base64::encodeBase64);
    }

    @Override
    public String hash(final Object objectToHide) throws GeneralSecurityException {
        return String.valueOf(Objects.hashCode(objectToHide));
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
