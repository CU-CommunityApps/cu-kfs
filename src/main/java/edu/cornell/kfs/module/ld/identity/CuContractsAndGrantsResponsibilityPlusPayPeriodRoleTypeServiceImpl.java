package edu.cornell.kfs.module.ld.identity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.kuali.kfs.coa.identity.ContractsAndGrantsResponsibilityRoleTypeServiceImpl;
import org.kuali.kfs.integration.ld.LaborLedgerExpenseTransferAccountingLine;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.coreservice.framework.CoreFrameworkServiceLocator;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.role.RoleMembership;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;

import edu.cornell.kfs.module.ld.CuLaborParameterConstants;

/**
 * Custom subclass of ContractsAndGrantsResponsibilityRoleTypeServiceImpl that can optionally filter memberships
 * based on whether the pay period the document was created in is far enough ahead of the document's earliest
 * accounting line pay period.
 */
public class CuContractsAndGrantsResponsibilityPlusPayPeriodRoleTypeServiceImpl extends ContractsAndGrantsResponsibilityRoleTypeServiceImpl {

    @Override
    public List<RoleMembership> getMatchingRoleMemberships(Map<String,String> qualification, List<RoleMembership> roleMemberList) {
        List<RoleMembership> memberships = super.getMatchingRoleMemberships(qualification, roleMemberList);
        String documentTypeName = qualification.get(KimConstants.AttributeConstants.DOCUMENT_TYPE_NAME);
        String documentNumber = qualification.get(KimConstants.AttributeConstants.DOCUMENT_NUMBER);
        
        // If necessary, filter out members based on the number of pay periods between doc creation and earliest accounting line payment.
        if (StringUtils.isNotBlank(documentTypeName) && StringUtils.isNotBlank(documentNumber)) {
            Collection<String> docTypesWithAwardExceptionRouting = CoreFrameworkServiceLocator.getParameterService().getParameterValuesAsString(
                    KFSConstants.OptionalModuleNamespaces.LABOR_DISTRIBUTION, KfsParameterConstants.DOCUMENT_COMPONENT,
                            CuLaborParameterConstants.DOC_TYPES_WITH_AWARD_EXCEPTION_ROUTING);
            
            // If the document's doc type is in the list, then perform alternate/exception Award routing instead.
            if (docTypesWithAwardExceptionRouting.contains(documentTypeName)) {
                // Get the maximum allowable fiscal/pay period difference for skipping Award routing.
                String payPeriodDifferenceLimit = CoreFrameworkServiceLocator.getParameterService().getParameterValueAsString(
                        KFSConstants.OptionalModuleNamespaces.LABOR_DISTRIBUTION, KfsParameterConstants.DOCUMENT_COMPONENT,
                                CuLaborParameterConstants.DEFAULT_NUMBER_OF_FISCAL_PERIODS_FOR_AWARD_EXCEPTION_ROUTING);
                
                // If parameter is non-blank and difference is within limit, then skip the routing.
                if (StringUtils.isNotBlank(payPeriodDifferenceLimit)
                        && documentIsWithinPayPeriodLimit(documentNumber, Integer.parseInt(payPeriodDifferenceLimit))) {
                    return Collections.emptyList();
                }
            }
        }
        
        return memberships;
    }



