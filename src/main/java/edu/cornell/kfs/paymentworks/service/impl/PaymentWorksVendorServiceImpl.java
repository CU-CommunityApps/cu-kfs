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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.kfs.kns.maintenance.Maintainable;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.paymentworks.PaymentWorksConstants;
import edu.cornell.kfs.paymentworks.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.paymentworks.service.PaymentWorksNewVendorConversionService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksVendorService;
import edu.cornell.kfs.paymentworks.util.PaymentWorksVendorUpdateConversionUtil;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksNewVendorDetailDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksVendorUpdatesDTO;

@Transactional
public class PaymentWorksVendorServiceImpl implements PaymentWorksVendorService {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(PaymentWorksVendorServiceImpl.class);

	private DateTimeService dateTimeService;
	private BusinessObjectService businessObjectService;
	private NoteService noteService;
	private PaymentWorksNewVendorConversionService paymentWorksNewVendorConversionService;

	@Override
	public PaymentWorksVendor savePaymentWorksVendorRecord(PaymentWorksNewVendorDetailDTO paymentWorksNewVendorDetailDTO) {

		PaymentWorksVendor paymentWorksNewVendor = getPaymentWorksNewVendorConversionService().createPaymentWorksVendor(paymentWorksNewVendorDetailDTO);

		// other
		paymentWorksNewVendor.setRequestStatus(paymentWorksNewVendorDetailDTO.getRequest_status());
		paymentWorksNewVendor.setProcessStatus(PaymentWorksConstants.ProcessStatus.VENDOR_REQUESTED);
		paymentWorksNewVendor.setTransactionType(PaymentWorksConstants.TransactionType.NEW_VENDOR);

		// create business object
		paymentWorksNewVendor = this.updatePaymentWorksVendor(paymentWorksNewVendor);

		return paymentWorksNewVendor;
	}

	@Override
	public PaymentWorksVendor savePaymentWorksVendorRecord(PaymentWorksVendorUpdatesDTO paymentWorksVendorUpdateDTO,
			String processStatus, String transactionType) {
		PaymentWorksVendor paymentWorksVendorUpdate = new PaymentWorksVendorUpdateConversionUtil()
				.createPaymentWorksVendorUpdate(paymentWorksVendorUpdateDTO);

		// other
		paymentWorksVendorUpdate.setRequestStatus(paymentWorksVendorUpdateDTO.getStatus());
		paymentWorksVendorUpdate.setProcessStatus(processStatus);
		paymentWorksVendorUpdate.setTransactionType(transactionType);

		// create business object
		paymentWorksVendorUpdate = this.updatePaymentWorksVendor(paymentWorksVendorUpdate);

		return paymentWorksVendorUpdate;
	}

	@Override
	public PaymentWorksVendor savePaymentWorksVendorRecord(VendorDetail vendorDetail, String documentNumber,
			String transactionType) {

		PaymentWorksVendor newVendor = getPaymentWorksNewVendorConversionService().createPaymentWorksVendor(vendorDetail, documentNumber);

		newVendor.setRequestStatus(PaymentWorksConstants.PaymentWorksStatusText.APPROVED);
		newVendor.setProcessStatus(PaymentWorksConstants.ProcessStatus.VENDOR_APPROVED);
		newVendor.setTransactionType(transactionType);
		newVendor.setVendorName(vendorDetail.getVendorName());

		// create business object
		newVendor = this.updatePaymentWorksVendor(newVendor);

		return newVendor;
	}

	@Override
	public PaymentWorksVendor updatePaymentWorksVendor(PaymentWorksVendor newVendor) {

		if (ObjectUtils.isNotNull(newVendor)) {
			newVendor.setProcessTimestamp(dateTimeService.getCurrentTimestamp());
			newVendor = businessObjectService.save(newVendor);
		}

		return newVendor;
	}

	@Override
	public void updatePaymentWorksVendorProcessStatusByDocumentNumber(String documentNumber, String processStatus) {

		Map<String, String> fieldValues = new HashMap<String, String>();
		fieldValues.put("documentNumber", documentNumber);

		Collection<PaymentWorksVendor> newVendors = businessObjectService.findMatching(PaymentWorksVendor.class,
				fieldValues);

		if (!newVendors.isEmpty()) {
			PaymentWorksVendor newVendor = newVendors.iterator().next();

			newVendor.setProcessStatus(processStatus);

			// update
			this.updatePaymentWorksVendor(newVendor);
		}

	}

