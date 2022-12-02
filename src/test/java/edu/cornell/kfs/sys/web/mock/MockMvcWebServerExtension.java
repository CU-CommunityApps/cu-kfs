package edu.cornell.kfs.sys.web.mock;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.NullEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.testing.classic.ClassicTestServer;
import org.apache.hc.core5.util.Timeout;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import edu.cornell.kfs.sys.CUKFSConstants;

/**
 * Helper class that boots up a simple local HTTP server and delegates the handling
 * of web requests to Spring MVC. This simplifies the process of testing KFS code
 * that makes calls to remote web services (especially remote RESTful services),
 * by allowing for the use of Spring MVC Controllers to handle the web requests.
 * 
 * This class is meant to be used as a *programmatically-configured* JUnit 5 extension.
 * Your test class should set up an instance of this extension by declaring an appropriate
 * non-private instance field annotated with "@RegisterExtension".
 * 
 * Prior to each test method being called, this class's internal MockMvc instance needs
 * to be initialized by the caller. Such setup can happen within an annotated "@BeforeEach"
 * method on the test class. The simplest way to perform the initialization is by calling
 * the initializeStandaloneMockMvcWithControllers() method and passing in the MVC Controllers
 * that you want to use. Alternatively, you can use the setMockMvc() method if you need
 * finer control over the MockMvc instance's configuration.
 * 
 * To retrieve the HTTP server's URL, call the getServerUrl() method from within your test class's
 * "@BeforeEach"-annotated method(s). The returned URL will *not* contain a trailing slash.
 */
public class MockMvcWebServerExtension implements BeforeEachCallback, BeforeTestExecutionCallback, AfterEachCallback {

    private static final String BASE_URL_PREFIX = "http://localhost:";
    private static final String ALL_URL_PATHS_PATTERN = "*";
    private static final Set<String> RESPONSE_DISALLOWED_AUTO_COPY_HEADERS = Set.of(
            HttpHeaders.CONTENT_TYPE.toLowerCase(Locale.US), HttpHeaders.CONTENT_LENGTH.toLowerCase(Locale.US));

    private ClassicTestServer testServer;
    private MockMvc mockMvc;
    private String serverUrl;

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        this.testServer = new ClassicTestServer(null,
                SocketConfig.custom()
                        .setSoTimeout(Timeout.ofMinutes(1L))
                        .build());
        testServer.registerHandler(ALL_URL_PATHS_PATTERN, this::handleMockRequest);
        testServer.start();
        
