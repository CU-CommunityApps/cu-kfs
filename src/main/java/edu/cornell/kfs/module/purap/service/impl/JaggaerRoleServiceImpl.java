package edu.cornell.kfs.module.purap.service.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kim.api.identity.Person;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerRoleSet;
import edu.cornell.kfs.module.purap.service.JaggaerRoleService;

public class JaggaerRoleServiceImpl implements JaggaerRoleService {

    private static final Logger LOG = LogManager.getLogger();

    @Override
    public List<String> getJaggaerRoles(Person user, JaggaerRoleSet roleSet) {
        LOG.warn("getJaggaerRoles, This method's logic has not been implemented yet; returning an empty list.");
        return List.of();
    }

}
