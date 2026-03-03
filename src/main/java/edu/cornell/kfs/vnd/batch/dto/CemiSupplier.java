package edu.cornell.kfs.vnd.batch.dto;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.VendorAlias;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;

import edu.cornell.kfs.sys.util.CemiUtils;
import edu.cornell.kfs.vnd.CUVendorConstants.VendorOwnershipCodes;
import edu.cornell.kfs.vnd.CemiVendorConstants;
import edu.cornell.kfs.vnd.CemiVendorConstants.TaxAuthorityFormTypes;

@SuppressWarnings("deprecation")
public class CemiSupplier {

    private final VendorDetail vendorDetail;
    private final String supplierId;
    private final String supplierReferenceId;
    private final String taxAuthorityFormType;
    private final String taxIdType;
    private final String taxIdValue;
    private final String transactionTaxId;
    private final String primaryTaxId;
    private final String countryTaxID;
    private final String dunsNumber;
    private final String paymentTerms;
    private final String alias0Name;
    private final String alias0Usage;
    private final String alias1Name;
    private final String alias1Usage;

    /*
     * For POJO properties that need to go into the spreadsheet (and the future temp table),
     * either create a related getter, or retrieve it from a Vendor Header/Detail getter.
     * When using the latter, specify a nested "vendorHeader.propName" or "vendorDetail.propName"
     * property in the XML definition.
     * 
     * This particular POJO populates derived values via static methods and keeps such values immutable.
     * If necessary, this object can be modified into a mutable one and/or a different mechanism could
     * be implemented to compute the derived values.
     */
    public CemiSupplier(final VendorDetail vendorDetail, final String supplierId) {
        this.vendorDetail = vendorDetail;
        this.supplierId = supplierId;
        this.supplierReferenceId = buildSupplierReferenceId(vendorDetail);
        this.taxAuthorityFormType = determineTaxAuthorityFormType(vendorDetail);
        this.taxIdType = determineTaxIdType(vendorDetail);
        this.taxIdValue = determineTaxIdValue(vendorDetail);
        this.transactionTaxId = determineTransactionTaxId(vendorDetail);
        this.primaryTaxId = determinePrimaryTaxId(vendorDetail);
        this.countryTaxID = vendorDetail.getVendorHeader().getVendorCorpCitizenCode();
        this.dunsNumber = vendorDetail.getVendorDunsNumber();
        this.paymentTerms = "";
        
        List<VendorAlias> vendorAliases = vendorDetail.getVendorAliases();
        this.alias0Name  = getAliasName(vendorAliases, 0);
        this.alias0Usage = getAliasUsage(vendorAliases, 0);
        this.alias1Name  = getAliasName(vendorAliases, 1);
        this.alias1Usage = getAliasUsage(vendorAliases, 1);
    }

    private String determineTaxIdValue(VendorDetail vendorDetail) {
        boolean cemiEnv = false; //TODO: determine if job is running in CEMI environment
        boolean maskCEMISesitiveValues = true; // TODO: determine if masking sensitive values for CEMI data conversion is on or off
        
        if(cemiEnv && !maskCEMISesitiveValues) {
            //return tax id value from vendor
        }
        return CemiVendorConstants.DUMMY_TAX_ID;
    }

    private static String getAliasName(final List<VendorAlias> aliases, final int index) {
        return index < aliases.size() ? aliases.get(index).getVendorAliasName() : "";
    }

    private static String getAliasUsage(final List<VendorAlias> aliases, final int index) {
        return index < aliases.size() ? CemiVendorConstants.ALTERNATE_NAME_USAGE_DEFAULT_VALUE : "";
    }

    private String determineTransactionTaxId(VendorDetail vendorDetail) {
        boolean primaryTaxId = StringUtils.isNotBlank(taxIdValue); // default to true if tax id is present
        return CemiUtils.convertToBooleanValueForFileExtract(primaryTaxId);
    }
    
    private String determinePrimaryTaxId(VendorDetail vendorDetail) {
        boolean transactionTaxId = StringUtils.isNotBlank(taxIdValue); // default to true if tax id is present
        transactionTaxId = !CemiVendorConstants.USA_SSN_TAX_TYPE.equalsIgnoreCase(taxIdType); // FALSE if tax type USA_SSN
        return CemiUtils.convertToBooleanValueForFileExtract(transactionTaxId);
    }


    private static String buildSupplierReferenceId(final VendorDetail vendor) {
        return MessageFormat.format(CemiVendorConstants.SUPPLIER_REFERENCE_ID_FORMAT,
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

    private static String determineTaxIdType(final VendorDetail vendor) {
        final String kfsTaxType = StringUtils.defaultString(vendor.getVendorHeader().getVendorTaxTypeCode());
        return CemiVendorConstants.TAX_ID_TYPES.get(kfsTaxType);
    }

    public VendorDetail getVendorDetail() {
        return vendorDetail;
    }

    public VendorHeader getVendorHeader() {
        return vendorDetail.getVendorHeader();
    }

    public String getSupplierId() {
        return supplierId;
    }

    public String getSupplierReferenceId() {
        return supplierReferenceId;
    }

    public String getTaxAuthorityFormType() {
        return taxAuthorityFormType;
    }

    public String getIrs1099SupplierFlag() {
        return CemiUtils.convertToBooleanValueForFileExtract(
                StringUtils.equals(taxAuthorityFormType, TaxAuthorityFormTypes.FORM_1099_MISC));
    }

    public String getTaxIdType() {
        return taxIdType;
    }

    public String getTaxIdValue() {
        return taxIdValue;
    }

    public String getTransactionTaxId() {
        return CemiUtils.convertToBooleanValueForFileExtract(
                StringUtils.isNoneBlank(taxIdType, taxIdValue)
                        && !StringUtils.equals(taxIdType, CemiVendorConstants.USA_SSN_TAX_TYPE));
    }

    public String getPrimaryTaxId() {
        return primaryTaxId;
    }

    public String getCountryTaxID() {
        return countryTaxID;
    }

    public String getDunsNumber() {
        return dunsNumber;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public String getAlias0Name() {
        return alias0Name;
    }

    public String getAlias0Usage() {
        return alias0Usage;
    }

    public String getAlias1Name() {
        return alias1Name;
    }

    public String getAlias1Usage() {
        return alias1Usage;
    }

}
