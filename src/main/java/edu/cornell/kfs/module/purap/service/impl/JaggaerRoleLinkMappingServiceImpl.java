package edu.cornell.kfs.module.purap.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerRoleSet;
import edu.cornell.kfs.module.purap.CUPurapPropertyConstants;
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
    
    @Override
    public Collection<JaggaerRoleLinkMapping> getJaggaerLinkRoles(JaggaerRoleSet roleSet) {
        Map<String, String> fieldValues = new HashMap<>();
        fieldValues.put(CUPurapPropertyConstants.ACTIVE, KFSConstants.ACTIVE_INDICATOR);
        fieldValues.put(roleSet.linkMappingFieldName, KFSConstants.ACTIVE_INDICATOR);
        
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
