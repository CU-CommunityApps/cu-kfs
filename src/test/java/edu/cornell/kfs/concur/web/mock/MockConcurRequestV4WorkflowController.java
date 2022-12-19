package edu.cornell.kfs.concur.web.mock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurConstants.ConcurWorkflowActions;
import edu.cornell.kfs.concur.ConcurConstants.RequestV4Status;
import edu.cornell.kfs.concur.ConcurTestConstants.ParameterTestValues;
import edu.cornell.kfs.concur.ConcurTestWorkflowInfo;
import edu.cornell.kfs.concur.batch.service.impl.fixture.RequestV4DetailFixture;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4ReportDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4StatusDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurV4WorkflowDTO;
import edu.cornell.kfs.sys.web.mock.ResettableController;

@RestController
public class MockConcurRequestV4WorkflowController implements ResettableController {

    private static final String REQUEST_UUID_VARIABLE = "requestUuid";
    private static final String ACTION_VARIABLE = "action";

    private final String expectedAuthorizationHeader;
    private final String baseRequestUrl;
    private final ConcurrentMap<String, RequestV4DetailFixture> travelRequests;
    private final ConcurrentMap<String, ConcurTestWorkflowInfo> requestActions;
    private final AtomicBoolean forceInternalServerError;
    private final AtomicBoolean forceMalformedResponse;

    public MockConcurRequestV4WorkflowController(String expectedAccessToken, String httpServerUrl) {
        if (StringUtils.isBlank(expectedAccessToken)) {
            throw new IllegalArgumentException("expectedAccessToken cannot be blank");
        }
        this.expectedAuthorizationHeader = StringUtils.join(
                ConcurConstants.BEARER_AUTHENTICATION_SCHEME, KFSConstants.BLANK_SPACE, expectedAccessToken);
        this.baseRequestUrl = httpServerUrl + ParameterTestValues.REQUEST_V4_RELATIVE_ENDPOINT;
        this.travelRequests = new ConcurrentHashMap<>();
        this.requestActions = new ConcurrentHashMap<>();
        this.forceInternalServerError = new AtomicBoolean(false);
        this.forceMalformedResponse = new AtomicBoolean(false);
    }

    @Override
    public void reset() {
        travelRequests.clear();
        requestActions.clear();
        forceInternalServerError.set(false);
        forceMalformedResponse.set(false);
    }

    @PostMapping(
            path = "/travelrequest/v4/requests/{requestUuid}/{action}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ConcurRequestV4ReportDTO> performTravelRequestWorkflowAction(
            @RequestBody ConcurV4WorkflowDTO workflowContent,
            @RequestHeader(ConcurConstants.AUTHORIZATION_PROPERTY) String authorizationHeader,
            @PathVariable(REQUEST_UUID_VARIABLE) String requestUuid,
            @PathVariable(ACTION_VARIABLE) String workflowAction
    ) {
        if (!StringUtils.equalsAny(workflowAction,
                ConcurWorkflowActions.APPROVE, ConcurWorkflowActions.SEND_BACK)) {
            return ResponseEntity.notFound().build();
        } else if (forceInternalServerError.get()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Forced Server-Side Error");
        }
        checkAuthorizationHeaderIsValid(authorizationHeader);
        
        RequestV4DetailFixture travelRequest = getExistingTravelRequest(requestUuid);
        ConcurTestWorkflowInfo oldWorkflowInfo = getWorkflowInfoForRequestAndVerifyStatus(travelRequest);
        checkWorkflowContentIsValid(workflowContent);
        
        ConcurTestWorkflowInfo newWorkflowInfo = new ConcurTestWorkflowInfo(
                workflowAction, workflowContent.getComment(), oldWorkflowInfo.getVersionNumber() + 1);
        requestActions.put(travelRequest.id, newWorkflowInfo);
        
        ConcurRequestV4ReportDTO requestDTO = createDTOForResponse(travelRequest, newWorkflowInfo);
        if (forceMalformedResponse.get()) {
            requestDTO.setApprovalStatus(null);
        }
        return ResponseEntity.ok(requestDTO);
    }

