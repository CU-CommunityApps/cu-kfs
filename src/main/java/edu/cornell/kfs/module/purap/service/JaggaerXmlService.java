package edu.cornell.kfs.module.purap.service;

import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.module.purap.businessobject.B2BInformation;

public interface JaggaerXmlService {

    String getJaggaerLoginXmlForEShop(Person user, B2BInformation b2bInformation);

    String getJaggaerLoginXmlForContractsPlus(Person user, B2BInformation b2bInformation);

    String getJaggaerLoginXmlForJaggaerAdmin(Person user, B2BInformation b2bInformation);

}
