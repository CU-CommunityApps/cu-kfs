package edu.cornell.kfs.coa.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.core.api.util.ClasspathOrFileResourceLoader;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;

import edu.cornell.kfs.coa.batch.CuCoaBatchConstants;
import edu.cornell.kfs.coa.batch.businessobject.LegacyAccountAttachment;
import edu.cornell.kfs.coa.web.mock.MockDownloadLegacyAccountAttachmentsController;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;
import edu.cornell.kfs.sys.web.mock.MockMvcWebServerExtension;

@Execution(ExecutionMode.SAME_THREAD)
public class DownloadLegacyAccountAttachmentsServiceImplTest {

    private static final String MOCK_API_KEY = "ABCD1234EFG567";
    private static final String OTHER_API_KEY = "ABCD1234EFG568";
    private static final String BASE_URL_SUFFIX = "/downloads";
    private static final String BASE_DIRECTORY = "edu/cornell/kfs/coa/legacy/fixture/";
    private static final String ATTACHMENT_FILE_1 = "attachment1";
    private static final String ATTACHMENT_FILE_2 = "secondattachment";

    @RegisterExtension
    MockMvcWebServerExtension mockServerExtension = new MockMvcWebServerExtension();

    private String serverUrl;
    private String apiKey;
    private MockDownloadLegacyAccountAttachmentsController mockDownloadAttachmentsEndpoint;
    private DownloadLegacyAccountAttachmentsServiceImpl downloadLegacyAccountAttachmentsService;

    private LegacyAccountAttachment processedAttachment;
    private byte[] actualFileContents;

    @BeforeEach
    void setUp() throws Exception {
        serverUrl = mockServerExtension.getServerUrl();
        apiKey = MOCK_API_KEY;
        mockDownloadAttachmentsEndpoint = new MockDownloadLegacyAccountAttachmentsController()
                .withExpectedApiKey(apiKey);
        downloadLegacyAccountAttachmentsService = new DownloadLegacyAccountAttachmentsServiceImpl();
        downloadLegacyAccountAttachmentsService.setWebServiceCredentialService(
                buildMockWebServiceCredentialService(serverUrl));

        mockServerExtension.initializeStandaloneMockMvcWithControllers(mockDownloadAttachmentsEndpoint);
    }

    private WebServiceCredentialService buildMockWebServiceCredentialService(final String baseUrl) {
        WebServiceCredentialService credentialService = Mockito.mock(WebServiceCredentialService.class);
        Mockito.when(credentialService.getWebServiceCredentialValue(
                CuCoaBatchConstants.DFA_ATTACHMENTS_GROUP_CODE, CuCoaBatchConstants.DFA_ATTACHMENTS_URL_KEY))
                .thenReturn(baseUrl + BASE_URL_SUFFIX);
        Mockito.when(credentialService.getWebServiceCredentialValue(
                CuCoaBatchConstants.DFA_ATTACHMENTS_GROUP_CODE, CuCoaBatchConstants.DFA_ATTACHMENTS_API_KEY))
                .then(invocation -> apiKey);
        return credentialService;
    }

    @AfterEach
    void tearDown() throws Exception {
        actualFileContents = null;
        processedAttachment = null;
        downloadLegacyAccountAttachmentsService = null;
        mockDownloadAttachmentsEndpoint = null;
        apiKey = null;
        serverUrl = null;
    }



    static Stream<String> validFiles() {
        return Stream.of(ATTACHMENT_FILE_1, ATTACHMENT_FILE_2);
    }

    static Stream<String> invalidFiles() {
        return Stream.of("file1", "nonexistent", "bad.syntax");
    }

    static Stream<String> variousFiles() {
        return Stream.of(validFiles(), invalidFiles()).flatMap(subStream -> subStream);
    }



    @ParameterizedTest
    @MethodSource("validFiles")
    void testSuccessfulFileDownload(final String fileName) throws Exception {
        assertAttachmentDownloadSucceeds(fileName);
    }

    @ParameterizedTest
    @MethodSource("invalidFiles")
    void testFailedFileDownloadDueToNonExistentOrBadlyNamedFiles(final String fileName) throws Exception {
        assertAttachmentDownloadFails(fileName);
    }

    @ParameterizedTest
    @MethodSource("variousFiles")
    void testFailedFileDownloadDueToBadApiKey(final String fileName) throws Exception {
        apiKey = OTHER_API_KEY;
        assertAttachmentDownloadFails(fileName);
    }

    @ParameterizedTest
    @MethodSource("variousFiles")
    void testFailedFileDownloadDueToUnexpectedServerResponse(final String fileName) throws Exception {
        mockDownloadAttachmentsEndpoint.setForceInternalServerError(true);
        assertAttachmentDownloadFails(fileName);
    }

    private void assertAttachmentDownloadSucceeds(final String fileName) throws Exception {
        final LegacyAccountAttachment attachment = createAttachmentEntry(fileName);
        final byte[] expectedFileContents = getExpectedFileContents(fileName);
        downloadLegacyAccountAttachmentsService.downloadAndProcessLegacyAccountAttachment(attachment,
                this::handleAttachment);
        assertNotNull(processedAttachment, "Attachment reference was not processed correctly after download");
        assertNotNull(actualFileContents, "Attachment contents were not processed correctly after download");
        assertEquals(attachment.getFileName(), processedAttachment.getFileName(), "Wrong attachment file name");
        assertEquals(attachment.getFilePath(), processedAttachment.getFilePath(), "Wrong attachment file path");
        assertArrayEquals(expectedFileContents, actualFileContents, "Unexpected file contents");
    }

    private void assertAttachmentDownloadFails(final String fileName) throws Exception {
        final LegacyAccountAttachment attachment = createAttachmentEntry(fileName);
        assertThrows(Exception.class,
                () -> downloadLegacyAccountAttachmentsService.downloadAndProcessLegacyAccountAttachment(attachment,
                        this::handleAttachment),
                "The attempted download should have failed");
        assertNull(processedAttachment, "The on-successful-download logic should not have been executed");
        assertNull(actualFileContents, "The on-successful-download logic should not have been executed");
    }

    private LegacyAccountAttachment createAttachmentEntry(final String fileName) {
        final LegacyAccountAttachment attachment = new LegacyAccountAttachment();
        attachment.setFileName(fileName + CUKFSConstants.TEXT_FILE_EXTENSION);
        attachment.setFilePath(CUKFSConstants.SLASH + BASE_DIRECTORY + fileName);
        return attachment;
    }

    private byte[] getExpectedFileContents(final String fileName) throws Exception {
        final Resource resource = new ClasspathOrFileResourceLoader().getResource(
                CUKFSConstants.CLASSPATH_PREFIX + BASE_DIRECTORY + fileName + CUKFSConstants.TEXT_FILE_EXTENSION);
        try (final InputStream inputStream = resource.getInputStream()) {
            return IOUtils.toByteArray(inputStream);
        }
    }

    private void handleAttachment(final LegacyAccountAttachment legacyAccountAttachment,
            final DataBuffer fileContents) throws IOException {
        processedAttachment = legacyAccountAttachment;
        try (final InputStream inputStream = fileContents.asInputStream()) {
            actualFileContents = IOUtils.toByteArray(inputStream);
        }
    }

}
