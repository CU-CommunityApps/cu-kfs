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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.businessobject.ACHBank;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.pdp.service.AchBankService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.rice.kew.api.exception.WorkflowException;
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
import edu.cornell.kfs.paymentworks.service.PaymentWorksAchConversionService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksKfsService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksNewVendorConversionService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksUploadSupplierService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksUtilityService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksVendorUpdateConversionService;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksFieldChangeDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksVendorUpdatesDTO;
import edu.cornell.kfs.sys.service.MailMessage;
import edu.cornell.kfs.vnd.CUVendorConstants;

@Transactional
public class PaymentWorksKfsServiceImpl implements PaymentWorksKfsService {
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksKfsServiceImpl.class);

	protected DocumentService documentService;
	protected MailService mailService;
	protected AchBankService achBankService;
	protected ParameterService parameterService;
	protected VendorService vendorService;
	protected BusinessObjectService businessObjectService;
	protected MaintenanceDocumentService maintenanceDocumentService;
	protected PaymentWorksNewVendorConversionService paymentWorksNewVendorConversionService;
	protected PaymentWorksUtilityService paymentWorksUtilityService;
	protected PaymentWorksAchConversionService paymentWorksAchConversionService;
	protected PaymentWorksUploadSupplierService paymentWorksUploadSupplierService;
	protected PaymentWorksVendorUpdateConversionService paymentWorksVendorUpdateConversionService;

	@Override
	public boolean routeNewVendor(PaymentWorksVendor paymentWorksVendor) {
		boolean routed = false;
		VendorDetail vendorDetail = getPaymentWorksNewVendorConversionService().createVendorDetail(paymentWorksVendor);

		try {
			MaintenanceDocument vendorMaintDoc = buildVendorMaintenanceDocument(paymentWorksVendor, vendorDetail);
			boolean valid = true;
			try {
				vendorMaintDoc.validateBusinessRules(new RouteDocumentEvent(vendorMaintDoc));
			} catch (ValidationException ve) {
				LOG.error("Failed to route Vendor document due to business rule error(s): " + getPaymentWorksUtilityService()
						.getAutoPopulatingErrorMessages(GlobalVariables.getMessageMap().getErrorMessages()), ve);
				valid = false;
			}

			if (valid) {
				documentService.routeDocument(vendorMaintDoc, "", new ArrayList());
				routed = true;
				paymentWorksVendor.setDocumentNumber(vendorMaintDoc.getDocumentNumber());
			}
		} catch (Exception e) {
			LOG.error("Failed to route Vendor document due to error(s): " + e.getMessage());
		} finally {
			GlobalVariables.getMessageMap().clearErrorMessages();
		}
		return routed;
	}

	@Override
	public boolean routeVendorEdit(PaymentWorksVendor paymentWorksVendor) {
		boolean routed = false;
		String vendorNumberList = paymentWorksVendor.getVendorNumberList();
		List<String> vendorNumbers = Arrays.asList(vendorNumberList.split("\\s*,\\s*"));
		StringBuffer documentNumbers = new StringBuffer("");

		for (String vendorNumber : vendorNumbers) {
			VendorDetail oldVendor = vendorService.getVendorDetail(vendorNumber);
			VendorDetail newVendor = (VendorDetail) ObjectUtils.deepCopy(oldVendor);

			if (!getPaymentWorksVendorUpdateConversionService().duplicateFieldsOnVendor(newVendor, paymentWorksVendor)) {
				routed = processNonDuplicateVendorDetail(paymentWorksVendor, documentNumbers, oldVendor, newVendor);
			} else {
				GlobalVariables.getMessageMap().putError("vendorDetail", KFSKeyConstants.ERROR_CUSTOM, "Duplicate vendor update detected");
				routed = false;
			}
		}

		if (documentNumbers.length() > 0) {
			paymentWorksVendor.setDocumentNumberList(documentNumbers.toString().substring(0, documentNumbers.length() - 1));
		}
		return routed;
	}

	protected boolean processNonDuplicateVendorDetail(PaymentWorksVendor paymentWorksVendor,
			StringBuffer documentNumbers, VendorDetail oldVendor, VendorDetail newVendor) {
		boolean routed = false;
		VendorDetail vendorDetail = getPaymentWorksVendorUpdateConversionService().createVendorDetailForEdit(newVendor, oldVendor, paymentWorksVendor);
		try {
			MaintenanceDocument vendorMaintDoc = buildVendorMaintenanceDocument(paymentWorksVendor, oldVendor, vendorDetail);

			if (!checkForLockingDocument(vendorMaintDoc)) {
				routed = processUnlockedVendorMaintenanceDocument(documentNumbers, vendorMaintDoc);
			} else {
				LOG.error("Failed to route Vendor Edit document due to lock");
				GlobalVariables.getMessageMap().putError("vendorDetail", KFSKeyConstants.ERROR_CUSTOM, "Failed to route Vendor Edit document due to lock");
			}
		} catch (Exception e) {
			LOG.error("Failed to route Vendor document due to error(s): " + e.getMessage());
		}
		return routed;
	}
	
	protected MaintenanceDocument buildVendorMaintenanceDocument(PaymentWorksVendor paymentWorksVendor, VendorDetail vendorDetail) 
			throws WorkflowException {
		MaintenanceDocument vendorMaintDoc = buildVendorMaintenanceDocumentBase(vendorDetail, new VendorDetail(), 
				paymentWorksVendor.getRequestingCompanyName(), KFSConstants.MAINTENANCE_NEW_ACTION);
		vendorMaintDoc.getDocumentHeader().setExplanation(paymentWorksVendor.getRequestingCompanyDesc());
		return vendorMaintDoc;
	}
	
	protected MaintenanceDocument buildVendorMaintenanceDocument(PaymentWorksVendor paymentWorksVendor, VendorDetail oldVendor,
			VendorDetail vendorDetail) throws WorkflowException {
		MaintenanceDocument vendorMaintDoc = buildVendorMaintenanceDocumentBase(vendorDetail, oldVendor, 
				paymentWorksVendor.getVendorName(), KFSConstants.MAINTENANCE_EDIT_ACTION);
		vendorMaintDoc.getNewMaintainableObject().setDocumentNumber(vendorMaintDoc.getDocumentNumber());
		return vendorMaintDoc;
	}
	
	protected MaintenanceDocument buildVendorMaintenanceDocumentBase(VendorDetail newVendorDetail, VendorDetail oldVendorDetail, 
			String documentDescription, String doucmentAction) throws WorkflowException {
		MaintenanceDocument vendorMaintDoc = (MaintenanceDocument) documentService.getNewDocument(CUVendorConstants.VENDOR_DOCUMENT_TYPE_NAME);
		vendorMaintDoc.getNewMaintainableObject().setBusinessObject(newVendorDetail);
		vendorMaintDoc.getOldMaintainableObject().setBusinessObject(oldVendorDetail);
		vendorMaintDoc.getDocumentHeader().setDocumentDescription(StringUtils.defaultIfBlank(documentDescription, StringUtils.EMPTY));
		vendorMaintDoc.getNewMaintainableObject().setMaintenanceAction(doucmentAction);
		return vendorMaintDoc;
	}

	protected boolean processUnlockedVendorMaintenanceDocument(StringBuffer documentNumbers, MaintenanceDocument vendorMaintDoc) throws WorkflowException {
		boolean routed = false;
		try {
			vendorMaintDoc.validateBusinessRules(new RouteDocumentEvent(vendorMaintDoc));
			documentService.routeDocument(vendorMaintDoc, null, null);
			routed = true;
			documentNumbers.append(vendorMaintDoc.getDocumentNumber() + ",");
		} catch (ValidationException ve) {
			LOG.error("Failed to route Vendor Edit document due to business rule error(s): "
					+ getPaymentWorksUtilityService().getAutoPopulatingErrorMessages(GlobalVariables.getMessageMap().getErrorMessages()));
		}
		return routed;
	}

	@Override
	public boolean directVendorEdit(PaymentWorksVendor paymentWorksVendor) {
		boolean success = true;
		String vendorNumberList = paymentWorksVendor.getVendorNumberList();
		List<String> vendorNumbers = Arrays.asList(vendorNumberList.split("\\s*,\\s*"));

		for (String vendorNumber : vendorNumbers) {
			VendorDetail vendorDetail = vendorService.getVendorDetail(vendorNumber);
			if (ObjectUtils.isNotNull(vendorDetail)) {
				if (!getPaymentWorksVendorUpdateConversionService().duplicateFieldsOnVendor(vendorDetail, paymentWorksVendor)) {
					vendorDetail = getPaymentWorksVendorUpdateConversionService().createVendorDetailForEdit(vendorDetail, null, paymentWorksVendor);
					try {
						businessObjectService.save(vendorDetail);
					} catch (Exception e) {
						GlobalVariables.getMessageMap().putError("vendorDetail", KFSKeyConstants.ERROR_CUSTOM, e.getCause().getMessage());
						success = false;
						break;
					}
				} else {
					GlobalVariables.getMessageMap().putError("vendorDetail", KFSKeyConstants.ERROR_CUSTOM, "Duplicate vendor update detected");
					success = false;
				}
			}
		}
		return success;
	}
	
	@Override
	public boolean directAchEdit(PaymentWorksVendorUpdatesDTO vendorUpdate, String vendorNumberList) {
		boolean successful = false;
		String routingNumber = getRoutingNumber(vendorUpdate.getField_changes().getField_changes());
		String accountNumber = getAccountNumber(vendorUpdate.getField_changes().getField_changes());
		
		if (validateRoutingNumberAndAccountNumber(routingNumber, accountNumber)) {
			return successful;
		}

		List<String> vendorNumbers = Arrays.asList(vendorNumberList.split("\\s*,\\s*"));
		for (String vendorNumber : vendorNumbers) {
			PayeeACHAccount payeeAchAccount = this.getActivePayeeAchAccount(PdpConstants.PayeeIdTypeCodes.VENDOR_ID, vendorNumber, PdpConstants.DisbursementTypeCodes.ACH);
			
			if (!inactivatePayeeAchAccount(payeeAchAccount)) {
				successful = false;
				break;
			}

			if(!validatePayeeAchAccountExistence(payeeAchAccount, routingNumber, accountNumber)) {
				return successful;
			}
			
			PayeeACHAccount payeeAchAccountNew = buildPayeeAchAccountNew(vendorUpdate, routingNumber, accountNumber, vendorNumber, payeeAchAccount);
			if(!processPayeeAchAccountNew(payeeAchAccountNew, payeeAchAccount)) {
				break;
			}
			successful = true;
		}

		return successful;
	}
	
	protected boolean inactivatePayeeAchAccount(PayeeACHAccount payeeAchAccount) {
		if (ObjectUtils.isNotNull(payeeAchAccount)) {
			payeeAchAccount.setActive(false);
			try {
				payeeAchAccount = businessObjectService.save(payeeAchAccount);
			} catch (Exception e) {
				GlobalVariables.getMessageMap().putError("payeeAchAccount", KFSKeyConstants.ERROR_CUSTOM, e.getCause().getMessage());
				return false;
			}
		}
		return true;
	}
	
	protected boolean processPayeeAchAccountNew(PayeeACHAccount payeeAchAccountNew, PayeeACHAccount payeeAchAccount) {
		boolean successful = true;
		try {
			payeeAchAccountNew = businessObjectService.save(payeeAchAccountNew);
		} catch (Exception e) {
			GlobalVariables.getMessageMap().putError("payeeAchAccount", KFSKeyConstants.ERROR_CUSTOM, e.getCause().getMessage());
			if (ObjectUtils.isNotNull(payeeAchAccount)) {
				payeeAchAccount.setActive(true);
				payeeAchAccount = businessObjectService.save(payeeAchAccount);
			}
			successful = false;
		}
		return successful;
	}

	protected PayeeACHAccount buildPayeeAchAccountNew(PaymentWorksVendorUpdatesDTO vendorUpdate, String routingNumber,
			String accountNumber, String vendorNumber, PayeeACHAccount payeeAchAccount) {
		PayeeACHAccount payeeAchAccountNew;
		if (ObjectUtils.isNull(payeeAchAccount)) {
			payeeAchAccountNew = getPaymentWorksAchConversionService().createPayeeAchAccount(vendorUpdate, vendorNumber);
		} else {
			payeeAchAccountNew = getPaymentWorksAchConversionService().createPayeeAchAccount(payeeAchAccount, routingNumber, accountNumber);
		}
		return payeeAchAccountNew;
	}
	
	protected boolean validatePayeeAchAccountExistence(PayeeACHAccount payeeAchAccount, String routingNumber, String accountNumber) {
		boolean valid = true;
		if (ObjectUtils.isNull(payeeAchAccount) && (StringUtils.isBlank(routingNumber) || StringUtils.isBlank(accountNumber))) {
			GlobalVariables.getMessageMap().putError("payeeAchAccount", KFSKeyConstants.ERROR_CUSTOM,
					"No existing Payee ACH Account, routing number and account number required");
			valid = false;
		}
		return valid;
	}
	
	protected boolean validateRoutingNumberAndAccountNumber(String routingNumber, String accountNumber) {
		boolean valid = true;
		if (StringUtils.isNotBlank(routingNumber)) {
			ACHBank achBank = achBankService.getByPrimaryId(routingNumber);
			if (ObjectUtils.isNull(achBank)) {
				GlobalVariables.getMessageMap().putError("payeeAchAccount", KFSKeyConstants.ERROR_CUSTOM, "ACH Bank does not exist for routing number");
				valid = false;
			}
		} else if (StringUtils.isBlank(accountNumber)) {
			GlobalVariables.getMessageMap().putError("payeeAchAccount", KFSKeyConstants.ERROR_CUSTOM, "Routing number or account number required");
			valid = false;
		}
		return valid;
	}

	@Override
	public void sendVendorInitiatedEmail(String documentNumber, String vendorName, String contactEmail) {
		String fromAddress = parameterService.getParameterValueAsString(PaymentWorksRetrieveNewVendorStep.class,
				PaymentWorksConstants.PaymentWorksParameters.VENDOR_INITIATED_EMAIL_FROM_ADDRESS);
		String subject = parameterService.getParameterValueAsString(PaymentWorksRetrieveNewVendorStep.class,
				PaymentWorksConstants.PaymentWorksParameters.VENDOR_INITIATED_EMAIL_SUBJECT);
		String body = parameterService.getParameterValueAsString(PaymentWorksRetrieveNewVendorStep.class, 
				PaymentWorksConstants.PaymentWorksParameters.VENDOR_INITIATED_EMAIL_BODY);

		MailMessage message = new MailMessage();
		message.setFromAddress(fromAddress);
		message.getToAddresses().add(contactEmail);
		message.setSubject(subject);
		message.setMessage(body);

		try {
			mailService.sendMessage(message);
		} catch (Exception e) {
			LOG.error("Failed to send vendor initiated email: " + e);
		}
	}

	@Override
	public void sendVendorApprovedEmail(String vendorNumber, String contactEmail, String vendorName) {
		String fromAddress = parameterService.getParameterValueAsString(PaymentWorksUploadSuppliersStep.class,
				PaymentWorksConstants.PaymentWorksParameters.VENDOR_APPROVED_EMAIL_FROM_ADRESS);
		String subject = parameterService.getParameterValueAsString(PaymentWorksUploadSuppliersStep.class,
				PaymentWorksConstants.PaymentWorksParameters.VENDOR_APPROVED_EMAIL_SUBJECT);
		String body = parameterService.getParameterValueAsString(PaymentWorksUploadSuppliersStep.class, 
				PaymentWorksConstants.PaymentWorksParameters.VENDOR_APPROVED_EMAIL_BODY);
		body = StringUtils.replace(body, "[VENDOR_NUMBER]", vendorNumber);
		body = StringUtils.replace(body, "[VENDOR_NAME]", vendorName);

		MailMessage message = new MailMessage();
		message.setFromAddress(fromAddress);
		message.getToAddresses().add(contactEmail);
		message.setSubject(subject);
		message.setMessage(body);

		try {
			mailService.sendMessage(message);
		} catch (Exception e) {
			LOG.error("Failed to send vendor approved email: " + e);
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
			if (fieldChange.getField_name().equals(PaymentWorksConstants.FieldNames.ROUTING_NUMBER)) {
				routingNumber = fieldChange.getTo_value();
				break;
			}
		}
		return routingNumber;
	}

	protected String getAccountNumber(List<PaymentWorksFieldChangeDTO> fieldChanges) {
		String accountNumber = null;
		for (PaymentWorksFieldChangeDTO fieldChange : fieldChanges) {
			if (fieldChange.getField_name().equals(PaymentWorksConstants.FieldNames.ACCOUNT_NUMBER)) {
				accountNumber = fieldChange.getTo_value();
				break;
			}
		}
		return accountNumber;
	}

	protected boolean checkForLockingDocument(MaintenanceDocument document) {
		String blockingDocId = getMaintenanceDocumentService().getLockingDocumentId(document);
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

	public MaintenanceDocumentService getMaintenanceDocumentService() {
		return maintenanceDocumentService;
	}

	public void setMaintenanceDocumentService(MaintenanceDocumentService maintenanceDocumentService) {
		this.maintenanceDocumentService = maintenanceDocumentService;
	}

	public PaymentWorksNewVendorConversionService getPaymentWorksNewVendorConversionService() {
		return paymentWorksNewVendorConversionService;
	}

	public void setPaymentWorksNewVendorConversionService(
			PaymentWorksNewVendorConversionService paymentWorksNewVendorConversionService) {
		this.paymentWorksNewVendorConversionService = paymentWorksNewVendorConversionService;
	}

	public PaymentWorksUtilityService getPaymentWorksUtilityService() {
		return paymentWorksUtilityService;
	}

	public void setPaymentWorksUtilityService(PaymentWorksUtilityService paymentWorksUtilityService) {
		this.paymentWorksUtilityService = paymentWorksUtilityService;
	}

	public PaymentWorksAchConversionService getPaymentWorksAchConversionService() {
		return paymentWorksAchConversionService;
	}

	public void setPaymentWorksAchConversionService(PaymentWorksAchConversionService paymentWorksAchConversionService) {
		this.paymentWorksAchConversionService = paymentWorksAchConversionService;
	}

	public PaymentWorksUploadSupplierService getPaymentWorksUploadSupplierService() {
		return paymentWorksUploadSupplierService;
	}

	public void setPaymentWorksUploadSupplierService(PaymentWorksUploadSupplierService paymentWorksUploadSupplierService) {
		this.paymentWorksUploadSupplierService = paymentWorksUploadSupplierService;
	}

	public PaymentWorksVendorUpdateConversionService getPaymentWorksVendorUpdateConversionService() {
		return paymentWorksVendorUpdateConversionService;
	}

	public void setPaymentWorksVendorUpdateConversionService(
			PaymentWorksVendorUpdateConversionService paymentWorksVendorUpdateConversionService) {
		this.paymentWorksVendorUpdateConversionService = paymentWorksVendorUpdateConversionService;
	}

}
