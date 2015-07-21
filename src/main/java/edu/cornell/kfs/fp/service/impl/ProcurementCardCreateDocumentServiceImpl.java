/*
 * Copyright 2006 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * kavery Apr 15, 2015
 * Jira # KUP-189
 * Description: Add UConn description for PCDO, using 13 characters for cardholder name
 */


package edu.cornell.kfs.fp.service.impl;

import static org.kuali.kfs.sys.KFSConstants.GL_CREDIT_CODE;
import static org.kuali.kfs.sys.KFSConstants.FinancialDocumentTypeCodes.PROCUREMENT_CARD;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.kfs.fp.businessobject.CapitalAssetInformation;
import org.kuali.kfs.fp.businessobject.ProcurementCardSourceAccountingLine;
import org.kuali.kfs.fp.businessobject.ProcurementCardTargetAccountingLine;
import org.kuali.kfs.fp.businessobject.ProcurementCardTransaction;
import org.kuali.kfs.fp.businessobject.ProcurementCardTransactionDetail;
import org.kuali.kfs.fp.document.ProcurementCardDocument;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.service.AccountingLineRuleHelperService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.krad.bo.DocumentHeader;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.util.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.fp.businessobject.FuelDetail;
import edu.cornell.kfs.fp.businessobject.FuelRecord;
import edu.cornell.kfs.fp.businessobject.LodgingDetail;
import edu.cornell.kfs.fp.businessobject.LodgingRecord;
import edu.cornell.kfs.fp.businessobject.NonFuelDetail;
import edu.cornell.kfs.fp.businessobject.NonFuelRecord;
import edu.cornell.kfs.fp.businessobject.PassengerTransportDetail;
import edu.cornell.kfs.fp.businessobject.PassengerTransportLegDetail;
import edu.cornell.kfs.fp.businessobject.PassengerTransportLegRecord;
import edu.cornell.kfs.fp.businessobject.PassengerTransportRecord;
import edu.cornell.kfs.fp.businessobject.ProcurementCardDetail;
import edu.cornell.kfs.fp.businessobject.ProcurementCardGenericDetail;
import edu.cornell.kfs.fp.businessobject.ProcurementCardGenericRecord;
import edu.cornell.kfs.fp.businessobject.ProcurementCardHolderDetail;
import edu.cornell.kfs.fp.businessobject.ProcurementCardRecord;
import edu.cornell.kfs.fp.businessobject.ProcurementCardTransactionDetailExtension;
import edu.cornell.kfs.fp.businessobject.ProcurementCardTransactionExtension;
import edu.cornell.kfs.fp.businessobject.ProcurementCardUserAmountDetail;
import edu.cornell.kfs.fp.businessobject.ProcurementCardUserAmountRecord;
import edu.cornell.kfs.fp.businessobject.RentalCarDetail;
import edu.cornell.kfs.fp.businessobject.RentalCarRecord;


/**
 * This is the default implementation of the ProcurementCardCreateDocumentService interface.
 *
 * @see org.kuali.kfs.fp.batch.service.ProcurementCardCreateDocumentService
 */
@Transactional
public class ProcurementCardCreateDocumentServiceImpl extends org.kuali.kfs.fp.batch.service.impl.ProcurementCardCreateDocumentServiceImpl
{
	private static Logger LOG = Logger.getLogger(ProcurementCardCreateDocumentServiceImpl.class);

	public static final String WORKFLOW_SEARCH_RESULT_KEY = "routeHeaderId";

	// hold the default values from the custom Procurement Cardholder table
	private String cardholderName;
	private String cardholderChartCode;
	private String cardholderAccountNumber;
	private String cardholderSubAccountNumber;
	private String cardholderObjectCode;
	private String cardholderSubObjectCode;

	private AccountingLineRuleHelperService accountingLineRuleUtil;

