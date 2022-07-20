package edu.cornell.kfs.module.purap.service;

import java.util.Collection;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerRoleSet;
import edu.cornell.kfs.module.purap.businessobject.JaggaerRoleLinkMapping;

public interface JaggaerRoleLinkMappingService {
    
    public Collection<JaggaerRoleLinkMapping> getEShopLinkRoles();
    
    public Collection<JaggaerRoleLinkMapping> getContractsPlusLinkRoles();
    
    public Collection<JaggaerRoleLinkMapping> getJaggaerAdminLinkRoles();
    
    public Collection<JaggaerRoleLinkMapping> getJaggaerLinkRoles(JaggaerRoleSet roleSet);
}
