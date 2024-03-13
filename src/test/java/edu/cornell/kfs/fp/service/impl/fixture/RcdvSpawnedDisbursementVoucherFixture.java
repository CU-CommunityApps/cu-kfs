package edu.cornell.kfs.fp.service.impl.fixture;

import java.util.List;
import java.util.stream.Collectors;

import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.mockito.Mockito;

import edu.cornell.kfs.fp.CuFPTestConstants;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.fp.service.impl.RecurringDisbursementVoucherDocumentRoutingServiceImpl;

public enum RcdvSpawnedDisbursementVoucherFixture {
    DV_VALID("55123456", "55000444", UserNameFixture.kfs),
    DV_ERROR_EXPIRED_ACCOUNT("55123457", "55000444", UserNameFixture.kfs,
            expiredAccountError("G555555", "IT", "G987654")),
    DV_ERROR_CLOSED_ACCOUNT("55678876", "49494949", UserNameFixture.kfs,
            closedAccountError("Accounting Line: 1, Chart: IT, Account: R111222 -  ")),
    DV_ERROR_INVALID_BANK("54333222", "53246800", UserNameFixture.kfs,
            unsupportedBankDisbursementError()),
    DV_VALID_2("50111234", "50101010", UserNameFixture.kfs),
    DV_ERROR_MULTIPLE("50111999", "49999997", UserNameFixture.kfs,
            unsupportedBankDisbursementError(),
            expiredAccountError("R777888", "IT", "R543210")),
    DV_ERROR_UNEXPECTED_INITIATOR("45454545", "44444444", UserNameFixture.ccs1),
    DV_ERROR_ROUTING_FAILURE("5200001", "5200000", UserNameFixture.kfs, true);

    public final String documentNumber;
    public final String rcdvDocumentNumber;
    public final UserNameFixture initiator;
    public final boolean forceRoutingError;
    private final List<ErrorMessage> validationErrors;

    private RcdvSpawnedDisbursementVoucherFixture(String documentNumber, String rcdvDocumentNumber,
            UserNameFixture initiator, ErrorMessage... validationErrors) {
        this(documentNumber, rcdvDocumentNumber, initiator, false, validationErrors);
    }

    private RcdvSpawnedDisbursementVoucherFixture(String documentNumber, String rcdvDocumentNumber,
            UserNameFixture initiator, boolean forceRoutingError, ErrorMessage... validationErrors) {
        this.documentNumber = documentNumber;
        this.rcdvDocumentNumber = rcdvDocumentNumber;
        this.initiator = initiator;
        this.forceRoutingError = forceRoutingError;
        this.validationErrors = List.of(validationErrors);
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public String getRcdvDocumentNumber() {
        return rcdvDocumentNumber;
    }

    public String getRcdvDocumentNumberForReportItem() {
        return forceRoutingError
                ? RecurringDisbursementVoucherDocumentRoutingServiceImpl.UNKNOWN_RCDV_ID
                : rcdvDocumentNumber;
    }

    public List<ErrorMessage> getValidationErrors() {
        return validationErrors.stream()
                .map(errorMessage -> new ErrorMessage(errorMessage.getErrorKey(), errorMessage.getMessageParameters()))
                .collect(Collectors.toUnmodifiableList());
    }

    public CuDisbursementVoucherDocument toDvDocument() {
        WorkflowDocument workflowDocument = Mockito.mock(WorkflowDocument.class);
        Mockito.when(workflowDocument.getDocumentId())
                .thenReturn(documentNumber);
        Mockito.when(workflowDocument.getInitiatorPrincipalId())
                .thenReturn(initiator.toString());

        CuDisbursementVoucherDocument document = new CuDisbursementVoucherDocument();
        document.setDocumentNumber(documentNumber);
        document.getDocumentHeader().setDocumentNumber(documentNumber);
        document.getDocumentHeader().setWorkflowDocument(workflowDocument);
        return document;
    }

    public int getExpectedErrorCount() {
        if (forceRoutingError || initiator != UserNameFixture.kfs) {
            return 1;
        } else {
            return validationErrors.size();
        }
    }

    private static ErrorMessage expiredAccountError(String account, String continuationChart,
            String continuationAccount) {
        return new ErrorMessage(KFSKeyConstants.ERROR_DOCUMENT_ACCOUNT_EXPIRED, account, continuationChart,
                continuationAccount);
    }

    private static ErrorMessage closedAccountError(String accountDetailsString) {
        return new ErrorMessage(KFSKeyConstants.ERROR_DOCUMENT_ACCOUNT_CLOSED_WITH_IDENTIFYING_ACCOUNTING_LINE,
                accountDetailsString, CuFPTestConstants.ACCOUNT_NUMBER_LABEL);
    }

    private static ErrorMessage unsupportedBankDisbursementError() {
        return new ErrorMessage(KFSKeyConstants.Bank.ERROR_DISBURSEMENT_NOT_SUPPORTED);
    }

}
