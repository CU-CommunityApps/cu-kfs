package edu.cornell.kfs.sys.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.sys.businessobject.XmlFragmentable;
import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.util.CuXMLStreamUtils;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.ValidationEventHandler;

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
    public File marshalObjectToXMLFragment(XmlFragmentable objectToMarshal, String outputFilePath) throws JAXBException, IOException {
        LOG.debug("marshalObjectToXMLFragment, entering, outputFilePath: " + outputFilePath);
        String xmlData = marshalObjectToXmlFragmentString(objectToMarshal);
        try (
                FileOutputStream fileOutputStream = new FileOutputStream(outputFilePath);
                OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
                BufferedWriter bufferedWriter = new BufferedWriter(writer);
            ) {
            bufferedWriter.write(xmlData);
        }
        return new File(outputFilePath);
    }

    @Override
    public void marshalObjectToXML(Object objectToMarshal, OutputStream outputStream)
            throws JAXBException, IOException {
        LOG.debug("marshalObjectToXML, entering");
        JAXBContext jaxbContext = JAXBContext.newInstance(objectToMarshal.getClass());
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        try (
            CloseShieldOutputStream wrappedStream = new CloseShieldOutputStream(outputStream);
            OutputStreamWriter streamWriter = new OutputStreamWriter(wrappedStream, StandardCharsets.UTF_8);
            BufferedWriter writer = new BufferedWriter(streamWriter);
        ) {
            jaxbMarshaller.marshal(objectToMarshal, writer);
            writer.flush();
        }
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
    public String marshalObjectToXmlFragmentString(XmlFragmentable objectToMarshal) throws JAXBException, IOException {
        LOG.debug("marshalObjectToXmlFragmentString, entering");
        JAXBContext jaxbContext = JAXBContext.newInstance(objectToMarshal.getClass());
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        if (objectToMarshal.shouldMarshalAsFragment()) {
            jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        }
        
        Map<String, Object> additionalProperties = objectToMarshal.getAdditionalJAXBProperties();
        for (String key : additionalProperties.keySet()) {
            jaxbMarshaller.setProperty(key, additionalProperties.get(key));
        }
        
        StringWriter stringWriter = new StringWriter();
        jaxbMarshaller.marshal(objectToMarshal, stringWriter);
        String marshalledXml = stringWriter.toString();
        String returnXmlString = objectToMarshal.getXmlPrefix() + KFSConstants.NEWLINE + marshalledXml;
        LOG.debug("marshalObjectToXmlString, the XML with manual prefix output is {}", returnXmlString);
        return returnXmlString;
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
    public <T> T unmarshalStringIgnoreDtd(String xmlString, Class<T> clazz)
            throws JAXBException, XMLStreamException, IOException {
        try (InputStream xmlStream = IOUtils.toInputStream(xmlString, StandardCharsets.UTF_8);) {
            return unmarshalStreamIgnoreDtd(xmlStream, clazz);
        }
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

    @Override
    public <T> T unmarshalStream(InputStream inputStream, Class<T> clazz)
            throws JAXBException, IOException {
        Unmarshaller unmarshaller = createUnmarshaller(clazz, Optional.empty());
        return unmarshalStream(inputStream, unmarshaller);
    }
    
    @Override
    public <T> T unmarshalStreamIgnoreDtd(InputStream inputStream, Class<T> clazz)
            throws JAXBException, IOException, XMLStreamException {
        JAXBContext jc = JAXBContext.newInstance(clazz);
        XMLInputFactory inputFactory = XMLInputFactory.newFactory();
        inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        XMLStreamReader streamReader = null;
        
        try (CloseShieldInputStream wrappedStream = new CloseShieldInputStream(inputStream);) {
            streamReader = inputFactory.createXMLStreamReader(wrappedStream, StandardCharsets.UTF_8.name());
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            return (T) unmarshaller.unmarshal(streamReader);
        } finally {
            CuXMLStreamUtils.closeQuietly(streamReader);
        }
    }

    @Override
    public <T> T unmarshalStream(InputStream inputStream, Class<T> clazz, Object listener)
            throws JAXBException, IOException {
        Unmarshaller unmarshaller = createUnmarshaller(clazz, Optional.of(listener));
        return unmarshalStream(inputStream, unmarshaller);
    }

    protected <T> T unmarshalStream(InputStream inputStream, Unmarshaller unmarshaller)
            throws JAXBException, IOException {
        try (
            CloseShieldInputStream wrappedStream = new CloseShieldInputStream(inputStream);
            InputStreamReader streamReader = new InputStreamReader(wrappedStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(streamReader);
        ) {
            return (T) unmarshaller.unmarshal(reader);
        }
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
