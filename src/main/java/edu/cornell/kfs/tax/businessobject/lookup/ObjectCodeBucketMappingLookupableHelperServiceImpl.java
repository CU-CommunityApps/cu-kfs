package edu.cornell.kfs.tax.businessobject.lookup;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kns.lookup.CollectionIncomplete;
import org.kuali.kfs.kns.lookup.KualiLookupableHelperServiceImpl;
import org.kuali.kfs.kns.lookup.LookupUtils;
import org.kuali.rice.krad.bo.BusinessObject;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CUTaxPropertyConstants;
import edu.cornell.kfs.tax.businessobject.ObjectCodeBucketMapping;

public class ObjectCodeBucketMappingLookupableHelperServiceImpl extends KualiLookupableHelperServiceImpl {

    /**
     * Overridden to treat the Any/None payment reason of "*" as a literal instead of a wildcard.
     */
    @Override
    protected List<? extends BusinessObject> getSearchResultsHelper(
            Map<String, String> fieldValues, boolean unbounded) {
        String paymentReasonCode = fieldValues.get(CUTaxPropertyConstants.DV_PAYMENT_REASON_CODE);
        if (StringUtils.equalsIgnoreCase(CUTaxConstants.ANY_OR_NONE_PAYMENT_REASON, paymentReasonCode)) {
            List<? extends BusinessObject> results = super.getSearchResultsHelper(fieldValues, true);
            List<? extends BusinessObject> filteredResults = results.stream()
                    .filter(this::bucketMappingHasPaymentReasonOfAnyOrNone)
                    .collect(Collectors.toList());
            Integer resultsLimit = getResultsLimitForCustomSearch(filteredResults, unbounded);
            Long actualResultsSize = Long.valueOf(filteredResults.size());
            if (actualResultsSize > resultsLimit) {
                filteredResults = filteredResults.subList(0, resultsLimit);
            }
            return new CollectionIncomplete<>(filteredResults, actualResultsSize);
        } else {
            return super.getSearchResultsHelper(fieldValues, unbounded);
        }
    }

    private boolean bucketMappingHasPaymentReasonOfAnyOrNone(BusinessObject bucketMapping) {
        return bucketMapping instanceof ObjectCodeBucketMapping
                && StringUtils.equalsIgnoreCase(CUTaxConstants.ANY_OR_NONE_PAYMENT_REASON,
                        ((ObjectCodeBucketMapping) bucketMapping).getDvPaymentReasonCode());
    }

    private Integer getResultsLimitForCustomSearch(List<? extends BusinessObject> results, boolean unbounded) {
        Integer resultsLimit = null;
        if (!unbounded) {
            resultsLimit = LookupUtils.getSearchResultsLimit(getBusinessObjectClass());
        }
        if (resultsLimit == null || resultsLimit < 0) {
            resultsLimit = results.size();
        }
        return Math.min(resultsLimit, results.size());
    }

}
