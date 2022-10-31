package edu.cornell.kfs.sys.web;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;

import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.internal.MultiPartWriter;

/**
 * Custom MessageBodyWriter that lazily initializes a Jersey MultiPartWriter and delegates the writer calls to it.
 * This implementation allows for field injection (instead of constructor injection) for the needed Providers implementation,
 * as a temporary workaround for using certain Jersey classes with non-Jersey JAX-RS clients.
 */
public class CuMultiPartWriter implements MessageBodyWriter<MultiPart> {

    @Context
    private Providers providers;

    private MultiPartWriter actualWriter;

    private MultiPartWriter getActualWriter() {
        if (actualWriter == null) {
            if (providers == null) {
                throw new IllegalStateException("providers object cannot be null");
            }
            actualWriter = new MultiPartWriter(providers);
        }
        return actualWriter;
    }

    @Override
    public long getSize(MultiPart entity, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return getActualWriter().getSize(entity, type, genericType, annotations, mediaType);
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return getActualWriter().isWriteable(type, genericType, annotations, mediaType);
    }

    @Override
    public void writeTo(MultiPart entity, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> headers, OutputStream stream) throws IOException, WebApplicationException {
        getActualWriter().writeTo(entity, type, genericType, annotations, mediaType, headers, stream);
    }

}