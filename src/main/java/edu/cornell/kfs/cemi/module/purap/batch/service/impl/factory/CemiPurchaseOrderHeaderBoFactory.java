package edu.cornell.kfs.cemi.module.purap.batch.service.impl.factory;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.criteria.GenericQueryResults;
import org.kuali.kfs.core.api.criteria.Predicate;
import org.kuali.kfs.core.api.criteria.PredicateFactory;
import org.kuali.kfs.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapConstants.ItemTypeCodes;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.Country;
import org.kuali.kfs.sys.service.LocationService;

import edu.cornell.kfs.cemi.module.purap.CemiPurchaseOrderConstants;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderDocumentLite;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderHeaderBo;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderIdBo;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderItemLite;
import edu.cornell.kfs.cemi.module.purap.dataaccess.CemiPurchaseOrderExtractDao;
import edu.cornell.kfs.cemi.module.purap.util.CemiPurchaseOrderUtils;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.vnd.CemiSupplierConstants;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CemiPurchaseOrderHeaderBoFactory {

    private static final Logger LOG = LogManager.getLogger();

    private CemiPurchaseOrderDocumentLite purchaseOrderDocument;
    private PersonService personService;
    private CemiPurchaseOrderExtractDao cemiPurchaseOrderExtractDao;
    private DateTimeService dateTimeService;
    private LocationService locationService;
    private Map<Integer, String> contractManagerEmplIdMappings;
    private Map<String, String> paymentTermsMappings;
    private String supplierJobRunDateString;

    public CemiPurchaseOrderHeaderBoFactory(final CemiPurchaseOrderDocumentLite purchaseOrderDocument,
            final PersonService personService, final CemiPurchaseOrderExtractDao cemiPurchaseOrderExtractDao,
            final DateTimeService dateTimeService, final LocationService locationService,
            final Map<Integer, String> contractManagerEmplIdMappings, final Map<String, String> paymentTermsMappings,
            final String supplierJobRunDateString) {
        Validate.notNull(purchaseOrderDocument, "purchaseOrderDocument cannot be null");
        Validate.notNull(personService, "personService cannot be null");
        Validate.notNull(cemiPurchaseOrderExtractDao, "cemiPurchaseOrderExtractDao cannot be null");
        Validate.notNull(dateTimeService, "dateTimeService cannot be null");
        Validate.notNull(locationService, "locationService cannot be null");
        Validate.notNull(contractManagerEmplIdMappings, "contractManagerEmplIdMappings cannot be null");
        Validate.notNull(paymentTermsMappings, "paymentTermsMappings cannot be null");
        Validate.notBlank(supplierJobRunDateString, "supplierJobRunDateString cannot be blank");
        this.purchaseOrderDocument = purchaseOrderDocument;
        this.personService = personService;
        this.cemiPurchaseOrderExtractDao = cemiPurchaseOrderExtractDao;
        this.dateTimeService = dateTimeService;
        this.locationService = locationService;
        this.contractManagerEmplIdMappings = contractManagerEmplIdMappings;
        this.paymentTermsMappings = paymentTermsMappings;
        this.supplierJobRunDateString = supplierJobRunDateString;
    }

    public static CemiPurchaseOrderHeaderBo createHeaderBoFrom(final CemiPurchaseOrderDocumentLite purchaseOrderDocument,
            final PersonService personService, final CemiPurchaseOrderExtractDao cemiPurchaseOrderExtractDao,
            final DateTimeService dateTimeService, final LocationService locationService,
            final Map<Integer, String> contractManagerEmplIdMappings, final Map<String, String> paymentTermsMappings,
            final String supplierJobRunDateString) {
        final CemiPurchaseOrderHeaderBoFactory factory = new CemiPurchaseOrderHeaderBoFactory(
                purchaseOrderDocument, personService, cemiPurchaseOrderExtractDao,
                dateTimeService, locationService, contractManagerEmplIdMappings,
                paymentTermsMappings, supplierJobRunDateString);
        return factory.createCemiPurchaseOrderHeaderBo();
    }

    public CemiPurchaseOrderHeaderBo createCemiPurchaseOrderHeaderBo() {
        final CemiPurchaseOrderHeaderBo headerBo = new CemiPurchaseOrderHeaderBo();

        final String purchaseOrderId = purchaseOrderDocument.getPurapDocumentIdentifier().toString();
        final String requestorEmployeeId = determineRequestorEmployeeId();
        final String deliveryRecipientEmployeeId = determineDeliveryRecipientEmployeeId();

        headerBo.setKfsDocumentNumber(purchaseOrderDocument.getDocumentNumber());
        headerBo.setKfsPurchaseOrderId(purchaseOrderDocument.getPurapDocumentIdentifier());

        headerBo.setSpreadsheetKey(purchaseOrderId);
        headerBo.setAddOnly(CemiBaseConstants.EMPTY_STRING);
        headerBo.setExistingPurchaseOrderDocumentNumber(CemiBaseConstants.EMPTY_STRING);
        headerBo.setOrderTypeReference(CemiBaseConstants.EMPTY_STRING);
        headerBo.setAutoComplete(CemiBaseConstants.YES);
        headerBo.setComment(CemiBaseConstants.EMPTY_STRING);
        headerBo.setWorker(requestorEmployeeId);
        headerBo.setPurchaseOrderId(purchaseOrderId);
        headerBo.setSubmit(CemiBaseConstants.YES);
        headerBo.setLockedInWorkday(CemiBaseConstants.EMPTY_STRING);
        headerBo.setDocumentNumber(purchaseOrderId);
        headerBo.setInvoiceStatus(CemiBaseConstants.EMPTY_STRING);
        headerBo.setPaymentStatus(CemiBaseConstants.EMPTY_STRING);
        headerBo.setReceivingStatus(CemiBaseConstants.EMPTY_STRING);
        headerBo.setShippingStatus(CemiBaseConstants.EMPTY_STRING);
        headerBo.setTrackingStatus(CemiBaseConstants.EMPTY_STRING);
        headerBo.setCompany(determineCompanyId());
        headerBo.setSupplier(determineSupplierId());
        headerBo.setPurchaseOrderType(determinePurchaseOrderType());
        headerBo.setExternalPoNumber(CemiBaseConstants.EMPTY_STRING);
        headerBo.setOrderFromSupplierConnection(CemiBaseConstants.EMPTY_STRING);
        headerBo.setDocumentDate(determineDocumentDate());
        headerBo.setTaxAmount(CemiBaseConstants.EMPTY_STRING);
        headerBo.setFreightAmount(determineFreightAmount());
        headerBo.setOtherCharges(CemiBaseConstants.EMPTY_STRING);
        headerBo.setPaymentTerms(determinePaymentTerms());
        headerBo.setOverridePaymentType(CemiBaseConstants.EMPTY_STRING);
        headerBo.setProcurementCreditCard(CemiBaseConstants.EMPTY_STRING);
        headerBo.setShippingTerms(determineShippingTerms());
        headerBo.setShippingMethod(determineShippingMethod());
        headerBo.setShippingInstruction(CemiBaseConstants.EMPTY_STRING);
        headerBo.setDueDate(CemiBaseConstants.EMPTY_STRING);
        headerBo.setSupplierContract(determineSupplierContractNumber());
        headerBo.setExternalSupplierInvoiceSource(CemiBaseConstants.EMPTY_STRING);
        headerBo.setCurrency(CemiPurchaseOrderConstants.CURRENCY_USD);
        headerBo.setAcknowledgementExpected(CemiBaseConstants.EMPTY_STRING);
        headerBo.setDefaultTaxOption(CemiBaseConstants.EMPTY_STRING);
        headerBo.setDefaultTaxCode(CemiBaseConstants.EMPTY_STRING);
        headerBo.setIssueOption(CemiPurchaseOrderConstants.PO_ISSUE_OPTION_PRINT);
        headerBo.setEmailRowId(CemiBaseConstants.EMPTY_STRING);
        headerBo.setEmailId(CemiBaseConstants.EMPTY_STRING);
        headerBo.setEmailAddress(CemiBaseConstants.EMPTY_STRING);
        headerBo.setBuyer(determineBuyerEmployeeId());
        headerBo.setBillToContact(CemiBaseConstants.EMPTY_STRING);
        headerBo.setBillToContactDetail(CemiBaseConstants.EMPTY_STRING);
        headerBo.setExistingBillToAddressId(CemiBaseConstants.EMPTY_STRING);
        headerBo.setNewBillToAddressId(CemiBaseConstants.EMPTY_STRING);
        headerBo.setShipToContact(determineShipToContactEmployeeId(deliveryRecipientEmployeeId, requestorEmployeeId));
        headerBo.setShipToContactDetail(determineShipToContactDetail());
        headerBo.setExistingShipToAddressId(CemiBaseConstants.EMPTY_STRING);
        headerBo.setNewShipToAddressId(CemiBaseConstants.EMPTY_STRING);
        headerBo.setDocumentLink(CemiBaseConstants.EMPTY_STRING);
        headerBo.setMemoForSupplier(determineMemoForSupplier());
        headerBo.setInternalMemo(CemiPurchaseOrderConstants.LEGACY_PO_CONVERSION_LABEL);
        headerBo.setPrepaid(CemiBaseConstants.EMPTY_STRING);
        headerBo.setPrepaymentReleaseType(CemiBaseConstants.EMPTY_STRING);
        headerBo.setExpectedReleaseDate(CemiBaseConstants.EMPTY_STRING);
        headerBo.setFrequency(CemiBaseConstants.EMPTY_STRING);
        headerBo.setNumberOfPrepaymentInstallments(CemiBaseConstants.EMPTY_STRING);
        headerBo.setUseInvoiceDate(CemiBaseConstants.EMPTY_STRING);
        headerBo.setSpecifiedDate(CemiBaseConstants.EMPTY_STRING);
        headerBo.setUsePrepaidPostingRulesForReceiptAccruals(CemiBaseConstants.EMPTY_STRING);
        headerBo.setPercentToRetain(CemiBaseConstants.EMPTY_STRING);
        headerBo.setEstimatedRetentionReleaseDate(CemiBaseConstants.EMPTY_STRING);
        headerBo.setXmlname3rdPartyRetention(CemiBaseConstants.EMPTY_STRING);
        headerBo.setRetentionMemo(CemiBaseConstants.EMPTY_STRING);
        headerBo.setDownPaymentAmount(CemiBaseConstants.EMPTY_STRING);
        headerBo.setDownPaymentPercentage(CemiBaseConstants.EMPTY_STRING);
        headerBo.setDownPaymentMemo(CemiBaseConstants.EMPTY_STRING);
        headerBo.setProcedureDate(CemiBaseConstants.EMPTY_STRING);
        headerBo.setProcedure(CemiBaseConstants.EMPTY_STRING);
        headerBo.setProcedureNumber(CemiBaseConstants.EMPTY_STRING);
        headerBo.setPatientId(CemiBaseConstants.EMPTY_STRING);
        headerBo.setMedicalRecordNumber(CemiBaseConstants.EMPTY_STRING);
        headerBo.setPhysicianId(CemiBaseConstants.EMPTY_STRING);
        headerBo.setVerifiedBy(CemiBaseConstants.EMPTY_STRING);
        headerBo.setSupplierRepresentative(CemiBaseConstants.EMPTY_STRING);
        headerBo.setSupplierSalesOrderNumber(CemiBaseConstants.EMPTY_STRING);
        headerBo.setAdditionalProcedureDetails(CemiBaseConstants.EMPTY_STRING);

        return headerBo;
    }

    private String determineRequestorEmployeeId() {
        return getEmployeeIdByEmail(
                purchaseOrderDocument.getRequestorPersonEmailAddress(), CemiPurchaseOrderConstants.REQUESTOR_LABEL);
    }

    private String determineDeliveryRecipientEmployeeId() {
        return getEmployeeIdByEmail(
                purchaseOrderDocument.getDeliveryToEmailAddress(), CemiPurchaseOrderConstants.DELIVERY_RECIPIENT_LABEL);
    }

    /*
     * Certain people mentioned on the PO (such as the requestor) are only identified by name, phone and/or email,
     * not by Principal ID or Principal Name. Thus, we attempt to use the email address to identify the person.
     */
    private String getEmployeeIdByEmail(final String emailAddress, final String label) {
        if (StringUtils.isBlank(emailAddress)) {
            LOG.warn("getEmployeeIdByEmail, Insufficient {} email information was available on PO document "
                    + "number {}; will return an empty employee ID", label, purchaseOrderDocument.getDocumentNumber());
            return CemiBaseConstants.EMPTY_STRING;
        }

        final String netId = CemiPurchaseOrderUtils.getNetIdFromEmailAddressIfPresent(emailAddress);
        if (StringUtils.isNotBlank(netId)) {
            final Person potentialEmployee = personService.getPersonByPrincipalName(netId);
            return ObjectUtils.isNotNull(potentialEmployee)
                    ? StringUtils.defaultIfBlank(potentialEmployee.getEmployeeId(), CemiBaseConstants.EMPTY_STRING)
                    : CemiBaseConstants.EMPTY_STRING;
        }

        final Predicate emailCriteria = PredicateFactory.equalIgnoreCase(KRADPropertyConstants.EMAIL_ADDRESS, netId);
        final QueryByCriteria query = QueryByCriteria.Builder.fromPredicates(emailCriteria);
        final GenericQueryResults<Person> results = personService.findPeople(query);
        final List<Person> dataResults = results.getResults();

        final int numResults = dataResults.size();
        if (numResults == 0) {
            LOG.warn("getEmployeeIdByEmail, Could not find a Person record for the {} on PO document "
                    + "number {}; will return an empty employee ID", label, purchaseOrderDocument.getDocumentNumber());
            return CemiBaseConstants.EMPTY_STRING;
        } else if (numResults != 1) {
            LOG.warn("getEmployeeIdByEmail, Found multiple Person records for the {} on PO document "
                    + "number {}; will return an empty employee ID", label, purchaseOrderDocument.getDocumentNumber());
            return CemiBaseConstants.EMPTY_STRING;
        } else {
            final Person matchingPerson = dataResults.get(0);
            return StringUtils.defaultIfBlank(matchingPerson.getEmployeeId(), CemiBaseConstants.EMPTY_STRING);
        }
    }

    private String determineCompanyId() {
        return CemiPurchaseOrderConstants.DEFAULT_ITHACA_COMPANY;
    }

    private String determineSupplierId() {
        final String supplierId = cemiPurchaseOrderExtractDao.getSupplierIdForVendor(
                purchaseOrderDocument.getVendorHeaderGeneratedIdentifier(),
                purchaseOrderDocument.getVendorDetailAssignedIdentifier(), supplierJobRunDateString);
        if (StringUtils.isBlank(supplierId)) {
            LOG.error("determineSupplierId, Could not find a Supplier ID for Vendor {}-{} and run date \"{}\"; "
                    + "either the wrong run date was used or the wrong Supplier scope was processed upstream. "
                    + "Will output an empty Supplier ID for now, which will need correcting.",
                    purchaseOrderDocument.getVendorHeaderGeneratedIdentifier(),
                    purchaseOrderDocument.getVendorDetailAssignedIdentifier(), supplierJobRunDateString);
        }
        return supplierId;
    }

    // TODO: At a future date, implement logic for deriving the Workday PO Type from the legacy PO Type.
    private String determinePurchaseOrderType() {
        return CemiBaseConstants.EMPTY_STRING;
    }

    @SuppressWarnings("deprecation")
    private String determineDocumentDate() {
        final CemiPurchaseOrderIdBo purchaseOrderIdBo = purchaseOrderDocument.getPurchaseOrderIdBo();
        Validate.validState(ObjectUtils.isNotNull(purchaseOrderIdBo),
                "Purchase Order ID helper object for PO Document Number %s should not have been null",
                purchaseOrderDocument.getDocumentNumber());
        Validate.validState(
                StringUtils.equalsAny(purchaseOrderIdBo.getDocRouteStatus(),
                        KewApiConstants.ROUTE_HEADER_PROCESSED_CD, KewApiConstants.ROUTE_HEADER_FINAL_CD),
                "PO Document Number %s should have been in PROCESSED or FINAL status",
                purchaseOrderDocument.getDocumentNumber());
        final Timestamp lastApprovedDate = purchaseOrderIdBo.getApprovedDate();
        Validate.validState(lastApprovedDate != null, "PO Document Number %s should have had a Last Approved Date",
                purchaseOrderDocument.getDocumentNumber());
        final LocalDateTime lastApprovedDateTime = dateTimeService.getLocalDateTime(lastApprovedDate);
        return CemiPurchaseOrderUtils.formatAsDate(lastApprovedDateTime);
    }

    private String determineFreightAmount() {
        final Optional<CemiPurchaseOrderItemLite> freightItem = getFreightItemIfPresent();
        if (freightItem.isPresent()) {
            final KualiDecimal remainingFreightAmount = freightItem.get().getItemOutstandingEncumberedAmount();
            return (remainingFreightAmount != null && remainingFreightAmount.isGreaterThan(KualiDecimal.ZERO))
                    ? CemiPurchaseOrderUtils.formatAmount(remainingFreightAmount) : CemiBaseConstants.EMPTY_STRING;
        } else {
            return CemiBaseConstants.EMPTY_STRING;
        }
    }

    @SuppressWarnings("deprecation")
    private Optional<CemiPurchaseOrderItemLite> getFreightItemIfPresent() {
        return purchaseOrderDocument.getItems().stream()
                .filter(item -> StringUtils.equals(item.getItemTypeCode(), ItemTypeCodes.ITEM_TYPE_FREIGHT_CODE))
                .filter(CemiPurchaseOrderItemLite::isItemActiveIndicator)
                .findFirst();
    }

    private String determinePaymentTerms() {
        final String paymentTermsCode = purchaseOrderDocument.getVendorPaymentTermsCode();
        if (StringUtils.isBlank(paymentTermsCode)) {
            return CemiBaseConstants.EMPTY_STRING;
        }
        return paymentTermsMappings.getOrDefault(paymentTermsCode, CemiSupplierConstants.PAYMENT_TERMS_UNKNOWN);
    }

    // TODO: At a future date, implement shipping term derivation logic.
    private String determineShippingTerms() {
        return CemiBaseConstants.EMPTY_STRING;
    }

    // TODO: At a future date, implement shipping method derivation logic.
    private String determineShippingMethod() {
        return CemiBaseConstants.EMPTY_STRING;
    }

    private String determineSupplierContractNumber() {
        final Integer vendorContractId = purchaseOrderDocument.getVendorContractGeneratedIdentifier();
        return (vendorContractId != null) ? vendorContractId.toString() : CemiBaseConstants.EMPTY_STRING;
    }

    private String determineBuyerEmployeeId() {
        final Integer contractManagerCode = purchaseOrderDocument.getContractManagerCode();
        if (contractManagerCode == null) {
            return CemiBaseConstants.EMPTY_STRING;
        }
        return contractManagerEmplIdMappings.getOrDefault(contractManagerCode, CemiBaseConstants.EMPTY_STRING);
    }

    private String determineShipToContactEmployeeId(final String deliveryRecipientEmployeeId,
            final String requestorEmployeeId) {
        if (StringUtils.isNotBlank(deliveryRecipientEmployeeId)) {
            return deliveryRecipientEmployeeId;
        } else if (StringUtils.isNotBlank(requestorEmployeeId)) {
            return requestorEmployeeId;
        } else {
            return CemiBaseConstants.EMPTY_STRING;
        }
    }

    private String determineShipToContactDetail() {
        final String[] deliveryAddressLines = {
            purchaseOrderDocument.getDeliveryToName(),
            getDeliveryLine1Address(),
            purchaseOrderDocument.getDeliveryBuildingLine2Address(),
            getDeliveryCityStatePostalCodeLine(),
            getDeliveryCountryName()
        };

        return Arrays.stream(deliveryAddressLines)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .collect(Collectors.joining(KFSConstants.NEWLINE));
    }

    private String getDeliveryLine1Address() {
        return StringUtils.join(purchaseOrderDocument.getDeliveryBuildingLine1Address(), CUKFSConstants.COMMA_AND_SPACE,
                CemiPurchaseOrderConstants.ROOM_NUMBER_SEGMENT_PREFIX,
                purchaseOrderDocument.getDeliveryBuildingRoomNumber());
    }

    private String getDeliveryCityStatePostalCodeLine() {
        return StringUtils.join(purchaseOrderDocument.getDeliveryCityName(), CUKFSConstants.COMMA_AND_SPACE,
                purchaseOrderDocument.getDeliveryStateCode(), KFSConstants.BLANK_SPACE,
                purchaseOrderDocument.getDeliveryPostalCode());
    }

    // This is a modified copy of the code from PurchasingDocumentBase.getDeliveryCountryName()
    // TODO: When implementing KFSPTS-39551, determine if this logic needs rework; may need to use ISO country code/name.
    private String getDeliveryCountryName() {
        if (StringUtils.isNotBlank(purchaseOrderDocument.getDeliveryCountryCode())) {
            final Country country = locationService.getCountry(purchaseOrderDocument.getDeliveryCountryCode());
            if (ObjectUtils.isNotNull(country)) {
                return country.getName();
            }
        }
        return null;
    }

    private String determineMemoForSupplier() {
        final CemiPurchaseOrderIdBo purchaseOrderIdBo = purchaseOrderDocument.getPurchaseOrderIdBo();
        Validate.validState(ObjectUtils.isNotNull(purchaseOrderIdBo),
                "Purchase Order ID helper object for PO Document Number %s should not have been null",
                purchaseOrderDocument.getDocumentNumber());

        final KualiDecimal totalDollarAmount = CemiPurchaseOrderUtils
                .getTotalPurchaseOrderDollarAmount(purchaseOrderDocument);
        final String originalPurchaseOrderTotalAmount = CemiPurchaseOrderUtils.formatAmount(totalDollarAmount);

        final StringBuilder memo = new StringBuilder(100);
        memo.append(CemiPurchaseOrderConstants.ORIGINAL_PO_AMOUNT_MEMO_PREFIX)
                .append(originalPurchaseOrderTotalAmount);
        if (StringUtils.isNotBlank(purchaseOrderIdBo.getExplanation())) {
            memo.append(KFSConstants.NEWLINE)
                    .append(CemiPurchaseOrderConstants.BUSINESS_PURPOSE_MEMO_PREFIX)
                    .append(purchaseOrderIdBo.getExplanation());
        }

        return memo.toString();
    }

}
