package edu.cornell.kfs.concur.batch;

import java.util.Objects;
import java.util.Optional;

import org.springframework.http.HttpMethod;

public class ConcurWebRequestBuilder<T> {

    private final Class<T> responseType;
    private String url;
    private HttpMethod httpMethod;
    private Optional<Object> jsonBody;

    public ConcurWebRequestBuilder(Class<T> responseType) {
        Objects.requireNonNull(responseType, "responseType cannot be null");
        this.responseType = responseType;
    }

    public static <R> ConcurWebRequestBuilder<R> forRequestExpectingResponseOfType(Class<R> responseType) {
        return new ConcurWebRequestBuilder<>(responseType);
    }

    public static ConcurWebRequestBuilder<Void> forRequestExpectingEmptyResponse() {
        return new ConcurWebRequestBuilder<>(Void.class);
    }

    public ConcurWebRequestBuilder<T> withUrl(String url) {
        this.url = url;
        return this;
    } 

    public ConcurWebRequestBuilder<T> withHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    public ConcurWebRequestBuilder<T> withJsonBody(Object jsonBody) {
        this.jsonBody = Optional.of(jsonBody);
        return this;
    }

    public ConcurWebRequestBuilder<T> withEmptyBody() {
        this.jsonBody = Optional.empty();
        return this;
    }

    public ConcurWebRequest<T> build() {
        if (jsonBody == null) {
            jsonBody = Optional.empty();
        }
        return new ConcurWebRequest<>(this);
    }

    public Class<T> getResponseType() {
        return responseType;
    }

    public String getUrl() {
        return url;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public Optional<Object> getJsonBody() {
        return jsonBody;
    }

}
