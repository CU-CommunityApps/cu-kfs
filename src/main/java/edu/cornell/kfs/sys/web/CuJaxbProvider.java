package edu.cornell.kfs.sys.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.regex.Pattern;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Custom JAX-RS message reader/writer for handling JAXB-annotated POJOs.
 * This class allows POJOs containing the "jakarta.xml" JAXB annotations
 * to be usable by the Jersey 2.x framework, which normally only supports
 * the equivalent "javax.xml" annotations.
 * 
 * TODO: Remove this class when we either upgrade to Jersey 3.x or fully migrate from JAX-RS to Spring MVC.
 */
public class CuJaxbProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object> {

    private static final Pattern XML_MEDIA_TYPES_PATTERN = Pattern.compile(
            "^[a-z\\-+*]+/[a-z\\-+*]*xml([\\-+*][a-z\\-+*]*)?(;.*)?$", Pattern.CASE_INSENSITIVE);

    private CUMarshalService cuMarshalService = new CUMarshalServiceImpl();

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return isXmlMediaType(mediaType) && isAnnotatedWithXmlRootElement(type);
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return isXmlMediaType(mediaType) && isAnnotatedWithXmlRootElement(type);
    }

    private boolean isXmlMediaType(MediaType mediaType) {
        return XML_MEDIA_TYPES_PATTERN.matcher(mediaType.toString()).matches();
    }

    private boolean isAnnotatedWithXmlRootElement(Class<?> type) {
        return type.getAnnotation(XmlRootElement.class) != null;
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
                    throws IOException, WebApplicationException {
        try {
            return cuMarshalService.unmarshalStream(entityStream, type);
        } catch (JAXBException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void writeTo(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
                    throws IOException, WebApplicationException {
        try {
            cuMarshalService.marshalObjectToXML(t, entityStream);
        } catch (JAXBException e) {
            throw new IOException(e);
        }
    }

    @Override
    public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1L;
    }

}
