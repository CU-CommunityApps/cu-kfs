package edu.cornell.kfs.fp.service.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AutoPopulatingList;

import edu.cornell.kfs.fp.batch.RecurringDisbursementVoucherDocumentRoutingReportItem;
import edu.cornell.kfs.fp.dataaccess.RecurringDisbursementVoucherSearchDao;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.fp.service.RecurringDisbursementVoucherDocumentRoutingService;

public class RecurringDisbursementVoucherDocumentRoutingServiceImpl
        implements RecurringDisbursementVoucherDocumentRoutingService {

    private static final Logger LOG = LogManager.getLogger();

    public static final String UNKNOWN_RCDV_ID = "Unknown";
    public static final String NONEXISTING_DV_MESSAGE = "Disbursement Voucher does not exist or has not been spawned";
    public static final String WRONG_INITIATOR_MESSAGE =
            "Disbursement Voucher did not have the KFS System User as the initiator";
    public static final String BLANKET_APPROVE_NOTE =
            "Batch job Submit and Blanket Approve performed for DV spawned by Recurring DV.";
    public static final String BLANKET_APPROVE_ANNOTATION = "Auto blanket approve from Batch Job";

    private RecurringDisbursementVoucherSearchDao recurringDisbursementVoucherSearchDao;
    private DocumentService documentService;
    private PersonService personService;
    private ConfigurationService configurationService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public RecurringDisbursementVoucherDocumentRoutingReportItem autoApproveSpawnedDisbursementVoucher(
            String spawnedDvDocumentNumber) {
        try {
            UserSession userSession = createUserSessionForSystemUser();
            return GlobalVariables.doInNewGlobalVariables(userSession,
                    () -> autoApproveSpawnedDisbursementVoucherInternal(spawnedDvDocumentNumber));
        } catch (Exception e) {
            LOG.error("autoApproveSpawnedDisbursementVoucher, Unexpected error occurred while auto-approving DV {}",
                    spawnedDvDocumentNumber, e);
            return new RecurringDisbursementVoucherDocumentRoutingReportItem(
                    UNKNOWN_RCDV_ID, spawnedDvDocumentNumber, e.getMessage());
        }
    }

    protected UserSession createUserSessionForSystemUser() {
        return new UserSession(KFSConstants.SYSTEM_USER);
    }

    private RecurringDisbursementVoucherDocumentRoutingReportItem autoApproveSpawnedDisbursementVoucherInternal(
            String spawnedDvDocumentNumber) {
        String recurringDvDocumentNumber = recurringDisbursementVoucherSearchDao.findRecurringDvIdForSpawnedDv(
                spawnedDvDocumentNumber);
        recurringDvDocumentNumber = StringUtils.defaultIfBlank(recurringDvDocumentNumber, UNKNOWN_RCDV_ID);

        try {
            CuDisbursementVoucherDocument dvDocument = (CuDisbursementVoucherDocument) documentService
                    .getByDocumentHeaderId(spawnedDvDocumentNumber);
            if (ObjectUtils.isNull(dvDocument)) {
                return new RecurringDisbursementVoucherDocumentRoutingReportItem(
                        recurringDvDocumentNumber, spawnedDvDocumentNumber, NONEXISTING_DV_MESSAGE);
            } else if (!documentWasInitiatedBySystemUser(dvDocument)) {
                return new RecurringDisbursementVoucherDocumentRoutingReportItem(
                        recurringDvDocumentNumber, spawnedDvDocumentNumber, WRONG_INITIATOR_MESSAGE);
            }
            addAutoApprovalNoteToDv(dvDocument);
            documentService.blanketApproveDocument(dvDocument, BLANKET_APPROVE_ANNOTATION, null);
        } catch (ValidationException e) {
            LOG.error("autoApproveSpawnedDisbursementVoucherInternal, Encountered validation errors while "
                    + "auto-approving DV {}", spawnedDvDocumentNumber, e);
            return buildReportItemForBusinessRuleValidationFailures(
                    recurringDvDocumentNumber, spawnedDvDocumentNumber);
        }
        return new RecurringDisbursementVoucherDocumentRoutingReportItem(
                recurringDvDocumentNumber, spawnedDvDocumentNumber);
    }

    private boolean documentWasInitiatedBySystemUser(CuDisbursementVoucherDocument document) {
        String initiatorId = document.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId();
        Person systemUser = personService.getPersonByPrincipalName(KFSConstants.SYSTEM_USER);
        if (ObjectUtils.isNull(systemUser)) {
            throw new IllegalStateException("Could not find KFS System User");
        }
        return StringUtils.equals(initiatorId, systemUser.getPrincipalId());
    }

    private void addAutoApprovalNoteToDv(CuDisbursementVoucherDocument dvDocument) {
        Note note = buildNoteBase();
        note.setNoteText(BLANKET_APPROVE_NOTE);
        dvDocument.addNote(note);
        documentService.saveDocument(dvDocument);
    }

    private Note buildNoteBase() {
        Note noteBase = new Note();
        noteBase.setAuthorUniversalIdentifier(GlobalVariables.getUserSession().getPerson().getPrincipalId());
        setNotePostedTimestampToCurrent(noteBase);
        return noteBase;
    }

    protected void setNotePostedTimestampToCurrent(Note note) {
        note.setNotePostedTimestampToCurrent();
    }

    private RecurringDisbursementVoucherDocumentRoutingReportItem buildReportItemForBusinessRuleValidationFailures(
            String recurringDvDocumentNumber, String spawnedDvDocumentNumber) {
        RecurringDisbursementVoucherDocumentRoutingReportItem reportItem =
                new RecurringDisbursementVoucherDocumentRoutingReportItem(
                        recurringDvDocumentNumber, spawnedDvDocumentNumber);
        Map<String, AutoPopulatingList<ErrorMessage>> errorMessages = GlobalVariables
                .getMessageMap().getErrorMessages();
        List<String> errorMessageStrings = errorMessages.values().stream()
                .flatMap(List::stream)
                .map(this::buildValidationErrorMessageForSingleError)
                .collect(Collectors.toUnmodifiableList());
        reportItem.addAllErrors(errorMessageStrings);
        return reportItem;
    }

    private String buildValidationErrorMessageForSingleError(ErrorMessage errorMessage) {
        String errorMessageString = configurationService.getPropertyValueAsString(errorMessage.getErrorKey());
        if (StringUtils.isBlank(errorMessageString)) {
            throw new RuntimeException("Cannot find error message for key: " + errorMessage.getErrorKey());
        }
        Object[] messageParameters = errorMessage.getMessageParameters();
        if (ArrayUtils.isNotEmpty(messageParameters)) {
            return MessageFormat.format(errorMessageString, messageParameters);
        } else {
            return errorMessageString;
        }
    }

    public void setRecurringDisbursementVoucherSearchDao(
            RecurringDisbursementVoucherSearchDao recurringDisbursementVoucherSearchDao) {
        this.recurringDisbursementVoucherSearchDao = recurringDisbursementVoucherSearchDao;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
