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
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.framework.persistence.jta.TransactionalNoValidationExceptionRollback;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.paymentworks.PaymentWorksConstants;
import edu.cornell.kfs.paymentworks.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.paymentworks.service.PaymentWorksNewVendorConversionService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksVendorService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksVendorUpdateConversionService;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksNewVendorDetailDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksVendorUpdatesDTO;

@TransactionalNoValidationExceptionRollback
public class PaymentWorksVendorServiceImpl implements PaymentWorksVendorService {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksVendorServiceImpl.class);

    protected DateTimeService dateTimeService;
    protected BusinessObjectService businessObjectService;
    protected NoteService noteService;
    protected PaymentWorksNewVendorConversionService paymentWorksNewVendorConversionService;
    protected PaymentWorksVendorUpdateConversionService paymentWorksVendorUpdateConversionService;

    @Override
    public PaymentWorksVendor savePaymentWorksVendorRecord(PaymentWorksNewVendorDetailDTO paymentWorksNewVendorDetailDTO) {
        PaymentWorksVendor paymentWorksVendor = getPaymentWorksNewVendorConversionService().createPaymentWorksVendor(paymentWorksNewVendorDetailDTO);
        paymentWorksVendor.setRequestStatus(paymentWorksNewVendorDetailDTO.getRequest_status());
        paymentWorksVendor.setProcessStatus(PaymentWorksConstants.ProcessStatus.VENDOR_REQUESTED);
        paymentWorksVendor.setTransactionType(PaymentWorksConstants.TransactionType.NEW_VENDOR);

        paymentWorksVendor = updatePaymentWorksVendor(paymentWorksVendor);

        return paymentWorksVendor;
    }

    @Override
    public PaymentWorksVendor savePaymentWorksVendorRecord(PaymentWorksVendorUpdatesDTO paymentWorksVendorUpdateDTO, String processStatus, String transactionType) {
        PaymentWorksVendor paymentWorksVendor = getPaymentWorksVendorUpdateConversionService().createPaymentWorksVendorUpdate(paymentWorksVendorUpdateDTO);
        paymentWorksVendor.setRequestStatus(paymentWorksVendorUpdateDTO.getStatus());
        paymentWorksVendor.setProcessStatus(processStatus);
        paymentWorksVendor.setTransactionType(transactionType);

        paymentWorksVendor = updatePaymentWorksVendor(paymentWorksVendor);

        return paymentWorksVendor;
    }

    @Override
    public PaymentWorksVendor savePaymentWorksVendorRecord(VendorDetail vendorDetail, String documentNumber, String transactionType) {
        PaymentWorksVendor paymentWorksVendor = getPaymentWorksNewVendorConversionService().createPaymentWorksVendor(vendorDetail, documentNumber);
        paymentWorksVendor.setRequestStatus(PaymentWorksConstants.PaymentWorksStatusText.APPROVED);
        paymentWorksVendor.setProcessStatus(PaymentWorksConstants.ProcessStatus.VENDOR_APPROVED);
        paymentWorksVendor.setTransactionType(transactionType);
        paymentWorksVendor.setVendorName(vendorDetail.getVendorName());

        paymentWorksVendor = updatePaymentWorksVendor(paymentWorksVendor);

        return paymentWorksVendor;
    }

    @Override
    public PaymentWorksVendor updatePaymentWorksVendor(PaymentWorksVendor paymentWorksVendor) {
        LOG.debug("updatePaymentWorksVendor, Entering");

        if (ObjectUtils.isNotNull(paymentWorksVendor)) {
            paymentWorksVendor.setProcessTimestamp(dateTimeService.getCurrentTimestamp());
            paymentWorksVendor = getBusinessObjectService().save(paymentWorksVendor);

            if (LOG.isDebugEnabled()) {
                LOG.debug("updatePaymentWorksVendor, saving payment works vendor with ID "
                        + paymentWorksVendor.getVendorRequestId() + " and a  request status of "
                        + paymentWorksVendor.getRequestStatus() + " and a process status of "
                        + paymentWorksVendor.getProcessStatus());
            }
        } else {
            LOG.error("updatePaymentWorksVendor a NULL paymentWorksVendor was supplied.");
        }

        return paymentWorksVendor;
    }

    @Override
    public void updatePaymentWorksVendorProcessStatusByDocumentNumber(String documentNumber, String processStatus) {
        Map<String, String> fieldValues = new HashMap<String, String>();
        fieldValues.put("documentNumber", documentNumber);

        Collection<PaymentWorksVendor> newVendors = businessObjectService.findMatching(PaymentWorksVendor.class, fieldValues);

        if (!newVendors.isEmpty()) {
            PaymentWorksVendor newVendor = newVendors.iterator().next();

            newVendor.setProcessStatus(processStatus);
            updatePaymentWorksVendor(newVendor);
        }

    }

    @Override
    public boolean isExistingPaymentWorksVendor(String vendorRequestId, String transactionType) {
        Map<String, String> fieldValues = new HashMap<String, String>();
        fieldValues.put("vendorRequestId", vendorRequestId);
        fieldValues.put("transactionType", transactionType);

        boolean isExists = getBusinessObjectService().countMatching(PaymentWorksVendor.class, fieldValues) > 0;

        if (LOG.isDebugEnabled()) {
            LOG.debug("isExistingPaymentWorksVendor, vendorRequestId: " + vendorRequestId + " transactionType: "
                    + transactionType + ".  Is it an esiting PaymentWorksVendor: " + isExists);
        }

        return isExists;
    }

    @Override
    public boolean isExistingPaymentWorksVendorByDocumentNumber(String documentNumber) {
        Map<String, String> fieldValues = new HashMap<String, String>();
        fieldValues.put("documentNumber", documentNumber);

        boolean isExists = getBusinessObjectService().countMatching(PaymentWorksVendor.class, fieldValues) > 0;

        if (LOG.isDebugEnabled()) {
            LOG.debug("isExistingPaymentWorksVendorByDocumentNumber, documentNumber: " + documentNumber
                    + ".  Is it an esiting PaymentWorksVendor: " + isExists);
        }

        return isExists;
    }

    @Override
    public PaymentWorksVendor getPaymentWorksVendorByDocumentNumber(String documentNumber) {
        Collection<PaymentWorksVendor> newVendorCollection = null;
        Map<String, String> fieldValues = new HashMap<String, String>();
        fieldValues.put("documentNumber", documentNumber);

        newVendorCollection = getBusinessObjectService().findMatching(PaymentWorksVendor.class, fieldValues);

        if (!newVendorCollection.isEmpty()) {
            return newVendorCollection.iterator().next();
        } else {
            return null;
        }

    }

    @Override
    public Collection<PaymentWorksVendor> getPaymentWorksVendorRecords(String processStatus, String requestStatus, String transactionType) {
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

        Collection<PaymentWorksVendor> newVendorCollection = getBusinessObjectService().findMatching(PaymentWorksVendor.class, fieldValues);

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

    public PaymentWorksNewVendorConversionService getPaymentWorksNewVendorConversionService() {
        return paymentWorksNewVendorConversionService;
    }

    public void setPaymentWorksNewVendorConversionService(
            PaymentWorksNewVendorConversionService paymentWorksNewVendorConversionService) {
        this.paymentWorksNewVendorConversionService = paymentWorksNewVendorConversionService;
    }

    public PaymentWorksVendorUpdateConversionService getPaymentWorksVendorUpdateConversionService() {
        return paymentWorksVendorUpdateConversionService;
    }

    public void setPaymentWorksVendorUpdateConversionService(
            PaymentWorksVendorUpdateConversionService paymentWorksVendorUpdateConversionService) {
        this.paymentWorksVendorUpdateConversionService = paymentWorksVendorUpdateConversionService;
    }

}
