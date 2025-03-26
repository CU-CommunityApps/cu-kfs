package edu.cornell.kfs.core.impl.encryption;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableFunction;
import org.kuali.kfs.core.api.encryption.EncryptionService;
import org.kuali.kfs.sys.KFSConstants;

/**
 * Unit-test-only encryption service that merely Base64 encodes/decodes the values
 * instead of performing true encryptions/decryptions. Also, the hash() method
 * just Base64 encodes the to-string form of the object that is passed in.
 */
public class TestEncryptionServiceImpl implements EncryptionService {

    @Override
    public String decrypt(final String ciphertext) throws GeneralSecurityException {
        if (StringUtils.isBlank(ciphertext)) {
            return KFSConstants.EMPTY_STRING;
        }
        return convertValue(ciphertext, this::decryptBytes);
    }

    private String convertValue(final String value,
            final FailableFunction<byte[], byte[], GeneralSecurityException> converter)
                    throws GeneralSecurityException {
        final byte[] valueAsBytes = value.getBytes(StandardCharsets.UTF_8);
        final byte[] convertedBytes = converter.apply(valueAsBytes);
        return new String(convertedBytes, StandardCharsets.UTF_8);
    }

    @Override
    public byte[] decryptBytes(final byte[] ciphertext) throws GeneralSecurityException {
        if (ciphertext == null) {
            return null;
        } else if (ciphertext.length == 0) {
            return new byte[0];
        } else {
            return Base64.decodeBase64(ciphertext);
        }
    }

    @Override
    public String encrypt(final Object valueToHide) throws GeneralSecurityException {
        if (valueToHide == null) {
            return KFSConstants.EMPTY_STRING;
        }
        return convertValue(valueToHide.toString(), this::encryptBytes);
    }

    @Override
    public byte[] encryptBytes(final byte[] valueToHide) throws GeneralSecurityException {
        if (valueToHide == null) {
            return null;
        } else if (valueToHide.length == 0) {
            return new byte[0];
        } else {
            return Base64.encodeBase64(valueToHide);
        }
    }

    @Override
    public String hash(final Object valueToHide) throws GeneralSecurityException {
        return encrypt(valueToHide);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