	/**
	 * From the transaction accounting attributes, creates source and target accounting lines. Attributes are validated first, and
	 * replaced with default and error values if needed. There will be 1 source and 1 target line generated.
	 *
	 * @param pcardDocument The procurement card document to add the new accounting lines to.
	 * @param transaction The transaction to process into account lines.
	 * @param docTransactionDetail The transaction detail to create source and target accounting lines from.
	 * @return String containing any error messages.
	 */
	protected String createAndValidateAccountingLines(ProcurementCardDocument pcardDocument, ProcurementCardTransaction transaction, ProcurementCardTransactionDetail docTransactionDetail)
	{
		// get default values from custom Procurement Cardholder table
		getDefaultValues(transaction.getTransactionCreditCardNumber());

		// build source lines
		ProcurementCardSourceAccountingLine sourceLine = createSourceAccountingLine(transaction, docTransactionDetail);
		sourceLine.setPostingYear(pcardDocument.getPostingYear());

		// add line to transaction through document since document contains the next sequence number fields
		pcardDocument.addSourceAccountingLine(sourceLine);

		// build target lines
		ProcurementCardTargetAccountingLine targetLine = createTargetAccountingLine(transaction, docTransactionDetail);
		targetLine.setPostingYear(pcardDocument.getPostingYear());

		// add line to transaction through document since document contains the next sequence number fields
		pcardDocument.addTargetAccountingLine(targetLine);

		return validateTargetAccountingLine(targetLine);
	}

	/**
	 * Gets the default Chart Code, Account from the custom Procurement Cardholder table.
	 */
	protected void getDefaultValues(String creditCardNumber)
	{
		Map<String, String> pkMap = new HashMap<String, String>();
		pkMap.put("creditCardNumber", creditCardNumber);
		ProcurementCardHolderDetail procurementCardHolderDetail = businessObjectService.findByPrimaryKey(ProcurementCardHolderDetail.class, pkMap);

		if (ObjectUtils.isNotNull(procurementCardHolderDetail))
		{
			cardholderName = procurementCardHolderDetail.getCardHolderName();
			cardholderChartCode = procurementCardHolderDetail.getChartOfAccountsCode();
			cardholderAccountNumber = procurementCardHolderDetail.getAccountNumber();
			cardholderSubAccountNumber = procurementCardHolderDetail.getSubAccountNumber();
			cardholderObjectCode = procurementCardHolderDetail.getFinancialObjectCode();
			cardholderSubObjectCode = procurementCardHolderDetail.getFinancialSubObjectCode();
		}
	}

	/**
	 * Creates a transaction detail record and adds that record to the document provided.
	 *
	 * @param pcardDocument Document to place record in.
	 * @param transaction Transaction to set fields from.
	 * @param transactionLineNumber Line number of the new transaction detail record within the procurement card document.
	 * @return The error text that was generated from the creation of the detail records. If the text is empty, no errors were
	 *         encountered.
	 */
	protected String createTransactionDetailRecord(ProcurementCardDocument pcardDocument, ProcurementCardTransaction transaction, Integer transactionLineNumber)
	{
		ProcurementCardTransactionDetail transactionDetail = new ProcurementCardTransactionDetail();

		// set the document transaction detail fields from the loaded transaction record
		transactionDetail.setDocumentNumber(pcardDocument.getDocumentNumber());
		transactionDetail.setFinancialDocumentTransactionLineNumber(transactionLineNumber);
		transactionDetail.setTransactionDate(transaction.getTransactionDate());
		transactionDetail.setTransactionReferenceNumber(transaction.getTransactionReferenceNumber());
		transactionDetail.setTransactionBillingCurrencyCode(transaction.getTransactionBillingCurrencyCode());
		transactionDetail.setTransactionCurrencyExchangeRate(transaction.getTransactionCurrencyExchangeRate());
		transactionDetail.setTransactionDate(transaction.getTransactionDate());
		transactionDetail.setTransactionOriginalCurrencyAmount(transaction.getTransactionOriginalCurrencyAmount());
		transactionDetail.setTransactionOriginalCurrencyCode(transaction.getTransactionOriginalCurrencyCode());
		transactionDetail.setTransactionPointOfSaleCode(transaction.getTransactionPointOfSaleCode());
		transactionDetail.setTransactionPostingDate(transaction.getTransactionPostingDate());
		transactionDetail.setTransactionPurchaseIdentifierDescription(transaction.getTransactionPurchaseIdentifierDescription());
		transactionDetail.setTransactionPurchaseIdentifierIndicator(transaction.getTransactionPurchaseIdentifierIndicator());
		transactionDetail.setTransactionSalesTaxAmount(transaction.getTransactionSalesTaxAmount());
		transactionDetail.setTransactionSettlementAmount(transaction.getTransactionSettlementAmount());
		transactionDetail.setTransactionTaxExemptIndicator(transaction.getTransactionTaxExemptIndicator());
		transactionDetail.setTransactionTravelAuthorizationCode(transaction.getTransactionTravelAuthorizationCode());
		transactionDetail.setTransactionUnitContactName(transaction.getTransactionUnitContactName());

		if (GL_CREDIT_CODE.equals(transaction.getTransactionDebitCreditCode()))
		{
			transactionDetail.setTransactionTotalAmount(transaction.getFinancialDocumentTotalAmount().negated());
		}
		else
		{
			transactionDetail.setTransactionTotalAmount(transaction.getFinancialDocumentTotalAmount());
		}

		// create the Extension object, which contains the Level 3 info
		createProcurementCardTransactionDetailExtension(transaction, transactionDetail);

		// create transaction vendor record
		createTransactionVendorRecord(pcardDocument, transaction, transactionDetail);

		// add transaction detail to document
		pcardDocument.getTransactionEntries().add(transactionDetail);

		// now create the initial source and target lines for this transaction
		return createAndValidateAccountingLines(pcardDocument, transaction, transactionDetail);
	}

