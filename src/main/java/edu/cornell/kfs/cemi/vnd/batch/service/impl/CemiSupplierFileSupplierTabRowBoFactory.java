package edu.cornell.kfs.cemi.vnd.batch.service.impl;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorAlias;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;

import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiForeignTaxIdType;
import edu.cornell.kfs.cemi.vnd.CemiSupplierConstants;
import edu.cornell.kfs.cemi.vnd.CemiSupplierConstants.TaxAuthorityFormTypes;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFileSupplierTabRowBo;
import edu.cornell.kfs.sys.service.ISOFIPSConversionService;
import edu.cornell.kfs.vnd.CUVendorConstants.VendorOwnershipCodes;


public class CemiSupplierFileSupplierTabRowBoFactory {
    
    private VendorDetail vendor;
    private String jobRunDateString;
    private DateTimeService dateTimeService;
    protected final DecimalFormat supplierIdFormatter;
    private static ISOFIPSConversionService conversionService;
    private int vendorCount;
    private boolean maskSensitiveData = true;

    public CemiSupplierFileSupplierTabRowBoFactory(final VendorDetail vendor, final String jobRunDateString, 
            final DateTimeService dateTimeService, final int vendorCount, final boolean maskSensitiveData) {
        this.vendor = vendor;
        this.jobRunDateString = jobRunDateString;
        this.dateTimeService = dateTimeService;
        this.vendorCount = vendorCount;
        this.supplierIdFormatter = new DecimalFormat(CemiSupplierConstants.SUPPLIER_ID_FORMAT);
        this.maskSensitiveData = maskSensitiveData;
    }
    
    public CemiSupplierFileSupplierTabRowBo createCemiSupplierFileSupplierTabRowBo() {
        Validate.validState(vendor != null, "Vendor cannot be null.");
        Validate.validState(jobRunDateString != null, "jobRunDateString cannot be null.");
        Validate.validState(dateTimeService != null, "DateTimeService cannot be null.");
        
        final CemiSupplierFileSupplierTabRowBo supplierTabDataRow = new CemiSupplierFileSupplierTabRowBo();
        supplierTabDataRow.setSupplierId(supplierIdFormatter.format(vendorCount));;
        supplierTabDataRow.setSupplierReferenceId(buildSupplierReferenceId(vendor));
        supplierTabDataRow.setTaxAuthorityFormType(determineTaxAuthorityFormType(vendor));
        supplierTabDataRow.setTaxIdText(determineTaxIdText(vendor, maskSensitiveData));
        supplierTabDataRow.setTaxIdType(determineTaxIdType(vendor, supplierTabDataRow.getTaxIdText()));
        supplierTabDataRow.setTransactionTaxId(determineTransactionTaxId(vendor, supplierTabDataRow.getTaxIdText(), supplierTabDataRow.getTaxIdType()));
        supplierTabDataRow.setPrimaryTaxId(determinePrimaryTaxId(vendor, supplierTabDataRow.getTaxIdText()));
        supplierTabDataRow.setCountryTaxId(determineCountryTaxId(vendor, supplierTabDataRow.getTaxIdText()));
        supplierTabDataRow.setDunsNumber(vendor.getVendorDunsNumber());
        supplierTabDataRow.setPaymentTerms(determineVendorPaymentTerms(vendor));
        
        List<VendorAlias> vendorAliases = vendor.getVendorAliases().stream()
                .filter(VendorAlias::isActive)
                .collect(Collectors.toList());
        supplierTabDataRow.setAlternateNameBusinessEntity1(getAliasName(vendorAliases, 0));
        supplierTabDataRow.setAlternateNameUsageBusinessEntity1(getAliasUsage(vendorAliases, 0));
        supplierTabDataRow.setAlternateNameBusinessEntity2(getAliasName(vendorAliases, 1));
        supplierTabDataRow.setAlternateNameUsageBusinessEntity2(getAliasUsage(vendorAliases, 1));
        
        return supplierTabDataRow;
    }

    
    private String determineCountryTaxId(VendorDetail vendorDetail, String taxIdValue) {
        VendorHeader vendorHeader = vendorDetail.getVendorHeader();
        if (StringUtils.isNotBlank(taxIdValue) && StringUtils.isNotBlank(vendorHeader.getVendorCorpCitizenCode())) {
            return getConversionService().convertFIPSCountryCodeToActiveISOCountryCode(vendorHeader.getVendorCorpCitizenCode());
        } else {
            return KFSConstants.EMPTY_STRING;
        }
    }

