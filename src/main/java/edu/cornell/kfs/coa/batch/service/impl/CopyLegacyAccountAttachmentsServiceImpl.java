package edu.cornell.kfs.coa.batch.service.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
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
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.NoteType;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.coa.CuCOAKeyConstants;
import edu.cornell.kfs.coa.batch.CopyLegacyAccountAttachmentsStep;
import edu.cornell.kfs.coa.batch.CuCoaBatchConstants;
import edu.cornell.kfs.coa.batch.CuCoaBatchParameterConstants;
import edu.cornell.kfs.coa.batch.businessobject.LegacyAccountAttachment;
import edu.cornell.kfs.coa.batch.dataaccess.CopyLegacyAccountAttachmentsDao;
import edu.cornell.kfs.coa.batch.service.CopyLegacyAccountAttachmentsService;
import edu.cornell.kfs.fp.batch.service.AccountingXmlDocumentDownloadAttachmentService;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentBackupLink;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;

public class CopyLegacyAccountAttachmentsServiceImpl implements CopyLegacyAccountAttachmentsService {

    private static final Logger LOG = LogManager.getLogger();

    private static final Pattern FILE_PATH_PATTERN = Pattern.compile("^(/\\w+)+$");

    private CopyLegacyAccountAttachmentsDao copyLegacyAccountAttachmentsDao;
    private AccountingXmlDocumentDownloadAttachmentService accountingXmlDocumentDownloadAttachmentService;
    private WebServiceCredentialService webServiceCredentialService;
    private AccountService accountService;
    private PersonService personService;
    private NoteService noteService;
    private DataDictionaryService dataDictionaryService;
    private ParameterService parameterService;
    private ConfigurationService configurationService;