	@Override
	public boolean isExistingPaymentWorksVendor(String vendorRequestId, String transactionType) {
		boolean isExists = false;

		Map<String, String> fieldValues = new HashMap<String, String>();
		fieldValues.put("vendorRequestId", vendorRequestId);
		fieldValues.put("transactionType", transactionType);

		if (businessObjectService.countMatching(PaymentWorksVendor.class, fieldValues) > 0) {
			isExists = true;
		}

		return isExists;
	}

	@Override
	public boolean isExistingPaymentWorksVendorByDocumentNumber(String documentNumber) {
		boolean isExists = false;

		Map<String, String> fieldValues = new HashMap<String, String>();
		fieldValues.put("documentNumber", documentNumber);

		if (businessObjectService.countMatching(PaymentWorksVendor.class, fieldValues) > 0) {
			isExists = true;
		}

		return isExists;
	}

	@Override
	public PaymentWorksVendor getPaymentWorksVendorByDocumentNumber(String documentNumber) {
		Collection<PaymentWorksVendor> newVendorCollection = null;
		Map<String, String> fieldValues = new HashMap<String, String>();
		fieldValues.put("documentNumber", documentNumber);

		newVendorCollection = businessObjectService.findMatching(PaymentWorksVendor.class, fieldValues);

		if (!newVendorCollection.isEmpty()) {
			return newVendorCollection.iterator().next();
		} else {
			return null;
		}

	}

	@Override
	public Collection<PaymentWorksVendor> getPaymentWorksVendorRecords(String processStatus, String requestStatus,
			String transactionType) {
		Collection<PaymentWorksVendor> newVendorCollection = null;
		Map<String, String> fieldValues = new HashMap<String, String>();

		if (StringUtils.isNotEmpty(processStatus)) {
			fieldValues.put("processStatus", processStatus);
		}

		if (StringUtils.isNotEmpty(requestStatus)) {
			fieldValues.put("requestStatus", requestStatus);
		}

		if (StringUtils.isNotEmpty(transactionType)) {
			fieldValues.put("transactionType", transactionType);
		}

		newVendorCollection = businessObjectService.findMatching(PaymentWorksVendor.class, fieldValues);

		return newVendorCollection;
	}

	@Override
	public boolean isVendorUpdateEligibleForRouting(PaymentWorksVendor paymentWorksVendor) {

		boolean isEligibleForRouting = false;

		if (StringUtils.equals(paymentWorksVendor.getGroupName(), "Company")
				&& (StringUtils.isNotBlank(paymentWorksVendor.getRequestingCompanyTin())
						|| StringUtils.isNotBlank(paymentWorksVendor.getRequestingCompanyTinType())
						|| StringUtils.isNotBlank(paymentWorksVendor.getRequestingCompanyLegalName())
						|| StringUtils.isNotBlank(paymentWorksVendor.getRequestingCompanyTaxClassificationCode())
						|| StringUtils.isNotBlank(paymentWorksVendor.getRequestingCompanyTaxCountry()))) {

			isEligibleForRouting = true;
		}

		return isEligibleForRouting;
	}

	protected void addNoteForNewVendorResponsesToVendor(Maintainable maintainable) {
		Note newBONote = new Note();
		newBONote.setNoteText("Test Note: Add detail");
		try {

			newBONote = noteService.createNote(newBONote, maintainable.getBusinessObject(),
					GlobalVariables.getUserSession().getPrincipalId());
			newBONote.setNotePostedTimestampToCurrent();
		} catch (Exception e) {
			throw new RuntimeException("Caught Exception While Trying To Add Note to Vendor", e);
		}
		List<Note> noteList = noteService.getByRemoteObjectId(maintainable.getBusinessObject().getObjectId());
		noteList.add(newBONote);
		noteService.saveNoteList(noteList);
	}

	public DateTimeService getDateTimeService() {
		return dateTimeService;
	}

	public void setDateTimeService(DateTimeService dateTimeService) {
		this.dateTimeService = dateTimeService;
	}

	public BusinessObjectService getBusinessObjectService() {
		return businessObjectService;
	}

	public void setBusinessObjectService(BusinessObjectService businessObjectService) {
		this.businessObjectService = businessObjectService;
	}

	public NoteService getNoteService() {
		return noteService;
	}

	public void setNoteService(NoteService noteService) {
		this.noteService = noteService;
	}

	public PaymentWorksNewVendorConversionService getPaymentWorksNewVendorConversionService() {
		return paymentWorksNewVendorConversionService;
	}

	public void setPaymentWorksNewVendorConversionService(
			PaymentWorksNewVendorConversionService paymentWorksNewVendorConversionService) {
		this.paymentWorksNewVendorConversionService = paymentWorksNewVendorConversionService;
	}

}
