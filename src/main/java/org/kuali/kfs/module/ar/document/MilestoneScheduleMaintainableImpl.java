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
import org.kuali.kfs.module.ar.businessobject.Milestone;
import org.kuali.kfs.module.ar.businessobject.MilestoneSchedule;
import org.kuali.kfs.module.ar.document.service.MilestoneScheduleMaintenanceService;
import org.kuali.kfs.module.ar.document.validation.impl.MilestoneScheduleRuleUtil;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.FinancialSystemMaintainable;

import java.util.List;
import java.util.Map;

/*
 * CU Customization: Overlayed this file to include the FINP-4678 fix from the 2019-05-02 KualiCo patch.
 * Please remove this overlay once we upgrade to financials version 2019-05-02 or newer.
 */
/**
 * Methods for the Milestone Schedule maintenance document UI.
 */
public class MilestoneScheduleMaintainableImpl extends FinancialSystemMaintainable {
    private static volatile MilestoneScheduleMaintenanceService milestoneScheduleMaintenanceService;

    /**
     * This method is called to check if the award/account already has milestones set, and to validate on refresh
     */
    @Override
    public void refresh(String refreshCaller, Map fieldValues, MaintenanceDocument document) {
        if (StringUtils.equals(ArConstants.AWARD_ACCOUNT_LOOKUP_IMPL,
            (String) fieldValues.get(KFSConstants.REFRESH_CALLER))) {

            MilestoneSchedule milestoneSchedule = getMilestoneSchedule();
            milestoneSchedule.setProposalNumber(milestoneSchedule.getProposalNumberForAwardAccountLookup());
            milestoneSchedule.setChartOfAccountsCode(milestoneSchedule.getChartOfAccountsCodeForAwardAccountLookup());
            milestoneSchedule.setAccountNumber(milestoneSchedule.getAccountNumberForAwardAccountLookup());

            if (MilestoneScheduleRuleUtil.checkIfMilestonesExists(milestoneSchedule)) {
                String pathToMaintainable = KFSPropertyConstants.DOCUMENT + "."
                    + KFSPropertyConstants.NEW_MAINTAINABLE_OBJECT;
                GlobalVariables.getMessageMap().addToErrorPath(pathToMaintainable);
                GlobalVariables.getMessageMap().putError(ArPropertyConstants.PROPOSAL_NUMBER_FOR_AWARD_ACCOUNT_LOOKUP,
                    ArKeyConstants.ERROR_AWARD_MILESTONE_SCHEDULE_EXISTS, milestoneSchedule.getProposalNumber(),
                    milestoneSchedule.getChartOfAccountsCode(), milestoneSchedule.getAccountNumber());
                GlobalVariables.getMessageMap().removeFromErrorPath(pathToMaintainable);
            }
        } else {
            super.refresh(refreshCaller, fieldValues, document);
        }
    }

    /**
     * Not to copy over the Milestones billed and milestoneIdentifier values to prevent
     * bad data and PK issues when saving new Milestones.
     */
    @Override
    public void processAfterCopy(MaintenanceDocument document, Map<String, String[]> parameters) {
        super.processAfterCopy(document, parameters);

        // clear out Bill IDs so new ones will get generated for these bills
        // reset billed indicator in case bill we're copying from was already billed
        List<Milestone> milestones = getMilestoneSchedule().getMilestones();
        if (ObjectUtils.isNotNull(milestones)) {
            for (Milestone milestone : milestones) {
                milestone.setBilled(false);
                milestone.setMilestoneIdentifier(null);
            }
        }
    }

    /**
     * Refresh any references that may need to be reconstituted after the MilestoneSchedule has been populated from
     * the maintenance document xml contents.
     */
    @Override
    public void processAfterRetrieve() {
        MilestoneSchedule milestoneSchedule = getMilestoneSchedule();
        milestoneSchedule.refreshNonUpdateableReferences();
        milestoneSchedule.forceAwardUpdate();
        super.processAfterRetrieve();
    }

    /**
     * Override the getSections method on this maintainable so that the active field can be set to read-only when
     * a CINV doc has been created with this Milestone Schedule and Milestones
     */
    @Override
    public List getSections(MaintenanceDocument document, Maintainable oldMaintainable) {
        List<Section> sections = super.getSections(document, oldMaintainable);
        MilestoneSchedule milestoneSchedule = (MilestoneSchedule) document.getNewMaintainableObject().getBusinessObject();
        String proposalNumber = milestoneSchedule.getProposalNumber();

        for (Section section : sections) {
            String sectionId = section.getSectionId();
            if (sectionId.equalsIgnoreCase(ArConstants.MILESTONES_SECTION)) {
                prepareMilestonesTab(section, proposalNumber);
            }
        }

        return sections;
    }

    /**
     * Sets the Milestone in the passed in section to be readonly if it has been copied to a CG Invoice doc.
     *
     * @param section        Milestone section to review and possibly set readonly
     * @param proposalNumber used to look for CG Invoice docs
     */
    protected void prepareMilestonesTab(Section section, String proposalNumber) {
        for (Row row : section.getRows()) {
            for (Field field : row.getFields()) {
                if (field.getCONTAINER().equalsIgnoreCase(field.getFieldType())) {
                    for (Row containerRow : field.getContainerRows()) {
                        for (Field containerRowfield : containerRow.getFields()) {
                            // a record is no longer editable if the bill has been copied to a CINV doc
                            if (ObjectUtils.getNestedAttributePrimitive(containerRowfield.getPropertyName()).matches(ArPropertyConstants.MilestoneFields.MILESTONE_IDENTIFIER)) {
                                String milestoneId = containerRowfield.getPropertyValue();
                                if (StringUtils.isNotEmpty(milestoneId)) {
                                    if (getMilestoneScheduleMaintenanceService().hasMilestoneBeenCopiedToInvoice(proposalNumber, milestoneId)) {
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

    /**
     * Gets the underlying Milestone Schedule.
     *
     * @return
     */
    public MilestoneSchedule getMilestoneSchedule() {
        return (MilestoneSchedule) getBusinessObject();
    }

    public MilestoneScheduleMaintenanceService getMilestoneScheduleMaintenanceService() {
        if (milestoneScheduleMaintenanceService == null) {
            milestoneScheduleMaintenanceService = SpringContext.getBean(MilestoneScheduleMaintenanceService.class);
        }
        return milestoneScheduleMaintenanceService;
    }
}
