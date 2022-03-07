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
package org.kuali.kfs.module.purap.document.web.struts;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.integration.purap.CapitalAssetLocation;
import org.kuali.kfs.integration.purap.CapitalAssetSystem;
import org.kuali.kfs.integration.purap.ItemCapitalAsset;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.question.ConfirmationQuestion;
import org.kuali.kfs.kns.util.KNSGlobalVariables;
import org.kuali.kfs.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.KualiRuleService;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.service.PersistenceService;
import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.MessageMap;
import org.kuali.kfs.krad.util.NoteType;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.businessobject.BillingAddress;
import org.kuali.kfs.module.purap.businessobject.CapitalAssetSystemState;
import org.kuali.kfs.module.purap.businessobject.CapitalAssetSystemType;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.businessobject.PurchasingCapitalAssetItem;
import org.kuali.kfs.module.purap.businessobject.PurchasingCapitalAssetSystemBase;
import org.kuali.kfs.module.purap.businessobject.PurchasingItemBase;
import org.kuali.kfs.module.purap.document.PurchaseOrderAmendmentDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocumentBase;
import org.kuali.kfs.module.purap.document.PurchasingDocument;
import org.kuali.kfs.module.purap.document.PurchasingDocumentBase;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.module.purap.document.service.PurchaseOrderService;
import org.kuali.kfs.module.purap.document.service.PurchasingService;
import org.kuali.kfs.module.purap.document.validation.event.AttributedAddPurchasingAccountsPayableItemEvent;
import org.kuali.kfs.module.purap.document.validation.event.AttributedAddPurchasingCapitalAssetLocationEvent;
import org.kuali.kfs.module.purap.document.validation.event.AttributedAddPurchasingItemCapitalAssetEvent;
import org.kuali.kfs.module.purap.document.validation.event.AttributedCommodityCodesForDistributionEvent;
import org.kuali.kfs.module.purap.document.validation.event.AttributedImportPurchasingAccountsPayableItemEvent;
import org.kuali.kfs.module.purap.document.validation.event.AttributedUpdateCamsViewPurapEvent;
import org.kuali.kfs.module.purap.exception.ItemParserException;
import org.kuali.kfs.module.purap.service.PurapAccountingService;
import org.kuali.kfs.module.purap.util.ItemParser;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.Building;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.validation.event.AddAccountingLineEvent;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorContract;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.kfs.vnd.service.PhoneNumberService;
import org.kuali.kfs.kew.api.WorkflowDocument;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.module.purap.document.web.struts.CuPurchaseOrderForm;
import edu.cornell.kfs.module.purap.util.PurchasingFavoriteAccountLineBuilderBase;
import edu.cornell.kfs.module.purap.util.PurchasingFavoriteAccountLineBuilderForDistribution;
import edu.cornell.kfs.module.purap.util.PurchasingFavoriteAccountLineBuilderForLineItem;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.businessobject.FavoriteAccount;
import edu.cornell.kfs.sys.businessobject.NoteExtendedAttribute;
import edu.cornell.kfs.sys.service.UserFavoriteAccountService;
import edu.cornell.kfs.vnd.businessobject.CuVendorAddressExtension;

/**
 * Struts Action for Purchasing documents.
 */
public class PurchasingActionBase extends PurchasingAccountsPayableActionBase {

    private static final Logger LOG = LogManager.getLogger();
    
    private static final int SIZE_5MB =5242880;

    @Override
    public ActionForward refresh(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        PurchasingAccountsPayableFormBase baseForm = (PurchasingAccountsPayableFormBase) form;

        PurchasingDocument document = (PurchasingDocument) baseForm.getDocument();
        String refreshCaller = baseForm.getRefreshCaller();
        BusinessObjectService businessObjectService = SpringContext.getBean(BusinessObjectService.class);
        PhoneNumberService phoneNumberService = SpringContext.getBean(PhoneNumberService.class);

        // Format phone numbers
        document.setInstitutionContactPhoneNumber(phoneNumberService.formatNumberIfPossible(
                document.getInstitutionContactPhoneNumber()));
        document.setRequestorPersonPhoneNumber(phoneNumberService.formatNumberIfPossible(
                document.getRequestorPersonPhoneNumber()));
        document.setDeliveryToPhoneNumber(phoneNumberService.formatNumberIfPossible(
                document.getDeliveryToPhoneNumber()));
        
        // names in KIM are longer than what we store these names at; truncate them to match our data dictionary
        // max lengths
        if (StringUtils.equals(refreshCaller, "kimPersonLookupable")) {
            Integer deliveryToNameMaxLength = SpringContext.getBean(DataDictionaryService.class)
                    .getAttributeMaxLength(document.getClass(), PurapPropertyConstants.DELIVERY_TO_NAME);
            // KFSPTS-518/KFSUPGRADE-351
            if (deliveryToNameMaxLength == null && document instanceof PurchaseOrderAmendmentDocument) {
            	deliveryToNameMaxLength = SpringContext.getBean(DataDictionaryService.class).getAttributeMaxLength(PurchaseOrderDocument.class, PurapPropertyConstants.DELIVERY_TO_NAME);
            }
            if (StringUtils.isNotEmpty(document.getDeliveryToName())
                    && ObjectUtils.isNotNull(deliveryToNameMaxLength)
                    && document.getDeliveryToName().length() > deliveryToNameMaxLength) {
                document.setDeliveryToName(document.getDeliveryToName().substring(0, deliveryToNameMaxLength));
                GlobalVariables.getMessageMap().clearErrorPath();
                GlobalVariables.getMessageMap().addToErrorPath(PurapConstants.DELIVERY_TAB_ERRORS);
                GlobalVariables.getMessageMap().putWarning(PurapPropertyConstants.DELIVERY_TO_NAME,
                        PurapKeyConstants.WARNING_DELIVERY_TO_NAME_TRUNCATED);
                GlobalVariables.getMessageMap().removeFromErrorPath(PurapConstants.DELIVERY_TAB_ERRORS);
            }

            Integer requestorNameMaxLength = SpringContext.getBean(DataDictionaryService.class).getAttributeMaxLength(document.getClass(), PurapPropertyConstants.REQUESTOR_PERSON_NAME);
            // KFSPTS-518/KFSUPGRADE-351
            if (requestorNameMaxLength == null && document instanceof PurchaseOrderAmendmentDocument) {
            	requestorNameMaxLength = SpringContext.getBean(DataDictionaryService.class).getAttributeMaxLength(PurchaseOrderDocument.class, PurapPropertyConstants.REQUESTOR_PERSON_NAME);
            }
            if (StringUtils.isNotEmpty(document.getRequestorPersonName())
                    && ObjectUtils.isNotNull(requestorNameMaxLength)
                    && document.getRequestorPersonName().length() > requestorNameMaxLength) {
                document.setRequestorPersonName(document.getRequestorPersonName().substring(0, requestorNameMaxLength));
                GlobalVariables.getMessageMap().clearErrorPath();
                GlobalVariables.getMessageMap().addToErrorPath(PurapConstants.ADDITIONAL_TAB_ERRORS);
                GlobalVariables.getMessageMap().putWarning(PurapPropertyConstants.REQUESTOR_PERSON_NAME,
                        PurapKeyConstants.WARNING_REQUESTOR_NAME_TRUNCATED);
                GlobalVariables.getMessageMap().removeFromErrorPath(PurapConstants.ADDITIONAL_TAB_ERRORS);
            }
        }

        // Refreshing the fields after returning from a vendor lookup in the vendor tab
        if (StringUtils.equals(refreshCaller, VendorConstants.VENDOR_LOOKUPABLE_IMPL)
                && document.getVendorDetailAssignedIdentifier() != null
                && document.getVendorHeaderGeneratedIdentifier() != null) {
            document.setVendorContractGeneratedIdentifier(null);
            document.refreshReferenceObject("vendorContract");

            // retrieve vendor based on selection from vendor lookup
            document.refreshReferenceObject("vendorDetail");
            document.templateVendorDetail(document.getVendorDetail());
         // KFSPTS-1612 : populate vendor contract name
                                     if (CollectionUtils.isNotEmpty(document.getVendorDetail()
                                                     .getVendorContracts())) {
                                             for (VendorContract vendorContract : document.getVendorDetail().getVendorContracts()) {
                                                     if (vendorContract.isActive()) {
                                                             document.setVendorContractGeneratedIdentifier(vendorContract.getVendorContractGeneratedIdentifier());
                                                            document.refreshReferenceObject("vendorContract");
                                                     }
                                             }
                                     }
            // populate default address based on selected vendor
            VendorAddress defaultAddress = SpringContext.getBean(VendorService.class).getVendorDefaultAddress(
                    document.getVendorDetail().getVendorAddresses(),
                    document.getVendorDetail().getVendorHeader().getVendorType().getAddressType()
                            .getVendorAddressTypeCode(), document.getDeliveryCampusCode());
            
            if(defaultAddress==null){
                GlobalVariables.getMessageMap().putError(VendorPropertyConstants.VENDOR_DOC_ADDRESS,
                        PurapKeyConstants.ERROR_INACTIVE_VENDORADDRESS);  
            }
            document.templateVendorAddress(defaultAddress);
            // CU enhancement KFSUPGRDE-348
			document.setPurchaseOrderTransmissionMethodCode(((CuVendorAddressExtension)defaultAddress.getExtension()).getPurchaseOrderTransmissionMethodCode());
        }

        // Refreshing the fields after returning from a contract lookup in the vendor tab
        if (StringUtils.equals(refreshCaller, VendorConstants.VENDOR_CONTRACT_LOOKUPABLE_IMPL)) {
            if (StringUtils.isNotEmpty(request.getParameter(KFSPropertyConstants.DOCUMENT + "." +
                    PurapPropertyConstants.VENDOR_CONTRACT_ID))) {
                // retrieve Contract based on selection from contract lookup
                VendorContract refreshVendorContract = new VendorContract();
                refreshVendorContract.setVendorContractGeneratedIdentifier(
                        document.getVendorContractGeneratedIdentifier());
                refreshVendorContract = (VendorContract) businessObjectService.retrieve(refreshVendorContract);

                // retrieve Vendor based on selected contract
                document.setVendorHeaderGeneratedIdentifier(refreshVendorContract.getVendorHeaderGeneratedIdentifier());
                document.setVendorDetailAssignedIdentifier(refreshVendorContract.getVendorDetailAssignedIdentifier());
                document.refreshReferenceObject("vendorDetail");
                document.templateVendorDetail(document.getVendorDetail());

                // always template contract after vendor to keep contract defaults last
                document.templateVendorContract(refreshVendorContract);

                // populate default address from selected vendor
                VendorAddress defaultAddress = SpringContext.getBean(VendorService.class).getVendorDefaultAddress(
                        document.getVendorDetail().getVendorAddresses(),
                        document.getVendorDetail().getVendorHeader().getVendorType().getAddressType()
                                .getVendorAddressTypeCode(), "");
                if (defaultAddress == null) {
                    GlobalVariables.getMessageMap().putError(VendorPropertyConstants.VENDOR_DOC_ADDRESS,
                            PurapKeyConstants.ERROR_INACTIVE_VENDORADDRESS);   
                }
                document.templateVendorAddress(defaultAddress);

                // update internal dollar limit for PO since the contract might affect this value
                if (document instanceof PurchaseOrderDocument) {
                    PurchaseOrderDocument poDoc = (PurchaseOrderDocument) document;
                    KualiDecimal limit = SpringContext.getBean(PurchaseOrderService.class)
                            .getInternalPurchasingDollarLimit(poDoc);
                    poDoc.setInternalPurchasingLimit(limit);
                }
            }
        }

        // Refreshing the fields after returning from an address lookup in the vendor tab
        if (StringUtils.equals(refreshCaller, VendorConstants.VENDOR_ADDRESS_LOOKUPABLE_IMPL)) {
            if (StringUtils.isNotEmpty(request.getParameter(KFSPropertyConstants.DOCUMENT + "." +
                    PurapPropertyConstants.VENDOR_ADDRESS_ID))) {
                // retrieve address based on selection from address lookup
                VendorAddress refreshVendorAddress = new VendorAddress();
                refreshVendorAddress.setVendorAddressGeneratedIdentifier(document.getVendorAddressGeneratedIdentifier());
                refreshVendorAddress = (VendorAddress) businessObjectService.retrieve(refreshVendorAddress);
                document.templateVendorAddress(refreshVendorAddress);
            }
        }

        // Refreshing corresponding fields after returning from various kuali lookups
        if (StringUtils.equals(refreshCaller, KFSConstants.KUALI_LOOKUPABLE_IMPL)) {
            if (request.getParameter("document.deliveryCampusCode") != null) {
                // returning from a building or campus lookup on the delivery tab (update billing address)
                BillingAddress billingAddress = new BillingAddress();
                billingAddress.setBillingCampusCode(document.getDeliveryCampusCode());
                Map keys = SpringContext.getBean(PersistenceService.class).getPrimaryKeyFieldValues(billingAddress);
                billingAddress = SpringContext.getBean(BusinessObjectService.class)
                        .findByPrimaryKey(BillingAddress.class, keys);
                document.templateBillingAddress(billingAddress);

                if (request.getParameter("document.deliveryBuildingName") == null) {
                    // came from campus lookup not building, so clear building
                    clearDeliveryBuildingInfo(document, true);
                }
                else {
                    // came from building lookup then turn off "OTHER" and clear room and line2address
                    document.setDeliveryBuildingOtherIndicator(false);
                    document.setDeliveryBuildingRoomNumber("");
                    document.setDeliveryBuildingLine2Address("");
                }
            }
            else if (request.getParameter("document.chartOfAccountsCode") != null) {
                // returning from a chart/org lookup on the document detail tab (update receiving address)
                document.loadReceivingAddress();
            }
            else {
                // returning from a building lookup in a capital asset tab location (update location address)
                String buildingCodeParam = findBuildingCodeFromCapitalAssetBuildingLookup(request);
                if (StringUtils.isNotEmpty(buildingCodeParam)) {
                    PurchasingFormBase purchasingForm = (PurchasingFormBase) form;
                    updateCapitalAssetLocation(request, purchasingForm, document, buildingCodeParam);
                }
            }
        }
        return super.refresh(mapping, form, request, response);
    }
    
