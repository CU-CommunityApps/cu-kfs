package edu.cornell.kfs.kim.impl.identity;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kim.impl.identity.PersonSearchService;
import org.kuali.kfs.krad.bo.BusinessObjectBase;

import edu.cornell.kfs.kim.CuKimPropertyConstants;

public class CuPersonSearchService extends PersonSearchService {

    @Override
    protected Pair<Collection<? extends BusinessObjectBase>, Integer> executeSearch(
            Class<? extends BusinessObjectBase> businessObjectClass, int skip, int limit, String sortField,
            boolean sortAscending, Map<String, String> searchProps) {
        return super.executeSearch(businessObjectClass, skip, limit, transformSortFieldIfNecessary(sortField),
                sortAscending, searchProps);
    }

    private static String transformSortFieldIfNecessary(final String sortField) {
        if (sortField == null) {
            return null;
        }

        String transformedSortField = sortField;
        if (StringUtils.equalsIgnoreCase(transformedSortField,
                KIMPropertyConstants.Person.NAME + CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX)) {
            transformedSortField = transformedSortField.replace(
                    KIMPropertyConstants.Person.NAME + CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX,
                    KIMPropertyConstants.Person.LAST_NAME);
        }

        return transformedSortField;
    }

}
