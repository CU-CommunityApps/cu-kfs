package edu.cornell.kfs.module.purap.document.service.impl;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapRuleConstants;
import org.kuali.kfs.module.purap.businessobject.RequisitionItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.service.impl.RequisitionServiceImpl;
import org.kuali.kfs.vnd.businessobject.VendorCommodityCode;
import org.kuali.kfs.vnd.businessobject.VendorContract;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kim.api.identity.Person;

import edu.cornell.kfs.module.purap.CUPurapParameterConstants;
import edu.cornell.kfs.module.purap.document.service.CuPurapService;

public class CuRequisitionServiceImpl extends RequisitionServiceImpl {
    private static final Logger LOG = LogManager.getLogger(CuRequisitionServiceImpl.class);
    
    /**
     * Checks the rule for Automatic Purchase Order eligibility of the requisition and return a String containing the reason why the
     * requisition was not eligible to become an APO if it was not eligible, or return an empty String if the requisition is
     * eligible to become an APO
     * 
     * @param requisition the requisition document to be checked for APO eligibility.
     * @return String containing the reason why the requisition was not eligible to become an APO if it was not eligible, or an
     *         empty String if the requisition is eligible to become an APO.
     */
    @Override
    protected String checkAutomaticPurchaseOrderRules(RequisitionDocument requisition) {
        String requisitionSource = requisition.getRequisitionSourceCode();
        KualiDecimal reqTotal = requisition.getTotalDollarAmount();
        KualiDecimal apoLimit = ((CuPurapService) purapService).getApoLimit(requisition);
        requisition.setOrganizationAutomaticPurchaseOrderLimit(apoLimit);

        if (LOG.isDebugEnabled()) {
            LOG.debug("isAPO() reqId = " + requisition.getPurapDocumentIdentifier() + "; apoLimit = " + apoLimit + "; reqTotal = " + reqTotal);
        }
        if (apoLimit == null) {
            return "APO limit is empty.";
        } else {
            if (reqTotal.compareTo(apoLimit) == 1) {
                return "Requisition total is greater than the APO limit.";
            }
        }

        if (reqTotal.compareTo(KualiDecimal.ZERO) <= 0) {
            return "Requisition total is not greater than zero.";
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("isAPO() vendor #" + requisition.getVendorHeaderGeneratedIdentifier() + "-" + requisition.getVendorDetailAssignedIdentifier());
        }
        if (requisition.getVendorHeaderGeneratedIdentifier() == null || requisition.getVendorDetailAssignedIdentifier() == null) {
            return "Vendor was not selected from the vendor database.";
        } else {
            VendorDetail vendorDetail = vendorService.getVendorDetail(requisition.getVendorHeaderGeneratedIdentifier(),
                    requisition.getVendorDetailAssignedIdentifier());
            if (vendorDetail == null) {
                return "Error retrieving vendor from the database.";
            }
            if (StringUtils.isBlank(requisition.getVendorLine1Address()) 
                    ||  StringUtils.isBlank(requisition.getVendorCityName()) 
                    || StringUtils.isBlank(requisition.getVendorCountryCode())) {
                return "Requisition does not have all of the vendor address fields that are required for Purchase Order.";
            }
            requisition.setVendorRestrictedIndicator(vendorDetail.getVendorRestrictedIndicator());
            if (requisition.getVendorRestrictedIndicator() != null && requisition.getVendorRestrictedIndicator()) {
                return "Selected vendor is marked as restricted.";
            }
            if (vendorDetail.isVendorDebarred()) {
                return "Selected vendor is marked as a debarred vendor";
            }
            requisition.setVendorDetail(vendorDetail);

            if ((!PurapConstants.RequisitionSources.B2B.equals(requisitionSource)) && ObjectUtils.isNull(requisition.getVendorContractGeneratedIdentifier())) {
               Person initiator = getPersonService().getPerson(requisition.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId());
                VendorContract b2bContract = vendorService.getVendorB2BContract(vendorDetail, initiator.getCampusCode());
                if (b2bContract != null) {
                    return "Standard requisition with no contract selected but a B2B contract exists for the selected vendor.";
                }
            }
            
            //vendor contract expirated date validation....KFSMI-8502
            // if the vendor is selected through vendor contract is selected 
            if (StringUtils.isNotBlank(requisition.getVendorContractName())) {
                boolean routeToCM = parameterService.getParameterValueAsBoolean(RequisitionDocument.class,
                        CUPurapParameterConstants.ROUTE_REQS_WITH_EXPIRED_CONTRACT_TO_CM, Boolean.FALSE);

                if (routeToCM && vendorService.isVendorContractExpired(requisition, requisition.getVendorContractGeneratedIdentifier(), vendorDetail)) {
                    return "Contracted Vendor used where the contract end date is expired.";
                }
            }
        }
        
        //if vendor address isn't complete, no APO
        if (StringUtils.isBlank(requisition.getVendorLine1Address()) 
                || StringUtils.isBlank(requisition.getVendorCityName()) 
                || StringUtils.isBlank(requisition.getVendorCountryCode()) 
                || !postalCodeValidationService.validateAddress(requisition.getVendorCountryCode(),
                        requisition.getVendorStateCode(), requisition.getVendorPostalCode(), "", "")) {
            return "Requistion does not contain a complete vendor address";
        }

        // These are needed for commodity codes. They are put in here so that
        // we don't have to loop through items too many times.
        String purchaseOrderRequiresCommodityCode = parameterService.getParameterValueAsString(PurchaseOrderDocument.class,
                PurapRuleConstants.ITEMS_REQUIRE_COMMODITY_CODE_IND);
        boolean commodityCodeRequired = "Y".equals(purchaseOrderRequiresCommodityCode);
        
        for (Iterator iter = requisition.getItems().iterator(); iter.hasNext();) {
            RequisitionItem item = (RequisitionItem) iter.next();
            if (item.isItemRestrictedIndicator()) {
                return "Requisition contains an item that is marked as restricted.";
            }
            
            //We only need to check the commodity codes if this is an above the line item.
            if (item.getItemType().isLineItemIndicator()) {
                String commodityCodesReason = "";
                List<VendorCommodityCode> vendorCommodityCodes = commodityCodeRequired ? requisition.getVendorDetail().getVendorCommodities() : null;
                commodityCodesReason = checkAPORulesPerItemForCommodityCodes(item, vendorCommodityCodes, commodityCodeRequired);
                if (StringUtils.isNotBlank(commodityCodesReason)) {
                    return commodityCodesReason;
                }
            }
            
            if (PurapConstants.ItemTypeCodes.ITEM_TYPE_ORDER_DISCOUNT_CODE.equals(item.getItemType().getItemTypeCode())
                    || PurapConstants.ItemTypeCodes.ITEM_TYPE_TRADE_IN_CODE.equals(item.getItemType().getItemTypeCode())) {
                if ((item.getItemUnitPrice() != null) && ((BigDecimal.ZERO.compareTo(item.getItemUnitPrice())) != 0)) {
                    // discount or trade-in item has unit price that is not empty or zero
                    return "Requisition contains a " + item.getItemType().getItemTypeDescription() + " item, so it does not qualify as an APO.";
                }
            }
            
            

        }

        

        if (StringUtils.isNotEmpty(requisition.getRecurringPaymentTypeCode())) {
            return "Payment type is marked as recurring.";
        }

        if ((requisition.getPurchaseOrderTotalLimit() != null) && (KualiDecimal.ZERO.compareTo(requisition.getPurchaseOrderTotalLimit()) != 0)) {
            LOG.debug("isAPO() po total limit is not null and not equal to zero; return false.");
            return "The 'PO not to exceed' amount has been entered.";
        }

        if (StringUtils.isNotEmpty(requisition.getAlternate1VendorName()) || StringUtils.isNotEmpty(requisition.getAlternate2VendorName()) 
                || StringUtils.isNotEmpty(requisition.getAlternate3VendorName()) || StringUtils.isNotEmpty(requisition.getAlternate4VendorName())
                || StringUtils.isNotEmpty(requisition.getAlternate5VendorName())) {
            LOG.debug("isAPO() alternate vendor name exists; return false.");
            return "Requisition contains additional suggested vendor names.";
        }
        
        if (requisition.isPostingYearNext() && !purapService.isTodayWithinApoAllowedRange()) {
            return "Requisition is set to encumber next fiscal year and approval is not within APO allowed date range.";
        }

        return "";
    }
    
