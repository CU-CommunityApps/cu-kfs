package edu.cornell.kfs.sys.web.mock;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.Filter;

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
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
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
import org.springframework.test.web.reactive.server.WebTestClient;
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
 * non-private instance field or static field annotated with "@RegisterExtension".
 * 
 * Prior to each test method being called, this class's internal MockMvc instance needs
 * to be initialized by the caller. Such setup can happen within a test class's annotated
 * "@BeforeEach" method (for an instance-level extension) or its "@BeforeAll" method
 * (for a class-level extension). The simplest way to perform the initialization is by calling
 * the initializeStandaloneMockMvcWithControllers() method and passing in the MVC Controllers
 * that you want to use. Alternatively, you can use either the setMockMvc() method or the
 * initializeStandaloneMockMvc() method if you need finer control over the MockMvc
 * instance's configuration.
 * 
 * To retrieve the HTTP server's URL, call the getServerUrl() method from within your test class's
 * "@BeforeEach"-annotated or "@BeforeAll"-annotated method(s). The returned URL will *not* contain
 * a trailing slash.
 * 
 * To create a WebTestClient instance that's bound to the local HTTP server, call the createWebTestClient()
 * method from within your test class's "@BeforeEach"-annotated or "@BeforeAll"-annotated method(s).
 * 
 * If the extension is set up at the class level, the test server will be initialized prior to
 * running the class's test suite and will be shut down after running the suite. If the configured
 * controller instances need to reset their state in between test methods, they can implement
 * the ResettableController interface and then this extension will reset them after each run
 * of a test method. (Note that this auto-reset feature is ignored if the extension was set up
 * as an instance variable or if the MVC configuration was set up via the setMockMvc() method.)
 * 
 * Note that if this extension is set up at the class/static level, it is strongly recommended
 * that the test class be configured for SAME_THREAD execution mode so that the test methods
 * don't interfere with each other.
 */
public class MockMvcWebServerExtension implements BeforeEachCallback, BeforeTestExecutionCallback, AfterEachCallback,
        BeforeAllCallback, AfterAllCallback {

    private static final String BASE_URL_PREFIX = "http://localhost:";
    private static final String ALL_URL_PATHS_PATTERN = "*";
    private static final Set<String> RESPONSE_DISALLOWED_AUTO_COPY_HEADERS = Set.of(
            HttpHeaders.CONTENT_TYPE.toLowerCase(Locale.US), HttpHeaders.CONTENT_LENGTH.toLowerCase(Locale.US));

    private boolean staticExtension;
    private List<ResettableController> resettableControllers;
    private ClassicTestServer testServer;
    private MockMvc mockMvc;
    private String serverUrl;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        staticExtension = true;
        initializeTestServer();
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        if (!staticExtension) {
            initializeTestServer();
        }
    }

    private void initializeTestServer() throws Exception {
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

    public WebTestClient createWebTestClient() {
        return WebTestClient.bindToServer()
                .baseUrl(getServerUrl())
                .build();
    }

    public void initializeStandaloneMockMvcWithControllers(final Object... controllers) {
        initializeStandaloneMockMvc(controllers, new Filter[0]);
    }

    public void initializeStandaloneMockMvc(final Object[] controllers, final Filter[] filters) {
        if (staticExtension) {
            this.resettableControllers = Stream.of(controllers)
                    .filter(controller -> controller instanceof ResettableController)
                    .map(controller -> (ResettableController) controller)
                    .collect(Collectors.toUnmodifiableList());
        }
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(controllers)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .addFilters(filters)
                .addInterceptors(new ForceUTF8TestResponseInterceptor())
                .setMessageConverters(getDefaultMessageConverters())
                .setAsyncRequestTimeout(15000L)
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
    public void afterAll(ExtensionContext context) throws Exception {
        shutDown();
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        if (staticExtension) {
            for (ResettableController controller : resettableControllers) {
                controller.reset();
            }
        } else {
            shutDown();
        }
    }

    private void shutDown() throws Exception {
        shutDownServerQuietly();
        serverUrl = null;
        mockMvc = null;
        testServer = null;
        resettableControllers = null;
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

        if (responseContent.length > 0) {
            ByteArrayEntity entity = new ByteArrayEntity(responseContent, contentType, false);
            response.setEntity(entity);
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
