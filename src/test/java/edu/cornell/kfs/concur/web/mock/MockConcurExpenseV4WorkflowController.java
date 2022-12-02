package edu.cornell.kfs.concur.web.mock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurConstants.ConcurWorkflowActions;
import edu.cornell.kfs.concur.ConcurTestWorkflowInfo;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurV4WorkflowDTO;

@RestController
public class MockConcurExpenseV4WorkflowController {

    private static final String REPORT_ID_VARIABLE = "reportId";
    private static final String ACTION_VARIABLE = "action";

    private final String expectedAuthorizationHeader;
    private final ConcurrentMap<String, ConcurTestWorkflowInfo> reportActions;
    private final AtomicBoolean forceInternalServerError;

    public MockConcurExpenseV4WorkflowController(String expectedAccessToken) {
        if (StringUtils.isBlank(expectedAccessToken)) {
            throw new IllegalArgumentException("expectedAccessToken cannot be blank");
        }
        this.expectedAuthorizationHeader = StringUtils.join(
                ConcurConstants.BEARER_AUTHENTICATION_SCHEME, KFSConstants.BLANK_SPACE, expectedAccessToken);
        this.reportActions = new ConcurrentHashMap<>();
        this.forceInternalServerError = new AtomicBoolean(false);
    }

    @PatchMapping(
            path = "/expensereports/v4/reports/{reportId}/{action}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> performExpenseReportWorkflowAction(
            @RequestBody ConcurV4WorkflowDTO requestContent,
            @RequestHeader(ConcurConstants.AUTHORIZATION_PROPERTY) String authorizationHeader,
            @PathVariable(REPORT_ID_VARIABLE) String reportId,
            @PathVariable(ACTION_VARIABLE) String workflowAction
    ) {
        if (!StringUtils.equalsAnyIgnoreCase(workflowAction,
                ConcurWorkflowActions.APPROVE, ConcurWorkflowActions.SEND_BACK)) {
            return ResponseEntity.notFound().build();
        } else if (forceInternalServerError.get()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Forced Server-Side Error");
        }
        checkAuthorizationHeaderIsValid(authorizationHeader);
        
        ConcurTestWorkflowInfo oldWorkflowInfo = reportActions.get(reportId);
        checkReportExistsAndIsAwaitingAction(reportId, oldWorkflowInfo);
        checkRequestContentIsValid(requestContent);
        
        reportActions.put(reportId, new ConcurTestWorkflowInfo(
                workflowAction, requestContent.getComment(), oldWorkflowInfo.getVersionNumber() + 1));
        return ResponseEntity.noContent().build();
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

    private void checkReportExistsAndIsAwaitingAction(String reportId, ConcurTestWorkflowInfo workflowInfo) {
        if (workflowInfo == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unknown report ID: " + reportId);
        } else if (StringUtils.isNotBlank(workflowInfo.getActionTaken())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Action has already been taken on report with ID: " + reportId);
        }
    }

    private void checkRequestContentIsValid(ConcurV4WorkflowDTO requestContent) {
        if (requestContent == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Expected JSON content is missing");
        } else if (StringUtils.isBlank(requestContent.getComment())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Workflow action comment is missing");
        } else if (StringUtils.length(requestContent.getComment())
                > ConcurConstants.VALIDATION_RESULT_MESSAGE_MAX_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Workflow action comment exceeds max length");
        }
    }

    public void setForceInternalServerError(boolean forceInternalServerError) {
        this.forceInternalServerError.set(forceInternalServerError);
    }

    public void addReportsAwaitingAction(String... reportIds) {
        for (String reportId : reportIds) {
            reportActions.put(reportId, ConcurTestWorkflowInfo.EMPTY);
        }
    }

    public ConcurTestWorkflowInfo getWorkflowInfoForReport(String reportId) {
        return reportActions.get(reportId);
    }

    public void overrideWorkflowInfoForReport(String reportId, ConcurTestWorkflowInfo workflowInfo) {
        reportActions.put(reportId, workflowInfo);
    }

}