    /**
     * Checks the APO rules for Commodity Codes. 
     * The rules are as follow:
     * 1. If an institution does not require a commodity code on a requisition but 
     *    does require a commodity code on a purchase order:
     *    a. If the requisition qualifies for an APO and the commodity code is blank
     *       on any line item then the system should use the default commodity code
     *       for the vendor.
     *    b. If there is not a default commodity code for the vendor then the
     *       requisition is not eligible to become an APO.
     * 2. The commodity codes where the restricted indicator is Y should disallow
     *    the requisition from becoming an APO.
     *    
     * @param purItem
     * @param vendorCommodityCodes
     * @param commodityCodeRequired
     * @return
     */
    protected String checkAPORulesPerItemForCommodityCodes(RequisitionItem purItem, List<VendorCommodityCode>vendorCommodityCodes, boolean commodityCodeRequired) {
        // If the commodity code is blank on any line item and a commodity code is required,
        // then the system should use the default commodity code for the vendor
        if (purItem.getCommodityCode() == null && commodityCodeRequired) {
            for (VendorCommodityCode vcc : vendorCommodityCodes) {
                if (vcc.isCommodityDefaultIndicator()) {
                    purItem.setCommodityCode(vcc.getCommodityCode());
                    purItem.setPurchasingCommodityCode(vcc.getPurchasingCommodityCode());
                }
            }
        }
        if (purItem.getCommodityCode() == null) {
            // If there is not a default commodity code for the vendor then the requisition is not eligible to become an APO.
            if (commodityCodeRequired) {
                return "There are missing commodity code(s).";
            }
        } else if (purItem.getCommodityCode().isRestrictedItemsIndicator()) {
            return "Requisition contains an item with a restricted commodity code.";
        }
        return "";
    }

}
