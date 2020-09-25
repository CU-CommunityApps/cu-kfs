package edu.cornell.kfs.module.cam.rest.resource;

import com.google.gson.Gson;
import edu.cornell.kfs.module.cam.CuCamsConstants;
import edu.cornell.kfs.module.cam.dataaccess.CuCapAssetInventoryDao;
import edu.cornell.kfs.module.cam.document.service.CuAssetService;
import edu.cornell.kfs.module.cam.document.service.impl.CuAssetServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.Building;
import org.kuali.kfs.sys.businessobject.Room;
import org.kuali.kfs.sys.context.SpringContext;

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
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Path("api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CuCapAssetInventoryApiResource {

    private static final Logger LOG = LogManager.getLogger(CuCapAssetInventoryApiResource.class);

    private CuCapAssetInventoryDao cuCapAssetInventoryDao;
    private Gson gson = new Gson();
    private CuAssetService cuAssetService;

    @Context
    protected HttpServletRequest servletRequest;

    @Context
    protected HttpServletResponse servletResponse;

    @GET
    public Response describeCapAssetApiResource() {
        return Response.ok(CuCamsConstants.CapAssetApi.CAPITAL_ASSET_KFS_API_DESCRIPTION).build();
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
                return respondNotFound();
            }
            Properties properties = getAssetProperties(asset);
            return Response.ok(gson.toJson(properties)).build();
        } catch (Exception ex) {
            LOG.error("getAsset", ex);
            return ex instanceof BadRequestException ? respondBadRequest() : respondInternalServerError(ex);
        }
    }

    @PUT
    @Path("asset/inventory/{capital_asset_number}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateAsset(@PathParam(CuCamsConstants.CapAssetApi.CAPITAL_ASSET_NUMBER) String capitalAssetNumber, @Context HttpHeaders headers) {
        try {
            if (!validateUpdateAssetQueryParameters()) {
                LOG.error("updateAsset: Bad Request for Asset Inventory #" + capitalAssetNumber + " " + servletRequest.getQueryString());
                return respondBadRequest();
            }

            LOG.info("updateAsset: Requesting Capital Asset Inventory #" + capitalAssetNumber + " " + servletRequest.getQueryString());
            String conditionCode = servletRequest.getParameter(CuCamsConstants.CapAssetApi.CONDITION_CODE);
            String buildingCode = servletRequest.getParameter(CuCamsConstants.CapAssetApi.BUILDING_CODE);
            String roomNumber = servletRequest.getParameter(CuCamsConstants.CapAssetApi.ROOM_NUMBER);
            Asset asset = getCuAssetService().updateAssetInventory(capitalAssetNumber, conditionCode, buildingCode, roomNumber);
            if (ObjectUtils.isNull(asset)) {
                LOG.error("updateAsset: Asset Inventory #" + capitalAssetNumber + " Not Found");
                return respondNotFound();
            }
            LOG.info("updateAsset: Updated Capital Asset Inventory #" + capitalAssetNumber + " " + asset.getLastInventoryDate().toString());

            Properties properties = getAssetProperties(asset);
            return Response.ok(gson.toJson(properties)).build();
        } catch (Exception ex) {
            LOG.error("updateAsset", ex);
            return ex instanceof BadRequestException ? respondBadRequest() : respondInternalServerError(ex);
        }
    }

    private boolean validateUpdateAssetQueryParameters() {
        List<String> paramNames = Collections.list(servletRequest.getParameterNames());
        return paramNames.contains(CuCamsConstants.CapAssetApi.CONDITION_CODE) &&
                paramNames.contains(CuCamsConstants.CapAssetApi.BUILDING_CODE) &&
                paramNames.contains(CuCamsConstants.CapAssetApi.ROOM_NUMBER);
    }

    private Properties getAssetProperties(Asset asset) {
        Properties assetProperties = new Properties();
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

    private String getSimpleJsonObject(String key, String value) {
        Properties obj = new Properties();
        obj.put(key, value);
        return gson.toJson(obj);
    }

    private Response respondBadRequest() {
        String responseBody = getSimpleJsonObject(CuCamsConstants.CapAssetApi.ERROR, CuCamsConstants.CapAssetApi.BAD_REQUEST);
        return Response.status(400).entity(responseBody).build();
    }

    private Response respondNotFound() {
        String responseBody = getSimpleJsonObject(CuCamsConstants.CapAssetApi.ERROR, CuCamsConstants.CapAssetApi.OBJECT_NOT_FOUND);
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

}
