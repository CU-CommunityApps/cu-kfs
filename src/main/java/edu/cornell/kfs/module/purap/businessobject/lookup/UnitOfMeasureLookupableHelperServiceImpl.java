package edu.cornell.kfs.module.purap.businessobject.lookup;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.kns.lookup.KualiLookupableHelperServiceImpl;
import org.kuali.kfs.krad.bo.BusinessObject;

public class UnitOfMeasureLookupableHelperServiceImpl extends KualiLookupableHelperServiceImpl {

    /**
     * Add * to the end of the search values. This way if user only enters the first few
     * letters of a value it will get as result all entries that start with that value
     * 
     * @see org.kuali.kfs.kns.lookup.KualiLookupableHelperServiceImpl#getSearchResults(java.util.Map)
     */
    @Override
    public List<? extends BusinessObject> getSearchResults(Map<String, String> fieldValues) {

        if (fieldValues.containsKey("itemUnitOfMeasureCode")) {
            String itemUnitOfMeasureCode = fieldValues.get("itemUnitOfMeasureCode");
            if (StringUtils.isNotEmpty(itemUnitOfMeasureCode)) {
                itemUnitOfMeasureCode = itemUnitOfMeasureCode + "*";

            } else {
                itemUnitOfMeasureCode = "*";
            }
            fieldValues.put("itemUnitOfMeasureCode", itemUnitOfMeasureCode);
        }

        if (fieldValues.containsKey("itemUnitOfMeasureDescription")) {
            String itemUnitOfMeasureDescription = fieldValues.get("itemUnitOfMeasureDescription");

            if (StringUtils.isNotEmpty(itemUnitOfMeasureDescription)) {
                itemUnitOfMeasureDescription = itemUnitOfMeasureDescription + "*";

            } else {
                itemUnitOfMeasureDescription = "*";
            }
            fieldValues.put("itemUnitOfMeasureDescription", itemUnitOfMeasureDescription);
        }
        return super.getSearchResults(fieldValues);
    }

}
