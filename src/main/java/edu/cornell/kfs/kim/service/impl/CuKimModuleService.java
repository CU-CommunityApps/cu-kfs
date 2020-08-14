package edu.cornell.kfs.kim.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.kim.impl.identity.PersonImpl;
import org.kuali.kfs.kim.service.impl.KimModuleService;

public class CuKimModuleService extends KimModuleService {
    
    @Override
    public List<List<String>> listAlternatePrimaryKeyFieldNames(Class businessObjectInterfaceClass) {
        List<List<String>> alternateKeyList = super.listAlternatePrimaryKeyFieldNames(businessObjectInterfaceClass);

        if (PersonImpl.class.isAssignableFrom(businessObjectInterfaceClass)) {
            if (alternateKeyList == null) {
                alternateKeyList = new ArrayList<List<String>>();
            }

            ArrayList<String> keyList = new ArrayList<String>();
            keyList.add("principalName");
            alternateKeyList.add(keyList);
        }
        return alternateKeyList;
    }

}
