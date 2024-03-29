package edu.cornell.kfs.sys.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLStreamException;

import edu.cornell.kfs.sys.businessobject.XmlFragmentable;
import jakarta.xml.bind.JAXBException;

public interface CUMarshalService {
    
    File marshalObjectToXML(Object objectToMarshal, String outputFilePath) throws JAXBException, IOException;
    
    File marshalObjectToXMLFragment(XmlFragmentable objectToMarshal, String outputFilePath) throws JAXBException, IOException;

    void marshalObjectToXML(Object objectToMarshal, OutputStream outputStream) throws JAXBException, IOException;

    String marshalObjectToXmlString(Object objectToMarshal) throws JAXBException, IOException;
    
    String marshalObjectToXmlFragmentString(XmlFragmentable objectToMarshal) throws JAXBException, IOException;
    
    <T> T unmarshalFile(File xmlFile, Class<T> clazz) throws JAXBException;
    
    <T> T unmarshalString(String xmlString, Class<T> clazz) throws JAXBException;
    
    <T> T unmarshalStringIgnoreDtd(String xmlString, Class<T> clazz) throws JAXBException, IOException, XMLStreamException;

    <T> T unmarshalFile(File xmlFile, Class<T> clazz, Object listener) throws JAXBException;

    <T> T unmarshalString(String xmlString, Class<T> clazz, Object listener) throws JAXBException;

    <T> T unmarshalStream(InputStream inputStream, Class<T> clazz) throws JAXBException, IOException;
    
    <T> T unmarshalStreamIgnoreDtd(InputStream inputStream, Class<T> clazz) throws JAXBException, IOException, XMLStreamException;

    <T> T unmarshalStream(InputStream inputStream, Class<T> clazz, Object listener) throws JAXBException, IOException;

}