    protected void updateAssetBuildingLocations(PurchasingFormBase purchasingForm, HttpServletRequest request,
            PurchasingDocument document) {
        List<String> buildingCodeParams = findAllBuildingCodesFromCapitalAssetBuildingLookup(request);
        for (String buildingCodeParam : buildingCodeParams) {
            updateCapitalAssetLocation(request, purchasingForm, document, buildingCodeParam);
        }
    }

    protected void updateCapitalAssetLocation(HttpServletRequest request, PurchasingFormBase purchasingForm,
            PurchasingDocument document, String buildingCodeParam) {
        String buildingCode = request.getParameterValues(buildingCodeParam)[0];
        String campusCodeParam = buildingCodeParam.replace("buildingCode", "campusCode");
        String campusCode = request.getParameterValues(campusCodeParam)[0];

        Building locationBuilding = findBuilding(buildingCode, campusCode);

        CapitalAssetLocation location = null;
        boolean isNewLine = StringUtils.containsIgnoreCase(buildingCodeParam, "newPurchasingCapitalAssetLocationLine");
        if (isNewLine) {
            if (document.getCapitalAssetSystemType().getCapitalAssetSystemTypeCode()
                    .equals(PurapConstants.CapitalAssetSystemTypes.INDIVIDUAL)) {
                String locationCapitalAssetItemNumber = getCaptialAssetItemNumberFromParameter(buildingCodeParam);
                PurchasingCapitalAssetItem capitalAssetItem = document.getPurchasingCapitalAssetItems()
                        .get(Integer.parseInt(locationCapitalAssetItemNumber));
                location = capitalAssetItem.getPurchasingCapitalAssetSystem()
                        .getNewPurchasingCapitalAssetLocationLine();
            } else {
                location = purchasingForm.getNewPurchasingCapitalAssetLocationLine();
            }
        } else if (StringUtils.containsIgnoreCase(buildingCodeParam, "purchasingCapitalAssetLocationLine")) {
            String locationCapitalAssetLocationNumber = getCaptialAssetLocationNumberFromParameter(buildingCodeParam);
            if (document.getCapitalAssetSystemType().getCapitalAssetSystemTypeCode()
                    .equals(PurapConstants.CapitalAssetSystemTypes.INDIVIDUAL)) {
                String locationCapitalAssetItemNumber = getCaptialAssetItemNumberFromParameter(buildingCodeParam);
                PurchasingCapitalAssetItem capitalAssetItem = document.getPurchasingCapitalAssetItems()
                        .get(Integer.parseInt(locationCapitalAssetItemNumber));
                location = capitalAssetItem.getPurchasingCapitalAssetSystem().getCapitalAssetLocations()
                        .get(Integer.parseInt(locationCapitalAssetLocationNumber));
            }
        } else if (StringUtils.containsIgnoreCase(buildingCodeParam, "purchasingCapitalAssetSystem")) {
            String locationCapitalAssetLocationNumber = getCaptialAssetLocationNumberFromParameter(buildingCodeParam);
            if (document.getCapitalAssetSystemType().getCapitalAssetSystemTypeCode()
                    .equals(PurapConstants.CapitalAssetSystemTypes.INDIVIDUAL)) {
                String locationCapitalAssetItemNumber = getCaptialAssetItemNumberFromParameter(buildingCodeParam);
                PurchasingCapitalAssetItem capitalAssetItem = document.getPurchasingCapitalAssetItems()
                        .get(Integer.parseInt(locationCapitalAssetItemNumber));
                location = capitalAssetItem.getPurchasingCapitalAssetSystem().getCapitalAssetLocations()
                        .get(Integer.parseInt(locationCapitalAssetLocationNumber));
            } else {
                CapitalAssetSystem capitalAssetSystem = document.getPurchasingCapitalAssetSystems().get(0);
                location = capitalAssetSystem.getCapitalAssetLocations()
                        .get(Integer.parseInt(locationCapitalAssetLocationNumber));
            }
        }

        if (location != null) {
            location.templateBuilding(locationBuilding);
        }
        if (locationBuilding == null && !(isNewLine && buildingCode.isEmpty())) {
            // ignore scenario where isNewLine and it's empty as this is the default case; only validate new lines if
            // data exists
            GlobalVariables.getMessageMap().putError(buildingCodeParam,
                    PurapKeyConstants.ERROR_CAPITAL_ASSET_LOCATION_BUILDING_CODE_INVALID);
        }
    }

    protected String getCaptialAssetLocationNumberFromParameter(String parameterKey) {
        int beginIndex = parameterKey.lastIndexOf("[") + 1;
        int endIndex = parameterKey.lastIndexOf("]");
        return parameterKey.substring(beginIndex, endIndex);
    }

    protected String getCaptialAssetItemNumberFromParameter(String parameterKey) {
        int beginIndex = parameterKey.indexOf("[") + 1;
        int endIndex = parameterKey.indexOf("]");
        return parameterKey.substring(beginIndex, endIndex);
    }

    protected void updateDeliveryBuilding(HttpServletRequest request, PurchasingDocument document) {
        String buildingCode = request.getParameter("document.deliveryBuildingCode");
        String campusCode = request.getParameter("document.deliveryCampusCode");

        Building deliveryBuilding = findBuilding(buildingCode, campusCode);
        if (deliveryBuilding != null) {
            document.setDeliveryBuildingName(deliveryBuilding.getBuildingName());
            document.setDeliveryBuildingLine1Address(deliveryBuilding.getBuildingStreetAddress());
            document.setDeliveryBuildingLine2Address("");
            document.setDeliveryBuildingRoomNumber("");
            document.setDeliveryCityName(deliveryBuilding.getBuildingAddressCityName());
            document.setDeliveryStateCode(deliveryBuilding.getBuildingAddressStateCode());
            document.setDeliveryPostalCode(deliveryBuilding.getBuildingAddressZipCode());
            document.setDeliveryCountryCode(deliveryBuilding.getBuildingAddressCountryCode());
        } else {
            clearDeliveryBuildingInfo(document, false);
            GlobalVariables.getMessageMap().putError("document.deliveryBuildingCode",
                    PurapKeyConstants.ERROR_DELIVERY_BUILDING_CODE_INVALID);
        }
    }

    /**
     * Clears delivery building information from the document.
     *
     * @param document
     * @param clearBuildingCode
     */
    private void clearDeliveryBuildingInfo(PurchasingDocument document, boolean clearBuildingCode) {
        if (clearBuildingCode) {
            document.setDeliveryBuildingCode("");
        }
        document.setDeliveryBuildingName("");
        document.setDeliveryBuildingLine1Address("");
        document.setDeliveryBuildingLine2Address("");
        document.setDeliveryBuildingRoomNumber("");
        document.setDeliveryCityName("");
        document.setDeliveryStateCode("");
        document.setDeliveryPostalCode("");
        document.setDeliveryCountryCode("");
    }

    private Building findBuilding(String buildingCode, String campusCode) {
        Building building = new Building();
        building.setCampusCode(campusCode);
        building.setBuildingCode(buildingCode.toUpperCase(Locale.US));
        Map<String, String> keys = SpringContext.getBean(PersistenceService.class).getPrimaryKeyFieldValues(building);
        building = SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(Building.class, keys);
        return building;
    }

