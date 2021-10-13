/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.fp.document.validation.impl;

import org.kuali.kfs.fp.FPKeyConstants;
import org.kuali.kfs.fp.document.BudgetAdjustmentDocument;
import org.kuali.kfs.fp.document.service.BudgetAdjustmentLaborBenefitsService;
import org.kuali.kfs.fp.document.web.struts.BudgetAdjustmentForm;
import org.kuali.kfs.fp.service.AccountingDocumentPreRuleService;
import org.kuali.kfs.kns.rules.PromptBeforeValidationBase;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocumentBase;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.kew.api.WorkflowDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks warnings and prompt conditions for ba document.
 */
/*
 * CU Customization: Backported this file from the 2021-05-06 financials patch to bring in the FINP-7506 changes.
 * This overlay should be removed when we upgrade to the 2021-05-06 financials patch.
 */
public class BudgetAdjustmentDocumentPreRules extends PromptBeforeValidationBase {

    protected ConfigurationService kualiConfiguration;

    /**
     * Execute pre-rules for BudgetAdjustmentDocument
     *
     * @param document document with pre-rules being applied
     * @return true if pre-rules fire without problem
     */
    @Override
    public boolean doPrompts(Document document) {
        BudgetAdjustmentDocument budgetDocument = (BudgetAdjustmentDocument) document;
        boolean preRulesOK = askLaborBenefitsGeneration(budgetDocument);

        preRulesOK &= SpringContext.getBean(AccountingDocumentPreRuleService.class).expiredAccountOverrideQuestion(
                (AccountingDocumentBase) document, this);

        return preRulesOK;
    }

    /**
     * Calls service to determine if any labor object codes are present on the ba document. If so, asks the user if they
     * want the system to automatically generate the benefit lines. If Yes, calls service to generate the accounting lines.
     *
     * @param budgetDocument submitted budget document
     * @return true if labor benefits generation question is NOT asked
     */
    protected boolean askLaborBenefitsGeneration(BudgetAdjustmentDocument budgetDocument) {
        // before prompting, check the document contains one or more labor object codes
        final boolean hasLaborObjectCodes = SpringContext.getBean(BudgetAdjustmentLaborBenefitsService.class)
                .hasLaborObjectCodes(budgetDocument);
        final boolean canEdit = ((BudgetAdjustmentForm) form).getDocumentActions().containsKey(
                KRADConstants.KUALI_ACTION_CAN_EDIT);
        final boolean canGenerateLaborBenefitsByRouteStatusResult = canGenerateLaborBenefitsByRouteStatus(
                budgetDocument);
        if (canEdit && hasLaborObjectCodes && canGenerateLaborBenefitsByRouteStatusResult) {
            final String questionText = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(
                    FPKeyConstants.QUESTION_GENERATE_LABOR_BENEFIT_LINES);
            final boolean generateBenefits = super.askOrAnalyzeYesNoQuestion(
                    KFSConstants.BudgetAdjustmentDocumentConstants.GENERATE_BENEFITS_QUESTION_ID, questionText);
            if (generateBenefits) {
                SpringContext.getBean(BudgetAdjustmentLaborBenefitsService.class).generateLaborBenefitsAccountingLines(
                        budgetDocument);
                // return to document after lines are generated
                super.event.setActionForwardName(KFSConstants.MAPPING_BASIC);
                return false;
            }
        }

        return true;
    }

    /**
     * TODO: remove this method once baseline accounting lines has been removed
     */
    protected List deepCopyAccountingLinesList(List originals) {
        if (originals == null) {
            return null;
        }
        List copiedLines = new ArrayList();
        for (Object original : originals) {
            copiedLines.add(ObjectUtils.deepCopy((AccountingLine) original));
        }
        return copiedLines;
    }

    /**
     * Based on the routing status of the document, determines if labor benefits can be generated on the document
     *
     * @param budgetAdjustmentDocument the budget adjustment document that labor benefits would be generated on
     * @return true if labor benefits can be generated, false otherwise
     */
    protected boolean canGenerateLaborBenefitsByRouteStatus(BudgetAdjustmentDocument budgetAdjustmentDocument) {
        final WorkflowDocument workflowDocument = budgetAdjustmentDocument.getDocumentHeader().getWorkflowDocument();
        if (workflowDocument.isInitiated() || workflowDocument.isSaved()) {
            // we're pre-route; we can add labor benefits
            return true;
        }

        return workflowDocument.isEnroute() && workflowDocument.getCurrentNodeNames().contains(
                KFSConstants.RouteLevelNames.ACCOUNT);
    }
}

