/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
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
package org.kuali.kfs.module.cam.document;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.maintenance.Maintainable;
import org.kuali.kfs.kns.util.KNSGlobalVariables;
import org.kuali.kfs.kns.web.ui.Section;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.maintenance.MaintenanceLock;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cam.CamsConstants;
import org.kuali.kfs.module.cam.CamsKeyConstants;
import org.kuali.kfs.module.cam.CamsPropertyConstants;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.module.cam.businessobject.AssetPayment;
import org.kuali.kfs.module.cam.businessobject.AssetRetirementGlobal;
import org.kuali.kfs.module.cam.businessobject.AssetRetirementGlobalDetail;
import org.kuali.kfs.module.cam.document.gl.AssetRetirementGeneralLedgerPendingEntrySource;
import org.kuali.kfs.module.cam.document.service.AssetPaymentService;
import org.kuali.kfs.module.cam.document.service.AssetRetirementService;
import org.kuali.kfs.module.cam.document.service.AssetService;
import org.kuali.kfs.module.cam.service.CapitalAssetManagementService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.LedgerPostingMaintainable;
import org.kuali.kfs.sys.document.service.FinancialSystemDocumentTypeService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/*
 * CU Customization: Backport FINP-11457 to prevent NPEs when performing an asset merge.
 *                   This overlay can be removed when we upgrade to the 2024-10-02 financials patch.
 */
public class AssetRetirementGlobalMaintainableImpl extends LedgerPostingMaintainable {

    private static final Logger LOG = LogManager.getLogger();
    protected static final String RETIRED_ASSET_TRANSFERRED_EXTERNALLY = "RetiredAssetTransferredExternally";
    protected static final String RETIRED_ASSET_SOLD_OR_GIFTED = "RetiredAssetSoldOrGifted";

    private transient AssetPaymentService assetPaymentService;
    private transient AssetRetirementService assetRetirementService;
    private transient AssetService assetService;
    private transient CapitalAssetManagementService capitalAssetManagementService;
    private transient DateTimeService dateTimeService;
    protected transient DocumentService documentService;
    private transient FinancialSystemDocumentTypeService financialSystemDocumentTypeService;

    @Override
    protected boolean answerSplitNodeQuestion(final String nodeName) throws UnsupportedOperationException {
        final String retirementReason = ((AssetRetirementGlobal) getBusinessObject()).getRetirementReasonCode();
        if (RETIRED_ASSET_TRANSFERRED_EXTERNALLY.equals(nodeName)) {
            return CamsConstants.AssetRetirementReasonCode.EXTERNAL_TRANSFER.equalsIgnoreCase(retirementReason);
        }
        if (RETIRED_ASSET_SOLD_OR_GIFTED.equals(nodeName)) {
            return CamsConstants.AssetRetirementReasonCode.SOLD.equalsIgnoreCase(retirementReason)
                    || CamsConstants.AssetRetirementReasonCode.GIFT.equalsIgnoreCase(retirementReason);
        } else if (CamsConstants.RouteLevelNames.ORGANIZATION_INACTIVE.equals(nodeName)) {
            return isRequiresOrganizationInactiveRouteNode();
        }

        throw new UnsupportedOperationException("Cannot answer split question for this node you call \"" + nodeName +
                "\"");
    }

    /**
     * @return if the organization inactive route node needs to be stopped at
     */
    protected boolean isRequiresOrganizationInactiveRouteNode() {
        final List<AssetRetirementGlobalDetail> assetRetirementGlobalDetails =
                ((AssetRetirementGlobal) getBusinessObject()).getAssetRetirementGlobalDetails();

        final List<Long> assets = new ArrayList<>();
        for (final AssetRetirementGlobalDetail assetRetirementGlobalDetail : assetRetirementGlobalDetails) {
            final Asset asset = assetRetirementGlobalDetail.getAsset();

            if (!asset.getOrganizationOwnerAccount().getOrganization().isActive()) {
                assets.add(asset.getCapitalAssetNumber());
            }
        }

        return !assets.isEmpty();
    }

    /**
     * We are using a substitute mechanism for asset locking which can lock on assets when rule check passed. Return
     * empty list from this method.
     */
    @Override
    public List<MaintenanceLock> generateMaintenanceLocks() {
        return new ArrayList<>();
    }

