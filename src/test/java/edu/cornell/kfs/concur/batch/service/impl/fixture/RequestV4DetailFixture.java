package edu.cornell.kfs.concur.batch.service.impl.fixture;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNotificationStatus;
import edu.cornell.kfs.concur.ConcurConstants.RequestV4Status;
import edu.cornell.kfs.concur.ConcurTestConstants;
import edu.cornell.kfs.concur.ConcurUtils;
import edu.cornell.kfs.concur.batch.fixture.ConcurFixtureUtils;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4AmountDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4CustomItemDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4ListItemDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4ReportDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4StatusDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4TypeDTO;
import edu.cornell.kfs.sys.CUKFSConstants;

public enum RequestV4DetailFixture {
    PENDING_EXTERNAL_VALIDATION_TEST_REQUEST_JOHN_TEST(
            "AABBCCDDEEFFGGHHIIJJKKLLMMNNOOPP", "1HPQ",
            "Test Pre-Trip Approval", "Need Pre-Trip Approval for Testing Purposes",
            RequestV4PersonFixture.JOHN_TEST, noPendingApprover(),
            RequestV4Status.PENDING_EXTERNAL_VALIDATION,
            "04/05/2022", "04/15/2022", "2022-01-01T05:00:00.000Z", "2022-01-01T05:06:07.000Z", "2022-01-03T08:30:55.000Z",
            ConcurTestConstants.CHART_IT, ConcurTestConstants.ACCT_1234321, ConcurTestConstants.SUB_ACCT_88888,
            ConcurTestConstants.SUB_OBJ_333, ConcurTestConstants.PROJ_AA_778899,
            false, RequestV4DestinationFixture.MIAMI_FL, 159.99, 159.99, 159.99),

    PENDING_APPROVAL_TEST_REQUEST_JOHN_TEST(
            "AAEEIIUUYYAAEEIIUUYYAAEEIIUUYYAA", "2DPJ",
            "Convention in Miami, FL", "Attending a Test Convention in Miami, FL",
            RequestV4PersonFixture.JOHN_TEST, pendingApprover(RequestV4PersonFixture.TEST_APPROVER),
            RequestV4Status.SUBMITTED_AND_PENDING_APPROVAL,
            "03/01/2022", "03/05/2022", "2022-01-15T12:00:00.000Z", "2022-01-15T12:24:36.000Z", "2022-01-15T12:24:36.000Z",
            ConcurTestConstants.CHART_IT, ConcurTestConstants.ACCT_1234321, ConcurTestConstants.SUB_ACCT_88888,
            ConcurTestConstants.SUB_OBJ_333, ConcurTestConstants.PROJ_AA_778899,
            false, RequestV4DestinationFixture.MIAMI_FL, 622.49, 622.49, 622.49),

    PENDING_EXTERNAL_VALIDATION_REGULAR_REQUEST_JOHN_DOE(
            "CCAACCAACCAACCAACCAACCAACCAACCAA", "1EPG",
            "Actual Pre-Trip Approval", "Need Pre-Trip Approval for Certain Purposes",
            RequestV4PersonFixture.JOHN_DOE, noPendingApprover(),
            RequestV4Status.PENDING_EXTERNAL_VALIDATION,
            "04/05/2022", "04/15/2022", "2022-01-01T05:00:00.000Z", "2022-01-01T05:06:07.000Z", "2022-01-03T08:30:55.000Z",
            ConcurTestConstants.CHART_IT, ConcurTestConstants.ACCT_1234321, ConcurTestConstants.SUB_ACCT_88888,
            ConcurTestConstants.SUB_OBJ_333, ConcurTestConstants.PROJ_AA_778899,
            false, RequestV4DestinationFixture.MIAMI_FL, 159.99, 159.99, 159.99),

    CANCELED_TEST_REQUEST_JOHN_TEST(
            "JJGGJJGGJJGGJJGGJJGGJJGGJJGGJJGG", "7GJG",
            "Test Pre-Trip Approval for Denver", "Need Pre-Trip Approval for Testing",
            RequestV4PersonFixture.JOHN_TEST, pendingApprover(RequestV4PersonFixture.TEST_APPROVER),
            RequestV4Status.CANCELED,
            "04/07/2022", "04/17/2022", "2022-01-03T05:01:00.000Z", "2022-01-03T05:07:09.000Z", "2022-01-03T08:30:55.000Z",
            ConcurTestConstants.CHART_IT, ConcurTestConstants.ACCT_1234321, ConcurTestConstants.SUB_ACCT_88888,
            ConcurTestConstants.SUB_OBJ_333, ConcurTestConstants.PROJ_AA_778899,
            false, RequestV4DestinationFixture.DENVER_CO, 189.99, 189.99, 189.99),

