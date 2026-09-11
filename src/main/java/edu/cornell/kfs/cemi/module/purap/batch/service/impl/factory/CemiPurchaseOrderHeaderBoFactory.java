package edu.cornell.kfs.cemi.module.purap.batch.service.impl.factory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.criteria.GenericQueryResults;
import org.kuali.kfs.core.api.criteria.Predicate;
import org.kuali.kfs.core.api.criteria.PredicateFactory;
import org.kuali.kfs.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapConstants.ItemTypeCodes;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.cemi.module.purap.CemiPurchaseOrderConstants;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderHeaderBo;
import edu.cornell.kfs.cemi.module.purap.dataaccess.CemiPurchaseOrderExtractDao;
import edu.cornell.kfs.cemi.module.purap.util.CemiPurchaseOrderUtils;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.kim.CuKimPropertyConstants;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CemiPurchaseOrderHeaderBoFactory {

    private static final Logger LOG = LogManager.getLogger();

    private PurchaseOrderDocument purchaseOrderDocument;
    private PersonService personService;
    private CemiPurchaseOrderExtractDao cemiPurchaseOrderExtractDao;
    private String supplierJobRunDateString;

    public CemiPurchaseOrderHeaderBoFactory(final PurchaseOrderDocument purchaseOrderDocument,
            final PersonService personService, final CemiPurchaseOrderExtractDao cemiPurchaseOrderExtractDao,
            final String supplierJobRunDateString) {
        Validate.notNull(purchaseOrderDocument, "purchaseOrderDocument cannot be null");
        Validate.notNull(personService, "personService cannot be null");
        Validate.notNull(cemiPurchaseOrderExtractDao, "cemiPurchaseOrderExtractDao cannot be null");
        Validate.notBlank(supplierJobRunDateString, "supplierJobRunDateString cannot be blank");
        this.purchaseOrderDocument = purchaseOrderDocument;
        this.personService = personService;
        this.cemiPurchaseOrderExtractDao = cemiPurchaseOrderExtractDao;
        this.supplierJobRunDateString = supplierJobRunDateString;
    }

    public static CemiPurchaseOrderHeaderBo createHeaderBoFrom(final PurchaseOrderDocument purchaseOrderDocument,
            final PersonService personService, final CemiPurchaseOrderExtractDao cemiPurchaseOrderExtractDao,
            final String supplierJobRunDateString) {
        final CemiPurchaseOrderHeaderBoFactory factory = new CemiPurchaseOrderHeaderBoFactory(
                purchaseOrderDocument, personService, cemiPurchaseOrderExtractDao, supplierJobRunDateString);
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
        headerBo.setCurrency(CemiBaseConstants.CURRENCY_USD);
        headerBo.setAcknowledgementExpected(CemiBaseConstants.EMPTY_STRING);
        headerBo.setDefaultTaxOption(CemiBaseConstants.EMPTY_STRING);
        headerBo.setDefaultTaxCode(CemiBaseConstants.EMPTY_STRING);
        headerBo.setIssueOption(CemiPurchaseOrderConstants.PO_ISSUE_OPTION_PRINT);
        headerBo.setEmailRowId(CemiBaseConstants.EMPTY_STRING);
        headerBo.setEmailId(CemiBaseConstants.EMPTY_STRING);
        headerBo.setEmailAddress(CemiBaseConstants.EMPTY_STRING);
        headerBo.setBuyer(determineBuyerEmployeeId(requestorEmployeeId));
        headerBo.setBillToContact(CemiBaseConstants.EMPTY_STRING);
        headerBo.setBillToContactDetail(CemiBaseConstants.EMPTY_STRING);
        headerBo.setExistingBillToAddressId(CemiBaseConstants.EMPTY_STRING);
        headerBo.setNewBillToAddressId(CemiBaseConstants.EMPTY_STRING);
        headerBo.setShipToContact(determineShipToContactEmployeeId(deliveryRecipientEmployeeId, requestorEmployeeId));
        headerBo.setShipToContactDetail(determineShipToContactDetail(deliveryRecipientEmployeeId));
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
        return getEmployeeIdForUser(purchaseOrderDocument.getRequestorPersonName(),
                purchaseOrderDocument.getRequestorPersonEmailAddress(), CemiPurchaseOrderConstants.REQUESTOR_LABEL);
    }

    private String determineDeliveryRecipientEmployeeId() {
        return getEmployeeIdForUser(purchaseOrderDocument.getDeliveryToName(),
                purchaseOrderDocument.getDeliveryToEmailAddress(), CemiPurchaseOrderConstants.DELIVERY_RECIPIENT_LABEL);
    }

    private String getEmployeeIdForUser(final String personName, final String emailAddress, final String label) {
        final Predicate[] criteria = createCriteriaForPersonQuery(personName, emailAddress);
        if (criteria.length == 0) {
            LOG.warn("getEmployeeIdForUser, Insufficient {} information was available on PO document "
                    + "number {}; will return an empty employee ID", label, purchaseOrderDocument.getDocumentNumber());
            return CemiBaseConstants.EMPTY_STRING;
        }

        final QueryByCriteria query = QueryByCriteria.Builder.fromPredicates(criteria);
        final GenericQueryResults<Person> results = personService.findPeople(query);
        final List<Person> dataResults = results.getResults();

        final int numResults = dataResults.size();
        if (numResults == 0) {
            LOG.warn("getEmployeeIdForUser, Could not find a Person record for the {} on PO document "
                    + "number {}; will return an empty employee ID", label, purchaseOrderDocument.getDocumentNumber());
            return CemiBaseConstants.EMPTY_STRING;
        } else if (numResults != 1) {
            LOG.warn("getEmployeeIdForUser, Found multiple Person records for the {} on PO document "
                    + "number {}; will return an empty employee ID", label, purchaseOrderDocument.getDocumentNumber());
            return CemiBaseConstants.EMPTY_STRING;
        } else {
            final Person matchingPerson = dataResults.get(0);
            return StringUtils.defaultIfBlank(matchingPerson.getEmployeeId(), CemiBaseConstants.EMPTY_STRING);
        }
    }

    private Predicate[] createCriteriaForPersonQuery(final String personName, final String emailAddress) {
        final List<Pair<String, String>> personNameCriteria = getPersonNameCriteria(personName);
        if (personNameCriteria.isEmpty() || StringUtils.isBlank(emailAddress)) {
            return new Predicate[0];
        }

        final List<Pair<String, String>> emailCriteria = List.of(
                Pair.of(KRADPropertyConstants.EMAIL_ADDRESS, emailAddress));

        return Stream.of(personNameCriteria, emailCriteria)
                .flatMap(List::stream)
                .map(criterion -> PredicateFactory.equalIgnoreCase(criterion.getLeft(), criterion.getRight()))
                .toArray(Predicate[]::new);
    }

    private List<Pair<String, String>> getPersonNameCriteria(final String personName) {
        final List<String> personNameFields = List.of(KIMPropertyConstants.Person.FIRST_NAME,
                CuKimPropertyConstants.MIDDLE_NAME, KIMPropertyConstants.Person.LAST_NAME);
        final List<String> personNameSegments = getPersonNameSegments(personName);
        final long numValidNameSegments = personNameSegments.stream()
                .filter(StringUtils::isNotBlank)
                .count();

        if (numValidNameSegments >= 2L) {
            return IntStream.range(0, 3)
                    .filter(index -> StringUtils.isNotBlank(personNameSegments.get(index)))
                    .mapToObj(index -> Pair.of(personNameFields.get(index), personNameSegments.get(index)))
                    .collect(Collectors.toUnmodifiableList());
        } else {
            return List.of();
        }
    }

    private List<String> getPersonNameSegments(final String personName) {
        final String[] splitName = StringUtils.split(personName, KFSConstants.BLANK_SPACE);
        if (splitName.length == 2 || splitName.length == 3) {
            final String firstName;
            final String middleName;
            final String lastName;
            if (splitName[0].endsWith(KFSConstants.COMMA)) {
                lastName = StringUtils.substringBeforeLast(splitName[0], KFSConstants.COMMA);
                firstName = splitName[1];
                middleName = (splitName.length == 3) ? splitName[2] : CemiBaseConstants.EMPTY_STRING;
            } else {
                firstName = splitName[0];
                lastName = splitName[splitName.length - 1];
                middleName = (splitName.length == 3) ? splitName[1] : CemiBaseConstants.EMPTY_STRING;
            }
            return List.of(firstName, middleName, lastName);
        } else {
            return CemiUtils.createListOfEmptyStrings(3);
        }
    }

    private String determineCompanyId() {
        return CemiBaseConstants.DEFAULT_ITHACA_COMPANY;
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

    private String determineDocumentDate() {
        final WorkflowDocument workflowDocument = purchaseOrderDocument.getDocumentHeader().getWorkflowDocument();
        Validate.validState(workflowDocument.isFinal(), "PO Document Number %s should have been finalized",
                purchaseOrderDocument.getDocumentNumber());
        final LocalDateTime dateFinalized = workflowDocument.getDateFinalized();
        Validate.validState(dateFinalized != null, "PO Document Number %s should have had a finalization date",
                purchaseOrderDocument.getDocumentNumber());
        return CemiPurchaseOrderUtils.formatAsDate(dateFinalized);
    }

    private String determineFreightAmount() {
        final Optional<PurchaseOrderItem> freightItem = getFreightItemIfPresent();
        if (freightItem.isPresent()) {
            final KualiDecimal remainingFreightAmount = freightItem.get().getItemOutstandingEncumberedAmount();
            return (remainingFreightAmount != null && remainingFreightAmount.isGreaterThan(KualiDecimal.ZERO))
                    ? CemiPurchaseOrderUtils.formatAmount(remainingFreightAmount) : CemiBaseConstants.EMPTY_STRING;
        } else {
            return CemiBaseConstants.EMPTY_STRING;
        }
    }

    @SuppressWarnings("deprecation")
    private Optional<PurchaseOrderItem> getFreightItemIfPresent() {
        final List<?> items = purchaseOrderDocument.getItems();
        return items.stream()
                .map(PurchaseOrderItem.class::cast)
                .filter(item -> StringUtils.equals(item.getItemTypeCode(), ItemTypeCodes.ITEM_TYPE_FREIGHT_CODE))
                .filter(PurchaseOrderItem::isItemActiveIndicator)
                .findFirst();
    }

    // Copied and modified the related logic from the Supplier extract.
    private String determinePaymentTerms() {
        purchaseOrderDocument.refreshReferenceObject("vendorPaymentTerms");
        if (ObjectUtils.isNotNull(purchaseOrderDocument.getVendorPaymentTerms())) {
            String paymentTermsDescription = purchaseOrderDocument.getVendorPaymentTerms()
                    .getVendorPaymentTermsDescription();
            if (StringUtils.isBlank(paymentTermsDescription)) {
                return paymentTermsDescription;
            }
            return paymentTermsDescription
                    .trim()
                    .replaceAll("[^a-zA-Z0-9]+", "_") // replace spans of special chars/spaces with _
                    .replaceAll("^_|_$", ""); // strip leading/trailing underscores
        }
        return CemiBaseConstants.EMPTY_STRING;
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

    private String determineBuyerEmployeeId(final String requestorEmployeeId) {
        if (StringUtils.isNotBlank(requestorEmployeeId)) {
            return requestorEmployeeId;
        } else {
            final Person defaultBuyer = personService.getPersonByPrincipalName(
                    CemiPurchaseOrderConstants.DEFAULT_BUYER_PRINCIPAL_NAME);
            if (ObjectUtils.isNotNull(defaultBuyer) && StringUtils.isNotBlank(defaultBuyer.getEmployeeId())) {
                return defaultBuyer.getEmployeeId();
            } else {
                LOG.warn("determineBuyerEmployeeId, Default buyer {} does not exist or has no employee ID; "
                        + "defaulting to a blank buyer employee ID",
                        CemiPurchaseOrderConstants.DEFAULT_BUYER_PRINCIPAL_NAME);
                return CemiBaseConstants.EMPTY_STRING;
            }
        }
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

    private String determineShipToContactDetail(final String deliveryRecipientEmployeeId) {
        final String[] deliveryAddressLines = {
            getDeliveryRecipientLine(deliveryRecipientEmployeeId),
            getDeliveryLine1Address(),
            purchaseOrderDocument.getDeliveryBuildingLine2Address(),
            getDeliveryCityStatePostalCodeLine(),
            purchaseOrderDocument.getDeliveryCountryName()
        };

        return Arrays.stream(deliveryAddressLines)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .collect(Collectors.joining(KFSConstants.NEWLINE));
    }

    private String getDeliveryRecipientLine(final String deliveryRecipientEmployeeId) {
        if (StringUtils.isNotBlank(deliveryRecipientEmployeeId)) {
            return purchaseOrderDocument.getDeliveryToName();
        } else if (StringUtils.isNotBlank(purchaseOrderDocument.getRequestorPersonName())) {
            return purchaseOrderDocument.getRequestorPersonName();
        } else {
            return purchaseOrderDocument.getDeliveryToName();
        }
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

    private String determineMemoForSupplier() {
        final String originalPurchaseOrderTotalAmount = CemiPurchaseOrderUtils.formatAmount(
                purchaseOrderDocument.getTotalDollarAmount());
        return StringUtils.join(
                CemiPurchaseOrderConstants.ORIGINAL_PO_AMOUNT_MEMO_PREFIX, originalPurchaseOrderTotalAmount);
    }

}