        this.serverUrl = BASE_URL_PREFIX + String.valueOf(testServer.getPort());
    }

    public String getServerUrl() {
        if (StringUtils.isBlank(serverUrl)) {
            throw new IllegalStateException("The test server has not been initialized");
        }
        return serverUrl;
    }

    public void initializeStandaloneMockMvcWithControllers(Object... controllers) {
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(controllers)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .addInterceptors(new ForceUTF8TestResponseInterceptor())
                .setMessageConverters(getDefaultMessageConverters())
                .build();
    }

    private HttpMessageConverter<?>[] getDefaultMessageConverters() {
        return new HttpMessageConverter<?>[] {
            new ByteArrayHttpMessageConverter(),
            new StringHttpMessageConverter(),
            new ResourceHttpMessageConverter(),
            new SourceHttpMessageConverter<>(),
            new FormHttpMessageConverter(),
            new TestXmlHttpMessageConverter(),
            new MappingJackson2HttpMessageConverter()
        };
    }

    public void setMockMvc(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        if (mockMvc == null) {
            throw new IllegalStateException("MockMvc instance has not been initialized");
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        shutDownServerQuietly();
        serverUrl = null;
        mockMvc = null;
        testServer = null;
    }

    private void shutDownServerQuietly() {
        if (testServer != null) {
            try {
                testServer.shutdown(CloseMode.IMMEDIATE);
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    private void handleMockRequest(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context)
            throws HttpException, IOException {
        try {
            MockHttpServletRequestBuilder servletRequest = buildMockServletRequestFromHttpRequest(request);
            MvcResult mvcResult = mockMvc.perform(servletRequest).andReturn();
            MockHttpServletResponse servletResponse = mvcResult.getResponse();
            populateHttpResponseFromServletResponse(response, servletResponse);
        } catch (HttpException | IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HttpException("Unexpected error occurred while handling HTTP request", e);
        }
    }

    private MockHttpServletRequestBuilder buildMockServletRequestFromHttpRequest(ClassicHttpRequest request)
            throws Exception {
        URI uri = new URI(serverUrl + request.getPath());
        HttpHeaders httpHeaders = buildSpringHttpHeaders(request);
        byte[] requestContent = getHttpRequestContentAsByteArray(request);
        MockHttpServletRequestBuilder servletRequest = MockMvcRequestBuilders
                .request(request.getMethod(), uri)
                .headers(httpHeaders)
                .content(requestContent);
        return servletRequest;
    }

    private HttpHeaders buildSpringHttpHeaders(ClassicHttpRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        for (Header header : request.getHeaders()) {
            httpHeaders.add(header.getName(), header.getValue());
        }
        return httpHeaders;
    }

    private byte[] getHttpRequestContentAsByteArray(ClassicHttpRequest request) throws IOException {
        HttpEntity entity = request.getEntity();
        if (entity == null || entity instanceof NullEntity) {
            return new byte[0];
        }
        try (
            InputStream requestContent = entity.getContent();
        ) {
            return IOUtils.toByteArray(requestContent);
        }
    }

    private void populateHttpResponseFromServletResponse(ClassicHttpResponse response,
            MockHttpServletResponse servletResponse) throws Exception {
        response.setCode(servletResponse.getStatus());
        populateHttpResponseHeadersFromServletResponse(response, servletResponse);
        populateHttpResponseContentFromServletResponse(response, servletResponse);
    }

    private void populateHttpResponseHeadersFromServletResponse(ClassicHttpResponse response,
            MockHttpServletResponse servletResponse) {
        for (String headerName : servletResponse.getHeaderNames()) {
            if (RESPONSE_DISALLOWED_AUTO_COPY_HEADERS.contains(StringUtils.lowerCase(headerName, Locale.US))) {
                continue;
            }
            List<Object> headerValues = servletResponse.getHeaderValues(headerName);
            if (CollectionUtils.isNotEmpty(headerValues)) {
                for (Object headerValue : headerValues) {
                    response.addHeader(headerName, headerValue);
                }
            } else {
                response.addHeader(headerName, null);
            }
        }
    }

    private void populateHttpResponseContentFromServletResponse(ClassicHttpResponse response,
            MockHttpServletResponse servletResponse) {
        String contentMimeType = getBareContentMimeTypeFromServletResponse(servletResponse);
        String charset = servletResponse.getCharacterEncoding();
        
        byte[] responseContent = servletResponse.getContentAsByteArray();
        if (responseContent == null) {
            responseContent = new byte[0];
        }
        
        ContentType contentType = null;
        if (StringUtils.isNotBlank(contentMimeType)) {
            contentType = StringUtils.isNotBlank(charset)
                    ? ContentType.create(contentMimeType, charset)
                    : ContentType.create(contentMimeType);
        }
        
        if (contentType != null) {
            ByteArrayEntity entity = new ByteArrayEntity(responseContent, contentType, false);
            response.setEntity(entity);
        } else if (responseContent.length > 0) {
            throw new IllegalStateException("Response had non-empty content but had no Content-Type defined");
        }
    }

    private String getBareContentMimeTypeFromServletResponse(MockHttpServletResponse servletResponse) {
        String contentMimeType = servletResponse.getContentType();
        if (StringUtils.contains(contentMimeType, CUKFSConstants.SEMICOLON)) {
            contentMimeType = StringUtils.substringBefore(contentMimeType, CUKFSConstants.SEMICOLON);
        }
        return contentMimeType;
    }

}
