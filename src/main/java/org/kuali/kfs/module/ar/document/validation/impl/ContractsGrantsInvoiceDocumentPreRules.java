/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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
package org.kuali.kfs.module.ar.document.validation.impl;

import java.util.Date;

import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAward;
import org.kuali.kfs.kns.rules.PromptBeforeValidationBase;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.ArKeyConstants;
import org.kuali.kfs.module.ar.businessobject.InvoiceGeneralDetail;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.sys.context.SpringContext;

//CU customization: backport FINP-5292, this file can be removed when we upgrade to the 06/30/2022 version of financials
public class ContractsGrantsInvoiceDocumentPreRules extends PromptBeforeValidationBase {

    private DateTimeService dateTimeService;
    private AccountingPeriodService accountingPeriodService;
    private ConfigurationService configurationService;

    @Override
    public boolean doPrompts(final Document document) {
        return checkFinalBillIndicator(document);
    }

    private boolean checkFinalBillIndicator(final Document document) {

        if (document instanceof ContractsGrantsInvoiceDocument) {
            final ContractsGrantsInvoiceDocument cinv = (ContractsGrantsInvoiceDocument) document;
            final InvoiceGeneralDetail invoiceGeneralDetail = cinv.getInvoiceGeneralDetail();
            if (invoiceGeneralDetail.isFinalBillIndicator()) {
                final java.sql.Date today = getDateTimeService().getCurrentSqlDate();
                final AccountingPeriod currPeriod = getAccountingPeriodService().getByDate(today);
                final Date acctingPeriodEndDate = currPeriod.getUniversityFiscalPeriodEndDate();

                final ContractsAndGrantsBillingAward award = invoiceGeneralDetail.getAward();
                final Date projectEndDate = award.getAwardEndingDate();

                boolean confirmUpdate = true;
                if (acctingPeriodEndDate.before(projectEndDate)) {
                    String questionText = getConfigurationService().getPropertyValueAsString(ArKeyConstants.ContractsGrantsInvoiceConstants.PROMPT_FINAL_BILL_INDICATOR);
                    confirmUpdate = askOrAnalyzeYesNoQuestion(ArConstants.UPDATE_FINAL_BILL_INDICATOR_QUESTION, questionText);
                }

                if (!confirmUpdate) {
                    abortRulesCheck();
                    return false;
                }
            }
        }

        return true;
    }

    private DateTimeService getDateTimeService() {
        if (dateTimeService == null) {
            dateTimeService = SpringContext.getBean(DateTimeService.class);
        }
        return dateTimeService;
    }

    private AccountingPeriodService getAccountingPeriodService() {
        if (accountingPeriodService == null) {
            accountingPeriodService = SpringContext.getBean(AccountingPeriodService.class);
        }
        return accountingPeriodService;
    }

    private ConfigurationService getConfigurationService() {
        if (configurationService == null) {
            configurationService = SpringContext.getBean(ConfigurationService.class);
        }
        return configurationService;
    }

}