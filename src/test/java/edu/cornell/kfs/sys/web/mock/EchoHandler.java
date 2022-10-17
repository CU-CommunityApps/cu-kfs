package edu.cornell.kfs.sys.web.mock;

import java.io.IOException;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;

public class EchoHandler implements HttpRequestHandler {

    @Override
    public void handle(final HttpRequest request, final HttpResponse response, final HttpContext context)
            throws HttpException, IOException {

        final String method = request.getRequestLine().getMethod().toUpperCase(Locale.ROOT);
        if (!"GET".equals(method) && !"POST".equals(method) && !"PUT".equals(method)) {
            throw new MethodNotSupportedException(method + " not supported by " + getClass().getName());
        }

        HttpEntity entity = null;
        if (request instanceof HttpEntityEnclosingRequest) {
            entity = ((HttpEntityEnclosingRequest) request).getEntity();
        }

        byte[] data;
        if (entity == null) {
            data = new byte[0];
        } else {
            data = EntityUtils.toByteArray(entity);
        }

        final ByteArrayEntity bae = new ByteArrayEntity(data);
        if (entity != null) {
            bae.setContentType(entity.getContentType());
        }
        entity = bae;

        response.setStatusCode(HttpStatus.SC_OK);
        response.setEntity(entity);

    }

}
