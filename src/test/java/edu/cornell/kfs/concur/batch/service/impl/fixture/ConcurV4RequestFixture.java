package edu.cornell.kfs.concur.batch.service.impl.fixture;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants.RequestV4Status;
import edu.cornell.kfs.concur.ConcurTestConstants;
import edu.cornell.kfs.concur.ConcurTestConstants.ParameterTestValues;
import edu.cornell.kfs.concur.ConcurUtils;
import edu.cornell.kfs.concur.batch.fixture.ConcurFixtureUtils;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4AmountDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4CustomItemDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4ListItemDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4ReportDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4StatusDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4TypeDTO;
import edu.cornell.kfs.sys.CUKFSConstants;

public enum ConcurV4RequestFixture {
    PENDING_EXTERNAL_VALIDATION_TEST_REQUEST_JOHN_DOE(
            "AABBCCDDEEFFGGHHIIJJKKLLMMNNOOPP", "1HPQ",
            "Test Pre-Trip Approval", "Need Pre-Trip Approval for Testing Purposes",
            ConcurV4PersonFixture.JOHN_DOE, ConcurV4PersonFixture.TEST_MANAGER,
            RequestV4Status.PENDING_EXTERNAL_VALIDATION,
            "04/05/2022", "04/15/2022", "2022-01-01T05:00:00Z", "2022-01-01T05:06:07Z", "2022-01-03T08:30:55Z",
            ConcurTestConstants.CHART_IT, ConcurTestConstants.ACCT_1234321, ConcurTestConstants.SUB_ACCT_88888,
            ConcurTestConstants.SUB_OBJ_333, ConcurTestConstants.PROJ_AA_778899,
            false, ConcurV4DestinationFixture.MIAMI_FL, 159.99, 159.99, 159.99),

    PENDING_APPROVAL_TEST_REQUEST_JOHN_DOE(
            "AAEEIIUUYYAAEEIIUUYYAAEEIIUUYYAA", "2DPJ",
            "Convention in Miami, FL", "Attending a Test Convention in Miami, FL",
            ConcurV4PersonFixture.JOHN_DOE, ConcurV4PersonFixture.TEST_MANAGER,
            RequestV4Status.SUBMITTED_AND_PENDING_APPROVAL,
            "03/01/2022", "03/05/2022", "2022-01-15T12:00:00Z", "2022-01-15T12:24:36Z", "2022-01-15T12:24:36Z",
            ConcurTestConstants.CHART_IT, ConcurTestConstants.ACCT_1234321, ConcurTestConstants.SUB_ACCT_88888,
            ConcurTestConstants.SUB_OBJ_333, ConcurTestConstants.PROJ_AA_778899,
            false, ConcurV4DestinationFixture.MIAMI_FL, 622.49, 622.49, 622.49),

    PENDING_EXTERNAL_VALIDATION_REGULAR_REQUEST_JOHN_DOE(
            "CCAACCAACCAACCAACCAACCAACCAACCAA", "1EPG",
            "Actual Pre-Trip Approval", "Need Pre-Trip Approval for Certain Purposes",
            ConcurV4PersonFixture.JOHN_DOE, ConcurV4PersonFixture.MARY_GRANT,
            RequestV4Status.PENDING_EXTERNAL_VALIDATION,
            "04/05/2022", "04/15/2022", "2022-01-01T05:00:00Z", "2022-01-01T05:06:07Z", "2022-01-03T08:30:55Z",
            ConcurTestConstants.CHART_IT, ConcurTestConstants.ACCT_1234321, ConcurTestConstants.SUB_ACCT_88888,
            ConcurTestConstants.SUB_OBJ_333, ConcurTestConstants.PROJ_AA_778899,
            false, ConcurV4DestinationFixture.MIAMI_FL, 159.99, 159.99, 159.99);

    public final String id;
    public final String requestId;
    public final String name;
    public final String businessPurpose;
    public final ConcurV4PersonFixture owner;
    public final ConcurV4PersonFixture approver;
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
    public final ConcurV4DestinationFixture mainDestination;
    public final KualiDecimal totalApprovedAmount;
    public final KualiDecimal totalPostedAmount;
    public final KualiDecimal totalRemainingAmount;

