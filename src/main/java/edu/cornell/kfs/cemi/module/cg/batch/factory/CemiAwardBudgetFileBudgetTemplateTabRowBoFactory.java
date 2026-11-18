package edu.cornell.kfs.cemi.module.cg.batch.factory;

import org.apache.commons.lang3.Validate;

import edu.cornell.kfs.cemi.module.cg.CemiAwardConstants;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardBudgetFileBudgetTemplateTabRowBo;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;

public class CemiAwardBudgetFileBudgetTemplateTabRowBoFactory {

    private String jobRunDateString;
    private boolean maskSensitiveData = true;

    public CemiAwardBudgetFileBudgetTemplateTabRowBoFactory(final String jobRunDateString,
            final boolean maskSensitiveData) {
        Validate.validState(jobRunDateString != null,
                "jobRunDateString cannot be null for CemiAwardBudgetFileBudgetTemplateTabRowBoFactory.");
        this.jobRunDateString = jobRunDateString;
        this.maskSensitiveData = maskSensitiveData;
    }

    public CemiAwardBudgetFileBudgetTemplateTabRowBo createCemiAwardBudgetFileBudgetTemplateTabRowBo() {
        final CemiAwardBudgetFileBudgetTemplateTabRowBo budgetTemplateTabDataRow = new CemiAwardBudgetFileBudgetTemplateTabRowBo();

        // Reference information related to business object being created that must be specified.
        // Both of these attributes are being set by abstract class CemiOrmDataBuilderBase when storeSheetRow is invoked.
        //      attribute jobRunRowIndex
        //      attribute jobRunDateString

        //Format and assign data values for these attributes as defined by the Huron mapping template specification.
//TODO FIXME replace placeholder values with the actual Award ID data in the base data iteration of this extract.
        budgetTemplateTabDataRow.setProposalNumberUsedForDataRow("{CGPRPSL_NBR}");
        budgetTemplateTabDataRow.setSpreadsheetKey("ITH_{CGPRPSL_NBR}");
        budgetTemplateTabDataRow.setPlanTemplateId("Template_ITH_{CGPRPSL_NBR}");
        budgetTemplateTabDataRow.setCompany(CemiAwardConstants.COMPANY_CORNELL_UNIVERSITY_MAIN_CAMPUS);
        budgetTemplateTabDataRow.setPlanStructure(CemiAwardConstants.AWARD);
        budgetTemplateTabDataRow.setEntryType(CemiAwardConstants.AWARD);
        budgetTemplateTabDataRow.setBudgetSource("ITH_{CGPRPSL_NBR}");
        budgetTemplateTabDataRow.setDefaultReportingCurrency(CemiAwardConstants.CURRENCY_USD);
        budgetTemplateTabDataRow.setDefaultBalancedAmendmentReference(CemiAwardConstants.NO);

        //Attributes specified as blank in the Huron mapping template specification.
        budgetTemplateTabDataRow.setAddOnly(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setPlanTemplate(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setIsInactive(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setOrganizationAsOfDateReference(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setTopPlanHierarchy(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setIncludeAllOrganizationsUnderTopPlanHierarchyReference(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setOrganizations(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setRollUpSubordinateReference(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setStatisticType(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setBudgetSourceId(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setPlanName(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setEnableMulticurrencyReference(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setCurrencyRateType(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setCurrencyRateTypeForBudgetSpend(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setAmendmentIncreaseParentDisabled(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setEnableForAllocationsReference(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setValidateExpenseAccountAmendmentsReference(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setRowId(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setFiscalYear(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setFiscalSchedule(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setFromYear(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setFromFiscalSchedule(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setFromSummaryPeriod(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setFromFiscalPostingInterval(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setVirtualParentProjectBudgetStartDateReference(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setToYear(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setToFiscalSchedule(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setToSummaryPeriod(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setToFiscalPostingInterval(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setVirtualParentProjectBudgetEndDateReference(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setLinkedVirtualChildrenBudgets(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setCurrencyRateTypeForBudget(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setProject(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setPlanCalculationType(CemiBaseConstants.EMPTY_STRING);
        budgetTemplateTabDataRow.setAssignPlanAs(CemiBaseConstants.EMPTY_STRING);

        return budgetTemplateTabDataRow;
    }

}
