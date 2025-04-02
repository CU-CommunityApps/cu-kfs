/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.core.proxy.ProxyHelper;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.datadictionary.Action;
import org.kuali.kfs.datadictionary.ActionsProvider;
import org.kuali.kfs.datadictionary.BusinessObjectAdminService;
import org.kuali.kfs.datadictionary.DetailsDictionary;
import org.kuali.kfs.datadictionary.LookupDictionary;
import org.kuali.kfs.datadictionary.Section;
import org.kuali.kfs.datadictionary.legacy.BusinessObjectDictionaryService;
import org.kuali.kfs.kew.doctype.bo.DocumentType;
import org.kuali.kfs.kew.doctype.service.DocumentTypeService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.datadictionary.BusinessObjectEntry;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.datadictionary.SortDefinition;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchJobStatus;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.businessobject.service.BusinessObjectCreationService;
import org.kuali.kfs.sys.businessobject.service.SearchService;
import org.kuali.kfs.sys.rest.KfsMediaType;
import org.kuali.kfs.sys.rest.resource.requests.BatchJobStatusRequest;
import org.kuali.kfs.sys.rest.resource.responses.ActionResponse;
import org.kuali.kfs.sys.rest.resource.responses.DetailsResponse;
import org.kuali.kfs.sys.service.OptionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * ====
 * CU Customization: Backported the FINP-9939 and FINP-10237 fixes into our current KFS release.
 *                   This overlay should be removed when we upgrade to the 2023-09-20 version of financials.
 * ====
 * 
 * REST APIs for business objects.
 *
 * All paths will be under {@link org.kuali.kfs.sys.context.KFSConfigurer#SPRING_MVC_ROOT_PATH}.
 *
 * This will get picked-up by Spring based on spring-core.xml's <context:component-scan ... /> element.
 */
@RestController
@RequestMapping("business-objects")
public class BusinessObjectsController {

    private static final Logger LOG = LogManager.getLogger();

    private static final String DEFAULT_PAGE_SIZE_STRING = "100";
    private static final int DEFAULT_PAGE_SIZE = Integer.parseInt(DEFAULT_PAGE_SIZE_STRING);

    private final BusinessObjectDictionaryService businessObjectDictionaryService;
    private final ConfigurationService configurationService;
    private final DetailsDictionary detailsDictionary;
    private final DocumentTypeService documentTypeService;
    private final LookupDictionary lookupDictionary;
    private final OptionsService optionsService;

    private final BusinessObjectsToCsvConverter boToCsvConverter;
    private final BusinessObjectToDetailJsonConverter boToDetailJsonConverter;
    private final BusinessObjectsToJsonConverter boToJsonConverter;
    private final BusinessObjectsToLookupJsonConverter boToLookupJsonConverter;
    private final CollectionConverter collectionConverter;
    private final BusinessObjectsControllersHelperService businessObjectsControllersHelperService;

    private final int maxLimit;

    @Autowired
    public BusinessObjectsController(
            final BusinessObjectDictionaryService businessObjectDictionaryService,
            final ConfigurationService configurationService,
            final DetailsDictionary detailsDictionary,
            final DocumentTypeService documentTypeService,
            final LookupDictionary lookupDictionary,
            final OptionsService optionsService,
            final BusinessObjectsToCsvConverter boToCsvConverter,
            final BusinessObjectToDetailJsonConverter boToDetailJsonConverter,
            final BusinessObjectsToJsonConverter boToJsonConverter,
            @Qualifier("boToLookupJsonConverter") final BusinessObjectsToLookupJsonConverter boToLookupJsonConverter,
            final CollectionConverter collectionConverter,
            final BusinessObjectsControllersHelperService businessObjectsControllersHelperService
    ) {
        Validate.isTrue(businessObjectDictionaryService != null, "businessObjectDictionaryService must be provided");
        this.businessObjectDictionaryService = businessObjectDictionaryService;
        Validate.isTrue(configurationService != null, "configurationService must be provided");
        this.configurationService = configurationService;
        Validate.isTrue(detailsDictionary != null, "detailsDictionary must be provided");
        this.detailsDictionary = detailsDictionary;
        Validate.isTrue(documentTypeService != null, "documentTypeService must be provided");
        this.documentTypeService = documentTypeService;
        Validate.isTrue(lookupDictionary != null, "lookupDictionary must be provided");
        this.lookupDictionary = lookupDictionary;
        Validate.isTrue(optionsService != null, "optionsService must be provided");
        this.optionsService = optionsService;
        Validate.isTrue(boToCsvConverter != null, "boToCsvConverter must be provided");
        this.boToCsvConverter = boToCsvConverter;
        Validate.isTrue(boToDetailJsonConverter != null, "boToDetailJsonConverter must be provided");
        this.boToDetailJsonConverter = boToDetailJsonConverter;
        Validate.isTrue(boToJsonConverter != null, "boToJsonConverter must be provided");
        this.boToJsonConverter = boToJsonConverter;
        Validate.isTrue(boToLookupJsonConverter != null, "boToLookupJsonConverter must be provided");
        this.boToLookupJsonConverter = boToLookupJsonConverter;
        Validate.isTrue(collectionConverter != null, "collectionConverter must be provided");
        this.collectionConverter = collectionConverter;
        Validate.isTrue(businessObjectsControllersHelperService != null, "businessObjectsControllersHelper must be provided");
        this.businessObjectsControllersHelperService = businessObjectsControllersHelperService;

        maxLimit = determineMaxLimit();
    }

    private int determineMaxLimit() {
        final String maxLimitString =
                configurationService.getPropertyValueAsString(KFSConstants.BUSINESS_OBJECT_API_MAX_RESULTS_KEY);
        try {
            return Integer.parseInt(maxLimitString);
        } catch (final NumberFormatException nfe) {
            // TODO: Fail-fast instead of silently doing something unexpected
            LOG.error(
                    "BusinessObjectsController(...) - Max limit configuration value is not an : maxLimitString={}",
                    maxLimitString
            );
            return DEFAULT_PAGE_SIZE;
        }
    }

    /**
     * TODO: Sort
     *
     * @return A set of all available business object names.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<String> getAllAvailableBusinessObjects() {
        LOG.debug("getAllAvailableBusinessObjects(...) - Enter");

        final Set<String> response = businessObjectDictionaryService.getBusinessObjectEntries()
                .values()
                .stream()
                .map(BusinessObjectEntry::getBusinessObjectClass)
                .filter(businessObjectDictionaryService::isLookupable)
                .filter(boClass -> lookupDictionary.getSearchService((Class<? extends BusinessObjectBase>) boClass)
                                   != null)
                .map(Class::getSimpleName)
                .collect(Collectors.toSet());
        LOG.debug("getAllAvailableBusinessObjects(...) - Exit : response={}", response);
        return response;
    }

    /**
     * TODO: Change the way the client specifies sorting and replace skip/limit/sorting with
     *       {@link org.springframework.data.domain.Pageable}.
     * TODO: Does not work for DocumentType
     */
    @GetMapping(
            value = "{businessObjectName}",
            produces = {
                MediaType.ALL_VALUE,
                MediaType.APPLICATION_JSON_VALUE
            }
    )
    public ResponseEntity<String> getBusinessObjectsAsJson(
            @PathVariable final String businessObjectName,
            @SessionAttribute final UserSession userSession,
            @RequestParam(required = false, defaultValue = "0") final int skip,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGE_SIZE_STRING) final int limit,
            @RequestParam(required = false) final String sort,
            @RequestParam final MultiValueMap<String, String> queryParameters
    ) {
        LOG.debug(
                "getBusinessObjectsAsJson(...) - Enter : businessObjectName={}; skip={}; limit={}; sort={}; "
                + "queryParameters={}",
                businessObjectName,
                skip,
                limit,
                sort,
                queryParameters
        );

        final Pair<Collection<? extends BusinessObjectBase>, Integer> results =
                getSearchResults(
                        businessObjectName,
                        userSession,
                        skip,
                        limit,
                        sort,
                        queryParameters
                );

        // TODO: Why is this necessary? Put it in the response JSON.
        final HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Item-Count", Integer.toString(results.getRight()));

        final ResponseEntity<String> response = createResponse(
                responseHeaders,
                (List<? extends BusinessObjectBase>) results.getLeft(),
                boToJsonConverter
        );
        LOG.debug("getBusinessObjectsAsJson(...) - Exit : response={}", response);
        return response;
    }

    private Pair<Collection<? extends BusinessObjectBase>, Integer> getSearchResults(
            final String businessObjectName,
            final UserSession userSession,
            final int skip,
            final int limit,
            final String sort,
            final MultiValueMap<String, String> queryParameters
    ) {
        final BusinessObjectEntry businessObjectEntry =
                businessObjectDictionaryService.getBusinessObjectEntry(businessObjectName);
        if (businessObjectEntry == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final Class<? extends BusinessObjectBase> businessObjectClass =
                (Class<? extends BusinessObjectBase>) businessObjectEntry.getBusinessObjectClass();
        if (businessObjectsControllersHelperService.notAuthorizedToView(businessObjectClass, userSession.getPrincipalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        final SearchService searchService = lookupDictionary.getSearchService(businessObjectClass);
        if (searchService == null) {
            LOG.error(
                    "getSearchResults(...) - Unable to find a SearchService, which is required for a lookup : "
                    + "businessObjectName={}", businessObjectName);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "The requested business object does not support lookup."
            );
        }

        final KfsPageable pageable = new KfsPageable(skip, limit, sort, businessObjectClass);
        return searchService.getSearchResults(
                businessObjectClass,
                queryParameters,
                pageable.skip,
                pageable.limit,
                pageable.sortField,
                pageable.sortAscending
        );
    }

    /**
     * TODO: Change the way the client specifies sorting and replace skip/limit/sorting with
     *       {@link org.springframework.data.domain.Pageable}.
     */
    @GetMapping(
            value = "{businessObjectName}",
            produces = KfsMediaType.APPLICATION_VND_LOOKUP_JSON_VALUE
    )
    public ResponseEntity<String> getBusinessObjectsAsLookupJson(
            @PathVariable final String businessObjectName,
            @SessionAttribute final UserSession userSession,
            @RequestParam(required = false, defaultValue = "0") final int skip,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGE_SIZE_STRING) final int limit,
            @RequestParam(required = false) final String sort,
            @RequestParam final MultiValueMap<String, String> queryParameters
    ) {
        LOG.debug(
                "getBusinessObjectsAsLookupJson(...) - Enter : businessObjectName={}; skip={}; limit={};"
                + " sort={}; queryParameters={}",
                businessObjectName,
                skip,
                limit,
                sort,
                queryParameters
        );

        final Pair<Collection<? extends BusinessObjectBase>, Integer> results =
                getSearchResults(
                        businessObjectName,
                        userSession,
                        skip,
                        limit,
                        sort,
                        queryParameters
                );

        // TODO: Why is this necessary? Put it in the response JSON.
        final HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Item-Count", Integer.toString(results.getRight()));

        final BusinessObjectEntry businessObjectEntry =
                businessObjectDictionaryService.getBusinessObjectEntry(businessObjectName);

        final ResponseEntity<String> response = createResponse(
                responseHeaders,
                (List<? extends BusinessObjectBase>) results.getLeft(),
                org.apache.commons.lang3.ObjectUtils.firstNonNull(
                        lookupDictionary.getBoToLookupJsonConverter((Class<? extends BusinessObjectBase>) businessObjectEntry.getBusinessObjectClass()),
                        boToLookupJsonConverter
                )
        );
        LOG.debug("getBusinessObjectsAsLookupJson(...) - Exit : response={}", response);
        return response;
    }

    /**
     * TODO: Change the way the client specifies sorting and replace skip/limit/sorting with
     *       {@link org.springframework.data.domain.Pageable}.
     */
    @GetMapping(
            value = "{businessObjectName}.csv",
            produces = KfsMediaType.TEXT_CSV_VALUE
    )
    public ResponseEntity<String> getBusinessObjectsAsCsv(
            @PathVariable final String businessObjectName,
            @SessionAttribute final UserSession userSession,
            @RequestParam(required = false, defaultValue = "0") final int skip,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGE_SIZE_STRING) final int limit,
            @RequestParam(required = false) final String sort,
            @RequestParam final MultiValueMap<String, String> queryParameters
    ) {
        LOG.debug(
                "getBusinessObjectsAsCsv(...) - Enter : businessObjectName={}; skip={}; limit={}; sort={}; "
                + "queryParameters={}",
                businessObjectName,
                skip,
                limit,
                sort,
                queryParameters
        );

        final Pair<Collection<? extends BusinessObjectBase>, Integer> results =
                getSearchResults(
                        businessObjectName,
                        userSession,
                        skip,
                        limit,
                        sort,
                        queryParameters
                );

        // TODO: Why is this necessary? Put it in the response JSON.
        final HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Item-Count", Integer.toString(results.getRight()));

        final ContentDisposition contentDisposition = ContentDisposition
                .attachment()
                .filename(businessObjectName + "-export.csv")
                .build();
        responseHeaders.setContentDisposition(contentDisposition);

        final ResponseEntity<String> response = createResponse(
                responseHeaders,
                (List<? extends BusinessObjectBase>) results.getLeft(),
                boToCsvConverter
        );
        LOG.debug("getBusinessObjectsAsCsv(...) - Exit : response={}", response);
        return response;
    }

    private ResponseEntity<String> createResponse(
            final HttpHeaders responseHeaders,
            final List<? extends BusinessObjectBase> businessObjects,
            final BusinessObjectsConverter converter
    ) {
        final String decoratedString = converter.convert(businessObjects);
        final ResponseEntity<String> response = ResponseEntity
                .ok()
                .headers(responseHeaders)
                .body(decoratedString);
        return response;
    }

    /**
     * TODO: Refactor BusinessObjectCreationService so JsonNode can be replaced with BatchJobStatusRequest
     * TODO: Refactor BatchJobStatusRequest, extracting BatchJobStatusResponse; return that instead of
     *       BusinessObjectBase.
     *
     * @param body The JSON payload representing a {@link BatchJobStatusRequest} instance.
     * @return A {@link BatchJobStatusRequest}.
     */
    @PostMapping(
            value = "BatchJobStatus",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<BusinessObjectBase> createBatchJobStatus(
            final UriComponentsBuilder uriComponentsBuilder,
            @SessionAttribute final UserSession userSession,
            @RequestBody final JsonNode body
    ) {
        LOG.debug("createBatchJobStatus(...) - Enter : body={}", body);

        final String businessObjectName = "BatchJobStatus";
        final BusinessObjectEntry businessObjectEntry =
                businessObjectDictionaryService.getBusinessObjectEntry(businessObjectName);
        if (businessObjectEntry == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final Class<BusinessObjectBase> boClass =
                (Class<BusinessObjectBase>) businessObjectEntry.getBusinessObjectClass();

        final BusinessObjectCreationService creationService =
                businessObjectDictionaryService.getBusinessObjectCreationService(boClass);
        if (creationService == null) {
            LOG.error(
                    "createBatchJobStatus(...) - Could not find creation service for business object entry {}",
                    businessObjectName
            );
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
        }

        final BusinessObjectAdminService adminService =
                businessObjectDictionaryService.getBusinessObjectAdminService(boClass);
        if (adminService == null) {
            LOG.error(
                    "createBatchJobStatus(...) - Could not find admin for business object entry {}",
                    businessObjectName
            );
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        final Person user = userSession.getPerson();
        if (!adminService.allowsCreate(boClass, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        final BusinessObjectBase bo = creationService.initialize(body, user);

        if (bo == null) {
            LOG.error(
                    "createBatchJobStatus(...) - Could not create business object : businessObjectName={}; body={}",
                    businessObjectName,
                    body
            );
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String id = "";
        if (bo instanceof BatchJobStatus) {
            id = ((BatchJobStatus) bo).getId();
        }

        final URI resourceLocation = uriComponentsBuilder
                .pathSegment("business-objects", businessObjectName, "{id}")
                .buildAndExpand(id)
                .toUri();
        final ResponseEntity<BusinessObjectBase> response = ResponseEntity.created(resourceLocation).body(bo);
        LOG.debug("createBatchJobStatus(...) - Exit : response={}", response);
        return response;
    }

    /**
     * @param businessObjectName The name of the BusinessObject to be created.
     * @return Nothing; see @throws
     * @throw ResponseStatusException(NOT_FOUND) because no other BOs currently support creation.
     */
    @PostMapping("{businessObjectName}")
    public Object createBusinessObjectNotImplemented(
            @PathVariable final String businessObjectName
    ) {
        LOG.error(
                "createBusinessObjectNotImplemented(...) - Create not supported : businessObjectName={}",
                businessObjectName
        );
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    /**
     * Retrieve a representation of a particular {@link BusinessObjectBase}.
     *
     * TODO: Cannot currently retrieve an {@link org.kuali.kfs.coa.businessobject.Account}.
     *
     * @param businessObjectName The desired {@link BusinessObjectBase} subclass.
     * @param id                 The ID of the desired {@code businessObjectName}.
     * @param userSession        The current user's {@link UserSession} retrieved from the request's session
     *                           attributes.
     * @return The {@link BusinessObjectBase} corresponding to the specified {@code businessObjectName} & {@code id}.
     */
    @GetMapping(
            value = "{businessObjectName}/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BusinessObjectBase getBusinessObjectAsJson(
            @PathVariable final String businessObjectName,
            @PathVariable final String id,
            @SessionAttribute final UserSession userSession
    ) {
        LOG.debug("getBusinessObjectAsJson(...) - Enter : businessObjectName={}; id={}", businessObjectName, id);

        final BusinessObjectEntry businessObjectEntry =
                businessObjectDictionaryService.getBusinessObjectEntry(businessObjectName);
        if (businessObjectEntry == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final Class<BusinessObjectBase> businessObjectClass =
                (Class<BusinessObjectBase>) businessObjectEntry.getBusinessObjectClass();
        if (businessObjectsControllersHelperService.notAuthorizedToView(businessObjectClass, userSession.getPrincipalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        final SearchService searchService = lookupDictionary.getSearchService(businessObjectClass);
        final BusinessObjectBase response = (BusinessObjectBase) searchService.find(businessObjectClass, id);

        if (ObjectUtils.isNull(response)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        LOG.debug("getBusinessObjectAsJson(...) - Exit : response={}", response);
        return response;
    }

    /**
     * Retrieve a representation of a particular {@link BusinessObjectBase}'s "detail JSON".
     *
     * @param businessObjectName The desired {@link BusinessObjectBase} subclass.
     * @param id                 The ID of the desired {@code businessObjectName}.
     * @param userSession        The current user's {@link UserSession} retrieved from the request's session
     *                           attributes.
     * @return The {@link BusinessObjectBase} corresponding to the specified {@code businessObjectName} & {@code id}.
     */
    @GetMapping(
            value = "{businessObjectName}/{id}",
            produces = KfsMediaType.APPLICATION_VND_DETAIL_JSON_VALUE

    )
    public ResponseEntity<String> getBusinessObjectAsDetailJson(
            @PathVariable final String businessObjectName,
            @PathVariable final String id,
            @SessionAttribute final UserSession userSession
    ) {
        LOG.debug("getBusinessObjectAsDetailJson(...) - Enter : businessObjectName={}; id={}", businessObjectName, id);

        final BusinessObjectEntry businessObjectEntry =
                businessObjectDictionaryService.getBusinessObjectEntry(businessObjectName);
        if (businessObjectEntry == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final Class<BusinessObjectBase> businessObjectClass =
                (Class<BusinessObjectBase>) businessObjectEntry.getBusinessObjectClass();
        if (businessObjectsControllersHelperService.notAuthorizedToView(businessObjectClass, userSession.getPrincipalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        final SearchService searchService = lookupDictionary.getSearchService(businessObjectClass);

        final HttpHeaders responseHeaders = new HttpHeaders();

        final BusinessObjectBase businessObject = (BusinessObjectBase) searchService.find(businessObjectClass, id);
        if (ObjectUtils.isNull(businessObject)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final ResponseEntity<String> response = createResponse(
                responseHeaders,
                List.of(businessObject),
                boToDetailJsonConverter
        );

        LOG.debug("getBusinessObjectAsDetailJson(...) - Exit : response={}", response);
        return response;
    }

    /**
     * @param businessObjectName The desired {@link BusinessObjectBase} subclass.
     * @param id                 The ID of the desired {@code businessObjectName}.
     * @param userSession        The current user's {@link UserSession} retrieved from the request's session
     *                           attributes.
     * @return The {@link Action}s supported for the {@link BusinessObjectBase} corresponding to the specified
     * {@code businessObjectName} & {@code id}.
     */
    @GetMapping("{businessObjectName}/{id}/actions")
    public List<ActionResponse> getBusinessObjectActions(
            @PathVariable final String businessObjectName,
            @PathVariable final String id,
            @SessionAttribute final UserSession userSession
    ) {
        LOG.debug("getBusinessObjectActions(...) - Enter : businessObjectName={}; id={}", businessObjectName, id);

        final BusinessObjectEntry businessObjectEntry =
                businessObjectDictionaryService.getBusinessObjectEntry(businessObjectName);
        if (businessObjectEntry == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final ActionsProvider actionsProvider = businessObjectEntry.getActionsProvider();
        if (actionsProvider == null) {
            LOG.error(
                    "getBusinessObjectActions(...) - There is no ActionsProvider : businessObjectName={}",
                    businessObjectName
            );
            // TODO: Find a more appropriate status code than UNSUPPORTED_MEDIA_TYPE
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }
        final BusinessObjectBase businessObject = getBusinessObjectAsJson(businessObjectName, id, userSession);
        final Person currentUser = userSession.getPerson();
        final List<Action> actionLinks = actionsProvider.getActionLinks(businessObject, currentUser);

        final List<ActionResponse> response =
                actionLinks
                        .stream()
                        .map(ActionResponse::from)
                        .collect(Collectors.toList());
        LOG.debug("getBusinessObjectActions(...) - Exit : response={}", response);
        return response;
    }

    /**
     * ==== CU Customization: Backported the FINP-9939 and FINP-10237 changes into this method. ====
     * 
     * @param businessObjectName     The desired {@link BusinessObjectBase} subclass.
     * @param id                     The ID of the desired {@code businessObjectName}.
     * @param collectionPropertyName The name of the property of the {@link BusinessObjectBase} subclass containing the
     *                               desired collection.
     * @param skip                   The "page". Must be >= 0; default is 0.
     * @param limit                  The number of "items" per "page" -- 1 <= limit <= {@code maxLimit}; default is
     *                               {@link #DEFAULT_PAGE_SIZE}.
     * @param sort                   A string consisting of an optional '-' followed by a supported attribute name, on
     *                               which sorting will be done.
     * @param queryParameters        Key/value pairs of additional search parameters. Will include "skip", "limit", &
     *                               "sort" too.
     * @param httpServletResponse    The response object, so the Item-Count header can be set on it.
     * @param userSession            The current user's {@link UserSession} retrieved from the request's session
     *                               attributes.
     * @return The requested collection of the {@link BusinessObjectBase} subclass corresponding to the specified
     * {@code businessObjectName}, {@code id}, & {@code collectionPropertyName}.
     */
    @GetMapping(
            value = "{businessObjectName}/{id}/collection/{collectionPropertyName}",
            produces = {
                MediaType.APPLICATION_JSON_VALUE,
                // TODO: This shouldn't be necessary
                KfsMediaType.APPLICATION_VND_COLLECTION_JSON_VALUE
            }
    )
    public String getCollectionResource(
            @PathVariable final String businessObjectName,
            @PathVariable final String id,
            @PathVariable final String collectionPropertyName,
            @RequestParam(required = false, defaultValue = "0") final int skip,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGE_SIZE_STRING) final int limit,
            @RequestParam(required = false) final String sort,
            @RequestParam final MultiValueMap<String, String> queryParameters,
            final HttpServletResponse httpServletResponse,
            @SessionAttribute final UserSession userSession
    ) {
        LOG.debug(
                "getCollectionResource(...) - Enter : businessObjectName={}; id={}; collectionPropertyName={}",
                businessObjectName,
                id,
                collectionPropertyName
        );

        final BusinessObjectEntry businessObjectEntry =
                businessObjectDictionaryService.getBusinessObjectEntry(businessObjectName);
        if (businessObjectEntry == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final Class<? extends BusinessObjectBase> businessObjectClass =
                (Class<? extends BusinessObjectBase>) businessObjectEntry.getBusinessObjectClass();
        if (businessObjectsControllersHelperService.notAuthorizedToView(businessObjectClass, userSession.getPrincipalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        final SearchService searchService = lookupDictionary.getSearchService(businessObjectClass);
        // ==== CU Customization: Updated this line (and removed the comment before it) for the FINP-9939 backport. ====
        final BusinessObjectBase foundBusinessObject = (BusinessObjectBase) searchService.find(businessObjectClass, id);

        if (ObjectUtils.isNull(foundBusinessObject)) {
            LOG.debug("getCollectionResource(...) - Could not find BO : id={}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final Class<? extends BusinessObjectBase> collectionClass;
        // ==== CU Customization: Updated this try/catch block with the FINP-9939 and FINP-10237 changes. ====
        try {
            Object collectionObject = null;
            if (ObjectUtils.isNestedAttribute(collectionPropertyName)) {
                final String nestedAttributePrefix = ObjectUtils.getNestedAttributePrefix(collectionPropertyName);
                final Object value = ObjectUtils.getPropertyValue(foundBusinessObject, nestedAttributePrefix);
                if (ObjectUtils.isNotNull(value) && value instanceof BusinessObjectBase) {
                    final Object realValue = ProxyHelper.isProxy(value) ? ProxyHelper.getRealObject(value) : value;
                    final String nestedAttributePrimitive = ObjectUtils.getNestedAttributePrimitive(collectionPropertyName);
                    collectionObject = PropertyUtils.getProperty(realValue, nestedAttributePrimitive);
                }
            } else {
                collectionObject = PropertyUtils.getProperty(foundBusinessObject, collectionPropertyName);
            }
            if (!(collectionObject instanceof Collection)) {
                LOG.error(
                        "getCollectionResource(...) - Property is not a collection : collectionPropertyName={}",
                        collectionPropertyName
                );
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }

            final Collection collection = (Collection) collectionObject;
            if (collection.isEmpty()) {
                return "[]";
            }
            collectionClass = (Class<? extends BusinessObjectBase>) collection.stream().findFirst().get().getClass();
        } catch (final IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            LOG.error(
                    "getCollectionResource(...) - Could not find collection : collectionPropertyName={}",
                    collectionPropertyName
            );
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final KfsPageable pageable = new KfsPageable(skip, limit, sort, collectionClass);

        final String response = collectionConverter.convert(
                businessObjectClass,
                collectionClass,
                collectionPropertyName,
                userSession.getPerson(),
                foundBusinessObject,
                httpServletResponse,
                pageable,
                queryParameters
        );
        LOG.debug("getCollectionResource(...) - Exit : response={}", response);
        return response;
    }

    /**
     * @param businessObjectName The name of the {@link BusinessObject} whose Details are desired.
     * @param userSession        A {@link UserSession} retrieved from the request's session attributes.
     * @return The appropriate {@link DetailsResponse}.
     * @throws ResponseStatusException if the user is not authorized for the requested Details (FORBIDDEN).
     */
    @GetMapping("{businessObjectName}/details")
    public DetailsResponse getBusinessObjectDetails(
            @PathVariable final String businessObjectName,
            @SessionAttribute final UserSession userSession
    ) {
        LOG.debug("getBusinessObjectDetails(...) - Enter : businessObjectName={}", businessObjectName);

        final BusinessObjectEntry businessObjectEntry =
                businessObjectDictionaryService.getBusinessObjectEntry(businessObjectName);

        final Class<? extends BusinessObjectBase> businessObjectClass =
                (Class<? extends BusinessObjectBase>) businessObjectEntry.getBusinessObjectClass();

        if (businessObjectsControllersHelperService.notAuthorizedToView(businessObjectClass, userSession.getPrincipalId())) {
            LOG.warn("getBusinessObjectDetails(...) - User is not authorized to view the Details for this "
                      + "business object : businessObjectName={}",
                    businessObjectName
            );
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        final String title = detailsDictionary.getDetailsTitle(businessObjectClass);
        final List<Section> sections = detailsDictionary.getSections(businessObjectClass);
        final DetailsResponse response = new DetailsResponse(title, sections);

        LOG.debug("getBusinessObjectDetails(...) - Exit : response={}", response);
        return response;
    }

    /**
     * TODO: Returns an XML file. Is this similar to the special handling for BatchFile? Can/should it be made to fit
     *       into other, existing, APIs or is this unique?
     *       Maybe like the following?
     *          GET /business-objects/DocumentType/{id}/export
     *          Accept: application/octet-stream
     *
     * @param id          The ID of the desired {@code DocumentType}.
     * @param userSession The current user's {@link UserSession} retrieved from the request's session attributes.
     * @return The exported XML file of the {@link DocumentType} associated with {@code id}.
     */
    @GetMapping(
            value = "export/DocumentType/{id}",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<Resource> getBusinessObjectDocumentTypeExport(
            @PathVariable(required = false) final String id,
            @SessionAttribute final UserSession userSession
    ) {
        LOG.debug("getBusinessObjectDocumentTypeExport(...) - Enter : id={}", id);

        // TODO: Can ID just be required instead...and this be removed?
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (businessObjectsControllersHelperService.notAuthorizedToView(DocumentType.class, userSession.getPrincipalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        final SearchService searchService = lookupDictionary.getSearchService(DocumentType.class);
        final DocumentType documentTypeBusinessObject = (DocumentType) searchService.find(DocumentType.class, id);

        if (ObjectUtils.isNull(documentTypeBusinessObject)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(
                HttpHeaders.CONTENT_TYPE,
                // TODO: I don't believe charset is needed here
                MediaType.APPLICATION_OCTET_STREAM_VALUE + "; charset=" + StandardCharsets.UTF_8.name()
        );

        final ContentDisposition contentDisposition = ContentDisposition
                .attachment()
                .filename(documentTypeBusinessObject.getName() + ".xml")
                .build();
        responseHeaders.setContentDisposition(contentDisposition);

        final String documentTypeXml = documentTypeService.export(documentTypeBusinessObject);
        final byte[] xmlBytes = documentTypeXml.getBytes(StandardCharsets.UTF_8);
        final ByteArrayResource body = new ByteArrayResource(xmlBytes);

        final ResponseEntity<Resource> response = ResponseEntity
                .ok()
                .headers(responseHeaders)
                .body(body);
        LOG.debug("getBusinessObjectDocumentTypeExport(...) - Exit : response={}", response);
        return response;
    }

    /**
     * TODO: This is not a BusinessObject; find a better home
     * TODO: produces = MediaType.TEXT_PLAIN_VALUE
     *
     * @return A four-digit integer, representing the current university fiscal year.
     */
    @GetMapping(
            value = "currentFiscalYear",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Integer getCurrentFiscalYear() {
        LOG.debug("getCurrentFiscalYear(...) - Enter");
        final SystemOptions currentYearOptions = optionsService.getCurrentYearOptions();
        final Integer response = currentYearOptions.getUniversityFiscalYear();
        LOG.debug("getCurrentFiscalYear(...) - Exit : response={}", response);
        return response;
    }

    /**
     * TODO: Replace with Spring's {@link org.springframework.data.domain.Pageable}.
     */
    class KfsPageable {
        final int skip;
        final int limit;
        final String sortField;
        final boolean sortAscending;

        KfsPageable(
                final int skip,
                final int limit,
                final String sort,
                final Class<? extends BusinessObjectBase> businessObjectClass
        ) {
            this.skip = determineActualSkip(skip);
            this.limit = determineActualLimit(limit);

            final SortDefinition sortDefinition = getSortDefinition(sort, businessObjectClass);
            if (sortDefinition == null) {
                sortAscending = false;
                sortField = null;
            } else {
                sortAscending = sortDefinition.getSortAscending();

                final List<String> attributeNames = sortDefinition.getAttributeNames();
                sortField = attributeNames.isEmpty() ? null : attributeNames.get(0);
            }
        }

        // TODO: I dislike quietly not doing what was requested; better to fail-fast
        private int determineActualSkip(final int skip) {
            // skip >= 0
            return Math.max(0, skip);
        }

        // TODO: I dislike quietly not doing what was requested; better to fail-fast
        private int determineActualLimit(final int limit) {
            // 1 <= limit <= maxResultsLimit
            return Math.min(Math.max(1, limit), maxLimit);
        }

        private SortDefinition getSortDefinition(
                final String sortParam,
                final Class<? extends BusinessObjectBase> businessObjectClass
        ) {
            if (sortParam == null) {
                return businessObjectDictionaryService.getLookupDefaultSortDefinition(businessObjectClass);
            }

            final boolean descending = sortParam.startsWith("-");
            final String sortKey = descending ? sortParam.substring(1) : sortParam;

            final SortDefinition sortDefinition = new SortDefinition();
            sortDefinition.setAttributeName(sortKey);
            sortDefinition.setSortAscending(!descending);
            return sortDefinition;
        }
    }

}
