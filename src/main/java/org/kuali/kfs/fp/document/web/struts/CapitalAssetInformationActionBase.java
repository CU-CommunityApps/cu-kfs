/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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
package org.kuali.kfs.fp.document.web.struts;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.fp.FPKeyConstants;
import org.kuali.kfs.fp.businessobject.CapitalAccountingLines;
import org.kuali.kfs.fp.businessobject.CapitalAssetAccountsGroupDetails;
import org.kuali.kfs.fp.businessobject.CapitalAssetInformation;
import org.kuali.kfs.fp.businessobject.CapitalAssetInformationDetail;
import org.kuali.kfs.fp.document.CapitalAccountingLinesDocumentBase;
import org.kuali.kfs.fp.document.CapitalAssetEditable;
import org.kuali.kfs.fp.document.CapitalAssetInformationDocumentBase;
import org.kuali.kfs.integration.cam.CapitalAssetManagementModuleService;
import org.kuali.kfs.integration.cam.businessobject.Asset;
import org.kuali.kfs.kns.util.WebUtils;
import org.kuali.kfs.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.kfs.kns.web.struts.form.KualiForm;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.service.SegmentedLookupResultsService;
import org.kuali.kfs.sys.web.struts.KualiAccountingDocumentActionBase;
import org.kuali.kfs.sys.web.struts.KualiAccountingDocumentFormBase;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cornell.kfs.fp.businessobject.CapitalAssetInformationDetailExtendedAttribute;

public abstract class CapitalAssetInformationActionBase extends KualiAccountingDocumentActionBase {

    private static final Logger LOG = LogManager.getLogger();

