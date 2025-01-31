package edu.cornell.kfs.sys.dataaccess.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.framework.persistence.ojb.conversion.OjbKualiEncryptDecryptFieldConversion;
import org.kuali.kfs.sys.KFSConstants;

/**
 * Custom test-only subclass of OjbKualiEncryptDecryptFieldConversion that merely performs
 * Base64 encoding/decoding instead of actual encryption/decryption.
 */
public class TestOjbKualiEncryptDecryptFieldConversion extends OjbKualiEncryptDecryptFieldConversion {

    private static final long serialVersionUID = 1L;

    private static final Base64.Encoder ENCODER = Base64.getEncoder();
    private static final Base64.Decoder DECODER = Base64.getDecoder();

    @Override
    public Object javaToSql(final Object value) {
        return convert(value, ENCODER::encode);
    }

    @Override
    public Object sqlToJava(final Object value) {
        return convert(value, DECODER::decode);
    }

    private String convert(final Object value, final Function<byte[], byte[]> converter) {
        final String stringValue = value != null ? value.toString() : null;
        if (StringUtils.isBlank(stringValue)) {
            return KFSConstants.EMPTY_STRING;
        }
        final byte[] byteValue = stringValue.getBytes(StandardCharsets.UTF_8);
        final byte[] convertedValue = converter.apply(byteValue);
        return new String(convertedValue, StandardCharsets.UTF_8);
    }

}
