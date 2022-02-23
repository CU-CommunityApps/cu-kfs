package edu.cornell.kfs.sys.web.mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.RequestLine;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.util.IOExceptionProneFunction;

/**
 * Base helper class for mocking an HTTP or RESTful remote service endpoint.
 * Allows the use of JUnit assertions for convenience, but will convert
 * the assertion errors into HTTP responses appropriately, to prevent
 * "connection reset" errors on the client side.
 */
public abstract class MockServiceEndpointBase implements HttpRequestHandler {

	private static final Logger LOG = LogManager.getLogger(MockServiceEndpointBase.class);

    protected String multiPartContentDirectory;

    public abstract String getRelativeUrlPatternForHandlerRegistration();

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        try {
            processRequest(request, response, context);
        } catch (AssertionError e) {
            LOG.error("handle(): An assertion failed during the web service call", e);
            prepareResponseForFailedAssertion(response, e);
        }
    }

    protected abstract void processRequest(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException;

    protected void prepareResponseForFailedAssertion(HttpResponse response, AssertionError assertionError) throws HttpException, IOException {
        response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
        response.setReasonPhrase(assertionError.getMessage());
    }

    protected void assertRequestHasCorrectHttpMethod(HttpRequest request, HttpMethod expectedMethod) {
        RequestLine requestLine = request.getRequestLine();
        assertEquals("Wrong HTTP method for request", expectedMethod.name(), requestLine.getMethod());
    }

    protected void assertRequestHasCorrectContentType(HttpRequest request, ContentType expectedContentType) {
        assertRequestHasCorrectContentType(request, expectedContentType.getMimeType());
    }

    protected void assertRequestHasCorrectContentType(HttpRequest request, String expectedContentType) {
        String actualContentType = getNonBlankHeaderValue(request, HttpHeaders.CONTENT_TYPE);
        if (StringUtils.equals(expectedContentType, ContentType.MULTIPART_FORM_DATA.getMimeType())
                && StringUtils.contains(actualContentType, CUKFSConstants.SEMICOLON)) {
            actualContentType = StringUtils.substringBefore(actualContentType, CUKFSConstants.SEMICOLON);
        }
        assertEquals("Wrong content type for request", expectedContentType, actualContentType);
    }

    protected void assertHeaderHasNonBlankValue(HttpRequest request, String headerName) {
        getNonBlankHeaderValue(request, headerName);
    }

    protected String getNonBlankHeaderValue(HttpRequest request, String headerName) {
        return getValueOrFail(
                () -> getNonBlankHeaderValueIfPresent(request, headerName),
                "The request should have had a non-blank value for header " + headerName);
    }

    protected Optional<String> getNonBlankHeaderValueIfPresent(HttpRequest request, String headerName) {
        Header header = request.getFirstHeader(headerName);
        if (header == null) {
            return Optional.empty();
        } else {
            return defaultToEmptyOptionalIfBlank(header.getValue());
        }
    }

    protected String getNonBlankValueFromRequestUrl(HttpRequest request, Pattern regex, int regexGroupIndex) {
        return getValueOrFail(
                () -> getNonBlankValueFromRequestUrlIfPresent(request, regex, regexGroupIndex),
                "The request URL did not contain the expected non-blank fragment according to the given regex");
    }

    protected Optional<String> getNonBlankValueFromRequestUrlIfPresent(HttpRequest request, Pattern regex, int regexGroupIndex) {
        RequestLine requestLine = request.getRequestLine();
        String requestUrl = requestLine.getUri();
        Matcher urlMatcher = regex.matcher(requestUrl);
        
        if (regexGroupIndex < 0 || urlMatcher.groupCount() < regexGroupIndex) {
            throw new IllegalArgumentException("Regex does not have a capturing group with an index of " + regexGroupIndex);
        } else if (!urlMatcher.matches()) {
            return Optional.empty();
        } else {
            return defaultToEmptyOptionalIfBlank(urlMatcher.group(regexGroupIndex));
        }
    }

    protected String getRequestContentAsString(HttpRequest request) {
        return getRequestContent(request,
                (contentStream) -> IOUtils.toString(contentStream, StandardCharsets.UTF_8));
    }

    protected JsonNode getRequestContentAsJsonNodeTree(HttpRequest request) {
        return getRequestContent(request,
                (contentStream) -> new ObjectMapper().readTree(contentStream));
    }

    protected <R> R getRequestContent(HttpRequest request, IOExceptionProneFunction<InputStream, R> entityContentConverter) {
        assertTrue("The request should have contained an entity", request instanceof HttpEntityEnclosingRequest);
        
        HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest) request;
        HttpEntity entity = entityRequest.getEntity();
        InputStream entityContent = null;
        R convertedContent = null;
        
        try {
            entityContent = entity.getContent();
            convertedContent = entityContentConverter.apply(entityContent);
        } catch (Exception e) {
            fail("Unexpected error reading request content: " + e.getMessage());
        } finally {
            IOUtils.closeQuietly(entityContent);
        }
        
        return convertedContent;
    }

    protected <T> T processMultiPartRequestContent(HttpRequest request, BiFunction<HttpRequest, List<FileItem>, T> requestContentHandler) {
        if (StringUtils.isBlank(multiPartContentDirectory)) {
            throw new IllegalStateException("Directory path to potentially store multipart content has not been specified");
        }
        
        List<FileItem> fileItems = null;
        DiskFileItemFactory fileItemFactory = new DiskFileItemFactory(DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD, new File(multiPartContentDirectory));
        FileUpload fileUpload = new FileUpload(fileItemFactory);
        
        try {
            RequestContext context = new HttpUploadContext(request);
            fileItems = fileUpload.parseRequest(context);
            return requestContentHandler.apply(request, fileItems);
        } catch (FileUploadException e) {
            throw new RuntimeException(e);
        } finally {
            if (CollectionUtils.isNotEmpty(fileItems)) {
                fileItems.stream()
                        .forEach(this::deleteFileItemQuietly);
            }
        }
    }

    protected void deleteFileItemQuietly(FileItem fileItem) {
        try {
            fileItem.delete();
        } catch (Exception e) {
            LOG.warn("deleteFileItemQuietly: Unexpected exception when deleting file item", e);
        }
    }

    protected Optional<String> defaultToEmptyOptionalIfBlank(String value) {
        String valueToWrap = StringUtils.defaultIfBlank(value, null);
        return Optional.ofNullable(valueToWrap);
    }

    protected <T> T getValueOrFail(Supplier<Optional<T>> valueRetriever, String failureMessage) {
        Optional<T> value = valueRetriever.get();
        if (!value.isPresent()) {
            fail(failureMessage);
        }
        return value.get();
    }

    protected String buildJsonTextFromNode(Consumer<ObjectNode> jsonNodeConfigurer) {
        String jsonText = null;
        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
            ObjectNode rootNode = nodeFactory.objectNode();
            jsonNodeConfigurer.accept(rootNode);
            jsonText = objectMapper.writeValueAsString(rootNode);
        } catch (JsonProcessingException e) {
            fail("Unexpected error when preparing JSON output: " + e.getMessage());
        }
        
        return jsonText;
    }

    protected void onServerInitialized(String baseUrl) {
        
    }

}
