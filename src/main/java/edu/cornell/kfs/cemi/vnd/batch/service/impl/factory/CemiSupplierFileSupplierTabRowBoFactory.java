package edu.cornell.kfs.cemi.vnd.batch.service.impl.factory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.VendorAlias;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiSupplierConstants;
import edu.cornell.kfs.cemi.vnd.CemiSupplierConstants.TaxAuthorityFormTypes;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierAliasBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFileSupplierTabRowBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierTaxIdBo;
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
        final CemiSupplierTaxIdBo domesticTaxId = buildTaxId(false);
        final CemiSupplierTaxIdBo foreignTaxId = buildTaxId(true);

        final List<String> acceptedPaymentTypes = determineAcceptedPaymentTypes();

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
        supplierRowBo.setTaxIdType1(domesticTaxId.getTaxIdType());
        supplierRowBo.setTaxIdText1(domesticTaxId.getTaxIdText());
        supplierRowBo.setTransactionTaxId1(domesticTaxId.getTransactionTaxId());
        supplierRowBo.setDefaultWithholdingTaxCode1(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setPrimaryTaxId1(domesticTaxId.getPrimaryTaxId());
        supplierRowBo.setCountryTaxId1(domesticTaxId.getCountryTaxId());
        supplierRowBo.setTaxIdType2(foreignTaxId.getTaxIdType());
        supplierRowBo.setTaxIdText2(foreignTaxId.getTaxIdText());
        supplierRowBo.setTransactionTaxId2(foreignTaxId.getTransactionTaxId());
        supplierRowBo.setPrimaryTaxId2(foreignTaxId.getPrimaryTaxId());
        supplierRowBo.setCountryTaxId2(foreignTaxId.getCountryTaxId());
        supplierRowBo.setSupplierCategory(CemiSupplierConstants.DEFAULT_SUPPLIER_CATEGORY);
        supplierRowBo.setSupplierGroup1(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setSupplierGroup2(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setSupplierGroup3(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setSupplierGroup4(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setTaxDocumentDate(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setCertificateOfInsuranceDate(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setPurchaseOrderIssueOption(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setEmailAddressPurchaseOrder(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setChangeOrderIssueOption(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setMultiSupplierSupplierLinkForPoIssue(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setShippingTerms(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setShippingMethod(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setEnableAsn(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setDunsNumber(vendorDetail.getVendorDunsNumber());
        supplierRowBo.setPaymentTerms(determineVendorPaymentTerms());
        supplierRowBo.setTermsBasedOnInvoiceReceivedDate(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setDefaultPaymentType(acceptedPaymentTypes.get(0));
        supplierRowBo.setPaymentTypesAccepted1(acceptedPaymentTypes.get(0));
        supplierRowBo.setPaymentTypesAccepted2(acceptedPaymentTypes.get(1));
        supplierRowBo.setPaymentTypesAccepted3(acceptedPaymentTypes.get(2));
        supplierRowBo.setCurrency(CemiSupplierConstants.DEFAULT_CURRENCY);
        supplierRowBo.setAcceptedCurrencies1(CemiSupplierConstants.DEFAULT_CURRENCY);
        supplierRowBo.setAcceptedCurrencies2(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setAcceptedCurrencies3(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setAcceptedCurrencies4(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setAcceptedCurrencies5(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setAcceptedCurrencies6(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setAcceptedCurrencies7(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setAcceptedCurrencies8(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setAcceptedCurrencies9(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setAcceptedCurrencies10(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setAcceptedCurrencies11(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setAcceptedCurrencies12(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setAcceptedCurrencies13(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setAcceptedCurrencies14(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setAcceptedCurrencies15(CemiBaseConstants.EMPTY_STRING);
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
        supplierRowBo.setWebAddress(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setWebAddressId(CemiBaseConstants.EMPTY_STRING);

        return supplierRowBo;
    }

    private List<String> determineAcceptedPaymentTypes() {
        return determinePaymentTypes(vendorDetail.getDefaultPaymentMethodCode(), vendorHasActiveBankAccounts);
    }

    /*
     * KFSPTS-38411: The default payment type (derived from the vendor's KFS default payment method)
     * is always the first list entry, followed by the always-accepted payment types, without duplicates.
     */
    static List<String> determinePaymentTypes(final String defaultPaymentMethodCode,
            final boolean vendorHasActiveBankAccounts) {
        final String defaultPaymentType = determineDefaultPaymentType(
                defaultPaymentMethodCode, vendorHasActiveBankAccounts);
        final List<String> paymentTypes = new ArrayList<>();
        paymentTypes.add(defaultPaymentType);
        if (!StringUtils.equals(defaultPaymentType, CemiSupplierConstants.PAYMENT_TYPE_CHECK)) {
            paymentTypes.add(CemiSupplierConstants.PAYMENT_TYPE_CHECK);
        }
        if (!StringUtils.equals(defaultPaymentType, CemiSupplierConstants.PAYMENT_TYPE_OUTSOURCED_CHECK)) {
            paymentTypes.add(CemiSupplierConstants.PAYMENT_TYPE_OUTSOURCED_CHECK);
        }
        return CemiUtils.createListPaddedToMinimumSizeIfNecessary(
                CemiSupplierConstants.MAX_SUPPLIER_ACCEPTED_PAYMENT_TYPES,
                paymentTypes.toArray(String[]::new));
    }

    static String determineDefaultPaymentType(final String defaultPaymentMethodCode,
            final boolean vendorHasActiveBankAccounts) {
        if (StringUtils.equals(defaultPaymentMethodCode,
                KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_DRAFT)) {
            return CemiSupplierConstants.PAYMENT_TYPE_FX_PAYMENTS;
        } else if (StringUtils.equals(defaultPaymentMethodCode,
                KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_WIRE)) {
            return CemiSupplierConstants.PAYMENT_TYPE_WIRE_MANUAL;
        } else if (StringUtils.equals(defaultPaymentMethodCode,
                KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK) && vendorHasActiveBankAccounts) {
            return CemiSupplierConstants.PAYMENT_TYPE_ACH_MANUAL;
        } else {
            return CemiSupplierConstants.PAYMENT_TYPE_OUTSOURCED_CHECK;
        }
    }

    private CemiSupplierTaxIdBo buildTaxId(final boolean foreign) {
        return CemiSupplierTaxIdBoFactory.createTaxIdBoFrom(
                vendorDetail.getVendorHeader(), foreign, maskSensitiveData, isoFipsConversionService);
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
