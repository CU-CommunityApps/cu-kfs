/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2014 The Kuali Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cornell.kfs.paymentworks.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpParameterConstants;
import org.kuali.kfs.pdp.batch.ExtractAchPaymentsStep;
import org.kuali.kfs.pdp.businessobject.ACHBank;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.pdp.service.AchBankService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.rules.rule.event.RouteDocumentEvent;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.MailService;
import org.kuali.kfs.krad.service.MaintenanceDocumentService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.paymentworks.PaymentWorksConstants;
import edu.cornell.kfs.paymentworks.batch.PaymentWorksRetrieveNewVendorStep;
import edu.cornell.kfs.paymentworks.batch.PaymentWorksUploadSuppliersStep;
import edu.cornell.kfs.paymentworks.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.paymentworks.service.PaymentWorksKfsService;
import edu.cornell.kfs.paymentworks.util.PaymentWorksAchConversionUtil;
import edu.cornell.kfs.paymentworks.util.PaymentWorksNewVendorConversionUtil;
import edu.cornell.kfs.paymentworks.util.PaymentWorksUtil;
import edu.cornell.kfs.paymentworks.util.PaymentWorksVendorUpdateConversionUtil;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksFieldChangeDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksVendorUpdatesDTO;
import edu.cornell.kfs.sys.service.MailMessage;

@Transactional
public class PaymentWorksKfsServiceImpl implements PaymentWorksKfsService {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(PaymentWorksKfsServiceImpl.class);

	private DocumentService documentService;
	private MailService mailService;
	private AchBankService achBankService;
	private ParameterService parameterService;
	private VendorService vendorService;
	private BusinessObjectService businessObjectService;

	@Override
	public boolean routeNewVendor(PaymentWorksVendor paymentWorksVendor) {
		boolean isRouted = false;

		// create vendor
		VendorDetail vendorDetail = new PaymentWorksNewVendorConversionUtil().createVendorDetail(paymentWorksVendor);

		// Route Maintenance document
		MaintenanceDocument vendorMaintDoc = null;
		try {
			vendorMaintDoc = (MaintenanceDocument) documentService.getNewDocument("PVEN");
			vendorMaintDoc.getDocumentHeader().setDocumentDescription(
					StringUtils.defaultString(paymentWorksVendor.getRequestingCompanyName(), ""));
			vendorMaintDoc.getDocumentHeader().setExplanation(paymentWorksVendor.getRequestingCompanyDesc());
			vendorMaintDoc.getOldMaintainableObject().setBusinessObject(new VendorDetail());
			vendorMaintDoc.getNewMaintainableObject().setBusinessObject(vendorDetail);
			vendorMaintDoc.getNewMaintainableObject().setMaintenanceAction(KFSConstants.MAINTENANCE_NEW_ACTION);

			boolean isValid = true;
			try {
				vendorMaintDoc.validateBusinessRules(new RouteDocumentEvent(vendorMaintDoc));
			} catch (ValidationException ve) {
				LOG.error("Failed to route Vendor document due to business rule error(s): " + new PaymentWorksUtil()
						.getAutoPopulatingErrorMessages(GlobalVariables.getMessageMap().getErrorMessages()));
				isValid = false;
			}

			if (isValid) {
				// add note
				// addNoteForNewVendorResponsesToVendor(vendorMaintDoc.getNewMaintainableObject());

				// route document
				documentService.routeDocument(vendorMaintDoc, "", new ArrayList());

				isRouted = true;

				// add document number
				paymentWorksVendor.setDocumentNumber(vendorMaintDoc.getDocumentNumber());
			}
		} catch (Exception e) {
			LOG.error("Failed to route Vendor document due to error(s): " + e.getMessage());
		} finally {
			// clear errors
			// GlobalVariables.getMessageMap().clearErrorMessages();
		}

		return isRouted;
	}

