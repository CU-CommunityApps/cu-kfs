package edu.cornell.kfs.kim.lookup;

import java.util.List;
import java.util.Map;

import org.kuali.kfs.kim.lookup.RoleLookupableHelperServiceImpl;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.util.KRADConstants;

/**
 * KFSPTS-22142 Add local customization to fix Role lookup from a child/collection object on a maintenance doc
 */
public class CuRoleLookupableHelperServiceImpl extends RoleLookupableHelperServiceImpl {

    @Override
    public List<? extends BusinessObject> getSearchResults(final Map<String, String> fieldValues) {
        fieldValues.remove(KRADConstants.REFERENCES_TO_REFRESH);
        return super.getSearchResults(fieldValues);
    }
}
