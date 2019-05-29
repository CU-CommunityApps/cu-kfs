/**
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2019 Kuali, Inc.
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
package org.kuali.kfs.module.ar.document;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.maintenance.Maintainable;
import org.kuali.kfs.kns.web.ui.Field;
import org.kuali.kfs.kns.web.ui.Row;
import org.kuali.kfs.kns.web.ui.Section;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.ArKeyConstants;
import org.kuali.kfs.module.ar.ArPropertyConstants;
import org.kuali.kfs.module.ar.businessobject.Bill;
import org.kuali.kfs.module.ar.businessobject.PredeterminedBillingSchedule;
import org.kuali.kfs.module.ar.document.service.PredeterminedBillingScheduleMaintenanceService;
import org.kuali.kfs.module.ar.document.validation.impl.PredeterminedBillingScheduleRuleUtil;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.FinancialSystemMaintainable;

import java.util.List;
import java.util.Map;

/*
 * CU Customization:
 * Added the 2019-05-02 patch version of this file as an overlay, to include the FINP-4678 fix.
 * Please remove this overlay once we upgrade to patch version 2019-05-02 or newer.
 */
/**
 * Methods for the Predetermined Billing Schedule maintenance document UI.
 */
public class PredeterminedBillingScheduleMaintainableImpl extends FinancialSystemMaintainable {
    private static volatile PredeterminedBillingScheduleMaintenanceService predeterminedBillingScheduleMaintenanceService;

    /**
     * This method is called to check if the award/account already has bills set, and to validate on refresh
     */
    @Override
    public void refresh(String refreshCaller, Map fieldValues, MaintenanceDocument document) {
        if (StringUtils.equals(ArConstants.AWARD_ACCOUNT_LOOKUP_IMPL, (String) fieldValues.get(KFSConstants.REFRESH_CALLER))) {
            final PredeterminedBillingSchedule predeterminedBillingSchedule = getPredeterminedBillingSchedule();
            predeterminedBillingSchedule.setProposalNumber(predeterminedBillingSchedule
                .getProposalNumberForAwardAccountLookup());
            predeterminedBillingSchedule.setChartOfAccountsCode(predeterminedBillingSchedule
                .getChartOfAccountsCodeForAwardAccountLookup());
            predeterminedBillingSchedule.setAccountNumber(predeterminedBillingSchedule
                .getAccountNumberForAwardAccountLookup());

            if (PredeterminedBillingScheduleRuleUtil.checkIfBillsExist(predeterminedBillingSchedule)) {
                String pathToMaintainable = KFSPropertyConstants.DOCUMENT + "."
                    + KFSPropertyConstants.NEW_MAINTAINABLE_OBJECT;
                GlobalVariables.getMessageMap().addToErrorPath(pathToMaintainable);
                GlobalVariables.getMessageMap().putError(ArPropertyConstants.PROPOSAL_NUMBER_FOR_AWARD_ACCOUNT_LOOKUP,
                    ArKeyConstants.ERROR_AWARD_PREDETERMINED_BILLING_SCHEDULE_EXISTS,
                    predeterminedBillingSchedule.getProposalNumber(),
                    predeterminedBillingSchedule.getChartOfAccountsCode(),
                    predeterminedBillingSchedule.getAccountNumber());
                GlobalVariables.getMessageMap().removeFromErrorPath(pathToMaintainable);
            }
        } else {
            super.refresh(refreshCaller, fieldValues, document);
        }
    }

    /**
     * Not to copy over the Bills billed and billIdentifier values to prevent
     * bad data and PK issues when saving new Bills.
     */
    @Override
    public void processAfterCopy(MaintenanceDocument document, Map<String, String[]> parameters) {
        super.processAfterCopy(document, parameters);

        // clear out Bill IDs so new ones will get generated for these bills
        // reset billed indicator in case bill we're copying from was already billed
        List<Bill> bills = getPredeterminedBillingSchedule().getBills();
        if (ObjectUtils.isNotNull(bills)) {
            for (Bill bill : bills) {
                bill.setBilled(false);
                bill.setBillIdentifier(null);
            }
        }
    }

    /**
     * Refresh any references that may need to be reconstituted after the PredeterminedBillingSchedule has been
     * populated from the maintenance document xml contents.
     */
    @Override
    public void processAfterRetrieve() {
        PredeterminedBillingSchedule predeterminedBillingSchedule = getPredeterminedBillingSchedule();
        predeterminedBillingSchedule.refreshNonUpdateableReferences();
        predeterminedBillingSchedule.setAward(null);
        predeterminedBillingSchedule.setAward(predeterminedBillingSchedule.getAward());
        super.processAfterRetrieve();
    }

    /**
     * Gets the underlying Predetermined Billing Schedule.
     *
     * @return
     */
    private PredeterminedBillingSchedule getPredeterminedBillingSchedule() {
        return (PredeterminedBillingSchedule) getBusinessObject();
    }

    /**
     * Override the getSections method on this maintainable so that the active field can be set to read-only when
     * a CINV doc has been created with this Predetermined Billing Schedule and Bills
     */
    @Override
    public List getSections(MaintenanceDocument document, Maintainable oldMaintainable) {
        List<Section> sections = super.getSections(document, oldMaintainable);
        String proposalNumber = getPredeterminedBillingSchedule().getProposalNumber();

        for (Section section : sections) {
            String sectionId = section.getSectionId();
            if (sectionId.equalsIgnoreCase(ArPropertyConstants.BILLS)) {
                prepareBillsTab(section, proposalNumber);
            }
        }

        return sections;
    }

    /**
     * Sets the Bill in the passed in section to be readonly if it has been copied to a CG Invoice doc.
     *
     * @param section        Bill section to review and possibly set readonly
     * @param proposalNumber used to look for CG Invoice docs
     */
    private void prepareBillsTab(Section section, String proposalNumber) {
        for (Row row : section.getRows()) {
            for (Field field : row.getFields()) {
                if (field.getCONTAINER().equalsIgnoreCase(field.getFieldType())) {
                    for (Row containerRow : field.getContainerRows()) {
                        for (Field containerRowfield : containerRow.getFields()) {
                            // a record is no longer editable if the bill has been copied to a CINV doc
                            if (ObjectUtils.getNestedAttributePrimitive(containerRowfield.getPropertyName())
                                    .matches(ArPropertyConstants.BillFields.BILL_IDENTIFIER)) {
                                String billId = containerRowfield.getPropertyValue();
                                if (StringUtils.isNotEmpty(billId)) {
                                    if (getPredeterminedBillingScheduleMaintenanceService()
                                            .hasBillBeenCopiedToInvoice(proposalNumber, billId)) {
                                        for (Field rowfield : row.getFields()) {
                                            if (rowfield.getCONTAINER().equalsIgnoreCase(rowfield.getFieldType())) {
                                                for (Row fieldContainerRow : rowfield.getContainerRows()) {
                                                    for (Field fieldContainerRowField : fieldContainerRow.getFields()) {
                                                        fieldContainerRowField.setReadOnly(true);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private PredeterminedBillingScheduleMaintenanceService getPredeterminedBillingScheduleMaintenanceService() {
        if (predeterminedBillingScheduleMaintenanceService == null) {
            predeterminedBillingScheduleMaintenanceService = SpringContext
                .getBean(PredeterminedBillingScheduleMaintenanceService.class);
        }
        return predeterminedBillingScheduleMaintenanceService;
    }
}
