package edu.cornell.kfs.module.purap.rest.resource;

import com.google.gson.Gson;
import edu.cornell.kfs.module.purap.CUPurapConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kuali.kfs.krad.dao.LookupDao;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

@Path("api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CuEinvoiceApiResource {

    private static final Log LOG = LogFactory.getLog(CuEinvoiceApiResource.class);

    private LookupDao lookupDao;
    private Gson gson = new Gson();

    @Context
    protected HttpServletRequest servletRequest;

    @Context
    protected HttpServletResponse servletResponse;

    @GET
    public Response describeEinvoiceApiResource() {
        return Response.ok(CUPurapConstants.EINVOICE_KFS_API_DESCRIPTION).build();
    }

    @GET
    @Path("vendors/{vendorHeaderGeneratedIdentifier}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getVendor(@PathParam(PurapPropertyConstants.VENDOR_HEADER_GENERATED_ID) String vendorHeaderGeneratedIdentifier,
                            @Context HttpHeaders headers) {
        try {
            HashMap<String, String> map = new HashMap<>();
            map.put(PurapPropertyConstants.VENDOR_HEADER_GENERATED_ID, vendorHeaderGeneratedIdentifier);
            VendorDetail vendorDetail = getLookupDao().findObjectByMap(VendorDetail.class.newInstance(), map);
            if (vendorDetail == null) {
                return getSimpleJsonObject(CUPurapConstants.ERROR, CUPurapConstants.OBJECT_NOT_FOUND);
            }
            return serializeVendorToJson(vendorDetail);
        }
        catch (Exception ex) {
            LOG.error(ex);
            return gson.toJson(ex);
        }
    }

    @GET
    @Path("po/{purapDocumentIdentifier}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getPurchaseOrder(@PathParam(PurapPropertyConstants.PURAP_DOC_ID) String purapDocumentIdentifier, @Context HttpHeaders headers) {
        try {
            HashMap<String, String> map = new HashMap<>();
            map.put(PurapPropertyConstants.PURAP_DOC_ID, purapDocumentIdentifier);
            PurchaseOrderDocument poDoc = getLookupDao().findObjectByMap(PurchaseOrderDocument.class.newInstance(), map);
            if (poDoc == null) {
                return getSimpleJsonObject(CUPurapConstants.ERROR, CUPurapConstants.OBJECT_NOT_FOUND);
            }
            return serializePoDocumentToJson(poDoc);
        }
        catch (Exception ex) {
            LOG.error(ex);
            return gson.toJson(ex);
        }
    }

    private String serializeVendorToJson(VendorDetail vendorDetail) {
        Properties vendorProperties = new Properties();
        safelyAddProperty(vendorProperties, CUPurapConstants.DUNS, vendorDetail.getVendorDunsNumber());
        safelyAddProperty(vendorProperties, PurapPropertyConstants.VENDOR_NUMBER, vendorDetail.getVendorNumber());
        safelyAddProperty(vendorProperties, PurapPropertyConstants.VENDOR_NAME, vendorDetail.getVendorName());
        vendorProperties.put(KRADPropertyConstants.ACTIVE_INDICATOR, vendorDetail.isActiveIndicator());
        addVendorRemitAddressToProperties(vendorProperties, vendorDetail);
        return gson.toJson(vendorProperties);
    }

    private String serializePoDocumentToJson(PurchaseOrderDocument poDoc) {
        Properties poProperties = new Properties();
        safelyAddProperty(poProperties, PurapPropertyConstants.VENDOR_NUMBER, poDoc.getVendorNumber());
        safelyAddProperty(poProperties, CUPurapConstants.DOCUMENT_NUMBER, poDoc.getDocumentNumber());
        safelyAddProperty(poProperties, CUPurapConstants.DOCUMENT_STATUS, poDoc.getApplicationDocumentStatus());
        addPoItemsToProperties(poProperties, poDoc.getItems());
        return gson.toJson(poProperties);
    }

    private void safelyAddProperty(Properties properties, String key, String value) {
        String safeValue = value;
        if (StringUtils.isBlank(value)) {
            safeValue = KFSConstants.EMPTY_STRING;
        }
        properties.put(key, safeValue);
    }

    private void addVendorRemitAddressToProperties(Properties vendorProperties, VendorDetail vendorDetail) {
        List<VendorAddress> vendorAddresses = vendorDetail.getVendorAddresses();
        if (CollectionUtils.isNotEmpty(vendorAddresses)) {
            VendorAddress vendorAddress = getVendorAddress(vendorDetail.getVendorAddresses());
            safelyAddProperty(vendorProperties, CUPurapConstants.EMAIL, vendorAddress.getVendorAddressEmailAddress());
            safelyAddProperty(vendorProperties, CUPurapConstants.ADDRESS_LINE1, vendorAddress.getVendorLine1Address());
            safelyAddProperty(vendorProperties, CUPurapConstants.ADDRESS_LINE2, vendorAddress.getVendorLine2Address());
            safelyAddProperty(vendorProperties, CUPurapConstants.STATE, vendorAddress.getVendorStateCode());
            safelyAddProperty(vendorProperties, CUPurapConstants.ZIPCODE, vendorAddress.getVendorZipCode());
            safelyAddProperty(vendorProperties, CUPurapConstants.COUNTRY, vendorAddress.getVendorCountryCode());
            safelyAddProperty(vendorProperties, CUPurapConstants.ADDRESS_TYPE_CODE, vendorAddress.getVendorAddressTypeCode());
        }
    }

    private VendorAddress getVendorAddress(List<VendorAddress> vendorAddresses) {
        for (VendorAddress address : vendorAddresses) {
            if (address.getVendorAddressType().getVendorAddressTypeCode().equalsIgnoreCase(VendorConstants.AddressTypes.REMIT)) {
                return address;
            }
        }
        return vendorAddresses.get(0);
    }

    private void addPoItemsToProperties(Properties poProperties, List poItems) {
        List<Properties> poLines = new ArrayList<>();
        for (Object obj : poItems) {
            if (obj instanceof PurApItem) {
                PurApItem purApItem = (PurApItem)obj;
                Properties lineProps = new Properties();
                safelyAddProperty(lineProps, CUPurapConstants.AMOUNT.toLowerCase(), purApItem.getTotalAmount().toString());
                safelyAddProperty(lineProps, CUPurapConstants.QUANTITY, purApItem.getItemQuantity().toString());
                safelyAddProperty(lineProps, CUPurapConstants.UNIT_OF_MEASURE, purApItem.getItemUnitOfMeasureCode());
                safelyAddProperty(lineProps, CUPurapConstants.ITEM_DESCRIPTION, purApItem.getItemDescription());
                poLines.add(lineProps);
            }
        }
        poProperties.put(CUPurapConstants.ITEMS, poLines.toArray());
    }

    private String getSimpleJsonObject(String key, String value) {
        Properties obj = new Properties();
        obj.put(key, value);
        return gson.toJson(obj);
    }

    private LookupDao getLookupDao() {
        if (lookupDao == null) {
            lookupDao = SpringContext.getBean(LookupDao.class);
        }
        return lookupDao;
    }

}
