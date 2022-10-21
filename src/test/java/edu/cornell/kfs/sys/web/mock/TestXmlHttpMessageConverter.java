package edu.cornell.kfs.sys.web.mock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Custom HTTP message converter for converting JAXB pojos that use
 * the "jakarta.xml" annotations instead of the "javax.xml" annotations.
 * We may be able to remove this class once we upgrade to a Spring version
 * that has finished the javax-to-jakarta conversion.
 * 
 * TODO: Reevaluate the need for this class when updating Production XML-related code to use Spring MVC.
 */
public class TestXmlHttpMessageConverter extends AbstractHttpMessageConverter<Object> {

    private CUMarshalService cuMarshalService;

    public TestXmlHttpMessageConverter() {
        this.cuMarshalService = new CUMarshalServiceImpl();
        setSupportedMediaTypes(List.of(
                MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML,
                MediaType.APPLICATION_PROBLEM_XML, MediaType.APPLICATION_RSS_XML, MediaType.APPLICATION_XHTML_XML));
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return clazz.getAnnotation(XmlRootElement.class) != null;
    }

    @Override
    protected Object readInternal(Class<? extends Object> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        try (
            InputStream inputStream = inputMessage.getBody();
        ) {
            return cuMarshalService.unmarshalStream(inputStream, clazz);
        } catch (JAXBException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void writeInternal(Object pojo, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        try (
            OutputStream outputStream = outputMessage.getBody();
        ) {
            cuMarshalService.marshalObjectToXML(pojo, outputStream);
        } catch (JAXBException e) {
            throw new IOException(e);
        }
    }

}