    /**
     * Multi-value asset lookup is implemented through the integration package by module's service to gather the
     * results. The results are processed for any capital accounting lines where the line is marked for selection.
     * After the capital assets are populated with the selected asset numbers, the system control amount is
     * redistributed equally among the assets when the distribution method is "distribute cost equally".
     */
    @Override
    public ActionForward refresh(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        super.refresh(mapping, form, request, response);

        //process the multiple value lookup data
        final CapitalAssetInformationFormBase capitalAssetInformationFormBase = (CapitalAssetInformationFormBase) form;

        Collection<PersistableBusinessObject> rawValues = null;
        final Map<String, Set<String>> segmentedSelection = new HashMap<>();

        // If multiple asset lookup was used to select the assets, then....
        if (StringUtils.equals(KFSConstants.MULTIPLE_VALUE, capitalAssetInformationFormBase.getRefreshCaller())) {
            final String lookupResultsSequenceNumber = capitalAssetInformationFormBase.getLookupResultsSequenceNumber();

            if (StringUtils.isNotBlank(lookupResultsSequenceNumber)) {
                // actually returning from a multiple value lookup
                final Set<String> selectedIds = SpringContext.getBean(SegmentedLookupResultsService.class)
                        .retrieveSetOfSelectedObjectIds(lookupResultsSequenceNumber,
                                GlobalVariables.getUserSession().getPerson().getPrincipalId());
                for (final String selectedId : selectedIds) {
                    final String selectedObjId = StringUtils.substringBefore(selectedId, ".");
                    if (!segmentedSelection.containsKey(selectedObjId)) {
                        segmentedSelection.put(selectedObjId, new HashSet<>());
                    }
                }
                // Retrieving selected data from table.
                LOG.debug("Asking segmentation service for object ids {}", segmentedSelection::keySet);
                rawValues = SpringContext.getBean(SegmentedLookupResultsService.class)
                        .retrieveSelectedResultBOs(lookupResultsSequenceNumber, segmentedSelection.keySet(),
                                Asset.class, GlobalVariables.getUserSession().getPerson().getPrincipalId());
            }

            if (rawValues == null || rawValues.size() == 0) {
                //redistribute capital asset amount to its group accounting lines on refresh
                DistributeCapitalAssetAmountToGroupAccountingLines((KualiAccountingDocumentFormBase) form);

                return mapping.findForward(KFSConstants.MAPPING_BASIC);
            }

            final KualiAccountingDocumentFormBase kualiAccountingDocumentFormBase = (KualiAccountingDocumentFormBase) form;
            final CapitalAccountingLinesFormBase calfb = (CapitalAccountingLinesFormBase) form;
            final CapitalAccountingLinesDocumentBase caldb = (CapitalAccountingLinesDocumentBase) calfb.getFinancialDocument();
            final List<CapitalAccountingLines> capitalAccountingLines = caldb.getCapitalAccountingLines();
            final List<CapitalAssetInformation> capitalAssetInformation = getCurrentCapitalAssetInformationObject(kualiAccountingDocumentFormBase);

            final List<CapitalAccountingLines> selectedCapitalAccountingLines = new ArrayList<>();

            for (final CapitalAccountingLines capitalAccountingLine : capitalAccountingLines) {
                if (capitalAccountingLine.isSelectLine() && !capitalAccountingLine.isAmountDistributed()) {
                    selectedCapitalAccountingLines.add(capitalAccountingLine);
                }
            }

            // process the data and create assets only for those accounting lines
            // where capital accounting line is "selected" and its amount is greater than already allocated.
            if (rawValues != null) {
                for (final PersistableBusinessObject bo : rawValues) {
                    final Asset asset = (Asset) bo;
                    final boolean addIt = modifyAssetAlreadyExists(capitalAssetInformation, asset.getCapitalAssetNumber());

                    // If it doesn't already exist in the list add it.
                    if (addIt) {
                        createNewModifyCapitalAsset(selectedCapitalAccountingLines, capitalAssetInformation,
                                calfb.getDocument().getDocumentNumber(),
                                KFSConstants.CapitalAssets.CAPITAL_ASSET_MODIFY_ACTION_INDICATOR,
                                getNextCapitalAssetLineNumber(kualiAccountingDocumentFormBase),
                                asset.getCapitalAssetNumber());
                    }
                }

                checkCapitalAccountingLinesSelected(calfb);

                // remove the blank capital asset modify records now...
                removeEmptyCapitalAssetModify(capitalAssetInformation);

                // now redistribute the amount for all assets if needed....
                redistributeModifyCapitalAssetAmount(mapping, form, request, response);
            }
        }

        // redistribute capital asset amount to its group accounting lines on refresh
        DistributeCapitalAssetAmountToGroupAccountingLines((KualiAccountingDocumentFormBase) form);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Remove if and any blank capital asset modify lines.
     *
     * @param capitalAssetInformation
     */
    protected void removeEmptyCapitalAssetModify(final List<CapitalAssetInformation> capitalAssetInformation) {
        final List<CapitalAssetInformation> removeCapitalAssetModify = new ArrayList<>();

        for (final CapitalAssetInformation capitalAssetRecord : capitalAssetInformation) {
            if (ObjectUtils.isNull(capitalAssetRecord.getCapitalAssetNumber())
                    && KFSConstants.CapitalAssets.CAPITAL_ASSET_MODIFY_ACTION_INDICATOR.equalsIgnoreCase(
                            capitalAssetRecord.getCapitalAssetActionIndicator())) {
                removeCapitalAssetModify.add(capitalAssetRecord);
            }
        }

        if (!removeCapitalAssetModify.isEmpty()) {
            capitalAssetInformation.removeAll(removeCapitalAssetModify);
        }
    }

    /**
     * sums the capital assets amount distributed so far for a given capital accounting line
     *
     * @param currentCapitalAssetInformation
     * @param capitalAccountingLine
     * @return capitalAssetsAmount amount that has been distributed for the specific capital accounting line
     */
    protected KualiDecimal getCapitalAssetsAmountAllocated(
            final List<CapitalAssetInformation> currentCapitalAssetInformation,
            final CapitalAccountingLines capitalAccountingLine) {
        //check the capital assets records totals
        KualiDecimal capitalAssetsAmount = KualiDecimal.ZERO;

        for (final CapitalAssetInformation capitalAsset : currentCapitalAssetInformation) {
            final List<CapitalAssetAccountsGroupDetails> groupAccountLines = capitalAsset.getCapitalAssetAccountsGroupDetails();
            for (final CapitalAssetAccountsGroupDetails groupAccountLine : groupAccountLines) {
                if (groupAccountLine.getCapitalAssetLineNumber().compareTo(capitalAsset.getCapitalAssetLineNumber()) == 0
                        && groupAccountLine.getSequenceNumber().compareTo(capitalAccountingLine.getSequenceNumber()) == 0
                        && groupAccountLine.getFinancialDocumentLineTypeCode().equals(KFSConstants.SOURCE
                                .equals(capitalAccountingLine .getLineType()) ?
                                    KFSConstants.SOURCE_ACCT_LINE_TYPE_CODE : KFSConstants.TARGET_ACCT_LINE_TYPE_CODE)
                        && groupAccountLine.getChartOfAccountsCode().equals(capitalAccountingLine.getChartOfAccountsCode())
                        && groupAccountLine.getAccountNumber().equals(capitalAccountingLine.getAccountNumber())
                        && groupAccountLine.getFinancialObjectCode().equals(capitalAccountingLine.getFinancialObjectCode())) {
                    capitalAssetsAmount = capitalAssetsAmount.add(groupAccountLine.getAmount());
                }
            }
        }

        return capitalAssetsAmount;
    }

    /**
     * checks the capital asset information list for the specific capital asset number
     * that was returned as part of the multi-value lookup.
     *
     * @param capitalAssetInformation
     * @param capitalAssetNumber
     * @return true if asset does not exist in the list else return false
     */
    protected boolean modifyAssetAlreadyExists(final List<CapitalAssetInformation> capitalAssetInformation, final Long capitalAssetNumber) {
        boolean addIt = true;

        for (final CapitalAssetInformation capitalAsset : capitalAssetInformation) {
            if (KFSConstants.CapitalAssets.CAPITAL_ASSET_MODIFY_ACTION_INDICATOR.equals(capitalAsset.getCapitalAssetActionIndicator())
                    && ObjectUtils.isNotNull(capitalAsset.getCapitalAssetNumber())
                    && capitalAsset.getCapitalAssetNumber().compareTo(capitalAssetNumber) == 0) {
                addIt = false;
                break;
            }
        }

        return addIt;
    }

    /**
     * checks if the selected capital accounting lines have a capital asset created by checking the
     * accounts associated with the capital asset information.
     *
     * @param capitalAccountingLines
     * @param capitalAsset
     * @return true if capital accounting line has a capital asset else return false.
     */
    protected boolean capitalAssetExists(
            final List<CapitalAccountingLines> capitalAccountingLines,
            final CapitalAssetInformation capitalAsset, final String actionTypeCode) {
        final List<CapitalAssetAccountsGroupDetails> groupAccountLines = capitalAsset.getCapitalAssetAccountsGroupDetails();

        for (final CapitalAccountingLines capitalAccountLine : capitalAccountingLines) {
            for (final CapitalAssetAccountsGroupDetails groupAccountLine : groupAccountLines) {
                if (groupAccountLine.getCapitalAssetLineNumber().compareTo(capitalAsset.getCapitalAssetLineNumber()) == 0
                        && groupAccountLine.getSequenceNumber().compareTo(capitalAccountLine.getSequenceNumber()) == 0
                        && groupAccountLine.getFinancialDocumentLineTypeCode().equals(KFSConstants.SOURCE
                                .equals(capitalAccountLine.getLineType()) ?
                                    KFSConstants.SOURCE_ACCT_LINE_TYPE_CODE : KFSConstants.TARGET_ACCT_LINE_TYPE_CODE)
                        && groupAccountLine.getChartOfAccountsCode().equals(capitalAccountLine.getChartOfAccountsCode())
                        && groupAccountLine.getAccountNumber().equals(capitalAccountLine.getAccountNumber())
                        && groupAccountLine.getFinancialObjectCode().equals(capitalAccountLine.getFinancialObjectCode())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @param form
     */
    protected void redistributeCostEquallyForModifiedAssets(final ActionForm form) {
        KualiDecimal remainingAmountToDistribute = KualiDecimal.ZERO;

        final CapitalAccountingLinesFormBase calfb = (CapitalAccountingLinesFormBase) form;
        final CapitalAccountingLinesDocumentBase caldb = (CapitalAccountingLinesDocumentBase) calfb.getFinancialDocument();

        final List<CapitalAccountingLines> capitalAccountingLines = caldb.getCapitalAccountingLines();

        final List<CapitalAccountingLines> selectedCapitalAccountingLines = new ArrayList<>();

        for (final CapitalAccountingLines capitalAccountingLine : capitalAccountingLines) {
            if (capitalAccountingLine.isSelectLine() && !capitalAccountingLine.isAmountDistributed()) {
                selectedCapitalAccountingLines.add(capitalAccountingLine);
                remainingAmountToDistribute = remainingAmountToDistribute.add(capitalAccountingLine.getAmount());
            }
        }

        final KualiAccountingDocumentFormBase kualiAccountingDocumentFormBase = (KualiAccountingDocumentFormBase) form;
        final List<CapitalAssetInformation> capitalAssetInformation = getCurrentCapitalAssetInformationObject(
                kualiAccountingDocumentFormBase);
        redistributeAmountsForAccountingsLineForModifyAssets(selectedCapitalAccountingLines, capitalAssetInformation,
                remainingAmountToDistribute);

        //now process any capital assets that has distribution set to "by amount"
        redistributeAmountsForAccountingsLineForModifyAssetsByAmounts(selectedCapitalAccountingLines,
                capitalAssetInformation, remainingAmountToDistribute);
    }

    /**
     * @param selectedCapitalAccountingLines
     * @param capitalAssetInformation
     * @param remainingAmountToDistribute
     */
    protected void redistributeAmountsForAccountingsLineForModifyAssets(
            final List<CapitalAccountingLines> selectedCapitalAccountingLines,
            final List<CapitalAssetInformation> capitalAssetInformation, final KualiDecimal remainingAmountToDistribute) {
        //get the total capital assets quantity
        final int totalQuantity = getNumberOfModifiedAssetsExist(selectedCapitalAccountingLines, capitalAssetInformation);
        if (totalQuantity > 0) {
            final KualiDecimal equalModifyAssetAmount = remainingAmountToDistribute.divide(new KualiDecimal(totalQuantity), true);

            int lastAssetIndex = 0;
            CapitalAssetInformation lastCapitalAsset = new CapitalAssetInformation();

            if (equalModifyAssetAmount.compareTo(KualiDecimal.ZERO) != 0) {
                for (final CapitalAssetInformation capitalAsset : capitalAssetInformation) {
                    if (KFSConstants.CapitalAssets.CAPITAL_ASSET_MODIFY_ACTION_INDICATOR.equals(
                                    capitalAsset.getCapitalAssetActionIndicator())
                        && ObjectUtils.isNotNull(capitalAsset.getCapitalAssetNumber())
                        && KFSConstants.CapitalAssets.DISTRIBUTE_COST_EQUALLY_CODE.equalsIgnoreCase(
                                capitalAsset.getDistributionAmountCode())) {
                        if (capitalAssetExists(selectedCapitalAccountingLines, capitalAsset,
                                KFSConstants.CapitalAssets.CAPITAL_ASSET_MODIFY_ACTION_INDICATOR)) {
                            capitalAsset.setCapitalAssetQuantity(1);
                            redistributeEqualAmounts(selectedCapitalAccountingLines, capitalAsset, equalModifyAssetAmount,
                                    totalQuantity);
                            lastAssetIndex++;
                            //get a reference to the last capital create asset to fix any variances...
                            lastCapitalAsset = capitalAsset;
                        }
                    }
                }
            }

            //apply any variance left to the last
            final KualiDecimal varianceForAssets = remainingAmountToDistribute.subtract(equalModifyAssetAmount.multiply(new KualiDecimal(lastAssetIndex)));
            if (varianceForAssets.isNonZero()) {
                lastCapitalAsset.setCapitalAssetLineAmount(lastCapitalAsset.getCapitalAssetLineAmount().add(varianceForAssets));
                redistributeEqualAmountsOnLastCapitalAsset(selectedCapitalAccountingLines, lastCapitalAsset,
                        capitalAssetInformation, KFSConstants.CapitalAssets.CAPITAL_ASSET_MODIFY_ACTION_INDICATOR);
            }
        }
    }

    /**
     * for modified assets the amount is distributed
     *
     * @param selectedCapitalAccountingLines
     * @param capitalAssetInformation
     */
    protected void redistributeAmountsForAccountingsLineForModifyAssetsByAmounts(
            final List<CapitalAccountingLines> selectedCapitalAccountingLines,
            final List<CapitalAssetInformation> capitalAssetInformation, final KualiDecimal remainingAmountToDistribute) {
        for (final CapitalAccountingLines capitalAccountLine : selectedCapitalAccountingLines) {
            for (final CapitalAssetInformation capitalAsset : capitalAssetInformation) {
                if (KFSConstants.CapitalAssets.DISTRIBUTE_COST_BY_INDIVIDUAL_ASSET_AMOUNT_CODE.equalsIgnoreCase(
                        capitalAsset.getDistributionAmountCode())) {
                    if (capitalAsset.getCapitalAssetLineAmount().compareTo(getAccountingLinesTotalAmount(capitalAsset)) != 0) {
                        final List<CapitalAssetAccountsGroupDetails> groupAccountLines = capitalAsset.getCapitalAssetAccountsGroupDetails();
                        for (final CapitalAssetAccountsGroupDetails groupAccountLine : groupAccountLines) {
                            if (groupAccountLine.getCapitalAssetLineNumber().compareTo(capitalAsset.getCapitalAssetLineNumber()) == 0
                                    && groupAccountLine.getSequenceNumber().compareTo(capitalAccountLine.getSequenceNumber()) == 0
                                    && groupAccountLine.getFinancialDocumentLineTypeCode().equals(KFSConstants.SOURCE
                                            .equals(capitalAccountLine.getLineType()) ?
                                                KFSConstants.SOURCE_ACCT_LINE_TYPE_CODE : KFSConstants.TARGET_ACCT_LINE_TYPE_CODE)
                                    && groupAccountLine.getChartOfAccountsCode().equals(capitalAccountLine.getChartOfAccountsCode())
                                    && groupAccountLine.getAccountNumber().equals(capitalAccountLine.getAccountNumber())
                                    && groupAccountLine.getFinancialObjectCode().equals(capitalAccountLine.getFinancialObjectCode())) {
                                //found the accounting line
                                groupAccountLine.setAmount(capitalAsset.getCapitalAssetLineAmount()
                                        .multiply(capitalAccountLine.getAccountLinePercent())
                                        .divide(new KualiDecimal(100)));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * redistributes the amounts to the capital asset and its related group accounting lines.
     * Adjusts any variance to the last capital asset accounting line.
     *
     * @param selectedCapitalAccountingLines
     * @param capitalAsset
     * @param amount
     * @param totalQuantity
     */
    protected void redistributeEqualAmounts(
            final List<CapitalAccountingLines> selectedCapitalAccountingLines,
            final CapitalAssetInformation capitalAsset, final KualiDecimal amount, final int totalQuantity) {
        int assetQuantity = 0;
        final KualiDecimal totalCapitalAssetQuantity = new KualiDecimal(totalQuantity);

        if (ObjectUtils.isNotNull(capitalAsset.getCapitalAssetQuantity())) {
            assetQuantity = capitalAsset.getCapitalAssetQuantity();
        }

        capitalAsset.setCapitalAssetLineAmount(
                capitalAsset.getCapitalAssetLineAmount().add(amount.multiply(new KualiDecimal(assetQuantity))));
        KualiDecimal appliedAccountingLinesTotal = KualiDecimal.ZERO;

        CapitalAssetAccountsGroupDetails lastLine = new CapitalAssetAccountsGroupDetails();

        final List<CapitalAssetAccountsGroupDetails> groupAccountLines = capitalAsset.getCapitalAssetAccountsGroupDetails();

        for (final CapitalAssetAccountsGroupDetails groupAccountLine : groupAccountLines) {
            final KualiDecimal capitalAccountingLineAmount = getCapitalAssetAccountLineAmount(selectedCapitalAccountingLines,
                    groupAccountLine, capitalAsset);
            final KualiDecimal calculatedLineAmount = capitalAccountingLineAmount.divide(totalCapitalAssetQuantity, true);
            groupAccountLine.setAmount(calculatedLineAmount.multiply(new KualiDecimal(assetQuantity)));
            appliedAccountingLinesTotal = appliedAccountingLinesTotal.add(groupAccountLine.getAmount());

            lastLine = groupAccountLine;
        }

        //apply any variance left to the last
        final KualiDecimal varianceForLines = capitalAsset.getCapitalAssetLineAmount().subtract(appliedAccountingLinesTotal);
        if (varianceForLines.isNonZero()) {
            lastLine.setAmount(lastLine.getAmount().add(varianceForLines));
        }
    }

    /**
     * redistributes the amounts to the capital asset and its related group accounting lines.
     * Adjusts any variance to the last capital asset accounting line.
     *
     * @param selectedCapitalAccountingLines
     * @param lastCapitalAsset
     * @param capitalAssetInformation
     * @param actionTypeCode
     */
    protected void redistributeEqualAmountsOnLastCapitalAsset(
            final List<CapitalAccountingLines> selectedCapitalAccountingLines,
            final CapitalAssetInformation lastCapitalAsset, final List<CapitalAssetInformation> capitalAssetInformation,
            final String actionTypeCode) {
        for (final CapitalAccountingLines capitalAccountingLine : selectedCapitalAccountingLines) {
            final KualiDecimal lineAmount = capitalAccountingLine.getAmount();
            final KualiDecimal distributedAmount = getAccountingLinesDistributedAmount(capitalAccountingLine,
                    capitalAssetInformation, actionTypeCode);
            final KualiDecimal difference = lineAmount.subtract(distributedAmount);
            if (!difference.isZero()) {
                adjustAccountingLineAmountOnLastCapitalAsset(capitalAccountingLine, lastCapitalAsset, difference);
            }
        }
    }

    /**
     * Gets the amount on the capital assets line for the selected capital accounting line by
     * finding the group accounting line.  When group accounting line is found in the selected
     * capital accounting lines, the amount from that capital accounting line is returned.
     *
     * @param selectedCapitalAccountingLines
     * @param groupAccountLine
     * @return lineAmount
     */
    protected KualiDecimal getCapitalAssetAccountLineAmount(
            final List<CapitalAccountingLines> selectedCapitalAccountingLines,
            final CapitalAssetAccountsGroupDetails groupAccountLine, final CapitalAssetInformation capitalAsset) {
        final KualiDecimal lineAmount = KualiDecimal.ZERO;

        for (final CapitalAccountingLines capitalAccountingLine : selectedCapitalAccountingLines) {
            if (groupAccountLine.getCapitalAssetLineNumber().compareTo(capitalAsset.getCapitalAssetLineNumber()) == 0
                    && groupAccountLine.getSequenceNumber().compareTo(capitalAccountingLine.getSequenceNumber()) == 0
                    && groupAccountLine.getFinancialDocumentLineTypeCode().equals(KFSConstants.SOURCE
                            .equals(capitalAccountingLine.getLineType()) ?
                                KFSConstants.SOURCE_ACCT_LINE_TYPE_CODE : KFSConstants.TARGET_ACCT_LINE_TYPE_CODE)
                    && groupAccountLine.getChartOfAccountsCode().equals(capitalAccountingLine.getChartOfAccountsCode())
                    && groupAccountLine.getAccountNumber().equals(capitalAccountingLine.getAccountNumber())
                    && groupAccountLine.getFinancialObjectCode().equals(capitalAccountingLine.getFinancialObjectCode())) {
                return capitalAccountingLine.getAmount();
            }
        }
        return lineAmount;
    }

    /**
     * @param selectedCapitalAccountingLines
     * @param capitalAssetInformation
     */
    protected void redistributeIndividualAmountsForAccountingLinesForModifyAssets(
            final List<CapitalAccountingLines> selectedCapitalAccountingLines,
            final List<CapitalAssetInformation> capitalAssetInformation) {
        for (final CapitalAssetInformation capitalAsset : capitalAssetInformation) {
            if (KFSConstants.CapitalAssets.CAPITAL_ASSET_MODIFY_ACTION_INDICATOR.equals(
                    capitalAsset.getCapitalAssetActionIndicator())) {
                if (capitalAssetExists(selectedCapitalAccountingLines, capitalAsset,
                        KFSConstants.CapitalAssets.CAPITAL_ASSET_MODIFY_ACTION_INDICATOR)) {
                    redistributeIndividualAmounts(selectedCapitalAccountingLines, capitalAsset);
                }
            }
        }
    }

    /**
     * checks the capital accounting line's amount to the sum of the distributed
     * accounting lines amounts and adjusts if there are any variances..
     *
     * @param selectedCapitalAccountingLines
     * @param capitalAssetInformation
     */
    protected void adjustCapitalAssetsAccountingLinesAmounts(
            final List<CapitalAccountingLines> selectedCapitalAccountingLines,
            final List<CapitalAssetInformation> capitalAssetInformation) {
        for (final CapitalAccountingLines capitalAcctLine : selectedCapitalAccountingLines) {
            adjustAccountingLinesAmounts(capitalAcctLine, capitalAssetInformation);
        }
    }

    /**
     * for each capital account line, compares its amounts to the accounting lines
     * on capital assets and adjusts its accounting lines amounts for any variances.
     *
     * @param capitalAcctLine
     * @param capitalAssetInformation
     */
    protected void adjustAccountingLinesAmounts(
            final CapitalAccountingLines capitalAcctLine,
            final List<CapitalAssetInformation> capitalAssetInformation) {
        CapitalAssetAccountsGroupDetails lastAcctLine = null;

        KualiDecimal totalAccountsAmount = KualiDecimal.ZERO;

        for (final CapitalAssetInformation capitalAsset : capitalAssetInformation) {
            if (capitalAsset.getCapitalAssetLineAmount().compareTo(getAccountingLinesTotalAmount(capitalAsset)) != 0) {
                final List<CapitalAssetAccountsGroupDetails> groupAccountLines = capitalAsset.getCapitalAssetAccountsGroupDetails();
                for (final CapitalAssetAccountsGroupDetails groupAccountLine : groupAccountLines) {
                    if (groupAccountLine.getCapitalAssetLineNumber().compareTo(capitalAsset.getCapitalAssetLineNumber()) == 0
                            && groupAccountLine.getSequenceNumber().compareTo(capitalAcctLine.getSequenceNumber()) == 0
                            && groupAccountLine.getFinancialDocumentLineTypeCode().equals(KFSConstants.SOURCE
                                    .equals(capitalAcctLine.getLineType()) ?
                                        KFSConstants.SOURCE_ACCT_LINE_TYPE_CODE : KFSConstants.TARGET_ACCT_LINE_TYPE_CODE)
                            && groupAccountLine.getChartOfAccountsCode().equals(capitalAcctLine.getChartOfAccountsCode())
                            && groupAccountLine.getAccountNumber().equals(capitalAcctLine.getAccountNumber())
                            && groupAccountLine.getFinancialObjectCode().equals(capitalAcctLine.getFinancialObjectCode())) {
                        totalAccountsAmount = totalAccountsAmount.add(groupAccountLine.getAmount());
                        lastAcctLine = groupAccountLine;
                    }
                }
            }
        }

        final KualiDecimal variance = capitalAcctLine.getAmount().subtract(totalAccountsAmount);
        if (variance.isNonZero() && ObjectUtils.isNotNull(lastAcctLine)) {
            lastAcctLine.setAmount(lastAcctLine.getAmount().add(variance));
        }
    }

    /**
     * adjusts variances on capital assets where distribution method is set
     * as "distribute evenly" and capital asset amount is odd value.  Reduce the
     * capital asset amount line by 0.01 and then adjust the account amounts. Finally
     * any variance between capital accounting lines and capital assets is added
     * to the last capital asset and its accounting lines.
     *
     * @param capitalAssetInformation
     */
    protected void adjustVarianceOnCapitalAssets(final List<CapitalAssetInformation> capitalAssetInformation) {
        KualiDecimal adjustedCapitalAssetBalance = KualiDecimal.ZERO;
        CapitalAssetInformation lastCapitalAsset = null;

        for (final CapitalAssetInformation capitalAsset : capitalAssetInformation) {
            //look at only cost equal assets...
            if (KFSConstants.CapitalAssets.DISTRIBUTE_COST_EQUALLY_CODE.equalsIgnoreCase(capitalAsset.getDistributionAmountCode())) {
                if (capitalAsset.getCapitalAssetLineAmount().mod(new KualiDecimal(2)) != KualiDecimal.ZERO) {
                    capitalAsset.setCapitalAssetLineAmount(capitalAsset.getCapitalAssetLineAmount().subtract(new KualiDecimal(0.01)));
                    adjustedCapitalAssetBalance = adjustedCapitalAssetBalance.add(new KualiDecimal(0.01));
                    lastCapitalAsset = capitalAsset;
                }
            }
        }

        if (ObjectUtils.isNotNull(lastCapitalAsset) && adjustedCapitalAssetBalance.isNonZero()) {
            lastCapitalAsset.setCapitalAssetLineAmount(lastCapitalAsset.getCapitalAssetLineAmount().add(adjustedCapitalAssetBalance));
        }
    }

    /**
     * @param selectedCapitalAccountingLines
     * @param capitalAssetInformation
     */
    protected void redistributeIndividualAmountsForAccountingLinesForCreateAssets(
            final List<CapitalAccountingLines> selectedCapitalAccountingLines,
            final List<CapitalAssetInformation> capitalAssetInformation) {
        for (final CapitalAssetInformation capitalAsset : capitalAssetInformation) {
            if (KFSConstants.CapitalAssets.CAPITAL_ASSET_CREATE_ACTION_INDICATOR.equals(
                    capitalAsset.getCapitalAssetActionIndicator())) {
                if (capitalAssetExists(selectedCapitalAccountingLines, capitalAsset,
                        KFSConstants.CapitalAssets.CAPITAL_ASSET_CREATE_ACTION_INDICATOR)) {
                    redistributeIndividualAmounts(selectedCapitalAccountingLines, capitalAsset);
                }
            }
        }
    }

    /**
     * @param selectedCapitalAccountingLines
     * @param capitalAsset
     */
    protected void redistributeIndividualAmounts(
            final List<CapitalAccountingLines> selectedCapitalAccountingLines,
            final CapitalAssetInformation capitalAsset) {
        final KualiDecimal capitalAssetAmount = capitalAsset.getCapitalAssetLineAmount();

        final KualiDecimal totalCapitalAccountsAmount = getTotalCapitalAccountsAmounts(selectedCapitalAccountingLines);

        CapitalAssetAccountsGroupDetails lastAccountLine = new CapitalAssetAccountsGroupDetails();
        KualiDecimal distributedAmount = KualiDecimal.ZERO;

        final List<CapitalAssetAccountsGroupDetails> groupAccountLines = capitalAsset.getCapitalAssetAccountsGroupDetails();
        for (final CapitalAssetAccountsGroupDetails groupAccountLine : groupAccountLines) {
            final BigDecimal linePercent = getCapitalAccountingLinePercent(selectedCapitalAccountingLines, groupAccountLine,
                    totalCapitalAccountsAmount);
            groupAccountLine.setAmount(new KualiDecimal(capitalAssetAmount.bigDecimalValue().multiply(linePercent)));
            lastAccountLine = groupAccountLine;
            distributedAmount = distributedAmount.add(groupAccountLine.getAmount());
        }

        lastAccountLine.setAmount(lastAccountLine.getAmount().add(capitalAssetAmount.subtract(distributedAmount)));
    }

    /**
     * @param selectedCapitalAccountingLines
     * @param groupAccountLine
     * @param totalCapitalAccountsAmount
     * @return percent
     */
    protected BigDecimal getCapitalAccountingLinePercent(
            final List<CapitalAccountingLines> selectedCapitalAccountingLines,
            final CapitalAssetAccountsGroupDetails groupAccountLine, final KualiDecimal totalCapitalAccountsAmount) {
        for (final CapitalAccountingLines capitalAccountingLine : selectedCapitalAccountingLines) {
            if (groupAccountLine.getSequenceNumber().compareTo(capitalAccountingLine.getSequenceNumber()) == 0
                    && groupAccountLine.getFinancialDocumentLineTypeCode().equals(KFSConstants.SOURCE
                            .equals(capitalAccountingLine.getLineType()) ?
                                KFSConstants.SOURCE_ACCT_LINE_TYPE_CODE : KFSConstants.TARGET_ACCT_LINE_TYPE_CODE)
                    && groupAccountLine.getChartOfAccountsCode().equals(capitalAccountingLine.getChartOfAccountsCode())
                    && groupAccountLine.getAccountNumber().equals(capitalAccountingLine.getAccountNumber())
                    && groupAccountLine.getFinancialObjectCode()
                            .equals(capitalAccountingLine.getFinancialObjectCode())) {
                return capitalAccountingLine.getAmount().bigDecimalValue()
                            .divide(totalCapitalAccountsAmount.bigDecimalValue(),
                        KFSConstants.CapitalAssets.CAPITAL_ACCOUNT_LINE_PERCENT_SCALE,
                                KFSConstants.CapitalAssets.PERCENT_SCALE);
            }
        }

        return BigDecimal.ZERO;
    }

    /**
     * @param selectedCapitalAccountingLines
     * @param capitalAssetInformation
     * @param remainingAmountToDistribute
     */
    protected void redistributeEqualAmountsForAccountingLineForCreateAssets(
            final List<CapitalAccountingLines> selectedCapitalAccountingLines,
            final List<CapitalAssetInformation> capitalAssetInformation, final KualiDecimal remainingAmountToDistribute) {
        //get the total capital assets quantity
        final int totalQuantity = getTotalQuantityOfCreateAssets(selectedCapitalAccountingLines, capitalAssetInformation);
        if (totalQuantity > 0) {
            final KualiDecimal equalCreateAssetAmount = remainingAmountToDistribute.divide(new KualiDecimal(totalQuantity), true);

            int lastAssetIndex = 0;
            CapitalAssetInformation lastCapitalAsset = new CapitalAssetInformation();

            if (equalCreateAssetAmount.compareTo(KualiDecimal.ZERO) != 0) {
                for (final CapitalAssetInformation capitalAsset : capitalAssetInformation) {
                    if (KFSConstants.CapitalAssets.CAPITAL_ASSET_CREATE_ACTION_INDICATOR
                                .equals(capitalAsset.getCapitalAssetActionIndicator())
                            && KFSConstants.CapitalAssets.DISTRIBUTE_COST_EQUALLY_CODE
                                .equalsIgnoreCase(capitalAsset.getDistributionAmountCode())
                            && capitalAssetExists(selectedCapitalAccountingLines, capitalAsset,
                                KFSConstants.CapitalAssets.CAPITAL_ASSET_CREATE_ACTION_INDICATOR)) {
                        redistributeEqualAmounts(selectedCapitalAccountingLines, capitalAsset, equalCreateAssetAmount,
                                totalQuantity);
                        if (ObjectUtils.isNotNull(capitalAsset.getCapitalAssetQuantity())) {
                            lastAssetIndex = lastAssetIndex + capitalAsset.getCapitalAssetQuantity();
                        }
                        //get a reference to the last capital create asset to fix any variances...
                        lastCapitalAsset = capitalAsset;
                    }
                }
            }

            //apply any variance left to the last
            final KualiDecimal varianceForAssets = remainingAmountToDistribute.subtract(equalCreateAssetAmount.multiply(new KualiDecimal(lastAssetIndex)));
            if (varianceForAssets.isNonZero()) {
                lastCapitalAsset.setCapitalAssetLineAmount(lastCapitalAsset.getCapitalAssetLineAmount().add(varianceForAssets));
                redistributeEqualAmountsOnLastCapitalAsset(selectedCapitalAccountingLines, lastCapitalAsset,
                        capitalAssetInformation, KFSConstants.CapitalAssets.CAPITAL_ASSET_CREATE_ACTION_INDICATOR);
            }
        }
    }

    /**
     * @param selectedCapitalAccountingLines
     * @param capitalAssetInformation
     * @return createAssetsCount count of create assets
     */
    protected int getTotalQuantityOfCreateAssets(
            final List<CapitalAccountingLines> selectedCapitalAccountingLines,
            final List<CapitalAssetInformation> capitalAssetInformation) {
        int totalQuantity = 0;

        for (final CapitalAssetInformation capitalAsset : capitalAssetInformation) {
            if (KFSConstants.CapitalAssets.CAPITAL_ASSET_CREATE_ACTION_INDICATOR.equals(capitalAsset.getCapitalAssetActionIndicator())) {
                if (capitalAssetExists(selectedCapitalAccountingLines, capitalAsset,
                        KFSConstants.CapitalAssets.CAPITAL_ASSET_CREATE_ACTION_INDICATOR)) {
                    if (ObjectUtils.isNotNull(capitalAsset.getCapitalAssetQuantity())) {
                        totalQuantity += capitalAsset.getCapitalAssetQuantity();
                    }
                }
            }
        }

        return totalQuantity;
    }

    /**
     * @param selectedCapitalAccountingLines
     * @param capitalAssetInformation
     * @return createAssetsCount count of create assets
     */
    protected int numberOfCreateAssetsExist(
            final List<CapitalAccountingLines> selectedCapitalAccountingLines,
            final List<CapitalAssetInformation> capitalAssetInformation) {
        int createAssetsCount = 0;

        for (final CapitalAssetInformation capitalAsset : capitalAssetInformation) {
            if (KFSConstants.CapitalAssets.CAPITAL_ASSET_CREATE_ACTION_INDICATOR.equals(capitalAsset.getCapitalAssetActionIndicator())) {
                if (capitalAssetExists(selectedCapitalAccountingLines, capitalAsset,
                        KFSConstants.CapitalAssets.CAPITAL_ASSET_CREATE_ACTION_INDICATOR)) {
                    createAssetsCount++;
                }
            }
        }

        if (createAssetsCount == 0) {
            return 1;
        }

        return createAssetsCount;
    }

    /**
     * @param selectedCapitalAccountingLines
     * @param capitalAssetInformation
     * @return modifiedAssetsCount number of modified assets
     */
    protected int getNumberOfModifiedAssetsExist(
            final List<CapitalAccountingLines> selectedCapitalAccountingLines,
            final List<CapitalAssetInformation> capitalAssetInformation) {
        int modifiedAssetsCount = 0;

        for (final CapitalAssetInformation capitalAsset : capitalAssetInformation) {
            if (KFSConstants.CapitalAssets.CAPITAL_ASSET_MODIFY_ACTION_INDICATOR.equals(capitalAsset.getCapitalAssetActionIndicator())
                    && ObjectUtils.isNotNull(capitalAsset.getCapitalAssetNumber())) {
                if (capitalAssetExists(selectedCapitalAccountingLines, capitalAsset,
                        KFSConstants.CapitalAssets.CAPITAL_ASSET_MODIFY_ACTION_INDICATOR)) {
                    modifiedAssetsCount++;
                }
            }
        }

        if (modifiedAssetsCount == 0) {
            return 1;
        }

        return modifiedAssetsCount;
    }

    /**
     * Clear the capital asset information that the user has entered
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return action forward string
     * @throws Exception
     */
    public ActionForward clearCapitalAssetInfo(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        LOG.debug("clearCapitalAssetInfo() - start");

        final KualiAccountingDocumentFormBase kualiAccountingDocumentFormBase = (KualiAccountingDocumentFormBase) form;
        final List<CapitalAssetInformation> capitalAssetInformation = getCurrentCapitalAssetInformationObject(kualiAccountingDocumentFormBase);

        if (capitalAssetInformation == null) {
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }

        final int clearIndex = getSelectedLine(request);
        resetCapitalAssetInfo(capitalAssetInformation.get(clearIndex));

        //now process the remaining capital asset records
        processRemainingCapitalAssetInfo(form, capitalAssetInformation);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * clear up the modify capital asset information.  The amount field is reset to 0
     * Processes any remaining capital assets so that it recalculates the system control
     * and system control remaining amounts.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return action forward string
     * @throws Exception
     */
    public ActionForward clearCapitalAssetModify(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        LOG.debug("clearCapitalAssetModify() - start");

        final KualiAccountingDocumentFormBase kualiAccountingDocumentFormBase = (KualiAccountingDocumentFormBase) form;
        final List<CapitalAssetInformation> capitalAssetInformation = getCurrentCapitalAssetInformationObject(kualiAccountingDocumentFormBase);

        if (capitalAssetInformation == null) {
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }

        final int clearIndex = getSelectedLine(request);
        capitalAssetInformation.get(clearIndex).setCapitalAssetLineAmount(KualiDecimal.ZERO);

        //zero out the amount distribute on the accounting lines...
        for (final CapitalAssetAccountsGroupDetails groupAccountLine : capitalAssetInformation.get(clearIndex).getCapitalAssetAccountsGroupDetails()) {
            groupAccountLine.setAmount(KualiDecimal.ZERO);
        }

        //now process the remaining capital asset records
        processRemainingCapitalAssetInfo(form, capitalAssetInformation);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * inserts capital asset information into capital assets list.
     * Also recalculates the system control and system control remaining amounts.
     * Puts a global error message if the user does not enter capital asset quantity.
     * If the quantity is > 1, it will insert that many tag/location detail records for this
     * capital asset item.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return action forward string
     * @throws Exception
     */
    public ActionForward insertCapitalAssetInfo(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        LOG.debug("insertCapitalAssetInfo() - start");

        final KualiAccountingDocumentFormBase kualiAccountingDocumentFormBase = (KualiAccountingDocumentFormBase) form;
        final List<CapitalAssetInformation> capitalAssetInformation = getCurrentCapitalAssetInformationObject(kualiAccountingDocumentFormBase);

        if (capitalAssetInformation == null) {
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }

        final int addIndex = getSelectedLine(request);
        if (capitalAssetInformation.get(addIndex).getCapitalAssetQuantity() == null
                || capitalAssetInformation.get(addIndex).getCapitalAssetQuantity() <= 0) {
            GlobalVariables.getMessageMap().putError(KFSConstants.EDIT_CAPITAL_ASSET_INFORMATION_ERRORS,
                    FPKeyConstants.ERROR_DOCUMENT_CAPITAL_ASSET_QUANTITY_REQUIRED);
        } else {
            addCapitalAssetInfoDetailLines(capitalAssetInformation.get(addIndex));
        }

        //now process the remaining capital asset records
        processRemainingCapitalAssetInfo(form, capitalAssetInformation);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * inserts capital asset information into capital assets list.
     * Also recalculates the system control and system control remaining amounts.
     * Puts a global error message if the user does not enter capital asset quantity.
     * If the quantity is > 1, it will insert that many tag/location detail records for this
     * capital asset item.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return action forward string
     * @throws Exception
     */
    public ActionForward addCapitalAssetTagLocationInfo(
            final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        LOG.debug("addCapitalAssetTagLocationInfo() - start");

        final KualiAccountingDocumentFormBase kualiAccountingDocumentFormBase = (KualiAccountingDocumentFormBase) form;
        final List<CapitalAssetInformation> capitalAssetInformation = getCurrentCapitalAssetInformationObject(kualiAccountingDocumentFormBase);

        if (capitalAssetInformation == null) {
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }

        final int addIndex = getSelectedLine(request);
        if (capitalAssetInformation.get(addIndex).getCapitalAssetQuantity() == null
                || capitalAssetInformation.get(addIndex).getCapitalAssetQuantity() <= 0) {
            GlobalVariables.getMessageMap().putError(KFSConstants.EDIT_CAPITAL_ASSET_INFORMATION_ERRORS,
                    FPKeyConstants.ERROR_DOCUMENT_CAPITAL_ASSET_QUANTITY_REQUIRED);
        } else {
            addCapitalAssetInfoDetailLines(capitalAssetInformation.get(addIndex));
        }

        //now process the remaining capital asset records
        processRemainingCapitalAssetInfo(form, capitalAssetInformation);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * refreshes capital asset modify information to the modify capital assets list.
     * Also recalculates the system control and system control remaining amounts.
     * Puts a global error message if the user does not enter capital asset number.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return action forward string
     * @throws Exception
     */
    public ActionForward refreshCapitalAssetModify(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        LOG.debug("refreshCapitalAssetModify() - start");

        final KualiAccountingDocumentFormBase kualiAccountingDocumentFormBase = (KualiAccountingDocumentFormBase) form;
        final List<CapitalAssetInformation> capitalAssetInformation = getCurrentCapitalAssetInformationObject(kualiAccountingDocumentFormBase);

        if (capitalAssetInformation == null) {
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }

        final int refreshIndex = getSelectedLine(request);

        if (capitalAssetInformation.get(refreshIndex).getCapitalAssetNumber() == null) {
            GlobalVariables.getMessageMap().putError(KFSConstants.EDIT_CAPITAL_ASSET_MODIFY_ERRORS,
                    FPKeyConstants.ERROR_DOCUMENT_CAPITAL_ASSET_NUMBER_REQUIRED);
        }

        //now process the remaining capital asset records
        processRemainingCapitalAssetInfo(form, capitalAssetInformation);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * deletes the capital asset information
     */
    public ActionForward deleteCapitalAssetInfo(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        LOG.debug("deleteCapitalAssetInfoDetail() - start");

        final KualiAccountingDocumentFormBase kualiAccountingDocumentFormBase = (KualiAccountingDocumentFormBase) form;
        final List<CapitalAssetInformation> capitalAssetInformation = getCurrentCapitalAssetInformationObject(kualiAccountingDocumentFormBase);

        if (capitalAssetInformation == null) {
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }

        final int lineIndexForCapitalAssetInfo = getLineToDelete(request);

        if (capitalAssetInformation.get(lineIndexForCapitalAssetInfo).getCapitalAssetInformationDetails() != null &&
            capitalAssetInformation.get(lineIndexForCapitalAssetInfo).getCapitalAssetInformationDetails().size() > 0) {
            capitalAssetInformation.get(lineIndexForCapitalAssetInfo).getCapitalAssetInformationDetails().clear();

        }

        if (capitalAssetInformation.get(lineIndexForCapitalAssetInfo).getCapitalAssetAccountsGroupDetails() != null &&
            capitalAssetInformation.get(lineIndexForCapitalAssetInfo).getCapitalAssetAccountsGroupDetails().size() > 0) {
            capitalAssetInformation.get(lineIndexForCapitalAssetInfo).getCapitalAssetAccountsGroupDetails().clear();
        }

        capitalAssetInformation.remove(lineIndexForCapitalAssetInfo);

        //now process the remaining capital asset records
        processRemainingCapitalAssetInfo(form, capitalAssetInformation);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * deletes the capital asset information
     */
    public ActionForward deleteCapitalAssetModify(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        LOG.debug("deleteCapitalAssetModify() - start");

        final KualiAccountingDocumentFormBase kualiAccountingDocumentFormBase = (KualiAccountingDocumentFormBase) form;
        final List<CapitalAssetInformation> capitalAssetInformation = getCurrentCapitalAssetInformationObject(kualiAccountingDocumentFormBase);

        if (capitalAssetInformation == null) {
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }

        final int lineIndexForCapitalAssetInfo = getLineToDelete(request);

        capitalAssetInformation.remove(lineIndexForCapitalAssetInfo);

        //now process the remaining capital asset records
        processRemainingCapitalAssetInfo(form, capitalAssetInformation);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * process any remaining capital asset info in the list to check and calculate the
     * remaining distributed amount.  Also checks to make sure if "select Lines" is to be
     * checked on/off
     *
     * @param form
     * @param capitalAssetInformation
     */
    protected void processRemainingCapitalAssetInfo(final ActionForm form, final List<CapitalAssetInformation> capitalAssetInformation) {
        final CapitalAccountingLinesFormBase calfb = (CapitalAccountingLinesFormBase) form;

        //recalculate the amount remaining to be distributed and save the value on the form
        calculateRemainingDistributedAmount(calfb, capitalAssetInformation);

        //set the amountDistributed property to true if the total amount of all the capital assets
        //for a given capital accounting line is greater or equal to the line amount.
        checkCapitalAccountingLinesSelected(calfb);

        //redistribute each capital asset amount to its group accounting lines...
        final KualiAccountingDocumentFormBase kualiAccountingDocumentFormBase = (KualiAccountingDocumentFormBase) form;
        DistributeCapitalAssetAmountToGroupAccountingLines(kualiAccountingDocumentFormBase);

        setTabStatesForCapitalAssets(form);
    }

    /**
     * delete a detail line from the capital asset information
     */
    public ActionForward deleteCapitalAssetInfoDetailLine(
            final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        LOG.debug("deleteCapitalAssetInfoDetailLine() - start");

        final KualiAccountingDocumentFormBase kualiAccountingDocumentFormBase = (KualiAccountingDocumentFormBase) form;
        final List<CapitalAssetInformation> capitalAssetInformation = getCurrentCapitalAssetInformationObject(kualiAccountingDocumentFormBase);

        if (capitalAssetInformation == null) {
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }

        final int lineIndexForCapitalAssetInfo = getLineToDelete(request);
        capitalAssetInformation.get(0).getCapitalAssetInformationDetails().remove(lineIndexForCapitalAssetInfo - 1);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * get the capital asset information object currently associated with the document
     */
    protected List<CapitalAssetInformation> getCurrentCapitalAssetInformationObject(
            final KualiAccountingDocumentFormBase kualiAccountingDocumentFormBase) {
        LOG.debug("getCurrentCapitalAssetInformationObject() - start");

        final CapitalAssetInformationDocumentBase capitalAssetInformationDocumentBase = (CapitalAssetInformationDocumentBase) kualiAccountingDocumentFormBase
                .getFinancialDocument();

        if (capitalAssetInformationDocumentBase == null) {
            return null;
        }
        return capitalAssetInformationDocumentBase.getCapitalAssetInformation();
    }

    /**
     * reset the nonkey fields of the given capital asset information
     * removes the corresponding capital asset information detail record from the list.
     *
     * @param capitalAssetInformation the given capital asset information
     */
    protected void resetCapitalAssetInfo(final CapitalAssetInformation capitalAssetInformation) {
        if (capitalAssetInformation != null) {
            capitalAssetInformation.setCapitalAssetDescription(null);
            capitalAssetInformation.setCapitalAssetManufacturerModelNumber(null);
            capitalAssetInformation.setCapitalAssetManufacturerName(null);

            capitalAssetInformation.setCapitalAssetNumber(null);
            capitalAssetInformation.setCapitalAssetTypeCode(null);
            capitalAssetInformation.setCapitalAssetQuantity(null);

            capitalAssetInformation.setVendorDetailAssignedIdentifier(null);
            capitalAssetInformation.setVendorHeaderGeneratedIdentifier(null);
            // Set the BO to null cause it won't be updated automatically when vendorDetailAssetIdentifier and
            // VendorHeanderGeneratedIdentifier set to null.
            capitalAssetInformation.setVendorDetail(null);
            capitalAssetInformation.setVendorName(null);
            capitalAssetInformation.setCapitalAssetLineAmount(KualiDecimal.ZERO);
            capitalAssetInformation.getCapitalAssetInformationDetails().clear();

            //zero out the amount distribute on the accounting lines...
            for (final CapitalAssetAccountsGroupDetails groupAccountLine : capitalAssetInformation.getCapitalAssetAccountsGroupDetails()) {
                groupAccountLine.setAmount(KualiDecimal.ZERO);
            }
        }
    }

    /**
     * Overridden to guarantee that form of copied document is set to whatever the entry mode of the document is
     */
    @Override
    public ActionForward copy(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final CapitalAccountingLinesFormBase capitalAccountingLinesFormBase = (CapitalAccountingLinesFormBase) form;
        final CapitalAccountingLinesDocumentBase caldb = (CapitalAccountingLinesDocumentBase) capitalAccountingLinesFormBase
                .getFinancialDocument();

        final List<CapitalAccountingLines> capitalAccountingLines = caldb.getCapitalAccountingLines();
        final List<CapitalAccountingLines> copiedCapitalAccountingLines = new ArrayList<>(capitalAccountingLines);
        capitalAccountingLines.clear();

        final ActionForward forward = super.copy(mapping, form, request, response);

        caldb.setCapitalAccountingLines(copiedCapitalAccountingLines);

        // if the copied document has capital asset collection, remove the collection
        final KualiAccountingDocumentFormBase kualiAccountingDocumentFormBase = (KualiAccountingDocumentFormBase) form;
        final AccountingDocument document = kualiAccountingDocumentFormBase.getFinancialDocument();
        if (document instanceof CapitalAssetEditable) {
            final CapitalAssetEditable capitalAssetEditable = (CapitalAssetEditable) document;

            final List<CapitalAssetInformation> capitalAssets = capitalAssetEditable.getCapitalAssetInformation();
            for (final CapitalAssetInformation capitalAsset : capitalAssets) {
                final Long capitalAssetNumber = capitalAsset.getCapitalAssetNumber();
                resetCapitalAssetInfo(capitalAsset);

                //set capital asset number to copied asset line if "modify" asset
                //because resetCapitalAssetInfo cleared the value.
                if (KFSConstants.CapitalAssets.CAPITAL_ASSET_MODIFY_ACTION_INDICATOR.equalsIgnoreCase(
                        capitalAsset.getCapitalAssetActionIndicator())) {
                    capitalAsset.setCapitalAssetNumber(capitalAssetNumber);
                }
                capitalAsset.setCapitalAssetProcessedIndicator(false);
            }
        }

        //setup the initial next sequence number column..
        final KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        setupIntialNextCapitalAssetLineNumber(kualiDocumentFormBase);

        checkCapitalAccountingLinesSelected(capitalAccountingLinesFormBase);

        return forward;
    }

    /**
     * setups the next capital asset line number
     *
     * @param kualiDocumentFormBase
     */
    protected void setupIntialNextCapitalAssetLineNumber(final KualiDocumentFormBase kualiDocumentFormBase) {
        final KualiAccountingDocumentFormBase kadfb = (KualiAccountingDocumentFormBase) kualiDocumentFormBase;
        final CapitalAssetInformationDocumentBase caidb = (CapitalAssetInformationDocumentBase) kadfb.getFinancialDocument();

        final List<CapitalAssetInformation> currentCapitalAssetInformation = getCurrentCapitalAssetInformationObject(kadfb);
        for (final CapitalAssetInformation capitalAsset : currentCapitalAssetInformation) {
            if (capitalAsset.getCapitalAssetLineNumber() > caidb.getNextCapitalAssetLineNumber()) {
                caidb.setNextCapitalAssetLineNumber(capitalAsset.getCapitalAssetLineNumber());
            }
        }

        caidb.setNextCapitalAssetLineNumber(caidb.getNextCapitalAssetLineNumber() + 1);
    }

    /**
     * calculates the percents for the selected capital accounting lines only
     *
     * @param calfb
     */
    protected void calculatePercentsForSelectedCapitalAccountingLines(final CapitalAccountingLinesFormBase calfb) {
        final CapitalAccountingLinesDocumentBase caldb = (CapitalAccountingLinesDocumentBase) calfb.getFinancialDocument();
        final List<CapitalAccountingLines> capitalAccountingLines = caldb.getCapitalAccountingLines();

        final KualiDecimal totalCapitalLinesSelectedAmount = calculateTotalCapitalLinesSelectedAmount(capitalAccountingLines)
                .abs();
        if (totalCapitalLinesSelectedAmount.isNonZero()) {
            for (final CapitalAccountingLines capitalAccountingLine : capitalAccountingLines) {
                if (capitalAccountingLine.isSelectLine() && !capitalAccountingLine.isAmountDistributed()) {
                    capitalAccountingLine.setAccountLinePercent(
                            capitalAccountingLine.getAmount().abs().divide(totalCapitalLinesSelectedAmount)
                                    .multiply(new KualiDecimal(100), true));
                }
            }
        }
    }

    /**
     * @param capitalAccountingLines
     * @return
     */
    protected KualiDecimal calculateTotalCapitalLinesSelectedAmount(final List<CapitalAccountingLines> capitalAccountingLines) {
        KualiDecimal totalLineAmount = KualiDecimal.ZERO;

        for (final CapitalAccountingLines capitalAccountingLine : capitalAccountingLines) {
            if (capitalAccountingLine.isSelectLine() && !capitalAccountingLine.isAmountDistributed()) {
                totalLineAmount = totalLineAmount.add(capitalAccountingLine.getAmount());
            }
        }

        return totalLineAmount;
    }

    /**
     * Populates capital asset information collection with capital accounting lines.
     * Based on actionType, capitalassetactionindicator attribute is filled with 'C' for create
     * and 'M' for modify assets, which will be used to differentiate to pull the records in
     * create asset screen or modify asset screen.
     *
     * @param calfb
     * @param actionType
     * @param distributionAmountCode
     */
    protected void createCapitalAssetForGroupAccountingLines(
            final CapitalAccountingLinesFormBase calfb, final String actionType,
            final String distributionAmountCode) {
        final CapitalAccountingLinesDocumentBase caldb = (CapitalAccountingLinesDocumentBase) calfb.getFinancialDocument();

        final List<CapitalAccountingLines> capitalAccountingLines = caldb.getCapitalAccountingLines();
        final List<CapitalAssetInformation> currentCapitalAssetInformation = getCurrentCapitalAssetInformationObject(
                calfb);

        final String documentNumber = calfb.getDocument().getDocumentNumber();
        calfb.setSystemControlAmount(KualiDecimal.ZERO);

        final List<CapitalAccountingLines> selectedCapitalAccountingLines = new ArrayList<>();

        for (final CapitalAccountingLines capitalAccountingLine : capitalAccountingLines) {
            if (capitalAccountingLine.isSelectLine() && !capitalAccountingLine.isAmountDistributed()) {
                capitalAccountingLine.setDistributionAmountCode(distributionAmountCode);
                selectedCapitalAccountingLines.add(capitalAccountingLine);
            }
        }

        final CapitalAssetInformation existingCapitalAsset = capitalAssetCreated(selectedCapitalAccountingLines,
                currentCapitalAssetInformation);
        if (ObjectUtils.isNotNull(existingCapitalAsset)) {
            if (!accountingLinesAmountDistributed(selectedCapitalAccountingLines, existingCapitalAsset)) {
                //accounting line amount not completely distributed yet so we need to create more assets
                //add the capital information record to the list of asset information
                createNewCapitalAsset(selectedCapitalAccountingLines, currentCapitalAssetInformation, documentNumber,
                        actionType, getNextCapitalAssetLineNumber(calfb));
            }
        } else {
            //add the capital information record to the list of asset information
            createNewCapitalAsset(selectedCapitalAccountingLines, currentCapitalAssetInformation, documentNumber,
                    actionType, getNextCapitalAssetLineNumber(calfb));
        }
    }

    /**
     * helper method to add accounting details for this new capital asset record
     *
     * @param capitalAccountingLines
     * @param currentCapitalAssetInformation
     * @param documentNumber
     * @param actionType
     * @param nextCapitalAssetLineNumber
     */
    protected void createNewCapitalAsset(
            final List<CapitalAccountingLines> capitalAccountingLines,
            final List<CapitalAssetInformation> currentCapitalAssetInformation, final String documentNumber, final String actionType,
            final Integer nextCapitalAssetLineNumber) {
        final CapitalAssetInformation capitalAsset = new CapitalAssetInformation();
        capitalAsset.setCapitalAssetLineAmount(KualiDecimal.ZERO);
        capitalAsset.setDocumentNumber(documentNumber);
        capitalAsset.setCapitalAssetLineNumber(nextCapitalAssetLineNumber);
        capitalAsset.setCapitalAssetActionIndicator(actionType);
        capitalAsset.setCapitalAssetProcessedIndicator(false);

        //now setup the account line information associated with this capital asset
        for (final CapitalAccountingLines capitalAccountingLine : capitalAccountingLines) {
            capitalAsset.setDistributionAmountCode(capitalAccountingLine.getDistributionAmountCode());
            createCapitalAssetAccountingLinesDetails(capitalAccountingLine, capitalAsset);
        }

        currentCapitalAssetInformation.add(capitalAsset);
    }

    /**
     * helper method to add accounting details for this new modify capital asset record
     *
     * @param capitalAccountingLines
     * @param currentCapitalAssetInformation
     * @param documentNumber
     * @param actionType
     * @param nextCapitalAssetLineNumber
     * @param capitalAssetNumber
     */
    protected void createNewModifyCapitalAsset(
            final List<CapitalAccountingLines> capitalAccountingLines,
            final List<CapitalAssetInformation> currentCapitalAssetInformation, final String documentNumber, final String actionType,
            final Integer nextCapitalAssetLineNumber, final long capitalAssetNumber) {
        final CapitalAssetInformation capitalAsset = new CapitalAssetInformation();
        capitalAsset.setCapitalAssetNumber(capitalAssetNumber);
        capitalAsset.setCapitalAssetLineAmount(KualiDecimal.ZERO);
        capitalAsset.setDocumentNumber(documentNumber);
        capitalAsset.setCapitalAssetLineNumber(nextCapitalAssetLineNumber);
        capitalAsset.setCapitalAssetActionIndicator(actionType);
        capitalAsset.setCapitalAssetProcessedIndicator(false);

        //now setup the account line information associated with this capital asset
        for (final CapitalAccountingLines capitalAccountingLine : capitalAccountingLines) {
            capitalAsset.setDistributionAmountCode(capitalAccountingLine.getDistributionAmountCode());
            createCapitalAssetAccountingLinesDetails(capitalAccountingLine, capitalAsset);
        }

        currentCapitalAssetInformation.add(capitalAsset);
    }

    /**
     * @param capitalAccountingLine
     * @param capitalAsset
     */
    protected void createCapitalAssetAccountingLinesDetails(
            final CapitalAccountingLines capitalAccountingLine,
            final CapitalAssetInformation capitalAsset) {
        //now setup the account line information associated with this capital asset
        final CapitalAssetAccountsGroupDetails capitalAssetAccountLine = new CapitalAssetAccountsGroupDetails();
        capitalAssetAccountLine.setDocumentNumber(capitalAsset.getDocumentNumber());
        capitalAssetAccountLine.setChartOfAccountsCode(capitalAccountingLine.getChartOfAccountsCode());
        capitalAssetAccountLine.setAccountNumber(capitalAccountingLine.getAccountNumber());
        capitalAssetAccountLine.setSubAccountNumber(capitalAccountingLine.getSubAccountNumber());
        capitalAssetAccountLine.setFinancialDocumentLineTypeCode(KFSConstants.SOURCE.equals(capitalAccountingLine
                .getLineType()) ? KFSConstants.SOURCE_ACCT_LINE_TYPE_CODE : KFSConstants.TARGET_ACCT_LINE_TYPE_CODE);
        capitalAssetAccountLine.setCapitalAssetAccountLineNumber(getNextAccountingLineNumber(capitalAccountingLine,
                capitalAsset));
        capitalAssetAccountLine.setCapitalAssetLineNumber(capitalAsset.getCapitalAssetLineNumber());
        capitalAssetAccountLine.setFinancialObjectCode(capitalAccountingLine.getFinancialObjectCode());
        capitalAssetAccountLine.setFinancialSubObjectCode(capitalAccountingLine.getFinancialSubObjectCode());
        capitalAssetAccountLine.setProjectCode(capitalAccountingLine.getProjectCode());
        capitalAssetAccountLine.setOrganizationReferenceId(capitalAccountingLine.getOrganizationReferenceId());
        capitalAssetAccountLine.setSequenceNumber(capitalAccountingLine.getSequenceNumber());
        capitalAssetAccountLine.setAmount(KualiDecimal.ZERO);
        capitalAsset.getCapitalAssetAccountsGroupDetails().add(capitalAssetAccountLine);
    }

    /**
     * calculates the next accounting line number for accounts details for each capital asset.
     * Goes through the current records and gets the last accounting line number.
     *
     * @param capitalAsset
     * @return nextAccountingLineNumber
     */
    protected Integer getNextAccountingLineNumber(
            final CapitalAccountingLines capitalAccountingLine,
            final CapitalAssetInformation capitalAsset) {
        Integer nextAccountingLineNumber = 0;
        final List<CapitalAssetAccountsGroupDetails> capitalAssetAccountLines = capitalAsset.getCapitalAssetAccountsGroupDetails();
        for (final CapitalAssetAccountsGroupDetails capitalAssetAccountLine : capitalAssetAccountLines) {
            nextAccountingLineNumber = capitalAssetAccountLine.getCapitalAssetAccountLineNumber();
        }

        return ++nextAccountingLineNumber;
    }

    /**
     * Finds a Capital Asset Information that matches the given capitalAccountingLine.
     *
     * @param capitalAccountingLine
     * @param capitalAssetInformation
     * @return return existingCapitalAsset
     */
    protected CapitalAssetInformation getCapitalAssetCreated(
            final CapitalAccountingLines capitalAccountingLine,
            final List<CapitalAssetInformation> capitalAssetInformation) {
        if (ObjectUtils.isNull(capitalAssetInformation) || capitalAssetInformation.size() <= 0) {
            return null;
        }

        for (final CapitalAssetInformation capitalAsset : capitalAssetInformation) {
            final List<CapitalAssetAccountsGroupDetails> groupAccountLines = capitalAsset.getCapitalAssetAccountsGroupDetails();
            for (final CapitalAssetAccountsGroupDetails groupAccountLine : groupAccountLines) {
                if (groupAccountLine.getCapitalAssetLineNumber().compareTo(capitalAsset.getCapitalAssetLineNumber()) == 0
                        && groupAccountLine.getSequenceNumber().compareTo(capitalAccountingLine.getSequenceNumber()) == 0
                        && groupAccountLine.getFinancialDocumentLineTypeCode().equals(KFSConstants.SOURCE
                             .equals(capitalAccountingLine.getLineType()) ?
                                KFSConstants.SOURCE_ACCT_LINE_TYPE_CODE : KFSConstants.TARGET_ACCT_LINE_TYPE_CODE)
                        && groupAccountLine.getChartOfAccountsCode().equals(capitalAccountingLine.getChartOfAccountsCode())
                        && groupAccountLine.getAccountNumber().equals(capitalAccountingLine.getAccountNumber())
                        && groupAccountLine.getFinancialObjectCode().equals(capitalAccountingLine.getFinancialObjectCode())) {
                    return capitalAsset;
                }
            }
        }

        return null;
    }

    /**
     * @param capitalAccountingLines
     * @param capitalAssetInformation
     * @return capitalAsset
     */
    protected CapitalAssetInformation capitalAssetCreated(
            final List<CapitalAccountingLines> capitalAccountingLines,
            final List<CapitalAssetInformation> capitalAssetInformation) {
        CapitalAssetInformation existingCapitalAsset = null;

        if (ObjectUtils.isNull(capitalAssetInformation) && capitalAssetInformation.size() <= 0) {
            return null;
        }

        for (final CapitalAccountingLines capitalAccountingLine : capitalAccountingLines) {
            existingCapitalAsset = getCapitalAssetCreated(capitalAccountingLine, capitalAssetInformation);
            if (ObjectUtils.isNotNull(existingCapitalAsset)) {
                return existingCapitalAsset;
            }
        }

        return existingCapitalAsset;
    }

    /**
     * @param capitalAccountingLine
     * @param capitalAssetInformation
     * @return modify capital asset
     */
    protected CapitalAssetInformation modifyCapitalAssetCreated(
            final CapitalAccountingLines capitalAccountingLine,
            final List<CapitalAssetInformation> capitalAssetInformation) {
        if (ObjectUtils.isNull(capitalAssetInformation) && capitalAssetInformation.size() <= 0) {
            return null;
        }

        for (final CapitalAssetInformation capitalAsset : capitalAssetInformation) {
            if (KFSConstants.CapitalAssets.CAPITAL_ASSET_MODIFY_ACTION_INDICATOR.equals(capitalAsset.getCapitalAssetActionIndicator())
                    && ObjectUtils.isNull(capitalAsset.getCapitalAssetNumber())) {
                final List<CapitalAssetAccountsGroupDetails> groupAccountLines = capitalAsset.getCapitalAssetAccountsGroupDetails();
                for (final CapitalAssetAccountsGroupDetails groupAccountLine : groupAccountLines) {
                    if (groupAccountLine.getCapitalAssetLineNumber().compareTo(capitalAsset.getCapitalAssetLineNumber()) == 0
                            && groupAccountLine.getSequenceNumber().compareTo(capitalAccountingLine.getSequenceNumber()) == 0
                            && groupAccountLine.getFinancialDocumentLineTypeCode().equals(KFSConstants.SOURCE
                                    .equals(capitalAccountingLine.getLineType()) ?
                                        KFSConstants.SOURCE_ACCT_LINE_TYPE_CODE : KFSConstants.TARGET_ACCT_LINE_TYPE_CODE)
                            && groupAccountLine.getChartOfAccountsCode().equals(capitalAccountingLine.getChartOfAccountsCode())
                            && groupAccountLine.getAccountNumber().equals(capitalAccountingLine.getAccountNumber())
                            && groupAccountLine.getFinancialObjectCode().equals(capitalAccountingLine.getFinancialObjectCode())) {
                        return capitalAsset;
                    }
                }
            }
        }

        return null;
    }

    /**
     * @param kualiAccountingDocumentFormBase
     * @return
     */
    protected Integer getNextCapitalAssetLineNumber(final KualiAccountingDocumentFormBase kualiAccountingDocumentFormBase) {
        final int nextCapitalAssetLineNumber;
        final CapitalAssetInformationDocumentBase caidb = (CapitalAssetInformationDocumentBase) kualiAccountingDocumentFormBase.getFinancialDocument();
        nextCapitalAssetLineNumber = caidb.getNextCapitalAssetLineNumber();
        caidb.setNextCapitalAssetLineNumber(nextCapitalAssetLineNumber + 1);
        return nextCapitalAssetLineNumber;
    }

    /**
     * @param capitalAccountingLines
     * @param existingCapitalAsset
     * @return true if accounting line amount equals to capital asset amount, else false.
     */
    protected boolean accountingLinesAmountDistributed(
            final List<CapitalAccountingLines> capitalAccountingLines,
            final CapitalAssetInformation existingCapitalAsset) {
        KualiDecimal accountingLineAmount = KualiDecimal.ZERO;
        for (final CapitalAccountingLines capitalAccountingLine : capitalAccountingLines) {
            accountingLineAmount = accountingLineAmount.add(capitalAccountingLine.getAmount().abs());
        }

        final KualiDecimal capitalAssetsAmount = existingCapitalAsset.getCapitalAssetLineAmount();
        return accountingLineAmount.equals(capitalAssetsAmount);
    }

    /**
     * Checks to see if all the capital assets' distributed amount is the same as
     * the capital accounting lines (there is only one lines, of course).
     *
     * @param capitalAccountingLine
     * @param capitalAssetsInformation
     * @return true if accounting line amount equals to capital asset amount, else false.
     */
    protected boolean capitalAccountingLineAmountDistributed(
            final CapitalAccountingLines capitalAccountingLine,
            final List<CapitalAssetInformation> capitalAssetsInformation) {
        KualiDecimal amountDistributed = KualiDecimal.ZERO;
        for (final CapitalAssetInformation capitalAsset : capitalAssetsInformation) {
            amountDistributed = amountDistributed.add(getAccountingLineAmount(capitalAsset, capitalAccountingLine));
        }

        final KualiDecimal capitalAccountingLineAmount = capitalAccountingLine.getAmount();

        return amountDistributed.equals(capitalAccountingLineAmount);
    }

    /**
     * Returns the amount of the group accounting line from the capital asset information that
     * matches the capital accounting lines (only one lines, of course). If none exists, zero is returned.
     *
     * @param capitalAsset
     * @param capitalAccountingLine
     * @return accountLineAmount
     */
    protected KualiDecimal getAccountingLineAmount(final CapitalAssetInformation capitalAsset, final CapitalAccountingLines capitalAccountingLine) {
        final KualiDecimal accountLineAmount = KualiDecimal.ZERO;

        final List<CapitalAssetAccountsGroupDetails> groupAccountLines = capitalAsset.getCapitalAssetAccountsGroupDetails();

        for (final CapitalAssetAccountsGroupDetails groupAccountLine : groupAccountLines) {
            if (groupAccountLine.getCapitalAssetLineNumber().compareTo(capitalAsset.getCapitalAssetLineNumber()) == 0 &&
                groupAccountLine.getSequenceNumber().compareTo(capitalAccountingLine.getSequenceNumber()) == 0 &&
                groupAccountLine.getFinancialDocumentLineTypeCode().equals(KFSConstants.SOURCE.equals(capitalAccountingLine.getLineType()) ? KFSConstants.SOURCE_ACCT_LINE_TYPE_CODE : KFSConstants.TARGET_ACCT_LINE_TYPE_CODE) &&
                groupAccountLine.getChartOfAccountsCode().equals(capitalAccountingLine.getChartOfAccountsCode()) &&
                groupAccountLine.getAccountNumber().equals(capitalAccountingLine.getAccountNumber()) &&
                groupAccountLine.getFinancialObjectCode().equals(capitalAccountingLine.getFinancialObjectCode())) {
                return groupAccountLine.getAmount();
            }
        }

        return accountLineAmount;
    }

    /**
     * adds any missing capital accounting line details as an accounting line into the collection of
     * accounting lines for this capital asset based on the action type.
     *
     * @param capitalAccountingLines
     * @param existingCapitalAsset
     */
    protected void addMissingAccountingLinesToCapitalAsset(
            final List<CapitalAccountingLines> capitalAccountingLines,
            final CapitalAssetInformation existingCapitalAsset) {
        final List<CapitalAssetAccountsGroupDetails> groupAccountLines = existingCapitalAsset.getCapitalAssetAccountsGroupDetails();

        for (final CapitalAccountingLines capitalAccountingLine : capitalAccountingLines) {
            if (capitalAccountingLineMissing(capitalAccountingLine, groupAccountLines, existingCapitalAsset.getCapitalAssetLineNumber())) {
                createCapitalAssetAccountingLinesDetails(capitalAccountingLine, existingCapitalAsset);
            }
        }
    }

    /**
     * creates a new tag/location details record and adds to the collection for capital asset
     *
     * @param capitalAsset
     */
    protected void createCapitalAssetInformationDetail(final CapitalAssetInformation capitalAsset) {
        final CapitalAssetInformationDetail assetDetail = new CapitalAssetInformationDetail();
        assetDetail.setDocumentNumber(capitalAsset.getDocumentNumber());
        assetDetail.setCapitalAssetLineNumber(capitalAsset.getCapitalAssetLineNumber());
        assetDetail.setItemLineNumber(getNextLineItemNumber(capitalAsset));
        // ==== CU Customization: Populate extended attribute on Information Detail object. ====
        CapitalAssetInformationDetailExtendedAttribute detailea = (CapitalAssetInformationDetailExtendedAttribute) assetDetail.getExtension();
        detailea.setDocumentNumber(capitalAsset.getDocumentNumber());
        detailea.setCapitalAssetLineNumber(assetDetail.getCapitalAssetLineNumber());
        detailea.setItemLineNumber(assetDetail.getItemLineNumber());
        capitalAsset.getCapitalAssetInformationDetails().add(0, assetDetail);
    }

    /**
     * calculates the next line item number for tag/location details for each capital asset.
     * Goes through the current records and gets the last number.
     *
     * @param capitalAsset
     * @return nextLineNumber
     */
    protected Integer getNextLineItemNumber(final CapitalAssetInformation capitalAsset) {
        Integer nextLineNumber = 0;
        final List<CapitalAssetInformationDetail> capitalAssetDetails = capitalAsset.getCapitalAssetInformationDetails();
        for (final CapitalAssetInformationDetail capitalAssetDetail : capitalAssetDetails) {
            nextLineNumber = capitalAssetDetail.getItemLineNumber();
        }

        return ++nextLineNumber;
    }

    protected void calculateRemainingDistributedAmount(
            final CapitalAccountingLinesFormBase calfb,
            final List<CapitalAssetInformation> capitalAssetInformation) {
        calfb.setCreatedAssetsControlAmount(calfb.getSystemControlAmount());

        //get amount allocated so far....or the system control remainder amount field.
        for (final CapitalAssetInformation capitalAsset : capitalAssetInformation) {
            calfb.setCreatedAssetsControlAmount(calfb.getCreatedAssetsControlAmount().subtract(capitalAsset.getCapitalAssetLineAmount()));
        }
    }

    /**
     * checks the current list of accounting lines created for the capital asset against the given
     * capital accounting line and returns true or false
     *
     * @param capitalAccountingLine
     * @param groupAccountLines
     * @param capitalAssetLineNumber
     * @return true if line exists else return false
     */
    protected boolean capitalAccountingLineMissing(
            final CapitalAccountingLines capitalAccountingLine,
            final List<CapitalAssetAccountsGroupDetails> groupAccountLines, final Integer capitalAssetLineNumber) {
        for (final CapitalAssetAccountsGroupDetails groupAccountLine : groupAccountLines) {
            if (groupAccountLine.getCapitalAssetLineNumber().compareTo(capitalAssetLineNumber) == 0
                    && groupAccountLine.getSequenceNumber().compareTo(capitalAccountingLine.getSequenceNumber()) == 0
                    && groupAccountLine.getFinancialDocumentLineTypeCode().equals(KFSConstants.SOURCE
                            .equals(capitalAccountingLine.getLineType()) ?
                                    KFSConstants.SOURCE_ACCT_LINE_TYPE_CODE : KFSConstants.TARGET_ACCT_LINE_TYPE_CODE)
                    && groupAccountLine.getChartOfAccountsCode().equals(capitalAccountingLine.getChartOfAccountsCode())
                    && groupAccountLine.getAccountNumber().equals(capitalAccountingLine.getAccountNumber())
                    && groupAccountLine.getFinancialObjectCode().equals(capitalAccountingLine.getFinancialObjectCode())) {
                return false;
            }
        }

        return true;
    }

    /**
     * sets the capital accounting lines select and amount distributed values to true if
     * there are capital asset records for a given capital accounting line. The system control
     * amount and system control remaining amounts are calculated here.
     *
     * @param calfb
     */
    protected void checkCapitalAccountingLinesSelected(final CapitalAccountingLinesFormBase calfb) {
        final CapitalAccountingLinesDocumentBase caldb = (CapitalAccountingLinesDocumentBase) calfb.getFinancialDocument();

        final List<CapitalAccountingLines> capitalAccountingLines = caldb.getCapitalAccountingLines();

        final List<CapitalAssetInformation> currentCapitalAssetInformation = getCurrentCapitalAssetInformationObject(
                calfb);

        SpringContext.getBean(CapitalAssetManagementModuleService.class).filterNonCapitalAssets(currentCapitalAssetInformation);

        calfb.setCreatedAssetsControlAmount(KualiDecimal.ZERO);
        calfb.setSystemControlAmount(KualiDecimal.ZERO);

        for (final CapitalAccountingLines capitalAccountingLine : capitalAccountingLines) {
            if (capitalAccountingLine.isSelectLine()) {
                calfb.setSystemControlAmount(calfb.getSystemControlAmount().add(capitalAccountingLine.getAmount()));
            }

            if (currentCapitalAssetInformation.size() <= 0) {
                capitalAccountingLine.setAccountLinePercent(null);
                capitalAccountingLine.setAmountDistributed(false);
                capitalAccountingLine.setSelectLine(false);
            } else {
                final CapitalAssetInformation existingCapitalAsset = getCapitalAssetCreated(capitalAccountingLine, currentCapitalAssetInformation);
                if (ObjectUtils.isNotNull(existingCapitalAsset)) {
                    // There is a CapitalAssetInformation matching the current accounting line.
                    capitalAccountingLine.setSelectLine(true);
                } else {
                    capitalAccountingLine.setAccountLinePercent(null);
                    capitalAccountingLine.setSelectLine(false);
                }
            }

            if (capitalAccountingLineAmountDistributed(capitalAccountingLine, currentCapitalAssetInformation)) {
                // all the money from this accounting line is distributed among the assets
                capitalAccountingLine.setAmountDistributed(true);
            } else {
                capitalAccountingLine.setAmountDistributed(false);
            }
        }

        KualiDecimal capitalAssetsTotal = KualiDecimal.ZERO;

        //get amount allocated so far....or the system control remainder amount field.
        for (final CapitalAssetInformation capitalAsset : currentCapitalAssetInformation) {
            capitalAssetsTotal = capitalAssetsTotal.add(capitalAsset.getCapitalAssetLineAmount());
        }

        calfb.setCreatedAssetsControlAmount(calfb.getSystemControlAmount().subtract(capitalAssetsTotal));
    }

    /**
     * sets the capital assets screens for create and modify and accounting lines for capitalization screen as open.
     * If accounting lines for capitalization list is not empty then set "Accounting Lines for Capitalization" tab to
     * open else set to close.
     * If capital asset with capital asset action indicator = 'C' then set "Create Capital Asset" tab to open else
     * set to close
     * If capital asset with capital asset action indicator = 'M' then set "Modify Capital Asset"
     * tab to open else set to close
     *
     * @param form
     */
    protected void setTabStatesForCapitalAssets(final ActionForm form) {
        final KualiForm kualiForm = (KualiForm) form;

        final CapitalAccountingLinesFormBase capitalAccountingLinesFormBase = (CapitalAccountingLinesFormBase) form;

        final Map<String, String> tabStates = kualiForm.getTabStates();

        final CapitalAssetInformationFormBase capitalAssetInformationFormBase = (CapitalAssetInformationFormBase) form;
        final CapitalAccountingLinesDocumentBase caldb = (CapitalAccountingLinesDocumentBase) capitalAssetInformationFormBase
                .getFinancialDocument();

        //generated tab key for the three tabs
        final String tabIdForAccountingLinesForCapitalization = WebUtils.generateTabKey(
                KFSConstants.CapitalAssets.ACCOUNTING_LINES_FOR_CAPITALIZATION_TAB_TITLE);
        final String tabIdForCreateCapitalAsset = WebUtils.generateTabKey(
                KFSConstants.CapitalAssets.CREATE_CAPITAL_ASSETS_TAB_TITLE);
        final String tabIdForModifyCapitalAsset = WebUtils.generateTabKey(
                KFSConstants.CapitalAssets.MODIFY_CAPITAL_ASSETS_TAB_TITLE);

        tabStates.remove(tabIdForAccountingLinesForCapitalization);
        tabStates.remove(tabIdForCreateCapitalAsset);
        tabStates.remove(tabIdForModifyCapitalAsset);

        //if there are any capital accounting lines for capitalization exists then
        if (caldb.getCapitalAccountingLines().size() > 0 || caldb.isCapitalAccountingLinesExist()) {
            tabStates.put(tabIdForAccountingLinesForCapitalization,
                    KFSConstants.CapitalAssets.CAPITAL_ASSET_TAB_STATE_OPEN);
        } else {
            tabStates.put(tabIdForAccountingLinesForCapitalization,
                    KFSConstants.CapitalAssets.CAPITAL_ASSET_TAB_STATE_CLOSE);
        }

        if (checkCreateAssetsExist(capitalAccountingLinesFormBase)) {
            tabStates.put(tabIdForCreateCapitalAsset, KFSConstants.CapitalAssets.CAPITAL_ASSET_TAB_STATE_OPEN);
        } else {
            tabStates.put(tabIdForCreateCapitalAsset, KFSConstants.CapitalAssets.CAPITAL_ASSET_TAB_STATE_CLOSE);
        }

        if (checkModifyAssetsExist(capitalAccountingLinesFormBase)) {
            tabStates.put(tabIdForModifyCapitalAsset, KFSConstants.CapitalAssets.CAPITAL_ASSET_TAB_STATE_OPEN);
        } else {
            tabStates.put(tabIdForModifyCapitalAsset, KFSConstants.CapitalAssets.CAPITAL_ASSET_TAB_STATE_CLOSE);
        }

        kualiForm.setTabStates(tabStates);
    }

    /**
     * @param capitalAccountingLinesFormBase
     * @return true if a capital asset with capital asset action indicator = 'C' else false;
     */
    protected boolean checkCreateAssetsExist(final CapitalAccountingLinesFormBase capitalAccountingLinesFormBase) {
        final CapitalAssetInformationDocumentBase capitalAssetInformationDocumentBase =
                (CapitalAssetInformationDocumentBase) capitalAccountingLinesFormBase.getFinancialDocument();

        final List<CapitalAssetInformation> capitalAssets = capitalAssetInformationDocumentBase.getCapitalAssetInformation();

        for (final CapitalAssetInformation capitalAsset : capitalAssets) {
            if (KFSConstants.CapitalAssets.CAPITAL_ASSET_CREATE_ACTION_INDICATOR.equals(capitalAsset.getCapitalAssetActionIndicator())) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param capitalAccountingLinesFormBase
     * @return true if a capital asset with capital asset action indicator = 'C' else false;
     */
    protected boolean checkModifyAssetsExist(final CapitalAccountingLinesFormBase capitalAccountingLinesFormBase) {
        final CapitalAssetInformationDocumentBase capitalAssetInformationDocumentBase =
                (CapitalAssetInformationDocumentBase) capitalAccountingLinesFormBase.getFinancialDocument();

        final List<CapitalAssetInformation> capitalAssets = capitalAssetInformationDocumentBase.getCapitalAssetInformation();
        for (final CapitalAssetInformation capitalAsset : capitalAssets) {
            if (KFSConstants.CapitalAssets.CAPITAL_ASSET_MODIFY_ACTION_INDICATOR.equals(capitalAsset.getCapitalAssetActionIndicator())) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return actionForward
     * @throws Exception
     */
    public ActionForward redistributeCreateCapitalAssetAmount(
            final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        LOG.debug("redistributeCreateCapitalAssetAmount() - start");

        final CapitalAccountingLinesFormBase calfb = (CapitalAccountingLinesFormBase) form;
        final List<CapitalAccountingLines> selectedCapitalAccountingLines = new ArrayList<>();

        final KualiDecimal remainingAmountToDistribute = getRemainingAmountToDistribute(selectedCapitalAccountingLines, form);

        final KualiAccountingDocumentFormBase kualiAccountingDocumentFormBase = (KualiAccountingDocumentFormBase) form;
        final List<CapitalAssetInformation> capitalAssetInformation = getCurrentCapitalAssetInformationObject(kualiAccountingDocumentFormBase);

        //run the process to redistribute the accounting line amount to the capital assets.
        redistributeEqualAmountsForAccountingLineForCreateAssets(selectedCapitalAccountingLines, capitalAssetInformation,
                remainingAmountToDistribute);

        redistributeIndividualAmountsForAccountingLinesForCreateAssets(selectedCapitalAccountingLines, capitalAssetInformation);

        //adjust any variance from capital accounting lines to the distributed accounting lines amounts....
        adjustCapitalAssetsAccountingLinesAmounts(selectedCapitalAccountingLines, capitalAssetInformation);

        checkCapitalAccountingLinesSelected(calfb);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * redistributes the capital asset amount for the modify capital asset lines.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward redistributeModifyCapitalAssetAmount(
            final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        LOG.debug("redistributeModifyCapitalAssetAmount() - start");

        final CapitalAccountingLinesFormBase calfb = (CapitalAccountingLinesFormBase) form;
        final List<CapitalAccountingLines> selectedCapitalAccountingLines = new ArrayList<>();

        final KualiDecimal remainingAmountToDistribute = getRemainingAmountToDistribute(selectedCapitalAccountingLines, form);

        final KualiAccountingDocumentFormBase kualiAccountingDocumentFormBase = (KualiAccountingDocumentFormBase) form;
        final List<CapitalAssetInformation> capitalAssetInformation = getCurrentCapitalAssetInformationObject(kualiAccountingDocumentFormBase);

        //run the process to redistribute the accounting line amount to the capital assets.
        redistributeAmountsForAccountingsLineForModifyAssets(selectedCapitalAccountingLines, capitalAssetInformation,
                remainingAmountToDistribute);

        redistributeIndividualAmountsForAccountingLinesForModifyAssets(selectedCapitalAccountingLines,
                capitalAssetInformation);

        //now process any capital assets that has distribution set to "by amount"
        redistributeAmountsForAccountingsLineForModifyAssetsByAmounts(selectedCapitalAccountingLines,
                capitalAssetInformation, remainingAmountToDistribute);

        //adjust any variance from capital accounting lines to the distributed accounting lines amounts....
        adjustCapitalAssetsAccountingLinesAmounts(selectedCapitalAccountingLines, capitalAssetInformation);

        processRemainingCapitalAssetInfo(form, capitalAssetInformation);

        //redistribute capital asset amount to its group accounting lines on refresh
        DistributeCapitalAssetAmountToGroupAccountingLines((KualiAccountingDocumentFormBase) form);

        checkCapitalAccountingLinesSelected(calfb);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Calculates the remaining amount to distribute by taking selected capital accounting lines
     * and subtracting the allocated capital asset accounting lines amounts totals.
     *
     * @param selectedCapitalAccountingLines
     * @param form
     * @return remainingAmountToDistribute
     */
    protected KualiDecimal getRemainingAmountToDistribute(
            final List<CapitalAccountingLines> selectedCapitalAccountingLines,
            final ActionForm form) {
        KualiDecimal capitalAccountsAmountToDistribute = KualiDecimal.ZERO;
        KualiDecimal capitalAssetsAllocatedAmount = KualiDecimal.ZERO;

        final CapitalAccountingLinesFormBase calfb = (CapitalAccountingLinesFormBase) form;

        final CapitalAssetInformationDocumentBase capitalAssetInformationDocumentBase =
                (CapitalAssetInformationDocumentBase) calfb.getFinancialDocument();
        final List<CapitalAssetInformation> capitalAssets = capitalAssetInformationDocumentBase.getCapitalAssetInformation();

        final CapitalAccountingLinesDocumentBase caldb = (CapitalAccountingLinesDocumentBase) calfb.getFinancialDocument();
        final List<CapitalAccountingLines> capitalAccountingLines = caldb.getCapitalAccountingLines();

        for (final CapitalAccountingLines capitalAccountingLine : capitalAccountingLines) {
            if (capitalAccountingLine.isSelectLine() && !capitalAccountingLine.isAmountDistributed()) {
                selectedCapitalAccountingLines.add(capitalAccountingLine);
                capitalAccountsAmountToDistribute = capitalAccountsAmountToDistribute.add(capitalAccountingLine.getAmount());
                capitalAssetsAllocatedAmount = capitalAssetsAllocatedAmount.add(getCapitalAssetsAmountAllocated(capitalAssets, capitalAccountingLine));
            }
        }

        return capitalAccountsAmountToDistribute.subtract(capitalAssetsAllocatedAmount);
    }

    /**
     * add detail lines into the given capital asset information
     *
     * @param capitalAssetInformation the given capital asset information
     */
    protected void addCapitalAssetInfoDetailLines(final CapitalAssetInformation capitalAssetInformation) {
        LOG.debug("addCapitalAssetInfoDetailLines() - start");

        if (ObjectUtils.isNull(capitalAssetInformation)) {
            return;
        }

        final Integer quantity = capitalAssetInformation.getCapitalAssetQuantity();
        if (quantity == null || quantity <= 0) {
            final String errorPath = KFSPropertyConstants.DOCUMENT + "." + KFSPropertyConstants.CAPITAL_ASSET_INFORMATION;
            GlobalVariables.getMessageMap().putError(errorPath, KFSKeyConstants.ERROR_INVALID_CAPITAL_ASSET_QUANTITY);
            return;
        }

        final List<CapitalAssetInformationDetail> detailLines = capitalAssetInformation.getCapitalAssetInformationDetails();
        int nextItemLineNumber = 0;

        if (ObjectUtils.isNotNull(detailLines) || detailLines.size() > 0) {
            for (final CapitalAssetInformationDetail detailLine : detailLines) {
                nextItemLineNumber = detailLine.getItemLineNumber();
            }
        }

        // If details collection has old lines, this loop will add new lines to make the total equal to the quantity.
        for (int index = 1; detailLines.size() < quantity; index++) {
            final CapitalAssetInformationDetail detailLine = new CapitalAssetInformationDetail();
            detailLine.setDocumentNumber(capitalAssetInformation.getDocumentNumber());
            detailLine.setCapitalAssetLineNumber(capitalAssetInformation.getCapitalAssetLineNumber());
            detailLine.setItemLineNumber(++nextItemLineNumber);
            // ==== CU Customization: Populate extended attribute on Information Detail object. ====
            CapitalAssetInformationDetailExtendedAttribute detailea = (CapitalAssetInformationDetailExtendedAttribute) detailLine.getExtension();
            detailea.setDocumentNumber(capitalAssetInformation.getDocumentNumber());
            detailea.setCapitalAssetLineNumber(detailLine.getCapitalAssetLineNumber());
            detailea.setItemLineNumber(detailLine.getItemLineNumber());
            detailLines.add(detailLine);
        }
    }

    /**
     * unchecks the capital accounting lines select when there are no capital assets created yet.
     *
     * @param calfb
     */
    protected void uncheckCapitalAccountingLinesSelected(final CapitalAccountingLinesFormBase calfb) {
        final CapitalAccountingLinesDocumentBase caldb = (CapitalAccountingLinesDocumentBase) calfb.getDocument();

        final List<CapitalAccountingLines> capitalAccountingLines = caldb.getCapitalAccountingLines();

        final List<CapitalAssetInformation> currentCapitalAssetInformation = getCurrentCapitalAssetInformationObject(
                calfb);

        final List<CapitalAccountingLines> selectedCapitalAccountingLines = new ArrayList<>();

        for (final CapitalAccountingLines capitalAccountingLine : capitalAccountingLines) {
            if (capitalAccountingLine.isSelectLine() && !capitalAccountingLine.isAmountDistributed()) {
                calfb.setSystemControlAmount(calfb.getSystemControlAmount().add(capitalAccountingLine.getAmount()));
                selectedCapitalAccountingLines.add(capitalAccountingLine);
            }
        }

        final CapitalAssetInformation existingCapitalAsset = capitalAssetCreated(selectedCapitalAccountingLines,
                currentCapitalAssetInformation);

        for (final CapitalAccountingLines capitalAccountingLine : selectedCapitalAccountingLines) {
            if (ObjectUtils.isNull(existingCapitalAsset)) {
                capitalAccountingLine.setSelectLine(false);
            }
        }
    }

    /**
     * checks "select" check box on capital accounting lines if there are
     * corresponding capital asset records.
     *
     * @param calfb
     */
    protected void checkSelectForCapitalAccountingLines(final CapitalAccountingLinesFormBase calfb) {
        final CapitalAccountingLinesDocumentBase caldb = (CapitalAccountingLinesDocumentBase) calfb.getFinancialDocument();
        final List<CapitalAccountingLines> capitalAccountingLines = caldb.getCapitalAccountingLines();
        final List<CapitalAssetInformation> currentCapitalAssetInformation = getCurrentCapitalAssetInformationObject(
                calfb);

        for (final CapitalAccountingLines capitalAccountingLine : capitalAccountingLines) {
            if (currentCapitalAssetInformation.size() <= 0) {
                capitalAccountingLine.setSelectLine(false);
            } else {
                final CapitalAssetInformation existingCapitalAsset = getCapitalAssetCreated(capitalAccountingLine,
                        currentCapitalAssetInformation);
                capitalAccountingLine.setSelectLine(ObjectUtils.isNotNull(existingCapitalAsset));
            }
        }
    }

    /**
     * gets the total of all accounting lines from that capital asset.
     *
     * @param capitalAssetInformation
     * @return accountingLinesTotalAmount
     */
    protected KualiDecimal getAccountingLinesTotalAmount(final CapitalAssetInformation capitalAssetInformation) {
        KualiDecimal accountingLinesTotalAmount = KualiDecimal.ZERO;

        final List<CapitalAssetAccountsGroupDetails> groupAccountLines = capitalAssetInformation.getCapitalAssetAccountsGroupDetails();

        for (final CapitalAssetAccountsGroupDetails groupAccountLine : groupAccountLines) {
            accountingLinesTotalAmount = accountingLinesTotalAmount.add(groupAccountLine.getAmount());
        }

        return accountingLinesTotalAmount;
    }

    /**
     * @param capitalAccountingLine
     * @param capitalAssetInformation
     * @param actionTypeCode
     * @return accountingLinesTotalAmount
     */
    protected KualiDecimal getAccountingLinesDistributedAmount(
            final CapitalAccountingLines capitalAccountingLine,
            final List<CapitalAssetInformation> capitalAssetInformation, final String actionTypeCode) {
        KualiDecimal accountingLinesTotalAmount = KualiDecimal.ZERO;

        for (final CapitalAssetInformation capitalAsset : capitalAssetInformation) {
            if (capitalAsset.getCapitalAssetActionIndicator().equalsIgnoreCase(actionTypeCode)) {
                accountingLinesTotalAmount = accountingLinesTotalAmount.add(getAccountingLineAmount(capitalAsset,
                        capitalAccountingLine));
            }
        }

        return accountingLinesTotalAmount;
    }

    /**
     * @param capitalAccountingLine
     * @param lastCapitalAsset
     * @param difference
     */
    protected void adjustAccountingLineAmountOnLastCapitalAsset(
            final CapitalAccountingLines capitalAccountingLine,
            final CapitalAssetInformation lastCapitalAsset, final KualiDecimal difference) {
        final List<CapitalAssetAccountsGroupDetails> groupAccountLines = lastCapitalAsset.getCapitalAssetAccountsGroupDetails();

        for (final CapitalAssetAccountsGroupDetails groupAccountLine : groupAccountLines) {
            if (groupAccountLine.getCapitalAssetLineNumber().compareTo(lastCapitalAsset.getCapitalAssetLineNumber()) == 0
                    && groupAccountLine.getSequenceNumber().compareTo(capitalAccountingLine.getSequenceNumber()) == 0
                    && groupAccountLine.getFinancialDocumentLineTypeCode().equals(KFSConstants.SOURCE
                            .equals(capitalAccountingLine.getLineType()) ?
                                KFSConstants.SOURCE_ACCT_LINE_TYPE_CODE : KFSConstants.TARGET_ACCT_LINE_TYPE_CODE)
                    && groupAccountLine.getChartOfAccountsCode().equals(capitalAccountingLine.getChartOfAccountsCode())
                    && groupAccountLine.getAccountNumber().equals(capitalAccountingLine.getAccountNumber())
                    && groupAccountLine.getFinancialObjectCode().equals(capitalAccountingLine.getFinancialObjectCode())) {
                groupAccountLine.setAmount(groupAccountLine.getAmount().add(difference));
            }
        }
    }

    /**
     * when the user user hits refresh button, takes the amount in the amount field and
     * distributes to the group capital accounting lines for that asset only.
     *
     * @param selectedCapitalAccountingLines
     * @param capitalAsset
     */
    protected void redistributeToGroupAccountingLinesFromAssetsByAmounts(
            final List<CapitalAccountingLines> selectedCapitalAccountingLines, final CapitalAssetInformation capitalAsset) {
        final KualiDecimal amountToDistribute = capitalAsset.getCapitalAssetLineAmount();
        KualiDecimal amountDistributed = KualiDecimal.ZERO;

        final KualiDecimal totalCapitalAccountsAmount = getTotalCapitalAccountsAmounts(selectedCapitalAccountingLines);

        //to capture the last group accounting line to update its amount with any variance.
        CapitalAssetAccountsGroupDetails lastGroupAccountLine = new CapitalAssetAccountsGroupDetails();

        final List<CapitalAssetAccountsGroupDetails> groupAccountLines = capitalAsset.getCapitalAssetAccountsGroupDetails();
        for (final CapitalAssetAccountsGroupDetails groupAccountLine : groupAccountLines) {
            final BigDecimal linePercent = getCapitalAccountingLinePercent(selectedCapitalAccountingLines, groupAccountLine,
                    totalCapitalAccountsAmount);
            //found the accounting line
            lastGroupAccountLine = groupAccountLine;

            final KualiDecimal groupAccountLineAmount = capitalAsset.getCapitalAssetLineAmount().multiply(new KualiDecimal(linePercent));
            groupAccountLine.setAmount(groupAccountLineAmount);

            //keep track of amount distributed so far.
            amountDistributed = amountDistributed.add(groupAccountLineAmount);
        }

        //add any variance in the amounts to the last group accounting line.
        lastGroupAccountLine.setAmount(lastGroupAccountLine.getAmount().add(amountToDistribute.subtract(amountDistributed)));
    }

    /**
     * calculates the total amount of the selected capital accounting lines
     *
     * @param capitalAccountingLines
     * @return total amount of the selected capital accounting lines.
     */
    protected KualiDecimal getTotalCapitalAccountsAmounts(final List<CapitalAccountingLines> capitalAccountingLines) {
        KualiDecimal totalCapitalAccountsAmount = KualiDecimal.ZERO;
        for (final CapitalAccountingLines capitalLine : capitalAccountingLines) {
            totalCapitalAccountsAmount = totalCapitalAccountsAmount.add(capitalLine.getAmount());
        }

        return totalCapitalAccountsAmount;
    }

    /**
     * This method redistributes the capital asset amount to its group accounting lines
     * based on the accounting line's percent.  Takes each capital assets amount and
     * distributes to the capital asset group accounting lines.
     *
     * @param kualiAccountingDocumentFormBase
     */
    protected void DistributeCapitalAssetAmountToGroupAccountingLines(final KualiAccountingDocumentFormBase kualiAccountingDocumentFormBase) {
        final CapitalAccountingLinesFormBase capitalAccountingLinesFormBase = (CapitalAccountingLinesFormBase) kualiAccountingDocumentFormBase;
        checkSelectForCapitalAccountingLines(capitalAccountingLinesFormBase);

        checkCapitalAccountingLinesSelected(capitalAccountingLinesFormBase);
        calculatePercentsForSelectedCapitalAccountingLines(capitalAccountingLinesFormBase);

        final CapitalAccountingLinesDocumentBase caldb = (CapitalAccountingLinesDocumentBase) capitalAccountingLinesFormBase.getFinancialDocument();
        final String distributionAmountCode = capitalAccountingLinesFormBase.getCapitalAccountingLine().getDistributionCode();

        final List<CapitalAccountingLines> capitalAccountingLines = caldb.getCapitalAccountingLines();

        final List<CapitalAccountingLines> selectedCapitalAccountingLines = new ArrayList<>();

        for (final CapitalAccountingLines capitalAccountingLine : capitalAccountingLines) {
            if (capitalAccountingLine.isSelectLine() && !capitalAccountingLine.isAmountDistributed()) {
                capitalAccountingLine.setDistributionAmountCode(distributionAmountCode);
                selectedCapitalAccountingLines.add(capitalAccountingLine);
            }
        }

        final List<CapitalAssetInformation> capitalAssetInformation = getCurrentCapitalAssetInformationObject(kualiAccountingDocumentFormBase);

        for (final CapitalAssetInformation capitalAsset : capitalAssetInformation) {
            // redistribute the capital asset modify amount to the group accounting lines based on amount.
            if (!capitalAssetAmountAlreadyDistributedToGroupAccountingLines(capitalAsset)) {
                redistributeToGroupAccountingLinesFromAssetsByAmounts(selectedCapitalAccountingLines, capitalAsset);
            }
        }
    }

    /**
     * checks if the capital asset amount already distributed to its group accounting lines
     *
     * @param capitalAsset
     * @return true if amount already distributed else return false.
     */
    protected boolean capitalAssetAmountAlreadyDistributedToGroupAccountingLines(final CapitalAssetInformation capitalAsset) {
        final KualiDecimal capitalAssetAmount = capitalAsset.getCapitalAssetLineAmount();
        KualiDecimal totalAmountDistributed = KualiDecimal.ZERO;

        final List<CapitalAssetAccountsGroupDetails> groupAccountLines = capitalAsset.getCapitalAssetAccountsGroupDetails();
        for (final CapitalAssetAccountsGroupDetails groupAccountLine : groupAccountLines) {
            //keep track of amount distributed so far.
            totalAmountDistributed = totalAmountDistributed.add(groupAccountLine.getAmount());
        }

        return capitalAssetAmount.compareTo(totalAmountDistributed) == 0;
    }
}
