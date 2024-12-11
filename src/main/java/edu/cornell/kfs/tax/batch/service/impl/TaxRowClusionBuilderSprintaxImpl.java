package edu.cornell.kfs.tax.batch.service.impl;

import org.kuali.kfs.coreservice.framework.parameter.ParameterService;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CUTaxConstants.Tax1042SParameterNames;
import edu.cornell.kfs.tax.CUTaxConstants.TaxCommonParameterNames;
import edu.cornell.kfs.tax.batch.TaxRowClusionBuilderBase;
import edu.cornell.kfs.tax.batch.TaxStatisticsHandler;
import edu.cornell.kfs.tax.dataaccess.impl.TaxStatType;

public class TaxRowClusionBuilderSprintaxImpl extends TaxRowClusionBuilderBase {

    public TaxRowClusionBuilderSprintaxImpl(final TaxStatisticsHandler statsHandler,
            final ParameterService parameterService) {
        super(statsHandler, parameterService, CUTaxConstants.TAX_1042S_PARM_DETAIL);
    }

    public TaxRowClusionBuilderSprintaxImpl appendCheckForVendorType(final String vendorTypeCode) {
        checkAgainstExclusions(
                CUTaxConstants.TAX_TYPE_1042S + TaxCommonParameterNames.EXCLUDE_BY_VENDOR_TYPE_PARAMETER_SUFFIX,
                vendorTypeCode, TaxStatType.NUM_VENDOR_TYPE_EXCLUSIONS_DETERMINED);
        return this;
    }

    public TaxRowClusionBuilderSprintaxImpl appendCheckForVendorOwnershipType(
            final String vendorOwnershipTypeCode) {
        checkAgainstExclusions(
                CUTaxConstants.TAX_TYPE_1042S + TaxCommonParameterNames.EXCLUDE_BY_OWNERSHIP_TYPE_PARAMETER_SUFFIX,
                vendorOwnershipTypeCode, TaxStatType.NUM_OWNERSHIP_TYPE_EXCLUSIONS_DETERMINED);
        return this;
    }

    public TaxRowClusionBuilderSprintaxImpl appendCheckForDocumentType(final String documentTypeCode) {
        checkAgainstExclusions(Tax1042SParameterNames.PDP_EXCLUDED_DOC_TYPES, documentTypeCode,
                TaxStatType.NUM_PDP_DOCTYPE_EXCLUSIONS_DETERMINED);
        return this;
    }

    public TaxRowClusionBuilderSprintaxImpl appendCheckForPaymentReason(final String paymentReasonCode) {
        checkAgainstExclusions(Tax1042SParameterNames.EXCLUDED_PAYMENT_REASON_CODE, paymentReasonCode,
                TaxStatType.NUM_PAYMENT_REASON_EXCLUSIONS_DETERMINED);
        return this;
    }

    public TaxRowClusionBuilderSprintaxImpl appendCheckForPaymentLine1Address(final String paymentLine1Address) {
        this.checkAgainstExcludedPrefixes(Tax1042SParameterNames.OTHER_INCOME_EXCLUDED_PMT_LN1_ADDR,
                paymentLine1Address, TaxStatType.NUM_PAYMENT_ADDRESS_EXCLUSIONS_DETERMINED);
        return this;
    }

}
