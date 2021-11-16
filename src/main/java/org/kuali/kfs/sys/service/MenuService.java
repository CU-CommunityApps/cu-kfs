/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
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
package org.kuali.kfs.sys.service;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.cache.CacheManagerRegistry;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.datadictionary.legacy.DocumentDictionaryService;
import org.kuali.kfs.kew.doctype.bo.DocumentType;
import org.kuali.kfs.kew.doctype.service.DocumentTypeService;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.kim.api.permission.PermissionService;
import org.kuali.kfs.krad.document.DocumentAuthorizer;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.KRADUtils;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.dashboardnav.MenuDao;
import org.kuali.kfs.sys.dashboardnav.models.NavigationDashboard;
import org.kuali.kfs.sys.dashboardnav.models.NavigationDashboardLink;
import org.kuali.kfs.sys.dashboardnav.models.NavigationDashboardLinkPermissionDetail;
import org.kuali.kfs.sys.dashboardnav.models.NavigationDashboardList;
import org.kuali.kfs.sys.dashboardnav.models.NavigationObject;
import org.kuali.kfs.sys.dataaccess.InstitutionLogoDao;
import org.kuali.kfs.sys.reports.ReportsService;
import org.springframework.cache.Cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

/* Cornell Customization: backport redis*/
public class MenuService {
    public static final String MENU_LINKS_CACHE_NAME = "MenuLinks";

    private PermissionService permissionService;
    private DocumentTypeService documentTypeService;
    private CacheManagerRegistry cacheManagerRegistry;
    private DocumentDictionaryService documentDictionaryService;
    private ConfigurationService configurationService;
    private ReportsService reportsService;
    private InstitutionLogoDao institutionLogoDao;
    private MenuDao dao;

    public Map<String, List<? extends NavigationObject>> userMenu(Person user, boolean useCached) {
        Map<String, List<? extends NavigationObject>> results = null;

        if (useCached) {
            Cache.ValueWrapper cachedResults = getMenuLinksCache().get(user.getPrincipalName());
            if (cachedResults != null) {
                results = (Map<String, List<? extends NavigationObject>>) cachedResults.get();
            }
        }

        if (results == null) {
            List<NavigationDashboardLink> allLinks = dao.getNavigationObjectsAll(NavigationDashboardLink.class);

            Map<String, Boolean> linkPermissions = new HashMap<>();
            // Start with all links and remove those associated with dashboards/lists as we go
            Map<String, NavigationDashboardLink> unassignedLinks = new HashMap<>();
            for (NavigationDashboardLink link : allLinks) {
                Boolean hasPermission = userHasAccessToLink(user, link);
                linkPermissions.put(link.getId(), hasPermission);
                if (hasPermission) {
                    unassignedLinks.put(link.getId(), link);
                }
            }

            List<NavigationDashboard> dashboardsWithLinks = dao.getNavigationObjectsAll(NavigationDashboard.class);
            for (NavigationDashboard dashboard : dashboardsWithLinks) {
                List<NavigationDashboardList> lists = dashboard.getLists();
                for (NavigationDashboardList list : lists) {
                    ListIterator<NavigationDashboardLink> linkIterator = list.getLinks().listIterator();
                    while (linkIterator.hasNext()) {
                        NavigationDashboardLink link = linkIterator.next();
                        if (!linkPermissions.get(link.getId())) {
                            linkIterator.remove();
                        }
                        // Remove assigned links so we can track the unassigned links and include them in the results
                        unassignedLinks.remove(link.getId());
                    }
                }
            }

            results = new HashMap<>();
            results.put("dashboards", dashboardsWithLinks);
            results.put("unassignedLinks", new ArrayList<>(unassignedLinks.values()));

            getMenuLinksCache().put(user.getPrincipalName(), results);
        }

        return results;
    }

    private boolean userHasAccessToLink(Person user, NavigationDashboardLink link) {
        if (!canViewLink(user.getPrincipalId(), link)) {
            return false;
        }

        if (StringUtils.isNotBlank(link.getDocTypeCode())) {
            if (ObjectUtils.isNotNull(link.getDocumentClass())) {
                if (link.isTransactionalDocLink() || link.isGlobalBusinessObjectLink()) {
                    return canInitiateDocument(link.getDocTypeCode(), user);
                } else if (link.isMaintainableDocLink()) {
                    return canViewBusinessObjectLookup(link.getMaintainableDataObjectClass(), user);
                }
            }
            return false;
        } else if (StringUtils.isNotBlank(link.getBoClassName())) {
            return canViewBusinessObjectLookup(link.getBusinessObjectClass(), user);
        }
        return true;
    }