    APPROVED_TEST_REQUEST_JANE_DOE(
            "AABBCCAABBCCAABBCCAABBCCAABBCCAA", "7XQJ",
            "Test Team Meeting In Denver", "There is a test meeting over in Colorado",
            RequestV4PersonFixture.JANE_DOE, noPendingApprover(),
            RequestV4Status.APPROVED,
            "05/01/2022", "05/31/2022", "2022-03-03T08:08:08.000Z", "2022-03-03T09:09:09.000Z", "2022-03-03T10:10:10.000Z",
            ConcurTestConstants.CHART_IT, ConcurTestConstants.ACCT_4455667, null, null, null,
            false, RequestV4DestinationFixture.DENVER_CO, 250.00, 250.00, 250.00),

    PENDING_EXTERNAL_VALIDATION_TEST_REQUEST_JANE_DOE(
            "AAZZBBYYCCXXDDWWEEVVFFUUGGTTHHSS", "3QRS",
            "Another Test Meeting In Denver", "Another test meeting is taking place in the state of Colorado",
            RequestV4PersonFixture.JANE_DOE, noPendingApprover(),
            RequestV4Status.PENDING_EXTERNAL_VALIDATION,
            "06/01/2022", "06/30/2022", "2022-03-03T08:09:08.000Z", "2022-03-03T09:10:09.000Z", "2022-03-03T10:11:10.000Z",
            ConcurTestConstants.CHART_IT, ConcurTestConstants.ACCT_4455667, null, null, null,
            false, RequestV4DestinationFixture.DENVER_CO, 250.00, 250.00, 250.00),
    
    PENDING_EXTERNAL_VALIDATION_INVALID_TEST_REQUEST_JANE_DOE(
            "11MM22LL33KK44JJ55II66HH77GG88FF", "9VVV",
            "Test Business Travel to Miami", "Visiting Miami for test-only business purposes",
            RequestV4PersonFixture.JANE_DOE, noPendingApprover(),
            RequestV4Status.PENDING_EXTERNAL_VALIDATION,
            "04/22/2022", "04/26/2022", "2022-04-01T09:00:34.000Z", "2022-04-01T09:15:11.000Z", "2022-04-05T22:33:44.000Z",
            ConcurTestConstants.CHART_IT, ConcurTestConstants.ACCT_XXXXXXX, null, null, null,
            false, RequestV4DestinationFixture.MIAMI_FL, 350.00, 350.00, 350.00),

    SENTBACK_INVALID_TEST_REQUEST_JANE_DOE(
            "98MM98LL76KK76JJ54II54HH32GG32FF", "7AZB",
            "More Test Business Travel to Miami", "Visiting Miami for another test-only business purpose",
            RequestV4PersonFixture.JANE_DOE, noPendingApprover(),
            RequestV4Status.SENTBACK,
            "04/29/2022", "05/09/2022", "2022-04-01T09:05:34.000Z", "2022-04-01T09:25:11.000Z", "2022-04-05T22:33:47.000Z",
            ConcurTestConstants.CHART_IT, ConcurTestConstants.ACCT_XXXXXXX, null, null, null,
            true, RequestV4DestinationFixture.MIAMI_FL, 422.00, 422.00, 422.00),

    PENDING_COST_APPROVAL_REGULAR_REQUEST_BOB_SMITH(
            "5K5K5K5K5K5K5K5K5K5K5K5K5K5K5K5K", "2EFG",
            "Actual Business Travel to Miami", "Visiting Miami for regular business purposes",
            RequestV4PersonFixture.BOB_SMITH, pendingApprover(RequestV4PersonFixture.MARY_GRANT),
            RequestV4Status.PENDING_COST_OBJECT_APPROVAL,
            "04/22/2022", "04/26/2022", "2022-04-01T11:00:34.000Z", "2022-04-01T11:15:11.000Z", "2022-04-05T23:33:44.000Z",
            ConcurTestConstants.CHART_IT, ConcurTestConstants.ACCT_4455667, null, null, null,
            false, RequestV4DestinationFixture.MIAMI_FL, 750.00, 750.00, 750.00),