	protected void createProcurementCardTransactionDetailExtension(ProcurementCardTransaction transaction, ProcurementCardTransactionDetail transactionDetail)
	{
		ProcurementCardTransactionDetailExtension detailExtension;
		if (ObjectUtils.isNull(transactionDetail.getExtension())) {
			detailExtension = new ProcurementCardTransactionDetailExtension();
		} else {
			detailExtension = (ProcurementCardTransactionDetailExtension) transactionDetail.getExtension();
		}

		if (ObjectUtils.isNotNull(transaction.getExtension()))
		{
			ProcurementCardTransactionExtension extension = (ProcurementCardTransactionExtension) transaction.getExtension();

			detailExtension.setCustomerCode(extension.getCustomerCode());
			detailExtension.setDocumentNumber(transactionDetail.getDocumentNumber());
			detailExtension.setFinancialDocumentTransactionLineNumber(transactionDetail.getFinancialDocumentTransactionLineNumber());

			createPcardDetails(extension, detailExtension);
			createPcardUserAmountDetails(extension, detailExtension);
			createPassengerTransportDetails(extension, detailExtension);
			createPassengerTransportLegDetails(extension, detailExtension);
			createLodgingDetails(extension, detailExtension);
			createRentalCarDetails(extension, detailExtension);
			createGenericDetails(extension, detailExtension);
			createFuelDetails(extension, detailExtension);
			createNonFuelDetails(extension, detailExtension);
		}
		transactionDetail.setExtension(detailExtension);
	}

	protected void createPcardDetails(ProcurementCardTransactionExtension extension, ProcurementCardTransactionDetailExtension detailExtension)
	{
		List<ProcurementCardDetail> details = new ArrayList<ProcurementCardDetail>();
		for (ProcurementCardRecord record : extension.getPcardRecords())
		{
			ProcurementCardDetail detail = new ProcurementCardDetail();
			detail.setDocumentNumber(detailExtension.getDocumentNumber());
			detail.setFinancialDocumentTransactionLineNumber(detailExtension.getFinancialDocumentTransactionLineNumber());
			detail.setProductCode(record.getProductCode());
			detail.setItemDescription(record.getItemDescription());
			detail.setItemQuantity(record.getItemQuantity());
			detail.setItemUnitOfMeasure(record.getItemUnitOfMeasure());
			detail.setExtendedItemAmount(record.getExtendedItemAmount());
			detail.setDebitCreditInd(record.getDebitCreditInd());
			detail.setNetGrossIndicator(record.getNetGrossIndicator());
			detail.setTaxRateApplied(record.getTaxRateApplied());
			detail.setTaxTypeApplied(record.getTaxTypeApplied());
			detail.setTaxAmount(record.getTaxAmount());
			detail.setDiscountIndicator(record.getDiscountIndicator());
			detail.setDiscountAmount(record.getDiscountAmount());
			details.add(detail);
		}
		detailExtension.setPcardDetails(details);
	}

