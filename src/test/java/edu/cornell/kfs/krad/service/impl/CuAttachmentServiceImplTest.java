package edu.cornell.kfs.krad.service.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.service.impl.AttachmentServiceImpl;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.util.Guid;
import org.kuali.kfs.vnd.businessobject.PhoneType;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import edu.cornell.kfs.krad.antivirus.service.ScanResult;
import edu.cornell.kfs.krad.antivirus.service.impl.DummyAntiVirusServiceImpl;
import edu.cornell.kfs.krad.dao.impl.CuAttachmentDaoOjb;

public class CuAttachmentServiceImplTest {

    private static final String ATTACHMENT_TEST_FILE_PATH = "src/test/resources/edu/cornell/kfs/krad/service/fixture";
    private static final String GOOD_FILE_CONTENTS = "This is a pretend good attachment file.";
    private static final String GOOD_FILE_NAME = "good.txt";
    private static final String TEST_PATH = "test";
    private static final String TEST_ATTACHMENTS_PATH = TEST_PATH + File.separator + "attachments";
    private static final String VIRUS_FILE_CONTENTS = "This is a pretend virus infected attachment file.";
    private static final String VIRUS_FILE_NAME = "virus.txt";
    private static final String ERROR_FILE_NAME = "error.txt";

    private CuAttachmentServiceImpl attachmentService;
    private Attachment attachment;
    private Note noteVirus;
    
    private InputStream virusInputStream;
    private InputStream goodInputStream;
    private InputStream errorInputStream;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {
        virusInputStream = setupInputStream(ATTACHMENT_TEST_FILE_PATH + File.separator + VIRUS_FILE_NAME);
        goodInputStream = setupInputStream(ATTACHMENT_TEST_FILE_PATH + File.separator + GOOD_FILE_NAME);
        errorInputStream = setupInputStream(ATTACHMENT_TEST_FILE_PATH + File.separator + ERROR_FILE_NAME);
        noteVirus = setupMockNote(String.valueOf(new Guid()));
        
        attachmentService = new CuAttachmentServiceImpl();
        attachmentService.setKualiConfigurationService(buildMockConfigurationService());
        attachmentService.setAttachmentDao(buildMockAttachmentDao());
        attachmentService.setNoteService(buildMockNoteService());
        attachmentService.setAntiVirusService(new DummyAntiVirusServiceImpl(ScanResult.Status.PASSED.toString()));

        attachment = new Attachment();
        attachment.setObjectId(String.valueOf(new Guid()));
        attachment.setAttachmentIdentifier(attachment.getObjectId());
        Note noteGood = setupMockNote(String.valueOf(new Guid()));
        attachment.setNote(noteGood);
        createAttachmentFile(noteGood, GOOD_FILE_NAME);

        
        createAttachmentFile(noteVirus, VIRUS_FILE_NAME);
    }
    
    @After
    public void tearDown() throws IOException {
        IOUtils.closeQuietly(virusInputStream);
        IOUtils.closeQuietly(goodInputStream);
        IOUtils.closeQuietly(errorInputStream);
        FileUtils.forceDelete(new File(TEST_PATH).getAbsoluteFile());
        virusInputStream = null;
        goodInputStream = null;
        errorInputStream = null;
        noteVirus = null;
        attachmentService = null;
    }
    
    private NoteService buildMockNoteService() {
        NoteService mockNoteService = Mockito.mock(NoteService.class);
        Mockito.when(mockNoteService.getNoteByNoteId(Mockito.any())).thenReturn(noteVirus);
        return mockNoteService;
    }
    
    private Note setupMockNote(String remoteObjectIdentifier) {
        Note mockNote = Mockito.mock(Note.class);
        Mockito.when(mockNote.getRemoteObjectIdentifier()).thenReturn(remoteObjectIdentifier);
        return mockNote;
    }
    
    private CuAttachmentDaoOjb buildMockAttachmentDao() {
        CuAttachmentDaoOjb mockDao = Mockito.mock(CuAttachmentDaoOjb.class);
        Mockito.when(mockDao.getAttachmentByAttachmentId(Mockito.any())).then(this::buildAttachment);
        return mockDao;
    }
    
    private Attachment buildAttachment(InvocationOnMock invocation) {
        String attachmentIdentifier = invocation.getArgument(0);
        Attachment attachment = new Attachment();
        attachment.setObjectId(attachmentIdentifier);
        attachment.setAttachmentIdentifier(attachmentIdentifier);
        Note note = setupMockNote(attachmentIdentifier);
        attachment.setNote(note);
        return attachment;
    }
    
    private ConfigurationService buildMockConfigurationService() {
        ConfigurationService mockConfigurationService = Mockito.mock(ConfigurationService.class);
        Mockito.when(mockConfigurationService.getPropertyValueAsString(Mockito.any())).then(this::getConfigurationPropertyAsString);
        return mockConfigurationService;
    }
    
    private String getConfigurationPropertyAsString(InvocationOnMock invocation) {
        String inputString = invocation.getArgument(0);
        if (StringUtils.equals(inputString, KRADConstants.ATTACHMENTS_DIRECTORY_KEY)) {
            return TEST_ATTACHMENTS_PATH;
        }
        return null;
    }

    private void createAttachmentFile(Note note, String fileName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {
        String documentDirectory = buildDocumentDirectory(note);
        File attachmentFile = new File(documentDirectory + File.separator + attachment.getAttachmentIdentifier());
        File sourceFile = new File(ATTACHMENT_TEST_FILE_PATH + File.separator + fileName);
        FileUtils.copyFile(sourceFile, attachmentFile);
    }

    private String buildDocumentDirectory(Note note) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return buildDocumentDirectory(note.getRemoteObjectIdentifier());
    }

