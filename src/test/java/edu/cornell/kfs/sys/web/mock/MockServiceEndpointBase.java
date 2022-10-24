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
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
        try {
            processRequest(request, response, context);
        } catch (AssertionError e) {
            LOG.error("handle(): An assertion failed during the web service call", e);
            prepareResponseForFailedAssertion(response, e);
        }
    }

    protected abstract void processRequest(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context) throws HttpException, IOException;

    protected void prepareResponseForFailedAssertion(ClassicHttpResponse response, AssertionError assertionError) throws HttpException, IOException {
        response.setCode(HttpStatus.SC_BAD_REQUEST);
        response.setReasonPhrase(assertionError.getMessage());
    }

    protected void assertRequestHasCorrectHttpMethod(ClassicHttpRequest request, HttpMethod expectedMethod) {
        assertEquals("Wrong HTTP method for request", expectedMethod.name(), request.getMethod());
    }

    protected void assertRequestHasCorrectContentType(ClassicHttpRequest request, ContentType expectedContentType) {
        assertRequestHasCorrectContentType(request, expectedContentType.getMimeType());
    }

    protected void assertRequestHasCorrectContentType(ClassicHttpRequest request, String expectedContentType) {
        String actualContentType = getNonBlankHeaderValue(request, HttpHeaders.CONTENT_TYPE);
        if (StringUtils.equals(expectedContentType, ContentType.MULTIPART_FORM_DATA.getMimeType())
                && StringUtils.contains(actualContentType, CUKFSConstants.SEMICOLON)) {
            actualContentType = StringUtils.substringBefore(actualContentType, CUKFSConstants.SEMICOLON);
        }
        assertEquals("Wrong content type for request", expectedContentType, actualContentType);
    }

    protected void assertHeaderHasNonBlankValue(ClassicHttpRequest request, String headerName) {
        getNonBlankHeaderValue(request, headerName);
    }

    protected String getNonBlankHeaderValue(ClassicHttpRequest request, String headerName) {
        return getValueOrFail(
                () -> getNonBlankHeaderValueIfPresent(request, headerName),
                "The request should have had a non-blank value for header " + headerName);
    }

    protected Optional<String> getNonBlankHeaderValueIfPresent(ClassicHttpRequest request, String headerName) {
        Header header = request.getFirstHeader(headerName);
        if (header == null) {
            return Optional.empty();
        } else {
            return defaultToEmptyOptionalIfBlank(header.getValue());
        }
    }

    protected String getNonBlankValueFromRequestUrl(ClassicHttpRequest request, Pattern regex, int regexGroupIndex) {
        return getValueOrFail(
                () -> getNonBlankValueFromRequestUrlIfPresent(request, regex, regexGroupIndex),
                "The request URL did not contain the expected non-blank fragment according to the given regex");
    }

    protected Optional<String> getNonBlankValueFromRequestUrlIfPresent(ClassicHttpRequest request, Pattern regex, int regexGroupIndex) {
        String requestUrl = request.getRequestUri();
        Matcher urlMatcher = regex.matcher(requestUrl);
        
        if (regexGroupIndex < 0 || urlMatcher.groupCount() < regexGroupIndex) {
            throw new IllegalArgumentException("Regex does not have a capturing group with an index of " + regexGroupIndex);
        } else if (!urlMatcher.matches()) {
            return Optional.empty();
        } else {
            return defaultToEmptyOptionalIfBlank(urlMatcher.group(regexGroupIndex));
        }
    }

    protected String getRequestContentAsString(ClassicHttpRequest request) {
        return getRequestContent(request,
                (contentStream) -> IOUtils.toString(contentStream, StandardCharsets.UTF_8));
    }

    protected JsonNode getRequestContentAsJsonNodeTree(ClassicHttpRequest request) {
        return getRequestContent(request,
                (contentStream) -> new ObjectMapper().readTree(contentStream));
    }

    protected <R> R getRequestContent(ClassicHttpRequest request, IOExceptionProneFunction<InputStream, R> entityContentConverter) {
        assertTrue("The request should have contained an entity", request instanceof BasicClassicHttpRequest);
        
        HttpEntity entity = request.getEntity();
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

    protected <T> T processMultiPartRequestContent(ClassicHttpRequest request, BiFunction<ClassicHttpRequest, List<FileItem>, T> requestContentHandler) {
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