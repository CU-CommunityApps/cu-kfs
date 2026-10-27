package edu.cornell.kfs.cemi.vnd.batch.service.impl.factory;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorAlias;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.kuali.kfs.vnd.businessobject.VendorSupplierDiversity;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiSupplierConstants;
import edu.cornell.kfs.cemi.vnd.CemiSupplierConstants.PurchaseOrderIssueOptions;
import edu.cornell.kfs.cemi.vnd.CemiSupplierConstants.TaxAuthorityFormTypes;
import edu.cornell.kfs.cemi.vnd.batch.CemiSupplierAlwaysSeparatePayments;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierAliasBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierClassificationBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierClassificationMappingBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFileSupplierTabRowBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierTaxIdBo;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiSupplierDao;
import edu.cornell.kfs.cemi.vnd.util.CemiVendorUtils;
import edu.cornell.kfs.module.purap.CUPurapConstants.POTransmissionMethods;
import edu.cornell.kfs.sys.service.ISOFIPSConversionService;
import edu.cornell.kfs.vnd.CUVendorConstants.VendorOwnershipCodes;
import edu.cornell.kfs.vnd.businessobject.CuVendorAddressExtension;

@SuppressWarnings("deprecation")
public class CemiSupplierFileSupplierTabRowBoFactory {

    private static final Logger LOG = LogManager.getLogger();

    private VendorDetail vendorDetail;
    private String supplierId;
    private ISOFIPSConversionService isoFipsConversionService;
    private CemiSupplierDao cemiSupplierDao;
    private boolean maskSensitiveData;
    private boolean vendorHasActiveBankAccounts;
    private Map<String, String> paymentTermsMappings;
    private List<CemiSupplierClassificationMappingBo> supplierClassificationMappings;

    public CemiSupplierFileSupplierTabRowBoFactory(final VendorDetail vendorDetail, final String supplierId,
            final ISOFIPSConversionService isoFipsConversionService, final CemiSupplierDao cemiSupplierDao,
            final boolean maskSensitiveData,
            final boolean vendorHasActiveBankAccounts, final Map<String, String> paymentTermsMappings,
            final List<CemiSupplierClassificationMappingBo> supplierClassificationMappings) {
        Validate.notNull(vendorDetail, "vendorDetail cannot be null");
        Validate.notBlank(supplierId, "supplierId cannot be blank");
        Validate.notNull(isoFipsConversionService, "isoFipsConversionService cannot be null");
        Validate.notNull(cemiSupplierDao, "cemiSupplierDao cannot be null");
        Validate.notNull(paymentTermsMappings, "paymentTermsMappings cannot be null");
        Validate.notNull(supplierClassificationMappings, "supplierClassificationMappings cannot be null");
        this.vendorDetail = vendorDetail;
        this.supplierId = supplierId;
        this.isoFipsConversionService = isoFipsConversionService;
        this.cemiSupplierDao = cemiSupplierDao;
        this.maskSensitiveData = maskSensitiveData;
        this.vendorHasActiveBankAccounts = vendorHasActiveBankAccounts;
        this.paymentTermsMappings = paymentTermsMappings;
        this.supplierClassificationMappings = supplierClassificationMappings;
    }

    public static CemiSupplierFileSupplierTabRowBo createTabRowBoFrom(final VendorDetail vendorDetail,
            final String supplierId, final ISOFIPSConversionService isoFipsConversionService,
            final CemiSupplierDao cemiSupplierDao,
            final boolean maskSensitiveData, final boolean vendorHasActiveBankAccounts,
            final Map<String, String> paymentTermsMappings,
            final List<CemiSupplierClassificationMappingBo> supplierClassificationMappings) {
        final CemiSupplierFileSupplierTabRowBoFactory factory = new CemiSupplierFileSupplierTabRowBoFactory(
                vendorDetail, supplierId, isoFipsConversionService, cemiSupplierDao, maskSensitiveData,
                vendorHasActiveBankAccounts, paymentTermsMappings, supplierClassificationMappings);
        return factory.createCemiSupplierFileSupplierTabRowBo();
    }

