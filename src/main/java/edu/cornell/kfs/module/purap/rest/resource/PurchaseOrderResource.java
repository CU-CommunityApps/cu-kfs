package edu.cornell.kfs.module.purap.rest.resource;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.krad.datadictionary.AttributeDefinition;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.service.PurchaseOrderService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;

import com.google.gson.Gson;

import edu.cornell.kfs.module.purap.rest.jsonOnjects.PurchaseOrderDetailDto;

@Path("api")
@Produces(MediaType.APPLICATION_JSON)
public class PurchaseOrderResource {
    private static final Logger LOG = LogManager.getLogger();
    private static final Gson gson = new Gson();

    private DataDictionaryService dataDictionaryService;
    private PurchaseOrderService purchaseOrderService;
    private VendorService vendorService;

    @Context
    protected HttpServletRequest servletRequest;

    @Context
    protected HttpServletResponse servletResponse;

    @GET
    public Response describePurchaseOrderResource() {
        return Response.ok("Purchase Order Resource").build();
    }

    @GET
    @Path("/getPurchaseOrderDetails")
    public Response getPurchaseOrderDetails() {
        try {
            final String poNumberString = servletRequest.getParameter("poNumber");
            LOG.debug("getPurchaseOrderDetails, entering with poNumber {}", poNumberString);

            if (!validatePoNumber(poNumberString)) {
                LOG.debug("getPurchaseOrderDetails, poNumber invalid");
                return Response.status(Status.BAD_REQUEST)
                        .entity("the purchase order number was not formatted correctly").build();
            }

            final PurchaseOrderDocument purchaseOrder = getPurchaseOrderService().getCurrentPurchaseOrder(Integer.valueOf(poNumberString));
            if (purchaseOrder == null) {
                String notFoundMessage = "Purchase order not found";
                return Response.status(Status.NOT_FOUND).entity(notFoundMessage).build();
            }
            
            final VendorDetail vendorDetail = getVendorService().getByVendorNumber(purchaseOrder.getVendorNumber());
            if (vendorDetail == null) {
                LOG.error("getPurchaseOrderDetails, for purchase order {} with a vendor number of {}, the vendor detail could not be found, this should not happen", poNumberString, purchaseOrder.getVendorNumber());
                String notFoundMessage = "Vendor detail not found";
                return Response.status(Status.NOT_FOUND).entity(notFoundMessage).build();
            }
            
            PurchaseOrderDetailDto dto = new PurchaseOrderDetailDto(purchaseOrder, vendorDetail);
            return Response.ok(gson.toJson(dto)).build();

        } catch (Exception e) {
            LOG.error("getPurchaseOrderDetails, had an error getting account details", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unexepected Error").build();
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

    public DataDictionaryService getDataDictionaryService() {
        if (dataDictionaryService == null) {
            dataDictionaryService = SpringContext.getBean(DataDictionaryService.class);
            ;
        }
        return dataDictionaryService;
    }

    public PurchaseOrderService getPurchaseOrderService() {
        if (purchaseOrderService == null) {
            purchaseOrderService = SpringContext.getBean(PurchaseOrderService.class);
        }
        return purchaseOrderService;
    }

    public VendorService getVendorService() {
        if (vendorService == null) {
            vendorService = SpringContext.getBean(VendorService.class);
        }
        return vendorService;
    }

}
