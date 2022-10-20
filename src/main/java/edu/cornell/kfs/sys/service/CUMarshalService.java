package edu.cornell.kfs.sys.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jakarta.xml.bind.JAXBException;

public interface CUMarshalService {
    
    File marshalObjectToXML(Object objectToMarshal, String outputFilePath) throws JAXBException, IOException;

    void marshalObjectToXML(Object objectToMarshal, OutputStream outputStream) throws JAXBException, IOException;

    String marshalObjectToXmlString(Object objectToMarshal) throws JAXBException, IOException;
    
    <T> T unmarshalFile(File xmlFile, Class<T> clazz) throws JAXBException;
    
    <T> T unmarshalString(String xmlString, Class<T> clazz) throws JAXBException;

    <T> T unmarshalFile(File xmlFile, Class<T> clazz, Object listener) throws JAXBException;

    <T> T unmarshalString(String xmlString, Class<T> clazz, Object listener) throws JAXBException;

    <T> T unmarshalStream(InputStream inputStream, Class<T> clazz) throws JAXBException, IOException;

    <T> T unmarshalStream(InputStream inputStream, Class<T> clazz, Object listener) throws JAXBException, IOException;

}
