package edu.cornell.kfs.cemi.module.cg.batch.factory;

import java.text.MessageFormat;

import org.apache.commons.lang3.Validate;
import org.kuali.kfs.module.cg.businessobject.Award;

import edu.cornell.kfs.cemi.module.cg.CemiAwardBudgetConstants;
import edu.cornell.kfs.cemi.module.cg.CemiAwardConstants;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardBudgetFileBudgetTemplateTabRowBo;

@SuppressWarnings("deprecation")
public class CemiAwardBudgetFileBudgetTemplateTabRowBoFactory {

    private Award award;

    public CemiAwardBudgetFileBudgetTemplateTabRowBoFactory(final Award award) {
        this.award = award;
    }

    public CemiAwardBudgetFileBudgetTemplateTabRowBo createCemiAwardBudgetFileBudgetTemplateTabRowBo() {
        Validate.validState(award != null, "Award cannot be null.");

        final CemiAwardBudgetFileBudgetTemplateTabRowBo budgetTemplateTabDataRow = new CemiAwardBudgetFileBudgetTemplateTabRowBo();

        final String rowSpreadsheetKey = buildSpreadsheetKey(award.getProposalNumber());
        final String rowPlanTemplateId = buildPlanTemplateId(award.getProposalNumber());

        budgetTemplateTabDataRow.setProposalNumberUsedForDataRow(award.getProposalNumber());

        budgetTemplateTabDataRow.setSpreadsheetKey(rowSpreadsheetKey);
        budgetTemplateTabDataRow.setPlanTemplateId(rowPlanTemplateId);
        budgetTemplateTabDataRow.setCompany(CemiAwardConstants.COMPANY_CORNELL_UNIVERSITY_MAIN_CAMPUS);
        budgetTemplateTabDataRow.setPlanStructure(CemiAwardConstants.AWARD);
        budgetTemplateTabDataRow.setEntryType(CemiAwardConstants.AWARD);
        budgetTemplateTabDataRow.setBudgetSource(rowSpreadsheetKey);
        budgetTemplateTabDataRow.setDefaultReportingCurrency(CemiAwardConstants.CURRENCY_USD);
        budgetTemplateTabDataRow.setDefaultBalancedAmendmentReference(CemiAwardConstants.NO);

        return budgetTemplateTabDataRow;
    }

    private static String buildSpreadsheetKey(final String awardProposalNumber) {
        return MessageFormat.format(CemiAwardConstants.SPREADSHEET_KEY_FORMAT, awardProposalNumber);
    }

    private static String buildPlanTemplateId(final String awardProposalNumber) {
        return MessageFormat.format(CemiAwardBudgetConstants.PLAN_TEMPLATE_ID_FORMAT, awardProposalNumber);
    }

}
