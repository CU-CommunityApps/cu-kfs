/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2020 Kuali, Inc.
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
import org.kuali.kfs.fp.document.GeneralLedgerTransferDocument;
import org.kuali.kfs.fp.document.service.GeneralLedgerTransferService;
import org.kuali.kfs.fp.service.AccountingDocumentPreRuleService;
import org.kuali.kfs.kns.rules.PromptBeforeValidationBase;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import java.util.Set;

/*
 * CU Customization:
 * Added the FINP-6813 fix from the 2020-07-02 financials patch.
 */
public class GeneralLedgerTransferDocumentPreRules extends PromptBeforeValidationBase {
    private AccountingDocumentPreRuleService accountingDocumentPreRuleService;
    private ConfigurationService configurationService;
    private GeneralLedgerTransferService generalLedgerTransferService;

    @Override
    public boolean doPrompts(Document document) {
        GeneralLedgerTransferDocument generalLedgerTransferDocument = (GeneralLedgerTransferDocument) document;

        boolean preRulesOK = getAccountingDocumentPreRuleService()
                .expiredAccountOverrideQuestion(generalLedgerTransferDocument, this);
        preRulesOK &= lateAdjustmentStatementQuestion(generalLedgerTransferDocument);

        return preRulesOK;
    }

    protected boolean lateAdjustmentStatementQuestion(GeneralLedgerTransferDocument generalLedgerTransferDocument) {
        Set<String> currentActiveNodes =
            generalLedgerTransferDocument.getDocumentHeader().getWorkflowDocument().getCurrentNodeNames();
        if (!currentActiveNodes.contains(KFSConstants.RouteLevelNames.ACCOUNT)) {
            return true;
        }

        if (getGeneralLedgerTransferService().doesExceedDefaultNumberOfDays(
                generalLedgerTransferDocument.getSourceAccountingLines(),
                generalLedgerTransferDocument.getTargetAccountingLines())) {
            return showCertificationStatementQuestion();
        }

        return true;
    }

    protected boolean showCertificationStatementQuestion() {
        String questionText = getConfigurationService().getPropertyValueAsString(
            FPKeyConstants.QUESTION_ADJUSTMENT_STATEMENT);
        boolean approved = askOrAnalyzeYesNoQuestion(
            KFSConstants.GeneralLedgerTransferLateAdjustment.QUESTION_ID, questionText);

        if (!approved) {
            event.setActionForwardName(KFSConstants.MAPPING_BASIC);
        }
        return approved;
    }

    public AccountingDocumentPreRuleService getAccountingDocumentPreRuleService() {
        if (ObjectUtils.isNull(accountingDocumentPreRuleService)) {
            accountingDocumentPreRuleService = SpringContext.getBean(AccountingDocumentPreRuleService.class);
        }

        return accountingDocumentPreRuleService;
    }

    public void setAccountingDocumentPreRuleService(AccountingDocumentPreRuleService accountingDocumentPreRuleService) {
        this.accountingDocumentPreRuleService = accountingDocumentPreRuleService;
    }

    public ConfigurationService getConfigurationService() {
        if (ObjectUtils.isNull(configurationService)) {
            configurationService = SpringContext.getBean(ConfigurationService.class);
        }

        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public GeneralLedgerTransferService getGeneralLedgerTransferService() {
        if (ObjectUtils.isNull(generalLedgerTransferService)) {
            generalLedgerTransferService = SpringContext.getBean(GeneralLedgerTransferService.class);
        }

        return generalLedgerTransferService;
    }

    public void setGeneralLedgerTransferService(GeneralLedgerTransferService generalLedgerTransferService) {
        this.generalLedgerTransferService = generalLedgerTransferService;
    }
}
