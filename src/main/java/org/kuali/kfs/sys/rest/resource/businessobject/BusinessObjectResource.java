/**
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2019 Kuali, Inc.
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
package org.kuali.kfs.sys.rest.resource.businessobject;

import com.google.gson.Gson;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kuali.kfs.coa.businessobject.AccountType;
import org.kuali.kfs.kns.datadictionary.BusinessObjectAdminService;
import org.kuali.kfs.kns.datadictionary.EntityNotFoundException;
import org.kuali.kfs.kns.service.BusinessObjectDictionaryService;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.dao.LookupDao;
import org.kuali.kfs.krad.datadictionary.BusinessObjectEntry;
import org.kuali.kfs.krad.datadictionary.SortDefinition;
import org.kuali.kfs.krad.exception.AuthorizationException;
import org.kuali.kfs.krad.service.LookupSearchService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.KRADUtils;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.rest.util.KualiMediaType;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.permission.PermissionService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

@Path("business-objects")
@Consumes(MediaType.APPLICATION_JSON)
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM, KualiMediaType.TEXT_CSV,
        KualiMediaType.LOOKUP_JSON})
public class BusinessObjectResource {

    private static final Log LOG = LogFactory.getLog(BusinessObjectResource.class);
    private static final String acceptTypeErrorMessage = "Only " + MediaType.WILDCARD + " ," +
            MediaType.APPLICATION_JSON + " ," + KualiMediaType.LOOKUP_JSON + " , and " +
            KualiMediaType.TEXT_CSV + " are supported at this time";
    private final int DEFAULT_PAGE_SIZE = 100;

    private BusinessObjectDictionaryService businessObjectDictionaryService;
    private PermissionService permissionService;
    private Gson gson = new Gson();

    @Context
    protected HttpServletRequest servletRequest;

    @Context
    protected HttpServletResponse servletResponse;

    @GET
    public Response describeBusinessObjectResource() {
        return Response.ok("Use this resource to interact with business objects.").build();
    }

    @Path("{businessObjectName}/lookup")
    public LookupResource getLookupResource(@PathParam("businessObjectName") BusinessObjectEntry businessObjectEntry) {
        return new LookupResource(servletRequest, businessObjectEntry);
    }

    @GET
    @Path("{businessObjectName}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.WILDCARD, KualiMediaType.LOOKUP_JSON, KualiMediaType.TEXT_CSV})
    public List<BusinessObjectBase> getBusinessObjects(@PathParam("businessObjectName") BusinessObjectEntry businessObjectEntry,
                                                       @Context UriInfo uriInfo, @Context HttpHeaders headers) {
        if (businessObjectEntry == null) {
            throw new NotFoundException();
        }

        Class<BusinessObjectBase> classForType = (Class<BusinessObjectBase>) businessObjectEntry.getBusinessObjectClass();
        if (!isAuthorizedForLookup(classForType)) {
            throw new ForbiddenException();
        }

        BusinessObjectDictionaryService boDictionaryService = getBusinessObjectDictionaryService();
        LookupSearchService searchService = boDictionaryService.getLookupSearchServiceForLookup(classForType);
        if (searchService == null) {
            LOG.error(businessObjectEntry.getName() + " seems to be missing a LookupSearchService! A lookup can " +
                    "not be performed without a LookupSearchService.");
            throw new InternalServerErrorException(gson.toJson("The requested business object does not support " +
                    "lookup."));
        }

        if (!acceptTypeIsSupported(headers.getAcceptableMediaTypes())) {
            throw new NotSupportedException(gson.toJson(acceptTypeErrorMessage));
        }

        MultivaluedMap<String, String> searchParams = uriInfo.getQueryParameters();
        Pair<Integer, Integer> skipAndLimit = getSkipAndLimit(searchParams);

        SortDefinition sortDefinition = getSortDefinition(searchParams);
        if (sortDefinition == null) {
            sortDefinition = boDictionaryService.getLookupDefaultSortDefinition(classForType);
        }

        String sortField = null;
        boolean sortAscending = true;
        if (sortDefinition != null && sortDefinition.getAttributeNames().size() > 0) {
            sortField = sortDefinition.getAttributeNames().get(0);
            sortAscending = sortDefinition.getSortAscending();
        }

        Pair<Collection<? extends BusinessObjectBase>, Integer> results = searchService.getSearchResults(classForType,
                searchParams, skipAndLimit.getLeft(), skipAndLimit.getRight(), sortField, sortAscending);
        servletResponse.setIntHeader("Item-Count", results.getRight());
        return (List<BusinessObjectBase>) results.getLeft();
    }

    @GET
    @Path("{businessObjectName}/{id}")
    public Object getBusinessObject(@PathParam("businessObjectName") BusinessObjectEntry businessObjectEntry,
                                    @PathParam("id") String id, @Context HttpHeaders headers) {
        if (businessObjectEntry == null) {
            throw new NotFoundException();
        }

        Class<BusinessObjectBase> classForType = (Class<BusinessObjectBase>) businessObjectEntry.getBusinessObjectClass();
        BusinessObjectAdminService adminService = getBusinessObjectDictionaryService().getBusinessObjectAdminService(
                classForType);
        if (adminService == null) {
            LOG.error(businessObjectEntry.getName() + "Seems to be missing a BusinessObjectAdminService! This GET " +
                    "operation can not be performed without a BusinessObjectAdminService.");
            throw new InternalServerErrorException();
        }

        List<MediaType> acceptTypes = headers.getAcceptableMediaTypes();
        if (acceptTypes.contains(MediaType.APPLICATION_OCTET_STREAM_TYPE)) {
            return fileForBusinessObject(businessObjectEntry.getName(), id, adminService);
        } else {
            // this is surely gonna change as we add more support.
            throw new NotSupportedException(
                    gson.toJson("Only " + MediaType.APPLICATION_OCTET_STREAM + " is accepted at this time."));
        }
    }

    @DELETE
    @Path("{businessObjectName}/{id}")
    public Response deleteBusinessObject(@PathParam("businessObjectName") BusinessObjectEntry businessObjectEntry,
                                         @PathParam("id") String id) {
        if (businessObjectEntry == null) {
            throw new NotFoundException();
        }

        Class<BusinessObjectBase> classForType = (Class<BusinessObjectBase>) businessObjectEntry.getBusinessObjectClass();
        BusinessObjectAdminService adminService = getBusinessObjectDictionaryService().getBusinessObjectAdminService(
                classForType);
        if (adminService == null) {
            LOG.error(businessObjectEntry.getName() + "Seems to be missing a BusinessObjectAdminService! This " +
                    "DELETE operation can not be performed without a BusinessObjectAdminService.");
            throw new InternalServerErrorException();
        }

        if (!adminService.allowsDelete(null, null)) {
            LOG.debug("Delete request received for business object: " + businessObjectEntry.getName() +
                    ". According to " + adminService.getClass().getSimpleName() + " this bo doesn't support deletion.");
            throw new NotAllowedException(gson.toJson("The requested business object does not support DELETE."));
        }

        boolean result;
        try {
            result = adminService.delete(id);
        } catch (AuthorizationException ae) {
            throw new ForbiddenException();
        } catch (EntityNotFoundException enfe) {
            throw new NotFoundException();
        }

        if (result) {
            return Response.noContent().build();
        } else {
            throw new InternalServerErrorException();
        }
    }

    private File fileForBusinessObject(String businessObjectName, String id, BusinessObjectAdminService adminService) {
        if (!adminService.allowsDownload(null, null)) {
            LOG.debug("Download request received for business object: " + businessObjectName + ". According to " +
                    adminService.getClass().getSimpleName() + " this bo doesn't support download.");
            throw new NotAllowedException(gson.toJson(
                    "The requested business object does not support GETs with the supplied media type."));
        }
        try {
            return adminService.download(id);
        } catch (AuthorizationException ae) {
            throw new ForbiddenException();
        } catch (EntityNotFoundException e) {
            throw new NotFoundException();
        }
    }

    private boolean acceptTypeIsSupported(List<MediaType> acceptTypes) {
        List<MediaType> supportedTypes = Arrays.asList(MediaType.WILDCARD_TYPE, MediaType.APPLICATION_JSON_TYPE,
                KualiMediaType.LOOKUP_JSON_TYPE, KualiMediaType.TEXT_CSV_TYPE);
        return supportedTypes.parallelStream().anyMatch(acceptTypes::contains);
    }

    private Pair<Integer, Integer> getSkipAndLimit(MultivaluedMap<String, String> params) {
        String skipString = params.getFirst("skip");
        int skipParam = skipString == null ? 0 : Integer.parseInt(skipString);
        int skip = Math.max(0, skipParam);

        String limitString = params.getFirst("limit");
        int limit = limitString == null ? DEFAULT_PAGE_SIZE : Integer.parseInt(limitString);
        if (limit < 0) {
            limit = DEFAULT_PAGE_SIZE;
        }

        return Pair.of(skip, limit);
    }

    private SortDefinition getSortDefinition(MultivaluedMap<String, String> criteria) {
        String sortParam = criteria.getFirst("sort");
        if (sortParam != null) {
            boolean descending = sortParam.startsWith("-");
            String sortKey;
            if (descending) {
                sortKey = sortParam.substring(1);
            } else {
                sortKey = sortParam;
            }
            SortDefinition sortDefinition = new SortDefinition();
            sortDefinition.setAttributeName(sortKey);
            sortDefinition.setSortAscending(!descending);
            return sortDefinition;
        }
        return null;
    }

    private boolean isAuthorizedForLookup(Class boClass) {
        return getPermissionService().isAuthorizedByTemplate(getPrincipalId(), KRADConstants.KNS_NAMESPACE,
                KimConstants.PermissionTemplateNames.LOOK_UP_RECORDS,
                KRADUtils.getNamespaceAndComponentSimpleName(boClass), Collections.emptyMap());
    }

    private String getPrincipalId() {
        return KRADUtils.getPrincipalIdFromRequest(servletRequest);
    }

    private BusinessObjectDictionaryService getBusinessObjectDictionaryService() {
        if (businessObjectDictionaryService == null) {
            businessObjectDictionaryService = SpringContext.getBean(BusinessObjectDictionaryService.class);
        }
        return businessObjectDictionaryService;
    }

    protected void setBusinessObjectDictionaryService(
            BusinessObjectDictionaryService businessObjectDictionaryService) {
        this.businessObjectDictionaryService = businessObjectDictionaryService;
    }

    private PermissionService getPermissionService() {
        if (permissionService == null) {
            permissionService = SpringContext.getBean(PermissionService.class);
        }
        return permissionService;
    }

    protected void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /* Cornell Customization for eInvoice */
    @GET
    @Path("einvoice/{businessObjectName}/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Object getVendorForEinvoice(@PathParam("businessObjectName") BusinessObjectEntry businessObjectEntry,
                                       @PathParam("id") String id, @Context HttpHeaders headers) {
        if (businessObjectEntry == null) {
            throw new NotFoundException();
        }

        try {
            HashMap<String, String> map = new HashMap<>();
            if (businessObjectEntry.getBusinessObjectClass().equals(AccountType.class)) {
                map.put("accountTypeCode", id);
                AccountType accountTypeClass = AccountType.class.newInstance();
                AccountType accountType = SpringContext.getBean(LookupDao.class).findObjectByMap(accountTypeClass, map);
                return gson.toJson(accountType);
            }
            if (businessObjectEntry.getBusinessObjectClass().equals(VendorDetail.class)) {
                map.put("vendorHeaderGeneratedIdentifier", id);
                VendorDetail clazz = VendorDetail.class.newInstance();
                VendorDetail vendorDetail = SpringContext.getBean(LookupDao.class).findObjectByMap(clazz, map);
                if (vendorDetail != null) {
                    Properties vendorProperties = new Properties();
                    safelyAddProperty(vendorProperties, "duns", vendorDetail.getVendorDunsNumber());
                    safelyAddProperty(vendorProperties, "vendor_nbr", vendorDetail.getVendorNumber());
                    safelyAddProperty(vendorProperties, "vendor_name", vendorDetail.getVendorName());
                    vendorProperties.put("activeIndicator", vendorDetail.isActiveIndicator());
                    addVendorRemitAddressToProperties(vendorProperties, vendorDetail);
                    return gson.toJson(vendorProperties);
                }
            }
        }
        catch (Exception ex) {
            LOG.error(ex);
        }

        throw new NotSupportedException(gson.toJson(businessObjectEntry.getName() + " is not supported at this time."));
    }

    private void safelyAddProperty(Properties properties, String key, String value) {
        String safeValue = value;
        if (StringUtils.isBlank(value)) {
            safeValue = "";
        }
        properties.put(key, safeValue);
    }

    private void addVendorRemitAddressToProperties(Properties vendorProperties, VendorDetail vendorDetail) {
        List<VendorAddress> vendorAddresses = vendorDetail.getVendorAddresses();
        if (CollectionUtils.isNotEmpty(vendorAddresses)) {
            VendorAddress vendorAddress = getVendorAddress(vendorDetail.getVendorAddresses());
            safelyAddProperty(vendorProperties, "email", vendorAddress.getVendorAddressEmailAddress());
            safelyAddProperty(vendorProperties, "address_line1", vendorAddress.getVendorLine1Address());
            safelyAddProperty(vendorProperties, "address_line2", vendorAddress.getVendorLine2Address());
            safelyAddProperty(vendorProperties, "state", vendorAddress.getVendorStateCode());
            safelyAddProperty(vendorProperties, "zipcode", vendorAddress.getVendorZipCode());
            safelyAddProperty(vendorProperties, "country", vendorAddress.getVendorCountryCode());
            safelyAddProperty(vendorProperties, "address_type_code", vendorAddress.getVendorAddressTypeCode());
        }
    }

    private VendorAddress getVendorAddress(List<VendorAddress> vendorAddresses) {
        for (VendorAddress address : vendorAddresses) {
            if (address.getVendorAddressType().getVendorAddressTypeCode().equalsIgnoreCase("RM")) {
                return address;
            }
        }
        return vendorAddresses.get(0);
    }
}