	private void createPcardUserAmountDetails(ProcurementCardTransactionExtension extension, ProcurementCardTransactionDetailExtension detailExtension)
	{
		List<ProcurementCardUserAmountDetail> details = new ArrayList<ProcurementCardUserAmountDetail>();
		for (ProcurementCardUserAmountRecord record : extension.getPcardUserAmountRecords())
		{
			ProcurementCardUserAmountDetail detail = new ProcurementCardUserAmountDetail();
			detail.setDocumentNumber(detailExtension.getDocumentNumber());
			detail.setFinancialDocumentTransactionLineNumber(detailExtension.getFinancialDocumentTransactionLineNumber());
			detail.setUserAmount(record.getUserAmount());
			details.add(detail);
		}
		detailExtension.setPcardUserAmountDetails(details);

	}

	private void createPassengerTransportDetails(ProcurementCardTransactionExtension extension, ProcurementCardTransactionDetailExtension detailExtension)
	{
		List<PassengerTransportDetail> details = new ArrayList<PassengerTransportDetail>();
		for (PassengerTransportRecord record : extension.getPassengerTransportRecords())
		{
			PassengerTransportDetail detail = new PassengerTransportDetail();
			detail.setDocumentNumber(detailExtension.getDocumentNumber());
			detail.setFinancialDocumentTransactionLineNumber(detailExtension.getFinancialDocumentTransactionLineNumber());
			detail.setPassengerName(record.getPassengerName());
			detail.setDepartureDate(record.getDepartureDate());
			detail.setAirportCode(record.getAirportCode());
			detail.setTravelAgencyCode(record.getTravelAgencyCode());
			detail.setTravelAgencyName(record.getTravelAgencyName());
			detail.setTicketNumber(record.getTicketNumber());
			detail.setCustomerCode(record.getCustomerCode());
			detail.setIssueDate(record.getIssueDate());
			detail.setIssuingCarrier(record.getIssuingCarrier());
			detail.setTotalFare(record.getTotalFare());
			detail.setTotalFees(record.getTotalFees());
			detail.setTotalTaxes(record.getTotalTaxes());
			details.add(detail);
		}
		detailExtension.setPassengerTransportDetails(details);
	}

	private void createPassengerTransportLegDetails(ProcurementCardTransactionExtension extension, ProcurementCardTransactionDetailExtension detailExtension)
	{
		List<PassengerTransportLegDetail> details = new ArrayList<PassengerTransportLegDetail>();
		for (PassengerTransportLegRecord record : extension.getPassengerTransportLegRecords())
		{
			PassengerTransportLegDetail detail = new PassengerTransportLegDetail();
			detail.setDocumentNumber(detailExtension.getDocumentNumber());
			detail.setFinancialDocumentTransactionLineNumber(detailExtension.getFinancialDocumentTransactionLineNumber());
			detail.setTripLegNumber(record.getTripLegNumber());
			detail.setCarrierCode(record.getCarrierCode());
			detail.setServiceClass(record.getServiceClass());
			detail.setStopOverCode(record.getStopOverCode());
			detail.setCityOfOrigin(record.getCityOfOrigin());
			detail.setConjunctionTicket(record.getConjunctionTicket());
			detail.setTravelDate(record.getTravelDate());
			detail.setExchangeTicket(record.getExchangeTicket());
			detail.setCouponNumber(record.getCouponNumber());
			detail.setCityOfDestination(record.getCityOfDestination());
			detail.setFareBaseCode(record.getFareBaseCode());
			detail.setFlightNumber(record.getFlightNumber());
			detail.setDepartureTime(record.getDepartureTime());
			detail.setDepartureTimeSegment(record.getDepartureTimeSegment());
			detail.setArrivalTime(record.getArrivalTime());
			detail.setArrivalTimeSegment(record.getArrivalTimeSegment());
			detail.setFare(record.getFare());
			detail.setFee(record.getFee());
			detail.setTaxes(record.getTaxes());
			detail.setEndorsements(record.getEndorsements());
			details.add(detail);
		}
		detailExtension.setPassengerTransportLegDetails(details);
	}

