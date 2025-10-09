package edu.cornell.kfs.fp.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.kuali.kfs.sys.KFSConstants;

@Execution(ExecutionMode.SAME_THREAD)
public class AccountingXmlDocumentDownloadAttachmentServiceImplTest {

    private AccountingXmlDocumentDownloadAttachmentServiceImpl attachmentService;

    @BeforeEach
    void setUp() throws Exception {
        attachmentService = new AccountingXmlDocumentDownloadAttachmentServiceImpl();
    }

    @AfterEach
    void tearDown() throws Exception {
        attachmentService = null;
    }

    enum MimeTypeTestCase {
        PDF("test.pdf", "application/pdf"),
        PDF_UPPERCASE("TEST.PDF", "application/pdf"),
        PDF_EXTENSION_ONLY(".pdf", "application/pdf"),
        DOC("test.doc", "application/msword"),
        DOC_UPPERCASE("TEST.DOC", "application/msword"),
        DOCX("test.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
        XLSX("test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        XLS("test.xls", "application/vnd.ms-excel"),
        PNG("test.png", "image/png"),
        TXT("test.txt", "text/plain"),
        UNRECOGNIZED_TYPE("test.kfs", "kfs"),
        UNRECOGNIZED_TYPE_UPPERCASE("TEST.KFS", "kfs"),
        UNRECOGNIZED_TYPE_EXTENSION_ONLY(".kfs", "kfs"),
        FILENAME_WITHOUT_EXTENSION("test", "test"),
        FILENAME_WITHOUT_EXTENSION_UPPERCASE("TEST", "test"),
        EMPTY_FILENAME(StringUtils.EMPTY, StringUtils.EMPTY),
        BLANK_SPACE_FILENAME(KFSConstants.BLANK_SPACE, StringUtils.EMPTY),
        NULL_FILENAME(null, StringUtils.EMPTY);

        private final String fileName;
        private final String expectedMimeType;

        private MimeTypeTestCase(final String fileName, final String expectedMimeType) {
            this.fileName = fileName;
            this.expectedMimeType = expectedMimeType;
        }
    }

    @ParameterizedTest
    @EnumSource(MimeTypeTestCase.class)
    void testFindMimeType(final MimeTypeTestCase testCase) {
        final String actualMimeType = attachmentService.findMimeType(testCase.fileName);
        assertEquals(testCase.expectedMimeType, actualMimeType, "Wrong MIME Type");
    }

}