    public Map<String, Object> menuSettings() {
        Map<String, Object> settings = new HashMap<>();

        settings.put("help", getHelpUrl());
        settings.put("versions", getVersionInformation());
        settings.put(KFSPropertyConstants.SIGNOUT_URL, getSignoutUrl());
        settings.put(KFSPropertyConstants.DOC_SEARCH_URL, getDocSearchUrl());
        settings.put(KFSPropertyConstants.ACTION_LIST_URL, getActionListUrl());
        settings.put("logoUrl", getLogoUrl());

        if (reportsService.isReportsEnabled()) {
            settings.put(KFSPropertyConstants.REPORTS_URL, getReportsUrl());
        }

        return settings;
    }

    public boolean hasConfigurationPermission(String principalId) {
        Map<String, String> permissionDetails = new HashMap<>();
        permissionDetails.put(KimConstants.AttributeConstants.NAMESPACE_CODE, KFSConstants.CoreModuleNamespaces.KFS);
        permissionDetails.put(KimConstants.AttributeConstants.ACTION_CLASS,
                KFSConstants.ReactComponents.INSTITUTION_CONFIG);

        return permissionService.hasPermissionByTemplate(principalId, KFSConstants.CoreModuleNamespaces.KFS,
                KimConstants.PermissionTemplateNames.USE_SCREEN, permissionDetails);
    }

    private String getApplicationUrl() {
        return configurationService.getPropertyValueAsString(KFSConstants.APPLICATION_URL_KEY);
    }

    private String getHelpUrl() {
        return configurationService.getPropertyValueAsString("externalizable.help.url") +
                configurationService.getPropertyValueAsString("externalizable.help.url.default");
    }

    private String getSignoutUrl() {
        return getApplicationUrl() + "/logout.do";
    }

    private String getDocSearchUrl() {
        return configurationService.getPropertyValueAsString(KRADConstants.WORKFLOW_DOCUMENTSEARCH_URL_KEY);
    }

    private String getActionListUrl() {
        return getApplicationUrl() + "/ActionList.do";
    }

    private String getReportsUrl() {
        return "reports";
    }

    private String getLogoUrl() {
        return institutionLogoDao.getInstitutionLogo().getLogoUrl();
    }

    private Map<String, String> getVersionInformation() {
        Map<String, String> versions = new HashMap<>();
        versions.put("Kuali Financials", configurationService.getPropertyValueAsString(KFSConstants.APPLICATION_VERSION_KEY));
        return versions;
    }

    private boolean canViewLink(String principalId, NavigationDashboardLink link) {
        if (link.getTemplateName() == null && link.getTemplateNamespace() == null) {
            return true;
        } else {
            if (link.getPermissionDetails().isEmpty()) {
                return false;
            } else {
                Map<String, String> details = link.getPermissionDetails().parallelStream().collect(
                        Collectors.toMap(NavigationDashboardLinkPermissionDetail::getKeyCode,
                                NavigationDashboardLinkPermissionDetail::getValue));
                return permissionService.isAuthorizedByTemplate(principalId, link.getTemplateNamespace(),
                        link.getTemplateName(), details, Collections.emptyMap());
            }
        }
    }

    private boolean canViewBusinessObjectLookup(Class<?> businessObjectClass, Person person) {
        if (businessObjectClass == null) {
            return true;
        }

        return permissionService.isAuthorizedByTemplate(
                person.getPrincipalId(), KFSConstants.CoreModuleNamespaces.KFS,
                KimConstants.PermissionTemplateNames.LOOK_UP_RECORDS,
                KRADUtils.getNamespaceAndComponentSimpleName(businessObjectClass),
                Collections.emptyMap());
    }

    private boolean canInitiateDocument(String documentTypeName, Person person) {
        DocumentAuthorizer documentAuthorizer = documentDictionaryService.getDocumentAuthorizer(documentTypeName);
        DocumentType documentType = documentTypeService.getDocumentTypeByName(documentTypeName);
        return documentType != null && documentType.isActive()
                && documentAuthorizer.canInitiate(documentTypeName, person);
    }

    private Cache getMenuLinksCache() {
        return cacheManagerRegistry.getCacheManagerByCacheName(MENU_LINKS_CACHE_NAME).getCache(MENU_LINKS_CACHE_NAME);
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setDao(MenuDao dao) {
        this.dao = dao;
    }

    public void setDocumentTypeService(DocumentTypeService documentTypeService) {
        this.documentTypeService = documentTypeService;
    }

    public void setDocumentDictionaryService(DocumentDictionaryService documentDictionaryService) {
        this.documentDictionaryService = documentDictionaryService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setReportsService(ReportsService reportsService) {
        this.reportsService = reportsService;
    }

    public void setCacheManagerRegistry(CacheManagerRegistry cacheManagerRegistry) {
        this.cacheManagerRegistry = cacheManagerRegistry;
    }

    public void setInstitutionLogoDao(InstitutionLogoDao institutionLogoDao) {
        this.institutionLogoDao = institutionLogoDao;
    }
}
