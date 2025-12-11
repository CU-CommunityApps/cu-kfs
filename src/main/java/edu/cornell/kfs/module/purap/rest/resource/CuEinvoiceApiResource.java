package edu.cornell.kfs.module.purap.rest.resource;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import edu.cornell.kfs.sys.web.service.CuApiJsonWebRequestReader;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.dao.LookupDao;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.businessobject.ItemType;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.UnitOfMeasure;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import com.google.gson.Gson;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.dataaccess.CuEinvoiceDao;
import edu.cornell.kfs.module.purap.dataaccess.impl.CuEinvoiceDaoOjb;

@Path("api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CuEinvoiceApiResource {

    private static final Logger LOG = LogManager.getLogger(CuEinvoiceApiResource.class);

    private LookupDao lookupDao;
    private CuEinvoiceDao cuEinvoiceDao;
    private Gson gson = new Gson();
    private CuApiJsonWebRequestReader cuApiJsonWebRequestReader;

    @Context
    protected HttpServletRequest servletRequest;

    @Context
    protected HttpServletResponse servletResponse;

    @GET
    @Produces (MediaType.APPLICATION_JSON)
    public Response describeEinvoiceApiResource() {
        HashMap<String, String> apiDescription = new HashMap<>();
        apiDescription.put("description", CUPurapConstants.Einvoice.EINVOICE_KFS_API_DESCRIPTION);
        return Response.ok(apiDescription).build();
    }

    @POST
    @Path(CUPurapConstants.Einvoice.VENDORS)
    @Consumes (MediaType.APPLICATION_JSON)
    @Produces (MediaType.APPLICATION_JSON)
    public Response getVendors(@Context HttpHeaders headers) {
        try {
            List<String> vendorNumbers = getVendorNumbers();
            if (CollectionUtils.isEmpty(vendorNumbers)) {
                return Response.ok("[]").build();
            }
            List<VendorDetail> vendors = getCuEinvoiceDao().getVendors(vendorNumbers);
            List<Properties> vendorsSerialized = vendors.stream().map(vendor -> getVendorProperties(vendor)).collect(Collectors.toList());
            return Response.ok(gson.toJson(vendorsSerialized)).build();
        } catch (Exception ex) {
            LOG.error("getVendors", ex);
            return ex instanceof BadRequestException ? respondGetVendorsBadRequest() : respondInternalServerError(ex);
        }
    }
    
    @GET
    @Path("vendornumbers/search/{searchcriteria}")
    public Response getVendorNumbersSearchCriteria(@PathParam("searchcriteria") String searchCriteria, @Context HttpHeaders headers) {
        LOG.debug("getVendorNumbersSearchCriteria, searchCriteria: " + searchCriteria);
        try {
            List<String> vendorNumbers = cuEinvoiceDao.getFilteredVendorNumbers(searchCriteria);
            if (ObjectUtils.isNull(vendorNumbers)) {
                return respondNotFound();
            }
            return Response.ok(buildVendorNumberList(vendorNumbers)).build();
        } catch (Exception ex) {
            LOG.error("getVendorNumbersSearchCriteria", ex);
            return ex instanceof BadRequestException ? respondGetVendorBadRequest() : respondInternalServerError(ex);
        }
    }
    
    protected String buildVendorNumberList(List<String> vendorNumbers) {
        String vendorNumbersList = StringUtils.EMPTY;
        if (CollectionUtils.isNotEmpty(vendorNumbers)) {
            vendorNumbersList = vendorNumbers.stream().collect(Collectors.joining(KFSConstants.COMMA));
        }
        return vendorNumbersList;
    }

    @GET
    @Path("vendors/{vendorNumber}")
    public Response getVendor(@PathParam(PurapPropertyConstants.VENDOR_NUMBER) String vendorNumber, @Context HttpHeaders headers) {
        try {
            HashMap<String, String> vendorCombinedPk = parseVendorNumber(vendorNumber);
            VendorDetail vendorDetail = getLookupDao().findObjectByMap(VendorDetail.class.newInstance(), vendorCombinedPk);
            if (ObjectUtils.isNull(vendorDetail)) {
                return respondNotFound();
            }
            String responseBody = gson.toJson(getVendorProperties(vendorDetail));
            return Response.ok(responseBody).build();
        } catch (Exception ex) {
            LOG.error("getVendor", ex);
            return ex instanceof BadRequestException ? respondGetVendorBadRequest() : respondInternalServerError(ex);
        }
    }

    @GET
    @Path("po/{purapDocumentIdentifier}")
    public Response getPurchaseOrder(@PathParam(PurapPropertyConstants.PURAP_DOC_ID) String purapDocumentIdentifier, @Context HttpHeaders headers) {
        try {
            HashMap<String, String> map = new HashMap<>();
            map.put(PurapPropertyConstants.PURAP_DOC_ID, purapDocumentIdentifier);
            map.put(PurapPropertyConstants.PURCHASE_ORDER_CURRENT_INDICATOR, CuFPConstants.YES);
            PurchaseOrderDocument poDoc = getLookupDao().findObjectByMap(PurchaseOrderDocument.class.newInstance(), map);
            if (ObjectUtils.isNull(poDoc)) {
                return respondNotFound();
            }
            String responseBody = serializePoDocumentToJson(poDoc);
            return Response.ok(responseBody).build();
        } catch (Exception ex) {
            LOG.error("getPurchaseOrder", ex);
            return respondInternalServerError(ex);
        }
    }

    @GET
    @Path("po/address/shipping/{purapDocumentIdentifier}")
    public Response getPurchaseOrderShippingAddress(
            @PathParam(PurapPropertyConstants.PURAP_DOC_ID) String purapDocumentIdentifier, @Context HttpHeaders headers) {
        try {
            HashMap<String, String> map = new HashMap<>();
            map.put(PurapPropertyConstants.PURAP_DOC_ID, purapDocumentIdentifier);
            map.put(PurapPropertyConstants.PURCHASE_ORDER_CURRENT_INDICATOR, CuFPConstants.YES);
            PurchaseOrderDocument poDoc = getLookupDao().findObjectByMap(PurchaseOrderDocument.class.newInstance(), map);
            if (ObjectUtils.isNull(poDoc)) {
                return respondNotFound();
            }
            String responseBody = serializePoShippingAddressToJson(poDoc);
            return Response.ok(responseBody).build();
        } catch (Exception ex) {
            LOG.error("getPurchaseOrderShippingAddress", ex);
            return respondInternalServerError(ex);
        }
    }

    @GET
    @Path("uom/active")
    public Response getUnitOfMeasureCodes(@Context HttpHeaders headers) {
        try {
            Map<String, String> criteria = Collections.singletonMap(
                    KRADPropertyConstants.ACTIVE, CuFPConstants.YES);
            Pair<Collection<UnitOfMeasure>, Integer> results = getLookupDao().findObjects(
                    UnitOfMeasure.class, criteria, 0, -1, KFSPropertyConstants.ITEM_UNIT_OF_MEASURE_CODE, true);
            String responseBody = serializeUnitOfMeasureCodesToJson(results.getLeft());
            return Response.ok(responseBody).build();
        } catch (Exception ex) {
            LOG.error("getUnitOfMeasureCodes", ex);
            return respondInternalServerError(ex);
        }
    }

    private List<String> getVendorNumbers() throws IOException, BadRequestException {
        String badRequestErrorMessage = "Invalid Request Body, expect JSON Object with \"vendors\" property of type array";
        JsonNode jsonNode = getCuApiJsonWebRequestReader().getJsonContentFromServletRequest(servletRequest);

        if (!jsonNode.has(CUPurapConstants.Einvoice.VENDORS) || !jsonNode.get(CUPurapConstants.Einvoice.VENDORS).isArray()) {
            throw new BadRequestException(badRequestErrorMessage);
        }

        List<String> vendorNumbers = new ArrayList<>();
        for (final JsonNode objNode : jsonNode.get(CUPurapConstants.Einvoice.VENDORS)) {
            vendorNumbers.add(objNode.asText());
        }

        return vendorNumbers;
    }

    private Properties getVendorProperties(VendorDetail vendorDetail) {
        Properties vendorProperties = new Properties();
        safelyAddProperty(vendorProperties, CUPurapConstants.Einvoice.DUNS, vendorDetail.getVendorDunsNumber());
        safelyAddProperty(vendorProperties, PurapPropertyConstants.VENDOR_NUMBER, vendorDetail.getVendorNumber());
        safelyAddProperty(vendorProperties, CUPurapConstants.Einvoice.VENDOR_NAME, vendorDetail.getVendorName());
        vendorProperties.put(KRADPropertyConstants.ACTIVE_INDICATOR, vendorDetail.isActiveIndicator());
        addVendorRemitAddressToProperties(vendorProperties, vendorDetail);
        return vendorProperties;
    }

    private String serializePoDocumentToJson(PurchaseOrderDocument poDoc) {
        Properties poProperties = new Properties();
        safelyAddProperty(poProperties, PurapPropertyConstants.VENDOR_NUMBER, poDoc.getVendorNumber());
        safelyAddProperty(poProperties, CUPurapConstants.Einvoice.DOCUMENT_NUMBER, poDoc.getDocumentNumber());
        safelyAddProperty(poProperties, CUPurapConstants.Einvoice.DOCUMENT_STATUS, poDoc.getApplicationDocumentStatus());
        addPoItemsToProperties(poProperties, poDoc.getItems());
        return gson.toJson(poProperties);
    }

    private void addVendorRemitAddressToProperties(Properties vendorProperties, VendorDetail vendorDetail) {
        List<VendorAddress> vendorAddresses = vendorDetail.getVendorAddresses();
        if (CollectionUtils.isNotEmpty(vendorAddresses)) {
            VendorAddress vendorAddress = getVendorAddress(vendorDetail.getVendorAddresses());
            safelyAddProperty(vendorProperties, CUPurapConstants.Einvoice.EMAIL, vendorAddress.getVendorAddressEmailAddress());
            safelyAddProperty(vendorProperties, CUPurapConstants.Einvoice.ADDRESS_LINE1, vendorAddress.getVendorLine1Address());
            safelyAddProperty(vendorProperties, CUPurapConstants.Einvoice.ADDRESS_LINE2, vendorAddress.getVendorLine2Address());
            safelyAddProperty(vendorProperties, CUPurapConstants.Einvoice.CITY, vendorAddress.getVendorCityName());
            safelyAddProperty(vendorProperties, CUPurapConstants.Einvoice.STATE, vendorAddress.getVendorStateCode());
            safelyAddProperty(vendorProperties, CUPurapConstants.Einvoice.ZIPCODE, vendorAddress.getVendorZipCode());
            safelyAddProperty(vendorProperties, CUPurapConstants.Einvoice.COUNTRY_CODE, vendorAddress.getVendorCountryCode());
            safelyAddProperty(vendorProperties, CUPurapConstants.Einvoice.COUNTRY_NAME, vendorAddress.getVendorCountry().getName());
            safelyAddProperty(vendorProperties, CUPurapConstants.Einvoice.ADDRESS_TYPE_CODE, vendorAddress.getVendorAddressTypeCode());
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
            if (obj instanceof PurchaseOrderItem) {
                PurchaseOrderItem poItem = (PurchaseOrderItem)obj;
                Properties lineProps = new Properties();
                safelyAddProperty(lineProps, CUPurapConstants.Einvoice.AMOUNT, poItem.getTotalAmount());
                safelyAddProperty(lineProps, CUPurapConstants.Einvoice.INVOICED_AMOUNT, poItem.getItemInvoicedTotalAmount());
                safelyAddProperty(lineProps, CUPurapConstants.Einvoice.PO_QUANTITY, poItem.getItemQuantity());
                safelyAddProperty(lineProps, CUPurapConstants.Einvoice.INVOICE_QUANTITY, poItem.getItemInvoicedTotalQuantity());
                safelyAddProperty(lineProps, CUPurapConstants.Einvoice.UNIT_OF_MEASURE, poItem.getItemUnitOfMeasureCode());
                safelyAddProperty(lineProps, CUPurapConstants.Einvoice.DESCRIPTION, poItem.getItemDescription());
                safelyAddProperty(lineProps, CUPurapConstants.Einvoice.UNIT_PRICE, poItem.getItemUnitPrice());
                safelyAddProperty(lineProps, CUPurapConstants.Einvoice.LINE_NUMBER, poItem.getItemLineNumber());
                safelyAddProperty(lineProps, CUPurapConstants.Einvoice.PART_NUMBER, poItem.getItemCatalogNumber());
                safelyAddProperty(lineProps, CUPurapConstants.Einvoice.ITEM_TYPE_DESCRIPTION, poItem.getItemType());
                safelyAddProperty(lineProps, CUPurapConstants.Einvoice.ITEM_TYPE_CODE, poItem.getItemTypeCode());
                safelyAddProperty(lineProps, CUPurapConstants.Einvoice.PO_ITEM_ID, poItem.getItemIdentifier());
                poLines.add(lineProps);
            }
        }
        poProperties.put(CUPurapConstants.Einvoice.ITEMS, poLines.toArray());
    }

    private String serializeUnitOfMeasureCodesToJson(Collection<UnitOfMeasure> unitsOfMeasure) {
        Properties uomWrapper = new Properties();
        Properties[] uomProperties = unitsOfMeasure.stream()
                .map(this::convertUnitOfMeasureToProperties)
                .toArray(Properties[]::new);
        uomWrapper.put(CUPurapConstants.Einvoice.UNITS_OF_MEASURE, uomProperties);
        return gson.toJson(uomWrapper);
    }

    private Properties convertUnitOfMeasureToProperties(UnitOfMeasure unitOfMeasure) {
        Properties unitProps = new Properties();
        safelyAddProperty(unitProps, CUPurapConstants.Einvoice.UNIT_OF_MEASURE, unitOfMeasure.getItemUnitOfMeasureCode());
        safelyAddProperty(unitProps, CUPurapConstants.Einvoice.DESCRIPTION, unitOfMeasure.getItemUnitOfMeasureDescription());
        return unitProps;
    }

    private String serializePoShippingAddressToJson(PurchaseOrderDocument poDoc) {
        Properties addressProps = new Properties();
        safelyAddProperty(addressProps, CUPurapConstants.Einvoice.DOCUMENT_NUMBER, poDoc.getDocumentNumber());
        safelyAddProperty(addressProps, CUPurapConstants.Einvoice.SHIPPING_NAME, poDoc.getDeliveryToName());
        if (StringUtils.isNotBlank(poDoc.getDeliveryBuildingCode())) {
            safelyAddProperty(addressProps, CUPurapConstants.Einvoice.SHIPPING_DELIVER_TO, poDoc.getDeliveryBuildingCode());
        }
        safelyAddProperty(addressProps, CUPurapConstants.Einvoice.ADDRESS_LINE1, poDoc.getDeliveryBuildingLine1Address());
        safelyAddProperty(addressProps, CUPurapConstants.Einvoice.ADDRESS_LINE2, poDoc.getDeliveryBuildingLine2Address());
        safelyAddProperty(addressProps, CUPurapConstants.Einvoice.ROOM_NUMBER_LINE, buildRoomNumberLine(poDoc));
        safelyAddProperty(addressProps, CUPurapConstants.Einvoice.CITY, poDoc.getDeliveryCityName());
        safelyAddProperty(addressProps, CUPurapConstants.Einvoice.STATE, poDoc.getDeliveryStateCode());
        safelyAddProperty(addressProps, CUPurapConstants.Einvoice.ZIPCODE, poDoc.getDeliveryPostalCode());
        safelyAddProperty(addressProps, CUPurapConstants.Einvoice.COUNTRY_CODE, poDoc.getDeliveryCountryCode());
        safelyAddProperty(addressProps, CUPurapConstants.Einvoice.COUNTRY_NAME, poDoc.getDeliveryCountryName());
        safelyAddProperty(addressProps, CUPurapConstants.Einvoice.EMAIL, poDoc.getDeliveryToEmailAddress());
        return gson.toJson(addressProps);
    }

    private String buildRoomNumberLine(PurchaseOrderDocument poDoc) {
        return CUPurapConstants.Einvoice.ROOM_NUMBER_PREFIX + poDoc.getDeliveryBuildingRoomNumber();
    }

    private void safelyAddProperty(Properties properties, String key, String value) {
        properties.put(key, StringUtils.defaultIfBlank(value, KFSConstants.EMPTY_STRING));
    }

    private void safelyAddProperty(Properties properties, String key, KualiDecimal value) {
        properties.put(key, ObjectUtils.isNull(value) ? CUPurapConstants.Einvoice.NULL : value.doubleValue());
    }

    private void safelyAddProperty(Properties properties, String key, BigDecimal value) {
        properties.put(key, ObjectUtils.isNull(value) ? CUPurapConstants.Einvoice.NULL : value.doubleValue());
    }

    private void safelyAddProperty(Properties properties, String key, Integer value) {
        properties.put(key, ObjectUtils.isNull(value) ? CUPurapConstants.Einvoice.NULL : value);
    }

    private void safelyAddProperty(Properties properties, String key, ItemType value) {
        properties.put(key, ObjectUtils.isNull(value) ? KFSConstants.EMPTY_STRING : value.getItemTypeDescription());
    }

    private String getSimpleJsonObject(String key, String value) {
        Properties obj = new Properties();
        obj.put(key, value);
        return gson.toJson(obj);
    }

    private LookupDao getLookupDao() {
        if (ObjectUtils.isNull(lookupDao)) {
            lookupDao = SpringContext.getBean(LookupDao.class);
        }
        return lookupDao;
    }

    private CuEinvoiceDao getCuEinvoiceDao() {
        if (ObjectUtils.isNull(cuEinvoiceDao)) {
            cuEinvoiceDao = SpringContext.getBean(CuEinvoiceDaoOjb.class);
        }
        return cuEinvoiceDao;
    }

    private CuApiJsonWebRequestReader getCuApiJsonWebRequestReader() {
        if (ObjectUtils.isNull(cuApiJsonWebRequestReader)) {
            cuApiJsonWebRequestReader = SpringContext.getBean(CuApiJsonWebRequestReader.class);
        }
        return cuApiJsonWebRequestReader;
    }

    private HashMap<String,String> parseVendorNumber(String vendorNumber) throws BadRequestException {
        HashMap<String, String> map = new HashMap<>();
        if (StringUtils.isEmpty(vendorNumber) || !vendorNumber.contains(KFSConstants.DASH) || StringUtils.countMatches(vendorNumber, KFSConstants.DASH) > 1) {
            throw new BadRequestException();
        }

        String generatedVendorHeaderId = StringUtils.substringBeforeLast(vendorNumber, KFSConstants.DASH);
        String vendorDetailNumber = StringUtils.substringAfterLast(vendorNumber, KFSConstants.DASH);
        map.put(PurapPropertyConstants.VENDOR_HEADER_GENERATED_ID, generatedVendorHeaderId);
        map.put(PurapPropertyConstants.VENDOR_DETAIL_ASSIGNED_ID, vendorDetailNumber);
        return map;
    }

    private Response respondGetVendorBadRequest() {
        String responseBody = getSimpleJsonObject(CUPurapConstants.Einvoice.ERROR, CUPurapConstants.Einvoice.EINVOICE_VENDOR_BAD_REQUEST);
        return Response.status(400).entity(responseBody).build();
    }

    private Response respondGetVendorsBadRequest() {
        String responseBody = getSimpleJsonObject(CUPurapConstants.Einvoice.ERROR, CUPurapConstants.Einvoice.EINVOICE_GET_VENDORS_BAD_REQUEST);
        return Response.status(400).entity(responseBody).build();
    }

    private Response respondNotFound() {
        String responseBody = getSimpleJsonObject(CUPurapConstants.Einvoice.ERROR, CUPurapConstants.Einvoice.OBJECT_NOT_FOUND);
        return Response.status(404).entity(responseBody).build();
    }

    private Response respondInternalServerError(Exception ex) {
        String responseBody = getSimpleJsonObject(CUPurapConstants.Einvoice.ERROR, ex.toString());
        return Response.status(500).entity(responseBody).build();
    }

}
