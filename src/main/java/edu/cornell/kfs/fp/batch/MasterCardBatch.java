/*
 * Copyright 2012 The Kuali Foundation.
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
package edu.cornell.kfs.fp.batch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.sys.Message;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.CuFPPropertyConstants;
import edu.cornell.kfs.fp.businessobject.ProcurementCardHolderDetail;
import edu.cornell.kfs.fp.businessobject.MasterCardHolderDetail;
import edu.cornell.kfs.fp.businessobject.MasterCardTransactionDetail;
import edu.cornell.kfs.fp.businessobject.MasterCardTransactionDetailAddendum1;
import edu.cornell.kfs.fp.businessobject.MasterCardTransactionDetailAddendum11;
import edu.cornell.kfs.fp.businessobject.MasterCardTransactionDetailAddendum2;
import edu.cornell.kfs.fp.businessobject.MasterCardTransactionDetailAddendum21;
import edu.cornell.kfs.fp.businessobject.MasterCardTransactionDetailAddendum3;
import edu.cornell.kfs.fp.businessobject.MasterCardTransactionDetailAddendum4;
import edu.cornell.kfs.fp.businessobject.MasterCardTransactionDetailAddendum5;
import edu.cornell.kfs.fp.businessobject.MasterCardTransactionDetailAddendum6;
import edu.cornell.kfs.fp.businessobject.MasterCardTransactionDetailAddendum61;
import edu.cornell.kfs.fp.businessobject.MasterCardTransactionDetailAddendum7;
import edu.cornell.kfs.fp.businessobject.ProcurementCardUploadDocument;

/**
 * This is the main class for processing the master card batch file.
 * @author Dave Raines
 * @version $Revision$
 */
public class MasterCardBatch
{
	private static Logger LOG = Logger.getLogger(MasterCardBatch.class);
	private Map<String, MasterCardHolderDetail> mcCardHolderData;
	private List<MasterCardTransactionDetail> mcTransactionData;
	private List<ProcurementCardUploadDocument> transactionList;
	private List<String> adminCardNumbers;
	private BusinessObjectService businessObjectService;

	private MasterCardHolderDetailFieldUtil mchDetailFieldUtil;
	private MasterCardTransactionDetailFieldUtil mctDetailFieldUtil;

	private MasterCardStatusAndErrorsData statusAndErrors;
	
	private MasterCardTransactionDetail currentTransaction = null;

	public MasterCardBatch()
	{
		mcCardHolderData = new HashMap<String, MasterCardHolderDetail>();
		mcTransactionData = new ArrayList<MasterCardTransactionDetail>();
		transactionList = new ArrayList<ProcurementCardUploadDocument>();
		statusAndErrors = new MasterCardStatusAndErrorsData();
	}

	public int getTransactionCount()
	{
		return mcTransactionData.size();
	}