    PENDING_EXTERNAL_VALIDATION_INVALID_REGULAR_REQUEST_BOB_SMITH(
            "4V4V4V4V4V4V4V4V4V4V4V4V4V4V4V4V", "4PPP",
            "Attend Convention in Denver", "Request related to a convention over in Colorado",
            RequestV4PersonFixture.BOB_SMITH, noPendingApprover(),
            RequestV4Status.PENDING_EXTERNAL_VALIDATION,
            "07/29/2022", "08/05/2022", "2022-04-04T18:00:35.000Z", "2022-04-04T18:25:22.000Z", "2022-04-05T23:44:55.000Z",
            ConcurTestConstants.CHART_IT, ConcurTestConstants.ACCT_XXXXXXX, null, null, null,
            false, RequestV4DestinationFixture.DENVER_CO, 999.99, 999.99, 999.99),

    PENDING_EXTERNAL_VALIDATION_REGULAR_REQUEST_BOB_SMITH(
            "90909090909090909090909090909090", "3JKL",
            "Attend Convention in Miami", "Request related to a convention over in Florida",
            RequestV4PersonFixture.BOB_SMITH, noPendingApprover(),
            RequestV4Status.PENDING_EXTERNAL_VALIDATION,
            "08/16/2022", "08/20/2022", "2022-04-04T19:00:35.000Z", "2022-04-04T19:25:22.000Z", "2022-04-06T23:59:59.000Z",
            ConcurTestConstants.CHART_IT, ConcurTestConstants.ACCT_4455667, null, null, null,
            false, RequestV4DestinationFixture.MIAMI_FL, 1024.55, 1024.55, 1024.55),

    NOT_SUBMITTED_REGULAR_REQUEST_BOB_SMITH(
            "4Z5Z6Z5Z4Z5Z6Z5Z4Z5Z6Z5Z4Z5Z6Z5Z", "8QKC",
            "Pre-Trip Approval for Denver", "Requesting Pre-Trip Approval for an Event in Colorado",
            RequestV4PersonFixture.BOB_SMITH, noPendingApprover(),
            RequestV4Status.NOT_SUBMITTED,
            "12/01/2022", "12/12/2022", "2022-04-05T00:00:00.000Z", null, "2022-04-07T00:00:00.000Z",
            ConcurTestConstants.CHART_IT, ConcurTestConstants.ACCT_4455667, null, null, null,
            false, RequestV4DestinationFixture.DENVER_CO, 1205.79, 1205.79, 1205.79);

    private static Optional<RequestV4PersonFixture> pendingApprover(RequestV4PersonFixture pendingApprover) {
        return Optional.of(pendingApprover);
    }

    private static Optional<RequestV4PersonFixture> noPendingApprover() {
        return Optional.empty();
    }

    public final String id;
    public final String requestId;
    public final String name;
    public final String businessPurpose;
    public final RequestV4PersonFixture owner;
    public final Optional<RequestV4PersonFixture> pendingApprover;
    public final RequestV4Status approvalStatus;
    public final DateTime startDate;
    public final DateTime endDate;
    public final DateTime creationDate;
    public final DateTime submitDate;
    public final DateTime lastModifiedDate;
    public final String chartCode;
    public final String accountNumber;
    public final String subAccountNumber;
    public final String subObjectCode;
    public final String projectCode;
    public final boolean everSentBack;
    public final RequestV4DestinationFixture mainDestination;
    public final KualiDecimal totalApprovedAmount;
    public final KualiDecimal totalPostedAmount;
    public final KualiDecimal totalRemainingAmount;