	@Override
	public boolean routeVendorEdit(PaymentWorksVendor paymentWorksVendor) {
		boolean isRouted = false;
		PaymentWorksVendorUpdateConversionUtil paymentWorksVendorUpdateConversionUtil = new PaymentWorksVendorUpdateConversionUtil();

		String vendorNumberList = paymentWorksVendor.getVendorNumberList();
		List<String> vendorNumbers = Arrays.asList(vendorNumberList.split("\\s*,\\s*"));
		StringBuffer documentNumbers = new StringBuffer("");

		// loop through vendors
		for (String vendorNumber : vendorNumbers) {

			// get two copies of vendor, for old and new. Deep copy has weird
			// consequences
			VendorDetail oldVendor = vendorService.getVendorDetail(vendorNumber);
			VendorDetail newVendor = (VendorDetail) ObjectUtils.deepCopy(oldVendor);

			if (!paymentWorksVendorUpdateConversionUtil.duplicateFieldsOnVendor(newVendor, paymentWorksVendor)) {

				// create vendor for edit
				VendorDetail vendorDetail = new PaymentWorksVendorUpdateConversionUtil()
						.createVendorDetailForEdit(newVendor, oldVendor, paymentWorksVendor);

				// Route Maintenance document
				MaintenanceDocument vendorMaintDoc = null;
				try {
					vendorMaintDoc = (MaintenanceDocument) documentService.getNewDocument("PVEN");
					vendorMaintDoc.getDocumentHeader()
							.setDocumentDescription(StringUtils.defaultString(paymentWorksVendor.getVendorName()));
					vendorMaintDoc.getOldMaintainableObject().setBusinessObject(oldVendor);
					vendorMaintDoc.getNewMaintainableObject().setBusinessObject(vendorDetail);
					vendorMaintDoc.getNewMaintainableObject()
							.setMaintenanceAction(KFSConstants.MAINTENANCE_EDIT_ACTION);
					vendorMaintDoc.getNewMaintainableObject().setDocumentNumber(vendorMaintDoc.getDocumentNumber());

					boolean isVendorLocked = checkForLockingDocument(vendorMaintDoc);

					if (!isVendorLocked) {
						boolean isValid = true;
						try {
							vendorMaintDoc.validateBusinessRules(new RouteDocumentEvent(vendorMaintDoc));
						} catch (ValidationException ve) {
							LOG.error("Failed to route Vendor Edit document due to business rule error(s): "
									+ new PaymentWorksUtil().getAutoPopulatingErrorMessages(
											GlobalVariables.getMessageMap().getErrorMessages()));
							isValid = false;
						}

						if (isValid) {
							// add note
							// addNoteForNewVendorResponsesToVendor(vendorMaintDoc.getNewMaintainableObject());

							// route document
							documentService.routeDocument(vendorMaintDoc, null, null);

							isRouted = true;

							// add document number
							documentNumbers.append(vendorMaintDoc.getDocumentNumber() + ",");
						}
					} else {
						LOG.error("Failed to route Vendor Edit document due to lock");
						GlobalVariables.getMessageMap().putError("vendorDetail", KFSKeyConstants.ERROR_CUSTOM,
								"Failed to route Vendor Edit document due to lock");
						isRouted = false;
					}
				} catch (Exception e) {
					LOG.error("Failed to route Vendor document due to error(s): " + e.getMessage());
					isRouted = false;
				}
			} else {
				GlobalVariables.getMessageMap().putError("vendorDetail", KFSKeyConstants.ERROR_CUSTOM,
						"Duplicate vendor update detected");
				isRouted = false;
			}
		}

		if (documentNumbers.length() > 0) {
			paymentWorksVendor
					.setDocumentNumberList(documentNumbers.toString().substring(0, documentNumbers.length() - 1));
		}
		return isRouted;
	}

