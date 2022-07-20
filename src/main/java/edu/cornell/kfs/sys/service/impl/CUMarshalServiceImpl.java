package edu.cornell.kfs.sys.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Optional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.sys.service.CUMarshalService;

public class CUMarshalServiceImpl implements CUMarshalService {
    private static final Logger LOG = LogManager.getLogger(CUMarshalServiceImpl.class);

    private static final String JAXB_DEFAULT_VALUE_INDICATOR = "##default";
    private static final String XML_HEADERS_PROPERTY = "com.sun.xml.bind.xmlHeaders";
    private static final String DTD_FORMAT = "<!DOCTYPE {0} SYSTEM \"{1}\">\n";

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
    public String marshalObjectToXmlString(Object objectToMarshal) throws JAXBException, IOException {
        return marshalObjectToXmlString(objectToMarshal, Optional.empty());
    }

    public String marshalObjectToXmlStringWithSystemDocType(Object objectToMarshal, String dtdUrl)
            throws JAXBException, IOException {
        String rootElementName = getRootElementNameFromDTO(objectToMarshal);
        String dtdSection = MessageFormat.format(DTD_FORMAT, rootElementName, dtdUrl);
        return marshalObjectToXmlString(objectToMarshal, Optional.of(dtdSection));
    }

    protected String getRootElementNameFromDTO(Object objectToMarshal) {
        Class<?> dtoClass = objectToMarshal.getClass();
        XmlRootElement elementAnnotation = dtoClass.getDeclaredAnnotation(XmlRootElement.class);
        if (elementAnnotation == null) {
            throw new IllegalArgumentException("DTO class does not have an XmlRootElement annotation");
        } else if (StringUtils.equals(elementAnnotation.name(), JAXB_DEFAULT_VALUE_INDICATOR)) {
            throw new IllegalArgumentException("DTO class specifies classname-based derivation "
                    + "of the root element name; this service implementation does not support that");
        }
        return elementAnnotation.name();
    }

    protected String marshalObjectToXmlString(Object objectToMarshal, Optional<String> dtdSection)
            throws JAXBException, IOException {
        LOG.debug("marshalObjectToXMLString, entering");
        JAXBContext jaxbContext = JAXBContext.newInstance(objectToMarshal.getClass());
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        if (dtdSection.isPresent()) {
            jaxbMarshaller.setProperty(XML_HEADERS_PROPERTY, dtdSection.get());
        }
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
