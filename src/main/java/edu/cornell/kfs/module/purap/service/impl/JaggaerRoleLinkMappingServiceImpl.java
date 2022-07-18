package edu.cornell.kfs.module.purap.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;

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
        Map<String, Boolean> fieldValues = new HashMap<String, Boolean>();
        /*
         * @todo move these to constants
         */
        fieldValues.put("ACTV_IND", true);
        fieldValues.put("ESHOP_LNK", roleSet == JaggaerRoleSet.ESHOP);
        fieldValues.put("CONTRACT_PLUS_LNK", roleSet == JaggaerRoleSet.CONTRACTS_PLUS);
        fieldValues.put("ADMIN_LNK", roleSet == JaggaerRoleSet.ADMINISTRATOR);
        
        
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
