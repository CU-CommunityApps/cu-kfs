package edu.cornell.kfs.coa.web.mock;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.util.ClasspathOrFileResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.cornell.kfs.coa.batch.CuCoaBatchConstants;
import edu.cornell.kfs.sys.CUKFSConstants;

@RestController
public class MockDownloadLegacyAccountAttachmentsController {

    private static final Pattern FILE_PATH_PATTERN = Pattern.compile("^(/\\w+)+$");
    private static final String VALID_PATH = "/edu/cornell/kfs/coa/legacy/fixture/";
    private static final String FILE_PATH_VARIABLE = "filePath";

    private String expectedApiKey;
    private AtomicBoolean forceInternalServerError = new AtomicBoolean(false);

    public MockDownloadLegacyAccountAttachmentsController withExpectedApiKey(final String expectedApiKey) {
        setExpectedApiKey(expectedApiKey);
        return this;
    }

    public void setExpectedApiKey(final String expectedApiKey) {
        this.expectedApiKey = expectedApiKey;
    }

    public void setForceInternalServerError(boolean forceInternalServerError) {
        this.forceInternalServerError.set(forceInternalServerError);
    }

    @GetMapping(path = "/downloads/{*filePath}")
    public ResponseEntity<byte[]> getAttachment(
            @RequestHeader(CuCoaBatchConstants.DFA_ATTACHMENTS_API_KEY) final String apiKey,
            @PathVariable(FILE_PATH_VARIABLE) final String filePath
    ) {
        if (forceInternalServerError.get()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Forcing internal server error");
        }
        checkApiKey(apiKey);
        final Resource resource = getAndCheckAttachmentResource(filePath);
        final byte[] fileContents = readResource(resource);
        return ResponseEntity.ok(fileContents);
    }

    private void checkApiKey(final String apiKey) {
        if (StringUtils.isBlank(apiKey)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "API key is missing or empty");
        } else if (!StringUtils.equals(apiKey, expectedApiKey)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Not authorized due to malformed or unrecognized API key");
        }
    }

    private Resource getAndCheckAttachmentResource(final String filePath) {
        if (StringUtils.isBlank(filePath)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File path is missing or empty");
        } else if (!FILE_PATH_PATTERN.matcher(filePath).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File path is malformed");
        } else if (!StringUtils.startsWithIgnoreCase(filePath, VALID_PATH)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }
        final String filePathWithoutFirstSlash = StringUtils.substringAfter(filePath, CUKFSConstants.SLASH);
        final Resource resource = new ClasspathOrFileResourceLoader().getResource(
                CUKFSConstants.CLASSPATH_PREFIX + filePathWithoutFirstSlash + CUKFSConstants.TEXT_FILE_EXTENSION);
        if (!resource.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }
        return resource;
    }

    private byte[] readResource(final Resource resource) {
        try (final InputStream inputStream = resource.getInputStream()) {
            return IOUtils.toByteArray(inputStream);
        } catch (final Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
