package edu.cornell.kfs.sys.batch;

import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBException;

import org.kuali.kfs.sys.batch.BatchInputFileTypeBase;
import org.kuali.kfs.sys.exception.ParseException;

import edu.cornell.kfs.sys.service.CUMarshalService;

public abstract class JAXBXmlBatchInputFileTypeBase extends BatchInputFileTypeBase {

    protected CUMarshalService marshalService;

    protected abstract Class<?> getPojoClass();

    @Override
    public Object parse(byte[] fileByteContent) throws ParseException {
        try {
            String fileStringContent = new String(fileByteContent, StandardCharsets.UTF_8);
            return marshalService.unmarshalString(fileStringContent, getPojoClass());
        } catch (JAXBException e) {
            throw new ParseException("Error attempting to unmarshal POJO from XML", e);
        }
    }

    @Override
    public void process(String fileName, Object parsedFileContents) {
        // Do nothing.
    }

    public void setMarshalService(CUMarshalService marshalService) {
        this.marshalService = marshalService;
    }

}