    @Override
    public void setupNewFromExisting(final MaintenanceDocument document, final Map<String, String[]> parameters) {
        super.setupNewFromExisting(document, parameters);

        final AssetRetirementGlobal assetRetirementGlobal = (AssetRetirementGlobal) getBusinessObject();
        if (getAssetRetirementService().isAssetRetiredByMerged(assetRetirementGlobal)) {
            if (ObjectUtils.isNotNull(assetRetirementGlobal.getMergedTargetCapitalAssetNumber())) {
                assetRetirementGlobal.setMergedTargetCapitalAssetDescription(
                        assetRetirementGlobal.getMergedTargetCapitalAsset().getCapitalAssetDescription());
            }
        }

        // add doc header description if retirement reason is "MERGED"
        if (CamsConstants.AssetRetirementReasonCode.MERGED.equals(assetRetirementGlobal.getRetirementReasonCode())) {
            document.getDocumentHeader().setDocumentDescription(
                    CamsConstants.AssetRetirementGlobal.MERGE_AN_ASSET_DESCRIPTION);
        }

        if (isFiscalPeriodEditable(document) && isPeriod13(assetRetirementGlobal)) {
            try {
                if (ObjectUtils.isNotNull(assetRetirementGlobal.getPostingYear())) {
                    assetRetirementGlobal.setAccountingPeriodCompositeString(
                            assetRetirementGlobal.getAccountingPeriod().getUniversityFiscalPeriodCode() +
                                    assetRetirementGlobal.getPostingYear());
                }
                assetRetirementGlobal.refreshNonUpdateableReferences();
            } catch (final Exception e) {
                LOG.error(e);
            }
        }
    }

    private boolean isFiscalPeriodEditable(final MaintenanceDocument document) {
        final String docType = document.getDocumentHeader().getWorkflowDocument().getDocumentTypeName();
        // CU Customization: Backport FINP-11457 fix.
        return getFinancialSystemDocumentTypeService().isFiscalPeriodEntryEnabledForDocumentType(docType);
    }

    @Override
    public List getSections(final MaintenanceDocument document, final Maintainable oldMaintainable) {
        final List<Section> sections = super.getSections(document, oldMaintainable);
        for (final Section section : sections) {
            if (section.getSectionId().equalsIgnoreCase("Accounting Period")
                    && !isAuthorizedToEditFiscalPeriod(document)) {
                section.setReadOnly(true);
            }
        }
        return sections;
    }

    private boolean isAuthorizedToEditFiscalPeriod(final Document document) {
        final Person user = GlobalVariables.getUserSession().getPerson();
        return getDocumentDictionaryService().getDocumentAuthorizer(document)
                .isAuthorized(document, KFSConstants.CoreModuleNamespaces.KFS,
                        KFSConstants.YEAR_END_ACCOUNTING_PERIOD_EDIT_PERMISSION, user.getPrincipalId());
    }

    @Override
    protected void processGlobalsAfterRetrieve() {
        super.processGlobalsAfterRetrieve();

        final AssetRetirementGlobal assetRetirementGlobal = (AssetRetirementGlobal) getBusinessObject();
        final List<AssetRetirementGlobalDetail> assetRetirementGlobalDetails =
                assetRetirementGlobal.getAssetRetirementGlobalDetails();

        for (final AssetRetirementGlobalDetail assetRetirementGlobalDetail : assetRetirementGlobalDetails) {
            // Set non-persistent values. So the screen can show them after submit.
            getAssetService().setAssetSummaryFields(assetRetirementGlobalDetail.getAsset());
        }
    }

    @Override
    public void addMultipleValueLookupResults(
            final MaintenanceDocument document, final String collectionName,
            final Collection<PersistableBusinessObject> rawValues, final boolean needsBlank, final PersistableBusinessObject bo) {
        final AssetRetirementGlobal assetRetirementGlobal = (AssetRetirementGlobal) document.getDocumentBusinessObject();

        final int nElements = assetRetirementGlobal.getAssetRetirementGlobalDetails().size() + rawValues.size();
        if (!getAssetService().isDocumentEnrouting(document)
                && !getAssetRetirementService().isAllowedRetireMultipleAssets(document)
                && nElements > 1) {
            GlobalVariables.getMessageMap().putErrorForSectionId(
                    CamsConstants.AssetRetirementGlobal.SECTION_ID_ASSET_DETAIL_INFORMATION,
                    CamsKeyConstants.Retirement.ERROR_MULTIPLE_ASSET_RETIRED);
        } else {
            GlobalVariables.getMessageMap().clearErrorMessages();
            // Adding the selected asset.
            super.addMultipleValueLookupResults(document, collectionName, rawValues, needsBlank, bo);
        }
    }