    private String buildDocumentDirectory(String objectId) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method getDocumentDirectoryMethod = AttachmentServiceImpl.class.getDeclaredMethod("getDocumentDirectory", String.class);
        getDocumentDirectoryMethod.setAccessible(true);
        return (String) getDocumentDirectoryMethod.invoke(attachmentService, objectId);
    }

    @Test
    public void retrieveAttachmentContents() throws Exception {
        validateRetrieveAttachmentContents(attachment, GOOD_FILE_CONTENTS);
    }

    @Test
    public void retrieveAttachmentContentsNoNote() throws Exception {
        attachment.setNote(setupMockNote(null));
        validateRetrieveAttachmentContents(attachment, VIRUS_FILE_CONTENTS);
    }

    @Test
    public void retrieveAttachmentContentsNoNoteRemoteObjectId() throws Exception {
        attachment.setNote(null);
        validateRetrieveAttachmentContents(attachment, VIRUS_FILE_CONTENTS);
    }

    private void validateRetrieveAttachmentContents(Attachment attachment, String expected) throws IOException {
        try (
            InputStream inputStream = attachmentService.retrieveAttachmentContents(attachment);
        ) {
            String fileContents = IOUtils.toString(inputStream, "UTF-8");
            Assert.assertEquals(expected, fileContents);
        }
    }

    @Test
    public void createAttachment() throws Exception {
        PersistableBusinessObject pbo = setupPersistableBusinessObject();

        Attachment createdAttachment = attachmentService.createAttachment(pbo, GOOD_FILE_NAME, "txt", 10, goodInputStream, "txt");
        String filePath = buildDocumentDirectory(pbo.getObjectId()) + File.separator + createdAttachment.getAttachmentIdentifier();
        String fileContents;
        try (
            InputStream fileStream = new FileInputStream(filePath);
            InputStream createdInputStream = new BufferedInputStream(fileStream);
        ) {
            fileContents = IOUtils.toString(createdInputStream, "UTF-8");
        }

        Assert.assertEquals(GOOD_FILE_CONTENTS, fileContents);
        Assert.assertEquals(GOOD_FILE_NAME, createdAttachment.getAttachmentFileName());
        Assert.assertEquals(10L, createdAttachment.getAttachmentFileSize().longValue());
        Assert.assertEquals("txt", createdAttachment.getAttachmentMimeTypeCode());
        Assert.assertEquals("txt", createdAttachment.getAttachmentTypeCode());
    }
    
    @Test
    public void createAttachmentWithOutVirus() throws Exception {
        PersistableBusinessObject pbo = setupPersistableBusinessObject();
        attachmentService.createAttachment(pbo, GOOD_FILE_NAME, "txt", 50, goodInputStream, "txt");
    }

    @Test
    public void createAttachmentWithVirus() throws Exception {
        attachmentService.setAntiVirusService(new DummyAntiVirusServiceImpl(ScanResult.Status.FAILED.toString()));
        
        PersistableBusinessObject pbo = setupPersistableBusinessObject();
        setupExpectedException("file contents failed virus scan");
        attachmentService.createAttachment(pbo, VIRUS_FILE_NAME, "txt", 50, virusInputStream, "txt");
    }
    
    @Test
    public void createAttachmentWithError() throws Exception {
        attachmentService.setAntiVirusService(new DummyAntiVirusServiceImpl(ScanResult.Status.ERROR.toString()));
        
        PersistableBusinessObject pbo = setupPersistableBusinessObject();
        setupExpectedException("file contents failed virus scan");
        attachmentService.createAttachment(pbo, ERROR_FILE_NAME, "txt", 50, errorInputStream, "txt");
    }

    @Test
    public void createAttachmentWithVirusInvalidDocument() throws Exception {
        setupExpectedException("invalid (null or uninitialized) document");
        attachmentService.createAttachment(null, GOOD_FILE_NAME, "txt", 50, goodInputStream, "txt");
    }

    @Test
    public void createAttachmentWithVirusInvalidFileName() throws Exception {
        PersistableBusinessObject pbo = setupPersistableBusinessObject();
        setupExpectedException("invalid (blank) fileName");
        attachmentService.createAttachment(pbo, "", "txt", 50, goodInputStream, "txt");
    }

    @Test
    public void createAttachmentWithVirusInvalidMimeType() throws Exception {
        PersistableBusinessObject pbo = setupPersistableBusinessObject();
        setupExpectedException("invalid (blank) mimeType");
        attachmentService.createAttachment(pbo, GOOD_FILE_NAME, "", 50, goodInputStream, "txt");
    }

    @Test
    public void createAttachmentWithVirusInvalidFileSize() throws Exception {
        PersistableBusinessObject pbo = setupPersistableBusinessObject();
        setupExpectedException("invalid (non-positive) fileSize");
        attachmentService.createAttachment(pbo, GOOD_FILE_NAME, "txt", 0, goodInputStream, "txt");
    }

    @Test
    public void createAttachmentWithVirusInvalidInputStream() throws Exception {
        PersistableBusinessObject pbo = setupPersistableBusinessObject();
        setupExpectedException("invalid (null) inputStream");
        attachmentService.createAttachment(pbo, GOOD_FILE_NAME, "txt", 50, null, "txt");
    }

    private PersistableBusinessObject setupPersistableBusinessObject() {
        PersistableBusinessObject pbo = new PhoneType();
        pbo.setObjectId(String.valueOf(new Guid()));
        attachment.setNote(setupMockNote(pbo.getObjectId()));
        return pbo;
    }

    private InputStream setupInputStream(String pathname) throws FileNotFoundException {
        File sourceFile = new File(pathname);
        return new FileInputStream(sourceFile);
    }

    private void setupExpectedException(String exceptionMessage) {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(exceptionMessage);
    }

}
