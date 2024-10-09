package edu.cornell.kfs.coa.batch.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.service.AttachmentService;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.NoteType;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.coa.CuCOAKeyConstants;
import edu.cornell.kfs.coa.batch.CopyLegacyAccountAttachmentsStep;
import edu.cornell.kfs.coa.batch.CuCoaBatchParameterConstants;
import edu.cornell.kfs.coa.batch.businessobject.LegacyAccountAttachment;
import edu.cornell.kfs.coa.batch.dataaccess.CopyLegacyAccountAttachmentsDao;
import edu.cornell.kfs.coa.batch.service.CopyLegacyAccountAttachmentsService;
import edu.cornell.kfs.coa.batch.service.DownloadLegacyAccountAttachmentsService;
import edu.cornell.kfs.sys.util.CuAttachmentUtils;

public class CopyLegacyAccountAttachmentsServiceImpl implements CopyLegacyAccountAttachmentsService {

    private static final Logger LOG = LogManager.getLogger();

    private CopyLegacyAccountAttachmentsDao copyLegacyAccountAttachmentsDao;
    private DownloadLegacyAccountAttachmentsService downloadLegacyAccountAttachmentsService;
    private AccountService accountService;
    private PersonService personService;
    private NoteService noteService;
    private AttachmentService attachmentService;
    private DataDictionaryService dataDictionaryService;
    private ParameterService parameterService;
    private ConfigurationService configurationService;

    @Transactional
    @Override
    public boolean copyLegacyAccountAttachmentsToKfs() {
        final int batchSize = getParameterValueAsInteger(CuCoaBatchParameterConstants.ATTACHMENT_BATCH_SIZE);
        final int maxRetries = getParameterValueAsInteger(CuCoaBatchParameterConstants.MAX_DOWNLOAD_RETRIES);
        final List<LegacyAccountAttachment> attachmentsToCopy = copyLegacyAccountAttachmentsDao
                .getLegacyAccountAttachmentsToCopy(batchSize, maxRetries);
        if (attachmentsToCopy.size() == 0) {
            LOG.info("copyLegacyAccountAttachmentsToKfs, There are no legacy attachments available for copying");
            return true;
        }

        LOG.info("copyLegacyAccountAttachmentsToKfs, Copying {} legacy account attachments into KFS",
                attachmentsToCopy.size());
        final List<LegacyAccountAttachment> copiedAttachments = new ArrayList<>(attachmentsToCopy.size());
        final List<LegacyAccountAttachment> copyFailures = new ArrayList<>();
        
        for (final LegacyAccountAttachment attachmentToCopy : attachmentsToCopy) {
            if (copyLegacyAccountAttachmentToKfs(attachmentToCopy)) {
                copiedAttachments.add(attachmentToCopy);
            } else {
                copyFailures.add(attachmentToCopy);
            }
        }

        updateLegacyAccountAttachmentEntries(copiedAttachments, copyFailures);

        LOG.info("====================");
        LOG.info("copyLegacyAccountAttachmentsToKfs, Successfully copied {} legacy account attachments into KFS",
                copiedAttachments.size());
        LOG.info("copyLegacyAccountAttachmentsToKfs, {} legacy account attachments failed copying into KFS",
                copyFailures.size());
        LOG.info("====================");

        return copiedAttachments.size() > 0;
    }

    private boolean copyLegacyAccountAttachmentToKfs(final LegacyAccountAttachment legacyAccountAttachment) {
        try {
            downloadLegacyAccountAttachmentsService.downloadAndProcessLegacyAccountAttachment(
                    legacyAccountAttachment, this::saveAttachmentToKfs);
            LOG.info("copyLegacyAccountAttachmentToKfs, Copied attachment '{}' targeting account '{}-{}'",
                    legacyAccountAttachment.getId(), legacyAccountAttachment.getKfsChartCode(),
                    legacyAccountAttachment.getKfsAccountNumber());
            return true;
        } catch (final Exception e) {
            LOG.error("copyLegacyAccountAttachmentToKfs, Failed to copy attachment '{}' targeting account '{}-{}'",
                    legacyAccountAttachment.getId(), legacyAccountAttachment.getKfsChartCode(),
                    legacyAccountAttachment.getKfsAccountNumber(), e);
            return false;
        }
    }

