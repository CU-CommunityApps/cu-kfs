package edu.cornell.kfs.sys.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.ValidationEventHandler;

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
        Unmarshaller unmarshaller = createUnmarshaller(clazz, Optional.empty());
        return (T) unmarshaller.unmarshal(xmlFile);
    }
    
    @Override
    public <T> T unmarshalString(String xmlString, Class<T> clazz) throws JAXBException {
        Unmarshaller unmarshaller = createUnmarshaller(clazz, Optional.empty());
        StringReader reader = new StringReader(xmlString);
        return (T) unmarshaller.unmarshal(reader);
    }

    @Override
    public <T> T unmarshalFile(File xmlFile, Class<T> clazz, Object listener) throws JAXBException {
        Unmarshaller unmarshaller = createUnmarshaller(clazz, Optional.of(listener));
        return (T) unmarshaller.unmarshal(xmlFile);
    }

    @Override
    public <T> T unmarshalString(String xmlString, Class<T> clazz, Object listener) throws JAXBException {
        Unmarshaller unmarshaller = createUnmarshaller(clazz, Optional.of(listener));
        StringReader reader = new StringReader(xmlString);
        return (T) unmarshaller.unmarshal(reader);
    }

    protected <T> Unmarshaller createUnmarshaller(Class<T> clazz, Optional<Object> listener) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        if (listener.isPresent()) {
            Object listenerObject = listener.get();
            if (listenerObject instanceof Unmarshaller.Listener) {
                unmarshaller.setListener((Unmarshaller.Listener) listenerObject);
            }
            if (listenerObject instanceof ValidationEventHandler) {
                unmarshaller.setEventHandler((ValidationEventHandler) listenerObject);
            }
        }
        return unmarshaller;
    }

}
