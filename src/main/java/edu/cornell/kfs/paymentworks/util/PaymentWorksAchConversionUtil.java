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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpParameterConstants;
import org.kuali.kfs.pdp.batch.ExtractAchPaymentsStep;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.paymentworks.PaymentWorksConstants;
import edu.cornell.kfs.paymentworks.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksVendorUpdatesDTO;

public class PaymentWorksAchConversionUtil {

	public PayeeACHAccount createPayeeAchAccount(PaymentWorksVendorUpdatesDTO vendorUpdate, String vendorNumber) {

		PayeeACHAccount payeeAchAccount = new PayeeACHAccount();
		// PayeeACHAccountExtension payeeAchAccountExtension = new
		// PayeeACHAccountExtension();

		Map<String, String> fieldChanges = new PaymentWorksUtil()
				.convertFieldArrayToMap(vendorUpdate.getField_changes());

		payeeAchAccount.setPayeeIdentifierTypeCode(PdpConstants.PayeeIdTypeCodes.VENDOR_ID);
		payeeAchAccount.setPayeeIdNumber(vendorNumber);
		payeeAchAccount.setBankRoutingNumber(fieldChanges.get("Routing num"));
		payeeAchAccount.setBankAccountNumber(fieldChanges.get("Acct num"));
		payeeAchAccount.setBankAccountTypeCode(PdpConstants.ACH_TRANSACTION_TYPE_DEFAULT);

		payeeAchAccount.setPayeeEmailAddress(getAchEmailAddress(vendorNumber));

		payeeAchAccount.setAchTransactionType(PdpConstants.DisbursementTypeCodes.ACH);
		payeeAchAccount.setActive(true);
		payeeAchAccount.setLastUpdateUserId(PaymentWorksConstants.SOURCE_USER);
		payeeAchAccount.setLastUpdate(SpringContext.getBean(DateTimeService.class).getCurrentTimestamp());

		// payeeAchAccountExtension.setBypassFeedUpdateIndicator(false);
		// payeeAchAccountExtension.setPdpPayeeACHAcctUpdateSource(PmwConstants.SOURCE_USER);
		// payeeAchAccountExtension.setPdpPayeeACHAcctLastUpdate(SpringContext.getBean(DateTimeService.class).getCurrentTimestamp());

		// payeeAchAccount.setExtension(payeeAchAccountExtension);

		return payeeAchAccount;
	}

	/**
	 * First checks staging table, then grabs from parameter
	 *
	 * @param vendorNumber
	 * @return
	 */
	protected String getAchEmailAddress(String vendorNumber) {
		String achEmailAddress = null;

		// split vendor number if not null and get latest ach email address from
		// staging table
		if (StringUtils.isNotEmpty(vendorNumber)) {
			String[] vendorNumberParts = StringUtils.split(vendorNumber, "-");
			String headerId = vendorNumberParts[0];
			String detailId = vendorNumberParts[1];

			Map<String, String> fieldValues = new HashMap<String, String>();
			fieldValues.put("vendorHeaderGeneratedIdentifier", headerId);
			fieldValues.put("vendorDetailAssignedIdentifier", detailId);
			fieldValues.put("transactionType", PaymentWorksConstants.TransactionType.NEW_VENDOR);

			Collection<PaymentWorksVendor> newVendors = SpringContext.getBean(BusinessObjectService.class)
					.findMatchingOrderBy(PaymentWorksVendor.class, fieldValues, "processTimestamp", false);
			PaymentWorksVendor vendor = null;

			if (ObjectUtils.isNotNull(newVendors) && !newVendors.isEmpty()) {
				vendor = newVendors.iterator().next();
				achEmailAddress = vendor.getAchEmailAddress();
			}

		}

		// if still null, try param
		if (ObjectUtils.isNull(achEmailAddress)) {
			List<String> toAddressList = new ArrayList<String>(SpringContext.getBean(ParameterService.class)
					.getParameterValuesAsString(ExtractAchPaymentsStep.class,
							PdpParameterConstants.ACH_SUMMARY_TO_EMAIL_ADDRESS_PARMAETER_NAME));
			if (ObjectUtils.isNotNull(toAddressList) && !toAddressList.isEmpty()) {
				achEmailAddress = toAddressList.get(0);
			}
		}

		return achEmailAddress;
	}

	public PayeeACHAccount createPayeeAchAccount(PayeeACHAccount payeeAchAccountOld, String routingNumber,
			String accountNumber) {

		PayeeACHAccount payeeAchAccount = (PayeeACHAccount) ObjectUtils.deepCopy(payeeAchAccountOld);
		// PayeeACHAccountExtension payeeAchAccountExtension = new
		// PayeeACHAccountExtension();

		payeeAchAccount.setBankRoutingNumber(
				StringUtils.defaultIfEmpty(routingNumber, payeeAchAccount.getBankRoutingNumber()));
		payeeAchAccount.setBankAccountNumber(
				StringUtils.defaultIfEmpty(accountNumber, payeeAchAccount.getBankAccountNumber()));

		payeeAchAccount.setObjectId(null);
		payeeAchAccount.setVersionNumber(null);
		payeeAchAccount.setAchAccountGeneratedIdentifier(null);

		payeeAchAccount.setActive(true);
		payeeAchAccount.setLastUpdateUserId(PaymentWorksConstants.SOURCE_USER);
		payeeAchAccount.setLastUpdate(SpringContext.getBean(DateTimeService.class).getCurrentTimestamp());

		// payeeAchAccountExtension.setBypassFeedUpdateIndicator(false);
		// payeeAchAccountExtension.setPdpPayeeACHAcctUpdateSource(PmwConstants.SOURCE_USER);
		// payeeAchAccountExtension.setPdpPayeeACHAcctLastUpdate(SpringContext.getBean(DateTimeService.class).getCurrentTimestamp());

		// payeeAchAccount.setExtension(payeeAchAccountExtension);

		return payeeAchAccount;
	}

}