    private static String determineTaxIdText(VendorDetail vendorDetail, boolean maskCemiSensitiveData) {
        if (!maskCemiSensitiveData) {
            VendorHeader vendorHeader = vendorDetail.getVendorHeader();
            if (vendorHeader.getVendorForeignIndicator()) {
                return vendorHeader.getVendorForeignTaxId();
            } else {
                return vendorHeader.getVendorTaxNumber();
            }
        }
        return CemiSupplierConstants.DUMMY_TAX_ID;
    }

    private static String getAliasName(final List<VendorAlias> aliases, final int index) {
        return index < aliases.size() ? aliases.get(index).getVendorAliasName() : "";
    }

    private static String getAliasUsage(final List<VendorAlias> aliases, final int index) {
        return index < aliases.size() ? CemiSupplierConstants.ALTERNATE_NAME_USAGE_DEFAULT_VALUE : "";
    }

    // default to true if tax id is present, FALSE if tax type USA_SSN
    private static String determineTransactionTaxId(VendorDetail vendorDetail, String taxIdValue, String taxIdType) {
        if (StringUtils.isNotBlank(taxIdValue)) {
            boolean transactionTaxId = StringUtils.isNotBlank(taxIdType)
                    && !CemiSupplierConstants.USA_SSN_TAX_TYPE.equalsIgnoreCase(taxIdType);
            return CemiUtils.convertToBooleanValueForFileExtract(transactionTaxId);
        } else {
            return KFSConstants.EMPTY_STRING;
        }

    }
    
    private static String determinePrimaryTaxId(VendorDetail vendorDetail, String taxIdValue) {
        if (StringUtils.isNotBlank(taxIdValue)) {
            return CemiUtils.convertToBooleanValueForFileExtract(true);
        } else {
            return KFSConstants.EMPTY_STRING;
        }
    }


    private static String buildSupplierReferenceId(final VendorDetail vendor) {
        return MessageFormat.format(CemiSupplierConstants.SUPPLIER_REFERENCE_ID_FORMAT,
                Integer.toString(vendor.getVendorHeaderGeneratedIdentifier()),
                Integer.toString(vendor.getVendorDetailAssignedIdentifier()));
    }

    private static String determineTaxAuthorityFormType(final VendorDetail vendor) {
        if (StringUtils.isNotBlank(vendor.getVendorHeader().getVendorW8TypeCode())) {
            return TaxAuthorityFormTypes.FORM_1042S;
        } else if (StringUtils.equals(vendor.getVendorHeader().getVendorOwnershipCode(),
                VendorOwnershipCodes.INDIVIDUAL_OR_SOLE_PROPRIETOR_OR_SMLLC)) {
            return TaxAuthorityFormTypes.FORM_1099_MISC;
        } else {
            return KFSConstants.EMPTY_STRING;
        }
    }

    private static String determineTaxIdType(final VendorDetail vendor, String taxIdValue) {
        if (StringUtils.isBlank(taxIdValue)) {
            return KFSConstants.EMPTY_STRING;
        } else {
            if( vendor.getVendorHeader().getVendorForeignIndicator()) {
                String fipsCountry = vendor.getVendorHeader().getVendorCorpCitizenCode();
                if (StringUtils.isNotBlank(fipsCountry)) {
                    final String isoCountryCode = getConversionService().convertFIPSCountryCodeToActiveISOCountryCode(vendor.getVendorHeader().getVendorCorpCitizenCode());
                    final String foreignTaxType = CemiForeignTaxIdType.fromIsoCode(isoCountryCode)
                            .map(CemiForeignTaxIdType::getTaxIdType)
                            .orElse(KFSConstants.EMPTY_STRING);
                    return foreignTaxType;
                } else {
                    return KFSConstants.EMPTY_STRING;
                }
            }
            final String kfsTaxType = StringUtils.defaultString(vendor.getVendorHeader().getVendorTaxTypeCode());
            return CemiSupplierConstants.TAX_ID_TYPES.get(kfsTaxType);
        }
    }
    
    private static String determineVendorPaymentTerms(VendorDetail vendorDetail) {
        vendorDetail.refreshReferenceObject("vendorPaymentTerms");
        if (ObjectUtils.isNotNull(vendorDetail.getVendorPaymentTerms())) {
            String paymentTermsDescription = vendorDetail.getVendorPaymentTerms().getVendorPaymentTermsDescription();
            if (paymentTermsDescription == null || paymentTermsDescription.isEmpty()) {
                return paymentTermsDescription;
            }
            return paymentTermsDescription
                    .trim()
                    .replaceAll("[^a-zA-Z0-9]+", "_") // replace spans of special chars/spaces with _
                    .replaceAll("^_|_$", ""); // strip leading/trailing underscores
        }
        return "";
    }

    
    public static ISOFIPSConversionService getConversionService(){
        if (conversionService == null) {
            conversionService = SpringContext.getBean(ISOFIPSConversionService.class);
        }
        return conversionService;
    }
}