    @Transactional
    @Override
    public boolean copyLegacyAccountAttachmentsToKfs() {
        final int batchSize = getParameterValueAsInteger(CuCoaBatchParameterConstants.ATTACHMENT_BATCH_SIZE);
        final int maxRetries = getParameterValueAsInteger(CuCoaBatchParameterConstants.MAX_DOWNLOAD_RETRIES);
        final int maxFailuresAllowed = getParameterValueAsInteger(
                CuCoaBatchParameterConstants.MAX_ATTACHMENT_FAILURES_PER_RUN);
        final List<LegacyAccountAttachment> attachmentsToCopy = copyLegacyAccountAttachmentsDao
                .getLegacyAccountAttachmentsToCopy(batchSize, maxRetries);
        if (attachmentsToCopy.size() == 0) {
            LOG.info("copyLegacyAccountAttachmentsToKfs, There are no legacy attachments available for copying");
            return true;
        }

        LOG.info("copyLegacyAccountAttachmentsToKfs, Copying {} legacy account attachments into KFS",
                attachmentsToCopy.size());
        final CopyLegacyAccountAttachmentsService proxiedService = SpringContext.getBean(
                CopyLegacyAccountAttachmentsService.class);
        int numCopiedAttachments = 0;
        int numCopyFailures = 0;
        
        for (final LegacyAccountAttachment attachmentToCopy : attachmentsToCopy) {
            if (proxiedService.copyLegacyAccountAttachmentToKfs(attachmentToCopy)) {
                numCopiedAttachments++;
            } else {
                numCopyFailures++;
                if (numCopyFailures > maxFailuresAllowed) {
                    LOG.error("copyLegacyAccountAttachmentsToKfs, More than {} attachments failed to copy; aborting",
                            maxFailuresAllowed);
                    return false;
                }
            }
        }

        LOG.info("====================");
        LOG.info("copyLegacyAccountAttachmentsToKfs, Successfully copied {} legacy account attachments into KFS",
                numCopiedAttachments);
        LOG.info("copyLegacyAccountAttachmentsToKfs, {} legacy account attachments failed copying into KFS",
                numCopyFailures);
        LOG.info("====================");

        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public boolean copyLegacyAccountAttachmentToKfs(final LegacyAccountAttachment legacyAccountAttachment) {
        try {
            LOG.debug("copyLegacyAccountAttachmentToKfs, Copying attachment: {}", legacyAccountAttachment);
            final Account account = getKfsAccountForAttachment(legacyAccountAttachment);
            final Attachment attachment = downloadAttachment(legacyAccountAttachment, account);
            saveAttachmentToKfs(legacyAccountAttachment, attachment, account);
            LOG.info("copyLegacyAccountAttachmentToKfs, Copied attachment with ID '{}' targeting account '{}-{}'",
                    legacyAccountAttachment.getId(), legacyAccountAttachment.getKfsChartCode(),
                    legacyAccountAttachment.getKfsAccountNumber());
            copyLegacyAccountAttachmentsDao.markLegacyAccountAttachmentAsCopied(legacyAccountAttachment);
            return true;
        } catch (final Exception e) {
            LOG.error("copyLegacyAccountAttachmentToKfs, Failed to copy attachment: {}", legacyAccountAttachment, e);
            copyLegacyAccountAttachmentsDao.recordCopyingErrorForLegacyAccountAttachment(
                    legacyAccountAttachment, e.getMessage());
            return false;
        }
    }

    private Account getKfsAccountForAttachment(final LegacyAccountAttachment legacyAccountAttachment) {
        final Account account = accountService.getByPrimaryId(legacyAccountAttachment.getKfsChartCode(),
                legacyAccountAttachment.getKfsAccountNumber());
        if (ObjectUtils.isNull(account)) {
            throw new RuntimeException("Account '" + legacyAccountAttachment.getKfsChartCode()
                    + "-" + legacyAccountAttachment.getKfsAccountNumber() + "' does not exist");
        }
        return account;
    }

    private Attachment downloadAttachment(final LegacyAccountAttachment legacyAccountAttachment,
            final Account account) throws URISyntaxException {
        final AccountingXmlDocumentBackupLink backupLink = new AccountingXmlDocumentBackupLink();
        backupLink.setCredentialGroupCode(CuCoaBatchConstants.DFA_ATTACHMENTS_GROUP_CODE);
        backupLink.setFileName(legacyAccountAttachment.getFileName());
        backupLink.setDescription(legacyAccountAttachment.getFileDescription());
        backupLink.setLinkUrl(buildAttachmentDownloadUrl(legacyAccountAttachment));
        return accountingXmlDocumentDownloadAttachmentService.createAttachmentFromBackupLink(account, backupLink);
    }

    private void saveAttachmentToKfs(final LegacyAccountAttachment legacyAccountAttachment,
            final Attachment attachment, final Account account) throws IOException {
        final Person systemUser = personService.getPersonByPrincipalName(KFSConstants.SYSTEM_USER);
        final String noteText = createNoteText(legacyAccountAttachment);

        final Note note = new Note();
        note.setAuthorUniversalIdentifier(systemUser.getPrincipalId());
        note.setNoteTypeCode(NoteType.BUSINESS_OBJECT.getCode());
        note.setRemoteObjectIdentifier(account.getObjectId());
        note.setNoteText(noteText);
        note.setNotePostedTimestampToCurrent();
        note.setAttachment(attachment);
        noteService.save(note);
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

    private String buildAttachmentDownloadUrl(final LegacyAccountAttachment legacyAccountAttachment)
            throws URISyntaxException {
        final String baseUrl = getBaseAttachmentDownloadUrl();
        final String filePath = legacyAccountAttachment.getFilePath();
        if (StringUtils.isBlank(baseUrl)) {
            throw new IllegalStateException("Base DFA attachment URL endpoint cannot be blank");
        } else if (!FILE_PATH_PATTERN.matcher(filePath).matches()) {
            throw new IllegalArgumentException("Account attachment has invalid or malformed file path: " + filePath);
        }
        return new URIBuilder(baseUrl)
                .appendPath(legacyAccountAttachment.getFilePath())
                .build()
                .toString();
    }

    private String getBaseAttachmentDownloadUrl() {
        return webServiceCredentialService.getWebServiceCredentialValue(
                CuCoaBatchConstants.DFA_ATTACHMENTS_GROUP_CODE, CuCoaBatchConstants.DFA_ATTACHMENTS_URL_KEY);
    }

    public void setCopyLegacyAccountAttachmentsDao(
            final CopyLegacyAccountAttachmentsDao copyLegacyAccountAttachmentsDao) {
        this.copyLegacyAccountAttachmentsDao = copyLegacyAccountAttachmentsDao;
    }

    public void setAccountingXmlDocumentDownloadAttachmentService(
            final AccountingXmlDocumentDownloadAttachmentService accountingXmlDocumentDownloadAttachmentService) {
        this.accountingXmlDocumentDownloadAttachmentService = accountingXmlDocumentDownloadAttachmentService;
    }

    public void setWebServiceCredentialService(final WebServiceCredentialService webServiceCredentialService) {
        this.webServiceCredentialService = webServiceCredentialService;
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
