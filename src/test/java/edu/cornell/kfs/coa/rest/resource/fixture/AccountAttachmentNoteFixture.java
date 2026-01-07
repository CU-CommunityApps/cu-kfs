package edu.cornell.kfs.coa.rest.resource.fixture;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.lang3.Validate;
import org.kuali.kfs.core.api.util.ClasspathOrFileResourceLoader;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import edu.cornell.kfs.coa.CuCoaTestConstants;

@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface AccountAttachmentNoteFixture {

    long noteId();
    String noteText();
    boolean hasAttachment() default true;
    String attachmentId() default KFSConstants.EMPTY_STRING;
    String mimeType() default KFSConstants.EMPTY_STRING;
    String fileName() default KFSConstants.EMPTY_STRING;

    public static final class Utils {
        public static Note toNote(final AccountAttachmentNoteFixture fixture) {
            final Note note = new Note();
            note.setNoteIdentifier(fixture.noteId());
            note.setNoteText(fixture.noteText());
            if (fixture.hasAttachment()) {
                final Attachment attachment = toAttachment(fixture);
                note.setAttachment(attachment);
            }
            return note;
        }

        public static Attachment toAttachment(final AccountAttachmentNoteFixture fixture) {
            Validate.isTrue(fixture.hasAttachment(), "fixture does not have an attachment configured");
            Validate.notBlank(fixture.attachmentId(), "attachmentId cannot be blank");
            Validate.notBlank(fixture.mimeType(), "mimeType cannot be blank");
            Validate.notBlank(fixture.fileName(), "fileName cannot be blank");
            final Attachment attachment = new Attachment();
            attachment.setNoteIdentifier(fixture.noteId());
            attachment.setAttachmentIdentifier(fixture.attachmentId());
            attachment.setAttachmentMimeTypeCode(fixture.mimeType());
            attachment.setAttachmentFileName(fixture.fileName());
            attachment.setAttachmentFileSize(getAttachmentFileSize(fixture));
            return null;
        }

        private static long getAttachmentFileSize(final AccountAttachmentNoteFixture fixture) {
            try {
                final String fullFilePath = CuCoaTestConstants.ACCOUNT_ATTACHMENT_TEST_BASE_PATH
                        + fixture.fileName();
                final ResourceLoader resourceLoader = new ClasspathOrFileResourceLoader();
                final Resource resource = resourceLoader.getResource(fullFilePath);
                Validate.validState(resource.exists(), "File does not exist: " + fullFilePath);
                return resource.contentLength();
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

}