	/**
	 * @param inputLine line from the master card flat file
	 */
	public void parseLine(String inputLine)
	{
		String recordType = getRecordType(inputLine);
		if (recordType.equals(CuFPPropertyConstants.RECORD_TYPE_4300))
		{
			// process 4300 record
			MasterCardHolderDetail mcHolderDetail = new MasterCardHolderDetail();
			mcHolderDetail.parseInput(inputLine);
			String creditCardNumber = mcHolderDetail.getCreditCardNumber();
			if (!isAdminCreditCard(creditCardNumber))
			{
				mcCardHolderData.put(creditCardNumber, mcHolderDetail);
			}
			statusAndErrors.incrementCardHolderInputRecords();
		}
		else if (recordType.equals(CuFPPropertyConstants.RECORD_TYPE_5000))
		{
			// process 5000 record
			String addendumType = getAddendumType(inputLine);
			if (addendumType.equals(CuFPPropertyConstants.ADDENDUM_TYPE_0))
			{
				MasterCardTransactionDetail transDetail = new MasterCardTransactionDetail();
				transDetail.parseInput(inputLine);

				String creditCardNumber = transDetail.getCreditCardNumber();
				if (!isAdminCreditCard(creditCardNumber))
				{
                    mcTransactionData.add(transDetail);
				    currentTransaction = transDetail;
				}
				statusAndErrors.incrementProCardTransactionInputRecords();
			}
            else if (addendumType.equals(CuFPPropertyConstants.ADDENDUM_TYPE_1))
            {
                MasterCardTransactionDetailAddendum1 transDetail = new MasterCardTransactionDetailAddendum1();
                transDetail.parseInput(inputLine);

                if (currentTransaction != null) {
                    currentTransaction.addAddendum1Details(transDetail);
                }

            }
            else if (addendumType.equals(CuFPPropertyConstants.ADDENDUM_TYPE_11))
            {
                MasterCardTransactionDetailAddendum11 transDetail = new MasterCardTransactionDetailAddendum11();
                transDetail.parseInput(inputLine);

                if (currentTransaction != null) {
                    currentTransaction.addAddendum11Details(transDetail);
                }
            }
            else if (addendumType.equals(CuFPPropertyConstants.ADDENDUM_TYPE_2))
            {
                MasterCardTransactionDetailAddendum2 transDetail = new MasterCardTransactionDetailAddendum2();
                transDetail.parseInput(inputLine);

                if (currentTransaction != null) {
                    currentTransaction.addAddendum2Details(transDetail);
                }

            }
            else if (addendumType.equals(CuFPPropertyConstants.ADDENDUM_TYPE_21))
            {
                MasterCardTransactionDetailAddendum21 transDetail = new MasterCardTransactionDetailAddendum21();
                transDetail.parseInput(inputLine);

                if (currentTransaction != null) {
                    currentTransaction.addAddendum21Details(transDetail);
                }
            }
            else if (addendumType.equals(CuFPPropertyConstants.ADDENDUM_TYPE_3))
            {
                MasterCardTransactionDetailAddendum3 transDetail = new MasterCardTransactionDetailAddendum3();
                transDetail.parseInput(inputLine);

                if (currentTransaction != null) {
                    currentTransaction.addAddendum3Details(transDetail);
                }
            }
            else if (addendumType.equals(CuFPPropertyConstants.ADDENDUM_TYPE_4))
            {
                MasterCardTransactionDetailAddendum4 transDetail = new MasterCardTransactionDetailAddendum4();
                transDetail.parseInput(inputLine);

                if (currentTransaction != null) {
                    currentTransaction.addAddendum4Details(transDetail);
                }
            }
            else if (addendumType.equals(CuFPPropertyConstants.ADDENDUM_TYPE_5))
            {
                MasterCardTransactionDetailAddendum5 transDetail = new MasterCardTransactionDetailAddendum5();
                transDetail.parseInput(inputLine);

                if (currentTransaction != null) {
                    currentTransaction.addAddendum5Details(transDetail);
                }
            }
            else if (addendumType.equals(CuFPPropertyConstants.ADDENDUM_TYPE_6))
            {
                MasterCardTransactionDetailAddendum6 transDetail = new MasterCardTransactionDetailAddendum6();
                transDetail.parseInput(inputLine);

                if (currentTransaction != null) {
                    currentTransaction.addAddendum6Details(transDetail);
                }
            }
            else if (addendumType.equals(CuFPPropertyConstants.ADDENDUM_TYPE_61))
            {
                MasterCardTransactionDetailAddendum61 transDetail = new MasterCardTransactionDetailAddendum61();
                transDetail.parseInput(inputLine);

                if (currentTransaction != null) {
                    currentTransaction.addAddendum61Details(transDetail);
                }
            }
			else if (addendumType.equals(CuFPPropertyConstants.ADDENDUM_TYPE_7))
			{
				MasterCardTransactionDetailAddendum7 transDetail = new MasterCardTransactionDetailAddendum7();
				transDetail.parseInput(inputLine);

				// get the addendum type 0 record and set merchantAddress into it
				// then throw away the addendum 7 record as it is no longer needed
				if (currentTransaction != null)
				{
				    currentTransaction.setMerchantAddress(transDetail.getMerchantAddress());
				    currentTransaction.setCorporationName(transDetail.getCorporationName());
				}
			}
		}
	}

