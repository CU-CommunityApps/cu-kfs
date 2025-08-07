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
package org.kuali.kfs.module.cam.document.service.impl;

import edu.cornell.kfs.fp.businessobject.CapitalAssetInformationDetailExtendedAttribute;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.service.ObjectTypeService;
import org.kuali.kfs.core.api.parameter.ParameterEvaluator;
import org.kuali.kfs.core.api.parameter.ParameterEvaluatorService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.fp.businessobject.CapitalAccountingLines;
import org.kuali.kfs.fp.businessobject.CapitalAssetAccountsGroupDetails;
import org.kuali.kfs.fp.businessobject.CapitalAssetInformation;
import org.kuali.kfs.fp.businessobject.CapitalAssetInformationDetail;
import org.kuali.kfs.fp.document.dataaccess.CapitalAssetInformationDao;
import org.kuali.kfs.gl.GLParameterConstants;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentHeaderService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cam.CamsConstants;
import org.kuali.kfs.module.cam.CamsConstants.DocumentTypeName;
import org.kuali.kfs.module.cam.CamsPropertyConstants;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.module.cam.businessobject.AssetGlobal;
import org.kuali.kfs.module.cam.businessobject.AssetGlobalDetail;
import org.kuali.kfs.module.cam.businessobject.AssetPaymentAssetDetail;
import org.kuali.kfs.module.cam.businessobject.AssetPaymentDetail;
import org.kuali.kfs.module.cam.businessobject.GeneralLedgerEntry;
import org.kuali.kfs.module.cam.businessobject.GeneralLedgerEntryAsset;
import org.kuali.kfs.module.cam.businessobject.defaultvalue.NextAssetNumberFinder;
import org.kuali.kfs.module.cam.document.AssetPaymentDocument;
import org.kuali.kfs.module.cam.document.service.AssetGlobalService;
import org.kuali.kfs.module.cam.document.service.GlLineService;
import org.kuali.kfs.module.cam.util.ObjectValueUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlLineServiceImpl implements GlLineService {
	private static final Logger LOG = LogManager.getLogger(GlLineServiceImpl.class);
    private static final String CAB_DESC_PREFIX = "CAB created for FP ";

    protected BusinessObjectService businessObjectService;
    protected AssetGlobalService assetGlobalService;
    protected ObjectTypeService objectTypeService;
    protected DocumentService documentService;
    protected ParameterService parameterService;
    protected ParameterEvaluatorService parameterEvaluatorService;
    protected DocumentHeaderService documentHeaderService;
    protected CapitalAssetInformationDao capitalAssetInformationDao;

    @Override
    public Document createAssetGlobalDocument(final GeneralLedgerEntry primary, final Integer capitalAssetLineNumber) {
        final MaintenanceDocument document = (MaintenanceDocument) documentService.getNewDocument(
                DocumentTypeName.ASSET_ADD_GLOBAL);

        final AssetGlobal assetGlobal = createAssetGlobal(primary, document);
        assetGlobal.setCapitalAssetBuilderOriginIndicator(true);
        assetGlobal.setAcquisitionTypeCode(assetGlobalService.getNewAcquisitionTypeCode());

        updatePreTagInformation(primary, document, assetGlobal, capitalAssetLineNumber);

        assetGlobal.getAssetPaymentDetails().addAll(createAssetPaymentDetails(primary, document, 0,
                capitalAssetLineNumber));

        // save the document
        document.getNewMaintainableObject().setMaintenanceAction(KRADConstants.MAINTENANCE_NEW_ACTION);
        document.getDocumentHeader().setDocumentDescription(CAB_DESC_PREFIX + primary.getDocumentNumber());
        document.getNewMaintainableObject().setBusinessObject(assetGlobal);
        document.getNewMaintainableObject().setDataObjectClass(assetGlobal.getClass());

        documentService.saveDocument(document);

        //mark the capital asset as processed..
        markCapitalAssetProcessed(primary, capitalAssetLineNumber);

        deactivateGLEntries(primary, document, capitalAssetLineNumber);

        return document;
    }

    protected void markCapitalAssetProcessed(final GeneralLedgerEntry primary, final Integer capitalAssetLineNumber) {
        final CapitalAssetInformation capitalAssetInformation = findCapitalAssetInformation(primary.getDocumentNumber(),
                capitalAssetLineNumber);
        //if it is create asset...
        if (ObjectUtils.isNotNull(capitalAssetInformation)) {
            capitalAssetInformation.setCapitalAssetProcessedIndicator(true);
            businessObjectService.save(capitalAssetInformation);
        }
    }

    protected void deactivateGLEntries(final GeneralLedgerEntry entry, final Document document, final Integer capitalAssetLineNumber) {
        //now deactivate the gl line..
        final CapitalAssetInformation capitalAssetInformation = findCapitalAssetInformation(entry.getDocumentNumber(),
                capitalAssetLineNumber);

        if (ObjectUtils.isNotNull(capitalAssetInformation)) {
            final List<CapitalAssetAccountsGroupDetails> groupAccountingLines =
                    capitalAssetInformation.getCapitalAssetAccountsGroupDetails();
            final Collection<GeneralLedgerEntry> documentGlEntries = findAllGeneralLedgerEntry(entry.getDocumentNumber());
            for (final CapitalAssetAccountsGroupDetails accountingLine : groupAccountingLines) {
                //find the matching GL entry for this accounting line.
                final Collection<GeneralLedgerEntry> glEntries = findMatchingGeneralLedgerEntries(documentGlEntries,
                        accountingLine);
                for (final GeneralLedgerEntry glEntry : glEntries) {
                    final KualiDecimal lineAmount = accountingLine.getAmount();

                    //update submitted amount on the gl entry and save the results.
                    createGeneralLedgerEntryAsset(glEntry, document, capitalAssetLineNumber);
                    updateTransactionSumbitGlEntryAmount(glEntry, lineAmount);
                }
            }
        }
    }

    /**
     * This method reads the pre-tag information and creates objects for asset global document
     *
     * @param entry       GL Line
     * @param document    Asset Global Maintenance Document
     * @param assetGlobal Asset Global Object
     */
    protected void updatePreTagInformation(
            final GeneralLedgerEntry entry, final MaintenanceDocument document,
            final AssetGlobal assetGlobal, final Integer capitalAssetLineNumber) {
        final CapitalAssetInformation capitalAssetInformation = findCapitalAssetInformation(entry.getDocumentNumber(),
                capitalAssetLineNumber);
        //if it is create asset...
        if (ObjectUtils.isNotNull(capitalAssetInformation)) {
            if (KFSConstants.CapitalAssets.CAPITAL_ASSET_CREATE_ACTION_INDICATOR.equals(
                    capitalAssetInformation.getCapitalAssetActionIndicator())) {
                final List<CapitalAssetInformationDetail> capitalAssetInformationDetails =
                        capitalAssetInformation.getCapitalAssetInformationDetails();
                for (final CapitalAssetInformationDetail capitalAssetInformationDetail : capitalAssetInformationDetails) {
                    // This is not added to constructor in CAMS to provide module isolation from CAMS
                    final AssetGlobalDetail assetGlobalDetail = new AssetGlobalDetail();
                    assetGlobalDetail.setDocumentNumber(document.getDocumentNumber());
                    assetGlobalDetail.setCampusCode(capitalAssetInformationDetail.getCampusCode());
                    assetGlobalDetail.setBuildingCode(capitalAssetInformationDetail.getBuildingCode());
                    assetGlobalDetail.setBuildingRoomNumber(capitalAssetInformationDetail.getBuildingRoomNumber());
                    assetGlobalDetail.setBuildingSubRoomNumber(capitalAssetInformationDetail.getBuildingSubRoomNumber());
                    assetGlobalDetail.setSerialNumber(capitalAssetInformationDetail.getCapitalAssetSerialNumber());
                    assetGlobalDetail.setCapitalAssetNumber(NextAssetNumberFinder.getLongValue());
                    assetGlobalDetail.setCampusTagNumber(capitalAssetInformationDetail.getCapitalAssetTagNumber());
                    
                    //KFSPTS-3597 add off campus information
                    CapitalAssetInformationDetailExtendedAttribute extendedAttribute = (CapitalAssetInformationDetailExtendedAttribute)capitalAssetInformationDetail.getExtension();
                    assetGlobalDetail.setOffCampusAddress(extendedAttribute.getAssetLocationStreetAddress());
                    assetGlobalDetail.setOffCampusCityName(extendedAttribute.getAssetLocationCityName());
                    assetGlobalDetail.setOffCampusCountryCode(extendedAttribute.getAssetLocationCountryCode());
                    assetGlobalDetail.setOffCampusStateCode(extendedAttribute.getAssetLocationStateCode());
                    assetGlobalDetail.setOffCampusZipCode(extendedAttribute.getAssetLocationZipCode());
                    
                    final AssetGlobalDetail uniqueAsset = new AssetGlobalDetail();
                    ObjectValueUtils.copySimpleProperties(assetGlobalDetail, uniqueAsset);
                    assetGlobalDetail.getAssetGlobalUniqueDetails().add(uniqueAsset);
                    assetGlobal.getAssetSharedDetails().add(assetGlobalDetail);
                }

                assetGlobal.setVendorName(capitalAssetInformation.getVendorName());
                assetGlobal.setInventoryStatusCode(CamsConstants.InventoryStatusCode.CAPITAL_ASSET_ACTIVE_IDENTIFIABLE);
                assetGlobal.setCapitalAssetTypeCode(capitalAssetInformation.getCapitalAssetTypeCode());
                assetGlobal.setManufacturerName(capitalAssetInformation.getCapitalAssetManufacturerName());
                assetGlobal.setManufacturerModelNumber(capitalAssetInformation.getCapitalAssetManufacturerModelNumber());
                assetGlobal.setCapitalAssetDescription(capitalAssetInformation.getCapitalAssetDescription());
            }
        }
    }

    /**
     * Updates pre tag information received from FP document
     *
     * @param entry    GeneralLedgerEntry
     * @param document AssetPaymentDocument
     */
    protected void updatePreTagInformation(
            final GeneralLedgerEntry entry, final AssetPaymentDocument document,
            final Integer capitalAssetLineNumber) {
        final CapitalAssetInformation capitalAssetInformation = findCapitalAssetInformation(entry.getDocumentNumber(),
                capitalAssetLineNumber);
        if (ObjectUtils.isNotNull(capitalAssetInformation)) {
            //if it is modify asset...
            if (KFSConstants.CapitalAssets.CAPITAL_ASSET_MODIFY_ACTION_INDICATOR.equals(
                    capitalAssetInformation.getCapitalAssetActionIndicator())) {
                final AssetPaymentAssetDetail assetPaymentAssetDetail = new AssetPaymentAssetDetail();
                assetPaymentAssetDetail.setDocumentNumber(document.getDocumentNumber());
                // get the allocated amount for the capital asset....
                assetPaymentAssetDetail.setCapitalAssetNumber(capitalAssetInformation.getCapitalAssetNumber());
                assetPaymentAssetDetail.setAllocatedAmount(KualiDecimal.ZERO);
                assetPaymentAssetDetail.setAllocatedUserValue(assetPaymentAssetDetail.getAllocatedAmount());
                assetPaymentAssetDetail.refreshReferenceObject(CamsPropertyConstants.AssetPaymentAssetDetail.ASSET);
                final Asset asset = assetPaymentAssetDetail.getAsset();
                if (ObjectUtils.isNotNull(asset)) {
                    assetPaymentAssetDetail.setPreviousTotalCostAmount(asset.getTotalCostAmount() != null ?
                            asset.getTotalCostAmount() : KualiDecimal.ZERO);
                    document.getAssetPaymentAssetDetail().add(assetPaymentAssetDetail);
                }
            }
        }
    }

    @Override
    public CapitalAssetInformation findCapitalAssetInformation(final String documentNumber, final Integer capitalAssetLineNumber) {
        final Map<String, String> primaryKeys = new HashMap<>(2);
        primaryKeys.put(CamsPropertyConstants.CapitalAssetInformation.DOCUMENT_NUMBER, documentNumber);
        primaryKeys.put(CamsPropertyConstants.CapitalAssetInformation.ASSET_LINE_NUMBER,
                capitalAssetLineNumber.toString());

        return businessObjectService.findByPrimaryKey(CapitalAssetInformation.class, primaryKeys);
    }

    @Override
    public List<CapitalAssetInformation> findAllCapitalAssetInformation(final String documentNumber) {
        final Map<String, String> primaryKeys = new HashMap<>(1);
        primaryKeys.put(CamsPropertyConstants.CapitalAssetInformation.DOCUMENT_NUMBER, documentNumber);
        return (List<CapitalAssetInformation>) businessObjectService.findMatchingOrderBy(
                CapitalAssetInformation.class, primaryKeys,
                CamsPropertyConstants.CapitalAssetInformation.ACTION_INDICATOR, true);
    }

    @Override
    public List<CapitalAssetInformation> findCapitalAssetInformationForGLLine(final GeneralLedgerEntry entry) {
        final Map<String, String> fields = new HashMap<>();
        fields.put(CamsPropertyConstants.CapitalAssetInformation.DOCUMENT_NUMBER, entry.getDocumentNumber());
        fields.put(CamsPropertyConstants.CapitalAssetInformation.ASSET_PROCESSED_IND, "N");

        final List<CapitalAssetInformation> assetInformation = (List<CapitalAssetInformation>) businessObjectService
                .findMatchingOrderBy(CapitalAssetInformation.class, fields,
                        CamsPropertyConstants.CapitalAssetInformation.ACTION_INDICATOR, true);

        final List<CapitalAssetInformation> matchingAssets = new ArrayList<>();

        for (final CapitalAssetInformation capitalAsset : assetInformation) {
            addToCapitalAssets(matchingAssets, capitalAsset, entry);
        }

        return matchingAssets;
    }


    /**
     * Finds capital asset information for a given GL entry by matching also on the accounting line type.
     *
     * @param entry
     * @return matching capital asset info
     */
    private List<CapitalAssetInformation> findCapitalAssetInformationForGLLineMatchLineType(GeneralLedgerEntry entry) {
        Map<String, String> primaryKeys = new HashMap<String, String>();
        primaryKeys.put(CamsPropertyConstants.CapitalAssetInformation.DOCUMENT_NUMBER, entry.getDocumentNumber());

        List<CapitalAssetInformation> assetInformation = (List<CapitalAssetInformation>) businessObjectService.findMatchingOrderBy(CapitalAssetInformation.class, primaryKeys, CamsPropertyConstants.CapitalAssetInformation.ACTION_INDICATOR, true);

        List<CapitalAssetInformation> matchingAssets = new ArrayList<CapitalAssetInformation>();

        for (CapitalAssetInformation capitalAsset : assetInformation) {
        	addToCapitalAssetsMatchingLineType(matchingAssets, capitalAsset, entry);
        }

        return matchingAssets;
    }


    /**
     * Compares the gl line to the group accounting lines in each capital asset and
     * when finds a match, adds the capital asset to the list of matching assets. It also checks the accounting line type against the GL line credit/debit code.
     *
     * @param matchingAssets
     * @param capitalAsset
     * @param entry
     */
    protected void addToCapitalAssetsMatchingLineType(final List<CapitalAssetInformation> matchingAssets,
            final CapitalAssetInformation capitalAsset, final GeneralLedgerEntry entry) {
        final List<CapitalAssetAccountsGroupDetails> groupAccountLines = capitalAsset.getCapitalAssetAccountsGroupDetails();

        for (CapitalAssetAccountsGroupDetails groupAccountLine : groupAccountLines) {
            if (groupAccountLine.getDocumentNumber().equals(entry.getDocumentNumber())
                    && groupAccountLine.getChartOfAccountsCode().equals(entry.getChartOfAccountsCode())
                    && groupAccountLine.getAccountNumber().equals(entry.getAccountNumber())
                    && groupAccountLine.getFinancialObjectCode().equals(entry.getFinancialObjectCode())) {
                // check that debit matches target acct lines and credit matches
                // source accounting lines
                final boolean isGroupLineSource = StringUtils.equals(groupAccountLine.getFinancialDocumentLineTypeCode(), KFSConstants.SOURCE_ACCT_LINE_TYPE_CODE);
                final boolean isGroupLineTarget = StringUtils.equals(groupAccountLine.getFinancialDocumentLineTypeCode(), KFSConstants.TARGET_ACCT_LINE_TYPE_CODE);
                final boolean isGLEntryCredit = StringUtils.equals(entry.getTransactionDebitCreditCode(), KFSConstants.GL_CREDIT_CODE);
                final boolean isGLEntryDebit = StringUtils.equals(entry.getTransactionDebitCreditCode(), KFSConstants.GL_DEBIT_CODE);
                final boolean isErrorCorrection = groupAccountLine.getAmount().isNegative();
                logAddToCapitalAssetsMatchingLineTypeDetails(isGroupLineSource, isGroupLineTarget, isGLEntryCredit, isGLEntryDebit, isErrorCorrection, 
                        groupAccountLine, entry);
                if ((isGroupLineSource && isGLEntryCredit) || (isGroupLineTarget && isGLEntryDebit) || 
                        (isErrorCorrection && isGroupLineSource && isGLEntryDebit) ||
                        (isErrorCorrection && isGroupLineTarget && isGLEntryCredit)) {
                    matchingAssets.add(capitalAsset);
                    break;
                }

            }
        }
    }
    
    private void logAddToCapitalAssetsMatchingLineTypeDetails(boolean isGroupLineSource, boolean isGroupLineTarget, boolean isGLEntryCredit, 
            boolean isGLEntryDebit, boolean isErrorCorrection, CapitalAssetAccountsGroupDetails groupAccountLine, GeneralLedgerEntry entry) {
        if (LOG.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("logAddToCapitalAssetsMatchingLineTypeDetails, ");
            sb.append(" isGroupLineSource: ").append(isGroupLineSource).append(" isGroupLineTarget: ").append(isGroupLineTarget);
            sb.append(" isGLEntryCredit: ").append(isGLEntryCredit).append(" isGLEntryDebit: ").append(isGLEntryDebit);
            sb.append(" isErrorCorrection: ").append(isErrorCorrection);
            LOG.debug(sb.toString());
            LOG.debug("logAddToCapitalAssetsMatchingLineTypeDetails, groupAccountLine: " + groupAccountLine);
            LOG.debug("logAddToCapitalAssetsMatchingLineTypeDetails, entry: " + entry);
        }
    }

    /**
     * Compares the gl line to the group accounting lines in each capital asset and
     * when finds a match, adds the capital asset to the list of matching assets
     *
     * @param matchingAssets
     * @param capitalAsset
     * @param entry
     */
    protected void addToCapitalAssets(
            final List<CapitalAssetInformation> matchingAssets,
            final CapitalAssetInformation capitalAsset, final GeneralLedgerEntry entry) {
        final List<CapitalAssetAccountsGroupDetails> groupAccountLines = capitalAsset.getCapitalAssetAccountsGroupDetails();

        for (final CapitalAssetAccountsGroupDetails groupAccountLine : groupAccountLines) {
            // Unfortunately, when no organization reference id is input, the General Ledger Entry's
            // organizationReferenceId is set to "" while the groupAccountLine's organizationReferenceId is set to
            // null. These are equivalent for the present concerns.
            final boolean isOrganizationReferenceIdEqual;
            if (StringUtils.equals(groupAccountLine.getOrganizationReferenceId(), entry.getOrganizationReferenceId())) {
                isOrganizationReferenceIdEqual = true;
            } else {
                isOrganizationReferenceIdEqual = StringUtils.isBlank(groupAccountLine.getOrganizationReferenceId())
                        && StringUtils.isBlank(entry.getOrganizationReferenceId());
            }
            if (isOrganizationReferenceIdEqual
                    && StringUtils.equals(groupAccountLine.getDocumentNumber(), entry.getDocumentNumber())
                    && StringUtils.equals(groupAccountLine.getChartOfAccountsCode(), entry.getChartOfAccountsCode())
                    && StringUtils.equals(groupAccountLine.getAccountNumber(), entry.getAccountNumber())
                    && StringUtils.equals(groupAccountLine.getFinancialObjectCode(), entry.getFinancialObjectCode())) {
                matchingAssets.add(capitalAsset);
                break;
            }
        }
    }

    @Override
    public long findUnprocessedCapitalAssetInformation(final String documentNumber) {
        final Map<String, String> fieldValues = new HashMap<>(2);
        fieldValues.put(CamsPropertyConstants.CapitalAssetInformation.DOCUMENT_NUMBER, documentNumber);
        fieldValues.put(CamsPropertyConstants.CapitalAssetInformation.ASSET_PROCESSED_IND,
                KFSConstants.CapitalAssets.CAPITAL_ASSET_PROCESSED_IND);

        return businessObjectService.countMatching(CapitalAssetInformation.class, fieldValues);
    }

    @Override
    public Collection<GeneralLedgerEntry> findMatchingGeneralLedgerEntries(
            final Collection<GeneralLedgerEntry> allGLEntries,
            final CapitalAssetAccountsGroupDetails accountingDetails) {
        final Collection<GeneralLedgerEntry> matchingGLEntries = new ArrayList<>();

        for (final GeneralLedgerEntry entry : allGLEntries) {
            if (doesGeneralLedgerEntryMatchAssetAccountingDetails(entry, accountingDetails)) {
                matchingGLEntries.add(entry);
            }
        }

        return matchingGLEntries;
    }

    protected boolean doesGeneralLedgerEntryMatchAssetAccountingDetails(
            final GeneralLedgerEntry entry,
            final CapitalAssetAccountsGroupDetails accountingDetails) {
        // this method will short-circuit and return false as soon as possible

        // sanity check - the arguments should already have the same document number
        if (!StringUtils.equals(entry.getDocumentNumber(), accountingDetails.getDocumentNumber())) {
            return false;
        }

        // required attributes - easy to compare
        if (!StringUtils.equals(entry.getAccountNumber(), accountingDetails.getAccountNumber())) {
            return false;
        }
        if (!StringUtils.equals(entry.getFinancialObjectCode(), accountingDetails.getFinancialObjectCode())) {
            return false;
        }
        if (!StringUtils.equals(entry.getChartOfAccountsCode(), accountingDetails.getChartOfAccountsCode())) {
            return false;
        }
        // account for blank equaling null
        if (!StringUtils.equals(entry.getOrganizationReferenceId(), accountingDetails.getOrganizationReferenceId())) {
            if (StringUtils.isNotBlank(entry.getOrganizationReferenceId())
                    || StringUtils.isNotBlank(accountingDetails.getOrganizationReferenceId())) {
                return false;
            }
        }
        // optional attributes - need to account for blank being equivalent to dashes
        // it's always dashes on the CAB GL Entry table - but could be blank on the accounting details
        if (!StringUtils.equals(entry.getSubAccountNumber(), accountingDetails.getSubAccountNumber())) {
            if (!StringUtils.equals(entry.getSubAccountNumber(), KFSConstants.getDashSubAccountNumber())
                    || StringUtils.isNotBlank(accountingDetails.getSubAccountNumber())) {
                return false;
            }
        }
        if (!StringUtils.equals(entry.getFinancialSubObjectCode(), accountingDetails.getFinancialSubObjectCode())) {
            if (!StringUtils.equals(entry.getFinancialSubObjectCode(), KFSConstants.getDashFinancialSubObjectCode())
                    || StringUtils.isNotBlank(accountingDetails.getFinancialSubObjectCode())) {
                return false;
            }
        }
        if (!StringUtils.equals(entry.getProjectCode(), accountingDetails.getProjectCode())) {
            return StringUtils.equals(entry.getProjectCode(), KFSConstants.getDashProjectCode())
                    && StringUtils.isBlank(accountingDetails.getProjectCode());
        }
        
        //check if GL Debit matches acct line type target and Credit matches Source
        boolean isGlEntryDebit = StringUtils.equals(entry.getTransactionDebitCreditCode(), KFSConstants.GL_DEBIT_CODE);
        boolean isGlEntryCredit = StringUtils.equals(entry.getTransactionDebitCreditCode(), KFSConstants.GL_CREDIT_CODE);
        boolean isAccountingDetailTarget = StringUtils.equals(accountingDetails.getFinancialDocumentLineTypeCode(), KFSConstants.TARGET_ACCT_LINE_TYPE_CODE);
        boolean isAccountingDetailSource = StringUtils.equals(accountingDetails.getFinancialDocumentLineTypeCode(), KFSConstants.SOURCE_ACCT_LINE_TYPE_CODE);
        boolean isErrorCorrection = accountingDetails.getAmount().isNegative();
        logDoesGeneralLedgerEntryMatchAssetAccountingDetails(isGlEntryDebit, isGlEntryCredit, isAccountingDetailTarget, isAccountingDetailSource, 
                isErrorCorrection, entry, accountingDetails);
        if ((isGlEntryDebit && isAccountingDetailTarget) || (isGlEntryCredit && isAccountingDetailSource)
                || (isErrorCorrection && isGlEntryDebit && isAccountingDetailSource)
                || (isErrorCorrection && isGlEntryCredit && isAccountingDetailTarget)) {
            // this is a match, keep going
        } else {
            return false;
        }    

        return true;
    }

    private void logDoesGeneralLedgerEntryMatchAssetAccountingDetails(boolean isGlEntryDebit, boolean isGlEntryCredit,
            boolean isAccountingDetailTarget, boolean isAccountingDetailSource, boolean isErrorCorrection,
            GeneralLedgerEntry entry, CapitalAssetAccountsGroupDetails accountingDetails) {
        if (LOG.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("logDoesGeneralLedgerEntryMatchAssetAccountingDetails, isGlEntryDebit: ");
            sb.append(isGlEntryDebit).append(" isGlEntryCredit: ").append(isGlEntryCredit).append(" isAccountingDetailTarget: ");
            sb.append(isAccountingDetailTarget).append(" isAccountingDetailSource: ").append(isAccountingDetailSource);
            sb.append(" isErrorCorrection: ").append(isErrorCorrection);
            LOG.debug(sb.toString());
            LOG.debug("logDoesGeneralLedgerEntryMatchAssetAccountingDetails, entry: " + entry);
            LOG.debug("logDoesGeneralLedgerEntryMatchAssetAccountingDetails, accountingDetails: " + accountingDetails);
        }
    }

    @Override
    public Collection<GeneralLedgerEntry> findAllGeneralLedgerEntry(final String documentNumber) {
        final Map<String, String> fieldValues = new HashMap<>(1);
        fieldValues.put(CamsPropertyConstants.GeneralLedgerEntry.DOCUMENT_NUMBER, documentNumber);

        return businessObjectService.findMatching(GeneralLedgerEntry.class, fieldValues);
    }

    protected void createGeneralLedgerEntryAsset(
            final GeneralLedgerEntry entry, final Document document,
            final Integer capitalAssetLineNumber) {
        // KFSMI-9645 : check if the document is already referenced to prevent an OJB locking error
        for (final GeneralLedgerEntryAsset glEntryAsset : entry.getGeneralLedgerEntryAssets()) {
            if (glEntryAsset.getCapitalAssetManagementDocumentNumber().equals(document.getDocumentNumber())
                && glEntryAsset.getCapitalAssetBuilderLineNumber().equals(capitalAssetLineNumber)) {
                // an object with this key already exists, abort and don't attempt to add another
                return;
            }
        }
        // If we get here, add a child record with the document number
        final GeneralLedgerEntryAsset entryAsset = new GeneralLedgerEntryAsset();
        entryAsset.setGeneralLedgerAccountIdentifier(entry.getGeneralLedgerAccountIdentifier());
        entryAsset.setCapitalAssetBuilderLineNumber(capitalAssetLineNumber);
        entryAsset.setCapitalAssetManagementDocumentNumber(document.getDocumentNumber());
        entry.getGeneralLedgerEntryAssets().add(entryAsset);
    }

    /**
     * Creates asset global
     *
     * @param entry    GeneralLedgerEntry
     * @param maintDoc MaintenanceDocument
     * @return AssetGlobal
     */
    protected AssetGlobal createAssetGlobal(final GeneralLedgerEntry entry, final MaintenanceDocument maintDoc) {
        final AssetGlobal assetGlobal = new AssetGlobal();
        assetGlobal.setOrganizationOwnerChartOfAccountsCode(entry.getChartOfAccountsCode());
        assetGlobal.setOrganizationOwnerAccountNumber(entry.getAccountNumber());
        assetGlobal.setDocumentNumber(maintDoc.getDocumentNumber());
        assetGlobal.setConditionCode(CamsConstants.Asset.CONDITION_CODE_E);

        //year end changes
        final String docType = DocumentTypeName.ASSET_ADD_GLOBAL;
        final ParameterEvaluator evaluator = parameterEvaluatorService
                .getParameterEvaluator(KFSConstants.CoreModuleNamespaces.KFS,
                        KfsParameterConstants.YEAR_END_ACCOUNTING_PERIOD_PARAMETER_NAMES.DETAIL_PARAMETER_TYPE,
                        KfsParameterConstants.YEAR_END_ACCOUNTING_PERIOD_PARAMETER_NAMES.FISCAL_PERIOD_DOCUMENT_TYPES,
                        docType);
        if (evaluator.evaluationSucceeds()) {
            final Integer closingYear = Integer.valueOf(parameterService
                    .getParameterValueAsString(KfsParameterConstants.GENERAL_LEDGER_BATCH.class,
                            GLParameterConstants.ANNUAL_CLOSING_FISCAL_YEAR));
            if (entry.getUniversityFiscalYear().equals(closingYear + 1)) {
                //default asset global year end accounting period drop down to current period instead of closing
                // period(period 13)
                assetGlobal.setAccountingPeriodCompositeString("");
            }
        }

        return assetGlobal;
    }

    @Override
    public Document createAssetPaymentDocument(final GeneralLedgerEntry primaryGlEntry, final Integer capitalAssetLineNumber) {
        // Find out the GL Entry
        // initiate a new document
        final AssetPaymentDocument document = (AssetPaymentDocument) documentService.getNewDocument(DocumentTypeName.ASSET_PAYMENT);
        document.setCapitalAssetBuilderOriginIndicator(true);

        //populate the capital asset line distribution amount code to the payment document.
        final CapitalAssetInformation capitalAssetInformation = findCapitalAssetInformation(primaryGlEntry.getDocumentNumber(),
                capitalAssetLineNumber);

        if (ObjectUtils.isNotNull(capitalAssetInformation)) {
            // If this was a shell Capital Asset Information record (for example GL Entries from enterprise feed or
            // Vendor Credit Memo) setup asset allocation info accordingly so it can be changed on Asset Payment Document
            if (ObjectUtils.isNull(capitalAssetInformation.getDistributionAmountCode())) {
                document.setAssetPaymentAllocationTypeCode(KFSConstants.CapitalAssets.DISTRIBUTE_COST_EQUALLY_CODE);
                document.setAllocationFromFPDocuments(false);
            } else {
                document.setAssetPaymentAllocationTypeCode(capitalAssetInformation.getDistributionAmountCode());
                document.setAllocationFromFPDocuments(true);
            }
        }

        document.getDocumentHeader().setDocumentDescription(CAB_DESC_PREFIX + primaryGlEntry.getDocumentNumber());
        updatePreTagInformation(primaryGlEntry, document, capitalAssetLineNumber);

        // Asset Payment Detail - sourceAccountingLines on the document....
        document.getSourceAccountingLines().addAll(createAssetPaymentDetails(primaryGlEntry, document, 0,
                capitalAssetLineNumber));

        KualiDecimal assetAmount = KualiDecimal.ZERO;

        final List<SourceAccountingLine> sourceAccountingLines = document.getSourceAccountingLines();
        for (final SourceAccountingLine sourceAccountingLine : sourceAccountingLines) {
            assetAmount = assetAmount.add(sourceAccountingLine.getAmount());
        }

        final List<AssetPaymentAssetDetail> assetPaymentDetails = document.getAssetPaymentAssetDetail();
        for (final AssetPaymentAssetDetail assetPaymentDetail : assetPaymentDetails) {
            assetPaymentDetail.setAllocatedAmount(assetAmount);
        }

        documentService.saveDocument(document);
        markCapitalAssetProcessed(primaryGlEntry, capitalAssetLineNumber);
        deactivateGLEntries(primaryGlEntry, document, capitalAssetLineNumber);

        return document;
    }

    /**
     * Creates asset payment details based on accounting lines distributed
     * for the given capital asset.
     *
     * @param entry
     * @param document
     * @param seqNo
     * @param capitalAssetLineNumber
     * @return List<AssetPaymentDetail>
     */
    protected List<AssetPaymentDetail> createAssetPaymentDetails(GeneralLedgerEntry entry, final Document document,
            final int seqNo, final Integer capitalAssetLineNumber) {
        final List<AssetPaymentDetail> appliedPayments = new ArrayList<>();
        final CapitalAssetInformation capitalAssetInformation = findCapitalAssetInformation(entry.getDocumentNumber(),
                capitalAssetLineNumber);
        final Collection<GeneralLedgerEntry> documentGlEntries = findAllGeneralLedgerEntry(entry.getDocumentNumber());

        if (ObjectUtils.isNotNull(capitalAssetInformation)) {
            final List<CapitalAssetAccountsGroupDetails> groupAccountingLines =
                    capitalAssetInformation.getCapitalAssetAccountsGroupDetails();
            int paymentSequenceNumber = 1;

            for (final CapitalAssetAccountsGroupDetails accountingLine : groupAccountingLines) {
                final AssetPaymentDetail detail = new AssetPaymentDetail();

                // find matching gl entry for asset accounting line
                for (final GeneralLedgerEntry glEntry : documentGlEntries) {
                    if (doesGeneralLedgerEntryMatchAssetAccountingDetails(glEntry, accountingLine)) {
                        entry = glEntry;
                    }
                }

                // TODO sub-object code, as well as sub-account, project code, and org ref id, shall not be populated
                // from GL entry; instead, they need to be passed from the original FP document for each individual
                // accounting line to be stored in CapitalAssetAccountsGroupDetails, and copied into each
                // corresponding accounting line in Asset Payment here.

                detail.setDocumentNumber(document.getDocumentNumber());
                detail.setSequenceNumber(paymentSequenceNumber++);
                detail.setPostingYear(entry.getUniversityFiscalYear());
                detail.setPostingPeriodCode(entry.getUniversityFiscalPeriodCode());
                detail.setChartOfAccountsCode(accountingLine.getChartOfAccountsCode());
                detail.setAccountNumber(replaceFiller(accountingLine.getAccountNumber()));
                detail.setSubAccountNumber(replaceFiller(accountingLine.getSubAccountNumber()));
                detail.setFinancialObjectCode(replaceFiller(accountingLine.getFinancialObjectCode()));
                detail.setFinancialSubObjectCode(replaceFiller(accountingLine.getFinancialSubObjectCode()));
                detail.setProjectCode(replaceFiller(accountingLine.getProjectCode()));
                detail.setOrganizationReferenceId(replaceFiller(accountingLine.getOrganizationReferenceId()));
                detail.setAmount(getAccountingLineAmountForPaymentDetail(entry, accountingLine));
                detail.setExpenditureFinancialSystemOriginationCode(replaceFiller(entry.getFinancialSystemOriginationCode()));
                detail.setExpenditureFinancialDocumentNumber(entry.getDocumentNumber());
                detail.setExpenditureFinancialDocumentTypeCode(replaceFiller(entry.getFinancialDocumentTypeCode()));
                detail.setExpenditureFinancialDocumentPostedDate(entry.getTransactionDate());
                detail.setPurchaseOrderNumber(replaceFiller(fetchReferenceFinancialDocumentNumberIfPreqOrCmDocument(entry)));
                detail.setTransferPaymentIndicator(false);

                detail.refreshNonUpdateableReferences();
                appliedPayments.add(detail);
            }
        }

        return appliedPayments;
    }

    protected String fetchReferenceFinancialDocumentNumberIfPreqOrCmDocument(final GeneralLedgerEntry entry) {
        if (PurapConstants.PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT.equals(entry.getFinancialDocumentTypeCode())
                || PurapConstants.PurapDocTypeCodes.CREDIT_MEMO_DOCUMENT.equals(entry.getFinancialDocumentTypeCode())) {
            return entry.getReferenceFinancialDocumentNumber();
        }

        return null;
    }

    /**
     * @param entry          GL entry
     * @param accountingLine accounting line in the capital asset
     * @return accountingLineAmount
     */
    protected KualiDecimal getAccountingLineAmountForPaymentDetail(
            final GeneralLedgerEntry entry,
            final CapitalAssetAccountsGroupDetails accountingLine) {
        final KualiDecimal accountLineAmount = accountingLine.getAmount();

        final List<String> expenseObjectTypes = objectTypeService.getExpenseAndTransferObjectTypesForPayments();
        final List<String> incomeObjectTypes = objectTypeService.getIncomeAndTransferObjectTypesForPayments();

        //we are dealing with error correction document so the from amount line should become positive.
        // KFSUPGRADE-931 : contribution code
        if (isDocumentAnErrorCorrection(entry) || StringUtils.equals(KFSConstants.FinancialDocumentTypeCodes.GENERAL_ERROR_CORRECTION, entry.getFinancialDocumentTypeCode())) {
            if (KFSConstants.SOURCE_ACCT_LINE_TYPE_CODE.equals(accountingLine.getFinancialDocumentLineTypeCode())) {
                return accountLineAmount.negated();
            }

            return accountLineAmount;
        }

        if (expenseObjectTypes.contains(entry.getFinancialObjectTypeCode())
                && KFSConstants.GL_CREDIT_CODE.equals(entry.getTransactionDebitCreditCode())
                && accountLineAmount.compareTo(KualiDecimal.ZERO) > 0) {
            return accountLineAmount.negated();
        }

        if (incomeObjectTypes.contains(entry.getFinancialObjectTypeCode())
                && accountLineAmount.compareTo(KualiDecimal.ZERO) > 0) {
            return accountLineAmount.negated();
        }

        return accountLineAmount;
    }

    /**
     * determines if the document is an error correction document...
     *
     * @param entry
     * @return true if the document is an error correction else false
     */
    protected boolean isDocumentAnErrorCorrection(final GeneralLedgerEntry entry) {
        final DocumentHeader docHeader = documentHeaderService.getDocumentHeaderById(entry.getDocumentNumber());

        return docHeader != null && StringUtils.isNotBlank(docHeader.getFinancialDocumentInErrorNumber());
    }

    /**
     * updates the submit amount by the amount on the accounting line.  When submit amount equals
     * transaction ledger amount, the activity status code is marked as in route status.
     *
     * @param matchingGLEntry
     * @param accountLineAmount
     */
    protected void updateTransactionSumbitGlEntryAmount(
            final GeneralLedgerEntry matchingGLEntry,
            final KualiDecimal accountLineAmount) {
        //update submitted amount on the gl entry and save the results.
        KualiDecimal submitTotalAmount = KualiDecimal.ZERO;
        if (ObjectUtils.isNotNull(matchingGLEntry.getTransactionLedgerSubmitAmount())) {
            submitTotalAmount = matchingGLEntry.getTransactionLedgerSubmitAmount();
        }

        matchingGLEntry.setTransactionLedgerSubmitAmount(submitTotalAmount.add(accountLineAmount.abs()));

        if (matchingGLEntry.getTransactionLedgerSubmitAmount().equals(
                matchingGLEntry.getTransactionLedgerEntryAmount())) {
            matchingGLEntry.setActivityStatusCode(CamsConstants.ActivityStatusCode.ENROUTE);
        }

        //save the updated gl entry in CAB
        businessObjectService.save(matchingGLEntry);
    }

    /**
     * retrieves the amount from the capital asset
     *
     * @param entry
     * @param capitalAssetLineNumber
     * @return capital asset amount.
     */
    protected KualiDecimal getCapitalAssetAmount(final GeneralLedgerEntry entry, final Integer capitalAssetLineNumber) {
        final CapitalAssetInformation capitalAssetInformation = findCapitalAssetInformation(entry.getDocumentNumber(),
                capitalAssetLineNumber);
        if (ObjectUtils.isNotNull(capitalAssetInformation)) {
            return capitalAssetInformation.getCapitalAssetLineAmount();
        }

        return KualiDecimal.ZERO;
    }

    /**
     * If the value contains only the filler characters, then return blank
     *
     * @param val Value
     * @return blank if value if a filler
     */
    protected String replaceFiller(final String val) {
        if (val == null) {
            return "";
        }
        final char[] charArray = val.trim().toCharArray();
        for (final char c : charArray) {
            if (c != '-') {
                return val;
            }
        }
        return "";
    }

    /**
     * Setup shell Capital Asset Information where it doesn't already exist (for example GL Entries
     * from enterprise feed or Vendor Credit Memo)
     *
     * @param entry
     */
    @Override
    @Transactional
    public void setupCapitalAssetInformation(final GeneralLedgerEntry entry) {
        final int nextCapitalAssetLineNumber = capitalAssetInformationDao.getNextCapitalAssetLineNumber(
                entry.getDocumentNumber());

        final List<CapitalAccountingLines> capitalAccountingLines = new ArrayList<>();
        createCapitalAccountingLine(capitalAccountingLines, entry, null);
        createNewCapitalAsset(capitalAccountingLines, entry.getDocumentNumber(), null,
                nextCapitalAssetLineNumber);
    }

    /**
     * Setup shell Capital Asset Information where it doesn't already exist (for
     * example for a PRNC that is in Process and has already generated some
     * capital asset information in a previous processing)
     *
     * @param entry
     */
    @Override
    public void setupMissingCapitalAssetInformation(String documentNumber) {
        List<CapitalAccountingLines> capitalAccountingLines;

        // get all related entries and create capital asset record for each
        Collection<GeneralLedgerEntry> glEntries = findAllGeneralLedgerEntry(documentNumber);
        Collection<CapitalAssetInformation> allCapitalAssetInformation = findAllCapitalAssetInformation(documentNumber);

        int nextCapitalAssetLineNumber = findMaximumCapitalAssetLineNumber(allCapitalAssetInformation) + 1;
        for (GeneralLedgerEntry glEntry : glEntries) {
            // check if it has capital Asset Info
            List<CapitalAssetInformation> entryCapitalAssetInfo = findCapitalAssetInformationForGLLineMatchLineType(glEntry);
            if (entryCapitalAssetInfo.isEmpty()) {
                capitalAccountingLines = new ArrayList<CapitalAccountingLines>();
                createCapitalAccountingLine(capitalAccountingLines, glEntry, null);
                createNewCapitalAsset(capitalAccountingLines, documentNumber, null, nextCapitalAssetLineNumber);
                nextCapitalAssetLineNumber++;
            }
        }

    }
    
    private int findMaximumCapitalAssetLineNumber(Collection<CapitalAssetInformation> allCapitalAssetInformation) {
        int maxLineNumber = 0;
        if (CollectionUtils.isNotEmpty(allCapitalAssetInformation)) {
            for (CapitalAssetInformation info : allCapitalAssetInformation) {
                if (info.getCapitalAssetLineNumber().intValue() > maxLineNumber) {
                    maxLineNumber = info.getCapitalAssetLineNumber().intValue();
                }
            }
        }
        return maxLineNumber;
    }

    protected List<CapitalAccountingLines> createCapitalAccountingLine(
            final List<CapitalAccountingLines> capitalAccountingLines, final GeneralLedgerEntry entry,
            final String distributionAmountCode) {
        //capital object code so we need to build the capital accounting line...
        final CapitalAccountingLines cal = addCapitalAccountingLine(capitalAccountingLines, entry);
        cal.setDistributionAmountCode(distributionAmountCode);
        capitalAccountingLines.add(cal);

        return capitalAccountingLines;
    }

    /**
     * convenience method to add a new capital accounting line to the collection of capital accounting lines.
     *
     * @param capitalAccountingLines
     * @param entry
     * @return
     */
    protected CapitalAccountingLines addCapitalAccountingLine(
            final List<CapitalAccountingLines> capitalAccountingLines,
            final GeneralLedgerEntry entry) {
        final CapitalAccountingLines cal = new CapitalAccountingLines();
        final String capitalAssetLineType =
                KFSConstants.GL_CREDIT_CODE.equals(entry.getTransactionDebitCreditCode()) ? KFSConstants.SOURCE :
                        KFSConstants.TARGET;
        cal.setLineType(capitalAssetLineType);
        cal.setSequenceNumber(entry.getTransactionLedgerEntrySequenceNumber());
        cal.setChartOfAccountsCode(entry.getChartOfAccountsCode());
        cal.setAccountNumber(entry.getAccountNumber());
        cal.setSubAccountNumber(entry.getSubAccountNumber());
        cal.setFinancialObjectCode(entry.getFinancialObjectCode());
        cal.setFinancialSubObjectCode(entry.getFinancialSubObjectCode());
        cal.setProjectCode(entry.getProjectCode());
        cal.setOrganizationReferenceId(entry.getOrganizationReferenceId());
        cal.setFinancialDocumentLineDescription(entry.getTransactionLedgerEntryDescription());
        cal.setAmount(entry.getAmount());
        cal.setAccountLinePercent(null);
        cal.setSelectLine(false);

        return cal;
    }

    /**
     * helper method to add accounting details for this new capital asset record
     *
     * @param capitalAccountingLines
     * @param documentNumber
     * @param actionType
     * @param nextCapitalAssetLineNumber
     */
    protected void createNewCapitalAsset(
            final List<CapitalAccountingLines> capitalAccountingLines, final String documentNumber,
            final String actionType, final Integer nextCapitalAssetLineNumber) {
        final CapitalAssetInformation capitalAsset = new CapitalAssetInformation();
        capitalAsset.setCapitalAssetLineAmount(KualiDecimal.ZERO);
        capitalAsset.setDocumentNumber(documentNumber);
        capitalAsset.setCapitalAssetLineNumber(nextCapitalAssetLineNumber);
        capitalAsset.setCapitalAssetActionIndicator(actionType);
        capitalAsset.setCapitalAssetProcessedIndicator(false);

        KualiDecimal capitalAssetLineAmount = KualiDecimal.ZERO;
        //now setup the account line information associated with this capital asset
        for (final CapitalAccountingLines capitalAccountingLine : capitalAccountingLines) {
            capitalAsset.setDistributionAmountCode(capitalAccountingLine.getDistributionAmountCode());
            createCapitalAssetAccountingLinesDetails(capitalAccountingLine, capitalAsset);
            capitalAssetLineAmount = capitalAssetLineAmount.add(capitalAccountingLine.getAmount());
        }

        capitalAsset.setCapitalAssetLineAmount(capitalAssetLineAmount);

        businessObjectService.save(capitalAsset);
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
        capitalAssetAccountLine.setFinancialDocumentLineTypeCode(
                KFSConstants.SOURCE.equals(capitalAccountingLine.getLineType()) ?
                        KFSConstants.SOURCE_ACCT_LINE_TYPE_CODE : KFSConstants.TARGET_ACCT_LINE_TYPE_CODE);
        capitalAssetAccountLine.setCapitalAssetAccountLineNumber(getNextAccountingLineNumber(capitalAccountingLine,
                capitalAsset));
        capitalAssetAccountLine.setCapitalAssetLineNumber(capitalAsset.getCapitalAssetLineNumber());
        capitalAssetAccountLine.setFinancialObjectCode(capitalAccountingLine.getFinancialObjectCode());
        capitalAssetAccountLine.setFinancialSubObjectCode(capitalAccountingLine.getFinancialSubObjectCode());
        capitalAssetAccountLine.setProjectCode(capitalAccountingLine.getProjectCode());
        capitalAssetAccountLine.setOrganizationReferenceId(capitalAccountingLine.getOrganizationReferenceId());
        capitalAssetAccountLine.setSequenceNumber(capitalAccountingLine.getSequenceNumber());
        capitalAssetAccountLine.setAmount(capitalAccountingLine.getAmount());
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
        final List<CapitalAssetAccountsGroupDetails> capitalAssetAccountLines =
                capitalAsset.getCapitalAssetAccountsGroupDetails();
        for (final CapitalAssetAccountsGroupDetails capitalAssetAccountLine : capitalAssetAccountLines) {
            nextAccountingLineNumber = capitalAssetAccountLine.getCapitalAssetAccountLineNumber();
        }

        return ++nextAccountingLineNumber;
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setAssetGlobalService(final AssetGlobalService assetGlobalService) {
        this.assetGlobalService = assetGlobalService;
    }

    public void setObjectTypeService(final ObjectTypeService objectTypeService) {
        this.objectTypeService = objectTypeService;
    }

    public void setDocumentService(final DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setParameterService(final ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setParameterEvaluatorService(final ParameterEvaluatorService parameterEvaluatorService) {
        this.parameterEvaluatorService = parameterEvaluatorService;
    }

    public void setDocumentHeaderService(final DocumentHeaderService documentHeaderService) {
        this.documentHeaderService = documentHeaderService;
    }

    public void setCapitalAssetInformationDao(final CapitalAssetInformationDao dao) {
        capitalAssetInformationDao = dao;
    }
}