    private void checkAuthorizationHeaderIsValid(String authorizationHeader) {
        if (StringUtils.isBlank(authorizationHeader)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Authorization header is missing or empty");
        } else if (!StringUtils.equals(authorizationHeader, expectedAuthorizationHeader)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Not authorized due to malformed or unrecognized access token");
        }
    }

    private RequestV4DetailFixture getExistingTravelRequest(String requestUuid) {
        RequestV4DetailFixture travelRequest = travelRequests.get(requestUuid);
        if (travelRequest == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown travel request UUID: " + requestUuid);
        }
        return travelRequest;
    }

    private ConcurTestWorkflowInfo getWorkflowInfoForRequestAndVerifyStatus(RequestV4DetailFixture travelRequest) {
        ConcurTestWorkflowInfo workflowInfo = requestActions.get(travelRequest.id);
        if (workflowInfo == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Illegal server-side state detected for travel request with UUID: " + travelRequest.id);
        } else if (StringUtils.isNotBlank(workflowInfo.getActionTaken())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Action has already been taken on travel request with UUID: " + travelRequest.id);
        }
        return workflowInfo;
    }

    private void checkWorkflowContentIsValid(ConcurV4WorkflowDTO workflowContent) {
        if (workflowContent == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Expected JSON content is missing");
        } else if (StringUtils.isBlank(workflowContent.getComment())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Workflow action comment is missing");
        } else if (StringUtils.length(workflowContent.getComment())
                > ConcurConstants.VALIDATION_RESULT_MESSAGE_MAX_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Workflow action comment exceeds max length");
        }
    }

    private ConcurRequestV4ReportDTO createDTOForResponse(RequestV4DetailFixture travelRequest,
            ConcurTestWorkflowInfo workflowInfo) {
        ConcurRequestV4ReportDTO requestDTO = travelRequest.toConcurRequestV4ReportDTO(baseRequestUrl);
        RequestV4Status status = getStatusMetadataForUpdatingResponse(workflowInfo, travelRequest.id);
        boolean isApproved = status == RequestV4Status.APPROVED;
        ConcurRequestV4StatusDTO statusDTO = requestDTO.getApprovalStatus();
        
        requestDTO.setApproved(isApproved);
        statusDTO.setCode(status.code);
        statusDTO.setName(status.name);
        if (status == RequestV4Status.SENTBACK) {
            requestDTO.setEverSentBack(true);
        }
        
        return requestDTO;
    }

    private RequestV4Status getStatusMetadataForUpdatingResponse(ConcurTestWorkflowInfo workflowInfo,
            String requestUuid) {
        if (StringUtils.equalsIgnoreCase(workflowInfo.getActionTaken(), ConcurWorkflowActions.APPROVE)) {
            return RequestV4Status.APPROVED;
        } else if (StringUtils.equalsIgnoreCase(workflowInfo.getActionTaken(), ConcurWorkflowActions.SEND_BACK)) {
            return RequestV4Status.SENTBACK;
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Illegal server-side state detected for travel request with UUID: " + requestUuid);
        }
    }

    public void setForceInternalServerError(boolean forceInternalServerError) {
        this.forceInternalServerError.set(forceInternalServerError);
    }

    public void setForceMalformedResponse(boolean forceMalformedResponse) {
        this.forceMalformedResponse.set(forceMalformedResponse);
    }

    public void addTravelRequestsAwaitingAction(RequestV4DetailFixture... requestFixtures) {
        for (RequestV4DetailFixture requestFixture : requestFixtures) {
            travelRequests.put(requestFixture.id, requestFixture);
            requestActions.put(requestFixture.id, ConcurTestWorkflowInfo.EMPTY);
        }
    }

    public boolean doesTravelRequestExistOnMockServer(String requestUuid) {
        return travelRequests.containsKey(requestUuid);
    }

    public ConcurTestWorkflowInfo getWorkflowInfoForTravelRequest(String requestUuid) {
        return requestActions.get(requestUuid);
    }

    public void overrideWorkflowInfoForTravelRequest(String requestUuid, ConcurTestWorkflowInfo workflowInfo) {
        requestActions.put(requestUuid, workflowInfo);
    }

}