    @Override
    public void refresh(final String refreshCaller, final Map fieldValues, final MaintenanceDocument document) {
        super.refresh(refreshCaller, fieldValues, document);
        final AssetRetirementGlobal assetRetirementGlobal = (AssetRetirementGlobal) document.getDocumentBusinessObject();
        final List<AssetRetirementGlobalDetail> assetRetirementGlobalDetails =
                assetRetirementGlobal.getAssetRetirementGlobalDetails();

        if (KFSConstants.MULTIPLE_VALUE.equalsIgnoreCase(refreshCaller)) {
            // Set non-persistent values in multiple lookup result collection. So the screen can show them when return
            // from multiple lookup.
            for (final AssetRetirementGlobalDetail assetRetirementGlobalDetail : assetRetirementGlobalDetails) {
                getAssetService().setAssetSummaryFields(assetRetirementGlobalDetail.getAsset());
            }
        } else if (CamsConstants.AssetRetirementGlobal.ASSET_LOOKUPABLE_ID.equalsIgnoreCase(refreshCaller)) {
            // Set non-persistent values in the result from asset lookup. So the screen can show them when return from
            // single asset lookup.
            final String referencesToRefresh = (String) fieldValues.get(KRADConstants.REFERENCES_TO_REFRESH);
            if (getAssetRetirementService().isAssetRetiredByMerged(assetRetirementGlobal)
                    && CamsPropertyConstants.AssetRetirementGlobal.MERGED_TARGET_CAPITAL_ASSET.equals(
                            referencesToRefresh)) {
                assetRetirementGlobal.setMergedTargetCapitalAssetDescription(
                        assetRetirementGlobal.getMergedTargetCapitalAsset().getCapitalAssetDescription());
            }
            final AssetRetirementGlobalDetail newDetail = (AssetRetirementGlobalDetail) newCollectionLines.get(
                    CamsPropertyConstants.AssetRetirementGlobal.ASSET_RETIREMENT_GLOBAL_DETAILS);
            getAssetService().setAssetSummaryFields(newDetail.getAsset());
        }
    }

    @Override
    public void doRouteStatusChange(final DocumentHeader documentHeader) {
        super.doRouteStatusChange(documentHeader);
        final AssetRetirementGlobal assetRetirementGlobal = (AssetRetirementGlobal) getBusinessObject();

        if (documentHeader.getWorkflowDocument().isEnroute()) {
            // display a message for asset not generating ledger entries when it is federally owned or non-capital
            boolean allPaymentsFederalOwnedOrNonCapital = true;
            final List<AssetRetirementGlobalDetail> assetRetirementGlobalDetails =
                    assetRetirementGlobal.getAssetRetirementGlobalDetails();
            for (final AssetRetirementGlobalDetail assetRetirementGlobalDetail : assetRetirementGlobalDetails) {
                final Asset asset = assetRetirementGlobalDetail.getAsset();
                if (getAssetService().isCapitalAsset(asset)) {
                    for (final AssetPayment assetPayment : asset.getAssetPayments()) {
                        if (!getAssetPaymentService().isPaymentFederalOwned(assetPayment)) {
                            allPaymentsFederalOwnedOrNonCapital = false;
                        }
                    }
                }
            }

            if (allPaymentsFederalOwnedOrNonCapital) {
                KNSGlobalVariables.getMessageList().add(
                        CamsKeyConstants.Retirement.MESSAGE_NO_LEDGER_ENTRY_REQUIRED_RETIREMENT);
            }

        }
        // all approvals have been processed, the retirement date is set to the approval date
        if (documentHeader.getWorkflowDocument().isProcessed()) {
            assetRetirementGlobal.setRetirementDate(getDateTimeService().getCurrentSqlDate());
            getBusinessObjectService().save(assetRetirementGlobal);

            if (getAssetRetirementService().isAssetRetiredByMerged(assetRetirementGlobal)) {
                assetRetirementGlobal.getMergedTargetCapitalAsset().setCapitalAssetDescription(
                        assetRetirementGlobal.getMergedTargetCapitalAssetDescription());
                getBusinessObjectService().save(assetRetirementGlobal.getMergedTargetCapitalAsset());
            }

        }
        new AssetRetirementGeneralLedgerPendingEntrySource(documentHeader)
                .doRouteStatusChange(assetRetirementGlobal.getGeneralLedgerPendingEntries());

        // release the lock when document status changed as following...
        final WorkflowDocument workflowDoc = documentHeader.getWorkflowDocument();
        if (workflowDoc.isCanceled() || workflowDoc.isDisapproved() || workflowDoc.isProcessed() || workflowDoc.isFinal()) {
            getCapitalAssetManagementService().deleteAssetLocks(getDocumentNumber(), null);
        }
    }

