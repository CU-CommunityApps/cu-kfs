package edu.cornell.kfs.sys.web.mock;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.fileupload.UploadContext;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;

/**
 * Custom UploadContext implementation that allows for integrating Apache HTTP Core/Client with Commons FileUpload.
 */
public class HttpUploadContext implements UploadContext {

    private String characterEncoding;
    private String contentType;
    private long contentLength;
    private HttpEntity httpEntity;
    private org.apache.hc.core5.http.HttpEntity core4HttpEntity;

    public HttpUploadContext(HttpRequest request) {
        this.characterEncoding = getHeaderValueOrUseDefault(request, HttpHeaders.CONTENT_ENCODING, StandardCharsets.UTF_8.displayName());
        this.contentType = getHeaderValue(request, HttpHeaders.CONTENT_TYPE);
        this.contentLength = Long.parseLong(getHeaderValueOrUseDefault(request, HttpHeaders.CONTENT_LENGTH, "-1"));
        this.httpEntity = ((HttpEntityEnclosingRequest) request).getEntity();
    }
    
    public HttpUploadContext(ClassicHttpRequest request) {
        this.characterEncoding = getHeaderValueOrUseDefault(request, HttpHeaders.CONTENT_ENCODING, StandardCharsets.UTF_8.displayName());
        this.contentType = getHeaderValue(request, HttpHeaders.CONTENT_TYPE);
        this.contentLength = Long.parseLong(getHeaderValueOrUseDefault(request, HttpHeaders.CONTENT_LENGTH, "-1"));
        this.core4HttpEntity = request.getEntity();
    }

    private String getHeaderValue(HttpRequest request, String headerName) {
        return getHeaderValueOrUseDefault(request, headerName, null);
    }
    
    private String getHeaderValue(ClassicHttpRequest request, String headerName) {
        return getHeaderValueOrUseDefault(request, headerName, null);
    }

    private String getHeaderValueOrUseDefault(HttpRequest request, String headerName, String defaultValue) {
        Header header = request.getFirstHeader(headerName);
        return (header != null) ? header.getValue() : defaultValue;
    }
    
    private String getHeaderValueOrUseDefault(ClassicHttpRequest request, String headerName, String defaultValue) {
        try {
            org.apache.hc.core5.http.Header header = request.getHeader(headerName);
            return (header != null) ? header.getValue() : defaultValue;
        } catch (ProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return defaultValue;
        }
        
    }

    @Override
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (httpEntity != null) {
            return httpEntity.getContent();
        } else if (core4HttpEntity != null) {
            return core4HttpEntity.getContent();
        } else {
            throw new IllegalStateException("No HTTP entity enetered");
        }
    }

    @Override
    public long contentLength() {
        return contentLength;
    }

    @Override
    public int getContentLength() {
        return (int) contentLength();
    }

}