	private void createLodgingDetails(ProcurementCardTransactionExtension extension, ProcurementCardTransactionDetailExtension detailExtension)
	{
		List<LodgingDetail> details = new ArrayList<LodgingDetail>();
		for (LodgingRecord record : extension.getLodgingRecords())
		{
			LodgingDetail detail = new LodgingDetail();
			detail.setDocumentNumber(detailExtension.getDocumentNumber());
			detail.setFinancialDocumentTransactionLineNumber(detailExtension.getFinancialDocumentTransactionLineNumber());
			detail.setArrivalDate(record.getArrivalDate());
			detail.setDepartureDate(record.getDepartureDate());
			detail.setFolioNumber(record.getFolioNumber());
			detail.setCustomerServiceNumber(record.getCustomerServiceNumber());
			detail.setRoomRate(record.getRoomRate());
			detail.setRoomTax(record.getRoomTax());
			detail.setProgramCode(record.getProgramCode());
			detail.setTelephoneCharges(record.getTelephoneCharges());
			detail.setRoomService(record.getRoomService());
			detail.setBarCharges(record.getBarCharges());
			detail.setGiftShopCharges(record.getGiftShopCharges());
			detail.setLaundryCharges(record.getLaundryCharges());
			detail.setOtherServicesCode(record.getOtherServicesCode());
			detail.setOtherServicesCharges(record.getOtherServicesCharges());
			detail.setBillingAdjustmentIndicator(record.getBillingAdjustmentIndicator());
			detail.setBillingAdjustmentAmount(record.getBillingAdjustmentAmount());
			detail.setPropertyPhoneNumber(record.getPropertyPhoneNumber());
			details.add(detail);
		}
		detailExtension.setLodgingDetails(details);
	}

	private void createRentalCarDetails(ProcurementCardTransactionExtension extension, ProcurementCardTransactionDetailExtension detailExtension)
	{
		List<RentalCarDetail> details = new ArrayList<RentalCarDetail>();
		for (RentalCarRecord record : extension.getRentalCarRecords())
		{
			RentalCarDetail detail = new RentalCarDetail();
			detail.setDocumentNumber(detailExtension.getDocumentNumber());
			detail.setFinancialDocumentTransactionLineNumber(detailExtension.getFinancialDocumentTransactionLineNumber());
			detail.setRentalAgreementNumber(record.getRentalAgreementNumber());
			detail.setRenterName(record.getRenterName());
			detail.setRentalReturnCity(record.getRentalReturnCity());
			detail.setRentalReturnState(record.getRentalReturnState());
			detail.setRentalReturnCountry(record.getRentalReturnCountry());
			detail.setRentalReturnDate(record.getRentalReturnDate());
			detail.setReturnLocationId(record.getReturnLocationId());
			detail.setCustomerServiceNumber(record.getCustomerServiceNumber());
			detail.setRentalClass(record.getRentalClass());
			detail.setDailyRentalRate(record.getDailyRentalRate());
			detail.setRatePerMile(record.getRatePerMile());
			detail.setTotalMiles(record.getTotalMiles());
			detail.setMaxFreeMiles(record.getMaxFreeMiles());
			detail.setInsuranceIndicator(record.getInsuranceIndicator());
			detail.setInsuranceCharges(record.getInsuranceCharges());
			detail.setAdjustedAmountIndicator(record.getAdjustedAmountIndicator());
			detail.setAdjustedAmount(record.getAdjustedAmount());
			detail.setProgramCode(record.getProgramCode());
			detail.setCheckoutDate(record.getCheckoutDate());
			detail.setRentalCarId(record.getRentalCarId());
			details.add(detail);
		}
		detailExtension.setRentalCarDetails(details);
	}

	private void createGenericDetails(ProcurementCardTransactionExtension extension, ProcurementCardTransactionDetailExtension detailExtension)
	{
		List<ProcurementCardGenericDetail> details = new ArrayList<ProcurementCardGenericDetail>();
		for (ProcurementCardGenericRecord record : extension.getPcardGenericRecords())
		{
			ProcurementCardGenericDetail detail = new ProcurementCardGenericDetail();
			detail.setDocumentNumber(detailExtension.getDocumentNumber());
			detail.setFinancialDocumentTransactionLineNumber(detailExtension.getFinancialDocumentTransactionLineNumber());
			detail.setGenericAddendumData(record.getGenericAddendumData());
			details.add(detail);
		}
		detailExtension.setPcardGenericDetails(details);
	}

