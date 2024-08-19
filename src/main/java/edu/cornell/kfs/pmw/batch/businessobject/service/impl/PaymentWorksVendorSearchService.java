package edu.cornell.kfs.pmw.batch.businessobject.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.kuali.kfs.core.api.search.SearchOperator;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.service.impl.DefaultSearchService;
import org.springframework.util.MultiValueMap;

import edu.cornell.kfs.pmw.batch.PaymentWorksPropertiesConstants.PaymentWorksVendor;

public class PaymentWorksVendorSearchService extends DefaultSearchService {

    @Override
    protected MultiValueMap<String, String> transformSearchParams(
            final Class<? extends BusinessObjectBase> boClass,
            final MultiValueMap<String, String> searchParams) {
        final MultiValueMap<String, String> transformedSearchParams =
                super.transformSearchParams(boClass, searchParams);

        transformVendorNumberParams(transformedSearchParams);
        transformProcessTimestampParams(transformedSearchParams);

        return transformedSearchParams;
    }

    private void transformVendorNumberParams(final MultiValueMap<String, String> transformedSearchParams) {
        if (transformedSearchParams.containsKey(PaymentWorksVendor.KFS_VENDOR_NUMBER)) {
            final String kfsVendorNumber = transformedSearchParams
                    .remove(PaymentWorksVendor.KFS_VENDOR_NUMBER)
                    .get(0);
            final Pair<String, String> parsedKfsVendorNumber = parseKfsVendorNumber(kfsVendorNumber);
            if (StringUtils.isNotBlank(parsedKfsVendorNumber.getLeft())) {
                transformedSearchParams.put(
                        PaymentWorksVendor.KFS_VENDOR_HEADER_GENERATED_IDENTIFIER,
                        List.of(parsedKfsVendorNumber.getLeft()));
            }
            if (StringUtils.isNotBlank(parsedKfsVendorNumber.getRight())) {
                transformedSearchParams.put(
                        PaymentWorksVendor.KFS_VENDOR_DETAIL_ASSIGNED_IDENTIFIER,
                        List.of(parsedKfsVendorNumber.getRight()));
            }
        }
    }

    private Pair<String, String> parseKfsVendorNumber(final String kfsVendorNumber) {
        if (StringUtils.isBlank(kfsVendorNumber)) {
            return Pair.of(KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING);
        } else if (!StringUtils.contains(kfsVendorNumber, KFSConstants.DASH)) {
            return Pair.of(kfsVendorNumber, KFSConstants.EMPTY_STRING);
        }
        final String vendorHeaderId = StringUtils.substringBefore(kfsVendorNumber, KFSConstants.DASH);
        final String vendorDetailId = StringUtils.substringAfter(kfsVendorNumber, KFSConstants.DASH);
        return Pair.of(
                StringUtils.defaultIfBlank(vendorHeaderId, KFSConstants.DASH),
                StringUtils.defaultIfBlank(vendorDetailId, KFSConstants.DASH));
    }

    /*
     * NOTE: We can potentially remove the workaround below when we upgrade to the 2023-11-01 financials patch,
     * at which time we can configure the lookup definition to reference the actual processTimestamp field. 
     */
    private void transformProcessTimestampParams(final MultiValueMap<String, String> transformedSearchParams) {
        String processTimestampFrom = null;
        String processTimestampTo = null;

        if (transformedSearchParams.containsKey(PaymentWorksVendor.PROCESS_TIMESTAMP_FROM)) {
            processTimestampFrom = transformedSearchParams
                    .remove(PaymentWorksVendor.PROCESS_TIMESTAMP_FROM)
                    .get(0);
            processTimestampFrom = ObjectUtils.clean(processTimestampFrom);
        }
        if (transformedSearchParams.containsKey(PaymentWorksVendor.PROCESS_TIMESTAMP_TO)) {
            processTimestampTo = transformedSearchParams
                    .remove(PaymentWorksVendor.PROCESS_TIMESTAMP_TO)
                    .get(0);
            processTimestampTo = ObjectUtils.clean(processTimestampTo);
        }

        final String processTimestampSearchString = buildProcessTimestampSearchString(
                processTimestampFrom, processTimestampTo);
        if (StringUtils.isNotBlank(processTimestampSearchString)) {
            transformedSearchParams.put(
                    PaymentWorksVendor.PROCESS_TIMESTAMP,
                    List.of(processTimestampSearchString));
        }
    }

    private String buildProcessTimestampSearchString(final String fromTimestamp, final String toTimestamp) {
        if (StringUtils.isNotBlank(fromTimestamp)) {
            if (StringUtils.isNotBlank(toTimestamp)) {
                return fromTimestamp + SearchOperator.BETWEEN + toTimestamp;
            } else {
                return SearchOperator.GREATER_THAN_EQUAL + fromTimestamp;
            }
        } else if (StringUtils.isNotBlank(toTimestamp)) {
            return SearchOperator.LESS_THAN_EQUAL + toTimestamp;
        } else {
            return KFSConstants.EMPTY_STRING;
        }
    }

}
