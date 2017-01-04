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

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.paymentworks.PaymentWorksConstants;
import edu.cornell.kfs.paymentworks.service.PaymentWorksAchConversionService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksUtilityService;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksVendorUpdatesDTO;

public class PaymentWorksAchConversionServiceImpl implements PaymentWorksAchConversionService {
	
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksAchConversionServiceImpl.class);
	
	protected PaymentWorksUtilityService paymentWorksUtilityService;
	protected DateTimeService dateTimeService;
	
	@Override
	public PayeeACHAccount createPayeeAchAccount(PaymentWorksVendorUpdatesDTO vendorUpdate, String vendorNumber) {
		PayeeACHAccount payeeAchAccount = new PayeeACHAccount();
		Map<String, String> fieldChanges = getPaymentWorksUtilityService().convertFieldArrayToMap(vendorUpdate.getField_changes());
		logFieldChanges(fieldChanges);
		payeeAchAccount.setPayeeIdentifierTypeCode(PdpConstants.PayeeIdTypeCodes.VENDOR_ID);
		payeeAchAccount.setPayeeIdNumber(vendorNumber);
		payeeAchAccount.setBankRoutingNumber(fieldChanges.get(PaymentWorksConstants.FieldNames.ROUTING_NUMBER));
		payeeAchAccount.setBankAccountNumber(fieldChanges.get(PaymentWorksConstants.FieldNames.ACCOUNT_NUMBER));
		payeeAchAccount.setBankAccountTypeCode(PdpConstants.ACH_TRANSACTION_TYPE_DEFAULT);
		payeeAchAccount.setAchTransactionType(PdpConstants.DisbursementTypeCodes.ACH);
		payeeAchAccount.setActive(true);
		payeeAchAccount.setLastUpdateUserId(PaymentWorksConstants.SOURCE_USER);
		payeeAchAccount.setLastUpdate(getDateTimeService().getCurrentTimestamp());
		return payeeAchAccount;
	}
	
	private void logFieldChanges(Map<String, String> fieldChanges) {
		if (LOG.isDebugEnabled()) {
			for (String key : fieldChanges.keySet()) {
				LOG.debug("logFieldChanges key: '" + key + "'  value: '" + fieldChanges.get(key) + "'");
			}
		}
	}

	@Override
	public PayeeACHAccount createPayeeAchAccount(PayeeACHAccount payeeAchAccountOld, String routingNumber, String accountNumber) {
		PayeeACHAccount payeeAchAccount = (PayeeACHAccount) ObjectUtils.deepCopy(payeeAchAccountOld);
		payeeAchAccount.setBankRoutingNumber(StringUtils.defaultIfEmpty(routingNumber, payeeAchAccount.getBankRoutingNumber()));
		payeeAchAccount.setBankAccountNumber(StringUtils.defaultIfEmpty(accountNumber, payeeAchAccount.getBankAccountNumber()));
		payeeAchAccount.setObjectId(null);
		payeeAchAccount.setVersionNumber(null);
		payeeAchAccount.setAchAccountGeneratedIdentifier(null);
		payeeAchAccount.setActive(true);
		payeeAchAccount.setLastUpdateUserId(PaymentWorksConstants.SOURCE_USER);
		payeeAchAccount.setLastUpdate(getDateTimeService().getCurrentTimestamp());
		return payeeAchAccount;
	}

	public PaymentWorksUtilityService getPaymentWorksUtilityService() {
		return paymentWorksUtilityService;
	}

	public void setPaymentWorksUtilityService(PaymentWorksUtilityService paymentWorksUtilityService) {
		this.paymentWorksUtilityService = paymentWorksUtilityService;
	}

	public DateTimeService getDateTimeService() {
		return dateTimeService;
	}

	public void setDateTimeService(DateTimeService dateTimeService) {
		this.dateTimeService = dateTimeService;
	}

}