	@Override
	public boolean directVendorEdit(PaymentWorksVendor paymentWorksVendor) {

		boolean success = true;
		PaymentWorksVendorUpdateConversionUtil paymentWorksVendorUpdateConversionUtil = new PaymentWorksVendorUpdateConversionUtil();

		String vendorNumberList = paymentWorksVendor.getVendorNumberList();
		List<String> vendorNumbers = Arrays.asList(vendorNumberList.split("\\s*,\\s*"));

		// loop through vendors
		for (String vendorNumber : vendorNumbers) {

			// retrieve vendor from kfs
			VendorDetail vendorDetail = vendorService.getVendorDetail(vendorNumber);

			if (ObjectUtils.isNotNull(vendorDetail)) {
				if (!paymentWorksVendorUpdateConversionUtil.duplicateFieldsOnVendor(vendorDetail, paymentWorksVendor)) {
					// set the necessary fields
					vendorDetail = paymentWorksVendorUpdateConversionUtil.createVendorDetailForEdit(vendorDetail, null,
							paymentWorksVendor);

					// save to DB, no document
					try {
						businessObjectService.save(vendorDetail);
					} catch (Exception e) {
						GlobalVariables.getMessageMap().putError("vendorDetail", KFSKeyConstants.ERROR_CUSTOM,
								e.getCause().getMessage());
						success = false;
						break;
					}
				} else {
					GlobalVariables.getMessageMap().putError("vendorDetail", KFSKeyConstants.ERROR_CUSTOM,
							"Duplicate vendor update detected");
					success = false;
				}
			}
		}

		return success;
	}

	@Override
	public boolean directAchEdit(PaymentWorksVendorUpdatesDTO vendorUpdate, String vendorNumberList) {

		boolean isSuccessful = false;

		String routingNumber = getRoutingNumber(vendorUpdate.getField_changes().getField_changes());
		String accountNumber = getAccountNumber(vendorUpdate.getField_changes().getField_changes());

		// check if bank exists
		if (StringUtils.isNotEmpty(routingNumber)) {
			ACHBank achBank = achBankService.getByPrimaryId(routingNumber);

			if (ObjectUtils.isNull(achBank)) {
				GlobalVariables.getMessageMap().putError("payeeAchAccount", KFSKeyConstants.ERROR_CUSTOM,
						"ACH Bank does not exist for routing number");
				return isSuccessful;
			}
		}

		// check if we do not have at least routing or account number
		// if not, throw an error
		if (StringUtils.isEmpty(routingNumber) && StringUtils.isEmpty(accountNumber)) {
			GlobalVariables.getMessageMap().putError("payeeAchAccount", KFSKeyConstants.ERROR_CUSTOM,
					"Routing number or account number required");
			return isSuccessful;
		}

		List<String> vendorNumbers = Arrays.asList(vendorNumberList.split("\\s*,\\s*"));

		// loop through vendors
		for (String vendorNumber : vendorNumbers) {

			PayeeACHAccount payeeAchAccount = this.getActivePayeeAchAccount(PdpConstants.PayeeIdTypeCodes.VENDOR_ID,
					vendorNumber, PdpConstants.DisbursementTypeCodes.ACH);

			// if existing, deactivate
			if (ObjectUtils.isNotNull(payeeAchAccount)) {
				payeeAchAccount.setActive(false);
				try {
					payeeAchAccount = businessObjectService.save(payeeAchAccount);
				} catch (Exception e) {
					GlobalVariables.getMessageMap().putError("payeeAchAccount", KFSKeyConstants.ERROR_CUSTOM,
							e.getCause().getMessage());
					isSuccessful = false;
					break;
				}
			}

			PayeeACHAccount payeeAchAccountNew = new PayeeACHAccount();

			// check if no existing ach account, and dont' have both fields
			// coming in (can't create a new record)
			if (ObjectUtils.isNull(payeeAchAccount)
					&& (StringUtils.isEmpty(routingNumber) || StringUtils.isEmpty(accountNumber))) {
				GlobalVariables.getMessageMap().putError("payeeAchAccount", KFSKeyConstants.ERROR_CUSTOM,
						"No existing Payee ACH Account, routing number and account number required");
				return isSuccessful;
			}

			// if we do not have an existing payee ach account by this time,
			// create new
			if (ObjectUtils.isNull(payeeAchAccount)) {
				payeeAchAccountNew = new PaymentWorksAchConversionUtil().createPayeeAchAccount(vendorUpdate,
						vendorNumber);
			} else {
				// if we do have a payee ach account, deep copy, and copy over
				// the fields
				payeeAchAccountNew = new PaymentWorksAchConversionUtil().createPayeeAchAccount(payeeAchAccount,
						routingNumber, accountNumber);
			}

			// save payee ach account
			try {
				payeeAchAccountNew = businessObjectService.save(payeeAchAccountNew);
			} catch (Exception e) {
				GlobalVariables.getMessageMap().putError("payeeAchAccount", KFSKeyConstants.ERROR_CUSTOM,
						e.getCause().getMessage());

				// roll back
				if (ObjectUtils.isNotNull(payeeAchAccount)) {
					payeeAchAccount.setActive(true);
					payeeAchAccount = businessObjectService.save(payeeAchAccount);
				}
				isSuccessful = false;
				break;
			}
			isSuccessful = true;
		}

		return isSuccessful;
	}

