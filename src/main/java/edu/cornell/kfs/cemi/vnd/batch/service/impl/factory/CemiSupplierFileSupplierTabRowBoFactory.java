package edu.cornell.kfs.cemi.vnd.batch.service.impl.factory;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.VendorAlias;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiForeignTaxIdType;
import edu.cornell.kfs.cemi.vnd.CemiSupplierConstants;
import edu.cornell.kfs.cemi.vnd.CemiSupplierConstants.TaxAuthorityFormTypes;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierAliasBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFileSupplierTabRowBo;
import edu.cornell.kfs.sys.service.ISOFIPSConversionService;
import edu.cornell.kfs.vnd.CUVendorConstants.VendorOwnershipCodes;

@SuppressWarnings("deprecation")
public class CemiSupplierFileSupplierTabRowBoFactory {

    private static final Logger LOG = LogManager.getLogger();

    private VendorDetail vendorDetail;
    private String supplierId;
    private ISOFIPSConversionService isoFipsConversionService;
    private boolean maskSensitiveData;
    private boolean vendorHasActiveBankAccounts;

    public CemiSupplierFileSupplierTabRowBoFactory(final VendorDetail vendorDetail, final String supplierId,
            final ISOFIPSConversionService isoFipsConversionService, final boolean maskSensitiveData, 
            final boolean vendorHasActiveBankAccounts) {
        Validate.notNull(vendorDetail, "vendorDetail cannot be null");
        Validate.notBlank(supplierId, "supplierId cannot be blank");
        Validate.notNull(isoFipsConversionService, "isoFipsConversionService cannot be null");
        this.vendorDetail = vendorDetail;
        this.supplierId = supplierId;
        this.isoFipsConversionService = isoFipsConversionService;
        this.maskSensitiveData = maskSensitiveData;
        this.vendorHasActiveBankAccounts = vendorHasActiveBankAccounts;
    }

    public static CemiSupplierFileSupplierTabRowBo createTabRowBoFrom(final VendorDetail vendorDetail,
            final String supplierId, final ISOFIPSConversionService isoFipsConversionService,
            final boolean maskSensitiveData, final boolean vendorHasActiveBankAccounts) {
        final CemiSupplierFileSupplierTabRowBoFactory factory = new CemiSupplierFileSupplierTabRowBoFactory(
                vendorDetail, supplierId, isoFipsConversionService, maskSensitiveData, vendorHasActiveBankAccounts);
        return factory.createCemiSupplierFileSupplierTabRowBo();
    }