    /*
     * Helper method for determining if the difference between the document's create date period and
     * the earliest account pay period is within the given limit. Assumed to be within limit unless
     * proven otherwise.
     */
    private boolean documentIsWithinPayPeriodLimit(String documentNumber, int limit) {
        boolean withinLimit = true;
        AccountingDocument document;
        
        // Get the document, which is expected to be an accounting one.
        try {
            document = (AccountingDocument) SpringContext.getBean(DocumentService.class).getByDocumentHeaderId(documentNumber);
        } catch (ClassCastException e) {
            document = null;
        }
        
        // Skip non-existent or non-retrievable documents, and have them default to within-limit routing.
        if (document == null) {
            return true;
        }
        
        // Make sure source/target lines containing the extra labor-ledger-related data actually exist on the document.
        boolean hasLLSourceLine = document.getSourceAccountingLineClass() != null
                && CollectionUtils.isNotEmpty(document.getSourceAccountingLines()) 
                && LaborLedgerExpenseTransferAccountingLine.class.isAssignableFrom(document.getSourceAccountingLineClass());
        boolean hasLLTargetLine = document.getTargetAccountingLineClass() != null
                && CollectionUtils.isNotEmpty(document.getTargetAccountingLines())
                && LaborLedgerExpenseTransferAccountingLine.class.isAssignableFrom(document.getTargetAccountingLineClass());
        
        if (hasLLSourceLine || hasLLTargetLine) {
            // Locate the earliest source/target line.
            LaborLedgerExpenseTransferAccountingLine earliestLine = null;
            if (hasLLSourceLine) {
                earliestLine = findEarliestPayPeriodAccountingLine(document.getSourceAccountingLines());
            }
            if (hasLLTargetLine) {
                if (earliestLine != null) {
                    LaborLedgerExpenseTransferAccountingLine earliestTargetLine = findEarliestPayPeriodAccountingLine(document.getTargetAccountingLines());
                    earliestLine = (earliestTargetLine == null)
                            ? earliestLine : findEarliestPayPeriodAccountingLine(Arrays.asList(earliestLine, earliestTargetLine));
                } else {
                    earliestLine = findEarliestPayPeriodAccountingLine(document.getTargetAccountingLines());
                }
            }
            
            // If an earliest line was found, then proceed with the within-limit determination.
            if (earliestLine != null) {
                // Prepare helper constants.
                final int NUM_MONTHS = 12;
                final int FY_OFFSET = 6;
                
                // Get the creation date, and calculate its corresponding fiscal year and pay period.
                LocalDateTime dateCreated = document.getDocumentHeader().getWorkflowDocument().getDateCreated();
                int dateCreatedFiscalYear;
                int dateCreatedPayPeriod = dateCreated.getMonthValue() + FY_OFFSET;
                if (dateCreatedPayPeriod > NUM_MONTHS) {
                    dateCreatedPayPeriod -= NUM_MONTHS;
                }
                dateCreatedFiscalYear = dateCreated.getYear() + ((dateCreatedPayPeriod <= FY_OFFSET) ? 1 : 0);
                
                // Determine difference between the pay period the doc was created in and the earliest impacted source/target account's pay period.
                int payPeriodDifference = ((dateCreatedFiscalYear - earliestLine.getPayrollEndDateFiscalYear().intValue()) * NUM_MONTHS)
                        + dateCreatedPayPeriod - Integer.parseInt(earliestLine.getPayrollEndDateFiscalPeriodCode());
                // Set flag based on whether the difference is within the limit for standard Award node routing.
                withinLimit = payPeriodDifference <= limit;
            }
        }
        
        return withinLimit;
    }



    /*
     * Helper method for finding the account with the earliest pay period. Ones with earlier years take precedence.
     */
    private LaborLedgerExpenseTransferAccountingLine findEarliestPayPeriodAccountingLine(List<?> accountingLines) {
        LaborLedgerExpenseTransferAccountingLine earliestLine = null;
        
        for (Object line : accountingLines) {
            LaborLedgerExpenseTransferAccountingLine accountingLine = (LaborLedgerExpenseTransferAccountingLine) line;
            if (earliestLine == null) {
                // If the first line, set it as the earliest by default.
                if (accountingLine.getPayrollEndDateFiscalYear() != null && StringUtils.isNotBlank(accountingLine.getPayrollEndDateFiscalPeriodCode())) {
                    earliestLine = accountingLine;
                }
            } else if (accountingLine.getPayrollEndDateFiscalYear() != null && StringUtils.isNotBlank(accountingLine.getPayrollEndDateFiscalPeriodCode())) {
                // For lines beyond the first, find the one with the earliest pay period.
                int tempCompare = accountingLine.getPayrollEndDateFiscalYear().compareTo(earliestLine.getPayrollEndDateFiscalYear());
                if (tempCompare < 0 || (tempCompare == 0 && accountingLine.getPayrollEndDateFiscalPeriodCode().compareTo(
                        earliestLine.getPayrollEndDateFiscalPeriodCode()) < 0)) {
                    earliestLine = accountingLine;
                }
            }
        }
        
        return earliestLine;
    }
}