    private RequestV4DetailFixture(String id, String requestId, String name, String businessPurpose,
            RequestV4PersonFixture owner,
            Optional<RequestV4PersonFixture> pendingApprover, RequestV4Status approvalStatus,
            String startDate, String endDate, String creationDate, String submitDate, String lastModifiedDate,
            String chartCode, String accountNumber, String subAccountNumber, String subObjectCode, String projectCode,
            boolean everSentBack, RequestV4DestinationFixture mainDestination,
            double totalApprovedAmount, double totalPostedAmount, double totalRemainingAmount) {
        this.id = id;
        this.requestId = requestId;
        this.name = name;
        this.businessPurpose = businessPurpose;
        this.owner = owner;
        this.pendingApprover = pendingApprover;
        this.approvalStatus = approvalStatus;
        this.startDate = ConcurFixtureUtils.toDateTimeAtMidnight(startDate);
        this.endDate = ConcurFixtureUtils.toDateTimeAtMidnight(endDate);
        this.creationDate = ConcurUtils.parseUTCDateToDateTime(creationDate);
        this.submitDate = StringUtils.isNotBlank(submitDate) ? ConcurUtils.parseUTCDateToDateTime(submitDate) : null;
        this.lastModifiedDate = ConcurUtils.parseUTCDateToDateTime(lastModifiedDate);
        this.chartCode = chartCode;
        this.accountNumber = accountNumber;
        this.subAccountNumber = subAccountNumber;
        this.subObjectCode = subObjectCode;
        this.projectCode = projectCode;
        this.everSentBack = everSentBack;
        this.mainDestination = mainDestination;
        this.totalApprovedAmount = new KualiDecimal(totalApprovedAmount);
        this.totalPostedAmount = new KualiDecimal(totalPostedAmount);
        this.totalRemainingAmount = new KualiDecimal(totalRemainingAmount);
    }

    public ConcurRequestV4ReportDTO toConcurRequestV4ReportDTO(String baseRequestUrl) {
        ConcurRequestV4ReportDTO requestDTO = new ConcurRequestV4ReportDTO();
        requestDTO.setHref(buildRequestHref(baseRequestUrl));
        requestDTO.setId(id);
        requestDTO.setApprovalStatus(buildApprovalStatusDTO());
        requestDTO.setApproved(isApproved());
        if (shouldAddApproverToRequestDTO()) {
            requestDTO.setApprover(pendingApprover.get().toConcurRequestV4PersonDTO());
        }
        requestDTO.setCanceledPostApproval(false);
        requestDTO.setClosed(false);
        requestDTO.setCreationDate(creationDate.toDate());
        requestDTO.setChart(buildCustomItemDTO(chartCode));
        requestDTO.setAccount(buildCustomItemDTO(accountNumber));
        requestDTO.setSubAccount(buildCustomItemDTO(subAccountNumber));
        requestDTO.setSubObjectCode(buildCustomItemDTO(subObjectCode));
        requestDTO.setProjectCode(buildCustomItemDTO(projectCode));
        requestDTO.setEndDate(endDate.toDate());
        requestDTO.setEverSentBack(everSentBack);
        requestDTO.setUserReviewed(true);
        requestDTO.setLastModifiedDate(lastModifiedDate.toDate());
        requestDTO.setMainDestination(mainDestination.toConcurRequestV4MainDestinationDTO());
        requestDTO.setName(name);
        requestDTO.setOwner(owner.toConcurRequestV4PersonDTO());
        requestDTO.setPendingApproval(isPendingApproval());
        requestDTO.setRequestId(requestId);
        requestDTO.setStartDate(startDate.toDate());
        if (submitDate != null) {
            requestDTO.setSubmitDate(submitDate.toDate());
        }
        requestDTO.setTotalApprovedAmount(buildAmountDTO(totalApprovedAmount));
        requestDTO.setTotalPostedAmount(buildAmountDTO(totalPostedAmount));
        requestDTO.setTotalRemainingAmount(buildAmountDTO(totalRemainingAmount));
        requestDTO.setType(buildTypeDTO());
        return requestDTO;
    }

