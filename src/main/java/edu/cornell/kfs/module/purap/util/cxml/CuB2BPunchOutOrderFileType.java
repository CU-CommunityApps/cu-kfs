package edu.cornell.kfs.module.purap.util.cxml;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.sys.exception.ParseException;

import java.io.ByteArrayInputStream;

import org.kuali.kfs.module.purap.util.cxml.B2BPunchOutOrderFileType;

public class CuB2BPunchOutOrderFileType extends B2BPunchOutOrderFileType {
    
    /**
     * @param  fileByteContent byte array file content
     * @return ElectronicInvoice
     * @throws ParseException in case of parsing exception
     */
    @Override
    public Object parse(final byte[] fileByteContent) {
        Validate.isTrue(ArrayUtils.isNotEmpty(fileByteContent), "fileByteContent must be provided");

        // validate contents against schema
        final ByteArrayInputStream validateFileContents = new ByteArrayInputStream(fileByteContent);
        validateContentsAgainstSchema(getSchemaLocation(), validateFileContents);

        try {
            final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fileByteContent);
            final JAXBContext jaxbContext = JAXBContext.newInstance(CuB2BShoppingCart.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            return jaxbUnmarshaller.unmarshal(byteArrayInputStream);

        } catch (final JAXBException e) {
            throw new ParseException("Error parsing xml contents: " + e.getMessage(), e);
        }
    }

}
