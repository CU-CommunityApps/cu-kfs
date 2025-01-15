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
package org.kuali.kfs.module.cam.businessobject.actions;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.datadictionary.Action;
import org.kuali.kfs.datadictionary.ActionType;
import org.kuali.kfs.datadictionary.BusinessObjectAdminService;
import org.kuali.kfs.datadictionary.legacy.DocumentDictionaryService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.datadictionary.BusinessObjectEntry;
import org.kuali.kfs.kns.document.authorization.MaintenanceDocumentAuthorizer;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.module.cam.CamsConstants;
import org.kuali.kfs.module.cam.CamsParameterConstants;
import org.kuali.kfs.module.cam.CamsPropertyConstants;
import org.kuali.kfs.module.cam.batch.AssetDepreciationStep;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.module.cam.businessobject.AssetGlobal;
import org.kuali.kfs.module.cam.businessobject.AssetRetirementGlobal;
import org.kuali.kfs.module.cam.document.service.AssetService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.actions.BusinessObjectActionsProvider;
import org.kuali.kfs.sys.document.authorization.FinancialSystemTransactionalDocumentAuthorizerBase;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Map.entry;

public class AssetActionsProvider extends BusinessObjectActionsProvider {
    private AssetService assetService;
    private DocumentDictionaryService documentDictionaryService;
    private ParameterService parameterService;

