package edu.cornell.kfs.sys.web.mock;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.fileupload.UploadContext;
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

    public HttpUploadContext(HttpRequest request) {
        this.characterEncoding = getHeaderValueOrUseDefault(request, HttpHeaders.CONTENT_ENCODING, StandardCharsets.UTF_8.displayName());
        this.contentType = getHeaderValue(request, HttpHeaders.CONTENT_TYPE);
        this.contentLength = Long.parseLong(getHeaderValueOrUseDefault(request, HttpHeaders.CONTENT_LENGTH, "-1"));
        this.httpEntity = ((HttpEntityEnclosingRequest) request).getEntity();
    }

    private String getHeaderValue(HttpRequest request, String headerName) {
        return getHeaderValueOrUseDefault(request, headerName, null);
    }

    private String getHeaderValueOrUseDefault(HttpRequest request, String headerName, String defaultValue) {
        Header header = request.getFirstHeader(headerName);
        return (header != null) ? header.getValue() : defaultValue;
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