	@Override
	public void sendEmailVendorInitiated(String documentNumber, String vendorName, String contactEmail) {

		String fromAddress = parameterService.getParameterValueAsString(PaymentWorksRetrieveNewVendorStep.class,
				PaymentWorksConstants.EmailParameters.PAYMENT_WORKS_VENDOR_INITIATED_EMAIL);
		String subject = parameterService.getParameterValueAsString(PaymentWorksRetrieveNewVendorStep.class,
				PaymentWorksConstants.EmailParameters.PAYMENT_WORKS_VENDOR_INITIATED_EMAIL_SUBJECT);

		MailMessage message = new MailMessage();

		message.setFromAddress(fromAddress);
		message.getToAddresses().add(contactEmail);
		message.setSubject(subject);

		StringBuffer body = new StringBuffer("");
		body.append("Hello,\n");
		body.append(vendorName);
		body.append(" has submitted their application to become a vendor with the University of Connecticut. ");
		body.append("The corresponding edoc number is ");
		body.append(documentNumber);
		body.append(". Please contact the Vendor Coordinator at ");
		body.append(fromAddress);
		body.append(" only if you are not working with this vendor.\n\n");
		body.append("Thank you,\n");
		body.append("Accounts Payable");

		message.setMessage(body.toString());

		try {
			mailService.sendMessage(message);
		} catch (Exception e) {
			LOG.error("Failed to send vendor initiated email: " + e + "\n" + body.toString());
		}
	}

	@Override
	public void sendEmailVendorApproved(String documentNumber, String vendorNumber, String vendorName,
			String contactEmail) {

		String fromAddress = parameterService.getParameterValueAsString(PaymentWorksUploadSuppliersStep.class,
				PaymentWorksConstants.EmailParameters.PAYMENT_WORKS_VENDOR_APPROVED_EMAIL);
		String subject = parameterService.getParameterValueAsString(PaymentWorksUploadSuppliersStep.class,
				PaymentWorksConstants.EmailParameters.PAYMENT_WORKS_VENDOR_APPROVED_EMAIL_SUBJECT);

		MailMessage message = new MailMessage();

		message.setFromAddress(fromAddress);
		message.getToAddresses().add(contactEmail);
		message.setSubject(subject);

		StringBuffer body = new StringBuffer("");
		body.append("Hello,\n");
		body.append("Edoc number ");
		body.append(documentNumber);
		body.append(" has been approved. ");
		body.append(vendorName);
		body.append(" is now a vendor with the University of Connecticut. The new vendor number is ");
		body.append(vendorNumber);
		body.append(".\n\n");
		body.append("Thank you,\n");
		body.append("Accounts Payable");

		message.setMessage(body.toString());

		try {
			mailService.sendMessage(message);
		} catch (Exception e) {
			LOG.error("Failed to send vendor approved email: " + e + "\n" + body.toString());
		}

	}