    public CemiSupplierFileSupplierTabRowBo createCemiSupplierFileSupplierTabRowBo() {
        final CemiSupplierFileSupplierTabRowBo supplierRowBo = new CemiSupplierFileSupplierTabRowBo();

        final String taxAuthorityFormType = determineTaxAuthorityFormType();
        final String taxIdText = determineTaxIdText();
        final String taxIdType = determineTaxIdType(taxIdText);

        final List<CemiSupplierAliasBo> aliases = determineSupplierAliases();
        final CemiSupplierAliasBo alias1 = aliases.get(0);
        final CemiSupplierAliasBo alias2 = aliases.get(1);

        supplierRowBo.setVendorHeaderGeneratedIdentifier(vendorDetail.getVendorHeaderGeneratedIdentifier());
        supplierRowBo.setVendorDetailAssignedIdentifier(vendorDetail.getVendorDetailAssignedIdentifier());

        supplierRowBo.setSupplierId(supplierId);
        supplierRowBo.setSupplierReferenceId(determineSupplierReferenceId());
        supplierRowBo.setSupplierName(vendorDetail.getVendorName());
        supplierRowBo.setTaxAuthorityFormType(taxAuthorityFormType);
        supplierRowBo.setIrs1099Supplier(determineIrs1099SupplierFlag(taxAuthorityFormType));
        supplierRowBo.setReport1099WithParent(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setTaxIdType(taxIdType);
        supplierRowBo.setTaxIdText(taxIdText);
        supplierRowBo.setTransactionTaxId(determineTransactionTaxId(taxIdText, taxIdType));
        supplierRowBo.setDefaultWithholdingTaxCode(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setPrimaryTaxId(determinePrimaryTaxId(taxIdText));
        supplierRowBo.setCountryTaxId(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setSupplierCategory(CemiSupplierConstants.DEFAULT_SUPPLIER_CATEGORY);
        supplierRowBo.setSupplierGroup1(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setSupplierGroup2(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setSupplierGroup3(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setSupplierGroup4(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setCustomerAccountNumber(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setDunsNumber(vendorDetail.getVendorDunsNumber());
        supplierRowBo.setPaymentTerms(determineVendorPaymentTerms());
        if (vendorHasActiveBankAccounts) {
            supplierRowBo.setDefaultPaymentType(CemiSupplierConstants.PAYMENT_TYPE_EFT);
            supplierRowBo.setPaymentTypesAccepted1(CemiSupplierConstants.PAYMENT_TYPE_EFT);
            supplierRowBo.setPaymentTypesAccepted2(CemiSupplierConstants.PAYMENT_TYPE_OUTSOURCED_CHECK);
        } else {
            supplierRowBo.setDefaultPaymentType(CemiSupplierConstants.PAYMENT_TYPE_OUTSOURCED_CHECK);
            supplierRowBo.setPaymentTypesAccepted1(CemiSupplierConstants.PAYMENT_TYPE_OUTSOURCED_CHECK);
            supplierRowBo.setPaymentTypesAccepted2(CemiBaseConstants.EMPTY_STRING);
        }
        supplierRowBo.setPaymentTypesAccepted3(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setCurrency(CemiSupplierConstants.DEFAULT_CURRENCY);
        supplierRowBo.setAcceptedCurrencies(CemiSupplierConstants.DEFAULT_CURRENCY);
        supplierRowBo.setProcurementCreditCard(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setAlwaysSeparatePayments(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setTextForDefaultSupplierPaymentMemo(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setUseSupplierReferenceAsDefaultSupplierPaymentMemo(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setUseInvoiceMemoAsDefaultSupplierPaymentMemo(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setUseSupplierConnectionMemo(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setDoNotReplaceAll(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setSupplierClassification1(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setSupplierClassificationField1(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setFieldDateValue1(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setFieldNumberValue1(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setFieldTextValue1(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setFieldSingleSelectChoice1(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setFieldMultiSelectChoice1(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setSupplierClassification2(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setSupplierClassificationField2(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setFieldDateValue2(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setFieldNumberValue2(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setFieldTextValue2(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setFieldSingleSelectChoice2(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setFieldMultiSelectChoice2(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setAlternateNameBusinessEntity1(alias1.getAliasName());
        supplierRowBo.setAlternateNameUsageBusinessEntity1(alias1.getAliasUsage());
        supplierRowBo.setAlternateNameBusinessEntity2(alias2.getAliasName());
        supplierRowBo.setAlternateNameUsageBusinessEntity2(alias2.getAliasUsage());

        return supplierRowBo;
    }

    private String determineTaxIdText() {
        final String unmaskedTaxId = determineUnmaskedTaxIdText();
        if (StringUtils.isBlank(unmaskedTaxId)) {
            return CemiBaseConstants.EMPTY_STRING;
        } else {
            return maskSensitiveData ? CemiSupplierConstants.DUMMY_TAX_ID : unmaskedTaxId;
        }
    }

    private String determineUnmaskedTaxIdText() {
        VendorHeader vendorHeader = vendorDetail.getVendorHeader();
        if (vendorHeader.getVendorForeignIndicator()) {
            return vendorHeader.getVendorForeignTaxId();
        } else {
            return vendorHeader.getVendorTaxNumber();
        }
    }

    // default to true if tax id is present, FALSE if tax type USA_SSN
    private String determineTransactionTaxId(final String taxIdText, final String taxIdType) {
        if (StringUtils.isNotBlank(taxIdText)) {
            boolean transactionTaxId = StringUtils.isNotBlank(taxIdType)
                    && !CemiSupplierConstants.USA_SSN_TAX_TYPE.equalsIgnoreCase(taxIdType);
            return CemiUtils.convertToBooleanValueForFileExtract(transactionTaxId);
        } else {
            return KFSConstants.EMPTY_STRING;
        }

    }

    private String determinePrimaryTaxId(String taxIdText) {
        if (StringUtils.isNotBlank(taxIdText)) {
            return CemiUtils.convertToBooleanValueForFileExtract(true);
        } else {
            return KFSConstants.EMPTY_STRING;
        }
    }

    private String determineSupplierReferenceId() {
        return MessageFormat.format(CemiSupplierConstants.SUPPLIER_REFERENCE_ID_FORMAT,
                Integer.toString(vendorDetail.getVendorHeaderGeneratedIdentifier()),
                Integer.toString(vendorDetail.getVendorDetailAssignedIdentifier()));
    }

    private String determineTaxAuthorityFormType() {
        if (StringUtils.isNotBlank(vendorDetail.getVendorHeader().getVendorW8TypeCode())) {
            return TaxAuthorityFormTypes.FORM_1042S;
        } else if (StringUtils.equals(vendorDetail.getVendorHeader().getVendorOwnershipCode(),
                VendorOwnershipCodes.INDIVIDUAL_OR_SOLE_PROPRIETOR_OR_SMLLC)) {
            return TaxAuthorityFormTypes.FORM_1099_MISC;
        } else {
            return KFSConstants.EMPTY_STRING;
        }
    }

    private String determineTaxIdType(String taxIdText) {
        if (StringUtils.isBlank(taxIdText)) {
            return CemiBaseConstants.EMPTY_STRING;
        } else {
            if (vendorDetail.getVendorHeader().getVendorForeignIndicator()) {
                String fipsCountry = vendorDetail.getVendorHeader().getVendorCorpCitizenCode();
                if (StringUtils.isNotBlank(fipsCountry)) {
                    final String isoCountryCode = isoFipsConversionService.convertFIPSCountryCodeToActiveISOCountryCode(
                            vendorDetail.getVendorHeader().getVendorCorpCitizenCode());
                    final String foreignTaxType = CemiForeignTaxIdType.fromIsoCode(isoCountryCode)
                            .map(CemiForeignTaxIdType::getTaxIdType)
                            .orElse(CemiBaseConstants.EMPTY_STRING);
                    return foreignTaxType;
                } else {
                    return CemiBaseConstants.EMPTY_STRING;
                }
            }
            final String kfsTaxType = StringUtils.defaultString(vendorDetail.getVendorHeader().getVendorTaxTypeCode());
            return CemiSupplierConstants.TAX_ID_TYPES.get(kfsTaxType);
        }
    }

    private String determineVendorPaymentTerms() {
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

    private String determineIrs1099SupplierFlag(final String taxAuthorityFormType) {
        return CemiUtils.convertToBooleanValueForFileExtract(
                StringUtils.equals(taxAuthorityFormType, TaxAuthorityFormTypes.FORM_1099_MISC));
    }

    private List<CemiSupplierAliasBo> determineSupplierAliases() {
        final CemiSupplierAliasBo emptyAlias = CemiSupplierAliasBoFactory.createAliasBoFrom(
                CemiBaseConstants.EMPTY_STRING, CemiBaseConstants.EMPTY_STRING);
        final CemiSupplierAliasBo[] convertedAliases = vendorDetail.getVendorAliases().stream()
                .filter(VendorAlias::isActive)
                .map(this::createCemiSupplierAliasBoFromVendorAlias)
                .toArray(CemiSupplierAliasBo[]::new);

        if (convertedAliases.length > CemiSupplierConstants.MAX_SUPPLIER_ALIASES) {
            LOG.warn("determineSupplierAliases, Found a total of {} active aliases for Vendor {}-{}; only the first {} "
                    + "will be used in the output",
                    convertedAliases.length, vendorDetail.getVendorHeaderGeneratedIdentifier(),
                    vendorDetail.getVendorDetailAssignedIdentifier(), CemiSupplierConstants.MAX_SUPPLIER_ALIASES);
        }

        return CemiUtils.createListPaddedToMinimumSizeIfNecessary(
                emptyAlias, CemiSupplierConstants.MAX_SUPPLIER_ALIASES, convertedAliases);
    }

    private CemiSupplierAliasBo createCemiSupplierAliasBoFromVendorAlias(final VendorAlias vendorAlias) {
        return CemiSupplierAliasBoFactory.createAliasBoFrom(
                vendorAlias.getVendorAliasName(), CemiSupplierConstants.ALTERNATE_NAME_USAGE_DEFAULT_VALUE);
    }

}