    public ConcurRequestV4ListItemDTO toConcurRequestV4ListItemDTO(String baseRequestUrl) {
        ConcurRequestV4ListItemDTO listItemDTO = new ConcurRequestV4ListItemDTO();
        listItemDTO.setHref(buildRequestHref(baseRequestUrl));
        listItemDTO.setId(id);
        listItemDTO.setApprovalStatus(buildApprovalStatusDTO());
        listItemDTO.setApproved(isApproved());
        if (shouldAddApproverToRequestDTO()) {
            listItemDTO.setApprover(pendingApprover.get().toConcurRequestV4PersonDTO());
        }
        listItemDTO.setBusinessPurpose(businessPurpose);
        listItemDTO.setCanceledPostApproval(false);
        listItemDTO.setClosed(false);
        listItemDTO.setCreationDate(creationDate.toDate());
        listItemDTO.setEndDate(endDate.toDate());
        listItemDTO.setEverSentBack(everSentBack);
        listItemDTO.setHighestExceptionLevel(ConcurTestConstants.REQUEST_EXCEPTION_LEVEL_NONE);
        listItemDTO.setUserReviewed(true);
        listItemDTO.setName(name);
        listItemDTO.setOwner(owner.toConcurRequestV4PersonDTO());
        listItemDTO.setPendingApproval(isPendingApproval());
        listItemDTO.setRequestId(requestId);
        listItemDTO.setStartDate(startDate.toDate());
        if (submitDate != null) {
            listItemDTO.setSubmitDate(submitDate.toDate());
        }
        listItemDTO.setTotalApprovedAmount(buildAmountDTO(totalApprovedAmount));
        listItemDTO.setTotalPostedAmount(buildAmountDTO(totalPostedAmount));
        listItemDTO.setTotalRemainingAmount(buildAmountDTO(totalRemainingAmount));
        listItemDTO.setType(buildTypeDTO());
        return listItemDTO;
    }

    private ConcurRequestV4StatusDTO buildApprovalStatusDTO() {
        ConcurRequestV4StatusDTO statusDTO = new ConcurRequestV4StatusDTO();
        statusDTO.setCode(approvalStatus.code);
        statusDTO.setName(approvalStatus.name);
        return statusDTO;
    }

    private ConcurRequestV4CustomItemDTO buildCustomItemDTO(String dataValue) {
        if (StringUtils.isBlank(dataValue)) {
            return null;
        }
        ConcurRequestV4CustomItemDTO itemDTO = new ConcurRequestV4CustomItemDTO();
        itemDTO.setCode(dataValue);
        itemDTO.setValid(true);
        return itemDTO;
    }

    private ConcurRequestV4AmountDTO buildAmountDTO(KualiDecimal amount) {
        ConcurRequestV4AmountDTO amountDTO = new ConcurRequestV4AmountDTO();
        amountDTO.setValue(amount);
        amountDTO.setCurrency(ConcurTestConstants.CURRENCY_USD);
        return amountDTO;
    }

    private ConcurRequestV4TypeDTO buildTypeDTO() {
        ConcurRequestV4TypeDTO typeDTO = new ConcurRequestV4TypeDTO();
        typeDTO.setCode(ConcurTestConstants.REQUEST_TRAVEL_TYPE_CODE);
        typeDTO.setLabel(ConcurTestConstants.REQUEST_TRAVEL_TYPE_LABEL);
        return typeDTO;
    }

    public String buildRequestHref(String baseRequestUrl) {
        return baseRequestUrl + CUKFSConstants.SLASH + id;
    }

    public boolean shouldAddApproverToRequestDTO() {
        return pendingApprover.isPresent() && (approvalStatus == RequestV4Status.SUBMITTED_AND_PENDING_APPROVAL
                || approvalStatus == RequestV4Status.PENDING_COST_OBJECT_APPROVAL);
    }

    public boolean isApproved() {
        return approvalStatus == RequestV4Status.APPROVED;
    }

    public boolean isPendingApproval() {
        switch (approvalStatus) {
            case SUBMITTED_AND_PENDING_APPROVAL :
            case PENDING_COST_OBJECT_APPROVAL :
            case PENDING_EXTERNAL_VALIDATION :
                return true;
            
            default :
                return false;
        }
    }

    public ConcurEventNotificationStatus getExpectedProcessingResult() {
        return isExpectedToPassAccountValidation()
                ? ConcurEventNotificationStatus.validAccounts
                : ConcurEventNotificationStatus.invalidAccounts;
    }

    public boolean isExpectedToPassAccountValidation() {
        return StringUtils.isNotBlank(chartCode) && StringUtils.isNotBlank(accountNumber)
                && !StringUtils.equalsIgnoreCase(ConcurTestConstants.ACCT_XXXXXXX, accountNumber);
    }

    public boolean isOwnedByTestUser() {
        return owner.testUser;
    }
}
