package edu.cornell.kfs.module.purap.document.service;

import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.module.purap.document.service.B2BShoppingService;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerRoleSet;

public interface CuB2BShoppingService extends B2BShoppingService {

    String getPunchOutUrlForRoleSet(Person user, JaggaerRoleSet roleSet);

}
