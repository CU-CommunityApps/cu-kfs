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
package edu.cornell.kfs.paymentworks;

public class PaymentWorksConstants {

	public static final String PAYMENT_WORKS_VENDOR_SEQ = "PAYMENT_WORKS_VENDOR_SEQ";

	public static final String SUPPLIER_FILE_HEADER_ROW = "VendorNum,SiteCode,SupplierName,Address1,Address2,City,State,Country,Zipcode,TIN,ContactEmail";
	public static final String SUPPLIER_FILE_NAME = "suppliers";
	public static final String DOUBLE_QUOTE = "\"";

	public static final String SOURCE_USER = "PaymentWorks";

	public static class EmailParameters {
		public static final String PAYMENT_WORKS_VENDOR_INITIATED_EMAIL_SUBJECT = "PAYMENT_WORKS_VENDOR_INITIATED_EMAIL_SUBJECT";
		public static final String PAYMENT_WORKS_VENDOR_INITIATED_EMAIL = "PAYMENT_WORKS_VENDOR_INITIATED_EMAIL";
		public static final String PAYMENT_WORKS_VENDOR_APPROVED_EMAIL_SUBJECT = "PAYMENT_WORKS_VENDOR_APPROVED_EMAIL_SUBJECT";
		public static final String PAYMENT_WORKS_VENDOR_APPROVED_EMAIL = "PAYMENT_WORKS_VENDOR_APPROVED_EMAIL";
	}

	public static class VendorUpdateGroups {
		public static final String COMPANY = "Company";
		public static final String CORP_ADDRESS = "Corporate Address";
		public static final String REMIT_ADDRESS = "Remittance Address";
	}

	public static class PaymentWorksNewVendorStatus {
		public static final String PENDING = "0";
		public static final String APPROVED = "1";
		public static final String PROCESSED = "2";
		public static final String REJECTED = "4";
	}

	public static class PaymentWorksUpdateStatus {
		public static final String PENDING = "0";
		public static final String PROCESSED = "3";
	}

	public static class PaymentWorksStatusText {
		public static final String PENDING = "Pending";
		public static final String APPROVED = "Approved";
		public static final String PROCESSED = "Processed";
		public static final String REJECTED = "Rejected";
	}

	public static class ProcessStatus {
		public static final String VENDOR_REQUESTED = "Vendor Requested";
		public static final String VENDOR_CREATED = "Vendor Created";
		public static final String VENDOR_REJECTED = "Vendor Rejected";
		public static final String VENDOR_APPROVED = "Vendor Approved";
		public static final String VENDOR_DISAPPROVED = "Vendor Disapproved";

		public static final String VENDOR_UPDATE_CREATED = "Vendor Update Created";
		public static final String VENDOR_UPDATE_COMPLETE = "Vendor Update Complete";
		public static final String VENDOR_UPDATE_REJECTED = "Vendor Update Rejected";

		public static final String ACH_UPDATE_REJECTED = "Ach Update Rejected";
		public static final String ACH_UPDATE_COMPLETE = "Ach Update Complete";

		public static final String SUPPLIER_UPLOADED = "Supplier Uploaded";
		public static final String SUPPLIER_UPLOAD_FAILED = "Supplier Upload Failed";
	}

	public static class TransactionType {
		public static final String NEW_VENDOR = "NV";
		public static final String VENDOR_UPDATE = "VU";
		public static final String ACH_UPDATE = "ACH";
	}

	public static class SupplierUploadSummaryTypes {
		public static final String KFS_NEW_VENDORS = "KFS_NEW_VENDORS";
		public static final String PAYMENT_WORKS_NEW_VENDORS = "PAYMENT_WORKS_NEW_VENDORS";
		public static final String DISAPPROVED_VENDORS = "DISAPPROVED_VENDORS";
		public static final String VENDOR_UPDATES = "VENDOR_UPDATES";
	}
}
