package edu.cornell.kfs.concur.batch;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.http.HttpMethod;

public final class ConcurWebRequest<T> {

    private final String url;
    private final HttpMethod httpMethod;
    private final Optional<Object> jsonBody;
    private final Class<T> responseType;

    public ConcurWebRequest(ConcurWebRequestBuilder<T> builder) {
        this.url = builder.getUrl();
        this.httpMethod = builder.getHttpMethod();
        this.jsonBody = builder.getJsonBody();
        this.responseType = builder.getResponseType();
        
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("url cannot be blank");
        }
        Objects.requireNonNull(httpMethod, "httpMethod cannot be null");
        Objects.requireNonNull(jsonBody, "jsonBody wrapper cannot be null");
        Objects.requireNonNull(responseType, "responseType cannot be null");
    }

    public String getUrl() {
        return url;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public String getHttpMethodAsString() {
        return httpMethod.name();
    }

    public boolean hasJsonBody() {
        return jsonBody.isPresent();
    }

    public Object getJsonBody() {
        return jsonBody.get();
    }

    public Class<T> getResponseType() {
        return responseType;
    }

    public boolean expectsEmptyResponse() {
        return Void.class.isAssignableFrom(responseType);
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