	/**
	 * Reads the card holder records, validates that the account number exists and persists the data.
	 */
	public void processMasterCardData()
	{
		int validCardHolderCounter = 0;
		int invalidAccountCounter = 0;

		for (MasterCardHolderDetail cardHolderDetail : mcCardHolderData.values())
		{
			// create procurementCardHolderDetail
			String creditCardNumber = cardHolderDetail.getCreditCardNumber();

			ProcurementCardHolderDetail procurementCardHolderDetail = lookupCardHolderDetail(creditCardNumber);
			cardHolderDetail.populateCardHolderObject(procurementCardHolderDetail);

			if (validAccountNumber(procurementCardHolderDetail))
			{
				// Save procurementCardHolderDetail
				// Card numbers are automatically encrypted by kuali encryption service during ORM.
				businessObjectService.save(procurementCardHolderDetail);
				validCardHolderCounter++;
				statusAndErrors.incrementCardHoldersProcessed();
			}
			else
			{
				String cardNumber = procurementCardHolderDetail.getCreditCardNumber();
				String cardNumberLast6 = getCreditCardNumberSuffix(cardNumber);
				String name = procurementCardHolderDetail.getCardHolderName();
				String account = procurementCardHolderDetail.getAccountNumber();
				LOG.error("Mastercard 4300 record for card number " + cardNumberLast6 + " and card holder name " + name + " has invalid account number " + account + ".");
				invalidAccountCounter++;

				statusAndErrors.incrementCardHoldersInError();
				statusAndErrors.addCardHolderError(cardNumberLast6, name, account);
			}
		}
		LOG.info("Processed " + validCardHolderCounter + " valid card holder records. " + invalidAccountCounter + " records had invalid account numbers.");
	}

	/**
	 * 
	 */
	public void buildProcurementDocument()
	{
		int invalidTransCounter = 0;
		LOG.info("Trans data has " + mcTransactionData.size() + " objects.");

		for (MasterCardTransactionDetail transDetail : mcTransactionData)
		{
			ProcurementCardUploadDocument procurementCardData = createProcurementCardUpload(transDetail);

			// There should always be card holder data for every 5000 record type (card holder transaction).
			// If it doesn't exist then the input record is in error!
			String cardNumber = procurementCardData.getTransactionCreditCardNumber();
			if (StringUtils.isNotBlank(cardNumber))
			{
				this.transactionList.add(procurementCardData);
				statusAndErrors.incrementProCardTransactionsProcessed();
				statusAndErrors.incrementTotalAmount(transDetail);
			}
			else
			{
				LOG.error("MasterCard 5000 record with Card number " + transDetail.getMaskedTransactionCreditCardNumber() + " does not exist in the card holder detail table.");
				invalidTransCounter++;

				statusAndErrors.incrementProCardTransactionsInError();
				statusAndErrors.addProCardTransactionError(transDetail.getMaskedTransactionCreditCardNumber(), procurementCardData.getFinancialDocTotalAmountAsKualiDecimal());
			}
		}
		LOG.info("Processed " + transactionList.size() + " valid transaction records. " + invalidTransCounter + " transaction records in error.");
	}

	/**
	 * Generates an XML file from the map of transactions. Each map entry is one transaction element in the file.
	 * 
	 * @param masterCardOutputXmlFileType
	 * @return file name of the output XML file
	 * @throws IOException
	 */
	public String writeProcurementDocument(MasterCardOutputXmlFileType masterCardOutputXmlFileType) throws IOException
	{
		String xmlFileName = "";

		if (!transactionList.isEmpty())
		{
			xmlFileName = masterCardOutputXmlFileType.writeXmlDocument(transactionList);
		}

		return xmlFileName;
	}

