package edu.cornell.kfs.fp.batch.service.impl;

import static org.junit.Assert.*;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.sys.KFSConstants;

public class AccountingXmlDocumentDownloadAttachmentServiceImplTest {
    
    private AccountingXmlDocumentDownloadAttachmentServiceImpl attachmentService;

    @Before
    public void setUp() throws Exception {
        attachmentService = new AccountingXmlDocumentDownloadAttachmentServiceImpl();
    }

    @After
    public void tearDown() throws Exception {
        attachmentService = null;
    }

    @Test
    public void testFindMimeTypePdf() {
        String mimeType = attachmentService.findMimeType("test.pdf");
        assertEquals("application/pdf", mimeType);
    }
    
    @Test
    public void testFindMimeTypePDF() {
        String mimeType = attachmentService.findMimeType("TEST.PDF");
        assertEquals("application/pdf", mimeType);
    }
    
    @Test
    public void testFindMimeTypeDoc() {
        String mimeType = attachmentService.findMimeType("test.doc");
        assertEquals("doc", mimeType);
    }
    
    @Test
    public void testFindMimeTypeDOC() {
        String mimeType = attachmentService.findMimeType("TEST.DOC");
        assertEquals("doc", mimeType);
    }
    
    @Test
    public void testFindMimeTypeXlsx() {
        String mimeType = attachmentService.findMimeType("test.xlsx");
        assertEquals("xlsx", mimeType);
    }
    
    @Test
    public void testFindMimeTypeXls() {
        String mimeType = attachmentService.findMimeType("test.xls");
        assertEquals("xls", mimeType);
    }
    
    @Test
    public void testFindMimeTypeCsv() {
        String mimeType = attachmentService.findMimeType("test.csv");
        assertEquals("csv", mimeType);
    }
    
    @Test
    public void testFindMimeTypeBadFileName() {
        String mimeType = attachmentService.findMimeType("test");
        assertEquals("test", mimeType);
    }
    
    @Test
    public void testFindMimeTypeEmptyFileName() {
        String mimeType = attachmentService.findMimeType(StringUtils.EMPTY);
        assertEquals(StringUtils.EMPTY, mimeType);
    }
    
    @Test
    public void testFindMimeTypeSpaceFileName() {
        String mimeType = attachmentService.findMimeType(KFSConstants.BLANK_SPACE);
        assertEquals(StringUtils.EMPTY, mimeType);
    }
    
    @Test
    public void testFindMimeTypeNullFileName() {
        String mimeType = attachmentService.findMimeType(null);
        assertEquals(StringUtils.EMPTY, mimeType);
    }

}