	@Override
	public void sendSummaryEmail(File reportFile, String subject) {
		if (reportFile == null) {
			throw new IllegalStateException("No report file to send.");
		}

		Collection<String> toAddressList = SpringContext.getBean(ParameterService.class).getParameterValuesAsString(
				ExtractAchPaymentsStep.class, PdpParameterConstants.ACH_SUMMARY_TO_EMAIL_ADDRESS_PARMAETER_NAME);
		Set<String> toAddressSet = new HashSet<String>(toAddressList);

		Collection<String> ccAddressList = SpringContext.getBean(ParameterService.class).getParameterValuesAsString(
				ExtractAchPaymentsStep.class, PdpParameterConstants.ACH_SUMMARY_CC_EMAIL_ADDRESSES_PARMAETER_NAME);
		Set<String> ccAddressSet = new HashSet<String>(ccAddressList);

		MailMessage message = new MailMessage();
		message.setFromAddress(mailService.getBatchMailingList());
		message.setToAddresses(toAddressSet);
		message.setCcAddresses(ccAddressSet);
		message.setSubject(subject);
		message.setMessage("Attached is the PaymentWorks summary report.");
		message.addAttachment(reportFile);

		try {
			mailService.sendMessage(message);
		} catch (Exception e) {
			LOG.error("Failed to send summary email(" + subject + "): " + e);
		}
	}

	protected PayeeACHAccount getActivePayeeAchAccount(String payeeIdentifierTypeCode, String payeeIdNumber,
			String achTransactionType) {

		Collection<PayeeACHAccount> payeeAchAccounts = null;
		PayeeACHAccount payeeAchAccount = null;
		Map<String, Object> fieldValues = new HashMap<String, Object>();
		fieldValues.put("payeeIdentifierTypeCode", payeeIdentifierTypeCode);
		fieldValues.put("payeeIdNumber", payeeIdNumber);
		fieldValues.put("achTransactionType", achTransactionType);
		fieldValues.put("active", true);

		payeeAchAccounts = businessObjectService.findMatching(PayeeACHAccount.class, fieldValues);

		if (ObjectUtils.isNotNull(payeeAchAccounts) && !payeeAchAccounts.isEmpty()) {
			payeeAchAccount = payeeAchAccounts.iterator().next();
		}

		return payeeAchAccount;
	}

	protected String getRoutingNumber(List<PaymentWorksFieldChangeDTO> fieldChanges) {

		String routingNumber = null;

		for (PaymentWorksFieldChangeDTO fieldChange : fieldChanges) {
			if (fieldChange.getField_name().equals("Routing num")) {
				routingNumber = fieldChange.getTo_value();
				break;
			}
		}

		return routingNumber;
	}

	protected String getAccountNumber(List<PaymentWorksFieldChangeDTO> fieldChanges) {

		String accountNumber = null;

		for (PaymentWorksFieldChangeDTO fieldChange : fieldChanges) {
			if (fieldChange.getField_name().equals("Acct num")) {
				accountNumber = fieldChange.getTo_value();
				break;
			}
		}

		return accountNumber;
	}

	protected boolean checkForLockingDocument(MaintenanceDocument document) {
		String blockingDocId = SpringContext.getBean(MaintenanceDocumentService.class).getLockingDocumentId(document);
		if (StringUtils.isBlank(blockingDocId)) {
			return false;
		} else {
			return true;
		}
	}

	public DocumentService getDocumentService() {
		return documentService;
	}

	public void setDocumentService(DocumentService documentService) {
		this.documentService = documentService;
	}

	public AchBankService getAchBankService() {
		return achBankService;
	}

	public void setAchBankService(AchBankService achBankService) {
		this.achBankService = achBankService;
	}

	public MailService getMailService() {
		return mailService;
	}

	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}

	public ParameterService getParameterService() {
		return parameterService;
	}

	public void setParameterService(ParameterService parameterService) {
		this.parameterService = parameterService;
	}

	public VendorService getVendorService() {
		return vendorService;
	}

	public void setVendorService(VendorService vendorService) {
		this.vendorService = vendorService;
	}

	public BusinessObjectService getBusinessObjectService() {
		return businessObjectService;
	}

	public void setBusinessObjectService(BusinessObjectService businessObjectService) {
		this.businessObjectService = businessObjectService;
	}

}
