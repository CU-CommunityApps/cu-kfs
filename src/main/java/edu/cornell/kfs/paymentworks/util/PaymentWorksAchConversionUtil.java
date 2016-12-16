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
package edu.cornell.kfs.paymentworks.util;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.paymentworks.PaymentWorksConstants;
import edu.cornell.kfs.paymentworks.service.PaymentWorksUtilityService;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksVendorUpdatesDTO;

public class PaymentWorksAchConversionUtil {
	
	PaymentWorksUtilityService paymentWorksUtilityService;

	public PayeeACHAccount createPayeeAchAccount(PaymentWorksVendorUpdatesDTO vendorUpdate, String vendorNumber) {
		PayeeACHAccount payeeAchAccount = new PayeeACHAccount();
		Map<String, String> fieldChanges = getPaymentWorksUtilityService().convertFieldArrayToMap(vendorUpdate.getField_changes());

		payeeAchAccount.setPayeeIdentifierTypeCode(PdpConstants.PayeeIdTypeCodes.VENDOR_ID);
		payeeAchAccount.setPayeeIdNumber(vendorNumber);
		payeeAchAccount.setBankRoutingNumber(fieldChanges.get(PaymentWorksConstants.FieldNames.ROUTING_NUMBER));
		payeeAchAccount.setBankAccountNumber(fieldChanges.get(PaymentWorksConstants.FieldNames.ACCOUNT_NUMBER));
		payeeAchAccount.setBankAccountTypeCode(PdpConstants.ACH_TRANSACTION_TYPE_DEFAULT);
		payeeAchAccount.setAchTransactionType(PdpConstants.DisbursementTypeCodes.ACH);
		payeeAchAccount.setActive(true);
		payeeAchAccount.setLastUpdateUserId(PaymentWorksConstants.SOURCE_USER);
		payeeAchAccount.setLastUpdate(SpringContext.getBean(DateTimeService.class).getCurrentTimestamp());
		return payeeAchAccount;
	}

	public PayeeACHAccount createPayeeAchAccount(PayeeACHAccount payeeAchAccountOld, String routingNumber, String accountNumber) {
		PayeeACHAccount payeeAchAccount = (PayeeACHAccount) ObjectUtils.deepCopy(payeeAchAccountOld);
		payeeAchAccount.setBankRoutingNumber(StringUtils.defaultIfEmpty(routingNumber, payeeAchAccount.getBankRoutingNumber()));
		payeeAchAccount.setBankAccountNumber(StringUtils.defaultIfEmpty(accountNumber, payeeAchAccount.getBankAccountNumber()));
		payeeAchAccount.setObjectId(null);
		payeeAchAccount.setVersionNumber(null);
		payeeAchAccount.setAchAccountGeneratedIdentifier(null);
		payeeAchAccount.setActive(true);
		payeeAchAccount.setLastUpdateUserId(PaymentWorksConstants.SOURCE_USER);
		payeeAchAccount.setLastUpdate(SpringContext.getBean(DateTimeService.class).getCurrentTimestamp());
		return payeeAchAccount;
	}

	public PaymentWorksUtilityService getPaymentWorksUtilityService() {
		if (paymentWorksUtilityService == null) {
			paymentWorksUtilityService = SpringContext.getBean(PaymentWorksUtilityService.class);
		}
		return paymentWorksUtilityService;
	}

	public void setPaymentWorksUtilityService(PaymentWorksUtilityService paymentWorksUtilityService) {
		this.paymentWorksUtilityService = paymentWorksUtilityService;
	}

}