    @Override
    public List<Action> getActionLinks(final BusinessObjectBase businessObject, final Person user) {
        if (!(businessObject instanceof Asset)) {
            return List.of();
        }
        final List<Action> actions = super.getActionLinks(businessObject, user);
        final Asset asset = (Asset) businessObject;
        if (assetService.isAssetRetired(asset)) {
            actions.clear();
            actions.add(getViewAssetUrl(asset));
        } else {
            // Cornell customization: only add custom links if user has edit permission
            if(allowsEdit(businessObject, user)) {
                addLoanUrls(asset, actions);
                actions.add(getMergeUrl(asset, user));
                actions.add(getSeparateUrl(asset, user));
                actions.add(getTransferUrl(asset, user));
            }
        }

        return actions.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private Action getTransferUrl(final Asset asset, final Person person) {
        if (isNonCapitalAsset(asset)) {
            return getTransferUrlForNonCapitalAsset(asset);
        } else {
            return getTransferUrlForCapitalAsset(asset, person);
        }
    }

    private Action getTransferUrlForCapitalAsset(final Asset asset, final Person person) {
        boolean isAuthorized = true;
        boolean assetMovable = false;
        try {
            assetMovable = assetService.isAssetMovableCheckByPayment(asset);
        } catch (final ValidationException ve) {
            isAuthorized = false;
        }
        if (!assetMovable) {
            final FinancialSystemTransactionalDocumentAuthorizerBase documentAuthorizer =
                    (FinancialSystemTransactionalDocumentAuthorizerBase) documentDictionaryService.getDocumentAuthorizer(
                            CamsConstants.DocumentTypeName.ASSET_TRANSFER);
            isAuthorized = documentAuthorizer.isAuthorized(asset,
                    CamsConstants.CAM_MODULE_CODE,
                    CamsConstants.PermissionNames.SEPARATE,
                    person.getPrincipalId()
            );
        }

        if (isAuthorized) {
            return buildTransferUrl(asset);
        }
        return null;
    }

    private Action buildTransferUrl(final Asset asset) {
        final Map<String, String> parameters = Map.ofEntries(
                entry(KFSConstants.DISPATCH_REQUEST_PARAMETER, KRADConstants.DOC_HANDLER_METHOD),
                entry(
                        CamsPropertyConstants.AssetTransferDocument.CAPITAL_ASSET_NUMBER,
                        asset.getCapitalAssetNumber().toString()
                ),
                entry(KFSConstants.PARAMETER_COMMAND, "initiate"),
                entry(KFSConstants.DOCUMENT_TYPE_NAME, CamsConstants.DocumentTypeName.ASSET_TRANSFER)
        );

        final String url = UrlFactory.parameterizeUrl(CamsConstants.StrutsActions.TRANSFER, parameters);

        return new Action(StringUtils.capitalize(CamsConstants.AssetActions.TRANSFER), "GET", url);
    }

    private boolean isNonCapitalAsset(final Asset asset) {
        final Collection<String> nonDepreciableNonCapitalAssetStatusCodes = parameterService.getParameterValuesAsString(
                AssetDepreciationStep.class,
                CamsParameterConstants.ASSET_STATUSES_EXCLUDED
        );
        return nonDepreciableNonCapitalAssetStatusCodes.contains(asset.getInventoryStatusCode());
    }

    private Action getTransferUrlForNonCapitalAsset(final Asset asset) {
        if (showTransferLinkForNonCapitalAsset(asset)) {
            return buildTransferUrl(asset);
        }
        return null;
    }

    private boolean showTransferLinkForNonCapitalAsset(final Asset asset) {
        final String financialObjectSubTypeCode = assetService.determineFinancialObjectSubTypeCode(asset);

        return showTransferLinkForNonCapitalAssetByPayment(financialObjectSubTypeCode);
    }

    private boolean showTransferLinkForNonCapitalAssetByPayment(final String financialObjectSubTypeCode) {
        if (ObjectUtils.isNull(financialObjectSubTypeCode)) {
            return true;
        }

        return parameterService.getParameterValuesAsString(CamsConstants.CAM_MODULE_CODE,
                        "AssetTransfer",
                        CamsParameterConstants.NON_CAPITAL_SUB_TYPES
                )
                .contains(financialObjectSubTypeCode);
    }

    private Action getSeparateUrl(final Asset asset, final Person person) {
        final MaintenanceDocumentAuthorizer documentAuthorizer =
                (MaintenanceDocumentAuthorizer) documentDictionaryService.getDocumentAuthorizer(CamsConstants.DocumentTypeName.ASSET_ADD_GLOBAL);
        final boolean isAuthorized = documentAuthorizer.isAuthorized(asset,
                CamsConstants.CAM_MODULE_CODE,
                CamsConstants.PermissionNames.SEPARATE,
                person.getPrincipalId()
        );

        if (isAuthorized) {
            final String url =
                    UrlFactory.parameterizeUrl(KFSConstants.MAINTENANCE_ACTION, getSeparateParameters(asset));
            return new Action(StringUtils.capitalize(CamsConstants.AssetActions.SEPARATE), "GET", url);
        }
        return null;
    }

    private Action getMergeUrl(final Asset asset, final Person person) {
        final MaintenanceDocumentAuthorizer documentAuthorizer =
                (MaintenanceDocumentAuthorizer) documentDictionaryService.getDocumentAuthorizer(CamsConstants.DocumentTypeName.ASSET_RETIREMENT_GLOBAL);
        final boolean isAuthorized = documentAuthorizer.isAuthorized(asset,
                CamsConstants.CAM_MODULE_CODE,
                CamsConstants.PermissionNames.MERGE,
                person.getPrincipalId()
        );

        if (isAuthorized) {
            final Map<String, String> parameters = Map.ofEntries(
                    entry(KFSConstants.DISPATCH_REQUEST_PARAMETER, KFSConstants.MAINTENANCE_NEW_WITH_EXISTING_ACTION),
                    entry(KFSConstants.BUSINESS_OBJECT_CLASS_ATTRIBUTE, AssetRetirementGlobal.class.getName()),
                    entry(
                            CamsPropertyConstants.AssetRetirementGlobal.MERGED_TARGET_CAPITAL_ASSET_NUMBER,
                            asset.getCapitalAssetNumber().toString()
                    ),
                    entry(
                            KFSConstants.OVERRIDE_KEYS,
                            CamsPropertyConstants.AssetRetirementGlobal.RETIREMENT_REASON_CODE
                            + KFSConstants.FIELD_CONVERSIONS_SEPERATOR
                            + CamsPropertyConstants.AssetRetirementGlobal.MERGED_TARGET_CAPITAL_ASSET_NUMBER
                    ),
                    entry(
                            CamsPropertyConstants.AssetRetirementGlobal.RETIREMENT_REASON_CODE,
                            CamsConstants.AssetRetirementReasonCode.MERGED
                    ),
                    entry(
                            KFSConstants.REFRESH_CALLER,
                            CamsPropertyConstants.AssetRetirementGlobal.RETIREMENT_REASON_CODE + "::"
                            + CamsConstants.AssetRetirementReasonCode.MERGED
                    )
            );

            final String url = UrlFactory.parameterizeUrl(KFSConstants.MAINTENANCE_ACTION, parameters);
            return new Action(StringUtils.capitalize(CamsConstants.AssetActions.MERGE), "GET", url);
        }
        return null;
    }

    private void addLoanUrls(final Asset asset, final List<Action> actions) {
        
        final Map<String, String> parameters = new HashMap<>();
        parameters.put(KFSConstants.DISPATCH_REQUEST_PARAMETER, KRADConstants.DOC_HANDLER_METHOD);
        parameters.put(CamsPropertyConstants.AssetTransferDocument.CAPITAL_ASSET_NUMBER,
                asset.getCapitalAssetNumber().toString()
        );
        parameters.put(KFSConstants.PARAMETER_COMMAND, "initiate");
        parameters.put(KFSConstants.DOCUMENT_TYPE_NAME, CamsConstants.DocumentTypeName.ASSET_EQUIPMENT_LOAN_OR_RETURN);

        if (assetService.isAssetLoaned(asset)) {
            parameters.put(CamsConstants.AssetActions.LOAN_TYPE, CamsConstants.AssetActions.LOAN_RENEW);
            String url = UrlFactory.parameterizeUrl(CamsConstants.StrutsActions.EQUIPMENT_LOAN_OR_RETURN, parameters);
            actions.add(new Action(StringUtils.capitalize(CamsConstants.AssetActions.LOAN_RENEW), "GET", url));

            parameters.remove(CamsConstants.AssetActions.LOAN_TYPE);
            parameters.put(CamsConstants.AssetActions.LOAN_TYPE, CamsConstants.AssetActions.LOAN_RETURN);
            url = UrlFactory.parameterizeUrl(CamsConstants.StrutsActions.EQUIPMENT_LOAN_OR_RETURN, parameters);
            actions.add(new Action(StringUtils.capitalize(CamsConstants.AssetActions.LOAN_RETURN), "GET", url));
        } else if (asset.getCampusTagNumber() != null) {
            parameters.put(CamsConstants.AssetActions.LOAN_TYPE, CamsConstants.AssetActions.LOAN);
            final String url =
                    UrlFactory.parameterizeUrl(CamsConstants.StrutsActions.EQUIPMENT_LOAN_OR_RETURN, parameters);
            actions.add(new Action(StringUtils.capitalize(CamsConstants.AssetActions.LOAN), "GET", url));
        }
    }

    private Action getViewAssetUrl(final Asset asset) {
        final String propertyName = businessObjectDictionaryService.getTitleAttribute(asset.getClass());
        final String inquiryUrl = detailsUrlService.getDetailsUrl(asset, propertyName);
        final boolean isDetailsUrl = detailsUrlService.isStringDetailsLink(inquiryUrl);
        final String method = isDetailsUrl ? "DETAILS" : "INQUIRY";
        return new Action(StringUtils.capitalize(CamsConstants.AssetActions.VIEW),
                method,
                isDetailsUrl ? inquiryUrl : inquiryUrl + "&mode=modal"
        );
    }

    protected Map<String, String> getSeparateParameters(final Asset asset) {
        final Map<String, String> parameters = Map.ofEntries(
                entry(KFSConstants.DISPATCH_REQUEST_PARAMETER, KFSConstants.MAINTENANCE_NEW_METHOD_TO_CALL),
                entry(KFSConstants.BUSINESS_OBJECT_CLASS_ATTRIBUTE, AssetGlobal.class.getName()),
                entry(
                        CamsPropertyConstants.AssetGlobal.SEPARATE_SOURCE_CAPITAL_ASSET_NUMBER,
                        asset.getCapitalAssetNumber().toString()
                ),
                // parameter that tells us this is a separate action. We read this in AssetMaintenanbleImpl.processAfterNew
                entry(
                        KFSPropertyConstants.FINANCIAL_DOCUMENT_TYPE_CODE,
                        CamsConstants.PaymentDocumentTypeCodes.ASSET_GLOBAL_SEPARATE
                )
        );
        return parameters;
    }
    
    //Cornell customization
    private boolean allowsEdit(final BusinessObjectBase businessObject, final Person user) {
        final BusinessObjectAdminService businessObjectAdminService = businessObjectDictionaryService
                .getBusinessObjectAdminService(businessObject.getClass());

        final BusinessObjectEntry businessObjectEntry = businessObjectDictionaryService.getBusinessObjectEntry(
                businessObject.getClass().getName());
        
        return businessObjectEntry.supportsAction(ActionType.EDIT)
                && businessObjectAdminService.allowsEdit(businessObject, user);
    }

    public void setAssetService(final AssetService assetService) {
        this.assetService = assetService;
    }

    public void setDocumentDictionaryService(final DocumentDictionaryService documentDictionaryService) {
        this.documentDictionaryService = documentDictionaryService;
    }

    public void setParameterService(final ParameterService parameterService) {
        this.parameterService = parameterService;
    }
}