    /**
     * Setup document to use "OTHER" building
     *
     * @param mapping An ActionMapping
     * @param form An ActionForm
     * @param request A HttpServletRequest
     * @param response A HttpServletResponse
     * @return An ActionForward
     * @throws Exception
     */
    public ActionForward useOtherDeliveryBuilding(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        PurchasingFormBase baseForm = (PurchasingFormBase) form;
        PurchasingDocument document = (PurchasingDocument) baseForm.getDocument();

        document.setDeliveryBuildingOtherIndicator(true);
        clearDeliveryBuildingInfo(document, true);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward useOffCampusAssetLocationBuildingByDocument(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingFormBase baseForm = (PurchasingFormBase) form;
        PurchasingDocument document = (PurchasingDocument) baseForm.getDocument();

        String fullParameter = (String) request.getAttribute(KFSConstants.METHOD_TO_CALL_ATTRIBUTE);
        String systemIndex = StringUtils.substringBetween(fullParameter, KFSConstants.METHOD_TO_CALL_PARM1_LEFT_DEL,
                KFSConstants.METHOD_TO_CALL_PARM1_RIGHT_DEL);
        String assetLocationIndex = StringUtils.substringBetween(fullParameter,
                KFSConstants.METHOD_TO_CALL_PARM2_LEFT_DEL, KFSConstants.METHOD_TO_CALL_PARM2_RIGHT_DEL);

        CapitalAssetSystem system = document.getPurchasingCapitalAssetSystems().get(Integer.parseInt(systemIndex));

        if ("new".equals(assetLocationIndex)) {
            useOffCampusAssetLocationBuilding(baseForm.getNewPurchasingCapitalAssetLocationLine());
        }
        else {
            useOffCampusAssetLocationBuilding(system.getCapitalAssetLocations().get(Integer.parseInt(assetLocationIndex)));
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward useOffCampusAssetLocationBuildingByItem(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingFormBase baseForm = (PurchasingFormBase) form;
        PurchasingDocument document = (PurchasingDocument) baseForm.getDocument();

        String fullParameter = (String) request.getAttribute(KFSConstants.METHOD_TO_CALL_ATTRIBUTE);
        String assetItemIndex = StringUtils.substringBetween(fullParameter,
                KFSConstants.METHOD_TO_CALL_PARM1_LEFT_DEL, KFSConstants.METHOD_TO_CALL_PARM1_RIGHT_DEL);
        String assetLocationIndex = StringUtils.substringBetween(fullParameter,
                KFSConstants.METHOD_TO_CALL_PARM2_LEFT_DEL, KFSConstants.METHOD_TO_CALL_PARM2_RIGHT_DEL);

        PurchasingCapitalAssetItem assetItem = document.getPurchasingCapitalAssetItems()
                .get(Integer.parseInt(assetItemIndex));
        CapitalAssetSystem system = assetItem.getPurchasingCapitalAssetSystem();

        if ("new".equals(assetLocationIndex)) {
            useOffCampusAssetLocationBuilding(system.getNewPurchasingCapitalAssetLocationLine());
        }
        else {
            useOffCampusAssetLocationBuilding(system.getCapitalAssetLocations()
                    .get(Integer.parseInt(assetLocationIndex)));
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    protected void useOffCampusAssetLocationBuilding(CapitalAssetLocation location) {
        if (location != null) {
            location.setOffCampusIndicator(true);
            location.setBuildingCode("");
            location.setCapitalAssetLine1Address("");
            location.setCapitalAssetCityName("");
            location.setCapitalAssetStateCode("");
            location.setCapitalAssetPostalCode("");
            location.setCapitalAssetCountryCode("");
            location.setBuildingRoomNumber("");
        }
    }

    /**
     * Add a new item to the document.
     *
     * @param mapping An ActionMapping
     * @param form An ActionForm
     * @param request The HttpServletRequest
     * @param response The HttpServletResponse
     * @return An ActionForward
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public ActionForward addItem(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        PurchasingFormBase purchasingForm = (PurchasingFormBase) form;
        PurApItem item = purchasingForm.getNewPurchasingItemLine();
        PurchasingDocument purDocument = (PurchasingDocument) purchasingForm.getDocument();
        boolean rulePassed = SpringContext.getBean(KualiRuleService.class)
                .applyRules(new AttributedAddPurchasingAccountsPayableItemEvent("", purDocument, item));

        if (rulePassed) {
            item = purchasingForm.getAndResetNewPurchasingItemLine();
            purDocument.addItem(item);
            // KFSPTS-985
            if (((PurchasingDocumentBase)purDocument).isIntegratedWithFavoriteAccount()) {
                populatePrimaryFavoriteAccount(item.getSourceAccountingLines(), getAccountClassFromNewPurApAccountingLine(purchasingForm));
            }
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Import items to the document from a spreadsheet.
     *
     * @param mapping An ActionMapping
     * @param form An ActionForm
     * @param request The HttpServletRequest
     * @param response The HttpServletResponse
     * @return An ActionForward
     * @throws Exception
     */
    public ActionForward importItems(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        LOG.info("Importing item lines");

        PurchasingFormBase purchasingForm = (PurchasingFormBase) form;
        PurchasingDocument purDocument = (PurchasingDocument) purchasingForm.getDocument();
        String documentNumber = purDocument.getDocumentNumber();
        FormFile itemFile = purchasingForm.getItemImportFile();
        Class itemClass = purDocument.getItemClass();
        String errorPath = PurapConstants.ITEM_TAB_ERRORS;
        ItemParser itemParser = purDocument.getItemParser();
        // starting position of the imported items, equals the # of existing above-the-line items.
        int itemLinePosition = purDocument.getItemLinePosition();

        try {
            List<PurApItem> importedItems = itemParser.importItems(itemFile, itemClass, documentNumber);
            // validate imported items
            boolean allPassed = true;
            int itemLineNumber = 0;
            for (PurApItem item : importedItems) {
                // Before the validation, set the item line number to the same as the line number in the import file
                // (starting from 1) so that the error message will use the correct line number if there're errors
                // for the current item line.
                item.setItemLineNumber(++itemLineNumber);
                allPassed &= SpringContext.getBean(KualiRuleService.class)
                        .applyRules(new AttributedImportPurchasingAccountsPayableItemEvent("", purDocument, item));
                // After the validation, set the item line number to the correct value as if it's added to the item
                // list.
                item.setItemLineNumber(itemLineNumber + itemLinePosition);
            }
            if (allPassed) {
                updateBOReferenceforNewItems(importedItems, (PurchasingDocumentBase) purDocument);
                purDocument.getItems().addAll(itemLinePosition, importedItems);

            }
        }
        catch (ItemParserException e) {
            GlobalVariables.getMessageMap().putError(errorPath, e.getErrorKey(), e.getErrorParameters());
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Whenever add a new item, we need to keep track of the reference from Item to Doc and from Account to Item
     *
     * @param importedItems
     */
    protected void updateBOReferenceforNewItems(List<PurApItem> importedItems, PurchasingDocumentBase purDocument) {
        // update reference from Item to Document and from Account to Item.
        for (PurApItem item : importedItems) {
            item.setPurapDocument(purDocument);
            // set the PurapDocumentIdentifier so in the future, item acquire the object again by calling
            // refreshReferenceObject for purApDocument.
            if (purDocument.getPurapDocumentIdentifier() != null) {
                item.setPurapDocumentIdentifier(purDocument.getPurapDocumentIdentifier());
            }
            for (PurApAccountingLine account : item.getSourceAccountingLines()) {
                account.setPurapItem(item);
                if (item.getItemIdentifier() != null) {
                    account.setItemIdentifier(item.getItemIdentifier());
                }
            }
        }
    }

    /**
     * Delete an item from the document.
     *
     * @param mapping An ActionMapping
     * @param form An ActionForm
     * @param request The HttpServletRequest
     * @param response The HttpServletResponse
     * @return An ActionForward
     * @throws Exception
     */
    public ActionForward deleteItem(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        PurchasingAccountsPayableFormBase purchasingForm = (PurchasingAccountsPayableFormBase) form;

        PurchasingDocument purDocument = (PurchasingDocument) purchasingForm.getDocument();

        purDocument.deleteItem(getSelectedLine(request));

        if (StringUtils.isNotBlank(purDocument.getCapitalAssetSystemTypeCode())) {
            boolean rulePassed = SpringContext.getBean(KualiRuleService.class)
                    .applyRules(new AttributedUpdateCamsViewPurapEvent(purDocument));
            if (rulePassed) {
                SpringContext.getBean(PurchasingService.class).setupCapitalAssetItems(purDocument);
            }
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Moves the selected item up one position.
     *
     * @param mapping An ActionMapping
     * @param form An ActionForm
     * @param request The HttpServletRequest
     * @param response The HttpServletResponse
     * @return An ActionForward
     * @throws Exception
     */
    public ActionForward upItem(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        PurchasingAccountsPayableFormBase purchasingForm = (PurchasingAccountsPayableFormBase) form;
        PurchasingDocument purDocument = (PurchasingDocument) purchasingForm.getDocument();
        int line = getSelectedLine(request);
        purDocument.itemSwap(line, line - 1);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Moves the selected item down one position (These two methods up/down could easily be consolidated. For now, it
     * seems more straightforward to keep them separate.)
     *
     * @param mapping  An ActionMapping
     * @param form     An ActionForm
     * @param request  The HttpServletRequest
     * @param response The HttpServletResponse
     * @return An ActionForward
     * @throws Exception
     */
    public ActionForward downItem(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        PurchasingAccountsPayableFormBase purchasingForm = (PurchasingAccountsPayableFormBase) form;
        PurchasingDocument purDocument = (PurchasingDocument) purchasingForm.getDocument();
        int line = getSelectedLine(request);
        purDocument.itemSwap(line, line + 1);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Reveals the account distribution section.
     *
     * @param mapping An ActionMapping
     * @param form An ActionForm
     * @param request The HttpServletRequest
     * @param response The HttpServletResponse
     * @return An ActionForward
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public ActionForward setupAccountDistribution(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        PurchasingFormBase purchasingForm = (PurchasingFormBase) form;
        PurchasingDocumentBase document = (PurchasingDocumentBase) purchasingForm.getDocument();

        purchasingForm.setHideDistributeAccounts(false);
        // KFSPTS-985
        if (document.isIntegratedWithFavoriteAccount()) {
            populatePrimaryFavoriteAccount(
                    purchasingForm.getAccountDistributionsourceAccountingLines(), getAccountClassFromNewPurApAccountingLine(purchasingForm));
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Clear out the accounting lines from all the items.
     *
     * @param mapping An ActionMapping
     * @param form An ActionForm
     * @param request The HttpServletRequest
     * @param response The HttpServletResponse
     * @return An ActionForward
     * @throws Exception
     */
    public ActionForward removeAccounts(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        PurchasingAccountsPayableFormBase purchasingForm = (PurchasingAccountsPayableFormBase) form;

        Object question = request.getParameter(PurapConstants.QUESTION_INDEX);
        Object buttonClicked = request.getParameter(KFSConstants.QUESTION_CLICKED_BUTTON);

        if (question == null) {
            String questionText = SpringContext.getBean(ConfigurationService.class)
                    .getPropertyValueAsString(PurapConstants.QUESTION_REMOVE_ACCOUNTS);
            return this.performQuestionWithoutInput(mapping, form, request, response,
                    PurapConstants.REMOVE_ACCOUNTS_QUESTION, questionText, KFSConstants.CONFIRMATION_QUESTION,
                    KFSConstants.ROUTE_METHOD, "0");
        }
        else if (ConfirmationQuestion.YES.equals(buttonClicked)) {
            for (PurApItem item : ((PurchasingAccountsPayableDocument) purchasingForm.getDocument()).getItems()) {
                item.getSourceAccountingLines().clear();
            }

            KNSGlobalVariables.getMessageList().add(PurapKeyConstants.PURAP_GENERAL_ACCOUNTS_REMOVED);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Clear out the commodity codes from all the items.
     *
     * @param mapping An ActionMapping
     * @param form An ActionForm
     * @param request The HttpServletRequest
     * @param response The HttpServletResponse
     * @return An ActionForward
     * @throws Exception
     */
    public ActionForward clearItemsCommodityCodes(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        PurchasingAccountsPayableFormBase purchasingForm = (PurchasingAccountsPayableFormBase) form;

        Object question = request.getParameter(PurapConstants.QUESTION_INDEX);
        Object buttonClicked = request.getParameter(KFSConstants.QUESTION_CLICKED_BUTTON);

        if (question == null) {
            String questionText = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(
                    PurapConstants.QUESTION_CLEAR_ALL_COMMODITY_CODES);

            return this.performQuestionWithoutInput(mapping, form, request, response,
                    PurapConstants.CLEAR_COMMODITY_CODES_QUESTION, questionText, KFSConstants.CONFIRMATION_QUESTION,
                    KFSConstants.ROUTE_METHOD, "0");
        }
        else if (ConfirmationQuestion.YES.equals(buttonClicked)) {
            for (PurApItem item : ((PurchasingAccountsPayableDocument) purchasingForm.getDocument()).getItems()) {
                PurchasingItemBase purItem = (PurchasingItemBase) item;
                purItem.setPurchasingCommodityCode(null);
                purItem.setCommodityCode(null);
            }

            KNSGlobalVariables.getMessageList().add(PurapKeyConstants.PUR_COMMODITY_CODES_CLEARED);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Validates that the accounting lines while a distribute accounts action is being taken.
     *
     * @param document
     * @param distributionSourceAccountingLines
     * @return
     */
    protected boolean validateDistributeAccounts(Document document,
            List<PurApAccountingLine> distributionSourceAccountingLines) {
        boolean rulePassed = true;
        int i = 0;

        for (PurApAccountingLine accountingLine : distributionSourceAccountingLines) {
            String errorPrefix = "distributionSourceAccountingLines" + "[" + Integer.toString(i) + "]";
            rulePassed &= SpringContext.getBean(KualiRuleService.class)
                    .applyRules(new AddAccountingLineEvent(errorPrefix, document, accountingLine));
            i++;
        }

        return rulePassed;
    }

    /**
     * Distribute accounting line(s) to the item(s). Does not distribute the accounting line(s) to an item if there
     * are already accounting lines associated with that item, if the item is a below-the-line item and has no unit
     * cost, or if the item is inactive. Distribute commodity code to the item(s). Does not distribute the commodity
     * code to an item if the item is not above the line item, is inactive or if the commodity code fails the
     * validation (i.e. inactive commodity code or non existence commodity code).
     *
     * @param mapping  An ActionMapping
     * @param form     An ActionForm
     * @param request  The HttpServletRequest
     * @param response The HttpServletResponse
     * @return An ActionForward
     * @throws Exception
     */
    public ActionForward doDistribution(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        PurchasingFormBase purchasingForm = (PurchasingFormBase) form;
        boolean needToDistributeCommodityCode = false;

        if (StringUtils.isNotBlank(purchasingForm.getDistributePurchasingCommodityCode())) {
            // Do the logic for distributing purchasing commodity code to all the items.
            needToDistributeCommodityCode = true;
        }

        boolean needToDistributeAccount = false;
        List<PurApAccountingLine> distributionsourceAccountingLines =
                purchasingForm.getAccountDistributionsourceAccountingLines();
        if (distributionsourceAccountingLines.size() > 0) {
            needToDistributeAccount = true;
        }
        if (needToDistributeAccount || needToDistributeCommodityCode) {
            PurchasingAccountsPayableDocumentBase purApDocument =
                    (PurchasingAccountsPayableDocumentBase) purchasingForm.getDocument();

            boolean institutionNeedsDistributeAccountValidation = SpringContext.getBean(ParameterService.class)
                    .getParameterValueAsBoolean(KfsParameterConstants.PURCHASING_DOCUMENT.class,
                            PurapParameterConstants.VALIDATE_ACCOUNT_DISTRIBUTION_IND);
            boolean foundAccountDistributionError = false;
            boolean foundCommodityCodeDistributionError = false;
            boolean performedAccountDistribution = false;
            boolean performedCommodityCodeDistribution = false;

            // do check for account percents only if distribution method not equal to "P"
            if (!PurapConstants.AccountDistributionMethodCodes.PROPORTIONAL_CODE.equalsIgnoreCase(
                    purApDocument.getAccountDistributionMethod())) {
                // If the institution's validate account distribution indicator is true and the total percentage in
                // the distribute account list does not equal 100 % then we should display error
                if (institutionNeedsDistributeAccountValidation && needToDistributeAccount
                        && purchasingForm.getTotalPercentageOfAccountDistributionsourceAccountingLines()
                            .compareTo(new BigDecimal(100)) != 0) {
                    GlobalVariables.getMessageMap().putError(PurapConstants.ACCOUNT_DISTRIBUTION_ERROR_KEY,
                            PurapKeyConstants.ERROR_DISTRIBUTE_ACCOUNTS_NOT_100_PERCENT);
                    foundAccountDistributionError = true;
                }
            }

            // if the institution's validate account distribution indicator is true and there is a validation error
            // in the accounts to distribute then we should display an error
            if (institutionNeedsDistributeAccountValidation && needToDistributeAccount
                    && !validateDistributeAccounts(purchasingForm.getDocument(), distributionsourceAccountingLines)) {
                foundAccountDistributionError = true;
            }

            for (PurApItem item : ((PurchasingAccountsPayableDocument) purchasingForm.getDocument()).getItems()) {
                boolean itemIsActive = true;
                if (item instanceof PurchaseOrderItem) {
                    // if item is PO item... only validate active items
                    itemIsActive = ((PurchaseOrderItem) item).isItemActiveIndicator();
                }
                if (needToDistributeCommodityCode) {
                    // only the above the line items need the commodity code.
                    if (item.getItemType().isLineItemIndicator()
                            && StringUtils.isBlank(((PurchasingItemBase) item).getPurchasingCommodityCode())
                            && itemIsActive) {
                        // Ideally we should invoke rules to check whether the commodity code is valid (active, not
                        // restricted, not missing, etc), probably somewhere here or invoke the rule class from here.
                        boolean rulePassed = SpringContext.getBean(KualiRuleService.class)
                                .applyRules(new AttributedCommodityCodesForDistributionEvent("",
                                        purchasingForm.getDocument(),
                                        purchasingForm.getDistributePurchasingCommodityCode()));
                        if (rulePassed) {
                            ((PurchasingItemBase) item).setPurchasingCommodityCode(
                                    purchasingForm.getDistributePurchasingCommodityCode());
                            performedCommodityCodeDistribution = true;
                        } else {
                            foundCommodityCodeDistributionError = true;
                        }
                    } else if (item.getItemType().isLineItemIndicator()
                            && StringUtils.isNotBlank(((PurchasingItemBase) item).getPurchasingCommodityCode())
                            && itemIsActive) {
                        // could not apply to line, as it wasn't blank
                        foundCommodityCodeDistributionError = true;
                    }
                }
                if (needToDistributeAccount && !foundAccountDistributionError) {
                    BigDecimal zero = new BigDecimal(0);
                    // We should be distributing accounting lines to above the line items all the time;
                    // but only to the below the line items when there is a unit cost.
                    boolean unitCostNotZeroForBelowLineItems = item.getItemType().isLineItemIndicator()
                            || item.getItemUnitPrice() != null && zero.compareTo(item.getItemUnitPrice()) < 0;
                    Document document = ((PurchasingFormBase) form).getDocument();
                    Class clazz = document instanceof PurchaseOrderAmendmentDocument ? PurchaseOrderDocument.class :
                            document.getClass();
                    List<String> typesNotAllowingEdit = new ArrayList<>(SpringContext.getBean(ParameterService.class)
                            .getParameterValuesAsString(clazz,
                                    PurapParameterConstants.PURAP_ITEM_TYPES_RESTRICTING_ACCOUNT_EDIT));
                    boolean itemOnExcludeList = typesNotAllowingEdit != null
                            && typesNotAllowingEdit.contains(item.getItemTypeCode());
                    if (item.getSourceAccountingLines().size() == 0 && unitCostNotZeroForBelowLineItems
                            && !itemOnExcludeList && itemIsActive) {
                        for (PurApAccountingLine purApAccountingLine : distributionsourceAccountingLines) {
                            item.getSourceAccountingLines()
                                    .add((PurApAccountingLine) ObjectUtils.deepCopy(purApAccountingLine));
                        }

                        performedAccountDistribution = true;
                    }
                }
            }

            if ((needToDistributeCommodityCode && performedCommodityCodeDistribution
                    && !foundCommodityCodeDistributionError)
                    || (needToDistributeAccount && performedAccountDistribution && !foundAccountDistributionError)) {
                if (needToDistributeCommodityCode && !foundCommodityCodeDistributionError
                        && performedCommodityCodeDistribution) {
                    KNSGlobalVariables.getMessageList().add(PurapKeyConstants.PUR_COMMODITY_CODE_DISTRIBUTED);
                    purchasingForm.setDistributePurchasingCommodityCode(null);
                }
                if (needToDistributeAccount && !foundAccountDistributionError && performedAccountDistribution) {
                    KNSGlobalVariables.getMessageList().add(PurapKeyConstants.PURAP_GENERAL_ACCOUNTS_DISTRIBUTED);
                    distributionsourceAccountingLines.clear();
                }
                purchasingForm.setHideDistributeAccounts(true);
            }

            if (needToDistributeAccount && !performedAccountDistribution && foundAccountDistributionError) {
                GlobalVariables.getMessageMap().putError(PurapConstants.ACCOUNT_DISTRIBUTION_ERROR_KEY,
                        PurapKeyConstants.PURAP_GENERAL_NO_ITEMS_TO_DISTRIBUTE_TO, "account numbers");
            }
            if (needToDistributeCommodityCode && !performedCommodityCodeDistribution
                    && foundCommodityCodeDistributionError) {
                GlobalVariables.getMessageMap().putError(PurapConstants.ITEM_PURCHASING_COMMODITY_CODE,
                        PurapKeyConstants.PURAP_GENERAL_NO_ITEMS_TO_DISTRIBUTE_TO, "commodity codes");
            }
        }
        else {
            GlobalVariables.getMessageMap().putError(PurapConstants.ACCOUNT_DISTRIBUTION_ERROR_KEY,
                    PurapKeyConstants.PURAP_GENERAL_NO_ACCOUNTS_TO_DISTRIBUTE);
        }


        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Simply hides the account distribution section.
     *
     * @param mapping An ActionMapping
     * @param form An ActionForm
     * @param request The HttpServletRequest
     * @param response The HttpServletResponse
     * @return An ActionForward
     * @throws Exception
     */
    public ActionForward cancelAccountDistribution(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        PurchasingFormBase purchasingForm = (PurchasingFormBase) form;
        purchasingForm.setHideDistributeAccounts(true);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * @see org.kuali.kfs.module.purap.document.web.struts.PurchasingAccountsPayableActionBase#processCustomInsertAccountingLine(PurchasingAccountsPayableFormBase, HttpServletRequest)
     */
    @Override
    public boolean processCustomInsertAccountingLine(PurchasingAccountsPayableFormBase purapForm,
            HttpServletRequest request) {
        boolean success = false;
        PurchasingFormBase purchasingForm = (PurchasingFormBase) purapForm;

        int itemIndex = getSelectedLine(request);

        boolean institutionNeedsDistributeAccountValidation = SpringContext.getBean(ParameterService.class)
                .getParameterValueAsBoolean(KfsParameterConstants.PURCHASING_DOCUMENT.class,
                        PurapParameterConstants.VALIDATE_ACCOUNT_DISTRIBUTION_IND);

        if (itemIndex == -2 && !institutionNeedsDistributeAccountValidation) {
            PurApAccountingLine line = purchasingForm.getAccountDistributionnewSourceLine();
            purchasingForm.addAccountDistributionsourceAccountingLine(line);
            success = true;
        }

        return success;
    }

    @Override
    public ActionForward deleteSourceLine(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        PurchasingFormBase purchasingForm = (PurchasingFormBase) form;

        String[] indexes = getSelectedLineForAccounts(request);
        int itemIndex = Integer.parseInt(indexes[0]);
        int accountIndex = Integer.parseInt(indexes[1]);
        if (itemIndex == -2) {
            purchasingForm.getAccountDistributionsourceAccountingLines().remove(accountIndex);
        }
        else {
            PurApItem item = ((PurchasingAccountsPayableDocument) purchasingForm.getDocument()).getItem(itemIndex);
            item.getSourceAccountingLines().remove(accountIndex);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Sets the line for account distribution.
     *
     * @param accountIndex The index of the account into the request parameter
     * @param purchasingAccountsPayableForm A form which inherits from PurchasingAccountsPayableFormBase
     * @return A SourceAccountingLine
     */
    @Override
    protected SourceAccountingLine customAccountRetrieval(int accountIndex, PurchasingAccountsPayableFormBase purchasingAccountsPayableForm) {
        PurchasingFormBase purchasingForm = (PurchasingFormBase) purchasingAccountsPayableForm;
        SourceAccountingLine line;
        line = (SourceAccountingLine) ObjectUtils.deepCopy(purchasingForm.getAccountDistributionsourceAccountingLines().get(accountIndex));
        return line;
    }

    /**
     * This method...
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward selectSystemType(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {

        PurchasingAccountsPayableFormBase purchasingForm = (PurchasingAccountsPayableFormBase) form;
        PurchasingDocumentBase document = (PurchasingDocumentBase) purchasingForm.getDocument();

        Object question = request.getParameter(PurapConstants.QUESTION_INDEX);
        Object buttonClicked = request.getParameter(KFSConstants.QUESTION_CLICKED_BUTTON);

        String systemTypeCode = (String) request.getAttribute(KRADConstants.METHOD_TO_CALL_ATTRIBUTE);
        systemTypeCode = StringUtils.substringBetween(systemTypeCode, "selectSystemType.", ".");

        if (question == null) {
            String questionText = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(PurapConstants.CapitalAssetTabStrings.QUESTION_SYSTEM_SWITCHING);

            return this.performQuestionWithoutInput(mapping, form, request, response, PurapConstants.CapitalAssetTabStrings.SYSTEM_SWITCHING_QUESTION, questionText, KFSConstants.CONFIRMATION_QUESTION, KFSConstants.ROUTE_METHOD, "0");
        }
        else if (ConfirmationQuestion.YES.equals(buttonClicked)) {

            // document.setCapitalAssetSystemTypeCode(systemTypeCode);
            document.refreshReferenceObject(PurapPropertyConstants.CAPITAL_ASSET_SYSTEM_TYPE);

            KNSGlobalVariables.getMessageList().add(PurapKeyConstants.PUR_CAPITAL_ASSET_SYSTEM_TYPE_SWITCHED);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward addItemCapitalAssetByDocument(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingFormBase purchasingForm = (PurchasingFormBase) form;
        PurchasingDocument purDocument = (PurchasingDocument) purchasingForm.getDocument();
        ItemCapitalAsset asset = purDocument.getPurchasingCapitalAssetItems().get(0).getNewPurchasingItemCapitalAssetLine();

        boolean rulePassed = SpringContext.getBean(KualiRuleService.class).applyRules(new AttributedAddPurchasingItemCapitalAssetEvent("", purDocument, asset));

        if (rulePassed) {
            // get specific asset item and grab system as well and attach asset number
            CapitalAssetSystem system = purDocument.getPurchasingCapitalAssetSystems().get(getSelectedLine(request));
            asset = purDocument.getPurchasingCapitalAssetItems().get(0).getAndResetNewPurchasingItemCapitalAssetLine();
            asset.setCapitalAssetSystemIdentifier(system.getCapitalAssetSystemIdentifier());
            if (capitalAssetSystemHasAssetItem(system, asset)) {
                GlobalVariables.getMessageMap().putError(PurapConstants.CAPITAL_ASSET_TAB_ERRORS, PurapKeyConstants.ERROR_CAPITAL_ASSET_DUPLICATE_ASSET);
            } else {
                system.getItemCapitalAssets().add(asset);
            }
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward addItemCapitalAssetByItem(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingFormBase purchasingForm = (PurchasingFormBase) form;
        PurchasingDocument purDocument = (PurchasingDocument) purchasingForm.getDocument();
        // get specific asset item
        PurchasingCapitalAssetItem assetItem = purDocument.getPurchasingCapitalAssetItems().get(getSelectedLine(request));

        ItemCapitalAsset asset = assetItem.getNewPurchasingItemCapitalAssetLine();

        boolean rulePassed = SpringContext.getBean(KualiRuleService.class).applyRules(new AttributedAddPurchasingItemCapitalAssetEvent("", purDocument, asset));

        if (rulePassed) {
            // grab system as well and attach asset number
            CapitalAssetSystem system = assetItem.getPurchasingCapitalAssetSystem();
            asset = assetItem.getAndResetNewPurchasingItemCapitalAssetLine();
            asset.setCapitalAssetSystemIdentifier(system.getCapitalAssetSystemIdentifier());
            if (capitalAssetSystemHasAssetItem(system, asset)) {
                GlobalVariables.getMessageMap().putError(PurapConstants.CAPITAL_ASSET_TAB_ERRORS, PurapKeyConstants.ERROR_CAPITAL_ASSET_DUPLICATE_ASSET);
            } else {
                system.getItemCapitalAssets().add(asset);
            }
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }
    
    private boolean capitalAssetSystemHasAssetItem(CapitalAssetSystem system, ItemCapitalAsset asset) {
        return system.getItemCapitalAssets()
                .stream()
                .map(ItemCapitalAsset::getCapitalAssetNumber)
                .collect(Collectors.toList())
                .contains(asset.getCapitalAssetNumber());
    }    

    public ActionForward deleteItemCapitalAssetByDocument(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingFormBase purchasingForm = (PurchasingFormBase) form;
        PurchasingDocument purDocument = (PurchasingDocument) purchasingForm.getDocument();
        // get specific asset item
        PurchasingCapitalAssetItem assetItem = purDocument.getPurchasingCapitalAssetItems().get(getSelectedLine(request));
        ItemCapitalAsset asset = assetItem.getNewPurchasingItemCapitalAssetLine();

        boolean rulePassed = true;
        if (rulePassed) {
            String fullParameter = (String) request.getAttribute(KFSConstants.METHOD_TO_CALL_ATTRIBUTE);
            String systemIndex = StringUtils.substringBetween(fullParameter, KFSConstants.METHOD_TO_CALL_PARM1_LEFT_DEL, KFSConstants.METHOD_TO_CALL_PARM1_RIGHT_DEL);
            String assetIndex = StringUtils.substringBetween(fullParameter, KFSConstants.METHOD_TO_CALL_PARM2_LEFT_DEL, KFSConstants.METHOD_TO_CALL_PARM2_RIGHT_DEL);

            PurchasingCapitalAssetSystemBase system = (PurchasingCapitalAssetSystemBase) purDocument.getPurchasingCapitalAssetSystems().get(Integer.parseInt(systemIndex));
            system.getItemCapitalAssets().remove(Integer.parseInt(assetIndex));
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward deleteItemCapitalAssetByItem(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingFormBase purchasingForm = (PurchasingFormBase) form;
        PurchasingDocument purDocument = (PurchasingDocument) purchasingForm.getDocument();
        // get specific asset item
        PurchasingCapitalAssetItem assetItem = purDocument.getPurchasingCapitalAssetItems().get(getSelectedLine(request));

        ItemCapitalAsset asset = assetItem.getNewPurchasingItemCapitalAssetLine();

        boolean rulePassed = true;
        if (rulePassed) {
            String fullParameter = (String) request.getAttribute(KFSConstants.METHOD_TO_CALL_ATTRIBUTE);
            String assetIndex = StringUtils.substringBetween(fullParameter, KFSConstants.METHOD_TO_CALL_PARM2_LEFT_DEL, KFSConstants.METHOD_TO_CALL_PARM2_RIGHT_DEL);
            PurchasingCapitalAssetSystemBase system = (PurchasingCapitalAssetSystemBase) assetItem.getPurchasingCapitalAssetSystem();
            system.getItemCapitalAssets().remove(Integer.parseInt(assetIndex));
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward addCapitalAssetLocationByDocument(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingFormBase purchasingForm = (PurchasingFormBase) form;
        CapitalAssetLocation location = purchasingForm.getAndResetNewPurchasingCapitalAssetLocationLine();
        PurchasingDocument purDocument = (PurchasingDocument) purchasingForm.getDocument();

        boolean rulePassed = SpringContext.getBean(KualiRuleService.class).applyRules(new AttributedAddPurchasingCapitalAssetLocationEvent("", purDocument, location));
        rulePassed = rulePassed && SpringContext.getBean(PurchasingService.class).checkCapitalAssetLocation(location);

        if (rulePassed) {
            // get specific asset item and grab system as well and attach asset number
            CapitalAssetSystem system = purDocument.getPurchasingCapitalAssetSystems().get(getSelectedLine(request));
            location.setCapitalAssetSystemIdentifier(system.getCapitalAssetSystemIdentifier());
            system.getCapitalAssetLocations().add(location);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward addCapitalAssetLocationByItem(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingFormBase purchasingForm = (PurchasingFormBase) form;
        PurchasingDocument purDocument = (PurchasingDocument) purchasingForm.getDocument();
        CapitalAssetLocation location = purDocument.getPurchasingCapitalAssetItems().get(getSelectedLine(request)).getPurchasingCapitalAssetSystem().getNewPurchasingCapitalAssetLocationLine();
        boolean rulePassed = SpringContext.getBean(KualiRuleService.class).applyRules(new AttributedAddPurchasingCapitalAssetLocationEvent("", purDocument, location));

        if (rulePassed) {
            // get specific asset item and grab system as well and attach asset location
            PurchasingCapitalAssetItem assetItem = purDocument.getPurchasingCapitalAssetItems().get(getSelectedLine(request));
            CapitalAssetSystem system = assetItem.getPurchasingCapitalAssetSystem();
            location.setCapitalAssetSystemIdentifier(system.getCapitalAssetSystemIdentifier());
            system.getCapitalAssetLocations().add(location);
            // now reset the location as all the rules are passed successfully
            purDocument.getPurchasingCapitalAssetItems().get(getSelectedLine(request)).getPurchasingCapitalAssetSystem().resetNewPurchasingCapitalAssetLocationLine();
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward deleteCapitalAssetLocationByDocument(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingFormBase purchasingForm = (PurchasingFormBase) form;
        PurchasingDocument purDocument = (PurchasingDocument) purchasingForm.getDocument();

        String fullParameter = (String) request.getAttribute(KFSConstants.METHOD_TO_CALL_ATTRIBUTE);
        String systemIndex = StringUtils.substringBetween(fullParameter, KFSConstants.METHOD_TO_CALL_PARM1_LEFT_DEL, KFSConstants.METHOD_TO_CALL_PARM1_RIGHT_DEL);
        String locationIndex = StringUtils.substringBetween(fullParameter, KFSConstants.METHOD_TO_CALL_PARM2_LEFT_DEL, KFSConstants.METHOD_TO_CALL_PARM2_RIGHT_DEL);

        // get specific asset item and grab system as well and attach asset number
        CapitalAssetSystem system = purDocument.getPurchasingCapitalAssetSystems().get(Integer.parseInt(systemIndex));
        system.getCapitalAssetLocations().remove(Integer.parseInt(locationIndex));


        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward deleteCapitalAssetLocationByItem(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingFormBase purchasingForm = (PurchasingFormBase) form;
        PurchasingDocument purDocument = (PurchasingDocument) purchasingForm.getDocument();

        String fullParameter = (String) request.getAttribute(KFSConstants.METHOD_TO_CALL_ATTRIBUTE);
        String assetItemIndex = StringUtils.substringBetween(fullParameter, KFSConstants.METHOD_TO_CALL_PARM1_LEFT_DEL, KFSConstants.METHOD_TO_CALL_PARM1_RIGHT_DEL);
        String locationIndex = StringUtils.substringBetween(fullParameter, KFSConstants.METHOD_TO_CALL_PARM2_LEFT_DEL, KFSConstants.METHOD_TO_CALL_PARM2_RIGHT_DEL);

        // get specific asset item and grab system as well and attach asset number
        PurchasingCapitalAssetItem assetItem = purDocument.getPurchasingCapitalAssetItems().get(Integer.parseInt(assetItemIndex));
        CapitalAssetSystem system = assetItem.getPurchasingCapitalAssetSystem();
        system.getCapitalAssetLocations().remove(Integer.parseInt(locationIndex));


        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward setupCAMSSystem(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingAccountsPayableFormBase purchasingForm = (PurchasingAccountsPayableFormBase) form;
        PurchasingDocument document = (PurchasingDocument) purchasingForm.getDocument();
        SpringContext.getBean(PurchasingService.class).setupCapitalAssetSystem(document);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward selectSystem(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingAccountsPayableFormBase purchasingForm = (PurchasingAccountsPayableFormBase) form;
        PurchasingDocument document = (PurchasingDocument) purchasingForm.getDocument();
        String errorPath = PurapConstants.CAPITAL_ASSET_TAB_ERRORS;
        // validate entry is selected for each field
        if (StringUtils.isEmpty(document.getCapitalAssetSystemTypeCode())) {
            GlobalVariables.getMessageMap().putError(errorPath, KFSKeyConstants.ERROR_CUSTOM, "Capital Asset System Type and Capital Asset System State are both required to proceed");
        } else if (StringUtils.isEmpty(document.getCapitalAssetSystemStateCode())) {
            GlobalVariables.getMessageMap().putError(errorPath, KFSKeyConstants.ERROR_CUSTOM, "Capital Asset System Type and Capital Asset System State are both required to proceed");
        } else {
            document.refreshReferenceObject(PurapPropertyConstants.CAPITAL_ASSET_SYSTEM_TYPE);
            document.refreshReferenceObject(PurapPropertyConstants.CAPITAL_ASSET_SYSTEM_STATE);

            if (validateCapitalAssetSystemStateAllowed(document.getCapitalAssetSystemType(), document.getCapitalAssetSystemState())) {
                SpringContext.getBean(PurchasingService.class).setupCapitalAssetSystem(document);
                SpringContext.getBean(PurchasingService.class).setupCapitalAssetItems(document);
                if (!document.getPurchasingCapitalAssetItems().isEmpty()) {
                    saveDocumentNoValidationUsingClearErrorMap(document);
                } else {
                    // TODO: extract this and above strings to app resources
                    GlobalVariables.getMessageMap().putError(errorPath, KFSKeyConstants.ERROR_CUSTOM, "No items were found that met the requirements for Capital Asset data collection");
                }
                saveDocumentNoValidationUsingClearErrorMap(document);
            } else {
                // Blank out type selection, otherwise UI marks it read only
                document.setCapitalAssetSystemStateCode(null);
                document.setCapitalAssetSystemState(null);
            }
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }
    
    /**
     * Validate allowed capital asset system state codes
     *
     * @param capitalAssetSystemType that was selected
     * @param capitalAssetSystemState that was selected
     * @return whether the selected type allows the selected state
     */
    protected static boolean validateCapitalAssetSystemStateAllowed(CapitalAssetSystemType capitalAssetSystemType, CapitalAssetSystemState capitalAssetSystemState) {
        List<String> allowedCodes = Arrays.asList(capitalAssetSystemType.getAllowedCapitalAssetSystemStateCodes().split(";"));
        if (!allowedCodes.contains(capitalAssetSystemState.getCapitalAssetSystemStateCode())) {
            GlobalVariables.getMessageMap().putError(KFSPropertyConstants.DOCUMENT + "." + PurapPropertyConstants.CAPITAL_ASSET_SYSTEM_STATE_CODE, PurapKeyConstants.ERROR_CAPITAL_ASSET_NOT_ALLOWED_SYSTEM_TYPE, capitalAssetSystemType.getCapitalAssetSystemTypeDescription(), capitalAssetSystemState.getCapitalAssetSystemStateDescription());
            return false;
        }

        return true;
    }

    /**
     * Sets the error map to a new, empty error map before calling saveDocumentNoValidation to save the document.
     *
     * @param document The purchase order document to be saved.
     */
    protected void saveDocumentNoValidationUsingClearErrorMap(PurchasingDocument document) {
        MessageMap errorHolder = GlobalVariables.getMessageMap();
        GlobalVariables.setMessageMap(new MessageMap());
        try {
            SpringContext.getBean(PurapService.class).saveDocumentNoValidation(document);
        }
        finally {
            GlobalVariables.setMessageMap(errorHolder);
        }
    }

    public ActionForward changeSystem(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingAccountsPayableFormBase purchasingForm = (PurchasingAccountsPayableFormBase) form;
        PurchasingDocument document = (PurchasingDocument) purchasingForm.getDocument();
        Object question = request.getParameter(PurapConstants.QUESTION_INDEX);
        Object buttonClicked = request.getParameter(KFSConstants.QUESTION_CLICKED_BUTTON);

        if (question == null) {
            String questionText = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(PurapKeyConstants.PURCHASING_QUESTION_CONFIRM_CHANGE_SYSTEM);

            return this.performQuestionWithoutInput(mapping, form, request, response, PurapConstants.CapitalAssetTabStrings.SYSTEM_SWITCHING_QUESTION, questionText, KFSConstants.CONFIRMATION_QUESTION, KFSConstants.ROUTE_METHOD, "0");
        }
        else if (ConfirmationQuestion.YES.equals(buttonClicked)) {
            // Add a note if system change occurs when the document is a PO that is being amended.
            if ((document instanceof PurchaseOrderDocument) && (PurchaseOrderStatuses.APPDOC_CHANGE_IN_PROCESS.equals(document.getApplicationDocumentStatus()))) {
                Integer poId = document.getPurapDocumentIdentifier();
                PurchaseOrderDocument currentPO = SpringContext.getBean(PurchaseOrderService.class).getCurrentPurchaseOrder(poId);
                String oldSystemTypeCode = "";
                if (currentPO != null) {
                    oldSystemTypeCode = currentPO.getCapitalAssetSystemTypeCode();
                }
                CapitalAssetSystemType oldSystemType = new CapitalAssetSystemType();
                oldSystemType.setCapitalAssetSystemTypeCode(oldSystemTypeCode);
                Map<String, String> keys = SpringContext.getBean(PersistenceService.class).getPrimaryKeyFieldValues(oldSystemType);
                oldSystemType = SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(CapitalAssetSystemType.class, keys);
                String description = ((oldSystemType == null) ? "(NONE)" : oldSystemType.getCapitalAssetSystemTypeDescription());

                if (document instanceof PurchaseOrderAmendmentDocument) {
                    String noteText = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(PurapKeyConstants.PURCHASE_ORDER_AMEND_MESSAGE_CHANGE_SYSTEM_TYPE);
                    noteText = StringUtils.replace(noteText, "{0}", description);

                    try {
                        Note systemTypeChangeNote = getDocumentService().createNoteFromDocument(document, noteText);
                        purchasingForm.setNewNote(systemTypeChangeNote);
                        insertBONote(mapping, purchasingForm, request, response);
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            if (form instanceof RequisitionForm) {
                ((RequisitionForm) form).resetNewPurchasingCapitalAssetLocationLine();
            }
            // remove capital assets from db
            if (document instanceof PurchaseOrderAmendmentDocument) {
                for (PurchasingCapitalAssetItem assetItem : document.getPurchasingCapitalAssetItems()) {
                    SpringContext.getBean(BusinessObjectService.class).delete((PersistableBusinessObject) assetItem);
                }
            }
            document.clearCapitalAssetFields();

            SpringContext.getBean(PurapService.class).saveDocumentNoValidation(document);
            KNSGlobalVariables.getMessageList().add(PurapKeyConstants.PURCHASING_MESSAGE_SYSTEM_CHANGED);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward updateCamsView(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingAccountsPayableFormBase purchasingForm = (PurchasingAccountsPayableFormBase) form;
        PurchasingDocument document = (PurchasingDocument) purchasingForm.getDocument();

        SpringContext.getBean(PurchasingService.class).setupCapitalAssetItems(document);
        
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward setManufacturerFromVendorByDocument(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingAccountsPayableFormBase purchasingForm = (PurchasingAccountsPayableFormBase) form;
        PurchasingDocument document = (PurchasingDocument) purchasingForm.getDocument();

        String vendorName = document.getVendorName();
        if (StringUtils.isEmpty(vendorName)) {
            GlobalVariables.getMessageMap().putError(PurapConstants.CAPITAL_ASSET_TAB_ERRORS, PurapKeyConstants.ERROR_CAPITAL_ASSET_NO_VENDOR, (String[]) null);
        }
        else {
            CapitalAssetSystem system = document.getPurchasingCapitalAssetSystems().get(getSelectedLine(request));
            if (system != null) {
                system.setCapitalAssetManufacturerName(vendorName);
            }
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward setManufacturerFromVendorByItem(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingAccountsPayableFormBase purchasingForm = (PurchasingAccountsPayableFormBase) form;
        PurchasingDocument document = (PurchasingDocument) purchasingForm.getDocument();

        String vendorName = document.getVendorName();
        if (StringUtils.isEmpty(vendorName)) {
            GlobalVariables.getMessageMap().putError(PurapConstants.CAPITAL_ASSET_TAB_ERRORS, PurapKeyConstants.ERROR_CAPITAL_ASSET_NO_VENDOR, (String[]) null);
        }
        else {
            PurchasingCapitalAssetItem assetItem = document.getPurchasingCapitalAssetItems().get(getSelectedLine(request));
            CapitalAssetSystem system = assetItem.getPurchasingCapitalAssetSystem();
            if (system != null) {
                system.setCapitalAssetManufacturerName(vendorName);
            }
        }
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward selectNotCurrentYearByDocument(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingAccountsPayableFormBase purchasingForm = (PurchasingAccountsPayableFormBase) form;
        PurchasingDocument document = (PurchasingDocument) purchasingForm.getDocument();

        CapitalAssetSystem system = document.getPurchasingCapitalAssetSystems().get(getSelectedLine(request));
        if (system != null) {
            system.setCapitalAssetNotReceivedCurrentFiscalYearIndicator(true);
            system.setCapitalAssetTypeCode(SpringContext.getBean(PurchasingService.class).getDefaultAssetTypeCodeNotThisFiscalYear());
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward selectNotCurrentYearByItem(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingAccountsPayableFormBase purchasingForm = (PurchasingAccountsPayableFormBase) form;
        PurchasingDocument document = (PurchasingDocument) purchasingForm.getDocument();

        PurchasingCapitalAssetItem assetItem = document.getPurchasingCapitalAssetItems().get(getSelectedLine(request));
        CapitalAssetSystem system = assetItem.getPurchasingCapitalAssetSystem();
        if (system != null) {
            system.setCapitalAssetNotReceivedCurrentFiscalYearIndicator(true);
            system.setCapitalAssetTypeCode(SpringContext.getBean(PurchasingService.class).getDefaultAssetTypeCodeNotThisFiscalYear());
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward clearNotCurrentYearByDocument(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingAccountsPayableFormBase purchasingForm = (PurchasingAccountsPayableFormBase) form;
        PurchasingDocument document = (PurchasingDocument) purchasingForm.getDocument();

        CapitalAssetSystem system = document.getPurchasingCapitalAssetSystems().get(getSelectedLine(request));
        if (system != null) {
            system.setCapitalAssetNotReceivedCurrentFiscalYearIndicator(false);
            system.setCapitalAssetTypeCode("");
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward clearNotCurrentYearByItem(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingAccountsPayableFormBase purchasingForm = (PurchasingAccountsPayableFormBase) form;
        PurchasingDocument document = (PurchasingDocument) purchasingForm.getDocument();

        PurchasingCapitalAssetItem assetItem = document.getPurchasingCapitalAssetItems().get(getSelectedLine(request));
        CapitalAssetSystem system = assetItem.getPurchasingCapitalAssetSystem();
        if (system != null) {
            system.setCapitalAssetNotReceivedCurrentFiscalYearIndicator(false);
            system.setCapitalAssetTypeCode("");
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    @Override
    public ActionForward calculate(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingAccountsPayableFormBase purchasingForm = (PurchasingAccountsPayableFormBase) form;
        PurchasingDocument purDoc = (PurchasingDocument) purchasingForm.getDocument();

        boolean defaultUseTaxIndicatorValue = SpringContext.getBean(PurchasingService.class).getDefaultUseTaxIndicatorValue(purDoc);
        SpringContext.getBean(PurapService.class).updateUseTaxIndicator(purDoc, defaultUseTaxIndicatorValue);
        SpringContext.getBean(PurapService.class).calculateTax(purDoc);

        // call prorateDiscountTradeIn
        SpringContext.getBean(PurapService.class).prorateForTradeInAndFullOrderDiscount(purDoc);

        // recalculate the amounts and percents on the accounting line.
        SpringContext.getBean(PurapAccountingService.class).updateAccountAmounts(purDoc);

        customCalculate(purDoc);

        PurchasingFormBase formBase = (PurchasingFormBase) form;
        formBase.setInitialZipCode(purDoc.getDeliveryPostalCode());
        formBase.setCalculated(true);
        purDoc.setCalculated(true);

        KNSGlobalVariables.getMessageList().clear();

        return super.calculate(mapping, form, request, response);
    }

    @Override
    protected void loadDocument(KualiDocumentFormBase kualiDocumentFormBase) {
        super.loadDocument(kualiDocumentFormBase);
        PurchasingFormBase formBase = (PurchasingFormBase) kualiDocumentFormBase;
        if (StringUtils.isEmpty(formBase.getInitialZipCode())) {
            formBase.setInitialZipCode(((PurchasingDocument) formBase.getDocument()).getDeliveryPostalCode());
        }
    }

    @Override
    public ActionForward clearAllTaxes(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingAccountsPayableFormBase purchasingForm = (PurchasingAccountsPayableFormBase) form;
        PurchasingDocument purDoc = (PurchasingDocument) purchasingForm.getDocument();

        SpringContext.getBean(PurapService.class).clearAllTaxes(purDoc);

        return super.clearAllTaxes(mapping, form, request, response);
    }

    /**
     * Determine from request parameters if user is returning from capital asset building lookup. Parameter will start with either
     * document.purchasingCapitalAssetItems or document.purchasingCapitalAssetSystems
     *
     * @param request
     * @return
     */
    protected String findBuildingCodeFromCapitalAssetBuildingLookup(HttpServletRequest request) {
        Enumeration anEnum = request.getParameterNames();
        while (anEnum.hasMoreElements()) {
            String paramName = (String) anEnum.nextElement();
            if (StringUtils.containsIgnoreCase(paramName, "purchasingcapitalasset") && paramName.contains("buildingCode")) {
                return paramName;
            }
        }
        return "";
    }
    
    protected List<String> findAllBuildingCodesFromCapitalAssetBuildingLookup(HttpServletRequest request) {
        List<String> buildingCodes = new ArrayList<>();
        Enumeration anEnum = request.getParameterNames();
        while (anEnum.hasMoreElements()) {
            String paramName = (String) anEnum.nextElement();
            if (StringUtils.containsIgnoreCase(paramName, "purchasingcapitalasset") && paramName.contains("buildingCode")) {
                buildingCodes.add(paramName);
            }
        }
        return buildingCodes;
    }

    /**
     * Overrides the superclass method so that it will also do proration for trade in and full order discount when the user clicks
     * on the submit button.
     *
     * @see org.kuali.kfs.sys.web.struts.KualiAccountingDocumentActionBase#route(org.apache.struts.action.ActionMapping,
     *      org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward route(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingFormBase purchasingForm = (PurchasingFormBase) form;
        PurchasingDocument purDoc = (PurchasingDocument) purchasingForm.getDocument();

        // if form is not yet calculated, return and prompt user to calculate
        if (requiresCalculate(purchasingForm)) {
            GlobalVariables.getMessageMap().putError(KFSConstants.DOCUMENT_ERRORS, PurapKeyConstants.ERROR_PURCHASING_REQUIRES_CALCULATE);

            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }

        // TODO : should combine all validation errors and return at the same time
		if (isAttachmentSizeExceedSqLimit(form, "route") || isReasonToChangeRequired(form)) {
			return mapping.findForward(KFSConstants.MAPPING_BASIC);
		}
		// save this flag before notes is saved during route
		boolean isCreatingReasonNote = isCreatingReasonNote(form);
        // call prorateDiscountTradeIn
        SpringContext.getBean(PurapService.class).prorateForTradeInAndFullOrderDiscount(purDoc);

        ActionForward forward = super.route(mapping, form, request, response);
        if (GlobalVariables.getMessageMap().hasNoErrors() && isCreatingReasonNote) {
    		createReasonNote(form);
        }
        return forward;
    }

    /**
     * Overrides the superclass method so that it will also do proration for trade in and full order discount when the user clicks
     * on the approve button.
     *
     * @see org.kuali.kfs.kns.web.struts.action.KualiDocumentActionBase#approve(org.apache.struts.action.ActionMapping,
     *      org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward approve(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
    	PurchasingFormBase purchasingForm = (PurchasingFormBase) form;
        PurchasingDocument purDoc = (PurchasingDocument) purchasingForm.getDocument();
		if (isAttachmentSizeExceedSqLimit(form, "approve") || isReasonToChangeRequired(form)) {
			return mapping.findForward(KFSConstants.MAPPING_BASIC);
		}

		boolean isCreatingReasonNote = isCreatingReasonNote(form);
		if (isCreatingReasonNote) {
			// save here, so it can be picked up in b2b
            SpringContext.getBean(NoteService.class).saveNoteList(purDoc.getNotes());

		}
       // call prorateDiscountTradeIn
        SpringContext.getBean(PurapService.class).prorateForTradeInAndFullOrderDiscount(purDoc);
        
        ActionForward forward = super.approve(mapping, form, request, response);
        
        if (GlobalVariables.getMessageMap().hasNoErrors() && isCreatingReasonNote) {
    		createReasonNote(form);
        }
        return forward;

    }

    @Override
    public ActionForward blanketApprove(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingFormBase purchasingForm = (PurchasingFormBase) form;
        PurchasingDocument purDoc = (PurchasingDocument) purchasingForm.getDocument();
		if (isAttachmentSizeExceedSqLimit(form, "blanket approve") || isReasonToChangeRequired(form)) {
			return mapping.findForward(KFSConstants.MAPPING_BASIC);
		}
		boolean isCreatingReasonNote = isCreatingReasonNote(form);
        // call prorateDiscountTradeIn
        SpringContext.getBean(PurapService.class).prorateForTradeInAndFullOrderDiscount(purDoc);
        ActionForward forward = super.blanketApprove(mapping, form, request, response);
        if (GlobalVariables.getMessageMap().hasNoErrors() && isCreatingReasonNote) {
    		createReasonNote(form);
        }
        return forward;
    }
    
    @Override
    public ActionForward insertBONote(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
       // TODO : should use 'addnoteevent' ?
    	/*
    	 * KFSPTS-794 : add rule check when adding note
    	 */
    	KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
    	FormFile attachmentFile = kualiDocumentFormBase.getAttachmentFile();
    	Note newNote = kualiDocumentFormBase.getNewNote();
		if (StringUtils.equals(CUPurapConstants.AttachemntToVendorIndicators.SEND_TO_VENDOR,newNote.getNoteTopicText())) {
			if (StringUtils.isBlank(attachmentFile.getFileName())) {
				GlobalVariables.getMessageMap().putError(String.format("%s.%s",KRADConstants.NEW_DOCUMENT_NOTE_PROPERTY_NAME,
						KRADConstants.NOTE_TOPIC_TEXT_PROPERTY_NAME), CUPurapKeyConstants.ERROR_ADD_NEW_NOTE_SEND_TO_VENDOR_NO_ATT);
				return mapping.findForward(KFSConstants.MAPPING_BASIC);
			} else {
				if (isAttachmentSizeExceedSqLimit(form, "add")) {
					return mapping.findForward(KFSConstants.MAPPING_BASIC);
				} else if (attachmentFile.getFileSize() > SIZE_5MB) {
					GlobalVariables.getMessageMap().putError(String.format("%s.%s",KRADConstants.NEW_DOCUMENT_NOTE_PROPERTY_NAME,
							KRADConstants.NOTE_TOPIC_TEXT_PROPERTY_NAME), CUPurapKeyConstants.ERROR_ATT_FILE_SIZE_OVER_LIMIT, attachmentFile.getFileName(), "5");
					return mapping.findForward(KFSConstants.MAPPING_BASIC);					
				}
			}
		}
		newNote.setNoteTypeCode(KFSConstants.NoteTypeEnum.DOCUMENT_HEADER_NOTE_TYPE.getCode());
		
        return super.insertBONote(mapping, form, request, response);
    }
    
    /*
     * check if number of attachments, that are sent to SQ, is over the limit.
     * default to 10, and also use a system param to make it flexible.
     */
    private boolean isAttachmentSizeExceedSqLimit(ActionForm form, String action) {
    	boolean isExceed = false;
    	KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
		String attachmentSize = "10";
        int addOne = StringUtils.equals("add", action) ? 1  : 0;
		try {
			attachmentSize = SpringContext.getBean(ParameterService.class).getParameterValueAsString(PurapConstants.PURAP_NAMESPACE, KRADConstants.DetailTypes.ALL_DETAIL_TYPE,
					CUPurapConstants.MAX_SQ_NO_ATTACHMENTS);
			
		} catch (Exception e) {
			// param not found
			LOG.info("Parameter MAX_SQ_NO_ATTACHMENTS not found");
		}
		if (StringUtils.isNotBlank(attachmentSize)
				&& Integer.parseInt(attachmentSize)  < (getNumberOfNotesToSendToVendor(kualiDocumentFormBase.getDocument()) + addOne)) {
			GlobalVariables.getMessageMap().putError(String.format("%s.%s",KRADConstants.NEW_DOCUMENT_NOTE_PROPERTY_NAME,
					KRADConstants.NOTE_TOPIC_TEXT_PROPERTY_NAME),CUPurapKeyConstants.ERROR_EXCEED_SQ_NUMBER_OF_ATT_LIMIT, action, attachmentSize);
			isExceed = true;
		}
		return isExceed;
    }
    
    /*
     * check if the 'send to vendor' flag is changed.  
     */
    private boolean isAttachmentReqChanged(ActionForm form) {
    	boolean ischanged = false;
    	KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        List<Note> savedNotes = getPersistedBoNotesNotes(kualiDocumentFormBase.getDocument());
        List<Note> boNotes = kualiDocumentFormBase.getDocument().getNotes();
        if (!(kualiDocumentFormBase.getDocument() instanceof RequisitionDocument)) {
        	restoreSendToVendorFlag(boNotes, ((CuPurchaseOrderForm)kualiDocumentFormBase).getCopiedNotes());
        }
        boolean isChanged = false;
        for (Note savedNote : savedNotes) {
        	for (Note note : boNotes) {
        		if (note.getNoteIdentifier().equals(savedNote.getNoteIdentifier()) && !StringUtils.equals(note.getNoteTopicText(), savedNote.getNoteTopicText())
        				&& (StringUtils.equalsIgnoreCase(note.getNoteTopicText(), CUPurapConstants.AttachemntToVendorIndicators.SEND_TO_VENDOR) || StringUtils.equalsIgnoreCase(savedNote.getNoteTopicText(), CUPurapConstants.AttachemntToVendorIndicators.SEND_TO_VENDOR))) {
        			isChanged = true;
        			break;
        		}
        		
        	}
        }
		return isChanged;
    }

    /*
     * This is for PO/POA which will refresh bonote during populate.  This is just to restore what is in form document
     */
    private void restoreSendToVendorFlag(List<Note> boNotes, List<Note> curNotes) {
		if (CollectionUtils.isNotEmpty(curNotes) && CollectionUtils.isNotEmpty(boNotes)) {
			for (Note curNote : curNotes) {
				for (Note note : boNotes) {
					if (note.getNoteIdentifier().equals(curNote.getNoteIdentifier())&& !StringUtils.equals(note.getNoteTopicText(),curNote.getNoteTopicText())) {
						note.setNoteTopicText(curNote.getNoteTopicText());
					}

				}
			}
		}

    }
    
    /*
     * check if reason is required
     */
    private boolean isReasonToChangeRequired(ActionForm form) {
    	boolean isReasonRequired = false;
    	KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;

		if (isCreatingReasonNote(form) && StringUtils.isBlank(((PurchasingFormBase)kualiDocumentFormBase).getReasonToChange())) {
			GlobalVariables.getMessageMap().putError(String.format("%s.%s",KRADConstants.NEW_DOCUMENT_NOTE_PROPERTY_NAME,
					KRADConstants.NOTE_TOPIC_TEXT_PROPERTY_NAME),CUPurapKeyConstants.ERROR_REASON_IS_REQUIRED);
			isReasonRequired = true;
		}
		return isReasonRequired;
    }

    @Override
    public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (isAttachmentSizeExceedSqLimit(form, "save") || isReasonToChangeRequired(form)) {
			return mapping.findForward(KFSConstants.MAPPING_BASIC);
		}
    	KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
		// save this flag before notes is saved during route
		boolean isCreatingReasonNote = isCreatingReasonNote(form);
//    	else if (!(kualiDocumentFormBase.getDocument() instanceof RequisitionDocument)) {
//        	restoreSendToVendorFlag(kualiDocumentFormBase.getDocument().getDocumentBusinessObject().getBoNotes(), ((PurchaseOrderForm)kualiDocumentFormBase).getCopiedNotes());
//        }

		
		ActionForward forward =  super.save(mapping, form, request, response);

        if (GlobalVariables.getMessageMap().hasNoErrors() && isCreatingReasonNote) {
    		createReasonNote(form);
        }
        return forward;

    }

    // only if it is still enroute
    /*
     * check if it needs to create a note for changing 'send to vendor'
     */
    private boolean isCreatingReasonNote(ActionForm form) {
    	KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        return 	(((PurchasingFormBase)kualiDocumentFormBase).isDocEnroute() || !(kualiDocumentFormBase.getDocument() instanceof RequisitionDocument)) 
        		&& isAttachmentReqChanged(form);
    }
    
    /*
     * creating the change to vendor reason note.
     */
	private void createReasonNote(ActionForm form) {
		// TODO : move to service ?
    	KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        try {
            Note noteObj = SpringContext.getBean(DocumentService.class).createNoteFromDocument(kualiDocumentFormBase.getDocument(), ((PurchasingFormBase)kualiDocumentFormBase).getReasonToChange());
            populateIdentifierOnNoteAndExtension(noteObj);
            kualiDocumentFormBase.getDocument().addNote(noteObj);
            if (doesDocumentAllowImmediateSaveOfNewNote(kualiDocumentFormBase.getDocument(), noteObj)) {
                getNoteService().save(noteObj);
            }
            ((PurchasingFormBase)kualiDocumentFormBase).setReasonToChange(KFSConstants.EMPTY_STRING);
        }
        catch(Exception e){
            String errorMessage = "Error creating and saving close note for reason change requirement with document service";
            LOG.error("createReasonNote " + errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }   
        
	}

    private void populateIdentifierOnNoteAndExtension(Note note) {
        Long nextNoteId = getSequenceAccessorService().getNextAvailableSequenceNumber(CUKFSConstants.NOTE_SEQUENCE_NAME);
        NoteExtendedAttribute extension = (NoteExtendedAttribute) note.getExtension();
        note.setNoteIdentifier(nextNoteId);
        extension.setNoteIdentifier(nextNoteId);
    }

    private boolean doesDocumentAllowImmediateSaveOfNewNote(Document document, Note note) {
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        PersistableBusinessObject noteTarget = document.getNoteTarget();
        // This is the same logic used by the KualiDocumentActionBase.insertBONote() method.
        return !workflowDocument.isInitiated() && StringUtils.isNotBlank(noteTarget.getObjectId())
                && !(document instanceof MaintenanceDocument && StringUtils.equals(NoteType.BUSINESS_OBJECT.getCode(), note.getNoteTypeCode()));
    }

    private List<Note> getPersistedBoNotesNotes(Document document) {
    	
        List<Note> notes = new ArrayList<Note>();
        if (document instanceof RequisitionDocument) {
            notes = SpringContext.getBean(NoteService.class).getByRemoteObjectId(document.getObjectId());
        } else {
             notes = SpringContext.getBean(PurchaseOrderService.class).getPurchaseOrderNotes(((PurchaseOrderDocument)document).getPurapDocumentIdentifier());
        	
        }
        return notes;
    }

    private int getNumberOfNotesToSendToVendor(Document purchaseOrder) {
    	int numberOfNotesToSendToVendor = 0;
    	// for POA purchaseOrder.getBoNotes() is empty, but purchaseOrder.getDocumentBusinessObject().getBoNotes() is not.
        List<Note> notesToSend = purchaseOrder.getNotes();
        if (CollectionUtils.isNotEmpty(notesToSend)) {
        	for (Note note : notesToSend) {
                if (StringUtils.equalsIgnoreCase(note.getNoteTopicText(), CUPurapConstants.AttachemntToVendorIndicators.SEND_TO_VENDOR)) {
            	    numberOfNotesToSendToVendor++;
                }
        	}
        }
        return numberOfNotesToSendToVendor;
    }

    @Override
    public ActionForward close(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        KualiDocumentFormBase docForm = (KualiDocumentFormBase) form;
        doProcessingAfterPost( docForm, request );

        // only want to prompt them to save if they already can save
        if (canSave(docForm)) {
            Object question = getQuestion(request);
            // logic for close question
            if (question == null) {
                // ask question if not already asked
                return this.performQuestionWithoutInput(mapping, form, request, response, KRADConstants.DOCUMENT_SAVE_BEFORE_CLOSE_QUESTION, getKualiConfigurationService().getPropertyValueAsString(KFSKeyConstants.QUESTION_SAVE_BEFORE_CLOSE), KRADConstants.CONFIRMATION_QUESTION, KRADConstants.MAPPING_CLOSE, "");
            }
            else {
                Object buttonClicked = request.getParameter(KRADConstants.QUESTION_CLICKED_BUTTON);
                if ((KRADConstants.DOCUMENT_SAVE_BEFORE_CLOSE_QUESTION.equals(question)) && ConfirmationQuestion.YES.equals(buttonClicked)) {
                    // if yes button clicked - save the doc
            		if (isAttachmentSizeExceedSqLimit(form, "save") || isReasonToChangeRequired(form)) {
            			return mapping.findForward(KFSConstants.MAPPING_BASIC);
            		} else {
            	    	if (isCreatingReasonNote(form)) {
            	    		createReasonNote(form);
            	    	}
                        getDocumentService().saveDocument(docForm.getDocument());
            		}
                }
                // else go to close logic below
            }
        }

        return returnToSender(request, mapping, docForm);
    }


    /**
     * Checks if calculation is required. Currently it is required when it has not already been calculated and if the user can
     * perform calculate
     *
     * @return true if calculation is required, false otherwise
     */
    protected boolean requiresCalculate(PurchasingFormBase purForm) {
        boolean requiresCalculate = true;

        requiresCalculate = !purForm.isCalculated() && purForm.canUserCalculate();

        return requiresCalculate;
    }

    public ActionForward populateBuilding(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingFormBase purForm = (PurchasingFormBase) form;
        PurchasingDocumentBase document = (PurchasingDocumentBase) purForm.getDocument();
        updateAssetBuildingLocations(purForm, request, document);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }
    
    public ActionForward populateDeliveryBuilding(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PurchasingFormBase purForm = (PurchasingFormBase) form;
        PurchasingDocumentBase document = (PurchasingDocumentBase) purForm.getDocument();
        updateDeliveryBuilding(request, document);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }
    
    // KFSPTS_985, KFSUPGRADE-75
    
    protected void populatePrimaryFavoriteAccount(List<PurApAccountingLine> sourceAccountinglines, Class<? extends PurApAccountingLine> accountingLineClass) {
        UserFavoriteAccountService userFavoriteAccountService = SpringContext.getBean(UserFavoriteAccountService.class);
    	FavoriteAccount account =  userFavoriteAccountService.getFavoriteAccount(GlobalVariables.getUserSession().getPrincipalId());
    	if (ObjectUtils.isNotNull(account)) {
    		sourceAccountinglines.add(userFavoriteAccountService.getPopulatedNewAccount(account, accountingLineClass));
    	}
    }
   
    /**
     * Gets the actual source accounting line class by retrieving and examining
     * a new instance from the form's setupNewPurchasingAccountingLine() method.
     * This is needed for cases where the document's source line implementation
     * does not match the one configured in its associated data dictionary group.
     *
     * @param purchasingForm The document form to retrieve the new accounting line from; cannot be null.
     * @return The implementation class of the document form's new source accounting lines.
     */
    protected Class<? extends PurApAccountingLine> getAccountClassFromNewPurApAccountingLine(PurchasingFormBase purchasingForm) {
        return purchasingForm.setupNewPurchasingAccountingLine().getClass();
    }

    /*
     * KFSPTS-985 : add favorite account.
     * This is a copy from requisitionaction.  to be shared by both req & po
     */
    public ActionForward addFavoriteAccount(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
    	PurchasingFormBase poForm = (PurchasingFormBase) form;
    	PurchasingDocumentBase document = (PurchasingDocumentBase) poForm.getDocument();

        int itemIdx = getSelectedLine(request);
        final int DISTRIBUTION_INDEX = -2;
        PurchasingFavoriteAccountLineBuilderBase<? extends PurApAccountingLine> favoriteAccountBuilder;

        // Initialize the correct builder based on whether the Favorite Account is for an item in the list or for account distribution.
		if (itemIdx >= 0) {
			PurchasingItemBase item = (PurchasingItemBase) document.getItem(itemIdx);
			favoriteAccountBuilder = new PurchasingFavoriteAccountLineBuilderForLineItem<PurApAccountingLine>(
					item, itemIdx, poForm.setupNewPurchasingAccountingLine());
		} else if (itemIdx == DISTRIBUTION_INDEX) {
			favoriteAccountBuilder = new PurchasingFavoriteAccountLineBuilderForDistribution<PurApAccountingLine>(
					document, poForm.getAccountDistributionsourceAccountingLines(), poForm.setupNewAccountDistributionAccountingLine());
		} else {
		    return mapping.findForward(KFSConstants.MAPPING_BASIC);
		}

		// Add a new Favorite-Account-derived accounting line to the list, with errors inserted into the message map as appropriate.
		favoriteAccountBuilder.addNewFavoriteAccountLineToListIfPossible();

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    protected SequenceAccessorService getSequenceAccessorService() {
        return SpringContext.getBean(SequenceAccessorService.class);
    }

}