    private void saveAttachmentToKfs(final LegacyAccountAttachment legacyAccountAttachment,
            final DataBuffer fileContents) throws IOException {
        final Account account = accountService.getByPrimaryId(legacyAccountAttachment.getKfsChartCode(),
                legacyAccountAttachment.getKfsAccountNumber());
        if (ObjectUtils.isNull(account)) {
            throw new RuntimeException("Account '" + legacyAccountAttachment.getKfsChartCode()
                    + "-" + legacyAccountAttachment.getKfsAccountNumber() + "' does not exist");
        }
        final Person systemUser = personService.getPersonByPrincipalName(KFSConstants.SYSTEM_USER);
        final String noteText = createNoteText(legacyAccountAttachment);
        final Attachment attachment = createKfsAttachment(legacyAccountAttachment, fileContents, account);

        final Note note = new Note();
        note.setAuthorUniversalIdentifier(systemUser.getPrincipalId());
        note.setNoteTypeCode(NoteType.BUSINESS_OBJECT.getCode());
        note.setRemoteObjectIdentifier(account.getObjectId());
        note.setNoteText(noteText);
        note.setNotePostedTimestampToCurrent();
        note.setAttachment(attachment);
        noteService.save(note);
    }

    private Attachment createKfsAttachment(final LegacyAccountAttachment legacyAccountAttachment,
            final DataBuffer fileContents, final Account account) throws IOException {
        final String fileName = legacyAccountAttachment.getFileName();
        final String mimeType = CuAttachmentUtils.findMimeType(fileName);
        final int fileSize = fileContents.readableByteCount();
        try (final InputStream fileStream = fileContents.asInputStream()) {
            return attachmentService.createAttachment(account, fileName, mimeType, fileSize, fileStream, null);
        }
    }

    private String createNoteText(final LegacyAccountAttachment legacyAccountAttachment) {
        final int noteTextMaxLength = dataDictionaryService.getAttributeMaxLength(Note.class,
                KRADConstants.NOTE_TEXT_PROPERTY_NAME);
        final String noteTemplate = configurationService.getPropertyValueAsString(
                CuCOAKeyConstants.LEGACY_ACCOUNT_ATTACHMENT_NOTE_TEXT_TEMPLATE);
        final String noteText = String.format(Locale.US, noteTemplate, legacyAccountAttachment.getLegacyAccountCode(),
                legacyAccountAttachment.getAddedBy(), legacyAccountAttachment.getFileDescription());
        return StringUtils.left(noteText, noteTextMaxLength);
    }

    private int getParameterValueAsInteger(final String parameterName) {
        final String parameterValue = parameterService.getParameterValueAsString(
                CopyLegacyAccountAttachmentsStep.class, parameterName);
        return Integer.parseInt(parameterValue);
    }

    private void updateLegacyAccountAttachmentEntries(final List<LegacyAccountAttachment> copiedAttachments,
            final List<LegacyAccountAttachment> copyFailures) {
        if (!copiedAttachments.isEmpty()) {
            copyLegacyAccountAttachmentsDao.markLegacyAccountAttachmentsAsCopied(copiedAttachments);
        }
        if (!copyFailures.isEmpty()) {
            copyLegacyAccountAttachmentsDao.incrementRetryCountsOnLegacyAccountAttachments(copyFailures);
        }
    }

    public void setCopyLegacyAccountAttachmentsDao(
            final CopyLegacyAccountAttachmentsDao copyLegacyAccountAttachmentsDao) {
        this.copyLegacyAccountAttachmentsDao = copyLegacyAccountAttachmentsDao;
    }

    public void setDownloadLegacyAccountAttachmentsService(
            final DownloadLegacyAccountAttachmentsService downloadLegacyAccountAttachmentsService) {
        this.downloadLegacyAccountAttachmentsService = downloadLegacyAccountAttachmentsService;
    }

    public void setAccountService(final AccountService accountService) {
        this.accountService = accountService;
    }

    public void setPersonService(final PersonService personService) {
        this.personService = personService;
    }

    public void setNoteService(final NoteService noteService) {
        this.noteService = noteService;
    }

    public void setAttachmentService(final AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    public void setDataDictionaryService(final DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public void setParameterService(final ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setConfigurationService(final ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
