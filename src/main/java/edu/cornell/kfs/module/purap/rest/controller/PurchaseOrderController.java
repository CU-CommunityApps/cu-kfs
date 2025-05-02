package edu.cornell.kfs.module.purap.rest.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.krad.datadictionary.AttributeDefinition;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.service.PurchaseOrderService;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.rest.jsonOnjects.PurchaseOrderDetailDto;
import edu.cornell.kfs.sys.CUKFSConstants;

import java.util.regex.Pattern;

@RestController
@RequestMapping("/api")
public class PurchaseOrderController {
    private static final Logger LOG = LogManager.getLogger();

    private final DataDictionaryService dataDictionaryService;
    private final PurchaseOrderService purchaseOrderService;
    private final VendorService vendorService;
    
    public PurchaseOrderController(DataDictionaryService dataDictionaryService,
                                  PurchaseOrderService purchaseOrderService,
                                  VendorService vendorService) {
        this.dataDictionaryService = dataDictionaryService;
        this.purchaseOrderService = purchaseOrderService;
        this.vendorService = vendorService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> describePurchaseOrderResource() {
        return ResponseEntity.ok(CUPurapConstants.PURCHASE_ORDER_ENDPOINT_DESCRIPTION);
    }

    @GetMapping(value = "/getPurchaseOrderDetails", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getPurchaseOrderDetails(
            @RequestParam(name = CUPurapConstants.PURCHASE_ORDER_NUMBER_URL_PARAMETER_NAME, required = false) String poNumberString,
            HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        try {
            LOG.debug("getPurchaseOrderDetails, entering with poNumber {}", poNumberString);

            if (!validatePoNumber(poNumberString)) {
                LOG.debug("getPurchaseOrderDetails, poNumber invalid");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(CUPurapConstants.PURCHASE_ORDER_NUMBER_FORMAT_ERROR_RESPONSE_MESSAGE);
            }

            final PurchaseOrderDocument purchaseOrder = getPurchaseOrderService().getCurrentPurchaseOrder(Integer.valueOf(poNumberString));
            if (purchaseOrder == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(CUPurapConstants.PURCHASE_ORDER_NOT_FOUND_MESSAGE);
            }
            
            final VendorDetail vendorDetail = getVendorService().getByVendorNumber(purchaseOrder.getVendorNumber());
            if (vendorDetail == null) {
                LOG.error("getPurchaseOrderDetails, for purchase order {} with a vendor number of {}, the vendor detail could not be found, this should not happen", poNumberString, purchaseOrder.getVendorNumber());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(CUPurapConstants.VENDOR_NOT_FOUND_MESSAGE);
            }
            
            PurchaseOrderDetailDto dto = new PurchaseOrderDetailDto(purchaseOrder, vendorDetail);
            return ResponseEntity.ok(dto);

        } catch (Exception e) {
            LOG.error("getPurchaseOrderDetails, had an error getting purchase order details", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CUKFSConstants.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean validatePoNumber(final String poNumber) {
        AttributeDefinition poNumberAttributeDefinition = getDataDictionaryService()
                .getAttributeDefinition(PurchaseOrderDocument.class.getName(), PurapPropertyConstants.PURAP_DOC_ID);
        Integer maxLength = poNumberAttributeDefinition.getMaxLength();
        Pattern validationExpression = poNumberAttributeDefinition.getValidationPattern().getRegexPattern();

        return StringUtils.isNotBlank(poNumber) && poNumber.length() <= maxLength
                && validationExpression.matcher(poNumber).matches();
    }

    private DataDictionaryService getDataDictionaryService() {
        return dataDictionaryService;
    }

    private PurchaseOrderService getPurchaseOrderService() {
        return purchaseOrderService;
    }

    private VendorService getVendorService() {
        return vendorService;
    }
}
