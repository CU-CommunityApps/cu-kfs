package edu.cornell.kfs.module.purap.service;

import java.util.List;

import org.kuali.kfs.kim.api.identity.Person;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerRoleSet;

public interface JaggaerRoleService {

    List<String> getJaggaerRoles(Person user, JaggaerRoleSet roleSet);

}
