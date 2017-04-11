package edu.cornell.kfs.krad.service.impl;

import edu.cornell.cynergy.antivirus.service.DummyAntiVirusServiceImpl;
import edu.cornell.cynergy.antivirus.service.DummyScanResult;
import edu.cornell.cynergy.antivirus.service.ScanResult;
import edu.cornell.kfs.krad.dao.impl.CuAttachmentDaoOjb;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.service.impl.AttachmentServiceImpl;
import org.kuali.kfs.krad.service.impl.NoteServiceImpl;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.util.Guid;
import org.kuali.kfs.vnd.businessobject.PhoneType;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class CuAttachmentServiceImplTest {

    private static final String ATTACHMENT_TEST_FILE_PATH = "src/test/resources/edu/cornell/kfs/krad/service/fixture";
    private static final String GOOD_FILE_CONTENTS = "This is a pretend good attachment file.";
    private static final String GOOD_FILE_NAME = "good.txt";
    private static final String TEST_PATH = "test";
    private static final String TEST_ATTACHMENTS_PATH = TEST_PATH + File.separator + "attachments";
    private static final String VIRUS_FILE_CONTENTS = "This is a pretend virus infected attachment file.";
    private static final String VIRUS_FILE_NAME = "virus.txt";

    private CuAttachmentServiceImpl attachmentService;
    private Attachment attachment;
    private Note noteVirus;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        attachmentService = new CuAttachmentServiceImpl();
        attachmentService.setKualiConfigurationService(new MockConfigurationService());
        attachmentService.setAttachmentDao(new MockAttachmentDao());
        attachmentService.setNoteService(new MockNoteService());
        attachmentService.setAntiVirusService(new MockAntivirusService());

        attachment = new Attachment();
        attachment.setObjectId(String.valueOf(new Guid()));
        attachment.setAttachmentIdentifier(attachment.getObjectId());
        Note noteGood = setupMockNote();
        attachment.setNote(noteGood);
        createAttachmentFile(noteGood, GOOD_FILE_NAME);

        noteVirus = setupMockNote();
        createAttachmentFile(noteVirus, VIRUS_FILE_NAME);
    }

    private void createAttachmentFile(Note note, String fileName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {
        String documentDirectory = buildDocumentDirectory(note);
        File attachmentFile = new File(documentDirectory + File.separator + attachment.getAttachmentIdentifier());
        File sourceFile = new File(ATTACHMENT_TEST_FILE_PATH + File.separator + fileName);
        FileUtils.copyFile(sourceFile, attachmentFile);
    }

    private Note setupMockNote() {
        Note mockNote = EasyMock.createMock(Note.class);

        EasyMock.expect(mockNote.getRemoteObjectIdentifier()).andStubReturn(String.valueOf(new Guid()));
        EasyMock.replay(mockNote);

        return mockNote;
    }

    private String buildDocumentDirectory(Note note) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return buildDocumentDirectory(note.getRemoteObjectIdentifier());
    }

    private String buildDocumentDirectory(String objectId) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method getDocumentDirectoryMethod = AttachmentServiceImpl.class.getDeclaredMethod("getDocumentDirectory", String.class);
        getDocumentDirectoryMethod.setAccessible(true);
        return (String) getDocumentDirectoryMethod.invoke(attachmentService, objectId);
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.forceDelete(new File(TEST_PATH).getAbsoluteFile());
    }

    @Test
    public void retrieveAttachmentContents() throws Exception {
        validateRetrieveAttachmentContents(attachment, GOOD_FILE_CONTENTS);
    }

    @Test
    public void retrieveAttachmentContentsNoNote() throws Exception {
        attachment.setNote(setupMockNoteWithoutRemoteObjectId());
        validateRetrieveAttachmentContents(attachment, VIRUS_FILE_CONTENTS);
    }

    private Note setupMockNoteWithoutRemoteObjectId() {
        Note mockNote = EasyMock.createMock(Note.class);

        EasyMock.expect(mockNote.getRemoteObjectIdentifier()).andStubReturn(null);
        EasyMock.replay(mockNote);

        return mockNote;
    }

    @Test
    public void retrieveAttachmentContentsNoNoteRemoteObjectId() throws Exception {
        attachment.setNote(null);
        validateRetrieveAttachmentContents(attachment, VIRUS_FILE_CONTENTS);
    }

    private void validateRetrieveAttachmentContents(Attachment attachment, String expected) throws IOException {
        InputStream inputStream = attachmentService.retrieveAttachmentContents(attachment);
        String fileContents = IOUtils.toString(inputStream, "UTF-8");
        Assert.assertEquals(expected, fileContents);
    }

    @Test
    public void createAttachment() throws Exception {
        attachmentService.setAntiVirusService(new DummyAntiVirusServiceImpl());

        PersistableBusinessObject pbo = setupPersistableBusinessObject();
        InputStream inputStream = setupInputStream(ATTACHMENT_TEST_FILE_PATH + File.separator + GOOD_FILE_NAME);

        Attachment createdAttachment = attachmentService.createAttachment(pbo, GOOD_FILE_NAME, "txt", 10, inputStream, "txt");
        InputStream createdInputStream = new BufferedInputStream(new FileInputStream(buildDocumentDirectory(pbo.getObjectId()) + File.separator + createdAttachment.getAttachmentIdentifier()));
        String fileContents = IOUtils.toString(createdInputStream, "UTF-8");

        Assert.assertEquals(GOOD_FILE_CONTENTS, fileContents);
        Assert.assertEquals(GOOD_FILE_NAME, createdAttachment.getAttachmentFileName());
        Assert.assertEquals(10L, createdAttachment.getAttachmentFileSize().longValue());
        Assert.assertEquals("txt", createdAttachment.getAttachmentMimeTypeCode());
        Assert.assertEquals("txt", createdAttachment.getAttachmentTypeCode());
    }

    @Test
    public void createAttachmentWithVirus() throws Exception {
        PersistableBusinessObject pbo = setupPersistableBusinessObject();
        InputStream inputStream = setupInputStream(ATTACHMENT_TEST_FILE_PATH + File.separator + VIRUS_FILE_NAME);
        setupExpectedException("file contents failed virus scan");
        attachmentService.createAttachment(pbo, VIRUS_FILE_NAME, "txt", 50, inputStream, "txt");
    }

    @Test
    public void createAttachmentWithVirusInvalidDocument() throws Exception {
        InputStream inputStream = setupInputStream(ATTACHMENT_TEST_FILE_PATH + File.separator + GOOD_FILE_NAME);
        setupExpectedException("invalid (null or uninitialized) document");
        attachmentService.createAttachment(null, GOOD_FILE_NAME, "txt", 50, inputStream, "txt");
    }

    @Test
    public void createAttachmentWithVirusInvalidFileName() throws Exception {
        PersistableBusinessObject pbo = setupPersistableBusinessObject();
        InputStream inputStream = setupInputStream(ATTACHMENT_TEST_FILE_PATH + File.separator + GOOD_FILE_NAME);
        setupExpectedException("invalid (blank) fileName");
        attachmentService.createAttachment(pbo, "", "txt", 50, inputStream, "txt");
    }

    @Test
    public void createAttachmentWithVirusInvalidMimeType() throws Exception {
        PersistableBusinessObject pbo = setupPersistableBusinessObject();
        InputStream inputStream = setupInputStream(ATTACHMENT_TEST_FILE_PATH + File.separator + GOOD_FILE_NAME);
        setupExpectedException("invalid (blank) mimeType");
        attachmentService.createAttachment(pbo, GOOD_FILE_NAME, "", 50, inputStream, "txt");
    }

    @Test
    public void createAttachmentWithVirusInvalidFileSize() throws Exception {
        PersistableBusinessObject pbo = setupPersistableBusinessObject();
        InputStream inputStream = setupInputStream(ATTACHMENT_TEST_FILE_PATH + File.separator + GOOD_FILE_NAME);
        setupExpectedException("invalid (non-positive) fileSize");
        attachmentService.createAttachment(pbo, GOOD_FILE_NAME, "txt", 0, inputStream, "txt");
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

    private Note setupMockNote(String objectId) {
        Note mockNote = EasyMock.createMock(Note.class);

        EasyMock.expect(mockNote.getRemoteObjectIdentifier()).andStubReturn(objectId);
        EasyMock.replay(mockNote);

        return mockNote;
    }

    private InputStream setupInputStream(String pathname) throws FileNotFoundException {
        File sourceFile = new File(pathname);
        return new FileInputStream(sourceFile);
    }

    private void setupExpectedException(String exceptionMessage) {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(exceptionMessage);
    }

    private class MockConfigurationService implements ConfigurationService {

        @Override
        public String getPropertyValueAsString(String s) {
            if (StringUtils.equals(s, KRADConstants.ATTACHMENTS_DIRECTORY_KEY)) {
                return TEST_ATTACHMENTS_PATH;
            }

            return null;
        }

        @Override
        public boolean getPropertyValueAsBoolean(String s) {
            return false;
        }

        @Override
        public boolean getPropertyValueAsBoolean(String s, boolean b) {
            return false;
        }

        @Override
        public Map<String, String> getAllProperties() {
            return null;
        }

    }

    private class MockAttachmentDao extends CuAttachmentDaoOjb {

        @Override
        public Attachment getAttachmentByAttachmentId(String attachmentIdentifier) {
            Attachment attachment = new Attachment();
            attachment.setObjectId(attachmentIdentifier);
            attachment.setAttachmentIdentifier(attachmentIdentifier);
            Note note = setupMockNote(attachmentIdentifier);
            attachment.setNote(note);

            return attachment;
        }

    }

    private class MockNoteService extends NoteServiceImpl {

        @Override
        public Note getNoteByNoteId(Long aLong) {
            return noteVirus;
        }

   }

   private class MockAntivirusService extends DummyAntiVirusServiceImpl {

       @Override
       public ScanResult scan(InputStream inputStream) {

           ScanResult mockScanResult = EasyMock.createMock(DummyScanResult.class);

           EasyMock.expect(mockScanResult.getResult()).andStubReturn(ScanResult.Status.FAILED.toString());
           EasyMock.expect(mockScanResult.getStatus()).andStubReturn(ScanResult.Status.FAILED);
           EasyMock.replay(mockScanResult);

           return mockScanResult;
       }
   }

}