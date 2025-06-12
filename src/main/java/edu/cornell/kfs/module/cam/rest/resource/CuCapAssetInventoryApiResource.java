package edu.cornell.kfs.module.cam.rest.resource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cam.CamsConstants;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.module.cam.businessobject.BarcodeInventoryErrorDetail;
import org.kuali.kfs.module.cam.document.BarcodeInventoryErrorDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.Building;
import org.kuali.kfs.sys.businessobject.Campus;
import org.kuali.kfs.sys.businessobject.Room;
import org.kuali.kfs.sys.context.SpringContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import edu.cornell.kfs.module.cam.CuCamsConstants;
import edu.cornell.kfs.module.cam.CuCamsPropertyConstants;
import edu.cornell.kfs.module.cam.dataaccess.CuCapAssetInventoryDao;
import edu.cornell.kfs.module.cam.document.service.CuAssetService;
import edu.cornell.kfs.module.cam.document.service.impl.CuAssetServiceImpl;

@Path("api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CuCapAssetInventoryApiResource {

    private static final Logger LOG = LogManager.getLogger(CuCapAssetInventoryApiResource.class);

    private CuCapAssetInventoryDao cuCapAssetInventoryDao;
    private Gson gson = new Gson();
    private CuAssetService cuAssetService;
    private DocumentService documentService;
    private DateTimeService dateTimeService;
    private BusinessObjectService businessObjectService;
    private Environment environment;

    protected ConfigurationService configurationService;

    @Context
    protected HttpServletRequest servletRequest;

    @Context
    protected HttpServletResponse servletResponse;

    @GET
    public Response describeCapAssetApiResource() {
        return Response.ok(CuCamsConstants.CapAssetApi.CAPITAL_ASSET_KFS_API_DESCRIPTION).build();
    }
    
    @GET
    @Path("/showdevbanner")
    public Response showDevBanner() {
        boolean showDevBanner = !getEnvironment().isProductionEnvironment();
        return Response.ok(showDevBanner).build();
    }

    @GET
    @Path("/campus/{campusCode}/buildings")
    public Response getBuildings(@PathParam(CuCamsConstants.CapAssetApi.CAMPUS_CODE_PARAMETER) String campusCode, @Context HttpHeaders headers) {
        try {
            String queryName = servletRequest.getParameter(CuCamsConstants.CapAssetApi.BUILDING_NAME);
            String queryCode = servletRequest.getParameter(CuCamsConstants.CapAssetApi.BUILDING_CODE);
            List<Building> buildings = getCuCapAssetInventoryDao().getBuildings(campusCode, queryCode, queryName);
            List<Properties> buildingsSerialized = buildings.stream().map(b -> getBuildingProperties(b)).collect(Collectors.toList());
            return Response.ok(gson.toJson(buildingsSerialized)).build();
        } catch (Exception ex) {
            LOG.error("getBuildings", ex);
            return ex instanceof BadRequestException ? respondBadRequest() : respondInternalServerError(ex);
        }
    }

    @GET
    @Path("/campus/{campusCode}/buildings/{buildingCode}/rooms")
    public Response getRooms(@PathParam(CuCamsConstants.CapAssetApi.CAMPUS_CODE_PARAMETER) String campusCode,
                             @PathParam(CuCamsConstants.CapAssetApi.BUILDING_CODE_PARAMETER) String buildingCode,
                             @Context HttpHeaders headers) {
        try {
            List<Room> rooms = getCuCapAssetInventoryDao().getBuildingRooms(campusCode, buildingCode);
            List<String> buildingsSerialized = rooms.stream().map(r -> r.getBuildingRoomNumber()).collect(Collectors.toList());
            return Response.ok(gson.toJson(buildingsSerialized)).build();
        } catch (Exception ex) {
            LOG.error("getBuildings", ex);
            return ex instanceof BadRequestException ? respondBadRequest() : respondInternalServerError(ex);
        }
    }

    @GET
    @Path("asset/{assetTag}")
    public Response getAsset(@PathParam(CuCamsConstants.CapAssetApi.ASSET_TAG_PARAMETER) String assetTag, @Context HttpHeaders headers) {
        try {
            Asset asset = getCuCapAssetInventoryDao().getAssetByTagNumber(assetTag);
            if (ObjectUtils.isNull(asset)) {
                return respondAssetNotFound();
            }
            Properties properties = getAssetProperties(asset);
            return Response.ok(gson.toJson(properties)).build();
        } catch (Exception ex) {
            LOG.error("getAsset", ex);
            return ex instanceof BadRequestException ? respondBadRequest() : respondInternalServerError(ex);
        }
    }

    @PUT
    @Path("asset/inventory/{assetTag}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateAsset(@PathParam(CuCamsConstants.CapAssetApi.ASSET_TAG_PARAMETER) String assetTag, @Context HttpHeaders headers) {
        try {
            String assetTagUpper = StringUtils.upperCase(assetTag);
            Map<String, String> jsonFields = getShallowJsonFieldMappingsFromCurrentRequest();

            if (!validateUpdateAssetRequestBodyAndTag(assetTagUpper, jsonFields)) {
                LOG.error("updateAsset: Bad Request for Asset Tag #" + assetTagUpper + " " + jsonFields);
                return respondBadRequest();
            }

            LOG.info("updateAsset: Requesting Capital Asset Tag #" + assetTagUpper + " " + jsonFields);
            String conditionCode = jsonFields.get(CuCamsConstants.CapAssetApi.CONDITION_CODE);
            String buildingCode = jsonFields.get(CuCamsConstants.CapAssetApi.BUILDING_CODE);
            String roomNumber = jsonFields.get(CuCamsConstants.CapAssetApi.ROOM_NUMBER);
            String netid = jsonFields.get(CuCamsConstants.CapAssetApi.NETID);
            String campusCode = jsonFields.get(CuCamsConstants.CapAssetApi.CAMPUS_CODE);

            if(!validateBuildingCode(campusCode, buildingCode)) {
                String errorMessageFormat = getConfigurationService().getPropertyValueAsString(CuCamsPropertyConstants.Asset.ERROR_INVALID_BUILDING_CODE);
                String errorMessage = String.format(errorMessageFormat, buildingCode);
                LOG.error("updateAsset: " + errorMessage);
                return respondBadRequest(errorMessage);
            }

            if(!validateBuildingRoomCombination(campusCode, buildingCode, roomNumber)) {
                String errorMessageFormat = getConfigurationService().getPropertyValueAsString(CuCamsPropertyConstants.Asset.ERROR_INVALID_BUILDING_ROOM);
                String errorMessage = String.format(errorMessageFormat, roomNumber, buildingCode);
                LOG.error("updateAsset: " + errorMessage);
                return respondBadRequest(errorMessage);
            }

            Asset asset = getCuCapAssetInventoryDao().getAssetByTagNumber(assetTagUpper);
            if (ObjectUtils.isNull(asset)) {
                LOG.error("updateAsset: Asset Inventory Tag #" + assetTagUpper + " Not Found");
                createCapitalAssetErrorDocument(netid, assetTagUpper, conditionCode, buildingCode, roomNumber, campusCode);
                return respondAssetNotFound();
            }

            asset = getCuAssetService().updateAssetInventory(asset, conditionCode, buildingCode, roomNumber, netid, campusCode);
            LOG.info("updateAsset: Updated Capital Asset Inventory Tag #" + assetTagUpper + " " + asset.getLastInventoryDate().toString());
            Properties properties = getAssetProperties(asset);
            return Response.ok(gson.toJson(properties)).build();
        } catch (Exception ex) {
            LOG.error("updateAsset", ex);
            return ex instanceof BadRequestException ? respondBadRequest() : respondInternalServerError(ex);
        }
    }
    
    @GET
    @Path("/campusCodes")
    public Response getCampusCodes(@Context HttpHeaders headers) {
        try {
            List<Map<String, String>> campusCodes = new ArrayList<>();
            
            Map<String, Object> fieldValues = new HashMap<>();
            fieldValues.put(CuCamsConstants.CapAssetApi.ACTIVE, true);
            Collection<Campus> campuses = getBusinessObjectService().findMatching(Campus.class, fieldValues);
            
            for (Campus campus : campuses) {
                Map<String, String> campusMap = new HashMap<>();
                campusMap.put(CuCamsConstants.CapAssetApi.VALUE, campus.getCode());

                String campusLabel = String.format(CuCamsConstants.CapAssetApi.LABEL_FORMAT, campus.getCode(), campus.getName());
                campusMap.put(CuCamsConstants.CapAssetApi.LABEL, campusLabel);

                campusCodes.add(campusMap);
            }
            
            return Response.ok(gson.toJson(campusCodes)).build();
        } catch (Exception ex) {
            LOG.error("getCampusCodes", ex);
            return respondInternalServerError(ex);
        }
    }

    private boolean validateBuildingRoomCombination(String campusCode, String buildingCode, String roomNumber) {
        List<Room> roomsInBuilding = getCuCapAssetInventoryDao().getBuildingRooms(campusCode, buildingCode);
        return roomsInBuilding.stream().anyMatch(room -> StringUtils.equalsIgnoreCase(room.getBuildingRoomNumber(), roomNumber));
    }

    private boolean validateBuildingCode(String campusCode, String buildingCode) {
        List<Building> buildings = getCuCapAssetInventoryDao().getBuildings(campusCode, buildingCode, StringUtils.EMPTY);
        return !buildings.isEmpty();
    }

    private void createCapitalAssetErrorDocument(String netid, String assetTag, String condition, String buildingCode, String roomNumber, String campusCode) {
        try {
            GlobalVariables.doInNewGlobalVariables(new UserSession(CuCamsConstants.CapAssetApi.KFS_SYSTEM_USER),
                () -> {
                    try {
                        BarcodeInventoryErrorDocument document = (BarcodeInventoryErrorDocument) getDocumentService().getNewDocument(BarcodeInventoryErrorDocument.class);

                        String errorDescription = CuCamsConstants.CapAssetApi.ASSET_NOT_FOUND_ERROR + assetTag;
                        document.getDocumentHeader().setExplanation(errorDescription + ". Asset Tag Not Found");
                        document.getDocumentHeader().setDocumentDescription(errorDescription);
                        document.setUploaderUniversalIdentifier(netid);
                        List<BarcodeInventoryErrorDetail> barcodeInventoryErrorDetails = new ArrayList<>();
                        barcodeInventoryErrorDetails.add(getErrorDetail(netid, assetTag, condition, buildingCode, roomNumber, campusCode));
                        document.setBarcodeInventoryErrorDetail(barcodeInventoryErrorDetails);
                        getDocumentService().saveDocument(document);

                        getDocumentService().routeDocument(document, "Capital Asset Inventory Error Asset Not Found", new ArrayList<>());
                        return true;
                    } catch (Exception ex) {
                        LOG.error("createCapitalAssetErrorDocument", ex);
                    }
                    return false;
                }
            );
        } catch (Exception ex) {
            LOG.error("createCapitalAssetErrorDocument", ex);
        }
    }

    private BarcodeInventoryErrorDetail getErrorDetail(String netid, String assetTag, String condition, String buildingCode, String roomNumber, String campusCode) {
        BarcodeInventoryErrorDetail barcodeInventoryErrorDetail = new BarcodeInventoryErrorDetail();
        barcodeInventoryErrorDetail.setUploadRowNumber(CuCamsConstants.CapAssetApi.UPLOAD_ROW_NUMBER);
        barcodeInventoryErrorDetail.setCampusCode(campusCode);
        barcodeInventoryErrorDetail.setAssetTagNumber(assetTag);
        barcodeInventoryErrorDetail.setUploadScanIndicator(true);
        barcodeInventoryErrorDetail.setUploadScanTimestamp(getDateTimeService().getCurrentTimestamp());
        barcodeInventoryErrorDetail.setBuildingCode(buildingCode);
        barcodeInventoryErrorDetail.setBuildingRoomNumber(roomNumber);
        barcodeInventoryErrorDetail.setAssetConditionCode(condition);
        barcodeInventoryErrorDetail.setErrorCorrectionStatusCode(CamsConstants.BarCodeInventoryError.STATUS_CODE_ERROR);
        barcodeInventoryErrorDetail.setCorrectorUniversalIdentifier(netid);
        barcodeInventoryErrorDetail.setErrorDescription("Asset Tag Not Found");
        return barcodeInventoryErrorDetail;
    }

    private boolean validateUpdateAssetRequestBodyAndTag(String assetTag, Map<String, String> jsonFields) {
        var fieldsToCheck = Stream.of(
                CuCamsConstants.CapAssetApi.CONDITION_CODE, CuCamsConstants.CapAssetApi.BUILDING_CODE,
                CuCamsConstants.CapAssetApi.ROOM_NUMBER, CuCamsConstants.CapAssetApi.NETID);
        var missingFieldNamesMessage = fieldsToCheck
                .filter(fieldName -> StringUtils.isBlank(jsonFields.get(fieldName)))
                .collect(Collectors.joining(", "));
        var result = true;
        
        if (StringUtils.isBlank(assetTag)) {
            LOG.error("validateUpdateAssetRequestBodyAndTag: Update request for Asset does not specify an Asset Tag");
            result = false;
        }
        if (StringUtils.isNotBlank(missingFieldNamesMessage)) {
            LOG.error("validateUpdateAssetRequestBodyAndTag: Update request for Asset Tag #" + assetTag
                    + " is missing the following required fields: " + missingFieldNamesMessage);
            result = false;
        }
        return result;
    }

    private Properties getAssetProperties(Asset asset) {
        Properties assetProperties = new Properties();
        safelyAddProperty(assetProperties, CuCamsConstants.CapAssetApi.CAMPUS_CODE, asset.getCampusCode());
        safelyAddProperty(assetProperties, CuCamsConstants.CapAssetApi.CAPITAL_ASSET_NUMBER, asset.getCapitalAssetNumber());
        safelyAddProperty(assetProperties, CuCamsConstants.CapAssetApi.CAMPUS_TAG_NUMBER_ATTRIBUTE, asset.getCampusTagNumber());
        safelyAddProperty(assetProperties, CuCamsConstants.CapAssetApi.BUILDING_CODE, asset.getBuildingCode());
        safelyAddProperty(assetProperties, CuCamsConstants.CapAssetApi.ROOM_NUMBER, asset.getBuildingRoomNumber());
        safelyAddProperty(assetProperties, CuCamsConstants.CapAssetApi.ORGANIZATION_INVENTORY_NAME, asset.getOrganizationInventoryName());
        safelyAddProperty(assetProperties, CuCamsConstants.CapAssetApi.SERIAL_NUMBER, asset.getSerialNumber());
        safelyAddProperty(assetProperties, CuCamsConstants.CapAssetApi.CAPITAL_ASSET_DESCRIPTION, asset.getCapitalAssetDescription());
        safelyAddProperty(assetProperties, CuCamsConstants.CapAssetApi.LAST_UPDATED, asset.getLastUpdatedTimestamp().toString());
        safelyAddProperty(assetProperties, CuCamsConstants.CapAssetApi.LAST_INVENTORY_DATE, asset.getLastInventoryDate().toString());
        return assetProperties;
    }

    private Properties getBuildingProperties(Building building) {
        Properties buildingProperties = new Properties();
        safelyAddProperty(buildingProperties, CuCamsConstants.CapAssetApi.CAMPUS_CODE, building.getCampusCode());
        safelyAddProperty(buildingProperties, CuCamsConstants.CapAssetApi.BUILDING_CODE, building.getBuildingCode());
        safelyAddProperty(buildingProperties, CuCamsConstants.CapAssetApi.BUILDING_NAME, building.getBuildingName());
        return buildingProperties;
    }

    private void safelyAddProperty(Properties properties, String key, Long value) {
        properties.put(key, ObjectUtils.isNotNull(value) ? value : 0);
    }

    private void safelyAddProperty(Properties properties, String key, String value) {
        properties.put(key, StringUtils.defaultIfBlank(value, KFSConstants.EMPTY_STRING));
    }

    private Map<String, String> getShallowJsonFieldMappingsFromCurrentRequest() throws IOException {
        var fieldMappings = new HashMap<String, String>();
        var jsonNode = getJsonContentFromCurrentRequest();
        for (var jsonFieldIterator = jsonNode.fields(); jsonFieldIterator.hasNext();) {
            var jsonField = jsonFieldIterator.next();
            fieldMappings.put(jsonField.getKey(), jsonField.getValue().asText());
        }
        return fieldMappings;
    }

    private JsonNode getJsonContentFromCurrentRequest() throws IOException {
        try (var requestInputStream = servletRequest.getInputStream();
                var streamReader = new InputStreamReader(requestInputStream, StandardCharsets.UTF_8)) {
            var objectMapper = new ObjectMapper();
            var jsonNode = objectMapper.readTree(streamReader);
            if (jsonNode == null) {
                throw new BadRequestException("The request has no content in its JSON payload");
            } else if (!jsonNode.isObject()) {
                throw new BadRequestException("The request does not have a JSON object as the root node");
            }
            return jsonNode;
        } catch (JsonProcessingException e) {
            throw new BadRequestException("The request has malformed JSON content");
        }
    }

    private String getSimpleJsonObject(String key, String value) {
        Properties obj = new Properties();
        obj.put(key, value);
        return gson.toJson(obj);
    }

    private Response respondBadRequest() {
        return respondBadRequest(StringUtils.EMPTY);
    }
    private Response respondBadRequest(String errorMessage) {
        String responseErrorMessage = CuCamsConstants.CapAssetApi.BAD_REQUEST;
        if (StringUtils.isNotBlank(errorMessage)) {
            responseErrorMessage += CuCamsConstants.CapAssetApi.COLON_SPACE + errorMessage;
        }

        final String responseBody = getSimpleJsonObject(CuCamsConstants.CapAssetApi.ERROR, responseErrorMessage);
        return Response.status(400).entity(responseBody).build();
    }

    private Response respondAssetNotFound() {
        String responseBody = getSimpleJsonObject(CuCamsConstants.CapAssetApi.ERROR, CuCamsConstants.CapAssetApi.ASSET_TAG_NOT_FOUND);
        return Response.status(404).entity(responseBody).build();
    }

    private Response respondInternalServerError(Exception ex) {
        String responseBody = getSimpleJsonObject(CuCamsConstants.CapAssetApi.ERROR, ex.toString());
        return Response.status(500).entity(responseBody).build();
    }

    private CuCapAssetInventoryDao getCuCapAssetInventoryDao() {
        if (ObjectUtils.isNull(cuCapAssetInventoryDao)) {
            cuCapAssetInventoryDao = SpringContext.getBean(CuCapAssetInventoryDao.class);
        }
        return cuCapAssetInventoryDao;
    }

    private CuAssetService getCuAssetService() {
        if (ObjectUtils.isNull(cuAssetService)) {
            cuAssetService = SpringContext.getBean(CuAssetServiceImpl.class);
        }
        return cuAssetService;
    }

    private DateTimeService getDateTimeService() {
        if (ObjectUtils.isNull(dateTimeService)) {
            dateTimeService = SpringContext.getBean(DateTimeService.class);
        }
        return dateTimeService;
    }

    private DocumentService getDocumentService() {
        if (ObjectUtils.isNull(documentService)) {
            documentService = SpringContext.getBean(DocumentService.class);
        }
        return documentService;
    }

    private ConfigurationService getConfigurationService() {
        if (ObjectUtils.isNull(configurationService)) {
            configurationService = SpringContext.getBean(ConfigurationService.class);
        }
        return configurationService;
    }

    private BusinessObjectService getBusinessObjectService() {
        if (ObjectUtils.isNull(businessObjectService)) {
            businessObjectService = SpringContext.getBean(BusinessObjectService.class);
        }
        return businessObjectService;
    }

    private Environment getEnvironment() {
        if (ObjectUtils.isNull(environment)) {
            environment = SpringContext.getBean(Environment.class);
        }
        return environment;
    }

}
