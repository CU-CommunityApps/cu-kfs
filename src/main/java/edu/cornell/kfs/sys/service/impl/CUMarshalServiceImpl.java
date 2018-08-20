package edu.cornell.kfs.sys.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.sys.service.CUMarshalService;

public class CUMarshalServiceImpl implements CUMarshalService {
	private static final Logger LOG = LogManager.getLogger(CUMarshalServiceImpl.class);

    @Override
    public File marshalObjectToXML(Object objectToMarshal, String outputFilePath) throws JAXBException, IOException {
        LOG.debug("marshalObjectToXML, entering, outputFilePath: " + outputFilePath);
        JAXBContext jaxbContext = JAXBContext.newInstance(objectToMarshal.getClass());
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        if (LOG.isDebugEnabled()) {
            jaxbMarshaller.marshal( objectToMarshal, System.out );
        }

        File marshalledXml = new File(outputFilePath);
        FileUtils.touch(marshalledXml);
        jaxbMarshaller.marshal(objectToMarshal, marshalledXml);
        LOG.debug("marshalObjectToXML, returning an XML file with the size of " + FileUtils.sizeOf(marshalledXml));
        return marshalledXml;
    }

    @Override
    public String marshalObjectToXmlString(Object objectToMarshal) throws JAXBException {
        LOG.debug("marshalObjectToXMLString, entering");
        JAXBContext jaxbContext = JAXBContext.newInstance(objectToMarshal.getClass());
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter stringWriter = new StringWriter();
        jaxbMarshaller.marshal(objectToMarshal, stringWriter);
        String marshalledXml = stringWriter.toString();
        return marshalledXml;
    }
    
    @Override
    public <T> T unmarshalFile(File xmlFile, Class<T> clazz) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(clazz);
        T object = (T) jc.createUnmarshaller().unmarshal(xmlFile);
        return object;
    }
    
    @Override
    public <T> T unmarshalString(String xmlString, Class<T> clazz) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(clazz);
        StringReader reader = new StringReader(xmlString);
        T object = (T) jc.createUnmarshaller().unmarshal(reader);
        return object;
    }

}
