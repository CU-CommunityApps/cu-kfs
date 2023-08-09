package edu.cornell.kfs.pdp.service.impl;

import java.text.MessageFormat;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kew.api.document.DocumentStatus;
import org.kuali.kfs.kew.api.document.DocumentStatusCategory;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.rules.PromptBeforeValidationBase;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.document.AccountsPayableDocumentBase;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentNoteText;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSConstants.PaymentSourceConstants;
import org.kuali.kfs.sys.KFSKeyConstants;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.businessobject.RecurringDisbursementVoucherDetail;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherConstants;
import edu.cornell.kfs.fp.document.RecurringDisbursementVoucherDocument;
import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.pdp.CUPdpKeyConstants;
import edu.cornell.kfs.pdp.service.CuCheckStubService;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CuCheckStubServiceImpl implements CuCheckStubService {

    private int checkStubMaxLengthForIso20022;
    private ConfigurationService configurationService;
    private NoteService noteService;
    private PersonService personService;

    @Override
    public boolean doesCheckStubNeedTruncatingForIso20022(Document document) {
        String paymentMethodCode;
        if (ObjectUtils.isNull(document)) {
            throw new IllegalArgumentException("document cannot be null");
        } else if (document instanceof DisbursementVoucherDocument) {
            paymentMethodCode = ((DisbursementVoucherDocument) document).getPaymentMethodCode();
            if (document instanceof RecurringDisbursementVoucherDocument
                    && doAnyRecurringCheckStubsNeedTruncatingForIso20022(
                            (RecurringDisbursementVoucherDocument) document)) {
                return true;
            }
        } else if (document instanceof PaymentRequestDocument) {
            paymentMethodCode = ((PaymentRequestDocument) document).getPaymentMethodCode();
        } else if (document instanceof VendorCreditMemoDocument) {
            paymentMethodCode = ((VendorCreditMemoDocument) document).getPaymentMethodCode();
        } else {
            throw new IllegalArgumentException("document of type " + document.getClass().getSimpleName()
                    + " does not have support for both check stubs and payment methods");
        }
        return StringUtils.equalsIgnoreCase(paymentMethodCode, PaymentSourceConstants.PAYMENT_METHOD_CHECK)
                && StringUtils.length(getFullCheckStub(document)) > checkStubMaxLengthForIso20022;
    }

    private boolean doAnyRecurringCheckStubsNeedTruncatingForIso20022(RecurringDisbursementVoucherDocument document) {
        return CollectionUtils.isNotEmpty(document.getRecurringDisbursementVoucherDetails())
                && StringUtils.equalsIgnoreCase(document.getPaymentMethodCode(),
                        PaymentSourceConstants.PAYMENT_METHOD_CHECK)
                && doAnyRecurringCheckStubsExceedIso20022MaxLength(document);
    }

    private boolean doAnyRecurringCheckStubsExceedIso20022MaxLength(RecurringDisbursementVoucherDocument document) {
        return document.getRecurringDisbursementVoucherDetails().stream()
                .map(RecurringDisbursementVoucherDetail::getDvCheckStub)
                .anyMatch(checkStub -> StringUtils.length(checkStub) > checkStubMaxLengthForIso20022);
    }

    @Override
    public void addNoteToDocumentRegardingCheckStubIso20022MaxLength(Document document) {
        if (ObjectUtils.isNull(document)) {
            throw new IllegalArgumentException("document cannot be null");
        }
        
        String warningMessage = createWarningMessageForCheckStubIso20022MaxLength(document);
        Person systemUser = personService.getPersonByPrincipalName(KFSConstants.SYSTEM_USER);

        Note note = noteService.createNote(new Note(), document.getNoteTarget(), systemUser.getPrincipalId());
        note.setNoteText(warningMessage);
        note.setNotePostedTimestampToCurrent();

        note = noteService.save(note);
        document.addNote(note);
    }

    @Override
    public void addIso20022CheckStubLengthWarningToDocumentIfNecessary(Document document) {
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        DocumentStatus documentStatus = workflowDocument.getStatus();
        if (documentStatus.getCategory() == DocumentStatusCategory.PENDING
                && doesCheckStubNeedTruncatingForIso20022(document)) {
            String warningMessage = createWarningMessageForCheckStubIso20022MaxLength(document);
            GlobalVariables.getMessageMap().putWarning(
                    KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_CUSTOM, warningMessage);
        }
    }

    @Override
    public String createWarningMessageForCheckStubIso20022MaxLength(Document document) {
        String fieldLabel = getFieldLabelForCheckStubLengthMessage(document);
        String baseMessage = configurationService.getPropertyValueAsString(
                CUPdpKeyConstants.WARNING_CHECK_STUB_ISO_20022_LENGTH);
        return MessageFormat.format(baseMessage, fieldLabel, checkStubMaxLengthForIso20022);
    }

    private String getFieldLabelForCheckStubLengthMessage(Document document) {
        if (ObjectUtils.isNull(document)) {
            throw new IllegalArgumentException("document cannot be null");
        } else if (document instanceof DisbursementVoucherDocument) {
            return CuFPConstants.DV_CHECK_STUB_FIELD_LABEL;
        } else if (document instanceof AccountsPayableDocumentBase) {
            return CUPurapConstants.AP_CHECK_STUB_FIELD_LABEL;
        } else {
            throw new IllegalArgumentException("document of type " + document.getClass().getSimpleName()
                    + " does not support check stubs");
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean performPreRulesValidationOfIso20022CheckStubLength(Document document,
            PromptBeforeValidationBase documentPreRules) {
        boolean result = true;
        if (doesCheckStubNeedTruncatingForIso20022(document)) {
            int iso20022MaxStubLength = getCheckStubMaxLengthForIso20022();
            String fieldLabel = getFieldLabelForCheckStubLengthMessage(document);
            String questionText = configurationService.getPropertyValueAsString(
                    CUPdpKeyConstants.QUESTION_CONFIRM_CHECK_STUB_LENGTH);
            String formattedQuestionText = MessageFormat.format(
                    questionText, fieldLabel, iso20022MaxStubLength);
            result = documentPreRules.askOrAnalyzeYesNoQuestion(
                    CUKFSConstants.CommonDocumentConstants.CHECK_STUB_TEXT_LENGTH_QUESTION_ID,
                    formattedQuestionText);
            if (!result) {
                documentPreRules.abortRulesCheck();
            }
        }
        return true;
    }

    @Override
    public String getFullCheckStub(Document document) {
        if (ObjectUtils.isNull(document)) {
            throw new IllegalArgumentException("document cannot be null");
        } else if (document instanceof DisbursementVoucherDocument) {
            return ((DisbursementVoucherDocument) document).getDisbVchrCheckStubText();
        } else if (document instanceof AccountsPayableDocumentBase) {
            AccountsPayableDocumentBase apDocument = ((AccountsPayableDocumentBase) document);
            Stream<String> noteLines = Stream.of(apDocument.getNoteLine1Text(), apDocument.getNoteLine2Text(),
                    apDocument.getNoteLine3Text());
            return noteLines.map(StringUtils::trim)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining(KFSConstants.BLANK_SPACE));
        } else {
            throw new IllegalArgumentException("document of type " + document.getClass().getSimpleName()
                    + " does not support check stubs");
        }
    }

    @Override
    public String getFullCheckStub(PaymentDetail paymentDetail) {
        if (ObjectUtils.isNull(paymentDetail)) {
            throw new IllegalArgumentException("paymentDetail cannot be null");
        } else if (CollectionUtils.isNotEmpty(paymentDetail.getNotes())) {
            return paymentDetail.getNotes().stream()
                    .filter(note -> StringUtils.startsWith(note.getCustomerNoteText(),
                            CuDisbursementVoucherConstants.DV_EXTRACT_TYPED_NOTE_PREFIX_IDENTIFIER))
                    .map(PaymentNoteText::getCustomerNoteText)
                    .map(noteText -> StringUtils.removeStart(
                            noteText, CuDisbursementVoucherConstants.DV_EXTRACT_TYPED_NOTE_PREFIX_IDENTIFIER))
                    .map(StringUtils::trim)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining(KFSConstants.BLANK_SPACE));
        } else {
            return KFSConstants.EMPTY_STRING;
        }
    }

    @Override
    public int getCheckStubMaxLengthForIso20022() {
        return checkStubMaxLengthForIso20022;
    }

    public void setCheckStubMaxLengthForIso20022(int checkStubMaxLengthForIso20022) {
        this.checkStubMaxLengthForIso20022 = checkStubMaxLengthForIso20022;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setNoteService(NoteService noteService) {
        this.noteService = noteService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

}
