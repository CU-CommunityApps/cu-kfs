package edu.cornell.kfs.kim.impl.identity;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kim.impl.identity.PersonSearchService;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.springframework.util.MultiValueMap;

import edu.cornell.kfs.kim.CuKimPropertyConstants;

public class CuPersonSearchService extends PersonSearchService {

    @Override
    public Pair<Collection<? extends BusinessObjectBase>, Integer> getSearchResults(
            Class<? extends BusinessObjectBase> businessObjectClass, MultiValueMap<String, String> fieldValues,
            int skip, int limit, String sortField, boolean sortAscending) {
        return super.getSearchResults(businessObjectClass, fieldValues, skip, limit, transformSortFieldIfNecessary(sortField), sortAscending);
    }

    private static String transformSortFieldIfNecessary(final String sortField) {
        if (sortField == null) {
            return null;
        }

        String transformedSortField = sortField;
        if (StringUtils.equalsIgnoreCase(sortField,
                KIMPropertyConstants.Person.NAME + CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX)) {
            transformedSortField = KIMPropertyConstants.Person.LAST_NAME;
        }

        return transformedSortField;
    }

}