	private void createFuelDetails(ProcurementCardTransactionExtension extension, ProcurementCardTransactionDetailExtension detailExtension)
	{
		List<FuelDetail> details = new ArrayList<FuelDetail>();
		for (FuelRecord record : extension.getFuelRecords())
		{
			FuelDetail detail = new FuelDetail();
			detail.setDocumentNumber(detailExtension.getDocumentNumber());
			detail.setFinancialDocumentTransactionLineNumber(detailExtension.getFinancialDocumentTransactionLineNumber());
			detail.setOilCompanyBrand(record.getOilCompanyBrand());
			detail.setMerchantStreetAddress(record.getMerchantStreetAddress());
			detail.setMerchantPostalCode(record.getMerchantPostalCode());
			detail.setTimeOfPurchase(record.getTimeOfPurchase());
			detail.setMotorFuelServiceType(record.getMotorFuelServiceType());
			detail.setMotorFuelProductCode(record.getMotorFuelProductCode());
			detail.setMotorFuelUnitPrice(record.getMotorFuelUnitPrice());
			detail.setMotorFuelUnitOfMeasure(record.getMotorFuelUnitOfMeasure());
			detail.setMotorFuelQuantity(record.getMotorFuelQuantity());
			detail.setMotorFuelSaleAmount(record.getMotorFuelSaleAmount());
			detail.setOdomoterReading(record.getOdomoterReading());
			detail.setVehicleNumber(record.getVehicleNumber());
			detail.setDriverNumber(record.getDriverNumber());
			detail.setMagneticStripeProductTypeCode(record.getMagneticStripeProductTypeCode());
			detail.setCouponDiscountAmount(record.getCouponDiscountAmount());
			detail.setTaxExemptAmount(record.getTaxExemptAmount());
			detail.setTaxAmount1(record.getTaxAmount1());
			detail.setTaxAmount2(record.getTaxAmount2());
			details.add(detail);
		}
		detailExtension.setFuelDetails(details);
	}