	/**
	 * Sets the BusinessObjectService
	 */
	public void setBusinessObjectService(BusinessObjectService businessObjectService)
	{
		this.businessObjectService = businessObjectService;
	}

	/**
	 * Sets the adminCardNumbers list of credit cards to skip
	 * 
	 * @param adminCardNumbers
	 */
	public void setAdminCardNumbers(List<String> adminCardNumbers)
	{
		this.adminCardNumbers = adminCardNumbers;
	}

	/**
	 * Exposes PCard transaction list
	 */
	public List<ProcurementCardUploadDocument> getTransactionList()
	{
		return transactionList;
	}

	/**
	 * Returns the statusAndErrors object
	 */
	public MasterCardStatusAndErrorsData getStatusAndErrors()
	{
		return statusAndErrors;
	}

	/**
	 * Parses the input line for all records to retrieve the record type field.
	 * 
	 * @param line
	 * @return
	 */
	private String getRecordType(String line)
	{
		MasterCardHolderDetailFieldUtil fieldUtil = getMasterCardHolderDetailFieldUtil();
		final Map<String, Integer> pMap = fieldUtil.getFieldBeginningPositionMap();
		int recordTypeStart = pMap.get(CuFPPropertyConstants.RECORD_TYPE);
		int recordTypeEnd = pMap.get(CuFPPropertyConstants.FILLER1);
		String recordType = StringUtils.substring(line, recordTypeStart, recordTypeEnd);
		return recordType;
	}

	/**
	 * Parses the input line for 5000 record types to retrieve the addendum type field.
	 * 
	 * @param line
	 * @return
	 */
	private String getAddendumType(String line)
	{
		MasterCardTransactionDetailFieldUtil fieldUtil = new MasterCardTransactionDetailFieldUtil();
		final Map<String, Integer> pMap = fieldUtil.getFieldBeginningPositionMap();
		int addendumTypeStart = pMap.get(CuFPPropertyConstants.ADDENDUM_TYPE);
		int addendumTypeEnd = pMap.get(CuFPPropertyConstants.FILLER2);
		String addendumType = StringUtils.substring(line, addendumTypeStart, addendumTypeEnd).trim();
		return addendumType;
	}

	/**
	 * Initializes MasterCardHolderDetailFieldUtil object.
	 * 
	 * @return MasterCardHolderDetailFieldUtil instance
	 */
	private MasterCardHolderDetailFieldUtil getMasterCardHolderDetailFieldUtil()
	{
		if (mchDetailFieldUtil == null)
		{
			mchDetailFieldUtil = new MasterCardHolderDetailFieldUtil();
		}
		return mchDetailFieldUtil;
	}

	/**
	 * Initializes MasterCardTransactionDetailFieldUtil object.
	 * 
	 * @return MasterCardTransactionDetailFieldUtil instance.
	 */
	private MasterCardTransactionDetailFieldUtil getMasterCardTransactionDetailFieldUtil()
	{
		if (mctDetailFieldUtil == null)
		{
			mctDetailFieldUtil = new MasterCardTransactionDetailFieldUtil();
		}
		return mctDetailFieldUtil;
	}

	/**
	 * returns true if this credit card transaction should be skipped
	 * 
	 * @param creditCardNumber
	 * @return true if card number is in property list
	 */
	private boolean isAdminCreditCard(String creditCardNumber)
	{
		return adminCardNumbers.contains(creditCardNumber);
	}

	/**
	 * Looks up ProcurementCardHolderDetail object in database and returns it if found, otherwise returns null.
	 * 
	 * @param creditCardNumber
	 * @return
	 */
	private ProcurementCardHolderDetail lookupCardHolderDetail(String creditCardNumber)
	{
		Map<String, String> pkMap = new HashMap<String, String>();
		pkMap.put("creditCardNumber", creditCardNumber);
		ProcurementCardHolderDetail procurementCardHolderDetail = (ProcurementCardHolderDetail) businessObjectService.findByPrimaryKey(ProcurementCardHolderDetail.class, pkMap);

		if (procurementCardHolderDetail == null)
		{
			procurementCardHolderDetail = new ProcurementCardHolderDetail();
		}

		return procurementCardHolderDetail;
	}