    public CemiSupplierFileSupplierTabRowBo createCemiSupplierFileSupplierTabRowBo() {
        final CemiSupplierFileSupplierTabRowBo supplierRowBo = new CemiSupplierFileSupplierTabRowBo();

        final String taxAuthorityFormType = determineTaxAuthorityFormType();
        final CemiSupplierTaxIdBo domesticTaxId = buildTaxId(false);
        final CemiSupplierTaxIdBo foreignTaxId = buildTaxId(true);

        final List<String> acceptedPaymentTypes = determineAcceptedPaymentTypes();

        final boolean isB2BVendor = determineIfVendorIsB2BVendor();
        final Optional<VendorAddress> primaryPOAddress = getPrimaryPOAddressIfPresentAndApplicable(isB2BVendor);
        final String purchaseOrderIssueOption = determinePurchaseOrderIssueOption(isB2BVendor, primaryPOAddress);

        final List<CemiSupplierClassificationBo> supplierClassifications = determineSupplierClassifications();
        final CemiSupplierClassificationBo classification1 = supplierClassifications.get(0);
        final CemiSupplierClassificationBo classification2 = supplierClassifications.get(1);

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
        supplierRowBo.setPurchaseOrderIssueOption(purchaseOrderIssueOption);
        supplierRowBo.setEmailAddressPurchaseOrder(
                determineEmailAddressPurchaseOrder(purchaseOrderIssueOption, primaryPOAddress));
        supplierRowBo.setChangeOrderIssueOption(PurchaseOrderIssueOptions.PRINT);
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
        supplierRowBo.setAlwaysSeparatePayments(determineAlwaysSeparatePayments());
        supplierRowBo.setTextForDefaultSupplierPaymentMemo(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setUseSupplierReferenceAsDefaultSupplierPaymentMemo(CemiBaseConstants.CLOVERDX_TRUE);
        supplierRowBo.setUseInvoiceMemoAsDefaultSupplierPaymentMemo(CemiBaseConstants.CLOVERDX_FALSE);
        supplierRowBo.setUseSupplierConnectionMemo(CemiBaseConstants.CLOVERDX_FALSE);
        supplierRowBo.setDoNotReplaceAll(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setSupplierClassification1(classification1.getWorkdayRefId());
        supplierRowBo.setSupplierClassificationField1(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setFieldDateValue1(classification1.getExpirationDate());
        supplierRowBo.setFieldNumberValue1(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setFieldTextValue1(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setFieldSingleSelectChoice1(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setFieldMultiSelectChoice1(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setSupplierClassification2(classification2.getWorkdayRefId());
        supplierRowBo.setSupplierClassificationField2(CemiBaseConstants.EMPTY_STRING);
        supplierRowBo.setFieldDateValue2(classification2.getExpirationDate());
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
        return CemiVendorUtils.determinePaymentTypes(
                vendorDetail.getDefaultPaymentMethodCode(), vendorHasActiveBankAccounts);
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
        final String paymentTermsCode = vendorDetail.getVendorPaymentTermsCode();
        if (StringUtils.isBlank(paymentTermsCode)) {
            return CemiBaseConstants.EMPTY_STRING;
        }
        // KFSPTS-38411: translate the KFS payment terms code to the Workday payment terms code.
        return paymentTermsMappings.getOrDefault(paymentTermsCode, CemiSupplierConstants.PAYMENT_TERMS_UNKNOWN);
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

    private boolean determineIfVendorIsB2BVendor() {
        return cemiSupplierDao.isB2BVendor(vendorDetail.getVendorHeaderGeneratedIdentifier(),
                vendorDetail.getVendorDetailAssignedIdentifier());
    }

    private Optional<VendorAddress> getPrimaryPOAddressIfPresentAndApplicable(final boolean isB2BVendor) {
        final VendorHeader vendorHeader = vendorDetail.getVendorHeader();
        if (isB2BVendor || !CemiVendorUtils.isPurchaseOrderVendor(vendorHeader.getVendorTypeCode())) {
            return Optional.empty();
        }

        final List<VendorAddress> addresses = vendorDetail.getVendorAddresses();
        if (CollectionUtils.isEmpty(addresses)) {
            return Optional.empty();
        }

        return addresses.stream()
                .filter(address -> CemiVendorUtils.isPrimaryVendorAddress(
                        VendorConstants.VendorTypes.PURCHASE_ORDER, address))
                .findFirst();
    }

    private String determinePurchaseOrderIssueOption(
            final boolean isB2BVendor, final Optional<VendorAddress> primaryPOAddress) {
        if (isB2BVendor) {
            return PurchaseOrderIssueOptions.XML_AUTO;
        } else if (primaryPOAddress.isPresent()) {
            final String purchaseOrderTransmissionMethod = primaryPOAddress
                    .map(address -> (CuVendorAddressExtension) address.getExtension())
                    .map(CuVendorAddressExtension::getPurchaseOrderTransmissionMethodCode)
                    .orElse(CemiBaseConstants.EMPTY_STRING);
            if (StringUtils.equals(purchaseOrderTransmissionMethod, POTransmissionMethods.EMAIL)) {
                return PurchaseOrderIssueOptions.EMAIL;
            }
        }
        return PurchaseOrderIssueOptions.PRINT;
    }

    private String determineEmailAddressPurchaseOrder(
            final String purchaseOrderIssueOption, final Optional<VendorAddress> primaryPOAddress) {
        if (!StringUtils.equals(purchaseOrderIssueOption, PurchaseOrderIssueOptions.EMAIL)) {
            return CemiBaseConstants.EMPTY_STRING;
        }

        final String emailAddress = primaryPOAddress.map(VendorAddress::getVendorAddressEmailAddress)
                .filter(StringUtils::isNotBlank)
                .orElse(CemiBaseConstants.EMPTY_STRING);
        if (StringUtils.isBlank(emailAddress)) {
            LOG.warn("determineEmailAddressPurchaseOrder, Vendor {}-{} is configured for PO transmission by email, "
                    + "yet its primary PO address does not define an email address. Data cleanup will be needed.",
                    vendorDetail.getVendorHeaderGeneratedIdentifier(), vendorDetail.getVendorDetailAssignedIdentifier());
        }

        return emailAddress;
    }

    private String determineAlwaysSeparatePayments() {
        final boolean alwaysSeparatePayments = CemiSupplierAlwaysSeparatePayments.shouldAlwaysSeparatePaymentsForVendor(
                vendorDetail.getVendorHeaderGeneratedIdentifier(), vendorDetail.getVendorDetailAssignedIdentifier());
        return CemiUtils.convertToBooleanValueForFileExtract(alwaysSeparatePayments);
    }

    // NOTE: It is assumed that supplierClassificationMappings is ordered such that multi-diversity entries come first.
    private List<CemiSupplierClassificationBo> determineSupplierClassifications() {
        final CemiSupplierClassificationBo emptyClassification = CemiSupplierClassificationBoFactory
                .createClassificationBoFrom(CemiBaseConstants.EMPTY_STRING, CemiBaseConstants.EMPTY_STRING);
        final Map<String, VendorSupplierDiversity> supplierDiversities = getLegacySupplierDiversitiesAsMap();
        if (supplierDiversities.isEmpty()) {
            return CemiUtils.createListOfEmptyValues(
                    CemiSupplierConstants.MAX_SUPPLIER_CLASSIFICATIONS, emptyClassification);
        }

        final Set<String> remainingDiversities = new HashSet<>(supplierDiversities.keySet());
        final Stream.Builder<CemiSupplierClassificationBo> classifications = Stream.builder();
        final Set<String> diversitiesToFilter = new HashSet<>();

        for (final CemiSupplierClassificationMappingBo classificationMapping : supplierClassificationMappings) {
            if (isPastThePointOfProcessingMultiDiversityClassifications(classificationMapping)) {
                remainingDiversities.removeAll(diversitiesToFilter);
            }
            if (remainingDiversities.containsAll(classificationMapping.getKfsSupplierDiversityCodes())) {
                final CemiSupplierClassificationBo classificationBo = createCemiSupplierClassificationBo(
                        classificationMapping, supplierDiversities);
                classifications.add(classificationBo);
                diversitiesToFilter.addAll(classificationMapping.getKfsSupplierDiversityCodes());
            }
        }

        final CemiSupplierClassificationBo[] classificationsArray = classifications.build()
                .toArray(CemiSupplierClassificationBo[]::new);
        if (classificationsArray.length > CemiSupplierConstants.MAX_SUPPLIER_CLASSIFICATIONS) {
            LOG.warn("determineSupplierClassifications, Found/Derived a total of {} unique classifications "
                    + "for Vendor {}-{}; only the first {} will be used in the output",
                    classificationsArray.length, vendorDetail.getVendorHeaderGeneratedIdentifier(),
                    vendorDetail.getVendorDetailAssignedIdentifier(), CemiSupplierConstants.MAX_SUPPLIER_CLASSIFICATIONS);
        }

        return CemiUtils.createListPaddedToMinimumSizeIfNecessary(
                emptyClassification, CemiSupplierConstants.MAX_SUPPLIER_CLASSIFICATIONS, classificationsArray);
    }

    private Map<String, VendorSupplierDiversity> getLegacySupplierDiversitiesAsMap() {
        final List<VendorSupplierDiversity> supplierDiversities = vendorDetail.getVendorHeader()
                .getActiveVendorSupplierDiversities();
        if (CollectionUtils.isEmpty(supplierDiversities)) {
            return Map.of();
        }
        return supplierDiversities.stream().collect(
                Collectors.toUnmodifiableMap(
                        VendorSupplierDiversity::getVendorSupplierDiversityCode, Function.identity()));
    }

    private boolean isPastThePointOfProcessingMultiDiversityClassifications(
            final CemiSupplierClassificationMappingBo classificationMapping) {
        return classificationMapping.getKfsSupplierDiversityCodes().size() == 1;
    }

    private CemiSupplierClassificationBo createCemiSupplierClassificationBo(
            final CemiSupplierClassificationMappingBo classificationMapping,
            final Map<String, VendorSupplierDiversity> supplierDiversities) {
        final String expirationDate = classificationMapping.getKfsSupplierDiversityCodes().stream()
                .map(supplierDiversities::get)
                .map(VendorSupplierDiversity::getCertificationExpirationDate)
                .filter(expDate -> expDate != null)
                .min(Comparator.naturalOrder())
                .map(CemiVendorUtils::formatDate)
                .orElse(CemiBaseConstants.EMPTY_STRING);

        return CemiSupplierClassificationBoFactory.createClassificationBoFrom(
                classificationMapping.getWorkdayRefId(), expirationDate);
    }

}