	private void createNonFuelDetails(ProcurementCardTransactionExtension extension, ProcurementCardTransactionDetailExtension detailExtension)
	{
		List<NonFuelDetail> details = new ArrayList<NonFuelDetail>();
		for (NonFuelRecord record : extension.getNonFuelRecords())
		{
			NonFuelDetail detail = new NonFuelDetail();
			detail.setDocumentNumber(detailExtension.getDocumentNumber());
			detail.setFinancialDocumentTransactionLineNumber(detailExtension.getFinancialDocumentTransactionLineNumber());
			detail.setItemProductCode(record.getItemProductCode());
			detail.setItemDescription(record.getItemDescription());
			detail.setItemQuantity(record.getItemQuantity());
			detail.setItemUnitOfMeasure(record.getItemUnitOfMeasure());
			detail.setExtendedItemAmount(record.getExtendedItemAmount());
			detail.setDiscountIndicator(record.getDiscountIndicator());
			detail.setDiscountAmount(record.getDiscountAmount());
			detail.setNetGrossIndicator(record.getNetGrossIndicator());
			detail.setTaxRateApplied(record.getTaxRateApplied());
			detail.setTaxTypeApplied(record.getTaxTypeApplied());
			detail.setTaxAmount(record.getTaxAmount());
			detail.setDebitCreditInd(record.getDebitCreditInd());
			detail.setAlternateTaxIdentifier(record.getAlternateTaxIdentifier());
			detail.setNonFuelId(record.getNonFuelId());
			details.add(detail);
		}
		detailExtension.setNonFuelDetails(details);
	}
	// KUP-189 Begin UConn Procurement Card Description (copy method from org)
    /**
     * Creates a ProcurementCardDocument from the List of transactions given.
     *
     * @param transactions List of ProcurementCardTransaction objects to be used for creating the document.
     * @return A ProcurementCardDocument populated with the transactions provided.
     */
    @Override
    protected ProcurementCardDocument createProcurementCardDocument(List transactions) {
        ProcurementCardDocument pcardDocument = null;

        try {
            // get new document from doc service
            pcardDocument = (ProcurementCardDocument) SpringContext.getBean(DocumentService.class).getNewDocument(PROCUREMENT_CARD);

            List<CapitalAssetInformation> capitalAssets = pcardDocument.getCapitalAssetInformation();
            for (CapitalAssetInformation capitalAsset : capitalAssets) {
                if (ObjectUtils.isNotNull(capitalAsset) && ObjectUtils.isNotNull(capitalAsset.getCapitalAssetInformationDetails())) {
                    capitalAsset.setDocumentNumber(pcardDocument.getDocumentNumber());
                }
            }

            ProcurementCardTransaction trans = (ProcurementCardTransaction) transactions.get(0);
            String errors = validateTransaction(trans);
            createCardHolderRecord(pcardDocument, trans);

            // for each transaction, create transaction detail object and then acct lines for the detail
            int transactionLineNumber = 1;
            KualiDecimal documentTotalAmount = KualiDecimal.ZERO;
            String errorText = "";
            for (Iterator iter = transactions.iterator(); iter.hasNext();) {
                ProcurementCardTransaction transaction = (ProcurementCardTransaction) iter.next();

                // create transaction detail record with accounting lines
                errorText += createTransactionDetailRecord(pcardDocument, transaction, transactionLineNumber);

                // update document total
                documentTotalAmount = documentTotalAmount.add(transaction.getFinancialDocumentTotalAmount());

                transactionLineNumber++;
            }

            pcardDocument.getFinancialSystemDocumentHeader().setFinancialDocumentTotalAmount(documentTotalAmount);
            // PCDO Default Description
            //pcardDocument.getDocumentHeader().setDocumentDescription("SYSTEM Generated");

            //UConn begin comment out org description logic and add edu description logic
            //setupDocumentDescription(pcardDocument);
            String documentDescription = createDescription((ProcurementCardTransaction) transactions.get(0));

            pcardDocument.getDocumentHeader().setDocumentDescription(documentDescription);
            //UConn end comment out org description logic and add edu description logic

            // Remove duplicate messages from errorText
            String messages[] = StringUtils.split(errorText, ".");
            for (int i = 0; i < messages.length; i++) {
                int countMatches = StringUtils.countMatches(errorText, messages[i]) - 1;
                errorText = StringUtils.replace(errorText, messages[i] + ".", "", countMatches);
            }
            // In case errorText is still too long, truncate it and indicate so.
            Integer documentExplanationMaxLength = dataDictionaryService.getAttributeMaxLength(DocumentHeader.class.getName(), KFSPropertyConstants.EXPLANATION);
            if (documentExplanationMaxLength != null && errorText.length() > documentExplanationMaxLength.intValue()) {
                String truncatedMessage = " ... TRUNCATED.";
                errorText = errorText.substring(0, documentExplanationMaxLength - truncatedMessage.length()) + truncatedMessage;
            }
            pcardDocument.getDocumentHeader().setExplanation(errorText);
        }
        catch (WorkflowException e) {
            LOG.error("Error creating pcdo documents: " + e.getMessage(),e);
            throw new RuntimeException("Error creating pcdo documents: " + e.getMessage(),e);
        }

        return pcardDocument;

    }

  //Begin Add UConn format description logic
  //*KUP-189  decrease Cardholder name by two (to 13) so that a six figure total amount can be displayed...

    public String createDescription(ProcurementCardTransaction procurementCardTransaction) {
        String description = "";
        description += procurementCardTransaction.getTransactionCreditCardNumber().substring(procurementCardTransaction.getTransactionCreditCardNumber().length()-4);
        description += "/";
        description += (procurementCardTransaction.getCardHolderName().length()>13?procurementCardTransaction.getCardHolderName().substring(0,13):procurementCardTransaction.getCardHolderName());
        description += "/";
        description += (procurementCardTransaction.getVendorName().length()>10?procurementCardTransaction.getVendorName().substring(0,10):procurementCardTransaction.getVendorName());
        description += "/";
        description += procurementCardTransaction.getFinancialDocumentTotalAmount().toString();
        return description;
    }
  //End Add UConn format description logic
 // KUP-189 End UConn Procurement Card Description
}
