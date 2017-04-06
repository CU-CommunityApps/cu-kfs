package edu.cornell.kfs.sys.service;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

public interface CUMarshalService {
    
    File marshalObjectToXML(Object objectToMarshal, String outputFilePath) throws JAXBException, IOException;
    
    <T> T unmarshalFile(File xmlFile, Class<T> clazz) throws JAXBException;
    
    <T> T unmarshalString(String xmlString, Class<T> clazz) throws JAXBException;

}
