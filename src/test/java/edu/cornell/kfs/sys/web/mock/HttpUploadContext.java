package edu.cornell.kfs.sys.web.mock;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.fileupload.UploadContext;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.Header;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Custom UploadContext implementation that allows for integrating Apache HTTP Core/Client with Commons FileUpload.
 */
public class HttpUploadContext implements UploadContext {
    private static final Logger LOG = LogManager.getLogger();

    private String characterEncoding;
    private String contentType;
    private long contentLength;
    private HttpEntity httpEntity;
    
    public HttpUploadContext(ClassicHttpRequest request) {
        this.characterEncoding = getHeaderValueOrUseDefault(request, HttpHeaders.CONTENT_ENCODING, StandardCharsets.UTF_8.displayName());
        this.contentType = getHeaderValue(request, HttpHeaders.CONTENT_TYPE);
        this.contentLength = Long.parseLong(getHeaderValueOrUseDefault(request, HttpHeaders.CONTENT_LENGTH, "-1"));
        this.httpEntity = request.getEntity();
    }
    
    private String getHeaderValue(ClassicHttpRequest request, String headerName) {
        return getHeaderValueOrUseDefault(request, headerName, null);
    }
    
    private String getHeaderValueOrUseDefault(ClassicHttpRequest request, String headerName, String defaultValue) {
        try {
            Header header = request.getHeader(headerName);
            return (header != null) ? header.getValue() : defaultValue;
        } catch (ProtocolException e) {
            LOG.error("getHeaderValueOrUseDefault, had and error getting the content", e);
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
        return httpEntity.getContent();
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