	/**
	 * Validates that account number from the 4300 record is a valid account number that already exists in the system.
	 * 
	 * @param procurementCardHolderDetail
	 * @return true if account number exists, and false if not
	 */
	private boolean validAccountNumber(ProcurementCardHolderDetail procurementCardHolderDetail)
	{
		Map<String, String> pkMap = new HashMap<String, String>();
		pkMap.put(CuFPPropertyConstants.BATCH_ACCT_NBR, procurementCardHolderDetail.getAccountNumber());
        pkMap.put(CuFPPropertyConstants.COA_CODE, procurementCardHolderDetail.getChartOfAccountsCode());
		Account account = (Account) businessObjectService.findByPrimaryKey(Account.class, pkMap);
		return (account != null);
	}

	/**
	 * Returns a String containing asterisks followed by last 4 digits of the credit card number.
	 * 
	 * @param cardNumber
	 * @return last 4 digits of card number
	 */
	private String getCreditCardNumberSuffix(String cardNumber)
	{
		String suffix = cardNumber.substring(cardNumber.length() - 4);
		return "**********" + suffix;
	}

	/**
	 * Process the input for the record type 5000. Generates and updates the ProcurementCardUploadDocument which holds all of the
	 * data for this transaction.
	 * 
	 * @param line the input line from the input master card file
	 * @return ProcurementCardUploadDocument holding data for this credit card number
	 */
	private ProcurementCardUploadDocument createProcurementCardUpload(MasterCardTransactionDetail transDetail)
	{
		ProcurementCardUploadDocument procurementCardData = new ProcurementCardUploadDocument();
		String creditCardNumber = transDetail.getCreditCardNumber();
		ProcurementCardHolderDetail procurementCardHolderDetail = lookupCardHolderDetail(creditCardNumber);

		procurementCardData.setCardHolder(procurementCardHolderDetail);
		procurementCardData.setTransactionRefNumber(transDetail.getTransactionRefNumber());
		procurementCardData.setDebitCreditCode(transDetail.getDebitCreditInd());
		procurementCardData.setFinancialDocTotalAmount(transDetail.getTransactionAmount());
		procurementCardData.setOrigCurrencyAmount(transDetail.getOrigCurrencyAmount());
		procurementCardData.setOrigCurrencyCode(transDetail.getOrigCurrencyCode());
		procurementCardData.setTransactionDate(transDetail.getTransactionDate());
		procurementCardData.setTransMerchantCategoryCode(transDetail.getCategoryCode());
		procurementCardData.setTransPostingDate(transDetail.getPostingDate());
		procurementCardData.setTransSalesTaxAmount(transDetail.getSalesTaxAmount());
		procurementCardData.setVendorName(transDetail.getMerchantName());
		procurementCardData.setVendorCity(transDetail.getMerchantCity());
		procurementCardData.setVendorState(transDetail.getMerchantState());
		procurementCardData.setVendorAddress1(transDetail.getMerchantAddress());
		
        procurementCardData.setCustomerCode(transDetail.getCustomerCode());

        procurementCardData.setPcardDetails(transDetail.getAddendum1Details());
        procurementCardData.setPcardUserAmountDetails(transDetail.getAddendum11Details());
        procurementCardData.setPassengerTransportDetails(transDetail.getAddendum2Details());
        procurementCardData.setPassengerTransportLegDetails(transDetail.getAddendum21Details());
        procurementCardData.setLodgingDetails(transDetail.getAddendum3Details());
        procurementCardData.setRentalCarDetails(transDetail.getAddendum4Details());
        procurementCardData.setGenericDetails(transDetail.getAddendum5Details());
        procurementCardData.setFuelDetails(transDetail.getAddendum6Details());
        procurementCardData.setNonFuelDetails(transDetail.getAddendum61Details());

		return procurementCardData;
	}

}