    @Override
    public void addNewLineToCollection(final String collectionName) {
        super.addNewLineToCollection(collectionName);

        final AssetRetirementGlobal assetRetirementGlobal = (AssetRetirementGlobal) getBusinessObject();
        if (StringUtils.isBlank(assetRetirementGlobal.getMergedTargetCapitalAssetDescription())
                && ObjectUtils.isNotNull(assetRetirementGlobal.getMergedTargetCapitalAssetNumber())) {
            assetRetirementGlobal.setMergedTargetCapitalAssetDescription(
                    assetRetirementGlobal.getMergedTargetCapitalAsset().getCapitalAssetDescription());
        }
    }

    @Override
    public Class<? extends PersistableBusinessObject> getPrimaryEditedBusinessObjectClass() {
        return Asset.class;
    }

    @Override
    public Map<String, String> populateNewCollectionLines(
            final Map<String, String> fieldValues,
            final MaintenanceDocument maintenanceDocument, final String methodToCall) {
        final String capitalAssetNumber = fieldValues.get(CamsPropertyConstants.AssetRetirementGlobal.CAPITAL_ASSET_NUMBER);

        if (StringUtils.isNotBlank(capitalAssetNumber)) {
            fieldValues.remove(CamsPropertyConstants.AssetRetirementGlobal.CAPITAL_ASSET_NUMBER);
            fieldValues.put(CamsPropertyConstants.AssetRetirementGlobal.CAPITAL_ASSET_NUMBER, capitalAssetNumber.trim());
        }
        return super.populateNewCollectionLines(fieldValues, maintenanceDocument, methodToCall);

    }

    /**
     * Checks for Accounting Period 13
     *
     * @param assetRetirementGlobal
     * @return true if the accountingPeriod in assetRetirementGlobal is 13.
     * TODO Remove hardcoding
     */
    private boolean isPeriod13(final AssetRetirementGlobal assetRetirementGlobal) {
        if (ObjectUtils.isNull(assetRetirementGlobal.getAccountingPeriod())) {
            return false;
        }
        return "13".equals(assetRetirementGlobal.getAccountingPeriod().getUniversityFiscalPeriodCode());
    }

    private AssetPaymentService getAssetPaymentService() {
        if (assetPaymentService == null) {
            assetPaymentService = SpringContext.getBean(AssetPaymentService.class);
        }
        return assetPaymentService;
    }

    private AssetRetirementService getAssetRetirementService() {
        if (assetRetirementService == null) {
            assetRetirementService = SpringContext.getBean(AssetRetirementService.class);
        }
        return assetRetirementService;
    }

    private AssetService getAssetService() {
        if (assetService == null) {
            assetService = SpringContext.getBean(AssetService.class);
        }
        return assetService;
    }

    protected CapitalAssetManagementService getCapitalAssetManagementService() {
        if (capitalAssetManagementService == null) {
            capitalAssetManagementService = SpringContext.getBean(CapitalAssetManagementService.class);
        }
        return capitalAssetManagementService;
    }

    private DateTimeService getDateTimeService() {
        if (dateTimeService == null) {
            dateTimeService = SpringContext.getBean(DateTimeService.class);
        }
        return dateTimeService;
    }

    protected DocumentService getDocumentService() {
        if (documentService == null) {
            documentService = SpringContext.getBean(DocumentService.class);
        }
        return documentService;
    }

    private FinancialSystemDocumentTypeService getFinancialSystemDocumentTypeService() {
        if (financialSystemDocumentTypeService == null) {
            financialSystemDocumentTypeService = SpringContext.getBean(FinancialSystemDocumentTypeService.class);
        }
        return financialSystemDocumentTypeService;
    }
}