    private ConcurV4RequestFixture(String id, String requestId, String name, String businessPurpose,
            ConcurV4PersonFixture owner, ConcurV4PersonFixture approver, RequestV4Status approvalStatus,
            String startDate, String endDate, String creationDate, String submitDate, String lastModifiedDate,
            String chartCode, String accountNumber, String subAccountNumber, String subObjectCode, String projectCode,
            boolean everSentBack, ConcurV4DestinationFixture mainDestination,
            double totalApprovedAmount, double totalPostedAmount, double totalRemainingAmount) {
        this.id = id;
        this.requestId = requestId;
        this.name = name;
        this.businessPurpose = businessPurpose;
        this.owner = owner;
        this.approver = approver;
        this.approvalStatus = approvalStatus;
        this.startDate = ConcurFixtureUtils.toDateTimeAtMidnight(startDate);
        this.endDate = ConcurFixtureUtils.toDateTimeAtMidnight(endDate);
        this.creationDate = ConcurUtils.parseUTCDateToDateTime(creationDate);
        this.submitDate = ConcurUtils.parseUTCDateToDateTime(submitDate);
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

    public ConcurRequestV4ReportDTO toConcurRequestV4ReportDTO() {
        ConcurRequestV4ReportDTO requestDTO = new ConcurRequestV4ReportDTO();
        requestDTO.setHref(buildRequestHref());
        requestDTO.setId(id);
        requestDTO.setApprovalStatus(buildApprovalStatusDTO());
        requestDTO.setApproved(isApproved());
        if (shouldAddApproverToRequestDTO()) {
            requestDTO.setApprover(approver.toConcurRequestV4PersonDTO());
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
        requestDTO.setSubmitDate(submitDate.toDate());
        requestDTO.setTotalApprovedAmount(buildAmountDTO(totalApprovedAmount));
        requestDTO.setTotalPostedAmount(buildAmountDTO(totalPostedAmount));
        requestDTO.setTotalRemainingAmount(buildAmountDTO(totalRemainingAmount));
        requestDTO.setType(buildTypeDTO());
        return requestDTO;
    }

    public ConcurRequestV4ListItemDTO toConcurRequestV4ListItemDTO() {
        ConcurRequestV4ListItemDTO listItemDTO = new ConcurRequestV4ListItemDTO();
        listItemDTO.setHref(buildRequestHref());
        listItemDTO.setId(id);
        listItemDTO.setApprovalStatus(buildApprovalStatusDTO());
        listItemDTO.setApproved(isApproved());
        if (shouldAddApproverToRequestDTO()) {
            listItemDTO.setApprover(approver.toConcurRequestV4PersonDTO());
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
        listItemDTO.setSubmitDate(submitDate.toDate());
        listItemDTO.setTotalApprovedAmount(buildAmountDTO(totalApprovedAmount));
        listItemDTO.setTotalPostedAmount(buildAmountDTO(totalPostedAmount));
        listItemDTO.setTotalRemainingAmount(buildAmountDTO(totalRemainingAmount));
        listItemDTO.setType(buildTypeDTO());
        return listItemDTO;
    }

    private String buildRequestHref() {
        return ParameterTestValues.REQUEST_V4_LOCALHOST_ENDPOINT + CUKFSConstants.SLASH + id;
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

    private boolean shouldAddApproverToRequestDTO() {
        return approver != null && (approvalStatus == RequestV4Status.SUBMITTED_AND_PENDING_APPROVAL
                || approvalStatus == RequestV4Status.PENDING_COST_OBJECT_APPROVAL);
    }

    private boolean isApproved() {
        return approvalStatus == RequestV4Status.APPROVED;
    }

    private boolean isPendingApproval() {
        switch (approvalStatus) {
            case SUBMITTED_AND_PENDING_APPROVAL :
            case PENDING_COST_OBJECT_APPROVAL :
            case PENDING_EXTERNAL_VALIDATION :
                return true;
            
            default :
                return false;
        }
    }

}
