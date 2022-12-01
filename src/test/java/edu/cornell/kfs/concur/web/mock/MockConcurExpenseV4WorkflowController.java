package edu.cornell.kfs.concur.web.mock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.cornell.kfs.concur.ConcurConstants.ConcurWorkflowActions;
import edu.cornell.kfs.concur.ConcurTestWorkflowInfo;

@RestController
public class MockConcurExpenseV4WorkflowController {

    private static final String REPORT_ID_VARIABLE = "reportId";
    private static final String ACTION_VARIABLE = "action";

    private final AtomicReference<String> expectedBearerToken;
    private final ConcurrentMap<String, ConcurTestWorkflowInfo> reportActions;

    public MockConcurExpenseV4WorkflowController() {
        this.expectedBearerToken = new AtomicReference<>();
        this.reportActions = new ConcurrentHashMap<>();
    }

    @PatchMapping(
            path = "/expensereports/v4/reports/{reportId}/{action}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> performExpenseReportWorkflowAction(
            HttpServletRequest request, @PathVariable(REPORT_ID_VARIABLE) String reportId,
            @PathVariable(ACTION_VARIABLE) String workflowAction) {
        if (!StringUtils.equalsAnyIgnoreCase(workflowAction,
                ConcurWorkflowActions.APPROVE, ConcurWorkflowActions.SEND_BACK)) {
            return ResponseEntity.notFound().build();
        }
        
        checkReportExistsAndIsAwaitingAction(reportId);
        
        return ResponseEntity.noContent().build();
    }

    private void checkReportExistsAndIsAwaitingAction(String reportId) {
        ConcurTestWorkflowInfo workflowInfo = reportActions.get(reportId);
        if (workflowInfo == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unknown report ID: " + reportId);
        } else if (StringUtils.isNotBlank(workflowInfo.getActionTaken())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Action has already been taken on report with ID: " + reportId);
        }
    }

    public void setExpectedBearerToken(String expectedBearerToken) {
        this.expectedBearerToken.set(expectedBearerToken);
    }

    public void addReportsAwaitingAction(String... reportIds) {
        for (String reportId : reportIds) {
            reportActions.put(reportId, ConcurTestWorkflowInfo.EMPTY);
        }
    }

    public void overrideWorkflowInfoForReport(String reportId, ConcurTestWorkflowInfo workflowInfo) {
        reportActions.put(reportId, workflowInfo);
    }

}
