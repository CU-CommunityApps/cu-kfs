package edu.cornell.kfs.module.purap.rest.resource;

import com.google.gson.Gson;
import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.dataaccess.impl.EinvoiceDaoOjb;
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
import javax.ws.rs.BadRequestException;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

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
    @Path("vendors")
    public Response getVendor(@Context HttpHeaders headers) {
        try {
            List<String> vendorNumbers = getVendorNumbersFromRequest();
            List<VendorDetail> vendors = SpringContext.getBean(EinvoiceDaoOjb.class).getVendors(vendorNumbers);
            List<Properties> vendorsSerialized = vendors.stream().map(vendor -> getVendorProperties(vendor)).collect(Collectors.toList());
            return Response.ok(gson.toJson(vendorsSerialized)).build();
        }
        catch (BadRequestException ex) {
            return respondGetVendorsBadRequest();
        }
        catch (Exception ex) {
            LOG.error(ex);
            return respondInternalServerError(ex);
        }
    }

    @GET
    @Path("vendors/{vendorNumber}")
    public Response getVendor(@PathParam(PurapPropertyConstants.VENDOR_NUMBER) String vendorNumber, @Context HttpHeaders headers) {
        try {

            HashMap<String, String> vendorCombinedPk = parseVendorNumber(vendorNumber);
            VendorDetail vendorDetail = getLookupDao().findObjectByMap(VendorDetail.class.newInstance(), vendorCombinedPk);
            if (vendorDetail == null) {
                return respondNotFound();
            }
            String responseBody =  gson.toJson(getVendorProperties(vendorDetail));
            return Response.ok(responseBody).build();
        }
        catch (BadRequestException ex) {
            return respondGetVendorBadRequest();
        }
        catch (Exception ex) {
            LOG.error(ex);
            return respondInternalServerError(ex);
        }
    }

    @GET
    @Path("po/{purapDocumentIdentifier}")
    public Response getPurchaseOrder(@PathParam(PurapPropertyConstants.PURAP_DOC_ID) String purapDocumentIdentifier, @Context HttpHeaders headers) {
        try {
            HashMap<String, String> map = new HashMap<>();
            map.put(PurapPropertyConstants.PURAP_DOC_ID, purapDocumentIdentifier);
            PurchaseOrderDocument poDoc = getLookupDao().findObjectByMap(PurchaseOrderDocument.class.newInstance(), map);
            if (poDoc == null) {
                return respondNotFound();
            }
            String responseBody = serializePoDocumentToJson(poDoc);
            return Response.ok(responseBody).build();
        }
        catch (Exception ex) {
            LOG.error(ex);
            return respondInternalServerError(ex);
        }
    }

    private List<String> getVendorNumbersFromRequest() {
        String vendorNumbersString = servletRequest.getParameter(CUPurapConstants.VENDOR_NUMBERS);
        if (StringUtils.isEmpty(vendorNumbersString)) {
            throw new BadRequestException();
        }
        String[] vendorNumbers = vendorNumbersString.split(CUPurapConstants.COMMA);
        return Arrays.asList(vendorNumbers);
    }

    private Properties getVendorProperties(VendorDetail vendorDetail) {
        Properties vendorProperties = new Properties();
        safelyAddProperty(vendorProperties, CUPurapConstants.DUNS, vendorDetail.getVendorDunsNumber());
        safelyAddProperty(vendorProperties, PurapPropertyConstants.VENDOR_NUMBER, vendorDetail.getVendorNumber());
        safelyAddProperty(vendorProperties, CUPurapConstants.VENDOR_NAME, vendorDetail.getVendorName());
        vendorProperties.put(KRADPropertyConstants.ACTIVE_INDICATOR, vendorDetail.isActiveIndicator());
        addVendorRemitAddressToProperties(vendorProperties, vendorDetail);
        return vendorProperties;
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
            safelyAddProperty(vendorProperties, CUPurapConstants.CITY, vendorAddress.getVendorCityName());
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
                safelyAddProperty(lineProps, CUPurapConstants.DESCRIPTION, purApItem.getItemDescription());
                lineProps.put(CUPurapConstants.UNIT_PRICE, purApItem.getItemUnitPrice());
                lineProps.put(CUPurapConstants.LINE_NUMBER, purApItem.getItemLineNumber());
                lineProps.put(CUPurapConstants.PART_NUMBER, purApItem.getItemAuxiliaryPartIdentifier());
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

    private HashMap<String,String> parseVendorNumber(String vendorNumber) throws BadRequestException {
        HashMap<String, String> map = new HashMap<>();
        if (StringUtils.isEmpty(vendorNumber) || !vendorNumber.contains(CUPurapConstants.DASH)) {
            throw new BadRequestException();
        }

        String generatedVendorHeaderId = vendorNumber.substring(0, vendorNumber.indexOf(CUPurapConstants.DASH));
        String vendorDetailNumber = vendorNumber.substring(vendorNumber.indexOf(CUPurapConstants.DASH) + 1);
        map.put(PurapPropertyConstants.VENDOR_HEADER_GENERATED_ID, generatedVendorHeaderId);
        map.put(PurapPropertyConstants.VENDOR_DETAIL_ASSIGNED_ID, vendorDetailNumber);
        return map;
    }

    private Response respondGetVendorBadRequest() {
        String responseBody = getSimpleJsonObject(CUPurapConstants.ERROR, CUPurapConstants.EINVOICE_VENDOR_BAD_REQUEST);
        return Response.status(400).entity(responseBody).build();
    }

    private Response respondGetVendorsBadRequest() {
        String responseBody = getSimpleJsonObject(CUPurapConstants.ERROR, CUPurapConstants.EINVOICE_GET_VENDORS_BAD_REQUEST);
        return Response.status(400).entity(responseBody).build();
    }

    private Response respondNotFound() {
        String responseBody = getSimpleJsonObject(CUPurapConstants.ERROR, CUPurapConstants.OBJECT_NOT_FOUND);
        return Response.status(404).entity(responseBody).build();
    }

    private Response respondInternalServerError(Exception ex) {
        String responseBody = getSimpleJsonObject(CUPurapConstants.ERROR, ex.toString());
        return Response.status(500).entity(responseBody).build();
    }

}
