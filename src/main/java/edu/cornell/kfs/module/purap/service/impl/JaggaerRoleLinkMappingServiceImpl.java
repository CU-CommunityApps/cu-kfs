package edu.cornell.kfs.module.purap.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerRoleSet;
import edu.cornell.kfs.module.purap.businessobject.JaggaerRoleLinkMapping;
import edu.cornell.kfs.module.purap.service.JaggaerRoleLinkMappingService;

public class JaggaerRoleLinkMappingServiceImpl implements JaggaerRoleLinkMappingService {
    private static final Logger LOG = LogManager.getLogger();
    protected BusinessObjectService businessObjectService;

    @Override
    public Collection<JaggaerRoleLinkMapping> getEShopLinkRoles() {
        return getJaggaerLinkRoles(JaggaerRoleSet.ESHOP);
    }

    @Override
    public Collection<JaggaerRoleLinkMapping> getContractsPlusLinkRoles() {
        return getJaggaerLinkRoles(JaggaerRoleSet.CONTRACTS_PLUS);
    }

    @Override
    public Collection<JaggaerRoleLinkMapping> getJaggaerAdminLinkRoles() {
        return getJaggaerLinkRoles(JaggaerRoleSet.ADMINISTRATOR);
    }
    
    protected Collection<JaggaerRoleLinkMapping> getJaggaerLinkRoles(JaggaerRoleSet roleSet) {
        Map<String, String> fieldValues = new HashMap<>();
        fieldValues.put(CUPurapConstants.JaggaerLinkMappingFieldNames.ACTIVE, KFSConstants.ACTIVE_INDICATOR);
        if (roleSet == JaggaerRoleSet.ESHOP) {
            fieldValues.put(CUPurapConstants.JaggaerLinkMappingFieldNames.ESHOP_LINK, KFSConstants.ACTIVE_INDICATOR);
        } else if (roleSet == JaggaerRoleSet.CONTRACTS_PLUS) {
            fieldValues.put(CUPurapConstants.JaggaerLinkMappingFieldNames.CONTACTS_PLUS_LINK, KFSConstants.ACTIVE_INDICATOR);
        } else if (roleSet == JaggaerRoleSet.ADMINISTRATOR) {
            fieldValues.put(CUPurapConstants.JaggaerLinkMappingFieldNames.JAGGAER_ADMIN_LINK, KFSConstants.ACTIVE_INDICATOR);
        } else {
            throw new IllegalStateException("Found an unexpected role: " + roleSet);
        }
        
        Collection<JaggaerRoleLinkMapping> linkMapping = businessObjectService.findMatching(JaggaerRoleLinkMapping.class, fieldValues);
        if (LOG.isDebugEnabled()) {
            if (CollectionUtils.isEmpty(linkMapping)) {
                LOG.debug("getJaggaerLinkRoles, found no link mappings for " + roleSet);
            } else {
                linkMapping.forEach(link -> LOG.debug("getJaggaerLinkRoles, roleset: " + roleSet + " link: " + link.toString()));
            }
        }
        return linkMapping;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
